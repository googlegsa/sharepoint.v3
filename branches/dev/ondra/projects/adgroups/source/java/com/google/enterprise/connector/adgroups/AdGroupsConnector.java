//Copyright 2007 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.adgroups;

import com.google.common.base.Strings;
import com.google.enterprise.connector.sharepoint.dao.ConnectorNamesDAO;
import com.google.enterprise.connector.sharepoint.dao.QueryProvider;
import com.google.enterprise.connector.sharepoint.dao.UserDataStoreDAO;
import com.google.enterprise.connector.sharepoint.dao.UserGroupMembershipRowMapper;
import com.google.enterprise.connector.adgroups.LdapConstants.AuthType;
import com.google.enterprise.connector.adgroups.LdapConstants.Method;
import com.google.enterprise.connector.adgroups.UserGroupsService.LdapConnectionSettings;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorPersistentStore;
import com.google.enterprise.connector.spi.ConnectorPersistentStoreAware;
import com.google.enterprise.connector.spi.ConnectorShutdownAware;
import com.google.enterprise.connector.spi.LocalDatabase;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the Connector interface from the spi for SharePoint This is
 * the primary class which represents a new connector instance. Every time a new
 * connector instance is created, an object of this class is created.
 * 
 * @author nitendra_thakur
 */
public class AdGroupsConnector implements Connector,
		ConnectorPersistentStoreAware, ConnectorShutdownAware {
	private static final Logger LOGGER = Logger.getLogger(AdGroupsConnector.class.getName());
	private ConnectorContext sharepointClientContext = null;

	private String domain;
	private String username;
	private String password;
	private String googleConnectorWorkDir = null;
	private String usernameFormatInAce;
	private String groupnameFormatInAce;
	private QueryProvider queryProvider;
	private UserGroupMembershipRowMapper userGroupMembershipRowMapper;
	private ConnectorPersistentStore connectorPersistnetStore;
	private String ldapServerHostAddress;
	private String ldapUserName;
	private String ldapPassword;
	private String ldapDomain;
	private String portNumber;
	private String authenticationType;
	private String connectMethod;
	private String searchBase;
	private String initialCacheSize;
	private boolean useCacheToStoreLdapUserGroupsMembership;
	private String cacheRefreshInterval;
	private LdapConnectionSettings ldapConnectionSettings;
	private LocalDatabase localDatabseImpl;
	private ConnectorNamesDAO connectorNamesDAO;
	private String connectorName;
	private UserDataStoreDAO userDataStoreDAO;

	public AdGroupsConnector() {
	}

	/**
	 * returns a session object for the current connector instance
	 */
	public Session login() throws RepositoryException {
		LOGGER.info("Connector login()");
		connectorNamesDAO = new ConnectorNamesDAO(
			localDatabseImpl.getDataSource(), queryProvider);
		// Add current connector instance name to the database table.
		connectorNamesDAO.addConnectorInstanceName(connectorName);
		return new AdGroupsSession(this, sharepointClientContext);
	}

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @param domain the domain to set
	 */
	public void setDomain(final String domain) {
		this.domain = domain;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(final String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * @return the googleConnectorWorkDir
	 */
	public String getGoogleConnectorWorkDir() {
		return googleConnectorWorkDir;
	}

	/**
	 * @param googleConnectorWorkDir the googleConnectorWorkDir to set
	 */
	public void setGoogleConnectorWorkDir(final String googleConnectorWorkDir) {
		this.googleConnectorWorkDir = googleConnectorWorkDir;
	}

	/**
	 * @param googleConnectorName the connector instance name
	 */
	public void setGoogleConnectorName(String googleConnectorName) {
		this.connectorName = googleConnectorName;
	}

	public void init() throws RepositoryException {
		LOGGER.config("domain = ["
				+ domain + "] , username = [" + username + "] , "
				+ "googleConnectorWorkDir = [" + googleConnectorWorkDir
				+ "], useCacheToStoreLdapUserGroupsMembership = ["
				+ useCacheToStoreLdapUserGroupsMembership + "], initialCacheSize = ["
				+ initialCacheSize + "], cacheRefreshInterval = ["
				+ cacheRefreshInterval + "], ldapServerHostAddress = ["
				+ ldapServerHostAddress + "], portNumber = [" + portNumber
				+ "], authenticationType = [" + authenticationType
				+ "], connectMethod = [" + connectMethod + "], searchBase = ["
				+ searchBase + " ]" + "]");

		sharepointClientContext = new ConnectorContext(/*sharepointUrl,*/
                    domain, username, password, googleConnectorWorkDir);
		sharepointClientContext.setUsernameFormatInAce(getUsernameFormatInAce());
		sharepointClientContext.setGroupnameFormatInAce(this.getGroupnameFormatInAce());
		sharepointClientContext.setDomain(this.domain);

		sharepointClientContext.setLdapConnectionSettings(getLdapConnectionSettings());
		sharepointClientContext.setUseCacheToStoreLdapUserGroupsMembership(this.useCacheToStoreLdapUserGroupsMembership);
		if (useCacheToStoreLdapUserGroupsMembership) {
			sharepointClientContext.setCacheRefreshInterval(Long.parseLong(this.cacheRefreshInterval));
			sharepointClientContext.setInitialCacheSize(Integer.parseInt(this.initialCacheSize));
		}
		
		LOGGER.info("Starting crawl");
		
		sharepointClientContext.multiCrawl = new MultiCrawl(sharepointClientContext);
		sharepointClientContext.multiCrawl.start();
	}

	public String getLdapUserName() {
		return ldapUserName;
	}

	public void setLdapUserName(String ldapUserName) {
		this.ldapUserName = ldapUserName;
	}

	public String getLdapPassword() {
		return ldapPassword;
	}

	public void setLdapPassword(String ldapPassword) {
		this.ldapPassword = ldapPassword;
	}

	public String getLdapDomain() {
		return ldapDomain;
	}

	public void setLdapDomain(String ldapDomain) {
		this.ldapDomain = ldapDomain;
	}

	public void setQueryProvider(QueryProvider queryProvider) {
		this.queryProvider = queryProvider;
	}

	public void setUserGroupMembershipRowMapper(
			UserGroupMembershipRowMapper userGroupMembershipRowMapper) {
		this.userGroupMembershipRowMapper = userGroupMembershipRowMapper;
	}

	public String getUsernameFormatInAce() {
		return usernameFormatInAce;
	}

	public void setUsernameFormatInAce(String usernameFormatInAce) {
		this.usernameFormatInAce = usernameFormatInAce;
	}

	public String getGroupnameFormatInAce() {
		return groupnameFormatInAce;
	}

	public void setGroupnameFormatInAce(String groupnameFormatInAce) {
		this.groupnameFormatInAce = groupnameFormatInAce;
	}

	public void setDatabaseAccess(ConnectorPersistentStore databaseAccess) {
		this.connectorPersistnetStore = databaseAccess;
		performUserDataStoreInitialization();
	}

	/**
	 * Perform initialization steps that are required to create User Data Store
	 * object. It also loads and register corresponding sqlQueries.properties for
	 * selected data base.
	 */
	private void performUserDataStoreInitialization() {
		localDatabseImpl = connectorPersistnetStore.getLocalDatabase();
		String locale = localDatabseImpl.getDatabaseType().name();
		LOGGER.config("Data base type : " + locale);
		if (null == locale || locale.length() == 0) {
			locale = "mssql";
		}
		queryProvider.setDatabase(locale);
		try {
			queryProvider.init(locale);
			userDataStoreDAO = new UserDataStoreDAO(localDatabseImpl.getDataSource(),
					queryProvider, userGroupMembershipRowMapper);
			LOGGER.config("DAO for UserDataStore created successfully");
		} catch (RepositoryException se) {
			LOGGER.log(Level.WARNING, "Failed to create UserDataStoreDAO object. ", se);
		}
		sharepointClientContext.setUserDataStoreDAO(userDataStoreDAO);
	}

	/**
	 * @return LDAp directory service host address.
	 */
	public String getLdapServerHostAddress() {
		return ldapServerHostAddress;
	}
	
	/**
	 * @param ldapServerHostAddress the ldapServerHostAddress to set.
	 */
	public void setLdapServerHostAddress(String ldapServerHostAddress) {
		this.ldapServerHostAddress = ldapServerHostAddress;
	}
	
	/**
	 * @return LDAP directory server port number.
	 */
	public String getPortNumber() {
		return portNumber;
	}

	/**
	 * @param portNumber the portNumber to set.
	 */
	public void setPortNumber(String portNumber) {
		if (Strings.isNullOrEmpty(portNumber)) {
			this.portNumber = SPConstants.LDAP_DEFAULT_PORT_NUMBER;
		} else {


			this.portNumber = portNumber;
		}
	}

	/**
	 * @return LDAP Authentication Type used to connect to LDAP directory server.
	 */
	public String getAuthenticationType() {
		AuthType authType;
		if (AuthType.ANONYMOUS.toString().equalsIgnoreCase(this.authenticationType.toString())) {
			authType = AuthType.ANONYMOUS;
		} else {
			authType = AuthType.SIMPLE;
		}
		return authType.toString();
	}

	/**
	 * @param authenticationType the authenticationType to set.
	 */
	public void setAuthenticationType(String authenticationType) {
		this.authenticationType = authenticationType;
	}

	/**
	 * @return LDAP directory server connect method.
	 */
	public String getConnectMethod() {
		Method method;
		if (Method.SSL.toString().equalsIgnoreCase(this.connectMethod.toString())) {
			method = Method.SSL;
		} else {
			method = Method.STANDARD;
		}
		return method.toString();
	}

	/**
	 * @param connectMethod the connectMethod to set.
	 */
	public void setConnectMethod(String connectMethod) {
		this.connectMethod = connectMethod;
	}

	/**
	 * @return LDAP user search base.
	 */
	public String getSearchBase() {
		return searchBase;
	}

	/**
	 * @param searchBase the searchBase to set.
	 */
	public void setSearchBase(String searchBase) {
		this.searchBase = searchBase;
	}

	/**
	 * @return LDAP user groups initial cache size.
	 */
	public String getInitialCacheSize() {
		return initialCacheSize;
	}

	/**
	 * @param initialCacheSize the initialCacheSize to set.
	 */
	public void setInitialCacheSize(String initialCacheSize) {
		this.initialCacheSize = initialCacheSize;
	}

	/**
	 * @return true indicates to create a LDAP user groups membership cache.
	 */
	public boolean isUseCacheToStoreLdapUserGroupsMembership() {
		return useCacheToStoreLdapUserGroupsMembership;
	}

	/**
	 * @param useCacheToStoreLdapUserGroupsMembership the
	 *          useCacheToStoreLdapUserGroupsMembership to set.
	 */
	public void setUseCacheToStoreLdapUserGroupsMembership(
			boolean useCacheToStoreLdapUserGroupsMembership) {
		this.useCacheToStoreLdapUserGroupsMembership = useCacheToStoreLdapUserGroupsMembership;
	}

	/**
	 * @return refresh interval time in seconds.
	 */
	public String getCacheRefreshInterval() {
		return cacheRefreshInterval;
	}

	/**
	 * @param cacheRefreshInterval the cacheRefreshInterval to set.
	 */
	public void setCacheRefreshInterval(String cacheRefreshInterval) {
		this.cacheRefreshInterval = cacheRefreshInterval;
	}

	/**
	 * @return {@linkplain LdapConnectionSettings}
	 */
	public LdapConnectionSettings getLdapConnectionSettings() {
		AuthType authType;
		if (AuthType.ANONYMOUS.toString().equalsIgnoreCase(this.authenticationType.toString())) {
			authType = AuthType.ANONYMOUS;
		} else {
			authType = AuthType.SIMPLE;
		}
		Method method;
		if (Method.SSL.toString().equalsIgnoreCase(this.connectMethod.toString())) {
			method = Method.SSL;
		} else {
			method = Method.STANDARD;
		}
		LdapConnectionSettings ldapConnectionSettings = new LdapConnectionSettings(
				method, this.ldapServerHostAddress, Integer.parseInt(this.portNumber),
				this.searchBase, authType, this.ldapUserName, this.ldapPassword, this.ldapDomain);
		// XXX: What's the point of this field? Should we use
		// it to construct the settings only once here?
		this.ldapConnectionSettings = ldapConnectionSettings;
		return ldapConnectionSettings;
	}

	/**
	 * @param ldapConnectionSettings the ldapConnectiionSettings to set.
	 */
	public void setLdapConnectionSettings(
			LdapConnectionSettings ldapConnectionSettings) {
		this.ldapConnectionSettings = ldapConnectionSettings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.enterprise.connector.spi.ConnectorShutdownAware#shutdown()
	 */
	public void shutdown() throws RepositoryException {
		LOGGER.info("Shutting down the connector with the name [" + connectorName
				+ "]");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.enterprise.connector.spi.ConnectorShutdownAware#delete()
	 */
	public void delete() throws RepositoryException {
		if (!ConnectorNamesDAO.connectorNames.isEmpty()
				&& connectorNamesDAO.getAllConnectorNames().size() > 0) {
			LOGGER.info("Deleting the connector with the name [" + connectorName
					+ "] from the database table.");
			// Removes the connector name from the database table.
			connectorNamesDAO.removeConnectorName(connectorName);
			Set<String> nameSpaceForTheConnector = new HashSet<String>();
                        // XXX TODO: Use local groups namespace?
			// String tempURL = this.sharepointUrl.trim();
			// if (tempURL.endsWith(SPConstants.SLASH)) {
			// 	tempURL = tempURL.substring(0, tempURL.length() - 1);
			// }
                        String tempURL = connectorName;
			nameSpaceForTheConnector.add(tempURL);
			LOGGER.info("Deleting all memberships for the connector" + connectorName
					+ " using the name space [" + nameSpaceForTheConnector + "]");
			userDataStoreDAO.removeAllMembershipsFromNamespace(nameSpaceForTheConnector);
			if (ConnectorNamesDAO.connectorNames.isEmpty()
					&& connectorNamesDAO.getAllConnectorNames().size() == 0) {
				LOGGER.log(Level.INFO, "Dropping the user data store table from the data base.");
				// Removes the user data store table from the database.
				userDataStoreDAO.dropUserDataStoreTable();
				// Removes the connector names table from the database.
				LOGGER.log(Level.INFO, "Dropping the connector names table from the data base.");
				connectorNamesDAO.dropConnectorNamesTable();
			}
		}
	}

	public ConnectorNamesDAO getConnectorNamesDAO() {
		return connectorNamesDAO;
	}

	public void setConnectorNamesDAO(ConnectorNamesDAO connectorNamesDAO) {
		this.connectorNamesDAO = connectorNamesDAO;
	}

	public String getConnectorName() {
		return connectorName;
	}

	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
	}

	public UserDataStoreDAO getUserDataStoreDAO() {
		return userDataStoreDAO;
	}

	public void setUserDataStoreDAO(UserDataStoreDAO userDataStoreDAO) {
		this.userDataStoreDAO = userDataStoreDAO;
	}

}
