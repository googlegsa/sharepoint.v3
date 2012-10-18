// Copyright 2007 Google Inc.
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

package com.google.enterprise.connector.adgroups;

import com.google.enterprise.connector.adgroups.TestConfiguration;
import com.google.enterprise.connector.spi.ConfigureResponse;

import junit.framework.TestCase;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdGroupsConnectorTypeTest extends TestCase {

  protected Map<String, String> configure(String hostname, String port,
      String method, String principal, String password) {
    Map<String, String> config = new HashMap<String, String>();

    config.put("hostname", hostname);
    config.put("port", port);
    config.put("method", method);
    config.put("principal", principal);
    config.put("password", password);

    return config;
  }
  
  protected void assertContains(ConfigureResponse response, String message) {
    assertTrue(response.getFormSnippet().replace("</tr>", "</tr>\r"),
        response.getFormSnippet().contains(message));
  }

  public void testCorrect() {
    AdGroupsConnectorType type = new AdGroupsConnectorType();
    Map<String, String> config = configure(TestConfiguration.d1hostname,
            Integer.toString(TestConfiguration.d1plaintextport), "STANDARD",
            TestConfiguration.d1principal, 
            TestConfiguration.d1password);
    type.setConfigKeys(new ArrayList<String>(config.keySet()));
    ConfigureResponse response = type.validateConfig(config, null, null);
    assertNull(response);
  }

  public void testEmptyData() {
    AdGroupsConnectorType type = new AdGroupsConnectorType();
    Map<String, String> config = configure("", "", "STANDARD", "", "");
    type.setConfigKeys(new ArrayList<String>(config.keySet()));
    ConfigureResponse response = type.validateConfig(config, null, null);
    assertFalse(response.getMessage().isEmpty());
  }

  public void testEmptyDataSSL() {
    AdGroupsConnectorType type = new AdGroupsConnectorType();
    Map<String, String> config = configure("", "", "SSL", "", "");
    type.setConfigKeys(new ArrayList<String>(config.keySet()));
    ConfigureResponse response = type.validateConfig(config, null, null);
    assertFalse(response.getMessage().isEmpty());
  }

  public void testUnknownHost() {
    AdGroupsConnectorType type = new AdGroupsConnectorType();
    Map<String, String> config =
        configure("invalid.hostname", "389", "STANDARD", "", "");
    type.setConfigKeys(new ArrayList<String>(config.keySet()));
    ConfigureResponse response = type.validateConfig(config, null, null);
    assertContains(response, "Can't resolve hostname");    
  }

  public void testRefusedConnection() throws Exception {
    // get unused port number on current computer
    ServerSocket s = new ServerSocket(0);
    int port = s.getLocalPort();
    s.close();

    AdGroupsConnectorType type = new AdGroupsConnectorType();
    Map<String, String> config =
        configure("localhost", "" + port, "STANDARD", "", "");
    type.setConfigKeys(new ArrayList<String>(config.keySet()));
    ConfigureResponse response = type.validateConfig(config, null, null);
    assertContains(response, "Connection refused");
  }

  public void testSlowHost() throws Exception {
    // socket that never responds to requests
    ServerSocket s = new ServerSocket(0);
    int port = s.getLocalPort();

    AdGroupsConnectorType type = new AdGroupsConnectorType();
    Map<String, String> config =
        configure("localhost", "" + port, "STANDARD", "", "");
    type.setConfigKeys(new ArrayList<String>(config.keySet()));
    ConfigureResponse response = type.validateConfig(config, null, null);
    assertContains(response, "LDAP response read timed out");
  }

  public void testIncorrectUsername() {
    AdGroupsConnectorType type = new AdGroupsConnectorType();
    Map<String, String> config = configure(TestConfiguration.d1hostname,
            Integer.toString(TestConfiguration.d1plaintextport), "STANDARD",
            TestConfiguration.d1principal + "invalid", 
            TestConfiguration.d1password);
    type.setConfigKeys(new ArrayList<String>(config.keySet()));
    ConfigureResponse response = type.validateConfig(config, null, null);
    assertContains(response, "Invalid credentials.");
  }

  public void testIncorrectPassword() {
    AdGroupsConnectorType type = new AdGroupsConnectorType();
    Map<String, String> config = configure(TestConfiguration.d1hostname,
            Integer.toString(TestConfiguration.d1plaintextport), "STANDARD",
            TestConfiguration.d1principal, 
            TestConfiguration.d1password + "invalid");
    type.setConfigKeys(new ArrayList<String>(config.keySet()));
    ConfigureResponse response = type.validateConfig(config, null, null);
    assertContains(response, "Invalid credentials.");
  }
}
