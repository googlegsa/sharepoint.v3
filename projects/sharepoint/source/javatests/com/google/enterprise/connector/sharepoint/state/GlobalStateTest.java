//Copyright 2007 Google Inc.

package com.google.enterprise.connector.sharepoint.state;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;

import junit.framework.TestCase;

import org.joda.time.DateTime;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

public class GlobalStateTest extends TestCase {

    private GlobalState state;

    /**
     * Directory where the GlobalState file will be stored. Replace this with a
     * suitable temporary directory if not running on a Linux or Unix-like
     * system.
     */
    private static final String TMP_DIR = "c:";
    SharepointClientContext sharepointClientContext;

    public void setUp() throws Exception {
        System.out.println("\n...Setting Up...");
        System.out.println("Initializing SharepointClientContext ...");
        final SharepointClientContext sharepointClientContext = new SharepointClientContext(
                TestConfiguration.sharepointUrl, TestConfiguration.domain,
                TestConfiguration.kdcserver, TestConfiguration.username, TestConfiguration.Password,
                TestConfiguration.googleConnectorWorkDir,
                TestConfiguration.includedURls, TestConfiguration.excludedURls,
                TestConfiguration.mySiteBaseURL, TestConfiguration.AliasMap,
                TestConfiguration.feedType);
        assertNotNull(sharepointClientContext);
        sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
        sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);

        this.state = new GlobalState(TMP_DIR, SPConstants.CONTENT_FEED);
    }

    public void testMakeWebStateDat() {
        System.out.println("Testing makeWebState()..");
        System.out.println(new DateTime().toString());
        try {
            final GlobalState state = new GlobalState(
                    TestConfiguration.googleConnectorWorkDir,
                    SPConstants.CONTENT_FEED);
            state.makeWebState(sharepointClientContext, TestConfiguration.ParentWebURL);
            if (this.state.lookupWeb(TestConfiguration.ParentWebURL, sharepointClientContext) != null) {
                System.out.println("[ makeWebState() ] Test Passed.");
            } else {
                System.out.println("[ makeWebState() ] Test Failed.");
            }
        } catch (final Exception e) {
            System.out.println("[ makeWebState() ] Test Failed.");
        }
    }

    public void testStartRecrawl() {
        System.out.println("Testing startRecrawl()..");
        this.state.startRecrawl();
        final Iterator itr = this.state.getIterator();
        while (itr.hasNext()) {
            if (((StatefulObject) itr.next()).isExisting()) {
                System.out.println("[ startRecrawl() ] Test Failed.");
            }
        }
        System.out.println("[ startRecrawl() ] Test Completed.");
    }

    /**
     * Test basic functionality: create a GlobalState, save it to XML, load
     * another from XML, save THAT to XML, and verify that the XML is the same.
     */
    public final void testBasic() throws SharepointException {
        System.out.println("Testing the basic functionalities of an stateful object");

        final DateTime time1 = Util.parseDate("20080702T140516.411+0000");
        final DateTime time2 = Util.parseDate("20080702T140516.411+0000");
        WebState ws = null;

        final GlobalState state = new GlobalState(
                TestConfiguration.googleConnectorWorkDir,
                SPConstants.CONTENT_FEED);
        state.makeWebState(sharepointClientContext, TestConfiguration.sharepointUrl);
        state.makeWebState(sharepointClientContext, TestConfiguration.ParentWebURL);

        final ListState list1 = ws.makeListState("foo", time1);
        final ListState list2 = ws.makeListState("bar", time2);

        ws.setCurrentList(list1);

        final SPDocument doc1 = new SPDocument("id1", "url1",
                new GregorianCalendar(2007, 1, 1), SPConstants.NO_AUTHOR,
                SPConstants.NO_OBJTYPE, SPConstants.PARENT_WEB_TITLE,
                SPConstants.CONTENT_FEED, SPConstants.METADATA_URL_FEED);
        list1.setLastDocument(doc1);
        ws.setCurrentList(list1);
        this.state.setCurrentWeb(ws);

        final SPDocument doc2 = new SPDocument("id2", "url2",
                new GregorianCalendar(2007, 1, 2), SPConstants.NO_AUTHOR,
                SPConstants.NO_OBJTYPE, SPConstants.PARENT_WEB_TITLE,
                SPConstants.CONTENT_FEED, SPConstants.METADATA_URL_FEED);
        list2.setLastDocument(doc2);

        // make a crawl queue & store it in our "current" ListState
        final ArrayList<SPDocument> docTree = new ArrayList<SPDocument>();
        final SPDocument doc3 = new SPDocument("id3", "url3",
                new GregorianCalendar(2007, 1, 3), SPConstants.NO_AUTHOR,
                SPConstants.NO_OBJTYPE, SPConstants.PARENT_WEB_TITLE,
                SPConstants.CONTENT_FEED, SPConstants.METADATA_URL_FEED);
        docTree.add(doc3);
        final SPDocument doc4 = new SPDocument("id4", "url4",
                new GregorianCalendar(2007, 1, 4), SPConstants.NO_AUTHOR,
                SPConstants.NO_OBJTYPE, SPConstants.PARENT_WEB_TITLE,
                SPConstants.CONTENT_FEED, SPConstants.METADATA_URL_FEED);
        docTree.add(doc4);
        list1.setCrawlQueue(docTree);

        this.state.setCurrentWeb(ws);
        this.state.setLastCrawledWebID(ws.getPrimaryKey());
        this.state.setLastCrawledListID(list1.getPrimaryKey());

        final String output = this.state.getStateXML();

        System.out.println(output);

        // assertEquals(expected, output);
        // Needs to be manually tested with the above expected code.

        this.state.saveState();// save the state to disk.. forms
        // TMP_DIR\Sharepoint_state.xml file
        GlobalState state2 = null;

        state2 = new GlobalState(TMP_DIR, SPConstants.CONTENT_FEED);
        state2.loadState(); // load from the old GlobalState's XML
        final String output2 = state2.getStateXML();

        assertEquals(output, output2);// output of the new GlobalState should
        // match the old one's:

        this.state.setBFullReCrawl(true);// Set this true when one crawling
        // cycle is over
        // This is required for garbage collection

        this.state.startRecrawl();// set exist all false
		this.state.AddOrUpdateWebStateInGlobalState(ws);// set exists for list1
        this.state.endRecrawl(this.sharepointClientContext);// list2 will be
        // removed from
        // global state

        // list1 should still be there, list2 should be gone
        StatefulObject obj = (StatefulObject) this.state.keyMap.get("bar");
        assertNull(obj);
        obj = (StatefulObject) this.state.keyMap.get("foo");
        assertNotNull(obj);

        System.out.println("[ Testing of basic functionalities DOM to DOC and vice versa conversion] Completed");
    }

    /**
     * Utility for testCircularIterators(): make sure the iterator returns the
     * same set of items as list.
     *
     * @param iter iterator to be tested
     * @param list array of the expected results
     */
    void verifyIterator(final Iterator iter, final WebState[] arr1) {
        for (int i = 0; i < arr1.length; i++) {
            assertTrue(iter.hasNext());
            final WebState found = (WebState) iter.next();
            assertEquals(found, arr1[i]);
        }
        assertFalse(iter.hasNext());
    }

    /**
     * Make sure that the getCircularIterator() call works properly (since, if
     * it doesn't, the connector will fail to pick up new or changed SharePoint.
     * documents)
     *
     * @throws InterruptedException
     */
    public void testCircularIterators() throws SharepointException,
            InterruptedException {
        System.out.println("Testing getCircularIterator()...");
        /*
         * final DateTime time1 = Util.parseDate("20070504T144419.403-0700");
         * final DateTime time2 = Util.parseDate("20070505T144419.867-0700");
         * final DateTime time3 = Util.parseDate("20070506T144419.867-0700");
         */
        WebState list1 = null, list2 = null, list3 = null;

        // create Web State inside Global state
        final GlobalState state = new GlobalState(
                TestConfiguration.googleConnectorWorkDir,
                SPConstants.CONTENT_FEED);
        state.makeWebState(sharepointClientContext, TestConfiguration.ParentWebURL);

        Thread.sleep(1000);// wait for some time
        state.makeWebState(sharepointClientContext, TestConfiguration.sharepointUrl);
        Thread.sleep(1000);// wait for some time
        state.makeWebState(sharepointClientContext, TestConfiguration.ParentWebURL);

        final WebState[] arr1 = { list1, list2, list3 };
        final WebState[] arr2 = { list2, list3, list1 };
        final WebState[] arr3 = { list3, list1, list2 };

        // state.setCurrentWeb(list1);
        this.verifyIterator(this.state.getCircularIterator(), arr1);
        this.state.setCurrentWeb(list2);
        this.verifyIterator(this.state.getCircularIterator(), arr2);
        this.state.setCurrentWeb(list3);
        this.verifyIterator(this.state.getCircularIterator(), arr3);
        System.out.println("[ getCircularIterator() ] Test Passed.");
    }

    /**
     * Test to check that web states are ordered in the descending order of
     * insertion time
     */
    public void testUpdateListState() {
        WebState ws = new WebState("metadata-and-URL");
        ws.setPrimaryKey("http://contentvm1.corp.google.com:12084/sites/testissue85");
        DateTime dt = new DateTime();
        ws.setInsertionTime(dt);

        WebState ws2 = new WebState("metadata-and-URL");
        ws2.setPrimaryKey("http://testcase.com:12084/sites/testissue85/abc");
        DateTime dt2 = new DateTime(2009, 9, 06, 10, 25, 36, 100);
        ws2.setInsertionTime(dt2);

        WebState ws3 = new WebState("metadata-and-URL");
        ws3.setPrimaryKey("http://testcase.com:12084/sites/testissue85");
        DateTime dt3 = new DateTime(2009, 9, 07, 10, 25, 36, 100);
        ws3.setInsertionTime(dt3);

        WebState ws4 = new WebState("metadata-and-URL");
        ws4.setPrimaryKey("http://testcase.com:12084/sites/testissue859");
        DateTime dt4 = new DateTime(2009, 9, 8, 11, 26, 38, 100);
        ws4.setInsertionTime(dt4);

        GlobalState gs = new GlobalState("c:\\", "metadata-and-URL");

		gs.AddOrUpdateWebStateInGlobalState(ws);
		gs.AddOrUpdateWebStateInGlobalState(ws3);
		gs.AddOrUpdateWebStateInGlobalState(ws4);
		gs.AddOrUpdateWebStateInGlobalState(ws2);

        assertEquals(3, gs.getAllWebStateSet().size());

        int count = 0;

        for (WebState webstate : gs.dateMap) {
            if (count == 0) {
                assertEquals(ws4.getPrimaryKey(), webstate.getPrimaryKey());
            }
            count++;
        }

        count = 0;

        Iterator<WebState> wsiT = gs.dateMap.iterator();

        while (wsiT.hasNext()) {
            WebState webstate = wsiT.next();
            if (count == 0) {
                assertEquals(ws4.getPrimaryKey(), webstate.getPrimaryKey());
            }
            count++;
        }
    }
}
