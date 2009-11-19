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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.WebsWS;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

/**
 * Represents a SharePoint Web Site as a stateful object
 *
 * @author nitendra_thakur
 */
public class WebState implements StatefulObject {
    private String webUrl = null;
    private String webId = null;
    private String title = "No Title";
    private DateTime lastMod = null; // lastMod has no meaning for webs.
    private DateTime insertionTime = null;
    private boolean exists = true; // By default mark all web state as exisitng
    // when they are created.
    private TreeSet<ListState> allListStateSet = new TreeSet<ListState>();
    private final Map<String, ListState> keyMap = new HashMap<String, ListState>();
    private final Collator collator = Util.getCollator();
    private static final Logger LOGGER = Logger.getLogger(WebState.class.getName());

    /**
     * The "current" object for ListState. The current object may be null.
     */
    private ListState currentList = null;
    private String lastCrawledListID = null;

    private String spType;
    private String feedType;

    /**
     * The timestamp of when was the site crawled last time by the connector
     */
    private String lastCrawledDateTime;

    /**
     * @param inFeedType
     */
    public WebState(final String inFeedType) {
        feedType = inFeedType;
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
        if (!SPConstants.SP2003.equalsIgnoreCase(spType)
                && !SPConstants.SP2007.equalsIgnoreCase(spType)) {
            LOGGER.log(Level.WARNING, "Unknown SharePoint version [ " + spType
                    + " ] URL [ " + spURL + " ]. WebState creation failed. ");
            spContext.logExcludedURL("[ " + spURL
                    + " ] Unknown SharePoint version [ " + spType + " ]. ");
            throw new SharepointException("Unknown SharePoint version.");
        }

        webId = webUrl = spURL;
        spContext.setSiteURL(webUrl);
        final WebsWS websWS = new WebsWS(spContext);
        title = websWS.getWebTitle(webUrl, spType);
        feedType = spContext.getFeedType();
        if (SPConstants.CONTENT_FEED.equalsIgnoreCase(feedType)
                && SPConstants.SP2003.equalsIgnoreCase(spType)) {
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
     * Get the lastMod time.
     *
     * @return time the Web was last modified
     */
    public DateTime getLastMod() {
        return lastMod;
    }

    /**
     * Set the lastMod time.
     *
     * @param inLastMod time
     */
    public void setLastMod(final DateTime inLastMod) {
        lastMod = inLastMod;
    }

    /**
     * Return lastMod in String form.
     *
     * @return lastMod string-ified
     */
    public String getLastModString() {
        return Util.formatDate(lastMod);
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
            final Iterator it = allListStateSet.iterator();
            while (it.hasNext()) {
                final ListState list = (ListState) it.next();
                list.setExisting(false);
            }
        }
    }

    /**
     * Reload this WebState from a DOM tree. The opposite of dumpToDOM().
     *
     * @param element the DOM element
     * @throws SharepointException if the DOM tree is not a valid representation
     *             of a ListState
     */
    public void loadFromDOM(final Element element) throws SharepointException {
        if (element == null) {
            throw new SharepointException("element not found");
        }

        webId = element.getAttribute(SPConstants.STATE_ID);
        if ((webId == null) || (webId.length() == 0)) {
            throw new SharepointException(
                    "Invalid XML: no id attribute for the WebState");
        }

        webUrl = element.getAttribute(SPConstants.STATE_URL);
        title = element.getAttribute(SPConstants.STATE_WEB_TITLE);
        try {
            final String insertTime = element.getAttribute(SPConstants.STATE_INSERT_TIME);
            insertionTime = Util.parseDate(insertTime);
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Unable to get insertion time for web state [ "
                    + title + " ]. ", e);
        }

        String lstCrawleddateTime = element.getAttribute(SPConstants.LAST_CRAWLED_DATETIME);
        if (lstCrawleddateTime != null) {
            setLastCrawledDateTime(lstCrawleddateTime);
        }

        spType = element.getAttribute(SPConstants.STATE_SPTYPE);

        final NodeList children = element.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
                final Node node = children.item(i);

                if ((node == null) || (node.getNodeType() != Node.ELEMENT_NODE)) {
                    continue;
                }
                final Element childElement = (Element) children.item(i);

                if (childElement == null) {
                    continue;
                }

                if (!collator.equals(childElement.getTagName(), SPConstants.LIST_STATE)) {
                    continue; // no exception; ignore xml for things we don't
                    // understand
                }
                final ListState subObject = new ListState(spType, feedType);
                subObject.loadFromDOM(childElement);
                updateList(subObject, subObject.getLastMod());

                // now, for "lastCrawledListID", for which so far we've only the
                // key, find the
                // actual object:

                if (lastCrawledListID != null) {
                    currentList = keyMap.get(lastCrawledListID);
                }
            }
        }
    }

    /**
     * For a single StatefulObject, update the two data structures (url -> obj
     * and time -> obj) and mark it "Existing" (if bFullReCrawl is true).
     *
     * @param state
     * @param time lastMod time for the List. If time is later than the existing
     *            lastMod, the List is reindexed in the allListStateSet.
     */
    public void updateList(final ListState state, final DateTime time) {
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
     * dump the Web state into state file
     */
    public Node dumpToDOM(final Document domDoc) throws SharepointException {
        final Element element = domDoc.createElement(SPConstants.WEB_STATE);
        if (webId != null) {
            element.setAttribute(SPConstants.STATE_ID, webId);
        }
        if (webUrl != null) {
            element.setAttribute(SPConstants.STATE_URL, webUrl);
        }

        if (getLastCrawledDateTime() != null) {
            element.setAttribute(SPConstants.LAST_CRAWLED_DATETIME, getLastCrawledDateTime());
        }

        if (title != null) {
            element.setAttribute(SPConstants.STATE_WEB_TITLE, title);
        }
        final String strInsertionTime = getInsertionTimeString();
        if (strInsertionTime != null) {
            element.setAttribute(SPConstants.STATE_INSERT_TIME, strInsertionTime);
        }

        if (spType != null) {
            element.setAttribute(SPConstants.STATE_SPTYPE, spType);
        }

        if (allListStateSet != null) {
            // dump the actual ListStates:
            final Iterator it = allListStateSet.iterator();
            while (it.hasNext()) {
                final StatefulObject obj = (StatefulObject) it.next();
                element.appendChild(obj.dumpToDOM(domDoc));
            }
        }
        return element;
    }

    /**
     * Compares this WebState to another (for the Comparable interface).
     * Comparison is first on the insertion date. If that produces a tie, the
     * primary key (the WebID) is used as tie-breaker. The comparison is flipped
     * to achieve descending ordering based on insertionTime
     *
     * @param o other WebState. If null, returns 1.
     * @return the usual integer result: -1 if other object is less than
     *         current, 1 if other is greater than current, 0 if equal (which
     *         should only happen for the identity comparison).
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
     * For Web Satate equality comparison
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
    public TreeSet getAllListStateSet() {
        return allListStateSet;
    }

    /**
     * @param inAllListStateSet
     */
    public void setAllListStateSet(final TreeSet<ListState> inAllListStateSet) {
        allListStateSet = inAllListStateSet;
    }

    /**
     * Factory method for ListState.
     *
     * @param key the "primary key" of the object. This would probably be the
     *            GUID.
     * @param inLastMod most recent time this object was modified.
     * @return new {@link ListState} which is already indexed in WebState's
     *         allListStateSet and keyMap
     */
    public ListState makeListState(final String key, final DateTime inLastMod)
            throws SharepointException {
        LOGGER.config("Creating List State: List key[" + key
                + "], lastmodified[" + inLastMod + "]");
        if (key != null) {
            final ListState obj = new ListState(spType, feedType);
            obj.setLastMod(inLastMod);
            obj.setPrimaryKey(key);
            updateList(obj, inLastMod); // add to our maps
            return obj;
        } else {
            LOGGER.warning("Unable to make ListState due to list key not found");
            throw new SharepointException(
                    "Unable to make ListState due to list key not found");
        }
    }

    /**
     * Convenience factory for clients who don't deal in Joda time.
     *
     * @param key the "primary key" of the object. This would probably be the
     *            GUID.
     * @param lastModCal (Calendar, not Joda time)
     * @return new {@link ListState} which is already indexed in WebState's
     *         allListStateSet and keyMap
     */
    public ListState makeListState(final String key, final Calendar lastModCal)
            throws SharepointException {
        return this.makeListState(key, Util.calendarToJoda(lastModCal));
    }

    /**
     * Signals that the recrawl cycle is over and any non-exisitng ListState can
     * be deleted
     *
     * @param spContext
     */
    public void endRecrawl(final SharepointClientContext spContext) {
        final Iterator iter = getIterator();
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

                    if (SPConstants.CONTENT_FEED.equalsIgnoreCase(spContext.getFeedType())) {
                        // Need to send delete feeds for all the documents that
                        // were inside this list. Not required for alerts.
                        final List<SPDocument> deletedDocs = new ArrayList<SPDocument>();
                        final int biggestID = list.getBiggestID();
                        int maxID = 1; // start from 1 because 0 is not a valid
                        // itemID. SharePoint starts allocating
                        // ID from 1.

                        // If we have sent come delete feeds in previous cycle,
                        // start from the next ID. Use LastDoc for this.
                        if ((list.getLastDocument() != null)
                                && ActionType.DELETE.equals(list.getLastDocument().getAction())) {
                            try {
                                maxID = Integer.parseInt(Util.getOriginalDocId(list.getLastDocument().getDocId(), SPConstants.CONTENT_FEED));
                            } catch (final Exception e) {
                                /*
                                 * If the list is deleted and the lastDoc
                                 * crawled is the list itself then it means that
                                 * we have not sent any delete feeds yet. Hence,
                                 * start from 0. Remember that for deleted list,
                                 * lastCrawledDoc is never set to the list
                                 * itself.
                                 */
                            }
                        }
                        LOGGER.log(Level.INFO, "List [ " + list.getListURL()
                                + " ] has been deleted. Using BiggestID [ "
                                + biggestID + " ] to construct delete feeds.");
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
                            String docID = list.getListURL()
                                    + SPConstants.DOC_TOKEN
                                    + Integer.toString(maxID);
                            final SPDocument doc = new SPDocument(docID,
                                    list.getListURL(), list.getLastModCal(),
                                    SPConstants.NO_AUTHOR,
                                    SPConstants.OBJTYPE_LIST_ITEM,
                                    list.getParentWebTitle(),
                                    SPConstants.CONTENT_FEED,
                                    SPConstants.SP2007);
                            doc.setAction(ActionType.DELETE);
                            deletedDocs.add(doc);

                            // If this list can contain attachments, assume that
                            // each item had attachments and send delete feed
                            // for them.
                            if (list.canContainAttachments()) {
								final List<String> attachments = list.getAttachmntURLsFor(Util.getOriginalDocId(docID, SPConstants.CONTENT_FEED));
                                final String originalDocID = docID;
								for (String attchmnt_url : attachments) {
                                    docID = SPConstants.ATTACHMENT_SUFFIX_IN_DOCID
                                            + "["
                                            + attchmnt_url
                                            + "]"
                                            + originalDocID;
                                    final SPDocument attchmnt = new SPDocument(
                                            docID, list.getListURL(),
                                            list.getLastModCal(),
                                            SPConstants.NO_AUTHOR,
											SPConstants.OBJTYPE_ATTACHMENT,
                                            list.getParentWebTitle(),
                                            SPConstants.CONTENT_FEED,
                                            SPConstants.SP2007);
                                    attchmnt.setAction(ActionType.DELETE);
                                    deletedDocs.add(attchmnt);
                                }
                            }
                            ++maxID;
                        }

                        // If we have sent the complete delete feeds, send one
                        // more delete feed corresponding to the list itself.
                        // This is not required for alerts.
                        if (!SPConstants.ALERTS_TYPE.equals(list.getType())
                                && (maxID >= biggestID)) {
                            final SPDocument doc = new SPDocument(
                                    list.getListURL() + SPConstants.DOC_TOKEN
                                            + list.getPrimaryKey(),
                                    list.getListURL(), list.getLastModCal(),
                                    SPConstants.NO_AUTHOR,
                                    SPConstants.OBJTYPE_LIST_ITEM,
                                    list.getParentWebTitle(),
                                    SPConstants.CONTENT_FEED,
                                    SPConstants.SP2007);
                            doc.setAction(ActionType.DELETE);
                            deletedDocs.add(doc);
                        }
						Collections.sort(deletedDocs);
                        list.setCrawlQueue(deletedDocs);
                        // Do not remove the list at this point of time. This
                        // will be removed after handleCrawlQueue will be called
                        // for this.
                        LOGGER.info("Sending #"
                                + deletedDocs.size()
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
    public Iterator getIterator() {
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
    public Iterator getCurrentListstateIterator() {
        final ListState start = getCurrentList();
        if (start == null) {
            return getIterator();
        }
        // one might think you could just do tail.addAll(head) here. But you
        // can't.
        final ArrayList<ListState> full = new ArrayList<ListState>(
                allListStateSet.tailSet(start));
        // full.addAll(allListStateSet.headSet(start));
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
     * @return the last crawled list ID
     */
    public String getLastCrawledListID() {
        if ((lastCrawledListID == null)
                || (lastCrawledListID.trim().equals(""))) {
            return null;
        }
        return lastCrawledListID;
    }

    /**
     * @param inLastCrawledListID
     */
    public void setLastCrawledListID(final String inLastCrawledListID) {
        lastCrawledListID = inLastCrawledListID;
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
    public String getSharePointType() {
        return spType;
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
}
