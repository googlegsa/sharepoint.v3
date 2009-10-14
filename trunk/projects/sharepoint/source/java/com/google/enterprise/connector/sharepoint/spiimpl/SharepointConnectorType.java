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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.contrib.auth.NegotiateScheme;

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.wsclient.GSBulkAuthorizationWS;
import com.google.enterprise.connector.sharepoint.wsclient.WebsWS;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.ConnectorType;

/**
 * ConnectorType implementation for Sharepoint This class is mainly desinged for
 * controlling the connector configuration which incompasses creation of
 * connector configuration form, validating the configuration values etc.
 *
 * @author nitendra_thakur
 */
public class SharepointConnectorType implements ConnectorType {
	private final Logger LOGGER = Logger.getLogger(SharepointConnectorType.class.getName());
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

	// Create dummy context for doing validations.
	private SharepointClientContext sharepointClientContext = null;

	private List keys = null;
	private final HashMap<Object, String> configStrings = new HashMap<Object, String>();
	private String initialConfigForm = null;

	ResourceBundle rb = null;
	public static final String GOOGLE_CONN_WORK_DIR = "googleConnectorWorkDir";	

	/**
	 * Sets the keys that are required for configuration. These are the actual
	 * keys used by the class.
	 *
	 * @param inKeys A list of String keys
	 */
	public void setConfigKeys(final List inKeys) {
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
	 * Makes a config form snippet using the keys (in the supplied order) and,
	 * if passed a non-null config map, pre-filling values in from that map.
	 *
	 * @param configMap The configuration keys and their values
	 * @param ed Contains the validation error, if any
	 */
	private String makeConfigForm(final Map configMap, final ErrorDignostics ed) {
		final StringBuffer buf = new StringBuffer();
		if (keys != null) {
			for (final Iterator itr = keys.iterator(); itr.hasNext();) {
				final String key = (String) itr.next();
				final String configKey = (String) configStrings.get(key);

				if ((ed == null) || !key.equals(ed.error_key)) {
					appendStartRow(buf, key, configKey, false);
				} else {
					appendStartRow(buf, key, configKey, true);
				}

				if (collator.equals(key, SPConstants.ALIAS_MAP)) {
					appendTableForAliasMapping(buf);
					if (configMap == null) {
						appendRowForAliasMapping(buf, SPConstants.BLANK_STRING, SPConstants.BLANK_STRING, false);
					} else {
						final String aliasMapString = (String) configMap.get(key);
						parseAlias(aliasMapString, null);
						if (aliasMap == null) {
							appendRowForAliasMapping(buf, SPConstants.BLANK_STRING, SPConstants.BLANK_STRING, false);
						} else {
							final Set aliasValues = aliasMap.keySet();
							int i = 0;
							for (final Iterator aliasItr = aliasValues.iterator(); aliasItr.hasNext();) {
								final String alias_source_pattern = (String) aliasItr.next();
								String alias_host_port = "";
								final ArrayList aliases = (ArrayList) aliasMap.get(alias_source_pattern);
								if (aliases.size() == 0) {
									if (i % 2 == 0) {
										appendRowForAliasMapping(buf, SPConstants.BLANK_STRING, SPConstants.BLANK_STRING, false);
									} else {
										appendRowForAliasMapping(buf, SPConstants.BLANK_STRING, SPConstants.BLANK_STRING, true);
									}
									++i;
								} else {
									try {
										for (final Iterator it = aliases.iterator(); it.hasNext();) {
											alias_host_port = (String) it.next();
											if (it.hasNext()
													|| aliasItr.hasNext()) {
												if (i % 2 == 0) {
													appendRowForAliasMapping(buf, alias_source_pattern, alias_host_port, false);
												} else {
													appendRowForAliasMapping(buf, alias_source_pattern, alias_host_port, true);
												}
											} else {
												if (i % 2 == 0) {
													appendRowForAliasMapping(buf, alias_source_pattern, alias_host_port, false);
												} else {
													appendRowForAliasMapping(buf, alias_source_pattern, alias_host_port, true);
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
					appendAttribute(buf, SPConstants.VALUE, rb.getString(SPConstants.ADD_MORE));
					appendAttribute(buf, SPConstants.ONCLICK, "addRow(); return false");
					buf.append(SPConstants.SLASH + SPConstants.CLOSE_ELEMENT);
					buf.append(SPConstants.TD_END);
					buf.append(SPConstants.TR_END);

					buf.append(SPConstants.END_TABLE);
				}

				String value = null;
				if (configMap != null) {
					value = (String) configMap.get(key);
				}
				if (value == null) {
					value = "";
				}

				if (collator.equals(key, SPConstants.AUTHORIZATION)) {

					buf.append(SPConstants.OPEN_ELEMENT);
					buf.append(SPConstants.INPUT);
					appendAttribute(buf, SPConstants.TYPE, SPConstants.RADIO);
					appendAttribute(buf, SPConstants.CONFIG_NAME, key);
					appendAttribute(buf, SPConstants.CONFIG_ID, key);
					appendAttribute(buf, SPConstants.VALUE, SPConstants.METADATA_URL_FEED);
					appendAttribute(buf, SPConstants.TITLE, rb.getString(SPConstants.HELP_AUTHZ_BY_GSA));
					if ((value.length() == 0)
							|| value.equalsIgnoreCase(SPConstants.METADATA_URL_FEED)) {
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
					appendAttribute(buf, SPConstants.VALUE, SPConstants.CONTENT_FEED);
					appendAttribute(buf, SPConstants.TITLE, rb.getString(SPConstants.HELP_AUTHZ_BY_CONNECTOR));
					if (value.equalsIgnoreCase(SPConstants.CONTENT_FEED)) {
						appendAttribute(buf, SPConstants.CHECKED, SPConstants.CHECKED);
					}
					buf.append(" /" + SPConstants.CLOSE_ELEMENT);
					buf.append(rb.getString(SPConstants.AUTHZ_BY_CONNECTOR));
				} else if ((collator.equals(key, SPConstants.EXCLUDED_URLS))
						|| (collator.equals(key, SPConstants.INCLUDED_URLS))) {
					buf.append(SPConstants.OPEN_ELEMENT);
					buf.append(SPConstants.TEXTAREA);
					appendAttribute(buf, SPConstants.CONFIG_NAME, key);
					appendAttribute(buf, SPConstants.CONFIG_ID, key);
					appendAttribute(buf, SPConstants.ROWS, SPConstants.ROWS_VALUE);
					appendAttribute(buf, SPConstants.COLS, SPConstants.COLS_VALUE);
					appendAttribute(buf, SPConstants.TEXTBOX_SIZE, SPConstants.TEXTBOX_SIZE_VALUE);
					buf.append(SPConstants.CLOSE_ELEMENT);

					buf.append(value.replace(SPConstants.SPACE, SPConstants.NEW_LINE));

					buf.append(SPConstants.OPEN_ELEMENT);
					buf.append(SPConstants.END_TEXTAREA);
					buf.append(SPConstants.CLOSE_ELEMENT);
				} else {
					buf.append(SPConstants.OPEN_ELEMENT);
					buf.append(SPConstants.INPUT);
					if (collator.equals(key, SPConstants.PASSWORD)) {
						appendAttribute(buf, SPConstants.TYPE, SPConstants.PASSWORD);
					} else if (collator.equals(key, SPConstants.ALIAS_MAP)) {
						appendAttribute(buf, SPConstants.TYPE, SPConstants.HIDDEN);
					} else {
						appendAttribute(buf, SPConstants.TYPE, SPConstants.TEXT);
					}
					appendAttribute(buf, SPConstants.CONFIG_NAME, key);
					appendAttribute(buf, SPConstants.CONFIG_ID, key);
					appendAttribute(buf, SPConstants.VALUE, value);
					if (collator.equals(key, SPConstants.SHAREPOINT_URL) /*
					 * ||
					 * collator
					 * .
					 * equals
					 * (key,
					 * DOMAIN
					 * )
					 */
					 || collator.equals(key, SPConstants.MYSITE_BASE_URL)) {
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
	 * For creating the region Site Alias Mapping field
	 *
	 * @param buf Contains the form snippet
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
		appendAttribute(buf, SPConstants.TITLE, rb.getString(SPConstants.ALIAS_SOURCE_PATTERN_HELP));
		buf.append(SPConstants.CLOSE_ELEMENT);
		buf.append(rb.getString(SPConstants.ALIAS_SOURCE_PATTERN));
		buf.append(SPConstants.TH_END);

		buf.append(SPConstants.OPEN_ELEMENT);
		buf.append(SPConstants.TH);
		appendAttribute(buf, SPConstants.TITLE, rb.getString(SPConstants.ALIAS_HELP));
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
	 * @param buf Contains form snippet
	 * @param sourceValue Source_URL_Pattern
	 * @param aliasValue Alias
	 * @param color To keep track of the alternate background color
	 */
	private void appendRowForAliasMapping(final StringBuffer buf,
			final String sourceValue, final String aliasValue,
			final boolean color) {
		buf.append(SPConstants.TR_START);

		if (color) {
			buf.append(SPConstants.TD_START_FORMAT);
		} else {
			buf.append(SPConstants.TD_START);
		}
		buf.append(SPConstants.OPEN_ELEMENT);
		buf.append(SPConstants.INPUT);
		appendAttribute(buf, SPConstants.TYPE, SPConstants.TEXT);
		appendAttribute(buf, SPConstants.TEXTBOX_SIZE, SPConstants.ALIAS_TEXTBOX_SIZE_VALUE);
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
		appendAttribute(buf, SPConstants.TEXTBOX_SIZE, SPConstants.ALIAS_TEXTBOX_SIZE_VALUE);
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
			 * , final String
			 * displayValue
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
		// TODO xml-encode the special characters (< > " etc.)
		buf.append(attrValue);
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
		}
	}

	/**
	 * Validates the values filled-in by the user at the connector's
	 * configuration page.
	 */
	private boolean validateConfigMap(final Map configData,
			final ErrorDignostics ed) {
		if (configData == null) {
			LOGGER.warning("configData map is not found");
			return false;
		}

		String feedType = null;
		String kdcServer = configData.get(SPConstants.KDC_SERVER).toString();

		if(!kdcServer.equalsIgnoreCase(SPConstants.BLANK_STRING)){
			kerberosSetUp(configData);
		}

		for (final Iterator i = keys.iterator(); i.hasNext();) {
			final String key = (String) i.next();
			final String val = (String) configData.get(key);

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
					final Set invalidSet = validatePatterns(val);
					if (invalidSet != null) {
						ed.set(SPConstants.INCLUDED_URLS, rb.getString(SPConstants.INVALID_INCLUDE_PATTERN)
								+ invalidSet.toString());
						return false;
					}
				}
			} else if (collator.equals(key, SPConstants.ALIAS_MAP)
					&& (val != null) && !val.equals(SPConstants.BLANK_STRING)) {
				final Set<String> wrongEntries = new HashSet<String>();
				final String message = parseAlias(val, wrongEntries);
				if (message != null) {
					ed.set(SPConstants.ALIAS_MAP, rb.getString(message) + " "
							+ wrongEntries);
					return false;
				}
			} else if (collator.equals(key, SPConstants.EXCLUDED_URLS)) {
				final Set invalidSet = validatePatterns(val);
				if (invalidSet != null) {
					ed.set(SPConstants.EXCLUDED_URLS, rb.getString(SPConstants.INVALID_EXCLUDE_PATTERN)
							+ invalidSet.toString());
					return false;
				}
			} else if (collator.equals(key, SPConstants.AUTHORIZATION)) {
				feedType = val;
			} else if(!kdcServer.equalsIgnoreCase(SPConstants.BLANK_STRING) && collator.equals(key,SPConstants.KDC_SERVER)){
				if (kdcServer.indexOf(".")==-1 && !validateIPAddress(kdcServer)) {
					ed.set(SPConstants.KDC_SERVER, rb.getString(SPConstants.KERBEROS_KDC_HOST_BLANK));
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
			sharepointClientContext = new SharepointClientContext(
					sharepointUrl, domain, kdcServer, username, password, "", includeURL,
					excludeURL, mySiteUrl, "", feedType);
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to create SharePointClientContext with the received configuration values. ");
		}
		String status = checkPattern(sharepointUrl);
		if (status != null) {
			ed.set(null, rb.getString(SPConstants.SHAREPOINT_URL) + " "
					+ status);
			return false;
		}
		status = null;

		if (SPConstants.CONTENT_FEED.equalsIgnoreCase(feedType)) {
			status = checkGSConnectivity(sharepointUrl);
			if (!SPConstants.CONNECTIVITY_SUCCESS.equalsIgnoreCase(status)) {
				ed.set(null, rb.getString(SPConstants.BULKAUTH_ERROR_CRAWL_URL)
						+ rb.getString(SPConstants.REASON) + status);
				return false;
			}
		}
		status = null;

		status = checkConnectivity(sharepointUrl);
		if (!SPConstants.CONNECTIVITY_SUCCESS.equalsIgnoreCase(status)) {
			ed.set(null, rb.getString(SPConstants.CANNOT_CONNECT)
					+ rb.getString(SPConstants.REASON) + status);
			return false;
		}
		status = null;

		final String SPVersion = sharepointClientContext.checkSharePointType(sharepointUrl);
		if (SPConstants.SP2007.equals(SPVersion) && (mySiteUrl != null)
				&& !mySiteUrl.equals(SPConstants.BLANK_STRING)) {
			if (!isURL(mySiteUrl)) {
				ed.set(SPConstants.MYSITE_BASE_URL, rb.getString(SPConstants.MALFORMED_MYSITE_URL));
				return false;
			}
			if (!isInFQDN(mySiteUrl)) {
				ed.set(SPConstants.MYSITE_BASE_URL, rb.getString(SPConstants.REQ_FQDN_MYSITE_URL));
				return false;
			}

			status = checkPattern(mySiteUrl);
			if (status != null) {
				ed.set(null, rb.getString(SPConstants.MYSITE_BASE_URL) + " "
						+ status);
				return false;
			}
			status = null;

			status = checkConnectivity(mySiteUrl);
			if (!SPConstants.CONNECTIVITY_SUCCESS.equalsIgnoreCase(status)) {
				ed.set(SPConstants.MYSITE_BASE_URL, rb.getString(SPConstants.CANNOT_CONNECT_MYSITE)
						+ rb.getString(SPConstants.REASON) + status);
				return false;
			}

			if (SPConstants.CONTENT_FEED.equalsIgnoreCase(feedType)) {
				status = checkGSConnectivity(mySiteUrl);
				if (!SPConstants.CONNECTIVITY_SUCCESS.equalsIgnoreCase(status)) {
					ed.set(SPConstants.MYSITE_BASE_URL, rb.getString(SPConstants.BULKAUTH_ERROR_MYSITE_URL)
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
	private ConfigureResponse makeValidatedForm(final Map configMap,
			final ErrorDignostics ed) {
		final String sFunName = className
		+ ".makeValidatedForm(final Map configMap, ErrorDignostics ed)";
		if (configMap == null) {
			LOGGER.warning(sFunName + ": configMap is not found");
			if (rb != null) {
				return new ConfigureResponse(
						rb.getString("CONFIGMAP_NOT_FOUND"), "");
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
	public ConfigureResponse getPopulatedConfigForm(final Map configMap,
			final Locale locale) {
		LOGGER.config("Locale " + locale);

		collator = Util.getCollator(locale);
		rb = ResourceBundle.getBundle("SharepointConnectorResources", locale);
		setConfigStrings();
		final ConfigureResponse result = new ConfigureResponse("",
				makeConfigForm(configMap, null));

		return result;
	}

	/**
	 * Called by connector-manager to validate the connector configuration
	 * values.
	 */
	// due to GCM changes
	public ConfigureResponse validateConfig(final Map configData,
			final Locale locale, final ConnectorFactory arg2) {
		return this.validateConfig(configData, locale);
	}

	public ConfigureResponse validateConfig(final Map configData,
			final Locale locale) {
		LOGGER.config("Locale " + locale);

		rb = ResourceBundle.getBundle("SharepointConnectorResources", locale);
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
	 * @param buf contains the Form snippet
	 */
	private void addJavaScript(final StringBuffer buf) {
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
				+ "\r\n if(leftBoxVal.search("
				+ SPConstants.SOURCE_ALIAS_SEPARATOR
				+ ")!=-1 || rightBoxVal.search("
				+ SPConstants.SOURCE_ALIAS_SEPARATOR
				+ ")!=-1 || leftBoxVal.search("
				+ SPConstants.ALIAS_ENTRIES_SEPARATOR
				+ ")!=-1 || rightBoxVal.search("
				+ SPConstants.ALIAS_ENTRIES_SEPARATOR
				+ ")!=-1) {continue;}"
				+ "\r\n aliasString += leftBoxVal + '"
				+ SPConstants.SOURCE_ALIAS_SEPARATOR
				+ "' + rightBoxVal + '"
				+ SPConstants.ALIAS_ENTRIES_SEPARATOR + "';" + "\r\n }"
				+ "\r\n document.getElementById('" + SPConstants.ALIAS_MAP
				+ "').value=aliasString;" + "\r\n }";

			js += "\r\n function trim(s) {return s.replace( /^\\s*/, \"\" ).replace( /\\s*$/, \"\" );}";

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
			 * Check for port value greater then allowed range is added beacuse
			 * of a bug in Axis where a NullPointer Exception was being returned
			 * as a vlaue during Web Service call.
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
			if ((excludeList != null)
					&& Util.match(excludeList, url, matchedPattern)) {
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
	 * @param patterns The pattern to be validated
	 * @return the set of wrong patterns, if any. Otherwise returns null
	 */
	private Set validatePatterns(final String patterns) {
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
				final String strContainKey = new String(
						tempBuffer.delete(0, SPConstants.CONTAINS.length()));
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
				final String strRegexPattrn = new String(
						tempBuffer.delete(0, SPConstants.REGEXP.length()));
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
				final String strRegexCasePattrn = new String(
						tempBuffer.delete(0, SPConstants.REGEXP_CASE.length()));
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
				final String strRegexIgnoreCasePattrn = new String(
						tempBuffer.delete(0, SPConstants.REGEXP_IGNORE_CASE.length()));
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
						tempBuffer.replace(indexOfStar, indexOfStar
								+ "*".length(), "[0-9].*");
					} else {
						tempBuffer.delete(0, "^".length());
						if (pattern.endsWith(SPConstants.DOLLAR)) {
							bDollar = true;
							tempBuffer.delete(tempBuffer.length()
									- SPConstants.DOLLAR.length(), tempBuffer.length());
						}
						try {
							final URL urlPatt = new URL(tempBuffer.toString());
							final int port = urlPatt.getPort();
							final String strHost = urlPatt.getHost().toString();
							if ((port == -1) && (strHost != null)
									&& (strHost.length() != 0)) {
								tempBuffer = new StringBuffer("^"
										+ urlPatt.getProtocol()
										+ SPConstants.URL_SEP
										+ urlPatt.getHost() + ":[0-9].*"
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
					urlPatt1stPart = "^" + urlPatt.getProtocol()
					+ SPConstants.URL_SEP + urlPatt.getHost()
					+ SPConstants.COLON + strPort;
					if (!(urlPatt.getFile()).startsWith(SPConstants.SLASH)) { // The
						// pattern
						// must
						// have
						// "/"
						// at
						// after
						// the
						// port
						invalidPatterns.add(pattern);
					}
					urlPatt2ndPart = "^" + urlPatt.getFile();
				} catch (final Exception e) {
					bPortStar = true;
				}

				if (bPortStar) {
					final int indexOfStar = patternDecoded.indexOf("*");
					if (indexOfStar != -1) {
						urlPatt1stPart = "^"
							+ patternDecoded.substring(0, indexOfStar)
							+ "[0-9].*";
						if (!(patternDecoded.substring(indexOfStar + 1)).startsWith(SPConstants.SLASH)) {
							invalidPatterns.add(pattern);
						}
						urlPatt2ndPart = "^"
						+ patternDecoded.substring(indexOfStar + 1);
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
						urlPatt1stPart = patternDecoded.substring(0, patternDecoded.indexOf(SPConstants.SLASH))
						+ ":[0-9].*";
					} else {
						urlPatt1stPart = patternDecoded.substring(0, patternDecoded.indexOf(SPConstants.SLASH));
					}
					urlPatt2ndPart = patternDecoded.substring(patternDecoded.indexOf(SPConstants.SLASH));
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
	 * Function Signature changed by nitendra_thakur. This is to make the
	 * function re-usable.
	 */
	private String checkConnectivity(final String endpoint) {
		LOGGER.config("Checking connectivity for [" + endpoint + "]");

		if ((endpoint == null) || !isURL(endpoint)) {
			return rb.getString(SPConstants.ENDPOINT_NOT_FOUND);
		}

		try {
			sharepointClientContext.setSiteURL(endpoint);
			final WebsWS websWS = new WebsWS(sharepointClientContext);
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
	 * @param endpoint the Web URL to which the Web Service call will be made
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
			final GSBulkAuthorizationWS testBulkAuth = new GSBulkAuthorizationWS(
					sharepointClientContext);
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
	 * @param aliasMapString The comples string containg all the entries made on
	 *            the configuration form. Entries of two consecutive rows are
	 *            separated by /$$CRLF$$/ A Source pattern is separated by its
	 *            corresponding Alias pattern by /$$EQUAL$$/
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
			final String[] alias_map = aliasMapString.split(SPConstants.ALIAS_ENTRIES_SEPARATOR);
			for (final String nextEntry : alias_map) {
				if ((nextEntry == null)
						|| nextEntry.equals(SPConstants.BLANK_STRING)) {
					continue;
				} else {
					try {
						final String[] alias_entry = nextEntry.split(SPConstants.SOURCE_ALIAS_SEPARATOR);
						if (alias_entry.length != 2) {
							LOGGER.warning("Skipping alias entry [ "
							                                       + nextEntry
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
								aliases = ((ArrayList<String>) (aliasMap.get(source_url)));
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
	 * Validates the String to check whether it represents an IP address or not and returns the boolean status.
	 * @param ip IP adress to be validated in the form of string.
	 * @return If ip address matches the regular expression then true else false is returned.
	 */
	private boolean validateIPAddress (String ip){
		if(ip.matches("[0-255]+.[0-255]+.[0-255]+.[0-255]+"))
			return true;
		else return false;
	}

	/**
	 *	All the initial set-up and pre-requisites for Kerberos Authentication. Following are the responsibilities:
	 *		- If KDC Host is provided on UI configuration then the Negotiate AuthScheme is registered with AuthPolicy of Httpclient.
	 *		- krb5.conf and login.conf files are copied to the connector instance's directory.
	 *		- Values of KDC Server and Realm are changed at runtime in krb5.conf.
	 *		- System properties required for the Kerberos AuthN are set. 
	 */
	private void kerberosSetUp(final Map configData){
		String kdcServer = configData.get(SPConstants.KDC_SERVER).toString();
		String googleConnWorkDir = (String)configData.get(GOOGLE_CONN_WORK_DIR);

		if(!kdcServer.equalsIgnoreCase(SPConstants.BLANK_STRING)){
			AuthPolicy.registerAuthScheme(SPConstants.NEGOTIATE, NegotiateScheme.class);

			InputStream krb5In = SharepointConnectorType.class.getClassLoader().getResourceAsStream(SPConstants.CONFIG_KRB5);
			if(krb5In != null){
				try {
					File krb5File = new File(googleConnWorkDir + SPConstants.DOUBLEBACKSLASH + SPConstants.FILE_KRB5); 
					String krb5Config = StringUtils.streamToStringAndThrow(krb5In);
					krb5Config = krb5Config.replace(SPConstants.VAR_KRB5_REALM_UPPERCASE, configData.get(SPConstants.DOMAIN).toString().toUpperCase());
					krb5Config = krb5Config.replace(SPConstants.VAR_KRB5_REALM_LOWERCASE, configData.get(SPConstants.DOMAIN).toString().toLowerCase());
					krb5Config = krb5Config.replace(SPConstants.VAR_KRB5_KDC_SERVER, configData.get(SPConstants.KDC_SERVER).toString().toUpperCase());
					FileOutputStream out = new FileOutputStream(krb5File);
					out.write(krb5Config.getBytes(SPConstants.UTF_8));
					out.close();
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "Failed to create krb5.conf in connector instance's directory.");
				}
			}

			InputStream loginIn = SharepointConnectorType.class.getClassLoader().getResourceAsStream(SPConstants.CONFIG_LOGIN);
			if(loginIn != null){
				try {
					File loginFile = new File(googleConnWorkDir + SPConstants.DOUBLEBACKSLASH + SPConstants.FILE_LOGIN); 
					String loginConfig = StringUtils.streamToStringAndThrow(loginIn);
					FileOutputStream out = new FileOutputStream(loginFile);
					out.write(loginConfig.getBytes(SPConstants.UTF_8));
					out.close();
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "Failed to create login.conf in connector instance's directory.");
				}
			}

			System.setProperty(SPConstants.SYS_PROP_AUTH_LOGIN_CONFIG, googleConnWorkDir + SPConstants.DOUBLEBACKSLASH + SPConstants.FILE_LOGIN);
			System.setProperty(SPConstants.SYS_PROP_AUTH_KRB5_CONFIG, googleConnWorkDir + SPConstants.DOUBLEBACKSLASH + SPConstants.FILE_KRB5);
			System.setProperty(SPConstants.SYS_PROP_AUTH_USESUBJETCREDSONLY, SPConstants.FALSE);
		}
	}
}
