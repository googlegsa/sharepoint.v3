// Copyright 2012 Google Inc.
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

import com.google.common.base.Strings;
import com.google.enterprise.connector.sharepoint.client.ListsUtil;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.Folder;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.wsclient.client.BaseWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.ListsWS;
import com.google.enterprise.connector.sharepoint.wsclient.handlers.InvalidXmlCharacterHandler;
import com.google.enterprise.connector.sharepoint.wsclient.util.DateUtil;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import org.apache.axis.message.MessageElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeaderElement;

/**
 * Java Client for calling Lists.asmx Provides a layer to talk to the Lists Web
 * Service on the SharePoint server Any call to this Web Service must go through
 * this layer.
 *
 * @author nitendra_thakur
 */
public class ListsHelper {
  private static final Logger LOGGER = Logger.getLogger(ListsHelper.class.getName());
  private final SharepointClientContext sharepointClientContext;
  private final String rowLimit;
  private final ListsWS listsWS;

  /**
   * @param inSharepointClientContext The Context is passed so that necessary
   *          information can be used to create the instance of current class
   *          Web Service endpoint is set to the default SharePoint URL stored
   *          in SharePointClientContext.
   * @throws SharepointException
   */
  public ListsHelper(final SharepointClientContext inSharepointClientContext)
      throws SharepointException {
    this.sharepointClientContext = inSharepointClientContext;

    final int batchHint = inSharepointClientContext.getBatchHint();
    if (batchHint > 0) {
      rowLimit = "" + batchHint;
    } else {
      rowLimit = SPConstants.DEFAULT_ROWLIMIT;
    }
    LOGGER.log(Level.FINEST, "RowLimit set to: " + rowLimit);
    listsWS = sharepointClientContext.getClientFactory().getListsWS(
        sharepointClientContext, rowLimit);

    final String strDomain = inSharepointClientContext.getDomain();
    String strUser = inSharepointClientContext.getUsername();
    strUser = Util.getUserNameWithDomain(strUser, strDomain);
    final String strPassword = inSharepointClientContext.getPassword();
    final int timeout = sharepointClientContext.getWebServiceTimeOut();
    LOGGER.fine("Setting time-out to " + timeout + " milliseconds.");

    listsWS.setUsername(strUser);
    listsWS.setPassword(strPassword);
    listsWS.setTimeout(timeout);
  }

  /**
   * Sets the username used for webservice calls. This is for testing.
   *
   * @param username the user name to use
   */
  public void setUsername(final String username) {
    listsWS.setUsername(username);
  }
  
  /**
   * Gets all the attachments of a particular list item.
   *
   * @param baseList List to which the item belongs
   * @param listItem list item for which the attachments need to be retrieved.
   * @return list of sharepoint SPDocuments corresponding to attachments for the
   *         given list item. These are ordered by last Modified time.
   */
  public List<SPDocument> getAttachments(final ListState baseList,
      final SPDocument listItem) {
    if (baseList == null) {
      LOGGER.warning("Unable to get the attachments because list is null. ");
      return Collections.emptyList();
    }

    if (listItem == null) {
      LOGGER.warning("Unable to get the attachments because listItem provided for list [ "
          + baseList.getListURL() + " ] is null. ");
      return Collections.emptyList();
    }

    LOGGER.config("baseList[ title=" + baseList.getListTitle() + " , Url="
        + listItem.getUrl() + "]");

    final String listItemId = Util.getOriginalDocId(listItem.getDocId(), listItem.getFeedType());

    // All the known attachments (discovered earlier and are their in the
    // connector's state) are first collected into the knownAttachments and
    // then all those which are still returned by the Web Service will be
    // removed. This way, we'll be able to track the deleted attachments.
    final List<String> knownAttachments = new ArrayList<String>();
    if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
      knownAttachments.addAll(baseList.getAttachmntURLsFor(listItemId));
    }

    // Make the getAttachments request.
    List<SPDocument> listAttachments = Util.makeWSRequest(
        sharepointClientContext, listsWS,
        new Util.RequestExecutor<List<SPDocument>>() {
      public List<SPDocument> onRequest(final BaseWS ws)
          throws Throwable {
        return ((ListsWS) ws).getAttachments(baseList, listItem,
            knownAttachments);
      }
      
      public void onError(final Throwable e) {
        final String listName = baseList.getPrimaryKey();
        LOGGER.log(Level.WARNING,
            "Unable to get attachments for ListItemId [ " + listItemId 
            + " ], listName [ " + listName + " ].", e);
      }
    });
    
    if (null == listAttachments) {
      return Collections.emptyList();
    }

    if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
      // All the urls which have been left in knownAttachments are
      // considered to be deleted.
      for (String attchmnt_url : knownAttachments) {
        final String docID = SPConstants.ATTACHMENT_SUFFIX_IN_DOCID + "["
            + attchmnt_url + "]" + listItem.getDocId();
        final SPDocument attchmnt = new SPDocument(docID, attchmnt_url,
            baseList.getLastModCal(), SPConstants.NO_AUTHOR,
            SPConstants.OBJTYPE_ATTACHMENT,
            baseList.getParentWebState().getTitle(),
            sharepointClientContext.getFeedType(),
            baseList.getParentWebState().getSharePointType());
        attchmnt.setAction(ActionType.DELETE);
        listAttachments.add(attchmnt);
      }
    }

    int countDeleted = (null == knownAttachments) ? 0 : knownAttachments.size();

    if (listAttachments.size() > 0 || countDeleted > 0) {
      LOGGER.log(Level.INFO, "Found " + listAttachments.size()
          + " new/updated and " + countDeleted
          + " deleted attachments for listItem [ " + listItem.getUrl() + "]. ");
    } else {
      LOGGER.log(Level.CONFIG, "Found " + listAttachments.size()
          + " new/updated and " + countDeleted
          + " deleted attachments for listItem [ " + listItem.getUrl() + "]. ");
    }

    Collections.sort(listAttachments);
    return listAttachments;
  }

  /**
   * Used to get list items under a list using getListItems() Web Method
   *
   * @param list the list whose items are to be retrieved
   * @param lastModified the date that serves as a base for incremental crawl
   * @param lastItemID the last ID crawled for incremental crawling
   * @param allWebs a collection to store any webs discovered as part of
   *          discovering list items. For example, link sites are stored as
   *          list items.
   * @return the list of documents as {@link SPDocument}
   */
  public List<SPDocument> getListItems(final ListState list,
      final Calendar lastModified, final String lastItemID,
      final Set<String> allWebs) {
    if (list == null) {
      LOGGER.warning("Unable to get the list items because list is null");
      return Collections.emptyList();
    }

    LOGGER.config("list: title [ " + list.getListTitle() + " ], URL [ "
        + list.getListURL() + " ], lastItemID [ " + lastItemID
        + " ], lastModified [ " + lastModified + " ]");

    if (sharepointClientContext == null) {
      LOGGER.warning("Unable to get the list items because client context is null");
      return Collections.emptyList();
    }

    // Create the query for the lists.
    final String listName = list.getPrimaryKey();
    final String viewName = "";
    final String webID = "";
 
    final ListsUtil.SPQueryInfo queryInfo = new ListsUtil.SPQueryInfo() {
      public MessageElement[] getQuery() throws Exception {
        return ListsUtil.createQuery(lastModified, lastItemID);
      }

      public MessageElement[] getViewFields() throws Exception {
        return ListsUtil.createViewFields();
      }

      public MessageElement[] getQueryOptions() throws Exception {
        return ListsUtil.createQueryOptions(list.canContainFolders(), null, null);
      }
    };

    // Make the getListItems request.
    List<SPDocument> listItems = Util.makeWSRequest(
        sharepointClientContext, listsWS,
        new Util.RequestExecutor<List<SPDocument>>() {
      public List<SPDocument> onRequest(final BaseWS ws)
          throws Throwable {
        return ((ListsWS) ws).getListItems(list, listName, viewName, queryInfo,
            webID, allWebs);
      }
      
      public void onError(final Throwable e) {
        LOGGER.log(Level.WARNING, "Unable to get items for list [ "
            + listName + " ].", e);
      }
    });

    if (null == listItems) {
      return Collections.emptyList();
    }

    if (listItems.size() >= Integer.parseInt(rowLimit)) {
      LOGGER.finer("At least rowlimit number of documents were found, "
          + "so next page might exist, setting the next page value to "
          + "non null, rowlimit = [" + rowLimit + "] listitemcount = [" 
          + listItems.size() + "]");
      list.setNextPage("next page might exist so setting to - not null");
    } else {
      LOGGER.finer("Less than rowlimit number of documents were found, "
          + "so next page does not exist, leaving next page value "
          + "unchanged. rowlimit = [" + rowLimit + "] listitemcount = ["
          + listItems.size() + "]");
    }

    Collections.sort(listItems);
    if (listItems.size() > 0) {
      LOGGER.info("found: " + listItems.size() + " Items in List/Library ["
          + list.getListURL() + "]");
    } else {
      LOGGER.config("No Items found in List/Library [" + list.getListURL()
          + "]");
    }
    return listItems;
  }

  /**
   * Retrieves all the folder hierarchy from a given folder level and updates
   * the ExtraIDs of the list. This operation is independent of the batch hint
   * because the discovered folders are not sent as docs.
   *
   * @param list Specify the base list
   * @param folder From where to discover the folder hierarchy
   * @param lastID If we have already identified some folders at this
   *          folderLevel, specify the lastItemID to get the next set of
   *          folders.
   * @return the list of folders in this list
   */
  public List<Folder> getSubFoldersRecursively(final ListState list,
      final Folder folder, final String lastID) {
    return listsWS.getSubFoldersRecursively(list, folder, lastID);
  }

  /**
   * Retrieves the list items only the specified level. This required when a
   * folder is restored and we need to discover items level by level.
   *
   * @param list Base List
   * @param lastItemIdAtFolderLevel Last Item ID that we have already
   *          identified at this level.
   * @param currentFolder The folder from where to discover the items
   * @return list of documents as {@link SPDocument}
   */
  public List<SPDocument> getListItemsAtFolderLevel(final ListState list,
      final String lastItemIdAtFolderLevel, final Folder currentFolder,
      final Folder renamedFolder) {
    if (null == currentFolder) {
      return Collections.emptyList();
    }

    LOGGER.fine("DocId for WS call : " + lastItemIdAtFolderLevel
        + " folder path : " + currentFolder + " for renamed folder "
        + renamedFolder);
    final String listName = list.getPrimaryKey();
    final String viewName = "";
    final String webID = "";

    final ListsUtil.SPQueryInfo queryInfo = new ListsUtil.SPQueryInfo() {
      public MessageElement[] getQuery() throws Exception {
        return ListsUtil.createQuery2(lastItemIdAtFolderLevel);
      }

      public MessageElement[] getViewFields() throws Exception {
        return ListsUtil.createViewFields();
      }

      public MessageElement[] getQueryOptions() throws Exception {
        return ListsUtil.createQueryOptions(false, currentFolder.getPath(), null);
      }
    };
        
    // Make the getListItems request.
    List<SPDocument> listItems = Util.makeWSRequest(
        sharepointClientContext, listsWS,
        new Util.RequestExecutor<List<SPDocument>>() {
      public List<SPDocument> onRequest(final BaseWS ws)
          throws Throwable {
        return ((ListsWS) ws).getListItems(list, listName, viewName, queryInfo, webID, null);
      }
      
      public void onError(final Throwable e) {
        LOGGER.log(Level.WARNING, "Unable to get items for list [ "
            + listName + " ].", e);
      }
    });

    if (null == listItems) {
      return Collections.emptyList();
    }
    
    // Set the parent and rename folder for each document.
    for (SPDocument doc : listItems) {
      doc.setRenamedFolder(renamedFolder);
      doc.setParentFolder(currentFolder);
    }

    Collections.sort(listItems);
    LOGGER.fine("Found " + listItems.size() + " in the folder "
        + currentFolder);

    return listItems;
  }

  /**
   * Used to get list items under a list. If a change token is specified, we
   * only get the changed items. If no change token is specified we get all the
   * items along with the list schema. CAML query that is used while calling
   * getListItemChangesSinceToken should only be used for items that are
   * returned and not for the changes. Because if the query stops any itemID to
   * be shown under the changes, web service returns wrong information about the
   * changes on that item. For example, a rename of ID 1 may be shown as deleted
   * or so. Take care of this while making the first call. If you are using a
   * token to get the changes and the CAML query specified may stop some element
   * from getting shown, do not trust the change info.
   *
   * @param list List whose items is to be retrieved
   * @param allWebs A collection to store any webs, discovered as part of
   *          discovering list items. Foe example link sites are stored as list
   *          items.
   * @return the list of documents as {@link SPDocument}
   */
  // FIXME Why using List and not Set?
  public List<SPDocument> getListItemChangesSinceToken(final ListState list,
      final Set<String> allWebs) throws SharepointException {
    List<SPDocument> listItems = new ArrayList<SPDocument>();
    if (list == null) {
      LOGGER.warning("Unable to get the list items because list is null");
      return listItems;
    }

    LOGGER.config("list [ " + list + " ], LastDoc [ "
        + list.getLastDocForWSRefresh() + " ]");

    // if there are any folder/rename/restore found during previous WS call,
    // processed the folders first
    if (null != list.getNextChangeTokenForSubsequectWSCalls()) {
      traverseChangedFolders(list, listItems);
      if (listItems.size() >= sharepointClientContext.getBatchHint()) {
        Collections.sort(listItems);
        if (null == list.getNextPage()) {
          // There might be more documents to be crawled using the
          // current change token.
          list.setNextPage("not null");
        }
        return listItems;
      }
    }

    final String listName = list.getPrimaryKey();
    final String viewName = "";
    final String token = list.getChangeTokenForWSCall();

    final ListsUtil.SPQueryInfo queryInfo = new ListsUtil.SPQueryInfo() {
      public MessageElement[] getQuery() throws Exception {
        if (null == token) {
          String lastDocID = "0";
          if (null != list.getLastDocForWSRefresh()) {
            lastDocID = Util.getOriginalDocId(list.getLastDocForWSRefresh().getDocId(),
                sharepointClientContext.getFeedType());
          }
          // During full crawl, crawl progresses using LastDocId
          return ListsUtil.createQuery2(lastDocID);
        } else {
          // During incremental crawl, crawl progresses using change token
          return ListsUtil.createQuery3("0");
        }
      }

      public MessageElement[] getViewFields() throws Exception {
        return ListsUtil.createViewFields();
      }

      public MessageElement[] getQueryOptions() throws Exception {
        return ListsUtil.createQueryOptions(true, null, null);
      }
    };

    // Make the getListItemChangesSinceToken request.
    final Set<String> deletedIDs = new HashSet<String>();
    final Set<String> restoredIDs = new HashSet<String>();
    final Set<String> renamedIDs = new HashSet<String>();
    // retry is defined as an array instead of simple boolean object
    // since it needs to be used with inline class.
    final boolean[] retry = new boolean[1];
    List<SPDocument> requestListItems = null;
    final List<SPDocument> initialRequestListItems = Util.makeWSRequest(
        sharepointClientContext, listsWS,
        new Util.RequestExecutor<List<SPDocument>>() {
      public List<SPDocument> onRequest(final BaseWS ws)
          throws Throwable {
        return ((ListsWS) ws).getListItemChangesSinceToken(list, listName, 
            viewName, queryInfo, token, allWebs, deletedIDs, restoredIDs, renamedIDs);
      }
      
      public void onError(final Throwable e) {
        retry[0] = handleListException(list, e);     
      }
    });    
    if (initialRequestListItems != null && retry[0] == false) {
      requestListItems = initialRequestListItems;
    } else if (retry[0]) {
      final String retryToken = list.getChangeTokenForWSCall();
      LOGGER.log(Level.INFO,
          "Retrying getListItemChangesSinceToken for List [" + list
          + "] with Change Token [" + retryToken + "]");
      final List<SPDocument> retryRequestListItems = Util.makeWSRequest(
          sharepointClientContext, listsWS,
          new Util.RequestExecutor<List<SPDocument>>() {
            public List<SPDocument> onRequest(final BaseWS ws)
                throws Throwable {
              return ((ListsWS) ws).getListItemChangesSinceToken(list, listName, 
                  viewName, queryInfo, retryToken, allWebs, deletedIDs, restoredIDs, renamedIDs);
            }

            public void onError(final Throwable e) {
              handleListException(list, e);     
            }
          });
      if (retryRequestListItems != null) {
        requestListItems = retryRequestListItems;
      }
    }

    // If some folder renames are found in WS response, handle it first.
    if (renamedIDs.size() > 0 || restoredIDs.size() > 0) {
      traverseChangedFolders(list, listItems);
    }

    if (listItems.size() < sharepointClientContext.getBatchHint()
        && null != requestListItems) {
      listItems.addAll(requestListItems);
    } else if (null == list.getNextPage() && null != requestListItems
        && requestListItems.size() > 0) {
      list.setNextPage("not null");
    }

    // Process deleted IDs
    if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
      try {
        listItems.addAll(processDeletedItems(deletedIDs, list));
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Problem while constructing delete feeds..", e);
      }
    }

    if (listItems.size() > 0) {
      LOGGER.info("found " + listItems.size() + " Items in List/Library ["
          + list.getListURL() + "] ");
    } else {
      LOGGER.config("No Items found in List/Library [" + list.getListURL()
          + "]");

      // If there are no more documents to be sent from the current window
      // of (changetoken to nextchangeToken), the completion of this
      // window must be announced to the caller.
      if (null == list.getNextPage()
          && null != list.getNextChangeTokenForSubsequectWSCalls()) {
        list.setNewList(true);
      }
    }

    Collections.sort(listItems);
    return listItems;
  }

  /**
   * Called when the connector is not able proceed after the current state of
   * list (lastDoc+changetoken) because the web service call has failed.
   *
   * @param list List for which the exception occurred
   * @return an advice to the caller indicating whether the web service call
   *         should be re-tried
   */
  private boolean handleListException(final ListState list, Throwable te) {
    LOGGER.log(Level.WARNING, "Unable to get the List Items for list [ "
        + list.getListURL() + " ]. ", te);
    String ct = list.getChangeTokenForWSCall();
    if (Strings.isNullOrEmpty(ct)) {
      // If nothing can be done to recover from this exception, at least
      // ensure that the crawl for this list will not proceed so that the user
      // would not get any false impression afterwards. Following will ensure
      // that list will not be sent as document and hence can not be assumed
      // completed.
      
      // In case of error if Change Token is null then don't process List again
      list.setNewList(false);
      return false;
    } else {
      // If change token is not null, reset List State and Try again.
      list.resetState();
      LOGGER.log(Level.WARNING, "Current change token [ " + ct
          + " ] of List [ " + list 
          + " ] has expired or is invalid. "
          + "State of the list was reset to initiate a full crawl....");
      list.setNewList(true);
      return true;
    }
  }

  /**
   * Construct SPDocument for all those items which has been deleted.
   *
   * @param deletedIDs all the item IDs for which delete feed is to be
   *          constructed
   * @param list the base list
   * @return the list of documents as {@link SPDocument}
   */
  private List<SPDocument> processDeletedItems(final Set<String> deletedIDs,
      final ListState list) {
    final ArrayList<SPDocument> listItems = new ArrayList<SPDocument>();
    int count = 0;
    final List<String> allDeletedIDs = new ArrayList<String>(deletedIDs);

    if (allDeletedIDs.size() < 1) {
      return listItems;
    }

    int threashold = 1000;
    try {
      threashold = Integer.parseInt(rowLimit);
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING, "Unable to parse rowlimit [ " + rowLimit
          + " ] for numeric value", e);
    }

    Collections.sort(allDeletedIDs);
    LOGGER.log(Level.FINE, "Constructing delete feeds for IDs " + allDeletedIDs
        + ". ");
    for (String currentItemID : deletedIDs) {
      if (list.isInDeleteCache(currentItemID)) {
        if (LOGGER.isLoggable(Level.FINER)) {
          LOGGER.log(Level.FINER, "Skipping document with original ID: "
              + currentItemID + " from list : " + list.getListURL());
        }
        continue;
      }
      if (count > threashold) {
        if (null == list.getNextPage()) {
          list.setNextPage("not null");
        }
        break;
      }
      String docID = list.getListURL() + SPConstants.DOC_TOKEN + currentItemID;
      final SPDocument doc = new SPDocument(docID, list.getListURL(),
          list.getLastModCal(), SPConstants.NO_AUTHOR,
          SPConstants.OBJTYPE_LIST_ITEM, list.getParentWebState().getTitle(),
          sharepointClientContext.getFeedType(),
          list.getParentWebState().getSharePointType());
      doc.setAction(ActionType.DELETE);
      listItems.add(doc);
      count++;

      // If this list can contain attachments, assume that each item had
      // attachments and send delete feed for them.
      if (list.canContainAttachments()) {
        final List<String> attachments = list.getAttachmntURLsFor(currentItemID);
        final String originalDocID = docID;
        for (String attchmnt_url : attachments) {
          docID = SPConstants.ATTACHMENT_SUFFIX_IN_DOCID + "[" + attchmnt_url
              + "]" + originalDocID;
          final SPDocument attchmnt = new SPDocument(docID, attchmnt_url,
              list.getLastModCal(), SPConstants.NO_AUTHOR,
              SPConstants.OBJTYPE_ATTACHMENT,
              list.getParentWebState().getTitle(),
              sharepointClientContext.getFeedType(),
              list.getParentWebState().getSharePointType());
          attchmnt.setAction(ActionType.DELETE);
          listItems.add(attchmnt);
          count++;
        }
      }
    }

    if (count > 0) {
      LOGGER.info("found: " + count + " Items in List/Library ["
          + list.getListURL() + "] for feed action=DELETE");
    } else {
      LOGGER.config("No items foudn in List/Library [" + list.getListURL()
          + "] for feed action=DELETE");
    }

    return listItems;
  }

  /**
   * Traverses a changed folder to retrieve items that have changed.
   *
   * @param list the list whose items are to be retrieved
   * @param allItems the collection used to store the discovered items
   */
  private void traverseChangedFolders(final ListState list,
      final List<SPDocument> allItems) {
    SPDocument lastDocument = list.getLastDocForWSRefresh();
    if (null == lastDocument || null == allItems) {
      return;
    }

    final Folder lastDocParentFolder = lastDocument.getParentFolder();
    final Folder lastDocRenamedFolder = lastDocument.getRenamedFolder();

    if (null != lastDocRenamedFolder) {
      int index = list.getChangedFolders().indexOf(lastDocRenamedFolder);
      if (index > 0) {
        list.getChangedFolders().subList(0, index).clear();
      }
    }

    Iterator<Folder> itrChangedFolders = list.getChangedFolders().iterator();
    while (itrChangedFolders.hasNext()) {
      Folder changedFolder = itrChangedFolders.next();
      if (allItems.size() >= sharepointClientContext.getBatchHint()) {
        // more folders to traverse
        if (null == list.getNextPage()) {
          list.setNextPage("not null");
        }
        break;
      }

      final List<Folder> folders = getSubFoldersRecursively(list, changedFolder, null);
      if (null != lastDocParentFolder) {
        int index = folders.indexOf(lastDocParentFolder);
        if (index > 0) {
          folders.subList(0, index).clear();
        }
      }

      LOGGER.log(Level.INFO, "Processing renamed/restored folder ["
          + changedFolder + "] ");

      int docCountFromCurrentFolder = 0;
      for (Folder currentFolder : folders) {
        if (allItems.size() >= sharepointClientContext.getBatchHint()) {
          // More sub folders to be crawled
          if (null == list.getNextPage()) {
            list.setNextPage("not null");
          }
          break;
        }
        String lastDocIdForCurrentFolder = "0";
        if (null != lastDocParentFolder
            && lastDocParentFolder.equals(currentFolder)) {
          lastDocIdForCurrentFolder = Util.getOriginalDocId(lastDocument.getDocId(), sharepointClientContext.getFeedType());
        }
        List<SPDocument> currentListItems = getListItemsAtFolderLevel(list, lastDocIdForCurrentFolder, currentFolder, changedFolder);
        LOGGER.log(Level.CONFIG, "found " + currentListItems.size()
            + " items under folder [" + currentFolder + " ] ");
        docCountFromCurrentFolder += currentListItems.size();
        allItems.addAll(currentListItems);
      }

      LOGGER.log(Level.INFO, "found " + docCountFromCurrentFolder
          + " items under restored/renamed folder [" + changedFolder + " ] ");

      if (null == list.getNextPage() && docCountFromCurrentFolder == 0) {
        itrChangedFolders.remove();
      }
    }
  }

  private List<SPDocument> parseCustomWSResponseForListItemNodes(
      final MessageElement wsElement, ListState list) {
    final ArrayList<SPDocument> listItems = new ArrayList<SPDocument>();

    if (null == wsElement
        || !SPConstants.GSSLISTITEMS.equals(wsElement.getNodeName())) {
      return listItems;
    }
    for (final Iterator itChilds = wsElement.getChildElements(); itChilds.hasNext();) {
      Object obj = itChilds.next();
      if (null == obj || !(obj instanceof MessageElement)) {
        continue;
      }
      try {
        final MessageElement row = (MessageElement) obj;
        final SPDocument doc = ListsUtil.processListItemElement(
            sharepointClientContext, row, list, null);
        if (doc == null) {
          LOGGER.info("Skipping the ID [" + row.getAttribute(SPConstants.ID)
              + "] under the List/Library URL " + list.getListURL() + ".");
        } else {
          listItems.add(doc);
        }
      } catch (final Exception e) {
        LOGGER.log(Level.WARNING, "Problem occurred while parsing node", e);
        continue;
      }
    }

    return listItems;
  }

  public List<SPDocument> parseCustomWSResponseForListItemNodes(String data,
      ListState list) {
    MessageElement wsElement = null;
    try {
      wsElement = ListsUtil.getMeFromString(data);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "", e);
    }
    return parseCustomWSResponseForListItemNodes(wsElement, list);
  }
}
