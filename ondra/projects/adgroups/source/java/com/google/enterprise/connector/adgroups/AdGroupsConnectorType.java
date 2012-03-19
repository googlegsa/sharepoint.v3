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

package com.google.enterprise.connector.adgroups;

import com.google.common.base.Strings;
import com.google.enterprise.connector.adgroups.LdapConstants.AuthType;
import com.google.enterprise.connector.adgroups.LdapConstants.LdapConnectionError;
import com.google.enterprise.connector.adgroups.LdapConstants.Method;
import com.google.enterprise.connector.adgroups.UserGroupsService.LdapConnection;
import com.google.enterprise.connector.adgroups.UserGroupsService.LdapConnectionSettings;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.XmlUtils;

import java.io.IOException;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
public class AdGroupsConnectorType implements ConnectorType {
	private final Logger LOGGER = Logger.getLogger(AdGroupsConnectorType.class.getName());
	private Collator collator = Util.getCollator();

	private String domain = null;
	private String username = null;
	private String password = null;

	private List<String> keys = null;
	private final HashMap<Object, String> configStrings = new HashMap<Object, String>();
	private String initialConfigForm = null;

	ResourceBundle rb = null;
	public static final String GOOGLE_CONN_WORK_DIR = "googleConnectorWorkDir";
	private String usernameFormatInAce;
	private String groupnameFormatInAce;
	private String ldapServerHostAddress;
	private String ldapUserName;
	private String ldapPassword;
	private String ldapDomain;
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
	 * Sets the keys that are required for configuration. These are the actual
	 * keys used by the class.
	 * 
	 * @param inKeys A list of String keys
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
			configStrings.put(SPConstants.MANDATORY_FIELDS, rb.getString(SPConstants.MANDATORY_FIELDS));
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
	 * @param configMap The configuration keys and their values
	 * @param ed Contains the validation error, if any
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

				String value = null;
				if (configMap != null) {
					value = configMap.get(key);
				}
				if (value == null) {
					value = "";
				}

				if (collator.equals(key, SPConstants.AUTHENTICATION_TYPE)) {
					buf.append(SPConstants.OPEN_ELEMENT + SPConstants.SELECT);
					appendAttribute(buf, SPConstants.CONFIG_NAME, key);
					appendAttribute(buf, SPConstants.CONFIG_ID, key);
					buf.append(SPConstants.SPACE + SPConstants.STYLE);

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
				} else if (collator.equals(key, SPConstants.USE_CACHE_TO_STORE_LDAP_USER_GROUPS_MEMBERSHIP)) {
					buf.append(SPConstants.BREAK_LINE);
					buf.append(SPConstants.OPEN_ELEMENT);
					buf.append(SPConstants.INPUT);
					appendAttribute(buf, SPConstants.TYPE, SPConstants.CHECKBOX);
					appendAttribute(buf, SPConstants.CONFIG_NAME, key);
					appendAttribute(buf, SPConstants.CONFIG_ID, key);
					appendAttribute(buf, SPConstants.TITLE, rb.getString(SPConstants.USE_CACHE_TO_STORE_LDAP_USER_GROUPS_MEMBERSHIP_LABEL));
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
					buf.append(rb.getString(SPConstants.USE_CACHE_TO_STORE_LDAP_USER_GROUPS_MEMBERSHIP_LABEL));
					buf.append(SPConstants.OPEN_ELEMENT + SPConstants.FORWARD_SLASH
							+ SPConstants.LABEL + SPConstants.CLOSE_ELEMENT);
				} else if (collator.equals(key, SPConstants.USERNAME_FORMAT_IN_ACE)) {
					buf.append(SPConstants.OPEN_ELEMENT + SPConstants.SELECT);
					appendAttribute(buf, SPConstants.CONFIG_NAME, key);
					appendAttribute(buf, SPConstants.CONFIG_ID, key);
					buf.append(SPConstants.SPACE + SPConstants.STYLE);
					buf.append(SPConstants.CLOSE_ELEMENT + SPConstants.NEW_LINE);
					buf.append(SPConstants.OPEN_ELEMENT + SPConstants.OPTION
							+ SPConstants.SPACE + SPConstants.VALUE + SPConstants.EQUAL_TO
							+ "\"");
					buf.append(SPConstants.USERNAME_FORMAT_IN_ACE_ONLY_USERNAME);
					buf.append("\"");
					if (Strings.isNullOrEmpty(value)
							|| value.equalsIgnoreCase(SPConstants.USERNAME_FORMAT_IN_ACE_ONLY_USERNAME)) {
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
					if (value.equalsIgnoreCase(SPConstants.USERNAME_FORMAT_IN_ACE_DOMAINNAME_SLASH_USERNAME)) {
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
					if (value.equalsIgnoreCase(SPConstants.USERNAME_FORMAT_IN_ACE_USERNAME_AT_DOMAINNAME)) {
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
					buf.append(SPConstants.CLOSE_ELEMENT + SPConstants.NEW_LINE);
					buf.append(SPConstants.OPEN_ELEMENT + SPConstants.OPTION
							+ SPConstants.SPACE + SPConstants.VALUE + SPConstants.EQUAL_TO
							+ "\"");
					buf.append(SPConstants.GROUPNAME_FORMAT_IN_ACE_ONLY_GROUP_NAME);
					buf.append("\"");
					if (value.equalsIgnoreCase(SPConstants.GROUPNAME_FORMAT_IN_ACE_ONLY_GROUP_NAME)) {
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
							|| value.equalsIgnoreCase(SPConstants.GROUPNAME_FORMAT_IN_ACE_DOMAINNAME_SLASH_GROUPNAME)) {
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
					if (value.equalsIgnoreCase(SPConstants.GROUPNAME_FORMAT_IN_ACE_GROUPNAME_AT_DOMAIN)) {
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
					if (collator.equals(key, SPConstants.PASSWORD) ||
							collator.equals(key, SPConstants.LDAP_PASSWORD)) {
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
								appendAttribute(buf, SPConstants.VALUE, SPConstants.LDAP_DEFAULT_PORT_NUMBER);
							} else if (value.equalsIgnoreCase(SPConstants.LDAP_DEFAULT_PORT_NUMBER)) {
								appendAttribute(buf, SPConstants.VALUE, SPConstants.LDAP_DEFAULT_PORT_NUMBER);
							} else {
								appendAttribute(buf, SPConstants.VALUE, value.trim());
							}
						} else if (collator.equals(key, SPConstants.INITAL_CACHE_SIZE)) {
							if (Strings.isNullOrEmpty(value)) {
								if (null == this.useCacheToStoreLdapUserGroupsMembership
										|| this.useCacheToStoreLdapUserGroupsMembership.equalsIgnoreCase(SPConstants.OFF)) {
									buf.append(SPConstants.SPACE + SPConstants.DISABLED
											+ SPConstants.EQUAL_TO + "\"" + SPConstants.TRUE + "\"");
								}
								appendAttribute(buf, SPConstants.VALUE, SPConstants.LDAP_INITIAL_CACHE_SIZE);
							} else if (value.equalsIgnoreCase(SPConstants.LDAP_INITIAL_CACHE_SIZE)) {
								appendAttribute(buf, SPConstants.VALUE, SPConstants.LDAP_INITIAL_CACHE_SIZE);
							} else {
								appendAttribute(buf, SPConstants.VALUE, value);
							}
						} else if (collator.equals(key, SPConstants.CACHE_REFRESH_INTERVAL)) {
							if (Strings.isNullOrEmpty(value)) {
								if (null == this.useCacheToStoreLdapUserGroupsMembership
										|| this.useCacheToStoreLdapUserGroupsMembership.equalsIgnoreCase(SPConstants.OFF)) {
									buf.append(SPConstants.SPACE + SPConstants.DISABLED
											+ SPConstants.EQUAL_TO + "\"" + SPConstants.TRUE + "\"");
								}
								appendAttribute(buf, SPConstants.VALUE, SPConstants.LDAP_CACHE_REFRESH_INTERVAL_TIME);
							} else if (value.equalsIgnoreCase(SPConstants.LDAP_CACHE_REFRESH_INTERVAL_TIME)) {
								appendAttribute(buf, SPConstants.VALUE, SPConstants.LDAP_CACHE_REFRESH_INTERVAL_TIME);
							} else {
								appendAttribute(buf, SPConstants.VALUE, value.trim());
							}
						}
						buf.append(SPConstants.SPACE + SPConstants.ONKEY_PRESS);
						buf.append("\"return onlyNumbers(event);\"");
					} else {
						appendAttribute(buf, SPConstants.VALUE, value);
					}

					if (collator.equals(key, SPConstants.LDAP_SERVER_HOST_ADDRESS)
							|| collator.equals(key, SPConstants.SEARCH_BASE)
							|| collator.equals(key, SPConstants.LDAP_USERNAME)
							|| collator.equals(key, SPConstants.LDAP_DOMAIN)
							|| collator.equals(key, SPConstants.LDAP_PASSWORD)) {
						appendAttribute(buf, SPConstants.TEXTBOX_SIZE, SPConstants.TEXTBOX_SIZE_VALUE);
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
			buf.append("</div><div style=\"text-align: right; ").append("color: red; font-weight: bold; ").append("margin-right: 0.3em;\">*</div>");
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
					"Exceptions while constructing the config form for attribute : ").append(attrName).append(" with value : ").append(attrValue).toString();
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
		} else if (collator.equals(key, SPConstants.USERNAME_FORMAT_IN_ACE)) {
			this.usernameFormatInAce = val.trim();
		} else if (collator.equals(key, SPConstants.GROUPNAME_FORMAT_IN_ACE)) {
			this.groupnameFormatInAce = val.trim();
		} else if (collator.equals(key, SPConstants.LDAP_SERVER_HOST_ADDRESS)) {
			this.ldapServerHostAddress = val.trim();
		} else if (collator.equals(key, SPConstants.LDAP_USERNAME)) {
			this.ldapUserName = val.trim();
		} else if (collator.equals(key, SPConstants.LDAP_PASSWORD)) {
			this.ldapPassword = val;
		} else if (collator.equals(key, SPConstants.LDAP_DOMAIN)) {
			this.ldapDomain = val.trim();
		} else if (collator.equals(key, SPConstants.PORT_NUMBER)) {
			this.portNumber = val.trim();
		} else if (collator.equals(key, SPConstants.SEARCH_BASE)) {
			this.searchBase = val.trim();
		} else if (collator.equals(key, SPConstants.AUTHENTICATION_TYPE)) {
			this.authenticationType = val.trim();
		} else if (collator.equals(key, SPConstants.CONNECT_METHOD)) {
			this.connectMethod = val.trim();
		} else if (collator.equals(key, SPConstants.USE_CACHE_TO_STORE_LDAP_USER_GROUPS_MEMBERSHIP)) {
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
		if (!configData.containsKey(SPConstants.USE_CACHE_TO_STORE_LDAP_USER_GROUPS_MEMBERSHIP)) {
			this.useCacheToStoreLdapUserGroupsMembership = SPConstants.OFF;
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
			 	}
			}
			setSharepointCredentials(key, val);
		}

		if ((username != null)
				&& ((username.indexOf("@") != -1) || (username.indexOf("\\") != -1))
				&& (domain != null) && !domain.equals(SPConstants.BLANK_STRING)) {
			ed.set(SPConstants.USERNAME, rb.getString(SPConstants.DUPLICATE_DOMAIN));
			return false;
		}

		try {
                  new ConnectorContext(domain, username, password, "");
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to create ConnectorContext with the received configuration values. ");
		}

		// Validating the feed ACLs related check boxes which are off by
		// default.
		validateFeedAclsAndLdapUserGroupsCacheCheckBoxes(configData);

		if (!validateFeedAclsRelatedHtmlControls(ed)) {
			return false;
		}

		return true;
	}

	/**
	 * Used while re-displaying the connector configuration page after any
	 * validation error occurs.
	 */
	private ConfigureResponse makeValidatedForm(
			final Map<String, String> configMap, final ErrorDignostics ed) {
		if (configMap == null) {
			LOGGER.warning("configMap is not found");
			if (rb != null) {
				return new ConfigureResponse(rb.getString("CONFIGMAP_NOT_FOUND"), "");
			} else {
				return new ConfigureResponse("resource bundle not found", "");
			}
		}
		final ConfigureResponse result = new ConfigureResponse(
				ed.error_message, makeConfigForm(configMap, ed));
		return result;
	}

	/**
	 * Called by connector-manager to display the connector configuration page.
	 */
	public ConfigureResponse getConfigForm(final Locale locale) {
		LOGGER.config("Locale " + locale);
		this.editMode = false;

		collator = Util.getCollator(locale);
		rb = ResourceBundle.getBundle("AdGroupsConnectorResources", locale);
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
		rb = ResourceBundle.getBundle("AdGroupsConnectorResources", locale);
		setConfigStrings();
		final ConfigureResponse result = new ConfigureResponse("",
				makeConfigForm(configMap, null));
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

		rb = ResourceBundle.getBundle("AdGroupsConnectorResources", locale);
		setConfigStrings();

		final ErrorDignostics ed = new ErrorDignostics();
		if (validateConfigMap(configData, ed)) {
			// all is ok
			return null;
		}
		final ConfigureResponse configureResponse = makeValidatedForm(configData, ed);

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
		if (collator.equals(configKey, SPConstants.USERNAME)
				|| collator.equals(configKey, SPConstants.PASSWORD)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * JavaScript required on the connector configuration page.
	 * 
	 * @param buf contains the Form snippet
	 */
	public void addJavaScript(final StringBuffer buf) {
		if (buf != null) {
			String js = "\r\n <script language=\"JavaScript\"> \r\n <![CDATA[ ";

			js += "\r\n function enableOrDisableUserGroupsCacheControles () {"
					+ ""
					+ "\r\n if (document.getElementById(\"useCacheToStoreLdapUserGroupsMembership\").checked == true){ "
					+ "\r\n document.getElementById(\"initialCacheSize\").disabled=false"
					+ "\r\n document.getElementById(\"cacheRefreshInterval\").disabled=false"
					+ "\r\n } else {"
					+ "\r\n document.getElementById(\"cacheRefreshInterval\").disabled=true"
					+ "\r\n document.getElementById(\"initialCacheSize\").disabled=true"
					+ "\r\n }" + "\r\n }";

			js += "\r\n function onlyNumbers(evt){"
					+ "\r\n  var charCode = (evt.which) ? evt.which : event.keyCode"
					+ "\r\n if (charCode > 31 && (charCode < 48 || charCode > 57))"
					+ "\r\n return false;" + "\r\n return true;" + "\r\n }";

			// XXX
			// js += "document.getElementById('" + SPConstants.SHAREPOINT_URL
			// 		+ "').focus()";

			js += "\r\n ]]> \r\n </script> \r\n";
			buf.append(js);
		}
	}

	/**
	 * Validating feed ACLs related check boxes.
	 * 
	 * @param configData
	 */
	private void validateFeedAclsAndLdapUserGroupsCacheCheckBoxes(
			final Map<String, String> configData) {
		if (!configData.containsKey(SPConstants.USE_CACHE_TO_STORE_LDAP_USER_GROUPS_MEMBERSHIP)) {
			configData.put(SPConstants.USE_CACHE_TO_STORE_LDAP_USER_GROUPS_MEMBERSHIP, Boolean.toString(false));
		} else {
			configData.put(SPConstants.USE_CACHE_TO_STORE_LDAP_USER_GROUPS_MEMBERSHIP, Boolean.toString(true));
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
		LOGGER.config("Selected Feed ACLs option.");
		if (!Strings.isNullOrEmpty(usernameFormatInAce)
				&& !Strings.isNullOrEmpty(groupnameFormatInAce)) {
			if (!checkForSpecialCharacters(usernameFormatInAce)) {
				ed.set(SPConstants.USERNAME_FORMAT_IN_ACE, rb.getString(SPConstants.SPECIAL_CHARACTERS_IN_USERNAME_FORMAT));
				return false;
			}
			if (!checkForSpecialCharacters(groupnameFormatInAce)) {
				ed.set(SPConstants.GROUPNAME_FORMAT_IN_ACE, rb.getString(SPConstants.SPECIAL_CHARACTERS_IN_GROUPNAME_FORMAT));
				return false;
			}
			if (!Strings.isNullOrEmpty(ldapServerHostAddress)
					&& !Strings.isNullOrEmpty(portNumber)
					&& !Strings.isNullOrEmpty(searchBase)
					&& !Strings.isNullOrEmpty(domain)) {
				LOGGER.config("Checking for a valid port number.");
				if (!checkForInteger(portNumber)) {
					ed.set(SPConstants.PORT_NUMBER, MessageFormat.format(rb.getString(SPConstants.INVALID_PORT_NUMBER), portNumber));
					return false;
				}
				Method method;
				if (Method.SSL.toString().equalsIgnoreCase(this.connectMethod.toString())) {
					method = Method.SSL;
				} else {
					method = Method.STANDARD;
				}

				AuthType authType;
				if (AuthType.ANONYMOUS.toString().equalsIgnoreCase(this.authenticationType.toString())) {
					authType = AuthType.ANONYMOUS;
				} else {
					authType = AuthType.SIMPLE;
				}
				LdapConnectionSettings settings = new LdapConnectionSettings(method,
						this.ldapServerHostAddress, Integer.parseInt(this.portNumber),
						this.searchBase, authType, this.ldapUserName, this.ldapPassword,
						this.ldapDomain);
				LOGGER.config("Created LDAP connection settings object to obtain LDAP context "
						+ ldapConnectionSettings);

				// if multildap do not validate
				if (!ldapServerHostAddress.contains("|")) {
					LdapConnection ldapConnection = new LdapConnection(settings);
					if (ldapConnection.getLdapContext() == null) {
						Map<LdapConnectionError, String> errors = ldapConnection.getErrors();
						Iterator<Entry<LdapConnectionError, String>> iterator = errors.entrySet().iterator();
						StringBuffer errorMessage = new StringBuffer();
						errorMessage.append(rb.getString(SPConstants.LDAP_CONNECTVITY_ERROR));
						while (iterator.hasNext()) {
							Map.Entry<LdapConnectionError, String> entry = iterator.next();
							errorMessage.append(SPConstants.SPACE + entry.getValue());
						}

						ed.set(SPConstants.LDAP_SERVER_HOST_ADDRESS, errorMessage.toString());
						return false;
					}

					if (ldapConnection.getLdapContext() == null) {
						LOGGER.log(Level.WARNING, "Couldn't obtain context object to query LDAP (AD) directory server.");
						ed.set(SPConstants.LDAP_SERVER_HOST_ADDRESS, rb.getString(SPConstants.LDAP_CONNECTVITY_ERROR));
						return false;
					}

					if (!checkForSearchBase(ldapConnection.getLdapContext(), searchBase, ed)) {
						return false;
					}
					LOGGER.log(Level.CONFIG, "Sucessfully created initial LDAP context to query LDAP directory server.");
				}
			} else {
				if (Strings.isNullOrEmpty(ldapServerHostAddress)) {
					ed.set(SPConstants.LDAP_SERVER_HOST_ADDRESS, rb.getString(SPConstants.LDAP_SERVER_HOST_ADDRESS_BLANK));
					return false;
				} else if (Strings.isNullOrEmpty(portNumber)) {
					ed.set(SPConstants.PORT_NUMBER, rb.getString(SPConstants.PORT_NUMBER_BLANK));
					return false;
				} else if (Strings.isNullOrEmpty(searchBase)) {
					ed.set(SPConstants.SEARCH_BASE, rb.getString(SPConstants.SEARCH_BASE_BLANK));
					return false;
				} else if (Strings.isNullOrEmpty(domain)) {
					ed.set(SPConstants.DOMAIN, rb.getString(SPConstants.BLANK_DOMAIN_NAME_LDAP));
					return false;
				}
			}
			if (!Strings.isNullOrEmpty(useCacheToStoreLdapUserGroupsMembership)) {
				if (null != useCacheToStoreLdapUserGroupsMembership
						&& this.useCacheToStoreLdapUserGroupsMembership.equalsIgnoreCase(SPConstants.ON)) {
					if (!Strings.isNullOrEmpty(initialCacheSize)
							&& !Strings.isNullOrEmpty(cacheRefreshInterval)) {
						if (!checkForInteger(this.initialCacheSize)) {
							ed.set(SPConstants.INITAL_CACHE_SIZE, MessageFormat.format(rb.getString(SPConstants.INVALID_INITIAL_CACHE_SIZE), initialCacheSize));
							return false;
						}
						if (!checkForInteger(this.cacheRefreshInterval)) {
							ed.set(SPConstants.CACHE_REFRESH_INTERVAL, MessageFormat.format(rb.getString(SPConstants.INVALID_CACHE_REFRESH_INTERVAL), cacheRefreshInterval));
							return false;
						}
					} else {
						if (Strings.isNullOrEmpty(initialCacheSize)) {
							ed.set(SPConstants.INITAL_CACHE_SIZE, rb.getString(SPConstants.BLANK_INITIAL_CACHE_SIZE));
							return false;
						}
						if (Strings.isNullOrEmpty(cacheRefreshInterval)) {
							ed.set(SPConstants.CACHE_REFRESH_INTERVAL, rb.getString(SPConstants.BLANK_CACHE_REFRESH_INTERVAL));
							return false;
						}
					}
				}
			}
		} else {
			if (Strings.isNullOrEmpty(groupnameFormatInAce)) {
				ed.set(SPConstants.GROUPNAME_FORMAT_IN_ACE, MessageFormat.format(rb.getString(SPConstants.BLANK_GROUPNAME_FORMAT), groupnameFormatInAce));
				return false;
			}
			if (Strings.isNullOrEmpty(usernameFormatInAce)) {
				ed.set(SPConstants.USERNAME_FORMAT_IN_ACE, MessageFormat.format(rb.getString(SPConstants.BLANK_USERNAME_FORMAT), usernameFormatInAce));
				return false;
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
			LOGGER.log(Level.WARNING, "Exception thrown while parsing LDAP port number.", ne);
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
			ed.set(SPConstants.SEARCH_BASE, rb.getString(SPConstants.SEARCH_BASE_INVALID_SYNTAX));
		} catch (NameNotFoundException e) {
			LOGGER.log(Level.WARNING, "Invalid name for search base" + "\n" + e);
			ed.set(SPConstants.SEARCH_BASE, rb.getString(SPConstants.SEARCH_BASE_INVALID_NAME));
		} catch (NamingException e) {
			LOGGER.log(Level.WARNING, "Invalid naming exception for search base lookup."
					+ "\n" + e);
			ed.set(SPConstants.SEARCH_BASE, rb.getString(SPConstants.SEARCH_BASE_INVALID));
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
