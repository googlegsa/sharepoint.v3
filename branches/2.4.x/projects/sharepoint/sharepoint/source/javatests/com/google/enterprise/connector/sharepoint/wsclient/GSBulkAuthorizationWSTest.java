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

import java.util.StringTokenizer;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.AuthData;

public class GSBulkAuthorizationWSTest extends TestCase {

    SharepointClientContext sharepointClientContext;
    GSBulkAuthorizationWS bulkAuth;

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

        System.out.println("Initializing GSBulkAuthorizationWS ...");
        this.bulkAuth = new GSBulkAuthorizationWS(this.sharepointClientContext);
    }

    public final void testGSBulkAuthorizationWS() throws Throwable {
        System.out.println("Testing GSBulkAuthorizationWS(SharepointClientContext, siteName)...");
        sharepointClientContext.setSiteURL(TestConfiguration.ParentWebURL);
        this.bulkAuth = new GSBulkAuthorizationWS(this.sharepointClientContext);
        assertNotNull(this.bulkAuth);
        System.out.println("[ GSBulkAuthorizationWS(SharepointClientContext, siteName) ] Test Passed");
    }

    public void testBulkAuthorize() throws Throwable {
        System.out.println("Testing bulkAuthorize()...");
        final AuthData[] inputDocs = new AuthData[3];
        System.out.println("Loading test documents for bulk authorization test...");
        StringTokenizer strTok = new StringTokenizer(
                TestConfiguration.SearchDocID1, SPConstants.DOC_TOKEN);
        if (strTok != null) {
            // String docURL = strTok.nextToken();
            final String listURL = strTok.nextToken();
            final String DocID = strTok.nextToken();
            // inputDocs[0] = new AuthData(docURL,listID,DocID,false,"");
            inputDocs[0] = new AuthData(listURL, DocID, false, "",
                    TestConfiguration.SearchDocID1);
        }
        strTok = new StringTokenizer(TestConfiguration.SearchDocID2,
                SPConstants.DOC_TOKEN);
        if (strTok != null) {
            // String docURL = strTok.nextToken();
            final String listURL = strTok.nextToken();
            final String DocID = strTok.nextToken();
            // inputDocs[1] = new AuthData(docURL,listID,DocID,false,"");
            inputDocs[1] = new AuthData(listURL, DocID, false, "",
                    TestConfiguration.SearchDocID2);
        }
        strTok = new StringTokenizer(TestConfiguration.SearchDocID3,
                SPConstants.DOC_TOKEN);
        if (strTok != null) {
            // String docURL = strTok.nextToken();
            final String listURL = strTok.nextToken();
            final String DocID = strTok.nextToken();
            // inputDocs[2] = new AuthData(docURL,listID,DocID,false,"");
            inputDocs[2] = new AuthData(listURL, DocID, false, "",
                    TestConfiguration.SearchDocID3);
        }

        final String searchUser = TestConfiguration.searchUserID;

        final AuthData[] outDocs = this.bulkAuth.bulkAuthorize(inputDocs, searchUser);
        assertNotNull(outDocs);
        if (outDocs.length == inputDocs.length) {
            System.out.println("[ bulkAuthorize() ] Test Passed.");
        } else {
            System.out.println("[ bulkAuthorize() ] Test Failed.");
        }
    }

    public void testCheckConnectivity() throws Throwable {
        System.out.println("Testing checkConnectivity()...");
        this.bulkAuth.checkConnectivity();
        System.out.println("[ checkConnectivity() ] Test Conpleted.");
    }
}
