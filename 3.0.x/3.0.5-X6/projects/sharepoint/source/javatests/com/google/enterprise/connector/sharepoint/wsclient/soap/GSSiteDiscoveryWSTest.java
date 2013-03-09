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
    this.sharepointClientContext = TestConfiguration.initContext();
    assertNotNull(this.sharepointClientContext);
    sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
    sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);

    this.siteDisc = new GSSiteDiscoveryWS(this.sharepointClientContext,
        TestConfiguration.sharepointUrl);
  }

  public final void testGetMatchingSiteCollections() {
    final Set siteCol = siteDisc.getMatchingSiteCollections();
    assertNotNull(siteCol);
    assertEquals(10, siteCol.size());
  }

  public final void testGetFQDNHost() {
    final String domain_fqdn = this.siteDisc.getFQDNHost(TestConfiguration.domain);
    assertEquals(TestConfiguration.domain, domain_fqdn);
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

    // isNoCrawl should initially be false.
    for (ListState listState : allListStates) {
      assertFalse(listState.isNoCrawl());
    }

    // Set isNoCrawl to true for all lists.
    for (ListState listState : allListStates) {
      listState.setNoCrawl(true);
    }

    // Verify isNoCrawl is true.
    for (ListState listState : allListStates) {
      assertTrue(listState.isNoCrawl());
    }

    // Update the crawl state from the web service. This should
    // set isNoCrawl back to false.
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
    assertFalse(wsResult[0].isNoCrawl());
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
