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

// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sharepoint.client;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocumentList;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.sharepoint.wsclient.soap.SPClientFactory;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

public class SharepointClientTest extends TestCase {

  private SharepointClient sharepointClient;
  private GlobalState globalState;
  private final SPClientFactory clientFactory = new SPClientFactory();

  protected void setUp() throws Exception {
    super.setUp();
    System.out.println("Initializing SharepointClientContext ...");
    final SharepointClientContext sharepointClientContext = TestConfiguration.initContext();
    assertNotNull(sharepointClientContext);
    sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
    sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);

    this.sharepointClient = new SharepointClient(clientFactory,
        sharepointClientContext);
    this.globalState = new GlobalState(clientFactory,
        sharepointClientContext.getGoogleConnectorWorkDir(),
        sharepointClientContext.getFeedType());
  }

  public void testUpdateGlbalState() throws SharepointException {
    System.out.println("Testing updateGlobalState()...");
    this.sharepointClient.updateGlobalState(this.globalState);
    assertNotNull(this.globalState);
    System.out.println("[ updateGlobalState() ] Test Completed.");
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.sharepoint.client. SharepointClient#getDocsFromDocumentLibrary()}
   * .
   */
  public void testTraverse() throws SharepointException {
    System.out.println("Testing [ testTraverse() ]...");
    final int iPageSizeHint = 100;
    this.sharepointClient.updateGlobalState(this.globalState);
    final Set webStates = this.globalState.getAllWebStateSet();
    final WebState curr_webState = (WebState) (webStates.toArray())[0];
    final SPDocumentList rs = this.sharepointClient.traverse(this.globalState, curr_webState, iPageSizeHint, false);
    int numDocs = 0;
    try {
      System.out.println("Documents found - ");
      SPDocument pm;
      if (rs != null) {
        pm = (SPDocument) rs.nextDocument();
        while (pm != null) {
          System.out.println("<document>");
          final Property lastModProp = pm.findProperty(SpiConstants.PROPNAME_LASTMODIFIED);
          if (lastModProp != null) {
            System.out.println("<lastModify>"
                + lastModProp.nextValue().toString() + "</lastModify>");
          }
          final Property docProp = pm.findProperty(SpiConstants.PROPNAME_DOCID);
          if (lastModProp != null) {
            System.out.println("<docId>" + docProp.nextValue().toString()
                + "</docId>");
          }
          final Property contentUrlProp = pm.findProperty(SpiConstants.PROPNAME_CONTENTURL);
          if (contentUrlProp != null) {
            System.out.println("<contentUrl>"
                + contentUrlProp.nextValue().toString() + "</contentUrl>");
          }
          final Property searchUrlProp = pm.findProperty(SpiConstants.PROPNAME_SEARCHURL);
          if (searchUrlProp != null) {
            System.out.println("<searchUrl>"
                + searchUrlProp.nextValue().toString() + "</searchUrl>");
          }
          final Property listGuidProp = pm.findProperty(Util.LIST_GUID);
          if (listGuidProp != null) {
            System.out.println("<listGuid>"
                + listGuidProp.nextValue().toString() + "</listguid>");
          }
          final Property displayUrlProp = pm.findProperty(SpiConstants.PROPNAME_DISPLAYURL);
          if (displayUrlProp != null) {
            System.out.println("<displayUrl>"
                + displayUrlProp.nextValue().toString() + "</displayUrl>");
          }
          final Property authorProp = pm.findProperty(SPConstants.AUTHOR);
          if (authorProp != null) {
            System.out.println("<author>" + authorProp.nextValue().toString()
                + "</author>");
          }
          final Property objTypeProp = pm.findProperty(SPConstants.OBJECT_TYPE);
          if (objTypeProp != null) {
            System.out.println("<" + SPConstants.OBJECT_TYPE + ">"
                + objTypeProp.nextValue().toString() + "<"
                + SPConstants.OBJECT_TYPE + ">");
          }
          final Property isPublicProp = pm.findProperty(SpiConstants.PROPNAME_ISPUBLIC);
          if (isPublicProp != null) {
            System.out.println("<isPublic>"
                + isPublicProp.nextValue().toString() + "</isPublic>");
          }
          System.out.println("</document>");

          pm.dumpAllAttrs();

          // check crawling coverage.. check if paticular document is
          // found
          numDocs++;

          pm = (SPDocument) rs.nextDocument();
        }
      }

    } catch (final RepositoryException e) {
      e.printStackTrace();
    }
    System.out.println("Total dos: " + numDocs);
    System.out.println("[ testTraverse() ] Test Completed.");
  }

  /**
   * Tests {@link SharepointClient#traverse(GlobalState, WebState, int)} to
   * check that only the lists starting from the given list are checked for
   * pending docs from previous crawl cycle
   *
   * @throws SharepointException
   */
  public void testTraverseToCheckValidLists() throws SharepointException {
    SharepointClientContext spContext = TestConfiguration.initContext();
    spContext.setBatchHint(Integer.MAX_VALUE);
    GlobalState gs = TestConfiguration.initState(spContext);
    WebState ws = gs.lookupWeb(TestConfiguration.Site1_URL, spContext);
    SharepointClient spclient = new SharepointClient(clientFactory, spContext);

    // Traverse the lists for the given web state
    spclient.traverse(gs, ws, 50, true);

    // Since there are 4 lists, the third list being set as last crawled,
    // the total no. of lists visited should be 2
    assertEquals(2, spclient.getNoOfVisitedListStates());
  }

  /**
   * Test case for
   * {@link SharepointClient#fetchACLInBatches(SPDocumentList, WebState, GlobalState, int)}
   *
   * @throws SharepointException
   */
  public void testFetchACLInBatches() throws SharepointException {
    SharepointClientContext spContext = TestConfiguration.initContext();
    spContext.setBatchHint(Integer.MAX_VALUE);
    spContext.setAclBatchSizeFactor(2);
    spContext.setFetchACLInBatches(true);
    GlobalState gs = TestConfiguration.initState(spContext);
    WebState ws = gs.lookupWeb(TestConfiguration.Site1_URL, spContext);
    SharepointClient spclient = new SharepointClient(clientFactory, spContext);

    SPDocument doc = new SPDocument("122", TestConfiguration.Site1_List1_URL,
        Calendar.getInstance(), ActionType.ADD);

    doc.setSharepointClientContext(spContext);

    List<SPDocument> list = new ArrayList<SPDocument>();
    list.add(doc);
    SPDocumentList docList = new SPDocumentList(list, gs);

    // Test that whenever 1 document, the batchsize is set to 1 and the
    // method does return and does not run into infinite loop
    boolean result = spclient.fetchACLInBatches(docList, ws, gs, 2);
    assertTrue(result);

    // Negative test case with 0 documents
    List<SPDocument> list2 = new ArrayList<SPDocument>();
    SPDocumentList docList2 = new SPDocumentList(list2, gs);
    assertFalse(spclient.fetchACLInBatches(docList2, ws, gs, 2));

  }

  /**
   * @throws SharepointException
   */
  public void testHandleACLForDocumentsForNonACLCrawl()
      throws SharepointException {
    SharepointClientContext spContext = TestConfiguration.initContext();
    spContext.setPushAcls(false);

    SharepointClient spclient = new SharepointClient(clientFactory, spContext);

    GlobalState gs = TestConfiguration.initState(spContext);
    WebState ws = gs.lookupWeb(TestConfiguration.Site1_URL, spContext);

    // Test that when feeding ACLs is turned off, you still get true to
    // indicate docs need to be fed to GSA
    assertTrue(spclient.handleACLForDocuments(null, ws, gs, false));

    SPDocumentList docList = getDocList(spContext, gs);
    spContext.setPushAcls(true);

    // Should fetch ACL and return true to indicate success
    assertTrue(spclient.handleACLForDocuments(docList, ws, gs, false));

    // Should just return true without fetching ACLs
    assertTrue(spclient.handleACLForDocuments(docList, ws, gs, true));

  }

  /**
   * Returns a doc list for test cases
   *
   * @param spContext The context info
   * @param gs The global state holding all web states and list states
   * @return The doc list
   */
  private SPDocumentList getDocList(SharepointClientContext spContext,
      GlobalState gs) {
    SPDocument doc = new SPDocument("122", TestConfiguration.Site1_List1_URL,
        Calendar.getInstance(), ActionType.ADD);

    doc.setSharepointClientContext(spContext);

    List<SPDocument> list = new ArrayList<SPDocument>();
    list.add(doc);
    SPDocumentList docList = new SPDocumentList(list, gs);
    return docList;
  }
}
