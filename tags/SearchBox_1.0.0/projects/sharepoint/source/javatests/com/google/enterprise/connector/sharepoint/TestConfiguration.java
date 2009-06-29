//Copyright 2007 Google Inc.

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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TestConfiguration {
	public static String googleConnectorWorkDir;
	public static String googleWorkDir;

	public static String sharepointUrl;
	public static String AliasMap;
	public static String domain; 
	public static String username;
	public static String Password;
	public static String mySiteBaseURL;
	public static String includedURls; 
	public static String excludedURls;
	
	public static String searchUserID;
	public static String searchUserPwd;
	public static String SearchDocID1;
	public static String SearchDocID2;
	public static String SearchDocID3;
	
	public static String DocID1;
	public static String DocID2;
	public static String DocID3;
	
	public static String ParentWebURL;
	public static String ParentWebTitle;
	public static String BaseListID;	
	public static String LastModified;
	public static String LastItemID;
	public static String lastItemURL;
	
	public static ArrayList<String> blackList = new ArrayList<String>();
	public static ArrayList<String> whiteList = new ArrayList<String>();
	public static boolean FQDNflag;
	public static String feedType;
	
	static {
		final Properties properties = new Properties();
	    try {
	    	properties.load(new FileInputStream("source/javatests/TestConfig.properties"));	              
	    } catch (final IOException e) {
	    	System.out.println("Unable to load the property file."+e);	    	
	    }
	    googleConnectorWorkDir = properties.getProperty("googleConnectorWorkDir");
        googleWorkDir = properties.getProperty("googleWorkDir");     
        sharepointUrl = properties.getProperty("sharepointUrl");
        AliasMap = properties.getProperty("AliasMap");
        domain = properties.getProperty("domain");
        username = properties.getProperty("username");
        Password = properties.getProperty("Password");
        mySiteBaseURL = properties.getProperty("mySiteBaseURL");
        includedURls = properties.getProperty("includedURls");
        excludedURls = properties.getProperty("excludedURls");
        
        searchUserID = properties.getProperty("SearchUserID");
        searchUserPwd = properties.getProperty("SearchUserPwd");
        SearchDocID1 = properties.getProperty("SearchDocID1");
        SearchDocID2 = properties.getProperty("SearchDocID2");
        SearchDocID3 = properties.getProperty("SearchDocID3");
        
        DocID1 = properties.getProperty("DocID1");
        DocID2 = properties.getProperty("DocID2");
        DocID3 = properties.getProperty("DocID3");
        
        ParentWebURL = properties.getProperty("ParentWebURL");
        ParentWebTitle = properties.getProperty("ParentWebTitle");
        BaseListID = properties.getProperty("BaseListID");
        LastModified = properties.getProperty("LastModified");
        LastItemID = properties.getProperty("LastItemID");
        lastItemURL = properties.getProperty("LastItemURL");
        
		blackList.add(".*cachedcustomprops$");
		blackList.add(".*parserversion$");
		blackList.add(".*ContentType$");
		blackList.add(".*cachedtitle$");
		blackList.add(".*ContentTypeId$");
		blackList.add(".*DocIcon$");
		blackList.add(".*cachedhastheme$");
		blackList.add(".*metatags$");
		blackList.add(".*charset$");
		blackList.add(".*cachedbodystyle$");
		blackList.add(".*cachedneedsrewrite$");
		
/*		whiteList.add(".*file type$");
		whiteList.add(".*vti_title$");
		whiteList.add(".*vti_author$");
*/        
		FQDNflag = false;
		feedType = "metadata-and-url";		
	}
	
	public static Map<String, String> getConfigMap() {
		final Map<String, String> configMap = new HashMap<String, String>();
		
		configMap.put("sharepointUrl", sharepointUrl);
		configMap.put("AliasMap", AliasMap);
		configMap.put("domain", domain);
		configMap.put("username", username);
		configMap.put("Password", Password);
		configMap.put("mySiteBaseURL", mySiteBaseURL);
		configMap.put("includedURls", includedURls);
		configMap.put("excludedURls", excludedURls);
		
		return configMap;
	}
}
