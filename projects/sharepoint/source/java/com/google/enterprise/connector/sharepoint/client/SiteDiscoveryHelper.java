// Copyright 2007 Google Inc.
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

package com.google.enterprise.connector.sharepoint.client;

import com.google.enterprise.connector.sharepoint.generated.gssitediscovery.ListCrawlInfo;
import com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.sharepoint.wsclient.client.BaseWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDiscoveryWS;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Java Client for calling GSSiteDiscovery.asmx. Provides a layer to talk to the
 * GSSiteDiscovery Web Service deployed on the SharePoint server Any call to
 * this Web Service must go through this layer.
 *
 * @author nitendra_thakur
 */
public class SiteDiscoveryHelper {
  private static final Logger LOGGER =
      Logger.getLogger(SiteDiscoveryHelper.class.getName());
  private SharepointClientContext sharepointClientContext;
  private SiteDiscoveryWS siteDiscoveryWS;

  /**
   * @param inSharepointClientContext The Context is passed so that necessary
   *          information can be used to create the instance of current class
   *          Web Service endpoint is set to the default SharePoint URL stored
   *          in SharePointClientContext.
   * @throws SharepointException
   */
  public SiteDiscoveryHelper(
      final SharepointClientContext inSharepointClientContext, String siteUrl)
      throws SharepointException {
    if (null == inSharepointClientContext) {
      throw new SharepointException("SharePointClient context cannot be null.");
    }
    sharepointClientContext = inSharepointClientContext;
    if (null == siteUrl) {
      siteUrl = sharepointClientContext.getSiteURL();
    }
    siteDiscoveryWS = sharepointClientContext.getClientFactory()
        .getSiteDiscoveryWS(sharepointClientContext, siteUrl);

    final String strDomain = sharepointClientContext.getDomain();
    String strUser = sharepointClientContext.getUsername();
    final String strPassword = sharepointClientContext.getPassword();
    final int timeout = sharepointClientContext.getWebServiceTimeOut();
    LOGGER.fine("Setting time-out to " + timeout + " milliseconds.");

    strUser = Util.getUserNameWithDomain(strUser, strDomain);
    siteDiscoveryWS.setUsername(strUser);
    siteDiscoveryWS.setPassword(strPassword);
    siteDiscoveryWS.setTimeout(timeout);
  }

  /**
   * Gets all the site collections from all the web applications for a given
   * sharepoint installation
   *
   * @return the set of all site colelltions returned by the GSSiteDiscovery
   */
  public Set<String> getMatchingSiteCollections() {
    Object[] res = Util.makeWSRequest(sharepointClientContext, siteDiscoveryWS,
        new Util.RequestExecutor<Object[]>() {
          public Object[] onRequest(final BaseWS ws) throws Throwable {
            return ((SiteDiscoveryWS) ws).getAllSiteCollectionFromAllWebApps();
          }
          
          public void onError(final Throwable e) {
            LOGGER.log(Level.WARNING,
                "Call to GSSiteDiscovery.getAllSiteCollectionFromAllWebApps() "
                + "failed.", e);
          }
        });

    final Set<String> siteCollections = new TreeSet<String>();
    if (null != res) {
      for (Object element : res) {
        String url = (String) element;
        URL u = null;
        try {
          u = new URL(url);
        } catch (final MalformedURLException e1) {
          LOGGER.log(Level.WARNING, "Malformed site collection URL found [ "
              + url + " ]", e1);
          continue;
        }

        int iPort = u.getPort();
        if (iPort == -1) {
          iPort = u.getDefaultPort();
        }

        url = u.getProtocol() + SPConstants.URL_SEP + getFQDNHost(u.getHost())
            + SPConstants.COLON + iPort;

        final String path = u.getPath();
        if ((path == null) || path.equalsIgnoreCase("")) {
          url += SPConstants.SLASH;
        } else {
          url += path;
        }

        if (sharepointClientContext.isIncludedUrl(url)) {
          siteCollections.add(url);
        } else {
          LOGGER.warning("excluding " + url);
        }
      }
    }
    LOGGER.config("GSSiteDiscovery discovered following Site Collection URLs: "
        + siteCollections);
    return siteCollections;
  }

  /**
   * @param hostName
   * @return the the host in FQDN format
   */
  public String getFQDNHost(final String hostName) {
    if (sharepointClientContext.isFQDNConversion()) {
      InetAddress ia = null;
      try {
        ia = InetAddress.getByName(hostName);
      } catch (final UnknownHostException e) {
        LOGGER.log(Level.WARNING, "Host cannot be identified [ " + hostName
            + " ]", e);
      }
      if (ia != null) {
        return ia.getCanonicalHostName();
      }
    }
    return hostName;
  }

  /**
   * Retrieves the information about crawl behavior of the web whose URL was
   * used to construct the endpoint
   *
   * @return WebCrawlInfo of the web whose URL was used to construct the
   *         endpoint
   */
  public WebCrawlInfo getCurrentWebCrawlInfo() {
    return Util.makeWSRequest(sharepointClientContext, siteDiscoveryWS,
        new Util.RequestExecutor<WebCrawlInfo>() {
          public WebCrawlInfo onRequest(final BaseWS ws) throws Throwable {
            return ((SiteDiscoveryWS) ws).getWebCrawlInfo();
          }
          
          public void onError(final Throwable e) {
            LOGGER.log(Level.WARNING,
                "Call to GSSiteDiscovery.getWebCrawlInfo() failed.", e);
          }
        });
  }

  /**
   * Retrieves the information about crawl behavior of a list of webs
   * corresponding to the passed in web urls
   *
   * @param weburls All web URLs whose crawl info is to be found
   */
  public WebCrawlInfo[] getWebCrawlInfoInBatch(final String[] weburls) {
    if (null == weburls || weburls.length == 0) {
      return null;
    }
    LOGGER.config("Fetching SharePoint indexing options for " + weburls.length
        + " web urls");
        
    return Util.makeWSRequest(sharepointClientContext, siteDiscoveryWS,
        new Util.RequestExecutor<WebCrawlInfo[]>() {
          public WebCrawlInfo[] onRequest(final BaseWS ws) throws Throwable {
            return ((SiteDiscoveryWS) ws).getWebCrawlInfoInBatch(weburls);
          }
          
          public void onError(final Throwable e) {
            LOGGER.log(Level.WARNING,
                "Call to GSSiteDiscovery.getWebCrawlInfoInBatch() failed.", e);
          }
        });
  }

  /**
   * Retrieves and update the information about crawl behavior of a set of webs
   *
   * @param webs
   */
  public void updateWebCrawlInfoInBatch(Set<WebState> webs) {
    if (null == webs || webs.size() == 0) {
      return;
    }
    final Map<String, List<WebState>> webappToWeburlMap = arrangeWebUrlPerWebApp(webs);
    LOGGER.log(Level.CONFIG, webappToWeburlMap.size()
        + " WS call(s) will be made to get WebCrawlInfo of " + webs.size()
        + " sites");

    for (Entry<String, List<WebState>> entry : webappToWeburlMap.entrySet()) {
      SiteDiscoveryHelper sitews = null;
      try {
        sitews = new SiteDiscoveryHelper(sharepointClientContext, entry.getKey());
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Failed to initialize stub for "
            + entry.getKey(), e);
        continue;
      }
      Map<String, WebState> webUrlMap = new HashMap<String, WebState>();
      String[] weburls = new String[webs.size()];
      int i = 0;
      for (WebState web : entry.getValue()) {
        weburls[i++] = web.getWebUrl();
        webUrlMap.put(web.getWebUrl(), web);
      }
      WebCrawlInfo[] webCrawlInfos = sitews.getWebCrawlInfoInBatch(weburls);
      if (null == webCrawlInfos) {
        return;
      }
      for (WebCrawlInfo webCrawlInfo : webCrawlInfos) {
        if (webCrawlInfo.isStatus()) {
          WebState webState = webUrlMap.get(webCrawlInfo.getWebKey());
          webState.setWebCrawlInfo(webCrawlInfo);
        } else {
          LOGGER.log(Level.WARNING, "WS encountered problem while fetching the crawl info of one of the web. WS ERROR -> "
              + webCrawlInfo.getError());
        }
      }
    }
  }

  /**
   * Retrieves the information about crawl behavior of a the lists and set it
   * into the passed in {@link ListState}
   *
   * @param listCollection ListStates to be be updated
   */
  public void updateListCrawlInfo(Collection<ListState> listCollection) {
    if (null == listCollection) {
      return;
    }
    Map<String, ListState> listCrawlInfoMap = new HashMap<String, ListState>();
    final String[] listGuids = new String[listCollection.size()];
    int i = 0;
    for (ListState listState : listCollection) {
      listGuids[i++] = listState.getPrimaryKey();
      listCrawlInfoMap.put(listState.getPrimaryKey(), listState);
    }

    final ListCrawlInfo[] listCrawlInfo = 
        Util.makeWSRequest(sharepointClientContext, siteDiscoveryWS,
            new Util.RequestExecutor<ListCrawlInfo[]>() {
          public ListCrawlInfo[] onRequest(final BaseWS ws) throws Throwable {
            return ((SiteDiscoveryWS) ws).getListCrawlInfo(listGuids);
          }
      
          public void onError(final Throwable e) {
            LOGGER.log(Level.WARNING,
                "Call to GSSiteDiscovery.GetListCrawlInfo() failed.", e);
          }
        });

    if (null == listCrawlInfo) {
      return;
    }

    for (ListCrawlInfo info : listCrawlInfo) {
      ListState listState = listCrawlInfoMap.get(info.getListGuid());
      if (null == listState) {
        LOGGER.log(Level.SEVERE, "One of the List GUID [ " + info.getListGuid()
            + " ] can not be found in the parentWebState. ");
        continue;
      }
      if (!info.isStatus()) {
        LOGGER.log(Level.WARNING, "GSSiteDiscovery has encountered the "
            + "following error while getting the crawl info for list URL [ "
            + listState.getListURL() + " ], GUID [ "
            + listState.getPrimaryKey() + " ]. "
            + "WS error [ " + info.getError() + " ].");
        continue;
      }
      listState.setNoCrawl(info.isNoCrawl());
    }
  }

  /**
   * Arranges URLs of all the webs according to their web application.
   *
   * @param webs
   * @return A map where the web application is mapped to the hosted webstates
   */
  private Map<String, List<WebState>> arrangeWebUrlPerWebApp(
      final Set<WebState> webs) {
    final Map<String, List<WebState>> webappToWeburlMap = new HashMap<String, List<WebState>>();
    for (WebState web : webs) {
      // We need to arrange all the URLs according to their web
      // application because the WS call for batch processing of URLs can
      // be made per web application only. If a url do not belongs to the
      // web app that was used while setting the endpoint, ws call will
      // fail
      final String webApp = Util.getWebApp(web.getWebUrl());
      List<WebState> lstWebs = webappToWeburlMap.get(webApp);
      if (null == lstWebs) {
        lstWebs = new ArrayList<WebState>();
        webappToWeburlMap.put(webApp, lstWebs);
      }
      lstWebs.add(web);
    }
    return webappToWeburlMap;
  }
}
