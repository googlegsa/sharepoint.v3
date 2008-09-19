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



import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;
import org.apache.catalina.util.URLEncoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.generated.lists.GetAttachmentCollectionResponseGetAttachmentCollectionResult;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesResponseGetListItemChangesResult;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesViewFields;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsQuery;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsQueryOptions;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsResponseGetListItemsResult;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsViewFields;
import com.google.enterprise.connector.sharepoint.generated.lists.Lists;
import com.google.enterprise.connector.sharepoint.generated.lists.ListsLocator;
import com.google.enterprise.connector.sharepoint.generated.lists.ListsSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.state.WebState;
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
	static final SimpleDateFormat ISO8601_DATE_FORMAT_SECS =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static final SimpleDateFormat ISO8601_DATE_FORMAT_MILLIS =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	private static String rowLimit="1000";//Get max 1000 items in one call
	private String nextPage=null;

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
	/**
	 * The "whitelist" is SharePoint meta attributes that we WILL
	 * pass to the GSA but will treat specially, so they should not be swept
	 * up into the 'attrs'.
	 * There is no operational difference between blacklist and whitelist;
	 * in both cases the attributes are not passed to the GSA.
	 */
	private static ArrayList whiteList;
	


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
//			update the rowLimit
			if(inSharepointClientContext.getBatchHint()>0){
				rowLimit = ""+inSharepointClientContext.getBatchHint();
			}
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

			if((strDomain==null)||(strDomain.trim().equals(""))){
				strDomain=strUser; //for user
			}else{
				strDomain+="\\"+strUser; // form domain/user
			}

			//set the user and pass
			stub.setUsername(strDomain);
			stub.setPassword(strPassword);
		}
		LOGGER.exiting(className, sFuncName);
	}

	public ListsWS(SharepointClientContext inSharepointClientContext,String siteName) throws RepositoryException {
		String sFuncName = "ListsWS(SharepointClientContext inSharepointClientContext,String siteName)";
		LOGGER.entering(className, sFuncName);
		if(inSharepointClientContext!=null){
//			update the rowLimit
			if(inSharepointClientContext.getBatchHint()>0){
				rowLimit = ""+inSharepointClientContext.getBatchHint();
			}

			setBlackListAndWhiteList();
			if(siteName!=null){
				URL siteURL ;
				try {
					siteURL = new URL(siteName);
				} catch (MalformedURLException e) {
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
				endpoint = siteURL.getProtocol()+URL_SEP+ siteURL.getHost()+":"+iPort/*siteURL.getPort()*/+enc.encode(siteURL.getPath())+ LISTS_END_POINT;
			}
		}
		ListsLocator loc = new ListsLocator();
		loc.setListsSoapEndpointAddress(endpoint);

		Lists listsService =loc;
		try {
			stub = (ListsSoap_BindingStub) listsService.getListsSoap();
			viewsWS = new ViewsWS(inSharepointClientContext);
			websWS = new WebsWS(inSharepointClientContext);
		} catch (ServiceException e) {
			LOGGER.log(Level.WARNING,className+":"+sFuncName,e);
			throw new SharepointException("Unable to get the list stub");
		}

		String strDomain = inSharepointClientContext.getDomain();
		String strUser = inSharepointClientContext.getUsername();
		String strPassword= inSharepointClientContext.getPassword();

		if((strDomain==null)||(strDomain.trim().equals(""))){
			strDomain=strUser; //for user
		}else{
			strDomain+="\\"+strUser; // form domain/user
		}

//		set the user and pass
		stub.setUsername(strDomain);
		stub.setPassword(strPassword);
		LOGGER.exiting(className, sFuncName);
	}

	private GetListItemChangesViewFields makeViewFields(String listName)
	throws SharepointException {
		String sFuncName = "GetListItemChangesViewFields makeViewFields(String listName)";
		LOGGER.entering(className, sFuncName);
		LOGGER.config(sFuncName+": listName["+listName+"]"); //added by Nitendra
		
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
	//public List getGenericListItemChanges(BaseList list, Calendar since) 
	public List getGenericListItems(BaseList list, Calendar lastModified, String lastItemID)throws SharepointException, MalformedURLException {
		String sFuncName = "getGenericListItemChanges(BaseList list, Calendar since)";
		LOGGER.entering(className, sFuncName);

		if(list==null){
			throw new SharepointException("Unable to get List");
		}
		
		LOGGER.config(sFuncName+": list[internalName="+list.getInternalName()+"|title="+list.getTitle()+"], lastModified["+lastModified+"], lastItemID["+lastItemID+"]"); //added by Nitendra
		
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

		String sLists =list.getListConst();
		LOGGER.fine("ListConst Value: "+sLists);

		ArrayList listItems = new ArrayList();
		String urlPrefix = sharepointClientContext.getProtocol()+URL_SEP + sharepointClientContext.getHost() + COLON 
		+ sharepointClientContext.getPort() 
		+ sLists + "/"   
		+ sDispForm;

//		nextPage="";
		try{
			String viewName = "";//Ignore this field for now
			GetListItemsQuery query =new GetListItemsQuery();
			query.set_any(createQuery(lastModified,lastItemID));//Create Query for the Lists

			GetListItemsViewFields viewFields =new GetListItemsViewFields(); //<ViewFields />
			viewFields.set_any(createViewFields());


			
			/*Keep On pulling data from the list page by page*/
//			while(nextPage!=null){
				GetListItemsQueryOptions queryOptions=new GetListItemsQueryOptions();
//				queryOptions.set_any(createQueryOptions(nextPage));
				queryOptions.set_any(createQueryOptions());

				String webID = "";
				GetListItemsResponseGetListItemsResult res = null;

				ArrayList listItemsTemp=null;
				
				res = stub.getListItems(listName, viewName, query, viewFields, rowLimit, queryOptions, webID);
//				nextPage=null;
				if(res!=null){
					MessageElement[] me = res.get_any();
					listItemsTemp = processListResult(me,urlPrefix,list);//get the result of the list item changes

					if((listItems!=null) && (listItemsTemp!=null)){
						listItems.addAll(listItemsTemp);//Add next set of documents
					}
				}
//			}//end:while(nextPage!=null){
			
			if(sharepointClientContext==null){
				throw new SharepointException("Unable to get the client context");
			}
			String strSharepointVersion = sharepointClientContext.getSharePointType();
			if(strSharepointVersion==null){
				throw new SharepointException("Unable to get the sharepoint version");
			}

			if((strSharepointVersion.equals(SharepointConnectorType.SP2007)) && (listTemplate.equalsIgnoreCase(BT_DISCUSSIONBOARD))){
				//get the discussion board items
				//GetListItemsResponseGetListItemsResult result = stub.getListItems(listName, null, null, null, "1000", null,null);
//				query =new GetListItemsQuery();
//				query.set_any(createQuery(lastModified,"0"));//Create Query for the Lists
				
				queryOptions=new GetListItemsQueryOptions();
				queryOptions.set_any(createQueryOptions2());
				
				GetListItemsResponseGetListItemsResult result = stub.getListItems(listName, viewName, query, viewFields, rowLimit, queryOptions, webID);
				
				//parse the result to extract the necessary attributes
				if(result!=null){
					listItemsTemp=null;
					MessageElement[] me = result.get_any();
					listItemsTemp = processListResult(me,urlPrefix,list);//get the result of the list item changes

					if((listItems!=null) && (listItemsTemp!=null)){
						listItems.addAll(listItemsTemp);//Add next set of documents
					}
				}
			}

			Collections.sort(listItems);
		}catch (RemoteException e) {
			LOGGER.log(Level.WARNING,className+":"+sFuncName,e);
			throw new SharepointException(e.toString());
		}catch(Throwable e){
			LOGGER.log(Level.FINE,className+":"+sFuncName,e);
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

	public List getDocLibListItems(BaseList list, Calendar lastModified, String lastItemID)throws SharepointException, MalformedURLException {
		String sFunctionName = "getDocLibListItemChanges(BaseList list, Calendar since)";
		LOGGER.entering(className,sFunctionName);

		if(list==null){
			throw new SharepointException("Unable to get list");
		}
		
		LOGGER.config(sFunctionName+": list[internalName="+list.getInternalName()+"|title="+list.getTitle()+"], lastModified["+lastModified+"], lastItemID["+lastItemID+"]"); //added by Nitendra

		if(sharepointClientContext==null){
			throw new SharepointException(sFunctionName+": sharepointClientContext not found");
		}		
		if(stub==null){
			throw new SharepointException(sFunctionName+": Unable to get the lists stub");
		}
//		nextPage="";


		String listName = list.getInternalName();
		ArrayList listItems = new ArrayList();
		String urlPrefix = sharepointClientContext.getProtocol()+URL_SEP + sharepointClientContext.getHost() + COLON 
		+ sharepointClientContext.getPort() + "/";

		try {
			String viewName = "";//Ignore this field for now
			GetListItemsQuery query =new GetListItemsQuery();
			query.set_any(createQuery(lastModified,lastItemID));//Create Query for the Lists
			String webID = "";
			GetListItemsResponseGetListItemsResult res = null;

			GetListItemsViewFields viewFields =new GetListItemsViewFields(); //<ViewFields />
			viewFields.set_any(createViewFields());

	

			/*Keep On pulling data from the list page by page*/
//			while(nextPage!=null){
				GetListItemsQueryOptions queryOptions=new GetListItemsQueryOptions();
				//queryOptions.set_any(createQueryOptions(nextPage));
				queryOptions.set_any(createQueryOptions());
				
				ArrayList listItemsTemp=null;
				res = stub.getListItems(listName, viewName, query, viewFields, rowLimit, queryOptions, webID);
//				nextPage=null;
				if(res!=null){
					MessageElement[] me = res.get_any();
					listItemsTemp = processResult(me,urlPrefix,list.getParentWebTitle());//get the result of the list item changes

					if((listItems!=null) && (listItemsTemp!=null)){
						listItems.addAll(listItemsTemp);//Add next set of documents
					}
				}
//			}//end: while(nextPage!=null){

		}catch (RemoteException e) {
			LOGGER.log(Level.WARNING,className+":"+sFunctionName,e);
			throw new SharepointException(e.toString());
		}catch(Throwable e){
			LOGGER.log(Level.CONFIG,"getDocLibListItemChanges: ",e);
		}
		if(listItems==null){
			listItems = new ArrayList();
		}
		Collections.sort(listItems);
//		if(listItems!=null){
			LOGGER.info("found: "+listItems.size()+" Items in DocumentLibrary ["+ list.getInternalName()+"]"); 
//		}

		LOGGER.exiting(className,sFunctionName);
		return listItems;
	}

	private ArrayList processResult(MessageElement[] me, String urlPrefix,String parentWebTitle) throws SharepointException {
		String sFunctionName="processResult";
		Collator collator = SharepointConnectorType.getCollator();
		StringBuffer url = new StringBuffer();
		ArrayList listItems = new ArrayList();

		if(me!=null){
			if(me.length>0){
				Iterator itChilds = me[0].getChildElements();
				if(itChilds!=null){
					if(itChilds.hasNext()){
						MessageElement child = (MessageElement) itChilds.next();
						////////////////process page ////////////////
						//Note: If no pages are left the nextPage=null
						nextPage = child.getAttribute("ListItemCollectionPositionNext");//upadate the page token
						LOGGER.config("Next Page: "+nextPage);
//						list.setNextPageToken(nextPage);//Update Page Token
						//////////////////////////////////////////////

						//get the child of child = files or folders
						Iterator itChildFilesOrFolders = child.getChildElements();
						while(itChildFilesOrFolders.hasNext()){
							//list item = file
							MessageElement listItem = (MessageElement) itChildFilesOrFolders.next();
							String docId = listItem.getAttribute(sID);//sID
							//check if the fileRef exists
							if(listItem.getAttribute(sFileRef)!=null){

								String lastModified = listItem.getAttribute(sModified);
								String fileName =listItem.getAttribute(sFileRef); 
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

								//sp2003 does not have metaInfo
								String metaInfo=listItem.getAttribute(sMetaInfo);
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
										
										/*Calendar cal =Value.iso8601ToCalendar(lastModified);
										System.out.println("Stored: "+cal.getTime());
										Value.calendarToIso8601(cal);
										System.out.println("Stored: "+Value.calendarToIso8601(cal));*/
										
										//doc = new SPDocument(url.toString(), url.toString(),Value.iso8601ToCalendar(lastModified),author,strObjectType);
										doc = new SPDocument(docId, url.toString(),Value.iso8601ToCalendar(lastModified),author,strObjectType,parentWebTitle);

										//get the sharepoint Type
										String strSharepointType = sharepointClientContext.getSharePointType();
										if(strSharepointType==null){
											throw new SharepointException(sFunctionName+": Unable to get the sharepoint type (sp2003/sp2007)");
										}

										if(strSharepointType.equals(SharepointConnectorType.SP2007)){
											String[] arrayOfMetaInfo = metaInfo.split("\n|\r\n");
											setDocLibMetadata(doc, arrayOfMetaInfo);	
										}//end: if(strSharepointType.equals(SharepointConnectorType.SP2007)){

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
															}
														}
													}

												}//if(oneAttr!=null){
											}
										}
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
		return listItems;
	}
	
	
	private ArrayList processListResult(MessageElement[] me, String urlPrefix, BaseList list) throws SharepointException {
		String sFunctionName="processResult";
//		Collator collator = SharepointConnectorType.getCollator();
		StringBuffer url = new StringBuffer();
		ArrayList listItems = new ArrayList();

		if(me!=null){
			if(me.length>0){
				Iterator itChilds = me[0].getChildElements();
				if(itChilds!=null){
					if(itChilds.hasNext()){
						MessageElement child = (MessageElement) itChilds.next();
						nextPage = child.getAttribute("ListItemCollectionPositionNext");//upadate the page token
						LOGGER.config("Next Page: "+nextPage);
						//get the child of child = files or folders
						Iterator itChildFilesOrFolders = child.getChildElements();
						while(itChildFilesOrFolders.hasNext()){
							MessageElement listItem = (MessageElement) itChildFilesOrFolders.next();

							String docId = listItem.getAttribute(sID);//sID
							String itemId = listItem.getAttribute(sID);

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


								doc = new SPDocument(docId, url.toString(), list.getLastMod(),author,strObjectType,list.getParentWebTitle());

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
													//System.out.println("Attribute key="+strAttrName+": value="+strAttrValue);
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
		return listItems;
	}

	//public List getLinkChanges(BaseList list) throws SharepointException, MalformedURLException {
	public TreeSet getLinkChanges(BaseList list) throws SharepointException, MalformedURLException {
		String sFunctionName = "getLinkChanges(BaseList list, Calendar since)";
		LOGGER.entering(className,sFunctionName);
		if(list==null){
			throw new SharepointException(sFunctionName+": Unable to get List");
		}
		
		LOGGER.config(sFunctionName+": list[internalName="+list.getInternalName()+"|title="+list.getTitle()+"]"); //added by Nitendra

		if(stub==null){
			throw new SharepointException(sFunctionName+": Unable to get the lists stub");
		}

		String listName = list.getInternalName();
		if(listName==null){
			throw new SharepointException("Unable to get the list name");
		}
		//ArrayList listItems = new ArrayList();
		TreeSet listItems = new TreeSet();

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

								if(spUtils.isIncludedUrl(includedURLs,excludedURLs, url)) {
									LOGGER.config(sFunctionName+" : included URL :"+url.toString());
									try{
//										String strWebURL=websWS.getWebURLFromPageURL(url);
//										LOGGER.config("Web URL: "+strWebURL);
										//WebState ws = new WebState(strWebURL,strWebURL,);
										WebState ws = websWS.getWebURLFromPageURL(url);
										listItems.add(ws);//Add The WebState Instead
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
		
		if(listItem==null) {
			throw new SharepointException("Unable to get listItem");
		}
		
		LOGGER.config(sFunctionName+": baseList[internalName="+baseList.getInternalName()+"|title="+baseList.getTitle()+"], listItem[DocId="+listItem.getDocId()+"|ListGuid"+listItem.getListGuid()+"|Url="+listItem.getUrl()+"]"); //added by Nitendra


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
										//SPDocument doc = new SPDocument(url, url, baseList.getLastMod(),SPDocument.OBJTYPE_ATTACHMENT);        
										SPDocument doc = new SPDocument(listItemId, url, baseList.getLastMod(),SPDocument.OBJTYPE_ATTACHMENT,baseList.getParentWebTitle());
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
			LOGGER.log(Level.WARNING,className+":"+sFunctionName,e);
			throw new SharepointException(e.toString(), e);
		} catch (Throwable e) {
			LOGGER.log(Level.FINER,className+":"+sFunctionName,e);
//			LOGGER.finer(sFunctionName+": "+e.toString());
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

	/*private MessageElement[] createQuery() {
		String strMyString="<Query><OrderBy><FieldRef Name=\"Modified\" Ascending=\"TRUE\" /></OrderBy></Query>";
		MessageElement[] meArray = {getMeFromString(strMyString)};
		return meArray;
	}*/
	
	private MessageElement[] createQuery(Calendar c, String listItemID) throws ParseException{
		LOGGER.config("Last Date: "+c);
		LOGGER.config("Last Item: "+listItemID);
		String date=null;
		boolean isList=false;
		
		//To ensure a properList ItemID...case of List where GUID is found
		try{
			Integer.parseInt(listItemID);
		}catch (Exception e) {
			LOGGER.config("List Discovered.. ID: "+listItemID);
			listItemID="0";
			//change the query
			isList=true;
		}
		
		
		if(c!=null){
			/*Date dt = Mydate.getTime();
			date =SIMPLE_DATE_FORMATTER3.format(dt);
			System.out.println("Time: "+date);*/
			
			date =Value.calendarToIso8601(c);
			Date dt = (Date)ISO8601_DATE_FORMAT_MILLIS.parse(date); 
			date =ISO8601_DATE_FORMAT_SECS.format(dt);
			LOGGER.config("Time: "+date);
		}
		
		//String date="2008-07-16T10:44:30Z";
//		String date="2008-07-16T11:00:30Z";
		String strMyString="<Query/>";//empty Query String
		
		if(((date==null)||listItemID==null)){
			LOGGER.config("Initial case ...");
			strMyString=""
				+"<Query>"
					+"<OrderBy><FieldRef Name=\"Modified\" Ascending=\"TRUE\" /></OrderBy>"
				+"</Query>";
		}else if(isList==true){
			LOGGER.config("list case...");
			strMyString=""
				+"<Query>"
					+"<Where>"
						+"<Gt>"
							+"<FieldRef Name=\"Modified\"/>"
							+"<Value Type=\"DateTime\" IncludeTimeValue=\"TRUE\" StorageTZ=\"TRUE\">"+date+"</Value>"
						+"</Gt>"
					+"</Where>"
					+"<OrderBy><FieldRef Name=\"Modified\" Ascending=\"TRUE\" /></OrderBy>"
				+"</Query>";
		}else{
			LOGGER.config("other cases ...");
			strMyString=""
				+"<Query>"
					+"<Where>"
						+"<Or>"
							+"<Gt>"
								+"<FieldRef Name=\"Modified\"/>"
								+"<Value Type=\"DateTime\" IncludeTimeValue=\"TRUE\" StorageTZ=\"TRUE\">"+date+"</Value>"
							+"</Gt>"
							
							+"<And>"
								+"<Eq>"
									+"<FieldRef Name=\"Modified\"/>"
									+"<Value Type=\"DateTime\" IncludeTimeValue=\"TRUE\" StorageTZ=\"TRUE\">"+date+"</Value>"
								+"</Eq>"
								+"<Gt>"
									+"<FieldRef Name=\"ID\"/>"
									+"<Value Type=\"Text\">"+listItemID+"</Value>"
								+"</Gt>"
							+"</And>"
						+"</Or>"
					+"</Where>"
					+"<OrderBy><FieldRef Name=\"Modified\" Ascending=\"TRUE\" /></OrderBy>"
				+"</Query>";
		}	
		MessageElement[] meArray = {getMeFromString(strMyString)};//Array of the message element
		return meArray;
	}


	MessageElement getMeFromString(String strMyString){
		//String strMyString = "<QueryOptions><Folder>"+strTempFolder+"</Folder></QueryOptions>";

		DocumentBuilder docBuilder = null;
		try {
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {

			e.printStackTrace();
		} catch (FactoryConfigurationError e) {

			e.printStackTrace();
		}
		StringReader reader = new StringReader(strMyString);
		InputSource inputsource = new InputSource(reader);
		Document doc = null;
		try {
			doc = docBuilder.parse(inputsource);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Element ele = doc.getDocumentElement();
		MessageElement msg = new MessageElement(ele);

		return msg;
	}

	private MessageElement[] createViewFields() {
		/* e.g.
		 <ViewFields>
			<FieldRef Name="Modified"/>
			<FieldRef Name="ID"/>
			<FieldRef Name="FileLeafRef"/>
		</ViewFields>
		 */

//		final String sName = "Name";
		final String sViewFields = "ViewFields";
		final String sFieldRef = "FieldRef";
//		final String namespace ="http://schemas.microsoft.com/sharepoint/soap/";
//		final String prefix ="ms";


		//Create an empty viewFields element
		MessageElement me = new MessageElement(new QName(sViewFields));
		MessageElement meFieldRef = new MessageElement(new QName(sFieldRef));
		MessageElement meFieldRef2 = new MessageElement(new QName(sFieldRef));

		//This is to order the result fields
		meFieldRef.addAttribute(null, "Name",new QName("Modified"));/*ns,localname,value*/
		meFieldRef2.addAttribute(null, "Name",new QName("ID"));/*ns,localname,value*/

		try {
			me.addChild(meFieldRef);
			me.addChild(meFieldRef2);
		} catch (SOAPException e) {
			e.printStackTrace();
		}
		MessageElement[] meArray = {me};//Array of the message element

		return meArray;
	}

//	create the query options for the getlist items Web Service API
	/*private MessageElement[] createQueryOptions2(String nextPage) {
		LOGGER.config("PageValue: "+nextPage);
		if(nextPage!=""){
			System.out.println("hi");
		}
		String strMyString="<QueryOptions>"
			+"<IncludeMandatoryColumns>true</IncludeMandatoryColumns>"
			+"<ViewAttributes Scope=\"Recursive\" />"
			+"<Paging ListItemCollectionPositionNext=\""+nextPage+"\"/>"
			+"</QueryOptions>";

		MessageElement[] meArray = {getMeFromString(strMyString)};//Array of the message element

		return meArray;
	}*/
	private MessageElement[] createQueryOptionsWithPaging(String nextPage) {

		LOGGER.config("PageValue: "+nextPage);
		/*if(nextPage!=""){
			System.out.println("hi");
		}*/
		/* e.g.
		   <QueryOptions>
				<Paging ListItemCollectionPositionNext=\""+nextPage+"\"/>"
				<ViewAttributes Scope=\"Recursive\" />
				<DateInUtc>TRUE</DateInUtc>
			</QueryOptions>
		 * */

		String strMyString="<QueryOptions>"
			+"<IncludeMandatoryColumns>true</IncludeMandatoryColumns>"
			+"<ViewAttributes Scope=\"Recursive\" />"
			+"<DateInUtc>TRUE</DateInUtc>"
			+"</QueryOptions>";
//		+"<Paging ListItemCollectionPositionNext=\""+nextPage+"\"/>"
		MessageElement me = getMeFromString(strMyString);
		//MessageElement[] meArray = {};//Array of the message element
		
		//final String sQueryOptions = "QueryOptions";
		final String sPaging= "Paging";
		//final String sViewAttributes= "ViewAttributes";
		//final String sDateInUtc= "DateInUtc";

		//MessageElement me = new MessageElement(new QName(sQueryOptions));
		MessageElement mePaging = new MessageElement(new QName(sPaging));
		//MessageElement meViewAttributes = new MessageElement(new QName(sViewAttributes));
		//MessageElement meDateInUtc = new MessageElement(new QName(sDateInUtc),"TRUE");
		

		//add attribute to paging 
		mePaging.addAttribute(null, "ListItemCollectionPositionNext",new QName(nextPage));/*ns,localname,value*/
		//meViewAttributes.addAttribute(null, "Scope",new QName("Recursive"));/*ns,localname,value*/
		try {
			me.addChild(mePaging);
			//me.addChild(meViewAttributes);
			//me.addChild(meDateInUtc);
		} catch (SOAPException e) {
			e.printStackTrace();
		}

		MessageElement[] meArray = {me};

		return meArray;
	}

	private MessageElement[] createQueryOptions() {
		/* e.g.
		   <QueryOptions>
				<Paging ListItemCollectionPositionNext=\""+nextPage+"\"/>"
				<ViewAttributes Scope=\"Recursive\" />
				<DateInUtc>TRUE</DateInUtc>
			</QueryOptions>
		 * */

		String strMyString="<QueryOptions>"
			+"<IncludeMandatoryColumns>true</IncludeMandatoryColumns>"
			+"<ViewAttributes Scope=\"Recursive\" />"
			+"<DateInUtc>TRUE</DateInUtc>"
			+"<Paging/>"
			+"</QueryOptions>";

		MessageElement me = getMeFromString(strMyString);
		MessageElement[] meArray = {me};
		return meArray;
	}

	//fo discussion board .. do not see recursive
	private MessageElement[] createQueryOptions2() {
		/* e.g.
		   <QueryOptions>
				<Paging ListItemCollectionPositionNext=\""+nextPage+"\"/>"
				<ViewAttributes Scope=\"Recursive\" />
				<DateInUtc>TRUE</DateInUtc>
			</QueryOptions>
		 * */

		String strMyString="<QueryOptions>"
			+"<IncludeMandatoryColumns>true</IncludeMandatoryColumns>"
			+"<DateInUtc>TRUE</DateInUtc>"
			+"<Paging/>"
			+"</QueryOptions>";

		MessageElement me = getMeFromString(strMyString);
		MessageElement[] meArray = {me};
		return meArray;
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
//		nextPage=null;
		String sFunctionName = "getDocLibListItemChanges(BaseList list, Calendar since)";
		LOGGER.entering(className,sFunctionName);
		
		if(list==null){
			throw new SharepointException("Unable to get list");
		}

		LOGGER.config(sFunctionName+": list[internalName="+list.getInternalName()+"|title="+list.getTitle()+"], since["+since+"]"); //added by Nitendra

		if(sharepointClientContext==null){
			throw new SharepointException(sFunctionName+": sharepointClientContext not found");
		}		
		if(stub==null){
			throw new SharepointException(sFunctionName+": Unable to get the lists stub");
		}


//		Collator collator = SharepointConnectorType.getCollator();
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
//			StringBuffer url = new StringBuffer();
			GetListItemChangesResponseGetListItemChangesResult res = stub.getListItemChanges(listName, viewFields , strSince, null);

			if(res!=null){
				ArrayList listItemsTemp=null;
				MessageElement[] me = res.get_any();
				listItemsTemp = processResult(me,urlPrefix,list.getParentWebTitle());//get the result of the list item changes

				if((listItems!=null) && (listItemsTemp!=null)){
					listItems.addAll(listItemsTemp);//Add next set of documents
				}
			}
			
		}catch (RemoteException e) {
			LOGGER.log(Level.WARNING,className+":"+sFunctionName,e);
			throw new SharepointException(e.toString());
		}catch(Throwable e){
			LOGGER.log(Level.CONFIG,"getDocLibListItemChanges: ",e);
//			LOGGER.config("getDocLibListItemChanges: "+e.toString());
		}
		Collections.sort(listItems);
		if(listItems!=null){
			LOGGER.info("found: "+listItems.size()+" Items in DocumentLibrary ["+ list.getInternalName()+"]"); 
		}
		LOGGER.exiting(className,sFunctionName);
		return listItems;
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
//		nextPage=null;
		String sFuncName = "getGenericListItemChanges(BaseList list, Calendar since)";
		LOGGER.entering(className, sFuncName);
		
		//	final String BT_DISCUSSIONBOARD = "DiscussionBoard";
		if(list==null){
			throw new SharepointException("Unable to get List");
		}
				
		LOGGER.config(sFuncName+": list[internalName="+list.getInternalName()+"|title="+list.getTitle()+"], since["+since+"]"); //added by Nitendra
		
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
		LOGGER.fine("ListConst Value: "+sLists);
//		String sLists ="Lists";
		/*String weburl = sharepointClientContext.getsiteName(); 
		URL siteURL = new URL(weburl);

		weburl=siteURL.getPath();*/



		ArrayList listItems = new ArrayList();
		String urlPrefix = sharepointClientContext.getProtocol()+URL_SEP + sharepointClientContext.getHost() + COLON 
		+ sharepointClientContext.getPort() 
		+ sLists + "/"   
		+ sDispForm;

		//--------changes for making axis 1_4 compliant
		String strSince =null;
		if (since != null) {
			strSince = Value.calendarToIso8601(since);
		}

		try{
			GetListItemChangesViewFields viewFields = makeViewFields(list.getInternalName());
//			StringBuffer url = new StringBuffer();
			GetListItemChangesResponseGetListItemChangesResult res = stub.getListItemChanges(listName, viewFields , strSince, null);

			
			if(res!=null){
				ArrayList listItemsTemp=null;
				MessageElement[] me = res.get_any();
				listItemsTemp = processListResult(me,urlPrefix,list);//get the result of the list item changes

				if((listItems!=null) && (listItemsTemp!=null)){
					listItems.addAll(listItemsTemp);//Add next set of documents
				}
			}
			

			//logic for handling the discussion items
			//Note: the method getListItemChanges() does not return the discussion board items
			//Only the replies to the discussion are crawled
			//--This is applicable to sp2007 only/not in sp2003
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
					ArrayList listItemsTemp=null;
					MessageElement[] me = result.get_any();
					listItemsTemp = processListResult(me,urlPrefix,list);//get the result of the list item changes

					if((listItems!=null) && (listItemsTemp!=null)){
						listItems.addAll(listItemsTemp);//Add next set of documents
					}
				}
			}
			/////-----------------------------------------------

			Collections.sort(listItems);
		}catch (RemoteException e) {
			LOGGER.log(Level.WARNING,className+":"+sFuncName,e);
			throw new SharepointException(e.toString());
		}catch(Throwable e){
			LOGGER.log(Level.FINE,className+":"+sFuncName,e);
			throw new SharepointException(e.toString());
		}
		if(listItems!=null){
			LOGGER.info("found: "+listItems.size()+" Items in List ["+ list.getInternalName()+"]"); 
		}
		LOGGER.exiting(className, sFuncName);
		return listItems;
	}
	
	public String getNextPage() {
		return nextPage;
	}
	/*public void setNextPage(String nextPage) {
		this.nextPage = nextPage;
	} */ 
}
