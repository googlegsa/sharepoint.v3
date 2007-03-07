// Copyright (C) 2007 Google Inc.
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
import com.google.enterprise.connector.pusher.DocPusher;
import com.google.enterprise.connector.pusher.Pusher;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.traversal.QueryTraverser;
import com.google.enterprise.connector.traversal.Traverser;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;


public class SharepointQueryTraveralManagerTest extends TestCase {

  /**
   * Test method for
   * {@link com.google.enterprise.connector.traversal.QueryTraverser
   * #runBatch(int)}.
   * @throws InterruptedException 
   */
  public final void testRunBatch() throws InterruptedException, 
    RepositoryException, FileNotFoundException {  
    
    runTestBatches(5);
    
  }

  private void runTestBatches(int batchSize) throws InterruptedException, 
    RepositoryException {
    final String sharepointUrl = "http://entpoint05.corp.google.com/unittest";
    final String domain = "ent-qa-d3";
    final String host = "entpoint05.corp.google.com";
    final int port = 80;
    final String username = "testing";
    final String password = "g00gl3";
    
    
    SharepointConnector sharepointConnector = new SharepointConnector(
                        sharepointUrl, domain, username, password);
    Session sess = sharepointConnector.login();
    

    String connectorName = "sharepoint-connector";
    QueryTraversalManager qtm = sess.getQueryTraversalManager();
    Pusher pusher;
    boolean caughtException = false;
    try {
      PrintStream out = 
        new PrintStream(new FileOutputStream("traverser-test.log"));
      pusher = new DocPusher(new MockFileFeedConnection(out));
      ConnectorStateStore connectorStateStore = new MockConnectorStateStore();
      Traverser traverser =
        new QueryTraverser(pusher, qtm, connectorStateStore, connectorName);

      System.out.println();
      System.out.println("Running batch test batchsize " + batchSize);
      
      traverser.runBatch(batchSize);      
      
    } catch (Exception e) {
      caughtException = true;
    }
    Assert.assertFalse(caughtException);
  }

}
