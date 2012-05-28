// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.sharepoint.wsclient.client;

import com.google.enterprise.connector.sharepoint.client.ListsUtil;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.state.Folder;
import com.google.enterprise.connector.sharepoint.state.ListState;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

// FIXME: Why using List for returns in these methods instead of Set?
public interface ListsWS extends BaseWS {
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
  public List<Folder> getSubFoldersRecursively(ListState list,
      Folder folder, String lastID);

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
   * @param queryInfo the query info used with the SOAP request, this data is
   *          used to initialize Sharepoint SOAP query objects
   * @param token the token used with the SOAP request
   *          getListItemChangesSinceToken
   * @param allWebs a collection to store any webs discovered as part of
   *          discovering list items. For example, link sites are stored as
   *          list items.
   * @param deletedIDs a collection used to store the IDs of deleted items
   * @param restoredIDs a collection used to store the IDs of restored items
   * @param renamedIDs a collection used to store the IDs of renamed items
   * @return the list of documents as {@link SPDocument}
   * @throws Exception on error
   */
  public List<SPDocument> getListItemChangesSinceToken(ListState list,
      String listName, String viewName, ListsUtil.SPQueryInfo queryInfo,
      String token, Set<String> allWebs, Set<String> deletedIDs,
      Set<String> restoredIDs, Set<String> renamedIDs)
      throws Exception;

  /**
   * Used to get list items under a list using getListItems() Web Method
   *
   * @param list the list whose items are to be retrieved
   * @param listName the list name used with the SOAP request getListItems
   * @param viewName the view name used with the SOAP request getListItems
   * @param queryInfo the query info used with the SOAP request, this data is
   *          used to initialize Sharepoint SOAP query objects
   * @param webID A string containing the GUID of the parent Web site of the list.
   *          An empty string means that the URL will be used.
   * @param allWebs a collection to store any webs discovered as part of
   *          discovering list items. For example, link sites are stored as
   *          list items.
   * @return the list of documents as {@link SPDocument}
   * @throws Exception on error
   */
  public List<SPDocument> getListItems(ListState list, String listName, 
      String viewName, ListsUtil.SPQueryInfo queryInfo, String webID,
      Set<String> allWebs) throws Exception;

  /**
   * Gets all the attachments of a particular list item.
   *
   * @param baseList List to which the item belongs
   * @param listItem List item for which the attachments need to be retrieved
   * @param knownAttachments A list of the known attachments. This is used
   *          to keep track of the attachments that have been found.
   *          Newly discovered attachments should be removed from this list
   *          (i.e. if a document is added to the return list then it's URL
   *          must be removed from the knownAttachments list).
   * @return A list of sharepoint SPDocuments corresponding to attachments
   *         for the given list item.
   * @throws Exception on error
   */
  public List<SPDocument> getAttachments(ListState baseList,
      SPDocument listItem, List<String> knownAttachments)
      throws Exception;
}
