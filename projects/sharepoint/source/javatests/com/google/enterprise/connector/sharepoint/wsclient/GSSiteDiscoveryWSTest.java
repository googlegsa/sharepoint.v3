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

import java.util.Set;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;

public class GSSiteDiscoveryWSTest extends TestCase {

    SharepointClientContext sharepointClientContext;
    GSSiteDiscoveryWS siteDisc;

    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("\n...Setting Up...");
        System.out.println("Initializing SharepointClientContext ...");
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

        System.out.println("Initializing GSSiteDiscoveryWS ...");
        this.siteDisc = new GSSiteDiscoveryWS(this.sharepointClientContext);
    }

    public final void testGetMatchingSiteCollections() {
        System.out.println("Testing getMatchingSiteCollections()");
        final Set siteCol = this.siteDisc.getMatchingSiteCollections();
        assertNotNull(siteCol);
        System.out.println("Total site colllections discovered: "
                + siteCol.size());
        System.out.println("[ getMatchingSiteCollections() ] Test Completed.");
    }

    public final void testGetFQDNHost() {
        System.out.println("Testing getFQDNHost()");
        final String domain_fqdn = this.siteDisc.getFQDNHost(TestConfiguration.domain);
        assertNotNull(domain_fqdn);
        System.out.println("[ getFQDNHost() ] Test Completed.");
    }

}
