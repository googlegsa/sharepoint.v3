// Copyright 2007 Google Inc.  All Rights Reserved.

package com.google.enterprise.connector.sharepoint;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.SharepointQueryTraversalManager;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;


public class SharepointSession implements Session {
  
  private final SharepointConnector connector;
  private final SharepointClientContext sharepointClientContext;
  
  public SharepointSession(SharepointConnector connector,
      SharepointClientContext sharepointClientContext) throws RepositoryException {
    this.connector = connector;
    this.sharepointClientContext = sharepointClientContext;
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.spi.Session#getAuthenticationManager()
   */
  public AuthenticationManager getAuthenticationManager()
      throws RepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.spi.Session#getAuthorizationManager()
   */
  public AuthorizationManager getAuthorizationManager()
      throws RepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.spi.Session#getQueryTraversalManager()
   */
  public QueryTraversalManager getQueryTraversalManager()
      throws RepositoryException {
    // TODO Auto-generated method stub
    return new SharepointQueryTraversalManager(connector, sharepointClientContext);
  }

}
