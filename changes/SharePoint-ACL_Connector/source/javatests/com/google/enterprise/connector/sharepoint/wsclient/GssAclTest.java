//Copyright 2009 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.wsclient;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclChangesSinceTokenResult;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclForUrlsResult;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssResolveSPGroupResult;

public class GssAclTest extends TestCase {

    SharepointClientContext sharepointClientContext;
    GssAclWS aclWS;

    protected void setUp() throws Exception {
        System.out.println("\n...Setting Up...");
        System.out.println("Initializing SharepointClientContext ...");
        this.sharepointClientContext = new SharepointClientContext(
                TestConfiguration.sharepointUrl, TestConfiguration.domain,
                TestConfiguration.kdcserver, TestConfiguration.username,
                TestConfiguration.Password,
                TestConfiguration.googleConnectorWorkDir,
                TestConfiguration.includedURls, TestConfiguration.excludedURls,
                TestConfiguration.mySiteBaseURL, TestConfiguration.AliasMap,
                TestConfiguration.feedType);
        assertNotNull(this.sharepointClientContext);
        sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
        sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);

        System.out.println("Initializing GsAclWS ...");
        this.aclWS = new GssAclWS(this.sharepointClientContext, null);
    }

    public void testGetAclForUrls() {
        String[] urls = {};
        GssGetAclForUrlsResult result = aclWS.getAclForUrls(urls);
        assertNotNull(result);
    }

    public void testGetAclChangesSinceToken() {
        String strChangeToken = "";
        GssGetAclChangesSinceTokenResult result = aclWS.getAclChangesSinceToken(strChangeToken);
        assertNotNull(result);
    }

    public void testGetAffectedItemIDsForChangeList() {
        String listGuid = "";
        String[] result = aclWS.getAffectedItemIDsForChangeList(listGuid);
        assertNotNull(result);
    }

    public void testGetAffectedListIDsForChangeWeb() {
        String webGuid = "";
        String[] result = aclWS.getAffectedListIDsForChangeWeb(webGuid);
        assertNotNull(result);
    }

    public void testResolveSPGroup() {
        String[] groupIds = {};
        GssResolveSPGroupResult result = aclWS.resolveSPGroup(groupIds);
        assertNotNull(result);
    }

    public void testCheckConnectivity() {
        String status = aclWS.checkConnectivity();
        assertEquals(SPConstants.CONNECTIVITY_SUCCESS, status);
    }
}
