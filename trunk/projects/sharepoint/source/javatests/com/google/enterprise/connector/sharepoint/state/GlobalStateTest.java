//Copyright 2007 Google Inc.

package com.google.enterprise.connector.sharepoint.state;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

import junit.framework.TestCase;

import org.joda.time.DateTime;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

public class GlobalStateTest extends TestCase {

    /**
     * Directory where the GlobalState file will be stored. Replace this with a
     * suitable temporary directory if not running on a Linux or Unix-like
     * system.
     */
    private static final String TMP_DIR = "c:";
    SharepointClientContext sharepointClientContext;

    public void setUp() throws Exception {
        sharepointClientContext = TestConfiguration.initContext();
    }

    /**
     * This will test 1. adding & deleting of list and web states in GlobalState
     * 2. Garbage collection and DELETE feed construction which are controlled
     * through start/endRecrawl(()
     *
     * @throws SharepointException
     */
	public void testStateMaintenance() throws SharepointException {
		GlobalState state = new GlobalState(
				TestConfiguration.googleConnectorWorkDir,
				FeedType.METADATA_URL_FEED);
		WebState web1 = state.makeWebState(sharepointClientContext, TestConfiguration.Site1_URL);
		// Using invalid ListURL so that they could be deleted from state.
		// Connector checks for an HTTP response of 404 before deletion.
		ListState list1 = new ListState(TestConfiguration.Site1_List1_GUID,
				"No Title", SPConstants.DOC_LIB, Calendar.getInstance(),
				SPConstants.NO_TEMPLATE, TestConfiguration.Site1_List1_URL
						+ "X", web1);
		web1.AddOrUpdateListStateInWebState(list1, new DateTime());
		ListState list2 = new ListState(TestConfiguration.Site1_List2_GUID,
				"No Title", SPConstants.DOC_LIB, Calendar.getInstance(),
				SPConstants.NO_TEMPLATE, TestConfiguration.Site1_List2_URL
						+ "X", web1);
		web1.AddOrUpdateListStateInWebState(list2, new DateTime());

		WebState web2 = state.makeWebState(sharepointClientContext, TestConfiguration.Site2_URL);
		ListState list3 = new ListState(TestConfiguration.Site2_List1_GUID,
				"No Title", SPConstants.DOC_LIB, Calendar.getInstance(),
				SPConstants.NO_TEMPLATE, TestConfiguration.Site2_List1_URL,
				web2);
		web2.AddOrUpdateListStateInWebState(list3, new DateTime());
		ListState list4 = new ListState(TestConfiguration.Site2_List2_GUID,
				"No Title", SPConstants.DOC_LIB, Calendar.getInstance(),
				SPConstants.NO_TEMPLATE, TestConfiguration.Site2_List2_URL,
				web2);
		web2.AddOrUpdateListStateInWebState(list4, new DateTime());

		assertEquals(web1, state.lookupWeb(TestConfiguration.Site1_URL, sharepointClientContext));
		assertEquals(list1, state.lookupList(TestConfiguration.Site1_URL, TestConfiguration.Site1_List1_GUID));
		assertEquals(list2, state.lookupList(TestConfiguration.Site1_URL, TestConfiguration.Site1_List2_GUID));

		state.setBFullReCrawl(true);
		// This will ensure that all the lists/webs will be marked non-existent
		state.startRecrawl();

		// This will remove all those lists/webs which are non-existent and not
		// found on SharePoint (HTTP 404)
		state.endRecrawl(sharepointClientContext);

		// Parent Web, though, marked as non-existent during startRecrawl, might
		// not be
		// removed, because an HTTP 404 will might not be received for it. So,
		// we assert only for lists.
		assertNull(state.lookupList(TestConfiguration.Site1_URL, TestConfiguration.Site1_List1_GUID));
		assertNull(state.lookupList(TestConfiguration.Site1_URL, TestConfiguration.Site1_List2_GUID));

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

		assertTrue((null == list1.getCrawlQueue() || list1.getCrawlQueue().size() == 0));
		assertTrue((null == list2.getCrawlQueue() || list2.getCrawlQueue().size() == 0));

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
     * This is to ensure that no information is lost while saving and loading
     * the state file.
     *
     * @throws SharepointException
     */
    public final void testStateReload() throws SharepointException {
        System.out.println("Testing the basic functionalities of an stateful object");

        final GlobalState state1 = new GlobalState(
                TestConfiguration.googleConnectorWorkDir, FeedType.CONTENT_FEED);

        WebState ws = state1.makeWebState(sharepointClientContext, TestConfiguration.Site1_URL);
        DateTime dt = new DateTime();
        ws.setInsertionTime(dt);
        ws.setLastCrawledDateTime(Util.formatDate(Calendar.getInstance(), Util.TIMEFORMAT_WITH_ZONE));

        final ListState list = new ListState("X", "X",
                SPConstants.GENERIC_LIST, Calendar.getInstance(), "X", "X", ws);
        list.setLastCrawledDateTime(Util.formatDate(Calendar.getInstance(), Util.TIMEFORMAT_WITH_ZONE));
        list.setAttchmnts(new StringBuffer("X"));
        list.addToDeleteCache("X");
        list.setIDs(new StringBuffer("X"));

        final SPDocument doc = new SPDocument("DocID", "DocURL",
                new GregorianCalendar(2007, 1, 1), SPConstants.NO_AUTHOR,
                SPConstants.NO_OBJTYPE, SPConstants.PARENT_WEB_TITLE,
                TestConfiguration.feedType, ws.getSharePointType());
        doc.setFolderLevel("X");

        list.setLastDocProcessedForWS(doc);
        ws.AddOrUpdateListStateInWebState(list, dt);
        ws.setCurrentList(list);
        state1.setCurrentWeb(ws);
        state1.setLastCrawledWeb(ws);
        state1.setLastCrawledList(list);
        state1.saveState();

        final GlobalState state2 = new GlobalState(
                TestConfiguration.googleConnectorWorkDir, FeedType.CONTENT_FEED);
        state2.loadState();

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
                assertEquals(tmpList1.getLastDocForWSRefresh().getFolderLevel(), tmpList2.getLastDocForWSRefresh().getFolderLevel());
                assertEquals(tmpList1.getLastDocForWSRefresh().getLastMod(), tmpList2.getLastDocForWSRefresh().getLastMod());
				assertEquals(tmpList1.getLastDocForWSRefresh().getAction(), tmpList2.getLastDocForWSRefresh().getAction());
            }
        }
    }

    /**
     * Make sure that the getCircularIterator() call works properly (since, if
     * it doesn't, the connector will fail to pick up new or changed SharePoint.
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
        final GlobalState state = new GlobalState(
                TestConfiguration.googleConnectorWorkDir,
                TestConfiguration.feedType);
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
        final GlobalState state = new GlobalState(
                TestConfiguration.googleConnectorWorkDir,
                TestConfiguration.feedType);

        WebState ws1 = new WebState(sharepointClientContext,
                TestConfiguration.Site1_URL);
        ws1.setInsertionTime(new DateTime(2009, 9, 05, 10, 25, 36, 100));
        state.AddOrUpdateWebStateInGlobalState(ws1);

        WebState ws2 = new WebState(sharepointClientContext,
                TestConfiguration.Site2_URL);
        ws2.setInsertionTime(new DateTime(2009, 9, 07, 10, 25, 36, 100));
        state.AddOrUpdateWebStateInGlobalState(ws2);

        WebState ws3 = new WebState(sharepointClientContext,
                TestConfiguration.Site3_URL);
        ws3.setInsertionTime(new DateTime(2009, 9, 06, 10, 25, 36, 100));
        state.AddOrUpdateWebStateInGlobalState(ws3);

        assertEquals(3, state.getAllWebStateSet().size());
        assertEquals(ws2, state.dateMap.first());
    }
}
