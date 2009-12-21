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

import java.util.ArrayList;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

public class WebStateTest extends TestCase {

    WebState webState;
    SharepointClientContext sharepointClientContext;
    String spURL;

    public void setUp() throws Exception {
        System.out.println("\n...Setting Up...");
        System.out.println("Initializing SharepointClientContext ...");
		sharepointClientContext = TestConfiguration.initContext();

        assertNotNull(sharepointClientContext);
        System.out.println("Creating test Web State for testing.");
        this.spURL = sharepointClientContext.getSiteURL();

		this.webState = TestConfiguration.createWebState(TestConfiguration.initState(), sharepointClientContext, this.spURL, 1);
        final SPDocument doc1 = new SPDocument("id1", "url1",
                new GregorianCalendar(2007, 1, 1), SPConstants.NO_AUTHOR,
                SPConstants.NO_OBJTYPE, SPConstants.PARENT_WEB_TITLE,
				FeedType.CONTENT_FEED, SPType.SP2007);
        final SPDocument doc2 = new SPDocument("id2", "url2",
                new GregorianCalendar(2007, 1, 2), SPConstants.NO_AUTHOR,
                SPConstants.NO_OBJTYPE, SPConstants.PARENT_WEB_TITLE,
				FeedType.CONTENT_FEED, SPType.SP2007);
        final SPDocument doc3 = new SPDocument("id3", "url3",
                new GregorianCalendar(2007, 1, 3), SPConstants.NO_AUTHOR,
                SPConstants.NO_OBJTYPE, SPConstants.PARENT_WEB_TITLE,
				FeedType.CONTENT_FEED, SPType.SP2007);

        ListState testList = null;
        try {
			testList = TestConfiguration.getListState("", 1, 1, "", webState);
        } catch (final SharepointException spe) {
            System.out.println("Failed to initialize test web state.");
        }

        testList.setUrl("http://gdc05.persistent.co.in:8889/");
        testList.updateExtraIDAsAttachment("1", "4");
        testList.setType(SPConstants.GENERIC_LIST);
        testList.setExisting(false);
        testList.setBiggestID(1);

        final ArrayList<SPDocument> crawlQueueList1 = new ArrayList<SPDocument>();
        crawlQueueList1.add(doc1);
        crawlQueueList1.add(doc2);
        crawlQueueList1.add(doc3);
        testList.setCrawlQueue(crawlQueueList1);
    }

    public void testCompareTo() {
        System.out.println("Testing compareTo()...");
        System.out.println("Creating temporary web state to compare");
        try {
            final WebState ws1 = this.webState = new WebState(
                    this.sharepointClientContext, this.spURL);
            final int i = this.webState.compareTo(ws1);
            assertEquals(i, 0);
            System.out.println("[ compareTo() ] Test completed.");
        } catch (final Exception e) {
            System.out.println("[ compareTo() ] Test Failed.");
        }
    }

    public void testEndRecrawl() {
        System.out.println("Testing endRecrawl for WebState..");
        this.webState.endRecrawl(this.sharepointClientContext);
        System.out.println("[ endRecrawl() ] Test Completed. ");
    }
}
