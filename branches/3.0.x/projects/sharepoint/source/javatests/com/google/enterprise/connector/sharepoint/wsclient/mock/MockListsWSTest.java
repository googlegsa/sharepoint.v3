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

package com.google.enterprise.connector.sharepoint.wsclient.mock;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.ListsHelper;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.wsclient.client.ClientFactory;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDataWS;
import com.google.enterprise.connector.sharepoint.wsclient.mock.XmlClientFactory;
import com.google.enterprise.connector.sharepoint.state.Folder;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;

import junit.framework.TestCase;

import java.util.List;

public class MockListsWSTest extends TestCase {
  SharepointClientContext sharepointClientContext;
  ListState testList;
  ListState testListUsernameAt;
  ListState testListUsernameSlash;
  String testListUrl;
  String testListUsernameAtUrl;
  String testListUsernameSlashUrl;
  ListsHelper listsHelper;
  final XmlClientFactory clientFactory = new XmlClientFactory(
      "source/javatests/data/testweb.xml");

  protected void setUp() throws Exception {
    sharepointClientContext = TestConfiguration.initContext(clientFactory);
    assertNotNull(sharepointClientContext);
    sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
    sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);
    sharepointClientContext.setBatchHint(10);
    sharepointClientContext.setFeedType(FeedType.CONTENT_FEED);
    sharepointClientContext.setFeedUnPublishedDocuments(false);

    listsHelper = new ListsHelper(sharepointClientContext);

    final SiteDataWS siteDataWS =
        clientFactory.getSiteDataWS(sharepointClientContext);

    final GlobalState state = new GlobalState(clientFactory,
        TestConfiguration.googleConnectorWorkDir, FeedType.CONTENT_FEED);
    final WebState ws =
        state.makeWebState(sharepointClientContext, "http://example.com");

    final List listCollection = siteDataWS.getNamedLists(ws);
    assertNotNull(listCollection);

    testListUrl = "http://example.com/Web1/List1";
    testListUsernameAtUrl = "http://example.com/WebAuth/ListAt";
    testListUsernameSlashUrl = "http://example.com/WebAuth/ListSlash";

    for (int i = 0; i < listCollection.size(); i++) {
      final ListState baseList = (ListState) listCollection.get(i);
      if (testListUrl.equals(baseList.getListURL())) {
        testList = baseList;
      } else if (testListUsernameAtUrl.equals(baseList.getListURL())) {
        testListUsernameAt = baseList;
      } else if (testListUsernameSlashUrl.equals(baseList.getListURL())) {
        testListUsernameSlash = baseList;
      }
    }
    assertNotNull(testList);
    assertNotNull(testListUsernameAt);
    assertNotNull(testListUsernameSlash);
  }

  /**
   * A simple test for getListItems using the mock interface.
   */
  public void testGetListItems() {
    final List items = listsHelper.getListItems(testList, null, null, null);
    assertNotNull(items);
    assertTrue(1 == items.size());

    SPDocument doc = (SPDocument) items.get(0);
    assertTrue((testListUrl + "/Doc1").equals(doc.getUrl()));
  }

  /**
   * Tests that the connector returns the correct folder hierarchy.
   */
  public void testGetFolderHierarchy() {
    final List items = listsHelper.getSubFoldersRecursively(testList,
        null, null);
    assertNotNull(items);
    assertEquals(1, items.size());

    Folder folder = (Folder) items.get(0);
    assertEquals(testListUrl + "/Folder1", folder.getPath());
  }

  /**
   * Tests that getListItems sends requests using different user name formats
   * for a list that requires the username in the user@domain format.
   */
  public void testUsernameAtFormat() {
    verifyGetListItemsAccessSuccess(testListUsernameAt, "example.com\\good",
        testListUsernameAtUrl + "/SampleDoc");
    verifyGetListItemsAccessSuccess(testListUsernameAt, "good@example.com",
        testListUsernameAtUrl + "/SampleDoc");
    verifyGetListItemsAccessFailure(testListUsernameAt, "example.com\\bad",
        testListUsernameAtUrl + "/SampleDoc");
    verifyGetListItemsAccessFailure(testListUsernameAt, "bad@example.com",
        testListUsernameAtUrl + "/SampleDoc");
  }

  /**
   * Tests that getListItems sends requests using different user name formats
   * for a list that requires the username in the domain\\user format.
   */
  public void testUsernameSlashFormat() {
    verifyGetListItemsAccessSuccess(testListUsernameSlash, "example.com\\good",
        testListUsernameSlashUrl + "/SampleDoc");
    verifyGetListItemsAccessSuccess(testListUsernameSlash, "good@example.com",
        testListUsernameSlashUrl + "/SampleDoc");
    verifyGetListItemsAccessFailure(testListUsernameSlash, "example.com\\bad",
        testListUsernameSlashUrl + "/SampleDoc");
    verifyGetListItemsAccessFailure(testListUsernameSlash, "bad@example.com",
        testListUsernameSlashUrl + "/SampleDoc");
  }

  /**
   * Verifies that a call to getListItems succeeds.
   */
  private void verifyGetListItemsAccessSuccess(ListState list, 
      String username, String expectedUrl) {
    listsHelper.setUsername(username);
    final List items = listsHelper.getListItems(list, null, null, null);
    assertNotNull(items);
    assertEquals(1, items.size());

    SPDocument doc = (SPDocument) items.get(0);
    assertEquals(expectedUrl, doc.getUrl());
  }

  /**
   * Verifies that a call to getListItems fails.
   */
  private void verifyGetListItemsAccessFailure(ListState list, 
      String username, String expectedUrl) {
    listsHelper.setUsername(username);
    final List items = listsHelper.getListItems(list, null, null, null);
    assertNotNull(items);
    assertEquals(0, items.size());
  }
}
