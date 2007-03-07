// Copyright 2007 Google Inc.  All Rights Reserved.

package com.google.enterprise.connector.sharepoint;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 
 * Implementation of the Connector interface from the spi.
 *
 */
public class SharepointConnector implements Connector {
  
  private final SharepointClientContext sharepointClientContext;
  
  public SharepointConnector(String sharepointUrl, String domain, 
                             String username, String password) {
    
    sharepointClientContext = new SharepointClientContext(sharepointUrl, 
        domain, username, password);
    
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
    try {
      URL url = new URL(sharepointUrl);
      sharepointClientContext.setHost(url.getHost());
      if (-1 != url.getPort()) {
        sharepointClientContext.setPort(url.getPort());
      }
      sharepointClientContext.setsiteName(url.getPath());
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
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
