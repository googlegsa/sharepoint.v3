// Copyright 2012 Google Inc.
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

import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.TraversalManager;

import javax.sql.DataSource;

public class TestThread extends Thread {
  private AdGroupsConnector con;
  int runs;

  public TestThread(String hostname, int port, String principal,
      String password, String dbType, DataSource db, int runs) {
    con = new AdGroupsConnector();
    con.setMethod("SSL");
    con.setHostname(hostname);
    con.setPort(Integer.toString(port));
    con.setPrincipal(principal);
    con.setPassword(password);
    con.setDataSource(dbType, db);

    this.runs = runs;
  }

  @Override
  public void run() {
    try {
      TraversalManager tm = con.login().getTraversalManager();
      tm.startTraversal();
      for (int i = 1; i < runs; ++i) {
        tm.startTraversal();
      }
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
  }
}
