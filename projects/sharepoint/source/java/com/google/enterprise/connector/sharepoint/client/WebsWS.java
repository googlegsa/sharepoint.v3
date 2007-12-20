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
package com.google.enterprise.connector.sharepoint.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

//import org.apache.catalina.util.URL;
import org.apache.catalina.util.URLEncoder;

import com.google.enterprise.connector.sharepoint.generated.webs.Webs;
import com.google.enterprise.connector.sharepoint.generated.webs.WebsLocator;
import com.google.enterprise.connector.sharepoint.generated.webs.WebsSoap_BindingStub;

/**
 * This class holds data and methods for any call to Webs Web Service.
 *
 */
public class WebsWS {
	private static final String WEBSENDPOINT = "/_vti_bin/Webs.asmx";
	private String endpoint;
	WebsSoap_BindingStub stub = null;
	static final String URL_SEP ="://";
	private static final Logger LOGGER = Logger.getLogger(WebsWS.class.getName());
	private String className = WebsWS.class.getName();
	public static URLEncoder enc  = new URLEncoder();

	static{
		//set the URLEncoder safe characters
		enc.addSafeCharacter('/');
		enc.addSafeCharacter(':');// required when endpoint is set using specified site
	}

	public WebsWS(SharepointClientContext inSharepointClientContext)
	throws SharepointException {
		String sFunctionName = "WebsWS(SharepointClientContext inSharepointClientContext)";
		LOGGER.entering(className, sFunctionName);
		if(inSharepointClientContext!=null){
			endpoint = inSharepointClientContext.getProtocol()+URL_SEP+ inSharepointClientContext.getHost() + ":"+inSharepointClientContext.getPort() +enc.encode(inSharepointClientContext.getsiteName()) + WEBSENDPOINT;
			//System.out.println("websend: "+endpoint);
			WebsLocator loc = new WebsLocator();
			loc.setWebsSoapEndpointAddress(endpoint);
			Webs service = loc;

			try {
				stub = (WebsSoap_BindingStub) service.getWebsSoap();
			} catch (ServiceException e) {
				LOGGER.finer("WebsWS(SharepointClientContext inSharepointClientContext): "+e.toString());
				throw new SharepointException("Unable to create webs stub");
			}

			String strDomain = inSharepointClientContext.getDomain();
			String strUser = inSharepointClientContext.getUsername();
			String strPassword= inSharepointClientContext.getPassword();
			strDomain+="\\"+strUser; // form domain/user 

			//set the user and pass
			stub.setUsername(strDomain);
			stub.setPassword(strPassword);
		}
		LOGGER.exiting(className, sFunctionName);
	}

	public WebsWS(SharepointClientContext inSharepointClientContext,
			String siteName) throws SharepointException {
		String sFunctionName = "WebsWS(SharepointClientContext inSharepointClientContext,String siteName)";
		LOGGER.entering(className, sFunctionName);
		if(siteName==null){
			throw new SharepointException("Unable to get the site name");
		}
//		endpoint = siteName + WEBSENDPOINT;
		URL siteURL ;
		try {
			siteURL = new URL(siteName);
		} catch (MalformedURLException e) {
			throw new SharepointException("Malformed URL: "+siteName);
		}
		int iPort = 0;
		
		//check if the def
		if (-1 != siteURL.getPort()) {
			iPort = siteURL.getPort();
		}else{
			iPort = siteURL.getDefaultPort();
		}
		endpoint = siteURL.getProtocol()+URL_SEP+ siteURL.getHost()+":"+/*siteURL.getPort()*/iPort+enc.encode(siteURL.getPath())+ WEBSENDPOINT;

		WebsLocator loc = new WebsLocator();
		loc.setWebsSoapEndpointAddress(endpoint);
		Webs service = loc;
		//System.out.println("websend: "+endpoint);
		try {
			stub = (WebsSoap_BindingStub) service.getWebsSoap();
		} catch (ServiceException e) {
			LOGGER.finer("WebsWS(SharepointClientContext inSharepointClientContext): "+e.toString());
			throw new SharepointException("Unable to create webs stub");
		}

		String strDomain = inSharepointClientContext.getDomain();
		String strUser = inSharepointClientContext.getUsername();
		String strPassword= inSharepointClientContext.getPassword();
		strDomain+="\\"+strUser; // form domain/user 

		//set the user and pass
		stub.setUsername(strDomain);
		stub.setPassword(strPassword);
		LOGGER.exiting(className, sFunctionName);
	}

	public String getWebURLFromPageURL(String strPageURL) throws SharepointException{
		final String strFunName = "getWebURLFromPageURL(String strPageURL)";
		LOGGER.entering(className, strFunName);
		String strWebURL = null;

		if(stub==null){
			throw new SharepointException("Unable to get the webs stub");
		}

		try{
			strWebURL= stub.webUrlFromPageUrl(strPageURL);
		}catch(Throwable e){
//			System.out.println("errorPageURL:"+strPageURL);
			LOGGER.config(strFunName+": "+e.toString());
			throw new SharepointException("Unable to get the sharepoint web site for the URL: "+strPageURL);
		}
		LOGGER.exiting(className, strFunName);
		return strWebURL;
	}

}
