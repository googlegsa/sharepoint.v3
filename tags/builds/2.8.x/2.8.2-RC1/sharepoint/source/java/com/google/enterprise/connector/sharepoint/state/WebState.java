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

package com.google.enterprise.connector.sharepoint.state;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.WebsWS;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import org.joda.time.DateTime;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a SharePoint Web Site as a stateful object
 *
 * @author nitendra_thakur
 */
public class WebState implements StatefulObject {
  private String webUrl = null;
  private String webId = null;
  private String title = "No Title";
  private DateTime insertionTime = null;

  // By default mark all web state as exisitng when they are created.
  private boolean exists = true;

  private TreeSet<ListState> allListStateSet = new TreeSet<ListState>();
  private final Map<String, ListState> keyMap = new HashMap<String, ListState>();
  private static final Logger LOGGER = Logger.getLogger(WebState.class.getName());

  /**
   * The "current" object for ListState. The current object may be null.
   */
  private ListState currentList = null;
  // private String lastCrawledListID = null;
  private ListState lastCrawledList = null;

  private SPType spType;

  /**
   * The timestamp of when was the site crawled last time by the connector
   */
  private String lastCrawledDateTime;

  // The current Change Token that should be used for synchronization.
  private String currentAclChangeToken;

  // The next Change Token Value that should be used. We need to maintain the
  // next Change Token value because:
  // Say for a given Change Token CT1, connector makes a WS call at time T1
  // and gets the next Change Token CT2
  // The data returned by the WS was partial and not complete. To get the
  // remaining data, connector will make another WS call without changing the
  // change token value.
  // The second WS call which is being made at time T2 may return a
  // Change Token CT3 (> CT2) because there were some more changes at
  // SharePoint between T1 and T2.
  // Now, for the third WS call that connector will make in future, it has to
  // decide whether to use CT2 or CT3.
  // In the current logic, it's CT2 because connector relies on an ordering
  // which is based on document IDs. if the resulting data window varies after
  // every WS call and adds more documents with different IDs, the ordering
  // will also change. It will become hard to maintain and rely on the
  // ordering approach than.
  private String nextAclChangeToken;

  // for determining the crawl behavior of the web
  private WebCrawlInfo webCrawlInfo;

  /**
   * For the sole purpose of loading WebState nodes as WebState objects when
   * state file is loaded in-memory.
   */
  private WebState(String webId, String webURL, String title,
      String lastCrawledAt, DateTime insertionTime, SPType spType)
      throws SharepointException {
    if (null == webId || null == webURL || null == spType) {
      throw new SharepointException("webID [ " + webId + "] / webUrl [ "
          + webURL + "] / spType [ " + spType + " ] can not be null. ");
    }
    this.webId = webId;
    this.webUrl = webURL;
    this.title = title;
    this.lastCrawledDateTime = lastCrawledAt;
    this.insertionTime = insertionTime;
    this.spType = spType;
  }

  /**
   * @param spContext
   * @param spURL
   * @throws SharepointException
   */
  WebState(final SharepointClientContext spContext, final String spURL)
      throws SharepointException {
    LOGGER.config("Creating Web State for [ " + spURL + " ]");
    spType = spContext.checkSharePointType(spURL);
    if (null == spType) {
      LOGGER.log(Level.WARNING, "Unknown SharePoint version [ " + spType
          + " ] URL [ " + spURL + " ]. WebState creation failed. ");
      spContext.logExcludedURL("[ " + spURL + " ] Unknown SharePoint version. ");
      throw new SharepointException("Unknown SharePoint version.");
    }

    webId = webUrl = spURL;
    spContext.setSiteURL(webUrl);
    final WebsWS websWS = new WebsWS(spContext);
    title = websWS.getWebTitle(webUrl, spType);
    if (FeedType.CONTENT_FEED == spContext.getFeedType()
        && SPType.SP2003 == spType) {
      LOGGER.warning("excluding "
          + spURL
          + " because it is a SP2003 site and the feedType being used is content. Content feed is not supported on SP2003. ");
      spContext.logExcludedURL("[ "
          + spURL
          + " ] it is a SP2003 site and the feedType being used is content. Content feed is not supported on SP2003. ");
      throw new SharepointException(
          "Unsupported Shrepoint version for content feed being used. ");
    }
  }

  /**
   * Get the primary key.
   *
   * @return primary key
   */
  public String getPrimaryKey() {
    return webId;
  }

  /**
   * Sets the primary key.
   *
   * @param newKey
   */
  public void setPrimaryKey(final String newKey) {
    if (newKey != null) {
      webId = newKey;
    }
  }

  /**
   * Checks of the Web is existing or deleted
   */
  public boolean isExisting() {
    return exists;
  }

  /**
   * Marks the Web as existing/non-existing
   */
  public void setExisting(final boolean existing) {
    exists = existing;
    if (exists == false) {
      // for each ListState, set "not existing" as the Webstate is not
      // existing
      final Iterator<ListState> it = allListStateSet.iterator();
      while (it.hasNext()) {
        final ListState list = (ListState) it.next();
        list.setExisting(false);
      }
    }
  }

  /**
   * This is the recommended method for adding a new ListsState entry into the
   * WebState Or, updating any such attribute of a ListState which can affect
   * the ordering of ListStates in current WebState. TODO: Currently,
   * lastModified is identified as the only such attribute. In future, this
   * method can be augmented with more generic informations which drives the
   * ordering, instead of stricting this to just LastModifiedDate
   *
   * @param state
   * @param time lastMod time for the List. If time is later than the existing
   *          lastMod, the List is reindexed in the allListStateSet.
   */
  public void AddOrUpdateListStateInWebState(final ListState state,
      final DateTime time) {
    if (state != null) {
      final ListState stateOld = keyMap.get(state.getPrimaryKey());
      if (stateOld != null) {
        if (stateOld.getLastMod().compareTo(time) != 0) { // if new time
          // differs
          allListStateSet.remove(state);
          state.setLastMod(time);
        }
      } else {
        state.setLastMod(time);
        keyMap.put(state.getPrimaryKey(), state);
      }
      allListStateSet.add(state);
    }
  }

  /**
   * Compares this WebState to another (for the Comparable interface).
   * Comparison is first on the insertion date. If that produces a tie, the
   * primary key (the WebID) is used as tie-breaker. The comparison is flipped
   * to achieve descending ordering based on insertionTime
   *
   * @param o other WebState. If null, returns 1.
   * @return the usual integer result: -1 if other object is less than current,
   *         1 if other is greater than current, 0 if equal (which should only
   *         happen for the identity comparison).
   */
  public int compareTo(final StatefulObject o) {
    if (equals(o)) {
      return 0;
    }
    final WebState other = (WebState) o;
    if (other == null) {
      return 1; // anything is greater than null
    }
    if ((insertionTime != null) && (other.insertionTime != null)) {
      // Flipping the way comparison is being done in order to achieve
      // descending ordering of webstates. The
      // TreeSet.descendingIterator() is in JDK 1.6
      final int insertComparison = other.insertionTime.compareTo(this.insertionTime);
      return insertComparison;
    }
    return webId.compareTo(other.webId);
  }

  /**
   * For Web State equality comparison
   */
  public boolean equals(final Object obj) {
    if ((null != obj) && (obj instanceof WebState)) {
      final WebState tmpWeb = (WebState) obj;
      if ((null != webId) && (null != tmpWeb)
          && webId.equals(tmpWeb.getPrimaryKey())) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return the web URL
   */
  public String getWebUrl() {
    return webUrl;
  }

  /**
   * @param inWebUrl
   */
  public void setWebUrl(final String inWebUrl) {
    webUrl = inWebUrl;
  }

  /**
   * @return set of all list statesin this web
   */
  public TreeSet<ListState> getAllListStateSet() {
    return allListStateSet;
  }

  /**
   * @param inAllListStateSet
   */
  public void setAllListStateSet(final TreeSet<ListState> inAllListStateSet) {
    allListStateSet = inAllListStateSet;
  }

  /**
   * Signals that the recrawl cycle is over and any non-existing ListState can
   * be deleted
   *
   * @param spContext
   */
  public void endRecrawl(final SharepointClientContext spContext) {
    final Iterator<ListState> iter = getIterator();
    if (null != iter) {
      while (iter.hasNext()) {
        final ListState list = (ListState) iter.next();
        if (!list.isExisting()) {
          int responseCode = 0;
          try {
            responseCode = spContext.checkConnectivity(Util.encodeURL(list.getListURL()), null);
          } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Connectivity failed! ", e);
          }
          LOGGER.log(Level.CONFIG, "responseCode:" + responseCode);
          if (responseCode == 200) {
            LOGGER.log(Level.CONFIG, "Marking the list as Existing as an HTTP response 200 is received for listURL ["
                + list.getListURL() + "]");
            list.setExisting(true);
            continue;
          } else if (responseCode != 404) {
            LOGGER.log(Level.CONFIG, "The list can not be considered as deleted because an HTTP response other then 404 is received for listURL ["
                + list.getListURL() + "]");
            continue;
          }

          if (FeedType.CONTENT_FEED == spContext.getFeedType()) {
            // Need to send delete feeds for all the documents that
            // were inside this list. Not required for alerts.
            final List<SPDocument> deletedDocs = new ArrayList<SPDocument>();
            final int biggestID = list.getBiggestID();

            // start from 1 because 0 is not a valid itemID.
            // SharePoint starts allocating ID from 1.
            int maxID = 1;
            // One may think of starting from the largest ID in the
            // lists's delete cache. But, that is not safe. The
            // biggest ID might not be the one that has been sent as
            // deleted feed after the list's deletion.

            LOGGER.log(Level.INFO, "List [ " + list.getListURL()
                + " ] has been deleted. Using BiggestID [ " + biggestID
                + " ] and to construct delete feeds.");
            while ((maxID <= biggestID)
                && (deletedDocs.size() < spContext.getBatchHint())) {
              if (list.isInDeleteCache(new Integer(maxID).toString())) {
                if (LOGGER.isLoggable(Level.FINER)) {
                  LOGGER.log(Level.FINER, "Skipping sending delete feed for document with ID : "
                      + maxID
                      + " under list : "
                      + list.getListURL()
                      + ". A delete feed has been sent in some earlier batch traversal.");
                }
                ++maxID;
                continue;
              }
              String docID = list.getListURL() + SPConstants.DOC_TOKEN
                  + Integer.toString(maxID);
              final SPDocument doc = new SPDocument(docID, list.getListURL(),
                  list.getLastModCal(), SPConstants.NO_AUTHOR,
                  SPConstants.OBJTYPE_LIST_ITEM,
                  list.getParentWebState().getTitle(), FeedType.CONTENT_FEED,
                  SPType.SP2007);
              doc.setAction(ActionType.DELETE);
              deletedDocs.add(doc);

              // If this list can contain attachments, assume that
              // each item had attachments and send delete feed
              // for them.
              if (list.canContainAttachments()) {
                final List<String> attachments = list.getAttachmntURLsFor(Util.getOriginalDocId(docID, FeedType.CONTENT_FEED));
                final String originalDocID = docID;
                for (String attchmnt_url : attachments) {
                  docID = SPConstants.ATTACHMENT_SUFFIX_IN_DOCID + "["
                      + attchmnt_url + "]" + originalDocID;
                  final SPDocument attchmnt = new SPDocument(docID,
                      list.getListURL(), list.getLastModCal(),
                      SPConstants.NO_AUTHOR, SPConstants.OBJTYPE_ATTACHMENT,
                      list.getParentWebState().getTitle(),
                      FeedType.CONTENT_FEED, SPType.SP2007);
                  attchmnt.setAction(ActionType.DELETE);
                  deletedDocs.add(attchmnt);
                }
              }
              ++maxID;
            }

            // If we have sent the complete delete feeds, send one
            // more delete feed corresponding to the list itself.
            // This is not required for alerts. For alerts, just
            // ensure that no more documents are to be sent.
            if (SPConstants.ALERTS_TYPE.equals(list.getType())
                && deletedDocs.size() == 0 && list.isCrawlQueueEmpty()) {
              LOGGER.log(Level.INFO, "Deleting Alerts List for list/library ["
                  + list.getListURL() + "]. ");
              iter.remove();
              keyMap.remove(list.getPrimaryKey());
            } else if (maxID >= biggestID) {
              String docID = null;
              if (!list.isSiteDefaultPage()) {
                docID = list.getListURL() + SPConstants.DOC_TOKEN
                    + list.getPrimaryKey();
                ;
              } else {
                docID = list.getLastDocProcessed().getDocId();
              }
              final SPDocument doc = new SPDocument(docID, list.getListURL(),
                  list.getLastModCal(), SPConstants.NO_AUTHOR,
                  SPConstants.OBJTYPE_LIST_ITEM,
                  list.getParentWebState().getTitle(), FeedType.CONTENT_FEED,
                  SPType.SP2007);
              doc.setAction(ActionType.DELETE);
              if (!list.isSendListAsDocument() || !isCrawlAspxPages()) {
                // send the listState as a feed only if it was
                // included (not excluded) in the URL pattern
                // matching
                // The other case is SharePoint admin has set a
                // flag at site level to exclude ASPX pages from
                // being crawled and indexed and hence no need
                // send DELETE feed for List
                if (list.isSiteDefaultPage()) {
                  doc.setToBeFed(true);
                } else {
                  doc.setToBeFed(false);
                }
                LOGGER.log(Level.FINE, "List Document marked as not to be fed because ASPX pages are not supposed to be crawled as per exclusion patterns OR SharePoint site level indexing options ");
              }
              deletedDocs.add(doc);
            }
            // We must always sort the documents inside a
            // SPDocumentList object. This is important for
            // nextDoc() and checkPoint() logic
            Collections.sort(deletedDocs);
            list.setCrawlQueue(deletedDocs);
            // Do not remove the list at this point of time. This
            // will be removed after handleCrawlQueue will be called
            // for this.
            LOGGER.info("Sending #" + deletedDocs.size()
                + " documents to delete from the deleted List/Library [ "
                + list.getListURL() + " ].");
          } else {
            LOGGER.log(Level.INFO, "Deleting the state information for list/library ["
                + list.getListURL() + "]. ");
            iter.remove();
            keyMap.remove(list.getPrimaryKey());
          }
        }
      }
    }
  }

  /**
   * @param inlist
   */
  public void removeListStateFromKeyMap(final ListState inlist) {
    keyMap.remove(inlist.getPrimaryKey());
  }

  /**
   * @param inlist
   */
  public void removeListStateFromSet(final ListState inlist) {
    allListStateSet.remove(inlist);
  }

  /**
   * @return the the iterator for the list contained in this web
   */
  public Iterator<ListState> getIterator() {
    return allListStateSet.iterator();
  }

  /**
   * Lookup a ListState by its key.
   *
   * @param key primary key
   * @return object handle, or null if none found
   */
  public ListState lookupList(final String key) {
    final ListState ls = keyMap.get(key);
    return ls;
  }

  /**
   * @return the circular iterator for the set of lists. This iterator will
   *         start iterating for the current web.
   */
  public Iterator<ListState> getCurrentListstateIterator() {
    final ListState start = getCurrentList();
    if (start == null) {
      return getIterator();
    }
    // Return the correct subset to be used for traversing. Start from the
    // current list
    final ArrayList<ListState> full = new ArrayList<ListState>(
        allListStateSet.tailSet(start));

    return full.iterator();
  }

  /**
   * @return the current {@link ListState}
   */
  private ListState getCurrentList() {
    return currentList;
  }

  /**
   * @param currentObj
   */
  public void setCurrentList(final ListState currentObj) {
    currentList = currentObj;
  }

  /**
   * @return the last crawled list reference
   */
  public ListState getLastCrawledList() {
    return lastCrawledList;
  }

  /**
   * @param inLastCrawledList
   */
  public void setLastCrawledList(final ListState inLastCrawledList) {
    lastCrawledList = inLastCrawledList;
  }

  /**
   * @return the insertion time of the web. This is the ime when this web has
   *         been discovered and added into the state
   */
  public DateTime getInsertionTime() {
    return insertionTime;
  }

  /**
   * @param inInsertionTime
   */
  public void setInsertionTime(final DateTime inInsertionTime) {
    insertionTime = inInsertionTime;
  }

  /**
   * @return the insertion time as string
   */
  public String getInsertionTimeString() {
    return Util.formatDate(insertionTime);
  }

  /**
   * @return the web title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title
   */
  public void setTitle(final String title) {
    this.title = title;
  }

  /**
   * @return the sharepoint version for this web
   */
  public SPType getSharePointType() {
    return spType;
  }

  public void setSharePointType(SPType spType) {
    this.spType = spType;
  }

  /**
   * @return the lastCrawledDateTime
   */
  public String getLastCrawledDateTime() {
    return lastCrawledDateTime;
  }

  /**
   * @param lastCrawledDateTime the lastCrawledDateTime to set
   */
  public void setLastCrawledDateTime(String lastCrawledDateTime) {
    this.lastCrawledDateTime = lastCrawledDateTime;
  }

  @Override
  public String toString() {
    return this.webUrl;
  }

  public void dumpStateToXML(ContentHandler handler, FeedType feedType)
      throws SAXException {
    AttributesImpl atts = new AttributesImpl();

    atts.clear();
    atts.addAttribute("", "", SPConstants.STATE_ID, SPConstants.STATE_ATTR_ID, getPrimaryKey());
    atts.addAttribute("", "", SPConstants.STATE_URL, SPConstants.STATE_ATTR_CDATA, getWebUrl());
    atts.addAttribute("", "", SPConstants.LAST_CRAWLED_DATETIME, SPConstants.STATE_ATTR_CDATA, getLastCrawledDateTime());
    atts.addAttribute("", "", SPConstants.STATE_WEB_TITLE, SPConstants.STATE_ATTR_CDATA, getTitle());
    atts.addAttribute("", "", SPConstants.STATE_SPTYPE, SPConstants.STATE_ATTR_CDATA, getSharePointType().toString());

    atts.addAttribute("", "", SPConstants.STATE_ACLNEXTCHANGETOKEN, SPConstants.STATE_ATTR_CDATA, getNextAclChangeToken());
    atts.addAttribute("", "", SPConstants.STATE_ACLCHANGETOKEN, SPConstants.STATE_ATTR_CDATA, getCurrentAclChangeToken());

    final String strInsertionTime = getInsertionTimeString();
    if (strInsertionTime != null) {
      atts.addAttribute("", "", SPConstants.STATE_INSERT_TIME, SPConstants.STATE_ATTR_CDATA, strInsertionTime);
    }

    atts.addAttribute("", "", SPConstants.STATE_NOCRAWL, SPConstants.STATE_ATTR_CDATA, String.valueOf(isNoCrawl()));
    atts.addAttribute("", "", SPConstants.STATE_CRAWLASPXPAGES, SPConstants.STATE_ATTR_CDATA, String.valueOf(isCrawlAspxPages()));

    handler.startElement("", "", SPConstants.WEB_STATE, atts);

    // dump the actual ListStates:
    // Dump the "NoCrawl" flag for liststates irrespective of whether the
    // site is set to index no content. The main reason is to cater
    // use-cases where the content is indexed first, then admin sets the
    // configuration to not index and then decides to re-index. We dont want
    // the content to be re-crawled from start but from the point where it
    // had stopped. Having the liststates persisted to state file will
    // ensure the same
    if (null != allListStateSet || !allListStateSet.isEmpty()) {
      for (ListState list : allListStateSet) {
        list.dumpStateToXML(handler, feedType);
      }
    }

    handler.endElement("", "", SPConstants.WEB_STATE);
  }

  /**
   * Construct and returns a WebState object using the attributes.
   *
   * @param web
   * @param atts
   * @param feedType
   * @return
   */
  public static WebState loadStateFromXML(Attributes atts)
      throws SharepointException {
    WebState web = new WebState(atts.getValue(SPConstants.STATE_ID),
        atts.getValue(SPConstants.STATE_URL),
        atts.getValue(SPConstants.STATE_WEB_TITLE),
        atts.getValue(SPConstants.LAST_CRAWLED_DATETIME), null,
        SPType.getSPType(atts.getValue(SPConstants.STATE_SPTYPE)));
    try {
      String insertTime = atts.getValue(SPConstants.STATE_INSERT_TIME);
      web.setInsertionTime(Util.parseDate(insertTime));
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Could not load insertion time for web-state [ "
          + web.getPrimaryKey() + " ]. ");
    }

    web.setNextAclChangeToken(atts.getValue(SPConstants.STATE_ACLNEXTCHANGETOKEN));
    web.setCurretAclChangeToken(atts.getValue(SPConstants.STATE_ACLCHANGETOKEN));

    WebCrawlInfo webCrawlInfo = new WebCrawlInfo();
    webCrawlInfo.setNoCrawl(Boolean.getBoolean(atts.getValue(SPConstants.STATE_NOCRAWL)));
    webCrawlInfo.setCrawlAspxPages(Boolean.getBoolean(atts.getValue(SPConstants.STATE_CRAWLASPXPAGES)));
    web.setWebCrawlInfo(webCrawlInfo);

    return web;
  }

  private String getCurrentAclChangeToken() {
    return currentAclChangeToken;
  }

  private void setCurretAclChangeToken(String aclChangeToken) {
    this.currentAclChangeToken = aclChangeToken;
  }

  public String getNextAclChangeToken() {
    return nextAclChangeToken;
  }

  public void setNextAclChangeToken(String aclChangeToken) {
    this.nextAclChangeToken = aclChangeToken;
  }

  public String getAclChangeTokenForWsCall() {
    return getCurrentAclChangeToken();
  }

  /**
   * The saved nextChangetoken will be committed as the current change token to
   * be used in the future WS calls.
   */
  public void commitAclChangeToken() {
    LOGGER.log(Level.CONFIG, "Before Commit... currentAclChangeToken [ "
        + currentAclChangeToken + " ], nextAclChangeToken [ "
        + nextAclChangeToken + " ] ");
    this.currentAclChangeToken = nextAclChangeToken;
    nextAclChangeToken = null;
  }

  /**
   * Resets the state of all the children Lists to initiate a complete re-crawl
   */
  public void resetState() {
    for (ListState liststate : allListStateSet) {
      liststate.resetState();
    }
  }

  /**
   * Looks-Up a ListState for a given GUID value as received by the custom web
   * service
   *
   * @param listGuid
   * @return
   */
  public ListState getListStateForGuid(String listGuid) {
    // It has been observed that the GUID values are always returned in
    // upper case by SP web services. But, the same is not the case with
    // COM API. This is the most probabilistic case.
    ListState listState = lookupList("{" + listGuid.toUpperCase() + "}");
    if (null == listState) {
      listState = lookupList("{" + listGuid + "}");
    }
    if (null == listState) {
      // It has been observed that the GUID values as returned by
      // SP web services are inside {}. But, the same is not the case with
      // COM API.
      listState = lookupList("{" + listGuid + "}");
    }
    return listState;
  }

  public boolean isCrawlAspxPages() {
    if (webCrawlInfo != null) {
      return webCrawlInfo.isCrawlAspxPages();
    }
    return true;
  }

  public boolean isNoCrawl() {
    if (webCrawlInfo != null) {
      return webCrawlInfo.isNoCrawl();
    }

    return false;
  }

  public void setWebCrawlInfo(WebCrawlInfo webCrawlInfo) {
    this.webCrawlInfo = webCrawlInfo;
  }

}
