//Copyright (C) 2006 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint;

import javax.security.auth.login.LoginException;

import junit.framework.TestCase;

import com.google.enterprise.connector.instantiator.MockInstantiator;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.persist.ConnectorStateStore;
import com.google.enterprise.connector.persist.MockConnectorStateStore;
import com.google.enterprise.connector.persist.StoreContext;
import com.google.enterprise.connector.pusher.MockPusher;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointConnector;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointTraversalManager;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.traversal.BatchResult;
import com.google.enterprise.connector.traversal.BatchSize;
import com.google.enterprise.connector.traversal.QueryTraverser;
import com.google.enterprise.connector.traversal.Traverser;

public class IntegrationTest extends TestCase {

    private SharepointConnector connector;

    public static final int TOTAL_DOCS = 185;// set the total expected documents

    public void setUp() throws Exception {
        super.setUp();
        System.out.println("\n...Setting Up...");
        System.out.println("Initializing Sharepoint Connector Instance ...");
        this.connector = new SharepointConnector(
                TestConfiguration.sharepointUrl, TestConfiguration.domain,
                TestConfiguration.username, TestConfiguration.Password,
                TestConfiguration.googleConnectorWorkDir,
                TestConfiguration.includedURls, TestConfiguration.excludedURls,
                TestConfiguration.mySiteBaseURL, TestConfiguration.AliasMap,
                TestConfiguration.feedType);
        this.connector.setIncluded_metadata(TestConfiguration.whiteList);
        this.connector.setExcluded_metadata(TestConfiguration.blackList);
        this.connector.setFQDNConversion(true);
    }

    /**
     * Test method for
     * {@link com.google.enterprise.connector.traversal.QueryTraverser #runBatch(int)}
     * .
     *
     * @throws InterruptedException
     * @throws RepositoryException
     * @throws LoginException
     */
    public final void testRunBatch() throws InterruptedException,
            RepositoryException {
        final int iBatch = 100;
        this.runTestBatches(iBatch);
    }

    private void runTestBatches(final int batchSize)
            throws InterruptedException, RepositoryException {
        final String connectorName = "sharepoint";
        final Session session = this.connector.login();
        GlobalState.forgetState(null); // used to delete the connector state
                                        // file.. testing purpose
        final SharepointTraversalManager manager = (SharepointTraversalManager) session.getTraversalManager();
        final MockPusher pusher = new MockPusher(System.out);
        final StoreContext storeContext = new StoreContext(connectorName);
        final ConnectorStateStore connectorStateStore = new MockConnectorStateStore();
        final MockInstantiator instantiator = new MockInstantiator(null);
        final Traverser traverser = new QueryTraverser(pusher, manager,
                instantiator.getTraversalStateStore(connectorName),
                connectorName, Context.getInstance().getTraversalContext());
        instantiator.setupTraverser(connectorName, traverser);
        System.out.println("\nRunning batch test batchsize " + batchSize);

        int docsProcessed = -1;
        int totalDocsProcessed = 0;
        int batchNumber = 0;
        while (true) {
            BatchResult result = traverser.runBatch(new BatchSize(10, 20));// do
                                                                            // the
                                                                            // traversal
            totalDocsProcessed += result.getCountProcessed();// do the
                                                                // checkpointing
                                                                // after
                                                // the traversal
            System.out.println("Batch# " + batchNumber + " docs "
                    + docsProcessed + " checkpoint "
                    + connectorStateStore.getConnectorState(storeContext));
            batchNumber++;

            // start recrawl cycle
            if (docsProcessed == 0) {
                System.out.println("No new documents discovered");
            }

        }
    }
}
