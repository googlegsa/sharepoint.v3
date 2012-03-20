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

  private LdapContext ldapContext = null;
  private SearchControls searchCtls;
  private Control[] resultControls;

  public String hostName;
  private int port = 389;
  public String nETBIOSName;
  private String userName;
  private String password;
  private Method connectMethod;
  private String dn;
  private String configurationNamingContext;
  public String dsServiceName;
  private int highestCommittedUSN;
  public String invocationID;

  public AdServer(
      Method connectMethod,
      String hostName,
      int port,
      String nETBIOSName,
      String userName,
      String password) {
    this.hostName = hostName;
    this.port = port;
    this.nETBIOSName = nETBIOSName;
    this.userName = userName;
    this.password = password;
    searchCtls = new SearchControls();
    searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    this.connectMethod = connectMethod;
  }

  public void connect() {
    if (ldapContext == null) {
      Hashtable<String, String> env = new Hashtable<String, String>();

      // Use the built-in LDAP support.
      env.put(Context.INITIAL_CONTEXT_FACTORY, AdConstants.COM_SUN_JNDI_LDAP_LDAP_CTX_FACTORY);
      env.put(Context.SECURITY_AUTHENTICATION, AdConstants.AUTHN_TYPE_SIMPLE);
      env.put(Context.SECURITY_PRINCIPAL, nETBIOSName + AdConstants.BACKSLASH + userName);
      env.put(Context.SECURITY_CREDENTIALS, password);

      String ldapUrl = (connectMethod == Method.STANDARD) ? "ldap://" : "ldaps://";
      ldapUrl += hostName + ":" + port;
      LOGGER.info(ldapUrl);
      env.put(Context.PROVIDER_URL, ldapUrl);

      try {
        ldapContext = new InitialLdapContext(env, null);
        dn = ldapContext.getAttributes("").get("defaultNamingContext").get(0).toString();
        dsServiceName = ldapContext.getAttributes("").get("dsServiceName").get(0).toString();
        highestCommittedUSN = Integer.parseInt(
            ldapContext.getAttributes("").get("highestCommittedUSN").get(0).toString());
        configurationNamingContext =
            ldapContext.getAttributes("").get("configurationNamingContext").get(0).toString();
        resultControls = new Control[] {new PagedResultsControl(1000, false)};
        ldapContext.setRequestControls(resultControls);
        byte[] invocationIDBytes = (byte[]) get(
            "distinguishedName=" + dsServiceName, "invocationID;binary", dsServiceName);
        StringBuilder sb = new StringBuilder("0x");
        for (byte b : invocationIDBytes) {
          sb.append(Integer.toHexString(b & 0xFF));
        }
        invocationID = sb.toString();
      } catch (CommunicationException e) {
        LOGGER.log(Level.WARNING,
            "Could not obtain an initial context to query LDAP (Active Directory) due to a communication failure.", e);
      } catch (AuthenticationNotSupportedException e) {
        LOGGER.log(Level.WARNING,
            "Could not obtain an initial context to query LDAP (Active Directory) due to authentication not supported exception.", e);
      } catch (AuthenticationException ae) {
        LOGGER.log(Level.WARNING,
            "Could not obtain an initial context to query LDAP (Active Directory) due to authentication exception.", ae);
      } catch (NamingException e) {
        LOGGER.log(Level.WARNING,
            "Could not obtain an initial context to query LDAP (Active Directory) due to a naming exception.", e);
      } catch (IOException e) {
        LOGGER.log(Level.WARNING,
            "Couldn't initialize LDAP paging control. Will continue without paging - this can cause issue if there are more than 1000 members in one group.");
      }
      if (ldapContext != null) {
        LOGGER.info("Sucessfully created an Initial LDAP context");
      }
    }
    nETBIOSName = (String) get("(ncName=" + dn + ")", "nETBIOSName", configurationNamingContext);
    LOGGER.log(Level.INFO, "Connected to domain (dn = " + dn + ", netbios = " + nETBIOSName
        + ", hostname = " + hostName + ", dsServiceName = " + dsServiceName
        + ", highestCommittedUSN = " + highestCommittedUSN + ", invocationID = " + invocationID
        + ")");
  }

  public Object get(String filter, String attribute, String base) {
    searchCtls.setReturningAttributes(new String[] {attribute});
    try {
      NamingEnumeration<SearchResult> ldapResults = ldapContext.search(base, filter, searchCtls);
      SearchResult sr = ldapResults.next();
      Attributes attrs = sr.getAttributes();
      Attribute at = attrs.get(attribute);
      if (at != null) {
        return attrs.get(attribute).get(0);
      }
    } catch (NamingException ex) {
      System.out.println("oh no");
    }
    return null;
  }

  public ArrayList<AdEntity> search(String filter, String[] attributes) {
    ArrayList<AdEntity> results = new ArrayList<AdEntity>();
    searchCtls.setReturningAttributes(attributes);
    try {
      byte[] cookie = null;
      do {
        NamingEnumeration<SearchResult> ldapResults = ldapContext.search(dn, filter, searchCtls);
        while (ldapResults.hasMoreElements()) {
          SearchResult sr = ldapResults.next();

          AdEntity e = new AdEntity(sr.getNameInNamespace());
          Attributes attrs = sr.getAttributes();
          e.sAMAccountName = (String) attrs.get("sAMAccountName").get(0);
          e.setObjectGUID((byte[]) attrs.get("objectGUID;binary").get(0));
          e.setSid((byte[]) attrs.get("objectSid;binary").get(0));
          e.uSNChanged = Long.parseLong((String) attrs.get("uSNChanged").get(0));
          if (attrs.get("primaryGroupId") != null) {
            e.primaryGroupId = (String) attrs.get("primaryGroupId").get(0);
          }
          if (attrs.get("userPrincipalName") != null) {
            e.userPrincipalName = (String) attrs.get("userPrincipalName").get(0);
          }

          Attribute member = attrs.get("member");
          if (member != null) {
            for (int i = 0; i < member.size(); ++i) {
              e.members.add(member.get(i).toString());
            }
          }

          results.add(e);
        }

        cookie = null;

        Control[] resultResponseControls = ldapContext.getResponseControls();

        for (int i = 0; i < resultResponseControls.length; ++i) {
          if (resultResponseControls[i] instanceof PagedResultsResponseControl) {
            cookie = ((PagedResultsResponseControl) resultResponseControls[i]).getCookie();
            ldapContext.setRequestControls(new Control[] {
                new PagedResultsControl(1000, cookie, Control.CRITICAL)});
          }
        }

      } while ((cookie != null) && (cookie.length != 0));

    } catch (NamingException e) {
      System.out.println("TODO:" + e);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING,
          "Couldn't initialize LDAP paging control. Will continue without paging - this can cause issue if there are more than 1000 members in one group. " + e);
    }

    return results;
  }

  public Map<String, Object> getSqlParams() {
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("dn", dn);
    map.put("dsservicename", dsServiceName);
    map.put("invocationid", invocationID);
    map.put("highestcommittedusn", highestCommittedUSN);
    map.put("netbiosname", nETBIOSName);

    return map;
  }

  /**
   * @return the dn
   */
  public final String getDn() {
    return dn;
  }

  /**
   * @param dn the dn to set
   */
  public void setDn(final String dn) {
    this.dn = dn;
  }
}
