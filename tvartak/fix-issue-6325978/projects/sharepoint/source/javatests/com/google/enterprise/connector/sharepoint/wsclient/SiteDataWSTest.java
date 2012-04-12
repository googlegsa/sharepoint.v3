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

//Copyright 2007 Google Inc.  All Rights Reserved.
package com.google.enterprise.connector.sharepoint.wsclient;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.spi.RepositoryException;

import java.net.MalformedURLException;
import java.util.List;

import junit.framework.TestCase;

public class SiteDataWSTest extends TestCase {
  SharepointClientContext sharepointClientContext;
  SiteDataWS siteDataWS;

  protected void setUp() throws Exception {
    System.out.println("\n...Setting Up...");
    System.out.println("Initializing SharepointClientContext ...");
    this.sharepointClientContext = TestConfiguration.initContext();

    assertNotNull(this.sharepointClientContext);
    sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
    sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);

    System.out.println("Initializing SiteDataWS ...");
    this.siteDataWS = new SiteDataWS(this.sharepointClientContext);
  }

  public final void testSiteDataWS() throws Throwable {
    System.out.println("Testing SiteDataWS(SharepointClientContext, siteName)...");
    sharepointClientContext.setSiteURL(TestConfiguration.sharepointUrl);
    this.siteDataWS = new SiteDataWS(this.sharepointClientContext);
    assertNotNull(this.siteDataWS);
    System.out.println("[ SiteDataWS(SharepointClientContext, siteName) ] Test Passed");
  }

  public void testGetNamedList() throws MalformedURLException,
      RepositoryException {
    System.out.println("Testing getNamedLists()...");
    final GlobalState state = new GlobalState(
        TestConfiguration.googleConnectorWorkDir, FeedType.CONTENT_FEED);
    WebState ws = state.makeWebState(sharepointClientContext, TestConfiguration.sharepointUrl);
    final List items = this.siteDataWS.getNamedLists(ws);
    assertNotNull(items);
    System.out.println("[ getNamedLists() ] Test Passed.");
  }

  public void testGetTitle() throws MalformedURLException, RepositoryException {
    System.out.println("Testing getTitle()...");
    try {
      final String webTitle = this.siteDataWS.getTitle();
      assertNotNull(webTitle);
      System.out.println("[ getTitle() ] Test Passed.");
    } catch (final Exception e) {
      System.out.println("[ getTitle() ] Test Failed.");
    }
  }

  public void testGetSiteData() throws MalformedURLException,
      RepositoryException {
    System.out.println("Testing getSiteDataAsList()...");
    final GlobalState state = new GlobalState(
        TestConfiguration.googleConnectorWorkDir, FeedType.CONTENT_FEED);
    WebState ws = state.makeWebState(sharepointClientContext, TestConfiguration.sharepointUrl
        + SPConstants.DEFAULT_SITE_LANDING_PAGE);
    final SPDocument document = this.siteDataWS.getSiteData(ws);
    assertNotNull(document);
    String author = document.getAuthor();
    String objectType = document.getObjType();
    assertEquals("googlesp", author);
    assertEquals("Site", objectType);
    System.out.println("[ getSiteData() ] Test Passed.");
  }
}
