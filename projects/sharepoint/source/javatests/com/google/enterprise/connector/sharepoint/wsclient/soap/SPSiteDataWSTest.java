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
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants.DocumentType;

import java.net.MalformedURLException;
import java.util.List;

import junit.framework.TestCase;

public class SPSiteDataWSTest extends TestCase {
  SharepointClientContext sharepointClientContext;
  SPSiteDataWS siteDataWS;
  final SPClientFactory clientFactory = new SPClientFactory();

  protected void setUp() throws Exception {
    this.sharepointClientContext = TestConfiguration.initContext();

    assertNotNull(this.sharepointClientContext);
    sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
    sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);

    this.siteDataWS = new SPSiteDataWS(this.sharepointClientContext);
  }

  public final void testSiteDataWS() throws Exception {
    sharepointClientContext.setSiteURL(TestConfiguration.sharepointUrl);
    this.siteDataWS = new SPSiteDataWS(this.sharepointClientContext);
    assertNotNull(this.siteDataWS);
  }

  public void testGetNamedList() throws Exception {
    final GlobalState state = new GlobalState(clientFactory,
        TestConfiguration.googleConnectorWorkDir, FeedType.CONTENT_FEED);
    final WebState ws = state.makeWebState(sharepointClientContext,
        TestConfiguration.sharepointUrl);
    assertNotNull(ws);
    final List items = this.siteDataWS.getNamedLists(ws);
    assertNotNull(items);
  }

  public void testGetTitle() throws Exception {
    final String webTitle = this.siteDataWS.getTitle();
    assertNotNull(webTitle);
  }

  public void testGetSiteData() throws Exception {
    final GlobalState state = new GlobalState(clientFactory,
        TestConfiguration.googleConnectorWorkDir, FeedType.CONTENT_FEED);
    WebState ws = state.makeWebState(sharepointClientContext,
        TestConfiguration.sharepointUrl + SPConstants.DEFAULT_SITE_LANDING_PAGE);
    final SPDocument document = this.siteDataWS.getSiteData(ws);
    assertNotNull(document);
    String author = document.getAuthor().toLowerCase();
    String objectType = document.getObjType();
    assertEquals(TestConfiguration.userNameFormat2, author);
    assertEquals("Site", objectType);
  }
  
  public void testGetSiteDataWithUtf() throws Exception {
    final GlobalState state = new GlobalState(clientFactory,
        TestConfiguration.googleConnectorWorkDir, FeedType.CONTENT_FEED);
    WebState ws = state.makeWebState(sharepointClientContext,
        TestConfiguration.UTF8SiteUrl);
    final SPDocument document = this.siteDataWS.getSiteData(ws);
    assertNotNull(document);
    // document Type should be null. For Publishing sites it will be ACL.
    assertNull(document.getDocumentType());
    String author = document.getAuthor().toLowerCase();
    String objectType = document.getObjType();
    assertEquals(TestConfiguration.userNameFormat2, author);
    assertEquals("Site", objectType);
  }
  
  public void testGetSiteDataPublishingSite() throws Exception {
    final GlobalState state = new GlobalState(clientFactory,
        TestConfiguration.googleConnectorWorkDir, FeedType.CONTENT_FEED);
    WebState ws = state.makeWebState(sharepointClientContext,
        TestConfiguration.publishingSiteUrl + SPConstants.DEFAULT_SITE_LANDING_PAGE);
    final SPDocument document = this.siteDataWS.getSiteData(ws);
    assertNotNull(document);
    // document Type should be null. For Publishing sites it will be ACL.
    assertEquals(DocumentType.ACL,document.getDocumentType());
    String author = document.getAuthor().toLowerCase();
    String objectType = document.getObjType();
    assertEquals(TestConfiguration.userNameFormat2, author);
    assertEquals("Site", objectType);
  }
  
}
