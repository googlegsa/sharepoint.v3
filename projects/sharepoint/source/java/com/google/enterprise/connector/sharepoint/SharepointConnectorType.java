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

package com.google.enterprise.connector.sharepoint; 

import java.net.MalformedURLException;
import java.text.Collator;
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

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SharepointClientUtils;
import com.google.enterprise.connector.sharepoint.client.SharepointException;
import com.google.enterprise.connector.sharepoint.client.SiteDataWS;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * ConnectorType implementation for Sharepoint.
 * @author amit_kagrawal
 * */
public class SharepointConnectorType implements ConnectorType {
	private static final Logger LOGGER = Logger.getLogger(SharepointConnectorType.class.getName());
	private String className = SharepointConnectorType.class.getName();
	private static final String VALUE = "value";
	private static final String NAME = "name";
	private static final String TEXT = "text";
	private static final String TEXTAREA = "textarea"; 
	private static final String TYPE = "type";
	private static final String INPUT = "input";
	private static final String CLOSE_ELEMENT = ">";
	private static final String OPEN_ELEMENT = "<";
	private static final String PASSWORD = "password";
	private static final String TR_END = "</tr>\r\n";
	private static final String TD_END = "</td>\r\n";
	private static final String TD_START = "<td style='white-space: nowrap'>";
	private static final String TR_START = "<tr valign='top'>\r\n";
	private static final String READONLY = "readonly";
	private static final String TRUE = "true";
//	private static final String TR_START_FOR_SP2007_BLOCK = "<tr valign='top' id='sp2007' name='sp2007' style=\"display:block;\">\r\n";
//	private static final String TR_START_FOR_SP2007_NONE = "<tr valign='top' id='sp2007' name='sp2007' style=\"display:none;\">\r\n";
//	private static String trStartForSP2007 = TR_START_FOR_SP2007_BLOCK;

	private static final String  ROWS= "rows";	
	private static final String ROWS_VALUE = "5";	
	private static final String  COLS= "cols";	
	private static final String COLS_VALUE = "50";	
	private static final String END_TEXTAREA = "/textarea";
	private static final String START_BOLD = "<b>";
	private static final String END_BOLD = "</b>";
	private static final String MANDATORY_FIELDS = "Mandatory_Fields" ;  
	private static final String  TEXTBOX_SIZE= "size";	
	private static final String TEXTBOX_SIZE_VALUE = "50";	

	private static final String USERNAME = "username";
	private static final String DOMAIN = "domain";
	private static final String SHAREPOINT_URL = "sharepointUrl";
	private static final String EXCLUDED_URLS  = "excludedURls";   
	private static final String INCLUDED_URLS  = "includedURls"; 
	private static final String MYSITE_BASE_URL  = "mySiteBaseURL"; 
	private static final String ALIAS_HOST_NAME  = "aliasHostName"; 
	private static final String ALIAS_PORT  = "aliasPort";
	private static final String SP_CONNECTOR_TYPE = "SPType";
	public static final String SP2003 = "sp2003";
	public static final String SP2007 = "sp2007";


	private static final String REQ_FIELDS_MISSING = 
		"Field_Is_Required";
	private static final String REQ_FQDN_URL = 
		"Url_Entered_Should_Be_Fully_Qualified";
	private static final String REQ_FQDN_ALIAS_HOSTNAME = 
		"AliasHostName_Entered_Should_Be_Fully_Qualified";
	private static final String CANNOT_CONNECT ="Cannot_Connect";
	private static final String BLANK_STRING = ""; 
	private static Collator collator = null;
	private static final String CRAWL_URL_PATTERN_MISMATCH = "CrawlURL_Pattern_Mismatch";

	private String sharepointUrl = null;
	private String domain = null ;
	private String username = null;
	private String password = null;
	private String includeURL = null;
	private String sharepointType = null;
	private String excludeURL = null;

	private List keys = null;
	private Set keySet = null;
	private final HashMap configStrings = new HashMap();
	private String initialConfigForm = null;

	/*static{
//		set an external configuration file for controlling logging
		System.setProperty("java.util.logging.config.file","logconfig.properties");
	}*/

	/**
	 * Sets the keys that are required for configuration. These are the actual 
	 * keys used by the class. 
	 * @param inKeys A list of String keys
	 */
	public void setConfigKeys(final List inKeys) {
		String sFuncName = "setConfigKeys(final List inKeys)";
		LOGGER.entering(className, sFuncName);
		if (this.keys != null) {
			throw new IllegalStateException();
		}
		if(inKeys!=null){
			this.keys = inKeys;
			this.keySet = new HashSet(inKeys);
		}
		LOGGER.exiting(className, sFuncName);
	}  

	/**
	 * Sets the display strings for the configuration form depending on the 
	 * language settings.
	 * @param rb Resource bundle for the particular language
	 */
	private void setConfigStrings(final ResourceBundle rb) {
		final String sFunctionName = "setConfigStrings(ResourceBundle rb)";
		LOGGER.entering(className, sFunctionName);
		if(rb!=null){
			//checking the parameters of the resource bundle passed
			for(int iKey=0;iKey<keys.size();++iKey){
				final Object key = keys.get(iKey);
				LOGGER.config(sFunctionName +" [ KEY: "+key.toString()+" | VALUE: "+ rb.getString(key.toString())+"]");
				configStrings.put(key, rb.getString((String)key));     
			}
			configStrings.put(MANDATORY_FIELDS, rb.getString(MANDATORY_FIELDS));
			configStrings.put(SP2003, rb.getString(SP2003));
			configStrings.put(SP2007, rb.getString(SP2007));
			configStrings.put(SP_CONNECTOR_TYPE, rb.getString(SP_CONNECTOR_TYPE));
		}else{
			LOGGER.warning(sFunctionName+": unable to get resource bundle");
		}
		LOGGER.exiting(className, sFunctionName);
	}

	/**
	 * Gets the initial/blank form.
	 * @return HTML form as string
	 */
	private String getInitialConfigForm() {
		String sFuncName = "getInitialConfigForm()";
		LOGGER.entering(className, sFuncName);
		if (initialConfigForm != null) {
			return initialConfigForm;
		}
		if (keys == null) {
			throw new IllegalStateException();
		}
		this.initialConfigForm = makeConfigForm(null);
		LOGGER.exiting(className, sFuncName);
		return initialConfigForm;
	}

	/**
	 * Makes a config form snippet using the keys (in the supplied order) and, if
	 * passed a non-null config map, pre-filling values in from that map.
	 * 
	 * @param configMap
	 * @return config form snippet
	 */
	private String makeConfigForm(final Map configMap) {
		final String sFunctionName = "makeConfigForm(Map configMap)";
		LOGGER.entering(className, sFunctionName);
		if(collator == null){
			collator = getCollator();
		}
		final StringBuffer buf = new StringBuffer();
		boolean isSP2007 = true;
		if (configMap != null) {
			final String value = (String) configMap.get(SP_CONNECTOR_TYPE);
			if(value != null && !collator.equals(SP2007, value)){
				//	trStartForSP2007 = TR_START_FOR_SP2007_NONE;
				isSP2007 = false;
			}else{
				//	trStartForSP2007 = TR_START_FOR_SP2007_BLOCK;
				isSP2007 = true;
			}
		}
		addJavaScript(buf);


		if(keys!=null){
			for (final Iterator i = keys.iterator(); i.hasNext();) {
				final String key = (String) i.next();
				final String configKey = (String) configStrings.get(key);


				if(!collator.equals(key,SP_CONNECTOR_TYPE)) {

					appendStartRow(buf,key, configKey, false);

					buf.append(OPEN_ELEMENT);
					if(!((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS)))){
						buf.append(INPUT);
					}else{  
						buf.append(TEXTAREA); 
					}
					if (collator.equals(key,PASSWORD)) {
						appendAttribute(buf, TYPE, PASSWORD);
					} if((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS))){ 
						appendAttribute(buf,ROWS , ROWS_VALUE);
						appendAttribute(buf,COLS , COLS_VALUE);
					}else {
						appendAttribute(buf, TYPE, TEXT);
						if(!((collator.equals(key,USERNAME)) || (collator.equals(key,PASSWORD))||(collator.equals(key,ALIAS_PORT)))){
							appendAttribute(buf, TEXTBOX_SIZE, TEXTBOX_SIZE_VALUE);
						}
					}
					appendAttribute(buf, NAME, key);
					if((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS))){ 
						buf.append(CLOSE_ELEMENT);
					}else if(collator.equals(key,MYSITE_BASE_URL)){
						appendAttribute(buf, "id", SP2007);
					if(!isSP2007){
							appendAttribute(buf, READONLY, TRUE);
						}
					}
					if (configMap != null) {
						final String value = (String) configMap.get(key);
						if (value != null) {
							if((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS))){ 
								buf.append(value);	
							}else {
								appendAttribute(buf, VALUE, value);
							}
						}
					}

					if((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS))){ 
						buf.append(OPEN_ELEMENT);
						buf.append(END_TEXTAREA);
						buf.append(CLOSE_ELEMENT);
						buf.append(TD_END);
						buf.append(TR_END);
					}else {
						appendEndRow(buf);
					}
				}else {
					String strSP2003 = (String) configStrings.get(SP2003);
					String strSP2007 = (String) configStrings.get(SP2007);
					String strSPConnectorType = (String) configStrings.get(SP_CONNECTOR_TYPE);
					if(isSP2007 == true){
						buf.append("<tr> <td> <div style='float: left;'>"+strSPConnectorType+"</div> </td>");
						buf.append("<td><select id=\""+key+"\" name=\""+key+"\" size=\"1\" onchange=\"loadChanged();\"><option selected value=\""+SP2007+"\" >");
						buf.append(strSP2007);
						buf.append("</option><option value=\""+SP2003+"\">");
						buf.append(strSP2003);
						buf.append("</option></select> </td> </tr>");
					}else {
						buf.append("<tr> <td> <div style='float: left;'>"+strSPConnectorType+"</div> </td>");
						buf.append("<td><select id=\""+key+"\" name=\""+key+"\" size=\"1\" onchange=\"loadChanged();\"><option selected value=\""+SP2003+"\" >");
						buf.append(strSP2003);
						buf.append("</option><option value=\""+SP2007+"\">");
						buf.append(strSP2007);
						buf.append("</option></select> </td> </tr>");
					}


				}
			}

		}//end null check

		buf.append("<tr valign='top' ><input type=\"hidden\" id='"+SP2003+"'/></tr>");
//		if(isSP2007 == true){
//			buf.append("<tr valign='top' id='"+SP2003+"' name='"+SP2003+"' style=\"display:none;\"><input type=\"hidden\" /></tr> \r\n");
//		}else {
//			buf.append("<tr valign='top' id='"+SP2003+"' name='"+SP2003+"' style=\"display:block;\"><input type=\"hidden\" /></tr> \r\n");
//		}
		buf.append(START_BOLD);
		buf.append(configStrings.get(MANDATORY_FIELDS));
		buf.append(END_BOLD);
		LOGGER.config(sFunctionName+" : configform ["+buf.toString()+"]");
		LOGGER.exiting(className, sFunctionName);
		return buf.toString();
	}

	private void appendStartRow(final StringBuffer buf, final String key, final String configKey,final boolean red) {
		String sFunctionName = "appendStartRow(final StringBuffer buf, final String key, final String configKey,final boolean red)";
		LOGGER.entering(className, sFunctionName);
		if(!collator.equals(key,MYSITE_BASE_URL)) {
			buf.append(TR_START);
		} else {
			buf.append(TR_START);
//			buf.append(trStartForSP2007);
		}
		buf.append(TD_START);
		if (red) {
			buf.append("<font color=red>");
		}
		if(isRequired(key)){
			buf.append("<div style='float: left;'>");
			buf.append(START_BOLD);
		}
		buf.append(configKey);
		if(isRequired(key)){

			buf.append(END_BOLD);
			buf.append("</div><div style='text-align: right; ").
			append("color: red; font-weight: bold; ").
			append("margin-right: 0.3em;\'>*</div>");
		}
		if (red) {
			buf.append("</font>");
		}
		buf.append(TD_END);
		buf.append(TD_START);
		LOGGER.exiting(className, sFunctionName);
	}

	private void appendEndRow(final StringBuffer buf) {
		String sFunctionName = "appendEndRow(final StringBuffer buf)";
		LOGGER.entering(className, sFunctionName);
		buf.append("/"+CLOSE_ELEMENT);
		buf.append(TD_END);
		buf.append(TR_END);
		LOGGER.exiting(className, sFunctionName);
	}

	private void appendAttribute(final StringBuffer buf, final String attrName,
			final String attrValue) {
		String sFunctionName = "appendAttribute(final StringBuffer buf, final String attrName,final String attrValue)";
		LOGGER.entering(className, sFunctionName);
		buf.append(" ");
		buf.append(attrName);
		buf.append("=\"");
		// TODO xml-encode the special characters (< > " etc.)
		buf.append(attrValue);
		buf.append("\"");
		LOGGER.exiting(className, sFunctionName);
	}

	private void setSharepointCredentials(final String key, final String val) {
		String sFunctionName = "setSharepointCredentials(final String key, final String val)";
		LOGGER.entering(className, sFunctionName);
		if(collator == null){
			collator = getCollator();
		}  
		if (collator.equals(key,USERNAME)) {
			username = val;
		} else if (collator.equals(key,DOMAIN)) {
			domain = val;
		} else if (collator.equals(key,PASSWORD)) {
			password = val;
		} else if (collator.equals(key,SHAREPOINT_URL)) {
			sharepointUrl = val;
		}else if (collator.equals(key,INCLUDED_URLS)) {  
			includeURL = val;
		}else if (collator.equals(key,SP_CONNECTOR_TYPE)) {  
			sharepointType= val;
		}else if (collator.equals(key,EXCLUDED_URLS)) {  
			excludeURL = val;
		}
		LOGGER.exiting(className, sFunctionName);
	}

	private String getErrorMessage(final String configKey, final String val, 
			final ResourceBundle rb) { 
		String sFunctionName = "getErrorMessage(final String configKey, final String val,final ResourceBundle rb)";
		LOGGER.entering(className, sFunctionName);
		if (val == null || val.length() == 0) {
			return rb.getString(REQ_FIELDS_MISSING) + " " + configKey;
		}      
		//if (val.startsWith("http://") && !val.contains(".")) {
		if (val.startsWith("http://") && (val.indexOf(".")==-1)/*!val.contains(".")*/) {
			return rb.getString(REQ_FQDN_URL);      
		}
		LOGGER.exiting(className, sFunctionName);
		return null;    
	}

	private String checkConnectivity() {
		final String sFunctionName = "checkConnectivity()";
		LOGGER.entering(className, sFunctionName);
		final SharepointClientContext sharepointClientContext = new SharepointClientContext(sharepointType,sharepointUrl, domain, username, 
				password, null,includeURL,null,null,null,null,null,null); // modified by A Mitra
		try {
			final SiteDataWS siteDataWS = new SiteDataWS(sharepointClientContext);
			if(siteDataWS==null){
				throw new SharepointException(sFunctionName+": sitedata stub is not found");
			}
			siteDataWS.getAllChildrenSites();			
		} catch (final SharepointException e) {      
			LOGGER.warning(sFunctionName+":"+ e.toString());
			return CANNOT_CONNECT;
		} catch (final RepositoryException e) {              
			LOGGER.warning(sFunctionName+":"+ e.toString());
			return CANNOT_CONNECT;
		}
		LOGGER.exiting(className, sFunctionName);
		return null;
	}

	/**
	 * Validates a given map, i.e., checks if a value if null or zero length.
	 * It also checks if the value is a url, then it should be fully qualified.
	 * @param configData Map of keys and values
	 * @return message string depending on the validation.
	 */
	private boolean validateConfigMap(final Map configData, final ResourceBundle rb) {
		final String sFunctionName="validateConfigMap(final Map configData, final ResourceBundle rb)";
		LOGGER.entering(className, sFunctionName);
		if(configData==null){
			LOGGER.warning(sFunctionName+": configData map is not found");
			return false;
		}

//		final String value = (String) configData.get(SP_CONNECTOR_TYPE);
//		if((value != null && value != BLANK_STRING) && !collator.equals(SP2007, value)){
//		configData.put(MYSITE_BASE_URL, BLANK_STRING);			
//		}
		String message = null;
		for (final Iterator i = keys.iterator(); i.hasNext();) {
			final String key = (String) i.next();
			final String val = (String) configData.get(key);
			setSharepointCredentials(key, val);
			if (isRequired(key)){ 
				message = getErrorMessage(key, val , rb);

			} else if((val != null && val != BLANK_STRING) && collator.equals(key,ALIAS_HOST_NAME)){
				message = isFQDN(key, val, rb);
			} else if (val ==null){  
				configData.put(key, BLANK_STRING);
			}
			if (message != null) {
				return false;
			}
		}

		if ((sharepointUrl != null) && (includeURL != null)) {
			message = checkPattern(rb);
			if (message != null) {
				return false;
			}
		}

		if ((sharepointUrl != null) && (domain != null) && (username != null) 
				&& (password !=null) && (includeURL != null)) {
			message = checkConnectivity();
			if (message != null) {
				return false;
			}
		}
		LOGGER.exiting(className, sFunctionName);
		return true;
	}

	private ConfigureResponse makeValidatedForm(final Map configMap, final ResourceBundle rb) 
	{
		final String sFunName="makeValidatedForm(final Map configMap, final ResourceBundle rb)";
		LOGGER.entering(className, sFunName);
		if(configMap==null){
			LOGGER.warning(sFunName+": configMap is not found");
			if(rb!=null){
				return new ConfigureResponse(rb.getString("CONFIGMAP_NOT_FOUND"),"");
			}else{
				return new ConfigureResponse("resource bundle not found","");
			}
		}
		final StringBuffer buf = new StringBuffer(2048);

		String strSP2003 = (String) configStrings.get(SP2003);
		String strSP2007 = (String) configStrings.get(SP2007);
		boolean isSP2007 = true;
		String finalMessage = null;

		if(buf!=null){
			addJavaScript(buf);

			String value = (String) configMap.get(SP_CONNECTOR_TYPE);
			if(value != null && !collator.equals(SP2007, value)){
//				trStartForSP2007 = TR_START_FOR_SP2007_NONE;
				isSP2007 = false;
			}else{
//				trStartForSP2007 = TR_START_FOR_SP2007_BLOCK;
				isSP2007 = true;
			}

			String message = null;


			for (final Iterator i = keys.iterator(); i.hasNext();) {
				final String key = (String) i.next();
				final String configKey = (String) configStrings.get(key);
				value = (String) configMap.get(key);


				/*if(!collator.equals(SP2007, value)) {
					TR_START_FOR_SP2007 = TR_START_FOR_SP2007_NONE;
					isSP2007 = false;
				}else{
					TR_START_FOR_SP2007 = TR_START_FOR_SP2007_BLOCK;
					isSP2007 = true;
				}*/
				message = null;
				if (finalMessage == null) {
					if (isRequired(key)){
						message = getErrorMessage(configKey, value, rb);
						finalMessage = message;
					}else if((value != null && value != BLANK_STRING) && collator.equals(key,ALIAS_HOST_NAME)){
						message = isFQDN(key, value, rb);
						finalMessage = message;
					}
				}      
				setSharepointCredentials(key, value); 
				if(!collator.equals(key,SP_CONNECTOR_TYPE)) {
					if (message == null) {
						appendStartRow(buf,key,configKey, false);
						buf.append(OPEN_ELEMENT);
						if(!((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS)))){   
							buf.append(INPUT);
						}else{  
							buf.append(TEXTAREA); 
						}
						if (collator.equals(key,PASSWORD)) {
							appendAttribute(buf, TYPE, PASSWORD);
						}if(((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS)))){ 
							appendAttribute(buf,ROWS , ROWS_VALUE);
							appendAttribute(buf,COLS , COLS_VALUE);

						} else {
							appendAttribute(buf, TYPE, TEXT);
							if(!((collator.equals(key,USERNAME)) || (collator.equals(key,PASSWORD))||(collator.equals(key,ALIAS_PORT)))){
								appendAttribute(buf, TEXTBOX_SIZE, TEXTBOX_SIZE_VALUE);
							}
							appendAttribute(buf, VALUE, value);

						}                
					} else {
						appendStartRow(buf,key, configKey, true);
						buf.append(OPEN_ELEMENT);
						if(!((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS)))){   
							buf.append(INPUT);
						}else{  
							buf.append(TEXTAREA); 
						}
						if (collator.equals(key,PASSWORD)) {
							appendAttribute(buf, TYPE, PASSWORD);
						} if(((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS)))){ 
							appendAttribute(buf,ROWS , ROWS_VALUE);
							appendAttribute(buf,COLS , COLS_VALUE);

						}else {
							appendAttribute(buf, TYPE, TEXT);
							if(!((collator.equals(key,USERNAME)) || (collator.equals(key,PASSWORD))||(collator.equals(key,ALIAS_PORT)))){
								appendAttribute(buf, TEXTBOX_SIZE, TEXTBOX_SIZE_VALUE);
							}
						}
					}
					/*appendAttribute(buf, NAME, key);
					if(((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS)))){ 
						buf.append(CLOSE_ELEMENT);
						buf.append(value);
						buf.append(OPEN_ELEMENT);
						buf.append(END_TEXTAREA);

					}

					appendEndRow(buf);
					 */
					appendAttribute(buf, NAME, key);
					if((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS))){ 
						buf.append(CLOSE_ELEMENT);
					}else if(collator.equals(key,MYSITE_BASE_URL)){
						appendAttribute(buf, "id", SP2007);
						if(!isSP2007){
							appendAttribute(buf, READONLY, TRUE);
						}
					}


					if (value != null) {
						if((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS))){ 
							buf.append(value);	
						}else {
							appendAttribute(buf, VALUE, value);
						}
					}


					if((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS))){ 
						buf.append(OPEN_ELEMENT);
						buf.append(END_TEXTAREA);
						buf.append(CLOSE_ELEMENT);
						buf.append(TD_END);
						buf.append(TR_END);
					}else {
						appendEndRow(buf);
					}

				} else {

					String strSPConnectorType = (String) configStrings.get(SP_CONNECTOR_TYPE);
					if(isSP2007 == true) {

						buf.append("<tr> <td> <div style='float: left;'>"+strSPConnectorType+"</div> </td>");
						buf.append("<td><select id=\""+key+"\" name=\""+key+"\" size=\"1\" onchange=\"loadChanged();\"><option selected value=\""+SP2007+"\" >");
						buf.append(strSP2007);
						buf.append("</option><option value=\""+SP2003+"\">");
						buf.append(strSP2003);
						buf.append("</option></select> </td> </tr>");
					} else {

						buf.append("<tr> <td> <div style='float: left;'>"+strSPConnectorType+"</div> </td>");
						buf.append("<td><select id=\""+key+"\" name=\""+key+"\" size=\"1\" onchange=\"loadChanged();\"><option selected value=\""+SP2003+"\" >");
						buf.append(strSP2003);
						buf.append("</option><option value=\""+SP2007+"\">");
						buf.append(strSP2007);
						buf.append("</option></select> </td> </tr>");
					}
				}
			}
			
			buf.append("<tr valign='top' ><input type=\"hidden\" id='"+SP2003+"'/></tr>");

//			if(isSP2007 == true) {
//
//				buf.append("<tr valign='top' id='"+SP2003+"' name='"+SP2003+"' style=\"display:none;\"><input type=\"hidden\" /></tr> \r\n");
//			}else{
//
//				buf.append("<tr valign='top' id='"+SP2003+"' name='"+SP2003+"' style=\"display:block;\"><input type=\"hidden\" /></tr> \r\n");
//			}
			buf.append(START_BOLD);
			buf.append(configStrings.get(MANDATORY_FIELDS));
			buf.append(END_BOLD);



			if (finalMessage == null) {
				if(rb!=null){
					String errorMessage =  checkConnectivity();
					if(errorMessage!=null){
						finalMessage = rb.getString(errorMessage);
					}
				}
				if (finalMessage != null) {
					buf.setLength(0);
					if((keys!=null) && (configStrings!=null)){
						addJavaScript(buf);


						for (final Iterator i = keys.iterator(); i.hasNext();) {
							final String key = (String) i.next();
							final String configKey = (String) configStrings.get(key);
							value = (String) configMap.get(key);

							if(!collator.equals(key,SP_CONNECTOR_TYPE)) {
								appendStartRow(buf, key,configKey, false);
								buf.append(OPEN_ELEMENT);
								if(!((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS)))){  
									buf.append(INPUT);
								}else{  
									buf.append(TEXTAREA); 
								}
								if (key.equalsIgnoreCase(PASSWORD)) {
									appendAttribute(buf, TYPE, PASSWORD);
								} if(((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS)))){ 
									appendAttribute(buf,ROWS , ROWS_VALUE);
									appendAttribute(buf,COLS , COLS_VALUE);
								}else {
									appendAttribute(buf, TYPE, TEXT);
									//do not increase size for username, password and port
									if(!((collator.equals(key,USERNAME)) || (collator.equals(key,PASSWORD))||(collator.equals(key,ALIAS_PORT)))){
										appendAttribute(buf, TEXTBOX_SIZE, TEXTBOX_SIZE_VALUE);
									}
								}

								/*appendAttribute(buf, NAME, key);
								if(((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS)))){ 
									buf.append(CLOSE_ELEMENT);
									buf.append(OPEN_ELEMENT);
									buf.append(END_TEXTAREA);

								}
								appendEndRow(buf);*/

								appendAttribute(buf, NAME, key);
								if((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS))){ 
									buf.append(CLOSE_ELEMENT);
								}else if(collator.equals(key,MYSITE_BASE_URL)){
									appendAttribute(buf, "id", SP2007);
									if(isSP2007){
										appendAttribute(buf, READONLY, TRUE);
									}
								}


								if (value != null) {
									if((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS))){ 
										buf.append(value);	
									}else {
										appendAttribute(buf, VALUE, value);
									}
								}


								if((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS))){ 
									buf.append(OPEN_ELEMENT);
									buf.append(END_TEXTAREA);
									buf.append(CLOSE_ELEMENT);
									buf.append(TD_END);
									buf.append(TR_END);
								}else {
									appendEndRow(buf);
								}


							} else {

								String strSPConnectorType = (String) configStrings.get(SP_CONNECTOR_TYPE);
								if(isSP2007 == true) {

									buf.append("<tr> <td> <div style='float: left;'>"+strSPConnectorType+"</div> </td>");
									buf.append("<td><select id=\""+key+"\" name=\""+key+"\" size=\"1\" onchange=\"loadChanged();\"><option selected value=\""+SP2007+"\" >");
									buf.append(strSP2007);
									buf.append("</option><option value=\""+SP2003+"\">");
									buf.append(strSP2003);
									buf.append("</option></select> </td> </tr>");
								} else {

									buf.append("<tr> <td> <div style='float: left;'>"+strSPConnectorType+"</div> </td>");
									buf.append("<td><select id=\""+key+"\" name=\""+key+"\" size=\"1\" onchange=\"loadChanged();\"><option selected value=\""+SP2003+"\" >");
									buf.append(strSP2003);
									buf.append("</option><option value=\""+SP2007+"\">");
									buf.append(strSP2007);
									buf.append("</option></select> </td> </tr>");
								}

							}
						}//end for



					}//if condn
					
					buf.append("<tr valign='top' ><input type=\"hidden\" id='"+SP2003+"'/></tr>");

//					if(isSP2007 == true) {
//
//						buf.append("<tr valign='top' id='"+SP2003+"' name='"+SP2003+"' style=\"display:none;\"><input type=\"hidden\" /></tr> \r\n");
//					}else{
//
//						buf.append("<tr valign='top' id='"+SP2003+"' name='"+SP2003+"' style=\"display:block;\"><input type=\"hidden\" /></tr> \r\n");
//					}

					buf.append(START_BOLD);
					buf.append(configStrings.get(MANDATORY_FIELDS));
					buf.append(END_BOLD);
				}
			} 

			if (finalMessage == null) {
				finalMessage = checkPattern(rb);  
				if (finalMessage != null) {
					buf.setLength(0);

					if((keys!=null) && (configStrings!=null)){
						addJavaScript(buf);


						for (final Iterator i = keys.iterator(); i.hasNext();) {
							final String key = (String) i.next();
							final String configKey = (String) configStrings.get(key);
							value = (String) configMap.get(key);

							if(!collator.equals(key,SP_CONNECTOR_TYPE)) {
								appendStartRow(buf, key,configKey, false);
								buf.append(OPEN_ELEMENT);
								if(!((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS)))){  
									buf.append(INPUT);
								}else{  
									buf.append(TEXTAREA); 
								}
								if (key.equalsIgnoreCase(PASSWORD)) {
									appendAttribute(buf, TYPE, PASSWORD);
								} if(((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS)))){ 
									appendAttribute(buf,ROWS , ROWS_VALUE);
									appendAttribute(buf,COLS , COLS_VALUE);
								}else {
									appendAttribute(buf, TYPE, TEXT);
									//do not increase size for username, password and port
									if(!((collator.equals(key,USERNAME)) || (collator.equals(key,PASSWORD))||(collator.equals(key,ALIAS_PORT)))){
										appendAttribute(buf, TEXTBOX_SIZE, TEXTBOX_SIZE_VALUE);
									}
								}

								/*appendAttribute(buf, NAME, key);
								if(((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS)))){ 
									buf.append(CLOSE_ELEMENT);
									buf.append(OPEN_ELEMENT);
									buf.append(END_TEXTAREA);

								}
								appendEndRow(buf);*/

								appendAttribute(buf, NAME, key);
								if((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS))){ 
									buf.append(CLOSE_ELEMENT);
								}else if(collator.equals(key,MYSITE_BASE_URL)){
									appendAttribute(buf, "id", SP2007);
									if(isSP2007){
											appendAttribute(buf, READONLY, TRUE);
									}
								}


								if (value != null) {
									if((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS))){ 
										buf.append(value);	
									}else {
										appendAttribute(buf, VALUE, value);
									}
								}


								if((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS))){ 
									buf.append(OPEN_ELEMENT);
									buf.append(END_TEXTAREA);
									buf.append(CLOSE_ELEMENT);
									buf.append(TD_END);
									buf.append(TR_END);
								}else {
									appendEndRow(buf);
								}


							} else {

								String strSPConnectorType = (String) configStrings.get(SP_CONNECTOR_TYPE);
								if(isSP2007 == true) {

									buf.append("<tr> <td> <div style='float: left;'>"+strSPConnectorType+"</div> </td>");
									buf.append("<td><select id=\""+key+"\" name=\""+key+"\" size=\"1\" onchange=\"loadChanged();\"><option selected value=\""+SP2007+"\" >");
									buf.append(strSP2007);
									buf.append("</option><option value=\""+SP2003+"\">");
									buf.append(strSP2003);
									buf.append("</option></select> </td> </tr>");
								} else {

									buf.append("<tr> <td> <div style='float: left;'>"+strSPConnectorType+"</div> </td>");
									buf.append("<td><select id=\""+key+"\" name=\""+key+"\" size=\"1\" onchange=\"loadChanged();\"><option selected value=\""+SP2003+"\" >");
									buf.append(strSP2003);
									buf.append("</option><option value=\""+SP2007+"\">");
									buf.append(strSP2007);
									buf.append("</option></select> </td> </tr>");
								}

							}
						}//end for



					}//if condn
					buf.append("<tr valign='top' ><input type=\"hidden\" id='"+SP2003+"'/></tr>");
//					if(isSP2007 == true) {
//
//						buf.append("<tr valign='top' id='"+SP2003+"' name='"+SP2003+"' style=\"display:none;\"><input type=\"hidden\" /></tr> \r\n");
//					}else{
//
//						buf.append("<tr valign='top' id='"+SP2003+"' name='"+SP2003+"' style=\"display:block;\"><input type=\"hidden\" /></tr> \r\n");
//					}

					buf.append(START_BOLD);
					buf.append(configStrings.get(MANDATORY_FIELDS));
					buf.append(END_BOLD);
				}
			} 
		}
		// toss in all the stuff that's in the map but isn't in the keyset
		// taking care to list them in alphabetic order (this is mainly for
		// testability).
		final Iterator i = new TreeSet(configMap.keySet()).iterator();
		while (i.hasNext()) {
			final String key = (String) i.next();
			if (!keySet.contains(key)) {
				// add another hidden field to preserve this data
				final String val = (String) configMap.get(key);
				buf.append("<input type=\"hidden\" value=\"");
				buf.append(val);
				buf.append("\" name=\"");
				buf.append(key);
				buf.append("\"/>\r\n");
			}
		}  

		LOGGER.exiting(className, sFunName);
		return new ConfigureResponse(finalMessage, buf.toString());
	}

	public ConfigureResponse getConfigForm(final Locale locale) {
		final String sFunctionName = "getConfigForm(Locale locale)";
		LOGGER.entering(className, sFunctionName);
		setCollator(locale);
		final ResourceBundle rb = ResourceBundle.getBundle("SharepointConnectorResources", locale);
		SharepointClientContext.setResourcebundle(rb);
		setConfigStrings(rb);
		final ConfigureResponse result = new ConfigureResponse("",
				getInitialConfigForm());
		LOGGER.config(sFunctionName+" : getConfigForm form:\n" + result.getFormSnippet());
		LOGGER.exiting(className, sFunctionName);
		return result;
	}

	public ConfigureResponse getPopulatedConfigForm(final Map configMap, final Locale locale)  {
		final String sFunctionName = "getPopulatedConfigForm(Map configMap, Locale locale)";
		LOGGER.entering(className, sFunctionName);
		setCollator(locale);
		final ResourceBundle rb = ResourceBundle.getBundle("SharepointConnectorResources", locale);
		SharepointClientContext.setResourcebundle(rb);
		setConfigStrings(rb);
		final ConfigureResponse result = new ConfigureResponse("",
				makeConfigForm(configMap));
		LOGGER.config(sFunctionName+" : getConfigForm form:\n" + result.getFormSnippet());
		LOGGER.exiting(className, sFunctionName);
		return result;
	}

	public ConfigureResponse validateConfig(final Map configData, final Locale locale) {
		final String sFunctionName = "validateConfig(Map configData, Locale locale)";
		LOGGER.entering(className, sFunctionName);
		final ResourceBundle rb = ResourceBundle.getBundle("SharepointConnectorResources", locale);
		SharepointClientContext.setResourcebundle(rb);
		setConfigStrings(rb);
		if (validateConfigMap(configData, rb)) {
			// all is ok
			return null;
		}
		final ConfigureResponse configureResponse =  makeValidatedForm(configData, rb);
		LOGGER.config(sFunctionName+" :  message:\n" + configureResponse.getMessage());
		LOGGER.config(sFunctionName+": new form:\n" + configureResponse.getFormSnippet());
		LOGGER.exiting(className, sFunctionName);
		return configureResponse;
	}


	/**
	 * Desc : returns true if the Config Key is a mandatory field.
	 * @param configKey
	 * @return
	 */
	private boolean isRequired(final String configKey){
		final String sFunctionName = "isRequired(final String configKey)";
		LOGGER.entering(className, sFunctionName);
		final boolean bValue = true;
		if(collator.equals(configKey,EXCLUDED_URLS) || collator.equals(configKey,MYSITE_BASE_URL) || collator.equals(configKey,ALIAS_HOST_NAME) || collator.equals(configKey,ALIAS_PORT)){
			return false;
		}
		LOGGER.exiting(className, sFunctionName);
		return bValue;
	}

	public static Collator getCollator() {
		final String sFunctionName = "getCollator()";
		LOGGER.entering(SharepointConnectorType.class.getName(), sFunctionName);
		if(collator == null){
			SharepointConnectorType.collator = Collator.getInstance();
			SharepointConnectorType.collator.setStrength(Collator.PRIMARY);
		}
		LOGGER.exiting(SharepointConnectorType.class.getName(), sFunctionName);
		return collator;
	}

	private static void setCollator(final Locale locale) {
		final String sFunctionName = "setCollator(final Locale locale)";
		LOGGER.entering(SharepointConnectorType.class.getName(), sFunctionName);
		SharepointConnectorType.collator = Collator.getInstance(locale);
		SharepointConnectorType.collator.setStrength(Collator.PRIMARY);
		LOGGER.exiting(SharepointConnectorType.class.getName(), sFunctionName);
	}

//	due to GCM changes
	public ConfigureResponse validateConfig(Map configData, Locale locale, ConnectorFactory arg2) {
		final String sFunctionName = "validateConfig(Map configData, Locale locale, ConnectorFactory arg2)";
		LOGGER.entering(className, sFunctionName);
		LOGGER.exiting(className, sFunctionName);
		return validateConfig(configData, locale);
	}

	private void addJavaScript(final StringBuffer buf) {
		final String sFunctionName = "addJavaScript(final StringBuffer buf)";
		LOGGER.entering(className, sFunctionName);
		if(buf!=null){
			//	buf.append("<script language=\"JavaScript\"> var ids=new Array('"+SP2007+"','"+SP2003+"'); function switchid(id){var strObj=id; hideallids(); showdiv(id);} function hideallids(){ for (var i=0;i<ids.length;i++){ hidediv(ids[i]); } } function hidediv(id) { if (document.getElementsByName) { document.getElementById(id).style.display = 'none'; } else { if (document.layers) { document.id.display = 'none'; } else { document.all.id.style.display = 'none'; } } } function showdiv(id) { if (document.getElementsByName) { document.getElementById(id).style.display = 'block'; }	else { if (document.layers) { document.id.display = 'block'; } else {	document.all.id.style.display = 'block'; } } } function loadChanged(){ var con = document.getElementById('"+SP_CONNECTOR_TYPE+"'); if(con != null){ switchid(con.value); } } </script>");
			//buf.append("<script language=\"JavaScript\"> var ids=new Array('sp2007','sp2003'); function switchid(id){var strObj=id; hideallids(); showdiv(id);} function hideallids(){ for (var i=0;i<ids.length;i++){ hidediv(ids[i]); } } function hidediv(id) { if (document.getElementsByName) { document.getElementById(id).disabled=true; } else { if (document.layers) { document.id.disabled=true; } else { document.all.id.disabled=true; } } } function showdiv(id) { if (document.getElementsByName) { document.getElementById(id).disabled=false; }	else { if (document.layers) { document.id.disabled=false; } else {	document.all.id.style.disabled=false; } } } function loadChanged(){ var con = document.getElementById('SPType'); if(con != null){ switchid(con.value); } } </script>");
			//buf.append("<script language=\"JavaScript\"> var ids=new Array('sp2007','sp2003'); function switchid(id){var strObj=id; hideallids(); showdiv(id);} function hideallids(){ for (var i=0;i<ids.length;i++){ hidediv(ids[i]); } } function hidediv(id) { if (document.getElementsByName) { document.getElementById(id).setAttribute('readonly', 'true'); } else { if (document.layers) { document.id.setAttribute('readonly', 'true'); } else { document.all.id.setAttribute('readonly', 'true'); } } } function showdiv(id) { if (document.getElementsByName) { document.getElementById(id).removeAttribute('readonly'); }	else { if (document.layers) { document.id.removeAttribute('readonly'); } else {	document.all.id.removeAttribute('readonly'); } } } function loadChanged(){ var con = document.getElementById('SPType'); if(con != null){ switchid(con.value); } } </script>");
			buf.append("<script language=\"JavaScript\"> var ids=new Array('sp2007','sp2003'); function switchid(id){var strObj=id; hideallids(); showdiv(id);} function hideallids(){ for (var i=0;i<ids.length;i++){ hidediv(ids[i]); } } function hidediv(id) { if (document.getElementsByName) { document.getElementById(id).setAttribute('readonly', 'true');document.getElementById(id).readOnly=true; } else { if (document.layers) { document.id.setAttribute('readonly', 'true'); } else { document.all.id.setAttribute('readonly', 'true'); } } } function showdiv(id) { if (document.getElementsByName) { document.getElementById(id).removeAttribute('readonly');document.getElementById(id).readOnly=false; }	else { if (document.layers) { document.id.removeAttribute('readonly'); } else {	document.all.id.removeAttribute('readonly'); } } } function loadChanged(){ var con = document.getElementById('SPType'); if(con != null){ switchid(con.value); } } </script>");

		}
		LOGGER.exiting(className, sFunctionName);
	}

	private String checkPattern(final ResourceBundle rb){
		final String sFunctionName = "checkPattern(final ResourceBundle rb)";
		LOGGER.entering(className, sFunctionName);
		boolean isInclude = true;
		final SharepointClientContext spContext = new SharepointClientContext();
		if(spContext !=null) {
			spContext.setIncludedURlList(includeURL);
			spContext.setExcludedURlList(excludeURL);
			final SharepointClientUtils spUtils = new SharepointClientUtils();
			if(spUtils!=null){
				try {
					isInclude = spUtils.isIncludedUrl(spContext.getIncludedURlList(),spContext.getExcludedURlList(),sharepointUrl);
				} catch (final SharepointException e) {
					LOGGER.warning(e.toString());

				} catch (final MalformedURLException e) {
					LOGGER.warning(e.toString());

				}
			}
		}

		if(isInclude == false && rb!=null){
			return rb.getString(CRAWL_URL_PATTERN_MISMATCH);
		}
		LOGGER.exiting(className, sFunctionName);
		return null;

	}

	private String isFQDN(final String configKey, final String val,final ResourceBundle rb) { 
		String sFunctionName = "isFQDN(final String configKey, final String val,final ResourceBundle rb)";
		LOGGER.entering(className, sFunctionName);
		if (val.indexOf(".")==-1) {
			return rb.getString(REQ_FQDN_ALIAS_HOSTNAME);      
		}
		LOGGER.exiting(className, sFunctionName);
		return null;    
	}

}
