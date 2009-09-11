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

package com.google.enterprise.connector.sharepoint.spiimpl;

import java.io.InputStream;
import java.net.URLDecoder;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;

import com.google.enterprise.connector.sharepoint.client.Attribute;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;
import com.google.enterprise.connector.spiimpl.BinaryValue;
import com.google.enterprise.connector.spiimpl.BooleanValue;
import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.StringValue;

/**
 * Class to hold data regarding a sharepoint document.
 * Anything that is sent ot GSA for indexing must be represented as an instance of this class.
 * @author nitendra_thakur
 *
 */
public class SPDocument implements Document, Comparable<SPDocument>{
	private String docId;
	private String url;
	private Calendar lastMod;
	private String author = SPConstants.NO_AUTHOR;
	private String objType = SPConstants.NO_OBJTYPE;
	private String parentWebTitle = "No Title";

	private String feedType;
	private String spType;
	private InputStream content = null;//content of documents
	private String content_type = null;
	private ActionType action = ActionType.ADD; // By default mark it as to be added.
	
	private String folderLevel;
	private String listguid;
	private String webid;
	
	// Added for document content download
	private String contentDwnldURL;
	private SharepointClientContext sharepointClientContext;
	
	private final Logger LOGGER = Logger.getLogger(SPDocument.class.getName());
		
	/**
	 * A guess as to how many attributes we should allow for initially.
	 */
	private final int INITIALATTRLISTSIZE = 5;
	private final ArrayList<Attribute> attrs = new ArrayList<Attribute>(INITIALATTRLISTSIZE);


	/**
	 * 
	 * @return List GUID
	 */
	public String getListGuid(){
		return listguid;
	}

	/**
	 * 
	 * @param newguid
	 */
	public void setListGuid(final String newguid){
		if(newguid!=null){
			listguid= newguid;
		}
	}
	
	/**
	 * 
	 * @param inDocId
	 * @param inUrl
	 * @param inLastMod
	 * @param inAuthor
	 * @param inObjType
	 * @param inParentWebTitle
	 * @param inFeedType
	 * @param inSpType
	 */
	public SPDocument(final String inDocId, final String inUrl, final Calendar inLastMod, final String inAuthor,final String inObjType,
						final String inParentWebTitle,final String inFeedType, final String inSpType) {
		docId = inDocId;
		url = inUrl;
		lastMod = inLastMod;
		author = inAuthor;
		objType = inObjType;
		parentWebTitle =inParentWebTitle;
		feedType = inFeedType;
		spType = inSpType;
		LOGGER.config("docid["+inDocId+"], URL["+inUrl+"], LastMod["+inLastMod+"], ObjectType["+inObjType+"]," +
				"author["+inAuthor+"],parentWebTitle["+parentWebTitle+"], feedType [" + inFeedType + "], spType ["+ inSpType + "] ");
	}
		
	/**
	 * To be used while loading the lastDocument from the state file.
	 */
	public SPDocument(final String inDocId, final Calendar inLastMod, final String inFolderLevel, final ActionType inAction) {
		docId = inDocId;
		lastMod = inLastMod;
		folderLevel = inFolderLevel;
		action = inAction;
	}
	
	/**
	 * 
	 * @return last modified date
	 */
	public Calendar getLastMod() {
		return lastMod;
	}

	/**
	 * 
	 * @return last modified date as string
	 */
	public String getLastDocLastModString() {
		try {
			return Util.formatDate(lastMod);
		} catch(final Exception e) {
			return null;
		}
	}
	
	/**
	 * 
	 * @return document ID
	 */
	public String getDocId() {
		return docId;
	}

	/**
	 * 
	 * @return document URL
	 */
	public String getUrl() {
		return url;
	}   

	/**
	 * 
	 * @param strUrl
	 */
	public void setUrl(final String strUrl) {
		if(url!=null){
			url = strUrl;
		}
	} 

	/**
	 * 
	 * @return document properties
	 */
	public ArrayList getAllAttrs() {
		return attrs;
	}

	// debug routine
	public void dumpAllAttrs() {
		if(attrs==null){
			return;
		}

		for (Object element : attrs) {
			final Attribute attr = (Attribute) element;
			System.out.println(attr.getName() + "=" + attr.getValue());
		}
	}
	
	/**
	 * Set an attribute which may not be one of the named ones listed above.
	 * @param key
	 * @param value
	 */
	public void setAttribute(final String key, final String value) {
		if(key != null){
			attrs.add(new Attribute(key, value));
		}
	}

	/**
	 * For setting document properties
	 */
	public void setAllAttributes(final List<Attribute> lstAttributes) {
		if(lstAttributes!=null){
			attrs.addAll(lstAttributes);
		}
	}

	/**
	 * 
	 * @return author of the document
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * 
	 * @return the document type
	 */
	public String getObjType() {
		return objType;
	}

	/**
	 * 
	 * @param inAuthor
	 */
	public void setAuthor(final String inAuthor) {
		if(inAuthor!=null){
			author = inAuthor;
		}
	}

	/**
	 * 
	 * @param inObjType
	 */
	public void setObjType(final String inObjType) {
		if(inObjType!=null){
			objType = inObjType;
		}
	}

	/**
	 * For SPDocument equality comparison
	 */
	public boolean equals(final Object obj) {
		if(obj instanceof SPDocument) {
			final SPDocument doc = (SPDocument) obj;
			if((doc != null) && (docId != null) && (doc.docId != null) &&  (webid != null) && (listguid != null) && (doc.webid != null) && (doc.listguid != null)
			   && webid.equals(doc.webid) && listguid.equals(doc.listguid) && docId.equals(doc.docId)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * In case of SP2003, following fields are tried in order: lastModified, ID, URL
	 * In case of SP2007, ID is used for ordering. Though, in case of renamed/restored folders, items under the folders are sent in order of the folder level.
	 * 	This is required because of the special handling folder rename/restoration. We must ensure that all the items under a renamed/restored folder are sent first for a given changeToken.
	 * Note: folderLevel info is set for a doc only in case of parent folder rename/restore.
	 * 
	 * If the two docs one from SP2007 and another from SP2003 is compared, SP2007 will always get the prefrence.
	 * @param doc
	 */
	public int compareTo(final SPDocument doc) {

		if(doc==null){
			return -1;
		}
		
		if(SPConstants.SP2007.equalsIgnoreCase(spType) && SPConstants.SP2003.equalsIgnoreCase(doc.spType)) {
			return -1;
		} else if(SPConstants.SP2007.equalsIgnoreCase(doc.spType) && SPConstants.SP2003.equalsIgnoreCase(spType)) {
			return 1;
		}
			
		int comparison = 0;
		
		if(SPConstants.SP2007.equalsIgnoreCase(spType)) {
			if((folderLevel != null) || (doc.folderLevel != null)) {
				if((folderLevel != null) && (doc.folderLevel != null)
						&& (folderLevel.length() != 0) && (doc.folderLevel.length() != 0)) {
					comparison = folderLevel.compareTo(doc.folderLevel);
				} else if(((folderLevel == null) || (folderLevel.length() == 0)) 
						&& ((doc.folderLevel != null) && (doc.folderLevel.length() != 0))) {
					return 1; // incoming doc should be sent before the current doc. We always send renamed/restored folder items first 
				} else if(((folderLevel != null) && (folderLevel.length() != 0)) 
						&& ((doc.folderLevel == null) || (doc.folderLevel.length() == 0))) {
					return -1; // current doc should be sent before the incoming doc. We always send renamed/restored folder items first 
				} 
			}			
		} else {
			comparison = lastMod.getTime().compareTo(doc.lastMod.getTime());
		}
		
		if(comparison == 0) {
			final String tmpDocID1 = Util.getOriginalDocId(docId, feedType);
			final String tmpDocID2 = Util.getOriginalDocId(doc.docId, doc.feedType);
			int id1 = 0;
			int id2 = 0;
			try{
				id1 = Integer.parseInt(tmpDocID1);
			}catch(final Exception e){
				return 1;
			}
			try{
				id2 = Integer.parseInt(tmpDocID2);
			}catch(final Exception e){
				return -1;
			}
			comparison = id1-id2;
			if(comparison != 0) {
				return comparison;
			}

			//compare the URLs
			String docURL1st = new String(url);
			String docURL2nd = new String(doc.url);
			try {
				docURL1st = URLDecoder.decode(docURL1st,"UTF-8");
				docURL2nd = URLDecoder.decode(docURL2nd, "UTF-8");
			} catch(final Exception e) {
				//eatup exception. Use the original URL...
			}
			if(docURL1st!=null){
				comparison = docURL1st.compareTo(docURL2nd);
			}
			
		}    
		return comparison;
	}
	
	/**
	 * Returns the property object for a given property name. CM calls this to gather all the information about a document.
	 * The property names that are requested can be either well known properties defined under connector SPI or, connector can inform them pre-hand during the call to getAll propertoes. 
	 */
	public Property findProperty(final String strPropertyName) throws RepositoryException {
		final Collator collator = Util.getCollator();
		if(collator.equals(strPropertyName,SpiConstants.PROPNAME_CONTENTURL)){			
			return new SPProperty(SpiConstants.PROPNAME_CONTENTURL, new StringValue(getUrl()));			
		}else if(collator.equals(strPropertyName,SpiConstants.PROPNAME_CONTENT)){ 
			if(SPConstants.CONTENT_FEED.equalsIgnoreCase(feedType) && ActionType.ADD.equals(action)) {
				if(null == content) {
					String status = downloadContents();
					if(!SPConstants.CONNECTIVITY_SUCCESS.equalsIgnoreCase(status)) {
						LOGGER.log(Level.WARNING, "Following response received while downloading contents: " + status);
					}
				}
				return new SPProperty(SpiConstants.PROPNAME_CONTENT, new BinaryValue(content));
			}
		}else if(collator.equals(strPropertyName,SpiConstants.PROPNAME_MIMETYPE)){ 
			if(SPConstants.CONTENT_FEED.equalsIgnoreCase(feedType) && ActionType.ADD.equals(action)) {
				if(null == content) {
					String status = downloadContents();
					if(!SPConstants.CONNECTIVITY_SUCCESS.equalsIgnoreCase(status)) {
						LOGGER.log(Level.WARNING, "Following response recieved while downloading contents: " + status);
					}
				}
				return new SPProperty(SpiConstants.PROPNAME_MIMETYPE, new StringValue(content_type));
			}
		}else if(collator.equals(strPropertyName,SpiConstants.PROPNAME_SEARCHURL)){
			if(!SPConstants.CONTENT_FEED.equalsIgnoreCase(feedType)) {
				return new SPProperty(SpiConstants.PROPNAME_SEARCHURL, new StringValue(getUrl()));
			}
		}else if(collator.equals(strPropertyName,SpiConstants.PROPNAME_DISPLAYURL)){
			return new SPProperty(SpiConstants.PROPNAME_DISPLAYURL, new StringValue(getUrl()));			
		}else if(collator.equals(strPropertyName,SPConstants.PARENT_WEB_TITLE)){
			return new SPProperty(SPConstants.PARENT_WEB_TITLE, new StringValue(getParentWebTitle()));
		}else if(collator.equals(strPropertyName,SpiConstants.PROPNAME_DOCID)){
			return new SPProperty(SpiConstants.PROPNAME_DOCID, new StringValue(getDocId()));			
		}else if(collator.equals(strPropertyName,SpiConstants.PROPNAME_LASTMODIFIED)){
			return new SPProperty(SpiConstants.PROPNAME_LASTMODIFIED, new DateValue(getLastMod()));
		}else if(collator.equals(strPropertyName,SPConstants.LIST_GUID)){
			return new SPProperty(SPConstants.LIST_GUID, new StringValue(getListGuid()));
		}else if(collator.equals(strPropertyName,SPConstants.SPAUTHOR)){
			return new SPProperty(SPConstants.SPAUTHOR, new StringValue(getAuthor()));
		}else if(strPropertyName.equals(SPConstants.OBJECT_TYPE)){
			return new SPProperty(SPConstants.OBJECT_TYPE, new StringValue(getObjType()));
		}else if(strPropertyName.equals(SpiConstants.PROPNAME_ISPUBLIC)){
			return new SPProperty(SpiConstants.PROPNAME_ISPUBLIC, BooleanValue.makeBooleanValue(false));
		}else if(strPropertyName.equals(SpiConstants.PROPNAME_ACTION)){
			return new SPProperty(SpiConstants.PROPNAME_ISPUBLIC, new StringValue(action.toString()));			
		}
		
		else{
			for (final Iterator iter=getAllAttrs().iterator();iter.hasNext();){
				final Attribute attr = (Attribute) iter.next();
				if(collator.equals(strPropertyName,attr.getName())){
					return new SPProperty(strPropertyName, new StringValue(attr.getValue().toString()));
				}
			}
		}
		
		LOGGER.finer("no matches found for["+strPropertyName+"]");
		return null;//no matches found
	}

	/**
	 * Return a set of metadata that are attached with this instance of SPDocument.
	 * CM will then call findProperty for each metadata to construct the feed for this document.
	 */
	public Set<String> getPropertyNames() throws RepositoryException {
		final Set<String> s = new HashSet<String>();
		s.add(SPConstants.OBJECT_TYPE);
		s.add(SPConstants.LIST_GUID);
		s.add(SPConstants.SPAUTHOR);
		s.add(SPConstants.PARENT_WEB_TITLE);
		
		// get the "extra" metadata fields, including those added by user:
		for (final Iterator iter=getAllAttrs().iterator();iter.hasNext();){
			final Attribute attr = (Attribute) iter.next();
			s.add(attr.getName().toString());
		}
		LOGGER.log(Level.FINEST, "Document properties set: " + s + " for docID [ " + docId + " ], docURL [ " + url + " ]. ");
		return s;
	}

	/**
	 * 
	 * @return parent web title
	 */
	public String getParentWebTitle() {
		return parentWebTitle;
	}

	/**
	 * 
	 * @param inParentWebTitle
	 */
	public void setParentWebTitle(final String inParentWebTitle) {
		if(null!=inParentWebTitle){
			parentWebTitle = inParentWebTitle;
		}
	}
	
	/**
	 * For setting document ID
	 * required while submitting document for feed
	 */
	public void setDocId(final String docId) {
		this.docId = docId;
	}
	
	/**
	 * For downloading the contents of the documents usinf its URL. USed in case of content feed only.
	 * @return the status of download
	 */
	private String downloadContents() {
		if(null == sharepointClientContext) {
			LOGGER.log(Level.SEVERE, "Failed to download document content because the connector context is not found!");
			return SPConstants.CONNECTIVITY_FAIL;
		}
		LOGGER.config("Document URL [ " + contentDwnldURL + " is getting processed for contents");
		int responseCode = 0;
		final String docURL = Util.encodeURL(contentDwnldURL);
		HttpMethodBase method = null;
		try {			
			method = new GetMethod(docURL);
			responseCode = sharepointClientContext.checkConnectivity(docURL, method);
			if(null == method) {
				return SPConstants.CONNECTIVITY_FAIL;			
			}
			content = method.getResponseBodyAsStream();
			final Header contentType = method.getResponseHeader("Content-Type");
			if(contentType != null) {
				content_type = contentType.getValue();
			}
		} catch (final Exception e) {
			LOGGER.log(Level.WARNING,"Unable to fetch contents from URL: " + url, e);
			return e.getLocalizedMessage();
		} catch(Throwable t) {
			LOGGER.log(Level.WARNING,"Unable to fetch contents from URL: " + url, t);
			return t.getLocalizedMessage();
		}
		
		if(responseCode == 200) {		
			return SPConstants.CONNECTIVITY_SUCCESS;
		} else {
			return "" + responseCode;
		}
	}	

	/**
	 * @return the content_type
	 */
	public String getContent_type() {
		return content_type;
	}

	/**
	 * @param content_type the content_type to set
	 */
	public void setContent_type(final String content_type) {
		this.content_type = content_type;
	}

	/**
	 * @return the feedType
	 */
	public String getFeedType() {
		return feedType;
	}

	/**
	 * @return the folderLevel
	 */
	public String getFolderLevel() {
		return folderLevel;
	}

	/**
	 * @param folderLevel the folderLevel to set
	 */
	public void setFolderLevel(final String folderLevel) {
		this.folderLevel = folderLevel;
	}

	/**
	 * @return the action
	 */
	public ActionType getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(final ActionType action) {
		this.action = action;
	}

	/**
	 * @return the spType
	 */
	public String getSharePointType() {
		return spType;
	}

	/**
	 * @return the webid
	 */
	public String getWebid() {
		return webid;
	}

	/**
	 * @param webid the webid to set
	 */
	public void setWebid(final String webid) {
		this.webid = webid;
	}

	/**
	 * @return the sharepointClientContext
	 */
	public SharepointClientContext getSharepointClientContext() {
		return sharepointClientContext;
	}

	/**
	 * @param sharepointClientContext the sharepointClientContext to set
	 */
	public void setSharepointClientContext(
			SharepointClientContext sharepointClientContext) {
		this.sharepointClientContext = sharepointClientContext;
	}

	/**
	 * @return the contentDwnldURL
	 */
	public String getContentDwnldURL() {
		return contentDwnldURL;
	}

	/**
	 * @param contentDwnldURL the contentDwnldURL to set
	 */
	public void setContentDwnldURL(String contentDwnldURL) {
		this.contentDwnldURL = contentDwnldURL;
	}
}
