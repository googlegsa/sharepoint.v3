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

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.wsclient.GSBulkAuthorizationWS;
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
public class SharepointAuthenticationManager implements AuthenticationManager {
    Logger LOGGER = Logger.getLogger(SharepointAuthenticationManager.class.getName());

    SharepointClientContext sharepointClientContext = null;

    /**
     * @param inSharepointClientContext Context Information is required to
     *            create the instance of this class
     */
    public SharepointAuthenticationManager(
            final SharepointClientContext inSharepointClientContext)
            throws SharepointException {
        if (inSharepointClientContext == null) {
            throw new SharepointException(
                    "SharePointClientContext can not be null");
        }
        sharepointClientContext = (SharepointClientContext) inSharepointClientContext.clone();
    }

    /**
     * Authenticates the user against the SharePoint server where Crawl URL
     * specified during connector configuration is hosted
     *
     * @param identity AuthenticationIdentity object created by CM while
     *            delegating authentication to the connector. This
     *            corresponds to one specific search user
     * @return AutheicationResponse Contains the authentication status for the
     *         incoming identity
     */
    public AuthenticationResponse authenticate(
            final AuthenticationIdentity identity)
            throws RepositoryLoginException, RepositoryException {
        if (sharepointClientContext == null) {
            LOGGER.warning("SharePointClientContext is null. Authentication Failed.");
            return null;
        }

        GSBulkAuthorizationWS bulkAuth = null;

        final String user = identity.getUsername();
        final String password = identity.getPassword();
        String domain = identity.getDomain();

        LOGGER.log(Level.INFO, "Received authN request for Username [ "
                + user + " ], domain [ " + domain + " ]. ");

        // If domain is not received as part of the authentication request, use
        // the one from SharePointClientContext
        if ((domain == null) || (domain.length() == 0)) {
            domain = sharepointClientContext.getDomain();
        }

        final String userName = Util.getUserNameWithDomain(user, domain);
        LOGGER.log(Level.INFO, "Authenticating User: " + userName);

        sharepointClientContext.setUsername(userName);
        sharepointClientContext.setPassword(password);

        try {
            bulkAuth = new GSBulkAuthorizationWS(sharepointClientContext);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize GSBulkAuthorizationWS.", e);
            return null;
        }

        /*
         * If we can make a call to the Web Service with the given credetial,
         * the user is a valid user. This should not be assumed as a valid
         * SharePoint user. A valid user is any user who is identified on the
         * SharePoint server. He may no have any access to the SharePoint site.
         * The Google Services deployed on the SharePoint can be called with any
         * such user's credential.
         */
        if (SPConstants.CONNECTIVITY_SUCCESS.equalsIgnoreCase(bulkAuth.checkConnectivity())) {
            LOGGER.log(Level.INFO, "Authentication succeded for " + userName);
            return new AuthenticationResponse(true, "");
        }

        LOGGER.log(Level.WARNING, "Authentication failed for " + user);
        return new AuthenticationResponse(false, "");
    }
}