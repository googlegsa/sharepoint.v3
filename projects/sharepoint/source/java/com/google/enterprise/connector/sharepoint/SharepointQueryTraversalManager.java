// Copyright 2007 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint;

import com.google.enterprise.connector.sharepoint.client.Document;
import com.google.enterprise.connector.sharepoint.client.SharepointClient;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SharepointException;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.StatefulObject;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.SimpleResultSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class is an implementation of the QueryTraversalManager from the spi.
 * All the traversal based logic is onvoked through this class.
 *
 */

public class SharepointQueryTraversalManager implements QueryTraversalManager {
  private static Log logger;
  private SharepointClientContext sharepointClientContext;
  private SharepointConnector connector;
  protected GlobalState globalState; // not private, so the unittest can see it
  private int hint = -1;
  
  public SharepointQueryTraversalManager(SharepointConnector connector,
    SharepointClientContext sharepointClientContext) 
      throws RepositoryException {
    logger = LogFactory.getLog(SharepointQueryTraversalManager.class);
    this.connector = connector;
    this.sharepointClientContext = sharepointClientContext;
    this.globalState = new GlobalState();
    this.globalState.loadState();
  }
  
  /**
   * Forget whatever state we have. This is strictly for the unittest, so it
   * doesn't get confused by a file left around from a previous run.
   */
  protected void forgetStateForUnittest() {
    try {
      this.globalState = new GlobalState();
    } catch (SharepointException e) {
      logger.error(e.toString());
    }
  }
  
  private void implementCheckpoint(PropertyMap map) throws RepositoryException {
    Document docCheckpoint = SharepointClient.docFromPropertyMap(map);
    String listGuid = SharepointClient.listGuidFromPropertyMap(map);

    /* fix the GlobalState to match 'doc'. Since the Connector Manager may
     * have finished several lists, we have to iterate through all the 
     * lists until we hit this one (that's why we save the list GUID).
     * 
     * First make sure there's no mistake, and this list is something we
     * actually know about:
     */
    ListState listCheckpoint = 
      (ListState) globalState.lookupObject("ListState", listGuid);
    if (listCheckpoint == null) {
      logger.error("Checkpoint specifies a non-existent list: " + listGuid);
      // what to do here? certainly remove crawl queues from any earlier Lists:
      for (Iterator<StatefulObject> iter = globalState.getIterator("ListState");
        iter.hasNext(); ) {
        if (docCheckpoint.getLastMod().compareTo(
            listCheckpoint.getLastModAsCalendar()) > 0) {
          listCheckpoint.setCrawlQueue(null);
        } else {
          break;
        }        
      }
      return;
    }
    logger.info("looking for " + listGuid);
    Iterator<StatefulObject> iterLists = globalState.getIterator("ListState");
    boolean foundCheckpoint = false;
    while (iterLists.hasNext() && !foundCheckpoint) {
      ListState listState = (ListState) iterLists.next();
      List<Document> crawlQueue= listState.getCrawlQueue();
      if (listState.getGuid().equals(listGuid)) {
        logger.info("found it");
        foundCheckpoint = true;
        // take out everything up to this document's lastMod date
        for (Iterator<Document> iterQueue = crawlQueue.iterator(); 
          iterQueue.hasNext(); ) {
          Document docQueue = iterQueue.next();

          // if this doc is later than the checkpoint, we're done:
          if (docQueue.getLastMod().compareTo(docCheckpoint.getLastMod()) >  0) {
            break;
          }
          // otherwise remove it from the queue
          logger.info("removing " + docQueue.getUrl() + " from queue");
          iterQueue.remove(); // it's safe to use the iterator's own remove()
          listState.setLastDocCrawled(docQueue);
          if (docQueue.getDocId().equals(docCheckpoint.getDocId())) {
            break;
          }    
        }  
      } else { // some other list. Assume CM got all the way through the queue
        logger.info("zeroing crawl queue for " + listState.getPrimaryKey());
        if (crawlQueue != null && crawlQueue.size() > 0) {
          listState.setLastDocCrawled(crawlQueue.get(crawlQueue.size() - 1));
        }
        listState.setCrawlQueue(null);
      }
    }
    /* once we've done this, there's no more need to remember where we
     * were. We can start at the earliest (by lastMod) List we have. 
     */
    globalState.setCurrentObject(SharepointClient.LIST_STATE_NAME, null);
  }
  
  /* (non-Javadoc)
   * @see com.google.enterprise.connector.spi.QueryTraversalManager#checkpoint
   * (com.google.enterprise.connector.spi.PropertyMap)
   */
  public String checkpoint(PropertyMap map) throws RepositoryException {
    Document doc = SharepointClient.docFromPropertyMap(map);
    logger.info("checkpoint received for " + doc.getUrl() + " in list " +
        SharepointClient.listGuidFromPropertyMap(map) + " with date " +
        doc.getLastMod());
    implementCheckpoint(map);
    logger.info("checkpoint processed; saving GlobalState to disk.");
    globalState.saveState(); // snapshot it all to disk
    return null;
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.spi.QueryTraversalManager
   * #resumeTraversal(java.lang.String)
   */
  public ResultSet resumeTraversal(String arg0) throws RepositoryException {
    logger.info("resumeTraversal");
    return doTraversal();
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.spi.QueryTraversalManager
   * #setBatchHint(int)
   */
  public void setBatchHint(int hint) throws RepositoryException {
    logger.info("setBatchHint " + hint);
    // this.hint = hint;
    this.hint = 5;
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.spi.QueryTraversalManager
   * #startTraversal()
   */
  public ResultSet startTraversal() throws RepositoryException {
    logger.info("startTraversal");
    return doTraversal();
  }

  /**
   * Private routine that actually does the traversal. If no docs are found
   * in the first sharepointClient.traverse() call, we go back to Sharepoint
   * and fetch a new set of stuff.
   * @return ResultSet
   * @throws RepositoryException
   */
  private ResultSet doTraversal() throws RepositoryException {
    SharepointClient sharepointClient = 
      new SharepointClient(sharepointClientContext);
    SimpleResultSet rs = sharepointClient.traverse(globalState, hint);
    // if the set is empty, then we need to sweep Sharepoint again:
    if (rs.size() == 0) {
      logger.info("traversal returned no new docs: re-fetching ...");
      sharepointClient.updateGlobalState(globalState);
      rs = sharepointClient.traverse(globalState, hint);
    }
    logger.info("doTraversal returning " + rs.size() + " items");
    return rs;           
  }
}
