// Copyright 2011 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.ldap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.dao.UserDataStoreDAO;
import com.google.enterprise.connector.sharepoint.dao.UserGroupMembership;
import com.google.enterprise.connector.sharepoint.ldap.LdapConstants.AuthType;
import com.google.enterprise.connector.sharepoint.ldap.LdapConstants.LdapConnectionError;
import com.google.enterprise.connector.sharepoint.ldap.LdapConstants.Method;
import com.google.enterprise.connector.sharepoint.ldap.LdapConstants.ServerType;
import com.google.enterprise.connector.sharepoint.ldap.UserGroupsService.LdapConnection;
import com.google.enterprise.connector.sharepoint.ldap.UserGroupsService.LdapConnectionSettings;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointAuthenticationManager;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.SpiConstants.PrincipalType;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

/**
 * An implementation of {@link LdapService} and encapsulates all interaction
 * with JNDI to get {@link LdapContext} and {@link LdapConnection} with
 * {@link LdapConnectionSettings} provided by
 * {@link SharepointAuthenticationManager} and also it talks to
 * {@link UserDataStoreDAO} to get all SP groups. This implementation is
 * specific to Active Directory service at the moment.
 *
 * @author nageswara_sura
 */
public class UserGroupsService implements LdapService {

  private static final Logger LOGGER = Logger.getLogger(UserGroupsService.class.getName());

  private LdapConnectionSettings ldapConnectionSettings;
  private LdapContext context;
  private final UserGroupsCache<Object,
      ConcurrentHashMap<String, Set<Principal>>> lugCacheStore;
  private LdapConnection ldapConnection;
  private final SharepointClientContext sharepointClientContext;

  /**
   * Initializes LDAP context object for a given {@link LdapConnectionSettings}
   * and also Constructs {@code LdapUserGroupsCache} cache with a refresh
   * interval and custom capacity.
   *
   * @param ldapConnectionSettings
   */
  @VisibleForTesting
  public UserGroupsService(LdapConnectionSettings ldapConnectionSettings,
      int cacheSize, long refreshInterval, boolean enableLUGCache) {
    this.ldapConnectionSettings = ldapConnectionSettings;
    ldapConnection = new LdapConnection(ldapConnectionSettings);
    context = getLdapContext();
    this.sharepointClientContext = null;
    if (enableLUGCache) {
      this.lugCacheStore = new UserGroupsCache<Object,
          ConcurrentHashMap<String, Set<Principal>>>(
              refreshInterval, cacheSize);
      LOGGER.log(Level.CONFIG, "Configured user groups cache store with refresh interval [ "
          + refreshInterval + " ] and with capacity [ " + cacheSize + " ]");
    } else {
      this.lugCacheStore = null;
      LOGGER.log(Level.CONFIG, "No cache has been configured to keep user groups memberships.");
    }
  }

  private UserGroupsService(LdapConnectionSettings ldapConnectionSettings,
      SharepointClientContext inSharepointClientContext) {
    if (!Strings.isNullOrEmpty(ldapConnectionSettings.getHostname())) {
      this.ldapConnectionSettings = ldapConnectionSettings;
      ldapConnection = new LdapConnection(ldapConnectionSettings);
      context = getLdapContext();
    } else {
      LOGGER.warning("Not attempting to create LDAP context, because LDAP host name is empty or null.");
    }

    this.sharepointClientContext = inSharepointClientContext;
    if (sharepointClientContext.isUseCacheToStoreLdapUserGroupsMembership()) {
      this.lugCacheStore = new UserGroupsCache<Object,
          ConcurrentHashMap<String, Set<Principal>>>(
              sharepointClientContext.getCacheRefreshInterval(),
              sharepointClientContext.getInitialCacheSize());
    } else {
      this.lugCacheStore = null;
      LOGGER.log(Level.INFO, "No cache has been configured to keep user groups memberships.");
    }
  }

  public UserGroupsService(SharepointClientContext inSharepointClientContext) {
    this(inSharepointClientContext.getLdapConnectionSettings(),
        inSharepointClientContext);
  }

  /**
   * A setter method used to set {@link LdapConnectionSettings} and creates a
   * {@link LdapConnection} object.
   *
   * @param ldapConnectionSettings to initialize and create
   *          {@link LdapConnection}
   */
  public void setLdapConnectionSettings(
      LdapConnectionSettings ldapConnectionSettings) {
    this.ldapConnectionSettings = ldapConnectionSettings;
    ldapConnection = new LdapConnection(ldapConnectionSettings);
  }

  public UserGroupsCache<Object, ConcurrentHashMap<String, Set<Principal>>> getLugCacheStore() {
    return lugCacheStore;
  }

  public LdapConnection getLdapConnection() {
    return ldapConnection;
  }

  @Override
  public LdapContext getLdapContext() {
    return ldapConnection.getLdapContext();
  }

  public Map<LdapConnectionError, String> getErrors() {
    if (ldapConnection != null) {
      return ldapConnection.getErrors();
    }
    throw new IllegalStateException(
        "Must successfully set connection config before getting error state");
  }

  public static class LdapConnection {

    private final LdapConnectionSettings settings;
    private LdapContext ldapContext = null;
    private final Map<LdapConnectionError, String> errors;

    public LdapConnection(LdapConnectionSettings ldapConnectionSettings) {
      LOGGER.info(ldapConnectionSettings.toString());
      this.settings = ldapConnectionSettings;
      Hashtable<String, String> env = configureLdapEnvironment();
      this.errors = Maps.newHashMap();
      this.ldapContext = createContext(env);
    }

    /**
     * @return Map of errors with {@link LdapConnectionError} as a key and
     *         detailed error message as a value.
     */
    public Map<LdapConnectionError, String> getErrors() {
      return errors;
    }

    /**
     * Returns initial {@link LdapContext}
     */
    public LdapContext getLdapContext() {
      return ldapContext;
    }

    private LdapContext createContext() {
      return createContext(configureLdapEnvironment());
    }

    /**
     * Returns {@link LdapContext} object.
     *
     * @param env hold LDAP
     * @return {@link LdapContext}
     */
    private LdapContext createContext(Hashtable<String, String> env) {
      LdapContext ctx = null;
      try {
        ctx = new InitialLdapContext(env, null);
      } catch (CommunicationException e) {
        errors.put(LdapConnectionError.CommunicationException, e.getCause().toString());
        LOGGER.log(Level.WARNING, "Could not obtain an initial context to query LDAP (Active Directory) due to a communication failure.", e);
      } catch (AuthenticationNotSupportedException e) {
        errors.put(LdapConnectionError.AuthenticationNotSupportedException, e.getCause().toString());
        LOGGER.log(Level.WARNING, "Could not obtain an initial context to query LDAP (Active Directory) due to authentication not supported exception.", e);
      } catch (AuthenticationException ae) {
        errors.put(LdapConnectionError.AuthenticationFailedException, ae.getCause().toString());
        LOGGER.log(Level.WARNING, "Could not obtain an initial context to query LDAP (Active Directory) due to authentication exception.", ae);
      } catch (NamingException e) {
        errors.put(LdapConnectionError.NamingException, e.getCause().toString());
        LOGGER.log(Level.WARNING, "Could not obtain an initial context to query LDAP (Active Directory) due to a naming exception.", e);
      }
      if (ctx == null) {
        return null;
      }
      LOGGER.info("Sucessfully created an Initial LDAP context");
      return ctx;
    }

    /**
     * Makes an LDAP or LDAPS URL. The default port for LDAPS URLs is 636 where
     * as for LDAP URLs it is 389.
     *
     * @return a LDAP or LDAPS URL bases on the {@link Method}
     */
    private String makeLdapUrl() {
      String url;
      Method connectMethod = settings.getConnectMethod();
      if (connectMethod == Method.SSL) {
        url = "ldaps://"; // For SSL
      } else {
        url = "ldap://"; // for NON-SSL
      }

      // Construct the full URL
      url = url + settings.getHostname();
      if (settings.getPort() > 0) {
        url = url + ":" + settings.getPort();
      }

      LOGGER.info("Complete LDAP URL : " + url);
      return url;
    }

    /**
     * Initialize the {@link java.util.HashSet} used to create an initial LDAP Context.
     * Note that we specifically require a {@link java.util.HashSet} rather than a
     * HashMap as the parameter type in the InitialLDAPContext constructor
     *
     * @return initialized {@link java.util.HashSet} suitable for constructing an
     *         InitiaLdaplContext
     */
    private Hashtable<String, String> configureLdapEnvironment() {
      Hashtable<String, String> env = new Hashtable<String, String>();
      // Use the built-in LDAP support.
      env.put(Context.INITIAL_CONTEXT_FACTORY, LdapConstants.COM_SUN_JNDI_LDAP_LDAP_CTX_FACTORY);

      // Set our authentication settings.
      AuthType authType = settings.getAuthType();
      if (authType == AuthType.SIMPLE) {
        env.put(Context.SECURITY_AUTHENTICATION, authType.toString().toLowerCase());
        env.put(Context.SECURITY_PRINCIPAL, settings.getUsername()
            + SPConstants.AT + settings.domainName);
        env.put(Context.SECURITY_CREDENTIALS, settings.getPassword());
        LOGGER.info("Using simple authentication.");
      } else {
        if (authType != AuthType.ANONYMOUS) {
          LOGGER.warning("Unknown authType - falling back to anonymous.");
        } else {
          LOGGER.info("Using anonymous authentication.");
        }
        env.put(Context.SECURITY_AUTHENTICATION, "none"); //$NON-NLS-1$
      }
      env.put(Context.PROVIDER_URL, makeLdapUrl());
      return env;
    }
  }

  public static class LdapConnectionSettings {
    private final String hostName;
    private final int port;
    private String domainName;
    private final AuthType authType;
    private final String userName;
    private final String password;
    private final Method connectMethod;
    private final String baseDN;
    private final ServerType serverType;

    public LdapConnectionSettings(Method connectMethod, String hostname,
        int port, String baseDN, AuthType authType, String userName,
        String password, String domainName) {
      this.authType = authType;
      this.baseDN = baseDN;
      this.connectMethod = connectMethod;
      this.hostName = hostname;
      this.password = password;
      this.port = port;
      this.serverType = ServerType.getDefault();
      this.userName = userName;
      this.domainName = domainName;
    }

    public LdapConnectionSettings(Method standard, String hostName, int port,
        String baseDN, String domainName) {
      this.authType = AuthType.ANONYMOUS;
      this.baseDN = baseDN;
      this.connectMethod = standard;
      this.hostName = hostName;
      this.password = null;
      this.port = port;
      this.serverType = ServerType.getDefault();
      this.userName = null;
      this.domainName = domainName;
    }

    @Override
    public String toString() {
      String displayPassword;
      if (password == null) {
        displayPassword = "null";
      } else if (password.length() < 1) {
        displayPassword = "<empty>";
      } else {
        displayPassword = "####";
      }
      return "LdapConnectionSettings [authType=" + authType + ", baseDN="
          + baseDN + ", connectMethod=" + connectMethod + ", hostname="
          + hostName + ", password=" + displayPassword + ", port=" + port
          + ", serverType=" + serverType + ", userName=" + userName
          + ", domainName =" + domainName + " ]";
    }

    public AuthType getAuthType() {
      return authType;
    }

    public String getBaseDN() {
      return baseDN;
    }

    public Method getConnectMethod() {
      return connectMethod;
    }

    public String getHostname() {
      return hostName;
    }

    public String getPassword() {
      return password;
    }

    public int getPort() {
      return port;
    }

    public ServerType getServerType() {
      return serverType;
    }

    public String getUsername() {
      return userName;
    }

    public String getDomainName() {
      return domainName;
    }
  }

  /**
   * Takes user SID as binary string, group RID as string and converts them to escaped hexa
   * representation of LDAP search filter
   *
   * @param sid user binary SID
   * @param primaryGroupId primary group RID (guaranteed to be within user's domain)
   * @return string containing LDAP search filter for user's primary group
   */
  String createSearchFilterForPrimaryGroup(byte[] sid, String primaryGroupId) {
    long primaryGroup = Long.parseLong(primaryGroupId);
    // replace the last four bytes of user's SID with group RID
    sid[sid.length - 1] = (byte)((primaryGroup >> 24) & 0xFF);
    sid[sid.length - 2] = (byte)((primaryGroup >> 16) & 0xFF);
    sid[sid.length - 3] = (byte)((primaryGroup >> 8) & 0xFF);
    sid[sid.length - 4] = (byte)(primaryGroup & 0xFF);
    // format the SID as escaped hexa (i.e. \01\05\ff...)
    StringBuilder primaryGroupSid = new StringBuilder();
    primaryGroupSid.append(LdapConstants.PREFIX_FOR_PRIMARY_GROUP_FILTER);
    for (int i = 0; i < sid.length; ++i) {
      int unsignedByte = sid[i] & 0xFF;
      // add zero padding for single digits
      if (unsignedByte < 16)
        primaryGroupSid.append("\\0");
      else
        primaryGroupSid.append("\\");
      primaryGroupSid.append(Integer.toHexString(unsignedByte));
    }
    primaryGroupSid.append(")");
    return primaryGroupSid.toString();
  }

  /**
   * Returns user's primary group
   *
   * @param userSid SID of the user in Active Directory
   * @param primaryGroupId domain local ID of the primary group
   * @return string containing the primary group's name
   */
  String getPrimaryGroupForTheSearchUser(byte[] userSid, String primaryGroupId) {
    if (userSid == null || primaryGroupId == null) {
      return null;
    }
    String primaryGroupDN = null;
    SearchControls searchCtls = makeSearchCtls(new String[]{LdapConstants.ATTRIBUTE_MEMBER_OF});
    searchCtls.setReturningAttributes(new String[]{});
    // Create the search filter
    String searchFilter = createSearchFilterForPrimaryGroup(userSid, primaryGroupId);
    // Specify the Base DN for the search
    String searchBase = ldapConnectionSettings.getBaseDN();
    NamingEnumeration<SearchResult> ldapResults = null;
    try {
      ldapResults = this.context.search(searchBase, searchFilter, searchCtls);
      SearchResult sr = ldapResults.next();
      primaryGroupDN = sr.getNameInNamespace();
    } catch (NamingException ne) {
      LOGGER.log(Level.WARNING, "Failed to retrieve primary group with SID: ["
          + searchFilter + "]", ne);
    } finally {
      try {
        if (null != ldapResults) {
          ldapResults.close();
        }
      } catch (NamingException e) {
        LOGGER.log(Level.WARNING, "Exception during clean up of ldap results.", e);
      }
    }
    return primaryGroupDN;
  }

  /**
   * Returns a set of all direct groups that the search user belongs to.
   *
   * @param userName search user name
   * @return a set of direct groups that the user belongs to in AD.
   */
  Set<String> getDirectGroupsForTheSearchUser(String userName) {
    // Create the search controls.
    SearchControls searchCtls = makeSearchCtls(new String[]{
          LdapConstants.ATTRIBUTE_MEMBER_OF,
          LdapConstants.ATTRIBUTE_PRIMARY_GROUP_ID,
          LdapConstants.ATTRIBUTE_OBJECTSID
        });
    // Create the search filter.
    String searchFilter = createSearchFilterForDirectGroups(userName);
    // Specify the Base DN for the search.
    String searchBase = ldapConnectionSettings.getBaseDN();
    int totalResults = 0;
    Set<String> directGroups = new HashSet<String>();
    NamingEnumeration<SearchResult> ldapResults = null;
    byte[] userSid = null;
    String primaryGroupId = null;
    try {
      ldapResults = this.context.search(searchBase, searchFilter, searchCtls);
      // Loop through the search results
      while (ldapResults.hasMoreElements()) {
        SearchResult sr = ldapResults.next();
        Attributes attrs = sr.getAttributes();
        if (attrs != null) {
          try {
            for (NamingEnumeration<? extends Attribute> ae = attrs.getAll(); ae.hasMore();) {
              Attribute attr = ae.next();
              if (attr.getID().equals(LdapConstants.ATTRIBUTE_OBJECTSID)){
                userSid = (byte[])attr.get(0);
              } else if (attr.getID().equals(LdapConstants.ATTRIBUTE_PRIMARY_GROUP_ID)) {
                primaryGroupId = (String)attr.get(0);
              } else {
                for (NamingEnumeration<?> e = attr.getAll(); e.hasMore(); totalResults++) {
                  directGroups.add(e.next().toString());
                }
              }
            }
          } catch (NamingException e) {
            LOGGER.log(Level.WARNING, "Exception while retrieving direct groups for the search user ["
                + userName + "]", e);
          }
        }
      }
    } catch (NamingException ne) {
      LOGGER.log(Level.WARNING, "Failed to retrieve direct groups for the user name : ["
          + userName + "]", ne);
    } finally {
      if (null != ldapResults) {
        try {
          ldapResults.close();
        } catch (NamingException e) {
          LOGGER.log(Level.WARNING, "Exception during clean up of ldap results for the search user : "
              + userName, e);
        }
      }
    }

    directGroups.add(getPrimaryGroupForTheSearchUser(userSid, primaryGroupId));
    LOGGER.info("[ " + userName + " ] is a direct member of "
        + directGroups.size() + " groups : " + directGroups);
    return directGroups;
  }

  private SearchControls makeSearchCtls(String attributes[]) {
    SearchControls searchCtls = new SearchControls();
    // Specify the search scope
    searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    // Specify the attributes to return
    searchCtls.setReturningAttributes(attributes);
    return searchCtls;
  }

  private String createSearchFilterForDirectGroups(String userName) {
    StringBuffer filter;
    filter = new StringBuffer().append(LdapConstants.PREFIX_FOR_DIRECT_GROUPS_FILTER
        + ldapEscape(userName) + SPConstants.DOUBLE_CLOSE_PARENTHESIS);
    LOGGER.config("search filter value for fetching direct groups :" + filter);
    return filter.toString();
  }

  @Override
  public void getAllParentGroups(String groupName,
      final Set<String> parentGroupsInfo) {
    if (!Strings.isNullOrEmpty(groupName)) {
      parentGroupsInfo.add(groupName);
      Set<String> parentGroups = getAllParentGroupsForTheGroup(groupName);
      LOGGER.log(Level.INFO, "Parent groups for the group [" + groupName
          + "] : " + parentGroups);

      for (String group : parentGroups) {
        if (!parentGroupsInfo.contains(group)) {
          getAllParentGroups(group, parentGroupsInfo);
        }
      }
    }
  }

  /**
   * Returns a set of all parent groups that the search user belongs to.
   *
   * @param groupName is the group, whose parent groups need to be retrieved.
   * @return a set of all parent groups
   */
  private Set<String> getAllParentGroupsForTheGroup(String groupName) {
    Set<String> parentGroups = new HashSet<String>();
    // Create the search controls
    SearchControls searchCtls = makeSearchCtls(new String[]{LdapConstants.ATTRIBUTE_MEMBER_OF});
    // Create the search filter
    String searchFilter = createSearchFilterForParentGroups(groupName);
    // Specify the Base DN for the search
    String searchBase = ldapConnectionSettings.getBaseDN();
    NamingEnumeration<SearchResult> ldapResults = null;
    try {
      ldapResults = this.context.search(searchBase, searchFilter, searchCtls);
      while (ldapResults.hasMoreElements()) {
        SearchResult sr = ldapResults.next();
        Attributes attrs = sr.getAttributes();
        if (attrs != null) {
          try {
            for (NamingEnumeration<? extends Attribute> ae = attrs.getAll(); ae.hasMore();) {
              Attribute attr = (Attribute) ae.next();
              for (NamingEnumeration<?> e = attr.getAll(); e.hasMore();) {
                parentGroups.add(e.next().toString());
              }
            }
          } catch (NamingException e) {
            LOGGER.log(Level.WARNING, "Exception while retrieving parent groups for the group ["
                + groupName + "]", e);
          }
        }
      }
    } catch (NamingException ne) {
      LOGGER.log(Level.WARNING, "Failed to retrieve parent groups for the group name : ["
          + groupName + "]", ne);
    } finally {
      try {
        if (null != ldapResults) {
          ldapResults.close();
        }
      } catch (NamingException e) {
        LOGGER.log(Level.WARNING, "Exception during clean up of ldap results.", e);
      }
    }
    return parentGroups;
  }

  /**
   * Escapes special characters used in string literals for LDAP search filters
   *
   * @param literal to be escaped and used in LDAP filter
   * @return escaped literal
   */
  String ldapEscape(String literal) {
    StringBuilder buffer = new StringBuilder(literal.length() * 2);
    for (int i = 0; i < literal.length(); ++i) {
      char c = literal.charAt(i);
      if (LdapConstants.ESCAPE_CHARACTERS.indexOf(c) == -1) {
        buffer.append(c);
      } else {
        String escape = (c < 16) ? "\\0" : "\\";
        buffer.append(escape).append(Integer.toHexString(c));
      }
    }
    return buffer.toString();
  }

  private String createSearchFilterForParentGroups(String groupName) {
    StringBuffer filter;
    filter = new StringBuffer().append(
        LdapConstants.PREFIX_FOR_PARENTS_GROUPS_FILTER
        + ldapEscape(groupName) + SPConstants.DOUBLE_CLOSE_PARENTHESIS);
    return filter.toString();
  }

  @Override
  public Set<String> getAllLdapGroups(String userName) {
    if (Strings.isNullOrEmpty(userName)) {
      return null;
    }
    Set<String> ldapGroups = new HashSet<String>();
    LOGGER.info("Querying LDAP directory server to fetch all direct groups "
        + "for the search user: " + userName);
    // fix me by creating a LDAP connection poll instead of creating context
    // object on demand.
    this.context = new LdapConnection(
        sharepointClientContext.getLdapConnectionSettings()).createContext();
    Set<String> directGroups = getDirectGroupsForTheSearchUser(userName);
    for (String groupName : directGroups) {
      getAllParentGroups(groupName, ldapGroups);
    }
    LOGGER.info("[ " + userName + " ] is a direct or indirect member of "
        + ldapGroups.size() + " groups");
    Set<String> groupNames = getSAMAccountNames(ldapGroups);
    if (null != directGroups) {
      directGroups = null;
      this.context = null;
    }
    return groupNames;
  }

  /**
   * Retrieves sAMAccountNames for list of entities
   * @param distinguishedNames set of distinguishedNames of all groups
   *     to resolve
   * @return sAMAccountName for each of the entities
   */
  Set<String> getSAMAccountNames(Set<String> distinguishedNames) {
    Set<String> result = new HashSet<String>();
    // Create the search controls
    SearchControls searchCtls = makeSearchCtls(
        new String[] {LdapConstants.ATTRIBUTE_SAMACCOUNTNAME});
    // Create the search filter
    StringBuffer filter = new StringBuffer("(|");
    // AD LDAP allows filters up to 10^7 bytes, we will hit issues, we should
    // hit out of memory in parent group resolution before hitting this limit
    for (String dn : distinguishedNames) {
      filter.append(LdapConstants.PREFIX_FOR_GROUP_FILTER)
          .append(ldapEscape(dn)).append(")");
    }
    filter.append(")");
    // Specify the Base DN for the search
    String searchBase = ldapConnectionSettings.getBaseDN();
    NamingEnumeration<SearchResult> ldapResults = null;
    try {
      ldapResults = this.context.search(
          searchBase, filter.toString(), searchCtls);
      while (ldapResults.hasMoreElements()) {
        SearchResult sr = ldapResults.next();
        Attributes attrs = sr.getAttributes();
        if (attrs != null) {
          try {
            Attribute sAMAccountName = attrs.get(
                LdapConstants.ATTRIBUTE_SAMACCOUNTNAME);
            if (sAMAccountName == null || sAMAccountName.size() == 0) {
              LOGGER.log(Level.WARNING,
                  "Could not establish sAMAccountName for [" 
                  + sr.getNameInNamespace() + "]");
              continue;
            }
            result.add(sAMAccountName.get(0).toString());
          } catch (NamingException e) {
            LOGGER.log(Level.WARNING,
                "Exception while retrieving group names. Search filter ["
                + filter + "]", e);
          }
        }
      }
    } catch (NamingException ne) {
      LOGGER.log(Level.WARNING,
          "Exception while retrieving group names. Search filter ["
          + filter + "]", ne);
    } finally {
      try {
        if (null != ldapResults) {
          ldapResults.close();
        }
      } catch (NamingException e) {
        LOGGER.log(
            Level.WARNING, "Exception during clean up of ldap results.", e);
      }
    }
    return result;
  }

  /**
   * Retrieves SAM account name for the search user for all the possible primary
   * verification identities sent by GSA and is require to query Directory
   * service to fetch all direct groups he belongs to. This implementation is
   * specific to the AD.
   *
   * @param searchUserName search user name.
   */
  @Override
  public String getSamAccountNameForSearchUser(final String searchUserName) {
    String tmpUserName = null;
    if (null == searchUserName) {
      return null;
    }
    if (searchUserName.lastIndexOf(SPConstants.AT) != SPConstants.MINUS_ONE) {
      tmpUserName = searchUserName.substring(0, searchUserName.indexOf(SPConstants.AT));
    } else if (searchUserName.indexOf(SPConstants.DOUBLEBACKSLASH) != SPConstants.MINUS_ONE) {
      tmpUserName = searchUserName.substring(searchUserName.indexOf(SPConstants.DOUBLEBACKSLASH) + 1);
    } else {
      tmpUserName = searchUserName;
    }
    return tmpUserName;
  }

  /**
   * It is a helper method that returns a set of SPGroups for the search user
   * and the AD groups of which he/she is a direct or indirect member of.
   *
   * @param searchUser the searchUser
   * @param adGroups a set of AD groups to which search user is a direct of
   *          indirect member of.
   */
  private Set<Principal> getAllSPGroupsForSearchUserAndLdapGroups(
      String searchUser, Set<Principal> adGroups) {
    String localNamespace = sharepointClientContext.getGoogleLocalNamespace();

    // Search user and SP groups memberships found in user data store.
    Set<Principal> spGroups = new HashSet<Principal>();
    try {
      if (null != this.sharepointClientContext.getUserDataStoreDAO()) {
        spGroups = sharepointClientContext.getUserDataStoreDAO()
            .getSharePointGroupsForSearchUserAndLdapGroups(
                localNamespace, adGroups, searchUser);
      }
    } catch (SharepointException se) {
      LOGGER.warning("Exception occured while fetching user groups memberships for the search user ["
          + searchUser + "] and AD groups [" + adGroups + "]");
    }
    return spGroups;
  }

  @Override
  public Set<Principal> getAllGroupsForSearchUser(
      SharepointClientContext sharepointClientContext, String searchUser)
      throws SharepointException {
    ConcurrentHashMap<String, Set<Principal>> userGroupsMap;
    Set<Principal> allUserGroups = new HashSet<Principal>();
    if (null != searchUser && null != lugCacheStore) {
      if (lugCacheStore.getSize() > 0
          && lugCacheStore.contains(searchUser.toLowerCase())) {
        userGroupsMap = lugCacheStore.get(searchUser);
        if (null != userGroupsMap) {
          allUserGroups.addAll(userGroupsMap.get(SPConstants.ADGROUPS));
          allUserGroups.addAll(userGroupsMap.get(SPConstants.SPGROUPS));
        }
        LOGGER.info("Found valid entry for search user [" + searchUser
            + "] in cache store and he/she is a direct or indirect member of "
            + allUserGroups.size() + " groups");
        return allUserGroups;
      } else {
        LOGGER.info("No entry found for the user [ "
            + searchUser
            + " ] in cache store. Hence querying LDAP server and User data store to fetch all AD and SP groups, to which the search user belongs to.");
        userGroupsMap = getAllADGroupsAndSPGroupsForSearchUser(searchUser);
        if (null != userGroupsMap) {
          allUserGroups.addAll(userGroupsMap.get(SPConstants.ADGROUPS));
          allUserGroups.addAll(userGroupsMap.get(SPConstants.SPGROUPS));
        }

        this.lugCacheStore.put(searchUser.toLowerCase(), userGroupsMap);

        return allUserGroups;
      }
    } else {
      if (Strings.isNullOrEmpty(searchUser)) {
        return null;
      }
      LOGGER.info("The LDAP cache is not yet initialized and hence querying LDAP and User Data Store directly.");
      userGroupsMap = getAllADGroupsAndSPGroupsForSearchUser(searchUser);
      allUserGroups.addAll(userGroupsMap.get(SPConstants.ADGROUPS));
      allUserGroups.addAll(userGroupsMap.get(SPConstants.SPGROUPS));
    }
    return allUserGroups;
  }

  /**
   * Returns a set of groups after adding the specific group name format
   * provided by connector administrator in the connector configuration page. It
   * should be called before making a call to user data store to get all SP
   * groups.
   *
   * @param groupNames set of AD group names.
   */
  Set<String> addGroupNameFormatForTheGroups(Set<String> groupNames) {
    String format = this.sharepointClientContext.getGroupnameFormatInAce();
    LOGGER.config("Groupname format in ACE : " + format);
    String domain = this.sharepointClientContext.getDomain();
    LOGGER.config("Domain : " + domain);
    Set<String> groups = new HashSet<String>();
    if (format.indexOf(SPConstants.AT) != SPConstants.MINUS_ONE) {
      for (String groupName : groupNames) {
        groups.add(Util.getGroupNameAtDomain(groupName.toLowerCase(), domain.toUpperCase()));
      }
      return groups;
    } else if (format.indexOf(SPConstants.DOUBLEBACKSLASH) != SPConstants.MINUS_ONE) {
      for (String groupName : groupNames) {
        groups.add(Util.getGroupNameWithDomain(groupName.toLowerCase(), domain.toUpperCase()));
      }
      return groups;
    } else {
      for (String groupName : groupNames) {
        groups.add(groupName.toLowerCase());
      }
      return groups;
    }
  }

  /**
   * Returns the search user name after changing its format to the user name
   * format specified by the connector administrator during connector
   * configuration.
   *
   * @param userName
   */
  String addUserNameFormatForTheSearchUser(final String userName) {
    String format = this.sharepointClientContext.getUsernameFormatInAce();
    LOGGER.config("Username format in ACE : " + format);
    String domain = this.sharepointClientContext.getDomain();
    if (format.indexOf(SPConstants.AT) != SPConstants.MINUS_ONE) {
      return Util.getUserNameAtDomain(userName, domain);
    } else if (format.indexOf(SPConstants.DOUBLEBACKSLASH) != SPConstants.MINUS_ONE) {
      return Util.getUserNameWithDomain(userName, domain);
    } else {
      return userName;
    }
  }

  /**
   * Create and returns a {@link ConcurrentHashMap} by querying LDAP directory
   * server to fetch all AD groups that the search user belongs to and then
   * queries User Data Store with a {@link Set}of AD groups and search user to
   * fetch all SP groups.
   *
   * @param searchUser the searchUser
   * @throws SharepointException
   */
  private ConcurrentHashMap<String, Set<Principal>>
      getAllADGroupsAndSPGroupsForSearchUser(String searchUser) {
    Set<String> adGroups = getAllLdapGroups(searchUser);
    Set<Principal> finalADGroups = new HashSet<Principal>();
    if (null != adGroups && adGroups.size() > 0) {
      String globalNamespace =
          sharepointClientContext.getGoogleGlobalNamespace();
      for (String adGroup : addGroupNameFormatForTheGroups(adGroups)) {
        finalADGroups.add(
            new Principal(PrincipalType.UNKNOWN, globalNamespace, adGroup));
      }
      finalADGroups.add(
          new Principal(PrincipalType.UNKNOWN, globalNamespace, "Everyone"));
      finalADGroups.add(new Principal(PrincipalType.UNKNOWN,
          globalNamespace, "NT AUTHORITY\\authenticated users"));
      finalADGroups.add(new Principal(PrincipalType.UNKNOWN,
          globalNamespace, "NT AUTHORITY\\interactive"));
    }
    String finalSearchUserName = addUserNameFormatForTheSearchUser(searchUser);
    LOGGER.info("Querying user data store with the AD groups: "
        + finalADGroups + " and search user [" + finalSearchUserName + "]");
    Set<Principal> spGroups = getAllSPGroupsForSearchUserAndLdapGroups(
        finalSearchUserName, finalADGroups);

    ConcurrentHashMap<String, Set<Principal>> userGroupsMap =
        new ConcurrentHashMap<String, Set<Principal>>(2);
    userGroupsMap.put(SPConstants.ADGROUPS, finalADGroups);
    userGroupsMap.put(SPConstants.SPGROUPS, spGroups);
    return userGroupsMap;
  }
}
