//Copyright (C) 2006 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.state;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

public class ListStateTest extends TestCase {

    SharepointClientContext sharepointClientContext;

    public void setUp() throws Exception {
        sharepointClientContext = TestConfiguration.initContext();
    }

    public void testComparison() throws SharepointException {
        ListState list1 = new ListState(TestConfiguration.Site1_List1_GUID,
                "No Title", SPConstants.DOC_LIB, Calendar.getInstance(),
                SPConstants.NO_TEMPLATE, TestConfiguration.Site1_List1_URL,
                null);
        list1.setLastMod(new DateTime(2009, 9, 07, 10, 25, 36, 100));

        ListState list2 = new ListState(TestConfiguration.Site2_List1_GUID,
                "No Title", SPConstants.DOC_LIB, Calendar.getInstance(),
                SPConstants.NO_TEMPLATE, TestConfiguration.Site2_List1_URL,
                null);
        list2.setLastMod(new DateTime(2009, 9, 05, 10, 25, 36, 100));

        assertEquals(1, list1.compareTo(list2));

        list1.setLastMod(new DateTime(2009, 9, 04, 10, 25, 36, 100));
        assertEquals(-1, list1.compareTo(list2));

        list1.setLastMod(list2.getLastMod());
        assertEquals(list1.getPrimaryKey().compareTo(list2.getPrimaryKey()), list1.compareTo(list2));
    }

    public void testGetDateForWSRefresh() throws SharepointException {
        Calendar cal1 = Calendar.getInstance();
        ListState list1 = new ListState(TestConfiguration.Site1_List1_GUID,
                "No Title", SPConstants.DOC_LIB, cal1, SPConstants.NO_TEMPLATE,
                TestConfiguration.Site1_List1_URL, null);
        assertEquals(cal1, list1.getDateForWSRefresh());

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.MONTH, -1);
        list1.setLastDocProcessed(new SPDocument("1", "X", cal2,
                ActionType.ADD));
        assertEquals(cal2, list1.getDateForWSRefresh());
    }

    public void testCachedDeletedID() throws SharepointException {
        ListState list1 = new ListState(TestConfiguration.Site1_List1_GUID,
                "No Title", SPConstants.DOC_LIB, null, SPConstants.NO_TEMPLATE,
                TestConfiguration.Site1_List1_URL, null);
        list1.addToDeleteCache("1");
        list1.addToDeleteCache("1");
        list1.addToDeleteCache("3");
        list1.addToDeleteCache("2");
        assertTrue(list1.isInDeleteCache("1"));
        list1.removeFromDeleteCache("1");
        assertFalse(list1.isInDeleteCache("1"));
    }

    public void testExtraIDs() throws SharepointException {
        System.out.println("Testing ExtraIDs handling...");
        final ListState state = new ListState("", "", "", null, "", "", null);
        state.setType(SPConstants.DOC_LIB);
        state.setUrl("http://host.mycom.co.in:25000/sanity/Test Library/Forms/AllItems.aspx");
        state.setListConst("sanity/Test Library/");
        String str1 = "#1~Forms1#2#33~Forms2#4#5~Forms3#3/#5/#33/#1";
        state.setIDs(new StringBuffer(str1));

        // Expected -> #1~Forms1#2#33~Forms2#4#5~Forms3#3/#5/#33/#1
        assertEquals(3, state.getExtraIDs("1").size());
        assertTrue(state.getExtraIDs("1").contains("2"));
        assertTrue(state.getExtraIDs("1").contains("4"));
        assertTrue(state.getExtraIDs("1").contains("3"));
        assertEquals(2, state.getExtraIDs("33").size());
        assertTrue(state.getExtraIDs("33").contains("4"));
        assertTrue(state.getExtraIDs("33").contains("3"));
        assertEquals(1, state.getExtraIDs("5").size());
        assertTrue(state.getExtraIDs("5").contains("3"));
        assertEquals(1, state.getExtraIDs("2").size());
        assertTrue(state.getExtraIDs("2").contains("2"));
        assertEquals(1, state.getExtraIDs("4").size());
        assertTrue(state.getExtraIDs("4").contains("4"));
        assertEquals(1, state.getExtraIDs("3").size());
        assertTrue(state.getExtraIDs("3").contains("3"));

        final String docURL = "http://host.mycom.co.in:25000/sanity/Test Library/Forms1/Forms2/Forms3/AllItems.aspx";
        state.updateExtraIDs(docURL, "30", false);
        // Expected -> #1~Forms1#2#33~Forms2#4#5~Forms3#30#3/#5/#33/#1
        assertEquals(4, state.getExtraIDs("1").size());
        assertTrue(state.getExtraIDs("1").contains("2"));
        assertTrue(state.getExtraIDs("1").contains("4"));
        assertTrue(state.getExtraIDs("1").contains("3"));
        assertTrue(state.getExtraIDs("1").contains("30"));
        assertEquals(3, state.getExtraIDs("33").size());
        assertTrue(state.getExtraIDs("33").contains("4"));
        assertTrue(state.getExtraIDs("33").contains("3"));
        assertTrue(state.getExtraIDs("33").contains("30"));
        assertEquals(2, state.getExtraIDs("5").size());
        assertTrue(state.getExtraIDs("5").contains("3"));
        assertTrue(state.getExtraIDs("5").contains("30"));
        assertEquals(1, state.getExtraIDs("2").size());
        assertTrue(state.getExtraIDs("2").contains("2"));
        assertEquals(1, state.getExtraIDs("4").size());
        assertTrue(state.getExtraIDs("4").contains("4"));
        assertEquals(1, state.getExtraIDs("3").size());
        assertTrue(state.getExtraIDs("3").contains("3"));
        assertEquals(1, state.getExtraIDs("30").size());
        assertTrue(state.getExtraIDs("30").contains("30"));

        state.removeExtraID("3");
        // Expected -> #1~Forms1#2#33~Forms2#4#5~Forms3#30/#5/#33/#1
        assertEquals(3, state.getExtraIDs("1").size());
        assertTrue(state.getExtraIDs("1").contains("2"));
        assertTrue(state.getExtraIDs("1").contains("4"));
        assertTrue(state.getExtraIDs("1").contains("30"));
        assertEquals(2, state.getExtraIDs("33").size());
        assertTrue(state.getExtraIDs("33").contains("4"));
        assertTrue(state.getExtraIDs("33").contains("30"));
        assertEquals(1, state.getExtraIDs("5").size());
        assertTrue(state.getExtraIDs("5").contains("30"));
        assertEquals(1, state.getExtraIDs("2").size());
        assertTrue(state.getExtraIDs("2").contains("2"));
        assertEquals(1, state.getExtraIDs("4").size());
        assertTrue(state.getExtraIDs("4").contains("4"));
        assertEquals(1, state.getExtraIDs("30").size());
        assertTrue(state.getExtraIDs("30").contains("30"));

        state.removeExtraID("30");
        state.removeExtraID("4");
        // Expected -> #1~Forms1#2#33~Forms2#5~Forms3/#5/#33/#1
        assertEquals(1, state.getExtraIDs("1").size());
        assertTrue(state.getExtraIDs("1").contains("2"));
        assertEquals(0, state.getExtraIDs("33").size());
        assertEquals(0, state.getExtraIDs("5").size());
    }

    public void testUpdateExtraIDAsAttachment() throws SharepointException {
        String attchmnt1 = "http://host.mycom.co.in:25000/sanity/Test Library1/Forms/AllItems.aspx";
        String attchmnt2 = "http://host.mycom.co.in:25000/sanity/Test Library2/Forms/AllItems.aspx";
        String attchmnt3 = "http://host.mycom.co.in:25000/sanity/Test Library3/Forms/AllItems.aspx";
        String attchmnt4 = "http://host.mycom.co.in:25000/sanity/Test Library4/Forms/AllItems.aspx";
        String attchmnt5 = "http://host.mycom.co.in:25000/sanity/Test Library5/Forms/AllItems.aspx";
        String attchmnt6 = "http://host.mycom.co.in:25000/sanity/Test Library6/Forms/AllItems.aspx";
        String attchmnt7 = "http://host.mycom.co.in:25000/sanity/Test Library7/Forms/AllItems.aspx";
        String attchmnt8 = "http://host.mycom.co.in:25000/sanity/Test Library8/Forms/AllItems.aspx";

        final ListState testList = new ListState("", "",
                SPConstants.GENERIC_LIST, null, "", "", null);
        testList.updateExtraIDAsAttachment("1", attchmnt1);
        testList.updateExtraIDAsAttachment("1", attchmnt2);
        testList.updateExtraIDAsAttachment("1", attchmnt3);
        testList.updateExtraIDAsAttachment("2", attchmnt4);
        testList.updateExtraIDAsAttachment("2", attchmnt5);
        testList.updateExtraIDAsAttachment("3", attchmnt6);
        testList.updateExtraIDAsAttachment("4", attchmnt7);
        testList.updateExtraIDAsAttachment("4", attchmnt8);

        List<String> attchmnts = testList.getAttachmntURLsFor("1");
        assertEquals(3, attchmnts.size());
        assertTrue(attchmnts.contains(attchmnt1));
        assertTrue(attchmnts.contains(attchmnt2));
        assertTrue(attchmnts.contains(attchmnt3));
        attchmnts = testList.getAttachmntURLsFor("2");
        assertEquals(2, attchmnts.size());
        assertTrue(attchmnts.contains(attchmnt4));
        assertTrue(attchmnts.contains(attchmnt5));
        attchmnts = testList.getAttachmntURLsFor("3");
        assertEquals(1, attchmnts.size());
        assertTrue(attchmnts.contains(attchmnt6));
        attchmnts = testList.getAttachmntURLsFor("4");
        assertEquals(2, attchmnts.size());
        assertTrue(attchmnts.contains(attchmnt7));
        assertTrue(attchmnts.contains(attchmnt8));

        testList.removeAttachmntURLFor("1", attchmnt3);
        attchmnts = testList.getAttachmntURLsFor("1");
        assertEquals(2, attchmnts.size());
        assertTrue(attchmnts.contains(attchmnt1));
        assertTrue(attchmnts.contains(attchmnt2));
        attchmnts = testList.getAttachmntURLsFor("2");
        assertEquals(2, attchmnts.size());
        assertTrue(attchmnts.contains(attchmnt4));
        assertTrue(attchmnts.contains(attchmnt5));
        attchmnts = testList.getAttachmntURLsFor("3");
        assertEquals(1, attchmnts.size());
        assertTrue(attchmnts.contains(attchmnt6));
        attchmnts = testList.getAttachmntURLsFor("4");
        assertEquals(2, attchmnts.size());
        assertTrue(attchmnts.contains(attchmnt7));
        assertTrue(attchmnts.contains(attchmnt8));

        testList.removeAttachmntURLFor("2", attchmnt4);
        testList.removeAttachmntURLFor("2", attchmnt5);
        attchmnts = testList.getAttachmntURLsFor("1");
        assertEquals(2, attchmnts.size());
        assertTrue(attchmnts.contains(attchmnt1));
        assertTrue(attchmnts.contains(attchmnt2));
        attchmnts = testList.getAttachmntURLsFor("2");
        assertEquals(0, attchmnts.size());
        attchmnts = testList.getAttachmntURLsFor("3");
        assertEquals(1, attchmnts.size());
        assertTrue(attchmnts.contains(attchmnt6));
        attchmnts = testList.getAttachmntURLsFor("4");
        assertEquals(2, attchmnts.size());
        assertTrue(attchmnts.contains(attchmnt7));
        assertTrue(attchmnts.contains(attchmnt8));
    }

    public void testGetLastDocForWSRefresh() throws SharepointException {
        Calendar cal1 = Calendar.getInstance();
        ListState list1 = new ListState(TestConfiguration.Site1_List1_GUID,
                "No Title", SPConstants.DOC_LIB, cal1, SPConstants.NO_TEMPLATE,
                TestConfiguration.Site1_List1_URL, null);
        assertNull(list1.getLastDocForWSRefresh());

        SPDocument doc1 = new SPDocument("1", "X", cal1, SPConstants.NO_AUTHOR,
                SPConstants.DOCUMENT, "XX", FeedType.METADATA_URL_FEED,
                SPType.SP2007);
        list1.setLastDocProcessed(doc1);
        assertEquals(doc1, list1.getLastDocForWSRefresh());

        SPDocument doc2 = new SPDocument("2", "X", cal1, SPConstants.NO_AUTHOR,
                SPConstants.DOCUMENT, "XX", FeedType.METADATA_URL_FEED,
                SPType.SP2007);
        SPDocument doc3 = new SPDocument("3", "X", cal1, SPConstants.NO_AUTHOR,
                SPConstants.DOCUMENT, "XX", FeedType.METADATA_URL_FEED,
                SPType.SP2007);
        SPDocument doc4 = new SPDocument("4", "X", cal1, SPConstants.NO_AUTHOR,
                SPConstants.DOCUMENT, "XX", FeedType.METADATA_URL_FEED,
                SPType.SP2007);
        List<SPDocument> docs = new LinkedList<SPDocument>();
        docs.add(doc2);
        docs.add(doc3);
        docs.add(doc4);
        list1.setCrawlQueue(docs);
        assertEquals(doc4, list1.getLastDocForWSRefresh());

        doc4.setAction(ActionType.DELETE);
        assertEquals(doc3, list1.getLastDocForWSRefresh());
    }
}
