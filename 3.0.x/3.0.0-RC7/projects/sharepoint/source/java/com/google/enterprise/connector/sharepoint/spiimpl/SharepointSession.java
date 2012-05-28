// Copyright 2007 Google Inc.
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

package com.google.enterprise.connector.sharepoint.spiimpl;

import com.google.enterprise.connector.adgroups.AdGroupsAuthenticationManager;
import com.google.enterprise.connector.adgroups.AdGroupsTraversalManager;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.social.SharepointSocialTraversalManager;
import com.google.enterprise.connector.sharepoint.wsclient.client.ClientFactory;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDiscoveryWS;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.TraversalManager;

import java.util.logging.Logger;

/**
 * Implements the Session interface from the spi. It implements methods to
 * return AuthenticationManager, AuthorizationManager and the
 * QueryTraversalManager for the Sharepoint connector to the caller.
 *
 * @author amit_kagrawal
 */
public class SharepointSession implements Session {

  private static final Logger LOGGER =
      Logger.getLogger(SharepointSession.class.getName());

  private final SharepointConnector connector;
  private final SharepointClientContext sharepointClientContext;
  private final Session socialSession;
  private final Session adGroupsSession;

  /**
   * @param inConnector
   * @param inSharepointClientContext
   */
  public SharepointSession(final SharepointConnector inConnector,
      final SharepointClientContext inSharepointClientContext) {
    this(inConnector, inSharepointClientContext, null, null);
  }

  /**
   * @param inConnector
   * @param inSharepointClientContext
   * @param inSocialSession social connection session to be encapsulated
   */
  public SharepointSession(final SharepointConnector inConnector,
      final SharepointClientContext inSharepointClientContext,
      final Session inSocialSession, final Session inAdGroupsSession) {
    connector = inConnector;
    socialSession = inSocialSession;
    adGroupsSession = inAdGroupsSession;
    if (inSharepointClientContext != null) {
      sharepointClientContext = (SharepointClientContext) inSharepointClientContext.clone();
    } else {
      sharepointClientContext = null;
    }
    LOGGER.info("SharepointSession(SharepointConnector inConnector," + 
         " SharepointClientContext inSharepointClientContext)");
  }

  /**
   * For getting the Authentication Manager using the current connector context
   */
  public AuthenticationManager getAuthenticationManager()
      throws RepositoryException {
    LOGGER.info("getAuthenticationManager()");
    return new SharepointAuthenticationManager(connector.getClientFactory(),
        sharepointClientContext,
        adGroupsSession != null ?
            (AdGroupsAuthenticationManager)
                adGroupsSession.getAuthenticationManager()
            : null);
  }

  /**
   * For getting the Authorization manager form the current connector context
   */
  public AuthorizationManager getAuthorizationManager()
      throws RepositoryException {
    LOGGER.info("getAuthorizationManager()");
    ClientFactory clientFactory = connector.getClientFactory();
    SiteDiscoveryWS siteDiscoveryWS =
        clientFactory.getSiteDiscoveryWS(sharepointClientContext, null);
    return new SharepointAuthorizationManager(clientFactory, 
        sharepointClientContext, siteDiscoveryWS.getMatchingSiteCollections());
  }

  /**
   * For getting the Traversal manager form the current connector context
   */
  public TraversalManager getTraversalManager() throws RepositoryException {
    LOGGER.info("getTraversalManager()");
    return new SharepointTraversalManager(
        connector,
        sharepointClientContext,
        socialSession != null ?
          (SharepointSocialTraversalManager) socialSession.getTraversalManager()
          : null,
        adGroupsSession != null ?
            (AdGroupsTraversalManager) adGroupsSession.getTraversalManager()
            : null);
  }

}
