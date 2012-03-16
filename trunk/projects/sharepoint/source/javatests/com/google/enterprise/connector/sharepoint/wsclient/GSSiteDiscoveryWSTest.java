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

package com.google.enterprise.connector.sharepoint.wsclient;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;

import java.util.Set;

import junit.framework.TestCase;

public class GSSiteDiscoveryWSTest extends TestCase {

  SharepointClientContext sharepointClientContext;
  GSSiteDiscoveryWS siteDisc;

  protected void setUp() throws Exception {
    super.setUp();
    System.out.println("\n...Setting Up...");
    System.out.println("Initializing SharepointClientContext ...");
    this.sharepointClientContext = TestConfiguration.initContext();
    assertNotNull(this.sharepointClientContext);
    sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
    sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);

    System.out.println("Initializing GSSiteDiscoveryWS ...");
    this.siteDisc = new GSSiteDiscoveryWS(this.sharepointClientContext,
        TestConfiguration.sharepointUrl);
  }

  public final void testGetMatchingSiteCollections() {
    System.out.println("Testing getMatchingSiteCollections()");
    final Set siteCol = this.siteDisc.getMatchingSiteCollections();
    assertNotNull(siteCol);
    System.out.println("Total site colllections discovered: " + siteCol.size());
    System.out.println("[ getMatchingSiteCollections() ] Test Completed.");
  }

  public final void testGetFQDNHost() {
    System.out.println("Testing getFQDNHost()");
    final String domain_fqdn = this.siteDisc.getFQDNHost(TestConfiguration.domain);
    assertNotNull(domain_fqdn);
    System.out.println("[ getFQDNHost() ] Test Completed.");
  }

  public final void testGetCurrentWebCrawlInfo() throws Exception {
    WebCrawlInfo webCrawlInfo = siteDisc.getCurrentWebCrawlInfo();
    assertNotNull(webCrawlInfo);
    // Assuming the initial crawl URL will never be marked for NoCrawl.
    assertFalse(webCrawlInfo.isNoCrawl());
  }

  public final void testUpdateListCrawlInfo() throws Exception {
    GlobalState globalState = TestConfiguration.initState(sharepointClientContext);
    WebState ws = globalState.lookupWeb(TestConfiguration.Site1_URL, sharepointClientContext);
    assertNotNull(ws);
    
    Set<ListState> allListStates = ws.getAllListStateSet();
    assertNotNull(allListStates);
    assertTrue(allListStates.size() > 0);

    for (ListState listState : allListStates) {
      listState.setNoCrawl(true);
    }
    
    WebCrawlInfo webCrawlInfo = new WebCrawlInfo();
    webCrawlInfo.setNoCrawl(true);
    siteDisc.updateListCrawlInfo(allListStates);
    for (ListState listState : allListStates) {
      assertFalse(listState.isNoCrawl());
    }
  }

  public final void testGetWebCrawlInfoInBatch() throws Exception {
    String[] weburls = { TestConfiguration.Site1_URL };
    WebCrawlInfo webCrawlInfo = new WebCrawlInfo();
    webCrawlInfo.setNoCrawl(true);
    WebCrawlInfo[] wsResult = siteDisc.getWebCrawlInfoInBatch(weburls);
    assertNotNull(wsResult);
    assertEquals(wsResult.length, weburls.length);
    assertEquals(false, wsResult[0].isNoCrawl());
  }

  public final void testUpdateWebCrawlInfoInBatch() throws Exception {
    GlobalState globalState = TestConfiguration.initState(sharepointClientContext);
    siteDisc.updateWebCrawlInfoInBatch(globalState.getAllWebStateSet());
    for (WebState web : globalState.getAllWebStateSet()) {
      // Assuming the site URL being used for testing are not marked for
      // NoCrawl.
      assertFalse(web.isNoCrawl());
    }
  }
}
