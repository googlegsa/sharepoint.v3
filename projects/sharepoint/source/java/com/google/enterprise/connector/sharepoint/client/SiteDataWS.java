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

import java.text.Collator;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.sharepoint.generated.SiteDataStub;
import com.google.enterprise.connector.sharepoint.generated.SiteDataStub.ArrayOf_sList;
import com.google.enterprise.connector.sharepoint.generated.SiteDataStub.ArrayOf_sWebWithTime;
import com.google.enterprise.connector.sharepoint.generated.SiteDataStub._sList;
import com.google.enterprise.connector.sharepoint.generated.SiteDataStub._sWebWithTime;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * This class holds data and methods for any call to SiteData web service.
 *
 */
public class SiteDataWS {   

	private static final String SITEDATAENDPOINT = "_vti_bin/SiteData.asmx";
	public static final String DOC_LIB = "DocumentLibrary";
	public static final String GENERIC_LIST = "GenericList";
	public static final String ISSUE = "Issue";
	final String URL_SEP ="://";
	private static Log logger = LogFactory.getLog(SiteDataWS.class);
	private SharepointClientContext sharepointClientContext;
	private String endpoint;
	private SiteDataStub stub;

	public SiteDataWS(SharepointClientContext inSharepointClientContext)throws RepositoryException {
		String sFunctionName = "SiteDataWS(SharepointClientContext sharepointClientContext)";
		if(inSharepointClientContext!=null){
			this.sharepointClientContext = inSharepointClientContext;
			endpoint = inSharepointClientContext.getProtocol()+URL_SEP + inSharepointClientContext.getHost() + ":" +inSharepointClientContext.getPort() +Util.getEscapedSiteName(inSharepointClientContext.getsiteName())+ SITEDATAENDPOINT;
	
			logger.debug(sFunctionName+" : End Point" +endpoint);
			try {
				stub = new SiteDataStub(endpoint);
				inSharepointClientContext.setStubWithAuth(stub, endpoint);
			} catch (AxisFault e) {
				throw new SharepointException(e.toString());
			}     
		}
	}

	public SiteDataWS(SharepointClientContext inSharepointClientContext,String siteName) throws RepositoryException {
		final String HTTP= "http";
		final String HTTPS= "https";
		String sFunctionName = "SiteDataWS(SharepointClientContext sharepointClientContext,String siteName";
		if(inSharepointClientContext!=null){
			this.sharepointClientContext = inSharepointClientContext;
			if(siteName!=null){
				if (siteName.startsWith(HTTP+URL_SEP)) {
					siteName = siteName.substring(7);
					endpoint = HTTP+URL_SEP + Util.getEscapedSiteName(siteName) + SITEDATAENDPOINT;
				}else if(siteName.startsWith(HTTPS+URL_SEP)) {
					siteName = siteName.substring(8);
					endpoint = HTTPS+URL_SEP + Util.getEscapedSiteName(siteName) + SITEDATAENDPOINT;
				}
				else {
					endpoint = Util.getEscapedSiteName(siteName) + SITEDATAENDPOINT;
				}
			}
			logger.debug(sFunctionName+" : End Point" +endpoint);
			try {
				stub = new SiteDataStub(endpoint);
				inSharepointClientContext.setStubWithAuth(stub, endpoint);
			} catch (AxisFault e) {
				throw new SharepointException(e.toString());
			}     
		}
	}  

	/**
	 * Gets all the sites from the sharepoint server.
	 * @return list of sharepoint documents corresponding to sites.
	 */
	public List getAllChildrenSites() throws SharepointException {
		String sFunctionName = "getAllChildrenSites()";
		ArrayList sites = new ArrayList();
		try {
			SiteDataStub.GetSite req = new SiteDataStub.GetSite();
			SiteDataStub.GetSiteResponse res = stub.GetSite(req);
			ArrayOf_sWebWithTime webs = res.getVWebs();
			_sWebWithTime[] els = webs.get_sWebWithTime();
			
			if(els!=null){
				for (int i = 0; i < els.length; ++i) {        
					String url = els[i].getUrl(); 
					logger.debug(sFunctionName+": URL :"+url);
					String[] includedURLs = sharepointClientContext.getIncludedURlList(); 
					String[] excludedURLs = sharepointClientContext.getExcludedURlList() ; 
					SharepointClientUtils spUtils = new SharepointClientUtils();	
	
					//two cases: with port and without port taken ... because if port 80 then the port no: may be skipped by sharepoint to get the contents
					if ((url.startsWith(sharepointClientContext.getProtocol()+URL_SEP + sharepointClientContext.getHost()+ ":" + sharepointClientContext.getPort()+sharepointClientContext.getsiteName()) 
							|| url.startsWith(sharepointClientContext.getProtocol()+URL_SEP + sharepointClientContext.getHost() 
									+ sharepointClientContext.getsiteName())) && (spUtils.isIncludedUrl(includedURLs,excludedURLs, url))) { 
						logger.debug(sFunctionName+" : include URL ["+url.toString()+"]");
						Calendar lastModified = els[i].getLastModified();   
	
						SPDocument doc = new SPDocument(url, url, lastModified,SPDocument.OBJTYPE_WEB);
						
						sites.add(doc);
					}
				}  
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
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
		if(baseType==null){
			throw new SharepointException("Unable to get the baseType");
		}
		ArrayList listCollection = new ArrayList(); 
		Collator collator = SharepointConnectorType.getCollator();
		try {
			SiteDataStub.GetListCollection req =new SiteDataStub.GetListCollection();
			SiteDataStub.GetListCollectionResponse res;
			res = stub.GetListCollection(req);
			
			//amit added this for null check
			if(res==null){
				throw new SharepointException("Unable to get the list collection");
			}
				
			ArrayOf_sList asl = res.getVLists();
			_sList[] sl = asl.get_sList();
			
			if (sl != null) {
				String[] includedURLs = sharepointClientContext.getIncludedURlList() ; 
				String[] excludedURLs = sharepointClientContext.getExcludedURlList() ; 
				SharepointClientUtils spUtils = new SharepointClientUtils();   
				for (int i = 0; i < sl.length; i++) {
					String url = null;		
					String alterUrl = null; 
//					two cases: with port and without port taken ... because if port 80 then the port no: may be skipped by sharepoint to get the contents
					if(sl[i] != null){
						url = sharepointClientContext.getProtocol()+URL_SEP + sharepointClientContext.getHost() 
						+ ":" + sharepointClientContext.getPort() +sl[i].getDefaultViewUrl();
						alterUrl =sharepointClientContext.getProtocol()+URL_SEP + sharepointClientContext.getHost()+ sl[i].getDefaultViewUrl();
					}
					logger.debug(sFunctionName +"  : URL :"+url);
					if(spUtils.isIncludedUrl(includedURLs,excludedURLs, url) || spUtils.isIncludedUrl(includedURLs,excludedURLs, alterUrl)) {	  
						logger.debug(sFunctionName+": included URL :["+url+"]");
						try { 
							if (collator.equals(sl[i].getBaseType(),(baseType))) {
								BaseList list = new BaseList(sl[i].getInternalName(), 
										sl[i].getTitle(), sl[i].getBaseType(),
										Util.siteDataStringToCalendar(sl[i].getLastModified()));              
								listCollection.add(list);
							}
						} catch (ParseException e) {
							throw new SharepointException(e.toString());
						}
					}
					Collections.sort(listCollection);
					dumpcollection(listCollection);
				}
			}
		}catch (Exception e) {
			throw new SharepointException(e.toString());
		}           
		return listCollection;
	}

	//for debugging purpose
	private void dumpcollection(ArrayList colln){
		if(colln==null){
			return;
		}
		logger.debug("-----------------------------------");
		for(int i=0;i<colln.size();++i){
			BaseList list = (BaseList) colln.get(i);
			logger.debug("Internal Name: "+list.getInternalName());
			logger.debug("Title: "+list.getTitle());
			logger.debug("Type: "+list.getType());
			logger.debug("Type: "+list.getLastMod());
		}
		logger.debug("-----------------------------------");
	}

	public List getAllLinks(SharepointClientContext inSharepointClientContext,String site){
		String sFunctionName = "getAllLinks(SharepointClientContext sharepointClientContext,String site)";
		ArrayList allLinks = new ArrayList();
		Collator collator = SharepointConnectorType.getCollator();
		logger.debug(sFunctionName+": site : "+site);
		try {
			ListsWS listsWS = new ListsWS(inSharepointClientContext, site);
			SiteDataStub.GetListCollection req = new SiteDataStub.GetListCollection();
			SiteDataStub.GetListCollectionResponse res;
			res = stub.GetListCollection(req);
			ArrayOf_sList asl = res.getVLists();
			_sList[] sl = asl.get_sList();
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
						logger.debug(sFunctionName +"  : URL :"+url);
						if(spUtils.isIncludedUrl(includedURLs,excludedURLs, url) || spUtils.isIncludedUrl(includedURLs,excludedURLs, alterUrl)) {	  

							logger.debug(sFunctionName+": included URL :["+url+"]");	
							BaseList list = new BaseList(sl[i].getInternalName(), 
									sl[i].getTitle(), sl[i].getBaseType(),
									Util.siteDataStringToCalendar(sl[i].getLastModified())); 

							//get all the items for the links
							List listItems = listsWS.getLinkChanges(list);
							allLinks.addAll(listItems);
						}
					}
				}
			}
		}catch (Exception e){
			logger.warn(sFunctionName+": "+e.getLocalizedMessage());
		}           
		return allLinks;
	}
}



