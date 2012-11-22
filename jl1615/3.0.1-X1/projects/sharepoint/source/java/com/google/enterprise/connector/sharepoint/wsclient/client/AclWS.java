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

import com.google.enterprise.connector.sharepoint.client.ListsHelper;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocumentList;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;

import java.util.List;

public interface AclWS {
  /**
   * Gets a set of documents in the form of {@link SPDocumentList} crawled
   * from a single SharePoint site {@link WebState} and fetches ACL for all
   * the documents and set it the document's properties.
   *
   * @param resultSet {@link SPDocumentList} to be processed
   * @param webState parent {@link WebState} from which documents have been
   *          crawled
   */
  public void fetchAclForDocuments(SPDocumentList resultSet,
      WebState webState);

  // TODO: Need a description for fetchAclChangesSinceTokenAndUpdateState.
  public void fetchAclChangesSinceTokenAndUpdateState(WebState webState);

  /**
   * Works similar to {@link ListsWS#getListItems} but is designed to
   * be used only to get those list items whose ACLs have changed
   * because of any security change at parent level.
   *
   * @param listState The list from which the items are to be retrieved
   * @param listsHelper The lists helper for parsing the web service response
   * @return a list of {@link SPDocument}
   */
  public List<SPDocument> getListItemsForAclChangeAndUpdateState(
      ListState listState, ListsHelper listsHelper);

  /**
   * Executes CheckConnectivity() web method of GssAcl web service. Used for
   * checking the Web Service connectivity
   */
  public void checkConnectivity() throws SharepointException;

  /**
   * Construct SPDocument object for representing Web application policy
   * ACL information
   */
  public SPDocument getWebApplicationPolicy(WebState webState,
      String strFeedType);
  
  /**
   * Execute resolveSharePointGroups method
   * @param webState WebState for Group Resolution
   * @return boolean flag indicating status of SP group resolution.
   *         True if group resolution is successful. 
   *         False if group resolution fails.
   */
  public boolean resolveSharePointGroups(WebState webState);
}
