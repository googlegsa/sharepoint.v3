// Copyright 2007 Google Inc.  All Rights Reserved.
package com.google.enterprise.connector.sharepoint.client;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.Stub;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

/**
 * Class to hold the context information for sharepoint client connection.
 *
 */

public class SharepointClientContext {

  private String sharepointUrl;
  private String domain;
  private String username;
  private String password;
  private int port = 80;
  private String host;
  
  public SharepointClientContext(String sharepointUrl, String domain, String host,
      int port, String username, String password) {
    this.sharepointUrl = sharepointUrl;
    this.domain = domain;
    this.host = host;
    this.port = port;
    this.username = username;
    this.password = password;
  }

 
  public String getDomain() {
    return domain;
  }

  public String getHost() {
    return host;
  }

  public String getPassword() {
    return password;
  }

  public int getPort() {
    return port;
  }

  public String getSharepointUrl() {
    return sharepointUrl;
  }

  public String getUsername() {
    return username;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setSharepointUrl(String sharepointUrl) {
    this.sharepointUrl = sharepointUrl;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void setPort(int port) {
    this.port = port;
  }

  /**
   * Sets the stub 
   * @param stub Stub Axis Client Stub to call the webservices on Sharepoint server.
   * @param endpoint Suffix to the particular webserive to use.
   */
  public void setStubWithAuth(Stub stub, String endpoint) {
    Options options = new Options();
    EndpointReference target = new EndpointReference(endpoint);
    options.setTo(target);
    HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
    auth.setDomain(domain);
    auth.setUsername(username);
    auth.setPassword(password);
    auth.setHost(host);
    auth.setRealm(domain);
    auth.setPort(port);    
    options.setProperty(HTTPConstants.AUTHENTICATE, auth);    
    stub._getServiceClient().setOptions(options);
    return;
  }
  
}
