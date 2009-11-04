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
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
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
    public Document nextDocument() {
        SPDocument spDocument = null;
        ListState listStateTemp;

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

            listStateTemp = globalState.lookupList(spDocument.getWebid(), spDocument.getListGuid());

            // Set the current doc as the last doc sent to CM. This will mark
            // the current doc and its position in the crawlQueue. This info
            // will be important when checkPoint() is called.
            listStateTemp.setLastDocument(spDocument);

        } while (!spDocument.isToBeFed());

        final ListState listState = listStateTemp;

        // for deleted documents, no need to use alias mapping. Only DocID
        // is sufficient.
        // This is because only documentURL and displayURL have the aliased
        // URLs and not the list URL included in the docId.
        if (ActionType.DELETE.equals(spDocument.getAction())) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, "Sending DocID [ "
                        + spDocument.getDocId() + " ] from List URL [ "
                        + listState.getListURL() + " ] to CM for DELETE");
            }
        } else if (ActionType.ADD.equals(spDocument.getAction())) {
            // Do Alias mapping before sending the doc
            doAliasMapping(spDocument);

            LOGGER.log(Level.INFO, "Sending DocID [ " + spDocument.getDocId()
                    + " ], docURL [ " + spDocument.getUrl()
                    + " ] to CM for ADD.");

        }

        // Set the last crawled web and list states which can be used by the
        // batch traversal to know from where to look for pending documents
        // from last batch traversal in the crawl queue that are supposed to
        // be sent to GSA
        globalState.setLastCrawledWebID(spDocument.getWebid());
        globalState.setLastCrawledListID(spDocument.getListGuid());

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

        SPDocument spDoc = documents.get(docsFedIndexPosition - 1);
        if (LOGGER.isLoggable(Level.CONFIG)) {
            LOGGER.log(Level.CONFIG, "Checkpoint received at document docID [ "
                    + spDoc.getDocId() + " ], docURL [ " + spDoc.getUrl()
                    + " ], Action [ " + spDoc.getAction() + " ]. ");
        }

        // FIXME: Delete this once Issue 85 fix is merged
        globalState.setLastCrawledWebID(spDoc.getWebid());
        globalState.setLastCrawledListID(spDoc.getListGuid());

        for (int i = 0; i < docsFedIndexPosition; i++) {
            // Process the liststate and its crawl queue for the given doc which
            // has been sent to CM and fed to GSA successfully
            processListStateforCheckPoint(documents.get(i));
        }

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
     * <ul>
     * <li>In case docs are pending, the change token in rolled back</li>
     * <li>In case all docs are done, change token is updated to the latest
     * value</li>
     * </ul>
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
        final ListState listState = globalState.lookupList(spDocument.getWebid(), spDocument.getListGuid());
        final String currentID = Util.getOriginalDocId(spDocument.getDocId(), spDocument.getFeedType());

        // for deleted documents, make sure to remove the extraid from list
        // state and add it to delete cache ids as long as the list itself has
        // not been deleted. Else just remove the list
        // For ADD action, simply mark the document as the reference from where
        // to begin next incremental crawl
        if (ActionType.DELETE.equals(spDocument.getAction())) {
            listState.removeExtraID(currentID);

            boolean isCurrentDocForList = Util.getCollator().equals(listState.getPrimaryKey(), currentID);

            // A delete feed has being sent from a list
            // Add it to the delete cache so that same delete feed is
            // not reported
            // Check that the docId is not for the current list. The delete
            // cache IDs are for listitems and not for individual lists
            if (!isCurrentDocForList) {
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

                final WebState parentWeb = globalState.lookupWeb(spDocument.getWebid(), null);
                if (parentWeb != null) {
                    parentWeb.removeListStateFromKeyMap(listState);
                    parentWeb.removeListStateFromSet(listState);
                }
            }

        } else if (ActionType.ADD.equals(spDocument.getAction())) {
            listState.setLastDocProcessedForWS(spDocument);
        }

        // The basic idea here is to rollback the change token in case there are
        // still docs pending to be pulled by CM or update to the latest one if
        // all docs have been sent. But this has to be done only
        // once. It cannot be done for every document as it might overwrite the
        // token with some invalid value and make the connector start to look
        // for any updates in future from some invalid state. So, the best way
        // to address this is to do this check only when the current document
        // from the document list is the last document sent from this liststate.
        // There cannot be the case where last document sent to CM from this
        // document list is after the last document sent for this list. If there
        // is such state, then something is wrong and is an invalid state.
        // Imp. Note: One might consider that what is the point in dealing with
        // change token if the list is deleted from web state since it is not
        // existing anymore. The check will get complicated when combined with
        // the check when the list exists and last document had ACTION=DELETE.
        // Best is to revert OR update the token irrespective of whether the
        // list exists or not.
        if (spDocument.equals(listState.getLastDocument())) {
            if (listState.allDocsFed()) {
                // The condition here ensures that we are done with all docs
                // from the crawlqueue of the list and hence need to update the
                // change token
                if (LOGGER.isLoggable(Level.CONFIG)) {
                    LOGGER.log(Level.CONFIG, "Setting the change token to its latest cached value. All the documents from the list's crawl queue is sent. listURL [ "
                            + listState.getListURL() + " ]. ");
                }
                listState.usingLatestToken();
                // Never clear the delete cache
                /*
                 * if (listState.getNextPage() == null) { if
                 * (LOGGER.isLoggable(Level.CONFIG)) { LOGGER.log(Level.CONFIG,
                 * "Cleaning delete cache..."); } listState.clearDeleteCache();
                 * }
                 */
            } else if ((listState.getNextPage() == null)
                    && (listState.getChangeToken() != null)) {
                // There are docs pending in this list, so roll back the change
                // token
                if (LOGGER.isLoggable(Level.CONFIG)) {
                    LOGGER.log(Level.CONFIG, "There are some docs left in the crawl queue of list [ "
                            + listState.getListURL()
                            + " ] at the time of checkpointing. rolling back the change token to its previous value.");
                }
                listState.rollbackToken();
            }
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "Document with DocID [ "
                    + spDocument.getDocId() + " ] from List URL [ "
                    + listState.getListURL()
                    + " ] is being removed from crawl queue");
        }
        // Remove the document from the crawl queue
        listState.removeDocFromCrawlQueue(spDocument);

    }

}