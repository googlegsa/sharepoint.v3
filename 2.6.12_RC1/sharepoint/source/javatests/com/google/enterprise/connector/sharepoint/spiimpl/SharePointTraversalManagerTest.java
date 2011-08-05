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
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;

public class SharePointTraversalManagerTest extends TestCase {

    SharepointClientContext sharepointClientContext;
    SharepointTraversalManager travMan;

    protected void setUp() throws Exception {
        System.out.println("\n...Setting Up...");
        System.out.println("Initializing SharepointClientContext ...");
        this.sharepointClientContext = TestConfiguration.initContext();

        assertNotNull(this.sharepointClientContext);
        sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
        sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);

        System.out.println("Initializing SharepointConnector ...");
        final SharepointConnector connector = TestConfiguration.getConnectorInstance();
        connector.setFQDNConversion(TestConfiguration.FQDNflag);
        System.out.println("Initializing SharepointTraversalManager ...");
        this.travMan = new SharepointTraversalManager(connector,
                this.sharepointClientContext);
        this.travMan.setBatchHint(100);
    }

    public void testStartTraversal() {
        System.out.println("Testing startTraversal()...");
        try {
            final DocumentList docList = this.travMan.startTraversal();
            assertNotNull(docList);
            System.out.println("[ startTraversal() ] Test Passed.");
        } catch (final RepositoryException re) {
            System.out.println(re);
            System.out.println("[ startTraversal() ] Test Failed.");
        }
    }

    public void testResumeTraversal() {
        System.out.println("Testing resumeTraversal()...");
        try {
            final DocumentList docList = this.travMan.resumeTraversal("SharePoint");
            assertNotNull(docList);
            System.out.println("[ resumeTraversal() ] Test Passed.");
        } catch (final RepositoryException re) {
            System.out.println(re);
            System.out.println("[ resumeTraversal() ] Test Failed.");
        }
    }
}
