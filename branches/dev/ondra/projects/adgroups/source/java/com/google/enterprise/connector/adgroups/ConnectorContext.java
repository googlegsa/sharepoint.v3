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

import com.google.enterprise.connector.sharepoint.dao.UserDataStoreDAO;
import com.google.enterprise.connector.adgroups.UserGroupsService.LdapConnectionSettings;
import com.google.enterprise.connector.spi.RepositoryException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//FIXME Should we can get rid of this class since it is unnecessarily creating a hop between SharePointConector and spi implementations
// config values can be passed to the appropriate classes directly using IoC.

/**
 * Class to hold the context information for sharepoint client connection. The
 * information is per connector instance.
 * 
 * @author nitendra_thakur
 */
public class ConnectorContext implements Cloneable {

	private final Logger LOGGER = Logger.getLogger(ConnectorContext.class.getName());
	private String domain;
	private String username;
	private String password;
	private String googleConnectorWorkDir = null;

	private String usernameFormatInAce;
	private String groupnameFormatInAce;

	private UserDataStoreDAO userDataStoreDAO;

	private int initialCacheSize;
	private boolean useCacheToStoreLdapUserGroupsMembership;
	private long cacheRefreshInterval;
	private LdapConnectionSettings ldapConnectionSettings;
	
	public MultiCrawl multiCrawl;

	public LdapConnectionSettings getLdapConnectionSettings() {
		return ldapConnectionSettings;
	}

	public void setLdapConnectionSettings(
			LdapConnectionSettings ldapConnectionSettings) {
		this.ldapConnectionSettings = ldapConnectionSettings;
	}

	/**
	 * For cloning
	 */
	public Object clone() {
		try {
			final ConnectorContext spCl = new ConnectorContext();

			if (null != domain) {
				spCl.setDomain(new String(domain));
			}

			if (null != password) {
				spCl.setPassword(new String(password));
			}

			if (null != username) {
				spCl.setUsername(new String(username));
			}

			if (null != userDataStoreDAO) {
				// It's ok if we do a shallow copy here
				spCl.userDataStoreDAO = this.userDataStoreDAO;
			}

			spCl.setUsernameFormatInAce(this.getUsernameFormatInAce());
			spCl.setGroupnameFormatInAce(this.getGroupnameFormatInAce());
			spCl.setLdapConnectionSettings(this.ldapConnectionSettings);
			spCl.setUseCacheToStoreLdapUserGroupsMembership(this.useCacheToStoreLdapUserGroupsMembership);
			spCl.setInitialCacheSize(this.initialCacheSize);
			spCl.setCacheRefreshInterval(this.cacheRefreshInterval);
			spCl.multiCrawl = this.multiCrawl;

			return spCl;
		} catch (final Throwable e) {
			LOGGER.log(Level.FINEST, "Unable to clone client context.", e);
			return null;
		}
	}

	/**
	 * Default constructor
	 */
	private ConnectorContext() {
	}

	/**
	 * @param inDomain
	 * @param inUsername
	 * @param inPassword
	 * @param inGoogleConnectorWorkDir
	 * @throws RepositoryException
	 */
	public ConnectorContext(final String inDomain,
			final String inUsername, final String inPassword,
			final String inGoogleConnectorWorkDir) throws RepositoryException {
		if (inUsername == null) {
			throw new RepositoryException("Username is null.");
		}
		if (inPassword == null) {
			throw new RepositoryException("Password is null.");
		}

		if ((inDomain == null) || inDomain.trim().equals("")) {
			LOGGER.log(Level.CONFIG, "Trying to get domain information from username specified [ "
					+ inUsername
					+ " ] because domain field has not been explicitly specified.");
			domain = Util.getDomainFromUsername(inUsername);
		} else {
			domain = inDomain;
		}
		LOGGER.finest("domain set to " + domain);

		username = Util.getUserFromUsername(inUsername);
		LOGGER.finest("username set to " + username);

		password = inPassword;
		googleConnectorWorkDir = inGoogleConnectorWorkDir;
		LOGGER.finest("googleConnectorWorkDir set to " + googleConnectorWorkDir);

		LOGGER.config("domain = ["
				+ inDomain + "] , username = [" + inUsername
				+ "] , googleConnectorWorkDir = [" + inGoogleConnectorWorkDir
				+ "]");
	}

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the connector instance directory
	 */
	public String getGoogleConnectorWorkDir() {
		return googleConnectorWorkDir;
	}

	/**
	 * @param indomain
	 */
	public void setDomain(final String indomain) {
		domain = indomain;
	}

	/**
	 * @param inPassword
	 */
	public void setPassword(final String inPassword) {
		password = inPassword;
	}

	/**
	 * @param inUsername
	 */
	public void setUsername(final String inUsername) {
		username = inUsername;
	}

	/**
	 * @param workDir
	 */
	public void setGoogleConnectorWorkDir(final String workDir) {
		googleConnectorWorkDir = workDir;
	}

	public UserDataStoreDAO getUserDataStoreDAO() {
		return userDataStoreDAO;
	}

	public void setUserDataStoreDAO(UserDataStoreDAO userDataStoreDAO) {
		this.userDataStoreDAO = userDataStoreDAO;
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

	/**
	 * @return initial LDAP user groups membership cache size.
	 */
	public int getInitialCacheSize() {
		return initialCacheSize;
	}

	/**
	 * @param initialCacheSize the initialCacheSize to set.
	 */
	public void setInitialCacheSize(int initialCacheSize) {
		this.initialCacheSize = initialCacheSize;
	}

	/**
	 * @return true if connector administrator configure to use cache for LDAP
	 *         user groups membership.
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
	 * @return the interval to which LDAP cache should invalidate its cache.
	 */
	public long getCacheRefreshInterval() {
		return cacheRefreshInterval;
	}

	/**
	 * @param cacheRefreshInterval the cacheRefreshInterval to set.
	 */
	public void setCacheRefreshInterval(long cacheRefreshInterval) {
		this.cacheRefreshInterval = cacheRefreshInterval;
	}

	public boolean isMultiDomain() {
		 return ldapConnectionSettings.getHostname().contains("|");
	}
}
