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

package com.google.enterprise.connector.sharepoint.client;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;

public class SharePointClientContextTest extends TestCase {

    SharepointClientContext sharepointClientContext;

    protected void setUp() throws Exception {
        System.out.println("Setting Up...");
        this.sharepointClientContext = new SharepointClientContext(
                TestConfiguration.sharepointUrl, TestConfiguration.domain,
                TestConfiguration.kdcserver, TestConfiguration.username, TestConfiguration.Password,
                TestConfiguration.googleConnectorWorkDir,
                TestConfiguration.includedURls, TestConfiguration.excludedURls,
                TestConfiguration.mySiteBaseURL, TestConfiguration.AliasMap,
                TestConfiguration.feedType);
        assertNotNull(this.sharepointClientContext);
        sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
        sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);

        System.out.println("SharepointClientContext has been initialized successfully.");
    }

    public void testClone() {
        System.out.println("Testing clone()...");
        final SharepointClientContext spc = (SharepointClientContext) this.sharepointClientContext.clone();
        assertNotSame(this.sharepointClientContext, spc);
        System.out.println("[ clone() ] Test Passed");
    }

    public void testCheckConnectivity() {
        System.out.println("Testing checkConnectivity()...");
        try {
            final int responseCode = this.sharepointClientContext.checkConnectivity(TestConfiguration.sharepointUrl, null);
            assertEquals(responseCode, 200);
        } catch (Exception e) {
            System.out.println(e);
            assertFalse(false);
        }
        System.out.println("[ checkConnectivity() ] Test Completed");
    }

    public void testCheckSharePointType() {
        System.out.println("Testing checkSharePointVersion()...");
        try {
			final SPType spType = this.sharepointClientContext.checkSharePointType(TestConfiguration.Site1_URL);
            assertEquals(spType, SPConstants.CONNECTIVITY_SUCCESS);
        } catch (Exception e) {
            System.out.println(e);
            assertFalse(false);
        }
        System.out.println("[ checkConnectivity() ] Test Completed");
    }

    public void testIsIncludeMetadata() {
        System.out.println("Testing isIncludeMetadata()...");
        final boolean include = this.sharepointClientContext.isIncludeMetadata("file type");
        assertTrue(include);
        System.out.println("[ isIncludeMetadata() ] Test Completed");
    }

    public void testIsIncludedUrl() {
        System.out.println("Testing isIncludedUrl()...");
        final boolean include = this.sharepointClientContext.isIncludedUrl(TestConfiguration.sharepointUrl);
        assertTrue(include);
        System.out.println("[ isIncludedUrl() ] Test Completed");
    }

    public void testLogExcludedURL() {
        System.out.println("Testing logExcludedURL()...");
        this.sharepointClientContext.logExcludedURL("testLog");
        System.out.println("[ logExcludedURL() ] Test Completed");
    }

    public void testRemoveExcludedURLLogs() {
        System.out.println("Testing removeExcludedURLLogs()...");
        this.sharepointClientContext.clearExcludedURLLogs();
        System.out.println("[ removeExcludedURLLogs() ] Test Completed");
    }
}
