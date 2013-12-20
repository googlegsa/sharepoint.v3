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

import com.google.common.base.Strings;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.webs.GetWebCollectionResponseGetWebCollectionResult;
import com.google.enterprise.connector.sharepoint.generated.webs.GetWebResponseGetWebResult;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.client.BaseWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.WebsWS;

import org.apache.axis.message.MessageElement;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Java Client for calling Webs.asmx Provides a layer to talk to the Webs Web
 * Service on the SharePoint server Any call to this Web Service must go through
 * this layer.
 *
 * @author nitendra_thakur
 */
public class WebsHelper {
  private static final Logger LOGGER =
      Logger.getLogger(WebsHelper.class.getName());
  private SharepointClientContext sharepointClientContext = null;
  private WebsWS websWS;

  /**
   * @param inSharepointClientContext The Context is passed so that necessary
   *          information can be used to create the instance of current class
   *          Web Service endpoint is set to the default SharePoint URL stored
   *          in SharePointClientContext.
   * @throws SharepointException
   */
  public WebsHelper(final SharepointClientContext inSharepointClientContext)
      throws SharepointException {
    if (null == inSharepointClientContext) {
      throw new SharepointException("SharePointClient context cannot be null.");
    }
    sharepointClientContext = inSharepointClientContext;
    websWS = sharepointClientContext.getClientFactory().getWebsWS(
        sharepointClientContext);

    final String strDomain = sharepointClientContext.getDomain();
    String strUser = sharepointClientContext.getUsername();
    final String strPassword = sharepointClientContext.getPassword();
    final int timeout = sharepointClientContext.getWebServiceTimeOut();
    LOGGER.fine("Setting time-out to " + timeout + " milliseconds.");

    strUser = Util.getUserNameWithDomain(strUser, strDomain);
    websWS.setUsername(strUser);
    websWS.setPassword(strPassword);
    websWS.setTimeout(timeout);
  }

  /**
   * Discovers all the sites from the current site collection which are in
   * hierarchy lower to the current web.
   *
   * @return The set of child sites
   */
  public Set<String> getDirectChildsites() {
    final Set<String> allWebsList = new TreeSet<String>();
    
    final GetWebCollectionResponseGetWebCollectionResult webcollnResult =
        Util.makeWSRequest(sharepointClientContext, websWS,
            new Util.RequestExecutor<
                GetWebCollectionResponseGetWebCollectionResult>() {
          public GetWebCollectionResponseGetWebCollectionResult 
              onRequest(final BaseWS ws) throws Throwable {
            return ((WebsWS) ws).getWebCollection();
          }
          
          public void onError(final Throwable e) {
            LOGGER.log(Level.WARNING,
                "Unable to get the child webs for the web site.", e);
          }
        });

    if (webcollnResult != null) {
      final MessageElement[] meWebs = webcollnResult.get_any();
      if ((meWebs != null) && (meWebs[0] != null)) {
        Iterator<?> itWebs = meWebs[0].getChildElements();
        if (itWebs != null) {
          while (itWebs.hasNext()) {
            // e.g. <ns1:Web Title="ECSCDemo"
            // Url="http://ps4312.persistent.co.in:2905/ECSCDemo"
            // xmlns:ns1="http://schemas.microsoft.com/sharepoint/soap/"/>
            final MessageElement meWeb = (MessageElement) itWebs.next();
            if (null == meWeb) {
              continue;
            }
            final String url = meWeb.getAttribute("Url");
            if (sharepointClientContext.isIncludedUrl(url)) {
              allWebsList.add(url);
            } else {
              LOGGER.warning("excluding " + url);
            }
          }
        }
      }
    }

    return allWebsList;
  }

  /**
   * To get the Web URL from any Page URL of the web
   *
   * @param pageURL
   * @return the well formed Web URL to be used for WS calls
   */
  public String getWebURLFromPageURL(final String pageURL) {
    LOGGER.config("Page URL: " + pageURL);

    String strWebURL =
        Util.makeWSRequest(sharepointClientContext, websWS,
            new Util.RequestExecutor<String>() {
          public String onRequest(final BaseWS ws) throws Throwable {
            return ((WebsWS) ws).webUrlFromPageUrl(pageURL);
          }
          
          public void onError(final Throwable e) {
            LOGGER.log(Level.WARNING,
                "Unable to get the sharepoint web URL for the URL.", e);
          }
        });

    if (Strings.isNullOrEmpty(strWebURL)) {
      strWebURL = Util.getWebURLForWSCall(pageURL);
    }
    LOGGER.config("WebURL: " + strWebURL);
    return strWebURL;
  }

  /**
   * To get the Web Title of a given web
   *
   * @param webURL To identiy the web whose Title is to be discovered
   * @param spType The SharePOint type for this web
   * @return the web title
   */
  public String getWebTitle(final String webURL, final SPType spType) {
    String webTitle = "No Title";

    LOGGER.config("Getting title for Web: " + webURL
        + " SharepointConnectorType: " + spType);

    if (SPType.SP2003 == spType) {
      try {
        final SiteDataHelper siteData =
            new SiteDataHelper(sharepointClientContext);
        webTitle = siteData.getTitle();
      } catch (final Exception e) {
        LOGGER.log(Level.WARNING, "Unable to Get Title for web [ " + webURL
            + " ]. Using the default web Title. " + e);
      }
    } else {
      final GetWebResponseGetWebResult resWeb =
          Util.makeWSRequest(sharepointClientContext, websWS,
              new Util.RequestExecutor<GetWebResponseGetWebResult>() {
            public GetWebResponseGetWebResult onRequest(final BaseWS ws)
                throws Throwable {
              return ((WebsWS) ws).getWeb(webURL);
            }
            
            public void onError(final Throwable e) {
              LOGGER.log(Level.WARNING, "Unable to get title for web. "
                  + "The request to getWeb failed.", e);
            }
          });

      if (null != resWeb) {
        final MessageElement[] meArray = resWeb.get_any();
        if ((meArray != null) && (meArray.length > 0)
            && (meArray[0] != null)) {
          webTitle = meArray[0].getAttribute(SPConstants.WEB_TITLE);
        }
      }
    }
    LOGGER.fine("Title: " + webTitle);
    return webTitle;
  }

  /**
   * For checking the Web Service connectivity
   *
   * @return the Web Service connectivity status
   */
  public String checkConnectivity() {
    final StringBuffer connectivityResponse = new StringBuffer();

    Util.makeWSRequestVoid(sharepointClientContext, websWS,
        new Util.RequestExecutorVoid() {
      public void onRequest(final BaseWS ws) throws Throwable {
        ((WebsWS) ws).getWebCollection();
        connectivityResponse.append(SPConstants.CONNECTIVITY_SUCCESS);
      }
      
      public void onError(final Throwable e) {
        LOGGER.log(Level.WARNING, "Unable to connect.", e);
        connectivityResponse.append(e.getLocalizedMessage());
      }
    });

    return connectivityResponse.toString();
  }
}
