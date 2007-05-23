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

import com.google.enterprise.connector.sharepoint.client.SPDocument;
import com.google.enterprise.connector.sharepoint.client.SharepointClient;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.spi.HasTimeout;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.PropertyMapList;
import com.google.enterprise.connector.spi.SimplePropertyMapList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.List;

/**
 * This class is an implementation of the TraversalManager from the spi.
 * All the traversal based logic is onvoked through this class.
 *
 */

public class SharepointTraversalManager implements TraversalManager,
  HasTimeout {
  private static Log logger;
  private SharepointClientContext sharepointClientContext;
  private SharepointConnector connector;
  protected GlobalState globalState; // not private, so the unittest can see it
  private int hint = -1;
  
  public SharepointTraversalManager(SharepointConnector connector,
    SharepointClientContext sharepointClientContext) 
      throws RepositoryException {
    logger = LogFactory.getLog(SharepointTraversalManager.class);
    this.connector = connector;
    this.sharepointClientContext = sharepointClientContext;
    this.globalState = new GlobalState();
    this.globalState.loadState();
  }
  
  /**
   * For the HasTimeout interface: tell ConnectorManager we need the maximum
   * amount of time
   * @return integer
   */
  public int getTimeoutMillis() {
    return Integer.MAX_VALUE;
  }
  
  private void implementCheckpoint(PropertyMap map) throws RepositoryException {
    SPDocument docCheckpoint = Util.docFromPropertyMap(map);
    String listGuid = Util.listGuidFromPropertyMap(map);

    /* fix the GlobalState to match 'doc'. Since the Connector Manager may
     * have finished several lists, we have to iterate through all the 
     * lists until we hit this one (that's why we save the list GUID).
     * 
     * First make sure there's no mistake, and this list is something we
     * actually know about:
     */
    ListState listCheckpoint = 
      (ListState) globalState.lookupList(listGuid);
    if (listCheckpoint == null) {
      logger.error("Checkpoint specifies a non-existent list: " + listGuid);
      // what to do here? certainly remove crawl queues from any earlier Lists:
      for (Iterator<ListState> iter = globalState.getIterator();
        iter.hasNext(); ) {
        if (docCheckpoint.getLastMod().compareTo(
            Util.jodaToCalendar(listCheckpoint.getLastMod())) > 0) {
          listCheckpoint.setCrawlQueue(null);
        } else {
          break;
        }        
      }
      return;
    }
    logger.info("looking for " + listGuid);
    Iterator<ListState> iterLists = globalState.getIterator();
    boolean foundCheckpoint = false;
    while (iterLists.hasNext() && !foundCheckpoint) {
      ListState listState = (ListState) iterLists.next();
      List<SPDocument> crawlQueue= listState.getCrawlQueue();
      if (listState.getGuid().equals(listGuid)) {
        logger.info("found it");
        foundCheckpoint = true;
        // take out everything up to this document's lastMod date
        for (Iterator<SPDocument> iterQueue = crawlQueue.iterator(); 
          iterQueue.hasNext(); ) {
          SPDocument docQueue = iterQueue.next();

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
        logger.info("zeroing crawl queue for " + listState.getUrl());
        if (crawlQueue != null && crawlQueue.size() > 0) {
          listState.setLastDocCrawled(crawlQueue.get(crawlQueue.size() - 1));
        }
        listState.setCrawlQueue(null);
      }
    }
    /* once we've done this, there's no more need to remember where we
     * were. We can start at the earliest (by lastMod) List we have. 
     */
    globalState.setCurrentList(null);
  }
  
  /* (non-Javadoc)
   * @see com.google.enterprise.connector.spi.TraversalManager#checkpoint
   * (com.google.enterprise.connector.spi.PropertyMap)
   */
  public String checkpoint(PropertyMap map) throws RepositoryException {
    SPDocument doc = Util.docFromPropertyMap(map);
    logger.info("checkpoint received for " + doc.getUrl() + " in list " +
        Util.listGuidFromPropertyMap(map) + " with date " +
        Util.formatDate(Util.calendarToJoda(doc.getLastMod())));
    implementCheckpoint(map);
    logger.info("checkpoint processed; saving GlobalState to disk.");
    globalState.saveState(); // snapshot it all to disk
    return null;
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.spi.TraversalManager
   * #resumeTraversal(java.lang.String)
   */
  public PropertyMapList resumeTraversal(String arg0) 
    throws RepositoryException {
    logger.info("resumeTraversal");
    return doTraversal();
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.spi.TraversalManager
   * #setBatchHint(int)
   */
  public void setBatchHint(int hint) throws RepositoryException {
    logger.info("setBatchHint " + hint);
    this.hint = hint;
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.spi.TraversalManager
   * #startTraversal()
   */
  public PropertyMapList startTraversal() 
    throws RepositoryException {
    logger.info("startTraversal");
    return doTraversal();
  }

  private void dumpPropertyMapList(SimplePropertyMapList rs) {
    System.out.println("PropertyMapList=" + rs.size() + " items");
    try {
      for (Iterator iter = rs.iterator(); iter.hasNext(); ) {
        PropertyMap pm = (PropertyMap) iter.next();
        for (Iterator iterProp = pm.getProperties(); iterProp.hasNext(); ) {
          Property prop = (Property) iterProp.next();
          System.out.println(prop.getName().toString() +
              " = " + prop.getValue().toString());
        }
      }
    } catch (RepositoryException e) {
      System.out.println("caught exception: " + e.toString());
    }
  }
  
  /**
   * Private routine that actually does the traversal. If no docs are found
   * in the first sharepointClient.traverse() call, we go back to Sharepoint
   * and fetch a new set of stuff.
   * @return PropertyMapList
   * @throws RepositoryException
   */
  private PropertyMapList doTraversal() throws RepositoryException {
    SharepointClient sharepointClient = 
      new SharepointClient(sharepointClientContext);
    SimplePropertyMapList rs = sharepointClient.traverse(globalState, hint);
    // if the set is empty, then we need to sweep Sharepoint again:
    if (rs.size() == 0) {
      logger.info("traversal returned no new docs: re-fetching ...");
      sharepointClient.updateGlobalState(globalState);
      rs = sharepointClient.traverse(globalState, hint);
    }
    if (logger.isInfoEnabled()) {
      dumpPropertyMapList(rs);
    }
    return rs;           
  }
}
