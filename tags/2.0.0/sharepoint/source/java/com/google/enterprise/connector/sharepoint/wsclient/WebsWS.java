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
package com.google.enterprise.connector.sharepoint.wsclient;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.message.MessageElement;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.webs.GetWebCollectionResponseGetWebCollectionResult;
import com.google.enterprise.connector.sharepoint.generated.webs.GetWebResponseGetWebResult;
import com.google.enterprise.connector.sharepoint.generated.webs.Webs;
import com.google.enterprise.connector.sharepoint.generated.webs.WebsLocator;
import com.google.enterprise.connector.sharepoint.generated.webs.WebsSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

/**
 * Java Client for calling Webs.asmx
 * Provides a layer to talk to the Webs Web Service on the SharePoint server 
 * Any call to this Web Service must go through this layer.
 * @author nitendra_thakur
 *
 */
public class WebsWS {
	private String endpoint;
	private WebsSoap_BindingStub stub = null;
	private final Logger LOGGER = Logger.getLogger(WebsWS.class.getName());
	private SharepointClientContext sharepointClientContext = null;
		
	/**
	 * @param inSharepointClientContext  The Context is passed so that necessary information can be used to create the instance of current class
	 * Web Service endpoint is set to the default SharePoint URL stored in SharePointClientContext.
	 * @throws SharepointException
	 */
	public WebsWS(final SharepointClientContext inSharepointClientContext)
	throws SharepointException {
		
		if(inSharepointClientContext!=null){
			sharepointClientContext=inSharepointClientContext;
			endpoint = Util.encodeURL(sharepointClientContext.getSiteURL()) + SPConstants.WEBSENDPOINT;
			LOGGER.log(Level.INFO, "Endpoint set to: "+endpoint);
			final WebsLocator loc = new WebsLocator();
			loc.setWebsSoapEndpointAddress(endpoint);
			final Webs service = loc;

			try {
				stub = (WebsSoap_BindingStub) service.getWebsSoap();
			} catch (final ServiceException e) {
				LOGGER.log(Level.WARNING,e.getMessage(),e);
				throw new SharepointException("Unable to create webs stub");
			}

			final String strDomain = inSharepointClientContext.getDomain();
			String strUser = inSharepointClientContext.getUsername();
			final String strPassword= inSharepointClientContext.getPassword();

			strUser = Util.getUserNameWithDomain(strUser, strDomain);						
			stub.setUsername(strUser);
			stub.setPassword(strPassword);			
		}
	}

	/**
	 * Discovers all the sites from the current site collection which are in hierarchy lower to the current web.
	 * @return The set of child sites
	 */
	public Set<String> getDirectChildsites() {
		final Set<String> allWebsList = new TreeSet<String>();// to store all the sub-webs state

		GetWebCollectionResponseGetWebCollectionResult webcollnResult=null;

		try {
			webcollnResult= stub.getWebCollection();			
		} catch(final AxisFault af) { // Handling of username formats for different authentication models.
			if((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1) && (sharepointClientContext.getDomain() != null)) {
				final String username = Util.switchUserNameFormat(stub.getUsername());
				LOGGER.log(Level.INFO,"Web Service call failed for username [ " + stub.getUsername()+" ].");
				LOGGER.log(Level.INFO,"Trying with " + username);
				stub.setUsername(username);
				try {
					webcollnResult= stub.getWebCollection();						
				} catch(final Exception e) {
					LOGGER.log(Level.WARNING,"Unable to get the child webs for the web site [" + sharepointClientContext.getSiteURL() + "].",e);
					return allWebsList;
				}
			} else {
				LOGGER.log(Level.WARNING,"Unable to get the child webs for the web site [" + sharepointClientContext.getSiteURL() + "].",af);
				return allWebsList;					
			}
		} catch (final RemoteException e) {
			LOGGER.log(Level.WARNING,"Unable to get the child webs for the web site [" + sharepointClientContext.getSiteURL() + "].",e);
			return allWebsList;
		}
		
		if(webcollnResult!=null){
			final MessageElement[] meWebs = webcollnResult.get_any();
			if((meWebs!=null)  && (meWebs[0]!=null)){

				final Iterator itWebs = meWebs[0].getChildElements();
				if(itWebs!=null){
					while(itWebs.hasNext()){
						//e.g. <ns1:Web Title="ECSCDemo" Url="http://ps4312.persistent.co.in:2905/ECSCDemo" xmlns:ns1="http://schemas.microsoft.com/sharepoint/soap/"/>
						final MessageElement meWeb = (MessageElement) itWebs.next();
						if(null == meWeb) {
							continue;
						}
						final String url = meWeb.getAttribute("Url");
						if (sharepointClientContext.isIncludedUrl(url)) { 
							allWebsList.add(url);
						}else{
							LOGGER.warning("excluding "+url);								
						}
					}
				}
			}
		}

		return allWebsList;
	}
	
	/**
	 * To get the Web URL from any Page URL of the web
	 * @param pageURL
	 * @return the well formed Web URL to be used for WS calls
	 */
	public String getWebURLFromPageURL(final String pageURL) {
		LOGGER.config("Page URL: "+pageURL);
		
		String strWebURL = null;
		try{
			strWebURL= stub.webUrlFromPageUrl(pageURL);
		} catch(final AxisFault af) { // Handling of username formats for different authentication models.
			if((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1) && (sharepointClientContext.getDomain() != null)) {
				final String username = Util.switchUserNameFormat(stub.getUsername());
				LOGGER.log(Level.INFO,"Web Service call failed for username [ " + stub.getUsername()+" ].");
				LOGGER.log(Level.INFO,"Trying with " + username);
				stub.setUsername(username);
				try {
					strWebURL= stub.webUrlFromPageUrl(pageURL);						
				} catch(final Exception e) {
					strWebURL = Util.getWebURLForWSCall(pageURL);
					LOGGER.log(Level.WARNING,"Unable to get the sharepoint web URL for the URL: [ " + pageURL + " ]. Using [ " + strWebURL + " ] as web URL. ");
					return strWebURL;
				}
			} else {
				strWebURL = Util.getWebURLForWSCall(pageURL);
				LOGGER.log(Level.WARNING,"Unable to get the sharepoint web URL for the URL: [ " + pageURL + " ]. Using [ " + strWebURL + " ] as web URL. ");
				return strWebURL;					
			}
		} catch(final Throwable e){
			strWebURL = Util.getWebURLForWSCall(pageURL);
			LOGGER.log(Level.WARNING, "Unable to get the sharepoint web URL for the URL: [ " + pageURL + " ]. Using [ " + strWebURL + " ] as web URL. ");
			return strWebURL;
		}
		LOGGER.log(Level.INFO, "WebURL: " + strWebURL);
		return strWebURL;
	}	
	
	/**
	 * To get the Web Title of a given web
	 * @param webURL To identiy the web whose Title is to be discovered
	 * @param spType The SharePOint type for this web
	 * @return the web title
	 */
	public String getWebTitle(final String webURL, final String spType) {
		String webTitle = "No Title";
		try{
			LOGGER.config("Getting title for Web: "+webURL+" SharepointConnectorType: "+spType);
						
			if(SPConstants.SP2003.equalsIgnoreCase(spType)){
				final SiteDataWS siteDataWS = new SiteDataWS(sharepointClientContext);
				webTitle=siteDataWS.getTitle();
			}
			else {
				GetWebResponseGetWebResult resWeb = null;
				try {
					resWeb = stub.getWeb(webURL);
				} catch(final AxisFault af) {
					if((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1) && (sharepointClientContext.getDomain() != null)) {
						final String username = Util.switchUserNameFormat(stub.getUsername());
						LOGGER.log(Level.INFO,"Web Service call failed for username [ " + stub.getUsername()+" ].");
						LOGGER.log(Level.INFO,"Trying with " + username);
						stub.setUsername(username);
						try {
							resWeb = stub.getWeb(webURL);						
						} catch(final Exception e) {
							LOGGER.log(Level.WARNING, "Unable to Get Title for web [ "+webURL+" ]. Using the default web Title. ", e);
						}
					} else {
						LOGGER.log(Level.WARNING, "Unable to Get Title for web [ "+webURL+" ]. Using the default web Title. ", af);					
					}
				}
				if(null!=resWeb){
					final MessageElement[] meArray = resWeb.get_any();
					if((meArray!=null) && (meArray[0]!=null)){
						webTitle= meArray[0].getAttribute(SPConstants.WEB_TITLE);//Get the Title
					}
				}
			}
		}catch(final Exception e){
			LOGGER.log(Level.WARNING, "Unable to Get Title for web [ "+webURL+" ]. Using the default web Title. "+e);			
		}
		LOGGER.log(Level.INFO, "Title: " + webTitle);
		return webTitle;
	}
	
	/**
	 * For checking the Web Service connectivity 
	 * @return the Web Service connectivity status
	 */
	public String checkConnectivity() {
		try {
			stub.getWebCollection(); //at least contribute permission is required. Fails in case of SP2003 if the url url contains repeated slashes.
		} catch(final AxisFault af) {
			if(SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) == -1) {
				// This is not an unauthorization exception. Do not retry with different username format.
				LOGGER.log(Level.WARNING, "Unable to connect.", af);
				return af.getFaultString();
			}
			final String username = Util.switchUserNameFormat(stub.getUsername());
			if((username == null) || username.equals(stub.getUsername())) {
				LOGGER.log(Level.WARNING, "Unable to connect.", af);
				return af.getFaultString();
			}
			LOGGER.log(Level.INFO,"Web Service call failed for username [ " + stub.getUsername()+" ].");
			LOGGER.log(Level.INFO,"Trying with " + username);			
			stub.setUsername(username);
			try {
				stub.getWebCollection();						
			} catch(final Exception e) {
				LOGGER.log(Level.WARNING, "Unable to connect.", e);
				return e.getLocalizedMessage();
			}			
		} catch(final Exception e) {
			LOGGER.log(Level.WARNING, "Unable to connect.", e);
			return e.getLocalizedMessage();
		}
		
		return SPConstants.CONNECTIVITY_SUCCESS;
	}
}
