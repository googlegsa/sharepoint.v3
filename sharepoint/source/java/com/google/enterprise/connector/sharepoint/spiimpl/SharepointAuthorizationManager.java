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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.AuthData;
import com.google.enterprise.connector.sharepoint.wsclient.GSBulkAuthorizationWS;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * This class provides an implementation of AuthentorizationManager SPI provided
 * by CM for authorize the search users against SharePoint documents.. To
 * understand how this module fits into the Connector Manager framework refer to
 * http://code.google.com/apis/searchappliance/documentation/connectors/110/
 * connector_dev/cdg_authorization.html
 *
 * @author nitendra_thakur
 */
public class SharepointAuthorizationManager implements AuthorizationManager {

    Logger LOGGER = Logger.getLogger(SharepointAuthorizationManager.class.getName());

    SharepointClientContext sharepointClientContext;

    /**
     * @param inSharepointClientContext Context Information is required to
     *            create the instance of this class
     */
    public SharepointAuthorizationManager(
            final SharepointClientContext inSharepointClientContext)
            throws SharepointException {
        if (inSharepointClientContext == null) {
            throw new SharepointException(
                    "SharePointClientContext can not be null");
        }
        sharepointClientContext = (SharepointClientContext) inSharepointClientContext.clone();
    }

    /**
     * Authorizes a user represented by AuthenticationIdentity against all the
     * docIDs. All the docIDs are first converted into a format as expected by
     * the GSBulkAuthorization web service. The web service expects the
     * documents to be sent in the form of AuthData. An instance of AuthData
     * contains all the document specific necessary information required for
     * authorization. We also need to categorize all the AuthData objects
     * according to the their web application. This is required because the
     * authorization of documents are done per Web Application. This constraint
     * is applied by the Web Service.
     *
     * @param docIDs Document IDs to be authorized. These document IDs had been
     *            initially constructed and sent to GSA by the connector itself.
     *            The document IDs are in the format:
     *            &lt;Parent_List_URL&gt;|&lt;Original_Doc_ID&gt; The allowed
     *            prefixes to the format are: [ALERT] represents alerts which
     *            have been sent as a SPDocument
     *            [ATTACHMENT][&lt;Attachment_URL&gt;] The document IDs are
     *            carefully parsed to construct the AuthData object for each
     *            document.
     * @param identity Represents the user to be authorized.
     */
    public Collection<AuthorizationResponse> authorizeDocids(
            final Collection<String> docIDs,
            final AuthenticationIdentity identity) throws RepositoryException {

        if (identity == null) {
            throw new SharepointException("Identity is null");
        }
        if (docIDs == null) {
            throw new SharepointException("Document IDs is null");
        }

        String userName = identity.getUsername();
        String domain = identity.getDomain();

        LOGGER.log(Level.INFO, "Received #" + docIDs.size()
                + " documents for authorization. Username [ " + userName
                + " ], domain [ " + domain + " ]. ");

        // If domain is not received as part of the authorization request, use
        // the one from SharePointClientContext
        if ((domain == null) || (domain.length() == 0)) {
            LOGGER.warning("domain not found in the Authorization Request. Using the one from connector's context.");
            domain = sharepointClientContext.getDomain();
        }
        LOGGER.log(Level.FINEST, "Using domain [ " + domain + " ]. ");
        userName = Util.getUserNameWithDomain(userName, domain);
        LOGGER.log(Level.INFO, "Authorizing User: " + userName);

        final List<AuthorizationResponse> response = new ArrayList<AuthorizationResponse>();

        // documents are arranged per web application
        final Map<String, Set<AuthData>> hmSortedDocuments = createAuthDataFromDocIDsPerWebApp(docIDs);
        GSBulkAuthorizationWS bulkAuthWS = null;

        final Set<Map.Entry<String, Set<AuthData>>> docPerWebApp = hmSortedDocuments.entrySet();
        if (null == docPerWebApp) {
            throw new SharepointException(
                    "Problem while creating authData and sorting them per Web App. docPerWebApp is null. ");
        }
        LOGGER.log(Level.INFO, "A total of #"
                + docPerWebApp.size()
                + " WS call will be made to the authZ web service to authorize all the incoming docIds. Total docIds are #"
                + docIDs.size());
        for (Entry<String, Set<AuthData>> webAppToAuthData : docPerWebApp) {
            final String key_webapp = webAppToAuthData.getKey();
            final Set<AuthData> authDocs = webAppToAuthData.getValue();
            if ((null == authDocs)) {
                continue;
            }

            AuthData[] authData = new AuthData[authDocs.size()];
            authData = authDocs.toArray(authData);

            try {
                sharepointClientContext.setSiteURL(key_webapp);
                bulkAuthWS = new GSBulkAuthorizationWS(sharepointClientContext);
                authData = bulkAuthWS.bulkAuthorize(authData, userName);
            } catch (final Exception e) {
                final String logMessage = "Problem while making remote call to BulkAuthorize. key_webapp [ "
                        + key_webapp + " ]";
                LOGGER.log(Level.WARNING, logMessage, e);
            }

            if (authData == null) {
                final String logMessage = "Problem while calling GSBulkAuthorization Web Service for the web app [ "
                        + key_webapp
                        + " ]. authData is null at the completion of the call. ";
                LOGGER.log(Level.SEVERE, logMessage);
                continue;
            }

            // convert the document object back to complex_docid and create
            // response
            response.addAll(getAuthResponseFromAuthData(authData));
        }

        return response;
    }

    /**
     * Creates AuthData object for each docIDs and categorize them according to
     * their web application
     *
     * @param docIDs AuthData object is created for each document represented by
     *            the docID
     * @return A map where the web application in the following format:
     *         &lt;web_app,List&lt;AuthData&gt;&gt;
     */
    private Map<String, Set<AuthData>> createAuthDataFromDocIDsPerWebApp(
            final Collection<String> docIDs) {
        // documents are arranged per web application
        final Map<String, Set<AuthData>> hmSortedDocuments = new HashMap<String, Set<AuthData>>();
        String logMessage = "";
        if ((docIDs == null) || (docIDs.size() == 0)) {
            return null;
        }

        for (Object element : docIDs) {
            final String complex_docID = (String) element;
            LOGGER.log(Level.FINEST, "Complex Document ID: " + complex_docID);
            if ((complex_docID == null) || (complex_docID.trim().length() == 0)) {
                LOGGER.log(Level.SEVERE, "One of the docID is found to be null...");
                continue;
            }

            String original = complex_docID;
            if (!complex_docID.contains("|")) {
                try {
                    original = URLDecoder.decode(complex_docID, "UTF-8");
                } catch (final UnsupportedEncodingException e1) {
                    LOGGER.log(Level.WARNING, "Unable to Decode [ "
                            + complex_docID + " ]", e1);
                }
            }

            final StringTokenizer strTok = new StringTokenizer(original,
                    SPConstants.DOC_TOKEN);
            if (strTok == null) {
                LOGGER.log(Level.SEVERE, "Failed to create tokenizer for the docID [ "
                        + complex_docID + " ]. ");
                continue;
            }

            String listURL = strTok.nextToken();
            final Matcher match = SPConstants.ATTACHMENT_SUFFIX_PATTERN.matcher(listURL);
            if (match.find()) {
                final int index = match.end();
                listURL = listURL.substring(index);
            } else if (listURL.startsWith(SPConstants.ALERT_SUFFIX_IN_DOCID)) {
                listURL = listURL.substring(SPConstants.ALERT_SUFFIX_IN_DOCID.length());
                if (listURL.endsWith("_" + SPConstants.ALERTS_TYPE)) {
                    listURL = listURL.substring(0, listURL.length()
                            - (1 + SPConstants.ALERTS_TYPE.length()));
                }
            }
            final String DocID = strTok.nextToken();// Get the DocID

            final AuthData document_obj = new AuthData();
            document_obj.setListURL(listURL);
            document_obj.setListItemId(DocID);
            document_obj.setComplexDocId(complex_docID);

            // Even if you have a web application in MOSS 2007 with not site
            // collection at root level
            // still web-services work with Endpoint as web application URL
            final String webApp = Util.getWebApp(listURL);
            Set<AuthData> webappDocSet = hmSortedDocuments.get(webApp);
            if (null == webappDocSet) {
                webappDocSet = new HashSet<AuthData>();
            }
            webappDocSet.add(document_obj);// add the document to the set
            hmSortedDocuments.put(webApp, webappDocSet);// update the hashmap
        }
        return hmSortedDocuments;
    }

    /**
     * Construct the AuthorizationResponse for each AuthData after authorization
     *
     * @param authDocs List of all the authorized documents as returned by the
     *            Web Service.
     * @return The AuthorizationResponse to be sent to CM
     */
    private List<AuthorizationResponse> getAuthResponseFromAuthData(
            final AuthData[] authDocs) {
        final List<AuthorizationResponse> response = new ArrayList<AuthorizationResponse>();
        for (AuthData element : authDocs) {
            if ((element.getError() != null)
                    && (element.getError().length() != 0)) {
                LOGGER.log(Level.WARNING, "Web Service has thrown the following error while authorizing. \n Error: "
                        + element.getError());
            }
            final boolean status = element.isIsAllowed();
            final String complex_docID = element.getComplexDocId();
            final String logMessage = "[status: " + status
                    + "], Complex Document ID: [ " + complex_docID + " ] ";
            if (status) {
                LOGGER.log(Level.FINER, logMessage);
            } else {
                LOGGER.log(Level.WARNING, logMessage);
            }
            response.add(new AuthorizationResponse(status, complex_docID));
        }
        return response;
    }
}
