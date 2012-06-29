// Copyright 2007 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.spiimpl;

import com.google.enterprise.connector.adgroups.AdGroupsTraversalManager;
import com.google.enterprise.connector.sharepoint.client.SharepointClient;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.social.SharepointSocialUserProfileDocumentList;
import com.google.enterprise.connector.sharepoint.social.SharepointSocialTraversalManager;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointConnector.SocialOption;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.GlobalState.CrawlState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.sharepoint.wsclient.client.ClientFactory;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.spi.TraversalContextAware;
import com.google.enterprise.connector.spi.TraversalManager;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is an implementation of the TraversalManager from the spi. All the
 * traversal based logic is invoked through this class.
 *
 * @author amit_kagrawal
 */

public class SharepointTraversalManager implements TraversalManager,
    TraversalContextAware {
  private final Logger LOGGER = Logger
      .getLogger(SharepointTraversalManager.class.getName());
  private final ClientFactory clientFactory;
  private SharepointClientContext sharepointClientContext;
  private SharepointClientContext sharepointClientContextOriginal = null;
  private GlobalState globalState;
  private int hint = -1;

  // The traversal context instance
  private TraversalContext traversalContext;
  private SharepointSocialTraversalManager socialTraversal;
  private AdGroupsTraversalManager adGroupsTraversal;

  /**
   * constructor.
   *
   * @param inConnector
   *          The instance of SharePoint connector for which traversal is to be
   *          done
   * @param inSharepointClientContext
   *          The context attached with the connector instances
   * @throws RepositoryException
   */
  public SharepointTraversalManager(final SharepointConnector inConnector,
      final SharepointClientContext inSharepointClientContext)
      throws RepositoryException {
    this(inConnector, inSharepointClientContext, null, null);
  }

  /**
   * constructor.
   *
   * @param inConnector
   *          The instance of SharePoint connector for which traversal is to be
   *          done
   * @param inSharepointClientContext
   *          The context attached with the connector instances
   * @param inSocialTraversal
   *          inner social connection traversal manager to encapsulate
   * @throws RepositoryException
   */
  public SharepointTraversalManager(final SharepointConnector inConnector,
      final SharepointClientContext inSharepointClientContext,
      SharepointSocialTraversalManager inSocialTraversal,
      AdGroupsTraversalManager inAdGroupsTraversal) 
      throws RepositoryException {
    if (inConnector == null) {
      throw new SharepointException(
          "Cannot initialize traversal manager because SharePointConnector object is null.");
    }
    if (inSharepointClientContext == null) {
      throw new SharepointException(
          "Cannot initialize traversal manager because SharePointClientContext object is null.");
    }
    clientFactory = inConnector.getClientFactory();
    try {
      socialTraversal = inSocialTraversal;
      adGroupsTraversal = inAdGroupsTraversal;
      LOGGER.config("SharepointTraversalManager: "
          + inSharepointClientContext.getSiteURL() + ", "
          + inSharepointClientContext.getGoogleConnectorWorkDir());
      sharepointClientContext = inSharepointClientContext;
      sharepointClientContextOriginal = (SharepointClientContext) inSharepointClientContext
          .clone();
      globalState = new GlobalState(clientFactory,
          inSharepointClientContext.getGoogleConnectorWorkDir(),
          inSharepointClientContext.getFeedType());
      globalState.loadState();
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING, e.getMessage());
      throw new SharepointException(e);
    }
    LOGGER
        .info("SharepointTraversalManager(SharepointConnector inConnector,SharepointClientContext inSharepointClientContext)");
  }

  /**
   * Starts the traversal from a checkpoint specified by CM. The connector has
   * returned this checkpoint information to the CM at the completion of last
   * traversal. Though, SharePoint Connector does not really make use of this
   * checkpoint information for resuming the traversal. Instead, it uses the
   * state file for this purpose. State file implementation is specific to the
   * connector and CM is unaware of this.
   *
   * @param checkpoint
   *          Not really used by the SharePoint connector
   */
  public DocumentList resumeTraversal(final String checkpoint)
      throws RepositoryException {
    LOGGER.info("resumeTraversal, checkpoint received: " + checkpoint);
    DocumentList rsSocial = null;
    boolean docCheckpoint = true; // is this a user profile checkpoint or a doc checkpoint
    if (sharepointClientContext.getSocialOption() != SocialOption.NO) {
      if (checkpoint.startsWith(SharepointSocialUserProfileDocumentList.CHECKPOINT_PREFIX)) {
        rsSocial = doUserprofileCrawl(checkpoint);
        docCheckpoint = false;
      }
    }
    if (docCheckpoint) { // we are resuming a doc feed
      if (adGroupsTraversal != null) {
        adGroupsTraversal.resumeTraversal(checkpoint);
      }
      return resumeDocTraversal(checkpoint);
    } else if ((rsSocial == null) && (sharepointClientContext.getSocialOption() 
        != SocialOption.ONLY)) { // we want doc feed and social feed is complete
      return startDocTraversal();
    } else {
      return rsSocial;
    }
  }

  private DocumentList resumeDocTraversal(final String checkpoint) throws RepositoryException {
    LOGGER.info("resuming document traversal");
    // If feed type has been changed after the last traversal cycle. Let's
    // start a full recrawl
    if ((globalState.getFeedType() == null)
        || !globalState.getFeedType().equals(
            sharepointClientContext.getFeedType())) {
      LOGGER.log(Level.INFO, "feedType updated. initiating a full recrawl. ");
      return startDocTraversal();
    } else {
      sharepointClientContext.setInitialTraversal(false);
      return doTraversal();
    }
  }
  /**
   * Sets the batch hint which declares a threashold on the number of documents
   * that should be sent per traversal
   *
   * @see com.google.enterprise.connector.spi.TraversalManager
   *      #setBatchHint(int)
   */
  public void setBatchHint(final int hintNew) throws RepositoryException {
    hint = hintNew;
    LOGGER.info("BatchHint Set to [ " + hintNew + " ] ");
  }

  private DocumentList doUserprofileCrawl(String checkPoint) {
    DocumentList rsSocial;
    if (socialTraversal != null) {
      try {
        if ((checkPoint == null) || (checkPoint.equals(""))) {
          rsSocial = this.socialTraversal.startTraversal();
        } else {
          rsSocial = this.socialTraversal.resumeTraversal(checkPoint);
        }
      } catch (RepositoryException e) {
        LOGGER.severe("Failed getting userprofiles, continuing with the site");
        rsSocial = null;
      }
    } else {
      LOGGER.info("SocialTraversalManger is null");
      rsSocial = null;
    }
    return rsSocial;
  }

  /**
   * To start a full crawl. Ignoring any checkpoint information.
   *
   * @see com.google.enterprise.connector.spi.TraversalManager #startTraversal()
   */
  public DocumentList startTraversal() throws RepositoryException {
    LOGGER.info("startTraversal()");
    DocumentList rsSocial = null;
    if (sharepointClientContext.getSocialOption() != SocialOption.NO) {
      rsSocial = doUserprofileCrawl("");
    }
    if (sharepointClientContext.getSocialOption() == SocialOption.ONLY)
      return rsSocial;

    // if there is no social traversal to be done or social traversal has
    // finished then do doc traversal
    if ((socialTraversal == null) || (rsSocial == null)) {
      if (adGroupsTraversal != null) {
        adGroupsTraversal.startTraversal();
      }
      return startDocTraversal();
    } else {
      return rsSocial;
    }
  }

  private void initializeGlobalStateForDocTraversal() {
    globalState = null;
    final String workDir = sharepointClientContext.getGoogleConnectorWorkDir();
    // delete the global state.. to simulate full crawl
    GlobalState.forgetState(workDir);
    sharepointClientContext.clearExcludedURLLogs();
    sharepointClientContext.setInitialTraversal(true);
    globalState = new GlobalState(clientFactory,
        sharepointClientContext.getGoogleConnectorWorkDir(),
        sharepointClientContext.getFeedType());
    globalState.setCrawlState(CrawlState.DOC_FEED);
  }

  public DocumentList startDocTraversal() throws RepositoryException {
    LOGGER.info("startDocTraversal");
    initializeGlobalStateForDocTraversal();
    return doTraversal();
  }

  private DocumentList doTraversal() throws RepositoryException {
    LOGGER.config("doTraversal()");

    if (hint == -1) {
      LOGGER.severe("Batch hint is -1");
      throw new SharepointException("Batch hint is -1");
    }
    if (sharepointClientContext == null) {
      LOGGER.severe("SharepointClientContext is null");
      throw new SharepointException("SharepointClientContext is null");
    }

    LOGGER.config("sharepointClientContext.feedType [ "
        + sharepointClientContext.getFeedType() + " ]");
    if (null == sharepointClientContext.getFeedType()) {
      LOGGER.severe("Aborting Traversal. Invalid Feed Type.");
      return null;
    }

    // Set the traversal context on client context so that it can be used by
    // any other classes that will make use of the same.
    sharepointClientContext.setTraversalContext(traversalContext);

    final SharepointClient sharepointClient = new SharepointClient(
        clientFactory, sharepointClientContext);

    sharepointClientContext.setBatchHint(hint);
    SPDocumentList rsAll = null;

    // First, get the documents discovered in the previous crawl cycle.
    // The true flag indicates that we want to check if there are any
    // pending docs from previous crawl cycle
    rsAll = traverse(sharepointClient, true);
    if ((rsAll != null) && (rsAll.size() > 0)) {
      LOGGER.info("Traversal returned " + rsAll.size()
          + " documents discovered in the previous batch traversal(s).");
    } else {
      LOGGER.info("No documents to be sent from previous batch traversal(s). Recrawling...");
      try {
        sharepointClient.updateGlobalState(globalState);
      } catch (final Exception e) {
        LOGGER.log(Level.SEVERE, "Exception while updating global state.... ", e);
      } catch (final Throwable t) {
        LOGGER.log(Level.SEVERE, "Error while updating global state.... ", t);
      }
      // The 'false' flag indicates that we want to scan for all lists for
      // any updates and just not the subset. This is required as the
      // above call to updateGlobalState(globalState) might have
      // discovered docs in one or more (worst case all) list states
      final SPDocumentList rs = traverse(sharepointClient, false);
      if (rs != null) {
        LOGGER.info("Traversal returned " + rs.size()
            + " documents discovered in the current batch traversal.");
        if (rsAll == null) {
          rsAll = rs;
        } else {
          rsAll.addAll(rs);
        }
      } else {
        LOGGER.info("No documents to be sent from the current crawl cycle.");
      }
      if (sharepointClient.isDoCrawl() && (null == rsAll || rsAll.size() == 0)
          && null != globalState.getLastCrawledWeb()) {
        LOGGER.log(Level.INFO, "Setting LastCrawledWebStateID and LastCrawledListStateID as null and updating the state file to reflect that a full crawl has completed...");
        globalState.setLastCrawledWeb(null);
        globalState.setLastCrawledList(null);
        globalState.saveState();
      }
    }

    if (sharepointClientContextOriginal != null) {
      LOGGER.log(Level.FINEST, "Resetting the sharepointClientContext to the original sharepointClientContext at the end of traversal.");
      sharepointClientContext = (SharepointClientContext) sharepointClientContextOriginal.clone();
    }
    if (rsAll != null) {
      LOGGER.info("Traversal returned [" + rsAll.size() + "] documents");
    } else {
      LOGGER.info("Traversal returned [0] documents");
    }

    return rsAll;
  }

  /**
   * Traverses the site for crawled docs. It checks the crawl queue for the
   * given list and creates a document list (instance of {@link SPDocumentList})
   * that will be returned from the current traversal
   * <p>
   * It will either check all lists or only a subset of lists for the current
   * site based on the flag: checkForPendingDocs. Possible cases
   * <ul>
   * <li>If checkForPendingDocs = true, it starts scanning from the web/list for
   * which checkPoint() was called from last batch traversal. Hence only a
   * subset of lists will be scanned.</li>
   * <li>If checkForPendingDocs = false, it starts scanning from the web/list
   * set during the document discovery (
   * {@link SharepointClient#updateGlobalState(GlobalState)})</li>
   * </ul>
   *
   * TODO: In future, this should always scan a subset of lists which have docs
   *       and avoid unnecessary processing of all lists and sites
   *
   * @param sharepointClient The instance of {@link SharepointClient} that will
   *          process the crawl queue to construct the document list
   * @param checkForPendingDocs If true, scans from the list at which
   *          checkPoint() was called. If false, will scan all lists
   * @return {@link SPDocumentList} The document list to be returned from
   *         current batch traversal
   * @since 2.4
   */
  private SPDocumentList traverse(final SharepointClient sharepointClient,
      boolean checkForPendingDocs) {
    if (checkForPendingDocs) {
      WebState ws = globalState.getLastCrawledWeb();
      ListState listState = globalState.getLastCrawledList();
      globalState.setCurrentWeb(ws);
      if (null != ws) {
        ws.setCurrentList(listState);
      }
    }

    // CurrentWeb and CurrentList will define the starting point for
    // the traversal/scan-of-crawl-queues. In case of list, all the
    // lists before CurrentList will not be scanned.
    // TODO: The same is to be done for webs also so that only the relevant
    // WebStates
    // gets scanned. It does not make sense to traverse all the WebStates
    // all the time. Precisely, what we need here is an intelligent
    // liniarIterator instead of a dumb circularIterator.

    SPDocumentList rsAll = null;
    int sizeSoFar = 0;
    LOGGER.log(Level.INFO, "Checking crawl queues of all ListStates/WebStates for pending docs.");
    for (final Iterator<WebState> iter = globalState.getCircularIterator(); iter.hasNext()
        && (sizeSoFar < hint);) {
      final WebState webState = iter.next();
      globalState.setCurrentWeb(webState);
      SPDocumentList rs = null;
      try {
        rs = sharepointClient.traverse(globalState, webState, sizeSoFar, checkForPendingDocs);
      } catch (final Exception e) {
        LOGGER.log(Level.WARNING, "Exception occured while traversing web URL [ "
            + webState.getWebUrl() + " ] ", e);
      } catch (final Throwable t) {
        LOGGER.log(Level.WARNING, "Error occured while traversing web URL [ "
            + webState.getWebUrl() + " ] ", t);
      }
      if ((rs != null) && (rs.size() > 0)) {
        LOGGER.log(Level.INFO, rs.size()
            + " document(s) to be sent from web URL [ " + webState.getWebUrl()
            + " ]. ");
        if (rsAll == null) {
          rsAll = rs;
        } else {
          rsAll.addAll(rs);
        }
        sizeSoFar = rsAll.size();
      } else {
        LOGGER.log(Level.CONFIG, "No documents to be sent from web [ "
            + webState.getWebUrl() + " ] ");
      }
    }
    return rsAll;
  }

  /**
   * Sets the traversal context
   *
   * @param traversalContext The {@link TraversalContext} instance
   */
  public void setTraversalContext(TraversalContext traversalContext) {
    this.traversalContext = traversalContext;
  }
}
