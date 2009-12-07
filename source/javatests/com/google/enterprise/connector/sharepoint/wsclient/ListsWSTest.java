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

import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.spi.RepositoryException;

public class ListsWSTest extends TestCase {
    SharepointClientContext sharepointClientContext;
    ListsWS listWS;
    ListState testList;
    Calendar lastModified;
    String lastItemID;
    String lastItemURL;

    protected void setUp() throws Exception {
        System.out.println("\n...Setting Up...");
        System.out.println("Initializing SharepointClientContext ...");
        this.sharepointClientContext = new SharepointClientContext(
                TestConfiguration.sharepointUrl, TestConfiguration.domain,
                TestConfiguration.kdcserver, TestConfiguration.username, TestConfiguration.Password,
                TestConfiguration.googleConnectorWorkDir,
                TestConfiguration.includedURls, TestConfiguration.excludedURls,
                TestConfiguration.mySiteBaseURL, TestConfiguration.AliasMap,
                TestConfiguration.feedType);

        assertNotNull(this.sharepointClientContext);
        sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
        sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);

        System.out.println("Initializing ListsWS ...");
        this.listWS = new ListsWS(this.sharepointClientContext);

        System.out.println("Creating test List ...");
        final SiteDataWS siteDataWS = new SiteDataWS(
                this.sharepointClientContext);

        final GlobalState state = new GlobalState(
                TestConfiguration.googleConnectorWorkDir,
                SPConstants.CONTENT_FEED);
        WebState ws = state.makeWebState(sharepointClientContext, TestConfiguration.ParentWebURL);

        final List listCollection = siteDataWS.getNamedLists(ws);

        assertNotNull(listCollection);
        for (int i = 0; i < listCollection.size(); i++) {
            final ListState baseList = (ListState) listCollection.get(i);
            if (baseList.getPrimaryKey().equals(TestConfiguration.BaseListID)) {
                this.testList = baseList;
            }
        }
        System.out.println("Test List being used: "
                + this.testList.getPrimaryKey());
    }

    public final void testListsWS() throws Throwable {
        System.out.println("Testing ListsWS(SharepointClientContext, siteName)...");
        sharepointClientContext.setSiteURL(TestConfiguration.ParentWebURL);
        this.listWS = new ListsWS(this.sharepointClientContext);
        assertNotNull(this.listWS);
        System.out.println("[ ListsWS(SharepointClientContext, siteName) ] Test Passed");
    }

    public void testGetAttachments() throws MalformedURLException,
            RepositoryException {
        System.out.println("Testing getAttachments()...");
        final SPDocument doc = new SPDocument("1", "url1",
                new GregorianCalendar(2007, 1, 1), SPConstants.NO_AUTHOR,
                SPConstants.NO_OBJTYPE, SPConstants.PARENT_WEB_TITLE,
                SPConstants.CONTENT_FEED, SPConstants.SP2007);
        final List items = this.listWS.getAttachments(this.testList, doc);
        assertNotNull(items);
        System.out.println("[ getAttachments() ] Test Passed.");
    }

    public void testGetFolderHierarchy() throws MalformedURLException,
            RepositoryException {
        System.out.println("Testing getFolderHierarchy()...");
        final List items = this.listWS.getFolderHierarchy(this.testList, "", null);
        assertNotNull(items);
        System.out.println("[ getFolderHierarchy() ] Test Passed.");
    }

    public void testGetListItemsAtFolderLevel() throws MalformedURLException,
            RepositoryException {
        System.out.println("Testing getListItemsAtFolderLevel()...");
        final List items = this.listWS.getListItemsAtFolderLevel(this.testList, null, null);
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

    public void testGetListItemChangesSinceToken()
            throws MalformedURLException, RepositoryException {
        System.out.println("Testing getListItemChangesSinceToken()...");
        final List items = this.listWS.getListItemChangesSinceToken(this.testList, null, null, null);
        assertNotNull(items);
        System.out.println("[ getListItemChangesSinceToken() ] Test Passed.");
    }
}
