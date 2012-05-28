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

package com.google.enterprise.connector.sharepoint.spiimpl;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.wsclient.GSSiteDiscoveryWS;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.SimpleAuthenticationIdentity;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import junit.framework.TestCase;

public class SharepointAuthorizationManagerTest extends TestCase {
    SharepointClientContext sharepointClientContext;

    protected void setUp() throws Exception {
        System.out.println("\n...Setting Up...");
        System.out.println("Initializing SharepointClientContext ...");
        this.sharepointClientContext = new SharepointClientContext(
                TestConfiguration.sharepointUrl, TestConfiguration.domain,
                TestConfiguration.kdcserver, TestConfiguration.username, TestConfiguration.Password,
                TestConfiguration.googleConnectorWorkDir,
                TestConfiguration.includedURls, TestConfiguration.excludedURls,
                TestConfiguration.mySiteBaseURL, TestConfiguration.AliasMap,
                TestConfiguration.feedType,
                TestConfiguration.useSPSearchVisibility);
        assertNotNull(this.sharepointClientContext);
        sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
        sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);
        System.out.println("Initializing SharepointAutho rizationManager ...");
    }

    public void testDocIdGrouping() throws SharepointException {
        String[] allUrls = "http://mycomp.com/,http://mycomp.com/site1,http://mycomp.com/site2".split(",");
        List<String> lstAllUrls = Arrays.asList(allUrls);
        Set<String> siteCollUrls = new TreeSet<String>(lstAllUrls);
        SharepointAuthorizationManager authMan = new SharepointAuthorizationManager(
                sharepointClientContext, siteCollUrls);

        // Assure that
        // 1. no URLs got skipped while grouping
        // 2. the longer the URL, the early it comes in sequence
        int countForAssert = 0;
        for(Entry<String, Set<String>> webAppEntry : authMan.getWebappToSiteCollections().entrySet()) {
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

    public void testAuthorizeDocids() throws Throwable {
        SharepointAuthorizationManager authMan = new SharepointAuthorizationManager(
                this.sharepointClientContext,
                new GSSiteDiscoveryWS(sharepointClientContext, null).getMatchingSiteCollections());
        AuthenticationIdentity authID = new SimpleAuthenticationIdentity(
                TestConfiguration.searchUserID, TestConfiguration.searchUserPwd);

        Set<String> docids = new HashSet<String>();
        docids.add(TestConfiguration.SearchDocID1);
        docids.add(TestConfiguration.SearchDocID2);
        docids.add(TestConfiguration.SearchDocID3);

        final Collection<AuthorizationResponse> authZResponses = authMan.authorizeDocids(docids, authID);
        assertEquals(docids.size(), authZResponses.size());
        for (AuthorizationResponse authZResponse : authZResponses) {
            assertNotSame(authZResponse.getStatus(), AuthorizationResponse.Status.INDETERMINATE);
        }
    }
}
