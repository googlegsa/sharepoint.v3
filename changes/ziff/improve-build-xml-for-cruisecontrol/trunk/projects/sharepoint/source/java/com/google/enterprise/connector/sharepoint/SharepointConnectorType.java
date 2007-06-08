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
import com.google.enterprise.connector.spi.ConnectorType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;


public class SharepointConnectorType implements ConnectorType {
  private static final Logger LOGGER = Logger
  .getLogger(SharepointConnectorType.class.getName());

  private static final String VALUE = "value";
  private static final String NAME = "name";
  private static final String TEXT = "text";
  private static final String TYPE = "type";
  private static final String INPUT = "input";
  private static final String CLOSE_ELEMENT = "/>";
  private static final String OPEN_ELEMENT = "<";
  private static final String PASSWORD = "password";
  private static final String TR_END = "</tr>\r\n";
  private static final String TD_END = "</td>\r\n";
  private static final String TD_START = "<td>";
  private static final String TR_START = "<tr>\r\n";
  
  private static final String REQ_FIELDS_MISSING = 
    "Field_Is_Missing";
  private static final String REQ_FQDN_URL = 
    "Url_Entered_Should_Be_Fully_Qualified";
  
  private List keys = null;
  private Set keySet = null;
  private HashMap<String, String> configStrings = new HashMap<String, String>();
  private String initialConfigForm = null;
  
  public void SharepointConnectorType() {

  }
  
  /**
   * Sets the keys that are required for configuration. These are the actual 
   * keys used by the class. 
   * @param keys A list of String keys
   */
  public void setConfigKeys(List keys) {
    if (this.keys != null) {
      throw new IllegalStateException();
    }
    this.keys = keys;
    this.keySet = new HashSet(keys);
  }  
  
  /**
   * Sets the display strings for the configuration form depending on the 
   * language settings.
   * @param rb Resource bundle for the particular language
   */
  private void setConfigStrings(ResourceBundle rb) {
    for (Object key : keys) {
      configStrings.put((String)key, rb.getString((String)key));     
    }
  }
  
  /**
   * Gets the initial/blank form.
   * @return HTML form as string
   */
  private String getInitialConfigForm() {
    if (initialConfigForm != null) {
      return initialConfigForm;
    }
    if (keys == null) {
      throw new IllegalStateException();
    }
    this.initialConfigForm = makeConfigForm(null);
    return initialConfigForm;
  }
  
  /**
   * Makes a config form snippet using the keys (in the supplied order) and, if
   * passed a non-null config map, pre-filling values in from that map
   * 
   * @param configMap
   * @return config form snippet
   */
  private String makeConfigForm(Map configMap) {
    StringBuffer buf = new StringBuffer(2048);
    for (Iterator i = keys.iterator(); i.hasNext();) {
      String key = (String) i.next();
      String configKey = configStrings.get(key);
      
      appendStartRow(buf, configKey, false);
      buf.append(OPEN_ELEMENT);
      buf.append(INPUT);
      if (key.equalsIgnoreCase(PASSWORD)) {
        appendAttribute(buf, TYPE, PASSWORD);
      } else {
        appendAttribute(buf, TYPE, TEXT);
      }
      appendAttribute(buf, NAME, key);
      if (configMap != null) {
        String value = (String) configMap.get(key);
        if (value != null) {
          appendAttribute(buf, VALUE, value);
        }
      }
      appendEndRow(buf);
    }
    return buf.toString();
  }
  
  private void appendStartRow(StringBuffer buf, String key, boolean red) {
    buf.append(TR_START);
    buf.append(TD_START);
    if (red) {
      buf.append("<font color=red>");
    }
    buf.append(key);
    if (red) {
      buf.append("</font>");
    }
    buf.append(TD_END);
    buf.append(TD_START);
  }

  private void appendEndRow(StringBuffer buf) {
    buf.append(CLOSE_ELEMENT);
    buf.append(TD_END);
    buf.append(TR_END);
  }
  
  private void appendAttribute(StringBuffer buf, String attrName,
      String attrValue) {
    buf.append(" ");
    buf.append(attrName);
    buf.append("=\"");
    // TODO xml-encode the special characters (< > " etc.)
    buf.append(attrValue);
    buf.append("\"");
  }

  /**
   * Validates a given map, i.e., checks if a value if null or zero length.
   * It also checks if the value is a url, then it should be fully qualified.
   * @param configData Map of keys and values
   * @return message string depending on the validation.
   */
  private boolean validateConfigMap(Map configData) {
    for (Iterator i = keys.iterator(); i.hasNext();) {
      String key = (String) i.next();
      String val = (String) configData.get(key);
      if (val == null || val.length() == 0) {
        return false;
      }      
      if (val.startsWith("http://") && !val.contains(".")) {
        return false;
      }
    }
    return true;
  }
  
  private ConfigureResponse makeValidatedForm(Map configMap, ResourceBundle rb) 
      {
    StringBuffer buf = new StringBuffer(2048);   
    String message = "";
    for (Iterator i = keys.iterator(); i.hasNext();) {
      String key = (String) i.next();
      String configKey = configStrings.get(key);

      String value = (String) configMap.get(key);
      if (value == null || value.length() == 0) {
        message = configKey + " "  + rb.getString(REQ_FIELDS_MISSING);
      }      
      if (value.startsWith("http://") && !value.contains(".")) {
        message = rb.getString(REQ_FQDN_URL);
      }
      if (message.equals("")) {
        appendStartRow(buf, configKey, false);
        buf.append(OPEN_ELEMENT);
        buf.append(INPUT);
        if (key.equalsIgnoreCase(PASSWORD)) {
          appendAttribute(buf, TYPE, PASSWORD);
        } else {
          appendAttribute(buf, TYPE, TEXT);
          appendAttribute(buf, VALUE, value);
        }                
      } else {
        appendStartRow(buf, configKey, true);
        buf.append(OPEN_ELEMENT);
        buf.append(INPUT);
        if (key.equalsIgnoreCase(PASSWORD)) {
          appendAttribute(buf, TYPE, PASSWORD);
        } else {
          appendAttribute(buf, TYPE, TEXT);
        }
      }
      appendAttribute(buf, NAME, key);
      appendEndRow(buf);
    }

    // toss in all the stuff that's in the map but isn't in the keyset
    // taking care to list them in alphabetic order (this is mainly for
    // testability).
    Iterator i = new TreeSet(configMap.keySet()).iterator();
    while (i.hasNext()) {
      String key = (String) i.next();
      if (!keySet.contains(key)) {
        // add another hidden field to preserve this data
        String val = (String) configMap.get(key);
        buf.append("<input type=\"hidden\" value=\"");
        buf.append(val);
        buf.append("\" name=\"");
        buf.append(key);
        buf.append("\"/>\r\n");
      }
    }      
    return new ConfigureResponse(message, buf.toString());
  }
  
  public ConfigureResponse getConfigForm(Locale locale) {
    ResourceBundle rb = ResourceBundle.getBundle("SharepointResources", locale);
    setConfigStrings(rb);
    ConfigureResponse result = new ConfigureResponse("",
        getInitialConfigForm());
    LOGGER.info("getConfigForm form:\n" + result.getFormSnippet());
    return result;
  }

  public ConfigureResponse getPopulatedConfigForm(Map configMap, Locale locale) 
      {
    ResourceBundle rb = ResourceBundle.getBundle("SharepointResources", locale);
    setConfigStrings(rb);
    ConfigureResponse result = new ConfigureResponse("",
        makeConfigForm(configMap));
    return result;
  }

  public ConfigureResponse validateConfig(Map configData, Locale locale) {
    ResourceBundle rb = ResourceBundle.getBundle("SharepointResources", locale);
    setConfigStrings(rb);
    if (validateConfigMap(configData)) {
      // all is ok
      return null;
    }
    ConfigureResponse configureResponse =  makeValidatedForm(configData, rb);
    LOGGER.info("validateConfig message:\n" + configureResponse.getMessage());
    LOGGER.info("validateConfig new form:\n" + 
        configureResponse.getFormSnippet());
    return configureResponse;
  }

}
