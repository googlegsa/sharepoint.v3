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
    private boolean doCrawl;// true, when threshhold is not reached and all webs

    // all lists all documents are done. false, when a
    // partial cycle is completed i.e, threashold is
    // reached before processing all the documents.

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

        final List crawlQueue = list.getCrawlQueue();// return the list of
        // documents for the
        // crawl queue
        if (null == crawlQueue) {
            LOGGER.log(Level.FINE, "No CrawlQueue..");
            return null;
        }
        final ArrayList<SPDocument> newlist = new ArrayList<SPDocument>();
        for (final Iterator iter = crawlQueue.iterator(); iter.hasNext();) {
            final SPDocument doc = (SPDocument) iter.next();
            doc.setListGuid(list.getPrimaryKey());
            doc.setWebid(web.getPrimaryKey());
            // Update necessary information required for downloading contents.
            if (SPConstants.CONTENT_FEED.equalsIgnoreCase(doc.getFeedType())) {
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
            docList.setFQDNConversion(sharepointClientContext.isFQDNConversion());// FQDN
            // Conversion
            // flag
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
            final WebState webState, final int sizeHint) {
        if (webState == null) {
            LOGGER.warning("global state is null");
            return null;
        }

        LOGGER.log(Level.INFO, "Traversing web [ " + webState.getWebUrl()
                + " ] ");
        SPDocumentList resultSet = null;
        int sizeSoFar = 0;
        for (final Iterator iter = webState.getCircularIterator(); iter.hasNext();) {
            final ListState list = (ListState) iter.next();

            webState.setCurrentList(list);
            if (list.getCrawlQueue() == null) {
                continue;
            }

            SPDocumentList resultsList = null;
            try {
                LOGGER.log(Level.INFO, "Handling crawl queue for list URL [ "
                        + list.getListURL() + " ]. ");
                resultsList = handleCrawlQueueForList(globalState, webState, list);
            } catch (final Exception e) {
                LOGGER.log(Level.WARNING, "Problem in handling crawl queue for list URL [ "
                        + list.getListURL() + " ]. ");
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
                sizeSoFar = resultSet.size();
            }

            // we heed the batch hint, but always finish a List before checking:
            if ((sizeHint > 0) && (sizeSoFar >= sizeHint)) {
                LOGGER.info("Stopping traversal because batch hint " + sizeHint
                        + " has been reached");
                break;
            }
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
            final String spType) throws SharepointException {
        if (SPConstants.SP2003.equalsIgnoreCase(spType)) {
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
        } else if (SPConstants.SP2007.equalsIgnoreCase(spType)) {
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
            final Set allSites) {
        boolean isGSUpdated = false;
        if ((null == allSites) || (allSites.size() == 0)) {
            return isGSUpdated;
        }
        final Iterator itAllSites = allSites.iterator();
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
        WebState wsGS = globalState.lookupWeb(url, null);// find the web in the
        // Web state

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
                    wsGS = globalState.lookupWeb(webUrl, null);// find the web
                    // in the Web
                    // state
                }
            }
        }

        if (null == wsGS) {// new web
            LOGGER.info("Making WebState for : " + webUrl);
            try {
                web = globalState.makeWebState(sharepointClientContext, webUrl);// webs
                // do
                // not
                // require
                // last
                // modified
                // date
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
        final SharepointClientContext tempCtx = (SharepointClientContext) sharepointClientContext.clone();
        final ArrayList<String> lstLookupForWebs = new ArrayList<String>();

        nDocuments = 0;
        doCrawl = true;

        String nextLastListID = globalState.getLastCrawledListID();
        String nextLastWebID = globalState.getLastCrawledWebID();

        WebState ws = null; // Web from where the traversal is to be started.
        if (null == nextLastWebID) {
            nextLastWebID = sharepointClientContext.getSiteURL();
            ws = globalState.lookupWeb(nextLastWebID, sharepointClientContext);
            globalState.setCurrentWeb(null);
        } else {
            ws = globalState.lookupWeb(nextLastWebID, sharepointClientContext);
            globalState.setCurrentWeb(ws);
            sharepointClientContext.setSiteURL(nextLastWebID);
        }

        LOGGER.info("Starting crawl cycle. initiating from the web [ "
                + nextLastWebID + " ]. ");

        WebsWS websWS = new WebsWS(sharepointClientContext);
        globalState.startRecrawl();// start and end recrawl is used for garbage
        // collection...removing the non existant
        // lists

        if (null == ws) {
            ws = updateGlobalState(globalState, nextLastWebID);
            globalState.setCurrentWeb(ws);
        }

        String spType = null;
        if (null != ws) {
            spType = ws.getSharePointType();
        }

        // To store the intermediate webs discovered during crawl
        Set<String> allSites = new TreeSet<String>();

        final Iterator itWebs = globalState.getCircularIterator();
        while (itWebs.hasNext()) {
            ws = (WebState) itWebs.next();// Get the first web
            if (ws == null) {
                continue;
            }

            final String webURL = ws.getPrimaryKey();
            nextLastWebID = webURL;// Keep Track of the webs getting traversed

            // Note: Lookup table maintains keeps track of the links which has
            // been visited till now.
            // This helps to curb the cyclic link problem in which SiteA can
            // have link to SiteB and SiteB having link to SiteA.
            if (lstLookupForWebs.contains(webURL)) {
                continue;
            } else {
                lstLookupForWebs.add(webURL);
            }

            try {
                final int currDocCount = nDocuments;
                final String siteName = ws.getPrimaryKey();
                LOGGER.info("Web [ " + siteName
                        + " ] is getting crawled for documents....");
                tempCtx.setSiteURL(siteName);
                updateWebStateFromSite(tempCtx, ws, nextLastListID, allSites); // Process
                // the
                // web
                // site,
                // and
                // add
                // the
                // link
                // site
                // info
                // to
                // allSites.
                globalState.updateList(ws);// update global state with the
                // updated web state
                if (currDocCount == nDocuments) {
                    // get Alerts for the web and update webState. The above
                    // check is added to reduce the frequency with which
                    // getAlerts WS call is made.
                    LOGGER.info("Web [ " + siteName
                            + " ] is getting crawled for alerts....");
                    processAlerts(ws, tempCtx);
                }
            } catch (final Exception e) {
                LOGGER.log(Level.WARNING, "Following exception occured while traversing/updating web state URL [ "
                        + webURL + " ]. ", e);
            } catch (final Throwable t) {
                LOGGER.log(Level.WARNING, "Following error occured while traversing/updating web state URL [ "
                        + webURL + " ]. ", t);
            }

            // Check if the threshhold (i.e. 2*batchHint is reached)
            final int batchHint = sharepointClientContext.getBatchHint();
            if (nDocuments >= (2 * batchHint)) {
                LOGGER.info("Stopping crawl cycle because batch hint has been reached");
                doCrawl = false;
                break;
            }

            // Get the next web and discover its direct children
            sharepointClientContext.setSiteURL(webURL);
            websWS = new WebsWS(sharepointClientContext);
            try {
                LOGGER.log(Level.INFO, "Getting child sites for web [ "
                        + webURL + "]. ");
                final Set<String> allWebStateSet = websWS.getDirectChildsites();
                allSites.addAll(allWebStateSet);
            } catch (final Exception e) {
                LOGGER.log(Level.WARNING, "Unable to get the Child sites for site "
                        + webURL, e);
            }

            // Set the last crawled date time. This is informative value for the
            // user viewing the state file
            ws.setLastCrawledDateTime(Util.formatDate(Calendar.getInstance(), Util.TIMEFORMAT_WITH_ZONE));

        }

        LOGGER.log(Level.INFO, "All the webs known till last crawl cycle have been crawled");
        if (globalState.isBFullReCrawl() && null != spType) {
            LOGGER.log(Level.INFO, "Discovering Extra webs");
            discoverExtraWebs(allSites, spType);
        }

        // Update all the web info into the globalstate
        final boolean isGSupdated = updateGlobalState(globalState, allSites);
        // If we have discovered some intermediate webs e.g, linked sites,
        // doCrawl must not be set true. This is becasue we'll process these
        // webs in the next crawl cycle.
        if (isGSupdated) {
            LOGGER.log(Level.INFO, "global state has been updated with newly intermediate webs");
            doCrawl = false;
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
        final String internalName = webState.getPrimaryKey() + "_"
                + SPConstants.ALERTS_TYPE;
        final Calendar cLastMod = Calendar.getInstance();
        cLastMod.setTime(new Date());
        ListState currentDummyAlertList = null;

        try {
            currentDummyAlertList = new ListState(internalName,
                    SPConstants.ALERTS_TYPE, SPConstants.ALERTS_TYPE, cLastMod,
                    SPConstants.ALERTS_TYPE, internalName,
                    webState.getPrimaryKey(), webState.getTitle(),
                    webState.getSharePointType(),
                    sharepointClientContext.getFeedType());
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Unable to create the dummy list state for alerts. ", e);
            return;
        }
        if (currentDummyAlertList == null) {
            LOGGER.log(Level.WARNING, "Unable to create the dummy list state for alerts.");
            return;
        }

        ListState dummyAlertListState = webState.lookupList(currentDummyAlertList.getPrimaryKey());// find
        // the
        // list
        // in
        // the
        // Web
        // state
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
            webState.updateList(dummyAlertListState, currentDummyAlertList.getLastMod());
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
            final WebState webState, final String nextLastListID,
            final Set<String> allWebs) throws SharepointException {
        List<SPDocument> listItems = new ArrayList<SPDocument>();

        // get all the lists for the given web
        final SiteDataWS siteDataWS = new SiteDataWS(tempCtx);
        List<ListState> listCollection = siteDataWS.getNamedLists(webState);// e.g.
        // picture,wiki,document
        // libraries
        // etc.

        // Remove duplicate lists, if any.
        listCollection = new ArrayList<ListState>(new TreeSet<ListState>(
                listCollection));

        /*
         * If the current collection of list contains the lastList traversed,
         * start traversing for that list onwards. Create a dummy ListState for
         * comparison. Use the required key , as we just need to check the
         * equality.
         */
        if ((null != nextLastListID) && (nextLastListID.trim().length() != 0)) {
            final ListState tmpList = new ListState(
                    webState.getSharePointType(),
                    sharepointClientContext.getFeedType());
            tmpList.setPrimaryKey(nextLastListID);
            if (listCollection.contains(tmpList)) {
                Collections.rotate(listCollection, -(listCollection.indexOf(tmpList)));
            }
        }

        final ListsWS listsWS = new ListsWS(tempCtx);
        for (int i = 0; i < listCollection.size(); i++) {
            final ListState currentList = listCollection.get(i);
            ListState listState = webState.lookupList(currentList.getPrimaryKey());// find
            // the
            // list
            // in
            // the
            // global
            // state

            /*
             * If we already knew about this list, then only fetch docs that
             * have changed since the last doc we processed. If it's a new list
             * (e.g. the first Sharepoint traversal), we fetch everything.
             */
            if (listState == null) {
                listState = currentList;
                listState.setNewList(true);
                webState.updateList(listState, listState.getLastMod());
                LOGGER.info("discovered new listState. List URL: "
                        + listState.getListURL());

                if (SPConstants.SP2007.equalsIgnoreCase(webState.getSharePointType())) {
                    if (SPConstants.CONTENT_FEED.equalsIgnoreCase(sharepointClientContext.getFeedType())) {
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

                SPDocument lastDoc = listState.getLastDocument();

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
                    if (SPConstants.CONTENT_FEED.equalsIgnoreCase(sharepointClientContext.getFeedType())
                            && ActionType.DELETE.equals(lastDoc.getAction())) {
                        listState.setChangeToken(null);
                        listState.setCachedPrevChangeToken(null);
                        listState.setLastDocument(null);
                        listState.setCrawlQueue(null);
                        if (SPConstants.CONTENT_FEED.equalsIgnoreCase(sharepointClientContext.getFeedType())) {
                            // In case of content feed, we need to keep track of
                            // folders and the items under that. This is
                            // reaquired for sending delete feeds for the
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

                if (SPConstants.SP2007.equalsIgnoreCase(webState.getSharePointType())) {
                    try {
                        lastDoc = listState.getLastDocForWSRefresh();
                        if (lastDoc != null) {
                            lastDocID = Util.getOriginalDocId(lastDoc.getDocId(), sharepointClientContext.getFeedType());
                            lastDocFolderLevel = lastDoc.getFolderLevel();
                        }
                        listState.updateList(currentList);
                        listState.setLastMod(currentList.getLastMod());
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
                        webState.updateList(listState, currentList.getLastMod());
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
                    if (SPConstants.CONTENT_FEED.equalsIgnoreCase(sharepointClientContext.getFeedType())) {
                        docId = listState.getListURL() + SPConstants.DOC_TOKEN
                                + docId;
                    }
                    final SPDocument listDoc = new SPDocument(docId,
                            listState.getListURL(), listState.getLastModCal(),
                            SPConstants.NO_AUTHOR, listState.getBaseTemplate(),
                            listState.getParentWebTitle(),
                            sharepointClientContext.getFeedType(),
                            webState.getSharePointType());

                    listDoc.setAllAttributes(listState.getAttrs());
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
                if (nDocuments >= (2 * batchHint)) {
                    doCrawl = false;
                    break;
                }
            }

            // Set the last crawled date time. This is informative value for the
            // user viewing the state file
            listState.setLastCrawledDateTime(Util.formatDate(Calendar.getInstance(), Util.TIMEFORMAT_WITH_ZONE));

        }// end:; for Lists

    }
}
