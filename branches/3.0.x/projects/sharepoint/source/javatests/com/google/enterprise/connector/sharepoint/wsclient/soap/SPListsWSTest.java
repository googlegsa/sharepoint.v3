// Copyright 2007 Google Inc.
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

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.Attribute;
import com.google.enterprise.connector.sharepoint.client.ListsHelper;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClient;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointTraversalManager;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.sharepoint.wsclient.handlers.FileTransport;
import com.google.enterprise.connector.sharepoint.wsclient.WsUtil;
import com.google.enterprise.connector.spi.RepositoryException;

import org.apache.axis.client.Call;
import org.apache.axis.transport.http.HTTPTransport;

import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.TestCase;

public class SPListsWSTest extends TestCase {
  SharepointClientContext sharepointClientContext;
  ListsHelper listsHelper;
  ListState testList, categoriesList, postsList, commentsList;
  Calendar lastModified;
  String lastItemID;
  String lastItemURL;
  SPClientFactory clientFactory = new SPClientFactory();

  protected void setUp() throws Exception {
    System.out.println("\n...Setting Up...");
    System.out.println("Initializing SharepointClientContext ...");
    this.sharepointClientContext = TestConfiguration.initContext();

    assertNotNull(this.sharepointClientContext);
    sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
    sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);
    sharepointClientContext.setBatchHint(10);
    sharepointClientContext.setFeedType(FeedType.CONTENT_FEED);
    sharepointClientContext.setFeedUnPublishedDocuments(false);

    System.out.println("Initializing SPListsWS ...");
    listsHelper = new ListsHelper(this.sharepointClientContext);

    System.out.println("Creating test List ...");
    final SPSiteDataWS siteDataWS = new SPSiteDataWS(this.sharepointClientContext);

    final GlobalState state = new GlobalState(clientFactory,
        TestConfiguration.googleConnectorWorkDir, FeedType.CONTENT_FEED);
    WebState ws = state.makeWebState(sharepointClientContext, TestConfiguration.sharepointUrl);

    final List listCollection = siteDataWS.getNamedLists(ws);
    assertNotNull(listCollection);

    for (int i = 0; i < listCollection.size(); i++) {
      final ListState baseList = (ListState) listCollection.get(i);
      if (baseList.getPrimaryKey().equals(TestConfiguration.Site1_List1_GUID)) {
        this.testList = baseList;
      }
      if (baseList.getBaseTemplate().equals(SPConstants.BT_CATEGORIES)) {
        this.categoriesList = baseList;
      } else if (baseList.getBaseTemplate().equals(SPConstants.BT_COMMENTS)) {
        this.commentsList = baseList;
      } else if (baseList.getBaseTemplate().equals(SPConstants.BT_POSTS)) {
        this.postsList = baseList;
      }
    }
    System.out.println("Test List being used: " + this.testList.getPrimaryKey());

    // A call to Call.initialize is required to reset the Axis HTTP transport
    // back to the orginal set by Axis.
    Call.initialize();
  }

  public void tearDown() throws Exception {
    // Reset the Axis transport protocol back to HTTP. This is needed
    // since some tests change the Axis transport protocol to a file.
    Call.setTransportForProtocol("http", HTTPTransport.class);
  }

  public final void testListsWS() throws Throwable {
    System.out.println("Testing SPListsWS(SharepointClientContext, siteName)...");
    sharepointClientContext.setSiteURL(TestConfiguration.Site1_URL);
    listsHelper = new ListsHelper(this.sharepointClientContext);
    assertNotNull(listsHelper);
    System.out.println("[ SPListsWS(SharepointClientContext, siteName) ] Test Passed");
  }

  public void testGetAttachments() throws MalformedURLException,
      RepositoryException {
    System.out.println("Testing getAttachments()...");
    final SPDocument doc = new SPDocument("1", "url1", new GregorianCalendar(
        2007, 1, 1), SPConstants.NO_AUTHOR, SPConstants.NO_OBJTYPE,
        SPConstants.PARENT_WEB_TITLE, FeedType.CONTENT_FEED, SPType.SP2007);
    final List items = listsHelper.getAttachments(this.testList, doc);
    assertNotNull(items);
    System.out.println("[ getAttachments() ] Test Passed.");
  }

  public void testGetFolderHierarchy() throws MalformedURLException,
      RepositoryException {
    System.out.println("Testing getFolderHierarchy()...");
    final List items = listsHelper.getSubFoldersRecursively(this.testList, null, null);
    assertNotNull(items);
    System.out.println("[ getFolderHierarchy() ] Test Passed.");
  }

  public void testGetListItemsAtFolderLevel() throws MalformedURLException,
      RepositoryException {
    System.out.println("Testing getListItemsAtFolderLevel()...");
    final List items = listsHelper.getListItemsAtFolderLevel(this.testList, null, null, null);
    assertNotNull(items);
    System.out.println("[ getListItemsAtFolderLevel() ] Test Passed.");
  }

  public void testGetListItems() throws MalformedURLException,
      RepositoryException {
    System.out.println("Testing getListItems()...");
    final List items = listsHelper.getListItems(this.testList, null, null, null);
    assertNotNull(items);
    System.out.println("[ getListItems() ] Test Passed.");
  }

  /**
   * Test for categories list items URLs of blog site
   */
  public void testGetListItemsForCategoriesInBlogSite()
      throws MalformedURLException, RepositoryException {
    final List<SPDocument> items = listsHelper.getListItems(this.categoriesList, null, null, null);
    String baseCategoriesExpectedURL = Util.getWebApp(sharepointClientContext.getSiteURL())
        + SPConstants.SLASH
        + this.categoriesList.getListConst()
        + SPConstants.VIEWCATEGORY;

    for (SPDocument item : items) {
      assertTrue(item.getDisplayUrl().startsWith(baseCategoriesExpectedURL));
      assertTrue(item.getUrl().startsWith(baseCategoriesExpectedURL));
    }
  }

  /**
   * Test for comment list items URLs of blog site
   */
  public void testGetListItemsForCommentsInBlogSite()
      throws MalformedURLException, RepositoryException {
    final List<SPDocument> items = listsHelper.getListItems(this.commentsList, null, null, null);
    String baseCommentExpectedURL = Util.getWebApp(sharepointClientContext.getSiteURL())
        + SPConstants.SLASH
        + this.commentsList.getListConst()
        + SPConstants.VIEWCOMMENT;

    for (SPDocument item : items) {
      assertTrue(item.getDisplayUrl().startsWith(baseCommentExpectedURL));
      assertTrue(item.getUrl().startsWith(baseCommentExpectedURL));
    }
  }

  /**
   * Test for posts list items URLs of blog site
   */
  public void testGetListItemsForPostsInBlogSite()
      throws MalformedURLException, RepositoryException {
    final List<SPDocument> items = listsHelper.getListItems(this.postsList, null, null, null);
    String basePostsExpectedURL = Util.getWebApp(sharepointClientContext.getSiteURL())
        + SPConstants.SLASH
        + this.postsList.getListConst()
        + SPConstants.VIEWPOST;
    for (SPDocument item : items) {
      assertTrue(item.getDisplayUrl().startsWith(basePostsExpectedURL));
      assertTrue(item.getUrl().startsWith(basePostsExpectedURL));
    }
  }

  public void testGetListItemChangesSinceToken() throws MalformedURLException,
      RepositoryException {
    testList.saveNextChangeTokenForWSCall(TestConfiguration.validChangeToken);
    testList.commitChangeTokenForWSCall();
    final List items = listsHelper.getListItemChangesSinceToken(this.testList, null);
    assertNotNull(items);
    assertEquals(TestConfiguration.changesSinceToken, items.size());
  }

  public void testGetListItemChangesSinceTokenWithInvalidChangeToken()
      throws MalformedURLException, RepositoryException {
    System.out.println("Testing getListItemChangesSinceToken()...");
    testList.saveNextChangeTokenForWSCall("1;3;ca894ebb-41ed-44ee-9f09-0e8cb578bab6;1;1");
    testList.commitChangeTokenForWSCall();
    try {
      final List items = listsHelper.getListItemChangesSinceToken(this.testList, null);
    } catch (Exception e) {
      assertTrue(e instanceof SharepointException);
      assertNull(testList.getNextChangeTokenForSubsequectWSCalls());
      assertNull(testList.getChangeTokenForWSCall());
      assertNull(testList.getLastDocForWSRefresh());
      assertNull(testList.getCrawlQueue());
      assertFalse(testList.isAclChanged());
      assertEquals(0, testList.getLastDocIdCrawledForAcl());
      assertTrue(this.testList.isNewList());
      final List items = listsHelper.getListItemChangesSinceToken(this.testList, null);
      assertNotNull(items);
    }
  }

  public void testGetListItemsForPublishedContent()
      throws MalformedURLException, RepositoryException {
    System.out.println("Testing getListItems() by setting FeedUnPublishedDocuments to false.");
    this.sharepointClientContext.setFeedUnPublishedDocuments(false);
    listsHelper = new ListsHelper(this.sharepointClientContext);
    final List items = listsHelper.getListItems(this.testList, null, null, null);
    assertNotNull(items);
    assertEquals(3, items.size());
    System.out.println("[ getListItems() ] test passed.");
  }

  public void testGetListItemsForUnPublishedContent()
      throws MalformedURLException, RepositoryException {
    System.out.println("Testing getListItems() by setting FeedUnPublishedDocuments true");
    this.sharepointClientContext.setFeedUnPublishedDocuments(true);
    listsHelper = new ListsHelper(this.sharepointClientContext);
    final List items = listsHelper.getListItems(this.testList, null, null, null);
    assertNotNull(items);
    assertEquals(3, items.size());
    System.out.println("[ getListItems() ] test passed");
  }

  /**
   * Verifies SAX parsing for SOAP responses with duplicate attributes.
   */
  public final void testDuplicateAttributes() throws Throwable {
    Call.setTransportForProtocol("http", FileTransport.class);
    FileTransport.setResponseFileName("source/javatests/data/duplicate-attributes.xml");

    WsUtil.UnregisterSaxClientFactory();
    List items = getSite1TestListChangesSinceToken();
    assertNotNull(items);
    assertEquals(0, items.size());

    // The SAX client factory is registered by the SharepointClient constructor 
    // so we need to create a new instance of SharepointClient.
    SharepointClient sharepointClient = new SharepointClient(clientFactory,
        sharepointClientContext);
    items = getSite1TestListChangesSinceToken();
    assertNotNull(items);
    assertTrue(items.size() > 0);
  }

  private List<SPDocument> getSite1TestListChangesSinceToken()
      throws SharepointException {
    sharepointClientContext.setSiteURL(TestConfiguration.Site1_URL);
    listsHelper = new ListsHelper(sharepointClientContext);
    return listsHelper.getListItemChangesSinceToken(testList, null);
  }

  /**
   * Verifies that the connector can load the supported characters.
   */
  public final void testSupportedCharacters() throws Throwable {
    Call.setTransportForProtocol("http", FileTransport.class);
    FileTransport.setResponseFileName("source/javatests/data/unicode-chars.xml");

    // The SAX client factory is registered by the SharepointClient constructor 
    // so we need to create a new instance of SharepointClient.
    SharepointClient sharepointClient = new SharepointClient(clientFactory,
        sharepointClientContext);

    List<SPDocument> items = getSite1TestListChangesSinceToken();
    assertNotNull(items);
    assertTrue(items.size() > 0);
    
    SPDocument doc = getGetDocumentFromListById(items, 1);
    assertNotNull(doc);
    String docTitle = getDocStringAttribute(doc, "Title");
    assertNotNull(docTitle);
    assertEquals(92, docTitle.length());
    assertEquals(0, docTitle.indexOf('!'));
    assertEquals(28, docTitle.indexOf('A'));
    assertEquals(89, docTitle.indexOf('~'));
    assertEquals(90, docTitle.indexOf('\u2013'));
    assertEquals(91, docTitle.indexOf('\u2014'));

    doc = getGetDocumentFromListById(items, 2);
    assertNotNull(doc);
    docTitle = getDocStringAttribute(doc, "Title");
    assertNotNull(docTitle);
    assertEquals(95, docTitle.length());
    assertEquals(0, docTitle.indexOf('\u00A1'));
    assertEquals(37, docTitle.indexOf('\u00C6'));
    assertEquals(94, docTitle.indexOf('\u00FF'));

    doc = getGetDocumentFromListById(items, 3);
    assertNotNull(doc);
    docTitle = getDocStringAttribute(doc, "Title");
    assertNotNull(docTitle);
    assertEquals(66, docTitle.length());
    assertEquals(0, docTitle.indexOf('\u0410'));
    assertEquals(32, docTitle.indexOf('\u042F'));
    assertEquals(65, docTitle.indexOf('\u044F'));
  }

  private SPDocument getGetDocumentFromListById(List<SPDocument> docs,
      int docId) {
    for (SPDocument doc : docs) {
      if (doc.getDocId().endsWith("|" + docId)) {
        return doc;
      }
    }
    return null;
  }

  private String getDocStringAttribute(SPDocument doc, String attrName) {
    for (Attribute attr : doc.getAllAttrs()) {
      if (attr.getName().equals(attrName)) {
        return (String) attr.getValue();
      }
    }
    return null;
  }
}
