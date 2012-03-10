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

package com.google.enterprise.connector.sharepoint.wsclient;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SharepointClient;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointTraversalManager;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.sharepoint.wsclient.handlers.FileTransport;
import com.google.enterprise.connector.sharepoint.wsclient.WsUtil;
import com.google.enterprise.connector.spi.RepositoryException;

import org.apache.axis.client.Call;

import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.TestCase;

public class ListsWSTest extends TestCase {
  SharepointClientContext sharepointClientContext;
  ListsWS listWS;
  ListState testList, categoriesList, postsList, commentsList;
  Calendar lastModified;
  String lastItemID;
  String lastItemURL;

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

    System.out.println("Initializing ListsWS ...");
    this.listWS = new ListsWS(this.sharepointClientContext);

    System.out.println("Creating test List ...");
    final SiteDataWS siteDataWS = new SiteDataWS(this.sharepointClientContext);

    final GlobalState state = new GlobalState(
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

  public final void testListsWS() throws Throwable {
    System.out.println("Testing ListsWS(SharepointClientContext, siteName)...");
    sharepointClientContext.setSiteURL(TestConfiguration.Site1_URL);
    this.listWS = new ListsWS(this.sharepointClientContext);
    assertNotNull(this.listWS);
    System.out.println("[ ListsWS(SharepointClientContext, siteName) ] Test Passed");
  }

  public void testGetAttachments() throws MalformedURLException,
      RepositoryException {
    System.out.println("Testing getAttachments()...");
    final SPDocument doc = new SPDocument("1", "url1", new GregorianCalendar(
        2007, 1, 1), SPConstants.NO_AUTHOR, SPConstants.NO_OBJTYPE,
        SPConstants.PARENT_WEB_TITLE, FeedType.CONTENT_FEED, SPType.SP2007);
    final List items = this.listWS.getAttachments(this.testList, doc);
    assertNotNull(items);
    System.out.println("[ getAttachments() ] Test Passed.");
  }

  public void testGetFolderHierarchy() throws MalformedURLException,
      RepositoryException {
    System.out.println("Testing getFolderHierarchy()...");
    final List items = this.listWS.getSubFoldersRecursively(this.testList, null, null);
    assertNotNull(items);
    System.out.println("[ getFolderHierarchy() ] Test Passed.");
  }

  public void testGetListItemsAtFolderLevel() throws MalformedURLException,
      RepositoryException {
    System.out.println("Testing getListItemsAtFolderLevel()...");
    final List items = this.listWS.getListItemsAtFolderLevel(this.testList, null, null, null);
    assertNotNull(items);
    System.out.println("[ getListItemsAtFolderLevel() ] Test Passed.");
  }

  public void testGetListItems() throws MalformedURLException,
      RepositoryException {
    System.out.println("Testing getListItems()...");
    final List items = this.listWS.getListItems(this.testList, null, null, null);
    assertNotNull(items);
    System.out.println("[ getListItems() ] Test Passed.");
  }

  /**
   * Test for categories list items URLs of blog site
   */

  public void testGetListItemsForCategoriesInBlogSite()
      throws MalformedURLException, RepositoryException {
    final List<SPDocument> items = this.listWS.getListItems(this.categoriesList, null, null, null);
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
    final List<SPDocument> items = this.listWS.getListItems(this.commentsList, null, null, null);
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
    final List<SPDocument> items = this.listWS.getListItems(this.postsList, null, null, null);
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
    System.out.println("Testing getListItemChangesSinceToken()...");
    // Following lines can be used for testing with specific change token
    // values, like something from state file
    testList.saveNextChangeTokenForWSCall("1;3;e7aa22c4-a3da-4fcd-b53d-c9a33f78f85d;634505447504230000;4947");
    testList.commitChangeTokenForWSCall();
    final List items = this.listWS.getListItemChangesSinceToken(this.testList, null);
    assertNotNull(items);
    assertEquals(3, items.size());
  }

  public void testGetListItemChangesSinceTokenWithInvalidChangeToken()
      throws MalformedURLException, RepositoryException {
    System.out.println("Testing getListItemChangesSinceToken()...");
    testList.saveNextChangeTokenForWSCall("1;3;ca894ebb-41ed-44ee-9f09-0e8cb578bab6;1;1");
    testList.commitChangeTokenForWSCall();
    try {
      final List items = this.listWS.getListItemChangesSinceToken(this.testList, null);
    } catch (Exception e) {
      assertTrue(e instanceof SharepointException);
      assertNull(testList.getNextChangeTokenForSubsequectWSCalls());
      assertNull(testList.getChangeTokenForWSCall());
      assertNull(testList.getLastDocForWSRefresh());
      assertNull(testList.getCrawlQueue());
      assertFalse(testList.isAclChanged());
      assertEquals(0, testList.getLastDocIdCrawledForAcl());
      final List items = this.listWS.getListItemChangesSinceToken(this.testList, null);
      assertNotNull(items);
    }
  }

  public void testGetListItemsForPublishedContent()
      throws MalformedURLException, RepositoryException {
    System.out.println("Testing getListItems() by setting FeedUnPublishedDocuments to false.");
    this.listWS = null;
    this.sharepointClientContext.setFeedUnPublishedDocuments(false);
    this.listWS = new ListsWS(this.sharepointClientContext);
    final List items = this.listWS.getListItems(this.testList, null, null, null);
    assertNotNull(items);
    assertEquals(3, items.size());
    System.out.println("[ getListItems() ] test passed.");
  }

  public void testGetListItemsForUnPublishedContent()
      throws MalformedURLException, RepositoryException {
    System.out.println("Testing getListItems() by setting FeedUnPublishedDocuments true");
    this.listWS = null;
    this.sharepointClientContext.setFeedUnPublishedDocuments(true);
    this.listWS = new ListsWS(this.sharepointClientContext);
    final List items = this.listWS.getListItems(this.testList, null, null, null);
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
    SharepointClient sharepointClient = new SharepointClient(sharepointClientContext);
    items = getSite1TestListChangesSinceToken();
    assertNotNull(items);
    assertTrue(items.size() > 0);
  }
  
  private List getSite1TestListChangesSinceToken() throws SharepointException {
    sharepointClientContext.setSiteURL(TestConfiguration.Site1_URL);
    listWS = new ListsWS(sharepointClientContext);
    assertNotNull(listWS);
    return listWS.getListItemChangesSinceToken(testList, null);
  }
}
