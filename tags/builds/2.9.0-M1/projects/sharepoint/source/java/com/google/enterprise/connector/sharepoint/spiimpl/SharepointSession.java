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

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.social.SharepointSocialTraversalManager;
import com.google.enterprise.connector.sharepoint.wsclient.GSSiteDiscoveryWS;
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

  private SharepointConnector connector = null;
  private SharepointClientContext sharepointClientContext = null;
  private final Logger LOGGER = Logger.getLogger(SharepointSession.class.getName());
  private Session socialSession;

  /**
   * @param inConnector
   * @param inSharepointClientContext
   */
  public SharepointSession(final SharepointConnector inConnector,
      final SharepointClientContext inSharepointClientContext) {
    this(inConnector, inSharepointClientContext, null);
  }

  /**
   * @param inConnector
   * @param inSharepointClientContext
   * @param inSocialSession social connection session to be encapsulated
   */
  public SharepointSession(final SharepointConnector inConnector,
      final SharepointClientContext inSharepointClientContext,
      final Session inSocialSession) {
    /*
     * throws RepositoryException
     */
    if (inConnector != null) {
      connector = inConnector;
    }
    socialSession = inSocialSession;
    if (inSharepointClientContext != null) {
      sharepointClientContext = (SharepointClientContext) inSharepointClientContext.clone();
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
    return new SharepointAuthenticationManager(sharepointClientContext);
  }

  /**
   * For getting the Authorization manager form the current connector context
   */
  public AuthorizationManager getAuthorizationManager()
      throws RepositoryException {
    LOGGER.info("getAuthorizationManager()");
    return new SharepointAuthorizationManager(sharepointClientContext,
        new GSSiteDiscoveryWS(sharepointClientContext, null)
            .getMatchingSiteCollections());
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
          : null);
  }

}
