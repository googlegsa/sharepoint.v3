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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SharepointException;
import com.google.enterprise.connector.sharepoint.client.SiteDataWS;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * ConnectorType implementation for Sharepoint.
 * */
public class SharepointConnectorType implements ConnectorType {
  private static final Log LOGGER = LogFactory.getLog(SharepointConnectorType.class);
  private static final String VALUE = "value";
  private static final String NAME = "name";
  private static final String TEXT = "text";
  private static final String TEXTAREA = "textarea"; 
  private static final String TYPE = "type";
  private static final String INPUT = "input";
  private static final String CLOSE_ELEMENT = "/>";
  private static final String OPEN_ELEMENT = "<";
  private static final String PASSWORD = "password";
  private static final String TR_END = "</tr>\r\n";
  private static final String TD_END = "</td>\r\n";
  private static final String TD_START = "<td style='white-space: nowrap'>";
  private static final String TR_START = "<tr valign='top'>\r\n";
  private static final String  ROWS= "rows";	
  private static final String ROWS_VALUE = "5";	
  private static final String  COLS= "cols";	
  private static final String COLS_VALUE = "50";	
  private static final String END_TEXTAREA = "/textarea";
  private static final String START_BOLD = "<b>";
  private static final String END_BOLD = "</b>";
//  private static final String ASTERISK = "*";
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
  
  
  private static final String REQ_FIELDS_MISSING = 
    "Field_Is_Required";
  private static final String REQ_FQDN_URL = 
    "Url_Entered_Should_Be_Fully_Qualified";
  private static final String CANNOT_CONNECT ="Cannot_Connect";
  private static final String BLANK_STRING = ""; 
  private static Collator collator = null;
  
  private String sharepointUrl = null;
  private String domain = null ;
  private String username = null;
  private String password = null;
  private String includeURL = null; 
  
  private List keys = null;
  private Set keySet = null;
  private final HashMap configStrings = new HashMap();
  private String initialConfigForm = null;
  
  /**
   * Sets the keys that are required for configuration. These are the actual 
   * keys used by the class. 
   * @param inKeys A list of String keys
   */
  public void setConfigKeys(final List inKeys) {
    if (this.keys != null) {
      throw new IllegalStateException();
    }
    if(inKeys!=null){
	    this.keys = inKeys;
	    this.keySet = new HashSet(inKeys);
    }
  }  
  
  /**
   * Sets the display strings for the configuration form depending on the 
   * language settings.
   * @param rb Resource bundle for the particular language
   */
  private void setConfigStrings(final ResourceBundle rb) {
	  final String sFunctionName = "setConfigStrings(ResourceBundle rb)";
	  
	  if(rb!=null){
		  //checking the parameters of the resource bundle passed
		  for(int iKey=0;iKey<keys.size();++iKey){
			  final Object key = keys.get(iKey);
	      LOGGER.debug(sFunctionName +" [ KEY: "+key.toString()+" | VALUE: "+ rb.getString(key.toString())+"]");
			  configStrings.put(key, rb.getString((String)key));     
	    }
		  configStrings.put(MANDATORY_FIELDS, rb.getString(MANDATORY_FIELDS));
	  }else{
		  LOGGER.warn(sFunctionName+": unable to get resource bundle");
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
   * passed a non-null config map, pre-filling values in from that map.
   * 
   * @param configMap
   * @return config form snippet
   */
  private String makeConfigForm(final Map configMap) {
	  final String sFunctionName = "makeConfigForm(Map configMap)";
	   if(collator == null){
		   collator = getCollator();
	   }
	    final StringBuffer buf = new StringBuffer(2048);
	    if(keys!=null){
		    for (final Iterator i = keys.iterator(); i.hasNext();) {
		      final String key = (String) i.next();
		      final String configKey = (String) configStrings.get(key);
		      
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
		          if(!((collator.equals(key,USERNAME)) || (collator.equals(key,PASSWORD))||(collator.equals(key,ALIAS_PORT))) ){
		        	  appendAttribute(buf, TEXTBOX_SIZE, TEXTBOX_SIZE_VALUE);
		          }
		      }
		      appendAttribute(buf, NAME, key);
		      if((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS))){ 
		    	  buf.append(CLOSE_ELEMENT);
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
		      }
		      appendEndRow(buf);
		    }
	    }//end null check
	    buf.append(START_BOLD);
	    buf.append(configStrings.get(MANDATORY_FIELDS));
	    buf.append(END_BOLD);
	    LOGGER.debug(sFunctionName+" : configform ["+buf.toString()+"]");
	    return buf.toString();
	  }
  
  private void appendStartRow(final StringBuffer buf, final String key, final String configKey,final boolean red) {
	  buf.append(TR_START);
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
	    //	buf.append(ASTERISK);
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

  }

  private void appendEndRow(final StringBuffer buf) {
    buf.append(CLOSE_ELEMENT);
    buf.append(TD_END);
    buf.append(TR_END);
  }
  
  private void appendAttribute(final StringBuffer buf, final String attrName,
      final String attrValue) {
    buf.append(" ");
    buf.append(attrName);
    buf.append("=\"");
    // TODO xml-encode the special characters (< > " etc.)
    buf.append(attrValue);
    buf.append("\"");
  }
  
  private void setSharepointCredentials(final String key, final String val) {
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
    }
  }
  
  private String getErrorMessage(final String configKey, final String val, 
      final ResourceBundle rb) {    
    if (val == null || val.length() == 0) {
      return rb.getString(REQ_FIELDS_MISSING) + " " + configKey;
    }      
    //if (val.startsWith("http://") && !val.contains(".")) {
    if (val.startsWith("http://") && (val.indexOf(".")==-1)/*!val.contains(".")*/) {
       return rb.getString(REQ_FQDN_URL);      
    }
    return null;    
  }
  
  private String checkConnectivity() {
	  final String sFuctionName = "checkConnectivity()";
    final SharepointClientContext sharepointClientContext = new 
        SharepointClientContext(sharepointUrl, domain, username, 
        password, null,includeURL,null,null,null,null); // modified by A Mitra
    try {
      final SiteDataWS siteDataWS = new SiteDataWS(sharepointClientContext);
      if(siteDataWS==null){
    	  throw new SharepointException(sFuctionName+": sitedata stub is not found");
      }
      siteDataWS.getAllChildrenSites();
    } catch (final SharepointException e) {      
      LOGGER.warn(sFuctionName+":"+ e.toString());
      return CANNOT_CONNECT;
    } catch (final RepositoryException e) {              
    	LOGGER.warn(sFuctionName+":"+ e.toString());
      return CANNOT_CONNECT;
    }         
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
	  if(configData==null){
		  LOGGER.warn(sFunctionName+": configData map is not found");
		  return false;
	  }
		  
    String message = null;
    for (final Iterator i = keys.iterator(); i.hasNext();) {
      final String key = (String) i.next();
      final String val = (String) configData.get(key);
      setSharepointCredentials(key, val);
     if (isRequired(key)){  
      message = getErrorMessage(key, val , rb);
    } else if (val ==null){  
    	configData.put(key, BLANK_STRING);
    }
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
    return true;
  }
  
  private ConfigureResponse makeValidatedForm(final Map configMap, final ResourceBundle rb) 
      {
	  	final String sFunName="makeValidatedForm(final Map configMap, final ResourceBundle rb)";
	  	
	  	if(configMap==null){
	  		LOGGER.warn(sFunName+": configMap is not found");
	  		if(rb!=null){
	  			return new ConfigureResponse(rb.getString("CONFIGMAP_NOT_FOUND"),"");
	  		}else{
	  			return new ConfigureResponse("resource bundle not found","");
	  		}
	  	}
	    final StringBuffer buf = new StringBuffer(2048);   
	    String message = null;
	    String finalMessage = null;   
	    for (final Iterator i = keys.iterator(); i.hasNext();) {
	      final String key = (String) i.next();
	      final String configKey = (String) configStrings.get(key);
	      final String value = (String) configMap.get(key);
	      message = null;
	      if (finalMessage == null) {
	        message = getErrorMessage(configKey, value, rb);
	        finalMessage = message;
	      }      
	      setSharepointCredentials(key, value);            
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
	          if(!((collator.equals(key,USERNAME)) || (collator.equals(key,PASSWORD))||(collator.equals(key,ALIAS_PORT))) ){
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
	   //		 	appendAttribute(buf,COLS , COLS_VALUE);
	      	    
	        }else {
	          appendAttribute(buf, TYPE, TEXT);
	        }
	      }
	      appendAttribute(buf, NAME, key);
	      if(((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS)))){ 
	    	  buf.append(CLOSE_ELEMENT);
	    	  buf.append(value);
	    	  buf.append(OPEN_ELEMENT);
	    	  buf.append(END_TEXTAREA);
	    	  
	      }
	      
	      appendEndRow(buf);
	    }
	    
	    if (finalMessage == null) {
	      finalMessage = rb.getString(checkConnectivity());  
	      if (finalMessage != null) {
	        buf.setLength(0);
	        if((keys!=null) && (configStrings!=null)){
		        for (final Iterator i = keys.iterator(); i.hasNext();) {
		          final String key = (String) i.next();
		          final String configKey = (String) configStrings.get(key);
	//	          final String value = (String) configMap.get(key);                     
		          appendStartRow(buf, key,configKey, true);
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
		     	//	 appendAttribute(buf,COLS , COLS_VALUE);
		          }else {
		            appendAttribute(buf, TYPE, TEXT);
		            //do not increase size for username, password and port
		            if(!((collator.equals(key,USERNAME)) || (collator.equals(key,PASSWORD))||(collator.equals(key,ALIAS_PORT))) ){
		            	appendAttribute(buf, TEXTBOX_SIZE, TEXTBOX_SIZE_VALUE);
		            }
		          }
		          appendAttribute(buf, NAME, key);
		          if(((collator.equals(key,EXCLUDED_URLS)) || (collator.equals(key,INCLUDED_URLS)))){ 
		        	  buf.append(CLOSE_ELEMENT);
		        	  buf.append(OPEN_ELEMENT);
		        	  buf.append(END_TEXTAREA);
		        	  
		          }
		          appendEndRow(buf);
		        }//end for
	        }//if condn
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
	    
	    
	    return new ConfigureResponse(finalMessage, buf.toString());
	  }
  
  public ConfigureResponse getConfigForm(final Locale locale) {
	final String sFunctionName = "getConfigForm(Locale locale)";
	setCollator(locale);
    final ResourceBundle rb = ResourceBundle.getBundle("SharepointConnectorResources", locale);
    SharepointClientContext.setResourcebundle(rb);
    setConfigStrings(rb);
    final ConfigureResponse result = new ConfigureResponse("",
        getInitialConfigForm());
    LOGGER.debug(sFunctionName+" : getConfigForm form:\n" + result.getFormSnippet());
    return result;
  }

  public ConfigureResponse getPopulatedConfigForm(final Map configMap, final Locale locale)  {
	  final String sFunctionName = "getPopulatedConfigForm(Map configMap, Locale locale)";
     setCollator(locale);
	  final ResourceBundle rb = ResourceBundle.getBundle("SharepointConnectorResources", locale);
	  SharepointClientContext.setResourcebundle(rb);
    setConfigStrings(rb);
    final ConfigureResponse result = new ConfigureResponse("",
        makeConfigForm(configMap));
    LOGGER.debug(sFunctionName+" : getConfigForm form:\n" + result.getFormSnippet());
    return result;
  }

  public ConfigureResponse validateConfig(final Map configData, final Locale locale) {
    final String sFunctionName = "validateConfig(Map configData, Locale locale)";
	  final ResourceBundle rb = ResourceBundle.getBundle("SharepointConnectorResources", locale);
	  SharepointClientContext.setResourcebundle(rb);
    setConfigStrings(rb);
    if (validateConfigMap(configData, rb)) {
      // all is ok
      return null;
    }
    final ConfigureResponse configureResponse =  makeValidatedForm(configData, rb);
    LOGGER.debug(sFunctionName+" :  message:\n" + configureResponse.getMessage());
    LOGGER.debug(sFunctionName+": new form:\n" + configureResponse.getFormSnippet());
    return configureResponse;
  }
  
  
  /**
   * Desc : returns true if the Config Key is a mandatory field.
   * @param configKey
   * @return
   */
  private boolean isRequired(final String configKey){
	  final boolean bValue = true;
	  if(collator.equals(configKey,EXCLUDED_URLS) || collator.equals(configKey,MYSITE_BASE_URL) || collator.equals(configKey,ALIAS_HOST_NAME) || collator.equals(configKey,ALIAS_PORT)){
		  return false;
	  }
	  return bValue;
  }

public static Collator getCollator() {
	if(collator == null){
		SharepointConnectorType.collator = Collator.getInstance();
		SharepointConnectorType.collator.setStrength(Collator.PRIMARY);
	}
	return collator;
}

private static void setCollator(final Locale locale) {
	SharepointConnectorType.collator = Collator.getInstance(locale);
	SharepointConnectorType.collator.setStrength(Collator.PRIMARY);
}

//due to GCM changes
public ConfigureResponse validateConfig(Map configData, Locale locale, ConnectorFactory arg2) {
	return validateConfig(configData, locale);
}



}
