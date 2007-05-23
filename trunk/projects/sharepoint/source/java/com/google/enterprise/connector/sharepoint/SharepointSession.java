// Copyright 2007 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package com.google.enterprise.connector.sharepoint;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

/**
 * Implements the Session interface from the spi.
 * It implements methods to return AuthenticationManager, AuthorizationManager
 * and the QueryTraversalManager for the Sharepoint connector to the caller.
 *
 */
public class SharepointSession implements Session {
  
  private final SharepointConnector connector;
  private final SharepointClientContext sharepointClientContext;
  
  public SharepointSession(SharepointConnector connector,
      SharepointClientContext sharepointClientContext) 
      throws RepositoryException {
    this.connector = connector;
    this.sharepointClientContext = sharepointClientContext;    
  }

  public AuthenticationManager getAuthenticationManager()
      throws RepositoryException {
    return null;
  }

  public AuthorizationManager getAuthorizationManager()
      throws RepositoryException {
    // TODO(meghna) Implment this.
    return null;
  }

  public TraversalManager getTraversalManager()
      throws RepositoryException {
    return new SharepointTraversalManager(connector, 
        sharepointClientContext);
  }
}
