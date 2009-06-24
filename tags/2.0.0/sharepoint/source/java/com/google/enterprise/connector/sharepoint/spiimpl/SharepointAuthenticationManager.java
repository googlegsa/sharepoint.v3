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

package com.google.enterprise.connector.sharepoint.spiimpl;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.wsclient.GSBulkAuthorizationWS;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

/**
 * This class provides an implementation of AuthenticationManager SPI provided by CM for authenticating the search users.
 * To understand how this module fits into the Connector Manager framework refer to
 * 	 http://code.google.com/apis/searchappliance/documentation/connectors/110/connector_dev/cdg_authentication.html 
 * @author nitendra_thakur
 */
public class SharepointAuthenticationManager implements AuthenticationManager {
	Logger LOGGER = Logger.getLogger(SharepointAuthenticationManager.class.getName());
	
	SharepointClientContext sharepointClientContext = null;
	
	/**
	 * @param inSharepointClientContext Context Information is required to create the instance of this class
	 */
	public SharepointAuthenticationManager(final SharepointClientContext inSharepointClientContext) throws SharepointException {
		if(inSharepointClientContext == null){
			throw new SharepointException("SharePointClientContext can not be null");
		}
		sharepointClientContext = (SharepointClientContext) inSharepointClientContext.clone();
	}

	/**
	 * Authenticates the user against the SharePoint server where Crawl URL specified during connector configuration is hosted
	 * @param identity AuthenticationIdentity object created by CM while belegating the authetication job to the connector. This corresponds to one specific search user
	 * @return AutheicationResponse Contains the authentication status fo the incoming identity
	 *   
	 */
	public AuthenticationResponse authenticate(final AuthenticationIdentity identity) throws RepositoryLoginException, RepositoryException {
		if(sharepointClientContext==null) {
			LOGGER.warning("SharePointClientContext is null. Authentication Failed.");
			return null;
		}
				
		GSBulkAuthorizationWS bulkAuth = null;
		
		final String user = identity.getUsername();
		final String password = identity.getPassword();
		String logMessage = "Authenticating User: "+user;
		LOGGER.log(Level.INFO, logMessage);
		
		String domain = identity.getDomain();
		// If domain is not received as part of the authentication request, use the one from SharePointClientContext
		if((domain == null) || (domain.length() == 0)) {
			LOGGER.warning("domain not found in the Authentication Request. Using the one from connector's context.");
			domain = sharepointClientContext.getDomain();
		}
		LOGGER.log(Level.FINEST, "Using domain [ " + domain + " ]. ");
		
		final String userName = Util.getUserNameWithDomain(user,domain);
		logMessage = "Trying with username: " + userName;
		LOGGER.log(Level.INFO, logMessage);
		sharepointClientContext.setUsername(userName);
		sharepointClientContext.setPassword(password);
				
		try {
			bulkAuth = new GSBulkAuthorizationWS(sharepointClientContext);
		} catch(final Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to initialize GSBulkAuthorizationWS.", e);
		}
		
		if(bulkAuth==null) {
			LOGGER.warning("Failed to initialize GSBulkAuthorizationWS.");
			return null;
		}		
		
		/* If we can make a call to the Web Service with the given credetial, the user is a valid user.
		 * This should not be assumed as a valid SharePoint user. A valid user is any user who is identified on the SharePoint server.
		 * He may no have any access to the SharePoint site.
		 * The Google Services deployed on the SharePoint can be called with any such user's credential.
		 */
		if(SPConstants.CONNECTIVITY_SUCCESS.equalsIgnoreCase(bulkAuth.checkConnectivity())) {
			logMessage = "Authentication succeded for "+userName;
			LOGGER.log(Level.INFO, logMessage);
			return new AuthenticationResponse(true,"");
		}
		
		logMessage = "Authentication failed for "+user;
		LOGGER.log(Level.WARNING, logMessage);
		return new AuthenticationResponse(false,"");		
	}	
}
