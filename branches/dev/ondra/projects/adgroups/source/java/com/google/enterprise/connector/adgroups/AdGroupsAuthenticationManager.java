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
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

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

	AdGroupsConnector connector = null;

	/**
	 * @param inSharepointClientContext Context Information is required to create
	 *          the instance of this class
	 */
	public AdGroupsAuthenticationManager(
			final AdGroupsConnector connector)
			throws RepositoryException {
		this.connector = connector;
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
		final String user = identity.getUsername();
		final String password = identity.getPassword();
		String domain = identity.getDomain();

		LOGGER.log(Level.INFO, "Received authN request for Username [ " + user
				+ " ], domain [ " + domain + " ]. ");

		LOGGER.log(Level.INFO, "Received authN request for Username [ " + user
				+ " ], domain [ " + domain + " ]. ");

        // TODO: Only non-null, empty does not imply group lookup.
		if (!Strings.isNullOrEmpty(password)) {
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
		/*Set<String> groups = this.ldapService.getAllGroupsForSearchUser(sharepointClientContext, identity);
		if (null != groups && groups.size() > 0) {
			// Should return true is there is at least one group returned by
			// LDAP service.
			StringBuffer buf = new StringBuffer(
					"Group resolution service returned following groups for the search user: ").append(identity.getUsername()).append(" \n").append(groups.toString());
			LOGGER.info(buf.toString());
			return new AuthenticationResponse(true, "", groups);
		}
		LOGGER.info("Group resolution service returned no groups for the search user: "
				+ identity.getUsername());
		// Should returns true with null groups.*/
		return new AuthenticationResponse(true, "", null);
	}
}
