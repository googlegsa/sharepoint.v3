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
package com.google.enterprise.connector.sharepoint.wsclient.soap;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.client.AclWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.AlertsWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.BulkAuthorizationWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.ClientFactory;
import com.google.enterprise.connector.sharepoint.wsclient.client.ListsWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDataWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDiscoveryWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfile2003WS;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfile2007WS;
import com.google.enterprise.connector.sharepoint.wsclient.client.WebsWS;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.params.HttpClientParams;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A factory for the interfaces that encapsulates the SharePoint
 * webservices.
 */
public class SPClientFactory implements ClientFactory {
  private static final Logger LOGGER = Logger.getLogger(SPClientFactory.class.getName());

  /**
   * Gets the instance of the alerts web service.
   *
   * @return a new alerts web service instance.
   */
  public AlertsWS getAlertsWS(final SharepointClientContext ctx) {
    try {
      return new SPAlertsWS(ctx);
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING,
          "Unable to create alerts web service instance.", e);
      return null;
    }
  }

  /**
   * Gets the instance of the bulk authorization web service.
   *
   * @return a new bulk authorization service instance.
   */
  public BulkAuthorizationWS getBulkAuthorizationWS(
      final SharepointClientContext ctx) {
    try {
      return new GSBulkAuthorizationWS(ctx);
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING, "Failed to initialize GSBulkAuthorizationWS.",
          e);
      return null;
    }
  }

  /**
   * Gets the instance of the lists web service.
   *
   * @return a new lists web service instance.
   */
  public ListsWS getListsWS(final SharepointClientContext ctx,
      final String rowLimit) {
    try {
      return new SPListsWS(ctx, rowLimit);
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING,
          "Unable to create lists web service instance.", e);
      return null;
    }
  }

  /**
   * Gets the instance of the site data web service.
   *
   * @return a new site data web service instance.
   */
  public SiteDataWS getSiteDataWS(final SharepointClientContext ctx) {
    try {
      return new SPSiteDataWS(ctx);
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING,
          "Unable to create site data web service instance.", e);
      return null;
    }
  }

  /**
   * Gets the instance of the 2003 user profile web service.
   *
   * @return a new 2003 user profile web service instance.
   */
  public UserProfile2003WS getUserProfile2003WS(
      final SharepointClientContext ctx) {
    try {
      return new com.google.enterprise.connector.sharepoint.wsclient.soap.
          sp2003.SPUserProfileWS(ctx);
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING,
          "Unable to create user profile web service instance.", e);
      return null;
    }
  }

  /**
   * Gets the instance of the 2007 user profile web service.
   *
   * @return a new 2007 user profile web service instance.
   */
  public UserProfile2007WS getUserProfile2007WS(
      final SharepointClientContext ctx) {
    try {
      return new SPUserProfileWS(ctx);
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING,
          "Unable to create user profile web service instance.", e);
      return null;
    }
  }

  /**
   * Gets the instance of the user profile web service.
   *
   * @return a new user profile web service instance.
   */
  public WebsWS getWebsWS(final SharepointClientContext ctx) {
    try {
      return new SPWebsWS(ctx);
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING,
          "Unable to create webs web service instance.", e);
      return null;
    }
  }

  /**
   * Gets the instance of the ACL web service.
   *
   * @return a new ACL service instance.
   */
  public AclWS getAclWS(final SharepointClientContext ctx, String webUrl) {
    try {
      return new GSAclWS(ctx, webUrl);
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING,
          "Unable to create ACLs web service instance.", e);
      return null;
    }
  }

  /**
   * Gets the instance of the site discovery web service.
   *
   * @return a new site discovery service instance.
   */
  public SiteDiscoveryWS getSiteDiscoveryWS(final SharepointClientContext ctx,
    String webUrl) {
    try {
      return new GSSiteDiscoveryWS(ctx, webUrl);
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING,
          "Unable to create site discovery web service instance.", e);
      return null;
    }
  }

  public int checkConnectivity(HttpMethodBase method,
      Credentials credentials) throws IOException {
    final HttpClient httpClient = new HttpClient();

    HttpClientParams params = httpClient.getParams();
    // Fix for the Issue[5408782] SharePoint connector fails to traverse a site,
    // circular redirect exception is observed.
    params.setBooleanParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, true);
    // If ALLOW_CIRCULAR_REDIRECTS is set to true, HttpClient throws an
    // exception if a series of redirects includes the same resources more than
    // once. MAX_REDIRECTS allows you to specify a maximum number of redirects
    // to follow.
    params.setIntParameter(HttpClientParams.MAX_REDIRECTS, 10);

    httpClient.getState().setCredentials(AuthScope.ANY, credentials);
    return httpClient.executeMethod(method);
  }

  public String getResponseHeader(HttpMethodBase method, String headerName) {
    String headerValue = null;
    final Header header = method.getResponseHeader(headerName);
    if (null != header) {
      headerValue = header.getValue();
    }
    return headerValue;
  }
}

