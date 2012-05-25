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

package com.google.enterprise.connector.sharepoint.client;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;

import junit.framework.TestCase;

public class SharePointClientContextTest extends TestCase {

  SharepointClientContext sharepointClientContext;

  protected void setUp() throws Exception {
    sharepointClientContext = TestConfiguration.initContext();
    assertNotNull(this.sharepointClientContext);
  }

  public void testClone() throws Exception {
    final SharepointClientContext spc =
        (SharepointClientContext) sharepointClientContext.clone();
    assertNotSame(sharepointClientContext, spc);
  }

  public void testCheckConnectivity() throws Exception {
    final int responseCode = sharepointClientContext.checkConnectivity(
        TestConfiguration.sharepointUrl, null);
    assertEquals(responseCode, 200);
  }

  public void testCheckSharePointType() throws Exception {
    final SPType spType = sharepointClientContext.checkSharePointType(
        TestConfiguration.sharepointUrl);
    assertEquals(SPType.SP2007, spType);
  }

  public void testIsIncludeMetadata() throws Exception {
    final boolean include =
        sharepointClientContext.isIncludeMetadata("file type");
    assertTrue(include);
  }

  public void testIsIncludedUrl() throws Exception {
    final boolean include = sharepointClientContext.isIncludedUrl(
        TestConfiguration.sharepointUrl);
    assertTrue(include);
  }

  public void testLogExcludedURL() throws Exception {
    sharepointClientContext.logExcludedURL("testLog");
  }

  public void testRemoveExcludedURLLogs() throws Exception {
    sharepointClientContext.clearExcludedURLLogs();
  }
}
