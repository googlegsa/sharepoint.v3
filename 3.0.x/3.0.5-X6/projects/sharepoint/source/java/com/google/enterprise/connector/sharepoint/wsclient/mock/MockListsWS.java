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

package com.google.enterprise.connector.sharepoint.wsclient.mock;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.ListsUtil;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.Folder;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.wsclient.client.ListsWS;

import java.rmi.RemoteException;
import java.util.logging.Logger;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MockListsWS implements ListsWS {
  private static final Logger LOGGER = Logger.getLogger(MockListsWS.class.getName());
  protected final SharepointClientContext sharepointClientContext;
  protected String username;
  protected String password;

  /**
   * @param ctx The Sharepoint context is passed so that necessary
   *    information can be used to create the instance of current class
   *    web service endpoint is set to the default SharePoint URL stored
   *    in SharePointClientContext.
   */
  public MockListsWS(final SharepointClientContext ctx) {
    sharepointClientContext = ctx;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public void setPassword(final String password) {
    this.password = password;
  }

  @Override
  public void setTimeout(final int timeout) {
  }

  @Override
  public List<Folder> getSubFoldersRecursively(final ListState list,
      final Folder folder, final String lastID) {
    return Collections.emptyList();
  }

  @Override
  public List<SPDocument> getListItemChangesSinceToken(
      final ListState list, final String listName, final String viewName, 
      final ListsUtil.SPQueryInfo queryInfo, final String token, 
      final Set<String> allWebs, final Set<String> deletedIDs, 
      final Set<String> restoredIDs, final Set<String> renamedIDs) 
      throws SharepointException, RemoteException {
    return Collections.emptyList();
  }

  @Override
  public List<SPDocument> getListItems(ListState list, final String listName, 
      final String viewName, final ListsUtil.SPQueryInfo queryInfo,
      final String webID, Set<String> allWebs) throws RemoteException {
    return Collections.emptyList();
  }

  @Override
  public List<SPDocument> getAttachments(final ListState baseList,
      final SPDocument listItem, List<String> knownAttachments) {
    return Collections.emptyList();
  }
}
