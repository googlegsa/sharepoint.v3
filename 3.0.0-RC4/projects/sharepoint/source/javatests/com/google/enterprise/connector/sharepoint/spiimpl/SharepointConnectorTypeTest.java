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

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.spi.ConfigureResponse;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SharepointConnectorTypeTest extends TestCase {
  private List<String> keys;
  private Map<String, String> configMap;
  private SharepointConnectorType sharepointConnectorType;
  private boolean editMode = false;

  protected void setUp() throws Exception {
    configMap = new HashMap<String, String>();
    configMap.putAll(TestConfiguration.getConfigMap());
    keys = new ArrayList<String>();
    keys.addAll(configMap.keySet());
    String isSelected = configMap.get(SPConstants.PUSH_ACLS);
    // if feedAcls option gets selected
    if (!isSelected.equalsIgnoreCase(SPConstants.TRUE)) {
      editMode = SPConstants.EDIT_MODE;
    } else {
      editMode = false;
    }
    sharepointConnectorType = new SharepointConnectorType();
    sharepointConnectorType.setConfigKeys(keys);
  }

  public void testValidateConfig() {
    final ConfigureResponse configRes =
        sharepointConnectorType.validateConfig(configMap, Locale.ENGLISH, null);
    assertNull(configRes);
  }

  public void testGetConfigForm() {
    final ConfigureResponse configureResponse =
        sharepointConnectorType.getConfigForm(Locale.ENGLISH);
    final String initialConfigForm = configureResponse.getFormSnippet();
    checkForExpectedFields(initialConfigForm);
    if (!editMode) {
      checkForDisabledFields(initialConfigForm);
    }
  }

  public void testGetPopulatedConfigForm() {
    final ConfigureResponse response =
        sharepointConnectorType.getPopulatedConfigForm(configMap, new Locale("test"));
    final String populatedConfigForm = response.getFormSnippet();
    checkForExpectedFields(populatedConfigForm);
    if (!editMode) {
      checkForExpectedFields(populatedConfigForm);
    }
  }

  /** Asserts that the given pattern is found in the given string. */
  private void assertFind(String strPattern, String configForm) {
    assertTrue("Match for " + strPattern + " not found in: " + configForm,
        Pattern.compile(strPattern).matcher(configForm).find());
  }

  private void checkForExpectedFields(final String configForm) {
    assertFind("<input.*id=\"sharepointUrl\".*>", configForm);
    assertFind("<textarea.*id=\"excludedURls\".*>", configForm);
    assertFind("<input.*id=\"aliasMap\".*>", configForm);
    assertFind("<textarea.*id=\"includedURls\".*>", configForm);
    assertFind("<input.*id=\"domain\".*>", configForm);
    assertFind("<input.*id=\"username\".*>", configForm);
    assertFind("<input.*id=\"mySiteBaseURL\".*>", configForm);
    assertFind("<input.*id=\"authorization\".*>", configForm);
    assertFind("<input.*id=\"useSPSearchVisibility\".*>", configForm);
    assertFind("<input.*id=\"pushAcls\".*>", configForm);
    assertFind("<input.*id=\"appendNamespaceInSPGroup\".*>", configForm);
    assertFind("<select.*id=\"usernameFormatInAce\".*>", configForm);
    assertFind("<select.*id=\"groupnameFormatInAce\".*>", configForm);
    assertFind("<input.*id=\"ldapServerHostAddress\".*>", configForm);
    assertFind("<input.*id=\"portNumber\".*>", configForm);
    assertFind("<select.*id=\"authenticationType\".*>", configForm);
    assertFind("<select.*id=\"connectMethod\".*>", configForm);
    assertFind("<input.*id=\"useCacheToStoreLdapUserGroupsMembership\".*>", configForm);
    assertFind("<input.*id=\"initialCacheSize\".*>", configForm);
    assertFind("<input.*id=\"cacheRefreshInterval\".*>", configForm);
  }

  private void checkForDisabledFields(final String configForm) {
    assertFind("<input.*id=\"cacheRefreshInterval\" disabled=\"true\".*>", configForm);
    assertFind("<input.*id=\"initialCacheSize\".*disabled=\"true\".*>", configForm);
  }
}
