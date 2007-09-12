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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * 
 * Class which maintains all the methods needed to get documents and sites from
 * the sharepoint server. It is a layer between the connector and the actual web
 * services calls .
 * 
 */
public class SharepointClient {
	private static Log logger = LogFactory.getLog(SharepointClient.class);
	private SharepointClientContext sharepointClientContext;
	//private SharepointClientContext sharepointClientContextOriginal = null;

	public SharepointClient(SharepointClientContext inSharepointClientContext)
	throws SharepointException {
		this.sharepointClientContext = inSharepointClientContext;
	}


	/**
	 * For a single ListState, handle its crawl queue (if any). This means add it
	 * to the ResultSet which we give back to the Connector Manager.
	 * @author amit_kagrawal
	 * @param state
	 * @param crawlQueue
	 * @return {@link SPDocumentList} conatining the crawled documents.
	 */
	private SPDocumentList handleCrawlQueueForList(GlobalState state,ListState list) {
		String sFunctionName ="handleCrawlQueueForList(GlobalState state,ListState list)";
		if(list==null){
			logger.warn(sFunctionName+": list is null");
			return null;
		}
		
		logger.debug(sFunctionName+" : handling " + list.getUrl());
		List crawlQueue = list.getCrawlQueue();//return the list of documents for the crawl queue
		
		//set list guid to the documents 
		ArrayList newlist = new ArrayList(); 
		for (Iterator iter = crawlQueue.iterator(); iter.hasNext();) {
			SPDocument doc = (SPDocument) iter.next();
			doc.setListGuid(list.getGuid());
			newlist.add(doc);
			logger.info(sFunctionName+": [ DocId = "+doc.getDocId()+", URL = "+doc.getUrl()+" ]");
		}
		
		SPDocumentList docList = new SPDocumentList(newlist,state);
		
		//for aliasing
		docList.setAliasPort(sharepointClientContext.getAliasPort());
		docList.setAliasHostName(sharepointClientContext.getAliasHostName());
		
		return docList;
	}

	/**
	 * Calls DocsFromDocLibPerSite for all the sites under the current site. It's
	 * possible that we're resuming traversal, because of batch hints. In this
	 * case, we rely on GlobalState's notion of "current". Each time we visit a
	 * List (whether or not it has docs to crawl), we mark it "current." On a
	 * subsequent call to traverse(), we start AFTER the current, if there is one.
	 * One might wonder why we don't just delete the crawl queue when done. The
	 * answer is, we don't consider it "done" until we're notified via the
	 * Connector Manager's call to checkpoint(). Until that time, it's possible
	 * we'd have to traverse() it again.
	 * @author amit_kagrawal
	 * @return {@link SPDocumentList} containing crawled {@link SPDocument}.
	 */
	public SPDocumentList traverse(GlobalState globalstate, int sizeHint) {
		final String sFunctionName="traverse(GlobalState globalstate, int sizeHint)";
		if(globalstate==null){
			logger.warn(sFunctionName+": global state is null");
			return null;
		}
		SPDocumentList resultSet = null;

		int sizeSoFar = 0;
		for (Iterator iter = globalstate.getCircularIterator();iter.hasNext();) {
			ListState list = (ListState) iter.next();
			globalstate.setCurrentList(list);
			if (list.getCrawlQueue() == null) {
				continue;
			}

			SPDocumentList resultsList = handleCrawlQueueForList(globalstate, list);
			if (resultsList.size() > 0) {
				//check for the initial condition
				if(resultSet==null){
					resultSet = resultsList;	
				}else{
					resultSet.addAll(resultsList);
				}
			}
			if(resultSet!=null){
				sizeSoFar = resultSet.size();
			}

			// we heed the batch hint, but always finish a List before checking:
			if (sizeHint > 0 && sizeSoFar >= sizeHint) {
				logger.info("Stopping traversal because batch hint " + sizeHint
						+ " has been reached");
				break;
			}
		}

		return resultSet;
	}

	/**
	 * Find all the Lists under the home Sharepoint site, and update the
	 * GlobalState object to represent them.
	 * 
	 * @param state
	 */
	public void updateGlobalState(GlobalState state) {
		logger.debug("updateGlobalState");
		final String sFunctionName= "updateGlobalState(GlobalState state)";
		if(state==null){
			logger.warn(sFunctionName+": global state is null");
			return;
		}
		/*if(sharepointClientContextOriginal == null){
			sharepointClientContextOriginal = (SharepointClientContext) sharepointClientContext.clone();
		}*/
		
		if(sharepointClientContext==null){
			logger.warn(sFunctionName+": sharepointClientContext is null");
			return;
		}
		try {
			/////////////////////STEPS ////////////////
			//1. START RECRAWL FOR GARBAGE COLLECTION
			//2. INITIALIZE SITEDATAWS TO GET THE WEBS FOR THE PARENT SITE
			//3. GET ALL THE LINKS FOR PARENT SITE
			//4. GET ALL THE PERSONAL SITES
			//5. GET MY LINKS
			//6. LOOP: CHECK IF THE LINKS CONTAINS VALID SHAREPOINT SITES NOT TRAVERSED YET (APPLY LOOKUP AND UPDATE LOOKUP)
			//7. END RECRAWL
			////////////////////////////////////////////

			TreeSet allLinks  = new TreeSet();
			List links = null;
			ArrayList lstLookupForWebs = new ArrayList();
			
			//initially all links will contain the user supplied URL
			String strUserURL =  sharepointClientContext.getProtocol()+"://" + sharepointClientContext.getHost() + ":"
			+ sharepointClientContext.getPort() +sharepointClientContext.getsiteName();

			allLinks.add(strUserURL);

			state.startRecrawl();//start and end recrawl is used for garbage collection...removing the non existant lists
			while(allLinks.size()>0){

				dumpLinks(allLinks); //for debug purpose.
				//get the link and crawal
				String strCurrentSite = (String) allLinks.first();
				sharepointClientContext.setURL(strCurrentSite);
				allLinks.remove(strCurrentSite);

				SiteDataWS siteDataWS = new SiteDataWS(sharepointClientContext);
				List allSites = siteDataWS.getAllChildrenSites();//works ok



				for (int i = 0; i < allSites.size(); i++) {
					SPDocument doc = (SPDocument) allSites.get(i);
					if(doc==null){
						logger.warn(sFunctionName+": doc["+i+"] not found");
						continue;
					}
					String strDocURL =doc.getUrl();
					
					//check lookup table
					if(lstLookupForWebs.contains(strDocURL)) {
						continue;
					} else {
						lstLookupForWebs.add(strDocURL);
					}
					updateGlobalStateFromSite(state, strDocURL);

					/////////for handling links for each site////////////
					//--collect all links first 
					//--filter it to avoid duplicates
					siteDataWS = new SiteDataWS(sharepointClientContext, strDocURL);
					links = siteDataWS.getAllLinks(sharepointClientContext,strDocURL);
					if(links!=null){
						allLinks.addAll(links);
					}
					///////////////////
				}

				//---------------------FOR PERSONAL SITES-----------------------------
				//--CHECK IS WSS OR MOSS(This is required to confirm if we need to fetch the MySite or not)
				//--as WSS sites do not contain mysites
				String strMySiteURL = sharepointClientContext.getMySiteBaseURL(); //--GET THE MYSITE URL
				UserProfileWS userProfileWS = new UserProfileWS(sharepointClientContext);
				if(((strMySiteURL!=null)&&(!strMySiteURL.trim().equals(""))&& userProfileWS.isSPS() && !lstLookupForWebs.contains(strMySiteURL))){
					
					lstLookupForWebs.add(strMySiteURL);
					
					//get All mylinks
					List lstMyLinks = userProfileWS.getMyLinks();
					allLinks.addAll(lstMyLinks);

					List personalSites = userProfileWS.getPersonalSiteList();
					List lstWebsOfOneSite;
					for(int iList=0;iList<personalSites.size();++iList){
						String strURL = (String) personalSites.get(iList);
						

						sharepointClientContext.setURL(strURL); //the SPClinet context is changed as personal site is a new site
						siteDataWS = new SiteDataWS(sharepointClientContext);
						lstWebsOfOneSite = siteDataWS.getAllChildrenSites();

						for (int i = 0; i < lstWebsOfOneSite.size(); i++) {
							SPDocument doc = (SPDocument) lstWebsOfOneSite.get(i);
							String strWebURL = doc.getUrl();
							//check for lookup 
							if(lstLookupForWebs.contains(strWebURL)) {
								continue;
							} else {
								lstLookupForWebs.add(strWebURL);
							}

							updateGlobalStateFromSite(state, strWebURL);
							//for handling links for each site
							siteDataWS = new SiteDataWS(sharepointClientContext,strWebURL);
							links = siteDataWS.getAllLinks(sharepointClientContext,strWebURL);
							allLinks.addAll(links);
						}
					}
				}
			}//end: while
			//---------------------END: FOR PERSONAL SITES-----------------------------

		} catch (SharepointException e) {
			logger.error(e.toString());
		} catch (RepositoryException e) {
			logger.error(e.toString());
		}
		
		state.endRecrawl();
//		set the sharepoint client context to the original one;
		/*if(sharepointClientContextOriginal!=null){
			//by manoj sharepointClientContext=(SharepointClientContext) sharepointClientContextTemp.clone();
			System.out.println("************[" + sharepointClientContextOriginal.getsiteName() + "]");
			sharepointClientContext=sharepointClientContextOriginal;
		}*/
	}


	//for testing if links are comming correctly
	private void dumpLinks(Set allLinks) {
		if(allLinks!=null){
			Iterator it = allLinks.iterator();
			while(it.hasNext()){
				logger.info("Links: "+it.next());
			}
		}
	}


	/**
	 * Gets all the docs from the SPDocument Library and all the items and their 
	 * attachments from Generic Lists and Issues in sharepoint under a given
	 * site. It first calls SiteData web service to get all the Lists. And then
	 * calls Lists web service to get the list items for the lists which are of
	 * the type SPDocument Library, Generic Lists or Issues.
	 * For attachments in Generic List items and Issues, it calls Lists 
	 * web service to get attachments for these list items. 
	 * @return resultSet
	 */
	private void updateGlobalStateFromSite(GlobalState state, String siteName) {
		logger.info("updateGlobalStateFromSite for " + siteName);
		List listItems = new ArrayList();
		Collator collator = SharepointConnectorType.getCollator();
		SiteDataWS siteDataWS;
		ListsWS listsWS;
		try {
			if (siteName == null) {
				siteDataWS = new SiteDataWS(sharepointClientContext);
				listsWS = new ListsWS(sharepointClientContext);
			} else {
				siteDataWS = new SiteDataWS(sharepointClientContext, siteName);
				listsWS = new ListsWS(sharepointClientContext, siteName);
			}
			List listCollection = siteDataWS.getDocumentLibraries();
			List listCollectionGenList = siteDataWS.getGenericLists();
			List listCollectionIssues = siteDataWS.getIssues();
			listCollection.addAll(listCollectionGenList);
			listCollection.addAll(listCollectionIssues);

			if(state==null){
				throw new SharepointException("Unable to obtain global state");
			}
			//Now we have collection of all the 1st level items (e.g. doclib, lists and issues)
			for (int i = 0; i < listCollection.size(); i++) {
				BaseList baseList = (BaseList) listCollection.get(i);
				ListState listState = state.lookupList(baseList.getInternalName());//find the list in the global state
				/*
				 * If we already knew about this list, then only fetch docs that have
				 * changed since the last doc we processed. If it's a new list (e.g. the
				 * first Sharepoint traversal), we fetch everything.
				 */
				if (listState == null) {
					listState = state.makeListState(
							baseList.getInternalName(), baseList.getLastMod());
					listState.setUrl(baseList.getTitle());
					if (collator.equals(baseList.getType(),SiteDataWS.DOC_LIB)) {
						//get all the documents for the list as changes since =null
						listItems = listsWS.getDocLibListItemChanges(
								baseList, null);
					} else if (collator.equals(baseList.getType(),SiteDataWS.GENERIC_LIST) 
							|| collator.equals(baseList.getType(),SiteDataWS.ISSUE)) {
						//get all list items
						listItems = listsWS.getGenericListItemChanges(
								baseList, null);
					}       
					logger.info("creating new listState: " + baseList.getTitle());
				} else {
					logger.info("revisiting old listState: " + listState.getUrl());
//					state.updateList(listState, listState.getLastMod());
					state.updateList(listState, Util.calendarToJoda(baseList.getLastMod()));
					
					Calendar dateSince = listState.getDateForWSRefresh();
					logger.info("fetching changes since " + Util.formatDate(dateSince));
					if (collator.equals(baseList.getType(),SiteDataWS.DOC_LIB)) {
						listItems = listsWS.getDocLibListItemChanges(
								baseList, dateSince);
					} else if (collator.equals(baseList.getType(),SiteDataWS.GENERIC_LIST) 
							|| collator.equals(baseList.getType(),SiteDataWS.ISSUE)) {
						listItems = listsWS.getGenericListItemChanges(
								baseList, dateSince);
					} 
				}
				if (collator.equals(baseList.getType(),SiteDataWS.GENERIC_LIST) 
						|| collator.equals(baseList.getType(),SiteDataWS.ISSUE)) {
					List attachmentItems = new ArrayList();
					if(listItems!=null){
						for (int j = 0; j < listItems.size(); j++) {           
							SPDocument doc = (SPDocument) listItems.get(j);            
							List attachments = listsWS.getAttachments(baseList, doc);
							attachmentItems.addAll(attachments);
						}
						listItems.addAll(attachmentItems);
					}//null check for list items
				} 

				listState.setCrawlQueue(listItems);
				if(listItems==null){
					System.out.println("listitems null");
				}
				logger.info("found " + listItems.size() + " items to crawl in "
						+ listState.getUrl());               
			}
		} /*catch (SharepointException e) {
			logger.error(e.toString());
		} catch (RepositoryException e) {
			logger.error(e.toString());
		}*/catch (Exception e) {
			logger.error("updateGlobalStateFromSite: "+e.toString());
		}
	}
}
