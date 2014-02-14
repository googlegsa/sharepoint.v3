// Copyright 2013 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.adgroups;

import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.TraversalManager;

import junit.framework.TestCase;

public class AdGroupsTraversalManagerTest extends TestCase {
  public void testDocumentList() throws Exception {
    for (String dbType : TestConfiguration.dbs.keySet()) {
      AdGroupsConnector con = new AdGroupsConnector();
      con.setMethod("SSL");
      con.setHostname(TestConfiguration.d1hostname);
      con.setPort(Integer.toString(TestConfiguration.d1port));
      con.setPrincipal(TestConfiguration.d1principal);
      con.setPassword(TestConfiguration.d1password);
      con.setDataSource(dbType, TestConfiguration.dbs.get(dbType));
      Session s = con.login();
      TraversalManager out = s.getTraversalManager();

      DocumentList result = out.startTraversal();
      assertNull(result.nextDocument());
      assertEquals(AdConstants.CHECKPOINT_VALUE, result.checkpoint());

      result = out.resumeTraversal(AdConstants.CHECKPOINT_VALUE);
      assertNull(result);
    }
  }
}
