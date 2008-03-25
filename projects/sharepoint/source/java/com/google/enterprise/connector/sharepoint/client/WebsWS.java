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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import org.apache.axis.message.MessageElement;
import org.apache.catalina.util.URLEncoder;

import com.google.enterprise.connector.sharepoint.generated.webs.GetWebCollectionResponseGetWebCollectionResult;
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
	
	private String stubUrl = null;	
	private SharepointClientContext spClientContext = null;
	static{
		//set the URLEncoder safe characters
		enc.addSafeCharacter('/');
		enc.addSafeCharacter(':');// required when endpoint is set using specified site
	}

	/**
	 * 
	 * @param inSharepointClientContext
	 * @throws SharepointException
	 */
	public WebsWS(SharepointClientContext inSharepointClientContext)
	throws SharepointException {
		
		String sFunctionName = "WebsWS(SharepointClientContext inSharepointClientContext)";
		LOGGER.entering(className, sFunctionName);
		if(inSharepointClientContext!=null){
			spClientContext=inSharepointClientContext;
			endpoint = inSharepointClientContext.getProtocol()+URL_SEP+ inSharepointClientContext.getHost() + ":"+inSharepointClientContext.getPort() +enc.encode(inSharepointClientContext.getsiteName()) + WEBSENDPOINT;
			stubUrl =  inSharepointClientContext.getProtocol()+URL_SEP+ inSharepointClientContext.getHost() + ":"+inSharepointClientContext.getPort() +inSharepointClientContext.getsiteName() ;
			//System.out.println("websend: "+endpoint);
			WebsLocator loc = new WebsLocator();
			loc.setWebsSoapEndpointAddress(endpoint);
			Webs service = loc;

			try {
				stub = (WebsSoap_BindingStub) service.getWebsSoap();
			} catch (ServiceException e) {
				LOGGER.warning(className+":"+sFunctionName+": "+e.toString());
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

	/**
	 * 
	 * @param inSharepointClientContext
	 * @param siteName
	 * @throws SharepointException
	 */
	public WebsWS(SharepointClientContext inSharepointClientContext,
			String siteName) throws SharepointException {
		String sFunctionName = "WebsWS(SharepointClientContext inSharepointClientContext,String siteName)";
		LOGGER.entering(className, sFunctionName);
		spClientContext=inSharepointClientContext;
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
		stubUrl =  siteURL.getProtocol()+URL_SEP+ siteURL.getHost()+":"+iPort+siteURL.getPath();

		WebsLocator loc = new WebsLocator();
		loc.setWebsSoapEndpointAddress(endpoint);
		Webs service = loc;
		//System.out.println("websend: "+endpoint);
		try {
			stub = (WebsSoap_BindingStub) service.getWebsSoap();
		} catch (ServiceException e) {
			LOGGER.warning(className+":"+sFunctionName+": "+e.toString());
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

	/**
	 * 
	 * @param strPageURL
	 * @return
	 * @throws SharepointException
	 */
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
			LOGGER.warning(className+":"+strFunName+": "+e.toString());
			throw new SharepointException("Unable to get the sharepoint web site for the URL: "+strPageURL);
		}
		LOGGER.exiting(className, strFunName);
		return strWebURL;
	}

	public List getAllChildrenSites() throws SharepointException {
		String sFuncName ="getAllChildrenSites(): "; 
		ArrayList allWebsList = new ArrayList();// to store all the sub-webs
		
		GetWebCollectionResponseGetWebCollectionResult webcollnResult=null;
		
		try {
			webcollnResult= stub.getWebCollection();
		} catch (RemoteException e) {
			LOGGER.warning(sFuncName+"Unable to get the child webs for the web site ["+stubUrl+"]");
			throw new SharepointException(e.toString());
		}
		if(webcollnResult!=null){
			MessageElement[] meWebs = webcollnResult.get_any();
			if(meWebs!=null  && meWebs[0]!=null){
				
				Iterator itWebs = meWebs[0].getChildElements();
				if(itWebs!=null){
					while(itWebs.hasNext()){
						//e.g. <ns1:Web Title="ECSCDemo" Url="http://ps4312.persistent.co.in:2905/ECSCDemo" xmlns:ns1="http://schemas.microsoft.com/sharepoint/soap/"/>
						MessageElement meWeb = (MessageElement) itWebs.next();
						String url =meWeb.getAttribute("Url");
						
						LOGGER.config(sFuncName+": URL :"+url);
						String[] includedURLs = spClientContext.getIncludedURlList(); 
						String[] excludedURLs = spClientContext.getExcludedURlList() ; 
						SharepointClientUtils spUtils = new SharepointClientUtils();	

						try{
							//two cases: with port and without port taken ... because if port 80 then the port no: may be skipped by sharepoint to get the contents
							if (spUtils.isIncludedUrl(includedURLs,excludedURLs, url)) { 
								//System.out.println(sFunctionName+" : include URL ["+url.toString()+"]");
								LOGGER.config(sFuncName+" : include URL ["+url+"]");
								
								allWebsList.add(url);
								updateStub(url);
								allWebsList.addAll(getAllChildrenSites());
							}else{
								LOGGER.warning(sFuncName+" : excluding "+url);
							}
						}catch(Throwable e){
							LOGGER.warning("Unable to filter the url ["+url+"], Actual Exception:\n"+e.toString());
						}


					}
				}
			}
		}
		
		return allWebsList;
	}
	
	private void updateStub(String url) throws SharepointException {
		String sFunctionName="setStub(String username, String password, String url)";
		
		if(null!=url){
			URL siteURL ;
			try {
				siteURL = new URL(url);
			} catch (MalformedURLException e) {
				throw new SharepointException("Malformed URL: "+url);
			}
			int iPort = 0;
			
			//check if the def
			if (-1 != siteURL.getPort()) {
				iPort = siteURL.getPort();
			}else{
				iPort = siteURL.getDefaultPort();
			}
			endpoint = siteURL.getProtocol()+URL_SEP+ siteURL.getHost()+":"+iPort+enc.encode(siteURL.getPath())+ WEBSENDPOINT;
			stubUrl =  siteURL.getProtocol()+URL_SEP+ siteURL.getHost()+":"+iPort+siteURL.getPath();
			
			WebsLocator loc = new WebsLocator();
			loc.setWebsSoapEndpointAddress(endpoint);
			Webs service = loc;

			try {
				stub = (WebsSoap_BindingStub) service.getWebsSoap();
			} catch (ServiceException e) {
				LOGGER.warning(className+":"+sFunctionName+": "+e.toString());
				throw new SharepointException("Unable to create webs stub");
			}

			String strDomain = spClientContext.getDomain();
			String strUser = spClientContext.getUsername();
			String strPassword= spClientContext.getPassword();
			strDomain+="\\"+strUser; // form domain/user 

			//set the user and pass
			stub.setUsername(strDomain);
			stub.setPassword(strPassword);
		}
		
		
	}


}
