//Copyright 2007-2010 Google Inc.

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

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;

import junit.framework.TestCase;

/**
 * @author rakesh_shete
 */
public class GSPFileContentWSTest extends TestCase {

    SharepointClientContext sharepointClientContext;
    GSPFileContentWS gspFileContentWS = null;

    @Override
    protected void setUp() throws Exception {
        System.out.println("\n...Setting Up...");
        System.out.println("Initializing SharepointClientContext ...");
        this.sharepointClientContext = TestConfiguration.initContext();

        assertNotNull(this.sharepointClientContext);

        gspFileContentWS = new GSPFileContentWS(sharepointClientContext, null);
        assertNotNull(this.gspFileContentWS);
    }

    public void testGetFileContent() {

        String fileURL = "http://example.com/Documents/MyExampleDocsHelp.aspx";

        ByteArrayInputStream fileContent = null;
        try {
            AuthenticationWS authws = null;
            try {
                authws = new AuthenticationWS(sharepointClientContext, null);
            } catch (SharepointException e) {
                fail("Failed with exception : " + e.getMessage());
            }

            gspFileContentWS.setAuthenticationCookie(authws.login());
            System.out.println("Cookie that is being used : " + authws.login());
            fileContent = gspFileContentWS.getFileContent(fileURL);
        } catch (RemoteException e) {
            e.printStackTrace();
            fail("Failed with exception : " + e.getMessage());
        }

        assertNotNull(fileContent);

    }

}
