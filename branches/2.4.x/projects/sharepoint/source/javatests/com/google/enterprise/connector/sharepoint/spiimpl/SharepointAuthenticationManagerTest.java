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

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointAuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.SimpleAuthenticationIdentity;

public class SharepointAuthenticationManagerTest extends TestCase {

    SharepointClientContext sharepointClientContext;
    SharepointAuthenticationManager authMan;
    AuthenticationIdentity authID;

    protected void setUp() throws Exception {
        System.out.println("\n...Setting Up...");
        System.out.println("Initializing SharepointClientContext ...");
        this.sharepointClientContext = new SharepointClientContext(
                TestConfiguration.sharepointUrl, TestConfiguration.domain,
                TestConfiguration.username, TestConfiguration.Password,
                TestConfiguration.googleConnectorWorkDir,
                TestConfiguration.includedURls, TestConfiguration.excludedURls,
                TestConfiguration.mySiteBaseURL, TestConfiguration.AliasMap,
                TestConfiguration.feedType);
        assertNotNull(this.sharepointClientContext);
        sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
        sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);
        System.out.println("Initializing SharepointAuthenticationManager ...");
        this.authMan = new SharepointAuthenticationManager(
                this.sharepointClientContext);
        System.out.println("Initializing SharepointAuthenticationIdentity ...");
        this.authID = new SimpleAuthenticationIdentity(
                TestConfiguration.searchUserID, TestConfiguration.searchUserPwd);
    }

    public void testAuthenticate() throws Throwable {
        System.out.println("Testing authenticate()...");
        this.authMan.authenticate(this.authID);
        System.out.println("[ authenticate() ] Test Completed.");
    }
}
