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

import com.google.enterprise.connector.instantiator.MockInstantiator;
import com.google.enterprise.connector.instantiator.ThreadPool;
import com.google.enterprise.connector.manager.Context;
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
import com.google.enterprise.connector.util.SystemClock;

import junit.framework.TestCase;

public class IntegrationTest extends TestCase {
  private SharepointConnector connector;

  public void setUp() throws Exception {
    super.setUp();
    this.connector = TestConfiguration.getConnectorInstance();
  }

  public void testTraversal() throws InterruptedException, RepositoryException {
    final String connectorName = "sharepoint";
    final Session session = this.connector.login();
    GlobalState.forgetState(null);
    final SharepointTraversalManager traversalManager = (SharepointTraversalManager) session.getTraversalManager();
    final MockInstantiator instantiator = new MockInstantiator(new ThreadPool(
        5, new SystemClock()));
    final Traverser traverser = new QueryTraverser(new MockPusher(System.out),
        traversalManager, instantiator.getTraversalStateStore(connectorName),
        connectorName, Context.getInstance().getTraversalContext(),
        new SystemClock(), null);
    BatchResult result = traverser.runBatch(new BatchSize(10, 20));
    int totalDocsProcessed = result.getCountProcessed();
    assertTrue(totalDocsProcessed > 0);
  }
}
