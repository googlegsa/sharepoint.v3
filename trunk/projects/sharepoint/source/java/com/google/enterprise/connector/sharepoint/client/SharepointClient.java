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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
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

		SPDocumentList docList = new SPDocumentList(newlist,state);
		if(sharepointClientContext!=null){
			//for aliasing
			docList.setAliasPort(sharepointClientContext.getAliasPort());
			docList.setAliasHostName(sharepointClientContext.getAliasHostName());

			// FQDN Conversion flag
			docList.setFQDNConversion(sharepointClientContext.isFQDNConversion());
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
	public SPDocumentList traverse(GlobalState globalstate, int sizeHint) {
		final String sFunctionName="traverse(GlobalState globalstate, int sizeHint)";
		LOGGER.entering(className, sFunctionName);
		
		if(globalstate==null){
			LOGGER.warning(className+":"+sFunctionName+": global state is null");
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
	 * @param state
	 */
	public void updateGlobalState(GlobalState state) {
		final String sFunctionName= "updateGlobalState(GlobalState state)";
		LOGGER.entering(className, sFunctionName);
		
//		LOGGER.config("updateGlobalState");
		if(state==null){
			LOGGER.warning(className+":"+sFunctionName+": global state does not exist");
			return;
		}
		/*if(sharepointClientContextOriginal == null){
			sharepointClientContextOriginal = (SharepointClientContext) sharepointClientContext.clone();
		}*/

		if(sharepointClientContext==null){
			LOGGER.warning(className+":"+sFunctionName+": sharepointClientContext is not found");
			return;
		}
		
		try {
			/////////////////////STEPS ////////////////
			//1. START RECRAWL FOR GARBAGE COLLECTION
			//2. INITIALIZE SITEDATAWS TO GET THE WEBS FOR THE PARENT SITE
			//3. GET ALL THE LINKS FOR PARENT SITE
			//4. GET ALL THE PERSONAL SITES
			//5. GET MY LINKS
			//6. GET ALL LINKED SITES 
			//7. LOOP: CHECK IF THE LINKS CONTAINS VALID SHAREPOINT SITES NOT TRAVERSED YET (APPLY LOOKUP AND UPDATE LOOKUP)
			//8. END RECRAWL
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
				
				//get the link and crawl
				String strCurrentSite = (String) allLinks.first();
				sharepointClientContext.setURL(strCurrentSite);
				allLinks.remove(strCurrentSite);

				SiteDataWS siteDataWS = new SiteDataWS(sharepointClientContext);
				
				//Get all the children sites for the given site
				//List allSites = siteDataWS.getAllChildrenSites();
				
				List allSites =new ArrayList();

				//add the top level web also in the collection
				allSites.add(strCurrentSite);

				
				WebsWS websWS = new WebsWS(sharepointClientContext);
				try{
					allSites.addAll(websWS.getAllChildrenSites());
				}catch(Throwable th){
					LOGGER.warning("Problems while fetching the children webs for site["+strCurrentSite+"], Actual Exception\n"+th.toString());
				}

				for (int i = 0; i < allSites.size(); i++) {
					///////////////WEBS ARE NOT FETCHED AND PROCESSED AS DOCUMENTS AS THEY ARE NOT FED TO THE APPLINACE/////////
					/*SPDocument doc = (SPDocument) allSites.get(i);
					if(doc==null){
						LOGGER.warning(className+":"+sFunctionName+": doc["+i+"] not found");
						continue;
					}
					String strDocURL =doc.getUrl();*/
					///////////////////////////////
					String strDocURL = (String) allSites.get(i);

					//Note: Lookup table maintains keeps track of the links which has been visited till now. 
					//		This helps to curb the cyclic link problem in which SiteA can have link to SiteB
					//      also SiteB having link to SiteA.
					
					//check lookup table
					if(lstLookupForWebs.contains(strDocURL)) {
						continue;
					} else {
						lstLookupForWebs.add(strDocURL);
					}
					updateGlobalStateFromSite(state, strDocURL);

					/////////for handling links for each site////////////
					//--collect all links first ...filter it to avoid duplicates
					siteDataWS = new SiteDataWS(sharepointClientContext, strDocURL);
					links = siteDataWS.getAllLinks(sharepointClientContext,strDocURL);
					if(links!=null){
						allLinks.addAll(links);
					}
					///////////////////
				}

				//---------------------FOR PERSONAL SITES-----------------------------
				//--CHECK IF WSS OR MOSS(This is required to confirm if we need to fetch the MySite or not)
				//--Note:WSS saites do not contain MySites

				//condition: SP2003 or SP2007
				//SP2003 does not require mysite base URL
				//There is a difference in the implementation for getting Mysite and Mylinks in sp2003 
				//and sp2007 as they have different stubs

				//get the version of sharepoint
				String strSharepointType = sharepointClientContext.getSharePointType();
				
				if(null==strSharepointType){
					LOGGER.warning(className+":"+sFunctionName+": Unable to get the SharePoint type SP2003/SP2007");
					throw new SharepointException("Unable to get the SharePoint type SP2003/SP2007");
				}else if(strSharepointType.equals(SharepointConnectorType.SP2003)){
					com.google.enterprise.connector.sharepoint.client.sp2003.UserProfileWS userProfileWS = new com.google.enterprise.connector.sharepoint.client.sp2003.UserProfileWS(sharepointClientContext);

					//check is SPS 2003 or WSS 2.0
					if(userProfileWS.isSPS()){
						//Get the list of my sites/personal sites  
						List personalSites = userProfileWS.getPersonalSiteList();
						
						//Get of the webs and sub-webs for each my site found
						List lstWebsOfOneSite;
						for(int iList=0;iList<personalSites.size();++iList){
							String strURL = (String) personalSites.get(iList);

							sharepointClientContext.setURL(strURL); //the SPClinet context is changed as personal site is a new site
							siteDataWS = new SiteDataWS(sharepointClientContext);
							websWS = new WebsWS(sharepointClientContext);
							
							//get all the children sites (for the personal site)
//							lstWebsOfOneSite = siteDataWS.getAllChildrenSites();
							lstWebsOfOneSite = websWS.getAllChildrenSites();
							lstWebsOfOneSite.add(strURL);
							
							for (int i = 0; i < lstWebsOfOneSite.size(); i++) {
								
								/*
								SPDocument doc = (SPDocument) lstWebsOfOneSite.get(i);
								String strWebURL = doc.getUrl();*/
								///////////////////////////////
								String strWebURL = (String) lstWebsOfOneSite.get(i);
								//maintain lookup for the sites discoved so far.. to avoid cyclic redudndancy 
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
				}else if(strSharepointType.equals(SharepointConnectorType.SP2007)){
					String strMySiteURL = sharepointClientContext.getMySiteBaseURL(); //--GET THE MYSITE URL
					UserProfileWS userProfileWS = new UserProfileWS(sharepointClientContext);

					if(((strMySiteURL!=null)&&(!strMySiteURL.trim().equals(""))&& userProfileWS.isSPS() && !lstLookupForWebs.contains(strMySiteURL))){

						lstLookupForWebs.add(strMySiteURL);//add the base mysite URL in the traversed list 

						//get All mylinks
						List lstMyLinks = userProfileWS.getMyLinks();
						allLinks.addAll(lstMyLinks);

						List personalSites = userProfileWS.getPersonalSiteList();
						List lstWebsOfOneSite;
						for(int iList=0;iList<personalSites.size();++iList){
							String strURL = (String) personalSites.get(iList);

							sharepointClientContext.setURL(strURL); //the SPClinet context is changed as personal site is a new site
							siteDataWS = new SiteDataWS(sharepointClientContext);
							websWS = new WebsWS(sharepointClientContext);
							
							lstWebsOfOneSite = websWS.getAllChildrenSites();
							lstWebsOfOneSite.add(strURL);
//							lstWebsOfOneSite = siteDataWS.getAllChildrenSites();

							for (int i = 0; i < lstWebsOfOneSite.size(); i++) {
								///////////////WEBS ARE NOT FETCHED AND PROCESSED AS DOCUMENTS AS THEY ARE NOT FED TO THE APPLINACE/////////
								/*
								SPDocument doc = (SPDocument) lstWebsOfOneSite.get(i);
								String strWebURL = doc.getUrl();*/
								///////////////////////////////
								String strWebURL = (String) lstWebsOfOneSite.get(i);

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
				}else{
					LOGGER.warning(className+":"+sFunctionName+": Unresolved Connector type");
					throw new SharepointException("Unresolved Connector type");
				}


				//remove all links which fall under lstLookupForWebs
				if(lstLookupForWebs!=null){
					for(int iLookUp =0;iLookUp<lstLookupForWebs.size();++iLookUp){
						allLinks.remove(lstLookupForWebs.get(iLookUp));
					}
				}

			}//end: while(allLinks.size()>0)
			//---------------------END: FOR PERSONAL SITES-----------------------------

		} catch (SharepointException e) {
			LOGGER.severe(className+":"+sFunctionName+":"+e.toString());
		} catch (RepositoryException e) {
			LOGGER.severe(className+":"+sFunctionName+":"+e.toString());
		}catch(Exception e){
			LOGGER.severe(className+":"+sFunctionName+":"+e.toString());
		}
		
		state.endRecrawl();
		LOGGER.exiting(className, sFunctionName);
	}


	//for testing if links are comming correctly
	private void dumpLinks(Set allLinks) {
		final String sFunctionName= "dumpLinks(Set allLinks)";
		LOGGER.entering(className, sFunctionName);
		if(allLinks!=null){
			Iterator it = allLinks.iterator();
			LOGGER.info("----------------------");
			LOGGER.info(className+":"+sFunctionName+": Links form Sharepoint site: ");
			while(it.hasNext()){
				 LOGGER.info((String)it.next());
			}
			LOGGER.info("----------------------");
		}
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
	private void updateGlobalStateFromSite(GlobalState state, String siteName) {
		final String sFunctionName= "updateGlobalStateFromSite(GlobalState state, String siteName)";
		LOGGER.entering(className, sFunctionName);
		LOGGER.info("updateGlobalStateFromSite for " + siteName);
		
		List listItems = new ArrayList();
		//List listFolders= new ArrayList();
		Collator collator = SharepointConnectorType.getCollator();
		SiteDataWS siteDataWS;
		ListsWS listsWS;
		AlertsWS alertsWS;
		
		try {

			if (siteName == null) {
				siteDataWS = new SiteDataWS(sharepointClientContext);
				listsWS = new ListsWS(sharepointClientContext);
				alertsWS = new AlertsWS(sharepointClientContext);//added for alerts
			} else {
				siteDataWS = new SiteDataWS(sharepointClientContext, siteName);
				listsWS = new ListsWS(sharepointClientContext, siteName);
				alertsWS = new AlertsWS(sharepointClientContext, siteName);//added for alerts
			}
			
			List listCollection = siteDataWS.getDocumentLibraries();//e.g. picture,wiki,document libraries etc.
			List listCollectionGenList = siteDataWS.getGenericLists();//e.g. announcement,tasks etc.
			List listCollectionIssues = siteDataWS.getIssues();//e.g. issues
			List listCollectionDiscussionBoards = siteDataWS.getDiscussionBoards();//e.g. discussion board
			List listCollectionSurveys= siteDataWS.getSurveys();//e.g. surveys

			//check for the global state
			if(state==null){
				LOGGER.warning(className+":"+sFunctionName+": Unable to obtain global state");
				throw new SharepointException("Unable to obtain global state");
			}
			////////// Added for getting Alerts ////////////
			//get Alerts for the web site 
			List listCollectionAlerts= alertsWS.getAlerts();
			if((listCollectionAlerts!=null)&&(listCollectionAlerts.size()>0)){
				//create a list state called alerts
				//Note: Alerts can be created at web site level

				//-> ID = siteName_Alerts: to make it unique for alerts
				//-> LastMod: current time
				String internalName = siteName+"_"+AlertsWS.ALERTS_TYPE; 
				Calendar cLastMod = Calendar.getInstance();
				cLastMod.setTime(new Date());

				/*
					System.out.println("AlertsName: "+internalName);
					System.out.println("AlertsTime: "+new Date());
					System.out.println("");
				*/
				
				BaseList baseList = new BaseList(internalName,AlertsWS.ALERTS_TYPE,AlertsWS.ALERTS_TYPE,cLastMod,AlertsWS.ALERTS_TYPE,internalName);
				ListState listState = state.lookupList(baseList.getInternalName());//find the list in the global state
				
				/*
				 * If we already knew about this list, then only fetch docs that have
				 * changed since the last doc we processed. If it's a new list (e.g. the
				 * first Sharepoint traversal), we fetch everything.
				 */
				if (listState == null) {
					listState = state.makeListState(baseList.getInternalName(), baseList.getLastMod());
					listState.setUrl(AlertsWS.ALERTS_TYPE);
				}else{
					LOGGER.info(className+":"+sFunctionName+":revisiting old listState: " + listState.getUrl());
					state.updateList(listState, Util.calendarToJoda(baseList.getLastMod()));
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

			//Note: alerts should not be framed as containers alerts will be sent as contents
			//listCollection.addAll(listCollectionAlerts);//alerts
			//Now we have collection of all the 1st level items (e.g. doclib, lists and issues)
			for (int i = 0; i < listCollection.size(); i++) {
				boolean isNewList = false;
				BaseList baseList = (BaseList) listCollection.get(i);
				//listFolders = new ArrayList(); //reset the folder list
				
				ListState listState = state.lookupList(baseList.getInternalName());//find the list in the global state
				/*
				 * If we already knew about this list, then only fetch docs that have
				 * changed since the last doc we processed. If it's a new list (e.g. the
				 * first Sharepoint traversal), we fetch everything.
				 */
				if (listState == null) {
					isNewList = true; //tell that add the doc lib as document
					listState = state.makeListState(baseList.getInternalName(), baseList.getLastMod());
					listState.setUrl(baseList.getTitle());
					if (collator.equals(baseList.getType(),SiteDataWS.DOC_LIB)) {
						//get all the documents for the list as changes since =null
						listItems = listsWS.getDocLibListItemChanges(
								baseList, null);
						
						////////////for folders /////////////////////
						String doclibFullURL = baseList.getUrl();
						
						if((doclibFullURL!=null)&&(siteName!=null)){
							//String docLibURL = doclibFullURL.replaceFirst(siteName, "");
							StringTokenizer strtok = new StringTokenizer(siteName,"/");
							while((strtok!=null)&&(strtok.hasMoreTokens())){
								String webToken = strtok.nextToken();
								doclibFullURL = doclibFullURL.replaceFirst(webToken, "");
								doclibFullURL = doclibFullURL.replaceFirst("/", "");
							}
							
							strtok = new StringTokenizer(doclibFullURL,"/");
							if((strtok!=null)&&(strtok.hasMoreTokens())){
								doclibFullURL = strtok.nextToken();
							}
//							listFolders = siteDataWS.getAllFolders(siteName,doclibFullURL,null);	
						}
						////////////end:for folders /////////////////////
						
					} else if (collator.equals(baseList.getType(),SiteDataWS.GENERIC_LIST) 
							|| collator.equals(baseList.getType(),SiteDataWS.ISSUE)
							|| collator.equals(baseList.getType(),SiteDataWS.DISCUSSION_BOARD)
							|| collator.equals(baseList.getType(),SiteDataWS.SURVEYS)){
						
						//get all list items
						listItems = listsWS.getGenericListItemChanges(
								baseList, null);
					}
					LOGGER.info(className+":"+sFunctionName+": creating new listState: " + baseList.getTitle());
				} else {
					LOGGER.info(className+":"+sFunctionName+": revisiting old listState: " + listState.getUrl());
					
					Calendar dateSince = listState.getDateForWSRefresh();
					
					state.updateList(listState, Util.calendarToJoda(baseList.getLastMod()));
					LOGGER.info(className+":"+sFunctionName+": fetching changes since " + Util.formatDate(dateSince));
					
					//check if date modified for the document library
					Calendar dateCurrent = baseList.getLastMod();
					if(dateSince.before(dateCurrent)){
//						System.out.println("#######################List is modified @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
						isNewList =true;
					}
					//////////////////////////
					
					if (collator.equals(baseList.getType(),SiteDataWS.DOC_LIB)) {
						listItems = listsWS.getDocLibListItemChanges(
								baseList, dateSince);
						
						//get the folders only when the document library is changed
						if(isNewList==true){
							////////////for folders /////////////////////
							String doclibFullURL = baseList.getUrl();
							
							if((doclibFullURL!=null)&&(siteName!=null)){
								//String docLibURL = doclibFullURL.replaceFirst(siteName, "");
								StringTokenizer strtok = new StringTokenizer(siteName,"/");
								while((strtok!=null)&&(strtok.hasMoreTokens())){
									String webToken = strtok.nextToken();
									doclibFullURL = doclibFullURL.replaceFirst(webToken, "");
									doclibFullURL = doclibFullURL.replaceFirst("/", "");
								}
								
								strtok = new StringTokenizer(doclibFullURL,"/");
								if((strtok!=null)&&(strtok.hasMoreTokens())){
									doclibFullURL = strtok.nextToken();
								}
								
								//listFolders = siteDataWS.getAllFolders(siteName,doclibFullURL,dateSince);	
							}
							////////////end:for folders /////////////////////
						}//end: if(isNewList==true){

						
					} else if (collator.equals(baseList.getType(),SiteDataWS.GENERIC_LIST) 
							|| collator.equals(baseList.getType(),SiteDataWS.ISSUE)
							|| collator.equals(baseList.getType(),SiteDataWS.DISCUSSION_BOARD)
							|| collator.equals(baseList.getType(),SiteDataWS.SURVEYS)) {
						listItems = listsWS.getGenericListItemChanges(
								baseList, dateSince);
						

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

				if(isNewList==true){
					SPDocument listDoc = new SPDocument(baseList.getInternalName(),baseList.getUrl(),baseList.getLastMod(),baseList.getBaseTemplate());
					listDoc.setAllAttributes(baseList.getAttrs());
					listItems.add(listDoc);
					/*//add the folders 
					if(listFolders!=null){
						listItems.addAll(listFolders);
					}*/
				}
				
				
				//Note: alerts are not part of any list e.g. document library 
				//adding alerts as documents
				listState.setCrawlQueue(listItems);
				if(listItems!=null){
					LOGGER.config(className+":"+sFunctionName+"found " + listItems.size() + " items to crawl in "
							+ listState.getUrl()); 
				}

			}
		}catch (Exception e) {
			LOGGER.warning(className+":"+sFunctionName+"Exception: "+e.toString());
		}
		LOGGER.exiting(className, sFunctionName);
	}
}
