//Copyright 2009 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.wsclient.soap;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.ListsHelper;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssPrincipal;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssResolveSPGroupResult;
import com.google.enterprise.connector.sharepoint.generated.gssacl.PrincipalType;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocumentList;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;

import java.util.List;

import junit.framework.TestCase;

public class GSAclWSTest extends TestCase {

  GlobalState globalState;
  SharepointClientContext sharepointClientContext;
  GSAclWS aclWS;
  SPSiteDataWS siteDataWS;

  protected void setUp() throws Exception {
    System.out.println("\n...Setting Up...");
    System.out.println("Initializing SharepointClientContext ...");
    this.sharepointClientContext = TestConfiguration.initContext();
    assertNotNull(this.sharepointClientContext);
    sharepointClientContext.setPushAcls(true);
    sharepointClientContext.setBatchHint(2);
    siteDataWS = new SPSiteDataWS(sharepointClientContext);
    aclWS = new GSAclWS(sharepointClientContext,
        TestConfiguration.sharepointUrl);
    globalState = TestConfiguration.initState(sharepointClientContext);
  }

  public void testGetAclForUrls() {
    WebState webState = globalState.lookupWeb(TestConfiguration.Site1_URL, sharepointClientContext);
    ListState listState = globalState.lookupList(TestConfiguration.Site1_URL, TestConfiguration.Site1_List1_GUID);
    assertNotNull(listState);

    List<SPDocument> testDocs = listState.getCrawlQueue();
    assertNotNull(testDocs);

    // SharePoint document representing list
    SPDocument spdocument = listState.getDocumentInstance(FeedType.CONTENT_FEED);
    testDocs.add(spdocument);

    // SharePoint document representing site landing page(site)
    SPDocument spdocument2;
    try {
      spdocument2 = siteDataWS.getSiteData(webState);
      testDocs.add(spdocument2);
    } catch (SharepointException e1) {
      System.out.println("Cannot create sharepoint document fro site landing Page");
    }

    SPDocumentList docList = new SPDocumentList(testDocs, globalState);
    assertNotNull(docList);

    try {
      aclWS = new GSAclWS(sharepointClientContext, webState.getWebUrl());
      aclWS.fetchAclForDocuments(docList, webState);
      for (SPDocument document : docList.getDocuments()) {
        assertNotNull(document);
        assertNotNull(document.getUsersAclMap());
        assertNotNull(document.getGroupsAclMap());
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testGetAclChangesSinceToken() throws Exception {
    WebState webstate = globalState.lookupWeb(TestConfiguration.Site1_URL, sharepointClientContext);
    String changeToken = "1;1;1648c1de-0093-4fb8-a888-f032f5a2da4c;634103077352630000;2263";
    webstate.setNextAclChangeToken(changeToken);
    webstate.commitAclChangeToken();
    this.aclWS = new GSAclWS(this.sharepointClientContext,
        webstate.getWebUrl());
    aclWS.fetchAclChangesSinceTokenAndUpdateState(webstate);
    assertNotSame("Change Token is not updated", changeToken, webstate.getNextAclChangeToken());
  }

  public void testGetListItemsWithInheritingRoleAssignments()
      throws SharepointException {
    ListState listState = globalState.lookupList(TestConfiguration.Site1_URL, TestConfiguration.Site1_List1_GUID);
    assertNotNull(listState);
    listState.startAclCrawl();
    ListsHelper listHelper = new ListsHelper(sharepointClientContext);
    assertNotNull(listHelper);
    this.aclWS = new GSAclWS(this.sharepointClientContext,
        listState.getParentWebState().getWebUrl());
    List<SPDocument> docs = aclWS.getListItemsForAclChangeAndUpdateState(listState, listHelper);
    assertNotNull(docs);
  }

  public void testResolveSPGroup() throws Exception {
    String[] groupIds = { "1", "[GSSiteCollectionAdministrator]", "5" };
    this.aclWS = new GSAclWS(this.sharepointClientContext,
        TestConfiguration.sharepointUrl);
    GssResolveSPGroupResult result = aclWS.resolveSPGroup(groupIds);
    assertNotNull(result);
    assertNotNull(result.getPrinicpals());
    assertEquals(result.getPrinicpals().length, groupIds.length);
  }

  public void testCheckConnectivity() throws Exception {
    aclWS = new GSAclWS(sharepointClientContext,
        TestConfiguration.sharepointUrl);
    try {
      aclWS.checkConnectivity();
    } catch (Exception e) {
      fail();
    }
    assertTrue(true);
  }

  public void testGetPrincipalName() {
    runBatchForGetPrincipalName("google\\users", "google\\users", PrincipalType.DOMAINGROUP, "domain\\username", "domain\\groupname");
    runBatchForGetPrincipalName("google\\searchuser", "google\\searchuser", PrincipalType.USER, "domain\\username", "domain\\groupname");
    runBatchForGetPrincipalName("users@google", "google\\users", PrincipalType.DOMAINGROUP, "domain\\username", "domain\\groupname");
    runBatchForGetPrincipalName("searchuser@google", "google\\searchuser", PrincipalType.USER, "domain\\username", "domain\\groupname");

    runBatchForGetPrincipalName("google\\users", "users@google", PrincipalType.DOMAINGROUP, "username@domain", "groupname@domain");
    runBatchForGetPrincipalName("google\\searchuser", "searchuser@google", PrincipalType.USER, "username@domain", "groupname@domain");
    runBatchForGetPrincipalName("users@google", "users@google", PrincipalType.DOMAINGROUP, "username@domain", "groupname@domain");
    runBatchForGetPrincipalName("searchuser@google", "searchuser@google", PrincipalType.USER, "username@domain", "groupname@domain");

    runBatchForGetPrincipalName("google\\users", "users", PrincipalType.DOMAINGROUP, "username", "groupname");
    runBatchForGetPrincipalName("google\\searchuser", "searchuser", PrincipalType.USER, "username", "groupname");
    runBatchForGetPrincipalName("users@google", "users", PrincipalType.DOMAINGROUP, "username", "groupname");
    runBatchForGetPrincipalName("searchuser@google", "searchuser", PrincipalType.USER, "username", "groupname");

    runBatchForGetPrincipalName("google\\domain users", "google\\domain users", PrincipalType.DOMAINGROUP, "domain\\username", "domain\\groupname");
    runBatchForGetPrincipalName("google\\domain admin", "google\\domain admin", PrincipalType.USER, "domain\\username", "domain\\groupname");
  }

  public void runBatchForGetPrincipalName(String principalname,
      String expectedPrincipalname, PrincipalType principalType,
      String usernameFormat, String groupnameFormat) {
    sharepointClientContext.setUsernameFormatInAce(usernameFormat);
    sharepointClientContext.setGroupnameFormatInAce(groupnameFormat);
    GssPrincipal principal = new GssPrincipal();
    principal.setType(principalType);
    principal.setName(principalname);
    assertEquals(expectedPrincipalname, aclWS.getPrincipalName(principal));
  }
}
