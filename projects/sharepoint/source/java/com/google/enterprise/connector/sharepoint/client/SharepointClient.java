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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocumentList;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.sharepoint.wsclient.AlertsWS;
import com.google.enterprise.connector.sharepoint.wsclient.GSSiteDiscoveryWS;
import com.google.enterprise.connector.sharepoint.wsclient.ListsWS;
import com.google.enterprise.connector.sharepoint.wsclient.SiteDataWS;
import com.google.enterprise.connector.sharepoint.wsclient.UserProfileWS;
import com.google.enterprise.connector.sharepoint.wsclient.WebsWS;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

/**
 * This class provides a layer of abstraction between the SharePoint Traversal
 * Manager and the java clients for making web service calls. Every time
 * traversal is started/resumed, connector goes through this layer. This class
 * has the inteliigence to know which web service should be consulted for some
 * purpose. This class has all the methods needed to get documents and sites
 * from the sharepoint server.
 *
 * @author nitendra_thakur
 */
public class SharepointClient {
    private final Logger LOGGER = Logger.getLogger(SharepointClient.class.getName());
    private SharepointClientContext sharepointClientContext;
    private int nDocuments = 0;

    // true -> when threshold is not reached and all webs
    // all lists all documents are done.
    // false -> when a partial cycle is completed i.e, threshold is
    // reached before processing all the documents.
    private boolean doCrawl;

    // This is mainly for test cases. It gives the count of liststates that are
    // checked for any docs pending from previous crawl cycle
    private int noOfVisitedListStates = 0;

    public SharepointClient(
            final SharepointClientContext inSharepointClientContext)
            throws SharepointException {
        sharepointClientContext = inSharepointClientContext;
    }

    /**
     * For a single ListState, handle its crawl queue (if any). This means add
     * it to the ResultSet which we give back to the Connector Manager.
     *
     * @param globalState The recent snapshot of the whole in-memory state file.
     * @param web Represets the current web state
     * @param list Represents the current list state
     * @return {@link SPDocumentList} conatining the crawled documents.
     */
    private SPDocumentList handleCrawlQueueForList(
            final GlobalState globalState, final WebState web,
            final ListState list) {
        if (null == web) {
            LOGGER.log(Level.WARNING, "web is not found");
            return null;
        }
        if (null == list) {
            LOGGER.log(Level.WARNING, "list is not found");
            return null;
        }

        final List<SPDocument> crawlQueue = list.getCrawlQueue();
        if (null == crawlQueue || crawlQueue.size() <= 0) {
            LOGGER.log(Level.FINE, "No CrawlQueue..");
            return null;
        }
        final ArrayList<SPDocument> newlist = new ArrayList<SPDocument>();
        for (final Iterator<SPDocument> iter = list.getCrawlQueue().iterator(); iter.hasNext();) {
            final SPDocument doc = (SPDocument) iter.next();
            doc.setParentList(list);
            doc.setParentWeb(web);
            // Update necessary information required for downloading contents.
            if (FeedType.CONTENT_FEED == doc.getFeedType()) {
                doc.setContentDwnldURL(doc.getUrl());
                doc.setSharepointClientContext(sharepointClientContext);
            }
            newlist.add(doc);
            LOGGER.log(Level.FINEST, "[ DocId = " + doc.getDocId() + ", URL = "
                    + doc.getUrl() + " ]");
        }

        final SPDocumentList docList = new SPDocumentList(newlist, globalState);
        if (null != sharepointClientContext) {
            docList.setAliasMap(sharepointClientContext.getAliasMap());
            docList.setFQDNConversion(sharepointClientContext.isFQDNConversion());
        } else {
            LOGGER.log(Level.SEVERE, "sharepointClientContext not found!");
        }
        return docList;
    }

    /**
     * Calls DocsFromDocLibPerSite for all the sites under the current site.
     * It's possible that we're resuming traversal, because of batch hints. In
     * this case, we rely on GlobalState's notion of "current". Each time we
     * visit a List (whether or not it has docs to crawl), we mark it "current."
     * On a subsequent call to traverse(), we start AFTER the current, if there
     * is one. One might wonder why we don't just delete the crawl queue when
     * done. The answer is, we don't consider it "done" until we're notified via
     * the Connector Manager's call to checkpoint(). Until that time, it's
     * possible we'd have to traverse() it again.
     *
     * @return {@link SPDocumentList} containing crawled {@link SPDocument}.
     */
    public SPDocumentList traverse(final GlobalState globalState,
            final WebState webState, int sizeSoFar) {
        if (webState == null) {
            LOGGER.warning("global state is null");
            return null;
        }

        noOfVisitedListStates = 0;

        LOGGER.log(Level.INFO, "Traversing web [ " + webState.getWebUrl()
                + " ] ");
        SPDocumentList resultSet = null;

        for (final Iterator<ListState> iter = webState.getCurrentListstateIterator(); iter.hasNext();) {
            final ListState list = (ListState) iter.next();

            // Mark the this list as current list so that the next traversal
            // request starts from here and already scanned lists are not
            // unnecessarily re-scanned.
            webState.setCurrentList(list);
            if (list.getCrawlQueue() == null) {
                continue;
            }

            SPDocumentList resultsList = null;
            try {
                LOGGER.log(Level.INFO, "Handling crawl queue for list URL [ "
                        + list.getListURL() + " ]. ");
                resultsList = handleCrawlQueueForList(globalState, webState, list);
                noOfVisitedListStates++;
            } catch (final Exception e) {
                LOGGER.log(Level.WARNING, "Problem in handling crawl queue for list URL [ "
                        + list.getListURL() + " ]. ", e);
            }

            if ((resultsList != null) && (resultsList.size() > 0)) {
                LOGGER.log(Level.INFO, resultsList.size()
                        + " documents to be sent from list URL [ "
                        + list.getListURL() + " ]. ");
                if (resultSet == null) {
                    resultSet = resultsList;
                } else {
                    resultSet.addAll(resultsList);
                }
            } else {
                LOGGER.log(Level.INFO, "No documents to be sent from list URL [ "
                        + list.getListURL() + " ]. ");
            }
            if (resultSet != null) {
                sizeSoFar += resultSet.size();
            }

            // we heed the batch hint, but always finish a List before checking:
            if (sizeSoFar >= sharepointClientContext.getBatchHint()) {
                LOGGER.info("Stopping traversal because batch hint "
                        + sharepointClientContext.getBatchHint()
                        + " has been reached");
                break;
            }
        }

        if (LOGGER.isLoggable(Level.CONFIG)) {
            LOGGER.config("No. of listStates scanned from site : "
                    + webState.getWebUrl() + " for current batch traversal : "
                    + noOfVisitedListStates);
        }

        return resultSet;
    }

    /**
     * Discover extra webs viz, MySites, Personal Sites, GSSiteDiscover
     * discovered sites etc and store them into allSites.
     *
     * @param allSites
     * @param spType
     * @throws SharepointException
     */
    private void discoverExtraWebs(final Set<String> allSites,
            final SPType spType) throws SharepointException {
        if (SPType.SP2003 == spType) {
            LOGGER.log(Level.INFO, "Getting the initial list of MySites/Personal sites for SharePoint type SP2003. Context URL [ "
                    + sharepointClientContext.getSiteURL() + " ]");
            final com.google.enterprise.connector.sharepoint.wsclient.sp2003.UserProfileWS userProfileWS = new com.google.enterprise.connector.sharepoint.wsclient.sp2003.UserProfileWS(
                    sharepointClientContext);
            if (userProfileWS.isSPS()) {// Check if SPS2003 or WSS 2.0
                try {
                    final Set<String> personalSites = userProfileWS.getPersonalSiteList();// Get
                    // the
                    // list
                    // of
                    // my
                    // sites/personal
                    // sites
                    allSites.addAll(personalSites);
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "Unable to get MySites for the Context URL [ "
                            + sharepointClientContext.getSiteURL() + " ]", e);
                }
            }
        } else if (SPType.SP2007 == spType) {
            final String strMySiteURL = sharepointClientContext.getMySiteBaseURL(); // --GET
            // THE
            // MYSITE
            // URL
            if ((strMySiteURL != null) && (!strMySiteURL.trim().equals(""))) {
                LOGGER.log(Level.INFO, "Getting the initial list of MySites for SharePoint type SP2007 from MySiteBaseURL [ "
                        + strMySiteURL + " ]");
                final UserProfileWS userProfileWS = new UserProfileWS(
                        sharepointClientContext);
                if (userProfileWS.isSPS()) {
                    try {
                        final Set<String> lstMyLinks = userProfileWS.getMyLinks();
                        allSites.addAll(lstMyLinks);// remove duplicates
                    } catch (final Exception e) {
                        LOGGER.log(Level.WARNING, "Unable to get MySites from MySiteBaseURL [ "
                                + strMySiteURL + " ]", e);
                    }

                    try {
                        final Set<String> personalSites = userProfileWS.getPersonalSiteList();
                        allSites.addAll(personalSites);
                    } catch (final Exception e) {
                        LOGGER.log(Level.WARNING, "Unable to get Personal Sites for Context URL [ "
                                + sharepointClientContext.getSiteURL() + " ]", e);
                    }
                }
            }

            // Get all top level sites from the farm. Supported only in SP2007.
            final GSSiteDiscoveryWS gspSiteDiscoveryWS = new GSSiteDiscoveryWS(
                    sharepointClientContext);
            final Set<String> sitecollection = gspSiteDiscoveryWS.getMatchingSiteCollections();
            allSites.addAll(sitecollection);
        }
    }

    /**
     * iterate through fresh list of webs in allSites and update GS (i.e. add WS
     * if not there already)
     *
     * @param globalState
     * @param allSites
     */
    private boolean updateGlobalState(final GlobalState globalState,
            final Set<String> allSites) {
        boolean isGSUpdated = false;
        if ((null == allSites) || (allSites.size() == 0)) {
            return isGSUpdated;
        }
        final Iterator<String> itAllSites = allSites.iterator();
        while ((itAllSites != null) && (itAllSites.hasNext())) {
            final String url = (String) itAllSites.next();
            final WebState webStatus = updateGlobalState(globalState, url);
            if (null != webStatus) {
                isGSUpdated = true;
            }
        }
        return isGSUpdated;
    }

    /**
     * Check for a web if it exists in the global state. If not, then creates a
     * corresponding web state and adds it into the global state.
     *
     * @param globalState
     * @param url
     * @return {@link WebState}
     */
    private WebState updateGlobalState(final GlobalState globalState,
            final String url) {
        WebState web = null;
        if (null == url) {
            LOGGER.log(Level.WARNING, "url not found!");
            return web;
        }
        String webUrl = url;
        WebState wsGS = globalState.lookupWeb(url, null);

        /*
         * The incoming url might not always be exactly the web URL that is used
         * while creation of web state and is required by Web Services as such.
         * Hence, a second check is required.
         */
        if (null == wsGS) {
            final String webAppURL = Util.getWebApp(url);
            WebsWS websWS = null;
            try {
                sharepointClientContext.setSiteURL(webAppURL);
                websWS = new WebsWS(sharepointClientContext);
            } catch (final Exception e) {
                LOGGER.log(Level.WARNING, "webWS creation failed for URL [ "
                        + url + " ]. ", e);
            }
            if (null != websWS) {
                webUrl = websWS.getWebURLFromPageURL(url);
                if (!url.equals(webUrl)) {
                    wsGS = globalState.lookupWeb(webUrl, null);
                }
            }
        }

        if (null == wsGS) {// new web
            LOGGER.info("Making WebState for : " + webUrl);
            try {
                web = globalState.makeWebState(sharepointClientContext, webUrl);
            } catch (final Exception e) {
                LOGGER.log(Level.WARNING, "Problem while creating web state for url [ "
                        + webUrl + " ]. ", e);
            }
        } else {
            wsGS.setExisting(true);
        }

        return web;
    }

    /**
     * Discovers the child sites, MySites, Personal Sites, Sites discovered by
     * GSSite discovery. State information is updated as and when the webs are
     * discovered. A further call to updateWebStateFromSite is made to discover
     * the lists/libraries and the documenst from each discovered web.
     *
     * @param globalState The recent state information
     */
    public void updateGlobalState(final GlobalState globalState)
            throws SharepointException {
        if (globalState == null) {
            LOGGER.warning("global state does not exist");
            return;
        }

        if (sharepointClientContext == null) {
            LOGGER.warning("sharepointClientContext is not found");
            return;
        }
        SharepointClientContext tempCtx = (SharepointClientContext) sharepointClientContext.clone();

        nDocuments = 0;
        doCrawl = true;

        ListState nextList = globalState.getLastCrawledList();
        WebState nextWeb = globalState.getLastCrawledWeb();

        if (null == nextWeb) {
            nextWeb = globalState.lookupWeb(sharepointClientContext.getSiteURL(), sharepointClientContext);
        } else {
            sharepointClientContext.setSiteURL(nextWeb.getWebUrl());
        }

        // start and end recrawl is used for detecting non-existent webs/lists
        globalState.startRecrawl();

        if (null == nextWeb) {
            nextWeb = updateGlobalState(globalState, sharepointClientContext.getSiteURL());
        }

        if (LOGGER.isLoggable(Level.CONFIG)) {
            LOGGER.config("Starting crawl cycle. initiating from the web [ "
                    + nextWeb + " ]. ");
        }

        SPType spType = null;
        if (null != nextWeb) {
            spType = nextWeb.getSharePointType();
        } else {
            throw new SharepointException(
                    "Starting WebState for the current traversal can not be ddetermined.");
        }

        // To store the intermediate webs discovered during crawl
        Set<String> allSites = new TreeSet<String>();

        ArrayList<String> lstLookupForWebs = new ArrayList<String>();

        // Traverse sites and lists from the last crawled site and list to fetch
        // batch hint # of docs
        nextWeb = traverseSites(globalState, allSites, tempCtx, nextWeb, nextList, lstLookupForWebs);

        // Update all the web info into the globalstate. Try and discover some
        // intermediate webs e.g, linked sites. These will be processed in the
        // same batch traversal in case the batch hint # of documents have not
        // been discovered
        boolean isGSupdated = updateGlobalState(globalState, allSites);

        // Cases being handled here:
        // 1. Batch hint # of documents have not been discovered, but there are
        // new sites which have been discovered. Crawl documents till you get
        // the batch hint # of docs
        // 2. Batch hint # of documents have not been discovered and no new
        // sites have been discovered. In such cases get any new
        // personal/mysites, sites discoevered by GSS. Add them to the global
        // state and crawl them till batch hint # of documents is reached.
        if (doCrawl && spType != null) {
            // If the first check has passed, it might mean Case 1. If the
            // following if block is skipped, it means this is Case 1, else it
            // will be Case 2
            if (!isGSupdated) {
                // If this check passed, it means Case 2
                if (LOGGER.isLoggable(Level.CONFIG)) {
                    LOGGER.log(Level.CONFIG, "Discovering new sites");
                }

                // Empty the current set of sites that have been traversed
                // before discovering the new ones. This is important in case
                // the current batch traversal has not discovered batch-hint no.
                // of docs. In such cases the connector should not traverse the
                // sites already traversed in the same batch traversal.
                allSites.clear();

                // Initiate the discovery of new sites
                discoverExtraWebs(allSites, spType);
                isGSupdated = updateGlobalState(globalState, allSites);
            }

            // The following does not care if the sites are discovered for Case
            // 1 or Case 2. It will simply go ahead and crawl batch hint no. of
            // docs from the new sites
            if (isGSupdated) {
                LOGGER.log(Level.INFO, "global state has been updated with newly discovered sites. About to traverse them for docs");
                // Traverse sites and lists under them to fetch batch hint # of
                // docs
                traverseSites(globalState, allSites, tempCtx, nextWeb, nextList, lstLookupForWebs);

                // There are chances that new sites are discovered (child sites
                // OR linked sites) during the traversal of sites discovered as
                // linked sites themselves OR as child sites OR through GSS. In
                // such cases, the connector should just create webstates and
                // add them to the global state. The next batch traversal will
                // take them up for traversal
                isGSupdated = updateGlobalState(globalState, allSites);
                if (isGSupdated) {
                    doCrawl = false;
                }

            }
        }

        globalState.setBFullReCrawl(doCrawl); // indicate if complete\Partial
        // crawlcycle
        globalState.endRecrawl(sharepointClientContext);

        LOGGER.log(Level.INFO, "Returning after crawl cycle.. ");
    }

    public boolean isDoCrawl() {
        return doCrawl;
    }

    /**
     * Makes a call to WSClient layer to get the alerts for a site and updates
     * the global state. Alerts, in SharePoint are created at web level. Though,
     * in the state file that connector maintains a SPDoc can only be inside a
     * ListState. Hence, we need to create a dummy list here. ListID =
     * siteName_Alerts: to make it unique for alerts and LastMod: current time
     *
     * @param webState
     * @param tempCtx
     */
    private void processAlerts(final WebState webState,
            final SharepointClientContext tempCtx) {
        if (null == webState) {
            return;
        }
        String internalName = webState.getPrimaryKey();
        if (!internalName.endsWith("/")) {
            internalName += "/";
        }
        internalName += "_" + SPConstants.ALERTS_TYPE;

        final Calendar cLastMod = Calendar.getInstance();
        cLastMod.setTime(new Date());
        ListState currentDummyAlertList = null;

        try {
            currentDummyAlertList = new ListState(internalName,
                    SPConstants.ALERTS_TYPE, SPConstants.ALERTS_TYPE, cLastMod,
                    SPConstants.ALERTS_TYPE, internalName, webState);
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Unable to create the dummy list state for alerts. ", e);
            return;
        }
        if (currentDummyAlertList == null) {
            LOGGER.log(Level.WARNING, "Unable to create the dummy list state for alerts.");
            return;
        }

        // find the list in the Web state
        ListState dummyAlertListState = webState.lookupList(currentDummyAlertList.getPrimaryKey());
        if (dummyAlertListState == null) {
            dummyAlertListState = currentDummyAlertList;
        }
        LOGGER.log(Level.INFO, "Getting alerts. internalName [ " + internalName
                + " ] ");
        List<SPDocument> listCollectionAlerts = null;

        try {
            final AlertsWS alertsWS = new AlertsWS(tempCtx);
            listCollectionAlerts = alertsWS.getAlerts(webState, dummyAlertListState);
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Problem while getting alerts. ", e);
        }
        if (dummyAlertListState.isExisting()) {
            webState.AddOrUpdateListStateInWebState(dummyAlertListState, currentDummyAlertList.getLastMod());
            dummyAlertListState.setCrawlQueue(listCollectionAlerts);
        }
    }

    /**
     * Gets all the docs from the SPDocument Library and all the items and their
     * attachments from Generic Lists and Issues in sharepoint under a given
     * site. It first calls SiteData web service to get all the Lists. And then
     * calls Lists web service to get the list items for the lists which are of
     * the type SPDocument Library, Generic Lists or Issues. For attachments in
     * Generic List items and Issues, it calls Lists web service to get
     * attachments for these list items.
     *
     * @param tempCtx Current connector context
     * @param webState The state information of the web which is ti be crawled
     *            for documents
     * @param nextLastListID Last List traversed. If the current web contains
     *            this list, the traversal will start from here.
     * @param allWebs Contains all the webs that has been doscovered from link
     *            sites/Site directory.
     */
    private void updateWebStateFromSite(final SharepointClientContext tempCtx,
            final WebState webState, ListState nextList,
            final Set<String> allWebs) throws SharepointException {
        List<SPDocument> listItems = new ArrayList<SPDocument>();

        // get all the lists for the given web // e.g. picture,wiki,document
        // libraries etc.
        final SiteDataWS siteDataWS = new SiteDataWS(tempCtx);
        List<ListState> listCollection = siteDataWS.getNamedLists(webState);

        // Remove duplicate lists, if any.
        listCollection = new ArrayList<ListState>(new TreeSet<ListState>(
                listCollection));

        /*
         * If the nextList belongs the current web and is still existing on the
         * SharePoint site, start traversing for this list onwards.
         */
        if (null != nextList && nextList.getParentWebState().equals(webState)
                && listCollection.contains(nextList)) {
            Collections.rotate(listCollection, -(listCollection.indexOf(nextList)));
        }

        final ListsWS listsWS = new ListsWS(tempCtx);
        for (int i = 0; i < listCollection.size(); i++) {
            final ListState currentList = listCollection.get(i);
            ListState listState = webState.lookupList(currentList.getPrimaryKey());

            /*
             * If we already knew about this list, then only fetch docs that
             * have changed since the last doc we processed. If it's a new list
             * (e.g. the first Sharepoint traversal), we fetch everything.
             */
            if (listState == null) {
                listState = currentList;
                listState.setNewList(true);
                webState.AddOrUpdateListStateInWebState(listState, listState.getLastMod());
                LOGGER.info("discovered new listState. List URL: "
                        + listState.getListURL());

                if (SPType.SP2007 == webState.getSharePointType()) {
                    if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
                        // In case of content feed, we need to keep track of
                        // folders and the items under that. This is reaquired
                        // for sending delete feeds for the documents when their
                        // parent folder is deleted.
                        LOGGER.log(Level.INFO, "Discovering all the folders in the current list/library [ "
                                + listState.getListURL() + " ] ");
                        try {
                            listsWS.getFolderHierarchy(listState, null, null);
                        } catch (final Exception e) {
                            LOGGER.log(Level.WARNING, "Exception occured while getting the folders hierarchy for list [ "
                                    + listState.getListURL() + " ]. ", e);
                        } catch (final Throwable t) {
                            LOGGER.log(Level.WARNING, "Error occured while getting the folders hierarchy for list [ "
                                    + listState.getListURL() + " ]. ", t);
                        }
                    }
                    try {
                        listItems = listsWS.getListItemChangesSinceToken(listState, null, allWebs, null);
                    } catch (final Exception e) {
                        LOGGER.log(Level.WARNING, "Exception thrown while getting the documents under list [ "
                                + listState.getListURL() + " ].", e);
                    } catch (final Throwable t) {
                        LOGGER.log(Level.WARNING, "Error thrown while getting the documents under list [ "
                                + listState.getListURL() + " ].", t);
                    }
                } else {
                    try {
                        listItems = listsWS.getListItems(listState, null, null, allWebs);
                    } catch (final Exception e) {
                        LOGGER.log(Level.WARNING, "Exception thrown while getting the documents under list [ "
                                + listState.getListURL() + " ].", e);
                    }
                }
            } else {
                LOGGER.info("revisiting old listState [ "
                        + listState.getListURL() + " ]. ");
                listState.setExisting(true);
                listState.setNextPage(null);

                String lastDocID = null;
                String lastDocFolderLevel = null;

                SPDocument lastDoc = listState.getLastDocForWSRefresh();

                /*
                 * We must ensure that the last doc that we are using was
                 * actually sent as ADD feed and not as DELETE feed. It might be
                 * possible that in one cycle we identify a list as non-existing
                 * and hence started sending delete feeds for it. But, in the
                 * next cycle that list has been restored, in that case we can
                 * not rely on the lastDoc which has been set by a delete feed.
                 * We also need to reset the change token in that case to start
                 * a full crawl.
                 */
                if (lastDoc != null) {
                    if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()
                            && ActionType.DELETE.equals(lastDoc.getAction())) {
                        listState.saveNextChangeTokenForWSCall(null);
                        listState.commitChangeTokenForWSCall();
                        listState.setLastDocProcessedForWS(null);
                        listState.setCrawlQueue(null);
                        if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
                            // In case of content feed, we need to keep track of
                            // folders and the items under that. This is
                            // required for sending delete feeds for the
                            // documents when their parent folder is deleted.
                            LOGGER.log(Level.INFO, "Discovering all the folders in the current list/library [ "
                                    + listState.getListURL() + " ] ");
                            try {
                                listsWS.getFolderHierarchy(listState, null, null);
                            } catch (final Exception e) {
                                LOGGER.log(Level.WARNING, "Exception occured while getting the folders hierarchy for list [ "
                                        + listState.getListURL() + " ]. ", e);
                            } catch (final Throwable t) {
                                LOGGER.log(Level.WARNING, "Error occured while getting the folders hierarchy for list [ "
                                        + listState.getListURL() + " ]. ", t);
                            }
                        }
                        LOGGER.info("recrawling the items under listState [ "
                                + listState.getListURL()
                                + " ] because this list has been restored after deletion.");
                    } else {
                        lastDocID = Util.getOriginalDocId(lastDoc.getDocId(), sharepointClientContext.getFeedType());
                    }
                }

                if (SPType.SP2007.equals(webState.getSharePointType())) {
                    try {
                        lastDoc = listState.getLastDocForWSRefresh();
                        if (lastDoc != null) {
                            lastDocID = Util.getOriginalDocId(lastDoc.getDocId(), sharepointClientContext.getFeedType());
                            lastDocFolderLevel = lastDoc.getFolderLevel();
                        }
                        listState.updateList(currentList);
                        webState.AddOrUpdateListStateInWebState(listState, currentList.getLastMod());
                        listItems = listsWS.getListItemChangesSinceToken(listState, lastDocID, allWebs, lastDocFolderLevel);
                    } catch (final Exception e) {
                        LOGGER.log(Level.WARNING, "Exception thrown while getting the documents under list [ "
                                + listState.getListURL() + " ].", e);
                    } catch (final Throwable t) {
                        LOGGER.log(Level.WARNING, "Error thrown while getting the documents under list [ "
                                + listState.getListURL() + " ].", t);
                    }
                } else {
                    try {
                        final Calendar dateSince = listState.getDateForWSRefresh();

                        listState.updateList(currentList);
                        webState.AddOrUpdateListStateInWebState(listState, currentList.getLastMod());
                        LOGGER.info("fetching changes since "
                                + Util.formatDate(dateSince) + " for list [ "
                                + listState.getListURL() + " ]. ");

                        // check if date modified for the document library
                        final Calendar dateCurrent = listState.getLastModCal();
                        if (dateSince.before(dateCurrent)) {
                            listState.setNewList(true);
                        }

                        listItems = listsWS.getListItems(listState, dateSince, lastDocID, allWebs);
                    } catch (final Exception e) {
                        LOGGER.log(Level.WARNING, "Exception thrown while getting the documents under list [ "
                                + listState.getListURL() + " ].", e);
                    } catch (final Throwable t) {
                        LOGGER.log(Level.WARNING, "Error thrown while getting the documents under list [ "
                                + listState.getListURL() + " ].", t);
                    }
                }
            }

            // Get the attachments for each discovered items, if the list allows
            // attachments
            if (listState.canContainAttachments() && (listItems != null)) {
                final List<SPDocument> attachmentItems = new ArrayList<SPDocument>();
                for (int j = 0; j < listItems.size(); j++) {
                    final SPDocument doc = listItems.get(j);
                    if (ActionType.ADD.equals(doc.getAction())) {
                        final List<SPDocument> attachments = listsWS.getAttachments(listState, doc);
                        attachmentItems.addAll(attachments);
                    }
                }
                listItems.addAll(attachmentItems);
            }

            final String nextPage = listState.getNextPage();
            // Logic: append list-> Document only when the whole list is
            // traversed
            if (nextPage == null) {
                if (((listItems != null) && (listItems.size() > 0))
                        || (listState.isNewList())) {
                    String docId = listState.getPrimaryKey();
                    if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
                        docId = listState.getListURL() + SPConstants.DOC_TOKEN
                                + docId;
                    }
                    final SPDocument listDoc = new SPDocument(docId,
                            listState.getListURL(), listState.getLastModCal(),
                            SPConstants.NO_AUTHOR, listState.getBaseTemplate(),
                            listState.getParentWebState().getTitle(),
                            sharepointClientContext.getFeedType(),
                            webState.getSharePointType());

                    listDoc.setAllAttributes(listState.getAttrs());

                    if (!listState.isSendListAsDocument()) {
                        // send the listState as a feed only if it was
                        // included
                        // (not excluded) in the URL pattern matching
                        listDoc.setToBeFed(false);
                        LOGGER.log(Level.FINE, "List Document marked as not to be fed");
                    }

                    listItems.add(listDoc);

                    Collections.sort(listItems);

                    /*
                     * The only purpose of list.isNewList to decide whether to
                     * send the list as a document. Since, just now we have done
                     * this, let's mark the list as not new.
                     */
                    listState.setNewList(false);
                }
            } else {
                // If any of the list has not been traversed completely, doCrawl
                // must not be set true.
                doCrawl = false;
            }

            listState.setCrawlQueue(listItems);
            if (listItems != null) {
                LOGGER.log(Level.INFO, "found " + listItems.size()
                        + " items to crawl in " + listState.getListURL());
                nDocuments += listItems.size();
                final int batchHint = sharepointClientContext.getBatchHint();

                // As per Issue 116 we need to stop at batchHint or a little
                // more
                if (nDocuments >= batchHint) {
                    doCrawl = false;
                    break;
                }
            }

            // Set the last crawled date time. This is informative value for the
            // user viewing the state file
            listState.setLastCrawledDateTime(Util.formatDate(Calendar.getInstance(), Util.TIMEFORMAT_WITH_ZONE));

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("Crawled list with ID : " + listState.getListURL()
                        + " for new/modified docs. Total : " + listItems.size());
            }

        }// end:; for Lists

        // Set the last crawled date time. This is informative value for the
        // user viewing the state file
        webState.setLastCrawledDateTime(Util.formatDate(Calendar.getInstance(), Util.TIMEFORMAT_WITH_ZONE));

        // Mark the current list as null so that the next time crawl queues are
        // scanned, all the ListStates are traversed and no documents that have
        // just been discovered gets skipped.
        webState.setCurrentList(null);
    }

    /**
     * Traverses list of sites (webstates) which have not yet been crawled and
     * discovers new docs to be sent to GSA
     *
     * @param globalState The global state which has the list of sites
     *            (webstates) that need to be crawled for documents
     * @param allSites The list of sites
     * @param sharePointClientContext The current connector context. Instance of
     *            {@link SharepointClientContext}
     * @param nextLastWebID The id of the last site (webstate) that was crawled
     * @param nextLastListID The id of the last liststate that as crawled
     * @throws SharepointException In case of any problems fetching documents
     * @return Last Web crawled. This helps caller an idea about from where the
     *         next crawl should begin.
     */
    private WebState traverseSites(GlobalState globalState,
            Set<String> allSites,
            SharepointClientContext sharePointClientContext, WebState nextWeb,
            ListState nextList, ArrayList<String> lstLookupForWebs)
            throws SharepointException {
        globalState.setCurrentWeb(nextWeb);
        final Iterator<WebState> itWebs = (Iterator<WebState>) globalState.getCircularIterator();
        while (itWebs.hasNext()) {
            WebState ws = (WebState) itWebs.next();// Get the first web
            if (ws == null) {
                continue;
            }

            final String webURL = ws.getPrimaryKey();

            // Note: Lookup table maintains keeps track of the links which has
            // been visited till now.
            // This helps to curb the cyclic link problem in which SiteA can
            // have link to SiteB and SiteB having link to SiteA.
            if (lstLookupForWebs.contains(webURL)) {
                continue;
            } else {
                lstLookupForWebs.add(webURL);
            }

            nextWeb = ws;

            try {
                final int currDocCount = nDocuments;
                final String siteName = ws.getPrimaryKey();

                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Web [ " + siteName
                            + " ] is getting crawled for documents....");
                }
                sharePointClientContext.setSiteURL(siteName);

                // Process the web site, and add the link site info to allSites.
                updateWebStateFromSite(sharePointClientContext, ws, nextList, allSites);

                // This call is not required currently as there are no such information getting updated as per the crawl which can affect the ordering of WebStates. For detail, refer to the javadoc of
                // {WebState.AddOrUpdateListStateInWebState}. Also, this is risky to call this method right here because this method may change the collection we are iterating over.
                /* globalState.AddOrUpdateWebStateInGlobalState(ws); */

                if (currDocCount == nDocuments) {
                    // get Alerts for the web and update webState. The above
                    // check is added to reduce the frequency with which
                    // getAlerts WS call is made.
                    LOGGER.info("Web [ " + siteName
                            + " ] is getting crawled for alerts....");
                    processAlerts(ws, sharePointClientContext);
                }
            } catch (final Exception e) {
                LOGGER.log(Level.WARNING, "Following exception occured while traversing/updating web state URL [ "
                        + webURL + " ]. ", e);
            } catch (final Throwable t) {
                LOGGER.log(Level.WARNING, "Following error occured while traversing/updating web state URL [ "
                        + webURL + " ]. ", t);
            }

            // Check if the threshold (i.e. batchHint is reached)
            final int batchHint = sharepointClientContext.getBatchHint();

            // As per Issue 116 we need to stop at batchHint or a little more
            if (nDocuments >= batchHint) {
                LOGGER.info("Stopping crawl cycle as connector has discovered (>= batchHint) # of docs. In total : "
                        + nDocuments + " docs");
                doCrawl = false;
                break;
            }

            // Get the next web and discover its direct children
            sharepointClientContext.setSiteURL(webURL);
            WebsWS websWS = new WebsWS(sharepointClientContext);
            try {
                LOGGER.log(Level.INFO, "Getting child sites for web [ "
                        + webURL + "]. ");
                final Set<String> allWebStateSet = websWS.getDirectChildsites();
                allSites.addAll(allWebStateSet);
            } catch (final Exception e) {
                LOGGER.log(Level.WARNING, "Unable to get the Child sites for site "
                        + webURL, e);
            }
        }
        return nextWeb;
    }

    /**
     * Returns the no of visited list states to check for pending docs from
     * previous batch traversal for a given web state (site)
     *
     * @return The no of visited list states
     */
    public int getNoOfVisitedListStates() {
        return noOfVisitedListStates;
    }
}
