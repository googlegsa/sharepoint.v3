// Copyright 2011 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.wsclient.mock;

import com.google.enterprise.connector.sharepoint.client.ListsUtil;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A mock factory for creating a large statefile.
 */
public class LargeStatefileClientFactory extends MockClientFactory {
  private static final Logger LOGGER = Logger.getLogger(LargeStatefileClientFactory.class.getName());

  private static final String siteWithLotsOfLists = "/SiteWithLotsOfLists";
  private static final String siteWithLotsOfDocs = "/SiteWithLotsOfDocs";
  private static final String siteWithLotsOfWebs = "/SiteWithLotsOfWebs";

  private static final int maxListsPerSite = 2000;
  private static final int minListsPerSite = 10;

  // This specifies the number of sub sites to create.
  // maxSubSites is the most site creates, minSubSites is the minimum
  // mumber of sub sites to create for a site, avgSubSites is
  // the number of sub sites to create for average sites.
  private static final int maxSubSites = 2000;
  private static final int avgSubSites = 5;
  private static final int minSubSites = 0;

  // Maximum top level documents supported by Sharepoint is 20000,
  // using only 2000 to reduce the time is takes to generate a very
  // large state file.
  private static final int maxDocumentsPerView = 2000;//20000;

  // This specifies the minumum number of documents in a list.
  private static final int minDocumentsToCreate = 1;

  // This specifies the percentage of additional documents to create
  // with a random id. A value of 1.0 means that if we are creating 2000
  // documents with fixed ID for a list then we will also create an
  // additional 2000 documents with random ID's. This value also applies
  // to lists. This is used to discover new lists and documents in every
  // crawl since these items are tracked by ID.
  private static final double variablePercent = 1.0;

  public LargeStatefileClientFactory() {
  }

  /* @Override */
  public ListsWS getListsWS(final SharepointClientContext ctx,
      final String rowLimit) {
    return new MockListsWS(ctx) {
      /* @Override */
      public List<Folder> getSubFoldersRecursively(final ListState list,
          final Folder folder, final String lastID) {
        return new ArrayList<Folder>();
      }

      /* @Override */
      public List<SPDocument> getListItemChangesSinceToken(final ListState list,
          final String listName, final String viewName,
          final ListsUtil.SPQueryInfo queryInfo, final String token,
          final Set<String> allWebs, final Set<String> deletedIDs,
          final Set<String> restoredIDs, final Set<String> renamedIDs) {
        return getDocuments(list);
      }

      /* @Override */
      public List<SPDocument> getListItems(final ListState list,
          final String listName, final String viewName, 
          final ListsUtil.SPQueryInfo queryInfo, final String webID,
          Set<String> allWebs) {
        return getDocuments(list);
      }

      private List<SPDocument> getDocuments(final ListState list) {
        String listUrl = list.getListURL();
        String path = getURLPath(listUrl);
        int numberOfDocs;
        if (path.startsWith(siteWithLotsOfDocs)) {
          numberOfDocs = maxDocumentsPerView;
        } else {
          numberOfDocs = minDocumentsToCreate;
        }
        List<SPDocument> docs =
            createDocuments(listUrl, numberOfDocs, ctx.getFeedType(), true);
        final int variableCount = (int) (numberOfDocs * variablePercent);
        if (variableCount > 0) {
          appendDocuments(docs, listUrl, variableCount, ctx.getFeedType(),
              false);
        }

        LOGGER.info("Created " + docs.size() + " documents for URL " +
            listUrl + ".");
        return docs;
      }
    };
  }

  /* @Override */
  public SiteDataWS getSiteDataWS(final SharepointClientContext ctx) {
    return new MockSiteDataWS(ctx) {
      /* @Override */
      public List<ListState> getNamedLists(final WebState webstate)
          throws SharepointException {
        String siteUrl = ctx.getSiteURL();
        String path = getURLPath(siteUrl);
        int numberOfLists;
        if (path.startsWith(siteWithLotsOfLists)) {
          numberOfLists = maxListsPerSite;
        } else {
          numberOfLists = minListsPerSite;
        }
        List<ListState> lists = createLists(siteUrl, numberOfLists,
            webstate, ctx.getFeedType(), true);
        final int variableCount = (int)(numberOfLists * variablePercent);
        if (variableCount > 0) {
          appendLists(lists, siteUrl, variableCount, webstate,
              ctx.getFeedType(), false);
        }

        LOGGER.info("Created " + lists.size() + " lists for URL "
            + siteUrl + ".");
        return lists;
      }

      /* @Override */
      public SPDocument getSiteData(final WebState webState)
          throws SharepointException {
        // TODO: What do we need to return here?
        return null;
      }
    };
  }

  /* @Override */
  public WebsWS getWebsWS(final SharepointClientContext ctx) {
    return new MockWebsWS(ctx) {
      /* @Override */
      public Set<String> getDirectChildsites() {
        String siteUrl = ctx.getSiteURL();
        String path = getURLPath(siteUrl);
        int numberOfSites;
        if (path.equals(siteWithLotsOfWebs)) {
          numberOfSites = maxSubSites;
        } else if (path.equals(siteWithLotsOfLists)) {
          numberOfSites = avgSubSites;
        } else {
          numberOfSites = minSubSites;
        }
        Set<String> sites = createSites(siteUrl, numberOfSites);
        LOGGER.info("Created " + sites.size() + " sites for URL "
            + siteUrl + ".");
        return sites;
      }

      /* @Override */
      public String getWebTitle(final String webURL, final SPType spType) {
        return getTitleFromURL(webURL);
      }
    };
  }

  /* @Override */
  public SiteDiscoveryWS getSiteDiscoveryWS(
      final SharepointClientContext ctx, String webUrl) {
    return new MockSiteDiscoveryWS(ctx, webUrl) {
      /* @Override */
      public Set<String> getMatchingSiteCollections() {
        String siteUrl = ctx.getSiteURL();
        String path = getURLPath(siteUrl).toLowerCase();
        TreeSet<String> sites = new TreeSet<String>();
        if (path.length() == 0) {
          sites.add(siteUrl + siteWithLotsOfLists);
          sites.add(siteUrl + siteWithLotsOfDocs);
          sites.add(siteUrl + siteWithLotsOfWebs);
        }
        return sites;
      }
    };
  }

  /**
   * Gets just the path of a URL, this excludes the host.
   * For example: http://mock.net/path/to/url would return /path/to/url.
   *
   * @param url the URL to get the path from
   * @return the path or empty string if there is no path in the 
   *      URL (i.e. http://mock.net:80).
   */
  private String getURLPath(final String url) {
    try {
      return new URL(url).getPath();
    } catch (MalformedURLException e) {
      LOGGER.log(Level.WARNING, "Unable to parse URL " + url, e);
      return "";
    }
  }

  /**
   * Generates a document title from the URL. This simply uses the last
   * part of the url as the title.
   * For example: http://mock.net/path/to/sample would return sample.
   *
   * @param url the URL to get the title from
   * @return a valid title string which could be the name of the host 
   *      (i.e. http://mock.net:80).
   */
  private String getTitleFromURL(final String url) {
    String title;
    String path = getURLPath(url);
    int sep = path.lastIndexOf("/");
    if ((-1 < sep) && (sep < (path.length() - 1))) {
      title = path.substring(sep + 1);
    } else {
      title = "";
    }
    return title;
  }

  /**
   * Generates a list of {@link ListState}.
   *
   * @param webUrl the URL of the parent website of the list
   * @param count the number of {@link ListState} objects to create
   * @param ws the WebState of the parent website of the list
   * @param feedType the feed type for the new ListState objects
   * @param fixedId a flag inidicating whether to use fixed or random ID's
   * @return a valid List of {@link ListState}
   * @throws SharepointException on error
   */
  private List<ListState> createLists(final String webUrl,
      final int count, final WebState ws, final FeedType feedType,
      final Boolean fixedId) throws SharepointException {
    ArrayList<ListState> lists = new ArrayList<ListState>();
    appendLists(lists, webUrl, count, ws, feedType, fixedId);
    return lists;
  }

  /**
   * Generates a list of {@link ListState} appending them to an existing list.
   *
   * @param lists the existing list to append the new {@link ListState} to
   * @param webUrl the URL of the parent website of the list
   * @param count the number of {@link ListState} objects to create
   * @param ws the WebState of hte parent website of the list
   * @param feedType the feed type for the new ListState objects
   * @param fixedId a flag inidicating whether to use fixed or random ID's
   * @throws SharepointException on error
   */
  private void appendLists(List<ListState> lists,
      final String webUrl, final int count, final WebState ws,
      final FeedType feedType, final Boolean fixedId)
      throws SharepointException {
    for (int i = 0; i < count; i++) {
      String listName = "List-" + (lists.size() + 1 + i);
      lists.add(createListState(webUrl, listName, ws, feedType, fixedId));
    }
  }

  /**
   * Generates a list of {@link SPDocument}.
   *
   * @param webUrl the URL of the parent website of the list
   * @param count the number of {@link SPDocument} objects to create
   * @param feedType the feed type for the new {@link SPDocument} objects
   * @param fixedId a flag inidicating whether to use fixed or random ID's
   * @return a valid List of {@link SPDocument}
   */
  private List<SPDocument> createDocuments(final String webUrl,
        final int count, final FeedType feedType, final Boolean fixedId) {
    ArrayList<SPDocument> docs = new ArrayList<SPDocument>();
    appendDocuments(docs, webUrl, count, feedType, fixedId);
    return docs;
  }

  /**
   * Generates a list of {@link SPDocument} appending them to an existing list.
   *
   * @param docs the existing list to append the new {@link SPDocument} to
   * @param webUrl the URL of the parent website of the list
   * @param count the number of {@link SPDocument} objects to create
   * @param feedType the feed type for the new {@link SPDocument} objects
   * @param fixedId a flag indicating whether to use fixed or random ID's
   */
  private void appendDocuments(List<SPDocument> docs,
        final String webUrl, final int count, final FeedType feedType,
        final Boolean fixedId) {
    for (int i = 0; i < count; i++) {
      String docName = "Doc-" + (docs.size() + 1 + i);
      docs.add(createDocument(webUrl, docName, feedType, fixedId));
    }
  }

  /**
   * Generates a list of site URLs.
   *
   * @param webUrl the URL of the parent website of the new sites
   * @param count the number of URLs to create
   * @return a valid Set of URLs
   */
  private Set<String> createSites(final String webUrl, final int count) {
    TreeSet<String> sites = new TreeSet<String>();
    for (int i = 0; i < count; i++) {
      String siteName = "Site-" + (i + 1);
      String siteUrl = webUrl + "/" + siteName;
      sites.add(siteUrl);
    }
    return sites;
  }
}
