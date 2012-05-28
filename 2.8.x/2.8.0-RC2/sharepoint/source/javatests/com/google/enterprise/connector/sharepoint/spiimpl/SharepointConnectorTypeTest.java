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

package com.google.enterprise.connector.sharepoint.spiimpl;

import com.google.enterprise.connector.common.I18NUtil;
import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.spi.ConfigureResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class SharepointConnectorTypeTest extends TestCase {

    private List<String> keys;
    private Map<String, String> configMap;
    private SharepointConnectorType sharepointConnectorType;
    private boolean editMode = false;

    protected void setUp() throws Exception {
        System.out.println("\n...Setting Up...");

        this.configMap = new HashMap<String, String>();
        this.configMap.putAll(TestConfiguration.getConfigMap());
        this.keys = new ArrayList<String>();
        this.keys.addAll(this.configMap.keySet());
        String isSelected = configMap.get(SPConstants.PUSH_ACLS);
        // if feedAcls option gets selected
        if (!isSelected.equalsIgnoreCase(SPConstants.TRUE)) {
            this.editMode = SPConstants.EDIT_MODE;
        } else {
            this.editMode = false;
        }
        this.sharepointConnectorType = new SharepointConnectorType();
        assertNotNull(this.sharepointConnectorType);
        System.out.println("SharepointConnectorType has been initialized successfully.");

        this.sharepointConnectorType.setConfigKeys(this.keys);
        System.out.println("Configuration keys has been loaded successfully.");
    }

    public void testValidateConfig() {
        final ConfigureResponse configRes = this.sharepointConnectorType.validateConfig(this.configMap, I18NUtil.getLocaleFromStandardLocaleString("en"), null);
        assertNull(configRes);
    }

    public void testGetConfigForm() {
        final ConfigureResponse configureResponse = this.sharepointConnectorType.getConfigForm(new Locale(
                "en"));
        final String initialConfigForm = configureResponse.getFormSnippet();
        final boolean check = this.checkForExpectedFields(initialConfigForm);
        assertTrue(check);
        if (!editMode) {
            checkForDisabledFileds(initialConfigForm);
        }
    }

    public void testGetPopulatedConfigForm() {
        final ConfigureResponse response = this.sharepointConnectorType.getPopulatedConfigForm(this.configMap, new Locale(
                "test"));
        final String populatedConfigForm = response.getFormSnippet();
        final boolean check = this.checkForExpectedFields(populatedConfigForm);
        assertTrue(check);
        if (!editMode) {
            checkForExpectedFields(populatedConfigForm);
        }
    }

    private boolean checkForExpectedFields(final String configForm) {

        System.out.println("Checking for SharePointURL field...");
        String strPattern = "<input.*id=\"sharepointUrl\".*>";
        Pattern pattern = Pattern.compile(strPattern);
        Matcher match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for excludedURls field...");
        strPattern = "<textarea.*id=\"excludedURls\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for AliasMap field...");
        strPattern = "<input.*id=\"aliasMap\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for includedURls field...");
        strPattern = "<textarea.*id=\"includedURls\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for domain field...");
        strPattern = "<input.*id=\"domain\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for username field...");
        strPattern = "<input.*id=\"username\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for mySiteBaseURL field...");
        strPattern = "<input.*id=\"mySiteBaseURL\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for authorization field...");
        strPattern = "<input.*id=\"authorization\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for useSPSearchVisibility field...");
        strPattern = "<input.*id=\"useSPSearchVisibility\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for pushAcls field...");
        strPattern = "<input.*id=\"pushAcls\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for appendNamespaceInSPGroup field...");
        strPattern = "<input.*id=\"appendNamespaceInSPGroup\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for usernameFormatInAce field...");
        strPattern = "<select.*id=\"usernameFormatInAce\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for groupnameFormatInAce field...");
        strPattern = "<select.*id=\"groupnameFormatInAce\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for ldapServerHostAddress field...");
        strPattern = "<input.*id=\"ldapServerHostAddress\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for portNumber field...");
        strPattern = "<input.*id=\"portNumber\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for authenticationType field...");
        strPattern = "<select.*id=\"authenticationType\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for connectMethod field...");
        strPattern = "<select.*id=\"connectMethod\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for useCacheToStoreLdapUserGroupsMembership field...");
        strPattern = "<input.*id=\"useCacheToStoreLdapUserGroupsMembership\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for initialCacheSize field...");
        strPattern = "<input.*id=\"initialCacheSize\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for cacheRefreshInterval field...");
        strPattern = "<input.*id=\"cacheRefreshInterval\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());
        return true;
    }

    private boolean checkForDisabledFileds(final String configForm) {

        System.out.println("Checking for cacheRefreshInterval field...");
        String strPattern = "<input.*id=\"cacheRefreshInterval\" disabled=\"true\".*>";
        Pattern pattern = Pattern.compile(strPattern);
        Matcher match = pattern.matcher(configForm);
        assertTrue(match.find());

        System.out.println("Checking for initialCacheSize field...");
        strPattern = "<input.*id=\"initialCacheSize\".*disabled=\"true\".*>";
        pattern = Pattern.compile(strPattern);
        match = pattern.matcher(configForm);
        assertTrue(match.find());
        return true;
    }
}
