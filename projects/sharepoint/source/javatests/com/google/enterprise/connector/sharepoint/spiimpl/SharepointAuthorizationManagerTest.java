// Copyright 2007 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.spiimpl;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SiteDiscoveryHelper;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.wsclient.soap.SPClientFactory;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.SimpleAuthenticationIdentity;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.TestCase;

public class SharepointAuthorizationManagerTest extends TestCase {
  SharepointClientContext sharepointClientContext;
  private final SPClientFactory clientFactory = new SPClientFactory();

  protected void setUp() throws Exception {
    this.sharepointClientContext = new SharepointClientContext(
        clientFactory, TestConfiguration.sharepointUrl, TestConfiguration.domain,
        TestConfiguration.kdcserver, TestConfiguration.username,
        TestConfiguration.password, TestConfiguration.googleConnectorWorkDir,
        TestConfiguration.googleGlobalNamespace,
        TestConfiguration.googleLocalNamespace,
        TestConfiguration.includedURls, TestConfiguration.excludedURls,
        TestConfiguration.mySiteBaseURL, TestConfiguration.aliasMap,
        TestConfiguration.feedType, TestConfiguration.useSPSearchVisibility);
    assertNotNull(this.sharepointClientContext);
    sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
    sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);
  }

  public void testDocIdGrouping() throws SharepointException {
    String[] allUrls = "http://mycomp.com/,http://mycomp.com/site1,http://mycomp.com/site2".split(",");
    List<String> lstAllUrls = Arrays.asList(allUrls);
    Set<String> siteCollUrls = new TreeSet<String>(lstAllUrls);
    SharepointAuthorizationManager authMan = new SharepointAuthorizationManager(
        clientFactory, sharepointClientContext, siteCollUrls);

    // Assure that
    // 1. no URLs got skipped while grouping
    // 2. the longer the URL, the early it comes in sequence
    int countForAssert = 0;
    for (Entry<String, Set<String>> webAppEntry : authMan.getWebappToSiteCollections().entrySet()) {
      Set<String> sortedUrls = webAppEntry.getValue();
      countForAssert += sortedUrls.size();

      assertTrue(sortedUrls instanceof SortedSet<?>);
      String prevUrl = ((SortedSet<String>) sortedUrls).first();
      for (String currUrl : sortedUrls) {
        assertTrue(prevUrl.length() >= currUrl.length());
        prevUrl = currUrl;
      }
    }
    assertEquals(siteCollUrls.size(), countForAssert);
  }

  // TODO: What is the difference between testAuthorizeDocids and testAuthorizeDocidsForMandUFeeds.
  // Need to gather more details on the difference in the data so that we can create if for the tests.
  public void testAuthorizeDocids() throws Throwable {
    SiteDiscoveryHelper siteDisc =
        new SiteDiscoveryHelper(sharepointClientContext, null);
    SharepointAuthorizationManager authMan = new SharepointAuthorizationManager(
        clientFactory, this.sharepointClientContext,
        siteDisc.getMatchingSiteCollections());
    AuthenticationIdentity authID = new SimpleAuthenticationIdentity(
        TestConfiguration.searchUserID, TestConfiguration.searchUserPwd);

    Set<String> docids = new HashSet<String>();
    docids.add(TestConfiguration.SearchDocID1);
    docids.add(TestConfiguration.SearchDocID2);
    docids.add(TestConfiguration.SearchDocID3);
    docids.add(TestConfiguration.SearchDocID5);
    docids.add(TestConfiguration.SearchDocID5.toLowerCase());
    docids.add(TestConfiguration.SearchDocID5.toUpperCase());

    final Collection<AuthorizationResponse> authZResponses = authMan.authorizeDocids(docids, authID);
    assertEquals(3, authZResponses.size());
    for (AuthorizationResponse authZResponse : authZResponses) {
      assertNotSame(authZResponse.getStatus(), AuthorizationResponse.Status.INDETERMINATE);
    }
  }

  public void testAuthorizeDocidsForMandUFeeds() throws Throwable {
    sharepointClientContext.setFeedType(FeedType.METADATA_URL_FEED);
    SiteDiscoveryHelper siteDisc =
        new SiteDiscoveryHelper(sharepointClientContext, null);
    SharepointAuthorizationManager authMan = new SharepointAuthorizationManager(
        clientFactory, this.sharepointClientContext,
        siteDisc.getMatchingSiteCollections());
    AuthenticationIdentity authID = new SimpleAuthenticationIdentity(
        TestConfiguration.searchUserID, TestConfiguration.searchUserPwd);

    Set<String> docids = new HashSet<String>();
    docids.add(TestConfiguration.SearchDocID4);
    docids.add(TestConfiguration.SearchDocID6);
    docids.add(TestConfiguration.SearchDocID6.toLowerCase());
    docids.add(TestConfiguration.SearchDocID6.toUpperCase());
    docids.add(TestConfiguration.SearchDocID114);
    docids.add(TestConfiguration.SearchDocID115);
    docids.add(TestConfiguration.SearchDocID116);
    docids.add(TestConfiguration.searchDocID117);
    docids.add(TestConfiguration.searchDocID118);
    docids.add(TestConfiguration.searchDocID119);
    docids.add(TestConfiguration.searchDocID120);
    docids.add(TestConfiguration.searchDocID121);
    docids.add(TestConfiguration.searchDocID122);
    docids.add(TestConfiguration.searchDocID123);
    docids.add(TestConfiguration.searchDocID124);

    final Collection<AuthorizationResponse> authZResponses = authMan.authorizeDocids(docids, authID);
    assertEquals(docids.size(), authZResponses.size());
    for (AuthorizationResponse authZResponse : authZResponses) {
      assertNotSame(authZResponse.getStatus(), AuthorizationResponse.Status.INDETERMINATE);
    }
  }
}
