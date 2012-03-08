// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.sharepoint.wsclient.client;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpMethodBase;

import java.io.IOException;

/**
 * A factory for the facade interface that encapsulates the SharePoint webservices.
 */
public interface ClientFactory {
  /**
   * Gets the instance of the alerts web service.
   *
   * @return a new alerts web service instance.
   */
  public AlertsWS getAlertsWS(SharepointClientContext ctx);

  /**
   * Gets the instance of the bulk authorization web service.
   *
   * @return a new bulk authorization service instance.
   */
  public BulkAuthorizationWS getBulkAuthorizationWS(
      SharepointClientContext ctx) throws SharepointException;

  /**
   * Gets the instance of the lists web service.
   *
   * @return a new lists web service instance.
   */
  public ListsWS getListsWS(SharepointClientContext ctx, String rowLimit);

  /**
   * Gets the instance of the site data web service.
   *
   * @return a new site data web service instance.
   */
  public SiteDataWS getSiteDataWS(SharepointClientContext ctx);

  /**
   * Gets the instance of the 2003 user profile web service.
   *
   * @return a new 2003 user profile web service instance.
   */
  public UserProfile2003WS getUserProfile2003WS(SharepointClientContext ctx);

  /**
   * Gets the instance of the 2007 user profile web service.
   *
   * @return a new 2007 user profile web service instance.
   */
  public UserProfile2007WS getUserProfile2007WS(SharepointClientContext ctx);

  /**
   * Gets the instance of the user profile web service.
   *
   * @return a new user profile web service instance.
   */
  public WebsWS getWebsWS(SharepointClientContext ctx);

  /**
   * Gets the instance of the ACL web service.
   *
   * @return a new ACL service instance.
   */
  public AclWS getAclWS(SharepointClientContext ctx, String webUrl);

  /**
   * Gets the instance of the ACL web service.
   *
   * @return a new ACL service instance.
   */
  public SiteDiscoveryWS getSiteDiscoveryWS(SharepointClientContext ctx,
      String webUrl);
      
  /**
   * Check connectivity to the specified URL with the specified credentials.
   *
   * @param method contains the URL to check for connectivity
   * @param credentials the credentials to use
   * @return the HTTP response code; 200 for success or other code
   * @throws IOException
   */
  public int checkConnectivity(HttpMethodBase method, 
      Credentials credentials) throws IOException;
      
  /**
   * Gets the header for a previous executed request to checkConnectivity.
   *
   * @param method the method used in the previous request
   * @param headerName the header name to return
   * @return the value of the header if it exists; null otherwise
   */
  public String getResponseHeader(HttpMethodBase method, String headerName);
}
