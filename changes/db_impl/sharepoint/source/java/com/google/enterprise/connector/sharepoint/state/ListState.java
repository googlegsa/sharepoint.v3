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

import com.google.enterprise.connector.sharepoint.client.Attribute;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import org.joda.time.DateTime;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a SharePoint List/Library as a stateful object
 *
 * @author nitendra_thakur
 */
public class ListState implements StatefulObject {
    protected String key = null;

    /**
     * Whether the underlying SharePoint object that this object was created to
     * represent actually exists. This variable is periodically set to false by
     * GlobalState, and then set true if the underlying object is found to be
     * still there.
     */
    // By default mark all list state as existing when they are created.
    private boolean exists = true;

    private String listTitle = "";
    private String listURL = "";

    /**
     * Flag to indicate if a this list is to be fed to GSA as a document or is
     * it to be ignored
     */
    private boolean sendListAsDocument;

    /**
     * The change token which is used for making web service call.
     */
    private String currentChangeToken;

    /**
     * The next change token value whihc should be used for subsequent web
     * service calls.
     */
    private String nextChangeToken;

    /**
     * To store all the extraIDs for which delete feed has been sent. This is
     * also kept in memory.
     */
    private Set<String> cachedDeletedIDs = new HashSet<String>();

    private static final Logger LOGGER = Logger.getLogger(ListState.class.getName());

    /**
     * This doc is the last doc sent to CM and successfully fed to GSA with
     * action=ADD. This is required during incremental crawl to tell the WS from
     * where to look for more docs
     */
    SPDocument lastDocProcessed;

    /**
     * an ordered list of SPDocuments due to be fed to the Connector Manager
     */
    private List<SPDocument> crawlQueue = null;

    private final Collator collator = Util.getCollator();

    private String type;
    private Calendar lastMod;
    private String baseTemplate;
    private ArrayList<Attribute> attrs = new ArrayList<Attribute>();
    private String listConst = "/Lists";

    /**
     * To keep track of those IDs which can not be discovered by making any web
     * service call. For Example, it is not possible to track the documents if
     * their parent folder is deleted.
     */
    private StringBuffer extraIDs = new StringBuffer();
    private StringBuffer attchmnts = new StringBuffer();
    /**
     * To keep track of the document with the biggest ID that has been
     * discovered from this list
     */
    private int biggestID = 0;
    /**
     * Helps to make web service call which returns the results page by page
     */
    private String nextPage = null;
    /**
     * Indicates list level change. Decides whether the list should be sent as a
     * separate doc.
     */
    private boolean isNewList;

    /**
     * The timestamp of when was the list crawled last time by the connector
     */
    private String lastCrawledDateTime;

    WebState parentWeb;

    // Does the ACl changed for this List?
    private boolean aclChanged;
    // LastDocId crawled due to any ACL change
    private int lastDocIdCrawledForAcl;
    // We need to cache the values of above two fields till the time all the
    // crawled documents are fed to GSA or at least there is no document to be
    // fed.
    private boolean tmp_aclChanged;
    private int tmp_lastDocIdCrawledForAcl;

    // To check if the list inherits permission from the parent web
    private boolean inheritedSecurity;

    // Flag to determine the crawl behavior of the list
    private boolean noCrawl;

    // Folders that are renamed/restored
    private List<Folder> changedFolders = new LinkedList<Folder>();

    /**
     * @param inInternalName
     * @param inTitle
     * @param inType
     * @param inLastMod
     * @param inBaseTemplate
     * @param inUrl
     * @throws SharepointException
     */
    public ListState(final String inPrimaryKey, final String inTitle,
            final String inType, final Calendar inLastMod,
            final String inBaseTemplate, final String inUrl,
            WebState inParentWeb)
            throws SharepointException {

        LOGGER.config("inInternalName[" + inPrimaryKey + "], inType["
                + inType + "], inLastMod[" + inLastMod + "], inBaseTemplate["
                + inBaseTemplate + "], inUrl[" + inUrl + "]");

        if (null == inPrimaryKey || null == inType) {
            throw new SharepointException("primary key [ " + inPrimaryKey
                    + " ] / type [ " + inType + "] can not be null.");
        }
        key = inPrimaryKey;
        listTitle = inTitle;
        type = inType;
        lastMod = inLastMod;
        baseTemplate = inBaseTemplate;
        listURL = inUrl;
        parentWeb = inParentWeb;
    }

    /**
     * @return list type (generic / document libraries)
     */
    public String getType() {
        return type;
    }

    /**
     * @return the base templete
     */
    public String getBaseTemplate() {
        return baseTemplate;
    }

    /**
     * @param inBaseTemplate
     */
    public void setBaseTemplate(final String inBaseTemplate) {
        if (inBaseTemplate != null) {
            baseTemplate = inBaseTemplate;
        }
    }

    /**
     * @return the list properties
     */
    public ArrayList<Attribute> getAttrs() {
        return attrs;
    }

    /**
     * @param attrs
     */
    public void setAttrs(final ArrayList<Attribute> attrs) {
        this.attrs = attrs;
    }

    /**
     * @param key
     * @param value
     */
    public void setAttribute(final String key, final String value) {
        if (attrs == null) {
            attrs = new ArrayList<Attribute>();
        }
        if (key != null) {
            attrs.add(new Attribute(key, value));
        }
    }

    /**
     * @param inUrl
     */
    public void setUrl(final String inUrl) {
        if (inUrl != null) {
            listURL = inUrl;
        }
    }

    /**
     * @return the list constant which is typically used for constructing the
     *         document URLs from the list URL
     */
    public String getListConst() {
        return listConst;
    }

    /**
     * @param inListConst
     */
    public void setListConst(final String inListConst) {
        if (inListConst != null) {
            listConst = inListConst;
        }
    }

    /**
     * @return the boolean value depicting if the list can contain folders
     */
    public boolean canContainFolders() {
        if (collator.equals(type, SPConstants.DOC_LIB)
                || collator.equals(type, SPConstants.GENERIC_LIST)
                || collator.equals(type, SPConstants.ISSUE)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the boolean value depicting if the list a Document library
     */
    public boolean isDocumentLibrary() {
        if (collator.equals(type, SPConstants.DOC_LIB)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the boolean value depicting if the items of this list contain
     *         attachments
     */
    public boolean canContainAttachments() {
        if (collator.equals(type, SPConstants.GENERIC_LIST)
                || collator.equals(type, SPConstants.ISSUE)
                || collator.equals(type, SPConstants.DISCUSSION_BOARD)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the boolean value depicting if the list can contain link sites
     */
    public boolean isLinkSite() {
        if ((collator.equals(baseTemplate, SPConstants.ORIGINAL_BT_LINKS))
                || (collator.equals(baseTemplate, SPConstants.BT_SITESLIST))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get the lastMod time.
     *
     * @return time the List was last modified
     */
    public DateTime getLastMod() {
        return Util.calendarToJoda(lastMod);
    }

    /**
     * Set the lastMod time.
     *
     * @param inLastMod time
     */
    public void setLastMod(final DateTime inLastMod) {
        lastMod = Util.jodaToCalendar(inLastMod);
    }

    /**
     * @return the l;ast modified date
     */
    public Calendar getLastModCal() {
        return lastMod;
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
        return key;
    }

    /**
     * Sets the primary key.
     *
     * @param newKey
     */
    public void setPrimaryKey(final String newKey) {
        // primary key cannot be null
        if (newKey != null) {
            key = newKey;
        }
    }

    /**
     * Checks if the list is still existing
     */
    public boolean isExisting() {
        return exists;
    }

    /**
     * mark the list as existing/non-existing
     */
    public void setExisting(boolean existing) {
        exists = existing;
    }

    /**
     * Compares this ListState to another (for the Comparable interface).
     * Comparison is first on the lastMod date. If that produces a tie, the
     * primary key (the GUID) is used as tie-breaker.
     *
     * @param o other ListState. If null, returns 1.
     * @return the usual integer result: -1 if this object is less, 1 if it's
     *         greater, 0 if equal (which should only happen for the identity
     *         comparison).
     */
    public int compareTo(final StatefulObject o) {
        ListState other = null;
        if (o instanceof ListState) {
            other = (ListState) o;
        }
        if ((other == null) || (other.lastMod == null)) {
            return 1; // anything is greater than null
        }
        if (lastMod == null) {
            return -1;
        }
        final int lastModComparison = lastMod.getTime().compareTo(other.lastMod.getTime());
        if (lastModComparison != 0) {
            return lastModComparison;
        } else {
            return key.compareTo(other.key);
        }
    }

    /**
     * For List equality comparison
     */
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        ListState other = null;
        if (obj instanceof ListState) {
            other = (ListState) obj;
            if (key.equals(other.getPrimaryKey())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the date which should be passed to Web Services (the
     * GetListItemChanges call of ListWS) in order to find new items for this
     * List. This should be called in preference to getLastDocCrawled() or any
     * other lower-level method, since this method can apply some intelligence,
     * e.g. looking at the crawl queue, and that intelligence may change over
     * time. Current algorithm: take the later of the date in
     * getLastDocCrawled() and the crawl queue, IF ANY. Otherwise, take the
     * lastMod of the List itself.
     *
     * @return Calendar suitable for passing to WebServices
     */
    public Calendar getDateForWSRefresh() {
        Calendar date = Util.jodaToCalendar(getLastMod()); // our default

        // Check if there was some doc that was last sent and use its last
        // modified date in case if it is later than that of the list
        if (getLastDocForWSRefresh() != null) {
            final Calendar lastCrawlQueueDate = getLastDocForWSRefresh().getLastMod();
            if (lastCrawlQueueDate.before(date)) {
                date = lastCrawlQueueDate;
            }
        }
        return date;
    }

    /**
     * Return the most suitable Document to start the crawl. Used in case of
     * SP2007. Ideally we should start from the lastDocProcessed. But if crawl
     * queue have pending documents to be sent to GSA and connector, before
     * sending them, starts discovering more documents from SharePoint, it
     * should pick the last document in crawl for discovery. Hence, this method
     * returns last document processed as last document to be used for
     * further WS calls only when crawl queue is empty. Otherwise, the last
     * document in crawl queue of ActionType ADD is returned. Deleted documents
     * have no significance while making WS calls.
     *
     * @return {@link SPDocument}
     */
    public SPDocument getLastDocForWSRefresh() {
        SPDocument lastDocFromCrawlQueue = getLastDocInCrawlQueueOfActionTypeADD();
        if (null == lastDocFromCrawlQueue) {
            return lastDocProcessed;
        } else if (lastDocFromCrawlQueue.compareTo(lastDocProcessed) > 0) {
            return lastDocFromCrawlQueue;
        } else {
            return lastDocProcessed;
        }
    }

    /**
     * Returns the last document in crawl queue which was sent as ADD feed. This
     * is the most suitable document for incremental crawl
     *
     * @return
     */
    private SPDocument getLastDocInCrawlQueueOfActionTypeADD() {
        if (null == crawlQueue || crawlQueue.size() == 0) {
            return null;
        } else {
            for (int i = crawlQueue.size() - 1; i >= 0; --i) {
                SPDocument lastDoc = crawlQueue.get(i);
                if (ActionType.ADD.equals(lastDoc.getAction())) {
                    return lastDoc;
                }
            }
        }
        return null;
    }

    /**
     * @return the crawl queue containg the documents from this list
     */
    public List<SPDocument> getCrawlQueue() {
        return crawlQueue;
    }

    /**
     * Debug routine: dump the crawl queue to stdout. (this is deliberately in
     * preference to log messages, since it's much easier to follow in Eclipse.)
     */
    public void dumpCrawlQueue() {
        if ((crawlQueue != null) && (crawlQueue.size() > 0)) {
            LOGGER.config("Crawl queue for " + getListURL());
            for (int iDoc = 0; iDoc < crawlQueue.size(); ++iDoc) {
                final SPDocument doc = (SPDocument) crawlQueue.get(iDoc);
                LOGGER.config(doc.getLastMod().getTime() + ", " + doc.getUrl());
                doc.dumpAllAttrs();
            }
        } else {
            LOGGER.config("Empty crawl queue for " + getListURL());
        }
    }

    /**
     * @param inCrawlQueue
     */
    public void setCrawlQueue(final List<SPDocument> inCrawlQueue) {
        crawlQueue = inCrawlQueue;
    }

    /**
     * @param doc : to be removed from the crawl queue
     */
    public void removeDocFromCrawlQueue(final SPDocument doc) {
        if (null == doc) {
            LOGGER.log(Level.WARNING, "Requested document for removal is null. ");
            return;
        }
        if (null == crawlQueue) {
            LOGGER.log(Level.WARNING, "Request for document removal is received when Crawl Queue is empty. docID [ "
                    + doc.getDocId() + " ], docURL [ " + doc.getUrl() + " ]. ");
            return;
        }
        boolean status = crawlQueue.remove(doc);
        LOGGER.log(Level.FINE, "Document Removed from Crawl Queue. docID [ "
                + doc.getDocId() + " ], docURL [ " + doc.getUrl()
                + " ], Action [ " + doc.getAction() + " ], deleteStatus [ "
                + status + " ], currentCrawlQueueSize [ " + crawlQueue.size()
                + " ]. ");
    }

    /**
     * @return the listURL
     */
    public String getListURL() {
        return listURL;
    }

    /**
     * @param listURL the listURL to set
     */
    public void setListURL(final String listURL) {
        this.listURL = listURL;
    }

    /**
     * @return the suitable changeToken value for making WS call.
     */
    public String getChangeTokenForWSCall() {
        return currentChangeToken;
    }

    /**
     * Must not be exposed to the clients. Never make it public. The appropriate
     * way of updating ChangeToken is to call
     * {@link ListState#commitChangeTokenForWSCall()}
     */
    private void setChangeTokenForWSCall(String inChangeToken) {
        currentChangeToken = inChangeToken;
    }

    /**
     * Return a change token value which can be safely committed as current
     * change token in {@link ListState#commitChangeTokenForWSCall()}
     *
     * @return the suitable changeToken value which can be committed as in
     *         {@link ListState#commitChangeTokenForWSCall()}
     */
    public String getNextChangeTokenForSubsequectWSCalls() {
        return nextChangeToken;
    }

    public boolean isNextChangeTokenBlank() {
        if (null == nextChangeToken || nextChangeToken.length() == 0) {
            return true;
        }
        return false;
    }

    public boolean isCurrentChangeTokenBlank() {
        if (null == currentChangeToken || currentChangeToken.length() == 0) {
            return true;
        }
        return false;
    }

    /**
     * Saves the nextChangeToken which will be committed as the
     * currentChangeToken for making WS call once all the documents discovered
     * using current change token will be fed to GSA. A saved nextChangeToken
     * value must be committed at some time in future before another value can
     * be saved.
     *
     * @param inChangeToken save it as the next change token to be used for WS
     *            calls. Note that, this value is not picked up during the WS
     *            call until the call to
     *            {@link ListState#commitChangeTokenForWSCall()}
     * @return true if the change token was saved successfully; false otherwise
     */
    public boolean saveNextChangeTokenForWSCall(final String inChangeToken) {
        if (inChangeToken == null || inChangeToken.equals(currentChangeToken)
        // An already saved token must first be committed
                || null != nextChangeToken) {
            return false;
        }
        nextChangeToken = inChangeToken;
        LOGGER.log(Level.CONFIG, "currentChangeToken [ " + currentChangeToken
                + " ], nextChangeToken [ " + nextChangeToken
                + " ]. ");
        return true;
    }

    /**
     * commits the internally saved nextChangeToken value as the
     * currentChangeToken and reset the next change token. If nextChangeToken is
     * null, the method returns without changing the value of
     * currentChangeToken.
     *
     * @return true if the change token was committed successfully; false
     *         otherwise
     */
    public boolean commitChangeTokenForWSCall() {
        if (null == nextChangeToken
                || nextChangeToken.equals(currentChangeToken)) {
            return false;
        }
        LOGGER.log(Level.CONFIG, "committing nextChangeToken [ "
                + nextChangeToken + " ] as currentChangetoken");
        currentChangeToken = nextChangeToken;
        nextChangeToken = null;
        this.changedFolders.clear();
        return true;
    }

    /**
     * @return the extraIDs
     */
    public StringBuffer getIDs() {
        return extraIDs;
    }

    /**
     * @param ds the extraIDs to set
     */
    public void setIDs(final StringBuffer ds) {
        extraIDs = ds;
    }

    /**
     * Used to store information about folders and items under them. This info
     * is stored in the form of #foldID~foldName#id1#id2/#foldID
     *
     * @param docPath the document path as returned by the web service in the
     *            value of relativeURL field
     * @param docID document ID
     * @param isFolder is the document a folder?
     */
    public void updateExtraIDs(final String docPath, final String docID,
            final boolean isFolder) throws SharepointException {
        if (!Util.isNumeric(docID)) {
            // This must be a list itself. And, we do not bother about list
            // here. We only need list items.
            return;
        }
        if (!canContainFolders()) {
            return;
        }
        if (docPath == null) {
            LOGGER.log(Level.WARNING, "docPath is null. Returning..");
            return;
        }

        int index = -1;
        String parentPath = null;
        String docTitle = null;

        index = docPath.indexOf(listConst);
        if (index == -1) {
            LOGGER.log(Level.WARNING, "The document path [ " + docPath
                    + " ] does not match its parent list listConst [ "
                    + listConst + " ], listURL [ " + listURL
                    + "]. returning...");
            return;
        }
        index += listConst.length();
        parentPath = docPath.substring(index);
        index = parentPath.lastIndexOf(SPConstants.SLASH);
        if (index == -1) {
            // this can be a top level folder. We need to update ExtraID, hence
            // make index 0.
            index = 0;
            docTitle = parentPath;
        } else {
            docTitle = parentPath.substring(index + 1);
        }

        int idPos = -1;

        final String idPattern = "\\#" + docID + "[\\#|\\~|/]";
        final Pattern pat = Pattern.compile(idPattern);
        final Matcher match = pat.matcher(extraIDs);
        if (match.find()) {
            idPos = match.start();
        }

        if (idPos != -1) {
            // We already know about this ID.
            if (isFolder) {
                int startPos = idPos + 1 + docID.length() + 1;
                int endPos = extraIDs.indexOf("#", startPos);
                int tmp_endPos = extraIDs.indexOf("/", startPos);
                if (tmp_endPos < endPos) {
                    endPos = tmp_endPos;
                }
                extraIDs.replace(startPos, endPos, docTitle);
                LOGGER.log(Level.INFO, "ExtraIDs updated for the folder "
                        + docTitle);
            }
            return;
        }

        parentPath = parentPath.substring(0, index);
        if ((parentPath == null) || parentPath.equals("")) {
            // Case of an item which is not inside any folder. If it is a
            // folder, update the complex string extraIDs. Otherwise, just
            // return, we don't need to store the outer document extraIDs.
            if (isFolder) {
                extraIDs.append("#" + docID + "~" + docTitle + "/#" + docID);
                LOGGER.log(Level.FINEST, "ExtraIDs updated for the folder "
                        + docTitle);
            } else {
                LOGGER.log(Level.FINE, "A top level document is received with docPath [ "
                        + docPath + " ]. ExtraID has not been updated.");
            }
            return;
        }

        // Reach up to the place where the ID is should be inserted. This place
        // will come after some folder entry e.g, #2~Folder__
        final StringTokenizer strTok = new StringTokenizer(parentPath,
                SPConstants.SLASH);
        if (strTok != null) {
            index = 0;
            while (strTok.hasMoreTokens()) {
                final String folder = strTok.nextToken();
                char chr = '#';
                do {
                    index = extraIDs.indexOf("~" + folder, index);
                    if (index == -1) {
                        LOGGER.log(Level.FINE, "A docID [ "
                                + docID
                                + " ] has been found whose parent folder ID is not known. listURL [ "
                                + listURL + " ]. returning...");
                        throw new SharepointException(
                                "extraIDs needs to be updated..");
                    }
                    index += 1 + folder.length();
                    chr = extraIDs.charAt(index);
                } while (chr != '#' && chr != '/');

            }
        }

        extraIDs.insert(index, "#" + docID);
        index += 1 + docID.length();
        if (isFolder) {
            extraIDs.insert(index, "~" + docTitle + "/#" + docID);
        }
        LOGGER.log(Level.FINEST, "ExtraIDs updated for the docID #" + docID
                + " List URL [ " + listURL + " ]. ");
    }

    /**
     * For a given ID, retrieves all the dependednt item extraIDs.
     *
     * @param docID
     * @return the set of dependent extraIDs
     */
    public Set<String> getExtraIDs(final String docID) {
        LOGGER.log(Level.FINEST, "Request for getting all dependent extraIDs for docID #"
                + docID + " ] is received. List URL [ " + listURL + " ]. ");
        final Set<String> depIds = new HashSet<String>();
        if (!Util.isNumeric(docID)) {
            // This must be a list itself. And, we do not bother about list
            // here. We only need list items.
            return depIds;
        }
        if (!canContainFolders() && Util.isNumeric(docID)) {
            depIds.add(docID);
            return depIds;
        }

        int startPos = -1;
        int endPos = -1;

        final String idPattern = "\\#" + docID + "[\\#|\\~|/]";
        Pattern pat = Pattern.compile(idPattern);
        Matcher match = pat.matcher(extraIDs);
        if (match.find()) {
            String idPart = match.group();
            startPos = match.start();
            if (idPart.endsWith("~")) {
                // Case of folder
                endPos = extraIDs.indexOf("/#" + docID);
                endPos += 2 + docID.length();
                idPart = extraIDs.substring(startPos, endPos);

                pat = Pattern.compile("\\#\\d+");
                match = pat.matcher(idPart);
                while (match.find()) {
                    String id = match.group();
                    startPos = match.start();
                    endPos = match.end();
                    if (((startPos > 0) && (idPart.charAt(startPos - 1) == SPConstants.SLASH_CHAR))
                            || ((endPos < idPart.length()) && (idPart.charAt(endPos) == '~'))) {
                        continue;
                        // This is a folder ID and we do not need send any feed
                        // for folders.
                    }
                    id = id.substring(1);
                    if (Util.isNumeric(id)) {
                        depIds.add(id);
                    }
                }
            } else if (Util.isNumeric(docID)) {
                // This is an item inside some folder.
                // Here we are checking certain conditions before adding the
                // original ID as to be sent as the delete feeds.
                // Because, this ID could be of a folder also, and we do not
                // send folder as docs.
                depIds.add(docID);
            }
        } else if (Util.isNumeric(docID)) {
            // This would be a top level doc. We do not make entries in extraIDs
            // for such docs.
            depIds.add(docID);
        }

        return depIds;
    }

    /**
     * Removes the specified ID form the local store
     *
     * @param docID
     */
    public void removeExtraID(final String docID) {
        LOGGER.log(Level.FINEST, "Request to delete docID #" + docID
                + " ] is received. List URL [ " + listURL + " ]. ");
        if (!Util.isNumeric(docID)) {
            // This must be a list itself. And, we do not bother about list
            // here. We only need list items.
            return;
        }
        if (!canContainFolders()) {
            return;
        }
        final String idPattern = "\\#" + docID + "[\\#|\\~|/]";
        final Pattern pat = Pattern.compile(idPattern);
        final Matcher match = pat.matcher(extraIDs);
        if (match.find()) {
            final String idPart = match.group();
            final int startPos = match.start();
            if (!idPart.endsWith("~")) {
                extraIDs.delete(startPos, startPos + 1 + docID.length());
            }
        }
    }

    /**
     * Returns all the stored extraIDs. Used for debugging purposes.
     *
     * @return
     */
    /*
     * public Set getAllExtraIDs() { Set idSet = new HashSet(); String idPattern
     * = "\\#\\d+[\\#|\\~|/]"; Pattern pat = Pattern.compile(idPattern); Matcher
     * match = pat.matcher(extraIDs); while(match.find()) { String idPart =
     * match.group(); Pattern pat2 = Pattern.compile("\\d+"); Matcher match2 =
     * pat2.matcher(idPart); if(match2.find()) { String id = match2.group();
     * if(Util.isNumeric(id)) { idSet.add(id); } } } return idSet; }
     */
    /**
     * This keeps track of the attachment URLs that have been sent for a for a
     * particular item ID. this info is stored as
     * #itemID|AttachURL1|AttachURL2....
     */
    public void updateExtraIDAsAttachment(final String itemID,
            final String attachmentURL) throws SharepointException {
        LOGGER.log(Level.FINEST, "Request to update attachment [ "
                + attachmentURL + " ] is received for item ID #" + itemID
                + ". List URL [ " + listURL + " ]. ");
        if (!canContainAttachments() || (attachmentURL == null)) {
            return;
        }
        final Pattern pat = Pattern.compile("\\#" + itemID + "\\|");
        Matcher match = pat.matcher(attachmentURL);
        if (match.find()) {
            LOGGER.log(Level.SEVERE, "attachmentURL [ "
                    + attachmentURL
                    + " matches the pattern #| which is a reserved pattern. returning..");
            return;
        }
        match = pat.matcher(attchmnts);
        if (match.find()) {
            final String newIdPart = "#" + itemID + "|" + attachmentURL + "|";
            attchmnts.replace(match.start(), match.end(), newIdPart);
        } else {
            attchmnts.append("#" + itemID + "|" + attachmentURL);
        }
    }

    /**
     * @param itemID
     * @return set of attachments as SPDocuemnts
     */
    public List<String> getAttachmntURLsFor(final String itemID) {
        LOGGER.log(Level.FINEST, "Request for getting all attachments for item ID #"
                + itemID + " is received. List URL [ " + listURL + " ]. ");
        final List<String> attachmnt_urls = new ArrayList<String>();
        if (!canContainAttachments()) {
            return attachmnt_urls;
        }

        final Pattern delimPat = Pattern.compile("\\#\\d+\\|");
        final Matcher delimMatch = delimPat.matcher(attchmnts);

        final Pattern pat = Pattern.compile("\\#" + itemID + "\\|");
        final Matcher match = pat.matcher(attchmnts);
        if (match.find()) {
            int startPos = -1;
            int endPos = -1;
            startPos = match.end();
            if (delimMatch.find(startPos)) {
                endPos = delimMatch.start();
            } else {
                endPos = attchmnts.length();
            }
            final String urlPart = attchmnts.substring(startPos, endPos);
            final StringTokenizer strTok = new StringTokenizer(urlPart, "|");
            while (strTok.hasMoreTokens()) {
                final String attchmnt = strTok.nextToken();
                if (Util.isURL(attchmnt)) {
                    attachmnt_urls.add(attchmnt);
                }
            }
        }
        return attachmnt_urls;
    }

    /**
     * @param itemID
     * @param attachmentURL
     * @return status of removal
     */
    public boolean removeAttachmntURLFor(final String itemID,
            final String attachmentURL) {
        LOGGER.log(Level.FINEST, "Request for deletion of attachment [ "
                + attachmentURL + " ] is received for item ID #" + itemID
                + ". List URL [ " + listURL + " ]. ");
        if (!canContainAttachments() || (attachmentURL == null)) {
            return false;
        }
        final Pattern pat = Pattern.compile("\\#" + itemID + "\\|.*?"
                + attachmentURL);
        final Matcher match = pat.matcher(attchmnts);
        if (match.find()) {
            final int endPos = match.end();
            final int startPos = endPos - attachmentURL.length();
            attchmnts.delete(startPos, endPos);
            return true;
        }
        return false;
    }

    /**
     * @return the biggestID
     */
    public int getBiggestID() {
        return biggestID;
    }

    /**
     * @param biggestID the biggestID to set
     */
    public void setBiggestID(final int biggestID) {
        this.biggestID = biggestID;
    }

    /**
     * Update the list only if the primary key matches. A list should not be
     * updated with any random list. Update the current ListState with useful
     * information from the coming list, which is the newly discovered version
     * of the list. These information, we do not write into the state file.
     */
    public void updateList(final ListState inList) {
        if (key.equals(inList.getPrimaryKey())) {
            attrs = inList.getAttrs();
            baseTemplate = inList.getBaseTemplate();
            listURL = inList.getListURL();
            type = inList.getType();
            listTitle = inList.getListTitle();
            listConst = inList.getListConst();
            sendListAsDocument = inList.isSendListAsDocument();
            inheritedSecurity = inList.isInheritedSecurity();
            noCrawl = inList.isNoCrawl();
        }
    }

    /**
     * @return the isNewList
     */
    public boolean isNewList() {
        return isNewList;
    }

    /**
     * @param isNewList the isNewList to set
     */
    public void setNewList(final boolean isNewList) {
        this.isNewList = isNewList;
    }

    /**
     * @return the nextPage
     */
    public String getNextPage() {
        return nextPage;
    }

    /**
     * @param nextPage the nextPage to set
     */
    public void setNextPage(final String nextPage) {
        this.nextPage = nextPage;
    }

    /**
     * Checks an itemID if it exists in the local delete cache
     *
     * @param deleteID
     */
    public boolean isInDeleteCache(final String deleteID) {
        if ((cachedDeletedIDs != null) && cachedDeletedIDs.contains(deleteID)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds an item ID into the local delete cache
     *
     * @param deleteID
     */
    public void addToDeleteCache(final String deleteID) {
        if ((cachedDeletedIDs != null) && Util.isNumeric(deleteID)) {
            cachedDeletedIDs.add(deleteID);
        }
    }

    /**
     * Removes an item ID from the locval delete cache
     *
     * @param deleteID
     */
    public void removeFromDeleteCache(final String deleteID) {
        if ((cachedDeletedIDs != null) && Util.isNumeric(deleteID)) {
            cachedDeletedIDs.remove(deleteID);
        }
    }

    /**
     * Clears the local deleted cache store
     */
    public void clearDeleteCache() {
        cachedDeletedIDs = new HashSet<String>();
    }

    /**
     * @return the parentWeb
     */
    public WebState getParentWebState() {
        return parentWeb;
    }

    /**
     * @return the listTitle
     */
    public String getListTitle() {
        return listTitle;
    }

    /**
     * @param type the type to set
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * @return the attchmnts
     */
    public StringBuffer getAttchmnts() {
        return attchmnts;
    }

    /**
     * @param attchmnts the attchmnts to set
     */
    public void setAttchmnts(StringBuffer attchmnts) {
        this.attchmnts = attchmnts;
    }

    /**
     * @return the sendListAsDocument
     */
    public boolean isSendListAsDocument() {
        return sendListAsDocument;
    }

    /**
     * @param sendListAsDocument the sendListAsDocument to set
     */
    public void setSendListAsDocument(boolean sendListAsDocument) {
        this.sendListAsDocument = sendListAsDocument;
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

    /**
     * @param lastDocProcessedForWS the lastDocProcessedForWS to set
     */
    public void setLastDocProcessed(SPDocument lastDocProcessed) {
        this.lastDocProcessed = lastDocProcessed;
    }

    public SPDocument getLastDocProcessed() {
        return lastDocProcessed;
    }

    @Override
    public String toString() {
        return this.listURL;
    }

    public Set<String> getDeleteCache() {
        return cachedDeletedIDs;
    }

    /**
     * dumps all the necessary information to a node in the state file
     *
     * @param handler
     * @param feedType
     * @throws SAXException
     */
    public void dumpStateToXML(ContentHandler handler, FeedType feedType)
            throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.clear();
        atts.addAttribute("", "", SPConstants.STATE_ID, SPConstants.STATE_ATTR_ID, getPrimaryKey());
        atts.addAttribute("", "", SPConstants.STATE_URL, SPConstants.STATE_ATTR_CDATA, getListURL());
        atts.addAttribute("", "", SPConstants.STATE_LASTMODIFIED, SPConstants.STATE_ATTR_CDATA, getLastModString());
        atts.addAttribute("", "", SPConstants.LAST_CRAWLED_DATETIME, SPConstants.STATE_ATTR_CDATA, getLastCrawledDateTime());
        atts.addAttribute("", "", SPConstants.STATE_TYPE, SPConstants.STATE_ATTR_CDATA, getType());
        atts.addAttribute("", "", SPConstants.STATE_ISACLCHANGED, SPConstants.STATE_ATTR_CDATA, String.valueOf(isAclChanged()));
        if (isAclChanged()) {
            atts.addAttribute("", "", SPConstants.STATE_LASTDOCIDCRAWLEDFORACL, SPConstants.STATE_ATTR_CDATA, String.valueOf(getLastDocIdCrawledForAcl()));
        }
        if (!SPConstants.ALERTS_TYPE.equalsIgnoreCase(getType())) {
            if (SPType.SP2007 == getParentWebState().getSharePointType()) {
                if (!isCurrentChangeTokenBlank()) {
                    atts.addAttribute("", "", SPConstants.STATE_CHANGETOKEN, SPConstants.STATE_ATTR_CDATA, getChangeTokenForWSCall());
                }
                if (!isNextChangeTokenBlank()) {
                    atts.addAttribute("", "", SPConstants.STATE_CACHED_CHANGETOKEN, SPConstants.STATE_ATTR_CDATA, getNextChangeTokenForSubsequectWSCalls());

                }

                if (FeedType.CONTENT_FEED == feedType) {
                    atts.addAttribute("", "", SPConstants.STATE_BIGGESTID, SPConstants.STATE_ATTR_CDATA, String.valueOf(getBiggestID()));

                    // We need to remember this so that duplicate delete feeds
                    // are not sent. This is critical because of a bug in GSA
                    // wherein duplicate delete feeds hangs the GSA indexer.
                    if (getDeleteCache() != null && getDeleteCache().size() > 0) {
                        StringBuffer deletedListItemIDs = new StringBuffer();
                        boolean firstElement = true;
                        for (String deletedID : getDeleteCache()) {
                            if (firstElement) {
                                deletedListItemIDs.append(deletedID);
                                firstElement = false;
                            } else {
                                deletedListItemIDs.append(SPConstants.HASH).append(deletedID);
                            }
                        }
                        atts.addAttribute("", "", SPConstants.STATE_DELETED_LIST_ITEMIDS, SPConstants.STATE_ATTR_CDATA, deletedListItemIDs.toString());
                    }
                }
            }
        }

        // Dump the "Nocrawl" flag
        atts.addAttribute("", "", SPConstants.STATE_NOCRAWL, SPConstants.STATE_ATTR_CDATA, String.valueOf(isNoCrawl()));

        handler.startElement("", "", SPConstants.LIST_STATE, atts);

        // Creating child nodes of ListState node
        if (SPConstants.ALERTS_TYPE.equalsIgnoreCase(type)) {
            if (getIDs() != null && getIDs().length() != 0) {
                atts.clear();
                handler.startElement("", "", SPConstants.STATE_EXTRAIDS_ALERTS, atts);
                handler.characters(getIDs().toString().toCharArray(), 0, getIDs().length());
                handler.endElement("", "", SPConstants.STATE_EXTRAIDS_ALERTS);
            }
        } else {
            if (SPType.SP2007 == getParentWebState().getSharePointType()) {
                if (FeedType.CONTENT_FEED == feedType) {
                    if (canContainFolders() && getIDs() != null
                            && getIDs().length() != 0) {
                        atts.clear();
                        handler.startElement("", "", SPConstants.STATE_EXTRAIDS_FOLDERS, atts);
                        handler.characters(getIDs().toString().toCharArray(), 0, getIDs().length());
                        handler.endElement("", "", SPConstants.STATE_EXTRAIDS_FOLDERS);
                    }

                    if (canContainAttachments() && getAttchmnts() != null
                            && getAttchmnts().length() != 0) {
                        atts.clear();
                        handler.startElement("", "", SPConstants.STATE_EXTRAIDS_ATTACHMENTS, atts);
                        handler.characters(getAttchmnts().toString().toCharArray(), 0, getAttchmnts().length());
                        handler.endElement("", "", SPConstants.STATE_EXTRAIDS_ATTACHMENTS);
                    }
                }
            }

            // dump the lastDocProcessed
            if (getLastDocProcessed() != null) {
                atts.clear();
                // ID and URL are mandatory field, used in
                // SpDocument.compareTo(). These attributes must be
                // preserved.
                atts.addAttribute("", "", SPConstants.STATE_ID, SPConstants.STATE_ATTR_ID, getLastDocProcessed().getDocId());
                atts.addAttribute("", "", SPConstants.STATE_URL, SPConstants.STATE_ATTR_CDATA, getLastDocProcessed().getUrl());

                if (SPType.SP2007 == getParentWebState().getSharePointType()) {
                    if (null != getLastDocProcessed().getRenamedFolder()) {
                        atts.addAttribute("", "", SPConstants.STATE_RENAMED_FOLDER_PATH, SPConstants.STATE_ATTR_CDATA, getLastDocProcessed().getRenamedFolder().getPath());
                        atts.addAttribute("", "", SPConstants.STATE_RENAMED_FOLDER_ID, SPConstants.STATE_ATTR_CDATA, getLastDocProcessed().getRenamedFolder().getId());
                    }
                    if (null != getLastDocProcessed().getParentFolder()) {
                        atts.addAttribute("", "", SPConstants.STATE_PARENT_FOLDER_PATH, SPConstants.STATE_ATTR_CDATA, getLastDocProcessed().getParentFolder().getPath());
                        atts.addAttribute("", "", SPConstants.STATE_PARENT_FOLDER_ID, SPConstants.STATE_ATTR_CDATA, getLastDocProcessed().getParentFolder().getId());
                    }
                    if (FeedType.CONTENT_FEED == feedType) {
                        atts.addAttribute("", "", SPConstants.STATE_ACTION, SPConstants.STATE_ATTR_CDATA, getLastDocProcessed().getAction().toString());
                    }
                }
                atts.addAttribute("", "", SPConstants.STATE_LASTMODIFIED, SPConstants.STATE_ATTR_CDATA, getLastDocProcessed().getLastDocLastModString());
                handler.startElement("", "", SPConstants.STATE_LASTDOCCRAWLED, atts);
                handler.endElement("", "", SPConstants.STATE_LASTDOCCRAWLED);
            }

            // Dump the renamed folder list that have been processed so far
            dumpRenamedFolderList(handler);
        }
        handler.endElement("", "", SPConstants.LIST_STATE);
    }

    /**
     * Dumps the list of renamed folders if any to the state file. These are the
     * list of folders which need to be processed on connector restart
     * <p>
     * Creates a node structure as under the list state node: <code>
     * &lt;RenamedFolderList&gt;
     * &lt;RenamedFolder ID="%FolderID%" Path="%FolderPath%"/&gt;
     * .
     * .
     * &lt;/RenamedFolderList&gt;
     * </code>
     * </p>
     *
     * @param handler
     * @throws SAXException
     */
    private void dumpRenamedFolderList(ContentHandler handler)
            throws SAXException {
        // Dump the RenamedFolderList if there are any folder renames
        if (changedFolders != null && !changedFolders.isEmpty()) {
            // Start of the RenamedFolderList node
            handler.startElement("", "", SPConstants.STATE_RENAMED_FOLDER_LIST, new AttributesImpl());

            for (Folder renamedFolder : changedFolders) {
                // Dump each folder as RenamedFolder node with id & path as
                // attributes
                AttributesImpl atts = new AttributesImpl();
                atts.addAttribute("", "", SPConstants.STATE_ID, SPConstants.STATE_ATTR_ID, renamedFolder.getId());
                atts.addAttribute("", "", SPConstants.STATE_RENAMED_FOLDERPATH, SPConstants.STATE_ATTR_CDATA, renamedFolder.getPath());
                handler.startElement("", "", SPConstants.STATE_RENAMED_FOLDER_NODE, atts);
                handler.endElement("", "", SPConstants.STATE_RENAMED_FOLDER_NODE);
            }

            handler.endElement("", "", SPConstants.STATE_RENAMED_FOLDER_LIST);
        }

    }

    /**
     * Creates a {@link Folder} object for each &lt;RenamedFolder&gt; node
     *
     * @param atts The list of attributes for the given path
     */
    public void loadRenamedFolderList(Attributes atts) {
        Folder renamedFolder = new Folder(
                atts.getValue(SPConstants.STATE_RENAMED_FOLDERPATH),
                atts.getValue(SPConstants.STATE_ID));
        changedFolders.add(renamedFolder);
    }

    /**
     * Construct and returns a ListState object using the attributes.
     *
     * @param web
     * @param atts
     * @param feedType
     * @return
     */
    public static ListState loadStateFromXML(WebState web, Attributes atts,
            FeedType feedType) throws SharepointException {
        ListState list = new ListState(atts.getValue(SPConstants.STATE_ID), "",
                atts.getValue(SPConstants.STATE_TYPE), null, "",
                atts.getValue(SPConstants.STATE_URL), web);
        try {
            final String lastModString = atts.getValue(SPConstants.STATE_LASTMODIFIED);
            list.setLastMod(Util.parseDate(lastModString));
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load Last Modified for list [ "
                    + list.getListURL() + " ]. ", e);
        }

        list.setLastCrawledDateTime(atts.getValue(SPConstants.LAST_CRAWLED_DATETIME));
        list.setAclChanged(Boolean.getBoolean(atts.getValue(SPConstants.STATE_ISACLCHANGED)));
        if (list.isAclChanged()) {
            try {
                list.setLastDocIdCrawledForAcl(Integer.getInteger(atts.getValue(SPConstants.STATE_LASTDOCIDCRAWLEDFORACL)));
            } catch (final Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to load LastDocIdCrawledForAcl. ", e);
            }
        }
        list.commitAclCrawlStatus();

        if (!SPConstants.ALERTS_TYPE.equalsIgnoreCase(list.getType())) {
            if (SPType.SP2007 == web.getSharePointType()) {
                list.setChangeTokenForWSCall(atts.getValue(SPConstants.STATE_CHANGETOKEN));
                list.saveNextChangeTokenForWSCall(atts.getValue(SPConstants.STATE_CACHED_CHANGETOKEN));

                if (FeedType.CONTENT_FEED == feedType) {
                    try {
                        list.setBiggestID(Integer.parseInt(atts.getValue(SPConstants.STATE_BIGGESTID)));
                    } catch (final Exception e) {
                        LOGGER.log(Level.WARNING, "Failed to load Biggest ID for list [ "
                                + list.getListURL() + " ]. ", e);
                    }

                    String deletedListItemsIDs = atts.getValue(SPConstants.STATE_DELETED_LIST_ITEMIDS);
                    if (deletedListItemsIDs != null
                            && !deletedListItemsIDs.equals("")) {

                        String[] deletedIds = deletedListItemsIDs.split(SPConstants.HASH);
                        Collections.addAll(list.getDeleteCache(), deletedIds);
                    }
                }
            }
        }

        list.setNoCrawl(Boolean.getBoolean(atts.getValue(SPConstants.STATE_NOCRAWL)));

        return list;
    }

    public boolean isCrawlQueueEmpty() {
        if (null == crawlQueue || crawlQueue.size() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Resets the state of this List to initiate a complete re-crawl
     */
    public void resetState() {
        currentChangeToken = nextChangeToken = null;
        setLastDocProcessed(null);
        setCrawlQueue(null);
        endAclCrawl();
    }

    public boolean isAclChanged() {
        return aclChanged;
    }


    private void setAclChanged(boolean aclChanged) {
        this.aclChanged = aclChanged;
    }

    private void setLastDocIdCrawledForAcl(int lastDocIdCrawledForAcl) {
        this.lastDocIdCrawledForAcl = lastDocIdCrawledForAcl;
    }

    public int getLastDocIdCrawledForAcl() {
        return lastDocIdCrawledForAcl;
    }

    /**
     * Marks a list for a candidate for ACL based crawling. All such lists are
     * queried for items with inheriting permissions during the crawl.
     */
    public void startAclCrawl() {
        if (isAclChanged()) {
            return;
        }
        aclChanged = tmp_aclChanged = true;
        lastDocIdCrawledForAcl = tmp_lastDocIdCrawledForAcl = 0;
    }

    /**
     * Updates the ACL crawl status of the list to reflect that the ACL based
     * crawling is finished for this list A call to this method will ensure that
     * the current ACL crawl of the list has been completed and the list can be
     * processed for the next set of ACL changes
     */
    public void endAclCrawl() {
        if (!isAclChanged()) {
            return;
        }
        aclChanged = tmp_aclChanged = false;
        lastDocIdCrawledForAcl = tmp_lastDocIdCrawledForAcl = 0;
    }

    public void updateAclCrawlStatus(boolean isMoreToCrawl, int lastDocIdCrawled) {
        this.tmp_aclChanged = isMoreToCrawl;
        this.tmp_lastDocIdCrawledForAcl = lastDocIdCrawled;
    }

    /**
     * Commits the updated info about ACL based crawling to be used latter.
     * {@link ListState#updateAclCrawlStatus(boolean, int)} along with this
     * method gives an scope to update the crawling status but not using it
     * until an specified time. These methods are helpful for the connectro
     * where crawling and feeding of documents are not one atomic operation.
     * <p/>
     * Note: The current usage of committing the ACL crawl status is idempotent
     * meaning, if i call this method consecutively more than once without
     * setting a new crawl status (i.e without calling
     * {@link ListState#updateAclCrawlStatus(boolean, int)} in between), it will
     * not have any unforseen effect on the ACL based crawling. Do take of this
     * if you make any change here
     */
    public void commitAclCrawlStatus() {
        aclChanged = tmp_aclChanged;
        lastDocIdCrawledForAcl = tmp_lastDocIdCrawledForAcl;
    }

    /**
     * Create a {@link SPDocument} corresponding this list and return the same
     *
     * @param feedType FeedType to be considered while creating the
     *            {@link SPDocument} object
     * @return {@link SPDocument}
     */
    public SPDocument getDocumentInstance(FeedType feedType) {
        String docId = getPrimaryKey();
        if (FeedType.CONTENT_FEED == feedType) {
            docId = getListURL() + SPConstants.DOC_TOKEN + getPrimaryKey();
        }
        final SPDocument listDoc = new SPDocument(docId, getListURL(),
                getLastModCal(), SPConstants.NO_AUTHOR, getBaseTemplate(),
                getParentWebState().getTitle(), feedType,
                getParentWebState().getSharePointType());

        listDoc.setAllAttributes(getAttrs());

        if (!isSendListAsDocument() || !getParentWebState().isCrawlAspxPages()) {
            // send the listState as a feed only if it was
            // included
            // (not excluded) in the URL pattern matching
            // The other case is SharePoint admin has set a
            // flag at site level to exclude ASPX pages from
            // being crawled and indexed and hence need to
            // honor the same
            listDoc.setToBeFed(false);
            LOGGER.log(Level.WARNING, "List Document marked as not to be fed because ASPX pages are not supposed to be crawled as per exclusion patterns OR SharePoint site level indexing options ");
            // TODO log it in excludedUrl.log
        }

        return listDoc;
    }

    public boolean isInheritedSecurity() {
        return inheritedSecurity;
    }

    public void setInheritedSecurity(boolean inheritedSecurity) {
        this.inheritedSecurity = inheritedSecurity;
    }

    public boolean isNoCrawl() {
        return noCrawl;
    }

    public void setNoCrawl(boolean noCrawl) {
        this.noCrawl = noCrawl;
    }

    public boolean isInfoPathLibrary() {
        return SPConstants.BT_FORMLIBRARY.equals(baseTemplate);
    }

    public List<Folder> getChangedFolders() {
        return changedFolders;
    }

    public void addToChangedFolders(Folder changedFolder) {
        this.changedFolders.add(changedFolder);
    }
}
