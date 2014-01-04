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

import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.GetUserProfileByIndexResult;
import com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.PropertyData;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.client.BaseWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfile2003WS;

import java.text.Collator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Java Client for calling UserProfile.asmx for SharePoint 2003 Provides a layer
 * to talk to the UserProfile Web Service on the SharePoint server 2003 Any call
 * to this Web Service must go through this layer.
 *
 * @author nitendra_thakur
 */
public class UserProfile2003Helper {
  private static Logger LOGGER =
      Logger.getLogger(UserProfile2003Helper.class.getName());
  private SharepointClientContext sharepointClientContext;
  private UserProfile2003WS userProfileWS;
  private final String personalSpaceTag = "PersonalSpace";

  /**
   * @param inSharepointClientContext The Context is passed so that necessary
   *          information can be used to create the instance of current class
   *          Web Service endpoint is set to the default SharePoint URL stored
   *          in SharePointClientContext.
   * @throws SharepointException
   */
  public UserProfile2003Helper(final SharepointClientContext 
      inSharepointClientContext) throws SharepointException {
    if (null == inSharepointClientContext) {
      throw new SharepointException("SharePointClient context cannot be null.");
    }

    sharepointClientContext = inSharepointClientContext;
    userProfileWS = sharepointClientContext.getClientFactory()
        .getUserProfile2003WS(sharepointClientContext);

    final String strDomain = sharepointClientContext.getDomain();
    String strUser = sharepointClientContext.getUsername();
    final String strPassword = sharepointClientContext.getPassword();
    final int timeout = sharepointClientContext.getWebServiceTimeOut();
    LOGGER.fine("Setting time-out to " + timeout + " milliseconds.");

    strUser = Util.getUserNameWithDomain(strUser, strDomain);
    userProfileWS.setUsername(strUser);
    userProfileWS.setPassword(strPassword);
    userProfileWS.setTimeout(timeout);
  }

  /**
   * Checks to see if the current web to which the web service endpioint is set
   * is an SPS site.
   *
   * @return if the endpoint being used is an SPS site
   * @throws SharepointException
   */
  public boolean isSPS() throws SharepointException {
    final GetUserProfileByIndexResult result = getUserProfileByIndex(0);
    return result != null;
  }

  /**
   * To get all the personal sites from the current web.
   *
   * @return the list of personal sites
   * @throws SharepointException
   */
  public Set<String> getPersonalSiteList() throws SharepointException {
    // list of personal sites and subsites
    final Set<String> personalSitesSet = new TreeSet<String>();
    final Collator collator = Util.getCollator();

    int index = 0;
    while (index >= 0) {
      final GetUserProfileByIndexResult result = getUserProfileByIndex(index);

      if ((result == null) || (result.getUserProfile() == null)) {
        break;
      }

      final PropertyData[] data = result.getUserProfile();
      if (data == null) {
        break;
      }

      for (PropertyData element : data) {
        final String name = element.getName();
        if (collator.equals(personalSpaceTag, name)) {
          final String propVal = element.getValue();// e.g.
          // /personal/administrator/
          if (propVal == null) {
            continue;
          }
          String strURL = Util.getWebApp(sharepointClientContext.getSiteURL())
              + propVal;

          if (strURL.endsWith(SPConstants.SLASH)) {
            strURL = strURL.substring(0, strURL.lastIndexOf(SPConstants.SLASH));
          }
          if (sharepointClientContext.isIncludedUrl(strURL, LOGGER)) {
            personalSitesSet.add(strURL);
            LOGGER.log(Level.CONFIG, "Personal Site: " + strURL);
          }
        }
      }
      final String next = result.getNextValue();
      index = Integer.parseInt(next);
    }
    if (personalSitesSet.size() > 0) {
      LOGGER.info("Discovered " + personalSitesSet.size()
          + " personal sites to crawl.");
    } else {
      LOGGER.info("No personal sites to crawl.");
    }
    return personalSitesSet;
  }

  /**
   * Calls UserProfileWS.getUserProfileByIndex to get a user profile.
   *
   * @param index The index of the user profile to be retrieved
   * @return a GetUserProfileByIndexResult
   */
  private GetUserProfileByIndexResult getUserProfileByIndex(final int index) {
    return Util.makeWSRequest(sharepointClientContext, userProfileWS,
        new Util.RequestExecutor<GetUserProfileByIndexResult>() {
      public GetUserProfileByIndexResult onRequest(final BaseWS ws)
          throws Throwable {
        return ((UserProfile2003WS) ws).getUserProfileByIndex(index);
      }
      
      public void onError(final Throwable e) {
        LOGGER.log(Level.WARNING, "Call to getUserProfileByIndex failed.", e);
      }
    });
  }
}
