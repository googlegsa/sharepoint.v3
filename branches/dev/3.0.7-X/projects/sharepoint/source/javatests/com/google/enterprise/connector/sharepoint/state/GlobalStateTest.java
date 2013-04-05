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

package com.google.enterprise.connector.sharepoint.state;

import com.google.common.io.Files;
import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.wsclient.soap.SPClientFactory;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import org.joda.time.DateTime;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

public class GlobalStateTest extends TestCase {

  /**
   * Directory where the GlobalState file will be stored. Replace this with a
   * suitable temporary directory if not running on a Linux or Unix-like system.
   */
  private static final String TMP_DIR = "c:";
  SharepointClientContext sharepointClientContext;
  private SPClientFactory clientFactory = new SPClientFactory();

	public void setUp() throws Exception {
    sharepointClientContext = TestConfiguration.initContext();
  }

  /**
   * Checks if the current platform is Windows.
   *
   * @return true if the current platform is Windows; false otherwise.
   */
  public static boolean isWindows() {
    String os = System.getProperty("os.name").toLowerCase();
    return (os.indexOf("win") >= 0);
  }

  /**
   * This will test 1. adding & deleting of list and web states in GlobalState
   * 2. Garbage collection and DELETE feed construction which are controlled
   * through start/endRecrawl(()
   *
   * @throws SharepointException
   */
  public void testStateMaintenance() throws SharepointException {
    GlobalState state = new GlobalState(clientFactory,
        TestConfiguration.googleConnectorWorkDir, FeedType.METADATA_URL_FEED);
    WebState web1 = state.makeWebState(sharepointClientContext, TestConfiguration.Site1_URL);
    // Using invalid ListURL so that they could be deleted from state.
    // Connector checks for an HTTP response of 404 before deletion.
    ListState list1 = new ListState(TestConfiguration.Site1_List1_GUID,
        "No Title", SPConstants.DOC_LIB, Calendar.getInstance(),
        SPConstants.NO_TEMPLATE, TestConfiguration.Site1_List1_URL + "X", web1);
    web1.AddOrUpdateListStateInWebState(list1, new DateTime());
    ListState list2 = new ListState(TestConfiguration.Site1_List2_GUID,
        "No Title", SPConstants.DOC_LIB, Calendar.getInstance(),
        SPConstants.NO_TEMPLATE, TestConfiguration.Site1_List2_URL + "X", web1);
    web1.AddOrUpdateListStateInWebState(list2, new DateTime());

    WebState web2 = state.makeWebState(sharepointClientContext, TestConfiguration.Site2_URL);
    ListState list3 = new ListState(TestConfiguration.Site2_List1_GUID,
        "No Title", SPConstants.DOC_LIB, Calendar.getInstance(),
        SPConstants.NO_TEMPLATE, TestConfiguration.Site2_List1_URL, web2);
    web2.AddOrUpdateListStateInWebState(list3, new DateTime());
    ListState list4 = new ListState(TestConfiguration.Site2_List2_GUID,
        "No Title", SPConstants.DOC_LIB, Calendar.getInstance(),
        SPConstants.NO_TEMPLATE, TestConfiguration.Site2_List2_URL, web2);
    web2.AddOrUpdateListStateInWebState(list4, new DateTime());

    assertEquals(web1, state.lookupWeb(TestConfiguration.Site1_URL, sharepointClientContext));
    assertEquals(list1, state.lookupList(TestConfiguration.Site1_URL, TestConfiguration.Site1_List1_GUID));
    assertEquals(list2, state.lookupList(TestConfiguration.Site1_URL, TestConfiguration.Site1_List2_GUID));

    state.setBFullReCrawl(true);
    // This will ensure that all the lists/webs will be marked non-existent
    state.startRecrawl();

    // TODO: The test comments say that Site1_List1_GUID and Site1_List2_GUID should not be in 
    // the map but the code says otherwise since Site1_URL contains 2 lists and neither list 
    // is the site default page.

    // This will remove all those lists/webs which are non-existent and not
    // found on SharePoint (HTTP 404)
    state.endRecrawl(sharepointClientContext);

    // Parent Web, though, marked as non-existent during startRecrawl, might
    // not be removed, because an HTTP 404 will might not be received for it. So,
    // we assert only for lists.
    assertNotNull(state.lookupList(TestConfiguration.Site1_URL, TestConfiguration.Site1_List1_GUID));
    assertNotNull(state.lookupList(TestConfiguration.Site1_URL, TestConfiguration.Site1_List2_GUID));

    // ////////////////////////////////////
    // NOW, CHECK IN CASE OF CONTENT FEED
    sharepointClientContext.setFeedType(FeedType.CONTENT_FEED);

    web1.AddOrUpdateListStateInWebState(list1, new DateTime());
    web1.AddOrUpdateListStateInWebState(list2, new DateTime());

    assertEquals(web1, state.lookupWeb(TestConfiguration.Site1_URL, sharepointClientContext));
    assertEquals(list1, state.lookupList(TestConfiguration.Site1_URL, TestConfiguration.Site1_List1_GUID));
    assertEquals(list2, state.lookupList(TestConfiguration.Site1_URL, TestConfiguration.Site1_List2_GUID));

    state.setBFullReCrawl(true);
    // This will ensure that all the lists/webs will be marked non-existent
    state.startRecrawl();

    assertNotNull(list1.getCrawlQueue());
    assertEquals(1, list1.getCrawlQueue().size());
    assertEquals(false, list1.isExisting());
    assertNotNull(list2.getCrawlQueue());
    assertEquals(1, list2.getCrawlQueue().size());
    assertEquals(false, list2.isExisting());

    list1.setBiggestID(2);
    list2.setBiggestID(3);
    sharepointClientContext.setBatchHint(10);

    // This will create DELETE feeds for documents that have been sent
    // from the non-existent lists.
    state.endRecrawl(sharepointClientContext);

    // Since, the list is of type DocLib, no extra documents will be created
    // for attachments or alerts. Total no. of document created should be
    // 2(biggestID)+1(List as document) = 3
    assertEquals(3, list1.getCrawlQueue().size());
    for (SPDocument doc : list1.getCrawlQueue()) {
      assertEquals(ActionType.DELETE, doc.getAction());
    }

    assertEquals(4, list2.getCrawlQueue().size());
    for (SPDocument doc : list2.getCrawlQueue()) {
      assertEquals(ActionType.DELETE, doc.getAction());
    }
  }

  /**
   * This is to ensure that no information is lost while saving and loading the
   * state file.
   *
   * @throws SharepointException
   */
  public final void testStateReload() throws SharepointException {
    System.out.println("Testing the basic functionalities of an stateful object");

    final GlobalState state1 = createGlobalState();
    state1.saveState();

    final GlobalState state2 = new GlobalState(clientFactory,
        TestConfiguration.googleConnectorWorkDir, FeedType.CONTENT_FEED);
    state2.loadState();

    verifyGlobalStatesAreEqual(state1, state2);
  }
  
  private GlobalState createGlobalState() throws SharepointException {
    final GlobalState state1 = new GlobalState(clientFactory,
        TestConfiguration.googleConnectorWorkDir, FeedType.CONTENT_FEED);

    WebState ws = state1.makeWebState(sharepointClientContext, TestConfiguration.Site1_URL);
    DateTime dt = new DateTime();
    ws.setInsertionTime(dt);
    ws.setLastCrawledDateTime(Util.formatDate(Calendar.getInstance(), Util.TIMEFORMAT_WITH_ZONE));

    final ListState list = new ListState("X", "X", SPConstants.GENERIC_LIST,
        Calendar.getInstance(), "X", "X", ws);
    list.setLastCrawledDateTime(Util.formatDate(Calendar.getInstance(), Util.TIMEFORMAT_WITH_ZONE));
    list.setAttchmnts(new StringBuffer("X"));
    list.addToDeleteCache("X");
    list.setIDs(new StringBuffer("X"));

    final SPDocument doc = new SPDocument("DocID", "DocURL",
        new GregorianCalendar(2007, 1, 1), SPConstants.NO_AUTHOR,
        SPConstants.NO_OBJTYPE, SPConstants.PARENT_WEB_TITLE,
        TestConfiguration.feedType, ws.getSharePointType());
    doc.setParentFolder(new Folder("/X/XX", "1"));

    list.setLastDocProcessed(doc);
    ws.AddOrUpdateListStateInWebState(list, dt);
    ws.setCurrentList(list);
    state1.setCurrentWeb(ws);
    state1.setLastCrawledWeb(ws);
    state1.setLastCrawledList(list);
    return state1;
  }
  
  private void verifyGlobalStatesAreEqual(final GlobalState state1,
      final GlobalState state2) {
    assertEquals(state1.getFeedType(), state2.getFeedType());
    assertEquals(state1.getLastCrawledList(), state2.getLastCrawledList());
    assertEquals(state1.getLastCrawledWeb(), state2.getLastCrawledWeb());
    assertEquals(state1.getAllWebStateSet().size(), state2.getAllWebStateSet().size());

    for (WebState web : state2.getAllWebStateSet()) {
      WebState tmpWeb1 = state1.lookupWeb(web.getPrimaryKey(), null);
      WebState tmpWeb2 = state2.lookupWeb(web.getPrimaryKey(), null);
      assertNotNull(tmpWeb1);
      assertNotNull(tmpWeb2);
      assertEquals(tmpWeb1.getPrimaryKey(), tmpWeb2.getPrimaryKey());
      assertEquals(tmpWeb1.getWebUrl(), tmpWeb2.getWebUrl());
      assertEquals(tmpWeb1.getInsertionTimeString(), tmpWeb2.getInsertionTimeString());
      assertEquals(tmpWeb1.getLastCrawledDateTime(), tmpWeb2.getLastCrawledDateTime());
      assertEquals(tmpWeb1.getTitle(), tmpWeb2.getTitle());
      assertEquals(tmpWeb1.getSharePointType(), tmpWeb2.getSharePointType());
      assertEquals(tmpWeb1.getAllListStateSet().size(), tmpWeb2.getAllListStateSet().size());

      for (ListState lst : web.getAllListStateSet()) {
        ListState tmpList1 = state1.lookupList(web.getPrimaryKey(), lst.getPrimaryKey());
        ListState tmpList2 = state2.lookupList(web.getPrimaryKey(), lst.getPrimaryKey());
        assertNotNull(tmpList1);
        assertNotNull(tmpList2);
        assertEquals(tmpList1.getPrimaryKey(), tmpList2.getPrimaryKey());
        assertEquals(tmpList1.getListURL(), tmpList2.getListURL());
        assertEquals(tmpList1.getLastModString(), tmpList2.getLastModString());
        assertEquals(tmpList1.getLastCrawledDateTime(), tmpList2.getLastCrawledDateTime());
        assertEquals(tmpList1.getType(), tmpList2.getType());
        assertEquals(tmpList1.getChangeTokenForWSCall(), tmpList2.getChangeTokenForWSCall());
        assertEquals(tmpList1.getNextChangeTokenForSubsequectWSCalls(), tmpList2.getNextChangeTokenForSubsequectWSCalls());
        assertEquals(tmpList1.getBiggestID(), tmpList2.getBiggestID());
        assertEquals(tmpList1.getAttchmnts().toString(), tmpList2.getAttchmnts().toString());
        assertEquals(tmpList1.getDeleteCache(), tmpList2.getDeleteCache());
        assertEquals(tmpList1.getIDs().toString(), tmpList2.getIDs().toString());
        assertEquals(tmpList1.getLastDocForWSRefresh(), tmpList2.getLastDocForWSRefresh());

        // FIXME Change it as per the recent change in the way folder
        // information are stored. Also, add check for RenamedFolder
        // assertEquals(tmpList1.getLastDocForWSRefresh().getParentFolder(),
        // tmpList2.getLastDocForWSRefresh().getParentFolder());

        assertEquals(tmpList1.getLastDocForWSRefresh().getLastMod(), tmpList2.getLastDocForWSRefresh().getLastMod());
        assertEquals(tmpList1.getLastDocForWSRefresh().getAction(), tmpList2.getLastDocForWSRefresh().getAction());
      }
    }
  }

  /**
   * Make sure that the getCircularIterator() call works properly (since, if it
   * doesn't, the connector will fail to pick up new or changed SharePoint.
   * documents) The sequence in which WebStates are added to the GlobalState
   * will govern the way they will be accessed through the iterator. Recently
   * added one will be accessed first.
   *
   * @throws InterruptedException, {@link SharepointException}
   */
  public void testCircularIterators() throws SharepointException,
      InterruptedException {
    WebState[] webs = new WebState[3];

    // create Web State inside Global state
    final GlobalState state = new GlobalState(clientFactory,
        TestConfiguration.googleConnectorWorkDir, TestConfiguration.feedType);
    webs[0] = state.makeWebState(sharepointClientContext, TestConfiguration.Site1_URL);
    Thread.sleep(1000);
    webs[1] = state.makeWebState(sharepointClientContext, TestConfiguration.Site2_URL);
    Thread.sleep(1000);
    webs[2] = state.makeWebState(sharepointClientContext, TestConfiguration.Site3_URL);
    // Current ordering will be as follows -> webs[2], webs[1], webs[0]

    state.setCurrentWeb(webs[1]);
    Iterator<WebState> itr = state.getCircularIterator();
    assertEquals(webs[1], itr.next());
    assertEquals(webs[0], itr.next());
    assertEquals(webs[2], itr.next());
  }

  /**
   * Test to check that web states are ordered in the descending order of
   * insertion time
   *
   * @throws SharepointException
   */
  public void testUpdateListState() throws SharepointException {
    final GlobalState state = new GlobalState(clientFactory,
        TestConfiguration.googleConnectorWorkDir, TestConfiguration.feedType);

    WebState ws1 = new WebState(clientFactory, sharepointClientContext,
        TestConfiguration.Site1_URL);
    ws1.setInsertionTime(new DateTime(2009, 9, 05, 10, 25, 36, 100));
    state.addOrUpdateWebStateInGlobalState(ws1);

    WebState ws2 = new WebState(clientFactory, sharepointClientContext,
        TestConfiguration.Site2_URL);
    ws2.setInsertionTime(new DateTime(2009, 9, 07, 10, 25, 36, 100));
    state.addOrUpdateWebStateInGlobalState(ws2);

    WebState ws3 = new WebState(clientFactory, sharepointClientContext,
        TestConfiguration.Site3_URL);
    ws3.setInsertionTime(new DateTime(2009, 9, 06, 10, 25, 36, 100));
    state.addOrUpdateWebStateInGlobalState(ws3);

    assertEquals(3, state.getAllWebStateSet().size());
    assertEquals(ws2, state.dateMap.first());
  }

  private List<String> getFileListForFolder(String path) {
    FilenameFilter fileFilter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return new File(dir, name).isFile();
      }
    };
    
    List<String> files = new ArrayList<String>();
    Collections.addAll(files, new File(path).list(fileFilter));
    Collections.sort(files);
    return files;
  }
  
  private void writeStringToFile(String folderPath, String fileName,
      String fileContents) throws IOException {
    Files.write(fileContents.getBytes(), new File(folderPath, fileName));
  }
  
  /**
   * Tests that creating a new statefile works when no existing statefile
   * is present. It verifies that no additional files are left over and
   * that the state file can be loaded and matches the statefile that was
   * saved.
   *
   * @throws SharepointException
   */
  public void testCreateNewStatefile() throws SharepointException {
    GlobalState.forgetState(TestConfiguration.googleWorkDir);
    List<String> beforeFiles = getFileListForFolder(
        TestConfiguration.googleWorkDir);
    assertNotNull(beforeFiles);
    assertFalse(new File(TestConfiguration.googleWorkDir,
        SPConstants.CONNECTOR_STATEFILE_NAME).exists());
    
    final GlobalState state1 = createGlobalState();
    state1.saveState();
    assertTrue(new File(TestConfiguration.googleWorkDir,
        SPConstants.CONNECTOR_STATEFILE_NAME).exists());

    List<String> afterFiles = getFileListForFolder(
        TestConfiguration.googleWorkDir);
    afterFiles.remove(SPConstants.CONNECTOR_STATEFILE_NAME);
    assertEquals(beforeFiles, afterFiles);

    final GlobalState state2 = new GlobalState(clientFactory,
        TestConfiguration.googleWorkDir, FeedType.CONTENT_FEED);
    state2.loadState();
    verifyGlobalStatesAreEqual(state1, state2);
  }

  /**
   * Tests that creating a new statefile works when an existing statefile
   * is already present. It verifies that no additional files are left over
   * and that the state file can be loaded and matches the statefile that
   * was saved.
   *
   * @throws SharepointException
   */
  public void testOverwriteStatefile() throws Exception {
    GlobalState.forgetState(TestConfiguration.googleWorkDir);
    List<String> beforeFiles = getFileListForFolder(
        TestConfiguration.googleWorkDir);
    assertNotNull(beforeFiles);
    writeStringToFile(TestConfiguration.googleWorkDir,
        SPConstants.CONNECTOR_STATEFILE_NAME,
        "<?xml version='1.0' encoding='UTF-8'?><State></State>");
    assertTrue(new File(TestConfiguration.googleWorkDir,
        SPConstants.CONNECTOR_STATEFILE_NAME).exists());

    // Verify that the current statefile is empty.
    final GlobalState state0 = new GlobalState(clientFactory,
        TestConfiguration.googleWorkDir, FeedType.CONTENT_FEED);
    state0.loadState();
    verifyGlobalStateIsEmptyContentFeed(state0);

    final GlobalState state1 = createGlobalState();
    verifyGlobalStateIsNotEmpty(state1);
    state1.saveState();
    assertTrue(new File(TestConfiguration.googleWorkDir,
        SPConstants.CONNECTOR_STATEFILE_NAME).exists());

    List<String> afterFiles = getFileListForFolder(
        TestConfiguration.googleWorkDir);
    afterFiles.remove(SPConstants.CONNECTOR_STATEFILE_NAME);
    assertEquals(beforeFiles, afterFiles);

    final GlobalState state2 = new GlobalState(clientFactory,
        TestConfiguration.googleWorkDir, FeedType.CONTENT_FEED);
    state2.loadState();
    verifyGlobalStatesAreEqual(state1, state2);
  }

  /**
   * Verifies that a {@link GlobalState} is empy and that it's
   * using a content feed type.
   */
  private void verifyGlobalStateIsEmptyContentFeed(final GlobalState state) {
    assertEquals(FeedType.CONTENT_FEED, state.getFeedType());
    assertNull(state.getLastCrawledList());
    assertNull(state.getLastCrawledWeb());
    assertEquals(0, state.getAllWebStateSet().size());
  }

  /**
   * Verifies that a {@link GlobalState} is not empy.
   */
  private void verifyGlobalStateIsNotEmpty(final GlobalState state) {
    assertNotNull(state.getLastCrawledList());
    assertNotNull(state.getLastCrawledWeb());
    assertTrue(0 < state.getAllWebStateSet().size());
  }
  
  /**
   * Tests that creating a new statefile works when an existing statefile 
   * is locked so that it cannot be updated.
   * Note: This test has some specific checks for Windows since it handles 
   * files a little different than Unix.
   */
  public void testOverwriteStatefileWithPrevLocked() throws Exception {
    GlobalState.forgetState(TestConfiguration.googleWorkDir);
    List<String> beforeFiles =
        getFileListForFolder(TestConfiguration.googleWorkDir);
    assertNotNull(beforeFiles);
    writeStringToFile(TestConfiguration.googleWorkDir, 
        SPConstants.CONNECTOR_STATEFILE_NAME,
        "<?xml version='1.0' encoding='UTF-8'?><State></State>");
    assertTrue(new File(TestConfiguration.googleWorkDir,
        SPConstants.CONNECTOR_STATEFILE_NAME).exists());

    // Lock the statefile so that it cannot be updated.
    FileInputStream in = new FileInputStream(new File(
        TestConfiguration.googleWorkDir,
        SPConstants.CONNECTOR_STATEFILE_NAME));
    assertNotNull(in);
    
    // Update the statefile.
    final GlobalState state1 = createGlobalState();
    try {
      state1.saveState();
      assertFalse("GlobalState.saveState should have failed.", isWindows());
    } catch (SharepointException e) {
      assertTrue("Expect GlobalState.saveState to failure in Windows",
          e.getMessage().startsWith("Save state failed"));
      assertTrue(isWindows());
    }

    // Release the lock on the statefile.
    in.close();

    assertTrue(new File(TestConfiguration.googleWorkDir,
        SPConstants.CONNECTOR_STATEFILE_NAME).exists());
    List<String> afterFiles =
        getFileListForFolder(TestConfiguration.googleWorkDir);
    afterFiles.remove(SPConstants.CONNECTOR_STATEFILE_NAME);

    final GlobalState tempState = createGlobalState();
    if (isWindows()) {
      // Since the state was not updated, we have left a dangling temp
      // state file.
      assertTrue(afterFiles.contains(SPConstants.CONNECTOR_TEMPFILE_NAME));
      afterFiles.remove(SPConstants.CONNECTOR_TEMPFILE_NAME);

      // Load the temp state file here before calling loadState() below 
      // because loadState will delete the temp file if it exists.
      tempState.loadState(tempState.getStateFileLocation(
          SPConstants.CONNECTOR_TEMP_EXT));
    }
  
    assertEquals(beforeFiles, afterFiles);
  
    final GlobalState state2 = new GlobalState(clientFactory,
        TestConfiguration.googleWorkDir, FeedType.CONTENT_FEED);
    state2.loadState();
    if (isWindows()) {
      verifyGlobalStateIsEmptyContentFeed(state2);
      verifyGlobalStatesAreEqual(state1, tempState);
    } else {
      verifyGlobalStatesAreEqual(state1, state2);
    }
  }
}
