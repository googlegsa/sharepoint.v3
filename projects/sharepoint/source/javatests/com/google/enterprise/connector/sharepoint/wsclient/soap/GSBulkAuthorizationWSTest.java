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

package com.google.enterprise.connector.sharepoint.wsclient.soap;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.BulkAuthorizationHelper;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;

import junit.framework.TestCase;

public class GSBulkAuthorizationWSTest extends TestCase {

  SharepointClientContext sharepointClientContext;
  BulkAuthorizationHelper bulkAuth;

  protected void setUp() throws Exception {
    System.out.println("\n...Setting Up...");
    System.out.println("Initializing SharepointClientContext ...");
    this.sharepointClientContext = TestConfiguration.initContext();
    assertNotNull(this.sharepointClientContext);
    sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
    sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);

    System.out.println("Initializing BulkAuthorizationHelper ...");
    this.bulkAuth = new BulkAuthorizationHelper(this.sharepointClientContext);
  }

  public final void testBulkAuthorization() throws Throwable {
    System.out.println("Testing BulkAuthorizationHelper(SharepointClientContext, siteName)...");
    sharepointClientContext.setSiteURL(TestConfiguration.Site1_URL);
    this.bulkAuth = new BulkAuthorizationHelper(this.sharepointClientContext);
    assertNotNull(this.bulkAuth);
    System.out.println("[ BulkAuthorizationHelper(SharepointClientContext, siteName) ] Test Passed");
  }

  public void testCheckConnectivity() throws Throwable {
    System.out.println("Testing checkConnectivity()...");
    this.bulkAuth.checkConnectivity();
    System.out.println("[ checkConnectivity() ] Test Conpleted.");
  }
}
