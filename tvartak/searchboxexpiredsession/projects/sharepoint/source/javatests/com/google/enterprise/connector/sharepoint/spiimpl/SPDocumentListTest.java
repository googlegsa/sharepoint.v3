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

package com.google.enterprise.connector.sharepoint.spiimpl;

import com.google.common.collect.ImmutableList;
import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.ListsHelper;
import com.google.enterprise.connector.sharepoint.client.SiteDataHelper;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.sharepoint.wsclient.mock.MockClientFactory;
import com.google.enterprise.connector.sharepoint.wsclient.soap.SPClientFactory;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;


public class SPDocumentListTest extends TestCase {
  SPDocumentList docs;
  SharepointClientContext sharepointClientContext;
  private SPClientFactory clientFactory = new SPClientFactory();

  protected void setUp() throws Exception {
    System.out.println("\n...Setting Up...");
    System.out.println("Initializing SPDocumentList ...");

    sharepointClientContext = TestConfiguration.initContext();

    final GlobalState state = new GlobalState(clientFactory,
        TestConfiguration.googleConnectorWorkDir, FeedType.CONTENT_FEED);
    WebState ws = state.makeWebState(sharepointClientContext, TestConfiguration.Site1_URL);

    final SiteDataHelper siteData = new SiteDataHelper(sharepointClientContext);
    List<ListState> listCollection = siteData.getNamedLists(ws);
    assertNotNull(listCollection);
    for (ListState baseList : listCollection) {
      ListsHelper listHelper = new ListsHelper(this.sharepointClientContext);
      List<SPDocument> listItems = listHelper.getListItems(baseList, null, null, null);
      if (listItems.size() > 0) {
        for (SPDocument spdoc : listItems) {
          spdoc.setParentWeb(ws);
          spdoc.setParentList(baseList);
        }
        System.out.println("Using " + baseList.getListURL()
            + " as test list...");
        this.docs = new SPDocumentList(listItems, state);
        ws.AddOrUpdateListStateInWebState(baseList, baseList.getLastMod());
        break;
      }
    }

    this.docs.setAliasMap(sharepointClientContext.getAliasMap());
  }

  public void testNextDocument() throws SkippedDocumentException {
    System.out.println("Testing nextDocument()...");
    this.docs.setFQDNConversion(true);
    final Document doc = this.docs.nextDocument();
    assertNotNull(doc);
    System.out.println("[ nextDocument() ] Test Passed.");
  }

  public void testCheckpoint() throws RepositoryException {
    System.out.println("Testing checkpoint()...");
    this.docs.setAliasMap(sharepointClientContext.getAliasMap());
    final String chk = this.docs.checkpoint();
    assertNotNull(chk);
    System.out.println("[ checkpoint() ] Test Completed.");
  }

  public void testDefensiveCopyForDocumentList() {
    MockClientFactory mockClientFactory = new MockClientFactory();
    GlobalState globalState =
        new GlobalState(mockClientFactory,"temp",FeedType.CONTENT_FEED);
    List<SPDocument> mutableDocumentList = new ArrayList<SPDocument>();
    SPDocument document1 = new SPDocument(
        "LIST_ITEM_1", "http://sharepoint.example.com/List1/DispForm.aspx?ID=1",
        Calendar.getInstance(), SpiConstants.ActionType.ADD);
    mutableDocumentList.add(document1);
    
    SPDocumentList documentList =
        new SPDocumentList(mutableDocumentList, globalState);
    assertEquals(ImmutableList.of(document1), documentList.getDocuments());

    // Add new document to mutable list
    SPDocument document2 = new SPDocument(
        "LIST_ITEM_2", "http://sharepoint.example.com/List1/DispForm.aspx?ID=2",
        Calendar.getInstance(), SpiConstants.ActionType.ADD);
    mutableDocumentList.add(document2);

    //Verify SPDocumentList is not modified
    assertEquals(ImmutableList.of(document1), documentList.getDocuments());
  }
}
