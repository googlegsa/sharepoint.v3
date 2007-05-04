// Copyright (C) 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint;

import com.google.enterprise.connector.persist.ConnectorStateStore;
import com.google.enterprise.connector.persist.MockConnectorStateStore;
import com.google.enterprise.connector.pusher.MockPusher;
import com.google.enterprise.connector.sharepoint.client.SharepointClient;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.traversal.QueryTraverser;
import com.google.enterprise.connector.traversal.Traverser;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * 
 */
public class IntegrationTest extends TestCase {

  final String sharepointUrl = "http://entpoint05.corp.google.com/unittest";
  final String domain = "ent-qa-d3";
  final String host = "entpoint05.corp.google.com";
  final int port = 80;
  final String username = "testing";
  final String password = "g00gl3";
  private SharepointClient sharepointClient;
  private Connector connector;  
  
  public static final int TOTAL_DOCS = 13;
  
  public void setUp() throws Exception {
    SharepointClientContext sharepointClientContext = new 
      SharepointClientContext(sharepointUrl, domain, username, password);
    sharepointClient = new SharepointClient(sharepointClientContext);
    connector = new SharepointConnector(sharepointUrl, 
        domain, username, password);      
    super.setUp();    
  }
  /**
   * Test method for
   * {@link com.google.enterprise.connector.traversal.QueryTraverser
   * #runBatch(int)}.
   * @throws InterruptedException 
   * @throws RepositoryException 
   * @throws LoginException 
   */
  public final void testRunBatch() throws InterruptedException,
      LoginException, RepositoryException {    
    runTestBatches(1);
    runTestBatches(2);
    runTestBatches(3);
    runTestBatches(4);
    runTestBatches(20);
  }

  private void runTestBatches(int batchSize) throws InterruptedException,
      LoginException, RepositoryException {
    String connectorName = "sharepoint";
    Session session = connector.login();
    SharepointQueryTraversalManager manager = 
        (SharepointQueryTraversalManager) session.getQueryTraversalManager(); 
    manager.forgetStateForUnittest();
    MockPusher pusher = new MockPusher(System.out);
    ConnectorStateStore connectorStateStore = new MockConnectorStateStore();

    Traverser traverser =
        new QueryTraverser(pusher, manager, connectorStateStore, connectorName);

    System.out.println();
    System.out.println("Running batch test batchsize " + batchSize);
    
    int docsProcessed = -1;
    int totalDocsProcessed = 0;
    int batchNumber = 0;
    while (docsProcessed != 0) {
      docsProcessed = traverser.runBatch(batchSize, null);
      totalDocsProcessed += docsProcessed;
      System.out.println("Batch# " + batchNumber + " docs " + docsProcessed +
          " checkpoint " + connectorStateStore.getConnectorState(connectorName));
      batchNumber++;
    }    
    Assert.assertEquals(TOTAL_DOCS,totalDocsProcessed);
  }
}
