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

package com.google.enterprise.connector.sharepoint.spiimpl;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

/**
 * An implementation of DocumentList Class to represents a list of SPDocuments
 * that are to be sent to GSA. This class holds all the things that should be
 * taken care of 1. when a document is actually sent to CM 2. when checkpoint is
 * received
 *
 * @author nitendra_thakur
 */
public class SPDocumentList implements DocumentList {

    private final Logger LOGGER = Logger.getLogger(SPDocumentList.class.getName());
    private List<SPDocument> documents;
    private GlobalState globalState;// this is required for checkpointing
    private boolean bFQDNConversion = false;// FQDN conversion flag

    private Map<String, String> aliasMap = null;
    // Holds the index position of the doc last sent to CM
    private int docsFedIndexPosition = 0;

    /**
     * @param inDocuments List of {@link SPDocument} to be sent to GSA
     * @param inGlobalState The current snapshot of {@link GlobalState}
     */
    public SPDocumentList(final List<SPDocument> inDocuments,
            final GlobalState inGlobalState) {
        if (inDocuments != null) {
            documents = inDocuments;
        }

        globalState = inGlobalState;
    }

    /**
     * @return FQDN conversion value
     */
    public boolean isFQDNConversion() {
        return bFQDNConversion;
    }

    /**
     * @param conversion
     */
    public void setFQDNConversion(final boolean conversion) {
        bFQDNConversion = conversion;
    }

    /**
     * @param list2
     * @return status as boolean value
     */
    public boolean addAll(final SPDocumentList list2) {
        if (list2 == null) {
            return false;
        }
        return documents.addAll(list2.documents);
    }

    /**
     * @param doc
     * @return status as boolean value
     */
    public boolean add(final SPDocument doc) {
        if (doc == null) {
            return false;
        }
        return documents.add(doc);
    }

    /**
     * The processing that are done when the document is actually sent to CM.
     * Site Alias mapping defined during connector configuration are used at
     * this point only.
     * <p>
     * <ul>
     * <li>Updates the index position of the document in the document list
     * maintained here</li>
     * <li>Sets the current document as the last document sent from its parent
     * list represented by the ListState.</li>
     * <li>Applies alias mapping rules for ADD action only. For DELETE action no
     * alias mapping is done as the docId never contains the aliased URL</li>
     * <li>Does not remove the document from the crawl queue of the list</li>
     * </ul>
     * </p>
     */
    public Document nextDocument() throws SkippedDocumentException {
        SPDocument spDocument = null;

        // find the next document in the list which is to be fed
        do {
            if (docsFedIndexPosition >= documents.size()) {
                LOGGER.log(Level.FINE, "docsFedIndexPosition reached beyond document list size [ "
                        + documents.size() + " ] discontinuing loop");
                return null;
            }
            spDocument = (SPDocument) documents.get(docsFedIndexPosition);
            // this will save the state of the last document returned
            if (spDocument == null) {
                LOGGER.log(Level.SEVERE, "No document found! ");
                return null;
            }

            docsFedIndexPosition++;

            if (!spDocument.isToBeFed()) {
                LOGGER.log(Level.FINE, "Document skipped from feed because it is excluded: "
                        + spDocument.getUrl());
            }

            if (null == spDocument.getParentList()) {
                String message = "Parent List for the document is not found. docURL [ "
                        + spDocument.getUrl();

                LOGGER.log(Level.WARNING, message);
                throw new SkippedDocumentException("Document skipped. "
                        + message);
            }

        } while (!spDocument.isToBeFed());

        // for deleted documents, no need to use alias mapping. Only DocID
        // is sufficient.
        // This is because only documentURL and displayURL have the aliased
        // URLs and not the list URL included in the docId.
        if (ActionType.DELETE.equals(spDocument.getAction())) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, "Sending DocID [ "
                        + spDocument.getDocId() + " ] to CM for DELETE");
            }
        } else if (ActionType.ADD.equals(spDocument.getAction())) {
            // Do Alias mapping before sending the doc
            doAliasMapping(spDocument);

            LOGGER.log(Level.INFO, "Sending DocID [ " + spDocument.getDocId()
                    + " ], docURL [ " + spDocument.getUrl()
                    + " ] to CM for ADD.");

        }

        return spDocument;
    }

    /**
     * Marks a pointer in the state info maintained by the connector and saves
     * it to the disk.
     * <p>
     * <ul>
     * <li>Processes the list to which the document belong to update the change
     * token value and removes the doc from the crawl queue</li>
     * <li>Saves the current in-memory state to the disk</li>
     * </ul>
     * </p>
     */
    public String checkpoint() throws RepositoryException {
        for (int i = 0; i < docsFedIndexPosition; i++) {
            // Process the liststate and its crawl queue for the given doc which
            // has been sent to CM and fed to GSA successfully
            processListStateforCheckPoint(documents.get(i));
        }
        doCheckPoint();
        if (LOGGER.isLoggable(Level.CONFIG)) {
            LOGGER.log(Level.CONFIG, "checkpoint processed; saving GlobalState to disk.");
        }
        globalState.saveState(); // snapshot it all to disk

        return SPConstants.CHECKPOINT_VALUE;
    }

    /**
     * adding methods to get the count of the documents in the list.
     *
     * @return no. of documents in the list
     */
    public int size() {
        if (documents == null) {
            return 0;
        } else {
            return documents.size();
        }

    }

    /**
     * @param inAliasMap
     */
    public void setAliasMap(final Map<String, String> inAliasMap) {
        if (inAliasMap != null) {
            aliasMap = inAliasMap;
        }
    }

    /**
     * @return Site Alias Map
     */
    public Map<String, String> getAliasMap() {
        return aliasMap;
    }

    /**
     * Re-writes the current document's URL in respect to the Alias mapping
     * specified.
     *
     * @param spDocument The document whose documentURL and displayURL need to
     *            be set with aliased URL
     */
    private void doAliasMapping(SPDocument spDocument) {
        if ((null == spDocument) || (null == spDocument.getUrl())) {
            return;
        }
        final String url = spDocument.getUrl();
        URL objURL = null;
        try {
            objURL = new URL(url);
        } catch (final MalformedURLException e) {
            LOGGER.log(Level.WARNING, "Malformed URL!", e);
        }
        String strUrl = "";
        if (objURL == null) {
            return;
        }

        boolean matched = false;
        // processing of alias values
        if ((null != aliasMap) && (null != aliasMap.keySet())) {
            for (final Iterator<String> aliasItr = aliasMap.keySet().iterator(); aliasItr.hasNext();) {

                String aliasPattern = (String) aliasItr.next();
                String aliasValue = (String) aliasMap.get(aliasPattern);

                if ((aliasPattern == null) || (aliasValue == null)) {
                    continue;
                }
                aliasPattern = aliasPattern.trim();
                aliasValue = aliasValue.trim();
                if (aliasPattern.equalsIgnoreCase("")
                        || aliasValue.equalsIgnoreCase("")) {
                    continue;
                }

                URL patternURL = null;
                String aliasPatternURL = aliasPattern;
                if (aliasPattern.startsWith(SPConstants.GLOBAL_ALIAS_IDENTIFIER)) {
                    aliasPatternURL = aliasPattern.substring(1);
                }

                try {
                    patternURL = new URL(aliasPatternURL);
                } catch (final MalformedURLException e) {
                    LOGGER.log(Level.WARNING, "Malformed alias pattern: "
                            + aliasPatternURL, e);
                }
                if (patternURL == null) {
                    continue;
                }

                if (!objURL.getProtocol().equalsIgnoreCase(patternURL.getProtocol())) {
                    continue;
                }

                if (!objURL.getHost().equalsIgnoreCase(patternURL.getHost())) {
                    continue;
                }

                if (aliasPattern.startsWith(SPConstants.GLOBAL_ALIAS_IDENTIFIER)) {
                    aliasPattern = aliasPattern.substring(1);
                    if (patternURL.getPort() == SPConstants.MINUS_ONE) {
                        aliasPattern = patternURL.getProtocol()
                                + SPConstants.URL_SEP + patternURL.getHost();
                        if (objURL.getPort() != SPConstants.MINUS_ONE) {
                            aliasPattern += SPConstants.COLON
                                    + objURL.getPort();
                        }
                        aliasPattern += patternURL.getFile();
                    }
                } else if ((objURL.getPort() == SPConstants.MINUS_ONE)
                        && (patternURL.getPort() == patternURL.getDefaultPort())) {
                    aliasPattern = patternURL.getProtocol()
                            + SPConstants.URL_SEP + patternURL.getHost()
                            + patternURL.getFile();
                } else if ((objURL.getPort() == objURL.getDefaultPort())
                        && (patternURL.getPort() == SPConstants.MINUS_ONE)) {
                    aliasPattern = patternURL.getProtocol()
                            + SPConstants.URL_SEP + patternURL.getHost()
                            + SPConstants.COLON + patternURL.getDefaultPort()
                            + patternURL.getFile();
                } else if (objURL.getPort() != patternURL.getPort()) {
                    continue;
                }

                if (url.startsWith(aliasPattern)) {
                    LOGGER.config("document url[" + url
                            + "] has matched against alias source URL [ "
                            + aliasPattern + " ]");
                    strUrl = aliasValue;
                    final String restURL = url.substring(aliasPattern.length());
                    if (!strUrl.endsWith(SPConstants.SLASH)
                            && !restURL.startsWith(SPConstants.SLASH)) {
                        strUrl += SPConstants.SLASH;
                    }
                    strUrl += restURL;
                    matched = true;
                    LOGGER.config("document url[" + url
                            + "] has been re-written to [ " + strUrl
                            + " ] in respect to the aliasing.");
                    break;
                }
            }
        }

        if (!matched) {
            strUrl = objURL.getProtocol() + SPConstants.URL_SEP;
            strUrl += getFQDNHostName(objURL.getHost()) + SPConstants.COLON;
            final int portNo = objURL.getPort();
            if (portNo != SPConstants.MINUS_ONE) {
                strUrl += portNo;
            } else {
                strUrl += objURL.getDefaultPort();
            }
            strUrl += objURL.getFile();
        }

        spDocument.setUrl(strUrl);
    }

    /**
     * Converts a host name to a FQDN. This should be called only if the fqdn
     * property has been set to true in the connectorInstance.xml.
     *
     * @param hostName
     * @return the host name in FQDN format
     */
    private String getFQDNHostName(final String hostName) {
        if (isFQDNConversion()) {
            InetAddress ia = null;
            try {
                ia = InetAddress.getByName(hostName);
            } catch (final UnknownHostException e) {
                LOGGER.log(Level.WARNING, "Exception occurred while converting to FQDN, hostname [ "
                        + hostName + " ].", e);
            }
            if (ia != null) {
                return ia.getCanonicalHostName();
            }
        }
        return hostName;
    }

    /**
     * Processes and updates the liststate of the list for the given document
     * during checkpoint
     * <p>
     * <ul>
     * <li>In case action was DELETE
     * <ul>
     * <li>Remove the extra-id for the list</li>
     * <li>If the list exists, add the documentId to the delete cache ids</li>
     * <li>For a deleted list, it is removed from the in memory state of the
     * connector</li>
     * </ul>
     * </li>
     * <li>If the action was ADD mark the current doc as the doc from where the
     * connector should start traversing in next time</li>
     * <li>If the current document is the last document sent from its list, it
     * checks if any more docs are pending
     * </li>
     * <li>The document is removed from the list's crawl queue</li>
     * </ul>
     * </p>
     *
     * @param spDocument The document being processed. This document is the one
     *            from the list maintained here and not the one from crawlqueue
     *            of individual lists
     */
    private void processListStateforCheckPoint(SPDocument spDocument) {
        final ListState listState = spDocument.getParentList();
        final String currentID = Util.getOriginalDocId(spDocument.getDocId(), spDocument.getFeedType());

        // for deleted documents, make sure to remove the extraid from list
        // state and add it to delete cache ids as long as the list itself has
        // not been deleted. Else just remove the list
        // For ADD action, simply mark the document as the reference from where
        // to begin next incremental crawl
        if (ActionType.DELETE.equals(spDocument.getAction())) {
            // Remove ExtraIDs
            if (SPConstants.OBJTYPE_ATTACHMENT.equals(spDocument.getObjType())) {
                listState.removeAttachmntURLFor(currentID, spDocument.getUrl());
            } else {
                listState.removeExtraID(currentID);
            }

            boolean isCurrentDocForList = Util.getCollator().equals(listState.getPrimaryKey(), currentID);

            // A delete feed has being sent from a list
            // Add it to the delete cache so that same delete feed is
            // not reported
            // Check that the docId is not for the current list. The delete
            // cache IDs are for listitems and not for individual lists
            if (!isCurrentDocForList
                    && !SPConstants.OBJTYPE_ATTACHMENT.equals(spDocument.getObjType())) {
                listState.addToDeleteCache(currentID);
            }
            if (!listState.isExisting() && isCurrentDocForList) {
                // Last delete feed of a non-existent list has been sent
                // Since list are sent at last and the list is non-exisitng, we
                // can now delete this list state.
                if (LOGGER.isLoggable(Level.CONFIG)) {
                    LOGGER.log(Level.CONFIG, "Removing List State info List URL [ "
                            + listState.getListURL() + " ].");
                }

                if (spDocument.getParentWeb() != null) {
                    spDocument.getParentWeb().removeListStateFromKeyMap(listState);
                    spDocument.getParentWeb().removeListStateFromSet(listState);
                }
            }
        } else if (ActionType.ADD.equals(spDocument.getAction())) {
            // The pre-existing logic of checkpointing does not apply to the
            // documents which are being crawled because of any ACL change.
            // This is to keep the regular crawling separate from ACl based
            // crawling.
            if (!spDocument.isForAclChange()) {
                listState.setLastDocProcessedForWS(spDocument);

                // Update ExtraIDs
                if (FeedType.CONTENT_FEED == spDocument.getFeedType()) {
                    updateExtraIDs(listState, spDocument, currentID);
                }
            }
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "Document with DocID [ "
                    + spDocument.getDocId() + " ] from List URL [ "
                    + listState.getListURL()
                    + " ] is being removed from crawl queue");
        }
        // Remove the document from the crawl queue. No need to remove the
        // documents from DocumentList as the whole list is discarded by the CM
        // at the completion of this traversal.
        listState.removeDocFromCrawlQueue(spDocument);

        if (listState.isCrawlQueueEmpty()) {
            listState.commitAclCrawlStatus();
            if (null == listState.getNextPage()
                    && !listState.isNextChangeTokenBlank()) {
                // Since, all the docs are sent, commit the next suitable change
                // token value as current change token which gets used for the
                // subsequent WS calls.
                listState.commitChangeTokenForWSCall();
            }
        }
    }

    /**
     * All the logics which are governed by the document where checkpoint has
     * occurred. This includes two things: 1. Setting the lastList and
     * lastParent in globalstate 2. rolling back the change token and getting
     * back the original token depending on whether all the docs discovered are
     * also sent to CM.
     */
    private void doCheckPoint() {
        SPDocument spDocument = documents.get(docsFedIndexPosition - 1);
        if (LOGGER.isLoggable(Level.CONFIG)) {
            LOGGER.log(Level.CONFIG, "Checkpoint received at document docID [ "
                    + spDocument.getDocId() + " ], docURL [ "
                    + spDocument.getUrl() + " ], Action [ "
                    + spDocument.getAction() + " ]. ");
        }

        // Set the last crawled web and list states which can be used by the
        // batch traversal to know from where to look for pending documents
        // from last batch traversal in the crawl queue that are supposed to
        // be sent to GSA
        globalState.setLastCrawledWeb(spDocument.getParentWeb());
        globalState.setLastCrawledList(spDocument.getParentList());
    }

    private void updateExtraIDs(ListState listState, SPDocument spDocument,
            String currentID) {
        if (SPConstants.OBJTYPE_ATTACHMENT.equals(spDocument.getObjType())) {
            final List<String> knownAttachments = listState.getAttachmntURLsFor(currentID);
            if (!knownAttachments.contains(spDocument.getUrl())) {
                try {
                    listState.updateExtraIDAsAttachment(currentID, spDocument.getUrl());
                } catch (SharepointException se) {
                    LOGGER.log(Level.WARNING, "Problem while updating Attachemenst as ExtraIDs.. "
                            + "AttachmentURL [ "
                            + spDocument.getUrl()
                            + " ], currentID [ "
                            + currentID
                            + " ] listURL [ "
                            + listState.getListURL() + " ]. ", se.getMessage());
                }
            }
        } else if (null != spDocument.getFileref()) {
            try {
                listState.updateExtraIDs(spDocument.getFileref(), currentID, false);
            } catch (SharepointException se) {
                LOGGER.log(Level.WARNING, "Problem while updating ExtraIDs.. relativeURL [ "
                        + spDocument.getFileref()
                        + " ], currentID [ "
                        + currentID
                        + " ] listURL [ "
                        + listState.getListURL()
                        + " ]. ", se.getMessage());
            }
        }
    }

    public List<SPDocument> getDocuments() {
        return documents;
    }
}