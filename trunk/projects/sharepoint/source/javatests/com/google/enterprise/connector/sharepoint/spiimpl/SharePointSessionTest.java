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
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.TraversalManager;

public class SharePointSessionTest extends TestCase {

    SharepointClientContext sharepointClientContext;
    SharepointSession session;

    protected void setUp() throws Exception {
        System.out.println("\n...Setting Up...");
        System.out.println("Initializing SharepointClientContext ...");
        this.sharepointClientContext = TestConfiguration.initContext();
        assertNotNull(this.sharepointClientContext);
        sharepointClientContext.setFeedType(FeedType.CONTENT_FEED);

        System.out.println("Initializing SharepointConnector ...");
        final SharepointConnector connector = new SharepointConnector();
        connector.setFQDNConversion(TestConfiguration.FQDNflag);
        System.out.println("Initializing SharepointSession ...");
        this.session = new SharepointSession(connector,
                this.sharepointClientContext);
    }

    public void testGetAuthenticationManager() {
        System.out.println("Testing getAuthenticationManager()...");
        try {
            final AuthenticationManager authZMan = this.session.getAuthenticationManager();
            assertNotNull(authZMan);
            System.out.println("[ getAuthenticationManager() ] Test Passed.");
        } catch (final RepositoryException re) {
            System.out.println(re);
            System.out.println("[ getAuthenticationManager() ] Test Failed.");
        }
    }

    public void testGetAuthorizationManager() {
        System.out.println("Testing getAuthorizationManager()...");
        try {
            final AuthorizationManager authNMan = this.session.getAuthorizationManager();
            assertNotNull(authNMan);
            System.out.println("[ getAuthorizationManager() ] Test Passed.");
        } catch (final RepositoryException re) {
            System.out.println(re);
            System.out.println("[ getAuthorizationManager() ] Test Failed.");
        }
    }

    public void testGetTraversalManager() {
        System.out.println("Testing getTraversalManager()...");
        try {
            final TraversalManager travMan = this.session.getTraversalManager();
            assertNotNull(travMan);
            System.out.println("[ getTraversalManager() ] Test Passed.");
        } catch (final RepositoryException re) {
            System.out.println(re);
            System.out.println("[ getTraversalManager() ] Test Failed.");
        }
    }
}
