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

import com.google.enterprise.connector.sharepoint.generated.userprofileservice.GetUserProfileByIndexResult;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyData;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.QuickLinkData;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.ValueData;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.client.BaseWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfile2007WS;

import java.text.Collator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Java Client for calling UserProfile.asmx for SharePoint2007 Provides a layer
 * to talk to the UserProfile Web Service on the SharePoint server 2007 Any call
 * to this Web Service must go through this layer.
 *
 * @author nitendra_thakur
 */
public class UserProfile2007Helper {
  private final Logger LOGGER =
      Logger.getLogger(UserProfile2007Helper.class.getName());
  private SharepointClientContext sharepointClientContext;
  private final String personalSpaceTag = "PersonalSpace";
  private UserProfile2007WS userProfileWS;

  /**
   * @param inSharepointClientContext The Context is passed so that necessary
   *          information can be used to create the instance of current class
   *          Web Service endpoint is set to the default SharePoint URL stored
   *          in SharePointClientContext.
   * @throws SharepointException
   */

  public UserProfile2007Helper(final SharepointClientContext 
      inSharepointClientContext) throws SharepointException {
    if (null == inSharepointClientContext) {
      throw new SharepointException("SharePointClient context cannot be null.");
    }

    sharepointClientContext = inSharepointClientContext;
    userProfileWS = sharepointClientContext.getClientFactory()
        .getUserProfile2007WS(sharepointClientContext);

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
   * Checks to see if the current web to which the web service end point is set
   * is an SPS site.
   *
   * @return true, if the end point being used is an SPS site. Else value is
   *         false.
   * @throws SharepointException
   */
  public boolean isSPS() throws SharepointException {
    final boolean[] isSPS = new boolean[1];
    Util.makeWSRequest(sharepointClientContext, userProfileWS,
        new Util.RequestExecutor<GetUserProfileByIndexResult>() {
      public GetUserProfileByIndexResult onRequest(final BaseWS ws)
          throws Throwable {
        GetUserProfileByIndexResult result =
            ((UserProfile2007WS) ws).getUserProfileByIndex(0);
        isSPS[0] = true;
        return result;
      }
      
      public void onError(final Throwable e) {
        LOGGER.log(Level.WARNING, "Call to getUserProfileByIndex failed.", e);
        isSPS[0] = false;
      }
    });
    return isSPS[0];
  }

  /**
   * To get all the personal sites from the current web.
   *
   * @return the list of personal sites
   * @throws SharepointException
   */
  public Set<String> getPersonalSiteList() throws SharepointException {
    final Set<String> lstAllPersonalSites = new TreeSet<String>();
    final Collator collator = Util.getCollator();

    // Method 1: High Level Steps:
    // ============================
    // 1. Call method GetUserProfileCount-> returns the no: of profiles
    // registered
    // 2. Iterate through the profiles and get the corresponding URL
    //
    // Method 2: High Level Steps (optimal as save extra WS call to get the
    // user profile count)
    // =======================================================================================
    // Start with Index =0 i.e. Get the first user profile
    //
    // [Loop]
    // 1. Get the user profile using getUserProfileByIndex API for index
    // 2. The above call should give you value of next index (to be used for
    // getting next profile and so on]
    // [End Loop]

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

      String space = null;
      for (PropertyData element : data) {
        try {
          final String name = element.getName();
          if (collator.equals(personalSpaceTag, name)) {
            final ValueData[] vd = element.getValues();
            if ((vd == null) || (vd.length < 1)) {
              continue;
            }
            space = (String) vd[0].getValue();
            String strMySiteBaseURL = sharepointClientContext.getMySiteBaseURL();
            if (strMySiteBaseURL.endsWith(SPConstants.SLASH)) {
              strMySiteBaseURL = strMySiteBaseURL.substring(0, strMySiteBaseURL.lastIndexOf(SPConstants.SLASH));
            }
            String strURL = strMySiteBaseURL + space;
            if (strURL.endsWith(SPConstants.SLASH)) {
              strURL = strURL.substring(0, strURL.lastIndexOf(SPConstants.SLASH));
            }
            if (sharepointClientContext.isIncludedUrl(strURL)) {
              lstAllPersonalSites.add(strURL);
              LOGGER.log(Level.CONFIG, "Personal Site: " + strURL);
            } else {
              LOGGER.log(Level.WARNING, "excluding " + strURL);
            }
          }
        } catch (final Exception e) {
          LOGGER.log(Level.WARNING, e.getMessage(), e);
          continue;
        }
      }
      final String next = result.getNextValue();
      index = Integer.parseInt(next);
    }

    if (lstAllPersonalSites.size() > 0) {
      LOGGER.info("Discovered " + lstAllPersonalSites.size()
          + " Personal sites to crawl.");
    } else {
      LOGGER.config("No Personal sites to crawl.");
    }
    return lstAllPersonalSites;
  }

  /**
   * To get all the My Sites from the specified MySite BAse URL on configuration
   * page.
   *
   * @return the list of MySites
   * @throws SharepointException
   */
  public Set<String> getMyLinks() throws SharepointException {
    final Set<String> myLinksSet = new TreeSet<String>();
    int index = 0;
    while (index >= 0) {
      final GetUserProfileByIndexResult result = getUserProfileByIndex(index);

      if ((result == null) || (result.getUserProfile() == null)) {
        break;
      }

      final QuickLinkData[] links = result.getQuickLinks();

      if (links == null) {
        break;
      }
      String url = null;

      for (QuickLinkData element : links) {
        url = element.getUrl();
        if (sharepointClientContext.isIncludedUrl(url)) {
          myLinksSet.add(url);
        } else {
          LOGGER.warning("excluding " + url.toString());
        }
      }

      final String next = result.getNextValue();
      index = Integer.parseInt(next);
    }

    if (myLinksSet.size() > 0) {
      LOGGER.info(myLinksSet.size() + " MyLinks found.");
    } else {
      LOGGER.info("No MyLinks found.");
    }
    return myLinksSet;
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
        return ((UserProfile2007WS) ws).getUserProfileByIndex(index);
      }
      
      public void onError(final Throwable e) {
        LOGGER.log(Level.WARNING, "Call to getUserProfileByIndex failed.", e);
      }
    });
  }
}
