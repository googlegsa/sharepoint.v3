// Copyright 2007 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.client;

import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.StatefulObject;
import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SimplePropertyMap;
import com.google.enterprise.connector.spi.SimpleResultSet;
import com.google.enterprise.connector.spi.SimpleValue;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.ValueType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * Class which maintains all the methods needed to get documents and sites from
 * the sharepoint server. It is a layer between the connector and the actual web
 * services calls .
 * 
 */

public class SharepointClient {
  private static Log logger = LogFactory.getLog(ListState.class);

  public static final String LIST_STATE_NAME = ListState.class.getSimpleName();
  /**
   * Build the Property Map for the connector manager from the sharepoint
   * document.
   * 
   * @param guidList GUID for the sharepoint List
   * @param doc sharepoint document
   * @return Property Map.
   */


  private SharepointClientContext sharepointClientContext;

  public SharepointClient(SharepointClientContext sharepointClientContext)
      throws SharepointException {
    this.sharepointClientContext = sharepointClientContext;
  }


  /**
   * For a single ListState, handle its crawl queue (if any). This means add it
   * to the ResultSet which we give back to the Connector Manager.
   * 
   * @param state
   * @param crawlQueue
   * @return
   */
  private SimpleResultSet handleCrawlQueueForList(GlobalState state,
      ListState list) {
    logger.info("handling " + list.getUrl());
    SimpleResultSet resultSet = new SimpleResultSet();
    List<Document> crawlQueue = list.getCrawlQueue();
    if (crawlQueue == null) return resultSet;
    for (Iterator<Document> iter = crawlQueue.iterator(); iter.hasNext();) {
      SimplePropertyMap pm = Util.propertyMapFromDoc(list.getGuid(), iter
          .next());
      resultSet.add(pm);
    }
    return resultSet;
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
   * 
   * @return resultSet
   */
  public SimpleResultSet traverse(GlobalState state, int sizeHint) {
    SimpleResultSet resultSet = new SimpleResultSet();
    try {
      int sizeSoFar = 0;
      ListState lastVisited = (ListState) state
          .getCurrentObject(LIST_STATE_NAME);
      boolean reachedLastVisited = false;
      if (lastVisited != null) {
        logger.info("traverse() looking for " + lastVisited.getUrl());
      }
      for (Iterator<StatefulObject> iter = state.getIterator(LIST_STATE_NAME); 
          iter.hasNext();) {
        ListState list = (ListState) iter.next();
        if (lastVisited != null && !reachedLastVisited) {
          if (list.getGuid().equals(lastVisited.getGuid())) {
            reachedLastVisited = true;
          }
          continue;
        }
        state.setCurrentObject(LIST_STATE_NAME, list);
        if (list.getCrawlQueue() == null) continue;
        SimpleResultSet resultsList = handleCrawlQueueForList(state, list);
        if (resultsList.size() > 0) {
          resultSet.addAll(resultsList);
          sizeSoFar += resultsList.size();
        }
        // we heed the batch hint, but always finish a List before checking:
        if (sizeHint > 0 && sizeSoFar >= sizeHint) {
          logger.info("Stopping traversal because batch hint " + sizeHint
              + " has been reached");
          break;
        }
      }
    } catch (SharepointException e) {
      e.printStackTrace();
      logger.error(e.toString());
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
    logger.info("updateGlobalState");
    try {
      SiteDataWS siteDataWS = new SiteDataWS(sharepointClientContext);
      List allSites = siteDataWS.getAllChildrenSites();
      state.startRefresh();
      for (int i = 0; i < allSites.size(); i++) {
        Document doc = (Document) allSites.get(i);
        updateGlobalStateFromSite(state, doc.getUrl());
      }
    } catch (SharepointException e) {
      e.printStackTrace();
      logger.error(e.toString());
    }
    state.endRefresh();
  }

  /**
   * Gets all the docs from the Document Library and all the attachments 
   * from items of Generic Lists in sharepoint under a given
   * site. It first calls SiteData web service to get all the Lists. And then
   * calls Lists web service to get the list items for the lists which are of
   * the type Document Library. 
   * For attachments in Generic List items, it calls Lists web service to get 
   * attachments for these list items. 
   * @return resultSet
   */
  private void updateGlobalStateFromSite(GlobalState state, String siteName) {
    logger.info("updateGlobalStateFromSite for " + siteName);
    List listItems = new ArrayList<Document>();

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
      listCollection.addAll(listCollectionGenList);
      for (int i = 0; i < listCollection.size(); i++) {
        BaseList baseList = (BaseList) listCollection.get(i);
        ListState listState = (ListState) state.lookupObject(LIST_STATE_NAME,
            baseList.getInternalName());
        /*
         * If we already knew about this list, then only fetch docs that have
         * changed since the last doc we processed. If it's a new list (e.g. the
         * first Sharepoint traversal), we fetch everything.
         */
        if (listState == null) {
          listState = (ListState) state.makeDependentObject(LIST_STATE_NAME,
              baseList.getInternalName(), baseList.getLastMod());
          listState.setUrl(baseList.getTitle());          
          listItems = listsWS.getListItemChanges(baseList.getInternalName(),
              null);
          logger.info("creating new listState: " + baseList.getTitle());
        } else {
          logger.info("revisiting old listState: " + listState.getUrl());
          state.updateStatefulObject(listState, listState.getLastMod());
          Calendar dateSince = listState.getDateForWSRefresh();
          logger.info("fetching changes since " + Util.formatDate(dateSince));
          listItems = listsWS.getListItemChanges(baseList.getInternalName(),
              dateSince);
        }
        if (baseList.getType().equals(SiteDataWS.GENERIC_LIST)) {
          List attachmentItems = new ArrayList<Document>();
          for (int j = 0; j < listItems.size(); j++) {           
            Document doc = (Document) listItems.get(j);            
            List attachments = listsWS.getAttachments(baseList, doc);
            attachmentItems.addAll(attachments);
          }
          listState.setCrawlQueue(attachmentItems);
          logger.info("found " + attachmentItems.size() + " items(attachments)"
              + " to crawl in " + siteName);    
        } else {
          listState.setCrawlQueue(listItems);
          logger.info("found " + listItems.size() + " items to crawl in "
            + siteName);        
        }
        listState.dumpCrawlQueue();
      }
    } catch (SharepointException e) {
      e.printStackTrace();
      logger.error(e.toString());
    }
  }
}
