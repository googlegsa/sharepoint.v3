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
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.client.WebsHelper;

import java.util.Set;

import junit.framework.TestCase;

/**
 * Test the functionality of webs web service.
 *
 * @author amit_kagrawal
 */
public class SPWebWSTest extends TestCase {
  SharepointClientContext sharepointClientContext;
  WebsHelper webs;

  protected void setUp() throws Exception {
    this.sharepointClientContext = TestConfiguration.initContext();
    assertNotNull(this.sharepointClientContext);
    sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
    sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);

    System.out.println("Initializing WebsHelper ...");
    webs = new WebsHelper(this.sharepointClientContext);
  }

  public final void testWebsHelper() throws Throwable {
    sharepointClientContext.setSiteURL(TestConfiguration.sharepointUrl);
    webs = new WebsHelper(this.sharepointClientContext);
    assertNotNull(webs);
  }

  public final void testGetDirectChildsites() throws Throwable {
    Set<String> sites = webs.getDirectChildsites();
    assertNotNull(sites);
  }

  public final void testGetWebURLFromPageURL() throws Throwable {
    final String siteURL =
        webs.getWebURLFromPageURL(TestConfiguration.Site1_List1_Item1_URL);
    assertNotNull(siteURL);
  }

  public final void testGetWebTitle() throws Throwable {
    final String siteURL =
        webs.getWebTitle(TestConfiguration.sharepointUrl, SPType.SP2007);
    assertNotNull(siteURL);
  }

  public final void testCheckConnectivity() throws Throwable {
    webs.checkConnectivity();
  }
}
