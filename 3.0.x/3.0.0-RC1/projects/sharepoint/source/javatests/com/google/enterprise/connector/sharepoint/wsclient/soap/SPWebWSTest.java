//Copyright 2007 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.wsclient.soap;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;

import java.util.Set;

import junit.framework.TestCase;

/**
 * Test the functionality of webs web service.
 *
 * @author amit_kagrawal
 */
public class SPWebWSTest extends TestCase {
  SharepointClientContext sharepointClientContext;
  SPWebsWS websWS;

  protected void setUp() throws Exception {
    System.out.println("\n...Setting Up...");
    System.out.println("Initializing SharepointClientContext ...");
    this.sharepointClientContext = TestConfiguration.initContext();
    assertNotNull(this.sharepointClientContext);
    sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
    sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);

    System.out.println("Initializing SPWebsWS ...");
    this.websWS = new SPWebsWS(this.sharepointClientContext);
  }

  public final void testWebsWS() throws Throwable {
    System.out.println("Testing SPWebsWS(SharepointClientContext, siteName)...");
    sharepointClientContext.setSiteURL(TestConfiguration.sharepointUrl);
    this.websWS = new SPWebsWS(this.sharepointClientContext);
    assertNotNull(this.websWS);
    System.out.println("[ SPWebsWS(SharepointClientContext, siteName) ] Test Passed");
  }

  public final void testGetDirectChildsites() throws Throwable {
    System.out.println("Testing getDirectChildsites()...");
    final Set sites = this.websWS.getDirectChildsites();
    assertNotNull(sites);
    System.out.println("[ getDirectChildsites() ] Test Passed");
  }

  public final void testGetWebURLFromPageURL() throws Throwable {
    System.out.println("Testing getWebURLFromPageURL()...");
    final String siteURL = this.websWS.getWebURLFromPageURL(TestConfiguration.Site1_List1_Item1_URL);
    assertNotNull(siteURL);
    System.out.println("[ getWebURLFromPageURL() ] Test Passed");
  }

  public final void testGetWebTitle() throws Throwable {
    System.out.println("Testing getWebTitle()...");
    final String siteURL = this.websWS.getWebTitle(TestConfiguration.sharepointUrl, SPType.SP2007);
    assertNotNull(siteURL);
    System.out.println("[ getWebTitle() ] Test Passed");
  }

  public final void testCheckConnectivity() throws Throwable {
    System.out.println("Testing checkConnectivity()...");
    this.websWS.checkConnectivity();
    System.out.println("[ checkConnectivity() ] Test Completed.");
  }
}
