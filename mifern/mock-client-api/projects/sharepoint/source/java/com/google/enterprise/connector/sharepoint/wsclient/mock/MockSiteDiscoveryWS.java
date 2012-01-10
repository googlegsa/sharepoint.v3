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
import com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDiscoveryWS;

import java.util.logging.Logger;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class MockSiteDiscoveryWS implements SiteDiscoveryWS {
  private static final Logger LOGGER = Logger.getLogger(MockSiteDiscoveryWS.class.getName());
  private final SharepointClientContext sharepointClientContext;

  /**
   * @param ctx The Sharepoint context is passed so that necessary
   *    information can be used to create the instance of current class
   *    web service endpoint is set to the default SharePoint URL stored
   *    in SharePointClientContext.
   * @throws SharepointException
   */
  public MockSiteDiscoveryWS(final SharepointClientContext ctx,
      String webUrl) {
    sharepointClientContext = ctx;
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public Set<String> getMatchingSiteCollections() {
    return Collections.emptySet();
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public void updateWebCrawlInfoInBatch(Set<WebState> webs) {
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public WebCrawlInfo getCurrentWebCrawlInfo() {
    return null;
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public void updateListCrawlInfo(Collection<ListState> listCollection) {
  }
}
