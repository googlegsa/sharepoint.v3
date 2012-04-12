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

package com.google.enterprise.connector.sharepoint.spiimpl;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.sharepoint.wsclient.ListsWS;
import com.google.enterprise.connector.sharepoint.wsclient.SiteDataWS;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.SkippedDocumentException;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

public class SPDocumentListTest extends TestCase {
  SPDocumentList docs;
  SharepointClientContext sharepointClientContext;

  protected void setUp() throws Exception {
    System.out.println("\n...Setting Up...");
    System.out.println("Initializing SPDocumentList ...");

    sharepointClientContext = TestConfiguration.initContext();

    final GlobalState state = new GlobalState(
        TestConfiguration.googleConnectorWorkDir, FeedType.CONTENT_FEED);
    WebState ws = state.makeWebState(sharepointClientContext, TestConfiguration.Site1_URL);

    final SiteDataWS siteDataWS = new SiteDataWS(this.sharepointClientContext);
    final List listCollection = siteDataWS.getNamedLists(ws);
    assertNotNull(listCollection);
    for (int i = 0; i < listCollection.size(); i++) {
      final ListState baseList = (ListState) listCollection.get(i);
      ListsWS listws = new ListsWS(this.sharepointClientContext);
      List<SPDocument> listItems = listws.getListItems(baseList, null, null, null);
      if (listItems.size() > 0) {
        for (Iterator itr = listItems.iterator(); itr.hasNext();) {
          SPDocument spdoc = (SPDocument) itr.next();
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

  public void testCheckpoint() {
    System.out.println("Testing checkpoint()...");
    this.docs.setAliasMap(sharepointClientContext.getAliasMap());
    try {
      final String chk = this.docs.checkpoint();
      assertNotNull(chk);
      System.out.println("[ checkpoint() ] Test Completed.");
    } catch (final Exception e) {
      System.out.println("[ checkpoint() ] Test Failed.");
    }
  }
}
