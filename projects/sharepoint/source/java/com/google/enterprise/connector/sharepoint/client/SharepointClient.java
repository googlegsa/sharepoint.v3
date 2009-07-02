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
import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * Class which maintains all the methods needed to get documents and sites from
 * the sharepoint server. It is a layer between the connector and the actual web
 * services calls .
 * @author amit_kagrawal
 */
public class SharepointClient {
	private static final Logger LOGGER = Logger.getLogger(SharepointClient.class.getName());
	private SharepointClientContext sharepointClientContext;
	private String className = SharepointClient.class.getName();


	//Added for OOME
	private int nDocuments=0;
	private boolean doCrawl;//This specifies if we have traversed enough #documents to end the traversal
	private boolean doCrawlLists =false;
	private String  strLastCrawledListID = null;//Store the last crawled list form the global state
	private String nextLastListID =null;
	private String nextLastWebID =null;

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
	private SPDocumentList handleCrawlQueueForList(GlobalState globalState,WebState state,ListState list) {
		String sFunctionName ="handleCrawlQueueForList(GlobalState globalState,WebState state,ListState list) {";
		LOGGER.entering(className, sFunctionName);
		if(list==null){
			LOGGER.warning(sFunctionName+": list is not found");
			return null;
		}

		LOGGER.config(sFunctionName+" : handling list:" + list.getUrl());
		List crawlQueue = list.getCrawlQueue();//return the list of documents for the crawl queue

		//set list guid to the documents 
		ArrayList newlist = new ArrayList(); 
		for (Iterator iter = crawlQueue.iterator(); iter.hasNext();) {
			SPDocument doc = (SPDocument) iter.next();
			doc.setListGuid(list.getGuid());
			newlist.add(doc);
			LOGGER.info(sFunctionName+": [ DocId = "+doc.getDocId()+", URL = "+doc.getUrl()+" ]");
		}

//		SPDocumentList docList = new SPDocumentList(newlist,state,globalState);
		SPDocumentList docList = new SPDocumentList(newlist,globalState);
		if(sharepointClientContext!=null){
			docList.setAliasPort(sharepointClientContext.getAliasPort());//for aliasing
			docList.setAliasHostName(sharepointClientContext.getAliasHostName());//for aliasing
			docList.setFQDNConversion(sharepointClientContext.isFQDNConversion());// FQDN Conversion flag
			LOGGER.info(sFunctionName+": [ AliasHost = "+sharepointClientContext.getAliasHostName()+", AliasPort = "+sharepointClientContext.getAliasPort()+", FQDNConversion = "+sharepointClientContext.isFQDNConversion()+" ]");
		}
		LOGGER.exiting(className, sFunctionName);
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
	public SPDocumentList traverse(GlobalState globalState,WebState webState, int sizeHint) {
		final String sFunctionName="traverse(GlobalState globalState,WebState webState, int sizeHint)";
		LOGGER.entering(className, sFunctionName);

		if(webState==null){
			LOGGER.warning(className+":"+sFunctionName+": global state is null");
			return null;
		}

		SPDocumentList resultSet = null;
		int sizeSoFar = 0;
		for (Iterator iter = webState.getCircularIterator();iter.hasNext();) {
			ListState list = (ListState) iter.next();
			webState.setCurrentList(list);
			if (list.getCrawlQueue() == null) {
				continue;
			}

			SPDocumentList resultsList = handleCrawlQueueForList(globalState,webState, list);
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
				LOGGER.info(className+":"+sFunctionName+": Stopping traversal because batch hint " + sizeHint
						+ " has been reached");
				break;
			}
		}
		LOGGER.exiting(className, sFunctionName);
		return resultSet;
	}

	/**
	 * Find all the Lists under the home Sharepoint site, and update the
	 * GlobalState object to represent them.
	 * 
	 * @param globalState
	 */
	public void updateGlobalState(GlobalState globalState) {
		boolean startWeb = false;
		final String sFunctionName= "updateGlobalState(GlobalState state)";
		LOGGER.entering(className, sFunctionName);

		if(globalState==null){
			LOGGER.warning(className+":"+sFunctionName+": global state does not exist");
			return;
		}

		if(sharepointClientContext==null){
			LOGGER.warning(className+":"+sFunctionName+": sharepointClientContext is not found");
			return;
		}

		try {
			/////////////////////STEPS ////////////////
			//1. START RECRAWL FOR GARBAGE COLLECTION (only after full recrawl, Garbage collection is NA for partial recrawl)
			//[PHASE 1]
			//2. GET ALL CHILDREN WEBS (ALL LEVELS) FOR THE TOP URL AND UPDATE GLOBAL STATE
			//3. GET ALL THE LINKS FOR PARENT SITE AND UPDATE GLOBAL STATE
			//4. TRAVERSE THROUGH THE GLOBAL STATE AND PROCESS EACH WEBS ADDED
			//[PHASE 2]
			//5. GET ALL THE PERSONAL SITES
			//6. GET MY LINKS
			//7. GET ALL LINKED SITES FOR EACH PERSONAL WEBS
			//8. DO PROCESS ALL THE WEBS FOR PHASE 2 AND UPDATE GLOBAL STATE
			//9. END RECRAWL
			////////////////////////////////////////////

			TreeSet tsLinks = null;
			ArrayList lstLookupForWebs = new ArrayList();
			TreeSet allWebStateSet = null;

			String strUserURL =  sharepointClientContext.getProtocol()+"://" + sharepointClientContext.getHost() + ":"
			+ sharepointClientContext.getPort() +sharepointClientContext.getsiteName();

			strUserURL = getUrlWithoutDefaultPort(strUserURL);


			globalState.startRecrawl();//start and end recrawl is used for garbage collection...removing the non existant lists
			doCrawlLists =false;//Indicates if you can start with further crawling of documents on or go to next web
			startWeb=false;//Indicates if you can start with further crawling of dcuments or go to next web
			nDocuments=0; //track the documents traversed during each crawl cycle
			doCrawl=true;//traversal is locked if set to false

			strLastCrawledListID = globalState.getLastCrawledListID();//Set the last List Crawled

			//get the link and crawl
//			WebState ws = new WebState(strUserURL,strUserURL);//Create web state for the top URL
			
			
			String strCurrentSite = strUserURL; 
			sharepointClientContext.setURL(strCurrentSite);
			SiteDataWS siteDataWS = new SiteDataWS(sharepointClientContext);
			WebsWS websWS = new WebsWS(sharepointClientContext);
			WebState ws = new WebState(strUserURL,strUserURL,websWS.getTitle(strUserURL));//Create web state for the top URL
			Set allSites  = new TreeSet();//To avoid redundancy
			allSites.add(ws);//add the top level web also in the collection

			try{
				//Get 
				//Get children webs if lastCrawledWebID=null , i.e.
				//1. The Initial case, or
				//2. One complete cycle is over
				String lastCrawledWebID  = globalState.getLastCrawledWebID();//check the last crawled web ID
				if(null==lastCrawledWebID){
					///////////////////process my sites
					String strSharepointType = sharepointClientContext.getSharePointType();//get the version of sharepoint
					if(null==strSharepointType){
						LOGGER.warning(className+":"+sFunctionName+": Unable to get the SharePoint type SP2003/SP2007");
						throw new SharepointException("Unable to get the SharePoint type SP2003/SP2007");
					}else if(strSharepointType.equals(SharepointConnectorType.SP2003)){
						LOGGER.fine(className+":"+sFunctionName+": Getting the initial list of MySites for SharePoint type SP2003"); //added by Nitendra
						
						//Get the initial list of Mysites						
						com.google.enterprise.connector.sharepoint.client.sp2003.UserProfileWS userProfileWS = new com.google.enterprise.connector.sharepoint.client.sp2003.UserProfileWS(sharepointClientContext);

						if(userProfileWS.isSPS()){//Check if SPS2003 or WSS 2.0
							SortedSet personalSites = userProfileWS.getPersonalSiteList();//Get the list of my sites/personal sites
							allSites.addAll(personalSites);
						}
					}else if(strSharepointType.equals(SharepointConnectorType.SP2007)){
						LOGGER.fine(className+":"+sFunctionName+": Getting the list of MySites from MySiteBaseURL for SharePoint type SP2007"); //added by Nitendra
						
						String strMySiteURL = sharepointClientContext.getMySiteBaseURL(); //--GET THE MYSITE URL
						UserProfileWS userProfileWS = new UserProfileWS(sharepointClientContext);

						if(((strMySiteURL!=null)&&(!strMySiteURL.trim().equals(""))&& userProfileWS.isSPS() && !lstLookupForWebs.contains(strMySiteURL))){
							LOGGER.fine("MySite Found:"+strMySiteURL);
							TreeSet lstMyLinks = (TreeSet) userProfileWS.getMyLinks();
							SortedSet personalSites = userProfileWS.getPersonalSiteList();

							allSites.addAll(lstMyLinks);//remove duplicates
							allSites.addAll(personalSites);
						}//if(((strMySiteURL!=null)&&(!strMySiteURL.trim().equals(""))&& userProfileWS.isSPS() && !lstLookupForWebs.contains(strMySiteURL))){						
					}
					////////////////End: process my sites //////////
									
					allWebStateSet = websWS.getDirectChildsites(); //add the children sites
					allSites.addAll(allWebStateSet);

					//get the linked sites for ws (Top level site)
					String webURL = ws.getPrimaryKey();
					siteDataWS = new SiteDataWS(sharepointClientContext, webURL);
					
//					tsLinks = siteDataWS.getAllLinks(sharepointClientContext,webURL);					
					tsLinks = siteDataWS.getAllLinks(sharepointClientContext,webURL,ws.getTitle());
					allSites.addAll(tsLinks);

					//iterate through fresh list of webs ... and update GS (i.e. add WS if not there already)
					if(null!=allSites){
						Iterator itAllSites = allSites.iterator();
						while((itAllSites!=null) && (itAllSites.hasNext())){
							WebState wsTemp = (WebState)itAllSites.next();
							if(null!=wsTemp){
								WebState wsGS = globalState.lookupList(wsTemp.getPrimaryKey());//find the list in the Web state
								if(wsGS==null){//new web
									// Updated to send one more arguement : title. By Nitendra
									globalState.makeWebState(wsTemp.getPrimaryKey(), (Calendar)null, wsTemp.getTitle());//webs do not require last modified date
									LOGGER.info("Making WebState for : "+wsTemp.getPrimaryKey());
								}
							}
						}
					}
					globalState.setLastCrawledWebID(webURL);//update the last web crawled 
				}//end: if(null==lastCrawledWebID){
				//////////////////////////////////////////////////////
			}catch(Throwable th){
				LOGGER.log(Level.WARNING, "Problems while fetching the children webs for site["+strCurrentSite+"], Actual Exception\n"+th.toString(), th);
			}

			////////////////PHASE1: PROCESS ALL THE PRINCIPAL WEBS AND THEIR CHILD WEBS //////////////////
			//All the web states are sorted in inserted date (Applicable only during makewebstate()) 
			//so that the order of traversal is consistent
			allSites = new TreeSet();//empty the list.. to collect intermediate webs and links
			Iterator itWebs = globalState.getIterator();
			if((null!=itWebs)&& (itWebs.hasNext())){
				ws = (WebState) itWebs.next();// Get the first web

				do{
					if(ws==null){
						break;
					}

					String webURL =ws.getPrimaryKey();
					nextLastWebID = webURL;//Keep Track of the webs getting traversed

					//Note: Lookup table maintains keeps track of the links which has been visited till now. 
					//This helps to curb the cyclic link problem in which SiteA can have link to SiteB
					//also SiteB having link to SiteA.
					if(lstLookupForWebs.contains(webURL)) {
						ws = (WebState) itWebs.next();
						continue;
					} else {
						lstLookupForWebs.add(webURL);
					}

					if(startWeb==false){
						String webID = ws.getPrimaryKey();
						String savedWebID = globalState.getLastCrawledWebID();

						if(savedWebID!=null){
							if(webID.equals(savedWebID)){
								startWeb = true; //Now, Start crawling from here
							}else{
								ws = (WebState) itWebs.next();
								continue; //go to start of the loop
							}
						}else{
							startWeb=true;
						}
					}

					LOGGER.info(className+":"+sFunctionName+": Web ["+webURL+"] is getting traversed for documents....");
					updateWebStateFromSite(ws, webURL);//Process the web site

					if(ws.isExisting()){//at this point all the webs will be created on Global state
						globalState.updateList(ws, ws.getLastMod());//update global state with the updated web state
					}

					//Check if the threshhold (i.e. 2*batchHint is reached)
					int batchHint = sharepointClientContext.getBatchHint();
					if(nDocuments>=(2*batchHint)){//end traversal
						doCrawl = false;//i.e. stop crawling .. our limit is reached
						break; //Get out of for loop
					}

					///////Get the next web and discover its direct children and links
					if(itWebs.hasNext()){
						ws = (WebState) itWebs.next();
						if(ws!=null){		
							
							String webURL1 = ws.getPrimaryKey();
							
							websWS=new WebsWS(sharepointClientContext,webURL1); //added by Nitendra
							siteDataWS = new SiteDataWS(sharepointClientContext, webURL1);
							
							//child webs				
							allWebStateSet= websWS.getDirectChildsites(); //add the children sites					
							allSites.addAll(allWebStateSet);

							//links							
							tsLinks = siteDataWS.getAllLinks(sharepointClientContext,webURL1,ws.getTitle());//get the linked sites for ws (Top level site)
							allSites.addAll(tsLinks);
						}
					}else{
						ws = null;
					}

				}while(ws!=null);//while(itWebs.hasNext()){
			}//if(null!=itWebs){


			///////////////////////adding new webs and links to the GS////////////////////////////////
			//iterate throgh fresh list of webs ... and update GS (i.e. add WS if not there already)
			if(null!=allSites){
				Iterator itAllSites = allSites.iterator();
				while((itAllSites!=null) && (itAllSites.hasNext())){
					WebState wsTemp = (WebState)itAllSites.next();
					if(null!=wsTemp){
						WebState wsGS = globalState.lookupList(wsTemp.getPrimaryKey());//find the list in the Web state
						if(wsGS==null){//new web
							globalState.makeWebState(wsTemp.getPrimaryKey(), (Calendar)null, wsTemp.getTitle());//webs do not require last modified date
							LOGGER.info("Making WebState for : "+wsTemp.getPrimaryKey());
						}
					}
				}
			}
			/////////////////////////////////////////////////////////////////////

		} catch (SharepointException e) {
			LOGGER.log(Level.SEVERE,className+":"+sFunctionName,e);
		} catch (RepositoryException e) {
			LOGGER.log(Level.SEVERE,className+":"+sFunctionName,e);
		}catch(Throwable e){
			LOGGER.log(Level.SEVERE,className+":"+sFunctionName,e);
		}



		if(doCrawl && globalState.isBFullReCrawl()){
			globalState.setBFullReCrawl(true);
		}else{
			globalState.setBFullReCrawl(false);
		}
		globalState.endRecrawl();

		///////////////////////////////////////INSTRUCTIONS FOR THE FLAGS //////////////////////////////////////
		//doCrawl = true ; when threshhold is not reached i.e. all webs all lists all documents are done
		//doCrawl = false ; when threshhold is reached i.e. partial cycle 
		/////////////////////////////////////////Amit: end: INSTRUCTIONS FOR THE FLAGS //////////////////////////////////////
		if(doCrawl==true){
			//update the last web and list
			globalState.setLastCrawledWebID(null);
			globalState.setLastCrawledListID(null);

		}else{
			//update the last web and list
			globalState.setLastCrawledWebID(nextLastWebID);
			globalState.setLastCrawledListID(nextLastListID);
		}
		globalState.setBFullReCrawl(doCrawl);//indicate if complete\Partial crawlcycle
		LOGGER.exiting(className, sFunctionName);
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
	private void updateWebStateFromSite(WebState webState, String siteName) {
		final String sFunctionName= "updateGlobalStateFromSite(GlobalState state, String siteName)";
		LOGGER.entering(className, sFunctionName);
		LOGGER.info("updateGlobalStateFromSite for " + siteName);

		List listItems = new ArrayList();
		Collator collator = SharepointConnectorType.getCollator();
		SiteDataWS siteDataWS;
		ListsWS listsWS;
		AlertsWS alertsWS;

		try {
			SharepointClientContext tempCtx = (SharepointClientContext) sharepointClientContext.clone();
			if(null!=siteName){
				tempCtx.setURL(siteName);
			}
			siteDataWS = new SiteDataWS(tempCtx);
			listsWS = new ListsWS(tempCtx);
			alertsWS = new AlertsWS(tempCtx);//added for alerts

			List listCollection = siteDataWS.getDocumentLibraries(webState.getTitle());//e.g. picture,wiki,document libraries etc.
			List listCollectionGenList = siteDataWS.getGenericLists(webState.getTitle());//e.g. announcement,tasks etc.
			List listCollectionIssues = siteDataWS.getIssues(webState.getTitle());//e.g. issues
			List listCollectionDiscussionBoards = siteDataWS.getDiscussionBoards(webState.getTitle());//e.g. discussion board
			List listCollectionSurveys= siteDataWS.getSurveys(webState.getTitle());//e.g. surveys

			TreeSet tempset =null;

			//check for the global state
			if(webState==null){
				LOGGER.warning(className+":"+sFunctionName+": Unable to obtain web state");
				throw new SharepointException("Unable to obtain web state");
			}
			////////// Added for getting Alerts ////////////
			//get Alerts for the web site 
			List listCollectionAlerts= alertsWS.getAlerts(webState.getTitle());
			if((listCollectionAlerts!=null)&&(listCollectionAlerts.size()>0)){

				//create a list state called alerts
				//Note: Alerts can be created at web site level

				//-> ID = siteName_Alerts: to make it unique for alerts
				//-> LastMod: current time
				String internalName = siteName+"_"+AlertsWS.ALERTS_TYPE; 
				Calendar cLastMod = Calendar.getInstance();
				cLastMod.setTime(new Date());

				BaseList baseList = new BaseList(internalName,AlertsWS.ALERTS_TYPE,AlertsWS.ALERTS_TYPE,cLastMod,AlertsWS.ALERTS_TYPE,internalName,webState.getTitle());
				ListState listState = webState.lookupList(baseList.getInternalName());//find the list in the Web state

				/*
				 * If we already knew about this list, then only fetch docs that have
				 * changed since the last doc we processed. If it's a new list (e.g. the
				 * first Sharepoint traversal), we fetch everything.
				 */
				if (listState == null) {
					listState = webState.makeListState(baseList.getInternalName(), baseList.getLastMod());
					listState.setUrl(AlertsWS.ALERTS_TYPE);
				}else{
					LOGGER.info(className+":"+sFunctionName+":revisiting old listState: " + listState.getUrl());
					webState.updateList(listState, Util.calendarToJoda(baseList.getLastMod()));
				}
				listState.setCrawlQueue(listCollectionAlerts);//listCollectionAlerts: is the actual list of alerts	
				if(listItems!=null){
					LOGGER.config(className+":"+sFunctionName+": found " + listItems.size() + " items to crawl in "+ listState.getUrl()); 
				}
			}
			///////////end: Added for getting Alerts ////////////

			listCollection.addAll(listCollectionGenList);
			listCollection.addAll(listCollectionIssues);
			listCollection.addAll(listCollectionDiscussionBoards);//added by amit
			listCollection.addAll(listCollectionSurveys);//added by amit

			tempset = new TreeSet(listCollection);
			listCollection = new ArrayList(tempset);

			////////////////////////////////////////////CASE OF DELETED LAST  LIST ////////////////////////
			if(!listCollection.contains(strLastCrawledListID)){
				doCrawlLists=true;
			}
			//END: //////////////////////////////////////////CASE OF DELETED LAST  LIST ////////////////////////

			//Note: alerts should not be framed as containers alerts will be sent as contents
			//listCollection.addAll(listCollectionAlerts);//alerts
			//Now we have collection of all the 1st level items (e.g. doclib, lists and issues)
			for (int i = 0; i < listCollection.size(); i++) {
				boolean isNewList = false;
				BaseList baseList = (BaseList) listCollection.get(i);
				ListState listState = webState.lookupList(baseList.getInternalName());//find the list in the global state
				nextLastListID = baseList.getInternalName();
				////////////////// Need to go till the last list traversed/////////////////////
				if(doCrawlLists==false){
					//Get the LastCrawledList from the globalstate
					if(null==strLastCrawledListID){
						doCrawlLists = true;
					}else if(strLastCrawledListID.equals(baseList.getInternalName())){
						doCrawlLists = true;
						continue;
					}else{
						continue;// Check the next list from the for loop
					}
				}
				/////////////////////////////////////////////////////////////////////////////

				/*
				 * If we already knew about this list, then only fetch docs that have
				 * changed since the last doc we processed. If it's a new list (e.g. the
				 * first Sharepoint traversal), we fetch everything.
				 */

				if (listState == null) {
					isNewList = true; //tell that add the doc lib as document
					listState = webState.makeListState(baseList.getInternalName(), baseList.getLastMod());
					listState.setUrl(baseList.getTitle());

					if (collator.equals(baseList.getType(),SiteDataWS.DOC_LIB)) {
						//get all the documents for the list as changes since =null
						//listItems = listsWS.getDocLibListItemChanges(baseList, null);
						listItems = listsWS.getDocLibListItems(baseList,null,null);//Call Get List Item Initially
					} else if (collator.equals(baseList.getType(),SiteDataWS.GENERIC_LIST) 
							|| collator.equals(baseList.getType(),SiteDataWS.ISSUE)
							|| collator.equals(baseList.getType(),SiteDataWS.DISCUSSION_BOARD)
							|| collator.equals(baseList.getType(),SiteDataWS.SURVEYS)){

						//get all list items
//						listItems = listsWS.getGenericListItemChanges(baseList, null);
						//listItems = listsWS.getGenericListItemChanges(baseList, "");
						listItems = listsWS.getGenericListItems(baseList,null,null);
					}
					LOGGER.info(className+":"+sFunctionName+": creating new listState: " + baseList.getTitle());
				} else {
					LOGGER.info(className+":"+sFunctionName+": revisiting old listState: " + listState.getUrl());

					String lastDocID =null;
					SPDocument lastCrawledDocument = listState.getLastDocCrawled();
					if(null!=lastCrawledDocument){
						String docid = lastCrawledDocument.getDocId();
						if((docid!=null)&&(!docid.trim().equals(""))){
							LOGGER.config("Last Crawled Document: "+docid);
							lastDocID = docid;
						}
					}

					Calendar dateSince = listState.getDateForWSRefresh();

					webState.updateList(listState, Util.calendarToJoda(baseList.getLastMod()));
					LOGGER.info(className+":"+sFunctionName+": fetching changes since " + Util.formatDate(dateSince));

					//check if date modified for the document library
					Calendar dateCurrent = baseList.getLastMod();
					if(dateSince.before(dateCurrent)){
						isNewList =true;
					}

					//////////////////////////

					if (collator.equals(baseList.getType(),SiteDataWS.DOC_LIB)) {
						//listItems = listsWS.getDocLibListItemChanges(baseList, dateSince);
						listItems = listsWS.getDocLibListItems(baseList,dateSince,lastDocID);//Call Get List Item Initially
					} else if (collator.equals(baseList.getType(),SiteDataWS.GENERIC_LIST) 
							|| collator.equals(baseList.getType(),SiteDataWS.ISSUE)
							|| collator.equals(baseList.getType(),SiteDataWS.DISCUSSION_BOARD)
							|| collator.equals(baseList.getType(),SiteDataWS.SURVEYS)) {
						//listItems = listsWS.getGenericListItemChanges(baseList, dateSince);
						listItems = listsWS.getGenericListItems(baseList,dateSince,lastDocID);


					} 
				}

				//note: discussion board ..added due to sp2003
				if (collator.equals(baseList.getType(),SiteDataWS.GENERIC_LIST) 
						|| collator.equals(baseList.getType(),SiteDataWS.ISSUE)
						|| collator.equals(baseList.getType(),SiteDataWS.DISCUSSION_BOARD)
				/*|| collator.equals(baseList.getType(),SiteDataWS.SURVEYS)*/) {//no surveys with attachments 


					List attachmentItems = new ArrayList();

					//check if list has got attachments
					//e.g. discussion boards can have attachments

					if(listItems!=null){
						for (int j = 0; j < listItems.size(); j++) {           
							SPDocument doc = (SPDocument) listItems.get(j);            
							List attachments = listsWS.getAttachments(baseList, doc);
							attachmentItems.addAll(attachments);
						}
						listItems.addAll(attachmentItems);
					}//null check for list items
				} 

				//if(isNewList==true){
				String nextPage =	listsWS.getNextPage();
				//Logic: append list-> Document only when the whole list is traversed
				if(nextPage==null){
					//also check if the some items present
					if(listItems!=null){
						if((listItems.size()>0)||(isNewList==true)){
							SPDocument listDoc = new SPDocument(baseList.getInternalName(),baseList.getUrl(),baseList.getLastMod(),baseList.getBaseTemplate(),baseList.getParentWebTitle());
							listDoc.setAllAttributes(baseList.getAttrs());
							listItems.add(listDoc);

							//sort the list
							Collections.sort(listItems);
						}
					}

				}


				//Note: alerts are not part of any list e.g. document library 
				//adding alerts as documents
				listState.setCrawlQueue(listItems);
				if(listItems!=null){
					LOGGER.config(className+":"+sFunctionName+"found " + listItems.size() + " items to crawl in "
							+ listState.getUrl()); 
					/*}else{*/
					/////////////////////////////////////check the count for the list Items
					nDocuments+= listItems.size();//Upadte the total documents count
					int batchHint = sharepointClientContext.getBatchHint();
					if(nDocuments>=2*batchHint){//check if the threshhold = 2*batchhint is reached
						break;//come out of for loop
					}
					/////////////////////////////////////////////////////////////////////////
				}

				//Set the nextPage for the List\Library
//				listState.setNextPage(baseList.getNextPageToken());

			}//end:; for Lists
			//}catch (Exception e) {
		}catch (Throwable e) {
			LOGGER.log(Level.WARNING,className+":"+sFunctionName,e);
			//LOGGER.warning(className+":"+sFunctionName+"Exception: "+e.toString());
		}

		doCrawlLists = true;//Amit->Logic:could be possible that the list is deleted in between
		//In that case the comparision will fail till the full crawl cycle is reached and lastList\WebID=null
		//doCrawlLists = true will ensure that further lists are not missed


		LOGGER.exiting(className, sFunctionName);
	}

	/**
	 * @description remove the port from the URL in case using the default port for a given protocol, e.g. http->80,https->243  
	 * @author amit_kagrawal
	 * */
	private String getUrlWithoutDefaultPort(String strUrl) {
		String sFunctionName = "getUrlWithoutDefaultPort(String strSharepointUrl)";
		String strSPURL = null;
		if(strUrl != null){
			try {
				URL url = new URL(strUrl);
				String hostTmp = url.getHost();
				String protocolTmp = url.getProtocol(); //to remove the hard-coded protocol
				int portTmp = -1; 
				if (-1 != url.getPort()) {
					if(url.getPort()!= url.getDefaultPort()){
						portTmp = url.getPort();
					}
				}
				String siteNameTmp = url.getPath();
				strSPURL =  protocolTmp+"://" + hostTmp;
				if(portTmp != -1){
					strSPURL += ":"+portTmp;
				}
				strSPURL+=siteNameTmp;
				return strSPURL;

			} catch (MalformedURLException e) {
				LOGGER.warning(sFunctionName +": "+e.toString());
			}
		}
		return strUrl;
	}
}
