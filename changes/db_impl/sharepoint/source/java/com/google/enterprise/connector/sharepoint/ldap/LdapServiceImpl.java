//Copyright 2011 Google Inc.

//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.ldap;

import com.google.common.base.Strings;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.ldap.LdapConstants.AuthType;
import com.google.enterprise.connector.sharepoint.ldap.LdapConstants.Method;
import com.google.enterprise.connector.sharepoint.ldap.LdapConstants.ServerType;
import com.google.enterprise.connector.sharepoint.ldap.LdapServiceImpl.LdapConnection;
import com.google.enterprise.connector.sharepoint.ldap.LdapServiceImpl.LdapConnectionSettings;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointAuthenticationManager;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
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
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

/**
 * An implementation of {@link LdapService} and encapsulates all interaction
 * with JNDI to get {@link LdapContext} and {@link LdapConnection} with
 * {@link LdapConnectionSettings} provided by
 * {@link SharepointAuthenticationManager}. This implementation is specific to
 * Active Directory service at the moment.
 *
 * @author nageswara_sura
 */
public class LdapServiceImpl implements LdapService {

    private static final Logger LOGGER = Logger.getLogger(LdapServiceImpl.class.getName());

    private LdapConnectionSettings ldapConnectionSettings;
    private LdapContext context;
    private LdapUserGroupsCache<Object, Object> lugCacheStore = null;
    private LdapConnection ldapConnection;

    public LdapServiceImpl() {

    }

    /**
     * Initializes LDAP context object for a given
     * {@link LdapConnectionSettings} and also Constructs
     * {@code LdapUserGroupsCache} cache with a refresh interval and custom
     * capacity.
     *
     * @param ldapConnectionSettings
     * @param domain
     */
    public LdapServiceImpl(LdapConnectionSettings ldapConnectionSettings,
            int cacheSize, long refreshInterval, boolean enableLUGCache) {
        this.ldapConnectionSettings = ldapConnectionSettings;
        ldapConnection = new LdapConnection(ldapConnectionSettings);
        context = getLdapContext();
        if (enableLUGCache) {
            this.lugCacheStore = new LdapUserGroupsCache<Object, Object>(
                    refreshInterval, cacheSize);
            LOGGER.log(Level.CONFIG, "Configured AD user groups cache store with refresh interval [ "
                    + refreshInterval
                    + " ] and with capacity [ "
                    + cacheSize
                    + " ]");
        } else {
            LOGGER.log(Level.CONFIG, "No cache has been configured to keep AD user group memberships.");
        }
    }

    /**
     * A setter method used to set {@link LdapConnectionSettings} and creates a
     * {@link LdapConnection} object.
     *
     * @param ldapConnectionSettings to initialize and create
     *            {@link LdapConnection}
     */
    public void setLdapConnectionSettings(
            LdapConnectionSettings ldapConnectionSettings) {
        this.ldapConnectionSettings = ldapConnectionSettings;
        ldapConnection = new LdapConnection(ldapConnectionSettings);

    }

    public LdapUserGroupsCache<Object, Object> getLugCacheStore() {
        return lugCacheStore;
    }

    public LdapConnection getLdapConnection() {
        return ldapConnection;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.google.enterprise.connector.sharepoint.ldap.LdapService#getLdapContext
     * ()
     */
    public LdapContext getLdapContext() {
        return ldapConnection.getLdapContext();
    }

    public static class LdapConnection {

        private final LdapConnectionSettings settings;
        private LdapContext ldapContext = null;

        public LdapConnection(LdapConnectionSettings ldapConnectionSettings) {
            LOGGER.info(ldapConnectionSettings.toString());
            this.settings = ldapConnectionSettings;
            Hashtable<String, String> env = configureLdapEnvironment();
            this.ldapContext = createContext(env);

        }

        /**
         * Returns initial {@link LdapContext}
         */
        public LdapContext getLdapContext() {
            return ldapContext;
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
                LOGGER.log(Level.WARNING, "Could not obtain an initial context to query LDAP (Active Directory) due to a communication failure.", e);
            } catch (AuthenticationNotSupportedException e) {
                LOGGER.log(Level.WARNING, "Could not obtain an initial context to query LDAP (Active Directory) due to authentication not supported exception.", e);
            } catch (AuthenticationException ae) {
                LOGGER.log(Level.WARNING, "Could not obtain an initial context to query LDAP (Active Directory) due to authentication exception.", ae);
            } catch (NamingException e) {
                LOGGER.log(Level.WARNING, "Could not obtain an initial context to query LDAP (Active Directory) due to a naming exception.", e);
            }
            if (ctx == null) {
                return null;
            }
            LOGGER.info("Sucessfully created an Initial LDAP context with the properties : "
                    + env);
            return ctx;
        }

        /**
         * Makes an LDAP or LDAPS URL. The default port for LDAPS URLs is 636
         * where as for LDAP URLs it is 389.
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
         * Initialize the {@link HashTable} used to create an initial LDAP
         * Context. Note that we specifically require a {@link HashTable} rather
         * than a HashMap as the parameter type in the InitialLDAPContext
         * constructor
         *
         * @return initialized {@link HashTable} suitable for constructing an
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

        public LdapConnectionSettings(Method standard, String hostName,
                int port, String baseDN, String domainName) {
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
                    + baseDN + ", connectMethod=" + connectMethod
                    + ", hostname=" + hostName + ", password="
                    + displayPassword + ", port=" + port + ", serverType="
                    + serverType + ", userName=" + userName + ", domainName ="
                    + domainName + " ]";
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
     * Returns a set of all direct groups that the search user belongs to.
     *
     * @param userName search user name
     * @return a set of direct groups that the user belongs to in AD.
     */
    Set<String> getDirectGroupsForTheSearchUser(String userName) {
        // Create the search controls.
        SearchControls searchCtls = makeSearhCtls();
        // Create the search filter.
        String searchFilter = createSearchFilterForDirectGroups(userName);
        // Specify the Base DN for the search.
        String searchBase = ldapConnectionSettings.getBaseDN();
        int totalResults = 0;
        Set<String> directGroups = new HashSet<String>();
        NamingEnumeration<SearchResult> ldapResults = null;
        try {
            ldapResults = this.context.search(searchBase, searchFilter, searchCtls);
            // Loop through the search results
            while (ldapResults.hasMoreElements()) {
                SearchResult sr = (SearchResult) ldapResults.next();
                Attributes attrs = sr.getAttributes();
                if (attrs != null) {
                    try {
                        for (NamingEnumeration<? extends Attribute> ae = attrs.getAll(); ae.hasMore();) {
                            Attribute attr = (Attribute) ae.next();
                            for (NamingEnumeration<?> e = attr.getAll(); e.hasMore(); totalResults++) {
                                directGroups.add(e.next().toString());
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
            try {
                ldapResults.close();
            } catch (NamingException e) {
                LOGGER.log(Level.WARNING, "Exception during clean up of ldap results for the search user : "
                        + userName, e);
            }
        }
        LOGGER.info("[ " + userName + " ] is a direct member of "
                + directGroups.size() + " groups : " + directGroups);
        return directGroups;
    }

    private SearchControls makeSearhCtls() {
        SearchControls searchCtls = new SearchControls();
        // Specify the search scope
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        // Specify the attributes to return
        String returnedAtts[] = { LdapConstants.RETURN_ATTRIBUTES_DIRECT_GROUPS_LIST };
        searchCtls.setReturningAttributes(returnedAtts);
        LOGGER.config("search controles : " + searchCtls);
        return searchCtls;
    }

    private String createSearchFilterForDirectGroups(String userName) {
        StringBuffer filter;
        filter = new StringBuffer().append(LdapConstants.PREFIX_FOR_DIRECT_GROUPS_FILTER
                + userName + SPConstants.DOUBLE_CLOSE_PARENTHESIS);
        LOGGER.config("search filter value for fetching direct gruops :"
                + filter);
        return filter.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.google.enterprise.connector.sharepoint.ldap.LdapService#
     * getAllUsersInGroup(java.lang.String, java.util.Set)
     */
    public void getAllParentGroups(String groupName,
            final Set<String> parentGroupsInfo) {
        if (!Strings.isNullOrEmpty(groupName)) {
            parentGroupsInfo.add(getGroupDNForTheGroup(groupName));
            Set<String> parentGroups = getAllParentGroupsForTheGroup(groupName);
            LOGGER.log(Level.INFO, "Parent groups for the group [" + groupName
                    + "] : " + parentGroups);

            for (String group : parentGroups) {
                getAllParentGroups(group, parentGroupsInfo);
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
        SearchControls searchCtls = makeSearhCtls();
        // Create the search filter
        String searchFilter = createSearchFilterForParentGroups(groupName);
        // Specify the Base DN for the search
        String searchBase = ldapConnectionSettings.getBaseDN();
        NamingEnumeration<SearchResult> ldapResults = null;
        try {
            ldapResults = this.context.search(searchBase, searchFilter, searchCtls);
            while (ldapResults.hasMoreElements()) {
                SearchResult sr = (SearchResult) ldapResults.next();
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
                ldapResults.close();
            } catch (NamingException e) {
                LOGGER.log(Level.WARNING, "Exception during clean up of ldap results.", e);
            }
        }
        return parentGroups;
    }

    private String createSearchFilterForParentGroups(String groupName) {
        StringBuffer filter;
        String groupDN = getGroupDNForTheGroup(groupName);
        filter = new StringBuffer().append(LdapConstants.PREFIX_FOR_PARENTS_GROUPS_FILTER
                + groupDN + SPConstants.DOUBLE_CLOSE_PARENTHESIS);
        return filter.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.google.enterprise.connector.sharepoint.ldap.LdapService#getAllLdapGroups
     * (java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Set<String> getAllLdapGroups(String userName) {
        Set<String> ldapGroups = new HashSet<String>();
        Set<String> directGroups = new HashSet<String>();
        if (Strings.isNullOrEmpty(userName)) {
            return null;
        }
        if (null != userName && null != lugCacheStore) {
            if (null != lugCacheStore.get(userName)) {
                ldapGroups = (Set<String>) lugCacheStore.get(userName, LinkedHashMap.class);
                LOGGER.info("Found valid entry for search user : "
                        + userName
                        + "in cache store and he/she is a direct or indirect member of : "
                        + ldapGroups.size());
                return ldapGroups;
            }
        } else {
            LOGGER.info("No entry found for the user [ "
                    + userName
                    + " ] in cache. Hence querying server to fetch groups, to which the search user belongs to.");
            directGroups = getDirectGroupsForTheSearchUser(userName);
        }
        for (String groupName : directGroups) {
            getAllParentGroups(groupName, ldapGroups);
        }
        LOGGER.info("[ " + userName + " ] is a direct or indirect member of "
                + ldapGroups.size() + " groups");

        if (ldapGroups.size() > 0 && null != lugCacheStore) {
            this.lugCacheStore.put(userName, ldapGroups);
        }
        return ldapGroups;
    }

    /**
     * Returns DN name for the given group while making LDAP search query to get
     * all parents groups for a given group we need to retrieve the DN name for
     * a group.
     *
     * @param groupName
     * @return group DN from group name.
     */
    private String getGroupDNForTheGroup(String groupName) {
        String tmpGroupName;
        tmpGroupName = groupName.substring(0, groupName.indexOf(SPConstants.COMMA));
        tmpGroupName = tmpGroupName.substring(tmpGroupName.indexOf(SPConstants.DOUBLE_EQUAL_TO) + 1);
        return tmpGroupName;
    }

    /*
     * Retrieves SAM account name for the search user for all the possible
     * primary verification identities sent by GSA and is require to query
     * Directory service to fetch all direct groups he belongs to. This
     * implementation is specific to the AD.
     *
     * @param searchUserName search user name.
     */
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

}
