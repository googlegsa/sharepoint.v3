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
	private UserGroupsCache<Object, ConcurrentHashMap<String, Set<String>>> lugCacheStore = null;
	private LdapConnection ldapConnection;
	private SharepointClientContext sharepointClientContext;
	private String userSearchBaseDN;

	public UserGroupsService() {

	}

	/**
	 * Initializes LDAP context object for a given {@link LdapConnectionSettings}
	 * and also Constructs {@code LdapUserGroupsCache} cache with a refresh
	 * interval and custom capacity.
	 * 
	 * @param ldapConnectionSettings
	 */
	public UserGroupsService(LdapConnectionSettings ldapConnectionSettings,
			int cacheSize, long refreshInterval, boolean enableLUGCache) {
		this.ldapConnectionSettings = ldapConnectionSettings;
		ldapConnection = new LdapConnection(ldapConnectionSettings);
		context = getLdapContext();
		if (enableLUGCache) {
			this.lugCacheStore = new UserGroupsCache<Object, ConcurrentHashMap<String, Set<String>>>(
					refreshInterval, cacheSize);
			LOGGER.log(Level.CONFIG, "Configured user groups cache store with refresh interval [ "
					+ refreshInterval + " ] and with capacity [ " + cacheSize + " ]");
		} else {
			LOGGER.log(Level.CONFIG, "No cache has been configured to keep user groups memberships.");
		}
	}

	public UserGroupsService(LdapConnectionSettings ldapConnectionSettings,
			SharepointClientContext inSharepointClientContext) {
		if (!Strings.isNullOrEmpty(ldapConnectionSettings.getHostname())
				|| !Strings.isNullOrEmpty(ldapConnectionSettings.getBaseDN())) {
			this.ldapConnectionSettings = ldapConnectionSettings;
			ldapConnection = new LdapConnection(ldapConnectionSettings);
			context = getLdapContext();
		} else {
			LOGGER.warning("Not attempting to create LDAP context, because LDAP host name or base DN is empty or null.");
		}

		this.sharepointClientContext = inSharepointClientContext;
		if (sharepointClientContext.isUseCacheToStoreLdapUserGroupsMembership()) {
			this.lugCacheStore = new UserGroupsCache<Object, ConcurrentHashMap<String, Set<String>>>(
					sharepointClientContext.getCacheRefreshInterval(),
					sharepointClientContext.getInitialCacheSize());
		} else {
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

	public UserGroupsCache<Object, ConcurrentHashMap<String, Set<String>>> getLugCacheStore() {
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
		 * Initialize the {@link HashTable} used to create an initial LDAP Context.
		 * Note that we specifically require a {@link HashTable} rather than a
		 * HashMap as the parameter type in the InitialLDAPContext constructor
		 * 
		 * @return initialized {@link Hashtable} suitable for constructing an
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
			// specify attributes to be returned in binary format
			env.put("java.naming.ldap.attributes.binary", "tokenGroups");
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
		private final String userSearchFilter;

		public LdapConnectionSettings(Method connectMethod, String hostname,
				int port, String baseDN, AuthType authType, String userName,
				String password, String domainName, String userSearchFilter) {
			this.authType = authType;
			this.baseDN = baseDN;
			this.connectMethod = connectMethod;
			this.hostName = hostname;
			this.password = password;
			this.port = port;
			this.serverType = ServerType.getDefault();
			this.userName = userName;
			this.domainName = domainName;
			this.userSearchFilter = userSearchFilter;
		}

		public LdapConnectionSettings(Method standard, String hostName, int port,
				String baseDN, String domainName, String userSearchFilter) {
			this.authType = AuthType.ANONYMOUS;
			this.baseDN = baseDN;
			this.connectMethod = standard;
			this.hostName = hostName;
			this.password = null;
			this.port = port;
			this.serverType = ServerType.getDefault();
			this.userName = null;
			this.domainName = domainName;
			this.userSearchFilter = userSearchFilter;
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
					+ ", domainName =" + domainName + ", userSearchFilter="
					+ userSearchFilter + " ]";
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

		public String getUserSearchFilter() {
			return userSearchFilter;
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
				SearchResult sr = ldapResults.next();
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
			if (null != ldapResults) {
				try {
					ldapResults.close();
				} catch (NamingException e) {
					LOGGER.log(Level.WARNING, "Exception during clean up of ldap results for the search user : "
							+ userName, e);
				}
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
		return searchCtls;
	}

	private String createSearchFilterForDirectGroups(String userName) {
		StringBuffer filter;
		filter = new StringBuffer().append(LdapConstants.PREFIX_FOR_DIRECT_GROUPS_FILTER
				+ userName + SPConstants.DOUBLE_CLOSE_PARENTHESIS);
		LOGGER.config("search filter value for fetching direct groups :" + filter);
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
	public Set<String> getAllLdapGroups(String userName) {
		if (Strings.isNullOrEmpty(userName)) {
			return null;
		}
		Set<String> ldapGroups = new HashSet<String>();
		Set<String> directGroups = new HashSet<String>();
		LOGGER.info("Quering LDAP directory server to fetch all direct groups for the search user: "
				+ userName);
		// fix me by creating a LDAP connection poll instead of creating context
		// object on demand.
		this.context = new LdapConnection(
				sharepointClientContext.getLdapConnectionSettings()).createContext();
		directGroups = getADGroupsForTheSearchUser(userName);

		LOGGER.info("[ " + userName + " ] is a direct or indirect member of "
				+ ldapGroups.size() + " groups");
		if (null != directGroups) {
			directGroups = null;
			this.context = null;
		}
		return ldapGroups;
	}

	/**
	 * Returns DN name for the given group while making LDAP search query to get
	 * all parents groups for a given group we need to retrieve the DN name for a
	 * group.
	 * 
	 * @param groupName
	 * @return group DN from group name.
	 */
	private String getGroupDNForTheGroup(String groupName) {
		String tmpGroupName;
		tmpGroupName = groupName.substring(0, groupName.indexOf(SPConstants.COMMA));
		tmpGroupName = tmpGroupName.substring(tmpGroupName.indexOf(SPConstants.EQUAL_TO) + 1);
		return tmpGroupName;
	}

	/*
	 * Retrieves SAM account name for the search user for all the possible primary
	 * verification identities sent by GSA and is require to query Directory
	 * service to fetch all direct groups he belongs to. This implementation is
	 * specific to the AD.
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

	/**
	 * It is a helper method that returns a set of SPGroups for the search user
	 * and the AD groups of which he/she is a direct or indirect member of.
	 * 
	 * @param searchUser the searchUser
	 * @param adGroups a set of AD groups to which search user is a direct of
	 *          indirect member of.
	 */
	private Set<String> getAllSPGroupsForSearchUserAndLdapGroups(
			String searchUser, Set<String> adGroups) {
		StringBuffer groupName;
		// Search user and SP groups memberships found in user data store.
		List<UserGroupMembership> groupMembershipList = null;
		Set<String> spGroups = new HashSet<String>();
		try {
			if (null != this.sharepointClientContext.getUserDataStoreDAO()) {
				groupMembershipList = this.sharepointClientContext.getUserDataStoreDAO().getAllMembershipsForSearchUserAndLdapGroups(adGroups, searchUser);
				for (UserGroupMembership userGroupMembership : groupMembershipList) {
					// append name space to SP groups.
					// groupName = new
					// StringBuffer().append(SPConstants.LEFT_SQUARE_BRACKET).append(userGroupMembership.getNamespace()).append(SPConstants.RIGHT_SQUARE_BRACKET).append(userGroupMembership.getGroupName());
					spGroups.add(userGroupMembership.getGroupName().toString());
				}
			}
		} catch (SharepointException se) {
			LOGGER.warning("Exception occured while fetching user groups memberships for the search user ["
					+ searchUser + "] and AD groups [" + adGroups + "]");
		} finally {
			if (null != groupMembershipList) {
				groupMembershipList = null;
			}
		}
		return spGroups;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.enterprise.connector.sharepoint.ldap.LdapService#
	 * getAllSearchUserGroups (com.google.enterprise.connector.sharepoint.client.
	 * SharepointClientContext , java.lang.String)
	 */
	public Set<String> getAllGroupsForSearchUser(
			SharepointClientContext sharepointClientContext, String searchUser)
			throws SharepointException {
		ConcurrentHashMap<String, Set<String>> userGroupsMap = new ConcurrentHashMap<String, Set<String>>(
				20);
		Set<String> allUserGroups = new HashSet<String>();
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
		if (null != userGroupsMap) {
			userGroupsMap = null;
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
	Set<String> addGroupNameFormatForTheGroups(String userName,
			Set<String> groupNames) {
		String format = this.sharepointClientContext.getGroupnameFormatInAce();
		LOGGER.config("Groupname format in ACE : " + format);
		String domain, searchUserDomainName = null;
		if (null != this.userSearchBaseDN) {
			searchUserDomainName = getDomainNameFromDN(this.userSearchBaseDN);
		} else {
			if (userName.indexOf(SPConstants.AT) != -1) {
				String[] temp = userName.split(SPConstants.AT);
				searchUserDomainName = temp[1];
			}
		}
		LOGGER.info("Search user domain name : " + searchUserDomainName);
		if (!searchUserDomainName.equalsIgnoreCase(this.sharepointClientContext.getDomain())) {
			domain = searchUserDomainName;
		} else {
			domain = this.sharepointClientContext.getDomain();
		}
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
	private ConcurrentHashMap<String, Set<String>> getAllADGroupsAndSPGroupsForSearchUser(
			String searchUser) {
		ConcurrentHashMap<String, Set<String>> userGroupsMap = new ConcurrentHashMap<String, Set<String>>(
				2);
		Set<String> adGroups = null, spGroups = null;
		Set<String> finalADGroups = new HashSet<String>();
		try {
			adGroups = getADGroupsForTheSearchUser(searchUser);
			if (null != adGroups && adGroups.size() > 0) {
				finalADGroups = addGroupNameFormatForTheGroups(searchUser, adGroups);
			}
			String finalSearchUserName = addUserNameFormatForTheSearchUser(searchUser);
			LOGGER.info("Quering User data store with the AD groups :"
					+ finalADGroups + " and search user [" + finalSearchUserName + "]");
			spGroups = getAllSPGroupsForSearchUserAndLdapGroups(searchUser, finalADGroups);
			userGroupsMap.put(SPConstants.ADGROUPS, finalADGroups);
			userGroupsMap.put(SPConstants.SPGROUPS, spGroups);
		} finally {
			if (null != adGroups) {
				adGroups = finalADGroups = spGroups = null;
			}
		}
		return userGroupsMap;
	}

	Set<String> getADGroupsForTheSearchUser(String searchUserName) {
		SearchControls userSearchCtls = new SearchControls();
		// Specify the search scope
		userSearchCtls.setSearchScope(SearchControls.OBJECT_SCOPE);
		// specify the LDAP search filter to find the user in question
		String actualSearchFilter = replaceUserSearchFilter(this.ldapConnectionSettings.getUserSearchFilter(), searchUserName);
		LOGGER.log(Level.INFO, "actual search filter : " + actualSearchFilter);

		// place holder for an LDAP filter that will store SIDs of the groups the
		// user belongs to
		StringBuffer groupsSearchFilter = new StringBuffer();
		groupsSearchFilter.append("(|");

		// Specify the attributes to return
		String userReturnedAtts[] = { "tokenGroups" };
		userSearchCtls.setReturningAttributes(userReturnedAtts);

		// Specify the Base for the search
		String userSearchBase = getUserSearBase(searchUserName);
		if (null == userSearchBase) {
			userSearchBase = "";
		}

		Set<String> adGroups = new HashSet<String>();
		if (this.context == null) {
			this.context = this.getLdapContext();
		}
		NamingEnumeration<SearchResult> ldapResults = null;
		LOGGER.info("Quering LDAP directory server to fetch all direct groups for the search user: "
				+ searchUserName);
		try {
			ldapResults = this.context.search(userSearchBase, actualSearchFilter, userSearchCtls);

			// Loop through the search results
			while (ldapResults.hasMoreElements()) {

				SearchResult sr = (SearchResult) ldapResults.next();
				Attributes attrs = sr.getAttributes();
				if (attrs != null) {
					try {
						for (NamingEnumeration ae = attrs.getAll(); ae.hasMore();) {
							Attribute attr = (Attribute) ae.next();
							for (NamingEnumeration e = attr.getAll(); e.hasMore();) {
								byte[] sid = (byte[]) e.next();
								groupsSearchFilter.append(LdapConstants.OBJECT_SID
										+ binarySidToStringSid(sid) + SPConstants.END_PARANTHESIS);
							}
							groupsSearchFilter.append(SPConstants.END_PARANTHESIS);
						}
					} catch (NamingException e) {
						System.err.println("Problem listing membership: " + e);
					}
				}
			}
			// Search for groups the user belongs to in order to get their names
			// Create the search controls
			SearchControls groupsSearchCtls = new SearchControls();
			// Specify the search scope
			groupsSearchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			// Specify the Base for the search
			String groupsSearchBase = ldapConnectionSettings.getBaseDN();
			// Specify the attributes to return
			// String groupsReturnedAtts[] = { "sAMAccountName" };
			String groupsReturnedAtts[] = { "sAMAccountName" };
			groupsSearchCtls.setReturningAttributes(groupsReturnedAtts);
			// Search for objects using the filter
			NamingEnumeration<SearchResult> groupsAnswer = this.context.search(groupsSearchBase, groupsSearchFilter.toString(), groupsSearchCtls);
			// Loop through the search results
			while (groupsAnswer.hasMoreElements()) {
				SearchResult sr = (SearchResult) groupsAnswer.next();
				Attributes attrs = sr.getAttributes();
				if (attrs != null && attrs.size() > 0) {
					String groupName = (String) attrs.get(LdapConstants.SAM_ACCOUNT_NAME).get();
					if (null != groupName) {
						adGroups.add(groupName);
					}
					// AllGroups.add((String) attrs.get("sAMAccountName").get());
				}
			}
			// Loop through the search results
		} catch (NamingException ne) {
			LOGGER.log(Level.WARNING, "Failed to retrieve groups for the search user : ["
					+ searchUserName + "]", ne);
		} finally {
			if (null != ldapResults) {
				try {
					ldapResults.close();
				} catch (NamingException e) {
					LOGGER.log(Level.WARNING, "Exception during clean up of ldap results for the search user : "
							+ searchUserName, e);
				}
			}
		}
		LOGGER.info("[ " + searchUserName + " ] is a member of " + adGroups.size()
				+ " groups : " + adGroups);
		return adGroups;
	}

	private String replaceUserSearchFilter(String userSearchFilter,
			String searchUserName) {
		String acetualFilter = userSearchFilter.replace(SPConstants.PECENTILE_S, searchUserName);
		return acetualFilter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.enterprise.connector.sharepoint.ldap.LdapService#getUserSearBase
	 * (java.lang.String)
	 */
	public String getUserSearBase(String userName) {
		String searchFilter;
		// Create the search controls
		SearchControls searchCtls = new SearchControls();
		// Specify the attributes to return
		String returnedAtts[] = { "dn", "distinguishedName" };
		searchCtls.setReturningAttributes(returnedAtts);
		// Specify the search scope

		searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		// specify the LDAP search filter
		if (userName.indexOf(SPConstants.AT) != -1) {
			searchFilter = LdapConstants.USER_PRINCIPAL_NAME + userName + "))";
		} else {
			searchFilter = LdapConstants.SAM_ACCOUNT_NAME_FILTER + userName + "))";
		}

		// Specify the Base for the search
		// an empty dn for all objects from all domains in the forest
		int counter = 0;
		NamingEnumeration<SearchResult> answer;

		// Check for multi domain to specify the base dn in the LDAP query.
		try {
			if (sharepointClientContext.isMultiDomainSupported()) {
				LOGGER.info("using empty dn to query AD.");
				answer = this.context.search("DC=CONNECT,DC=COM", searchFilter, searchCtls);
			} else {
				answer = this.context.search(this.ldapConnectionSettings.getBaseDN(), searchFilter, searchCtls);
			}
			// Loop through the result.
			while (answer.hasMoreElements()) {
				SearchResult sr = (SearchResult) answer.next();
				this.userSearchBaseDN = sr.getNameInNamespace();
				LOGGER.log(Level.INFO, "Base DN for the user [" + userName + "] is : "
						+ sr.getNameInNamespace());
				++counter;
			}
		} catch (NamingException e) {
			LOGGER.log(Level.WARNING, "Failed to retrieve dn for the search user : ["
					+ userName + "]", e);
		}
		if (counter > 1) {
			LOGGER.log(Level.INFO, "Search user [" + userName + "] belongs to "
					+ counter + " domain(s).");
		}
		return userSearchBaseDN;
	}

	public static final String binarySidToStringSid(byte[] SID) {
		String strSID = "";
		// convert the SID into string format
		long version;
		long authority;
		long count;
		long rid;
		strSID = "S";
		version = SID[0];
		strSID = strSID + "-" + Long.toString(version);
		authority = SID[4];

		for (int i = 0; i < 4; i++) {
			authority <<= 8;
			authority += SID[4 + i] & 0xFF;
		}
		strSID = strSID + "-" + Long.toString(authority);
		count = SID[2];
		count <<= 8;
		count += SID[1] & 0xFF;

		for (int j = 0; j < count; j++) {
			rid = SID[11 + (j * 4)] & 0xFF;
			for (int k = 1; k < 4; k++) {
				rid <<= 8;
				rid += SID[11 - k + (j * 4)] & 0xFF;
			}
			strSID = strSID + "-" + Long.toString(rid);
		}
		return strSID;
	}

	public String getUserSearchBaseDN() {
		return userSearchBaseDN;
	}

	public void setUserSearchBaseDN(String userSearchBase) {
		this.userSearchBaseDN = userSearchBase;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.enterprise.connector.sharepoint.ldap.LdapService#getDomainNameFromDN
	 * (java.lang.String)
	 */
	public String getDomainNameFromDN(String dn) {
		if (null == dn) {
			LOGGER.warning("wrong user name or he/she belongs to none of the doamins.");
			return null;
		}
		String[] tokens = dn.split(SPConstants.COMMA);
		for (String token : tokens) {
			if (token.startsWith(LdapConstants.DC)) {
				String[] subTokens = token.split(SPConstants.EQUAL_TO);
				LOGGER.log(Level.INFO, "returning domain name as : " + subTokens[1]);
				return subTokens[1];
			}
		}
		LOGGER.info("dn has no domain information.");
		return null;
	}
}
