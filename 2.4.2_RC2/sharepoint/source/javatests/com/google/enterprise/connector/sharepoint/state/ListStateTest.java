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

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.ContentHandler;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.SiteDataWS;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

public class ListStateTest extends TestCase {

    ListState testList;

    public void setUp() throws Exception {
        System.out.println("\n...Setting Up...");
        System.out.println("Initializing SharepointClientContext ...");
        SharepointClientContext spContext = TestConfiguration.initContext();
        final SiteDataWS siteDataWS = new SiteDataWS(spContext);
        WebState ws = TestConfiguration.createWebState(TestConfiguration.initState(), spContext, TestConfiguration.sharepointUrl, 1);
        final List<ListState> listCollection = siteDataWS.getNamedLists(ws);

        assertNotNull(listCollection);
        for (int i = 0; i < listCollection.size(); i++) {
            final ListState baseList = (ListState) listCollection.get(i);
            if (baseList.getPrimaryKey().equals(TestConfiguration.BaseListID)) {
                this.testList = baseList;
            }
        }

        final SPDocument doc1 = new SPDocument("2",
                "http://www.host.mycomp.com/test1", new GregorianCalendar(2007,
                        1, 1), SPConstants.NO_AUTHOR, SPConstants.NO_OBJTYPE,
                SPConstants.PARENT_WEB_TITLE, FeedType.CONTENT_FEED,
                SPType.SP2007);
        final SPDocument doc2 = new SPDocument("3",
                "http://www.host.mycomp.com/test2", new GregorianCalendar(2007,
                        1, 2), SPConstants.NO_AUTHOR, SPConstants.NO_OBJTYPE,
                SPConstants.PARENT_WEB_TITLE, FeedType.CONTENT_FEED,
                SPType.SP2007);
        final SPDocument doc3 = new SPDocument("4",
                "http://www.host.mycomp.com/test3", new GregorianCalendar(2007,
                        1, 3), SPConstants.NO_AUTHOR, SPConstants.NO_OBJTYPE,
                SPConstants.PARENT_WEB_TITLE, FeedType.CONTENT_FEED,
                SPType.SP2007);
        final SPDocument doc4 = new SPDocument("5",
                "http://www.host.mycomp.com/test4", new GregorianCalendar(2007,
                        1, 3), SPConstants.NO_AUTHOR, SPConstants.NO_OBJTYPE,
                SPConstants.PARENT_WEB_TITLE, FeedType.CONTENT_FEED,
                SPType.SP2007);

        // final DateTime time = Util.parseDate("20080702T140516.411+0000");

        this.testList = TestConfiguration.getListState("http://", 1, 6, "", ws);
        this.testList.setType(SPConstants.GENERIC_LIST);

        final ArrayList<SPDocument> crawlQueueList1 = new ArrayList<SPDocument>();
        crawlQueueList1.add(doc1);
        crawlQueueList1.add(doc2);
        crawlQueueList1.add(doc3);
        crawlQueueList1.add(doc4);

        this.testList.setCrawlQueue(crawlQueueList1);
        Collections.sort(this.testList.getCrawlQueue());
        this.testList.dumpCrawlQueue();

        System.out.println("Test List being used: "
                + this.testList.getPrimaryKey());

    }

    public void testCompareTo() throws SharepointException {
        System.out.println("Testing compareTo()...");
        System.out.println("Creating temporary listState to compare");
        // DateTime time = Util.parseDate("20080702T140520.411+0000");
        final ListState lst1 = new ListState("", "", "", null, "", "", null);
        lst1.setLastMod(this.testList.getLastMod());
        final int i = this.testList.compareTo(lst1);
        assertEquals(i, 0);
        System.out.println("[ compareTo() ] Test completed.");
    }

    public void testGetDateForWSRefresh() {
        System.out.println("Testing getDateForWSRefresh()..");
        final Calendar cal = this.testList.getDateForWSRefresh();
        assertNotNull(cal);
        System.out.println("[ getDateForWSRefresh() ] Test Completed.");
    }

    public void testDOCToDOMToDOC() throws Exception {
        GlobalState gs1 = TestConfiguration.initState();
        FileOutputStream fos = new FileOutputStream(
                TestConfiguration.googleConnectorWorkDir);
        OutputFormat of = new OutputFormat("XML", "UTF-8", true);
        of.setLineWidth(500);
        of.setIndent(2);
        XMLSerializer serializer = new XMLSerializer(fos, of);
        ContentHandler handler = serializer.asContentHandler();
        TestConfiguration.initState().dumpStateToXML(handler);
        fos.close();

        GlobalState gs2 = TestConfiguration.initState();
        gs2.loadState();
        assertEquals(gs1.getAllWebStateSet(), gs2.getAllWebStateSet());
    }

    public void testCachedDeletedID() {
        System.out.println("Testing the caching of deleted IDs ...");
        this.testList.addToDeleteCache("1");
        final boolean bl = this.testList.isInDeleteCache("1");
        assertTrue(bl);
        System.out.println("[ cachedDeletedIDs() ] Test Completed.");
    }

    public void testExtraIDs() throws SharepointException {
        System.out.println("Testing ExtraIDs handling...");
        final ListState state = new ListState("", "", "", null, "", "", null);
        state.setType(SPConstants.DOC_LIB);
        state.setUrl("http://host.mycom.co.in:25000/sanity/Test Library/Forms/AllItems.aspx");
        state.setListConst("sanity/Test Library/");
        state.setIDs(new StringBuffer(
                "#1~Forms1#2#33~Forms2#4#5~Forms3#3/#5/#33/#1"));
        final String docURL = "http://gdc05.persistent.co.in:25000/sanity/Test Library/Forms1/Forms2/Forms3/AllItems.aspx";
        try {
            System.out.println("Current ExtraID value: " + state.getIDs());
            state.updateExtraIDs(docURL, "30", false);
            System.out.println("ExtraID value after updation: "
                    + state.getIDs());
            final Set ids = state.getExtraIDs("33");
            System.out.println("Dependednt IDs: " + ids);
            state.removeExtraID("3");
            System.out.println("Extra IDs after removal: " + state.getIDs());
        } catch (final Exception se) {
            System.out.println("Test failed with following error:\n" + se);
        }
    }

    public void testUpdateExtraIDAsAttachment() throws SharepointException {
        System.out.println("Testing the attachment count handling ...");
        this.testList.updateExtraIDAsAttachment("1", "http://host.mycom.co.in:25000/sanity/Test Library/Forms/AllItems.aspx");
        this.testList.updateExtraIDAsAttachment("1", "http://host.mycom.co.in:25000/sanity/Test Library2/Forms/AllItems.aspx");
        final List attchmnts = this.testList.getAttachmntURLsFor("1");
        System.out.println(attchmnts);
        this.testList.removeAttachmntURLFor("1", "http://host.mycom.co.in:25000/sanity/Test Library/Forms/AllItems.aspx");
        assertNotNull(attchmnts);

        final Pattern pat = Pattern.compile("$");
        final Matcher match = pat.matcher("$");
        System.out.println(match.find());
        System.out.println("[ updateExtraIDAsAttachment() ] Test Completed.");
    }

    public void testGetLastDocForWSRefresh() {
        int i = 1;
        for (SPDocument doc : testList.getCrawlQueue()) {
            if (i % 2 == 0) {
                doc.setAction(ActionType.DELETE);
            }
            ++i;
        }
        SPDocument doc = testList.getLastDocForWSRefresh();
        assertEquals(ActionType.ADD, doc.getAction());
    }
}
