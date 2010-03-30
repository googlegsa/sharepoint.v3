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

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.filecontent.GSPFileContent;
import com.google.enterprise.connector.sharepoint.generated.filecontent.GSPFileContentLocator;
import com.google.enterprise.connector.sharepoint.generated.filecontent.GSPFileContentSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

import org.apache.axis.AxisFault;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

/**
 * Java Client for calling GSPFileContent.asmx Provides a layer to talk to the
 * GSPFileContent custom Web Service on the SharePoint server. Any call to this
 * Web Service must go through this layer.
 *
 * @author rakesh_shete
 */
public class GSPFileContentWS {

    private final Logger LOGGER = Logger.getLogger(GSPFileContentWS.class.getName());
    private SharepointClientContext sharepointClientContext;
    private String endpoint;
    private GSPFileContentSoap_BindingStub stub = null;

    /**
     * Minimal constructor
     *
     * @param inSharepointClientContext The SharePoint client context. Ensure
     *            that it has the siteUrl as the URL of web application of the
     *            file whose contents will be retrieved
     * @throws SharepointException
     */
    public GSPFileContentWS(SharepointClientContext inSharepointClientContext)
            throws SharepointException {
        if (inSharepointClientContext != null) {
            sharepointClientContext = inSharepointClientContext;

            String siteUrl = Util.getWebURLForWSCall(sharepointClientContext.getSiteURL());
            endpoint = Util.encodeURL(siteUrl)
                    + SPConstants.GSPFILECONTENT_END_POINT;
            LOGGER.config("endpoint set to: " + endpoint);

            final GSPFileContentLocator loc = new GSPFileContentLocator();
            loc.setGSPFileContentSoapEndpointAddress(endpoint);

            final GSPFileContent gspFileContent = loc;
            try {
                stub = (GSPFileContentSoap_BindingStub) gspFileContent.getGSPFileContentSoap();
            } catch (final ServiceException e) {
                LOGGER.log(Level.WARNING, "Unable to get the stub for GSPFileContent web service", e);
                throw new SharepointException(
                        "Unable to get the stub for GSPFileContent web service");
            }

            final String strDomain = inSharepointClientContext.getDomain();
            String strUser = inSharepointClientContext.getUsername();
            final String strPassword = inSharepointClientContext.getPassword();

            strUser = Util.getUserNameWithDomain(strUser, strDomain);
            stub.setUsername(strUser);
            stub.setPassword(strPassword);
        }
    }

    /**
     * Web service to retrieve the file content for the given document URL
     * <p>
     * Imp. Note: The web service root needs to be set to the web application
     * under which the file resides
     * </p>
     *
     * @param fileURL The URL of the file whose contents are to be retrieved
     * @return The file content
     * @throws RemoteException
     */
    public ByteArrayInputStream getFileContent(String fileURL)
            throws RemoteException {
        byte[] fileContent = null;
        try {
            LOGGER.log(Level.FINER, "Document content retrieval for document : "
                    + fileURL + " using GSPFileContent web service ");
            fileContent = stub.getFileContents(fileURL);
        } catch (final AxisFault af) { // Handling of username formats for
            // different authentication models.
            if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
                    && (sharepointClientContext.getDomain() != null)) {
                final String username = Util.switchUserNameFormat(stub.getUsername());
                LOGGER.log(Level.INFO, "Web Service call failed for username [ "
                        + stub.getUsername() + " ].");
                LOGGER.log(Level.INFO, "Trying with " + username);
                stub.setUsername(username);
                try {
                    fileContent = stub.getFileContents(fileURL);
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "Unable to get file content for "
                            + fileURL, e);
                }
            } else {
                LOGGER.log(Level.WARNING, "Unable to get file content for "
                        + fileURL, af);
            }
        }

        // This indicates failure in retrieval of file content
        if (fileContent == null) {
            return null;
        }

        LOGGER.log(Level.FINER, "Document content retrieval done for document : "
                + fileURL);
        return new ByteArrayInputStream(fileContent);
    }

    /**
     * Test method for accessing WS successfully
     *
     * @return The return value
     */
    public String sayHello() {
        try {
            return stub.helloWorld();
        } catch (final AxisFault af) { // Handling of username formats for
            // different authentication models.
            if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
                    && (sharepointClientContext.getDomain() != null)) {
                final String username = Util.switchUserNameFormat(stub.getUsername());
                LOGGER.log(Level.INFO, "Web Service call failed for username [ "
                        + stub.getUsername() + " ].");
                LOGGER.log(Level.INFO, "Trying with " + username);
                stub.setUsername(username);
                try {
                    return stub.helloWorld();
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "Unable to get file content for ", e);
                }
            } else {
                LOGGER.log(Level.WARNING, "Unable to get file content for ", af);
            }
        } catch (final Throwable e) {
            LOGGER.log(Level.WARNING, "Unable to get file content for ", e);
        }
        return null;
    }


}
