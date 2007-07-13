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

import com.google.enterprise.connector.spi.ConfigureResponse;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SharepointConnectorTypeTest extends TestCase {

  private List keys;
  private SharepointConnectorType sharepointConnectorType;
  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    keys = new ArrayList<String>();
    keys.add("sharepointUrl");
    keys.add("domain");
    keys.add("username");
    keys.add("password");
    sharepointConnectorType = new SharepointConnectorType();
    sharepointConnectorType.setConfigKeys(keys);
    super.setUp();
  }

  public void testGetConfigForm() { 
    String expected = "<tr>\r\n" + 
            "<td>SharepointUrl TEST</td>\r\n" + 
            "<td><input type=\"text\" name=\"sharepointUrl\"/></td>\r\n" + 
            "</tr>\r\n" + 
            "<tr>\r\n" + 
            "<td>Domain TEST</td>\r\n" + 
            "<td><input type=\"text\" name=\"domain\"/></td>\r\n" + 
            "</tr>\r\n" + 
            "<tr>\r\n" +
            "<td>Username TEST</td>\r\n" + 
            "<td><input type=\"text\" name=\"username\"/></td>\r\n" + 
            "</tr>\r\n" + 
            "<tr>\r\n" + 
            "<td>Password TEST</td>\r\n" + 
            "<td><input type=\"password\" name=\"password\"/></td>\r\n" + 
            "</tr>\r\n";
    ConfigureResponse configureResponse = sharepointConnectorType
        .getConfigForm(new Locale("test"));
    String initialConfigForm = configureResponse.getFormSnippet();
    Assert.assertEquals(expected, initialConfigForm);
  }

  public void testGetPopulatedConfigForm() {
    String expected = "<tr>\r\n" + 
            "<td>SharepointUrl TEST</td>\r\n" + 
            "<td><input type=\"text\" name=\"sharepointUrl\" " +
            "value=\"http://entpoint05.corp.google.com/unittest\"/></td>\r\n"
            + "</tr>\r\n" + 
            "<tr>\r\n" + 
            "<td>Domain TEST</td>\r\n" + 
            "<td><input type=\"text\" name=\"domain\" value=\"ent-qa-d3\"/>" +
            "</td>\r\n" + 
            "</tr>\r\n" + 
            "<tr>\r\n" + 
            "<td>Username TEST</td>\r\n" + 
            "<td><input type=\"text\" name=\"username\" value=\"testing\"/>" +
            "</td>\r\n" + 
            "</tr>\r\n" + 
            "<tr>\r\n" + 
            "<td>Password TEST</td>\r\n" + 
            "<td><input type=\"password\" name=\"password\" " +
            "value=\"g00gl3\"/></td>\r\n" + 
            "</tr>\r\n";
    Map configMap = new HashMap();
    configMap.put("sharepointUrl", "http://entpoint05.corp.google.com/unittest");
    configMap.put("domain", "ent-qa-d3");
    configMap.put("username", "testing");
    configMap.put("password", "g00gl3");
    
    ConfigureResponse response = sharepointConnectorType.getPopulatedConfigForm
        (configMap, new Locale("test"));
    Assert.assertEquals(expected, response.getFormSnippet());    
  }
  
  public void testValidateConfigRequiredField() {
    String expectedMessage = "Required field not specified: Domain TEST";
    String expectedFormSnippet = "<tr>\r\n" + 
    "<td>SharepointUrl TEST</td>\r\n" + 
    "<td><input type=\"text\" " +
    "value=\"http://entpoint05.corp.google.com/unittest\" " +
    "name=\"sharepointUrl\"/></td>\r\n"
    + "</tr>\r\n" + 
    "<tr>\r\n" + 
    "<td><font color=red>Domain TEST</font></td>\r\n" + 
    "<td><input type=\"text\" name=\"domain\"/>" +
    "</td>\r\n" + 
    "</tr>\r\n" + 
    "<tr>\r\n" + 
    "<td>Username TEST</td>\r\n" + 
    "<td><input type=\"text\" value=\"testing\" name=\"username\"/>" +
    "</td>\r\n" + 
    "</tr>\r\n" + 
    "<tr>\r\n" + 
    "<td>Password TEST</td>\r\n" + 
    "<td><input type=\"password\" name=\"password\"" +
    "/></td>\r\n" + 
    "</tr>\r\n";
    Map configMap = new HashMap();
    configMap.put("sharepointUrl", "http://entpoint05.corp.google.com/unittest");
    configMap.put("domain", "");
    configMap.put("username", "testing");
    configMap.put("password", "g00gl3");    
    ConfigureResponse response = 
      sharepointConnectorType.validateConfig(configMap, new Locale("test"));
    Assert.assertEquals(expectedMessage, response.getMessage());
    Assert.assertEquals(expectedFormSnippet, response.getFormSnippet());
  }
  
  public void testValidateConfigFQDN() {
    String expectedMessage = "The SharepointUrl TEST must contain a fully " +
    		"qualified domain name. Please check the SharepointUrl TEST value.";
    String expectedFormSnippet = "<tr>\r\n" + 
    "<td><font color=red>SharepointUrl TEST</font></td>\r\n" + 
    "<td><input type=\"text\" " +
    "name=\"sharepointUrl\"/></td>\r\n"
    + "</tr>\r\n" + 
    "<tr>\r\n" + 
    "<td>Domain TEST</td>\r\n" + 
    "<td><input type=\"text\" value=\"ent-qa-d3\" name=\"domain\"/>" +
    "</td>\r\n" + 
    "</tr>\r\n" + 
    "<tr>\r\n" + 
    "<td>Username TEST</td>\r\n" + 
    "<td><input type=\"text\" value=\"\" name=\"username\"/>" +
    "</td>\r\n" + 
    "</tr>\r\n" + 
    "<tr>\r\n" + 
    "<td>Password TEST</td>\r\n" + 
    "<td><input type=\"password\" name=\"password\"" +
    "/></td>\r\n" + 
    "</tr>\r\n";
    Map configMap = new HashMap();
    configMap.put("sharepointUrl", "http://a");
    configMap.put("domain", "ent-qa-d3");
    configMap.put("username", "");
    configMap.put("password", "g00gl3");     
    ConfigureResponse response = 
      sharepointConnectorType.validateConfig(configMap, new Locale("test"));
    Assert.assertEquals(expectedMessage, response.getMessage());
    Assert.assertEquals(expectedFormSnippet, response.getFormSnippet());
  }
  
  public void testValidateConfigConnectivity() {
    String expectedMessage = "Cannot connect to the given SharepointUrl TEST with the supplied Domain TEST/Username TEST/Password TEST. Please re-enter.";
    String expectedFormSnippet = "<tr>\r\n" + 
    "<td><font color=red>SharepointUrl TEST</font></td>\r\n" + 
    "<td><input type=\"text\" " +
    "name=\"sharepointUrl\"/></td>\r\n"
    + "</tr>\r\n" + 
    "<tr>\r\n" + 
    "<td><font color=red>Domain TEST</font></td>\r\n" + 
    "<td><input type=\"text\" name=\"domain\"/>" +
    "</td>\r\n" + 
    "</tr>\r\n" + 
    "<tr>\r\n" + 
    "<td><font color=red>Username TEST</font></td>\r\n" + 
    "<td><input type=\"text\" name=\"username\"/>" +
    "</td>\r\n" + 
    "</tr>\r\n" + 
    "<tr>\r\n" + 
    "<td><font color=red>Password TEST</font></td>\r\n" + 
    "<td><input type=\"password\" name=\"password\"" +
    "/></td>\r\n" + 
    "</tr>\r\n";
    Map configMap = new HashMap();
    configMap.put("sharepointUrl", "http://entpoint.corp.google.com/Marketing");
    configMap.put("domain", "ent-qa-d3");
    configMap.put("username", "testing");
    configMap.put("password", "g00gl3");     
    ConfigureResponse response = 
      sharepointConnectorType.validateConfig(configMap, new Locale("test"));
    Assert.assertEquals(expectedMessage, response.getMessage());
    Assert.assertEquals(expectedFormSnippet, response.getFormSnippet());
  }
}
