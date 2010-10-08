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

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.AuthData;
import com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.AuthDataPacket;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.wsclient.GSBulkAuthorizationWS;
import com.google.enterprise.connector.sharepoint.wsclient.GSSiteDiscoveryWS;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.RepositoryException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

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
    private final String UNKNOWN_SITE_COLLECTION = "UNKNOWN_SITE_COLLECTION";

    /**
     * Web Application and all the site collection URL's path that are hosted
     * under it. These site collection URLs are used for grouping authZ urls as
     * per their parent site collection URLs. The URLs are arranged in
     * non-increasing order of the length of their paths.
     * <p/>
     * XXX The best place to have this information is in the connector's state.
     * This can be a subset of {@link GlobalState#getAllWebStateSet()}
     */
    final private Map<String, Set<String>> webappToSiteCollections = new HashMap<String, Set<String>>();

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

        // A comparator that sorts in non-increasing order of length
        Comparator<String> nonIncreasingComparator = new Comparator<String>() {
            public int compare(String str1, String str2) {
                if (null == str1) {
                    if (null == str2) {
                        return 0;
                    } else {
                        return 1;
                    }
                } else {
                    if (null == str1) {
                        return -1;
                    } else if (str1.equals(str2)) {
                        return 0;
                    } else {
                        return str2.length() - str1.length();
                    }
                }
            };
        };

        // Populate all site collection URLs using the above comparator
        GSSiteDiscoveryWS siteDiscoWs = new GSSiteDiscoveryWS(
                inSharepointClientContext);
        Set<String> siteCollUrls = siteDiscoWs.getMatchingSiteCollections();
        for (String siteCollUrl : siteCollUrls) {
            String webapp = Util.getWebApp(siteCollUrl);
            Set<String> urlPaths = null;
            if (webappToSiteCollections.containsKey(webapp)) {
                urlPaths = webappToSiteCollections.get(webapp);
            } else {
                urlPaths = new TreeSet<String>(nonIncreasingComparator);
                webappToSiteCollections.put(webapp, urlPaths);
            }
            try {
                urlPaths.add(new URL(siteCollUrl).getPath());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Could not register path [ "
                        + siteCollUrl + " ]. ", e);
            }
        }
    }

    String getSiteCollectionUrlPath(String strUrl)
            throws MalformedURLException {
        String webapp = Util.getWebApp(strUrl);
        String path = new URL(strUrl).getPath();
        if (null == path || path.length() == 0) {
            return webapp;
        }

        Set<String> siteCollUrlPaths = webappToSiteCollections.get(webapp);
        if (null == siteCollUrlPaths) {
            return UNKNOWN_SITE_COLLECTION;
        }

        for (String siteCollUrlPath : siteCollUrlPaths) {
            if (path.startsWith(siteCollUrlPath)) {
                return webapp + siteCollUrlPath;
            }
        }
        return UNKNOWN_SITE_COLLECTION;
    }

    /**
     * Authorizes a user represented by AuthenticationIdentity against all the
     * docIDs. All the docIDs are first converted into a format as expected by
     * the GSBulkAuthorization web service. The web service expects the
     * documents to be sent in the form of {@link AuthData} and
     * {@link AuthDataPacket}. An instance of AuthData contains all the document
     * specific details required for authorization. AuthDataPacket helps to
     * group AuthData units according to their parent site collection.
     * <p/>
     * WS calls are made per web application. So, AuthDataPackets are finally
     * grouped as per the web application.
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
        long startTime = System.currentTimeMillis();
        if (identity == null) {
            throw new SharepointException("Identity is null");
        }
        if (docIDs == null) {
            throw new SharepointException("Document IDs is null");
        }

        String userName = identity.getUsername();
        String domain = identity.getDomain();

        LOGGER.log(Level.INFO, "Received for authZ: [Docs Count: #"
                + docIDs.size() + "], [Username: " + userName + "], [domain: "
                + domain + " ]. ");

        // If domain is not received as part of the authorization request, use
        // the one from SharePointClientContext
        if ((domain == null) || (domain.length() == 0)) {
            domain = sharepointClientContext.getDomain();
        }
        userName = Util.getUserNameWithDomain(userName, domain);
        LOGGER.log(Level.INFO, "Authorizing User " + userName);

        final List<AuthorizationResponse> response = new ArrayList<AuthorizationResponse>();

        // documents are arranged per web application per site collection
        final Map<String, Map<String, Set<AuthData>>> webAppSorted = groupDocIds(docIDs);
        LOGGER.log(Level.INFO, "A Total of " + webAppSorted.size()
                + " WS calls will be made for authorization.");

        for (Entry<String, Map<String, Set<AuthData>>> webAppEntry : webAppSorted.entrySet()) {
            final String webapp = webAppEntry.getKey();
            Map<String, Set<AuthData>> siteCollSorted = webAppEntry.getValue();
            if (null == siteCollSorted) {
                continue;
            }

            AuthDataPacket[] authDataPacketArray = new AuthDataPacket[siteCollSorted.size()];
            int i = 0;
            for (Entry<String, Set<AuthData>> siteCollEntry : siteCollSorted.entrySet()) {
                Set<AuthData> authDataSet = siteCollEntry.getValue();

                AuthDataPacket authDataPacket = new AuthDataPacket();
                authDataPacket.setSiteCollectionUrl(siteCollEntry.getKey());

                AuthData[] authDataArray = new AuthData[authDataSet.size()];
                authDataArray = authDataSet.toArray(authDataArray);
                authDataPacket.setAuthDataArray(authDataArray);

                authDataPacketArray[i++] = authDataPacket;
            }

            if (null == authDataPacketArray || authDataPacketArray.length == 0) {
                continue;
            }

            GSBulkAuthorizationWS bulkAuthWS = null;
            try {
                if (authDataPacketArray.length == 1
                        && !UNKNOWN_SITE_COLLECTION.equals(authDataPacketArray[0].getSiteCollectionUrl())) {
                    sharepointClientContext.setSiteURL(authDataPacketArray[0].getSiteCollectionUrl());
                    bulkAuthWS = new GSBulkAuthorizationWS(
                            sharepointClientContext);
                    authDataPacketArray[0] = bulkAuthWS.authorizeInCurrentSiteCollectionContext(authDataPacketArray[0], userName);
                } else {
                    sharepointClientContext.setSiteURL(webapp);
                    bulkAuthWS = new GSBulkAuthorizationWS(
                            sharepointClientContext);
                    authDataPacketArray = bulkAuthWS.authorize(authDataPacketArray, userName);
                }
            } catch (final Exception e) {
                LOGGER.log(Level.WARNING, "WS call failed for GSBulkAuthorization using webapp [ "
                        + webapp + " ] ", e);
                continue;
            }

            if (null == authDataPacketArray) {
                LOGGER.log(Level.SEVERE, "WS call failed for GSBulkAuthorization using webapp [ "
                        + webapp
                        + " ] AuthDataPacketArray is null at the completion of call. ");
                continue;
            }

            // convert the document object back to complex_docid and create
            // response
            response.addAll(getAuthResponseFromAuthData(authDataPacketArray));
        }

        LOGGER.log(Level.INFO, "This batch of request complited in "
                + ((double) (System.currentTimeMillis() - startTime) / (double) 1000)
                + " seconds. Total docs received was #" + docIDs.size()
                + ". Total authorized #" + response.size());
        return response;
    }

    /**
     * Creates AuthData object every docID and group these objects as per the
     * web application and site collection.
     *
     * @param docIDs AuthData object is created for each document represented by
     *            the docID
     * @return A map where the web application is mapped to a map which maps
     *         site collections to the documents
     */
    private Map<String, Map<String, Set<AuthData>>> groupDocIds(
            final Collection<String> docIDs) {
        final Map<String, Map<String, Set<AuthData>>> sortedDocuments = new HashMap<String, Map<String, Set<AuthData>>>();
        if ((docIDs == null) || (docIDs.size() == 0)) {
            return null;
        }

        for (Object docId : docIDs) {
            final String complex_docID = (String) docId;
            if ((complex_docID == null) || (complex_docID.trim().length() == 0)) {
                LOGGER.log(Level.WARNING, "One of the docIDs is null!");
                continue;
            }

            String original = null;
            try {
                original = URLDecoder.decode(complex_docID, "UTF-8");
            } catch (final UnsupportedEncodingException e1) {
                LOGGER.log(Level.WARNING, "Unable to Decode!", e1);
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

            final String DocID = strTok.nextToken();

            final AuthData authData = new AuthData();
            authData.setContainer(listURL);
            authData.setItemId(DocID);
            authData.setComplexDocId(complex_docID);

            final String webApp = Util.getWebApp(listURL);
            Map<String, Set<AuthData>> siteCollMap = sortedDocuments.get(webApp);
            if (null == siteCollMap) {
                siteCollMap = new HashMap<String, Set<AuthData>>();
                sortedDocuments.put(webApp, siteCollMap);
            }

            String siteCollUrl = null;
            try {
                siteCollUrl = getSiteCollectionUrlPath(listURL);
            } catch (MalformedURLException e) {
                LOGGER.log(Level.WARNING, "DocId [ "
                        + complex_docID
                        + " ] cannot be authorized becasue it refers to an invalid list URL [ "
                        + listURL + " ] ");
                continue;
            }
            Set<AuthData> authDataSet = siteCollMap.get(siteCollUrl);
            if(null == authDataSet) {
                authDataSet = new HashSet<AuthData>();
                siteCollMap.put(siteCollUrl, authDataSet);
            }

            authDataSet.add(authData);
        }
        return sortedDocuments;
    }

    /**
     * Construct the AuthorizationResponse for each AuthData after authorization
     *
     * @param authDocs List of all the authorized documents as returned by the
     *            Web Service.
     * @return The AuthorizationResponse to be sent to CM
     */
    private List<AuthorizationResponse> getAuthResponseFromAuthData(
            final AuthDataPacket[] authDataPacketArray) {
        final List<AuthorizationResponse> response = new ArrayList<AuthorizationResponse>();

        for (AuthDataPacket authDataPacket : authDataPacketArray) {
            if (null == authDataPacket) {
                continue;
            }
            if(!authDataPacket.isIsDone()) {
                int count = (null == authDataPacket.getAuthDataArray()) ? 0 : authDataPacket.getAuthDataArray().length;
                LOGGER.log(Level.WARNING, "Authorization of #"
                        + count
                        + " documents from site collection [ "
                        + authDataPacket.getSiteCollectionUrl()
                        + " ] was not completed becasue web service encountered following error -> "
                        + authDataPacket.getMessage());
                for (AuthData authData : authDataPacket.getAuthDataArray()) {
                    LOGGER.log(Level.WARNING, "Authorization of DocId [ "
                            + authData.getComplexDocId()
                            + " ] was not completed becasue web service encountered following error -> "
                            + authData.getMessage());
                }
                continue;
            }

            AuthData[] authdataArray = authDataPacket.getAuthDataArray();
            for (AuthData authData : authdataArray) {
                if (!authData.isIsDone()) {
                    LOGGER.log(Level.WARNING, "Authorization of DocId [ "
                            + authData.getComplexDocId()
                            + " ] was not completed becasue web service encountered following error -> "
                            + authData.getMessage());
                    continue;
                }

                final boolean authZstatus = authData.isIsAllowed();
                final String complex_docID = authData.getComplexDocId();
                final String logMessage = "[AuthZ status: " + authZstatus
                        + "] for DocID: [ " + complex_docID
                        + " ] ";
                if (authZstatus) {
                    LOGGER.log(Level.FINER, logMessage);
                } else {
                    LOGGER.log(Level.WARNING, logMessage);
                }
                response.add(new AuthorizationResponse(authZstatus,
                        complex_docID));
            }
        }

        return response;
    }
}
