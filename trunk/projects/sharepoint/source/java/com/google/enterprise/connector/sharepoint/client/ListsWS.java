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
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.rpc.ServiceException;

import org.apache.axis.message.MessageElement;
import org.apache.catalina.util.URLEncoder;

import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.generated.lists.GetAttachmentCollectionResponseGetAttachmentCollectionResult;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesResponseGetListItemChangesResult;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesViewFields;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsResponseGetListItemsResult;
import com.google.enterprise.connector.sharepoint.generated.lists.Lists;
import com.google.enterprise.connector.sharepoint.generated.lists.ListsLocator;
import com.google.enterprise.connector.sharepoint.generated.lists.ListsSoap_BindingStub;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;

/**
 * This class holds data and methods for any call to Lists Web Service.
 * @author amit_kagrawal
 */
public class ListsWS {
	private static final String LISTS_END_POINT = "/_vti_bin/Lists.asmx";
	private static final Logger LOGGER = Logger.getLogger(ListsWS.class.getName());
	private String className = ListsWS.class.getName();
	static final String COLON = ":";
	//final String sUniqueID="ows_UniqueId";
//	final String sUniqueID="ows_FileRef";
	final String sModified="ows_Modified";
	final String sFileRef="ows_FileRef";
	final String sMetaInfo="ows_MetaInfo";
	final String sContentType="ows_ContentType";
	final String sEditor="ows_Editor";
	final String sAuthor="ows_Author";
	final String sQueryOptions = "QueryOptions";
	final String sViewAttributes = "ViewAttributes";
	final String sScope = "Scope";
	final String sRecursive = "Recursive";
	final String sQuery = "Query";
	final String sOrderBy = "OrderBy";
	final String sDispForm = "DispForm.aspx?ID=";
//	final String sLists = "Lists";
	final String sID = "ows_ID";
	final String sDiscussionLastUpdated = "ows_DiscussionLastUpdated";
	final String sDocument = "Document";
	static final String HASH = "#";
	static final String COMMA = ",";
	final String sURL = "ows_URL";
	static final String URL_SEP ="://";
	public static URLEncoder enc  = new URLEncoder();
	private SharepointClientContext sharepointClientContext;
	private String endpoint;
	ListsSoap_BindingStub stub = null;
	private ViewsWS viewsWS;
	private WebsWS websWS;
	static final String BT_DISCUSSIONBOARD = "DiscussionBoard";

	static{
		//set the URLEncoder safe characters
		//Adding safe characters will prevent them to encoded while encoding the URL
		enc.addSafeCharacter('/');
		enc.addSafeCharacter(':');// required when endpoint is set using specified site
	}
	
	/**
	 * The "blacklist" is the SharePoint meta attributes that we will NOT
	 * pass to the GSA. (these all come from the ows_metaInfo attribute, which
	 * actually encodes a large number of other attributes).
	 * Note that these can be regular expressions, in order to catch
	 * 1;#Subject and 2;#Subject
	 * 
	 * Also note that these are just one person's opinion about the metadata you
	 * probably don't want.  Feel free to add or remove items.
	 */
	private static ArrayList blackList;
	/*static {
		BLACK_LIST = new ArrayList();
		BLACK_LIST.add(Pattern.compile(".*vti_cachedcustomprops$"));
		BLACK_LIST.add(Pattern.compile(".*vti_parserversion$"));
		BLACK_LIST.add(Pattern.compile(".*ContentType$"));
		BLACK_LIST.add(Pattern.compile(".*vti_cachedtitle$"));
		BLACK_LIST.add(Pattern.compile(".*ContentTypeId$"));
		BLACK_LIST.add(Pattern.compile(".*DocIcon$"));
		BLACK_LIST.add(Pattern.compile(".*vti_cachedhastheme$"));
		BLACK_LIST.add(Pattern.compile(".*vti_metatags$"));
		BLACK_LIST.add(Pattern.compile(".*vti_charset$"));
		BLACK_LIST.add(Pattern.compile(".*vti_cachedbodystyle$"));
		BLACK_LIST.add(Pattern.compile(".*vti_cachedneedsrewrite$"));
	}*/

	/**
	 * The "whitelist" is SharePoint meta attributes that we WILL
	 * pass to the GSA but will treat specially, so they should not be swept
	 * up into the 'attrs'.
	 * There is no operational difference between blacklist and whitelist;
	 * in both cases the attributes are not passed to the GSA.
	 */
	private static ArrayList whiteList;
	/*static {
		WHITE_LIST = new ArrayList();
		WHITE_LIST.add(Pattern.compile(".*vti_title$"));
		WHITE_LIST.add(Pattern.compile(".*vti_author$"));
	}*/


	/**
	 * set metadate white list and black list from SharepointClientContext.
	 */

	private void setBlackListAndWhiteList(){
		String sFuncName = "setBlackListAndWhiteList()";
		LOGGER.entering(className, sFuncName);
		ArrayList metaBlackList = null;
		ArrayList metaWhiteList = null;
		if(blackList == null){
			blackList = new ArrayList();
		}
		if(whiteList == null){
			whiteList = new ArrayList();
		}

		blackList.clear();
		whiteList.clear();

		// set metadata black list
		if(sharepointClientContext!=null){
			metaBlackList = sharepointClientContext.getBlackList();
			if(metaBlackList != null){
				int size = metaBlackList.size();
				for(int index = 0; index < size ; index++){
					blackList.add(Pattern.compile((String)metaBlackList.get(index)));
				}
			}

			// set metadata white list			
			metaWhiteList = sharepointClientContext.getWhiteList();
			if(metaWhiteList != null){
				int size = metaWhiteList.size();
				for(int index = 0; index < size ; index++){
					whiteList.add(Pattern.compile((String)metaWhiteList.get(index)));
				}
			}
		}
		LOGGER.exiting(className, sFuncName);
	}
	/**
	 * Determine if any entry in a given List matches the given input.
	 * @param list
	 * @param input
	 * @return boolean
	 */
	private static boolean listMatches(List list, String input) {
		String sFuncName = "listMatches(List list, String input)";
		LOGGER.entering(ListsWS.class.getName(), sFuncName);
		
		if(list!=null){
			for (int iPattern=0;iPattern<list.size();++iPattern) {
				Pattern pattern = (Pattern) list.get(iPattern);
				Matcher matcher = pattern.matcher(input);
				if (matcher.matches()) {
					return true;
				}
			}
		}
		LOGGER.exiting(ListsWS.class.getName(), sFuncName);
		return false;
	}

	/**
	 * Extract the meaningful part of a meaningful metadata string. The two
	 * "meaningful"s signify, for an example "BobAdditional:SW|SomeBobData"
	 * 1) name: BobAdditional
	 * 2) value: SomeBobData
	 *   because "BobAdditional" is neither in the blacklist nor the whitelist
	 *   The "SW|" is discarded as type info (we treat everything as strings).
	 * example: "vti_author:SW|Bob" returns null, because vti_author
	 *   matches an entry in the whitelist.
	 * example "_Category:SW|" returns "" because there's nothing after
	 *   the "|".
	 * @param meta
	 * @return value, or empty string if none
	 */
	private static String getMetadataContent(String meta) {
		String sFuncName = "getMetadataContent(String meta)";
		LOGGER.entering(ListsWS.class.getName(), sFuncName);
		if(meta!=null){
			String[] parts = meta.split(COLON);
			if (parts.length < 2){
				return "";
			}
			String name = parts[0].trim();
			if (!listMatches(blackList, name) && !listMatches(whiteList, name)) {

				String value = parts[1].trim();
				int ix = value.indexOf('|');
				if (ix >= 0) {
					if (ix < value.length()) {
						return value.substring(ix+1);
					} else {
						return "";
					}
				} else {
					return value;
				}
			}
		}
		LOGGER.exiting(ListsWS.class.getName(), sFuncName);
		return "";
	}

	public ListsWS(SharepointClientContext inSharepointClientContext) 
	throws RepositoryException {
		String sFuncName = "ListsWS(SharepointClientContext inSharepointClientContext)";
		LOGGER.entering(className, sFuncName);
		
		if(inSharepointClientContext!=null){
			this.sharepointClientContext = inSharepointClientContext;

			//set black and white list patterns
			setBlackListAndWhiteList();
	
			endpoint = inSharepointClientContext.getProtocol()+URL_SEP + inSharepointClientContext.getHost() + COLON 
			+inSharepointClientContext.getPort() 
			+ enc.encode(inSharepointClientContext.getsiteName()) + LISTS_END_POINT;

			LOGGER.config("ListsEndPt: "+endpoint);
			ListsLocator loc = new ListsLocator();
			loc.setListsSoapEndpointAddress(endpoint);

			Lists listsService =loc;
			try {
				stub = (ListsSoap_BindingStub) listsService.getListsSoap();
				viewsWS = new ViewsWS(inSharepointClientContext);
				websWS = new WebsWS(inSharepointClientContext);
			} catch (ServiceException e) {
				throw new SharepointException("Unable to get the list stub");
			}

			String strDomain = inSharepointClientContext.getDomain();
			String strUser = inSharepointClientContext.getUsername();
			String strPassword= inSharepointClientContext.getPassword();
			strDomain+="\\"+strUser; // form domain/user 

			//set the user and pass
			stub.setUsername(strDomain);
			stub.setPassword(strPassword);
		}
		LOGGER.exiting(className, sFuncName);
	}

	public ListsWS(SharepointClientContext inSharepointClientContext,String siteName) throws RepositoryException {
//		final String HTTP ="http";
//		final String HTTPS ="https";
		String sFuncName = "ListsWS(SharepointClientContext inSharepointClientContext,String siteName)";
		LOGGER.entering(className, sFuncName);
		if(inSharepointClientContext!=null){
			this.sharepointClientContext = inSharepointClientContext;
		}

		setBlackListAndWhiteList();
		if(siteName!=null){
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
			endpoint = siteURL.getProtocol()+URL_SEP+ siteURL.getHost()+":"+iPort/*siteURL.getPort()*/+enc.encode(siteURL.getPath())+ LISTS_END_POINT;
			/*if (siteName.startsWith(HTTP+URL_SEP)) {
				siteName = siteName.substring(7);
//				endpoint = HTTP+URL_SEP + Util.getEscapedSiteName(siteName) + LISTS_END_POINT;
				endpoint = HTTP+URL_SEP + siteName + LISTS_END_POINT;
			}else if(siteName.startsWith(HTTPS+URL_SEP)) {
				siteName = siteName.substring(8);
//				endpoint = HTTPS+URL_SEP + Util.getEscapedSiteName(siteName) + LISTS_END_POINT;
				endpoint = HTTPS+URL_SEP + siteName + LISTS_END_POINT;
			} else {
//				endpoint = Util.getEscapedSiteName(siteName) + LISTS_END_POINT;
				endpoint = siteName + LISTS_END_POINT;
			}*/
		}

//		System.out.println("ListsEndPt: "+endpoint);
		ListsLocator loc = new ListsLocator();
//		loc.setListsSoap12EndpointAddress(endpoint);
		loc.setListsSoapEndpointAddress(endpoint);

		Lists listsService =loc;
		try {
//			stub = (ListsSoap_BindingStub) listsService.getListsSoap12();
			stub = (ListsSoap_BindingStub) listsService.getListsSoap();
			viewsWS = new ViewsWS(inSharepointClientContext);
			websWS = new WebsWS(inSharepointClientContext);
		} catch (ServiceException e) {
			throw new SharepointException("Unable to get the list stub");
		}

		String strDomain = inSharepointClientContext.getDomain();
		String strUser = inSharepointClientContext.getUsername();
		String strPassword= inSharepointClientContext.getPassword();
		strDomain+="\\"+strUser; // form domain/user

//		set the user and pass
		stub.setUsername(strDomain);
		stub.setPassword(strPassword);
		LOGGER.exiting(className, sFuncName);
	}

	private GetListItemChangesViewFields makeViewFields(String listName)
	throws SharepointException {
		String sFuncName = "GetListItemChangesViewFields makeViewFields(String listName)";
		LOGGER.entering(className, sFuncName);
		final String sName = "Name";
		final String sViewFields = "viewFields";
		final String sFieldRef = "FieldRef";
		final String namespace ="http://schemas.microsoft.com/sharepoint/soap/";
		final String prefix ="ms";

		if(listName==null){
			throw new SharepointException("Unable to get listname");
		}

		//this call will return ArrayList of the view fields
		List viewFieldStrings = viewsWS.getViewFields(listName);
		if (viewFieldStrings == null) {
			return null;
		}

		//create the view fiels structure for the getlistitem changes call
		MessageElement me2 = new MessageElement(sViewFields,prefix,namespace);//localpart=nodename,prefix,ns
		try{
			//iterate through the list of fields and construct the view fields structure
			for (int iField=0;iField<viewFieldStrings.size();++iField) {
				String fieldName = (String) viewFieldStrings.get(iField);
				MessageElement meAttr = new MessageElement(sFieldRef,prefix,namespace);//localpart=nodename,prefix,ns
				meAttr.addAttribute(prefix,namespace,sName,fieldName);
				me2.addChild(meAttr);
			}
		}catch(Exception e){
			LOGGER.finer(e.toString());
			throw new SharepointException("Unable to fetch the view fields for "+listName);
		}

		GetListItemChangesViewFields viewFields = new GetListItemChangesViewFields();
		try{
			MessageElement[] meArray2 = {me2};
			viewFields.set_any(meArray2);
		}catch(Throwable th){
			LOGGER.finer("makeViewFields: "+th.toString());
			return null;
		}
		LOGGER.exiting(className, sFuncName);
		return viewFields;
	}

	/**
	 * Gets all the list item changes of a particular generic list since 
	 * a particular time. Generic lists include Discussion boards, Calendar,
	 * Tasks, Links, Announcements.
	 * @param list BaseList object
	 * @return list of sharepoint SPDocuments corresponding to items in the list. 
	 * These are ordered by last Modified time.
	 * @throws SharepointException 
	 * @throws MalformedURLException 
	 */
	public List getGenericListItemChanges(BaseList list, Calendar since) 
	throws SharepointException, MalformedURLException {
		String sFuncName = "getGenericListItemChanges(BaseList list, Calendar since)";
		LOGGER.entering(className, sFuncName);
	//	final String BT_DISCUSSIONBOARD = "DiscussionBoard";
		if(list==null){
			throw new SharepointException("Unable to get List");
		}
		String sFunctionName = "getGenericListItemChanges(BaseList list, Calendar since)";
		if(stub==null){
			throw new SharepointException(sFunctionName+": Unable to get the lists stub");
		}

		String listName = list.getInternalName();
		if(listName==null){
			throw new SharepointException("Unable to get the list name");
		}
		String listTemplate = list.getBaseTemplate();
		if(listTemplate==null){
			throw new SharepointException("Unable to get the list template");
		}

		
		///////////////REMOVE THE 
		String sLists =list.getListConst();
//		String sLists ="Lists";
		/*String weburl = sharepointClientContext.getsiteName(); 
		URL siteURL = new URL(weburl);
		
		weburl=siteURL.getPath();*/
		
		

		ArrayList listItems = new ArrayList();
		String urlPrefix = sharepointClientContext.getProtocol()+URL_SEP + sharepointClientContext.getHost() + COLON 
		+ sharepointClientContext.getPort() 
		+ sharepointClientContext.getsiteName()
		+ sLists + "/"   /*@@@ problem : need to get it dynamically */
		+ list.getTitle() + "/" + sDispForm;

		//--------changes for making axis 1_4 compliant
		String strSince =null;
		if (since != null) {
			strSince = Value.calendarToIso8601(since);
		}

		try{
			GetListItemChangesViewFields viewFields = makeViewFields(list.getInternalName());
			StringBuffer url = new StringBuffer();
			GetListItemChangesResponseGetListItemChangesResult res = stub.getListItemChanges(listName, viewFields , strSince, null);

			//get the result of the list item changes
			if(res!=null){
				MessageElement[] me = res.get_any();
				if(me!=null){
					if(me.length>0){
						Iterator itChilds = me[0].getChildElements();
						if(itChilds!=null){
							if(itChilds.hasNext()){
								MessageElement child = (MessageElement) itChilds.next();
								//get the child of child = files or folders
								Iterator itChildFilesOrFolders = child.getChildElements();
								while(itChildFilesOrFolders.hasNext()){
									MessageElement listItem = (MessageElement) itChildFilesOrFolders.next();

									String docId = listItem.getAttribute(sID);//sID
									String itemId = listItem.getAttribute(sID);

									//print values of all the attributes of document /////
									/*System.out.println("---------------LIST ITEM--------------");
									Iterator it = listItem.getAllAttributes();
									while(it.hasNext()){
										String attrKey =  (String) it.next().toString();
										String attrValue  = listItem.getAttribute(attrKey);

										System.out.println("Key: "+attrKey+"| value: "+attrValue);
									}
									System.out.println("---------------END:LIST ITEM--------------");*/
									//end: ///////////////print values of all the attributes of document /////

									url.setLength(0);
									url.append(urlPrefix);
									url.append(itemId); 
									LOGGER.config(sFunctionName+": URL :"+url);
									String[] includedURLs = sharepointClientContext.getIncludedURlList() ; 
									String[] excludedURLs = sharepointClientContext.getExcludedURlList() ; 
									SharepointClientUtils spUtils = new SharepointClientUtils();   
									if(spUtils.isIncludedUrl(includedURLs,excludedURLs, url.toString())) {	  
										LOGGER.config(sFunctionName+" : included URL :"+url.toString());
										SPDocument doc;

										String strObjectType = listItem.getAttribute(sContentType);
										if(strObjectType==null){
											strObjectType = SPDocument.OBJTYPE_LIST_ITEM;
										}

										String author = listItem.getAttribute(sEditor);
										if(author==null){
											author = listItem.getAttribute(sAuthor);
											if(author==null){
												author = SPDocument.NO_AUTHOR;
											}else{
												author=author.substring(author.indexOf(HASH) + 1); //e.g.1073741823;#System Account
											}
										}else{
											author=author.substring(author.indexOf(HASH) + 1); //e.g.1073741823;#System Account
										}

										
										doc = new SPDocument(docId, url.toString(), list.getLastMod(),author,strObjectType);
										//for list items we require the ID.. this is required to get the announcements
//										doc = new SPDocument(url.toString(), url.toString(), list.getLastMod(),author,strObjectType);
										
										////////////////add all the metadata
										Iterator itAttrs = listItem.getAllAttributes();
										if(itAttrs!=null){
											while(itAttrs.hasNext()){
												Object oneAttr =  itAttrs.next();
												if(oneAttr!=null){
													String strAttrName =oneAttr.toString();
													if((strAttrName !=null)&&(!strAttrName.trim().equals(""))){
														String strAttrValue = listItem.getAttribute(strAttrName);
	
														//check if the attribute could be considered as metadata
														if (!listMatches(blackList, strAttrName) && !listMatches(whiteList, strAttrName)){
															doc.setAttribute(strAttrName, strAttrValue);
	//																	System.out.println("Attribute key="+strAttrName+": value="+strAttrValue);
														}
													}
													
												}//if(oneAttr!=null){
											}
										}
										////////////////////
										
										listItems.add(doc);
									}else{
										LOGGER.warning(sFunctionName+" : excluding "+url.toString());
									}
								}
							}
						}
					}//end: for loop
				}//end: if(me!=null){
			}//end:if(res!=null){

			//logic for handling the discussion items
			//Note: the method getListItemChanges() does not return the discussion board items
			//Only the replies to the discussion are crawled
			//--This is applicable to sp2007 only/not in sp2003
			//////-----------------------------------------------

			if(sharepointClientContext==null){
				throw new SharepointException("Unable to get the client context");
			}
			String strSharepointVersion = sharepointClientContext.getSharePointType();
			if(strSharepointVersion==null){
				throw new SharepointException("Unable to get the sharepoint version");
			}


			if((strSharepointVersion.equals(SharepointConnectorType.SP2007)) && (listTemplate.equalsIgnoreCase(BT_DISCUSSIONBOARD))){
				//get the discussion board items
				GetListItemsResponseGetListItemsResult result = stub.getListItems(listName, null, null, null, "1000", null,null);
				//parse the result to extract the necessary attributes
				if(result!=null){
					MessageElement[] me = result.get_any();
					if(me!=null){
						if(me.length>0){
							Iterator itChilds = me[0].getChildElements();
							if(itChilds!=null){
								if(itChilds.hasNext()){
									MessageElement child = (MessageElement) itChilds.next();

									int counted =0;
									//get the child of child = files or folders
									Iterator itChildFilesOrFolders = child.getChildElements();
									while(itChildFilesOrFolders.hasNext()){
										MessageElement listItem = (MessageElement) itChildFilesOrFolders.next();

										String docId = listItem.getAttribute(sID);//sID
										String itemId = listItem.getAttribute(sID);


										String lastmodified =listItem.getAttribute(sDiscussionLastUpdated);
										if(lastmodified==null){
											lastmodified="";
										}

										//print values of all the attributes of document /////
										/*System.out.println("---------------LIST ITEM--------------");
										Iterator it = listItem.getAllAttributes();
										while(it.hasNext()){
											String attrKey =  (String) it.next().toString();
											String attrValue  = listItem.getAttribute(attrKey);

											System.out.println("Key: "+attrKey+"| value: "+attrValue);
										}
										System.out.println("---------------END:LIST ITEM--------------");*/
										//end: ///////////////print values of all the attributes of document /////

										url.setLength(0);
										url.append(urlPrefix);
										url.append(itemId); 
										LOGGER.config(sFunctionName+": URL :"+url);
										++counted;
										//System.out.println("DiscURL["+counted+"]:"+url);
										String[] includedURLs = sharepointClientContext.getIncludedURlList() ; 
										String[] excludedURLs = sharepointClientContext.getExcludedURlList() ; 
										SharepointClientUtils spUtils = new SharepointClientUtils();   
										if(spUtils.isIncludedUrl(includedURLs,excludedURLs, url.toString())) {
//											if(strSince==null){ //include every thing 
											//additional check to match the last modified documents requirement
											//strSince = watermark
											//ows_DiscussionLastModified
											/*
												 e.g ------------strSince= 2007-10-26T05:55:17.000Z
													 ------------ows_DiscussionLastModified= 2007-10-26 11:25:17 
											 */

											////////date conversion issues ////////
											//Calendar x= Util.listItemsStringToCalendar(lastmodified);
											/*final TimeZone TIME_ZONE_GMT = TimeZone.getTimeZone("GMT+0");
											Calendar x = Calendar.getInstance(TIME_ZONE_GMT);
											final SimpleDateFormat SIMPLE_DATE_FORMATTER2 = 
											    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
											Date dt = SIMPLE_DATE_FORMATTER2.parse(lastmodified);

											 x.setTime(dt);    
											////////end: date conversion issues ////////

//											Calendar x= Value.iso8601ToCalendar(lastmodified);

											System.out.println("------------strSince211= "+strSince);
											System.out.println("------------ows_DiscussionLastModified= "+Value.calendarToIso8601(x));
											if((strSince==null)||(x.after(since))){*/


											//convert lastmodified to




											LOGGER.config(sFunctionName+" : included URL :"+url.toString());
											SPDocument doc;

											String strObjectType = listItem.getAttribute(sContentType);
											if(strObjectType==null){
												strObjectType = SPDocument.OBJTYPE_LIST_ITEM;
											}

											String author = listItem.getAttribute(sEditor);
											if(author==null){
												author = listItem.getAttribute(sAuthor);
												if(author==null){
													author = SPDocument.NO_AUTHOR;
												}else{
													author=author.substring(author.indexOf(HASH) + 1); //e.g.1073741823;#System Account
												}
											}else{
												author=author.substring(author.indexOf(HASH) + 1); //e.g.1073741823;#System Account
											}


											doc = new SPDocument(docId, url.toString(), list.getLastMod(),author,strObjectType);
											//for list items we require the ID.. this is required to get the announcements
											//											doc = new SPDocument(url.toString(), url.toString(), list.getLastMod(),author,strObjectType);
											listItems.add(doc);
//											}
											/*else{ //include only changed items
											//additional check to match the last modified documents requirement
											//strSince = watermark
											//ows_DiscussionLastModified

											 e.g ------------strSince= 2007-10-26T05:55:17.000Z
												 ------------ows_DiscussionLastModified= 2007-10-26 11:25:17 

											Calendar x= Util.listItemsStringToCalendar(lastmodified);
											x.after(since)
											//convert lastmodified to

											System.out.println("------------strSince= "+strSince);
											System.out.println("------------ows_DiscussionLastModified= "+lastmodified);


											LOGGER.config(sFunctionName+" : included URL :"+url.toString());
											SPDocument doc;

											String strObjectType = listItem.getAttribute(sContentType);
											if(strObjectType==null){
												strObjectType = SPDocument.OBJTYPE_LIST_ITEM;
											}

											String author = listItem.getAttribute(sEditor);
											if(author==null){
												author = listItem.getAttribute(sAuthor);
												if(author==null){
													author = SPDocument.NO_AUTHOR;
												}else{
													author=author.substring(author.indexOf(HASH) + 1); //e.g.1073741823;#System Account
												}
											}else{
												author=author.substring(author.indexOf(HASH) + 1); //e.g.1073741823;#System Account
											}


											doc = new SPDocument(docId, url.toString(), list.getLastMod(),author,strObjectType);
											//for list items we require the ID.. this is required to get the announcements
//											doc = new SPDocument(url.toString(), url.toString(), list.getLastMod(),author,strObjectType);
											listItems.add(doc);
									}//end: strsince ==null
											 */									  }
									}
								}//end: if(itChilds.hasNext()){
							}//end: if(itChilds!=null){
						}//end: if(me.length>0){
					}//end: if(me!=null){
				}//end: if(result!=null){

			}
			/////-----------------------------------------------

			Collections.sort(listItems);
		}catch (RemoteException e) {
			throw new SharepointException(e.toString());
		}catch(Throwable e){
			LOGGER.fine("getGenericListItemChanges: "+e.toString());
			throw new SharepointException(e.toString());
		}
		//end:--------changes for making axis 1_4 compliant
		if(listItems!=null){
			LOGGER.info("found: "+listItems.size()+" Items in List ["+ list.getInternalName()+"]"); 
		}
		LOGGER.exiting(className, sFuncName);
		return listItems;
	}  

	/**
	 * Gets all the list item changes of a particular SPDocument library since 
	 * a particular time.
	 * @param list BaseList object
	 * @return list of sharepoint SPDocuments corresponding to items in the list. 
	 * These are ordered by last Modified time.
	 * @throws SharepointException 
	 * @throws MalformedURLException 
	 */
	public List getDocLibListItemChanges(BaseList list, Calendar since) 
	throws SharepointException, MalformedURLException {
		String sFunctionName = "getDocLibListItemChanges(BaseList list, Calendar since)";
		LOGGER.entering(className,sFunctionName);
		if(list==null){
			throw new SharepointException("Unable to get list");
		}
		if(sharepointClientContext==null){
			throw new SharepointException(sFunctionName+": sharepointClientContext not found");
		}		
		if(stub==null){
			throw new SharepointException(sFunctionName+": Unable to get the lists stub");
		}


		Collator collator = SharepointConnectorType.getCollator();
		String listName = list.getInternalName();
		ArrayList listItems = new ArrayList();
		String urlPrefix = sharepointClientContext.getProtocol()+URL_SEP + sharepointClientContext.getHost() + COLON 
		+ sharepointClientContext.getPort() + "/";

		//setting the since (when get the changes)
		String strSince =null;
		if (since != null) {
			strSince = Value.calendarToIso8601(since);
		}

		try {

			//------modified to make axis 1.4 compliant
			GetListItemChangesViewFields viewFields = makeViewFields(listName);
			StringBuffer url = new StringBuffer();
			GetListItemChangesResponseGetListItemChangesResult res = stub.getListItemChanges(listName, viewFields , strSince, null);

			//		get the result of the list item changes
			if(res!=null){
				MessageElement[] me = res.get_any();
				if(me!=null){
					if(me.length>0){
						Iterator itChilds = me[0].getChildElements();
						if(itChilds!=null){
							if(itChilds.hasNext()){
								MessageElement child = (MessageElement) itChilds.next();
								//get the child of child = files or folders
								Iterator itChildFilesOrFolders = child.getChildElements();
								while(itChildFilesOrFolders.hasNext()){
									//list item = file
									MessageElement listItem = (MessageElement) itChildFilesOrFolders.next();

									
									
									//////////end: get all attributes///////////////
									
									//check if the fileref exists
									if(listItem.getAttribute(sFileRef)!=null){

										String lastModified = listItem.getAttribute(sModified);
										String fileName =listItem.getAttribute(sFileRef); 
										
										
										/////////////////print values of all the attributes of document /////
										/*System.out.println("---------------DOCUMENT--------------");

											Iterator it = listItem.getAllAttributes();
											while(it.hasNext()){
												String attrKey =  (String) it.next().toString();
												String attrValue  = listItem.getAttribute(attrKey);

												System.out.println("Key: "+attrKey+"| value: "+attrValue);
											}

											System.out.println("---------------END:DOCUMENT--------------");*/

										//end:print values of all the attributes of document /////


										/*
										 * An example of ows_FileRef is 
										 * 1;#unittest/Shared SPDocuments/sync.doc 
										 * We need to get rid of 1;#
										 */
										fileName = fileName.substring(fileName.indexOf(HASH) + 1);  
										LOGGER.config(sFunctionName +" : [ filename: "+fileName+"  |modified: "+lastModified+" ]");
										url.setLength(0);
										url.append(urlPrefix);
										url.append(fileName);    

										//amit has changed this !! ..sp2003 does not have metaInfo
										String metaInfo=listItem.getAttribute(sMetaInfo);


										//end: amit has changed this !! ..sp2003 does not have metaInfo
										LOGGER.config(sFunctionName+": URL :"+url);
										String[] includedURLs = sharepointClientContext.getIncludedURlList() ; 
										String[] excludedURLs = sharepointClientContext.getExcludedURlList() ; 
										SharepointClientUtils spUtils = new SharepointClientUtils();   
										if(spUtils.isIncludedUrl(includedURLs,excludedURLs, url.toString())) {	  
											LOGGER.config(sFunctionName+" : included url : "+url.toString());
											
											try {
												SPDocument doc;

												String strObjectType = listItem.getAttribute(sContentType);
												if(strObjectType==null){
													strObjectType = sDocument;
												}

												//get the author
												String author = listItem.getAttribute(sEditor);
												if(author==null){
													author = listItem.getAttribute(sAuthor);
													if(author==null){
														author = SPDocument.NO_AUTHOR;
													}else{
														author=author.substring(author.indexOf(HASH) + 1); //e.g.1073741823;#System Account
													}
												}else{
													author=author.substring(author.indexOf(HASH) + 1); //e.g.1073741823;#System Account
												}
												//doc = new SPDocument(docId, url.toString(),Value.iso8601ToCalendar(lastModified),strObjectType);
//												doc = new SPDocument(docId, url.toString(),Value.iso8601ToCalendar(lastModified),author,strObjectType);
												doc = new SPDocument(url.toString(), url.toString(),Value.iso8601ToCalendar(lastModified),author,strObjectType);


												// gather up the rest of the metadata:
												//case: sp2003-> get all the attributes-> check if in black or whitelist : return metadata
												//case: sp2007 call fn: setDocLibMetadata(doc, arrayOfMetaInfo);

												//get the sharepoint Type
												String strSharepointType = sharepointClientContext.getSharePointType();
												if(strSharepointType==null){
													throw new SharepointException(sFunctionName+": Unable to get the sharepoint type (sp2003/sp2007)");
												}
												
												if(strSharepointType.equals(SharepointConnectorType.SP2007)){
													String[] arrayOfMetaInfo = metaInfo.split("\n|\r\n");
													setDocLibMetadata(doc, arrayOfMetaInfo);	
												}//end: if(strSharepointType.equals(SharepointConnectorType.SP2007)){
												
												
//												if(strSharepointType.equals(SharepointConnectorType.SP2003)){
												//iterate through all the attributes get the atribute name and value
												Iterator itAttrs = listItem.getAllAttributes();
												if(itAttrs!=null){
													while(itAttrs.hasNext()){
														Object oneAttr =  itAttrs.next();
														if(oneAttr!=null){
															String strAttrName =oneAttr.toString();
															if((strAttrName !=null)&&(!strAttrName.trim().equals(""))){
																if(!collator.equals(strAttrName, sMetaInfo)){
																	String strAttrValue = listItem.getAttribute(strAttrName);
		
																	//check if the attribute could be considered as metadata
																	if (!listMatches(blackList, strAttrName) && !listMatches(whiteList, strAttrName)){
																		doc.setAttribute(strAttrName, strAttrValue);
		//																	System.out.println("Attribute key="+strAttrName+": value="+strAttrValue);
																	}
																}
															}
															
														}//if(oneAttr!=null){
													}
												}


												/*}else if(strSharepointType.equals(SharepointConnectorType.SP2007)){
													String[] arrayOfMetaInfo = metaInfo.split("\n|\r\n");
													setDocLibMetadata(doc, arrayOfMetaInfo);	
												}else{
													throw new SharepointException(sFunctionName+": Invalid sharepoint type, e.g. sharepoint type should be either sp2003/sp2007");
												}*/

												listItems.add(doc);
											} catch (ParseException e) {
												throw new SharepointException(e.toString(), e);
											}  
										}else{
											LOGGER.warning(sFunctionName+" : excluding "+url.toString());
										}
									}//end if
								}//end while
							}//end: while(itChildFilesOrFolders.hasNext()){
						}//if(itChilds!=null){
					}//end: for loop
				}//end: if(me!=null){
			}//end:if(res!=null){
		}catch (RemoteException e) {
			throw new SharepointException(e.toString());
		}catch(Throwable e){
			LOGGER.config("getDocLibListItemChanges: "+e.toString());
		}
		Collections.sort(listItems);
		if(listItems!=null){
			LOGGER.info("found: "+listItems.size()+" Items in DocumentLibrary ["+ list.getInternalName()+"]"); 
		}
		LOGGER.exiting(className,sFunctionName);
		return listItems;
	}

	public List getLinkChanges(BaseList list) 
	throws SharepointException, MalformedURLException {
		String sFunctionName = "getLinkChanges(BaseList list, Calendar since)";
		LOGGER.entering(className,sFunctionName);
		if(list==null){
			throw new SharepointException(sFunctionName+": Unable to get List");
		}
		if(stub==null){
			throw new SharepointException(sFunctionName+": Unable to get the lists stub");
		}

		String listName = list.getInternalName();
		if(listName==null){
			throw new SharepointException("Unable to get the list name");
		}
		ArrayList listItems = new ArrayList();

		//modified to support axis 1_4
		GetListItemChangesResponseGetListItemChangesResult res = null;
		try {
			res = stub.getListItemChanges(listName, makeViewFields(listName), null, null);
		} catch (RemoteException e) {
			LOGGER.finer("getLinkChanges: "+e.toString());
			throw new SharepointException("Unable to get the links for: "+listName);
		}catch(Throwable e){
			LOGGER.finer("getLinkChanges: "+e.toString());
		}

		if(res!=null){
			MessageElement[] me = res.get_any();
			if(me!=null){
				//for(int i=0;i<me.length;++i){
				if(me.length>0){
					Iterator itChilds = me[0].getChildElements();
					if(itChilds!=null){
						//while(itChilds.hasNext()){
						if(itChilds.hasNext()){
							MessageElement child = (MessageElement) itChilds.next();
							//get the child of child = files or folders
							Iterator itChildFilesOrFolders = child.getChildElements();
							while(itChildFilesOrFolders.hasNext()){
								MessageElement listItem = (MessageElement) itChildFilesOrFolders.next();
								String url = listItem.getAttribute(sURL);//e.g. http://www.abc.com, abc site"
								if(url==null){
									throw new SharepointException("Unable to get the link URL");
								}

								//filter out description
								url = url.substring(0, url.indexOf(COMMA));

								LOGGER.config(sFunctionName+": URL :"+url);
								String[] includedURLs = sharepointClientContext.getIncludedURlList() ; 
								String[] excludedURLs = sharepointClientContext.getExcludedURlList() ; 
								SharepointClientUtils spUtils = new SharepointClientUtils();

								//for debug
//								System.out.println("FoundLink: "+url);

								if(spUtils.isIncludedUrl(includedURLs,excludedURLs, url)) {
									LOGGER.config(sFunctionName+" : included URL :"+url.toString());
//									System.out.print("---status OK");
									try{
										String strWebURL=websWS.getWebURLFromPageURL(url);
										LOGGER.config("Web URL: "+strWebURL);
										//listItems.add(url);
										listItems.add(strWebURL);
									}catch(Throwable e){
										LOGGER.finer(sFunctionName+": "+e.toString());
									}
								}else{
									LOGGER.warning(sFunctionName+" : excluding "+url.toString());
								}

							}//end: while
						}//end: if(itChilds.hasNext()){
					}//end:if(itChilds!=null){
				}//end: if(me.length>0){
			}//end: if(me!=null){
		}//end: if(res!=null){
		//end: modification
		if(listItems!=null){
			LOGGER.info("found: "+listItems.size()+" Items in Links ["+ list.getInternalName()+"]"); 
		}
		LOGGER.exiting(className,sFunctionName);
		return listItems;
	}

	/**
	 * Gets all the attachments of a particular list item.
	 * @param baseList List to which the item belongs
	 * @param listItem list item for which the attachments need to be retrieved.
	 * @return list of sharepoint SPDocuments corresponding to attachments
	 * for the given list item. 
	 * These are ordered by last Modified time.
	 * @throws SharepointException 
	 * @throws MalformedURLException 
	 */
	public List getAttachments(BaseList baseList, SPDocument listItem) 
	throws SharepointException, MalformedURLException {
		String sFunctionName = "getAttachments(BaseList baseList, SPDocument listItem)";
		LOGGER.entering(className,sFunctionName);
		if(baseList==null){
			throw new SharepointException("Unable to get List");
		}

		if(stub==null){
			throw new SharepointException(sFunctionName+": Unable to get the lists stub");
		}
		String listName = baseList.getInternalName();
		/*
		 * An example of docId is 3;#{BC0E981B-FAA5-4476-A44F-83EA27155513}.
		 * For listItemId, we need to pass "3". 
		 */
		ArrayList listAttachments = new ArrayList();
		try {
			String arrayOflistItemId[] = listItem.getDocId().split(";#");
			String listItemId = arrayOflistItemId[0];

			//--------changes for axis 1_4 compliance
			GetAttachmentCollectionResponseGetAttachmentCollectionResult res = stub.getAttachmentCollection(listName, listItemId);
			if(res!=null){
				MessageElement[] me = res.get_any();	
				if(me!=null){
					if(me.length>0){
						if(me[0]!=null){
							Iterator ita = me[0].getChildElements();
							while((ita!=null)&&(ita.hasNext())){

								MessageElement attachmentsOmElement = (MessageElement) ita.next();
								for (Iterator attachmentsIt =attachmentsOmElement.getChildElements(); attachmentsIt.hasNext();) {
									String url = attachmentsIt.next().toString();
									/*/////////////////print values of all the attributes of document /////
									System.out.println("---------------ATTACHMENT--------------");
									Iterator it = attachmentsOmElement.getAllAttributes();
									while(it.hasNext()){
										OMAttribute attr = (OMAttribute) it.next();
										System.out.println(attr.getLocalName()+" : value= "+attr.getAttributeValue());
									}
									System.out.println("---------------END:ATTACHMENT--------------");

									//end: ///////////////print values of all the attributes of document /////
									 */				
									LOGGER.config(sFunctionName+": URL :"+url);
									String[] includedURLs = sharepointClientContext.getIncludedURlList() ; 
									String[] excludedURLs = sharepointClientContext.getExcludedURlList() ; 
									SharepointClientUtils spUtils = new SharepointClientUtils();   
									if(spUtils.isIncludedUrl(includedURLs,excludedURLs, url.toString())) {	  
										LOGGER.config(sFunctionName+":included URL ["+url+" ]");
										SPDocument doc = new SPDocument(url, url, baseList.getLastMod(),SPDocument.OBJTYPE_ATTACHMENT);        
										listAttachments.add(doc);  
									}else{
										LOGGER.warning(sFunctionName+" : excluding "+url.toString());
									}
								}
							}//end: if((ita!=null)&&(ita.hasNext())){
						}
					}
				}//end: if
			}
		} catch (RemoteException e) {
			throw new SharepointException(e.toString(), e);
		} catch (Throwable e) {
			LOGGER.finer(sFunctionName+": "+e.toString());
		}
		Collections.sort(listAttachments);
		LOGGER.exiting(className,sFunctionName);
		return listAttachments;
	}

	/**
	 * Collect all "interesting" metadata for an item from a Document Library
	 * described in a GetListItemsChanges WSDL call.
	 * Do not collect items which are
	 * either already dealt with (the whitelist) or the ones we're configured
	 * to not care about (the blacklist).  whitelist and blacklist are
	 * static sets in this module, so changes should be made there, not here.
	 * @param doc SPDocument
	 * @param arrayOfMetaInfo array of strings derived from ows_metaInfo
	 */
	private void setDocLibMetadata(SPDocument doc, String[] arrayOfMetaInfo) {
		String sFunctionName = "setDocLibMetadata(SPDocument doc, String[] arrayOfMetaInfo)";
		LOGGER.entering(className,sFunctionName);
		if((arrayOfMetaInfo!=null)&&(doc!=null)){
			for(int iMeta=0; iMeta<arrayOfMetaInfo.length;++iMeta){
				String meta = arrayOfMetaInfo[iMeta];
				String[] parts = meta.split(COLON);
				if (parts.length < 2) {
					continue;
				}
				String value = getMetadataContent(meta);
				if (value.length() > 0) {
					doc.setAttribute(parts[0].trim(), value);
				}
			}
		}
		LOGGER.exiting(className,sFunctionName);
	}


}
