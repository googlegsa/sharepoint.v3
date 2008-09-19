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
import java.text.Collator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import org.apache.axis.message.MessageElement;
import org.apache.catalina.util.URLEncoder;

import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.generated.webs.GetWebCollectionResponseGetWebCollectionResult;
import com.google.enterprise.connector.sharepoint.generated.webs.GetWebResponseGetWebResult;
import com.google.enterprise.connector.sharepoint.generated.webs.Webs;
import com.google.enterprise.connector.sharepoint.generated.webs.WebsLocator;
import com.google.enterprise.connector.sharepoint.generated.webs.WebsSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.state.WebState;

/**
 * This class holds data and methods for any call to Webs Web Service.
 * @author amit_kagrawal
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
				//LOGGER.warning(className+":"+sFunctionName+": "+e.toString());
				LOGGER.log(Level.WARNING,className+":"+sFunctionName,e);
				throw new SharepointException("Unable to create webs stub");
			}

			String strDomain = inSharepointClientContext.getDomain();
			String strUser = inSharepointClientContext.getUsername();
			String strPassword= inSharepointClientContext.getPassword();

			if((strDomain==null)||(strDomain.trim().equals(""))){
				strDomain=strUser; //for user
			}else{
				strDomain+="\\"+strUser; // form domain/user
			}

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
		LOGGER.config(sFunctionName+": inSharepointClientContext[SiteName="+inSharepointClientContext.getsiteName()+"], SiteName["+siteName+"]"); //added by Nitendra
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
		spClientContext=(SharepointClientContext) inSharepointClientContext.clone();
		spClientContext.setURL(siteName);
		
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

		if((strDomain==null)||(strDomain.trim().equals(""))){
			strDomain=strUser; //for user
		}else{
			strDomain+="\\"+strUser; // form domain/user
		}

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
	public WebState getWebURLFromPageURL(String strPageURL) throws SharepointException{
		final String strFunName = "getWebURLFromPageURL(String strPageURL)";
		LOGGER.entering(className, strFunName);
		LOGGER.config("Page URL: "+strPageURL);
		
		String tpEndPoint = null;
		
		WebsSoap_BindingStub tpStub = null;//Create a temporary stub
		URL siteURL=null;
		try {
			siteURL = new URL(strPageURL);
		} catch (MalformedURLException e1) {
			LOGGER.log(Level.WARNING,className+":"+strFunName+": Malformed Page URL ["+strPageURL+"] found as Link",e1);
			throw new SharepointException(e1);
		}

		if(null!=siteURL){
			int iPort = 0;

			if (-1 != siteURL.getPort()) {
				iPort = siteURL.getPort();
			}else{
				iPort = siteURL.getDefaultPort();
			}
			
			tpEndPoint = siteURL.getProtocol()+URL_SEP+ siteURL.getHost()+":"+iPort+ WEBSENDPOINT;
			WebsLocator loc = new WebsLocator();
			loc.setWebsSoapEndpointAddress(tpEndPoint);
			Webs service = loc;

			try {
				tpStub = (WebsSoap_BindingStub) service.getWebsSoap();
			} catch (ServiceException e) {
				LOGGER.log(Level.WARNING,className+":"+strFunName+": ",e);
				throw new SharepointException("Unable to create webs stub");
			}

			String strDomain = spClientContext.getDomain();
			String strUser = spClientContext.getUsername();
			String strPassword= spClientContext.getPassword();

			if((strDomain==null)||(strDomain.trim().equals(""))){
				strDomain=strUser; //for user
			}else{
				strDomain+="\\"+strUser; // form domain/user
			}

			tpStub.setUsername(strDomain);//set the user and pass
			tpStub.setPassword(strPassword);
		}

		if(tpStub==null){
			throw new SharepointException("Unable to get the webs stub");
		}
		
		WebState ws =null;
		String strWebURL = null;
		String webTitle = "No Title";
		try{
			strWebURL= tpStub.webUrlFromPageUrl(strPageURL);
			
			
			GetWebResponseGetWebResult resWeb = tpStub.getWeb(strWebURL);
			if(null!=resWeb){
				MessageElement[] meArray = resWeb.get_any();
				if(meArray!=null && meArray[0]!=null){
					webTitle= meArray[0].getAttribute("Title");//Get the Title
				}
			}
			
			
			ws = new WebState(strWebURL,strWebURL,webTitle);
		}catch(Throwable e){
			LOGGER.log(Level.WARNING, className+":"+strFunName, e);
			throw new SharepointException("Unable to get the sharepoint web site for the URL: "+strPageURL);
		}
		LOGGER.exiting(className, strFunName);
		return ws;
	}

	public TreeSet getDirectChildsites() throws SharepointException {
		String sFuncName ="getDirectChildsites(): ";
		LOGGER.config(sFuncName+"Getting the child webs for the web site ["+stubUrl+"]"); //added by Nitendra
		
		TreeSet allWebsList = new TreeSet();// to store all the sub-webs state

		GetWebCollectionResponseGetWebCollectionResult webcollnResult=null;

		try {
			webcollnResult= stub.getWebCollection();
		} catch (RemoteException e) {
			//LOGGER.warning(sFuncName+"Unable to get the child webs for the web site ["+stubUrl+"]");
			LOGGER.log(Level.WARNING,sFuncName+"Unable to get the child webs for the web site ["+stubUrl+"]",e);
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
						String title = meWeb.getAttribute("Title");
						WebState ws = null;

						LOGGER.config(sFuncName+": URL :"+url);
						String[] includedURLs = spClientContext.getIncludedURlList(); 
						String[] excludedURLs = spClientContext.getExcludedURlList() ; 
						SharepointClientUtils spUtils = new SharepointClientUtils();	

						try{
							//two cases: with port and without port taken ... because if port 80 then the port no: may be skipped by sharepoint to get the contents
							if (spUtils.isIncludedUrl(includedURLs,excludedURLs, url)) { 
								LOGGER.config(sFuncName+" : include URL ["+url+"]");
								ws = new WebState(url,url,title);
								allWebsList.add(ws);
							}else{
								LOGGER.warning(sFuncName+" : excluding "+url);
							}
						}catch(Throwable e){
							LOGGER.log(Level.WARNING,"Unable to filter the url ["+url+"]",e);
						}

					}
				}
			}
		}

		return allWebsList;
	}
	public String getTitle(String strWebURL){
		String webTitle = "No Title";

		if((strWebURL!=null) && (strWebURL.endsWith("/"))){
			strWebURL = strWebURL.substring(0, strWebURL.lastIndexOf("/"));
		}
		
/*		// added by Nitendra
		try {
			StringBuffer sb=null;
			while(strWebURL.charAt(strWebURL.length()-1)=='/') {
				sb=new StringBuffer(strWebURL);			
				sb.deleteCharAt(sb.length()-1);
				strWebURL=new String(sb);
			}
		}
		catch(Exception e) {
			return webTitle;
		}		
*/
		try{
			LOGGER.config("Getting title for Web: "+strWebURL+" SharepointConnectorType: "+spClientContext.getSharePointType());
						
			// SP2003 Top URL case added by Nitendra
			if(spClientContext.getSharePointType().equals(SharepointConnectorType.SP2003) /* && is a top URL */){
				SiteDataWS siteDataWS = new SiteDataWS(spClientContext,strWebURL);
				webTitle=siteDataWS.getTitle();
			}
			else {
				GetWebResponseGetWebResult resWeb = stub.getWeb(strWebURL);			
				if(null!=resWeb){
					MessageElement[] meArray = resWeb.get_any();
					if(meArray!=null && meArray[0]!=null){
						webTitle= meArray[0].getAttribute("Title");//Get the Title
					}
				}
			}
		}catch(Exception e){
			//LOGGER.log(Level.WARNING, "Unable to Get Web Information ",e);
			LOGGER.log(Level.WARNING, "Unable to Get Title for web "+strWebURL);
			/*StringTokenizer strTok = new StringTokenizer(strWebURL,"/");
			if(null!=strTok){
				while(strTok.hasMoreTokens()){
					webTitle = strTok.nextToken();
				}
			}else{
				webTitle = "No Title";
			}*/
			
		}
		return webTitle;
	}
	
	//public TreeSet getAllChildrenSites(String strWebType) throws SharepointException {
	/*public TreeSet getAllChildrenSites() throws SharepointException {
		String sFuncName ="getAllChildrenSites(): "; 
		TreeSet allWebsList = new TreeSet();// to store all the sub-webs state

		GetWebCollectionResponseGetWebCollectionResult webcollnResult=null;

		try {
			webcollnResult= stub.getWebCollection();
		} catch (RemoteException e) {
			//LOGGER.warning(sFuncName+"Unable to get the child webs for the web site ["+stubUrl+"]");
			LOGGER.log(Level.WARNING,sFuncName+"Unable to get the child webs for the web site ["+stubUrl+"]",e);
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
						WebState ws = null;

						LOGGER.config(sFuncName+": URL :"+url);
						String[] includedURLs = spClientContext.getIncludedURlList(); 
						String[] excludedURLs = spClientContext.getExcludedURlList() ; 
						SharepointClientUtils spUtils = new SharepointClientUtils();	

						try{
							//two cases: with port and without port taken ... because if port 80 then the port no: may be skipped by sharepoint to get the contents
							if (spUtils.isIncludedUrl(includedURLs,excludedURLs, url)) { 
								LOGGER.config(sFuncName+" : include URL ["+url+"]");
								ws = new WebState(url,url);
								allWebsList.add(ws);
								updateStub(url);
								allWebsList.addAll(getAllChildrenSites());
							}else{
								LOGGER.warning(sFuncName+" : excluding "+url);
							}
						}catch(Throwable e){
							LOGGER.log(Level.WARNING,"Unable to filter the url ["+url+"]",e);
						}

					}
				}
			}
		}

		return allWebsList;
	}*/

	/*public void getDirectChildsites() throws SharepointException {
		String sFuncName ="getDirectChildsites(): "; 
		try {
			stub.getWebCollection();
		} catch (RemoteException e) {
			LOGGER.log(Level.WARNING,sFuncName+"Unable to get the child webs for the web site ["+stubUrl+"]",e);
			throw new SharepointException(e.toString());
		}
	}*/

	/*private void updateStub(String url) throws SharepointException {
		String sFunctionName="setStub(String username, String password, String url)";

		if(null!=url){
			URL siteURL ;
			try {
				siteURL = new URL(url);
			} catch (MalformedURLException e) {
				LOGGER.log(Level.WARNING ,"Malformed URL: "+url,e);
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
				//LOGGER.warning(className+":"+sFunctionName+": "+e.toString());
				LOGGER.log(Level.WARNING ,className+":"+sFunctionName,e);
				throw new SharepointException("Unable to create webs stub");
			}

			String strDomain = spClientContext.getDomain();
			String strUser = spClientContext.getUsername();
			String strPassword= spClientContext.getPassword();

			if((strDomain==null)||(strDomain.trim().equals(""))){
				strDomain=strUser; //for user
			}else{
				strDomain+="\\"+strUser; // form domain/user
			}

			//set the user and pass
			stub.setUsername(strDomain);
			stub.setPassword(strPassword);
		}


	}*/


}
