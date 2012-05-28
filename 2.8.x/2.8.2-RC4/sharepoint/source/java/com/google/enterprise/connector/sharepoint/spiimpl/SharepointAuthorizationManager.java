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
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.AuthData;
import com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.AuthDataPacket;
import com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.Container;
import com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.ContainerType;
import com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.EntityType;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.wsclient.GSBulkAuthorizationWS;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Authorizes items by making web service call to GsBulkAuthorization web
 * service. Documents are grouped per web application per site collection. One
 * web service call is made for each web application. In every call, an array of
 * {@link AuthDataPacket} is passed wherein one AuthDataPacket corresponds to
 * one site collection. Such groupings makes web service job easier and puts
 * less overhead on SharePoint content database. This is required because
 * authorization is not flattened as such and requires knowledge of parent
 * containers where the item is stored. If site collection information is cannot
 * be found for a document URL, the web service still works with some loss of
 * performance.
 * <p/>
 * Every {@link AuthDataPacket} contains an array of {@link AuthData} which is
 * the basic authorization unit and corresponds to an item to be authorized. For
 * detailed structuring of AuthData and AuthDataPacket, refer to the web service
 * documentation.
 *
 * @author nitendra_thakur
 */
public class SharepointAuthorizationManager implements AuthorizationManager {
  private static Logger LOGGER = Logger.getLogger(SharepointAuthorizationManager.class.getName());
  SharepointClientContext sharepointClientContext;

  /**
   * Web Application and all the site collection URL's path that are hosted
   * under it. These site collection URLs are used for grouping authZ urls as
   * per their parent site collection URLs. The URLs are arranged in
   * non-increasing order of their length.
   * <p/>
   * TODO The best place to have this information is in the connector's state.
   * This can be a subset of {@link GlobalState#getAllWebStateSet()}
   */
  // TODO When to delete entries from this? This would happen when a site
  // collection is deleted from SharePoint.
  final private Map<String, Set<String>> webappToSiteCollections = new HashMap<String, Set<String>>();

  /**
   * Attachments are sent as independent documents to GSA. Their authorization
   * however is not any different from the item which contains the attachment.
   * Hence, if there are multiple attachments of a single item are to be
   * authorized then it make sense to authorize the item itself and replicate
   * the authZ status for every attachments that it owns.
   * <p/>
   * This class acts as a key (hence, final and immutable) for storing
   * authorization dependency information in such case.
   *
   * @author nitendra_thakur
   */
  private final static class AttachmentKey {
    private final String listUrl;
    private final String itemId;

    public AttachmentKey(String listUrl, String itemId) {
      if (null == listUrl || null == itemId) {
        throw new NullPointerException("listUrl [ " + listUrl + " ], ItemId [ "
            + itemId + " ]");
      }
      this.listUrl = listUrl;
      this.itemId = itemId;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof AttachmentKey) {
        AttachmentKey inAttachment = (AttachmentKey) obj;
        if (inAttachment.listUrl.equals(this.listUrl)
            && inAttachment.itemId.equals(this.itemId)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public int hashCode() {
      return listUrl.hashCode() + itemId.hashCode();
    }
  }

  // Keeps track of authZ dependency of attachments where authZ status of an
  // item can be safely replicated to many others
  Map<AttachmentKey, List<String>> attachments = null;

  /**
   * @param inSharepointClientContext Context Information is required to create
   *          the instance of this class
   */
  public SharepointAuthorizationManager(
      final SharepointClientContext inSharepointClientContext,
      final Set<String> siteCollUrls) throws SharepointException {
    if (inSharepointClientContext == null) {
      throw new SharepointException("SharePointClientContext can not be null");
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
          if (null == str2) {
            return -1;
          } else {
            int comp = str2.length() - str1.length();
            if (comp == 0) {
              comp = str2.compareTo(str1);
            }
            return comp;
          }
        }
      };
    };

    // Populate all site collection URLs using the above comparator
    try {
      for (String siteCollUrl : siteCollUrls) {
        String webapp = Util.getWebApp(siteCollUrl);
        Set<String> urlPaths = null;
        if (webappToSiteCollections.containsKey(webapp)) {
          urlPaths = webappToSiteCollections.get(webapp);
        } else {
          urlPaths = new TreeSet<String>(nonIncreasingComparator);
          webappToSiteCollections.put(webapp, urlPaths);
        }
        urlPaths.add(new URL(siteCollUrl).getPath());
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Prolem occured while registering site collection URLs ", e);
      // A partial fill can be buggy
      webappToSiteCollections.clear();
    }
  }

  /**
   * Finds the site collection URL of a SharePoint URL by matching it with the
   * populated list of known site collection URLs.
   *
   * @param strUrl
   * @return
   * @throws MalformedURLException
   */
  private Container getSiteCollectionContainer(String strUrl)
      throws MalformedURLException {
    Container container = new Container();
    container.setType(ContainerType.NA);

    String webapp = Util.getWebApp(strUrl);
    Set<String> siteCollUrlPaths = webappToSiteCollections.get(webapp);
    if (null != siteCollUrlPaths) {
      String path = new URL(strUrl).getPath();
      if (null == path || path.length() == 0) {
        container.setUrl(webapp);
        container.setType(ContainerType.SITE_COLLECTION);
      } else {
        for (String siteCollUrlPath : siteCollUrlPaths) {
          if (path.startsWith(siteCollUrlPath)) {
            container.setUrl(webapp + siteCollUrlPath);
            container.setType(ContainerType.SITE_COLLECTION);
            break;
          }
        }
      }
    }

    return container;
  }

  /**
   * Authorizes a user represented by AuthenticationIdentity against all the
   * docIDs. All the docIDs are first converted into a format as expected by the
   * GSBulkAuthorization web service. The web service expects the documents to
   * be sent in the form of {@link AuthData} and {@link AuthDataPacket}. An
   * instance of AuthData contains entire document specific details required for
   * authorization. AuthDataPacket helps to group AuthData units according to
   * their parent site collection.
   * <p/>
   * WS calls are made per web application. So, AuthDataPackets are finally
   * grouped as per the web application.
   *
   * @param docIDs Document IDs to be authorized. These document IDs had been
   *          initially constructed and sent to GSA by the connector itself. The
   *          document IDs are in the format:
   *          &lt;Parent_List_URL&gt;|&lt;Original_Doc_ID&gt; The allowed
   *          prefixes to the format are: [ALERT] represents alerts which have
   *          been sent as a SPDocument [ATTACHMENT][&lt;Attachment_URL&gt;] The
   *          document IDs are carefully parsed to construct the AuthData object
   *          for each document.
   * @param identity Represents the user to be authorized.
   */
  public Collection<AuthorizationResponse> authorizeDocids(
      final Collection<String> docIDs, final AuthenticationIdentity identity)
      throws RepositoryException {

    long startTime = System.currentTimeMillis();
    if (identity == null) {
      throw new SharepointException("Identity is null");
    }
    if (docIDs == null) {
      throw new SharepointException("Document IDs is null");
    }

    String userName = identity.getUsername();
    String domain = identity.getDomain();

    LOGGER.log(Level.INFO, "Received authZ request for " + docIDs.size()
        + " docs. Username [ " + userName + " ], domain [ " + domain + " ]. ");

    // If domain is not received as part of the authorization request, use
    // the one from SharePointClientContext
    if ((domain == null) || (domain.length() == 0)) {
      domain = sharepointClientContext.getDomain();
    }
    userName = Util.getUserNameWithDomain(userName, domain);
    LOGGER.log(Level.INFO, "Authorizing User " + userName);

    attachments = new HashMap<AttachmentKey, List<String>>();

    // authZ response are filled here
    final List<AuthorizationResponse> response = new ArrayList<AuthorizationResponse>(
        docIDs.size());

    // documents are arranged per web application per site collection
    final Map<String, Map<Container, Set<AuthData>>> groupedDocIds = groupDocIds(docIDs);

    LOGGER.log(Level.CONFIG, "A Total of #" + groupedDocIds.size()
        + " WS calls will be made for authorization.");

    // For every entry in groupedDocIds, makes one WS call and send the AuthData
    // as payload for authorization
    for (Entry<String, Map<Container, Set<AuthData>>> webAppEntry : groupedDocIds.entrySet()) {
      final String webapp = webAppEntry.getKey();
      Map<Container, Set<AuthData>> siteCollSorted = webAppEntry.getValue();
      if (null == siteCollSorted) {
        continue;
      }

      AuthDataPacket[] authDataPacketArray = new AuthDataPacket[siteCollSorted.size()];
      int i = 0;
      for (Entry<Container, Set<AuthData>> siteCollEntry : siteCollSorted.entrySet()) {
        Set<AuthData> authDataSet = siteCollEntry.getValue();

        AuthDataPacket authDataPacket = new AuthDataPacket();
        authDataPacket.setContainer(siteCollEntry.getKey());

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
        sharepointClientContext.setSiteURL(webapp);
        bulkAuthWS = new GSBulkAuthorizationWS(sharepointClientContext);
        authDataPacketArray = bulkAuthWS.authorize(authDataPacketArray, userName);
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
      response.addAll(getAuthResponse(authDataPacketArray));
    }

    LOGGER.log(Level.INFO, "This batch of request completed in "
        + ((double) (System.currentTimeMillis() - startTime) / (double) 1000)
        + " seconds. Total docs received was #" + docIDs.size()
        + ". Total authorized #" + response.size());
    return response;
  }

  /**
   * Creates AuthData object every docID and group these objects as per the web
   * application and site collection. Outer map is for web application based
   * mapping; inner map is for site collection
   *
   * @param docIDs AuthData object is created for each document represented by
   *          the docID
   * @return A map where the web application is mapped to a map which maps site
   *         collections to the documents
   */
  private Map<String, Map<Container, Set<AuthData>>> groupDocIds(
      final Collection<String> docIDs) {
    final Map<String, Map<Container, Set<AuthData>>> sortedDocuments = new HashMap<String, Map<Container, Set<AuthData>>>();
    if ((docIDs == null) || (docIDs.size() == 0)) {
      return null;
    }

    for (Object docId : docIDs) {
      final String complexDocId = (String) docId;
      AuthData authData = null;
      try {
        authData = createAuthDataFromComplexDocId(complexDocId);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Parsing failure! Skipping DocId [ "
            + complexDocId + " ] ", e);
        continue;
      }
      if (null == authData) {
        continue;
      }

      final String webApp = Util.getWebApp(authData.getContainer().getUrl());
      Map<Container, Set<AuthData>> siteCollMap = sortedDocuments.get(webApp);
      if (null == siteCollMap) {
        siteCollMap = new HashMap<Container, Set<AuthData>>();
        sortedDocuments.put(webApp, siteCollMap);
      }

      Container siteCollContainer = null;
      try {
        siteCollContainer = getSiteCollectionContainer(authData.getContainer().getUrl());
      } catch (MalformedURLException e) {
        LOGGER.log(Level.WARNING, "Skipping DocId [ "
            + authData.getComplexDocId() + " ] ", e);
        continue;
      }
      Set<AuthData> authDataSet = siteCollMap.get(siteCollContainer);
      if (null == authDataSet) {
        authDataSet = new HashSet<AuthData>();
        siteCollMap.put(siteCollContainer, authDataSet);
      }

      authDataSet.add(authData);
    }
    return sortedDocuments;
  }

  /**
   * Creates AuthData for DocId.
   *
   * @param complexDocId
   * @return null if no AuthData is required for this DocID. This will happen
   *         when the DocId is an attachment and its authorization is
   *         functionally dependent on the authorization of the item which
   *         contains the attachment.
   */
  private AuthData createAuthDataFromComplexDocId(String complexDocId) {
    String originalComplexDocId = complexDocId;
    try {
      complexDocId = URLDecoder.decode(complexDocId, "UTF-8");
    } catch (final UnsupportedEncodingException e1) {
      LOGGER.log(Level.WARNING, "Unable to Decode!", e1);
    }
    final Container container = new Container();
    StringTokenizer strTok = null;
    String URL = null;
    String DocID = null;
    // In case of meta URL feed, separate list URL from the docID using
    // "?" as a delimiter.
    if (complexDocId.indexOf(SPConstants.EQUAL_TO) != -1) {
      strTok = new StringTokenizer(complexDocId,
          SPConstants.META_URL_FEED_DOC_TOKEN);
      URL = strTok.nextToken();
      String tempDocID = strTok.nextToken();
      // to remove "ID=" from docID.
      DocID = tempDocID.substring(3);
    } else {
      // This change in case of content feed.
      strTok = new StringTokenizer(complexDocId, SPConstants.DOC_TOKEN);
      URL = strTok.nextToken();
      if (strTok.hasMoreElements()) {
        DocID = strTok.nextToken();
      }
    }

    if (URL != null && URL.endsWith(SPConstants.DEFAULT_SITE_LANDING_PAGE)) {
      // If the URL ends with default.aspx, the container type should be SITE.
      container.setType(ContainerType.SITE);
    } else {
      container.setType(ContainerType.LIST);
    }
    final AuthData authData = new AuthData();
    authData.setContainer(container);

    boolean isAttachment = false;

    try {
      Integer.parseInt(DocID);
      authData.setType(EntityType.LISTITEM);
    } catch (final Exception e) {
      if (URL.endsWith(SPConstants.DEFAULT_SITE_LANDING_PAGE)) {
        authData.setType(EntityType.SITE);
      } else {
        if (!URL.endsWith(SPConstants.ASPX)) {
          authData.setType(EntityType.LISTITEM);
        } else {
          authData.setType(EntityType.LIST);
        }
      }
    }

    // Fix me: Get the details of Attachments and Alert URLs in case of
    // Meta and URL feed mode and find away to get required information
    // to authorize.

    if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
      final Matcher match = SPConstants.ATTACHMENT_SUFFIX_PATTERN.matcher(URL);
      if (match.find()) {
        final int index = match.end();
        URL = URL.substring(index);
        isAttachment = true;
      } else if (URL.startsWith(SPConstants.ALERT_SUFFIX_IN_DOCID)) {
        URL = URL.substring(SPConstants.ALERT_SUFFIX_IN_DOCID.length());
        if (URL.endsWith("_" + SPConstants.ALERTS_TYPE)) {
          URL = URL.substring(0, URL.length()
              - (1 + SPConstants.ALERTS_TYPE.length()));
        }
        container.setType(ContainerType.SITE);
        authData.setType(EntityType.ALERT);
      }
    } else {
      Pattern pattern = Pattern.compile(SPConstants.ATTACHMENTS);
      Matcher match = pattern.matcher(URL);
      if (match.find()) {
        isAttachment = true;
        DocID = getDocIDFromAttachmentURLInMetaUrlFeedMode(URL);
        URL = getAttachmentUrlInMetaUrlFeedMode(URL);
        container.setType(ContainerType.LIST);
        authData.setType(EntityType.LISTITEM);
      } else if (complexDocId.contains(SPConstants.ALERTS_EQUALTO)) {
        DocID = DocID.substring(4, DocID.indexOf("}"));
        URL = URL.substring(0, URL.indexOf("_layouts"));
        container.setType(ContainerType.SITE);
        authData.setType(EntityType.ALERT);
      }
    }
    container.setUrl(URL);

    if (isAttachment) {
      AttachmentKey attachmentKey = new AttachmentKey(URL, DocID);
      if (attachments.containsKey(attachmentKey)) {
        attachments.get(attachmentKey).add(originalComplexDocId);
        return null;
      } else {
        attachments.put(attachmentKey, new LinkedList<String>());
      }
    }

    authData.setItemId(DocID);
    authData.setComplexDocId(originalComplexDocId);

    return authData;
  }

  /**
   * @param URL Attachments record URL in case of Meta and URL feed mode.
   * @return document ID
   */
  private String getDocIDFromAttachmentURLInMetaUrlFeedMode(String URL) {
    String tempDocID = URL.substring(URL.indexOf("Attachments") + 12);
    return tempDocID.substring(0, tempDocID.indexOf("/"));
  }

  /**
   * @param URL Attachments record URL in case of META and URL feed mode.
   * @return valid attachment URL to authorize in case of Meta and URL feed
   *         mode.
   */
  private String getAttachmentUrlInMetaUrlFeedMode(String URL) {
    String tempUrl = URL.substring(0, URL.indexOf("Attachments"));
    tempUrl += "AllItems.aspx";
    // tempUrl.substring(tempUrl.indexOf("[", 2) + 1);
    return tempUrl;

  }

  /**
   * Construct the AuthorizationResponse for each AuthData after authorization
   *
   * @param authDocs List of all the authorized documents as returned by the Web
   *          Service.
   * @return The AuthorizationResponse to be sent to CM
   */
  private List<AuthorizationResponse> getAuthResponse(
      final AuthDataPacket[] authDataPacketArray) {
    final List<AuthorizationResponse> response = new ArrayList<AuthorizationResponse>();

    for (AuthDataPacket authDataPacket : authDataPacketArray) {
      if (!checkAuthDataPacketAfterAuthZ(authDataPacket)) {
        continue;
      }
      AuthData[] authdataArray = authDataPacket.getAuthDataArray();
      for (AuthData authData : authdataArray) {
        if (!checkAuthDataAfterAuthZ(authData)) {
          continue;
        }

        addToResponse(response, authData.getComplexDocId(), authData.isIsAllowed());

        if (authData.getComplexDocId().startsWith(SPConstants.ATTACHMENT_SUFFIX_IN_DOCID)) {
          AttachmentKey attachmentKey = new AttachmentKey(
              authData.getContainer().getUrl(), authData.getItemId());
          List<String> dependentDocIds = attachments.get(attachmentKey);
          if (null != dependentDocIds) {
            for (String complexDocId : dependentDocIds) {
              addToResponse(response, complexDocId, authData.isIsAllowed());
            }
            attachments.remove(attachmentKey);
          }
        }
      }
    }

    return response;
  }

  /**
   * Checks if this packet was processed successfully and is good to proceed
   *
   * @param authDataPacket
   * @return true if documents in this packet have been authorized and their
   *         status can be sent back to GSA
   */
  private boolean checkAuthDataPacketAfterAuthZ(
      final AuthDataPacket authDataPacket) {
    if (null == authDataPacket) {
      LOGGER.log(Level.SEVERE, "One of the AuthDataPacket objects is null after authZ!");
      return false;
    }

    if (authDataPacket.isIsDone()) {
      LOGGER.config("WS Message -> " + authDataPacket.getMessage());
      return true;
    }

    int count = (null == authDataPacket.getAuthDataArray()) ? 0
        : authDataPacket.getAuthDataArray().length;
    LOGGER.log(Level.WARNING, "Authorization of #"
        + count
        + " documents from site collection [ "
        + authDataPacket.getContainer().getUrl()
        + " ] was not completed becasue web service encountered following error -> "
        + authDataPacket.getMessage());

    for (AuthData authData : authDataPacket.getAuthDataArray()) {
      LOGGER.log(Level.WARNING, "AuthZ status: INDETERMINATE for DocId [ "
          + authData.getComplexDocId()
          + " ] becasue the current AuthDataPacket packet was discarded due to following WS error -> "
          + authDataPacket.getMessage());

      if (authData.getComplexDocId().startsWith(SPConstants.ATTACHMENT_SUFFIX_IN_DOCID)) {
        AttachmentKey attachmentKey = new AttachmentKey(
            authData.getContainer().getUrl(), authData.getItemId());
        List<String> dependentDocIds = attachments.get(attachmentKey);
        if (null != dependentDocIds) {
          for (String complexDocId : dependentDocIds) {
            LOGGER.log(Level.WARNING, "AuthZ status: INDETERMINATE for DocId [ "
                + complexDocId
                + " ] caused due to failure of DocId [ "
                + authData.getComplexDocId());
          }
          attachments.remove(attachmentKey);
        }
      }
    }
    return false;
  }

  /**
   * Checks if this document was processed successfully
   *
   * @param authDataPacket
   * @return true if this document has been authorized and the status can be
   *         sent back to GSA
   */
  private boolean checkAuthDataAfterAuthZ(final AuthData authData) {
    if (null == authData) {
      LOGGER.log(Level.SEVERE, "One of the AuthData objects is null after authZ!");
      return false;
    }
    if (authData.isIsDone()) {
      LOGGER.config("WS Message -> " + authData.getMessage());
      return true;
    }

    LOGGER.log(Level.WARNING, "AuthZ status: INDETERMINATE for DocId [ "
        + authData.getComplexDocId()
        + " ] because web service encountered following error -> "
        + authData.getMessage());

    if (authData.getComplexDocId().startsWith(SPConstants.ATTACHMENT_SUFFIX_IN_DOCID)) {
      AttachmentKey attachmentKey = new AttachmentKey(
          authData.getContainer().getUrl(), authData.getItemId());
      List<String> dependentDocIds = attachments.get(attachmentKey);
      if (null != dependentDocIds) {
        for (String complexDocId : dependentDocIds) {
          LOGGER.log(Level.WARNING, "AuthZ status: INDETERMINATE for DocId [ "
              + complexDocId + " ] caused due to failure of DocId [ "
              + authData.getComplexDocId());
        }
        attachments.remove(attachmentKey);
      }
    }
    return false;
  }

  /**
   * Creates a {@link AuthorizationResponse} and add it to the collection
   *
   * @param response
   * @param DocId
   * @param status
   * @return {@link Collection#add(Object)}
   */
  private boolean addToResponse(Collection<AuthorizationResponse> response,
      String DocId, boolean status) {
    final String logMessage = "AuthZ status: " + status + " for DocID: "
        + DocId;
    if (status) {
      LOGGER.log(Level.FINE, logMessage);
    } else {
      LOGGER.log(Level.WARNING, logMessage);
    }
    return response.add(new AuthorizationResponse(status, DocId));
  }

  /*
   * For Testing purpose
   */
  public Map<String, Set<String>> getWebappToSiteCollections() {
    return webappToSiteCollections;
  }
}
