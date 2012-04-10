package com.google.enterprise.connector.adgroups;

import com.google.enterprise.connector.adgroups.AdConstants.Method;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

public class AdServer {
  private static final Logger LOGGER = Logger.getLogger(AdServer.class.getName());

  protected LdapContext ldapContext = null;
  private SearchControls searchCtls;
  private Control[] resultControls;

  // properties necessary for connection
  private String hostName;
  private int port;
  private String principal;
  private String password;

  // retrieved properties of the Active Directory controller
  private String nETBIOSName;
  private Method connectMethod;
  private String dn;
  private String configurationNamingContext;
  private String dsServiceName;
  private String sid;
  private int highestCommittedUSN;
  private String invocationID;
  private String dnsRoot;

  public AdServer(
      Method connectMethod,
      String hostName,
      int port,
      String principal,
      String password) {
    this.hostName = hostName;
    this.port = port;
    this.principal = principal;
    this.password = password;
    searchCtls = new SearchControls();
    searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    this.connectMethod = connectMethod;
  }

  /**
   * Connects to the Active Directory server and retrieves AD configuration
   * information.
   */
  public void connect() throws CommunicationException, NamingException {
    if (ldapContext == null) {
      Hashtable<String, String> env = new Hashtable<String, String>();

      // Use the built-in LDAP support.
      env.put(Context.INITIAL_CONTEXT_FACTORY,
          AdConstants.COM_SUN_JNDI_LDAP_LDAP_CTX_FACTORY);
      env.put(Context.SECURITY_AUTHENTICATION,
          AdConstants.AUTHN_TYPE_SIMPLE);
      env.put(Context.SECURITY_PRINCIPAL, principal);
      env.put(Context.SECURITY_CREDENTIALS, password);

      String ldapUrl =
          connectMethod.toString() + hostName + AdConstants.COLON + port;
      LOGGER.info("LDAP provider url: " + ldapUrl);
      env.put(Context.PROVIDER_URL, ldapUrl);

      ldapContext = new InitialLdapContext(env, null);
      Attributes attributes = ldapContext.getAttributes(AdConstants.EMPTY);
      dn = attributes.get(
          AdConstants.ATTR_DEFAULTNAMINGCONTEXT).get(0).toString();
      dsServiceName = attributes.get(
          AdConstants.ATTR_DSSERVICENAME).get(0).toString();
      highestCommittedUSN = Integer.parseInt(attributes.get(
          AdConstants.ATTR_HIGHESTCOMMITTEDUSN).get(0).toString());
      configurationNamingContext = attributes.get(
          AdConstants.ATTR_CONFIGURATIONNAMINGCONTEXT).get(0).toString();
      sid = AdEntity.getTextSid((byte[])get(
          AdConstants.ATTR_DISTINGUISHEDNAME + AdConstants.EQUALS + dn,
          AdConstants.ATTR_OBJECTSID, dn));
      invocationID = AdEntity.getTextGuid((byte[]) get(
          AdConstants.ATTR_DISTINGUISHEDNAME
          + AdConstants.EQUALS + dsServiceName,
          AdConstants.ATTR_INVOCATIONID,
          dsServiceName));
    }
  }

  public void initialize() {
    try {
      connect();
    } catch (CommunicationException e) {
      LOGGER.log(Level.WARNING, "Could not obtain an initial context to"
          + "query LDAP (Active Directory) due to a communication failure.",
          e);
    } catch (AuthenticationNotSupportedException e) {
      LOGGER.log(Level.WARNING, "Could not obtain an initial context to"
          + "query LDAP (Active Directory) due to authentication not"
          +  "supported exception.",
          e);
    } catch (AuthenticationException e) {
      LOGGER.log(Level.WARNING, "Could not obtain an initial context to"
          + "query LDAP (Active Directory) due to authentication exception.",
          e);
    } catch (NamingException e) {
      LOGGER.log(Level.WARNING, "Could not obtain an initial context to"
          + "query LDAP (Active Directory) due to a naming exception.",
          e);
    }

    if (ldapContext == null) {
      return;
    }

    LOGGER.info("Sucessfully created an Initial LDAP context");
    try {
      resultControls = new Control[] {new PagedResultsControl(1000, false)};
      ldapContext.setRequestControls(resultControls);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Couldn't initialize LDAP paging control. "
        + "Will continue without paging - this can cause issue if there"
        + "are more than 1000 members in one group.", e);
    } catch (NamingException e) {
      LOGGER.log(Level.WARNING, "Couldn't initialize LDAP paging control. "
          + "Will continue without paging - this can cause issue if there"
          + "are more than 1000 members in one group.", e);
    }

    nETBIOSName = (String) get("(ncName=" + dn + ")",
        AdConstants.ATTR_NETBIOSNAME, configurationNamingContext);
    dnsRoot = (String) get("(ncName=" + dn + ")",
        AdConstants.ATTR_DNSROOT, configurationNamingContext);
    LOGGER.log(Level.INFO, "Connected to domain (dn = " + dn + ", netbios = "
        + nETBIOSName + ", hostname = " + hostName + ", dsServiceName = "
        + dsServiceName + ", highestCommittedUSN = " + highestCommittedUSN
        + ", invocationID = " + invocationID + ", dnsRoot = " + dnsRoot + ")");
  }

  /**
   * Retrieves one attribute from the Active Directory. Used for searching of
   * configuration details.
   * @param filter LDAP filter to search for
   * @param attribute name of attribute to retrieve
   * @param base base name to bind to
   * @return first attribute object
   */
  protected Object get(String filter, String attribute, String base) {
    searchCtls.setReturningAttributes(new String[] {attribute});
    try {
      NamingEnumeration<SearchResult> ldapResults =
          ldapContext.search(base, filter, searchCtls);
      if (!ldapResults.hasMore()) {
        return null;
      }
      SearchResult sr = ldapResults.next();
      Attributes attrs = sr.getAttributes();
      Attribute at = attrs.get(attribute);
      if (at != null) {
        return attrs.get(attribute).get(0);
      }
    } catch (NamingException e) {
      LOGGER.log(Level.WARNING,
          "Failed retrieving " + filter + " from AD server", e);
    }
    return null;
  }

  /**
   * Searches Active Directory and creates AdEntity on each result found
   * @param filter LDAP filter to search in the AD for
   * @param attributes list of attributes to retrieve
   * @return list of entities found
   */
  public ArrayList<AdEntity> search(String filter, String[] attributes) {
    ArrayList<AdEntity> results = new ArrayList<AdEntity>();
    searchCtls.setReturningAttributes(attributes);
    try {
      byte[] cookie = null;
      do {
        NamingEnumeration<SearchResult> ldapResults =
            ldapContext.search(dn, filter, searchCtls);
        while (ldapResults.hasMoreElements()) {
          SearchResult sr = ldapResults.next();
          results.add(new AdEntity(sr));
        }
        cookie = null;
        Control[] resultResponseControls = ldapContext.getResponseControls();
        for (int i = 0; i < resultResponseControls.length; ++i) {
          if (resultResponseControls[i] instanceof
              PagedResultsResponseControl) {
            cookie = ((PagedResultsResponseControl) resultResponseControls[i])
                .getCookie();
            ldapContext.setRequestControls(new Control[] {
                new PagedResultsControl(1000, cookie, Control.CRITICAL)});
          }
        }
      } while ((cookie != null) && (cookie.length != 0));

    } catch (NamingException e) {
      System.out.println("TODO:" + e);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Couldn't initialize LDAP paging control. Will"
          + " continue without paging - this can cause issue if there are more"
          + " than 1000 members in one group. ",
          e);
    }

    return results;
  }

  /**
   * Generate properties to be used for parameter binding in JDBC
   * @return map of names and properties of current object
   */
  public Map<String, Object> getSqlParams() {
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put(AdConstants.DB_DN, dn);
    map.put(AdConstants.DB_DSSERVICENAME, dsServiceName);
    map.put(AdConstants.DB_INVOCATIONID, invocationID);
    map.put(AdConstants.DB_HIGHESTCOMMITTEDUSN, highestCommittedUSN);
    map.put(AdConstants.DB_NETBIOSNAME, nETBIOSName);
    map.put(AdConstants.DB_SID, sid);
    map.put(AdConstants.DB_DNSROOT, dnsRoot);
    return map;
  }

  /**
   * @return the distinguished Name
   */
  public final String getDn() {
    return dn;
  }

  /**
   * @return the dsServiceName
   */
  public String getDsServiceName() {
    return dsServiceName;
  }

  /**
   * @return the invocationID
   */
  public String getInvocationID() {
    return invocationID;
  }
}
