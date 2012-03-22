//Copyright (C) 2007 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.spiimpl;

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.ldap.LdapConstants.AuthType;
import com.google.enterprise.connector.sharepoint.ldap.LdapConstants.LdapConnectionError;
import com.google.enterprise.connector.sharepoint.ldap.LdapConstants.Method;
import com.google.enterprise.connector.sharepoint.ldap.UserGroupsService.LdapConnection;
import com.google.enterprise.connector.sharepoint.ldap.UserGroupsService.LdapConnectionSettings;
import com.google.enterprise.connector.sharepoint.wsclient.client.BulkAuthorizationWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.ClientFactory;
import com.google.enterprise.connector.sharepoint.wsclient.client.WebsWS;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.XmlUtils;

import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.contrib.auth.NegotiateScheme;

import gnu.regexp.RE;
import gnu.regexp.REMatch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InvalidNameException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

/**
 * ConnectorType implementation for Sharepoint This class is mainly desinged for
 * controlling the connector configuration which incompasses creation of
 * connector configuration form, validating the configuration values etc.
 * 
 * @author nitendra_thakur
 */
public class SharepointConnectorType implements ConnectorType {
  private final Logger LOGGER = Logger.getLogger(SharepointConnectorType.class
      .getName());
  private final String className = SharepointConnectorType.class.getName();
  private Collator collator = Util.getCollator();

  private String sharepointUrl = null;
  private String domain = null;
  private String username = null;
  private String password = null;
  private String includeURL = null;
  private String excludeURL = null;
  private String mySiteUrl = null;
  private Map<String, ArrayList<String>> aliasMap = null;
  private String useSPSearchVisibility = null;

  // Create dummy context for doing validations.
  private SharepointClientContext sharepointClientContext = null;

  private List<String> keys = null;
  private final HashMap<Object, String> configStrings = new HashMap<Object, String>();
  private String initialConfigForm = null;

  ResourceBundle rb = null;
  public static final String GOOGLE_CONN_WORK_DIR = "googleConnectorWorkDir";
  private String pushAcls = null;
  private String usernameFormatInAce;
  private String groupnameFormatInAce;
  private String ldapServerHostAddress;
  private String portNumber;
  private String authenticationType;
  private String connectMethod;
  private String searchBase;
  private String initialCacheSize;
  private String useCacheToStoreLdapUserGroupsMembership;
  private String cacheRefreshInterval;
  private LdapConnectionSettings ldapConnectionSettings;
  private boolean editMode;

  /**
   * The client factory used to configure and instantiate the
   * client web service facade.
   */
  private ClientFactory clientFactory;

  /**
   * Returns the client factory for the web services.
   *
   * @return a client factory object
   */
  public ClientFactory getClientFactory() {
    return clientFactory;
  }

  /**
   * Sets the client factory for the web services.
   *
   * @param clientFactory the client factory to use for the web services
   */
  public void setClientFactory(final ClientFactory clientFactory) {
    this.clientFactory = clientFactory;
  }

/**
   * Sets the keys that are required for configuration. These are the actual
   * keys used by the class.
   * 
   * @param inKeys
   *          A list of String keys
   */
  public void setConfigKeys(final List<String> inKeys) {
    if (keys != null) {
      throw new IllegalStateException();
    }
    if (inKeys != null) {
      keys = inKeys;
    }
  }

  /**
   * Sets the display strings for the configuration form depending on the
   * language settings.
   */
  private void setConfigStrings() {
    if (rb != null) {
      // checking the parameters of the resource bundle passed
      for (int iKey = 0; iKey < keys.size(); ++iKey) {
        final Object key = keys.get(iKey);
        configStrings.put(key, rb.getString((String) key));
      }
      configStrings.put(SPConstants.MANDATORY_FIELDS,
          rb.getString(SPConstants.MANDATORY_FIELDS));
      LOGGER.config("Config Strings loaded from the resource bundle : "
          + configStrings.toString());
    } else {
      LOGGER.warning("unable to get resource bundle");
    }
  }

  /**
   * Gets the initial/blank form.
   * 
   * @return HTML form as string
   */
  private String getInitialConfigForm() {
    if (keys == null) {
      throw new IllegalStateException();
    }
    initialConfigForm = makeConfigForm(null, null);
    return initialConfigForm;
  }

  /**
   * Makes a config form snippet using the keys (in the supplied order) and, if
   * passed a non-null config map, pre-filling values in from that map.
   * 
   * @param configMap
   *          The configuration keys and their values
   * @param ed
   *          Contains the validation error, if any
   */
  private String makeConfigForm(final Map<String, String> configMap,
      final ErrorDignostics ed) {
    final StringBuffer buf = new StringBuffer();
    if (keys != null) {
      for (String key : keys) {
        final String configKey = configStrings.get(key);

        if ((ed == null) || !key.equals(ed.error_key)) {
          appendStartRow(buf, key, configKey, false);
        } else {
          appendStartRow(buf, key, configKey, true);
        }

        if (collator.equals(key, SPConstants.ALIAS_MAP)) {
          appendTableForAliasMapping(buf);
          if (configMap == null) {
            appendRowForAliasMapping(buf, SPConstants.BLANK_STRING,
                SPConstants.BLANK_STRING, false);
          } else {
            final String aliasMapString = configMap.get(key);
            parseAlias(aliasMapString, null);
            if (aliasMap == null) {
              appendRowForAliasMapping(buf, SPConstants.BLANK_STRING,
                  SPConstants.BLANK_STRING, false);
            } else {
              final Set<String> aliasValues = aliasMap.keySet();
              int i = 0;
              for (final Iterator<String> aliasItr = aliasValues.iterator(); aliasItr
                  .hasNext();) {
                final String alias_source_pattern = aliasItr.next();
                String alias_host_port = "";
                final ArrayList<String> aliases = aliasMap
                    .get(alias_source_pattern);
                if (aliases.size() == 0) {
                  if (i % 2 == 0) {
                    appendRowForAliasMapping(buf, SPConstants.BLANK_STRING,
                        SPConstants.BLANK_STRING, false);
                  } else {
                    appendRowForAliasMapping(buf, SPConstants.BLANK_STRING,
                        SPConstants.BLANK_STRING, true);
                  }
                  ++i;
                } else {
                  try {
                    for (final Iterator<String> it = aliases.iterator(); it
                        .hasNext();) {
                      alias_host_port = it.next();
                      if (it.hasNext() || aliasItr.hasNext()) {
                        if (i % 2 == 0) {
                          appendRowForAliasMapping(buf, alias_source_pattern,
                              alias_host_port, false);
                        } else {
                          appendRowForAliasMapping(buf, alias_source_pattern,
                              alias_host_port, true);
                        }
                      } else {
                        if (i % 2 == 0) {
                          appendRowForAliasMapping(buf, alias_source_pattern,
                              alias_host_port, false);
                        } else {
                          appendRowForAliasMapping(buf, alias_source_pattern,
                              alias_host_port, true);
                        }
                      }
                      ++i;
                    }
                  } catch (final Exception e) {
                    final String logMessage = "Could not find the alias value for the pattern ["
                        + alias_source_pattern + "].";
                    LOGGER.log(Level.WARNING, logMessage, e);
                  }
                }
              }
            }
          }
          buf.append(SPConstants.TR_START);
          buf.append(SPConstants.TD_START_ADDMRE);
          buf.append(SPConstants.OPEN_ELEMENT);
          buf.append(SPConstants.INPUT);
          appendAttribute(buf, SPConstants.TYPE, SPConstants.SUBMIT);
          appendAttribute(buf, SPConstants.VALUE,
              rb.getString(SPConstants.ADD_MORE));
          appendAttribute(buf, SPConstants.ONCLICK, "addRow(); return false");
          buf.append(SPConstants.SLASH + SPConstants.CLOSE_ELEMENT);
          buf.append(SPConstants.TD_END);
          buf.append(SPConstants.TR_END);

          buf.append(SPConstants.END_TABLE);
        }

        String value = null;
        if (configMap != null) {
          value = configMap.get(key);
        }
        if (value == null) {
          value = "";
        }

        if (collator.equals(key, SPConstants.SOCIAL_OPTION)) {
          buf.append(SPConstants.BREAK_LINE);
          buf.append(SPConstants.OPEN_ELEMENT);
          buf.append(SPConstants.INPUT);
          appendAttribute(buf, SPConstants.TYPE, SPConstants.RADIO);
          appendAttribute(buf, SPConstants.CONFIG_NAME, key);
          appendAttribute(buf, SPConstants.CONFIG_ID, key);
          appendAttribute(buf, SPConstants.VALUE, SPConstants.SOCIAL_OPTION_YES);
          appendAttribute(buf, SPConstants.TITLE, rb.getString(SPConstants.SOCIAL_OPTION_YES));
          if (value.equalsIgnoreCase(SPConstants.SOCIAL_OPTION_YES)) {
            appendAttribute(buf, SPConstants.CHECKED, SPConstants.CHECKED);
          }
          buf.append(" /" + SPConstants.CLOSE_ELEMENT);
          buf.append(rb.getString(SPConstants.SOCIAL_OPTION_YES));
          
          buf.append(SPConstants.BREAK_LINE);
          buf.append(SPConstants.OPEN_ELEMENT);
          buf.append(SPConstants.INPUT);
          appendAttribute(buf, SPConstants.TYPE, SPConstants.RADIO);
          appendAttribute(buf, SPConstants.CONFIG_NAME, key);
          appendAttribute(buf, SPConstants.CONFIG_ID, key);
          appendAttribute(buf, SPConstants.VALUE, SPConstants.SOCIAL_OPTION_NO);
          appendAttribute(buf, SPConstants.TITLE, rb.getString(SPConstants.SOCIAL_OPTION_NO));
        //Checking radio button for No if value is blank or No or any other value other than Yes and Social Only
          if (value.equalsIgnoreCase(SPConstants.SOCIAL_OPTION_YES) == false && value.equalsIgnoreCase(SPConstants.SOCIAL_OPTION_ONLY) == false ) {
            appendAttribute(buf, SPConstants.CHECKED, SPConstants.CHECKED);
          }
          buf.append(" /" + SPConstants.CLOSE_ELEMENT);
          buf.append(rb.getString(SPConstants.SOCIAL_OPTION_NO));
          buf.append(SPConstants.BREAK_LINE);
          buf.append(SPConstants.OPEN_ELEMENT);
          buf.append(SPConstants.INPUT);
          appendAttribute(buf, SPConstants.TYPE, SPConstants.RADIO);
          appendAttribute(buf, SPConstants.CONFIG_NAME, key);
          appendAttribute(buf, SPConstants.CONFIG_ID, key);
          appendAttribute(buf, SPConstants.VALUE, SPConstants.SOCIAL_OPTION_ONLY);
          appendAttribute(buf, SPConstants.TITLE, rb.getString(SPConstants.SOCIAL_OPTION_ONLY));
          if (value.equalsIgnoreCase(SPConstants.SOCIAL_OPTION_ONLY)) {
            appendAttribute(buf, SPConstants.CHECKED, SPConstants.CHECKED);
          }
          buf.append(" /" + SPConstants.CLOSE_ELEMENT);
          buf.append(rb.getString(SPConstants.SOCIAL_OPTION_ONLY));
        } else if (collator.equals(key, SPConstants.AUTHORIZATION)) {

          buf.append(SPConstants.BREAK_LINE);
          buf.append(SPConstants.OPEN_ELEMENT);
          buf.append(SPConstants.INPUT);
          appendAttribute(buf, SPConstants.TYPE, SPConstants.RADIO);
          appendAttribute(buf, SPConstants.CONFIG_NAME, key);
          appendAttribute(buf, SPConstants.CONFIG_ID, key);
          appendAttribute(buf, SPConstants.VALUE,
              FeedType.METADATA_URL_FEED.toString());
          appendAttribute(buf, SPConstants.TITLE,
              rb.getString(SPConstants.HELP_AUTHZ_BY_GSA));
          if ((value.length() == 0)
              || value.equalsIgnoreCase(FeedType.METADATA_URL_FEED.toString())) {
            appendAttribute(buf, SPConstants.CHECKED, SPConstants.CHECKED);
          }
          buf.append(" /" + SPConstants.CLOSE_ELEMENT);
          buf.append(rb.getString(SPConstants.AUTHZ_BY_GSA));
          buf.append(SPConstants.BREAK_LINE);
          buf.append(SPConstants.OPEN_ELEMENT);
          buf.append(SPConstants.INPUT);
          appendAttribute(buf, SPConstants.TYPE, SPConstants.RADIO);
          appendAttribute(buf, SPConstants.CONFIG_NAME, key);
          appendAttribute(buf, SPConstants.CONFIG_ID, key);
          appendAttribute(buf, SPConstants.VALUE,
              FeedType.CONTENT_FEED.toString());
          appendAttribute(buf, SPConstants.TITLE,
              rb.getString(SPConstants.HELP_AUTHZ_BY_CONNECTOR));
          if (value.equalsIgnoreCase(FeedType.CONTENT_FEED.toString())) {
            appendAttribute(buf, SPConstants.CHECKED, SPConstants.CHECKED);
          }
          buf.append(" /" + SPConstants.CLOSE_ELEMENT);
          buf.append(rb.getString(SPConstants.AUTHZ_BY_CONNECTOR));
        } else if (collator.equals(key, SPConstants.SHAREPOINT_CRAWLING_OPTION)) {
          // displaying search visibility option
          String spSearchVisibilityValue = null;
          String spSearchVisibilityKey = SPConstants.USE_SP_SEARCH_VISIBILITY;
          buf.append(SPConstants.OPEN_ELEMENT);
          buf.append(SPConstants.INPUT);
          appendAttribute(buf, SPConstants.TYPE, SPConstants.CHECKBOX);
          appendAttribute(buf, SPConstants.CONFIG_NAME, spSearchVisibilityKey);
          appendAttribute(buf, SPConstants.CONFIG_ID, spSearchVisibilityKey);
          appendAttribute(buf, SPConstants.TITLE,
              rb.getString(SPConstants.USE_SP_SEARCH_VISIBILITY_HELP));
          // The value can be true if its a pre-configured connector
          // being edited and blank if the default connector form is
          // being displayed.
          if (configMap != null) {
            spSearchVisibilityValue = configMap
                .get(SPConstants.USE_SP_SEARCH_VISIBILITY);
          }

          if (spSearchVisibilityValue == null) {
            spSearchVisibilityValue = "";
          }
          if (spSearchVisibilityValue.equalsIgnoreCase("true")
              || spSearchVisibilityValue.length() == 0) {
            appendAttribute(buf, SPConstants.CHECKED, Boolean.toString(true));
          }
          buf.append(" /" + SPConstants.CLOSE_ELEMENT);
          // It allows to select check box using it's label.
          buf.append(SPConstants.OPEN_ELEMENT + SPConstants.LABEL_FOR
              + SPConstants.EQUAL_TO + "\""
              + SPConstants.USE_SP_SEARCH_VISIBILITY + "\""
              + SPConstants.CLOSE_ELEMENT);
          buf.append(rb.getString(SPConstants.USE_SP_SEARCH_VISIBILITY_LABEL));
          buf.append(SPConstants.OPEN_ELEMENT + SPConstants.FORWARD_SLASH
              + SPConstants.LABEL + SPConstants.CLOSE_ELEMENT);

          // dispplaying unpublished content feed option
          String feedUnpublishedContentValue = null;
          String unpublishedContentKey = SPConstants.FEED_UNPUBLISHED_CONTENT;
          buf.append(SPConstants.BREAK_LINE);
          buf.append(SPConstants.OPEN_ELEMENT);
          buf.append(SPConstants.INPUT);
          appendAttribute(buf, SPConstants.TYPE, SPConstants.CHECKBOX);
          appendAttribute(buf, SPConstants.CONFIG_NAME, unpublishedContentKey);
          appendAttribute(buf, SPConstants.CONFIG_ID, unpublishedContentKey);
          appendAttribute(buf, SPConstants.TITLE,
              rb.getString(unpublishedContentKey));
          if (configMap != null) {
            feedUnpublishedContentValue = configMap
                .get(SPConstants.FEED_UNPUBLISHED_CONTENT);
          }
          if (feedUnpublishedContentValue == null) {
            feedUnpublishedContentValue = "";
          }
          if (feedUnpublishedContentValue.equalsIgnoreCase("true")
              || feedUnpublishedContentValue.length() == 0) {
            appendAttribute(buf, SPConstants.CHECKED, Boolean.toString(true));
          } else {
            appendAttribute(buf, SPConstants.UNCHECKED, Boolean.toString(false));
          }
          buf.append(" /" + SPConstants.CLOSE_ELEMENT);
          // It allows to select check box using it's label.
          buf.append(SPConstants.OPEN_ELEMENT + SPConstants.LABEL_FOR
              + SPConstants.EQUAL_TO + "\""
              + SPConstants.FEED_UNPUBLISHED_CONTENT + "\""
              + SPConstants.CLOSE_ELEMENT);
          buf.append(rb.getString(SPConstants.FEED_UNPUBLISHED_CONTENT_LABEL));
          buf.append(SPConstants.OPEN_ELEMENT + SPConstants.FORWARD_SLASH
              + SPConstants.LABEL + SPConstants.CLOSE_ELEMENT);
          buf.append(SPConstants.BREAK_LINE);

        } else if (collator.equals(key, SPConstants.AUTHENTICATION_TYPE)) {
          buf.append(SPConstants.OPEN_ELEMENT + SPConstants.SELECT);
          appendAttribute(buf, SPConstants.CONFIG_NAME, key);
          appendAttribute(buf, SPConstants.CONFIG_ID, key);
          buf.append(SPConstants.SPACE + SPConstants.STYLE);

          if (editMode) {
            if (this.pushAcls.equalsIgnoreCase(SPConstants.OFF)) {
              buf.append(SPConstants.SPACE + SPConstants.DISABLED
                  + SPConstants.EQUAL_TO + "\"" + SPConstants.TRUE + "\"");
            }
          }
          buf.append(SPConstants.CLOSE_ELEMENT + SPConstants.NEW_LINE);
          buf.append(SPConstants.OPEN_ELEMENT + SPConstants.OPTION
              + SPConstants.SPACE + SPConstants.VALUE + SPConstants.EQUAL_TO
              + "\"");
          buf.append(SPConstants.AUTHENTICATION_TYPE_SIMPLE);
          buf.append("\"");
          if (Strings.isNullOrEmpty(value)
              || value.equalsIgnoreCase(SPConstants.AUTHENTICATION_TYPE_SIMPLE)) {
            buf.append(SPConstants.SPACE + SPConstants.SELECTED
                + SPConstants.EQUAL_TO + "\"" + SPConstants.SELECTED + "\"");
          }
          buf.append(SPConstants.CLOSE_ELEMENT
              + SPConstants.AUTHENTICATION_TYPE_SIMPLE);
          buf.append(SPConstants.OPEN_ELEMENT + "/" + SPConstants.OPTION
              + SPConstants.CLOSE_ELEMENT);
          buf.append(SPConstants.NEW_LINE + SPConstants.OPEN_ELEMENT
              + SPConstants.OPTION + SPConstants.SPACE + SPConstants.VALUE
              + SPConstants.EQUAL_TO + "\"");
          buf.append(SPConstants.AUTHENTICATION_TYPE_ANONYMOUS);
          buf.append("\"");
          if (value.equalsIgnoreCase(SPConstants.AUTHENTICATION_TYPE_ANONYMOUS)) {
            buf.append(SPConstants.SPACE + SPConstants.SELECTED
                + SPConstants.EQUAL_TO + "\"" + SPConstants.SELECTED + "\"");
          }
          buf.append(SPConstants.CLOSE_ELEMENT
              + SPConstants.AUTHENTICATION_TYPE_ANONYMOUS);
          buf.append(SPConstants.OPEN_ELEMENT + "/" + SPConstants.OPTION
              + SPConstants.CLOSE_ELEMENT);
          buf.append(SPConstants.NEW_LINE + SPConstants.OPEN_ELEMENT + "/"
              + SPConstants.SELECT + SPConstants.CLOSE_ELEMENT);
        } else if (collator.equals(key, SPConstants.CONNECT_METHOD)) {
          buf.append(SPConstants.OPEN_ELEMENT + SPConstants.SELECT);
          appendAttribute(buf, SPConstants.CONFIG_NAME, key);
          appendAttribute(buf, SPConstants.CONFIG_ID, key);
          buf.append(SPConstants.SPACE + SPConstants.STYLE);
          if (editMode) {
            if ((this.pushAcls == null) || (this.pushAcls.equalsIgnoreCase(SPConstants.OFF))) {
              buf.append(SPConstants.SPACE + SPConstants.DISABLED
                  + SPConstants.EQUAL_TO + "\"" + SPConstants.TRUE + "\"");
            }
          }
          buf.append(SPConstants.CLOSE_ELEMENT + SPConstants.NEW_LINE);
          buf.append(SPConstants.OPEN_ELEMENT + SPConstants.OPTION
              + SPConstants.SPACE + SPConstants.VALUE + SPConstants.EQUAL_TO
              + "\"");
          buf.append(SPConstants.CONNECT_METHOD_STANDARD);
          buf.append("\"");
          if (Strings.isNullOrEmpty(value)
              || value.equalsIgnoreCase(SPConstants.CONNECT_METHOD_STANDARD)) {
            buf.append(SPConstants.SPACE + SPConstants.SELECTED
                + SPConstants.EQUAL_TO + "\"" + SPConstants.SELECTED + "\"");
          }
          buf.append(SPConstants.CLOSE_ELEMENT
              + SPConstants.CONNECT_METHOD_STANDARD);
          buf.append(SPConstants.OPEN_ELEMENT + "/" + SPConstants.OPTION
              + SPConstants.CLOSE_ELEMENT);
          buf.append(SPConstants.NEW_LINE + SPConstants.OPEN_ELEMENT
              + SPConstants.OPTION + SPConstants.SPACE + SPConstants.VALUE
              + SPConstants.EQUAL_TO + "\"");
          buf.append(SPConstants.CONNECT_METHOD_SSL);
          buf.append("\"");
          if (value.equalsIgnoreCase(SPConstants.CONNECT_METHOD_SSL)) {
            buf.append(SPConstants.SPACE + SPConstants.SELECTED
                + SPConstants.EQUAL_TO + "\"" + SPConstants.SELECTED + "\"");
          }
          buf.append(SPConstants.CLOSE_ELEMENT + SPConstants.CONNECT_METHOD_SSL);
          buf.append(SPConstants.OPEN_ELEMENT + "/" + SPConstants.OPTION
              + SPConstants.CLOSE_ELEMENT);
          buf.append(SPConstants.NEW_LINE + SPConstants.OPEN_ELEMENT + "/"
              + SPConstants.SELECT + SPConstants.CLOSE_ELEMENT);
        } else if (collator.equals(key, SPConstants.PUSH_ACLS)) {
          buf.append(SPConstants.OPEN_ELEMENT);
          buf.append(SPConstants.INPUT);
          appendAttribute(buf, SPConstants.TYPE, SPConstants.CHECKBOX);
          appendAttribute(buf, SPConstants.CONFIG_NAME, key);
          appendAttribute(buf, SPConstants.CONFIG_ID, key);
          appendAttribute(buf, SPConstants.TITLE,
              rb.getString(SPConstants.PUSH_ACLS_LABEL));
          if (value.equalsIgnoreCase("true") || value.length() == 0) {
            appendAttribute(buf, SPConstants.CHECKED, Boolean.toString(true));
          } else {
            appendAttribute(buf, SPConstants.UNCHECKED, Boolean.toString(false));
            this.pushAcls = SPConstants.OFF;
          }
          buf.append(SPConstants.SPACE + SPConstants.ON_CLICK);
          buf.append("\"enableFeedAclsRelatedHtmlControles();\"");
          buf.append(" /" + SPConstants.CLOSE_ELEMENT);
          // It allows to select check box using it's label.
          buf.append(SPConstants.OPEN_ELEMENT + SPConstants.LABEL_FOR
              + SPConstants.EQUAL_TO + "\"" + key + "\""
              + SPConstants.CLOSE_ELEMENT);
          buf.append(rb.getString(SPConstants.PUSH_ACLS_LABEL));
          buf.append(SPConstants.OPEN_ELEMENT + SPConstants.FORWARD_SLASH
              + SPConstants.LABEL + SPConstants.CLOSE_ELEMENT);
        } else if (collator.equals(key,
            SPConstants.USE_CACHE_TO_STORE_LDAP_USER_GROUPS_MEMBERSHIP)) {
          buf.append(SPConstants.BREAK_LINE);
          buf.append(SPConstants.OPEN_ELEMENT);
          buf.append(SPConstants.INPUT);
          appendAttribute(buf, SPConstants.TYPE, SPConstants.CHECKBOX);
          appendAttribute(buf, SPConstants.CONFIG_NAME, key);
          appendAttribute(buf, SPConstants.CONFIG_ID, key);
          appendAttribute(
              buf,
              SPConstants.TITLE,
              rb.getString(SPConstants.USE_CACHE_TO_STORE_LDAP_USER_GROUPS_MEMBERSHIP_LABEL));
          if (value.equalsIgnoreCase("false") || value.length() == 0) {
            appendAttribute(buf, SPConstants.UNCHECKED, Boolean.toString(false));
            if (editMode) {
              buf.append(SPConstants.SPACE + SPConstants.DISABLED
                  + SPConstants.EQUAL_TO + "\"" + SPConstants.TRUE + "\"");
            }
          } else {
            appendAttribute(buf, SPConstants.CHECKED, Boolean.toString(true));
          }

          buf.append(SPConstants.SPACE + SPConstants.ON_CLICK);
          buf.append("\"enableOrDisableUserGroupsCacheControles();\"");
          buf.append(" /" + SPConstants.CLOSE_ELEMENT);
          // It allows to select check box using it's label.
          buf.append(SPConstants.OPEN_ELEMENT + SPConstants.LABEL_FOR
              + SPConstants.EQUAL_TO + "\"" + key + "\""
              + SPConstants.CLOSE_ELEMENT);
          buf.append(rb
              .getString(SPConstants.USE_CACHE_TO_STORE_LDAP_USER_GROUPS_MEMBERSHIP_LABEL));
          buf.append(SPConstants.OPEN_ELEMENT + SPConstants.FORWARD_SLASH
              + SPConstants.LABEL + SPConstants.CLOSE_ELEMENT);
        } else if ((collator.equals(key, SPConstants.EXCLUDED_URLS))
            || (collator.equals(key, SPConstants.INCLUDED_URLS))) {
          buf.append(SPConstants.OPEN_ELEMENT);
          buf.append(SPConstants.TEXTAREA);
          appendAttribute(buf, SPConstants.CONFIG_NAME, key);
          appendAttribute(buf, SPConstants.CONFIG_ID, key);
          appendAttribute(buf, SPConstants.ROWS, SPConstants.ROWS_VALUE);
          appendAttribute(buf, SPConstants.COLS, SPConstants.COLS_VALUE);
          appendAttribute(buf, SPConstants.TEXTBOX_SIZE,
              SPConstants.TEXTBOX_SIZE_VALUE);
          buf.append(SPConstants.CLOSE_ELEMENT);

          buf.append(value.replace(SPConstants.SPACE, SPConstants.NEW_LINE));

          buf.append(SPConstants.OPEN_ELEMENT);
          buf.append(SPConstants.END_TEXTAREA);
          buf.append(SPConstants.CLOSE_ELEMENT);
        } else if (collator.equals(key, SPConstants.USERNAME_FORMAT_IN_ACE)) {
          buf.append(SPConstants.OPEN_ELEMENT + SPConstants.SELECT);
          appendAttribute(buf, SPConstants.CONFIG_NAME, key);
          appendAttribute(buf, SPConstants.CONFIG_ID, key);
          buf.append(SPConstants.SPACE + SPConstants.STYLE);
          if (editMode) {
            if ((this.pushAcls == null) || (this.pushAcls.equalsIgnoreCase(SPConstants.OFF))) {
              buf.append(SPConstants.SPACE + SPConstants.DISABLED
                  + SPConstants.EQUAL_TO + "\"" + SPConstants.TRUE + "\"");
            }
          }
          buf.append(SPConstants.CLOSE_ELEMENT + SPConstants.NEW_LINE);
          buf.append(SPConstants.OPEN_ELEMENT + SPConstants.OPTION
              + SPConstants.SPACE + SPConstants.VALUE + SPConstants.EQUAL_TO
              + "\"");
          buf.append(SPConstants.USERNAME_FORMAT_IN_ACE_ONLY_USERNAME);
          buf.append("\"");
          if (Strings.isNullOrEmpty(value)
              || value
              .equalsIgnoreCase(SPConstants.USERNAME_FORMAT_IN_ACE_ONLY_USERNAME)) {
            buf.append(SPConstants.SPACE + SPConstants.SELECTED
                + SPConstants.EQUAL_TO + "\"" + SPConstants.SELECTED + "\"");
          }
          buf.append(SPConstants.CLOSE_ELEMENT
              + SPConstants.USERNAME_FORMAT_IN_ACE_ONLY_USERNAME);
          buf.append(SPConstants.OPEN_ELEMENT + "/" + SPConstants.OPTION
              + SPConstants.CLOSE_ELEMENT);
          buf.append(SPConstants.NEW_LINE + SPConstants.OPEN_ELEMENT
              + SPConstants.OPTION + SPConstants.SPACE + SPConstants.VALUE
              + SPConstants.EQUAL_TO + "\"");
          buf.append(SPConstants.USERNAME_FORMAT_IN_ACE_DOMAINNAME_SLASH_USERNAME);
          buf.append("\"");
          if (value
              .equalsIgnoreCase(SPConstants.USERNAME_FORMAT_IN_ACE_DOMAINNAME_SLASH_USERNAME)) {
            buf.append(SPConstants.SPACE + SPConstants.SELECTED
                + SPConstants.EQUAL_TO + "\"" + SPConstants.SELECTED + "\"");
          }
          buf.append(SPConstants.CLOSE_ELEMENT
              + SPConstants.USERNAME_FORMAT_IN_ACE_DOMAINNAME_SLASH_USERNAME);
          buf.append(SPConstants.OPEN_ELEMENT + "/" + SPConstants.OPTION
              + SPConstants.CLOSE_ELEMENT);
          buf.append(SPConstants.NEW_LINE + SPConstants.OPEN_ELEMENT
              + SPConstants.OPTION + SPConstants.SPACE + SPConstants.VALUE
              + SPConstants.EQUAL_TO + "\"");
          buf.append(SPConstants.USERNAME_FORMAT_IN_ACE_USERNAME_AT_DOMAINNAME);
          buf.append("\"");
          if (value
              .equalsIgnoreCase(SPConstants.USERNAME_FORMAT_IN_ACE_USERNAME_AT_DOMAINNAME)) {
            buf.append(SPConstants.SPACE + SPConstants.SELECTED
                + SPConstants.EQUAL_TO + "\"" + SPConstants.SELECTED + "\"");
          }
          buf.append(SPConstants.SPACE + ">"
              + SPConstants.USERNAME_FORMAT_IN_ACE_USERNAME_AT_DOMAINNAME
              + SPConstants.SPACE);
          buf.append(SPConstants.OPEN_ELEMENT + "/" + SPConstants.OPTION
              + SPConstants.CLOSE_ELEMENT);
          buf.append(SPConstants.NEW_LINE + SPConstants.OPEN_ELEMENT + "/"
              + SPConstants.SELECT + SPConstants.CLOSE_ELEMENT);
        } else if (collator.equals(key, SPConstants.GROUPNAME_FORMAT_IN_ACE)) {

          buf.append(SPConstants.OPEN_ELEMENT + SPConstants.SELECT);
          appendAttribute(buf, SPConstants.CONFIG_NAME, key);
          appendAttribute(buf, SPConstants.CONFIG_ID, key);
          buf.append(SPConstants.SPACE + SPConstants.STYLE);
          if (editMode) {
            if (this.pushAcls.equalsIgnoreCase(SPConstants.OFF)) {
              buf.append(SPConstants.SPACE + SPConstants.DISABLED
                  + SPConstants.EQUAL_TO + "\"" + SPConstants.TRUE + "\"");
            }
          }
          buf.append(SPConstants.CLOSE_ELEMENT + SPConstants.NEW_LINE);
          buf.append(SPConstants.OPEN_ELEMENT + SPConstants.OPTION
              + SPConstants.SPACE + SPConstants.VALUE + SPConstants.EQUAL_TO
              + "\"");
          buf.append(SPConstants.GROUPNAME_FORMAT_IN_ACE_ONLY_GROUP_NAME);
          buf.append("\"");
          if (value
              .equalsIgnoreCase(SPConstants.GROUPNAME_FORMAT_IN_ACE_ONLY_GROUP_NAME)) {
            buf.append(SPConstants.SPACE + SPConstants.SELECTED
                + SPConstants.EQUAL_TO + "\"" + SPConstants.SELECTED + "\"");
          }
          buf.append(SPConstants.CLOSE_ELEMENT
              + SPConstants.GROUPNAME_FORMAT_IN_ACE_ONLY_GROUP_NAME);
          buf.append(SPConstants.OPEN_ELEMENT + "/" + SPConstants.OPTION
              + SPConstants.CLOSE_ELEMENT);
          buf.append(SPConstants.NEW_LINE + SPConstants.OPEN_ELEMENT
              + SPConstants.OPTION + SPConstants.SPACE + SPConstants.VALUE
              + SPConstants.EQUAL_TO + "\"");
          buf.append(SPConstants.GROUPNAME_FORMAT_IN_ACE_DOMAINNAME_SLASH_GROUPNAME);
          buf.append("\"");
          if (Strings.isNullOrEmpty(value)
              || value
              .equalsIgnoreCase(SPConstants.GROUPNAME_FORMAT_IN_ACE_DOMAINNAME_SLASH_GROUPNAME)) {
            buf.append(SPConstants.SPACE + SPConstants.SELECTED
                + SPConstants.EQUAL_TO + "\"" + SPConstants.SELECTED + "\"");
          }
          buf.append(SPConstants.CLOSE_ELEMENT
              + SPConstants.GROUPNAME_FORMAT_IN_ACE_DOMAINNAME_SLASH_GROUPNAME);
          buf.append(SPConstants.OPEN_ELEMENT + "/" + SPConstants.OPTION
              + SPConstants.CLOSE_ELEMENT);
          buf.append(SPConstants.NEW_LINE + SPConstants.OPEN_ELEMENT
              + SPConstants.OPTION + SPConstants.SPACE + SPConstants.VALUE
              + SPConstants.EQUAL_TO + "\"");
          buf.append(SPConstants.GROUPNAME_FORMAT_IN_ACE_GROUPNAME_AT_DOMAIN);
          buf.append("\"");
          if (value
              .equalsIgnoreCase(SPConstants.GROUPNAME_FORMAT_IN_ACE_GROUPNAME_AT_DOMAIN)) {
            buf.append(SPConstants.SPACE + SPConstants.SELECTED
                + SPConstants.EQUAL_TO + "\"" + SPConstants.SELECTED + "\"");
          }
          buf.append(">"
              + SPConstants.GROUPNAME_FORMAT_IN_ACE_GROUPNAME_AT_DOMAIN);
          buf.append(SPConstants.OPEN_ELEMENT + "/" + SPConstants.OPTION
              + SPConstants.CLOSE_ELEMENT);
          buf.append(SPConstants.NEW_LINE + SPConstants.OPEN_ELEMENT + "/"
              + SPConstants.SELECT + SPConstants.CLOSE_ELEMENT);
          buf.append(SPConstants.TD_END);
          buf.append(SPConstants.TR_END);
          buf.append(SPConstants.BREAK_LINE);
          buf.append(SPConstants.TR_START + SPConstants.TD_START
              + SPConstants.START_BOLD);
          buf.append("LDAP Configuration Settings");
          buf.append(SPConstants.END_BOLD);
        } else {
          buf.append(SPConstants.OPEN_ELEMENT);
          buf.append(SPConstants.INPUT);
          if (collator.equals(key, SPConstants.PASSWORD)
              || collator.equals(key, SPConstants.GSAADMINPASSWORD)) {
            appendAttribute(buf, SPConstants.TYPE, SPConstants.PASSWORD);
          } else if (collator.equals(key, SPConstants.ALIAS_MAP)) {
            appendAttribute(buf, SPConstants.TYPE, SPConstants.HIDDEN);
          } else {
            appendAttribute(buf, SPConstants.TYPE, SPConstants.TEXT);
          }
          appendAttribute(buf, SPConstants.CONFIG_NAME, key);
          appendAttribute(buf, SPConstants.CONFIG_ID, key);
          if (collator.equals(key, SPConstants.PORT_NUMBER)
              || collator.equals(key, SPConstants.INITAL_CACHE_SIZE)
              || collator.equals(key, SPConstants.CACHE_REFRESH_INTERVAL)) {
            if (collator.equals(key, SPConstants.PORT_NUMBER)) {
              if (Strings.isNullOrEmpty(value)) {
                appendAttribute(buf, SPConstants.VALUE,
                    SPConstants.LDAP_DEFAULT_PORT_NUMBER);
              } else if (value
                  .equalsIgnoreCase(SPConstants.LDAP_DEFAULT_PORT_NUMBER)) {
                appendAttribute(buf, SPConstants.VALUE,
                    SPConstants.LDAP_DEFAULT_PORT_NUMBER);
              } else {
                appendAttribute(buf, SPConstants.VALUE, value.trim());
              }
              if (editMode) {
                if (this.pushAcls.equalsIgnoreCase(SPConstants.OFF)) {
                  buf.append(SPConstants.SPACE + SPConstants.DISABLED
                      + SPConstants.EQUAL_TO + "\"" + SPConstants.TRUE + "\"");
                }
              }
            } else if (collator.equals(key, SPConstants.INITAL_CACHE_SIZE)) {
              if (Strings.isNullOrEmpty(value)) {
                if (null == this.useCacheToStoreLdapUserGroupsMembership
                    || this.useCacheToStoreLdapUserGroupsMembership
                    .equalsIgnoreCase(SPConstants.OFF)) {
                  buf.append(SPConstants.SPACE + SPConstants.DISABLED
                      + SPConstants.EQUAL_TO + "\"" + SPConstants.TRUE + "\"");
                }
                appendAttribute(buf, SPConstants.VALUE,
                    SPConstants.LDAP_INITIAL_CACHE_SIZE);
              } else if (value
                  .equalsIgnoreCase(SPConstants.LDAP_INITIAL_CACHE_SIZE)) {
                appendAttribute(buf, SPConstants.VALUE,
                    SPConstants.LDAP_INITIAL_CACHE_SIZE);
              } else {
                appendAttribute(buf, SPConstants.VALUE, value);
              }
            } else if (collator.equals(key, SPConstants.CACHE_REFRESH_INTERVAL)) {
              if (Strings.isNullOrEmpty(value)) {
                if (null == this.useCacheToStoreLdapUserGroupsMembership
                    || this.useCacheToStoreLdapUserGroupsMembership
                    .equalsIgnoreCase(SPConstants.OFF)) {
                  buf.append(SPConstants.SPACE + SPConstants.DISABLED
                      + SPConstants.EQUAL_TO + "\"" + SPConstants.TRUE + "\"");
                }
                appendAttribute(buf, SPConstants.VALUE,
                    SPConstants.LDAP_CACHE_REFRESH_INTERVAL_TIME);
              } else if (value
                  .equalsIgnoreCase(SPConstants.LDAP_CACHE_REFRESH_INTERVAL_TIME)) {
                appendAttribute(buf, SPConstants.VALUE,
                    SPConstants.LDAP_CACHE_REFRESH_INTERVAL_TIME);
              } else {
                appendAttribute(buf, SPConstants.VALUE, value.trim());
              }
            }
            buf.append(SPConstants.SPACE + SPConstants.ONKEY_PRESS);
            buf.append("\"return onlyNumbers(event);\"");
          } else {
            appendAttribute(buf, SPConstants.VALUE, value);
          }
          if (collator.equals(key, SPConstants.SHAREPOINT_URL)
              || collator.equals(key, SPConstants.MYSITE_BASE_URL)) {
            appendAttribute(buf, SPConstants.TEXTBOX_SIZE,
                SPConstants.TEXTBOX_SIZE_VALUE);
          }

          if (collator.equals(key, SPConstants.LDAP_SERVER_HOST_ADDRESS)
              || collator.equals(key, SPConstants.SEARCH_BASE)) {
            appendAttribute(buf, SPConstants.TEXTBOX_SIZE,
                SPConstants.TEXTBOX_SIZE_VALUE);
            if (editMode) {
              if (this.pushAcls.equalsIgnoreCase(SPConstants.OFF)) {
                buf.append(SPConstants.SPACE + SPConstants.DISABLED
                    + SPConstants.EQUAL_TO + "\"" + SPConstants.TRUE + "\"");
              }
            }
          }

          buf.append(SPConstants.SLASH + SPConstants.CLOSE_ELEMENT);

        }
        buf.append(SPConstants.TD_END);
        buf.append(SPConstants.TR_END);
      }
    }// end null check

    buf.append(SPConstants.START_BOLD);
    buf.append(configStrings.get(SPConstants.MANDATORY_FIELDS));
    buf.append(SPConstants.END_BOLD);

    addJavaScript(buf);

    return buf.toString();
  }

  /**
   * For creating the region Site Alias Mapping field
   * 
   * @param buf
   *          Contains the form snippet
   */
  private void appendTableForAliasMapping(final StringBuffer buf) {
    buf.append(SPConstants.OPEN_ELEMENT);
    buf.append(SPConstants.TABLE);
    appendAttribute(buf, SPConstants.CONFIG_ID, SPConstants.ALIASCONTAINER);
    appendAttribute(buf, SPConstants.CELLSPACING, "1");
    appendAttribute(buf, SPConstants.CELLPADDING, "2");
    buf.append(SPConstants.CLOSE_ELEMENT);

    buf.append(SPConstants.TR_START);

    buf.append(SPConstants.OPEN_ELEMENT);
    buf.append(SPConstants.TH);
    appendAttribute(buf, SPConstants.TITLE,
        rb.getString(SPConstants.ALIAS_SOURCE_PATTERN_HELP));
    buf.append(SPConstants.CLOSE_ELEMENT);
    buf.append(rb.getString(SPConstants.ALIAS_SOURCE_PATTERN));
    buf.append(SPConstants.TH_END);

    buf.append(SPConstants.OPEN_ELEMENT);
    buf.append(SPConstants.TH);
    appendAttribute(buf, SPConstants.TITLE,
        rb.getString(SPConstants.ALIAS_HELP));
    buf.append(SPConstants.CLOSE_ELEMENT);
    buf.append(rb.getString(SPConstants.ALIAS));
    buf.append(SPConstants.TH_END);

    buf.append(SPConstants.OPEN_ELEMENT + SPConstants.TH
        + SPConstants.CLOSE_ELEMENT);
    buf.append(SPConstants.TH_END);

    buf.append(SPConstants.TR_END);
  }

  /**
   * Appends a new row for Site Alias Mapping
   * 
   * @param buf
   *          Contains form snippet
   * @param sourceValue
   *          Source_URL_Pattern
   * @param aliasValue
   *          Alias
   * @param color
   *          To keep track of the alternate background color
   */
  private void appendRowForAliasMapping(final StringBuffer buf,
      final String sourceValue, final String aliasValue, final boolean color) {
    buf.append(SPConstants.TR_START);

    if (color) {
      buf.append(SPConstants.TD_START_FORMAT);
    } else {
      buf.append(SPConstants.TD_START);
    }
    buf.append(SPConstants.OPEN_ELEMENT);
    buf.append(SPConstants.INPUT);
    appendAttribute(buf, SPConstants.TYPE, SPConstants.TEXT);
    appendAttribute(buf, SPConstants.TEXTBOX_SIZE,
        SPConstants.ALIAS_TEXTBOX_SIZE_VALUE);
    appendAttribute(buf, SPConstants.VALUE, sourceValue);
    appendAttribute(buf, SPConstants.ONCHANGE, "readAlias()");
    buf.append(SPConstants.SLASH + SPConstants.CLOSE_ELEMENT);
    buf.append(SPConstants.TD_END);

    if (color) {
      buf.append(SPConstants.TD_START_FORMAT);
    } else {
      buf.append(SPConstants.TD_START);
    }
    buf.append(SPConstants.OPEN_ELEMENT);
    buf.append(SPConstants.INPUT);
    appendAttribute(buf, SPConstants.TYPE, SPConstants.TEXT);
    appendAttribute(buf, SPConstants.TEXTBOX_SIZE,
        SPConstants.ALIAS_TEXTBOX_SIZE_VALUE);
    appendAttribute(buf, SPConstants.VALUE, aliasValue);
    appendAttribute(buf, SPConstants.ONCHANGE, "readAlias()");
    buf.append(SPConstants.SLASH + SPConstants.CLOSE_ELEMENT);
    buf.append(SPConstants.TD_END);

    buf.append(SPConstants.TR_END);
  }

  /**
   * Starts a new row in the configuration page. used while designing the
   * configuration form
   * 
   * @param buf
   */
  private void appendStartRow(final StringBuffer buf, final String key,
      final String configKey, final boolean red/*
       * , final String displayValue
       */) {
    buf.append(SPConstants.TR_START);
    buf.append(SPConstants.TD_START);

    if (red) {
      buf.append("<font color=\"red\">");
    }
    if (isRequired(key)) {
      buf.append("<div style=\"float: left;\">");
      buf.append(SPConstants.START_BOLD);
    }

    buf.append(configKey);

    if (isRequired(key)) {
      buf.append(SPConstants.END_BOLD);
      buf.append("</div><div style=\"text-align: right; ")
        .append("color: red; font-weight: bold; ")
        .append("margin-right: 0.3em;\">*</div>");
    }
    if (red) {
      buf.append("</font>");
    }
    buf.append(SPConstants.TD_END);
    buf.append(SPConstants.TD_START);
  }

  /**
   * Adds a property to the current HTML element. used while designing the
   * configuration form
   * 
   * @param buf
   * @param attrName
   * @param attrValue
   */
  private void appendAttribute(final StringBuffer buf, final String attrName,
      final String attrValue) {
    buf.append(" ");
    buf.append(attrName);
    buf.append("=\"");
    try {
      // XML-encode the special characters (< > " etc.)
      // Check the basic requirement mentioned in ConnectorType as part of
      // CM-Issue 186
      XmlUtils.xmlAppendAttrValue(attrValue, buf);
    } catch (IOException e) {
      String msg = new StringBuffer(
          "Exceptions while constructing the config form for attribute : ")
          .append(attrName).append(" with value : ").append(attrValue)
          .toString();
      LOGGER.log(Level.WARNING, msg, e);
    }
    buf.append("\"");
  }

  /**
   * initialize the instance variables for furhter processing.
   * 
   * @param key
   * @param val
   */
  private void setSharepointCredentials(final String key, final String val) {
    if (val == null) {
      return;
    }
    if (collator.equals(key, SPConstants.USERNAME)) {
      username = val.trim();
    } else if (collator.equals(key, SPConstants.DOMAIN)) {
      domain = val.trim();
    } else if (collator.equals(key, SPConstants.PASSWORD)) {
      password = val;
    } else if (collator.equals(key, SPConstants.SHAREPOINT_URL)) {
      sharepointUrl = val.trim();
    } else if (collator.equals(key, SPConstants.INCLUDED_URLS)) {
      includeURL = val;
    } else if (collator.equals(key, SPConstants.EXCLUDED_URLS)) {
      excludeURL = val;
    } else if (collator.equals(key, SPConstants.MYSITE_BASE_URL)) {
      mySiteUrl = val.trim();
    } else if (collator.equals(key, SPConstants.USE_SP_SEARCH_VISIBILITY)) {
      useSPSearchVisibility = val.trim();
    } else if (collator.equals(key, SPConstants.PUSH_ACLS)) {
      this.pushAcls = val.trim();
    } else if (collator.equals(key, SPConstants.USERNAME_FORMAT_IN_ACE)) {
      this.usernameFormatInAce = val.trim();
    } else if (collator.equals(key, SPConstants.GROUPNAME_FORMAT_IN_ACE)) {
      this.groupnameFormatInAce = val.trim();
    } else if (collator.equals(key, SPConstants.LDAP_SERVER_HOST_ADDRESS)) {
      this.ldapServerHostAddress = val.trim();
    } else if (collator.equals(key, SPConstants.PORT_NUMBER)) {
      this.portNumber = val.trim();
    } else if (collator.equals(key, SPConstants.SEARCH_BASE)) {
      this.searchBase = val.trim();
    } else if (collator.equals(key, SPConstants.AUTHENTICATION_TYPE)) {
      this.authenticationType = val.trim();
    } else if (collator.equals(key, SPConstants.CONNECT_METHOD)) {
      this.connectMethod = val.trim();
    } else if (collator.equals(key,
        SPConstants.USE_CACHE_TO_STORE_LDAP_USER_GROUPS_MEMBERSHIP)) {
      this.useCacheToStoreLdapUserGroupsMembership = val.trim();
    } else if (collator.equals(key, SPConstants.INITAL_CACHE_SIZE)) {
      this.initialCacheSize = val.trim();
    } else if (collator.equals(key, SPConstants.CACHE_REFRESH_INTERVAL)) {
      this.cacheRefreshInterval = val.trim();
    }
  }

  /**
   * Validates the values filled-in by the user at the connector's configuration
   * page.
   */
  private boolean validateConfigMap(final Map<String, String> configData,
      final ErrorDignostics ed) {
    if (configData == null) {
      LOGGER.warning("configData map is not found");
      return false;
    }
    LOGGER.info("push acls validate :" + configData.get("pushAcls"));
    if (!configData
        .containsKey(SPConstants.USE_CACHE_TO_STORE_LDAP_USER_GROUPS_MEMBERSHIP)) {
      this.useCacheToStoreLdapUserGroupsMembership = SPConstants.OFF;
    }
    if (null == configData.get(SPConstants.PUSH_ACLS)) {
      this.pushAcls = SPConstants.OFF;
    }

    FeedType feedType = null;
    String kdcServer = configData.get(SPConstants.KDC_SERVER).toString();

    if (!kdcServer.equalsIgnoreCase(SPConstants.BLANK_STRING)) {
      kerberosSetUp(configData);
    } else {
      unregisterKerberosSetUp(configData);
    }

    for (final Iterator<String> i = keys.iterator(); i.hasNext();) {
      final String key = i.next();
      final String val = configData.get(key);

      if (isRequired(key)) {
        if ((val == null) || val.equals(SPConstants.BLANK_STRING)
            || (val.length() == 0)) {
          ed.set(key, rb.getString(SPConstants.REQ_FIELDS_MISSING)
              + SPConstants.SPACE + rb.getString(key));
          return false;
        } else if (collator.equals(key, SPConstants.SHAREPOINT_URL)) {
          if (!isURL(val)) {
            ed.set(key, rb.getString(SPConstants.MALFORMED_URL));
            return false;
          }
          if (!isInFQDN(val)) {
            ed.set(key, rb.getString(SPConstants.REQ_FQDN_URL));
            return false;
          }
        } else if (collator.equals(key, SPConstants.INCLUDED_URLS)) {
          final Set<String> invalidSet = validatePatterns(val);
          if (invalidSet != null) {
            ed.set(
                SPConstants.INCLUDED_URLS,
                rb.getString(SPConstants.INVALID_INCLUDE_PATTERN)
                + invalidSet.toString());
            return false;
          }
        }
      } else if (collator.equals(key, SPConstants.ALIAS_MAP) && (val != null)
          && !val.equals(SPConstants.BLANK_STRING)) {
        final Set<String> wrongEntries = new HashSet<String>();
        final String message = parseAlias(val, wrongEntries);
        if (message != null) {
          ed.set(SPConstants.ALIAS_MAP, rb.getString(message) + " "
              + wrongEntries);
          return false;
        }
      } else if (collator.equals(key, SPConstants.EXCLUDED_URLS)) {
        final Set<String> invalidSet = validatePatterns(val);
        if (invalidSet != null) {
          ed.set(
              SPConstants.EXCLUDED_URLS,
              rb.getString(SPConstants.INVALID_EXCLUDE_PATTERN)
              + invalidSet.toString());
          LOGGER.warning("Invalid Exclude pattern:" + val);
          return false;
        }
      } else if (collator.equals(key, SPConstants.AUTHORIZATION)) {
        feedType = FeedType.getFeedType(val);
      } else if (!kdcServer.equalsIgnoreCase(SPConstants.BLANK_STRING)
          && collator.equals(key, SPConstants.KDC_SERVER)) {
        boolean isFQDN = false;
        if (!Util.isFQDN(kdcServer)) {
          ed.set(SPConstants.KDC_SERVER,
              rb.getString(SPConstants.KERBEROS_KDC_HOST_BLANK));
          return false;
        } else {
          try {
            Integer.parseInt(kdcServer.substring(0, kdcServer.indexOf(".")));
          } catch (NumberFormatException nfe) {
            isFQDN = true;
          }
          if (!isFQDN && !validateIPAddress(kdcServer)) {
            ed.set(SPConstants.KDC_SERVER,
                rb.getString(SPConstants.KERBEROS_KDC_HOST_BLANK));
            return false;
          }
        }
      } else if (collator.equals(key, SPConstants.SOCIAL_OPTION)) {
        if ( (val != null) ){
          String option = val.trim();  
          if ((!option.equalsIgnoreCase(SPConstants.BLANK_STRING)) 
              && (!option.equalsIgnoreCase(SPConstants.SOCIAL_OPTION_YES))
              && (!option.equalsIgnoreCase(SPConstants.SOCIAL_OPTION_NO))
              && (!option.equalsIgnoreCase(SPConstants.SOCIAL_OPTION_ONLY))) {
            LOGGER.warning("Invalid social option " + val);
            ed.set(SPConstants.SOCIAL_OPTION, rb.getString(SPConstants.SOCIAL_OPTION_INVALID));
            return false;
          }
        }
      }
      setSharepointCredentials(key, val);
    }

    // The Use SP indexing options is a checkbox which is turned on by
    // default. In case the user unchecks it, no HTML form element is
    // returned for the same and hence the connector should add to the
    // config map with a value of "false" indicating that the SP indexing
    // options are not to be considered by the connector. If it is
    // available, overwrite its value "on" with "true".
    if (!configData.containsKey(SPConstants.USE_SP_SEARCH_VISIBILITY)) {
      configData.put(SPConstants.USE_SP_SEARCH_VISIBILITY,
          Boolean.toString(false));
    } else {
      configData.put(SPConstants.USE_SP_SEARCH_VISIBILITY,
          Boolean.toString(true));
    }

    if ((username != null)
        && ((username.indexOf("@") != -1) || (username.indexOf("\\") != -1))
        && (domain != null) && !domain.equals(SPConstants.BLANK_STRING)) {
      ed.set(SPConstants.USERNAME, rb.getString(SPConstants.DUPLICATE_DOMAIN));
      return false;
    }

    try {
      sharepointClientContext = new SharepointClientContext(clientFactory, 
        sharepointUrl, domain, kdcServer, username, password, "", 
        includeURL, excludeURL, mySiteUrl, "", feedType,
        new Boolean(useSPSearchVisibility).booleanValue());
    } catch (final Exception e) {
      LOGGER
      .log(
          Level.SEVERE,
          "Failed to create SharePointClientContext with the received configuration values. ");
    }
    String status = checkPattern(sharepointUrl);
    if (status != null) {
      ed.set(null, rb.getString(SPConstants.SHAREPOINT_URL) + " " + status);
      return false;
    }
    status = null;

    if (FeedType.CONTENT_FEED == feedType) {
      status = checkGSConnectivity(sharepointUrl);
      if (!SPConstants.CONNECTIVITY_SUCCESS.equalsIgnoreCase(status)) {
        ed.set(
            null,
            rb.getString(SPConstants.BULKAUTH_ERROR_CRAWL_URL)
            + rb.getString(SPConstants.REASON) + status);
        return false;
      }
    }
    status = null;

    status = checkConnectivity(sharepointUrl);
    if (!SPConstants.CONNECTIVITY_SUCCESS.equalsIgnoreCase(status)) {
      ed.set(
          null,
          rb.getString(SPConstants.CANNOT_CONNECT)
          + rb.getString(SPConstants.REASON) + status);
      return false;
    }
    status = null;

    // Validating the feed ACLs related check boxes which are off by
    // default.
    validateFeedAclsAndLdapUserGroupsCacheCheckBoxes(configData);

    if (!validateFeedAclsRelatedHtmlControls(ed)) {
      return false;
    }

    final SPType SPVersion = sharepointClientContext
        .checkSharePointType(sharepointUrl);
    if (SPType.SP2007 == SPVersion && mySiteUrl != null
        && !mySiteUrl.equals(SPConstants.BLANK_STRING)) {
      if (!isURL(mySiteUrl)) {
        ed.set(SPConstants.MYSITE_BASE_URL,
            rb.getString(SPConstants.MALFORMED_MYSITE_URL));
        return false;
      }
      if (!isInFQDN(mySiteUrl)) {
        ed.set(SPConstants.MYSITE_BASE_URL,
            rb.getString(SPConstants.REQ_FQDN_MYSITE_URL));
        return false;
      }

      status = checkPattern(mySiteUrl);
      if (status != null) {
        ed.set(null, rb.getString(SPConstants.MYSITE_BASE_URL) + " " + status);
        return false;
      }
      status = null;

      status = checkConnectivity(mySiteUrl);
      if (!SPConstants.CONNECTIVITY_SUCCESS.equalsIgnoreCase(status)) {
        ed.set(
            SPConstants.MYSITE_BASE_URL,
            rb.getString(SPConstants.CANNOT_CONNECT_MYSITE)
            + rb.getString(SPConstants.REASON) + status);
        return false;
      }

      if (FeedType.CONTENT_FEED == feedType) {
        status = checkGSConnectivity(mySiteUrl);
        if (!SPConstants.CONNECTIVITY_SUCCESS.equalsIgnoreCase(status)) {
          ed.set(
              SPConstants.MYSITE_BASE_URL,
              rb.getString(SPConstants.BULKAUTH_ERROR_MYSITE_URL)
              + rb.getString(SPConstants.REASON) + status);
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Used while re-displaying the connector configuration page after any
   * validation error occurs.
   */
  private ConfigureResponse makeValidatedForm(
      final Map<String, String> configMap, final ErrorDignostics ed) {
    final String sFunName = className
        + ".makeValidatedForm(final Map configMap, ErrorDignostics ed)";
    if (configMap == null) {
      LOGGER.warning(sFunName + ": configMap is not found");
      if (rb != null) {
        return new ConfigureResponse(rb.getString("CONFIGMAP_NOT_FOUND"), "");
      } else {
        return new ConfigureResponse("resource bundle not found", "");
      }
    }
    final StringBuffer buf = new StringBuffer(2048);
    buf.append(makeConfigForm(configMap, ed));

    return new ConfigureResponse(ed.error_message, buf.toString());
  }

  /**
   * Called by connector-manager to display the connector configuration page.
   */
  public ConfigureResponse getConfigForm(final Locale locale) {
    LOGGER.config("Locale " + locale);
    this.editMode = false;

    collator = Util.getCollator(locale);
    rb = ResourceBundle.getBundle("SharepointConnectorResources", locale);
    setConfigStrings();
    final ConfigureResponse result = new ConfigureResponse("",
        getInitialConfigForm());

    return result;
  }

  /**
   * Called by connector-manager to display the connector configuration page
   * with filled in values.
   */
  public ConfigureResponse getPopulatedConfigForm(
      final Map<String, String> configMap, final Locale locale) {
    LOGGER.config("Locale " + locale);
    String isSelected = configMap.get(SPConstants.PUSH_ACLS);
    if (null != isSelected && !isSelected.equalsIgnoreCase(SPConstants.TRUE)) {
      this.editMode = SPConstants.EDIT_MODE;
    } else {
      this.editMode = false;
    }

    collator = Util.getCollator(locale);
    rb = ResourceBundle.getBundle("SharepointConnectorResources", locale);
    setConfigStrings();
    final ConfigureResponse result = new ConfigureResponse("", makeConfigForm(
        configMap, null));

    return result;
  }

  /**
   * Called by connector-manager to validate the connector configuration values.
   */
  // due to GCM changes
  public ConfigureResponse validateConfig(final Map<String, String> configData,
      final Locale locale, final ConnectorFactory arg2) {
    return this.validateConfig(configData, locale);
  }

  public ConfigureResponse validateConfig(final Map<String, String> configData,
      final Locale locale) {
    LOGGER.config("Locale " + locale);

    rb = ResourceBundle.getBundle("SharepointConnectorResources", locale);
    setConfigStrings();

    final ErrorDignostics ed = new ErrorDignostics();
    if (validateConfigMap(configData, ed)) {
      // all is ok
      return null;
    }
    final ConfigureResponse configureResponse = makeValidatedForm(configData,
        ed);

    LOGGER.config("message:\n" + configureResponse.getMessage());
    return configureResponse;
  }

  /**
   * Stores any error message corresponding to any field on the connector's
   * configuration page. *
   * 
   * @author nitendra_thakur
   */
  class ErrorDignostics {
    String error_key;
    String error_message;

    void set(final String key, final String msg) {
      error_key = key;
      error_message = msg;
    }
  }

  /**
   * Desc : returns true if the Config Key is a mandatory field.
   * 
   * @param configKey
   * @return is this field is mandatory?
   */
  private boolean isRequired(final String configKey) {
    if (collator.equals(configKey, SPConstants.SHAREPOINT_URL)
        || collator.equals(configKey, SPConstants.USERNAME)
        || collator.equals(configKey, SPConstants.PASSWORD)
        || collator.equals(configKey, SPConstants.INCLUDED_URLS)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * JavaScript required on the connector configuration page.
   * 
   * @param buf
   *          contains the Form snippet
   */
  public void addJavaScript(final StringBuffer buf) {
    if (buf != null) {
      String js = "\r\n <script language=\"JavaScript\"> \r\n <![CDATA[ ";
      js += "\r\n function addRow() {"
          + "\r\n var i=1;    var aliasTable = document.getElementById('aliasContainer');    var len=aliasTable.rows.length;"
          + "\r\n for(i=1;i<len-1;i++){"
          + "\r\n row = aliasTable.rows[i]; leftBox = row.cells[0].childNodes[0]; rightBox = row.cells[1].childNodes[0];"
          + "\r\n if(leftBox.value=='' || rightBox.value=='') {"
          + "\r\n alert(\""
          + rb.getString(SPConstants.EMPTY_FIELD)
          + "\"); return;"
          + "\r\n }"
          + "\r\n }"
          + "\r\n var newrow = aliasTable.insertRow(len-1);"
          +

          "\r\n newCell0 = newrow.insertCell(0); newCell0.innerHTML= '<input type=\"text\" size=\""
          + SPConstants.ALIAS_TEXTBOX_SIZE_VALUE
          + "\" onchange=\"readAlias()\"/> '; "
          + "\r\n newCell1 = newrow.insertCell(1); newCell1.innerHTML= '<input type=\"text\" size=\""
          + SPConstants.ALIAS_TEXTBOX_SIZE_VALUE
          + "\" onchange=\"readAlias()\"/> '; "
          +

          "\r\n if(row.cells[0].style.backgroundColor=='') { newCell0.style.backgroundColor='#DDDDDD'; newCell1.style.backgroundColor='#DDDDDD'; }"
          +

          "\r\n }";

      js += "\r\n function readAlias(){"
          + "\r\n var aliasString=''; var i=1; var aliasTable = document.getElementById('aliasContainer'); var SOURCE_ALIAS_SEPARATOR=/\\$\\$EQUAL\\$\\$/; var ALIAS_ENTRIES_SEPARATOR=/\\$\\$CRLF\\$\\$/;"
          + "\r\n for(i=1;i<aliasTable.rows.length-1;i++){"
          + "\r\n row = aliasTable.rows[i]; leftBoxVal = trim(row.cells[0].childNodes[0].value); rightBoxVal = trim(row.cells[1].childNodes[0].value);"
          + "\r\n if(leftBoxVal=='' || leftBoxVal==null || leftBoxVal==undefined || rightBoxVal=='' || rightBoxVal==null || rightBoxVal==undefined) {continue;}"
          + "\r\n if(leftBoxVal.search(" + SPConstants.SOURCE_ALIAS_SEPARATOR
          + ")!=-1 || rightBoxVal.search(" + SPConstants.SOURCE_ALIAS_SEPARATOR
          + ")!=-1 || leftBoxVal.search(" + SPConstants.ALIAS_ENTRIES_SEPARATOR
          + ")!=-1 || rightBoxVal.search("
          + SPConstants.ALIAS_ENTRIES_SEPARATOR + ")!=-1) {continue;}"
          + "\r\n aliasString += leftBoxVal + '"
          + SPConstants.SOURCE_ALIAS_SEPARATOR + "' + rightBoxVal + '"
          + SPConstants.ALIAS_ENTRIES_SEPARATOR + "';" + "\r\n }"
          + "\r\n document.getElementById('" + SPConstants.ALIAS_MAP
          + "').value=aliasString;" + "\r\n }";

      js += "\r\n function enableOrDisableUserGroupsCacheControles () {"
          + ""
          + "\r\n if (document.getElementById(\"useCacheToStoreLdapUserGroupsMembership\").checked == true){ "
          + "\r\n document.getElementById(\"initialCacheSize\").disabled=false"
          + "\r\n document.getElementById(\"cacheRefreshInterval\").disabled=false"

          + "\r\n } else {"
          + "\r\n document.getElementById(\"cacheRefreshInterval\").disabled=true"
          + "\r\n document.getElementById(\"initialCacheSize\").disabled=true"

          + "\r\n }" + "\r\n }";

      js += "\r\n function enableFeedAclsRelatedHtmlControles () {"
          + ""
          + "\r\n if (document.getElementById(\"pushAcls\").checked == true){ "
          + "\r\n document.getElementById(\"portNumber\").disabled=false"
          + "\r\n document.getElementById(\"ldapServerHostAddress\").disabled=false"
          + "\r\n document.getElementById(\"groupnameFormatInAce\").disabled=false"
          + "\r\n document.getElementById(\"usernameFormatInAce\").disabled=false"
          + "\r\n document.getElementById(\"searchBase\").disabled=false"
          + "\r\n document.getElementById(\"authenticationType\").disabled=false"
          + "\r\n document.getElementById(\"connectMethod\").disabled=false"
          + "\r\n document.getElementById(\"useCacheToStoreLdapUserGroupsMembership\").disabled=false"
          + "\r\n document.getElementById(\"appendNamespaceInSPGroup\").checked=true"
          + "\r\n if (document.getElementById(\"useCacheToStoreLdapUserGroupsMembership\").checked == true){ "
          + "\r\n document.getElementById(\"initialCacheSize\").disabled=false"
          + "\r\n document.getElementById(\"cacheRefreshInterval\").disabled=false"
          + "\r\n }"

          + "\r\n } else {"
          + "\r\n document.getElementById(\"portNumber\").disabled=true"
          + "\r\n document.getElementById(\"ldapServerHostAddress\").disabled=true"
          + "\r\n document.getElementById(\"groupnameFormatInAce\").disabled=true"
          + "\r\n document.getElementById(\"usernameFormatInAce\").disabled=true"
          + "\r\n document.getElementById(\"searchBase\").disabled=true"
          + "\r\n document.getElementById(\"authenticationType\").disabled=true"
          + "\r\n document.getElementById(\"connectMethod\").disabled=true"
          + "\r\n document.getElementById(\"useCacheToStoreLdapUserGroupsMembership\").disabled=true"
          + "\r\n document.getElementById(\"cacheRefreshInterval\").disabled=true"
          + "\r\n document.getElementById(\"initialCacheSize\").disabled=true"
          + "\r\n }" + "\r\n }";

      js += "\r\n function onlyNumbers(evt){"
          + "\r\n  var charCode = (evt.which) ? evt.which : event.keyCode"
          + "\r\n if (charCode > 31 && (charCode < 48 || charCode > 57))"
          + "\r\n return false;" + "\r\n return true;" + "\r\n }";

      js += "\r\n function trim(s) {return s.replace( /^\\s*/, \"\" ).replace( /\\s*$/, \"\" );}";

      js += "document.getElementById('" + SPConstants.SHAREPOINT_URL
          + "').focus()";

      js += "\r\n ]]> \r\n </script> \r\n";
      buf.append(js);
    }
  }

  /**
   * Checks if the value is in FQDN *
   */
  private boolean isInFQDN(final String val) {
    LOGGER.config("Checking " + val + " for FQDN");
    try {
      final URL url = new URL(val);
      final String host = url.getHost();
      if (host.indexOf(".") == -1) {
        return false;
      }
    } catch (final Exception e) {
      return false;
    }
    return true;
  }

  /**
   * Checks if the url is a malformed url *
   */
  private boolean isURL(final String url) {
    LOGGER.config("Checking " + url + " for malformed URL");
    try {
      final URL chkURL = new URL(url);
      /*
       * Check for port value greater then allowed range is added because of a
       * bug in Axis where a NullPointer Exception was being returned as a value
       * during Web Service call.
       */
      if (null == chkURL || chkURL.getPort() > SPConstants.MAX_PORT_VALUE) {
        return false;
      }
    } catch (final MalformedURLException e) {
      final String logMessage = "Malformed URL.";
      LOGGER.log(Level.WARNING, logMessage, e);
      return false;
    }
    return true;
  }

  /**
   * Matches the provided url against included and excluded patterns Function
   * Signature changed by nitendra_thakur. This is to make the function
   * re-usable.
   */
  private String checkPattern(final String url) {
    LOGGER.info("Matching patterns for [" + url + "]");
    String[] includeList = null;
    String[] excludeList = null;
    if (includeURL != null) {
      includeList = includeURL.split(SPConstants.SEPARATOR);
    }
    if (excludeURL != null) {
      excludeList = excludeURL.split(SPConstants.SEPARATOR);
    }

    if (includeList == null) {
      return rb.getString(SPConstants.INCLUDED_PATTERN_MISMATCH);
    }
    if (Util.match(includeList, url, null)) {
      final StringBuffer matchedPattern = new StringBuffer();
      if ((excludeList != null) && Util.match(excludeList, url, matchedPattern)) {
        return rb.getString(SPConstants.EXCLUDED_PATTERN_MATCH)
            + matchedPattern.toString();
      }
      return null;
    } else {
      return rb.getString(SPConstants.INCLUDED_PATTERN_MISMATCH);
    }
  }

  /**
   * Validates all the patterns under included / excluded to check if any of
   * them is not a valid pattern.
   * 
   * @param patterns
   *          The pattern to be validated
   * @return the set of wrong patterns, if any. Otherwise returns null
   */
  private Set<String> validatePatterns(final String patterns) {
    LOGGER.info("validating patterns [ " + patterns + " ]. ");
    String[] patternsList = null;
    if ((patterns != null) && (patterns.trim().length() != 0)) {
      patternsList = patterns.split(SPConstants.SEPARATOR);
    }
    if (patternsList == null) {
      return null;
    }

    final Set<String> invalidPatterns = new HashSet<String>();

    for (final String pattern : patternsList) {
      if (pattern.startsWith(SPConstants.HASH)
          || pattern.startsWith(SPConstants.MINUS)) {
        continue;
      }
      if (pattern.startsWith(SPConstants.CONTAINS)) {
        final StringBuffer tempBuffer = new StringBuffer(pattern);
        if (tempBuffer == null) {
          invalidPatterns.add(pattern);
        }
        final String strContainKey = new String(tempBuffer.delete(0,
            SPConstants.CONTAINS.length()));
        try {
          new RE(strContainKey); // with case
        } catch (final Exception e) {
          invalidPatterns.add(pattern);
        }
        continue;
      }

      if (pattern.startsWith(SPConstants.REGEXP)) {
        final StringBuffer tempBuffer = new StringBuffer(pattern);
        if (tempBuffer == null) {
          invalidPatterns.add(pattern);
        }
        final String strRegexPattrn = new String(tempBuffer.delete(0,
            SPConstants.REGEXP.length()));
        try {
          new RE(strRegexPattrn);
        } catch (final Exception e) {
          invalidPatterns.add(pattern);
        }
        continue;
      }

      if (pattern.startsWith(SPConstants.REGEXP_CASE)) {
        final StringBuffer tempBuffer = new StringBuffer(pattern);
        if (tempBuffer == null) {
          invalidPatterns.add(pattern);
        }
        final String strRegexCasePattrn = new String(tempBuffer.delete(0,
            SPConstants.REGEXP_CASE.length()));
        try {
          new RE(strRegexCasePattrn);
        } catch (final Exception e) {
          invalidPatterns.add(pattern);
        }
        continue;
      }

      if (pattern.startsWith(SPConstants.REGEXP_IGNORE_CASE)) {
        final StringBuffer tempBuffer = new StringBuffer(pattern);
        if (tempBuffer == null) {
          invalidPatterns.add(pattern);
        }
        final String strRegexIgnoreCasePattrn = new String(tempBuffer.delete(0,
            SPConstants.REGEXP_IGNORE_CASE.length()));
        try {
          new RE(strRegexIgnoreCasePattrn, RE.REG_ICASE); // ignore
          // case
        } catch (final Exception e) {
          invalidPatterns.add(pattern);
        }
        continue;
      }

      if (pattern.startsWith(SPConstants.CARET)
          || pattern.endsWith(SPConstants.DOLLAR)) {
        StringBuffer tempBuffer = new StringBuffer(pattern);
        boolean bDollar = false;
        if (pattern.startsWith(SPConstants.CARET)) {
          tempBuffer = new StringBuffer(pattern);
          final int indexOfStar = tempBuffer.indexOf("*");
          if (indexOfStar != -1) {
            tempBuffer.replace(indexOfStar, indexOfStar + "*".length(),
                "[0-9].*");
          } else {
            tempBuffer.delete(0, "^".length());
            if (pattern.endsWith(SPConstants.DOLLAR)) {
              bDollar = true;
              tempBuffer.delete(
                  tempBuffer.length() - SPConstants.DOLLAR.length(),
                  tempBuffer.length());
            }
            try {
              final URL urlPatt = new URL(tempBuffer.toString());
              final int port = urlPatt.getPort();
              final String strHost = urlPatt.getHost().toString();
              if ((port == -1) && (strHost != null) && (strHost.length() != 0)) {
                tempBuffer = new StringBuffer("^" + urlPatt.getProtocol()
                    + SPConstants.URL_SEP + urlPatt.getHost() + ":[0-9].*"
                    + urlPatt.getPath());
              }
              if (bDollar) {
                tempBuffer.append(SPConstants.DOLLAR);
              }
            } catch (final Exception e) {
              tempBuffer = new StringBuffer(pattern);
            }
          }
        }

        try {
          new RE(tempBuffer);
        } catch (final Exception e) {
          invalidPatterns.add(pattern);
        }
        continue;
      }

      String patternDecoded = pattern;
      try {
        patternDecoded = URLDecoder.decode(pattern, "UTF-8");
      } catch (final Exception e) {
        // eatup exception. use the original value
        patternDecoded = pattern;
      }

      if (patternDecoded == null) {
        invalidPatterns.add(pattern);
      }

      boolean containProtocol = false;
      try {
        final RE re = new RE(SPConstants.URL_SEP);
        final REMatch reMatch = re.getMatch(patternDecoded);
        if (reMatch != null) {
          containProtocol = true; // protocol is present
        }

      } catch (final Exception e) {
        containProtocol = false;
      }

      if (containProtocol) {
        String urlPatt1stPart = null;
        String urlPatt2ndPart = null;
        boolean bPortStar = false;
        try {
          final URL urlPatt = new URL(patternDecoded);
          final int port = urlPatt.getPort();
          String strPort = "";
          if (port == -1) {
            strPort = "[0-9].*";
          } else {
            strPort = port + "";
          }
          urlPatt1stPart = "^" + urlPatt.getProtocol() + SPConstants.URL_SEP
              + urlPatt.getHost() + SPConstants.COLON + strPort;
          if (!(urlPatt.getFile()).startsWith(SPConstants.SLASH)) { // The pattern must have "/" 
                                                                    // after the port
            invalidPatterns.add(pattern);
          }
          urlPatt2ndPart = "^" + urlPatt.getFile();
        } catch (final Exception e) {
          bPortStar = true;
        }

        if (bPortStar) {
          final int indexOfStar = patternDecoded.indexOf("*");
          if (indexOfStar != -1) {
            urlPatt1stPart = "^" + patternDecoded.substring(0, indexOfStar)
                + "[0-9].*";
            if (!(patternDecoded.substring(indexOfStar + 1))
                .startsWith(SPConstants.SLASH)) {
              invalidPatterns.add(pattern);
            }
            urlPatt2ndPart = "^" + patternDecoded.substring(indexOfStar + 1);
          }
        }

        try {
          new RE(urlPatt1stPart);
          new RE(urlPatt2ndPart);
        } catch (final Exception e) {
          invalidPatterns.add(pattern);
        }
      } else {
        String urlPatt1stPart = null;
        String urlPatt2ndPart = null;
        if (patternDecoded.indexOf(SPConstants.SLASH) != -1) {
          if (patternDecoded.indexOf(SPConstants.COLON) == -1) {
            urlPatt1stPart = patternDecoded.substring(0,
                patternDecoded.indexOf(SPConstants.SLASH))
                + ":[0-9].*";
          } else {
            urlPatt1stPart = patternDecoded.substring(0,
                patternDecoded.indexOf(SPConstants.SLASH));
          }
          urlPatt2ndPart = patternDecoded.substring(patternDecoded
              .indexOf(SPConstants.SLASH));
        } else {
          invalidPatterns.add(pattern);
        }

        urlPatt1stPart = "^.*://.*" + urlPatt1stPart;
        urlPatt2ndPart = "^" + urlPatt2ndPart;
        try {
          new RE(urlPatt1stPart);
          new RE(urlPatt2ndPart);
        } catch (final Exception e) {
          invalidPatterns.add(pattern);
        }
      }
    }
    if (invalidPatterns.size() == 0) {
      return null;
    } else {
      return invalidPatterns;
    }
  }

  /**
   * Checks if the endpoint provided is a sharepoint url and is connectable.
   * Function Signature changed by nitendra_thakur. This is to make the function
   * re-usable.
   */
  private String checkConnectivity(final String endpoint) {
    LOGGER.config("Checking connectivity for [" + endpoint + "]");

    if ((endpoint == null) || !isURL(endpoint)) {
      return rb.getString(SPConstants.ENDPOINT_NOT_FOUND);
    }

    try {
      sharepointClientContext.setSiteURL(endpoint);
			final WebsWS websWS = clientFactory.getWebsWS(sharepointClientContext);
      return websWS.checkConnectivity();
    } catch (final Exception e) {
      final String logMessage = "Problem while connecting.";
      LOGGER.log(Level.WARNING, logMessage, e);
      return e.getLocalizedMessage();
    }
  }

  /**
   * Check for the connectivtity to the google services for SharePoint
   * 
   * @param endpoint
   *          the Web URL to which the Web Service call will be made
   * @return the connectivity status
   */
  private String checkGSConnectivity(final String endpoint) {
    LOGGER.config("Checking Google Services connectivity for [" + endpoint
        + "]");

    if ((endpoint == null) || !isURL(endpoint)) {
      return rb.getString(SPConstants.ENDPOINT_NOT_FOUND);
    }

    try {
      sharepointClientContext.setSiteURL(endpoint);
			final BulkAuthorizationWS testBulkAuth =
          clientFactory.getBulkAuthorizationWS(sharepointClientContext);
      return testBulkAuth.checkConnectivity();
    } catch (final Exception e) {
      final String logMessage = "Problem while connecting.";
      LOGGER.log(Level.WARNING, logMessage, e);
      return e.getLocalizedMessage();
    }
  }

  /**
   * Parses the received value of Alias source pattern and values in a string
   * and updates the map.
   * 
   * @param aliasMapString
   *          The comples string containg all the entries made on the
   *          configuration form. Entries of two consecutive rows are separated
   *          by /$$CRLF$$/ A Source pattern is separated by its corresponding
   *          Alias pattern by /$$EQUAL$$/
   */
  private String parseAlias(final String aliasMapString,
      Set<String> wrongEntries) {
    LOGGER.config("parsing aliasString: " + aliasMapString);
    aliasMap = null;
    if ((aliasMapString == null)
        || aliasMapString.equals(SPConstants.BLANK_STRING)) {
      return null;
    }
    String message = null;
    if (null == wrongEntries) {
      wrongEntries = new HashSet<String>();
    }

    try {
      final String[] alias_map = aliasMapString
          .split(SPConstants.ALIAS_ENTRIES_SEPARATOR);
      for (final String nextEntry : alias_map) {
        if ((nextEntry == null) || nextEntry.equals(SPConstants.BLANK_STRING)) {
          continue;
        } else {
          try {
            final String[] alias_entry = nextEntry
                .split(SPConstants.SOURCE_ALIAS_SEPARATOR);
            if (alias_entry.length != 2) {
              LOGGER.warning("Skipping alias entry [ " + nextEntry
                  + " ] because required values are not found.");
              continue;
            } else {
              final String source_url = alias_entry[0];
              final String alias_url = alias_entry[1];

              String sourceURL = source_url;
              if (source_url.startsWith(SPConstants.GLOBAL_ALIAS_IDENTIFIER)) {
                sourceURL = source_url.substring(1);
              }

              if ((null == message)
                  || message.equals(SPConstants.MALFORMED_ALIAS_PATTERN)) {
                if (!isURL(sourceURL)) {
                  message = SPConstants.MALFORMED_ALIAS_PATTERN;
                  wrongEntries.add(sourceURL);
                }
                if (!isURL(alias_url)) {
                  message = SPConstants.MALFORMED_ALIAS_PATTERN;
                  wrongEntries.add(alias_url);
                }
              }

              ArrayList<String> aliases = null;
              if (aliasMap == null) {
                aliasMap = new LinkedHashMap<String, ArrayList<String>>();
              }
              if (aliasMap.containsKey(source_url)) {
                aliases = aliasMap.get(source_url);
                if ((null == message)
                    || message.equals(SPConstants.DUPLICATE_ALIAS)) {
                  message = SPConstants.DUPLICATE_ALIAS;
                  wrongEntries.add(source_url);
                }
              }
              if (aliases == null) {
                aliases = new ArrayList<String>();
              }
              aliases.add(alias_url);
              aliasMap.put(source_url, aliases);
            }
          } catch (final Exception e) {
            final String logMessage = "Exception thrown while parsing AliasMap [ "
                + aliasMapString + " ] Values:";
            LOGGER.log(Level.WARNING, logMessage, e);
            continue;
          }
        }
      }
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING, "Problem while parsing alias string [ "
          + aliasMapString + " ].", e);
    }
    return message;
  }

  /**
   * Validates the String to check whether it represents an IP address or not
   * and returns the boolean status.
   * 
   * @param ip
   *          IP adress to be validated in the form of string.
   * @return If ip address matches the regular expression then true else false
   *         is returned.
   */
  private boolean validateIPAddress(String ip) {
    // if(ip.matches("[0-255]+.[0-255]+.[0-255]+.[0-255]+"))
    if (ip
        .matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"))
      return true;
    else
      return false;
  }

  /**
   * All the initial set-up and pre-requisites for Kerberos Authentication.
   * Following are the responsibilities: - If KDC Host is provided on UI
   * configuration then the Negotiate AuthScheme is registered with AuthPolicy
   * of Httpclient. - krb5.conf and login.conf files are copied to the connector
   * instance's directory. - Values of KDC Server and Realm are changed at
   * runtime in krb5.conf. - System properties required for the Kerberos AuthN
   * are set.
   */
  private void kerberosSetUp(final Map configData) {
    String kdcServer = configData.get(SPConstants.KDC_SERVER).toString();
    String googleConnWorkDir = (String) configData.get(GOOGLE_CONN_WORK_DIR);

    if (!kdcServer.equalsIgnoreCase(SPConstants.BLANK_STRING)) {
      AuthPolicy.registerAuthScheme(SPConstants.NEGOTIATE, NegotiateScheme.class);

      InputStream krb5In = SharepointConnectorType.class.getClassLoader().getResourceAsStream(
          SPConstants.CONFIG_KRB5);
      if (krb5In != null) {
        try {
          File krb5File = new File(googleConnWorkDir, SPConstants.FILE_KRB5);
          String krb5Config = new String(ByteStreams.toByteArray(krb5In), SPConstants.UTF_8);
          krb5Config = krb5Config.replace(SPConstants.VAR_KRB5_REALM_UPPERCASE,
              configData.get(SPConstants.DOMAIN).toString().toUpperCase());
          krb5Config = krb5Config.replace(SPConstants.VAR_KRB5_REALM_LOWERCASE,
              configData.get(SPConstants.DOMAIN).toString().toLowerCase());
          krb5Config = krb5Config.replace(SPConstants.VAR_KRB5_KDC_SERVER,
              configData.get(SPConstants.KDC_SERVER).toString().toUpperCase());
          FileOutputStream out = new FileOutputStream(krb5File);
          out.write(krb5Config.getBytes(SPConstants.UTF_8));
          out.close();
        } catch (IOException e) {
          LOGGER.log(Level.SEVERE,
              "Failed to create krb5.conf in connector instance's directory.");
        }
      }

      InputStream loginIn = SharepointConnectorType.class.getClassLoader().getResourceAsStream(
          SPConstants.CONFIG_LOGIN);
      if (loginIn != null) {
        try {
          File loginFile = new File(googleConnWorkDir, SPConstants.FILE_LOGIN);
          String loginConfig = new String(ByteStreams.toByteArray(loginIn), SPConstants.UTF_8);
          FileOutputStream out = new FileOutputStream(loginFile);
          out.write(loginConfig.getBytes(SPConstants.UTF_8));
          out.close();
        } catch (IOException e) {
          LOGGER.log(Level.SEVERE,
              "Failed to create login.conf in connector instance's directory.");
        }
      }

      System.setProperty(SPConstants.SYS_PROP_AUTH_LOGIN_CONFIG, googleConnWorkDir
          + File.separator + SPConstants.FILE_LOGIN);
      System.setProperty(SPConstants.SYS_PROP_AUTH_KRB5_CONFIG, googleConnWorkDir
          + File.separator + SPConstants.FILE_KRB5);
      System.setProperty(SPConstants.SYS_PROP_AUTH_USESUBJETCREDSONLY, SPConstants.FALSE);
    }
  }

  private void unregisterKerberosSetUp(Map configData) {
    AuthPolicy.unregisterAuthScheme(SPConstants.NEGOTIATE);
    String googleConnWorkDir = (String) configData.get(GOOGLE_CONN_WORK_DIR);
    File fileKrb5 = new File(googleConnWorkDir, SPConstants.FILE_KRB5);
    if (fileKrb5 != null && fileKrb5.exists()) {
      fileKrb5.delete();
    }
    File fileLogin = new File(googleConnWorkDir, SPConstants.FILE_LOGIN);
    if (fileLogin != null && fileLogin.exists()) {
      fileLogin.delete();
    }
  }

  /**
   * Returns the flag if SharePoint search visibility options are to be fetched
   * and used
   * 
   * @return the useSPSearchVisibility The search visibility flag
   */
  public String getUseSPSearchVisibility() {
    return useSPSearchVisibility;
  }

  /**
   * Sets the flag if SharePoint search visibility options are to be fetched and
   * used
   * 
   * @param useSPSearchVisibility
   *          The useSPSearchVisibility flag to set
   */
  public void setUseSPSearchVisibility(String useSPSearchVisibility) {
    this.useSPSearchVisibility = useSPSearchVisibility;
  }

  /**
   * Validating feed ACLs related check boxes.
   * 
   * @param configData
   */
  private void validateFeedAclsAndLdapUserGroupsCacheCheckBoxes(
      final Map<String, String> configData) {
    if (!configData.containsKey(SPConstants.PUSH_ACLS)) {
      configData.put(SPConstants.PUSH_ACLS, Boolean.toString(false));
    } else {
      configData.put(SPConstants.PUSH_ACLS, Boolean.toString(true));
    }
    if (!configData.containsKey(SPConstants.FEED_UNPUBLISHED_CONTENT)) {
      configData.put(SPConstants.FEED_UNPUBLISHED_CONTENT,
          Boolean.toString(false));
    } else {
      configData.put(SPConstants.FEED_UNPUBLISHED_CONTENT,
          Boolean.toString(true));
    }

    if (!configData
        .containsKey(SPConstants.USE_CACHE_TO_STORE_LDAP_USER_GROUPS_MEMBERSHIP)) {
      configData.put(
          SPConstants.USE_CACHE_TO_STORE_LDAP_USER_GROUPS_MEMBERSHIP,
          Boolean.toString(false));
    } else {
      configData.put(
          SPConstants.USE_CACHE_TO_STORE_LDAP_USER_GROUPS_MEMBERSHIP,
          Boolean.toString(true));
    }
  }

  /**
   * Validating feed ACLs related HTML controls against blank, specific formats
   * and for valid LDAP context.
   * 
   * @return true if feed ACLs is not selected by the connector administrator
   *         during setting of connector configuration at GSA and false in case
   *         if any of its fields are blank, wrong user name format or couldn't
   *         get valid initial LDAP context.
   */
  private boolean validateFeedAclsRelatedHtmlControls(final ErrorDignostics ed) {
    if (null != pushAcls && this.pushAcls.equalsIgnoreCase(SPConstants.ON)) {
      LOGGER.config("Selected Feed ACLs option.");
      if (!Strings.isNullOrEmpty(usernameFormatInAce)
          && !Strings.isNullOrEmpty(groupnameFormatInAce)) {
        if (!checkForSpecialCharacters(usernameFormatInAce)) {
          ed.set(SPConstants.USERNAME_FORMAT_IN_ACE,
              rb.getString(SPConstants.SPECIAL_CHARACTERS_IN_USERNAME_FORMAT));
          return false;
        }
        if (!checkForSpecialCharacters(groupnameFormatInAce)) {
          ed.set(SPConstants.GROUPNAME_FORMAT_IN_ACE,
              rb.getString(SPConstants.SPECIAL_CHARACTERS_IN_GROUPNAME_FORMAT));
          return false;
        }
        if (!Strings.isNullOrEmpty(ldapServerHostAddress)
            && !Strings.isNullOrEmpty(portNumber)
            && !Strings.isNullOrEmpty(searchBase)
            && !Strings.isNullOrEmpty(domain)) {

          LOGGER.config("Checking for a valid port number.");
          if (!checkForInteger(portNumber)) {
            ed.set(
                SPConstants.PORT_NUMBER,
                MessageFormat.format(
                    rb.getString(SPConstants.INVALID_PORT_NUMBER), portNumber));
            return false;
          }
          Method method;
          if (Method.SSL.toString().equalsIgnoreCase(
              this.connectMethod.toString())) {
            method = Method.SSL;
          } else {
            method = Method.STANDARD;
          }

          AuthType authType;
          if (AuthType.ANONYMOUS.toString().equalsIgnoreCase(
              this.authenticationType.toString())) {
            authType = AuthType.ANONYMOUS;
          } else {
            authType = AuthType.SIMPLE;
          }
          LdapConnectionSettings settings = new LdapConnectionSettings(method,
              this.ldapServerHostAddress, Integer.parseInt(this.portNumber),
              this.searchBase, authType, this.username, this.password,
              this.domain);
          LOGGER
          .config("Created LDAP connection settings object to obtain LDAP context "
              + ldapConnectionSettings);
          LdapConnection ldapConnection = new LdapConnection(settings);
          if (ldapConnection.getLdapContext() == null) {
            Map<LdapConnectionError, String> errors = ldapConnection
                .getErrors();
            Iterator<Entry<LdapConnectionError, String>> iterator = errors
                .entrySet().iterator();
            StringBuffer errorMessage = new StringBuffer();
            errorMessage.append(rb
                .getString(SPConstants.LDAP_CONNECTVITY_ERROR));
            while (iterator.hasNext()) {
              Map.Entry<LdapConnectionError, String> entry = iterator.next();
              errorMessage.append(SPConstants.SPACE + entry.getValue());
            }
            ed.set(SPConstants.LDAP_SERVER_HOST_ADDRESS,
                errorMessage.toString());
            return false;
          }

          if (ldapConnection.getLdapContext() == null) {
            LOGGER
            .log(Level.WARNING,
                "Couldn't obtain context object to query LDAP (AD) directory server.");
            ed.set(SPConstants.LDAP_SERVER_HOST_ADDRESS,
                rb.getString(SPConstants.LDAP_CONNECTVITY_ERROR));
            return false;
          }

          if (!checkForSearchBase(ldapConnection.getLdapContext(), searchBase,
              ed)) {
            return false;
          }
          LOGGER
          .log(Level.CONFIG,
              "Sucessfully created initial LDAP context to query LDAP directory server.");
        } else {
          if (Strings.isNullOrEmpty(ldapServerHostAddress)) {
            ed.set(SPConstants.LDAP_SERVER_HOST_ADDRESS,
                rb.getString(SPConstants.LDAP_SERVER_HOST_ADDRESS_BLANK));
            return false;
          } else if (Strings.isNullOrEmpty(portNumber)) {
            ed.set(SPConstants.PORT_NUMBER,
                rb.getString(SPConstants.PORT_NUMBER_BLANK));
            return false;
          } else if (Strings.isNullOrEmpty(searchBase)) {
            ed.set(SPConstants.SEARCH_BASE,
                rb.getString(SPConstants.SEARCH_BASE_BLANK));
            return false;
          } else if (Strings.isNullOrEmpty(domain)) {
            ed.set(SPConstants.DOMAIN,
                rb.getString(SPConstants.BLANK_DOMAIN_NAME_LDAP));
            return false;
          }
        }
        if (!Strings.isNullOrEmpty(useCacheToStoreLdapUserGroupsMembership)) {
          if (null != useCacheToStoreLdapUserGroupsMembership
              && this.useCacheToStoreLdapUserGroupsMembership
              .equalsIgnoreCase(SPConstants.ON)) {
            if (!Strings.isNullOrEmpty(initialCacheSize)
                && !Strings.isNullOrEmpty(cacheRefreshInterval)) {
              if (!checkForInteger(this.initialCacheSize)) {
                ed.set(SPConstants.INITAL_CACHE_SIZE, MessageFormat.format(
                    rb.getString(SPConstants.INVALID_INITIAL_CACHE_SIZE),
                    initialCacheSize));
                return false;
              }
              if (!checkForInteger(this.cacheRefreshInterval)) {
                ed.set(SPConstants.CACHE_REFRESH_INTERVAL, MessageFormat
                    .format(rb
                        .getString(SPConstants.INVALID_CACHE_REFRESH_INTERVAL),
                        cacheRefreshInterval));
                return false;
              }
            } else {
              if (Strings.isNullOrEmpty(initialCacheSize)) {
                ed.set(SPConstants.INITAL_CACHE_SIZE,
                    rb.getString(SPConstants.BLANK_INITIAL_CACHE_SIZE));
                return false;
              }
              if (Strings.isNullOrEmpty(cacheRefreshInterval)) {
                ed.set(SPConstants.CACHE_REFRESH_INTERVAL,
                    rb.getString(SPConstants.BLANK_CACHE_REFRESH_INTERVAL));
                return false;
              }
            }
          }

        }
      } else {
        if (Strings.isNullOrEmpty(groupnameFormatInAce)) {
          ed.set(SPConstants.GROUPNAME_FORMAT_IN_ACE, MessageFormat.format(
              rb.getString(SPConstants.BLANK_GROUPNAME_FORMAT),
              groupnameFormatInAce));
          return false;
        }
        if (Strings.isNullOrEmpty(usernameFormatInAce)) {
          ed.set(SPConstants.USERNAME_FORMAT_IN_ACE, MessageFormat.format(
              rb.getString(SPConstants.BLANK_USERNAME_FORMAT),
              usernameFormatInAce));
          return false;
        }
      }

    }
    return true;
  }

  /**
   * Parses the string argument as a signed decimal integer. The characters in
   * the string must all be decimal digits.
   * 
   * @param number
   * @return true if given string
   */
  private boolean checkForInteger(String number) {
    try {
      Integer.parseInt(number);
    } catch (NumberFormatException ne) {
      LOGGER.log(Level.WARNING,
          "Exception thrown while parsing LDAP port number.", ne);
      return false;
    }
    return true;
  }

  public boolean checkForSearchBase(LdapContext ldapContext, String searchBase,
      ErrorDignostics ed) {
    /**
     * InvalidNameException indicates that specified name does not conform to
     * the naming syntax for search base naming convention
     */
    try {
      if (ldapContext.lookup(searchBase) != null) {
        return true;
      }
    } catch (InvalidNameException e) {
      LOGGER.log(Level.WARNING, "Invalid name for search base lookup." + "\n"
          + e);
      ed.set(SPConstants.SEARCH_BASE,
          rb.getString(SPConstants.SEARCH_BASE_INVALID_SYNTAX));
    } catch (NameNotFoundException e) {
      LOGGER.log(Level.WARNING, "Invalid name for search base" + "\n" + e);
      ed.set(SPConstants.SEARCH_BASE,
          rb.getString(SPConstants.SEARCH_BASE_INVALID_NAME));
    } catch (NamingException e) {
      LOGGER.log(Level.WARNING,
          "Invalid naming exception for search base lookup." + "\n" + e);
      ed.set(SPConstants.SEARCH_BASE,
          rb.getString(SPConstants.SEARCH_BASE_INVALID));
    }
    return false;
  }

  /**
   * Checks if the given name consist of special characters or not and returns
   * true if there is no special characters found in the given name.
   * 
   * @param nameFormat
   * @return false if given name consist of at least on special character other
   *         than '@' and '\'
   */
  private boolean checkForSpecialCharacters(String nameFormat) {
    Pattern pattern = Pattern.compile("[^a-zA-Z0-9@\\\\]");
    Matcher matcher = pattern.matcher(nameFormat);
    if (matcher.find()) {
      return false;
    }
    return true;
  }
}
