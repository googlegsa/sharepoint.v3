// Copyright 2012 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.adgroups;

import com.google.common.base.Strings;
import com.google.enterprise.connector.adgroups.AdConstants.Method;
import com.google.enterprise.connector.spi.RepositoryException;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
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
  private long highestCommittedUSN;
  private String invocationID;
  private String dnsRoot;
  private Timestamp lastFullSync;

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
   * 
   * This method is used for crawling as well as authorization of credentials 
   * against Active Directory.
   */
  public void connect() throws CommunicationException, NamingException {
    Hashtable<String, String> env = new Hashtable<String, String>();

    // Use the built-in LDAP support.
    env.put(Context.INITIAL_CONTEXT_FACTORY,
        AdConstants.COM_SUN_JNDI_LDAP_LDAP_CTX_FACTORY);
    if (Strings.isNullOrEmpty(principal)) {
      env.put(Context.SECURITY_AUTHENTICATION, 
          AdConstants.AUTHN_TYPE_ANONYMOUS);
    } else {
      env.put(Context.SECURITY_AUTHENTICATION,
          AdConstants.AUTHN_TYPE_SIMPLE);
      env.put(Context.SECURITY_PRINCIPAL, principal);
      env.put(Context.SECURITY_CREDENTIALS, password);
    }

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
  }

  public void initialize() throws RepositoryException {
    try {
      connect();
      sid = AdEntity.getTextSid((byte[])get(
          AdConstants.ATTR_DISTINGUISHEDNAME + AdConstants.EQUALS + dn,
          AdConstants.ATTR_OBJECTSID, dn));
      invocationID = AdEntity.getTextGuid((byte[]) get(
          AdConstants.ATTR_DISTINGUISHEDNAME
          + AdConstants.EQUALS + dsServiceName,
          AdConstants.ATTR_INVOCATIONID,
          dsServiceName));
    } catch (CommunicationException e) {
      throw new RepositoryException(e);
    } catch (AuthenticationNotSupportedException e) {
      throw new RepositoryException(e);
    } catch (AuthenticationException e) {
      throw new RepositoryException(e);
    } catch (NamingException e) {
      throw new RepositoryException(e);
    }

    LOGGER.info("Sucessfully created an Initial LDAP context");

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
   * Set request controls on the LDAP query 
   * @param deleted include deleted control
   */
  private void setControls(boolean deleted) {
    try {
      Control[] controls;
      if (deleted) {
        controls = new Control[] {
            new PagedResultsControl(1000, false), new DeletedControl()};
      } else {
        controls = new Control[] {
            new PagedResultsControl(1000, false)};
      }
      ldapContext.setRequestControls(controls);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Couldn't initialize LDAP paging control. "
        + "Will continue without paging - this can cause issue if there"
        + "are more than 1000 members in one group.", e);
    } catch (NamingException e) {
      LOGGER.log(Level.WARNING, "Couldn't initialize LDAP paging control. "
          + "Will continue without paging - this can cause issue if there"
          + "are more than 1000 members in one group.", e);
    }
  }

  /**
   * Searches Active Directory and creates AdEntity on each result found
   * @param filter LDAP filter to search in the AD for
   * @param attributes list of attributes to retrieve
   * @return list of entities found
   */
  public Set<AdEntity> search(
      String filter, boolean deleted, String[] attributes) {
    Set<AdEntity> results = new HashSet<AdEntity>();
    searchCtls.setReturningAttributes(attributes);
    setControls(deleted);
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
      LOGGER.log(Level.WARNING, "", e);
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
    if (lastFullSync != null) {
      map.put(AdConstants.DB_LASTFULLSYNC,
          new java.sql.Timestamp(lastFullSync.getTime()));
    }
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

  /**
   * @return the nETBIOSName
   */
  public String getnETBIOSName() {
    return nETBIOSName;
  }

  /**
   * @return the sid
   */
  public String getSid() {
    return sid;
  }

  class DeletedControl implements Control {
    @Override
    public byte[] getEncodedValue() {
        return new byte[] {};
    }
    @Override
    public String getID() {
        return "1.2.840.113556.1.4.417";
    }
    @Override
    public boolean isCritical() {
        return true;
    }
  }

  /**
   * @return the lastFullSync
   */
  public Timestamp getLastFullSync() {
    return lastFullSync;
  }

  /**
   * @param lastFullSync the lastFullSync to set
   */
  public void setLastFullSync(Timestamp lastFullSync) {
    this.lastFullSync = lastFullSync;
  }
  
  @Override
  public String toString() {
    return "[" + nETBIOSName + "] ";
  }

  /**
   * @return the highestCommittedUSN
   */
  public long getHighestCommittedUSN() {
    return highestCommittedUSN;
  }
}
