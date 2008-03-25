package com.google.enterprise.connector.sharepoint.client;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.Collator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
/**
 * @author amit_kagrawal
 * {@link DocumentList} implementation for Sharepoint.
 * */
public class SPDocumentList  implements DocumentList {

	private static final String BLANK_STRING = "";
	private static final int MINUS_ONE	 = -1;
	private static final Logger LOGGER = Logger.getLogger(SPDocumentList.class.getName());
	private static String className = SPDocumentList.class.getName();
	private List  documents;
	private Iterator iterator;
	private SPDocument document;
	private GlobalState globalState;//this is required for checkpointing
	

	// For aliasing
	private String aliasHostName;
	private String aliasPort;

	// FQDN conversion flag
	private boolean bFQDNConversion = false;

	/**
	 * 
	 * @param inDocuments
	 * @param inState
	 */
	public SPDocumentList(List inDocuments,GlobalState inState) {
		String sFunctionName = "SPDocumentList(List inDocuments,GlobalState inState)";
		LOGGER.entering(className, sFunctionName);
		if(inDocuments!=null){
			this.documents = inDocuments;
		}
		this.iterator = null;
		this.document = null;
		globalState = inState;
		LOGGER.exiting(className, sFunctionName);
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isFQDNConversion() {
		return bFQDNConversion;
	}

	/**
	 * 
	 * @param conversion
	 */
	public void setFQDNConversion(boolean conversion) {
		bFQDNConversion = conversion;
	}

	/**
	 * 
	 * @param list2
	 * @return
	 */
	public boolean addAll(SPDocumentList list2){
		String sFunctionName = "getAllChildrenSites()";
		LOGGER.entering(className, sFunctionName);
		if(list2==null){
			return false;
		}
		LOGGER.exiting(className, sFunctionName);
		return documents.addAll(list2.documents);
	}

	/**
	 * 
	 */
	public Document nextDocument() {
		String sFunctionName = "nextDocument()";
		LOGGER.entering(className, sFunctionName);
		Collator collator = SharepointConnectorType.getCollator();
		if (iterator == null) {
			iterator = documents.iterator();
		}
		if (iterator.hasNext()) {
			document = (SPDocument) iterator.next();//this will save the state of the last document returned
			//Handling aliasing
			if(document != null){
				String url = document.getUrl();
				URL objURL = null;
				try {
					objURL = new URL(url.toString());
				} catch (MalformedURLException e) {
					LOGGER.warning(className+":"+sFunctionName+ " : "+e.getMessage());
				}
				String strUrl = "";

				if (objURL != null) {

					strUrl = objURL.getProtocol() + "://";
					if(aliasHostName != null && !collator.equals(aliasHostName,BLANK_STRING)){
						strUrl = strUrl + getFQDNHostName(aliasHostName) + ":";
					}else {
						strUrl = strUrl + getFQDNHostName(objURL.getHost())+ ":";
					}

					if(aliasPort != null && !collator.equals(aliasPort,BLANK_STRING)){
						strUrl = strUrl + aliasPort ;
					}else {
						int portNo = objURL.getPort();
						if(portNo != MINUS_ONE) {
							strUrl = strUrl + portNo;
						}else {
							strUrl = strUrl + objURL.getDefaultPort();
						}
					}
					strUrl = strUrl + objURL.getFile();
					
					LOGGER.fine(sFunctionName+": Document URL sending to CM :"+strUrl);
					document.setUrl(strUrl);
				}
			}
			if(document!=null){
				LOGGER.info(className+":"+sFunctionName+":Document URL  ["+document.getUrl()+"]");
			}
			LOGGER.exiting(className, sFunctionName);
			return document;
		}
		LOGGER.exiting(className, sFunctionName);
		return null;
	}

	/**
	 * 
	 */
	public String checkpoint() throws RepositoryException {
		String sFunctionName = "checkpoint()";
		LOGGER.entering(className, sFunctionName);
		if (document == null) {
			return null;
		}
		
		try{
		LOGGER.info(className+":"+sFunctionName+": checkpoint received for " + document.getUrl() + " in list " +document.getListGuid() + " with date "+Util.formatDate(Util.calendarToJoda(document.getLastMod())));
		implementCheckpoint();
		LOGGER.info(className+":"+sFunctionName+": checkpoint processed; saving GlobalState to disk.");
		globalState.saveState(); // snapshot it all to disk
		}catch (Exception e) {
		LOGGER.warning(className+":"+sFunctionName+":Exception: Problem in checkpoint");
		throw new SharepointException(e);
		}
		LOGGER.exiting(className, sFunctionName);
		//return null, because in sharepoint system tere is no internal implementation of checkpoint
		//we need to recrawl every time to get the documents  
		return null;
	}
	
	/**
	 * 
	 * @throws RepositoryException
	 */
	private void implementCheckpoint() throws RepositoryException {
		String sFunctionName = "implementCheckpoint()";
		LOGGER.entering(className, sFunctionName);
		SPDocument docCheckpoint = document;
		String listGuid = document.getListGuid();
		Collator collator = SharepointConnectorType.getCollator();

		/* fix the GlobalState to match 'doc'. Since the Connector Manager may
		 * have finished several lists, we have to iterate through all the 
		 * lists until we hit this one (that's why we save the list GUID).
		 * 
		 * First make sure there's no mistake, and this list is something we
		 * actually know about:

		ListState listCheckpoint = globalState.lookupList(listGuid);
		if (listCheckpoint == null) {
			logger.error(sFunctionName+" : Checkpoint specifies a non-existent list: " + listGuid);
			// what to do here? certainly remove crawl queues from any earlier Lists:
			for (Iterator iter = globalState.getIterator();iter.hasNext();){
				if (docCheckpoint.getLastMod().compareTo(
						Util.jodaToCalendar(listCheckpoint.getLastMod())) > 0) {
					listCheckpoint.setCrawlQueue(null);
				} else {
					break;
				}        
			}
			return;
		}*/
		try{
		LOGGER.finer(className+":"+sFunctionName+": looking for " + listGuid);
		Iterator iterLists = globalState.getIterator();
		boolean foundCheckpoint = false;
		if(iterLists!=null){
			while (iterLists.hasNext() && !foundCheckpoint) {
				ListState listState = (ListState) iterLists.next();
				List crawlQueue = listState.getCrawlQueue();
				if (collator.equals(listState.getGuid(),listGuid)) {
					LOGGER.finer(className+":"+sFunctionName+":found "+ listGuid);
					foundCheckpoint = true;
					// take out everything up to this document's lastMod date
					for (Iterator iterQueue = crawlQueue.iterator(); 
					iterQueue.hasNext();){
						SPDocument docQueue = (SPDocument) iterQueue.next();

						// if this doc is later than the checkpoint, we're done:
						//if (docQueue.getLastMod().compareTo(docCheckpoint.getLastMod()) >  0) {
						if (docQueue.getLastMod().after(docCheckpoint.getLastMod())) {
							LOGGER.fine(className+":"+sFunctionName+": Crawl queue document is after the check point document....ignore check point");
							break;
						}
						// otherwise remove it from the queue
						LOGGER.info(className+":"+sFunctionName+": removing " + docQueue.getUrl() + " from queue");
						iterQueue.remove(); // it's safe to use the iterator's own remove()
						listState.setLastDocCrawled(docQueue);
						if (collator.equals(docQueue.getDocId(),docCheckpoint.getDocId())) {
							break;
						}    
					}  
				} else { // some other list. Assume CM got all the way through the queue
					LOGGER.info(className+":"+sFunctionName+": zeroing crawl queue for " + listState.getUrl());
					if (crawlQueue != null && crawlQueue.size() > 0) {
						listState.setLastDocCrawled((SPDocument) crawlQueue.get(crawlQueue.size() - 1));
					}
					listState.setCrawlQueue(null);
				}
			}
		}
		/* once we've done this, there's no more need to remember where we
		 * were. We can start at the earliest (by lastMod) List we have. 
		 */
		globalState.setCurrentList(null);
		}catch(Exception e){
			LOGGER.warning(className+":"+sFunctionName+"Exception :"+e.getMessage());
			throw new SharepointException(e);
		}
		LOGGER.exiting(className, sFunctionName);
	}

	/**
	 * adding methods to get the count of the documents in the list. 
	 * @return
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
	 * @return
	 */
	public String getAliasHostName() {
		return aliasHostName;
	}

	/**
	 * 
	 * @param inAliasHostName
	 */
	public void setAliasHostName(String inAliasHostName) {
		if(inAliasHostName!=null){
			this.aliasHostName = inAliasHostName;
		}
	}

	/**
	 * 
	 * @return
	 */
	public String getAliasPort() {
		return aliasPort;
	}

	/**
	 * 
	 * @param inAliasPort
	 */
	public void setAliasPort(String inAliasPort) {
		if(inAliasPort!=null){
			this.aliasPort = inAliasPort;
		}
	}
	
	/**
	 * 
	 * @param hostName
	 * @return
	 */
	private String getFQDNHostName(String hostName){
		String sFunctionName = "getFQDNHostName(String hostName)";
		LOGGER.entering(className, sFunctionName);
		if(isFQDNConversion()){
			InetAddress ia = null;
			try {
				ia = InetAddress.getByName(hostName);
			} catch (UnknownHostException e) {
				LOGGER.warning("Exception occurred : "+e.toString());
			}
			if(ia!=null){
				return ia.getCanonicalHostName();
			}
		}
		LOGGER.exiting(className, sFunctionName);
		return hostName;
	}

}