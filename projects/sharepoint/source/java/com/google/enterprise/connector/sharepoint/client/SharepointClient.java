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

import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocumentList;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.sharepoint.wsclient.client.AclWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.AlertsWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.ClientFactory;
import com.google.enterprise.connector.sharepoint.wsclient.client.ListsWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDataWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDiscoveryWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfile2003WS;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfile2007WS;
import com.google.enterprise.connector.sharepoint.wsclient.client.WebsWS;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import org.apache.axis.utils.XMLUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

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
  private static final Logger LOGGER = Logger.getLogger(SharepointClient.class.getName());
  private final SharepointClientContext sharepointClientContext;
  private final ClientFactory clientFactory;
  private int nDocuments = 0;

  // true -> when threshold is not reached and all webs
  // all lists all documents are done.
  // false -> when a partial cycle is completed i.e, threshold is
  // reached before processing all the documents.
  private boolean doCrawl;

  // This is mainly for test cases. It gives the count of liststates that are
  // checked for any docs pending from previous crawl cycle
  private int noOfVisitedListStates = 0;

  public SharepointClient(final ClientFactory clientFactory,
      final SharepointClientContext inSharepointClientContext)
      throws SharepointException {
    this.clientFactory = clientFactory;
    sharepointClientContext = inSharepointClientContext;

    // Register a SAX client factory with Axis so that we can intercept SAX
    // parsing failures. This is needed to ignore some SAX parsing failures 
    // such as duplicate attributes defined in the metadata of a document.
    XMLUtils.initSAXFactory(
        "com.google.enterprise.connector.sharepoint.wsclient.handlers.SaxErrorFactory",
        true, false);
  }

  /**
   * For a single ListState, handle its crawl queue (if any). This means add it
   * to the ResultSet which we give back to the Connector Manager.
   *
   * @param globalState The recent snapshot of the whole in-memory state file.
   * @param web Represets the current web state
   * @param list Represents the current list state
   * @return {@link SPDocumentList} conatining the crawled documents.
   */
  private SPDocumentList handleCrawlQueueForList(final GlobalState globalState,
      final WebState web, final ListState list) {
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
      final SPDocument doc = iter.next();
      doc.setParentList(list);
      doc.setParentWeb(web);
      doc.setSharepointClientContext(sharepointClientContext);
      // Update necessary information required for downloading contents.
      if (FeedType.CONTENT_FEED == doc.getFeedType()) {
        doc.setContentDwnldURL(doc.getUrl());
      }

      newlist.add(doc);
      LOGGER.log(Level.FINEST, "[ DocId = " + doc.getDocId() + ", URL = "
          + doc.getUrl() + " ]");
    }

    final SPDocumentList docList = new SPDocumentList(newlist, globalState);
    // FIXME These could be set in traversal manager just before returning
    // start/resumeTraversal
    if (null != sharepointClientContext) {
      // FIXME These could be set in traversal manager just before
      // returning
      // start/resumeTraversal
      docList.setAliasMap(sharepointClientContext.getAliasMap());
      docList.setFQDNConversion(sharepointClientContext.isFQDNConversion());
      docList.setReWriteDisplayUrlUsingAliasMappingRules(sharepointClientContext.isReWriteDisplayUrlUsingAliasMappingRules());
      docList.setReWriteRecordUrlUsingAliasMappingRules(sharepointClientContext.isReWriteRecordUrlUsingAliasMappingRules());
    } else {
      LOGGER.log(Level.SEVERE, "sharepointClientContext not found!");
    }
    return docList;
  }

  /**
   * Scans the crawl queue of all the ListStates from a given WebState and
   * constructs a {@link SPDocumentList} object to be returned to CM.
   * {@link WebState#getCurrentListstateIterator()} takes care of the fact that
   * same list is not scanned twice in case the traversal has been resumed.
   * <p/>
   * <p>
   * At the end, fetches the ACL of all the documents contained in the
   * {@link SPDocumentList} object. Ensures that ACL are not re-fetched when
   * documents from previous batch traversal are being returned.
   * </p>
   * <p>
   * <b>No documents are returned in case there are failures/errors while
   * retrieving ACLs</b>
   * </p>
   * <p>
   * Logs the {@link OutOfMemoryError} when fetching ACLs. For retry, need to
   * edit properties in connectorInstance.xml and restart
   * <ul>
   * <li>If 'fetchACLInBatches' is enabled, tries to fetch ACLs in smaller
   * batches of (n/aclBatchSizeFactor) (n being the number of documents).</li>
   * <li>Both 'fetchACLInBatches' and 'aclBatchSizeFactor' can be edited from
   * connectorInstance.xml</li>
   * </ul>
   * </p>
   *
   * @param globalState The {@link GlobalState} representing all the SharePoint
   *          sites. Primary required when constructing the
   *          {@link SPDocumentList}
   * @param webState The {@link WebState} whose lists ned to be scanned for
   *          documents
   * @param sizeSoFar This indicates the number documents that have been
   *          previously fetched and added to the global crawl queue. This is
   *          useful in cases when a single list/site does not have sufficient
   *          documents that can match the batchHint and hence multiple
   *          site/lists need to be scanned.
   * @param sendPendingDocs True will indicate that documents retrieved as part
   *          of previous batch traversal need to be sent. This will be the case
   *          when connector returned batch Hint or little more docs, but the CM
   *          did not feed all of them to GSA and checkPoint() was called,
   *          implying there are docs from previous batch traversal to be sent.
   *          In such a case, ACLs should not be re-fetched
   * @return {@link SPDocumentList} containing crawled {@link SPDocument}.
   */
  public SPDocumentList traverse(final GlobalState globalState,
      final WebState webState, int sizeSoFar, boolean sendPendingDocs) {
    if (webState == null) {
      LOGGER.warning("global state is null");
      return null;
    }

    noOfVisitedListStates = 0;
    SPDocumentList resultSet = null;
    for (final Iterator<ListState> iter = webState.getCurrentListstateIterator(); iter.hasNext();) {
      final ListState list = iter.next();

      // Mark this list as current list so that the next traversal
      // request starts from here and already scanned lists are not
      // unnecessarily re-scanned.
      webState.setCurrentList(list);
      if (list.getCrawlQueue() == null) {
        continue;
      }

      SPDocumentList resultsList = null;

      try {
        LOGGER.log(Level.FINE, "Handling crawl queue for list URL [ "
            + list.getListURL() + " ]. ");
        resultsList = handleCrawlQueueForList(globalState, webState, list);
        noOfVisitedListStates++;
      } catch (final Exception e) {
        LOGGER.log(Level.WARNING, "Problem in handling crawl queue for list URL [ "
            + list.getListURL() + " ]. ", e);
      }

      if ((resultsList != null) && (resultsList.size() > 0)) {
        LOGGER.log(Level.INFO, resultsList.size()
            + " document(s) to be sent from list URL [ " + list.getListURL()
            + " ]. ");
        if (resultSet == null) {
          resultSet = resultsList;
        } else {
          resultSet.addAll(resultsList);
        }
      } else {
        LOGGER.log(Level.FINE, "No documents to be sent from list URL [ "
            + list.getListURL() + " ]. ");
      }
      if (resultsList != null) {
        sizeSoFar += resultsList.size();
      }

      // Check if the docs added so far meet the batchHint
      if (sizeSoFar >= sharepointClientContext.getBatchHint()) {
        LOGGER.info("Stopping traversal because batch hint "
            + sharepointClientContext.getBatchHint()
            + " has been reached. Processed documents: " + sizeSoFar);
        break;
      }
    }

    // Fetch ACL for all the documents crawled from the current WebState
    if (!handleACLForDocuments(resultSet, webState, globalState, sendPendingDocs)) {
      return null;
    }

    LOGGER.config(noOfVisitedListStates + " lists scanned from site "
        + webState.getWebUrl() + ". found " + resultSet + " docs");

    return resultSet;
  }

  /**
   * If the connector is set to push ACL, fetches the ACL. Takes care to
   * consider that ACL is not retrieved more than once esp. for when documents
   * are pending from previous batch traversals
   *
   * @param resultSet The list of documents discovered in current/previous batch
   *          traversals
   * @param webState The web state representing the site
   * @param globalState The global state representing the list of all sites and
   *          their information
   * @param sendPendingDocs True if the documents were discovered in previous
   *          batch traversal but fed in the current traversal OR false
   *          otherwise
   * @return True if ACL was retrieved successfully OR false in case of any
   *         exceptions/errors
   */
  boolean handleACLForDocuments(SPDocumentList resultSet, WebState webState,
      GlobalState globalState, boolean sendPendingDocs) {

    if (!sharepointClientContext.isPushAcls()) {
      // When the connector is not set to feed ACLs no further checks are
      // required, just return true to send docs to CM and GSA
      return true;
    }

    if (sendPendingDocs) {
      // This is to indicate that ACLs have been retrieved previously and
      // hence just return the set of docs
      return true;
    }

    boolean aclRetrievalResult = false;
    // Fetch ACL for all the documents crawled from the current WebState
    // Do not try to re-fetch the ACL when documents are pending from
    // previous batch traversals
    if (null != resultSet && resultSet.size() > 0) {

      if (sharepointClientContext.isFetchACLInBatches()) {
        aclRetrievalResult = fetchACLInBatches(resultSet, webState, globalState, sharepointClientContext.getAclBatchSizeFactor());
      } else {
        aclRetrievalResult = fetchACLForDocuments(resultSet, webState, globalState);
      }

      if (!aclRetrievalResult) {
        LOGGER.log(Level.WARNING, "No documents will be sent for site [ "
            + webState.getWebUrl()
            + " ] as ACL retrieval has failed. Please check the errors/logs associated with ACL retrieval before this");
      }
    }

    return aclRetrievalResult;
  }

  /**
   * Fetches the ACL for documents.
   * <p>
   * Based on the size of ACL per document, the WS response can be large and
   * result in {@link java.lang.OutOfMemoryError}. In such a case, the connector
   * will log the error
   * </p>
   *
   * @param resultSet The list of documents for which ACL should be fetched.
   * @param webState The web state representing the site
   * @param globalState The global state representing the list of all sites and
   *          their information
   * @return True if ACL was retrieved successfully OR false in case of any
   *         exceptions/errors
   */
  private boolean fetchACLForDocuments(SPDocumentList resultSet,
      WebState webState, GlobalState globalState) {

    if (resultSet.size() <= 0) {
      LOGGER.log(Level.CONFIG, "Result set is empty. No documents to fetch ACL");
      return false;
    }

    LOGGER.log(Level.INFO, "Fetching ACls for #" + resultSet.size()
        + " documents crawled from web " + webState.getWebUrl());
    try {
      AclWS aclWs = clientFactory.getAclWS(sharepointClientContext,
          webState.getWebUrl());
      aclWs.fetchAclForDocuments(resultSet, webState);
    } catch (Throwable t) {
      logError(resultSet, webState, t);
      // Return false indicating that the ACL retrieval for current batch
      // has failed and skipped
      return false;
    }

    // Return true indicating successful retrieval of ACL
    return true;
  }

  /**
   * Common method to log ACL retrieval errors
   *
   * @param resultSet The document list for which ACL retrieval was attempted
   * @param te The error/exception encountered
   */
  private void logError(SPDocumentList resultSet, WebState webState,
      Throwable te) {

    // Check for OOM and indicate that connector service needs to be
    // restarted
    if (te instanceof OutOfMemoryError) {
      LOGGER.log(Level.SEVERE, "Connector encountered fatal error : \"OutOfMemoryError\" which might be due to a large web service response while fetching ACL for "
          + resultSet.size()
          + " documents for documents crawled under WebState [ "
          + webState.getWebUrl()
          + " ]. Please enable 'fetchACLInBatches' flag and modify the 'aclBatchSizeFactor' in connectorInstance.xml and restart the connector service", te);
    } else {
      LOGGER.log(Level.WARNING, "Problem while fetching ACLs for documents crawled under WebState [ "
          + webState.getWebUrl() + " ] . ", te);
    }

    LOGGER.warning("Skipping ACL retrieval for the document list : "
        + resultSet.toString());
  }

  /**
   * Fetches ACL for documents in batches. Required to handle the
   * {@link OutOfMemoryError} kind errors
   * <ul>
   * <li>When re-fetching ACLs, tries to fetch in smaller batches of
   * n/batchSizeFactor (n being he number of documents).</li>
   * </ul>
   *
   * @param resultSet The set of documents whose ACL needs to be re-fetched in
   *          smaller batches
   * @param webState The {@link WebState} to which the documents belong
   * @param globalState The {@link GlobalState} required primarily for the
   *          {@link SPDocumentList}
   * @param batchSizeFactor The factor by which the current batch of documents
   *          should be divided to arrive at a smaller batch. The formula used
   *          is [n/batchSizeFactor]
   * @return True if ACLs were retrieved successfully OR false in case of any
   *         exceptions/errors
   */
  /*
   * The access method is package level for JUnit test cases
   */
  boolean fetchACLInBatches(SPDocumentList resultSet, WebState webState,
      GlobalState globalState, int batchSizeFactor) {

    if (resultSet.size() <= 0) {
      LOGGER.log(Level.CONFIG, "Result set is empty. No documents to fetch ACL");
      return false;
    }

    // Default is 1
    int batchSize = 1;

    if (batchSizeFactor > 1) {
      // Connector should attempt ACL retrieval in batches. Determine the
      // batchSize using batchSizeFactor.
      batchSize = resultSet.size() / batchSizeFactor;

      // This is to handle the cases like [1/2=0] and the batchSize will
      // be set to 0. This can result into an infinite loop
      if (batchSize == 0)
        batchSize = resultSet.size();
    }

    LOGGER.info("The connector will attempt to fetch ACLs for documents in batches of "
        + batchSize);

    int toIndex = 0;
    for (int i = 0; i < resultSet.size(); i += batchSize) {
      // Use the batchSize to identify the subset of docs. The toIndex
      // indicates the end of sub-set with 'i' indicating the start.
      toIndex += batchSize;
      if (toIndex > resultSet.size()) {
        toIndex = resultSet.size();

        // In case the start and end index is same it will result in an
        // empty list. So ignore and proceed to next level
        if (i == toIndex) {
          LOGGER.log(Level.WARNING, "The start and end index of the List of the documents should not be same");
          continue;
        }
      }
      SPDocumentList docList = new SPDocumentList(
          resultSet.getDocuments().subList(i, toIndex), globalState);

      // Fetch ACL
      if (!fetchACLForDocuments(docList, webState, globalState)) {
        // Return false indicating ACL retrieval has failed and the
        // entire batch of documents need to be skipped
        return false;
      }
    }

    return true;
  }

  /**
   * Discover extra webs viz, MySites, Personal Sites, GSSiteDiscover discovered
   * sites etc and store them into allSites.
   *
   * @param allSites
   * @param spType
   * @throws SharepointException
   */
  private void discoverExtraWebs(final Set<String> allSites, final SPType spType)
      throws SharepointException {
    // TODO: Move this to the client factory.
    if (SPType.SP2003 == spType) {
      LOGGER.log(Level.INFO, "Getting the initial list of MySites/Personal "
          + "sites for SharePoint type SP2003. Context URL [ "
          + sharepointClientContext.getSiteURL() + " ]");
      final UserProfile2003WS userProfileWS =
          clientFactory.getUserProfile2003WS(sharepointClientContext);
      if (userProfileWS.isSPS()) {// Check if SPS2003 or WSS 2.0
        try {
          final Set<String> personalSites = userProfileWS.getPersonalSiteList();
          allSites.addAll(personalSites);
        } catch (final Exception e) {
          LOGGER.log(Level.WARNING, "Unable to get MySites for the Context URL [ "
              + sharepointClientContext.getSiteURL() + " ]", e);
        }
      }
    } else if (SPType.SP2007 == spType) {
      final String strMySiteURL = sharepointClientContext.getMySiteBaseURL();
      if ((strMySiteURL != null) && (!strMySiteURL.trim().equals(""))) {
        LOGGER.log(Level.INFO, "Getting the initial list of MySites for SharePoint type SP2007 from MySiteBaseURL [ "
            + strMySiteURL + " ]");
        final UserProfile2007WS userProfileWS =
            clientFactory.getUserProfile2007WS(sharepointClientContext);
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
      final SiteDiscoveryWS gspSiteDiscoveryWS =
          clientFactory.getSiteDiscoveryWS(sharepointClientContext, null);
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
   * @return a set of all new webs that have been added to the globalstate
   */
  private Set<WebState> updateGlobalState(final GlobalState globalState,
      final Set<String> allSites) {
    Set<WebState> newWebs = new HashSet<WebState>();
    if ((null == allSites) || (allSites.size() == 0)) {
      return newWebs;
    }
    final Iterator<String> itAllSites = allSites.iterator();
    while ((itAllSites != null) && (itAllSites.hasNext())) {
      final String url = itAllSites.next();
      final WebState webState = updateGlobalState(globalState, url);
      if (null != webState) {
        newWebs.add(webState);
      }
    }
    return newWebs;
  }

  /**
   * Check for a web if it exists in the global state. If not, then creates a
   * corresponding web state and adds it into the global state.
   *
   * @param globalState
   * @param url
   * @return {@link WebState} null if the webstate was already existing in the
   *         globalstate. Otherwise a valid reference to the newly created
   *         WebState
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
        websWS = clientFactory.getWebsWS(sharepointClientContext);
      } catch (final Exception e) {
        LOGGER.log(Level.WARNING, "webWS creation failed for URL [ " + url
            + " ]. ", e);
      }
      if (null != websWS) {
        webUrl = websWS.getWebURLFromPageURL(url);
        if (!url.equals(webUrl)) {
          wsGS = globalState.lookupWeb(webUrl, null);
        }
      }
    }

    if (null == wsGS) {// new web
      LOGGER.config("Making WebState for : " + webUrl);
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
   * the lists/libraries and the documents from each discovered web.
   *
   * @param globalState The recent state information
   */
  // FIXME SharePointClientContext should not be passed as an argument in the
  // methods that are called from here. Instead, use the class member.
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

    SiteDiscoveryWS webCrawlInfoFetcher = null;
    if (sharepointClientContext.isUseSPSearchVisibility()) {
      webCrawlInfoFetcher = clientFactory.getSiteDiscoveryWS(tempCtx, null);
    }

    // At the start of a new traversal cycle, we update the WebCrawlInfo of
    // all the webs
    if (globalState.isBFullReCrawl() && null != webCrawlInfoFetcher) {
      webCrawlInfoFetcher.updateWebCrawlInfoInBatch(globalState.getAllWebStateSet());
    }

    nDocuments = 0;
    doCrawl = true;

    ListState nextList = globalState.getLastCrawledList();
    WebState nextWeb = globalState.getLastCrawledWeb();

    if (null == nextWeb) {
      nextWeb = globalState.lookupWeb(sharepointClientContext.getSiteURL(),
        sharepointClientContext);
    } else {
      sharepointClientContext.setSiteURL(nextWeb.getWebUrl());
    }

    // start and end recrawl is used for detecting non-existent webs/lists
    globalState.startRecrawl();

    if (null == nextWeb) {
      nextWeb = updateGlobalState(globalState, sharepointClientContext.getSiteURL());
      if (null == nextWeb) {
        throw new SharepointException(
            "Starting WebState for the current traversal can not be ddetermined.");
      }
      if (null != webCrawlInfoFetcher) {
        nextWeb.setWebCrawlInfo(webCrawlInfoFetcher.getCurrentWebCrawlInfo());
      }
    }

    LOGGER.info("Starting traversal from site [ " + nextWeb + " ]. ");

    SPType spType = nextWeb.getSharePointType();

    // To store the intermediate webs discovered during crawl
    Set<String> allSites = new TreeSet<String>();

    ArrayList<String> lstLookupForWebs = new ArrayList<String>();

    // Traverse sites and lists from the last crawled site and list to fetch
    // batch hint # of docs
    nextWeb = traverseSites(globalState, allSites, tempCtx, nextWeb, nextList, lstLookupForWebs);

    // This will contain all the newly discovered webs and is used to
    // identify those webs which should be queried for their search
    // visibility options set on SharePoint.
    Set<WebState> newWebs = new HashSet<WebState>();

    // Update all the web info into the globalstate. The newly discovered
    // webs, if any, will be processed in the same batch traversal in case
    // the batch hint # of documents have not been discovered
    newWebs.addAll(updateGlobalState(globalState, allSites));

    // Cases being handled here:
    // 1. Batch hint # of documents have not been discovered, but there are
    // new sites which have been discovered. Crawl documents till you get
    // the batch hint # of docs
    // 2. Batch hint # of documents have not been discovered and no new
    // sites have been discovered. In such cases get any new
    // personal/mysites, sites discovered by GSS. Add them to the global
    // state and crawl them till batch hint # of documents is reached.
    if (doCrawl && spType != null) {
      // If the first check has passed, it might mean Case 1. If the
      // following if block is skipped, it means this is Case 1, else it
      // will be Case 2
      if (newWebs.size() == 0) {
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
        newWebs.addAll(updateGlobalState(globalState, allSites));
      }

      // The following does not care if the sites are discovered for Case
      // 1 or Case 2. It will simply go ahead and crawl batch hint no. of
      // docs from the new sites
      if (newWebs.size() > 0) {
        LOGGER.log(Level.INFO, "global state has been updated with #"
            + newWebs.size()
            + " newly discovered sites. About to traverse them for docs");
        if (null != webCrawlInfoFetcher) {
          webCrawlInfoFetcher.updateWebCrawlInfoInBatch(newWebs);
        }

        // Traverse sites and lists under them to fetch batch hint # of
        // docs
        traverseSites(globalState, allSites, tempCtx, nextWeb, nextList, lstLookupForWebs);
        newWebs.clear();

        // There are chances that new sites are discovered (child sites
        // OR linked sites) during the traversal of sites discovered as
        // linked sites themselves OR as child sites OR through GSS. In
        // such cases, the connector should just create webstates and
        // add them to the global state. The next batch traversal will
        // take them up for traversal
        newWebs.addAll(updateGlobalState(globalState, allSites));
        if (newWebs.size() > 0) {
          if (null != webCrawlInfoFetcher) {
            webCrawlInfoFetcher.updateWebCrawlInfoInBatch(newWebs);
          }
          doCrawl = false;
        }
      }
    } else if (newWebs.size() > 0 && null != webCrawlInfoFetcher) {
      // This is the case when we have reached the batch-hint while
      // crawling the first web itself and hence no further discovery
      // has been done. At this point, we must update the WebcrawlInfo of
      // all the child/linked sites that might have been discovered as
      // part of the site's crawling. If we do not do this here, these
      // webs will become known webs in the next batch traversal and we do
      // not query WebCrawlInfo of known webs in between a traversal
      // cycle.
      webCrawlInfoFetcher.updateWebCrawlInfoInBatch(newWebs);
    }

    globalState.setBFullReCrawl(doCrawl);
    globalState.endRecrawl(sharepointClientContext);

    if (null != sharepointClientContext.getUserDataStoreDAO()
        && sharepointClientContext.getUserDataStoreDAO().getUdsCacheSize() > 0) {
      sharepointClientContext.getUserDataStoreDAO().cleanupCache();
    }
    LOGGER.log(Level.INFO, "Returning after crawl cycle.. ");
  }

  public boolean isDoCrawl() {
    return doCrawl;
  }

  /**
   * Makes a call to WSClient layer to get the alerts for a site and updates the
   * global state. Alerts, in SharePoint are created at web level. Though, in
   * the state file that connector maintains a SPDoc can only be inside a
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
      final AlertsWS alertsWS = clientFactory.getAlertsWS(tempCtx);
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
   * attachments from Generic Lists and Issues in sharepoint under a given site.
   * It first calls SiteData web service to get all the Lists. And then calls
   * Lists web service to get the list items for the lists which are of the type
   * SPDocument Library, Generic Lists or Issues. For attachments in Generic
   * List items and Issues, it calls Lists web service to get attachments for
   * these list items.
   *
   * @param tempCtx Current connector context
   * @param webState The state information of the web which is to be crawled for
   *          documents
   * @param nextList Last List traversed. If the current web contains this list,
   *          the traversal will start from here.
   * @param allWebs Contains all the webs that has been discovered from link
   *          sites/Site directory.
   */
  private void updateWebStateFromSite(final SharepointClientContext tempCtx,
      final WebState webState, ListState nextList, final Set<String> allWebs)
      throws SharepointException {
    List<SPDocument> listItems = new ArrayList<SPDocument>();

    // get all the lists for the given web // e.g. picture,wiki,document
    // libraries etc.
    final SiteDataWS siteDataWS = clientFactory.getSiteDataWS(tempCtx);
    List<ListState> listCollection = siteDataWS.getNamedLists(webState);

    // Remove duplicate lists, if any.
    // TODO: We do not need to do this. Web Service does not return
    // duplicate lists.
    listCollection = new ArrayList<ListState>(new TreeSet<ListState>(
        listCollection));

    if (tempCtx.isUseSPSearchVisibility()) {
      try {
        SiteDiscoveryWS gssd = clientFactory.getSiteDiscoveryWS(
            tempCtx, webState.getWebUrl());
        gssd.updateListCrawlInfo(listCollection);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Exception occurred when trying to to update the ListCrawlInfo for web [ "
            + webState.getWebUrl() + " ] ", e);
      }
    }

    // Updating the latest metadata info for all list states. We may do this
    // updation when the crawl will begin; that will save this extra
    // iteration over the ListStates. But, there is one metadata which
    // must be updated before the change (ACL) detection and crawl begins.
    // That metadata is ListState.InheritiedSecurity flag which is very
    // important while processing ACL related changes.
    // TODO: with some re-structuring of code, we can still avoid this extra
    // iteration.
    for (ListState currentListState : listCollection) {
      ListState listState = webState.lookupList(currentListState.getPrimaryKey());
      if (null != listState) {
        listState.updateList(currentListState);
      }
    }

    /*
     * If the nextList belongs the current web and is still existing on the
     * SharePoint site, start traversing for this list onwards.
     */
    if (null != nextList && nextList.getParentWebState().equals(webState)
        && listCollection.contains(nextList)) {
      Collections.rotate(listCollection, -(listCollection.indexOf(nextList)));
    }

    AclWS aclWs = clientFactory.getAclWS(tempCtx, webState.getWebUrl());
    try {
      aclWs.fetchAclChangesSinceTokenAndUpdateState(webState);
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING, "Problem Interacting with Custom ACl WS. web site [ "
          + webState.getWebUrl() + " ]. ", e);
    }

    List<SPDocument> aclChangedItems = null;
    final ListsHelper listsHelper = new ListsHelper(tempCtx);
    for (int i = 0; i < listCollection.size(); i++) {
      final ListState currentList = listCollection.get(i);
      ListState listState = webState.lookupList(currentList.getPrimaryKey());

      if (sharepointClientContext.isUseSPSearchVisibility()) {
        // If this list is marked for No Crawling, do not crawl this
        // list.
        // Please note that, if this list is already known to the
        // connector, it'll keep existing in the connector's state. This
        // implies that if a list is marked as NoCrawl list on
        // SharePoint in between the connector's traversal, crawling of
        // this list will be paused at whatever state it is in. As soon
        // as the NoCrawl flag on SharePoint is reverted, the crawling
        // will be resumed from the saved state.
        if (currentList.isNoCrawl()) {
          LOGGER.log(Level.WARNING, "Skipping List URL [ "
              + currentList.getListURL()
              + " ] while crawling because it has been marked for No Crawling on SharePoint. ");
          if (null == listState) {
            // Make this list known by keeping it in the state. But,
            // do not crawl
            webState.AddOrUpdateListStateInWebState(currentList, currentList.getLastMod());
          }
          continue;
        }
      }

      /*
       * If we already knew about this list, then only fetch docs that have
       * changed since the last doc we processed. If it's a new list (e.g. the
       * first SharePoint traversal), we fetch everything.
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
            // folders and the items under that. This is required
            // for sending delete feeds for the documents when their
            // parent folder is deleted.
            LOGGER.log(Level.CONFIG, "Discovering all folders under current list/library [ "
                + listState.getListURL() + " ] ");
            try {
              listsHelper.getSubFoldersRecursively(listState, null, null);
            } catch (final Exception e) {
              LOGGER.log(Level.WARNING, "Exception occured while getting the folders hierarchy for list [ "
                  + listState.getListURL() + " ]. ", e);
            } catch (final Throwable t) {
              LOGGER.log(Level.WARNING, "Error occured while getting the folders hierarchy for list [ "
                  + listState.getListURL() + " ]. ", t);
            }
          }

          try {
            listItems = listsHelper.getListItemChangesSinceToken(listState, allWebs);
          } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Exception thrown while getting the documents under list [ "
                + listState.getListURL() + " ].", e);
          } catch (final Throwable t) {
            LOGGER.log(Level.WARNING, "Error thrown while getting the documents under list [ "
                + listState.getListURL() + " ].", t);
          }
        } else {
          try {
            listItems = listsHelper.getListItems(listState, null, null, allWebs);
          } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Exception thrown while getting the documents under list [ "
                + listState.getListURL() + " ].", e);
          }
        }
      } else {
        LOGGER.info("revisiting listState [ " + listState.getListURL() + " ]. ");
        listState.setExisting(true);
        listState.setNextPage(null);

        String lastDocID = null;

        SPDocument lastDoc = listState.getLastDocForWSRefresh();

        /*
         * We must ensure that the last doc that we are using was actually sent
         * as ADD feed and not as DELETE feed. It might be possible that in one
         * cycle we identify a list as non-existing and hence started sending
         * delete feeds for it. But, in the next cycle that list has been
         * restored, in that case we can not rely on the lastDoc which has been
         * set by a delete feed. We also need to reset the change token in that
         * case to start a full crawl.
         */
        if (lastDoc != null) {
          if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()
              && ActionType.DELETE.equals(lastDoc.getAction())) {
            listState.resetState();
            if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
              // In case of content feed, we need to keep track of
              // folders and the items under that. This is
              // required for sending delete feeds for the
              // documents when their parent folder is deleted.
              LOGGER.log(Level.CONFIG, "Discovering all folders under current list/library [ "
                  + listState.getListURL() + " ] ");
              try {
                listsHelper.getSubFoldersRecursively(listState, null, null);
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
            webState.AddOrUpdateListStateInWebState(listState, currentList.getLastMod());

            // Any documents to be crawled because of ACl Changes
            aclChangedItems = aclWs.getListItemsForAclChangeAndUpdateState(listState, listsHelper);

            if (null == aclChangedItems
                || aclChangedItems.size() < sharepointClientContext.getBatchHint()) {
              // Do regular incremental crawl
              listItems = listsHelper.getListItemChangesSinceToken(listState, allWebs);
            }
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
            webState.AddOrUpdateListStateInWebState(listState, currentList.getLastMod());
            LOGGER.info("fetching changes since " + Util.formatDate(dateSince)
                + " for list [ " + listState.getListURL() + " ]. ");

            // check if date modified for the document library
            final Calendar dateCurrent = listState.getLastModCal();
            if (dateSince.before(dateCurrent)) {
              listState.setNewList(true);
            }

            listItems = listsHelper.getListItems(listState, dateSince, lastDocID, allWebs);
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
            final List<SPDocument> attachments = listsHelper.getAttachments(listState, doc);
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
          final SPDocument listDoc = listState.getDocumentInstance(sharepointClientContext.getFeedType());
          listItems.add(listDoc);

          /*
           * The only purpose of list.isNewList to decide whether to send the
           * list as a document. Since, just now we have done this, let's mark
           * the list as not new.
           */
          listState.setNewList(false);
        }
      } else {

        // If any of the list has not been traversed completely, doCrawl
        // must not be set true.
        doCrawl = false;
      }

      // Add aclChangedItems to the docs crawled under regular crawling.
      // This is the right place to do this because all the operations
      // pertaining to regular crawling have been made. But, the
      // batch-hint check is yet to be done
      if (null != aclChangedItems) {
        if (null != listItems) {
          listItems.addAll(aclChangedItems);
        } else {
          listItems = aclChangedItems;
        }
      }

      listState.setCrawlQueue(listItems);
      // Set the last crawled date time. This is informative value for the
      // user viewing the state file
      listState.setLastCrawledDateTime(Util.formatDate(Calendar.getInstance(), Util.TIMEFORMAT_WITH_ZONE));

      if (null == listItems || listItems.size() == 0) {
        LOGGER.log(Level.CONFIG, "No items found from list " + listState);
      } else {
        Collections.sort(listItems);
        LOGGER.log(Level.INFO, "found " + listItems.size()
            + " items from list " + listState);
        nDocuments += listItems.size();
        final int batchHint = sharepointClientContext.getBatchHint();

        // As per Issue 116 we need to stop at batchHint or a little
        // more
        if (nDocuments >= batchHint) {
          doCrawl = false;
          break;
        }
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
   * @param globalState The global state which has the list of sites (webstates)
   *          that need to be crawled for documents
   * @param allSites The list of sites
   * @param sharePointClientContext The current connector context. Instance of
   *          {@link SharepointClientContext}
   * @param nextWeb last site (webstate) that was crawled
   * @param nextList last liststate that as crawled
   * @param lstLookupForWebs webs which are already traversed and should not be
   *          traversed again
   * @throws SharepointException In case of any problems fetching documents
   * @return Last Web crawled. This helps caller an idea about from where the
   *         next crawl should begin.
   */
  // TODO: Why do we pass SharePointClientContext object as argument here?
  // It's already available as a member of this class. Is there any
  // intentional differences between the states of these two
  // SharePointClientContexts?
  private WebState traverseSites(GlobalState globalState, Set<String> allSites,
      SharepointClientContext sharePointClientContext, WebState nextWeb,
      ListState nextList, ArrayList<String> lstLookupForWebs)
      throws SharepointException {
    globalState.setCurrentWeb(nextWeb);
    final Iterator<WebState> itWebs = globalState.getCircularIterator();
    while (itWebs.hasNext()) {
      WebState ws = itWebs.next(); // Get the first web
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

      try {
        sharePointClientContext.setSiteURL(webURL);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Exception occurred when trying to set the webUrl [ "
            + webURL + " ] context", e);
        continue;
      }

      if (sharepointClientContext.isUseSPSearchVisibility()) {
        // Even if a web is not crawled due to the SP search visibility,
        // it's reference is kept in the connector's state. This is to
        // avoid unnecessary discovery (and WebState construction) of
        // these webs again and again.
        if (ws.isNoCrawl()) {
          LOGGER.log(Level.WARNING, "Skipping Web URL [ "
              + webURL
              + " ] while crawling because it has been marked for No Crawling on SharePoint. ");
          continue;
        }
      }

      nextWeb = ws;
      LOGGER.config("Crawling site [ " + webURL + " ] ");
      final int currDocCount = nDocuments;
      try {
        // Process the web site, and add the link site info to allSites.
        updateWebStateFromSite(sharePointClientContext, ws, nextList, allSites);

        if (currDocCount == nDocuments) {
          // get Alerts for the web and update webState. The above
          // check is added to reduce the frequency with which
          // getAlerts WS call is made.
          LOGGER.fine("Getting alerts under site [ " + webURL + " ]");
          processAlerts(ws, sharePointClientContext);
          // get site data for the web and update webState.
          LOGGER.fine("Geting landing page data for the site [ " + webURL
              + " ]");
          processSiteData(ws, sharepointClientContext);
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
            + nDocuments + " docs. batch-hint is " + batchHint);
        doCrawl = false;
        break;
      }

      // Get the next web and discover its direct children
      sharepointClientContext.setSiteURL(webURL);
      WebsWS websWS = clientFactory.getWebsWS(sharepointClientContext);
      try {
        final Set<String> allWebStateSet = websWS.getDirectChildsites();
        final int size = allWebStateSet.size();
        if (size > 0) {
          LOGGER.log(Level.INFO, "Discovered " + size + " child sites under [ "
              + webURL + "]. ");
        } else {
          LOGGER.log(Level.CONFIG, "Discovered " + size
              + " child sites under [ " + webURL + "]. ");
        }
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

  /**
   * Makes a call to SiteData web service to get data for a site and update
   * global state. Site data in SharePoint is created at site level. Though, in
   * the state file that connector maintains a SPDocument can only be inside a
   * ListState. Hence we need to create a dummy list here.
   *
   * @param webState for which SPDcocument needs to be constructed.
   * @param tempCtx is the temporary SharepointClientContext object.
   */
  private void processSiteData(final WebState webState,
      final SharepointClientContext tempCtx) {
    if (null == webState) {
      return;
    }

    final Calendar cLastMod = Calendar.getInstance();
    cLastMod.setTime(new Date());
    ListState currentDummySiteDataList = null;

    try {
      currentDummySiteDataList = new ListState(webState.getPrimaryKey(),
          webState.getTitle(), webState.getPrimaryKey(), cLastMod,
          SPConstants.SITE, webState.getPrimaryKey(), webState);
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING, "Unable to create the dummy list state for site. "
          + webState.getWebUrl(), e);
      return;
    }

    // find the list in the Web state
    ListState dummySiteListState = webState.lookupList(currentDummySiteDataList.getPrimaryKey());
    if (dummySiteListState == null) {
      dummySiteListState = currentDummySiteDataList;
    }
    LOGGER.log(Level.INFO, "Getting site data. internalName [ "
        + webState.getWebUrl() + " ] ");
    List<SPDocument> documentList = new ArrayList<SPDocument>();
    SPDocument document = null;

    try {
      final SiteDataWS siteDataWS = clientFactory.getSiteDataWS(tempCtx);
      // need to check whether the site exist or not and is not null
      if (webState.isExisting() && null != webState) {
        document = siteDataWS.getSiteData(webState);
        if (null != document) {
          documentList.add(document);
        }
      }
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING, "Problem while getting site data. ", e);
    }

    if (dummySiteListState.isExisting() && null != document) {
      // Mark dummy list state to true in order to differentiate this list state
      // with
      // other lists in web state.
      dummySiteListState.setSiteDefaultPage(true);
      webState.AddOrUpdateListStateInWebState(dummySiteListState, currentDummySiteDataList.getLastMod());
      dummySiteListState.setCrawlQueue(documentList);
    }
  }
}
