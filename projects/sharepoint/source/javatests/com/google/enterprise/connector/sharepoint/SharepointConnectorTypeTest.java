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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.google.enterprise.connector.spi.ConfigureResponse;
/**
 * @author amit_kagrawal
 * */
public class SharepointConnectorTypeTest extends TestCase {

  private List keys;
  private SharepointConnectorType sharepointConnectorType;
  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    keys = new ArrayList();
    keys.add("sharepointUrl");
    keys.add("domain");
    keys.add("password");
    keys.add("username");
    keys.add("excludedURls");
    keys.add("includedURls");
    keys.add("mySiteBaseURL");
    keys.add("aliasHostName");
    keys.add("aliasPort");
    sharepointConnectorType = new SharepointConnectorType();
    sharepointConnectorType.setConfigKeys(keys);
    super.setUp();
  }

  public void testGetConfigForm() { 
	 String expected="<tr>\r\n"
		 +"<td><b>SharePoint Site URL*</b></td>\r\n"
		 +"<td><input type=\"text\" name=\"sharepointUrl\"/></td>\r\n"
		 +"</tr>\r\n"
		 +"<tr>\r\n"
		 +"<td><b>Domain*</b></td>\r\n"
		 +"<td><input type=\"text\" name=\"domain\"/></td>\r\n"
		 +"</tr>\r\n"
		 +"<tr>\r\n"
		 +"<td><b>Password*</b></td>\r\n"
		 +"<td><input type=\"password\" type=\"text\" name=\"password\"/></td>\r\n"
		 +"</tr>\r\n"
		 +"<tr>\r\n"
		 +"<td><b>Username*</b></td>\r\n"
		 +"<td><input type=\"text\" name=\"username\"/></td>\r\n"
		 +"</tr>\r\n"
		 +"<tr>\r\n"
		 +"<td>Do Not Include URLs Matching the Following Patterns</td>\r\n"
		 +"<td><textarea rows=\"5\" cols=\"60\" name=\"excludedURls\"/></textarea/></td>\r\n"
		 +"</tr>\r\n"
		 +"<tr>\r\n"
		 +"<td><b>Include URLs Matching the Following Patterns*</b></td>\r\n"
		 +"<td><textarea rows=\"5\" cols=\"60\" name=\"includedURls\"/></textarea/></td>\r\n"
		 +"</tr>\r\n"
		 +"<tr>\r\n"
		 +"<td>MySite URL</td>\r\n"
		 +"<td><input type=\"text\" name=\"mySiteBaseURL\"/></td>\r\n"
		 +"</tr>\r\n"
		 +"<tr>\r\n"
		 +"<td>SharePoint Site Alias Host Name</td>\r\n"
		 +"<td><input type=\"text\" name=\"aliasHostName\"/></td>\r\n"
		 +"</tr>\r\n"
		 +"<tr>\r\n"
		 +"<td>SharePoint Site Alias Port Number</td>\r\n"
		 +"<td><input type=\"text\" name=\"aliasPort\"/></td>\r\n"
		 +"</tr>\r\n"
		 +"<b>Fields with (*) are Manadatory</b>"; 
    
    //ConfigureResponse configureResponse = sharepointConnectorType.getConfigForm(new Locale("test"));
    ConfigureResponse configureResponse = sharepointConnectorType.getConfigForm(new Locale("en"));
    String initialConfigForm = configureResponse.getFormSnippet();
    Assert.assertEquals(expected, initialConfigForm);
  }

  public void testGetPopulatedConfigForm() {
	  
	  String expected="<tr>\r\n"
		  +"<td><b>SharepointUrl TEST*</b></td>\r\n"
		  +"<td><input type=\"text\" name=\"sharepointUrl\"/></td>\r\n"
		  +"</tr>\r\n"
		  +"<tr>\r\n"
		  +"<td><b>Domain TEST*</b></td>\r\n"
		  +"<td><input type=\"text\" name=\"domain\"/></td>\r\n"
		  +"</tr>\r\n"
		  +"<tr>\r\n"
		  +"<td><b>Password TEST*</b></td>\r\n"
		  +"<td><input type=\"password\" type=\"text\" name=\"password\" value=\"amit_kagrawal\"/></td>\r\n"
		  +"</tr>\r\n"
		  +"<tr>\r\n"
		  +"<td><b>Username TEST*</b></td>\r\n"
		  +"<td><input type=\"text\" name=\"username\"/></td>\r\n"
		  +"</tr>\r\n"
		  +"<tr>\r\n"
		  +"<td>Do Not Include URLs Matching the Following Patterns</td>\r\n"
		  +"<td><textarea rows=\"5\" cols=\"60\" name=\"excludedURls\"/></textarea/></td>\r\n"
		  +"</tr>\r\n"
		  +"<tr>\r\n"
		  +"<td><b>Include URLs Matching the Following Patterns*</b></td>\r\n"
		  +"<td><textarea rows=\"5\" cols=\"60\" name=\"includedURls\"/></textarea/></td>\r\n"
		  +"</tr>\r\n"
		  +"<tr>\r\n"
		  +"<td>MySite URL</td>\r\n"
		  +"<td><input type=\"text\" name=\"mySiteBaseURL\"/></td>\r\n"
		  +"</tr>\r\n"
		  +"<tr>\r\n"
		  +"<td>SharePoint Site Alias Host Name</td>\r\n"
		  +"<td><input type=\"text\" name=\"aliasHostName\"/></td>\r\n"
		  +"</tr>\r\n"
		  +"<tr>\r\n"
		  +"<td>SharePoint Site Alias Port Number</td>\r\n"
		  +"<td><input type=\"text\" name=\"aliasPort\"/></td>\r\n"
		  +"</tr>\r\n"
		  +"<b>Fields with (*) are Manadatory</b>"; 
    
    Map configMap = new HashMap();
    configMap.put("password", "user");
    
    ConfigureResponse response = sharepointConnectorType.getPopulatedConfigForm(configMap, new Locale("test"));
    Assert.assertEquals(expected, response.getFormSnippet());    
  }
  
  public void testValidateConfigRequiredField() {
    String expectedMessage = "Required field not specified: Domain TEST";
    String expectedFormSnippet ="<tr>\r\n"
    	+"<td><b>SharepointUrl TEST*</b></td>\r\n"
    	+"<td><input type=\"text\" value=\"http://entpoint05.corp.google.com/unittest\" name=\"sharepointUrl\"/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td><font color=red><b>Domain TEST*</b></font></td>\r\n"
    	+"<td><input type=\"text\" name=\"domain\"/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td><b>Password TEST*</b></td>\r\n"
    	+"<td><input type=\"password\" type=\"text\" value=\"g00gl3\" name=\"password\"/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td><b>Username TEST*</b></td>\r\n"
    	+"<td><input type=\"text\" value=\"testing\" name=\"username\"/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td>Do Not Include URLs Matching the Following Patterns</td>\r\n"
    	+"<td><textarea rows=\"5\" cols=\"60\" name=\"excludedURls\"/>null</textarea/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td><b>Include URLs Matching the Following Patterns*</b></td>\r\n"
    	+"<td><textarea rows=\"5\" cols=\"60\" name=\"includedURls\"/>null</textarea/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td>MySite URL</td>\r\n"
    	+"<td><input type=\"text\" value=\"null\" name=\"mySiteBaseURL\"/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td>SharePoint Site Alias Host Name</td>\r\n"
    	+"<td><input type=\"text\" value=\"null\" name=\"aliasHostName\"/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td>SharePoint Site Alias Port Number</td>\r\n"
    	+"<td><input type=\"text\" value=\"null\" name=\"aliasPort\"/></td>\r\n"
    	+"</tr>\r\n";

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
    String expectedMessage = "The SharepointUrl TEST must contain a fully " 
    	+"qualified domain name. Please check the SharepointUrl TEST value.";
    String expectedFormSnippet = "<tr>\r\n"
    	+"<td><font color=red><b>SharepointUrl TEST*</b></font></td>\r\n"
    	+"<td><input type=\"text\" name=\"sharepointUrl\"/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td><b>Domain TEST*</b></td>\r\n"
    	+"<td><input type=\"text\" value=\"ent-qa-d3\" name=\"domain\"/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td><b>Password TEST*</b></td>\r\n"
    	+"<td><input type=\"password\" type=\"text\" value=\"g00gl3\" name=\"password\"/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td><b>Username TEST*</b></td>\r\n"
    	+"<td><input type=\"text\" value=\"\" name=\"username\"/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td>Do Not Include URLs Matching the Following Patterns</td>\r\n"
    	+"<td><textarea rows=\"5\" cols=\"60\" name=\"excludedURls\"/>null</textarea/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td><b>Include URLs Matching the Following Patterns*</b></td>\r\n"
    	+"<td><textarea rows=\"5\" cols=\"60\" name=\"includedURls\"/>null</textarea/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td>MySite URL</td>\r\n"
    	+"<td><input type=\"text\" value=\"null\" name=\"mySiteBaseURL\"/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td>SharePoint Site Alias Host Name</td>\r\n"
    	+"<td><input type=\"text\" value=\"null\" name=\"aliasHostName\"/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td>SharePoint Site Alias Port Number</td>\r\n"
    	+"<td><input type=\"text\" value=\"null\" name=\"aliasPort\"/></td>\r\n"
    	+"</tr>\r\n";

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
    String expectedMessage = "The SharepointUrl TEST must contain a fully qualified domain name. Please check the SharepointUrl TEST value.";
    String expectedFormSnippet = "<tr>\r\n"
    	+"<td><font color=red><b>SharepointUrl TEST*</b></font></td>\r\n"
    	+"<td><input type=\"text\" name=\"sharepointUrl\"/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td><b>Domain TEST*</b></td>\r\n"
    	+"<td><input type=\"text\" value=\"ps4312\" name=\"domain\"/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td><b>Password TEST*</b></td>\r\n"
    	+"<td><input type=\"password\" type=\"text\" value=\"pspl!@#\" name=\"password\"/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td><b>Username TEST*</b></td>\r\n"
    	+"<td><input type=\"text\" value=\"Administrator\" name=\"username\"/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td>Do Not Include URLs Matching the Following Patterns</td>\r\n"
    	+"<td><textarea rows=\"5\" cols=\"60\" name=\"excludedURls\"/>null</textarea/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td><b>Include URLs Matching the Following Patterns*</b></td>\r\n"
    	+"<td><textarea rows=\"5\" cols=\"60\" name=\"includedURls\"/>null</textarea/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td>MySite URL</td>\r\n"
    	+"<td><input type=\"text\" value=\"null\" name=\"mySiteBaseURL\"/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td>SharePoint Site Alias Host Name</td>\r\n"
    	+"<td><input type=\"text\" value=\"null\" name=\"aliasHostName\"/></td>\r\n"
    	+"</tr>\r\n"
    	+"<tr>\r\n"
    	+"<td>SharePoint Site Alias Port Number</td>\r\n"
    	+"<td><input type=\"text\" value=\"null\" name=\"aliasPort\"/></td>\r\n"
    	+"</tr>\r\n";

    Map configMap = new HashMap();
    configMap.put("sharepointUrl", "http://");
    configMap.put("domain", "domain");
    configMap.put("username", "username");
    configMap.put("password", "password");     
    ConfigureResponse response = 
      sharepointConnectorType.validateConfig(configMap, new Locale("test"));
    Assert.assertEquals(expectedMessage, response.getMessage());
    Assert.assertEquals(expectedFormSnippet, response.getFormSnippet());
  }
}
