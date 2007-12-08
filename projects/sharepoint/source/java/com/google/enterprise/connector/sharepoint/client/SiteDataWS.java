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

//import java.io.UnsupportedEncodingException;
//import java.net.URLEncoder;
import java.net.MalformedURLException;
import java.text.Collator;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.holders.StringHolder;

import org.apache.axis.holders.UnsignedIntHolder;
import org.apache.catalina.util.URL;
import org.apache.catalina.util.URLEncoder;

import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.sharepoint.generated.sitedata.SiteData;
import com.google.enterprise.connector.sharepoint.generated.sitedata.SiteDataLocator;
import com.google.enterprise.connector.sharepoint.generated.sitedata.SiteDataSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.generated.sitedata._sList;
import com.google.enterprise.connector.sharepoint.generated.sitedata._sWebWithTime;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOfStringHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOf_sListHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOf_sWebWithTimeHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders._sSiteMetadataHolder;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * This class holds data and methods for any call to SiteData web service.
 * @author amit_kagrawal
 */
public class SiteDataWS {   

	private static final String SITEDATAENDPOINT = "/_vti_bin/SiteData.asmx";
	public static final String DOC_LIB = "DocumentLibrary";//BaseType=1 (In SPS2003)
	public static final String DISCUSSION_BOARD = "DiscussionBoard";//BaseType=3 (In SPS2003)
	public static final String SURVEYS = "Survey"; //BaseType=4 (In SPS2003)
	public static final String GENERIC_LIST = "GenericList";//BaseType=0 (In SPS2003)
	public static final String ISSUE = "Issue";//BaseType=5 (In SPS2003)
	public static URLEncoder enc  = new URLEncoder();
	static final String URL_SEP ="://";

//	private static Log logger = LogFactory.getLog(SiteDataWS.class);
	private static final Logger LOGGER = Logger.getLogger(SiteDataWS.class.getName());
	private String className = SiteDataWS.class.getName();
	private SharepointClientContext sharepointClientContext;
	private String endpoint;
//	SiteDataSoap12Stub stub = null;
	SiteDataSoap_BindingStub stub = null;

	//set the content length of the HTTPResponse to a huge value
	static{
		//set getResponseHeaderGroup().getHeaders("Content-Length"); to Long.MAX_VALUE


		//set the URLEncoder safe characters
		enc.addSafeCharacter('/');
		enc.addSafeCharacter(':');// required when endpoint is set using specified site
	}


	public SiteDataWS(SharepointClientContext inSharepointClientContext)throws RepositoryException {
		String sFunctionName = "SiteDataWS(SharepointClientContext sharepointClientContext)";
		LOGGER.entering(className, sFunctionName);
		if(inSharepointClientContext!=null){
			this.sharepointClientContext = inSharepointClientContext;
//			String strsite =inSharepointClientContext.getsiteName().substring(1); 
			endpoint = inSharepointClientContext.getProtocol()+URL_SEP + inSharepointClientContext.getHost() + ":" +inSharepointClientContext.getPort() +enc.encode(inSharepointClientContext.getsiteName())+ SITEDATAENDPOINT;
//			endpoint= endpoint.replaceAll("%3A", ":");
//			endpoint =endpoint.replaceAll("%2F", "/");
			LOGGER.config(sFunctionName+" : End Point" +endpoint);

			SiteDataLocator loc = new SiteDataLocator();
//			loc.setSiteDataSoap12EndpointAddress(endpoint);
			loc.setSiteDataSoapEndpointAddress(endpoint);
			SiteData servInterface = loc;

			try {
//				stub = (SiteDataSoap_BindingStub) serv_interface.getSiteDataSoap12();
				stub = (SiteDataSoap_BindingStub) servInterface.getSiteDataSoap();
			} catch (ServiceException e) {
				LOGGER.config("SiteDataWS: "+e.toString());
				throw new SharepointException("unable to create sitedata stub");
			}

//			set the user and pass
			String strDomain = inSharepointClientContext.getDomain();
			String strUser = inSharepointClientContext.getUsername();
			String strPassword= inSharepointClientContext.getPassword();
			strDomain+="\\"+strUser; // form domain/user 

			/*//added by amit
				final String namespace ="http://schemas.microsoft.com/sharepoint/soap/";
//				final String prefix ="ms";
				//s.setHeader("http://my.name.space/headers", "mysecurityheader", "This guy is OK");
				String str = ""+Long.MAX_VALUE;
//				stub.setHeader(namespace,"Content-Length", str );
				stub.setHeader(namespace,"http.method.response.maximum.size", str );
			 */

			//set the user and pass
			stub.setUsername(strDomain);
			stub.setPassword(strPassword);
		}
		LOGGER.exiting(className, sFunctionName);
	}

	public SiteDataWS(SharepointClientContext inSharepointClientContext,String siteName) throws RepositoryException {
//		final String sHTTP= "http";
//		final String sHTTPS= "https";
		String sFunctionName = "SiteDataWS(SharepointClientContext sharepointClientContext,String siteName";
		LOGGER.entering(className, sFunctionName);
		if(inSharepointClientContext!=null){
			this.sharepointClientContext = inSharepointClientContext;
			if(siteName!=null){
				//extract the of path of the site and encode it
				//to support internatational site URLs e.g. japanese 
				//e.g."http://ps4312.persistent.co.in:2905/内容"
				URL siteURL ;
				try {
					siteURL = new URL(siteName);
				} catch (MalformedURLException e) {
					throw new SharepointException("Malformed URL: "+siteName);
				}
				endpoint = siteURL.getProtocol()+URL_SEP+ siteURL.getHost()+":"+siteURL.getPort()+enc.encode(siteURL.getPath())+ SITEDATAENDPOINT;
				/*if (siteName.startsWith(sHTTP+URL_SEP)) {
					siteName = siteName.substring(7);
						//endpoint = sHTTP+URL_SEP +siteName+ SITEDATAENDPOINT;
						endpoint = sHTTP+URL_SEP +enc.encode(siteName)  + SITEDATAENDPOINT;

						//note: we do not want ":" and "/" to be encoded
						//so replace the equivalent encoding with ":" and "/" wherever applicable
//						endpoint= endpoint.replaceAll("%3A", ":");
//						endpoint =endpoint.replaceAll("%2F", "/");
				}else if(siteName.startsWith(sHTTPS+URL_SEP)) {
					siteName = siteName.substring(8);
						endpoint = sHTTPS+URL_SEP + enc.encode(siteName) + SITEDATAENDPOINT;
//						endpoint = sHTTPS+URL_SEP + siteName + SITEDATAENDPOINT;
//						endpoint= endpoint.replaceAll("%3A", ":");
//						endpoint =endpoint.replaceAll("%2F", "/");
				}else {
//					endpoint = Util.getEscapedSiteName(siteName) + SITEDATAENDPOINT;

											endpoint = enc.encode(siteName)+ SITEDATAENDPOINT;
//											endpoint = siteName+ SITEDATAENDPOINT;
//										endpoint= endpoint.replaceAll("%3A", ":");
//										endpoint =endpoint.replaceAll("%2F", "/");
				}*/
			}
			//System.out.println("sws2: "+endpoint);
			LOGGER.config(sFunctionName+" : End Point" +endpoint);
			SiteDataLocator loc = new SiteDataLocator();
//			loc.setSiteDataSoap12EndpointAddress(endpoint);
			loc.setSiteDataSoapEndpointAddress(endpoint);
			SiteData servInterface = loc;

			try {
				stub = (SiteDataSoap_BindingStub) servInterface.getSiteDataSoap();
			} catch (ServiceException e) {
				LOGGER.config("SiteDataWS: "+e.toString());
				throw new SharepointException("Unable to create sitedata stub");
			}
//			set the user and pass
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
	 * Gets all the sites from the sharepoint server.
	 * @return list of sharepoint documents corresponding to sites.
	 */
	public List getAllChildrenSites() throws SharepointException {
		String sFunctionName = "getAllChildrenSites()";
		LOGGER.entering(className, sFunctionName);
		if(stub==null){
			throw new SharepointException(sFunctionName+": Unable to get the sitedata stub");
		}

		ArrayList sites = new ArrayList();
		try {

			ArrayOfStringHolder vGroups = new ArrayOfStringHolder();
			_sSiteMetadataHolder sSiteMetadata = new _sSiteMetadataHolder();
			StringHolder strUsers = new StringHolder();
			UnsignedIntHolder getSiteResult = new UnsignedIntHolder();
			StringHolder strGroups = new StringHolder();
			ArrayOf_sWebWithTimeHolder vWebs = new ArrayOf_sWebWithTimeHolder();
			stub.getSite(getSiteResult, sSiteMetadata, vWebs, strUsers, strGroups, vGroups);



			_sWebWithTime[] els  =vWebs.value;

			if(els!=null){
				for (int i = 0; i < els.length; ++i) {        
					String url = els[i].getUrl(); 
					LOGGER.config(sFunctionName+": URL :"+url);
					String[] includedURLs = sharepointClientContext.getIncludedURlList(); 
					String[] excludedURLs = sharepointClientContext.getExcludedURlList() ; 
					SharepointClientUtils spUtils = new SharepointClientUtils();	

					//two cases: with port and without port taken ... because if port 80 then the port no: may be skipped by sharepoint to get the contents
					if ((url.startsWith(sharepointClientContext.getProtocol()+URL_SEP + sharepointClientContext.getHost()+ ":" + sharepointClientContext.getPort()+sharepointClientContext.getsiteName()) 
							|| url.startsWith(sharepointClientContext.getProtocol()+URL_SEP + sharepointClientContext.getHost() 
									+ sharepointClientContext.getsiteName())) && (spUtils.isIncludedUrl(includedURLs,excludedURLs, url))) { 
						//System.out.println(sFunctionName+" : include URL ["+url.toString()+"]");
						LOGGER.config(sFunctionName+" : include URL ["+url.toString()+"]");
						Calendar lastModified = els[i].getLastModified();   
//						url = enc.encode(url);
//						url= url.replaceAll("%3A", ":");
//						url =url.replaceAll("%2F", "/");

						SPDocument doc = new SPDocument(url, url, lastModified,SPDocument.OBJTYPE_WEB);
						sites.add(doc);
					}else{
						LOGGER.warning(sFunctionName+" : excluding "+url.toString());
					}
				}  
			}
		} catch (Throwable e) {
			LOGGER.warning(sFunctionName+": Unable to access URL["+endpoint+"]");
			LOGGER.finer(e.toString());
			throw new SharepointException(e.toString());
		}
		LOGGER.exiting(className, sFunctionName);
		return sites;      
	}

	/**
	 * Gets the collection of all the SPDocument Libraries on the sharepoint 
	 * server.
	 * @return list of BaseList objects.
	 * @throws SharepointException
	 */
	public  List getDocumentLibraries() throws SharepointException {
		return getNamedLists(DOC_LIB);
	}

	/**
	 * Gets the collection of all the Generic Lists on the sharepoint server.
	 * @return list of BaseList objects.
	 * @throws SharepointException
	 */
	public  List getGenericLists() throws SharepointException {
		return getNamedLists(GENERIC_LIST);
	}

	/**
	 * Gets the collection of all the Issues on the sharepoint server.
	 * @return list of BaseList objects.
	 * @throws SharepointException
	 */
	public  List getIssues() throws SharepointException {
		return getNamedLists(ISSUE);
	}

	/**
	 * Gets the collection of all the lists on the sharepoint server which are
	 * of a given type. E.g., DocumentLibrary
	 * @return list of BaseList objects.
	 */
	private List getNamedLists(String baseType) throws SharepointException {
		String sFunctionName = "getNamedLists(String baseType)";
		LOGGER.entering(className, sFunctionName);
		if(baseType==null){
			throw new SharepointException("Unable to get the baseType");
		}
		if(stub==null){
			throw new SharepointException(sFunctionName+": Unable to get the sitedata stub");
		}

		ArrayList listCollection = new ArrayList(); 
		Collator collator = SharepointConnectorType.getCollator();
		try {

			ArrayOf_sListHolder vLists = new ArrayOf_sListHolder();
			UnsignedIntHolder getListCollectionResult = new UnsignedIntHolder();
			stub.getListCollection(getListCollectionResult, vLists);

			if(vLists==null){
				throw new SharepointException("Unable to get the list collection");
			}

			_sList[] sl = vLists.value;

			if (sl != null) {
				String[] includedURLs = sharepointClientContext.getIncludedURlList() ; 
				String[] excludedURLs = sharepointClientContext.getExcludedURlList() ; 
				SharepointClientUtils spUtils = new SharepointClientUtils();   
				for (int i = 0; i < sl.length; i++) {
					String url = null;		
					String alterUrl = null;
					String strBaseTemplate =null;
//					two cases: with port and without port taken ... because if port 80 then the port no: may be skipped by sharepoint to get the contents
					if(sl[i] != null){
						url = sharepointClientContext.getProtocol()+URL_SEP + sharepointClientContext.getHost() 
						+ ":" + sharepointClientContext.getPort() +sl[i].getDefaultViewUrl();
						alterUrl =sharepointClientContext.getProtocol()+URL_SEP + sharepointClientContext.getHost()+ sl[i].getDefaultViewUrl();
						strBaseTemplate = sl[i].getBaseTemplate();
						if(strBaseTemplate==null){
							strBaseTemplate = "No Template";
						}
					}
					LOGGER.config(sFunctionName +"  : URL :"+url);
					if(spUtils.isIncludedUrl(includedURLs,excludedURLs, url) || spUtils.isIncludedUrl(includedURLs,excludedURLs, alterUrl)) {	  
						LOGGER.config(sFunctionName+": included URL :["+url+"]");
						try { 
							if (collator.equals(sl[i].getBaseType(),(baseType))) {
								BaseList list = new BaseList(sl[i].getInternalName(), 
										sl[i].getTitle(), sl[i].getBaseType(),
										Util.siteDataStringToCalendar(sl[i].getLastModified()),strBaseTemplate);              
								listCollection.add(list);
							}
						} catch (ParseException e) {
							throw new SharepointException(e.toString());
						}
					}else{
						LOGGER.warning(sFunctionName+" : excluding "+url.toString());
					}
					Collections.sort(listCollection);
					dumpcollection(listCollection);
				}
			}
		}catch (Throwable e) {
			throw new SharepointException(e.toString());
		} 
		LOGGER.exiting(className, sFunctionName);
		return listCollection;
	}

	//for debugging purpose
	private void dumpcollection(ArrayList colln){
		if(colln==null){
			return;
		}
		LOGGER.config("-----------------------------------");
		for(int i=0;i<colln.size();++i){
			BaseList list = (BaseList) colln.get(i);
			LOGGER.config("Internal Name: "+list.getInternalName());
			LOGGER.config("Title: "+list.getTitle());
			LOGGER.config("Type: "+list.getType());
			LOGGER.config("Type: "+list.getLastMod());
		}
		LOGGER.config("-----------------------------------");
	}

	public List getAllLinks(SharepointClientContext inSharepointClientContext,String site) throws SharepointException{
		String sFunctionName = "getAllLinks(SharepointClientContext sharepointClientContext,String site)";
		LOGGER.entering(className, sFunctionName);
		if(stub==null){
			throw new SharepointException(sFunctionName+": Unable to get the sitedata stub");
		}
		ArrayList allLinks = new ArrayList();
		Collator collator = SharepointConnectorType.getCollator();
		LOGGER.config(sFunctionName+": site : "+site);
		try {
			ListsWS listsWS = new ListsWS(inSharepointClientContext, site);

			UnsignedIntHolder getListCollectionResult = new UnsignedIntHolder();
			ArrayOf_sListHolder vLists = new ArrayOf_sListHolder();
			stub.getListCollection(getListCollectionResult, vLists);
			if(vLists==null){
				throw new SharepointException("Unable to get the lists");
			}
			_sList[] sl = vLists.value;

			if (sl != null) {//check out the links from other list collection 
				for(int i=0;i<sl.length;++i){
					if (collator.equals(sl[i].getBaseTemplate(),"Links")) {
						String url = null;		
						String alterUrl = null; 
						SharepointClientUtils spUtils = new SharepointClientUtils();   
						String[] includedURLs = inSharepointClientContext.getIncludedURlList() ; 
						String[] excludedURLs = inSharepointClientContext.getExcludedURlList() ; 

						url = inSharepointClientContext.getProtocol()+URL_SEP + inSharepointClientContext.getHost() 
						+ ":" + inSharepointClientContext.getPort() +sl[i].getDefaultViewUrl();

						alterUrl =inSharepointClientContext.getProtocol()+URL_SEP + inSharepointClientContext.getHost()+ sl[i].getDefaultViewUrl();
						LOGGER.config(sFunctionName +"  : URL :"+url);
						if(spUtils.isIncludedUrl(includedURLs,excludedURLs, url) || spUtils.isIncludedUrl(includedURLs,excludedURLs, alterUrl)) {	  

							LOGGER.config(sFunctionName+": included URL :["+url+"]");	
							BaseList list = new BaseList(sl[i].getInternalName(), 
									sl[i].getTitle(), sl[i].getBaseType(),
									Util.siteDataStringToCalendar(sl[i].getLastModified()),"Links"); 

							//get all the items for the links
							List listItems = listsWS.getLinkChanges(list);
							allLinks.addAll(listItems);
						}else{
							LOGGER.warning(sFunctionName+" : excluding "+url.toString());
						}
					}
				}
			}
		}catch (Throwable e){
			LOGGER.warning(sFunctionName+": "+e.getLocalizedMessage());
		}
		LOGGER.exiting(className, sFunctionName);
		return allLinks;
	}


	/**
	 * Gets the collection of all the DiscussionBoards on the sharepoint server.
	 * @return list of BaseList objects.
	 * @throws SharepointException
	 */
	public List getDiscussionBoards() throws SharepointException {
		return getNamedLists(DISCUSSION_BOARD);
	}

	/**
	 * Gets the collection of all the Surveys on the sharepoint server.
	 * @return list of BaseList objects.
	 * @throws SharepointException 
	 */
	public List getSurveys() throws SharepointException {
		return getNamedLists(SURVEYS);
	}
}



