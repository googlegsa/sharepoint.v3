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



import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SimpleResultSet;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.FileOutputStream;
import java.io.PrintStream;


public class SharepointQueryTraversalManagerTest extends TestCase {

  final String sharepointUrl = "http://entpoint05.corp.google.com/unittest";
  final String domain = "ent-qa-d3";
  final String host = "entpoint05.corp.google.com";
  final int port = 80;
  final String username = "testing";
  final String password = "g00gl3";
  
  /**
   * Test method for
   * {@link com.google.enterprise.connector.traversal.QueryTraverser
   * #runBatch(int)}.
   * @throws InterruptedException, RepositoryException
   */
  public final void testRunBatch() throws InterruptedException, 
    RepositoryException {      
    runTestBatches(5); 
    runTestBatches(6);
    runTestBatches(7);
    runTestBatches(8);
    runTestBatches(9);
    runTestBatches(10);
  }

  private void runTestBatches(int batchSize) throws InterruptedException, 
    RepositoryException {
    SharepointConnector sharepointConnector = new SharepointConnector(
                        sharepointUrl, domain, username, password);
    Session sess = sharepointConnector.login();
    String connectorName = "sharepoint-connector";
    SharepointQueryTraversalManager qtm = 
        (SharepointQueryTraversalManager) sess.getQueryTraversalManager();
    try {
      System.out.println("\nRunning batch test batchsize " + batchSize);
      SimpleResultSet rs = (SimpleResultSet) qtm.startTraversal();
      Assert.assertEquals(7, rs.size());
    } catch (Exception e) {      
      fail(e.toString());
    }
  }  
}
