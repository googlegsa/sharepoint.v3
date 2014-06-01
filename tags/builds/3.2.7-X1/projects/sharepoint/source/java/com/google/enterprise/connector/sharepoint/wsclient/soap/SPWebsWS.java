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
import com.google.enterprise.connector.sharepoint.generated.webs.GetWebCollectionResponseGetWebCollectionResult;
import com.google.enterprise.connector.sharepoint.generated.webs.GetWebResponseGetWebResult;
import com.google.enterprise.connector.sharepoint.generated.webs.Webs;
import com.google.enterprise.connector.sharepoint.generated.webs.WebsLocator;
import com.google.enterprise.connector.sharepoint.generated.webs.WebsSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.client.WebsWS;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

/**
 * Java Client for calling Webs.asmx Provides a layer to talk to the Webs Web
 * Service on the SharePoint server Any call to this Web Service must go through
 * this layer.
 *
 * @author nitendra_thakur
 */
public class SPWebsWS implements WebsWS {
  private static final Logger LOGGER = 
      Logger.getLogger(SPWebsWS.class.getName());
  private WebsSoap_BindingStub stub = null;

  /**
   * @param inSharepointClientContext The Context is passed so that necessary
   *          information can be used to create the instance of current class
   *          Web Service endpoint is set to the default SharePoint URL stored
   *          in SharePointClientContext.
   * @throws SharepointException
   */
  public SPWebsWS(final SharepointClientContext inSharepointClientContext)
      throws SharepointException {
    String endpoint = Util.encodeURL(inSharepointClientContext.getSiteURL())
        + SPConstants.WEBSENDPOINT;
    LOGGER.log(Level.CONFIG, "Endpoint set to: " + endpoint);
    final WebsLocator loc = new WebsLocator();
    loc.setWebsSoapEndpointAddress(endpoint);
    final Webs service = loc;

    try {
      stub = (WebsSoap_BindingStub) service.getWebsSoap();
    } catch (final ServiceException e) {
      LOGGER.log(Level.WARNING, e.getMessage(), e);
      throw new SharepointException("Unable to create webs stub");
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
   * Returns the titles and urls of all sites directly beneath the 
   * current site.
   *
   * @return a GetWebCollectionResponseGetWebCollectionResult
   */
  public GetWebCollectionResponseGetWebCollectionResult getWebCollection()
      throws RemoteException {
    return stub.getWebCollection();
  }
  
  /**
   * To get the web URL from any page URL of the web
   *
   * @param pageUrl
   * @return the well formed web URL to be used for WS calls
   * @throws RemoteException
   */
  public String webUrlFromPageUrl(String pageUrl) throws RemoteException {
    return stub.webUrlFromPageUrl(pageUrl);
  }

  /**
   * Returns properties of a site (for example, name, description, and theme).
   *
   * @param webURL The Sharepoint web URL to get the properties of
   * @return a GetWebResponseGetWebResult
   * @throws RemoteException
   */
  public GetWebResponseGetWebResult getWeb(final String webURL)
      throws RemoteException {
    return stub.getWeb(webURL);
  }
}
