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
    keys.add("foo");
    keys.add("bar");
    keys.add("password");
    sharepointConnectorType = new SharepointConnectorType();
    sharepointConnectorType.setConfigKeys(keys);
    super.setUp();
  }

  public void testGetConfigForm() { 
    String expected = "<tr>\r\n" + 
            "<td>Foo</td>\r\n" + 
            "<td><input type=\"text\" name=\"foo\"/></td>\r\n" + 
            "</tr>\r\n" + 
            "<tr>\r\n" + 
            "<td>Bar</td>\r\n" + 
            "<td><input type=\"text\" name=\"bar\"/></td>\r\n" + 
            "</tr>\r\n" + 
            "<tr>\r\n" + 
            "<td>Password</td>\r\n" + 
            "<td><input type=\"password\" name=\"password\"/></td>\r\n" + 
            "</tr>\r\n";
    ConfigureResponse configureResponse = sharepointConnectorType
        .getConfigForm(new Locale("test"));
    String initialConfigForm = configureResponse.getFormSnippet();
    System.out.println(initialConfigForm);
    Assert.assertEquals(expected, initialConfigForm);
  }

  public void testGetPopulatedConfigForm() {
    String expected = "<tr>\r\n" + 
            "<td>Foo</td>\r\n" + 
            "<td><input type=\"text\" name=\"foo\" value=\"foo_val\"/></td>\r\n"
            + "</tr>\r\n" + 
            "<tr>\r\n" + 
            "<td>Bar</td>\r\n" + 
            "<td><input type=\"text\" name=\"bar\" value=\"http://xyz\"/>" +
            "</td>\r\n" + 
            "</tr>\r\n" + 
            "<tr>\r\n" + 
            "<td>Password</td>\r\n" + 
            "<td><input type=\"password\" name=\"password\" " +
            "value=\"password_val\"/></td>\r\n" + 
            "</tr>\r\n";
    Map configMap = new HashMap();
    configMap.put("foo", "foo_val");
    configMap.put("bar", "http://xyz");
    configMap.put("password", "password_val");
    
    ConfigureResponse response = sharepointConnectorType.getPopulatedConfigForm
        (configMap, new Locale("test"));
    System.out.println(response.getMessage() + "\n" + 
        response.getFormSnippet());
    Assert.assertEquals(expected, response.getFormSnippet());    
  }
  
  public void testValidateConfigRequiredField() {
    String expectedMessage = "Required field not specified: Foo";
    Map configMap = new HashMap();
    configMap.put("foo", "");
    configMap.put("bar", "http://xyz.com");
    configMap.put("password", "password_val");
    ConfigureResponse response = 
      sharepointConnectorType.validateConfig(configMap, new Locale("test"));
    System.out.println(response.getMessage() + "\n" + 
        response.getFormSnippet());
    Assert.assertEquals(expectedMessage, response.getMessage());
  }
  
  public void testValidateConfigFQDN() {
    String expectedMessage = "The Bar must contain a fully qualified domain name. Please check the Bar value.";
    Map configMap = new HashMap();
    configMap.put("foo", "foo_val");
    configMap.put("bar", "http://");
    configMap.put("password", "password_val");
    ConfigureResponse response = 
      sharepointConnectorType.validateConfig(configMap, new Locale("test"));
    System.out.println(response.getMessage() + "\n" + 
        response.getFormSnippet());
    Assert.assertEquals(expectedMessage, response.getMessage());
  }
}
