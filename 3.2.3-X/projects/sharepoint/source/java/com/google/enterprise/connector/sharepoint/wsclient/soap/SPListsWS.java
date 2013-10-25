// Copyright 2007-2011 Google Inc.
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

package com.google.enterprise.connector.sharepoint.wsclient.soap;

import com.google.enterprise.connector.sharepoint.client.ListsUtil;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.generated.lists.GetAttachmentCollectionResponseGetAttachmentCollectionResult;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenContains;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenQuery;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenQueryOptions;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenViewFields;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsQuery;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsQueryOptions;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsResponseGetListItemsResult;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsViewFields;
import com.google.enterprise.connector.sharepoint.generated.lists.Lists;
import com.google.enterprise.connector.sharepoint.generated.lists.ListsLocator;
import com.google.enterprise.connector.sharepoint.generated.lists.ListsSoap_BindingStub;
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
import java.rmi.RemoteException;
import java.util.ArrayList;
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

/**
 * Java Client for calling Lists.asmx Provides a layer to talk to the Lists Web
 * Service on the SharePoint server Any call to this Web Service must go through
 * this layer.
 *
 * @author nitendra_thakur
 */
public class SPListsWS implements ListsWS {
  private static final Logger LOGGER = Logger.getLogger(SPListsWS.class.getName());
  private final SharepointClientContext sharepointClientContext;
  private final String endpoint;
  private final ListsSoap_BindingStub stub;
  private final String rowLimit;

  /**
   * @param sharepointClientContext The Context is passed so that necessary
   *          information can be used to create the instance of current class
   *          Web Service endpoint is set to the default SharePoint URL stored
   *          in SharePointClientContext.
   * @throws SharepointException
   */
  public SPListsWS(final SharepointClientContext sharepointClientContext, 
      final String rowLimit) throws SharepointException {
      this.sharepointClientContext = sharepointClientContext;
      this.rowLimit = rowLimit;

      endpoint = Util.encodeURL(sharepointClientContext.getSiteURL())
          + SPConstants.LISTS_END_POINT;
      LOGGER.config("endpoint set to: " + endpoint);

      final ListsLocator loc = new ListsLocator();
      loc.setListsSoapEndpointAddress(endpoint);

      final Lists listsService = loc;

      try {
        stub = (ListsSoap_BindingStub) listsService.getListsSoap();
      } catch (final ServiceException e) {
        LOGGER.log(Level.WARNING, "Unable to get the list stub", e);
        throw new SharepointException("Unable to get the list stub");
      }
  }

  /* @Override */
  public String getUsername() {
    return stub.getUsername();
  }

  /* @Override */
  public void setUsername(final String username) {
    stub.setUsername(username);
  }

  /* @Override */
  public void setPassword(final String password) {
    stub.setPassword(password);
  }

  /* @Override */
  public void setTimeout(final int timeout) {
    stub.setTimeout(timeout);
  }

  /**
   * Gets all the attachments of a particular list item.
   *
   * @param baseList List to which the item belongs
   * @param listItem list item for which the attachments need to be retrieved.
   * @return list of sharepoint SPDocuments corresponding to attachments for the
   *         given list item. These are ordered by last Modified time.
   * @throws RemoteException on a web service request error
   */
  public List<SPDocument> getAttachments(final ListState baseList,
      final SPDocument listItem, final List<String> knownAttachments)
      throws RemoteException {
    final ArrayList<SPDocument> listAttachments = new ArrayList<SPDocument>();

    if (stub == null) {
      LOGGER.warning("Unable to get the attachments for listItem [ "
          + listItem.getUrl() + " ], list [ " + baseList.getListURL()
          + " ] since stub is null.");
      return listAttachments;
    }

    final String listName = baseList.getPrimaryKey();
    final String listItemId = Util.getOriginalDocId(listItem.getDocId(),
        listItem.getFeedType());
    // Check for Attachments metedata property of ListItem. If no attachments
    // for listitem then value will be 0. So avoid webservice call.
    // For some reason attachment property is not available with listItem then
    // make web service call. SharePoint document library don't allow
    // attachments so that scenario should be covered with
    // ListState.canContainAttachments(). For Folder Items Attachments property
    // will not be present but FSObjType = 1. Ignore folders 
    // for attachment verification.
    final String strAttachmentValue = 
        listItem.getMetaDataAttributeValue(SPConstants.DOC_ATTACHMENTS);
    final String strFSObjType =  
        listItem.getMetaDataAttributeValue(SPConstants.FSOBJTYPE);
    LOGGER.info("List ["+baseList.toString()+"] Item ["+listItemId 
        + "] Attachments ["+strAttachmentValue 
        +"] FSObjType [" +strFSObjType+"]");    
    GetAttachmentCollectionResponseGetAttachmentCollectionResult res;
    if ((strAttachmentValue != null && strAttachmentValue.equals("0"))
        || (strFSObjType != null && strFSObjType.equals("1"))) {
      // Attachments not applicable if strFSObjType is 1 or List Item
      // does not contain any attachments as per strAttachmentsValue.
      res = null;      
    } else {
      LOGGER.info("Calling web service to get Attachment for List ["
          + baseList.toString() +"] Item ["+listItemId
          + "] Attachments [" +strAttachmentValue
          + "] FSObjType [" +strFSObjType+"]");
      res = stub.getAttachmentCollection(listName, listItemId); 
    }
    if (res != null) {
      final MessageElement[] me = res.get_any();
      if (me != null) {
        if (me.length > 0) {
          if (me[0] != null) {
            Iterator<?> ita = me[0].getChildElements();
            while ((ita != null) && (ita.hasNext())) {
              final MessageElement attachmentsOmElement = (MessageElement) ita.next();
              Iterator<?> attachmentsIt =
                  attachmentsOmElement.getChildElements();
              while (attachmentsIt.hasNext()) {
                final String url = attachmentsIt.next().toString();
                LOGGER.config("Attachment URL: " + url);

                if (sharepointClientContext.isIncludedUrl(url)) {
                  String modifiedID = listItemId;
                  if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
                    modifiedID = SPConstants.ATTACHMENT_SUFFIX_IN_DOCID + "["
                        + url + "]" + listItem.getDocId();

                    if (knownAttachments.contains(url)) {
                      knownAttachments.remove(url);
                    }
                  }
                  final SPDocument doc = new SPDocument(modifiedID, url,
                      baseList.getLastModCal(), SPConstants.NO_AUTHOR,
                      SPConstants.OBJTYPE_ATTACHMENT,
                      baseList.getParentWebState().getTitle(),
                      sharepointClientContext.getFeedType(),
                      listItem.getSPType());
                  doc.setParentList(baseList);

                  listAttachments.add(doc);
                } else {
                  LOGGER.warning("Excluding attachment " + url);
                }
              }
            }
          }
        }
      }
    }

    return listAttachments;
  }

  /**
   * Used to get list items under a list using getListItems() Web Method
   *
   * @param list the list whose items are to be retrieved
   * @param listName the list name used with the SOAP request getListItems
   * @param viewName the view name used with the SOAP request getListItems
   * @param queryInfo the query, fields, and options used with the
   *        SOAP request getListItems
   * @param webID
   * @param allWebs a collection to store any webs discovered as part of
   *          discovering list items. For example, link sites are stored as
   *          list items.
   * @return the list of documents as {@link SPDocument}
   */
  public List<SPDocument> getListItems(final ListState list,
      final String listName, final String viewName, 
      final ListsUtil.SPQueryInfo queryInfo, final String webID, 
      final Set<String> allWebs) throws RemoteException {
    final ArrayList<SPDocument> listItems = new ArrayList<SPDocument>();

    if (stub == null) {
      LOGGER.warning("Unable to get the list items since stub is null.");
      return listItems;
    }

    final GetListItemsQuery query = new GetListItemsQuery();
    final GetListItemsViewFields viewFields = new GetListItemsViewFields();
    final GetListItemsQueryOptions queryOptions = new GetListItemsQueryOptions();

    try {
      query.set_any(queryInfo.getQuery());
      viewFields.set_any(queryInfo.getViewFields());
      queryOptions.set_any(queryInfo.getQueryOptions());
      LOGGER.config("Making web service request with the following "
          + "parameters: query [ " + query.get_any()[0] 
          + " ], queryoptions [ " + queryOptions.get_any()[0] 
          + " ], viewFields [ " + viewFields.get_any()[0] + "] ");
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Unable to get items at folder level "
          + "for list [ " + listName + " ].", e);
      return Collections.emptyList();
    }

    GetListItemsResponseGetListItemsResult res = stub.getListItems(
        listName, viewName, query, viewFields, rowLimit, queryOptions, webID);

    if (res != null) {
      final MessageElement[] me = res.get_any();
      if ((me != null) && (me.length > 0)) {
        Iterator<?> itChilds = me[0].getChildElements();
        while (itChilds.hasNext()) {
          final MessageElement child = (MessageElement) itChilds.next();
          if (SPConstants.DATA.equalsIgnoreCase(child.getLocalName())) {
            Iterator<?> itrchild = child.getChildElements();
            while (itrchild.hasNext()) {
              final MessageElement row = (MessageElement) itrchild.next();
              final SPDocument doc = ListsUtil.processListItemElement(
                  sharepointClientContext, row, list, allWebs);
              if (doc != null) {
                listItems.add(doc);
              }
            }
          }
        }
      }
    }

    return listItems;
  }

  // TODO: getSubFoldersRecursively needs to cleaned up. The call to 
  // Util.makeWSRequest should be moved out of the web service classes.
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
    List<Folder> folders = new ArrayList<Folder>();
    if (!list.canContainFolders()) {
      return folders;
    }

    // TODO Check if the incoming folder should be added. This is probably
    // creating duplicate entries into the result.
    String tmpfolderLevel;
    if (null != folder) {
      folders.add(folder);
      tmpfolderLevel = folder.getPath();
    } else {
      tmpfolderLevel = null;
    }
    final String folderLevel = tmpfolderLevel;
    String tmpNextPage = null;
    do {
      final GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult res =
          getChildFolders(folderLevel, list, tmpNextPage);

      if (res == null) {
        LOGGER.log(Level.INFO, "No child items found @ ["
            + folderLevel + "] with next page as [" + tmpNextPage + "]");      
        break;
      }
      tmpNextPage = null;
      final MessageElement[] me = res.get_any();
      if ((me != null) && (me.length > 0)) {
        Iterator<?> itChilds = me[0].getChildElements();
        while (itChilds.hasNext()) {
          final MessageElement child = (MessageElement) itChilds.next();
          if (!SPConstants.DATA.equalsIgnoreCase(child.getLocalName())) {
            continue;
          }
          tmpNextPage = child.getAttribute(
              SPConstants.LIST_ITEM_COLLECTION_POSITION_NEXT);          
          Iterator<?> itrchild = child.getChildElements();
          while (itrchild.hasNext()) {
            final MessageElement row = (MessageElement) itrchild.next();            
            final String fsObjType =
                Util.normalizeMetadataValue(
                    row.getAttribute(SPConstants.OWS_FSOBJTYPE));
            String relativeURL = row.getAttribute(SPConstants.FILEREF);
            final String docId = row.getAttribute(SPConstants.ID);
            if ((fsObjType == null) || (relativeURL == null)
                || (docId == null)) {
              continue;
            } 
            relativeURL = relativeURL.substring(relativeURL.indexOf(SPConstants.HASH) + 1);
            String folderPath = null;
            if (fsObjType.equals("1")) {
              if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
                if (!list.updateExtraIDs(relativeURL, docId, true)) {
                  LOGGER.log(Level.INFO, "Unable to update relativeURL [ "
                      + relativeURL + " ], listURL [ " + list.getListURL()
                      + " ]. Perhaps a folder or list was renamed.");
                }
              }
              folderPath = relativeURL;
              if (folderPath == null) {
                continue;
              }
              if ((folderLevel != null) && (folderLevel.trim().length() != 0)) {
                if (folderPath.startsWith(folderLevel)) {
                  folders.add(new Folder(folderPath, docId));
                }
              } else {
                folders.add(new Folder(folderPath, docId));
              }
            }
          }
        }
      }
    } while (tmpNextPage != null);

    // removing duplicate entries
    folders = new ArrayList<Folder>(new HashSet<Folder>(folders));

    Collections.sort(folders);
    return folders;
  }

  private GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult getChildFolders(
      final String folderLevel,
      final ListState list, String nextPage ) {
    
    final String listName = list.getPrimaryKey();
    final String viewName = "";
    final GetListItemChangesSinceTokenQuery query =
        new GetListItemChangesSinceTokenQuery();
    final GetListItemChangesSinceTokenViewFields viewFields =
        new GetListItemChangesSinceTokenViewFields();
    final GetListItemChangesSinceTokenQueryOptions queryOptions =
        new GetListItemChangesSinceTokenQueryOptions();
    final String token = null;
    final GetListItemChangesSinceTokenContains contains = null;

    try {
      if (folderLevel == null) {
        query.set_any(ListsUtil.createQuery1("0"));
      } else {
        query.set_any(ListsUtil.createQuerySubFolders(folderLevel));
      }
      viewFields.set_any(ListsUtil.createViewFields());
      queryOptions.set_any(ListsUtil.createQueryOptions(true, nextPage));
      LOGGER.config("Making web service request with the following "
          + "parameters: query [ " + query.get_any()[0]
          + " ], queryoptions [ " + queryOptions.get_any()[0]
          + " ], viewFields [ " + viewFields.get_any()[0] + "] ");
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Unable to get folder hierarchy at folderLevel [ "
          + folderLevel + " ], list [ " + list.getListURL() + " ].", e);
      return null;
    }
    // Make the getListItemChangesSinceToken request.
    final GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult res =
        Util.makeWSRequest(sharepointClientContext, this, new Util.RequestExecutor<
        GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult>() {
          public GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult
              onRequest(final BaseWS ws) throws Throwable {
            return stub.getListItemChangesSinceToken(listName, viewName,
                query, viewFields, rowLimit, queryOptions, token, contains);
          }

          public void onError(final Throwable e) {
            LOGGER.log(Level.WARNING, "Unable to get folder hierarchy at folderLevel [ "
                + folderLevel + " ], list [ " + list.getListURL() + " ].", e);
          }
        });
    return res;
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
   * @param list the list whose items are to be retrieved
   * @param listName the list name used with the SOAP request
   *          getListItemChangesSinceToken
   * @param viewName the view name used with the SOAP request
   *          getListItemChangesSinceToken
   * @param queryInfo the query and fields used with the
   *        SOAP request getListItemChangesSinceToken
   * @param token the token used with the SOAP request
   *          getListItemChangesSinceToken
   * @param allWebs a collection to store any webs discovered as part of
   *          discovering list items. For example, link sites are stored as
   *          list items.
   * @param deletedIDs a collection used to store the IDs of deleted items
   * @param restoredIDs a collection used to store the IDs of restored items
   * @param renamedIDs a collection used to store the IDs of renamed items
   * @return the list of documents as {@link SPDocument}
   * @throws SharepointException
   * @throws RemoteException
   */
  // FIXME Why using List and not Set?
  public List<SPDocument> getListItemChangesSinceToken(final ListState list,
      final String listName, final String viewName, 
      final ListsUtil.SPQueryInfo queryInfo, String token,
      final Set<String> allWebs, final Set<String> deletedIDs, 
      final Set<String> restoredIDs, final Set<String> renamedIDs) 
      throws SharepointException, RemoteException {
    final ArrayList<SPDocument> listItems = new ArrayList<SPDocument>();

    if (stub == null) {
      LOGGER.warning("Unable to get the list items since stub is null.");
      return listItems;
    }

    // Set token as null If it is blank, because the web service expects so,
    // otherwise it fails.
    if ((token != null) && (token.trim().length() == 0)) {
      token = null;
    }

    final GetListItemChangesSinceTokenQuery query =
        new GetListItemChangesSinceTokenQuery();
    final GetListItemChangesSinceTokenViewFields viewFields =
        new GetListItemChangesSinceTokenViewFields();
    final GetListItemChangesSinceTokenQueryOptions queryOptions =
        new GetListItemChangesSinceTokenQueryOptions();

    try {
      query.set_any(queryInfo.getQuery());
      viewFields.set_any(queryInfo.getViewFields());
      queryOptions.set_any(queryInfo.getQueryOptions());
      LOGGER.config("Making web service request with the following "
          + "parameters: query [ " + query.get_any()[0] 
          + " ], queryoptions [ " + queryOptions.get_any()[0] 
          + " ], viewFields [ " + viewFields.get_any()[0] 
          + "], token [ " + token + " ] ");
    } catch (final Throwable e) {
      return Collections.emptyList();
    }

    GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult
        res = stub.getListItemChangesSinceToken(listName, viewName, query, 
        viewFields, rowLimit, queryOptions, token, null);

    List<MessageElement> updatedListItems = null;
    if (res != null) {
      final MessageElement[] me = res.get_any();
      if ((me != null) && (me.length > 0)) {
        // To ensure that Changes are accessed before documents
        boolean inSequence = false;
        Iterator<?> itChilds = me[0].getChildElements();
        while (itChilds.hasNext()) {
          final MessageElement child = (MessageElement) itChilds.next();
          if (SPConstants.CHANGES.equalsIgnoreCase(child.getLocalName())) {
            inSequence = true;
            ListsUtil.processListChangesElement(sharepointClientContext, 
                child, list, deletedIDs, restoredIDs, renamedIDs);
          } else if (SPConstants.DATA.equalsIgnoreCase(child.getLocalName())) {
            if (!inSequence) {
              LOGGER.log(Level.SEVERE, "Bad Sequence.");
            }
            // FIXME: Is this code correct? Do we only expect this code to be 
            // executed once? updatedListItems is taking the value of the last
            // processListDataElement call. We lose some data if 
            // updatedListItems is not null.
            if (null != updatedListItems) {
              LOGGER.warning("Unexpected behavior; updatedListItems is not "
                  + "null while processing processListDataElement.");
            }
            updatedListItems = processListDataElement(child, list,
                deletedIDs, restoredIDs, renamedIDs, allWebs);
          }
        }
      }
    }

    if (null != updatedListItems) {
      for (Object element : updatedListItems) {
        final MessageElement row = (MessageElement) element;
        final SPDocument doc = ListsUtil.processListItemElement(
            sharepointClientContext, row, list, allWebs);
        if (doc != null) {
          listItems.add(doc);
        }
      }
    }

    return listItems;
  }

  /**
   * Method to get list items under folder hierarchy including
   * sub folders and child list items.
   * @param list SharePoint list to query for child items
   * @param currentFolder Folder to get child list items from
   * @return list of SPDocuments for child items.
   */
  public List<SPDocument>getListItemsUnderFolderHeirarchy(
      ListState list, Folder currentFolder) {
    final ArrayList<SPDocument> listItems = new ArrayList<SPDocument>();
    if (null == currentFolder) {
      return listItems;
    }
    final String folderPath = currentFolder.getPath();
    final String listName = list.getPrimaryKey();
    final String listUrl = list.getListURL();
    final String viewName = "";
    final GetListItemChangesSinceTokenQuery query =
        new GetListItemChangesSinceTokenQuery();
    final GetListItemChangesSinceTokenViewFields viewFields =
        new GetListItemChangesSinceTokenViewFields();
    final GetListItemChangesSinceTokenQueryOptions queryOptions =
        new GetListItemChangesSinceTokenQueryOptions();
    final String token = null;
    final GetListItemChangesSinceTokenContains contains = null;


    try {
      if (folderPath == null) {
        query.set_any(ListsUtil.createQuery1("0"));
      } else {
        query.set_any(ListsUtil.createQueryInsideFolder(folderPath));
      }
      viewFields.set_any(ListsUtil.createViewFields());
      queryOptions.set_any(ListsUtil.createQueryOptions(
          true, currentFolder.getNextPage()));
      LOGGER.config("Making web service request with the following "
          + "parameters: query [ " + query.get_any()[0]
              + " ], queryoptions [ " + queryOptions.get_any()[0]
                  + " ], viewFields [ " + viewFields.get_any()[0] + "] ");
    } catch (Exception e) {
      LOGGER.log(Level.WARNING,
          "Unable to get folder hierarchy at folderLevel [ "
              + folderPath + " ], list [ " + list.getListURL() + " ].", e);
      currentFolder.setNextPage(null);
      return listItems;
    }

    final GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult res =
        Util.makeWSRequest(sharepointClientContext, this, new Util.RequestExecutor<
            GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult>() {
          public GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult
          onRequest(final BaseWS ws) throws Throwable {
            return stub.getListItemChangesSinceToken(listName, viewName,
                query, viewFields, rowLimit, queryOptions, token, contains);
          }

          public void onError(final Throwable e) {
            LOGGER.log(Level.WARNING, "Unable to get folder hierarchy at folderLevel [ "
                + folderPath + " ], list [ " + listUrl + " ].", e);
          }
        });

    if (res == null) {
      LOGGER.log(Level.INFO, "No child items found @ ["
          + folderPath + "] from folder [" + currentFolder + "]");
      currentFolder.setNextPage(null);
      return listItems;
    }
    
    String nextPage = null;
    final MessageElement[] me = res.get_any();
    if ((me != null) && (me.length > 0)) {
      Iterator<?> itChilds = me[0].getChildElements();
      while (itChilds.hasNext()) {
        final MessageElement child = (MessageElement) itChilds.next();
        if (!SPConstants.DATA.equalsIgnoreCase(child.getLocalName())) {
          continue;
        }
        nextPage = 
            child.getAttribute(SPConstants.LIST_ITEM_COLLECTION_POSITION_NEXT);
        Iterator<?> itrchild = child.getChildElements();
        while (itrchild.hasNext()) {
          final MessageElement row = (MessageElement) itrchild.next();
          final SPDocument doc = ListsUtil.processListItemElement(
              sharepointClientContext, row, list, null);
          if (doc != null) {
            listItems.add(doc);
          }
        }        
      }
    }    
    currentFolder.setNextPage(nextPage);
    return listItems;
  }

  // TODO: processListDataElement has a dependency on getSubFoldersRecursively
  // if the dependency can be removed then processListDataElement should be 
  // moved to ListsUtil.
  /**
   * Processing of rs:data element as returned by getListItemChangesSinceToken.
   *
   * @param dataElement represents the parent node which contains all the list
   *          items node.
   * @param list Base list
   * @param deletedIDs Set of deleted IDs. Delete feed will be constructed for
   *          them.
   * @param restoredIDs Set of restored IDs. New feeds are sent for these
   *          items.
   * @param renamedIDs If it is a folder. New feeds are sent for all the items
   *          beneath it.
   * @param allWebs A collection to store any webs discovered as part of
   *          discovering the list items. For example link sites are stored as list
   *          items.
   * @return the list items which WS returns as rs:rows. These do not include
   *         folders
   */
  private List<MessageElement> processListDataElement(
      final MessageElement dataElement, final ListState list,
      final Set<String> deletedIDs, final Set<String> restoredIDs,
      final Set<String> renamedIDs, final Set<String> allWebs) {

    final ArrayList<MessageElement> updatedListItems = new ArrayList<MessageElement>();
    final String receivedNextPage = dataElement.getAttribute(SPConstants.LIST_ITEM_COLLECTION_POSITION_NEXT);
    LOGGER.log(Level.FINE, "Next Page Received [ " + receivedNextPage + " ]. ");
    list.setNextPage(receivedNextPage);
    list.setListItemCollectionPositionNext(receivedNextPage);
    /*
     * One may think of using nextPage as the backbone of page by page crawling
     * when threshold is reached. This definitely seems to be a simple and
     * straight way. But, ListItemCollectionPositionNext is not very reliable. I
     * have tested this with a volume site of around 6000 docs, where all the
     * docs were under a very complex folder hierarchy. The observation was
     * "ListItemCollectionPositionNext does not actually remember the pages it
     * has returned when the docs are under a very complex hierarchy of folder
     * and very large in numbers. Hence, at the completion of all the docs, when
     * it is expected that ListItemCollectionPositionNext should then be
     * returned as null, it does not happen so. Instead,
     * ListItemCollectionPositionNext keeps recrawling the same set of document
     * again and again."
     */
    Iterator<?> itrchild = dataElement.getChildElements();
    while (itrchild.hasNext()) {
      try {
        final MessageElement row = (MessageElement) itrchild.next();
        final String docId = row.getAttribute(SPConstants.ID);
        if (null == docId) {
          LOGGER.log(Level.WARNING, "Skipping current rs:data node as docID is not found. listURL [ "
              + list.getListURL() + " ]. ");
          continue;
        }
        
        String fsObjType = Util.normalizeMetadataValue(
            row.getAttribute(SPConstants.OWS_FSOBJTYPE));
        if (fsObjType == null) {
          fsObjType = Util.normalizeMetadataValue(
              row.getAttribute(SPConstants.OWS_FSOBJTYPE_INMETA));
        }
        
        boolean isFolder = (fsObjType != null && fsObjType.equals("1"));        
        
        String relativeURL = row.getAttribute(SPConstants.FILEREF);

        LOGGER.log(Level.CONFIG, "docID [ " + docId + " ], relativeURL [ "
            + relativeURL + " ], fsObjType [ " + fsObjType + " ]. ");

        if (null == relativeURL) {
          LOGGER.log(Level.WARNING, "No relativeURL (FILEREF) attribute"
              + " found for the document, docID [ "
              + docId + " ], listURL [ " + list.getListURL() + " ]. ");
        } else if (null == fsObjType) {
          LOGGER.log(Level.WARNING,
              "No fsObjType found for the document, relativeURL [ "
              + relativeURL + " ], listURL [ " + list.getListURL() + " ]. ");
        } else {           
          relativeURL = 
              relativeURL.substring(relativeURL.indexOf(SPConstants.HASH) + 1);
          if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
            /*
             * Since we have got an entry for this item, this item can never
             * be considered as deleted. Remember,
             * getListItemChangesSinceToken always return the changes,
             * irrespective of any conditions specified in the CAML query.
             * And, if for any change the conditions becomes false, the change
             * details returned for this item may be misleading. For Example,
             * if item 1 is renamed, and in query we have asked to return only
             * those items whose ID is greater then 1; Then in that case, the
             * WS may return change info as delete along with rename for item
             * 1.
             */
            deletedIDs.remove(docId);
            list.removeFromDeleteCache(docId);

            if (isFolder) {
              if (!list.updateExtraIDs(relativeURL, docId, true)) {
                // Try again after updating the folders
                // info.
                // Because, the folder might have been renamed.
                LOGGER.log(Level.INFO, "Unable to update relativeURL [ "
                    + relativeURL + " ], listURL [ " + list.getListURL()
                    + " ]. Retrying after updating the folders info.. ");
                getSubFoldersRecursively(list, null, null);

                if (!list.updateExtraIDs(relativeURL, docId, true)) {
                  LOGGER.log(Level.INFO, "Unable to update relativeURL [ "
                      + relativeURL + " ], listURL [ " + list.getListURL()
                      + " ]. Perhaps a folder or list was renamed.");
                }
              }
            }
          }

          if (isFolder) {
            if (restoredIDs.contains(docId) || renamedIDs.contains(docId)) {
              list.addToChangedFolders(new Folder(relativeURL, docId));
            }
          }
        }

        /*
         * Do not process list items i.e, rs:rows here. This is because, we need
         * to process the renamed/restored folders cases first. If we'll not
         * reach the batch hint with such documents then only we'll process the
         * updated items.
         */
        
        boolean isFeedable = 
            ListsUtil.isFeedableListItem(sharepointClientContext, row, list);
        boolean pushAcls = sharepointClientContext.isPushAcls();

        if (isFeedable || (isFolder && pushAcls)) {
          updatedListItems.add(row);
        } else if (!sharepointClientContext.isInitialTraversal()) {
          // Added unpublished documents to delete list if
          // feedUnPublishedDocuments is set to false, so
          // that connector sends delete feeds for unpublished
          // content in SharePoint to GSA.
          LOGGER.warning("Adding the list item or document ["
              + row.getAttribute(SPConstants.FILEREF)
              + "] to the deleted IDs list to send delete feeds "
              + "for unpublished content in the list URL: "
              + list.getListURL()
              + ", and its current version is "
              + row.getAttribute(SPConstants.MODERATION_STATUS));
          deletedIDs.add(docId);
        }
      } catch (final Exception e) {
        LOGGER.log(Level.WARNING, "Problem occured while parsing the rs:data node", e);
        continue;
      }
    } // end of For

    return updatedListItems;
  }
}

