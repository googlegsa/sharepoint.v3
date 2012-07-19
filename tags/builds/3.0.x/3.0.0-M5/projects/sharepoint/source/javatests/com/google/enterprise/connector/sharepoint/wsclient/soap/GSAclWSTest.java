// Copyright 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
import com.google.enterprise.connector.sharepoint.wsclient.client.AclWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDataWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDiscoveryWS;
import com.google.enterprise.connector.spi.SimpleTraversalContext;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

  public void testGetAclForUrls() throws Exception {
    WebState webState = globalState.lookupWeb(TestConfiguration.Site1_URL, sharepointClientContext);
    ListState listState = globalState.lookupList(TestConfiguration.Site1_URL, TestConfiguration.Site1_List1_GUID);
    assertNotNull(listState);

    List<SPDocument> testDocs = listState.getCrawlQueue();
    assertNotNull(testDocs);

    // SharePoint document representing list
    SPDocument spdocument = listState.getDocumentInstance(FeedType.CONTENT_FEED);
    testDocs.add(spdocument);

    // SharePoint document representing site landing page(site)
    final SPDocument spdocument2 = siteDataWS.getSiteData(webState);
    testDocs.add(spdocument2);

    SPDocumentList docList = new SPDocumentList(testDocs, globalState);
    assertNotNull(docList);

    aclWS = new GSAclWS(sharepointClientContext, webState.getWebUrl());
    aclWS.fetchAclForDocuments(docList, webState);
    for (SPDocument document : docList.getDocuments()) {
      assertNotNull(document);
      assertNotNull(document.getUsersAclMap());
      assertNotNull(document.getGroupsAclMap());
    }
  }

  public void testWebApplicationPolicyDocument() throws Exception {
    WebState webState = globalState.lookupWeb(TestConfiguration.Site4_URL,
        sharepointClientContext); 
    SharepointClientContext spContext = (SharepointClientContext)sharepointClientContext.clone();
    SimpleTraversalContext context = new SimpleTraversalContext();
    context.setSupportsInheritedAcls(true);
    spContext.setTraversalContext(context);

    SPClientFactory clientFactory = new SPClientFactory();
    AclWS aclWs = clientFactory.getAclWS(spContext, 
        webState.getWebUrl());
    assertNotNull(aclWs);
    SPDocument webApppolicy = null;
    webApppolicy = aclWs.getWebApplicationPolicy(webState,
        spContext.getFeedType().toString());
    assertNotNull(webApppolicy);
    assertEquals("Web app policy URL should be same as root site URL",
        TestConfiguration.Site4_URL, webApppolicy.getUrl());
    assertNotNull(webApppolicy.getGroupsAclMap());
    assertFalse(webApppolicy.getGroupsAclMap().isEmpty());
    assertNotNull(webApppolicy.getDenyUsersAclMap());
    assertFalse(webApppolicy.getDenyUsersAclMap().isEmpty());
    assertNotNull(webApppolicy.getUsersAclMap());
    assertFalse(webApppolicy.getUsersAclMap().isEmpty());      
  }

  public void testGetAclForUrlsUsingInheritance() throws Exception {
    // Get WebState for ACL Test Site 
    WebState webState = globalState.lookupWeb(TestConfiguration.Site4_URL,
        sharepointClientContext);
    SharepointClientContext spContext = (SharepointClientContext)sharepointClientContext.clone();
    SimpleTraversalContext context = new SimpleTraversalContext();
    context.setSupportsInheritedAcls(true);
    spContext.setTraversalContext(context);
    assertNotNull(webState);
    SPClientFactory clientFactory = new SPClientFactory();
    List<SPDocument> docsToPass = new ArrayList<SPDocument>(); 
    //Get Web application policy document.
    AclWS aclWs = clientFactory.getAclWS(spContext, 
        webState.getWebUrl());
    assertNotNull(aclWs);

    // Load Document for Root Web Home Page.
    SPDocument webDoc = siteDataWS.getSiteData(webState);
    assertNotNull(webDoc);
    docsToPass.add(webDoc);

    // Load Documents scenarios for ACLs

    docsToPass.add(getDocumentForUrl(TestConfiguration.SearchDocID401, "401"));
    docsToPass.add(getDocumentForUrl(TestConfiguration.SearchDocID402, "402"));
    docsToPass.add(getDocumentForUrl(TestConfiguration.SearchDocID403, "403"));
    docsToPass.add(getDocumentForUrl(TestConfiguration.SearchDocID404, "404"));
    docsToPass.add(getDocumentForUrl(TestConfiguration.SearchDocID405, "405"));
    docsToPass.add(getDocumentForUrl(TestConfiguration.SearchDocID406, "406"));
    docsToPass.add(getDocumentForUrl(TestConfiguration.SearchDocID407, "407"));
    docsToPass.add(getDocumentForUrl(TestConfiguration.SearchDocID408, "408"));
    docsToPass.add(getDocumentForUrl(TestConfiguration.SearchDocID409, "409"));
    docsToPass.add(getDocumentForUrl(TestConfiguration.SearchDocID410, "410"));
    docsToPass.add(getDocumentForUrl(TestConfiguration.SearchDocID411, "411"));
    docsToPass.add(getDocumentForUrl(TestConfiguration.SearchDocID412, "412"));
    docsToPass.add(getDocumentForUrl(TestConfiguration.SearchDocID413, "413"));

    SPDocumentList docList = new SPDocumentList(docsToPass, globalState);
    assertNotNull(docList);  
    System.out.println("\n...Processing doclist..." + docList);

    aclWs.fetchAclForDocuments(docList, webState);
    for (SPDocument document : docList.getDocuments()) {
      assertNotNull(document);       
      if (document.getUrl().
          equalsIgnoreCase(TestConfiguration.SearchDocID401)) {
        // Search Doc ID 401 - List item with Inheriting permissions.
        checkInheritPermissions(document, TestConfiguration.SearchDocID410);
      } else if (document.getUrl().
          equalsIgnoreCase(TestConfiguration.SearchDocID402)) {
        // Search Doc ID 402 - List item with Unique permissions.
        checkUniquePermissions(document, TestConfiguration.Site4_URL);
      } else if (document.getUrl().
          equalsIgnoreCase(TestConfiguration.SearchDocID403)) {
        // Search Doc ID 403 - Folder with Inheriting permissions.
        checkInheritPermissions(document,TestConfiguration.SearchDocID410);
      } else if (document.getUrl().
          equalsIgnoreCase(TestConfiguration.SearchDocID404)) {
        // Search Doc ID 404 - Item Inside Folder with 
        // Inheriting permissions.
        checkInheritPermissions(document, TestConfiguration.SearchDocID403);
      } else if (document.getUrl().
          equalsIgnoreCase(TestConfiguration.SearchDocID405)) {
        // Search Doc ID 405 - Attachment for List item with 
        // inheriting permissions.
        checkInheritPermissions(document, TestConfiguration.SearchDocID410);
      } else if (document.getUrl().
          equalsIgnoreCase(TestConfiguration.SearchDocID406)) {
        // Search Doc ID 406 - Attachment for List item with 
        // Unique permissions.
        checkUniquePermissions(document, TestConfiguration.Site4_URL );
      } else if (document.getUrl().
          equalsIgnoreCase(TestConfiguration.SearchDocID407)) {
        // Search Doc ID 407 - Document with inheriting permissions.
        checkInheritPermissions(document, TestConfiguration.SearchDocID411);
      } else if (document.getUrl().
          equalsIgnoreCase(TestConfiguration.SearchDocID408)) {
        // Search Doc ID 408 -Document with Unique permissions.
        checkUniquePermissions(document, TestConfiguration.Site4_URL);
      } else if (document.getUrl().
          equalsIgnoreCase(TestConfiguration.SearchDocID409)) {
        // Search Doc ID 409 - List with Unique Permissions.
        checkUniquePermissions(document, TestConfiguration.Site4_URL);
      } else if (document.getUrl().
          equalsIgnoreCase(TestConfiguration.SearchDocID410)) {
        // Search Doc ID 410 - List with inheriting Permissions.
        checkInheritPermissions(document,
            TestConfiguration.Site4_URL + "/default.aspx");
      } else if (document.getUrl().
          equalsIgnoreCase(TestConfiguration.SearchDocID411)) {
        // Search Doc ID 411 - Doc Lib with inheriting Permissions.
        checkInheritPermissions(document,
            TestConfiguration.Site4_URL + "/default.aspx");
      }
    }
  }

  private SPDocument getDocumentForUrl(String url,String docID) {
    SPDocument doc = new SPDocument(docID, url,
        Calendar.getInstance(), ActionType.ADD);
    return doc;
  }

  private void checkInheritPermissions(SPDocument document, String parentUrl) {
    assertFalse(document.isUniquePermissions());
    assertNotNull("parent url is null for "
        + document, document.getParentUrl());
    assertNotNull("parent id is null for "
        + document, document.getParentId());
    assertTrue(document.getParentUrl().equalsIgnoreCase(parentUrl));
    assertTrue(null == document.getGroupsAclMap() ||
        document.getGroupsAclMap().isEmpty());
    assertTrue(null == document.getDenyUsersAclMap()||
        document.getDenyUsersAclMap().isEmpty());   
    assertTrue(null == document.getUsersAclMap() ||
        document.getUsersAclMap().isEmpty() );
    assertTrue(null == document.getDenyGroupsAclMap() ||
        document.getDenyGroupsAclMap().isEmpty());
  }

  private void checkUniquePermissions (SPDocument document, String parentUrl) {   
    assertTrue("UniquePermission not true" + document, 
        document.isUniquePermissions());
    assertNotNull("getUsersAclMap is empty", 
        document.getUsersAclMap());          
    assertNotNull(document.getGroupsAclMap());
    assertTrue("Parent Url Mismatch", 
        document.getParentUrl().equalsIgnoreCase(parentUrl));
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
