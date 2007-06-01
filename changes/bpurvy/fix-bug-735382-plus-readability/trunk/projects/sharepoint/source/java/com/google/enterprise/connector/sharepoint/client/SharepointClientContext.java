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

package com.google.enterprise.connector.sharepoint.client;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.Stub;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class to hold the context information for sharepoint client connection.
 *
 */
public class SharepointClientContext {

  private String siteName;
  private String domain;
  private String username;
  private String password;
  private int port = 80;
  private String host;
  private String googleConnectorWorkDir = null;
  
  public SharepointClientContext(String sharepointUrl, String domain,
                                 String username, String password,
                                 String googleConnectorWorkDir) {
    if (sharepointUrl.endsWith("/")) {
      sharepointUrl = sharepointUrl.substring(
          0, sharepointUrl.lastIndexOf("/"));
    }
    try {
      URL url = new URL(sharepointUrl);
      this.host = url.getHost();
      if (-1 != url.getPort()) {
        this.port = url.getPort();
      }
      this.siteName = url.getPath();      
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    
    this.domain = domain;    
    this.username = username;
    this.password = password;
    this.googleConnectorWorkDir = googleConnectorWorkDir;
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

  public String getsiteName() {
    return siteName;
  }

  public String getUsername() {
    return username;
  }

  public String getGoogleConnectorWorkDir() {
    return this.googleConnectorWorkDir;
  }
  
  public void setDomain(String domain) {
    this.domain = domain;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setsiteName(String siteNameNew) {
    this.siteName = siteNameNew;
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

  public void setGoogleConnectorWorkDir(String workDir) {
    this.googleConnectorWorkDir = workDir;
  }
  
  /**
   * Sets the stub 
   * @param stub Axis Client Stub to call the webservices on 
   * Sharepoint server.
   * @param endpoint Suffix to the particular webserive to use.
   */
  public void setStubWithAuth(Stub stub, String endpoint) {
    Options options = new Options();
    EndpointReference target = new EndpointReference(endpoint);
    options.setTo(target);
    HttpTransportProperties.Authenticator auth = 
      new HttpTransportProperties.Authenticator();
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
