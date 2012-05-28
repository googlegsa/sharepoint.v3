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
public class AdGroupsSession implements Session {

  private AdGroupsConnector connector = null;
  private final Logger LOGGER =
      Logger.getLogger(AdGroupsSession.class.getName());

  /**
   * @param connector
   */
  public AdGroupsSession(final AdGroupsConnector connector) {
    LOGGER.info("AdGroupsSession(AdGroupsConnector inConnector)");
    this.connector = connector;
  }

  /**
   * For getting the Authentication Manager using the current connector
   */
  public AuthenticationManager getAuthenticationManager()
      throws RepositoryException {
    LOGGER.info("getAuthenticationManager()");
    return new AdGroupsAuthenticationManager(connector);
  }

  /**
   * For getting the Authorization manager form the current connector context
   */
  @Override
  public AuthorizationManager getAuthorizationManager()
      throws RepositoryException {
    LOGGER.info("getAuthorizationManager()");
    return null;
  }

  /**
   * For getting the Traversal manager form the current connector context
   */
  @Override
  public TraversalManager getTraversalManager()
      throws RepositoryException {
    LOGGER.info("getTraversalManager()");
    return new AdGroupsTraversalManager(connector);
  }
}
