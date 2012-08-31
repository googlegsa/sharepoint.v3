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

import com.google.enterprise.connector.spi.Session;

import junit.framework.TestCase;

public class ConnectivityTests extends TestCase {
  
  public void testSSLConnectWithUpn() throws Exception {
    AdGroupsConnector con = new AdGroupsConnector();
    
    con.setMethod("SSL");
    con.setHostname(TestConfiguration.d1hostname);
    con.setPort(Integer.toString(TestConfiguration.d1port));
    con.setPrincipal(TestConfiguration.d1upn);
    con.setPassword(TestConfiguration.d1password);
    
    con.setDataSource("h2", TestConfiguration.dbs.get("h2"));
    
    Session s = con.login();
    s.getTraversalManager();
  }
  
  public void testSSLConnectWithNetbios() throws Exception {
    AdGroupsConnector con = new AdGroupsConnector();
    
    con.setMethod("SSL");
    con.setHostname(TestConfiguration.d1hostname);
    con.setPort(Integer.toString(TestConfiguration.d1port));
    con.setPrincipal(TestConfiguration.d1principal);
    con.setPassword(TestConfiguration.d1password);
    
    con.setDataSource("h2", TestConfiguration.dbs.get("h2"));
    
    Session s = con.login();
    s.getTraversalManager();
  }
  
  //TODO: investigate if anonymous binds are possible
  public void testSSLConnectAnonymous() throws Exception {
    AdGroupsConnector con = new AdGroupsConnector();
    
    con.setMethod("SSL");
    con.setHostname(TestConfiguration.d1hostname);
    con.setPort(Integer.toString(TestConfiguration.d1plaintextport));
    con.setPrincipal(TestConfiguration.d1principal);
    con.setPassword(TestConfiguration.d1password);
    
    con.setDataSource("h2", TestConfiguration.dbs.get("h2"));
    
    Session s = con.login();
    s.getTraversalManager();
  }
  
  public void testPlainConnectWithUpn() throws Exception {
    AdGroupsConnector con = new AdGroupsConnector();
    
    con.setMethod("STANDARD");
    con.setHostname(TestConfiguration.d1hostname);
    con.setPort(Integer.toString(TestConfiguration.d1plaintextport));
    con.setPrincipal(TestConfiguration.d1upn);
    con.setPassword(TestConfiguration.d1password);
    
    con.setDataSource("h2", TestConfiguration.dbs.get("h2"));
    
    Session s = con.login();
    s.getTraversalManager();
  }
  
  public void testPlainConnectWithNetbios() throws Exception {
    AdGroupsConnector con = new AdGroupsConnector();
    
    con.setMethod("STANDARD");
    con.setHostname(TestConfiguration.d1hostname);
    con.setPort(Integer.toString(TestConfiguration.d1plaintextport));
    con.setPrincipal(TestConfiguration.d1principal);
    con.setPassword(TestConfiguration.d1password);
    
    con.setDataSource("h2", TestConfiguration.dbs.get("h2"));
    
    Session s = con.login();
    s.getTraversalManager();
  }

  //TODO: investigate if anonymous binds are possible
  public void testPlainConnectAnonymous() throws Exception {
    AdGroupsConnector con = new AdGroupsConnector();
    
    con.setMethod("STANDARD");
    con.setHostname(TestConfiguration.d1hostname);
    con.setPort(Integer.toString(TestConfiguration.d1plaintextport));
    
    con.setDataSource("h2", TestConfiguration.dbs.get("h2"));
    
    Session s = con.login();
    s.getTraversalManager();
  }
}
