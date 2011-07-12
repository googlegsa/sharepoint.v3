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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import com.google.enterprise.connector.common.I18NUtil;
import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointConnectorType;
import com.google.enterprise.connector.spi.ConfigureResponse;

public class SharepointConnectorTypeTest extends TestCase {

    private List<String> keys;
    private Map<String, String> configMap;
    private SharepointConnectorType sharepointConnectorType;

    protected void setUp() throws Exception {
        System.out.println("\n...Setting Up...");

        this.configMap = new HashMap<String, String>();
        this.configMap.putAll(TestConfiguration.getConfigMap());
        this.keys = new ArrayList<String>();
        this.keys.addAll(this.configMap.keySet());

        this.sharepointConnectorType = new SharepointConnectorType();
        assertNotNull(this.sharepointConnectorType);
        System.out.println("SharepointConnectorType has been initialized successfully.");

        this.sharepointConnectorType.setConfigKeys(this.keys);
        System.out.println("Configuration keys has been loaded successfully.");
    }

    public void testValidateConfig() {
        System.out.println("Testing validateConfig()...");
        /*
         * ResourceBundle rb =
         * ResourceBundle.getBundle("SharepointConnectorResources",
         * I18NUtil.getLocaleFromStandardLocaleString("en")); String str =
         * rb.getString("Cannot_Connect"); System.out.println(str);
         */
        final ConfigureResponse configRes = this.sharepointConnectorType.validateConfig(this.configMap, I18NUtil.getLocaleFromStandardLocaleString("en"), null);
        assertNull(configRes);
        System.out.println("[ validateConfig() ] Test Passed.");
    }

    public void testGetConfigForm() {

        final ConfigureResponse configureResponse = this.sharepointConnectorType.getConfigForm(new Locale(
                "en"));
        final String initialConfigForm = configureResponse.getFormSnippet();
        final boolean check = this.checkForExpectedFields(initialConfigForm);
        assertTrue(check);
        /*
         * DocumentBuilderFactory factory =
         * DocumentBuilderFactory.newInstance(); try { DocumentBuilder builder =
         * factory.newDocumentBuilder(); Document document =
         * builder.parse("<top>"+initialConfigForm+"</top>"); } catch
         * (ParserConfigurationException pce) { System.out.println(pce); } catch
         * (SAXException se) { System.out.println(se); } catch (IOException ioe)
         * { System.out.println(ioe); }
         */

        // String newForm = ServletUtil.filterSensitiveData(initialConfigForm);
        // assertNotNull(newForm);
        System.out.println("[ getConfigForm() ] Test Completed.");
    }

    public void testGetPopulatedConfigForm() {
        final ConfigureResponse response = this.sharepointConnectorType.getPopulatedConfigForm(this.configMap, new Locale(
                "test"));
        final String initialConfigForm = response.getFormSnippet();
        final boolean check = this.checkForExpectedFields(initialConfigForm);
        if (check) {
            System.out.println("[ getConfigForm() ] Test Completed.");
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
        strPattern = "<input.*id=\"AliasMap\".*>";
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

        return true;
    }
}