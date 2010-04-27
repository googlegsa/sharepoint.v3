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

package com.google.enterprise.connector.sharepoint.wsclient;

import java.util.List;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssResolveSPGroupResult;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocumentList;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;

public class GssAclTest extends TestCase {

    GlobalState globalState;
    SharepointClientContext sharepointClientContext;
    GssAclWS aclWS;

    protected void setUp() throws Exception {
        System.out.println("\n...Setting Up...");
        System.out.println("Initializing SharepointClientContext ...");
        this.sharepointClientContext = TestConfiguration.initContext();
        assertNotNull(this.sharepointClientContext);
        sharepointClientContext.setPushAcls(true);
        sharepointClientContext.setBatchHint(2);
        globalState = TestConfiguration.initState(sharepointClientContext);
        System.out.println("Initializing GsAclWS ...");
        this.aclWS = new GssAclWS(this.sharepointClientContext, null);
    }

    public void testGetAclForUrls() {
        WebState webState = globalState.lookupWeb(TestConfiguration.Site1_URL, sharepointClientContext);
        ListState listState = globalState.lookupList(TestConfiguration.Site1_URL, TestConfiguration.Site1_List1_GUID);
        assertNotNull(listState);

        List<SPDocument> testDocs = listState.getCrawlQueue();
        assertNotNull(testDocs);

        SPDocumentList docList = new SPDocumentList(testDocs, globalState);
        assertNotNull(docList);

        aclWS.fetchAclForDocuments(docList, webState);
        for (SPDocument document : docList.getDocuments()) {
            assertNotNull(document);
            assertNotNull(document.getUsersAclMap());
            assertNotNull(document.getGroupsAclMap());
        }
    }

    public void testGetAclChangesSinceToken() throws Exception {
        WebState webstate = globalState.lookupWeb(TestConfiguration.Site1_URL, sharepointClientContext);
        String changeToken = "";
        webstate.setNextAclChangeToken(changeToken);
        webstate.commitAclChangeToken();
        aclWS.fetchAclChangesSinceTokenAndUpdateState(webstate);
        assertNotSame("Change Token is not updated", changeToken, webstate.getNextAclChangeToken());
    }

    public void testGetListItemsWithInheritingRoleAssignments()
            throws SharepointException {
        ListState listState = globalState.lookupList(TestConfiguration.Site1_URL, TestConfiguration.Site1_List1_GUID);
        assertNotNull(listState);
        listState.startAclCrawl();
        ListsWS listWs = new ListsWS(sharepointClientContext);
        assertNotNull(listWs);
        List<SPDocument> docs = aclWS.getListItemsForAclChangeAndUpdateState(listState, listWs);
        assertNotNull(docs);
    }


    public void testResolveSPGroup() {
        String[] groupIds = {};
        GssResolveSPGroupResult result = aclWS.resolveSPGroup(groupIds);
        assertNotNull(result);
    }

    public void testCheckConnectivity() {
        String status = aclWS.checkConnectivity();
        assertEquals(SPConstants.CONNECTIVITY_SUCCESS, status);
    }
}
