// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.sharepoint.wsclient.client;

import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.Folder;
import com.google.enterprise.connector.sharepoint.state.ListState;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

public interface ListsWS {
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
  public List<Folder> getSubFoldersRecursively(ListState list, Folder folder,
      String lastID);

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
  public List<SPDocument> getListItemChangesSinceToken(ListState list,
      Set<String> allWebs) throws SharepointException;

  /**
   * Used to get list items under a list using getListItems() Web Method
   *
   * @param list List whose items is to be retrieved
   * @param lastModified serves as a base for incremental crawl
   * @param lastItemID Serves as a base for incremental crawl
   * @param allWebs A collection to store any webs, discovered as part of
   *          discovering list items. Foe example link sites are stored as list
   *          items.
   * @return the list of documents as {@link SPDocument}
   */
  public List<SPDocument> getListItems(ListState list,
      Calendar lastModified, String lastItemID, Set<String> allWebs);

  /**
   * Gets all the attachments of a particular list item.
   *
   * @param baseList List to which the item belongs
   * @param listItem list item for which the attachments need to be retrieved.
   * @return list of sharepoint SPDocuments corresponding to attachments for the
   *         given list item. These are ordered by last Modified time.
   * @throws SharepointException
   * @throws MalformedURLException
   */
  public List<SPDocument> getAttachments(ListState baseList,
      SPDocument listItem);

  // TODO: java docs needed.
  public List<SPDocument> parseCustomWSResponseForListItemNodes(String data,
      ListState list);
}
