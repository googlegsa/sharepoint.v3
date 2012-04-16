// Copyright 2012 Google Inc.
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

import com.google.enterprise.connector.sharepoint.client.ListsUtil;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.state.Folder;
import com.google.enterprise.connector.sharepoint.state.ListState;

import org.apache.axis.AxisFault;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Set;

public class XmlListsWS extends MockListsWS {
  private static final Logger LOGGER =
      Logger.getLogger(XmlListsWS.class.getName());

  private final XmlClientFactory clientFactory;

  /**
   * @param clientFactory The client factory for webservice calls
   * @param ctx The Sharepoint context is passed so that necessary
   *    information can be used to create the instance of current class
   *    web service endpoint is set to the default SharePoint URL stored
   *    in SharePointClientContext.
   */
  public XmlListsWS(XmlClientFactory clientFactory,
      SharepointClientContext ctx) {
    super(ctx);
    this.clientFactory = clientFactory;
  }

  @Override
  public List<Folder> getSubFoldersRecursively(ListState list,
      Folder folder, final String lastID) {
    final ArrayList<Folder> folders = new ArrayList<Folder>();
    final String listUrl = list.getListURL();
    try {
      MockItem listItem = clientFactory.getItemFromUrl(listUrl, username);
      appendFolders(folders, listUrl, listItem, username);
    } catch (Exception e) {
      LOGGER.warning("Unable to add folders for listUrl " + listUrl + ".");
    }
    LOGGER.info("Created " + folders.size() + " folders for URL " + 
        listUrl + ".");
    return folders;
  }

  @Override
  public List<SPDocument> getListItemChangesSinceToken(
      ListState list, String listName, String viewName, 
      ListsUtil.SPQueryInfo queryInfo, String token, Set<String> allWebs, 
      Set<String> deletedIDs, Set<String> restoredIDs, Set<String> renamedIDs)
      throws AxisFault {
    return getDocuments(list);
  }

  @Override
  public List<SPDocument> getListItems(ListState list, String listName, 
      String viewName, ListsUtil.SPQueryInfo queryInfo, String webID, 
      Set<String> allWebs) throws AxisFault {
    return getDocuments(list);
  }

  private List<SPDocument> getDocuments(ListState list) throws AxisFault {
    final ArrayList<SPDocument> docs = new ArrayList<SPDocument>();
    final String listUrl = list.getListURL();
    MockItem listItem = clientFactory.getItemFromUrl(listUrl, username);
    if (null != listItem) {
      try {
        appendDocuments(docs, listUrl, listItem,
        sharepointClientContext.getFeedType(), username);
      } catch (Throwable e) {
        LOGGER.log(Level.WARNING, "Unable to create documents for [ "
            + listUrl + " ].", e);
      }
    }
    LOGGER.info("Created " + docs.size() + " documents for URL " + 
        listUrl + ".");
    return docs;
  }

  /**
   * Adds the child documents of {@link MockItem} to a list of 
   * {@link SPDocument}.
   *
   * @param docs The list collection to append the documents to
   * @param webUrl The username requesting access
   * @param item The {@link MockItem} that contains the documents to add
   *        the list
   * @param feedType The {@link FeedType} to use for the new documents
   * @param username The user requesting access
   * @throws AxisFault when the user is not authorized
   */
  private void appendDocuments(List<SPDocument> docs, String webUrl,
      MockItem item, FeedType feedType, String username)
      throws SharepointException, AxisFault {
    if (!item.hasPermission(username)) {
      throw new AxisFault(SPConstants.UNAUTHORIZED);
    }

    for (MockItem child : item.getChildren()) {
      if (MockType.Document == child.getType()) {
        docs.add(MockClientFactory.createDocument(webUrl, child.getName(),
            feedType, true));
      } else if (child.isContainer()) {
        final String childUrl = MockClientFactory.makeUrl(webUrl,
            child.getName());
        appendDocuments(docs, childUrl, child, feedType, username);
      }
    }
  }

  /**
   * Adds the child folders of {@link MockItem} to a list of {@link Folder}.
   *
   * @param folders The list collection to append the folders to
   * @param webUrl The username requesting access
   * @param item The {@link MockItem} that contains the folders to add the list
   * @param username The user requesting access
   * @throws AxisFault when the user is not authorized
   */
  private void appendFolders(List<Folder> folders, String webUrl,
      MockItem item, String username) throws SharepointException, AxisFault {
    if (!item.hasPermission(username)) {
      throw new AxisFault(SPConstants.UNAUTHORIZED);
    }

    for (MockItem child : item.getChildren()) {
      if (MockType.Folder == child.getType()) {
        folders.add(MockClientFactory.createFolder(webUrl, child.getName()));
      }
      if (child.isContainer()) {
        final String childUrl = MockClientFactory.makeUrl(webUrl,
            child.getName());
        appendFolders(folders, childUrl, child, username);
      }
    }
  }
}
