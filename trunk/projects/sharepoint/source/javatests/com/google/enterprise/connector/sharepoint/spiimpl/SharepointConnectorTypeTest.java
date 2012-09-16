// Copyright 2007 Google Inc.
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

package com.google.enterprise.connector.sharepoint.spiimpl;

import com.google.common.collect.Maps;
import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.wsclient.mock.MockClientFactory;
import com.google.enterprise.connector.spi.ConfigureResponse;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class SharepointConnectorTypeTest extends TestCase {
  private Map<String, String> configMap;
  private SharepointConnectorType connectorType;
  private boolean editMode;
  private final ResourceBundle bundle =
      ResourceBundle.getBundle("SharepointConnectorResources", Locale.ENGLISH);

  protected void setUp() throws Exception {
    configMap = Maps.newHashMap(TestConfiguration.getConfigMap());
    String isSelected = configMap.get(SPConstants.PUSH_ACLS);
    editMode = !isSelected.equalsIgnoreCase(SPConstants.TRUE);
    connectorType = new SharepointConnectorType();
    connectorType.setClientFactory(new MockClientFactory());
  }

  public void testConfigFields() {
    // Be careful to preserve ordering in both collections.
    assertEquals(
        new ArrayList<String>(TestConfiguration.getConfigMap().keySet()),
        SharepointConnectorType.CONFIG_FIELDS);
  }

  public void testValidateConfig() {
    ConfigureResponse response =
        connectorType.validateConfig(configMap, Locale.ENGLISH, null);
    if (response != null) {
      fail("Expected null but got: " + response.getMessage() + "\n"
          + response.getFormSnippet());
    }
  }

  /** Tests fields that are always required. */
  public void testValidateConfigRequiredFields() {
    for (String key : SharepointConnectorType.CONFIG_FIELDS) {
      if (connectorType.isRequired(key)) {
        System.out.println("REQUIRED: " + key);
        configMap = Maps.newHashMap(TestConfiguration.getConfigMap());
        configMap.remove(key);
        ConfigureResponse response = connectorType.validateConfig(configMap,
            Locale.ENGLISH, null);
        assertNotNull(key, response);
        assertFind(bundle.getString(key), response.getMessage());
        assertFind("<font color=\"red\">", response.getFormSnippet());
        checkFormSnippet(response.getFormSnippet());
      }
    }
  }

  /**
   * Tests fields that can be optional. The test configurations should
   * be such that all of them are optional here.
   */
  // TODO: Test fields that may be required in other configurations
  // (e.g., LDAP settings with ACLs enabled, or user profile settings).
  public void testValidateConfigOptionalFields() {
    for (String key : SharepointConnectorType.CONFIG_FIELDS) {
      if (!connectorType.isRequired(key)) {
        configMap = Maps.newHashMap(TestConfiguration.getConfigMap());
        configMap.put("socialOption", "no");
        configMap.remove(key);
        ConfigureResponse response = connectorType.validateConfig(configMap,
            Locale.ENGLISH, null);
        if (response != null) {
          fail("Expected null but got: " + response.getMessage() + "\n"
              + response.getFormSnippet());
        }
      }
    }
  }

  public void testGetConfigForm() {
    ConfigureResponse response = connectorType.getConfigForm(Locale.ENGLISH);
    checkFormSnippet(response.getFormSnippet());
  }

  public void testGetPopulatedConfigForm() {
    ConfigureResponse response =
        connectorType.getPopulatedConfigForm(configMap, new Locale("test"));
    checkFormSnippet(response.getFormSnippet());
  }

  /** Asserts that the given pattern is found in the given string. */
  private void assertFind(String strPattern, String configForm) {
    assertTrue("Match for " + strPattern + " not found in: " + configForm,
        Pattern.compile(strPattern).matcher(configForm).find());
  }

  /** Checks the given form snippet for completeness. */
  private void checkFormSnippet(String formSnippet) {
    checkForExpectedFields(formSnippet);
    if (!editMode) {
      checkForDisabledFields(formSnippet);
    }
    StringBuffer buffer = new StringBuffer();
    connectorType.addJavaScript(buffer);
    assertFind(Pattern.quote(buffer.toString()), formSnippet);
  }

  private void checkForExpectedFields(final String configForm) {
    for (String key : SharepointConnectorType.CONFIG_FIELDS) {
      assertFind("<(input|textarea|select).*id=\""+key+"\".*>", configForm);
    }
  }

  private void checkForDisabledFields(final String configForm) {
    assertFind("<input.*id=\"cacheRefreshInterval\" disabled=\"true\".*>", configForm);
    assertFind("<input.*id=\"initialCacheSize\".*disabled=\"true\".*>", configForm);
  }
}
