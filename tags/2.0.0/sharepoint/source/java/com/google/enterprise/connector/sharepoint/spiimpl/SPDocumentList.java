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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

/**
 * An implementation of DocumentList
 * Class to represents a list of SPDocuments that are to be sent to GSA.
 * This class holds all the things that should be taken care of 
 * 	1. when a document is actually sent to CM
 * 	2. when checkpoint is received 
 * @author nitendra_thakur
 *
 */
public class SPDocumentList  implements DocumentList {

	private final Logger LOGGER = Logger.getLogger(SPDocumentList.class.getName());
	private List<SPDocument>  documents;
	private Iterator iterator;
	private SPDocument document;
	private GlobalState globalState;//this is required for checkpointing
	private Map aliasMap=null;
	private boolean bFQDNConversion = false;// FQDN conversion flag

	/**
	 * 
	 * @param inDocuments List of {@link SPDocument} to be sent to GSA
	 * @param inGlobalState The current snapshot of {@link GlobalState}
	 */
	public SPDocumentList(final List<SPDocument> inDocuments,final GlobalState inGlobalState) {
		if(inDocuments!=null){
			documents = inDocuments;
		}
		iterator = null;
		document = null;

		globalState = inGlobalState;
	}

	/**
	 * 
	 * @return FQDN conversion value
	 */
	public boolean isFQDNConversion() {
		return bFQDNConversion;
	}

	/**
	 * 
	 * @param conversion
	 */
	public void setFQDNConversion(final boolean conversion) {
		bFQDNConversion = conversion;
	}

	/**
	 * 
	 * @param list2
	 * @return status as boolean value
	 */
	public boolean addAll(final SPDocumentList list2){
		if(list2==null){
			return false;
		}
		return documents.addAll(list2.documents);
	}

	/**
	 * 
	 * @param doc
	 * @return status as boolean value
	 */
	public boolean add(final SPDocument doc){
		if(doc==null){
			return false;
		}
		return documents.add(doc);
	}
	
	/**
	 * The processing that are done when the document is actually sent to CM. Site Alias mapping defined during connectro configuration are used at this point only.
	 */
	public Document nextDocument() {
		if (iterator == null) {
			iterator = documents.iterator();
		}
		if (iterator.hasNext()) {
			document = (SPDocument) iterator.next();//this will save the state of the last document returned
			if(document == null) {
				LOGGER.log(Level.SEVERE, "No document found! ");
				return document;
			}
			
			final ListState listState = globalState.lookupList(document.getWebid(), document.getListGuid());
			final String currentID = Util.getOriginalDocId(document.getDocId(), document.getFeedType());
			
			// for deleted documents, no need to use alias mapping. Only DocID is sufficient.
			if(ActionType.DELETE.equals(document.getAction())) {
				LOGGER.log(Level.INFO, "Sending DocID [ " + document.getDocId() + " ] from List URL [ " + listState.getListURL() + " ] to CM for DELETE");
				if(listState == null) {
					LOGGER.log(Level.WARNING, "Parent list for the document not found!");
					return document;
				}
				
				listState.removeExtraID(currentID);
				if(listState.isExisting()) { // CASE 1: A delete feed is being sent from an existing list
					// ListState.cachedDeletedIDs is used only for exisitng lists.
					listState.addToDeleteCache(currentID);
				} else if (Util.getCollator().equals(listState.getPrimaryKey(),currentID)) { // CASE 2: Last delete feed of a non-existent list is being sent
					// Since list are sent at last and the list is non-exisitng, we can now delete this list state.
					LOGGER.log(Level.INFO, "Removing List State info List URL [ " + listState.getListURL() + " ].");
					final WebState parentWeb = globalState.lookupWeb(document.getWebid(),null);
					if(parentWeb != null) {
						parentWeb.removeListStateFromKeyMap(listState);
						parentWeb.removeListStateFromSet(listState);
					}														
				} else { // CASE 1: A delete feed is being sent from an non-existing list
					/*
					 * We set Deleted document as lastDoc only if list has been deleted. 
					 * Setting a deleted doc as lastDoc when the paremt list is existent and is to be recrawled, may mislead us while tracking the changes. Look at the description / behavior of getListItemChnagesSinceToken.
					 * for deleted docuemnts whose parent list is existing, we use deleteCache for further crawl; lastDoc is not updated for such docuemnts.
					 */
					listState.setLastDocument(document);
				}
				
				// for deleted documents, no need to use alias mapping. Only DocID is sufficient.
				listState.removeDocFromCrawlQueue(document);
				return document;					
			}
			
			listState.setLastDocument(document);
			doAliasMapping();			
			
			LOGGER.log(Level.INFO, "Sending DocID [ " + document.getDocId() + " ], docURL [ " + document.getUrl() + " ] to CM for ADD.");				
		
			listState.removeDocFromCrawlQueue(document);
			
			if((listState.isExisting()) 
					&& ((listState.getCrawlQueue() == null) || (listState.getCrawlQueue().size() == 0))) {		
				LOGGER.log(Level.INFO, "Setting the change token to its latest cached value. All the documents from the list's crawl queue is sent. listURL [ " + listState.getListURL() + " ]. ");
				listState.usingLatestToken();
				if(listState.getNextPage() == null) {
					LOGGER.log(Level.INFO, "Cleaning delete cache...");
					listState.clearDeleteCache();
				}				
			}
			return document;
		}
		return null;
	}

	/**
	 * Tasks that are to be performed when checkpoint is received.
	 */
	public String checkpoint() throws RepositoryException {
		if (document == null) {
			return SPConstants.CHECKPOINT_VALUE;
		}

		LOGGER.log(Level.INFO, "Checkpoint received at document docID [ " + document.getDocId() 
				+ " ], docURL [ " + document.getUrl() + " ], Action [ " + document.getAction() + " ]. ");
		
		final ListState listState = globalState.lookupList(document.getWebid(), document.getListGuid());
		if(null == listState) { // This list would have been deleted
			return SPConstants.CHECKPOINT_VALUE;
		}
		
		if((listState.isExisting())	
				&& (listState.getCrawlQueue() != null) && (listState.getCrawlQueue().size() > 0)		
				&& (listState.getNextPage() == null) && (listState.getChangeToken() != null)) {
			LOGGER.log(Level.INFO, "There are some docs left in the crawl queue of list [ " + listState.getListURL() + " ] at the time of checkpointing. rolling back the change token to its previous value.");
			listState.rollbackToken();			
		}
		
		globalState.setLastCrawledWebID(document.getWebid());
		globalState.setLastCrawledListID(document.getListGuid());
		
		LOGGER.info("checkpoint processed; saving GlobalState to disk.");
		globalState.saveState(); // snapshot it all to disk
	
		return SPConstants.CHECKPOINT_VALUE;
	}
		

	/**
	 * adding methods to get the count of the documents in the list. 
	 * @return no. of documents in the list
	 */
	public int size(){
		if(documents==null){
			return 0;
		}else{
			return documents.size();
		}

	}
	
	/**
	 * 
	 * @param inAliasMap
	 */
	public void setAliasMap(final Map inAliasMap) {
		if(inAliasMap!=null){
			aliasMap = inAliasMap;
		}
	}
	
	/**
	 * 
	 * @return Site Alias Map
	 */
	public Map getAliasMap() {
		return aliasMap;
	}
	
	/**
	 * Re-writes the current document's URL in respect to the Alias mapping specified. 
	 *
	 */
	private void doAliasMapping() {
		if((null == document) || (null == document.getUrl())) {
			return;
		}
		final String url = document.getUrl();			
		URL objURL = null;
		try {
			objURL = new URL(url);
		} catch (final MalformedURLException e) {
			LOGGER.log(Level.WARNING,"Malformed URL!",e);
		}
		String strUrl = "";
		if (objURL == null) {
			return;
		}
		
		boolean matched = false;
		// processing of alias values
		if((null != aliasMap) && (null != aliasMap.keySet())) {
			for(final Iterator aliasItr = aliasMap.keySet().iterator(); aliasItr.hasNext();) {
				
				String aliasPattern = (String) aliasItr.next();
				String aliasValue=(String) aliasMap.get(aliasPattern);
				
				if((aliasPattern==null) || (aliasValue==null)) {
					continue;
				}
				aliasPattern = aliasPattern.trim();
				aliasValue = aliasValue.trim();
				if(aliasPattern.equalsIgnoreCase("") || aliasValue.equalsIgnoreCase("")) {
					continue;
				}
				
				URL patternURL = null;
				String aliasPatternURL = aliasPattern;
				if(aliasPattern.startsWith(SPConstants.GLOBAL_ALIAS_IDENTIFIER)) {
					aliasPatternURL = aliasPattern.substring(1);
				}
	
				try {
					patternURL = new URL(aliasPatternURL);								
				} catch (final MalformedURLException e) {
					LOGGER.log(Level.WARNING,"Malformed alias pattern: "+aliasPatternURL,e);
				}
				if(patternURL==null) {
					continue;
				}
				
				if(!objURL.getProtocol().equalsIgnoreCase(patternURL.getProtocol())) {
					continue;
				}
				
				if(!objURL.getHost().equalsIgnoreCase(patternURL.getHost())) {
					continue;
				}							
				
				
				if(aliasPattern.startsWith(SPConstants.GLOBAL_ALIAS_IDENTIFIER)) {
					aliasPattern = aliasPattern.substring(1);
					if(patternURL.getPort()==SPConstants.MINUS_ONE) {									
						aliasPattern=patternURL.getProtocol() + SPConstants.URL_SEP + patternURL.getHost();
						if(objURL.getPort()!=SPConstants.MINUS_ONE) {									
							aliasPattern += SPConstants.COLON + objURL.getPort(); 
						}
						aliasPattern += patternURL.getFile();
					}								
				} else if((objURL.getPort()==SPConstants.MINUS_ONE) && (patternURL.getPort()==patternURL.getDefaultPort())) {								
					aliasPattern=patternURL.getProtocol() + SPConstants.URL_SEP + patternURL.getHost()+patternURL.getFile();
				} else if((objURL.getPort()==objURL.getDefaultPort()) && (patternURL.getPort()==SPConstants.MINUS_ONE)) {
					aliasPattern=patternURL.getProtocol() + SPConstants.URL_SEP + patternURL.getHost() + SPConstants.COLON + patternURL.getDefaultPort()+patternURL.getFile();
				} else if(objURL.getPort()!=patternURL.getPort()) {
					continue;
				}
					
				if(url.startsWith(aliasPattern)) {
					LOGGER.config("document url["+url+"] has matched against alias source URL [ "+aliasPattern+" ]");								
					strUrl = aliasValue;
					final String restURL = url.substring(aliasPattern.length());
					if(!strUrl.endsWith(SPConstants.SLASH) && !restURL.startsWith(SPConstants.SLASH)) {
						strUrl += SPConstants.SLASH;
					}
					strUrl += restURL;
					matched = true;
					LOGGER.config("document url["+url+"] has been re-written to [ " + strUrl+ " ] in respect to the aliasing.");
					break;									
				}					
			}
		}
		
		if(!matched) {
			strUrl = objURL.getProtocol() + SPConstants.URL_SEP;
			strUrl += getFQDNHostName(objURL.getHost())+ SPConstants.COLON;
			final int portNo = objURL.getPort();
			if(portNo != SPConstants.MINUS_ONE) {
				strUrl += portNo;
			}else {
				strUrl += objURL.getDefaultPort();
			}
			strUrl += objURL.getFile();
		}
		
		document.setUrl(strUrl);		
	}
	
	/**
	 * Converts a host name to a FQDN. This should be called only if the fqdn property has been set to true in the connectorInstance.xml.
	 * @param hostName
	 * @return the host name in FQDN format
	 */
	private String getFQDNHostName(final String hostName){
		if(isFQDNConversion()){
			InetAddress ia = null;
			try {
				ia = InetAddress.getByName(hostName);
			} catch (final UnknownHostException e) {
				LOGGER.log(Level.WARNING,"Exception occurred while converting to FQDN, hostname [ "+hostName+" ].",e);
			}
			if(ia!=null){
				return ia.getCanonicalHostName();
			}
		}
		return hostName;
	}
}