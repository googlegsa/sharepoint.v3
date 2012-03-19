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
import com.google.enterprise.connector.adgroups.UserGroupsService.LdapConnectionSettings;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides an implementation of AuthenticationManager SPI provided
 * by CM for authenticating the search users. To understand how this module fits
 * into the Connector Manager framework refer to
 * http://code.google.com/apis/searchappliance
 * /documentation/connectors/110/connector_dev/cdg_authentication.html
 * 
 * @author nitendra_thakur
 */
public class AdGroupsAuthenticationManager implements AuthenticationManager {
	Logger LOGGER = Logger.getLogger(AdGroupsAuthenticationManager.class.getName());

	ConnectorContext sharepointClientContext = null;
	LdapService ldapService = null;

	/**
	 * @param inSharepointClientContext Context Information is required to create
	 *          the instance of this class
	 */
	public AdGroupsAuthenticationManager(
			final ConnectorContext inSharepointClientContext)
			throws RepositoryException {
		if (inSharepointClientContext == null) {
			throw new RepositoryException("Context can not be null");
		}
		sharepointClientContext = (ConnectorContext) inSharepointClientContext.clone();
		if (!sharepointClientContext.isMultiDomain()) {
			LdapConnectionSettings ldapConnectionSettings = sharepointClientContext.getLdapConnectionSettings();
			if (!Strings.isNullOrEmpty(ldapConnectionSettings.getHostname())
					&& !Strings.isNullOrEmpty(ldapConnectionSettings.getBaseDN())) {
				ldapService = new UserGroupsService(inSharepointClientContext);
			}
		} else {
			ldapService = new MultiLdapService(inSharepointClientContext);
		}
	}

	/**
	 * Authenticates the user against the SharePoint server where Crawl URL
	 * specified during connector configuration is hosted
	 * 
	 * @param identity AuthenticationIdentity object created by CM while
	 *          delegating authentication to the connector. This corresponds to
	 *          one specific search user
	 * @return AutheicationResponse Contains the authentication status for the
	 *         incoming identity
	 */
	public AuthenticationResponse authenticate(
			final AuthenticationIdentity identity) throws RepositoryLoginException,
			RepositoryException {
		if (sharepointClientContext == null) {
			LOGGER.warning("SharePointClientContext is null. Authentication Failed.");
			return null;
		}

		final String user = identity.getUsername();
		final String password = identity.getPassword();
		String domain = identity.getDomain();

		LOGGER.log(Level.INFO, "Received authN request for Username [ " + user
				+ " ], domain [ " + domain + " ]. ");

		LOGGER.log(Level.INFO, "Received authN request for Username [ " + user
				+ " ], domain [ " + domain + " ]. ");

		// If domain is not received as part of the authentication request, use
		// the one from SharePointClientContext
		if ((domain == null) || (domain.length() == 0)) {
			domain = sharepointClientContext.getDomain();
		}

                // TODO: Only non-null, empty does not imply group lookup.
		if (!Strings.isNullOrEmpty(password)) {
			final String userName = Util.getUserNameWithDomain(user, domain);
			LOGGER.log(Level.INFO, "Authenticating User: " + userName);
			LOGGER.log(Level.WARNING, "Authentication failed for " + user);
			return new AuthenticationResponse(false, "", null);
		} else {
			LOGGER.config("AuthN was not attempted as password is empty and groups are being returned.");
			return getAllGroupsForTheUser(identity);
		}
	}

	/**
	 * This method makes a call to {@link LdapService} to get all AD groups and SP
	 * groups of which he/she is a direct or indirect member of and returns
	 * {@link AuthenticationResponse}.
	 * 
	 * @param searchUser to fetch all SharePoint groups and Directory groups to
	 *          which he/she is a direct or indirect member of.
	 * @return {@link AuthenticationResponse}
	 * @throws RepositoryException
	 */
	AuthenticationResponse getAllGroupsForTheUser(AuthenticationIdentity identity)
			throws RepositoryException {
		LOGGER.info("Attempting group resolution for user : " + identity.getUsername());
		Set<String> allSearchUserGroups = this.ldapService.getAllGroupsForSearchUser(sharepointClientContext, identity);
		Set<String> finalGroupNames = encodeGroupNames(allSearchUserGroups);
		if (null != finalGroupNames && finalGroupNames.size() > 0) {
			// Should return true is there is at least one group returned by
			// LDAP service.
			StringBuffer buf = new StringBuffer(
					"Group resolution service returned following groups for the search user: ").append(identity.getUsername()).append(" \n").append(finalGroupNames.toString());
			LOGGER.info(buf.toString());
			return new AuthenticationResponse(true, "", allSearchUserGroups);
		}
		LOGGER.info("Group resolution service returned no groups for the search user: "
				+ identity.getUsername());
		// Should returns true with null groups.
		return new AuthenticationResponse(true, "", null);
	}

	/**
	 * Returns a set of encoded group names by iterating to all the groups.
	 * 
	 * @param allSearchUserGroups set of group names to encode.
	 */
	private Set<String> encodeGroupNames(Set<String> allSearchUserGroups) {
		Set<String> tmpGroups = new HashSet<String>();
		if (null != allSearchUserGroups && allSearchUserGroups.size() > 0) {
			for (String groupName : allSearchUserGroups) {
				tmpGroups.add(StringEscapeUtils.escapeXml(groupName));
			}
			return tmpGroups;
		} else {
			LOGGER.info("Received zero or null groups to encode.");
			return null;
		}
	}

}
