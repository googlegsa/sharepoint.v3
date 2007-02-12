// Copyright 2007 Google Inc.  All Rights Reserved.

package com.google.enterprise.connector.sharepoint;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.SharepointSession;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import java.util.logging.Logger;

public class SharepointConnector implements Connector {
  
  private final SharepointClientContext sharepointClientContext;
  
  public SharepointConnector(String sharepointUrl, String domain, String host,
      int port, String username, String password) {
    sharepointClientContext = new SharepointClientContext(sharepointUrl, domain, host, port, 
                                            username, password);
  }
  
  public void setDomain(String domain) {
    sharepointClientContext.setDomain(domain);
  }
  
  public void setHost(String host) {
    sharepointClientContext.setHost(host);
  }
  
  public void setPort(int port) {
    sharepointClientContext.setPort(port);
  }
  
  public void setSharepointUrl(String sharepointUrl) {
    sharepointClientContext.setSharepointUrl(sharepointUrl);
  }
  
  public void setUsername(String username) {
    sharepointClientContext.setUsername(username);
  }
  
  public void setPassword(String password) {
    sharepointClientContext.setPassword(password);
  }  
  /* (non-Javadoc)
   * @see com.google.enterprise.connector.spi.Connector#login()
   */
  public Session login() throws LoginException, RepositoryException {
    
    return new SharepointSession(this, sharepointClientContext);
  }
}
