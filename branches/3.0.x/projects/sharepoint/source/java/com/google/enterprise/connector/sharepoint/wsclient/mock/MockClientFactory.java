// Copyright 2011 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.wsclient.mock;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.Folder;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.sharepoint.wsclient.client.AclWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.AlertsWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.BulkAuthorizationWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.ClientFactory;
import com.google.enterprise.connector.sharepoint.wsclient.client.ListsWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDataWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDiscoveryWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfile2003WS;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfile2007WS;
import com.google.enterprise.connector.sharepoint.wsclient.client.WebsWS;

import org.xml.sax.helpers.AttributesImpl;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpMethodBase;

import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Logger;
import java.util.UUID;

/**
 * A mock factory for the SharePoint webservices.
 */
public class MockClientFactory implements ClientFactory {
  private static final Logger LOGGER = Logger.getLogger(MockClientFactory.class.getName());

  public MockClientFactory() {
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  /* @Override */
  public AlertsWS getAlertsWS(final SharepointClientContext ctx) {
    return new MockAlertsWS(ctx);
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  /* @Override */
  public BulkAuthorizationWS getBulkAuthorizationWS(
      final SharepointClientContext ctx) throws SharepointException {
    return new MockBulkAuthorizationWS(ctx);
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  /* @Override */
  public ListsWS getListsWS(final SharepointClientContext ctx,
      final String rowLimit) {
    return new MockListsWS(ctx);
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  /* @Override */
  public SiteDataWS getSiteDataWS(final SharepointClientContext ctx) {
    return new MockSiteDataWS(ctx);
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  /* @Override */
  public UserProfile2003WS getUserProfile2003WS(
      final SharepointClientContext ctx) {
    return new MockUserProfile2003WS(ctx);
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  /* @Override */
  public UserProfile2007WS getUserProfile2007WS(
      final SharepointClientContext ctx) {
    return new MockUserProfile2007WS(ctx);
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  /* @Override */
  public WebsWS getWebsWS(final SharepointClientContext ctx) {
    return new MockWebsWS(ctx);
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  /* @Override */
  public AclWS getAclWS(final SharepointClientContext ctx, String webUrl) {
    return new MockAclWS(ctx, webUrl);
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  /* @Override */
  public SiteDiscoveryWS getSiteDiscoveryWS(final SharepointClientContext ctx,
    String webUrl) {
    return new MockSiteDiscoveryWS(ctx, webUrl);
  }

  /**
   * (@inheritDoc)
   *
   * Returns the HTTP result 200 code which indicates that the connector 
   * can connect to the host.
   */
  /* @Override */
  public int checkConnectivity(HttpMethodBase method, 
      Credentials credentials) throws IOException {
    return 200;
  }

  /**
   * (@inheritDoc)
   *
   * Returns the Sharepoint version "14.0.0" which indicates that it's 
   * Sharepoint 2007.
   */
  /* @Override */
  public String getResponseHeader(HttpMethodBase method, String headerName) {
    String headerValue;
    if ("MicrosoftSharePointTeamServices".equals(headerName)) {
      headerValue = "14.0.0";
    } else {
      headerValue = null;
    }
    return headerValue;
  }

  /**
   * Creates a new {@link ListState}.
   *
   * @param webUrl The URL of the parent web
   * @param listName The name of the new list
   * @param ws The web state of the parent web
   * @param feedType The feed type of the new list
   * @param fixedId If true created an ID from the list URL; otherwise
   *          a new random ID is created
   * @return a new {@link ListState}
   */
  protected ListState createListState(final String webUrl, 
      final String listName, final WebState ws, final FeedType feedType,
      final Boolean fixedId) throws SharepointException {
    String listUrl = webUrl + "/" + listName;
    String listId = generateId(listUrl, fixedId);

    AttributesImpl attr = new AttributesImpl();
    attr.addAttribute("", "", SPConstants.STATE_ID, "", listId);
    attr.addAttribute("", "", SPConstants.STATE_BIGGESTID, "", "0");
    attr.addAttribute("", "", SPConstants.STATE_TYPE, "",
        SPConstants.DOC_LIB);
    attr.addAttribute("", "", SPConstants.STATE_URL, "", listUrl);
    attr.addAttribute("", "", SPConstants.STATE_LASTMODIFIED, "",
        Util.formatDate(Calendar.getInstance()));
    attr.addAttribute("", "", SPConstants.STATE_CHANGETOKEN, "",
        "mock-change-token");
    return ListState.loadStateFromXML(ws, attr, feedType);
  }

  /**
   * Creates a new {@link SPDocument}.
   *
   * @param webUrl The URL of the parent web
   * @param docName The name of the new document
   * @param feedType The feed type of the new document
   * @param fixedId If true created an ID from the list URL; otherwise
   *          a new random ID is created
   * @return a new {@link SPDocument}
   */
  public static SPDocument createDocument(final String webUrl, 
      final String docName, final FeedType feedType, final Boolean fixedId) {
    String docUrl = webUrl + "/" + docName;
    String docId = generateId(docUrl, fixedId);

    // TODO: Using hardcoded SPType.SP2007.
    return new SPDocument(docId, docUrl, Calendar.getInstance(),
        SPConstants.NO_AUTHOR, SPConstants.NO_OBJTYPE, 
        SPConstants.PARENT_WEB_TITLE, feedType, SPConstants.SPType.SP2007);
  }

  /**
   * Creates a new {@link Folder}.
   *
   * @param webUrl The URL of the parent web
   * @param folderName The name of the new folder
   * @return a new {@link Folder}
   */
  public static Folder createFolder(final String webUrl, 
      final String folderName) {
    String folderUrl = webUrl + "/" + folderName;
    String folderId = "" + (int) System.currentTimeMillis();
    return new Folder(folderUrl, folderId);
  }

  /**
   * Generates a new Sharepoint ID.
   *
   * @param url the URL of the object
   * @param fixedId a flag inidicating whether to use fixed or random ID's
   * @return a valid string ID
   */
  public static String generateId(final String url, Boolean fixedId) {
    String id;
    if (fixedId) {
      // This generates an ID from the URL that is passed in so that we get
      // the same ID for a URL.
      id = Integer.toHexString(url.hashCode());
      id = String.format("%1$#32s", id).replace(" ", "0");
      id = "{" + id.substring(0, 8) + "-" + id.substring(8, 12) + "-" +
          id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + 
          id.substring(20) + "}";
    } else {
      // This just generates a random ID.
      id = "{" + UUID.randomUUID().toString() + "}";
    }
    return id;
  }

  /**
   * Generates a new URL from a parent URL and a child name.
   *
   * @param parentUrl The URL of the parent, this can be an empty string 
   *          if the child is at the root
   * @param childName The name of the child
   * @return a valid string that is the concatenation of the parent URL
   *           and child name
   */
  public static String makeUrl(final String parentUrl,
      final String childName) {
    String url;
    if (parentUrl.length() > 0)
      url = parentUrl + "/" + childName;
    else
      url = childName;
    return url;
  }
}
