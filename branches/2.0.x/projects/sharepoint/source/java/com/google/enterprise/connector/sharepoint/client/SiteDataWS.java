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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.holders.StringHolder;

import org.apache.axis.holders.UnsignedIntHolder;
import org.apache.catalina.util.URLEncoder;
import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.sharepoint.generated.sitedata.SiteData;
import com.google.enterprise.connector.sharepoint.generated.sitedata.SiteDataLocator;
import com.google.enterprise.connector.sharepoint.generated.sitedata.SiteDataSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.generated.sitedata._sList;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOfStringHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOf_sFPUrlHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOf_sListHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOf_sListWithTimeHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOf_sWebWithTimeHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders._sWebMetadataHolder;
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
	private static final String BT_SLIDELIBRARY="SlideLibrary";
	private static final String BT_FORMLIBRARY="FormLibrary";
	private static final String BT_TRANSLATIONMANAGEMENTLIBRARY="TranslationManagementLibrary";
	private static final String BT_REPORTLIBRARY="ReportLibrary";
	private static final String BT_TRANSLATOR="Translator";
	private static final String BT_PROJECTTASK ="ProjectTask";
	private static final String BT_SITESLIST ="SitesList";
	private static final String ORIGINAL_BT_SLIDELIBRARY="2100";
	private static final String ORIGINAL_BT_FORMLIBRARY="XMLForm";
	private static final String ORIGINAL_BT_TRANSLATIONMANAGEMENTLIBRARY="1300";
	private static final String ORIGINAL_BT_REPORTLIBRARY="433";
	private static final String ORIGINAL_BT_TRANSLATOR="1301";
	private static final String ORIGINAL_BT_PROJECTTASK ="GanttTasks";
	private static final String ORIGINAL_BT_SITESLIST ="300";
	private static final String ORIGINAL_BT_LINKS="Links";
	private static final String NO_TEMPLATE="No Template";
	private static final String ATTR_DEFAULTVIEWURL = "DefaultViewUrl";
	private static final String ATTR_DESCRIPTION = "Description";
	private static final String ATTR_TITLE = "Title";

	public static URLEncoder enc  = new URLEncoder();
	static final String URL_SEP ="://";

	private static final Logger LOGGER = Logger.getLogger(SiteDataWS.class.getName());
	private String className = SiteDataWS.class.getName();
	private SharepointClientContext sharepointClientContext;
	private String endpoint;
	SiteDataSoap_BindingStub stub = null;


	private static final String ATTR_READSECURITY = "ReadSecurity";

	//url of the site
	//private String siteRelativeUrl ="";



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
			endpoint = inSharepointClientContext.getProtocol()+URL_SEP + inSharepointClientContext.getHost() + ":" +inSharepointClientContext.getPort() +enc.encode(inSharepointClientContext.getsiteName())+ SITEDATAENDPOINT;
			LOGGER.config(sFunctionName+" : End Point: " +endpoint);

			//set the relativeURL
//			siteRelativeUrl = inSharepointClientContext.getsiteName();

			SiteDataLocator loc = new SiteDataLocator();
			loc.setSiteDataSoapEndpointAddress(endpoint);
			SiteData servInterface = loc;

			try {
				stub = (SiteDataSoap_BindingStub) servInterface.getSiteDataSoap();
			} catch (ServiceException e) {
				LOGGER.config("SiteDataWS: "+e.toString());
				throw new SharepointException("unable to create sitedata stub");
			}

//			set the user and pass
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

	public SiteDataWS(SharepointClientContext inSharepointClientContext,String siteName) throws RepositoryException {
		String sFunctionName = "SiteDataWS(SharepointClientContext sharepointClientContext,String siteName";
		LOGGER.entering(className, sFunctionName);
		if(inSharepointClientContext!=null){
			
			if(siteName!=null){
				//extract the of path of the site and encode it
				//to support internatational site URLs e.g. japanese 
				//e.g."http://ps4312.persistent.co.in:2905/内容"
				URL siteURL ;
				try {
					siteURL = new URL(siteName);

				} catch (MalformedURLException e) {
					LOGGER.config(sFunctionName+": actual error:\n"+e.toString());
					throw new SharepointException("Malformed URL: "+siteName);
				}
				this.sharepointClientContext = inSharepointClientContext;
				sharepointClientContext.setURL(siteName);
				
				int iPort = 0;

				//check if the def
				if (-1 != siteURL.getPort()) {
					iPort = siteURL.getPort();
				}else{
					iPort = siteURL.getDefaultPort();
				}

				endpoint = siteURL.getProtocol()+URL_SEP+ siteURL.getHost()+":"+iPort/*siteURL.getPort()*/+enc.encode(siteURL.getPath())+ SITEDATAENDPOINT;
				//set the relativeURL
//				siteRelativeUrl = siteURL.getPath();
			}

			LOGGER.config(sFunctionName+" : End Point: " +endpoint);
			SiteDataLocator loc = new SiteDataLocator();
			loc.setSiteDataSoapEndpointAddress(endpoint);
			SiteData servInterface = loc;

			try {
				stub = (SiteDataSoap_BindingStub) servInterface.getSiteDataSoap();
			} catch (ServiceException e) {
				LOGGER.config(sFunctionName+": Actual error: \n"+e.toString());
				throw new SharepointException("Unable to create sitedata stub");
			}
//			set the user and pass
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
	 * Gets the collection of all the SPDocument Libraries on the sharepoint 
	 * server.
	 * @return list of BaseList objects.
	 * @throws SharepointException
	 */
	public  List getDocumentLibraries(String parentWebTitle) throws SharepointException {
		return getNamedLists(DOC_LIB,parentWebTitle);
	}

	/**
	 * Gets the collection of all the Generic Lists on the sharepoint server.
	 * @return list of BaseList objects.
	 * @throws SharepointException
	 */
	public  List getGenericLists(String parentWebTitle) throws SharepointException {
		return getNamedLists(GENERIC_LIST,parentWebTitle);
	}

	/**
	 * Gets the collection of all the Issues on the sharepoint server.
	 * @return list of BaseList objects.
	 * @throws SharepointException
	 */
	public  List getIssues(String parentWebTitle) throws SharepointException {
		return getNamedLists(ISSUE,parentWebTitle);
	}

	/**
	 * Gets the collection of all the lists on the sharepoint server which are
	 * of a given type. E.g., DocumentLibrary
	 * @return list of BaseList objects.
	 */
	private List getNamedLists(String baseType,String parentWebTitle) throws SharepointException {
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
					//Amit: check for the base type at the inital stage .. do not proceed further for non matching types
					if((sl[i] != null)){
						if((collator.equals(sl[i].getBaseType(),(baseType)))){
							url = sharepointClientContext.getProtocol()+URL_SEP + sharepointClientContext.getHost() 
							+ ":" + sharepointClientContext.getPort() +sl[i].getDefaultViewUrl();
//							}
							alterUrl =sharepointClientContext.getProtocol()+URL_SEP + sharepointClientContext.getHost()+ sl[i].getDefaultViewUrl();
							strBaseTemplate = sl[i].getBaseTemplate();
							if(strBaseTemplate==null){
								strBaseTemplate = NO_TEMPLATE;
							}else{
								//check for special type of illogical\numeric base templkates
								if(collator.equals(strBaseTemplate, ORIGINAL_BT_SLIDELIBRARY)){//for SlideLibrary
									strBaseTemplate=BT_SLIDELIBRARY;
								}else if(collator.equals(strBaseTemplate, ORIGINAL_BT_FORMLIBRARY)){//for FormLibrary
									strBaseTemplate=BT_FORMLIBRARY;
								}else if(collator.equals(strBaseTemplate,ORIGINAL_BT_TRANSLATIONMANAGEMENTLIBRARY)){//for TranslationManagementLibrary
									strBaseTemplate=BT_TRANSLATIONMANAGEMENTLIBRARY;
								}else if(collator.equals(strBaseTemplate, ORIGINAL_BT_TRANSLATOR)){//for Translator
									strBaseTemplate=BT_TRANSLATOR;
								}else if(collator.equals(strBaseTemplate, ORIGINAL_BT_REPORTLIBRARY)){//for ReportLibrary
									strBaseTemplate=BT_REPORTLIBRARY;
								}else if(collator.equals(strBaseTemplate, ORIGINAL_BT_PROJECTTASK)){//for ReportLibrary
									strBaseTemplate=BT_PROJECTTASK;
								}else if(collator.equals(strBaseTemplate, ORIGINAL_BT_SITESLIST)){//for ReportLibrary
									strBaseTemplate=BT_SITESLIST;
								}
							}
						}else{
							continue;
						}
					}
					LOGGER.config(sFunctionName +"  : URL :"+url);
					if(spUtils.isIncludedUrl(includedURLs,excludedURLs, url) || spUtils.isIncludedUrl(includedURLs,excludedURLs, alterUrl)) {	  
						LOGGER.config(sFunctionName+": included URL :["+url+"]");
						try { 
							BaseList list = new BaseList(sl[i].getInternalName(), 
									sl[i].getTitle(), sl[i].getBaseType(),
									Util.siteDataStringToCalendar(sl[i].getLastModified()),strBaseTemplate,url,parentWebTitle);

							//Add attribute: ListsConst
							//Note: It is Important for the baseType="Lists"
							//For baseType="DocumentLibrary" do not care
							//if(collator.equals(sl[i].getBaseType(),GENERIC_LIST)){
							if(collator.equals(sl[i].getBaseType(),GENERIC_LIST)
									||collator.equals(sl[i].getBaseType(),ISSUE)
									||collator.equals(sl[i].getBaseType(),SURVEYS)
									||collator.equals(sl[i].getBaseType(),DISCUSSION_BOARD))									
							{
								String listUrl = sl[i].getDefaultViewUrl();//e.g. /sites/abc/Lists/Announcements/AllItems.aspx
								if((listUrl!=null) /*&& (siteRelativeUrl!=null)*/){
									StringTokenizer strTokList = new StringTokenizer(listUrl,"/");
									if(null!=strTokList){
										String myNewListConst ="";

										while((strTokList.hasMoreTokens())  &&  (strTokList.countTokens()>1)){
											String listToken = strTokList.nextToken();
											if(null!=listToken){
												myNewListConst+="/"+listToken;
											}	
										}//while(strTokList.hasMoreTokens()){
										list.setListConst(myNewListConst);
									}
								}
							}

							//add the attribute(Metadata to the list )
							list = getListWithAllAttributes(list,sl[i]);
							listCollection.add(list);
//							}

						} catch (ParseException e) {
							LOGGER.log(Level.WARNING,className+":"+sFunctionName,e);
							throw new SharepointException(e.toString());
						}
					}else{
						LOGGER.warning(sFunctionName+" : excluding "+url.toString());
					}

					//Sort the base list
					Collections.sort(listCollection);
					dumpcollection(listCollection);
				}
			}
		}catch (Throwable e) {
			LOGGER.log(Level.WARNING,className+":"+sFunctionName,e);
			throw new SharepointException(e.toString());
		} 

		if(listCollection!=null){
			LOGGER.info("Total Lists returned: "+listCollection.size()+" for list of type: "+baseType);
		}
		LOGGER.exiting(className, sFunctionName);
		return listCollection;
	}

	private BaseList getListWithAllAttributes(BaseList list, _sList documentLibrary) {
		if((list==null)||(documentLibrary==null)){
			return list;
		}

		String str = "";
		str = documentLibrary.getDefaultViewUrl();
		if((str!=null)&&(!str.trim().equals(""))){
			list.setAttribute(ATTR_DEFAULTVIEWURL, str);
		}
		str = "";
		str = documentLibrary.getDescription();
		if((str!=null)&&(!str.trim().equals(""))){
			list.setAttribute(ATTR_DESCRIPTION, str);
		}
		str = "";
		str = documentLibrary.getTitle();
		if((str!=null)&&(!str.trim().equals(""))){
			list.setAttribute(ATTR_TITLE, str);
		}
		str = "";
		str += documentLibrary.getReadSecurity();
		if((str!=null)&&(!str.trim().equals(""))){
			list.setAttribute(ATTR_READSECURITY, str);
		}

		return list;
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

	public TreeSet getAllLinks(SharepointClientContext inSharepointClientContext,String site,String webTitle) throws SharepointException{
		String sFunctionName = "getAllLinks(SharepointClientContext sharepointClientContext,String site)";
		LOGGER.entering(className, sFunctionName);

		if(stub==null){
			throw new SharepointException(sFunctionName+": Unable to get the sitedata stub");
		}

		//ArrayList allLinks = new ArrayList();
		TreeSet allLinks = new TreeSet();
		Collator collator = SharepointConnectorType.getCollator();
		LOGGER.config(sFunctionName+": Getting all links for site ["+site+"]");

		try {
			ListsWS listsWS = null;

			UnsignedIntHolder getListCollectionResult = new UnsignedIntHolder();
			ArrayOf_sListHolder vLists = new ArrayOf_sListHolder();
			stub.getListCollection(getListCollectionResult, vLists);

			if(vLists==null){
				throw new SharepointException("Unable to get the lists");
			}
			_sList[] sl = vLists.value;

			if (sl != null) {//check out the links from other list collection 
				for(int i=0;i<sl.length;++i){
					if ((collator.equals(sl[i].getBaseTemplate(),ORIGINAL_BT_LINKS)) || (collator.equals(sl[i].getBaseTemplate(),ORIGINAL_BT_SITESLIST))) {
						String url = null;		
						String alterUrl = null; 
						SharepointClientUtils spUtils = new SharepointClientUtils();   
						String[] includedURLs = inSharepointClientContext.getIncludedURlList() ; 
						String[] excludedURLs = inSharepointClientContext.getExcludedURlList() ; 

						url = inSharepointClientContext.getProtocol()+URL_SEP + inSharepointClientContext.getHost() 
						+ ":" + inSharepointClientContext.getPort() +sl[i].getDefaultViewUrl();

						alterUrl =inSharepointClientContext.getProtocol()+URL_SEP + inSharepointClientContext.getHost()+ sl[i].getDefaultViewUrl();

						if(spUtils.isIncludedUrl(includedURLs,excludedURLs, url) || spUtils.isIncludedUrl(includedURLs,excludedURLs, alterUrl)) {	  
							LOGGER.config(sFunctionName+": included URL :["+url+"]");	
							BaseList list = new BaseList(sl[i].getInternalName(), 
									sl[i].getTitle(), sl[i].getBaseType(),
									Util.siteDataStringToCalendar(sl[i].getLastModified()),"Links",url,webTitle); 

							listsWS=new ListsWS(inSharepointClientContext, site);
							//get all the items for the links
							TreeSet listItems = (TreeSet)listsWS.getLinkChanges(list);
							allLinks.addAll(listItems);
						}else{
							LOGGER.warning(sFunctionName+" : excluding "+url.toString());
						}
					}
				}
			}
		}catch (Throwable e){
			LOGGER.log(Level.WARNING,sFunctionName+": "+e.getLocalizedMessage(),e);
		}
		if(allLinks!=null){
			LOGGER.info("Links returned: "+allLinks.size());
		}

		LOGGER.exiting(className, sFunctionName);
		return allLinks;
	}


	/**
	 * Gets the collection of all the DiscussionBoards on the sharepoint server.
	 * @return list of BaseList objects.
	 * @throws SharepointException
	 */
	public List getDiscussionBoards(String parentWebTitle) throws SharepointException {
		return getNamedLists(DISCUSSION_BOARD,parentWebTitle);
	}

	/**
	 * Gets the collection of all the Surveys on the sharepoint server.
	 * @return list of BaseList objects.
	 * @throws SharepointException 
	 */
	public List getSurveys(String parentWebTitle) throws SharepointException {
		return getNamedLists(SURVEYS,parentWebTitle);
	}
	
	/* Added By Nitendra
	 * 
	 * Retrieves the title of a Web Site.
	 * Should only be used in case of SP2003 Top URL. For all other cases, WebWS.getTitle() is the preffered method. 
	 */
	public String getTitle() throws RemoteException {
		UnsignedIntHolder getWebResult=new UnsignedIntHolder();
		_sWebMetadataHolder sWebMetadata=new _sWebMetadataHolder();
		ArrayOf_sWebWithTimeHolder vWebs=new ArrayOf_sWebWithTimeHolder();
		ArrayOf_sListWithTimeHolder vLists=new ArrayOf_sListWithTimeHolder();
		ArrayOf_sFPUrlHolder vFPUrls=new ArrayOf_sFPUrlHolder();
		StringHolder strRoles=new StringHolder();
		ArrayOfStringHolder vRolesUsers=new ArrayOfStringHolder();
		ArrayOfStringHolder vRolesGroups=new ArrayOfStringHolder();
		stub.getWeb(getWebResult, sWebMetadata, vWebs, vLists, vFPUrls, strRoles, vRolesUsers, vRolesGroups);
		return sWebMetadata.value.getTitle();
	}

}



