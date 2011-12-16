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

package com.google.enterprise.connector.sharepoint.wsclient.mock;

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

import java.util.logging.Logger;

/**
 * A mock factory for the SharePoint webservices.
 */
public class MockClientFactory implements ClientFactory {
  private static final Logger LOGGER = Logger.getLogger(MockClientFactory.class.getName());

  public MockClientFactory() {
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public AlertsWS getAlertsWS(final SharepointClientContext ctx) {
    return new MockAlertsWS(ctx);
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public BulkAuthorizationWS getBulkAuthorizationWS(
      final SharepointClientContext ctx) throws SharepointException {
    return new MockBulkAuthorizationWS(ctx);
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public ListsWS getListsWS(final SharepointClientContext ctx) {
    return new MockListsWS(ctx);
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public SiteDataWS getSiteDataWS(final SharepointClientContext ctx) {
    return new MockSiteDataWS(ctx);
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public UserProfile2003WS getUserProfile2003WS(
      final SharepointClientContext ctx) {
    return new MockUserProfile2003WS(ctx);
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public UserProfile2007WS getUserProfile2007WS(
      final SharepointClientContext ctx) {
    return new MockUserProfile2007WS(ctx);
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public WebsWS getWebsWS(final SharepointClientContext ctx) {
    return new MockWebsWS(ctx);
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public AclWS getAclWS(final SharepointClientContext ctx, String webUrl) {
    return new MockAclWS(ctx, webUrl);
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public SiteDiscoveryWS getSiteDiscoveryWS(final SharepointClientContext ctx,
    String webUrl) {
    return new MockSiteDiscoveryWS(ctx, webUrl);
  }
}
