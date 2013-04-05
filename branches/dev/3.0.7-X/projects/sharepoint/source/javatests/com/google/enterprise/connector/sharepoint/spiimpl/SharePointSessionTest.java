// Copyright 2007 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.spiimpl;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.TraversalManager;

import junit.framework.TestCase;

public class SharePointSessionTest extends TestCase {

  SharepointClientContext sharepointClientContext;
  SharepointSession session;

  protected void setUp() throws Exception {
    this.sharepointClientContext = TestConfiguration.initContext();
    assertNotNull(this.sharepointClientContext);
    sharepointClientContext.setFeedType(FeedType.CONTENT_FEED);

    final SharepointConnector connector =
        TestConfiguration.getConnectorInstance();
    connector.setFQDNConversion(TestConfiguration.FQDNflag);
    this.session = new SharepointSession(connector, sharepointClientContext);
  }

  public void testGetAuthenticationManager() throws Exception {
    final AuthenticationManager authZMan = session.getAuthenticationManager();
    assertNotNull(authZMan);
  }

  public void testGetAuthorizationManager() throws Exception {
    final AuthorizationManager authNMan = session.getAuthorizationManager();
    assertNotNull(authNMan);
  }

  public void testGetTraversalManager() throws Exception {
    final TraversalManager travMan = session.getTraversalManager();
    assertNotNull(travMan);
  }
}
