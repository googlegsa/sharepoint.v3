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

package com.google.enterprise.connector.sharepoint.wsclient.soap;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.gssitediscovery.ListCrawlInfo;
import com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscovery;
import com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoveryLocator;
import com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap_BindingStub;
import com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDiscoveryWS;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

/**
 * Java Client for calling GSSiteDiscovery.asmx. Provides a layer to talk to the
 * GSSiteDiscovery Web Service deployed on the SharePoint server Any call to
 * this Web Service must go through this layer.
 *
 * @author nitendra_thakur
 */
public class GSSiteDiscoveryWS implements SiteDiscoveryWS {
  private static final Logger LOGGER =
      Logger.getLogger(GSSiteDiscoveryWS.class.getName());
  private SiteDiscoverySoap_BindingStub stub = null;

  /**
   * @param inSharepointClientContext The Context is passed so that necessary
   *          information can be used to create the instance of current class
   *          Web Service endpoint is set to the default SharePoint URL stored
   *          in SharePointClientContext.
   * @throws SharepointException
   */
  public GSSiteDiscoveryWS(
      final SharepointClientContext inSharepointClientContext, String siteUrl)
      throws SharepointException {
    final String endpoint = Util.encodeURL(siteUrl)
        + SPConstants.GSPSITEDISCOVERYWS_END_POINT;
    LOGGER.log(Level.CONFIG, "Endpoint set to: " + endpoint);

    final SiteDiscoveryLocator loc = new SiteDiscoveryLocator();
    loc.setSiteDiscoverySoapEndpointAddress(endpoint);
    final SiteDiscovery gspSiteDiscovery = loc;
    try {
      stub = (SiteDiscoverySoap_BindingStub) gspSiteDiscovery.getSiteDiscoverySoap();
    } catch (final ServiceException e) {
      LOGGER.log(Level.WARNING, e.getMessage(), e);
      throw new SharepointException("Unable to get the GSSiteDiscovery stub");
    }
  }

  /**
   * (@inheritDoc)
   */
  public String getUsername() {
    return stub.getUsername();
  }

  /**
   * (@inheritDoc)
   */
  public void setUsername(final String username) {
    stub.setUsername(username);
  }

  /**
   * (@inheritDoc)
   */
  public void setPassword(final String password) {
    stub.setPassword(password);
  }

  /**
   * (@inheritDoc)
   */
  public void setTimeout(final int timeout) {
    stub.setTimeout(timeout);
  }

  /**
   * Returns the top level URL of all site collections form all web 
   * applications for a given sharepoint installation.   
   *
   * @return an Object array where each entry is a URL String
   * @throws RemoteException
   */
  public Object[] getAllSiteCollectionFromAllWebApps() throws RemoteException {
    return stub.getAllSiteCollectionFromAllWebApps();
  }

  /**
   * Returns the crawl info of the current web.
   *
   * @return WebCrawlInfo of the web whose URL was used to construct the
   *         endpoint
   * @return a WebCrawlInfo
   * @throws RemoteException
   */
  public WebCrawlInfo getWebCrawlInfo() throws RemoteException {
    return stub.getWebCrawlInfo();
  }

  /**
   * Retrieves the information about crawl behavior of a list of webs
   * corresponding to the passed in web urls
   *
   * @param weburls All web URLs whose crawl info is to be found
   * @return an WebCrawlInfo array
   * @throws RemoteException
   */
  public WebCrawlInfo[] getWebCrawlInfoInBatch(String[] weburls)
      throws RemoteException {
    return stub.getWebCrawlInfoInBatch(weburls);
  }

  /**
   * Get the lists crawl info for the the current web.
   *
   * @param listGuids An array of list guids
   * @return an ListCrawlInfo array
   * @throws RemoteException
   */
  public ListCrawlInfo[] getListCrawlInfo(String[] listGuids)
      throws RemoteException {
    return stub.getListCrawlInfo(listGuids);
  }
}
