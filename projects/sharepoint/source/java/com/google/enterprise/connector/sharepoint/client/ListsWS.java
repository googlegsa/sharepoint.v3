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
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.sharepoint.generated.ListsStub;
import com.google.enterprise.connector.sharepoint.generated.ListsStub.GetAttachmentCollection;
import com.google.enterprise.connector.sharepoint.generated.ListsStub.GetListItemChanges;
import com.google.enterprise.connector.sharepoint.generated.ListsStub.GetListItems;
import com.google.enterprise.connector.sharepoint.generated.ListsStub.ViewFields_type14;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;

/**
 * This class holds data and methods for any call to Lists Web Service.
 */
public class ListsWS {
	private static final String LISTS_END_POINT = "_vti_bin/Lists.asmx";
	private static final Log LOGGER = LogFactory.getLog(ListsWS.class);
	final static String COLON = ":";
	final String sUniqueID="ows_UniqueId";
	final String sModified="ows_Modified";
	final String sFileRef="ows_FileRef";
	final String sMetaInfo="ows_MetaInfo";
	final String sContentType="ows_ContentType";
	final String sEditor="ows_Editor";
	final String sQueryOptions = "QueryOptions";
	final String sViewAttributes = "ViewAttributes";
	final String sScope = "Scope";
	final String sRecursive = "Recursive";
	final String sQuery = "Query";
	final String sOrderBy = "OrderBy";
	final String sDispForm = "DispForm.aspx?ID=";
	final String sLists = "Lists";
	final String sID = "ows_ID";
	final String sDocument = "Document";
	final String HASH = "#";
	final String COMMA = ",";
	final String sURL = "ows_URL";
	final String URL_SEP ="://";
	
	private SharepointClientContext sharepointClientContext;
	private String endpoint;
	private ListsStub stub;
	private ViewsWS viewsWS;

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
	private static final ArrayList BLACK_LIST;
	static {
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
	}

	/**
	 * The "whitelist" is SharePoint meta attributes that we WILL
	 * pass to the GSA but will treat specially, so they should not be swept
	 * up into the 'attrs'.
	 * There is no operational difference between blacklist and whitelist;
	 * in both cases the attributes are not passed to the GSA.
	 */
	private static final ArrayList WHITE_LIST;
	static {
		WHITE_LIST = new ArrayList();
		WHITE_LIST.add(Pattern.compile(".*vti_title$"));
		WHITE_LIST.add(Pattern.compile(".*vti_author$"));
	}

	/**
	 * Determine if any entry in a given List matches the given input.
	 * @param list
	 * @param input
	 * @return boolean
	 */
	private static boolean listMatches(List list, String input) {
		if(list!=null){
			for (int iPattern=0;iPattern<list.size();++iPattern) {
				Pattern pattern = (Pattern) list.get(iPattern);
				Matcher matcher = pattern.matcher(input);
				if (matcher.matches()) {
					return true;
				}
			}
		}
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
		if(meta!=null){
			String[] parts = meta.split(COLON);
			if (parts.length < 2){
				return "";
			}
			String name = parts[0].trim();
			if (!listMatches(BLACK_LIST, name) && !listMatches(WHITE_LIST, name)) {
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
		return "";
	}

	public ListsWS(SharepointClientContext inSharepointClientContext) 
	throws RepositoryException {
		if(inSharepointClientContext!=null){
			this.sharepointClientContext = inSharepointClientContext;
			endpoint = inSharepointClientContext.getProtocol()+URL_SEP + inSharepointClientContext.getHost() + COLON 
			+inSharepointClientContext.getPort() 
			+ Util.getEscapedSiteName(inSharepointClientContext.getsiteName()) + LISTS_END_POINT;
			try {
				stub = new ListsStub(endpoint);
				inSharepointClientContext.setStubWithAuth(stub, endpoint);
				viewsWS = new ViewsWS(inSharepointClientContext);
			} catch (AxisFault e) {
				throw new SharepointException(e.toString());        
			}
		}
	}

	public ListsWS(SharepointClientContext inSharepointClientContext,String siteName) throws RepositoryException {
		final String HTTP ="http";
		final String HTTPS ="https";
		
		if(inSharepointClientContext!=null){
			this.sharepointClientContext = inSharepointClientContext;
		}
		
		if(siteName!=null){
			if (siteName.startsWith(HTTP+URL_SEP)) {
				siteName = siteName.substring(7);
				endpoint = HTTP+URL_SEP + Util.getEscapedSiteName(siteName) + LISTS_END_POINT;
			}else if(siteName.startsWith(HTTPS+URL_SEP)) {
				siteName = siteName.substring(8);
				endpoint = HTTPS+URL_SEP + Util.getEscapedSiteName(siteName) + LISTS_END_POINT;
			} else {
				endpoint = Util.getEscapedSiteName(siteName) + LISTS_END_POINT;
			}
		}
		try {
			stub = new ListsStub(endpoint);
			sharepointClientContext.setStubWithAuth(stub, endpoint);
			viewsWS = new ViewsWS(sharepointClientContext);
		} catch (AxisFault e) {
			throw new SharepointException(e.toString());
		}     
	}

	private ViewFields_type14 makeViewFields(String listName)
	throws SharepointException {
		final String sName = "Name";
		final String sAuthor = "Author";
		final String sViewFields = "viewFields";
		final String sFieldRef = "FieldRef";
		
		if(listName==null){
			throw new SharepointException("Unable to get listname");
		}
		List viewFieldStrings = viewsWS.getViewFields(listName);
		if (viewFieldStrings == null) {
			return null;
		}
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespace ms = factory.createOMNamespace(
				"http://schemas.microsoft.com/sharepoint/soap/", "ms");

		OMElement subRoot = factory.createOMElement(sViewFields, ms);
		OMElement childTest = factory.createOMElement(sFieldRef, ms);
		ViewFields_type14 viewFields = new ListsStub.ViewFields_type14();
		for (int iField=0;iField<viewFieldStrings.size();++iField) {
			String fieldName = (String) viewFieldStrings.get(iField);
			OMElement field = factory.createOMElement(sFieldRef, ms);
			field.addAttribute(sName, fieldName, ms);
			subRoot.addChild(field);
		}

		childTest.addAttribute(sName, sAuthor, ms);
		viewFields.setExtraElement(subRoot);
		return viewFields;
	}

	/**
	 * Gets all the list items of a particular list.
	 * @param listName internal name of the list
	 * @return list of sharepoint documents corresponding to items in the list.
	 * @throws SharepointException 
	 * @throws MalformedURLException 
	 */
	public List getListItems(String listName) throws SharepointException, MalformedURLException {
		String sFunctionName = "getListItems(String listName)";
		
		final String SLASH = "/";
		if(listName==null){
			throw new SharepointException("list name is null");
		}
		
		ArrayList listItems = new ArrayList();
		String urlPrefix = sharepointClientContext.getProtocol()+URL_SEP + sharepointClientContext.getHost() + COLON 
		+ sharepointClientContext.getPort() + SLASH;

		ListsStub.GetListItems req = new ListsStub.GetListItems();
		req.setListName(listName);
		req.setQuery(null);

		req.setViewFields(null);
		req.setRowLimit("");
		req.setViewName("");
		req.setWebID("");

		/* Setting query options to be Recursive, so that docs under folders are
		 * retrieved recursively
		 */
		ListsStub.QueryOptions_type36 queryOptions = 
			new ListsStub.QueryOptions_type36();
		req.setQueryOptions(queryOptions);
		OMFactory omfactory = OMAbstractFactory.getOMFactory();                  
		OMElement options = omfactory.createOMElement(sQueryOptions, null);
		queryOptions.setExtraElement(options);
		OMElement va = omfactory.createOMElement(sViewAttributes, null,
				options);
		OMAttribute attr = omfactory.createOMAttribute(sScope, null,
		sRecursive);    
		va.addAttribute(attr);

		/* Setting the query so that the returned items are in lastModified 
		 * order. 
		 */    
		ListsStub.Query_type34 query = new ListsStub.Query_type34();
		req.setQuery(query);

		OMElement queryOM = omfactory.createOMElement(sQuery, null);
		query.setExtraElement(queryOM);
		OMElement orderBy = omfactory.createOMElement(sOrderBy, null, queryOM);
		orderBy.addChild(omfactory.createOMText(orderBy, sModified));

		try {
			ListsStub.GetListItemsResponse res = stub.GetListItems(req);
			OMFactory omf = OMAbstractFactory.getOMFactory();
			OMElement oe = res.getGetListItemsResult().getOMElement(GetListItems.MY_QNAME, omf);
			LOGGER.debug(sFunctionName +": "+oe.toString());
			StringBuffer url = new StringBuffer();
			for (Iterator ita = oe.getChildElements(); ita.hasNext();) {
				OMElement resultOmElement = (OMElement) ita.next();
				Iterator resultIt = resultOmElement.getChildElements();
				OMElement dataOmElement = (OMElement) resultIt.next();
				for (Iterator dataIt = dataOmElement.getChildElements();
				dataIt.hasNext();) {
					OMElement rowOmElement = (OMElement) dataIt.next();            
					if (rowOmElement.getAttribute(new QName(sFileRef)) != null) {
						String docId = rowOmElement.getAttribute(
								new QName(sUniqueID)).getAttributeValue();  
						String lastModified = rowOmElement.getAttribute(
								new QName(sModified)).getAttributeValue();
						String fileName = rowOmElement.getAttribute(
								new QName(sFileRef)).getAttributeValue();
						fileName = fileName.substring(fileName.indexOf(HASH) + 1);
						url.setLength(0);
						url.append(urlPrefix);
						url.append(fileName);              

						String[] includedURLs = sharepointClientContext.getIncludedURlList() ; 
						String[] excludedURLs = sharepointClientContext.getExcludedURlList() ; 
						SharepointClientUtils spUtils = new SharepointClientUtils();   
						LOGGER.debug(sFunctionName+": URL :"+url);
						if(spUtils.isIncludedUrl(includedURLs,excludedURLs, url.toString())) {	  
							try {
								LOGGER.debug(sFunctionName+" : included URL : "+url.toString());
								SPDocument doc;

								//get object type
								String strObjectType = rowOmElement.getAttribute(new QName(sContentType)).getAttributeValue();
								doc = new SPDocument(docId, url.toString(),Util.listItemsStringToCalendar(lastModified),strObjectType);
								listItems.add(doc);
							} catch (ParseException e) {
								throw new SharepointException(e.toString(), e);
							} 
						}
					}
				}
			}
		} catch (RemoteException e) {
			throw new SharepointException(e.toString(), e);
		}     
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
		if(list==null){
			throw new SharepointException("Unable to get List");
		}
		String sFunctionName = "getGenericListItemChanges(BaseList list, Calendar since)";
		String listName = list.getInternalName();
		ArrayList listItems = new ArrayList();
		String urlPrefix = sharepointClientContext.getProtocol()+URL_SEP + sharepointClientContext.getHost() + COLON 
		+ sharepointClientContext.getPort() 
		+ sharepointClientContext.getsiteName() + "/" +sLists + "/" 
		+ list.getTitle() + "/" + sDispForm;
		ListsStub.GetListItemChanges req = new ListsStub.GetListItemChanges();
		req.setListName(listName);
		req.setViewFields(makeViewFields(list.getInternalName()));  
		if (since != null) {
			req.setSince(Value.calendarToIso8601(since));
		} else {
			req.setSince(null);      
		}
		try {
			ListsStub.GetListItemChangesResponse res = stub.GetListItemChanges(req);
			OMFactory omf = OMAbstractFactory.getOMFactory();
			OMElement oe = res.getGetListItemChangesResult().getOMElement(GetListItemChanges.MY_QNAME, omf);
			StringBuffer url = new StringBuffer();

			for (Iterator ita = oe.getChildElements(); ita.hasNext();) {
				OMElement resultOmElement = (OMElement) ita.next();
				Iterator resultIt = resultOmElement.getChildElements();
				OMElement dataOmElement = (OMElement) resultIt.next();
				for (Iterator dataIt = dataOmElement.getChildElements();
				dataIt.hasNext();) {
					OMElement rowOmElement = (OMElement) dataIt.next();            
					String docId = rowOmElement.getAttribute(
							new QName(sUniqueID)).getAttributeValue();
					String itemId = rowOmElement.getAttribute(
							new QName(sID)).getAttributeValue();
					
					
					/////////////////print values of all the attributes of document /////
					/*System.out.println("---------------LIST ITEM--------------");
					Iterator it = rowOmElement.getAllAttributes();
					while(it.hasNext()){
						OMAttribute attr = (OMAttribute) it.next();
						System.out.println(attr.getLocalName()+" : value= "+attr.getAttributeValue());
					}
					System.out.println("---------------END:LIST ITEM--------------");*/
						
					//end: ///////////////print values of all the attributes of document /////
					
					
					url.setLength(0);
					url.append(urlPrefix);
					url.append(itemId); 
					LOGGER.debug(sFunctionName+": URL :"+url);
					String[] includedURLs = sharepointClientContext.getIncludedURlList() ; 
					String[] excludedURLs = sharepointClientContext.getExcludedURlList() ; 
					SharepointClientUtils spUtils = new SharepointClientUtils();   
					if(spUtils.isIncludedUrl(includedURLs,excludedURLs, url.toString())) {	  
						LOGGER.debug(sFunctionName+" : included URL :"+url.toString());
						SPDocument doc;

						//get object type
						OMAttribute attr=rowOmElement.getAttribute(new QName(sContentType));
						String strObjectType = SPDocument.OBJTYPE_LIST_ITEM;
						if(attr!=null){
							strObjectType=attr.getAttributeValue();	
						}
						
						//get the author
						OMAttribute attr2=rowOmElement.getAttribute(new QName(sEditor));
						String author = SPDocument.NO_AUTHOR;
						if(attr2!=null){
							author=attr2.getAttributeValue();	
							author=author.substring(author.indexOf(HASH) + 1); 
						}
						
						
//						doc = new SPDocument(docId, url.toString(), list.getLastMod(),strObjectType);
						doc = new SPDocument(docId, url.toString(), list.getLastMod(),author,strObjectType);
						listItems.add(doc);
					}
				}
			}
			Collections.sort(listItems);
		} catch (RemoteException e) {
			throw new SharepointException(e.toString(), e);
		}     
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
		if(list==null){
			throw new SharepointException("Unable to get list");
		}
		if(sharepointClientContext==null){
			throw new SharepointException(sFunctionName+": sharepointClientContext not found");
		}
		
		
		String listName = list.getInternalName();
		ArrayList listItems = new ArrayList();
		String urlPrefix = sharepointClientContext.getProtocol()+URL_SEP + sharepointClientContext.getHost() + COLON 
		+ sharepointClientContext.getPort() + "/";
		ListsStub.GetListItemChanges req = new ListsStub.GetListItemChanges();
		req.setListName(listName);
		req.setViewFields(makeViewFields(list.getInternalName()));   
		if (since != null) {
			req.setSince(Value.calendarToIso8601(since));
		} else {
			req.setSince(null);      
		}
		try {
			ListsStub.GetListItemChangesResponse res = stub.GetListItemChanges(req);
			if(res==null){
				throw new SharepointException("Unable to get ListItemChangesResponse");
			}
			OMFactory omf = OMAbstractFactory.getOMFactory();
			OMElement oe = res.getGetListItemChangesResult().getOMElement(GetListItemChanges.MY_QNAME, omf);
			if(oe!=null){
				StringBuffer url = new StringBuffer();
				for (Iterator ita = oe.getChildElements(); ita.hasNext();) {
					OMElement resultOmElement = (OMElement) ita.next();
					Iterator resultIt = resultOmElement.getChildElements();
					OMElement dataOmElement = (OMElement) resultIt.next();
					for (Iterator dataIt = dataOmElement.getChildElements();
					dataIt.hasNext();) {
						OMElement rowOmElement = (OMElement) dataIt.next();  
						if (rowOmElement.getAttribute(new QName(sFileRef)) != null) {
							String docId = rowOmElement.getAttribute(
									new QName(sUniqueID)).getAttributeValue();  
							String lastModified = rowOmElement.getAttribute(
									new QName(sModified)).getAttributeValue();
							String fileName = rowOmElement.getAttribute(
									new QName(sFileRef)).getAttributeValue();
							
							/*String authorName = rowOmElement.getAttribute(
									new QName(sEditor)).getAttributeValue();//e.g.1073741823;#System Account
							//clean author name
							authorName=authorName.substring(authorName.indexOf(HASH) + 1); 
							System.out.println("author: "+authorName);*/
							
							
							/////////////////print values of all the attributes of document /////
							/*System.out.println("---------------DOCUMENT--------------");
							Iterator it = rowOmElement.getAllAttributes();
							while(it.hasNext()){
								OMAttribute attr = (OMAttribute) it.next();
								System.out.println(attr.getLocalName()+" : value= "+attr.getAttributeValue());
							}
							System.out.println("---------------END:DOCUMENT--------------");
								*/
							//end:print values of all the attributes of document /////
							
							
							/*
							 * An example of ows_FileRef is 
							 * 1;#unittest/Shared SPDocuments/sync.doc 
							 * We need to get rid of 1;#
							 */
							fileName = fileName.substring(fileName.indexOf(HASH) + 1);  
							LOGGER.debug(sFunctionName +" : [ filename: "+fileName+"  |modified: "+lastModified+" ]");
							url.setLength(0);
							url.append(urlPrefix);
							url.append(fileName);    
							String metaInfo = rowOmElement.getAttribute(
									new QName(sMetaInfo)).getAttributeValue();
	
							LOGGER.debug(sFunctionName+": URL :"+url);
							String[] includedURLs = sharepointClientContext.getIncludedURlList() ; 
							String[] excludedURLs = sharepointClientContext.getExcludedURlList() ; 
							SharepointClientUtils spUtils = new SharepointClientUtils();   
							if(spUtils.isIncludedUrl(includedURLs,excludedURLs, url.toString())) {	  
								LOGGER.debug(sFunctionName+" : included url : "+url.toString());
								try {
									SPDocument doc;
	
	
	//								get object type
									OMAttribute attr=rowOmElement.getAttribute(new QName(sContentType));
									String strObjectType = sDocument;
									if(attr!=null){
										strObjectType=attr.getAttributeValue();	
									}
	//								get the author
									OMAttribute attr2=rowOmElement.getAttribute(new QName(sEditor));
									String author = SPDocument.NO_AUTHOR;
									if(attr2!=null){
										author=attr2.getAttributeValue();	
										author=author.substring(author.indexOf(HASH) + 1); //e.g.1073741823;#System Account
									}								//doc = new SPDocument(docId, url.toString(),Value.iso8601ToCalendar(lastModified),strObjectType);
									doc = new SPDocument(docId, url.toString(),Value.iso8601ToCalendar(lastModified),author,strObjectType);
	
									
									// gather up the rest of the metadata:
									String[] arrayOfMetaInfo = metaInfo.split("\n|\r\n");
									setDocLibMetadata(doc, arrayOfMetaInfo);
									listItems.add(doc);
								} catch (ParseException e) {
									throw new SharepointException(e.toString(), e);
								}  
							}
						}
					}
				}
			}//oe null check
			Collections.sort(listItems);
		} catch (RemoteException e) {
			throw new SharepointException(e.toString(), e);
		}
		return listItems;
	}

	public List getLinkChanges(BaseList list) 
	throws SharepointException, MalformedURLException {
		String sFunctionName = "getLinkChanges(BaseList list, Calendar since)";
		if(list==null){
			throw new SharepointException(sFunctionName+": Unable to get List");
		}
		
		String listName = list.getInternalName();
		ArrayList listItems = new ArrayList();
		ListsStub.GetListItemChanges req = new ListsStub.GetListItemChanges();
		req.setListName(listName);
		req.setViewFields(makeViewFields(list.getInternalName()));   
		req.setSince(null); //get all links
		
		try {
			ListsStub.GetListItemChangesResponse res = stub.GetListItemChanges(req);
			OMFactory omf = OMAbstractFactory.getOMFactory();
			OMElement oe = res.getGetListItemChangesResult().getOMElement(GetListItemChanges.MY_QNAME, omf);
			for (Iterator ita = oe.getChildElements(); ita.hasNext();) {
				OMElement resultOmElement = (OMElement) ita.next();
				Iterator resultIt = resultOmElement.getChildElements();
				OMElement dataOmElement = (OMElement) resultIt.next();
				for (Iterator dataIt = dataOmElement.getChildElements();
				dataIt.hasNext();) {
					OMElement rowOmElement = (OMElement) dataIt.next();  
					if (rowOmElement.getAttribute(new QName(sFileRef)) != null) {

						String url = rowOmElement.getAttribute(
								new QName(sURL)).getAttributeValue();//e.g. http://www.abc.com, abc site"

						//filter out description
						url = url.substring(0, url.indexOf(COMMA));

						LOGGER.debug(sFunctionName+": URL :"+url);
						String[] includedURLs = sharepointClientContext.getIncludedURlList() ; 
						String[] excludedURLs = sharepointClientContext.getExcludedURlList() ; 
						SharepointClientUtils spUtils = new SharepointClientUtils();   
						if(spUtils.isIncludedUrl(includedURLs,excludedURLs, url)) {
							LOGGER.debug(sFunctionName+" : included URL :"+url.toString());
							listItems.add(url);
						}
					}
				}
			}//end: for
			
		} catch (RemoteException e) {
			throw new SharepointException(e.toString(), e);
		}     
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
		if(baseList==null){
			throw new SharepointException("Unable to get List");
		}
		String sFunctionName = "getAttachments(BaseList baseList, SPDocument listItem)";
		String listName = baseList.getInternalName();
		/*
		 * An example of docId is 3;#{BC0E981B-FAA5-4476-A44F-83EA27155513}.
		 * For listItemId, we need to pass "3". 
		 */
		ArrayList listAttachments = new ArrayList();
		try {
			String arrayOflistItemId[] = listItem.getDocId().split(";#");
			String listItemId = arrayOflistItemId[0];
			
			ListsStub.GetAttachmentCollection req = 
				new ListsStub.GetAttachmentCollection();
			req.setListName(listName);
			req.setListItemID(listItemId);
		
			ListsStub.GetAttachmentCollectionResponse res = 
				stub.GetAttachmentCollection(req);
			OMFactory omf = OMAbstractFactory.getOMFactory();
			OMElement oe = res.getGetAttachmentCollectionResult().getOMElement(GetAttachmentCollection.MY_QNAME, omf);
			Iterator ita = oe.getChildElements();
			OMElement attachmentsOmElement = (OMElement) ita.next();
			for (Iterator attachmentsIt =attachmentsOmElement.getChildElements(); attachmentsIt.hasNext();) {
				OMElement attachmentOmElement = (OMElement) attachmentsIt.next();        
				String url = attachmentOmElement.getText();
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
				LOGGER.debug(sFunctionName+": URL :"+url);
				String[] includedURLs = sharepointClientContext.getIncludedURlList() ; 
				String[] excludedURLs = sharepointClientContext.getExcludedURlList() ; 
				SharepointClientUtils spUtils = new SharepointClientUtils();   
				if(spUtils.isIncludedUrl(includedURLs,excludedURLs, url.toString())) {	  
					LOGGER.debug(sFunctionName+":included URL ["+url+" ]");
					SPDocument doc = new SPDocument(url, url, baseList.getLastMod(),SPDocument.OBJTYPE_ATTACHMENT);        
					listAttachments.add(doc);  
				}
			}
			Collections.sort(listAttachments);
		} catch (RemoteException e) {
			throw new SharepointException(e.toString(), e);
		}
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
	}
	
}
