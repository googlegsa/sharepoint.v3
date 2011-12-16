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

package com.google.enterprise.connector.sharepoint.wsclient.soap;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.GetUserProfileByIndexResult;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyData;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.QuickLinkData;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.UserProfileService;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.UserProfileServiceLocator;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.UserProfileServiceSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.ValueData;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfile2007WS;

import org.apache.axis.AxisFault;

import java.text.Collator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

/**
 * Java Client for calling UserProfile.asmx for SharePoint2007 Provides a layer
 * to talk to the UserProfile Web Service on the SharePoint server 2007 Any call
 * to this Web Service must go through this layer.
 *
 * @author nitendra_thakur
 */
public class SPUserProfileWS implements UserProfile2007WS {
  private final Logger LOGGER = Logger.getLogger(SPUserProfileWS.class.getName());
  private SharepointClientContext sharepointClientContext;
  private UserProfileServiceSoap_BindingStub stub;
  private final String personalSpaceTag = "PersonalSpace";
  private String endpoint;

  /**
   * @param inSharepointClientContext The Context is passed so that necessary
   *          information can be used to create the instance of current class
   *          Web Service endpoint is set to the default SharePoint URL stored
   *          in SharePointClientContext.
   * @throws SharepointException
   */

  public SPUserProfileWS(final SharepointClientContext inSharepointClientContext)
      throws SharepointException {
    if (inSharepointClientContext != null) {
      sharepointClientContext = inSharepointClientContext;
      endpoint = Util.encodeURL(sharepointClientContext.getSiteURL())
          + SPConstants.USERPROFILEENDPOINT;
      LOGGER.log(Level.CONFIG, "Endpoint set to: " + endpoint);

      try {
        final UserProfileServiceLocator loc = new UserProfileServiceLocator();
        loc.setUserProfileServiceSoapEndpointAddress(endpoint);

        final UserProfileService service = loc;
        try {
          stub = (UserProfileServiceSoap_BindingStub) service.getUserProfileServiceSoap();
        } catch (final ServiceException e) {
          LOGGER.log(Level.WARNING, e.getMessage(), e);
          throw new SharepointException("Unable to create the userprofile stub");
        }

        final String strDomain = inSharepointClientContext.getDomain();
        String strUserName = inSharepointClientContext.getUsername();
        final String strPassword = inSharepointClientContext.getPassword();

        strUserName = Util.getUserNameWithDomain(strUserName, strDomain);
        stub.setUsername(strUserName);
        stub.setPassword(strPassword);
        // The web service time-out value
        stub.setTimeout(sharepointClientContext.getWebServiceTimeOut());
        LOGGER.fine("Set time-out of : "
            + sharepointClientContext.getWebServiceTimeOut() + " milliseconds");
      } catch (final Exception e) {
        LOGGER.log(Level.WARNING, "Problem while creating the stub for UserProfile WS", e);
      }
    }
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
    try {
      stub.getUserProfileByIndex(0);
      LOGGER.config("SPS site. Using endpoint " + endpoint);
      return true;
    } catch (final AxisFault fault) {
      if ((SPConstants.UNAUTHORIZED.indexOf(fault.getFaultString()) != -1)
          && (sharepointClientContext.getDomain() != null)) {
        final String username = Util.switchUserNameFormat(stub.getUsername());
        LOGGER.log(Level.CONFIG, "Web Service call failed for username [ "
            + stub.getUsername() + " ]. Trying with " + username);
        stub.setUsername(username);
        try {
          stub.getUserProfileByIndex(0);
          LOGGER.config("SPS site. Using endpoint " + endpoint);
          return true;
        } catch (final Exception e) {
          LOGGER.log(Level.WARNING, "Unable to call getUserProfileByIndex(0). endpoint [ "
              + endpoint + " ].", e);
          return false;
        }
      } else {
        LOGGER.config("WSS site. Using endpoint " + endpoint);
        return false;
      }
    } catch (final Exception e) {
      LOGGER.warning(e.toString());
      return false;
    }

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
      GetUserProfileByIndexResult result = null;
      try {
        result = stub.getUserProfileByIndex(index);
      } catch (final AxisFault fault) {
        if ((SPConstants.UNAUTHORIZED.indexOf(fault.getFaultString()) != -1)
            && (sharepointClientContext.getDomain() != null)) {
          final String username = Util.switchUserNameFormat(stub.getUsername());
          LOGGER.log(Level.CONFIG, "Web Service call failed for username [ "
              + stub.getUsername() + " ]. Trying with " + username);
          stub.setUsername(username);
          try {
            result = stub.getUserProfileByIndex(index);
          } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Unable to get Personal sites as call to getUserProfileByIndex("
                + index + ") has failed. endpoint [ " + endpoint + " ].", e);
          }
        } else {
          LOGGER.log(Level.WARNING, "Unable to get Personal sites as call to getUserProfileByIndex("
              + index + ") has failed. endpoint [ " + endpoint + " ].", fault);
        }
      } catch (final Exception e) {
        LOGGER.log(Level.WARNING, "Unable to get Personal sites as call to getUserProfileByIndex("
            + index + ") has failed. endpoint [ " + endpoint + " ].", e);
      }

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
          + " Personal sites to crawl. Using endpoint " + endpoint);
    } else {
      LOGGER.config("No Personal sites to crawl. Using endpoint " + endpoint);
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

      GetUserProfileByIndexResult result = null;
      try {
        result = stub.getUserProfileByIndex(index);
      } catch (final AxisFault fault) {
        if ((SPConstants.UNAUTHORIZED.indexOf(fault.getFaultString()) != -1)
            && (sharepointClientContext.getDomain() != null)) {
          final String username = Util.switchUserNameFormat(stub.getUsername());
          LOGGER.log(Level.CONFIG, "Web Service call failed for username [ "
              + stub.getUsername() + " ]. Trying with " + username);
          stub.setUsername(username);
          try {
            result = stub.getUserProfileByIndex(index);
          } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Unable to call getUserProfileByIndex("
                + index + "). endpoint [ " + endpoint + " ].", e);
          }
        } else {
          LOGGER.log(Level.WARNING, "Unable to call getUserProfileByIndex("
              + index + "). endpoint [ " + endpoint + " ].", fault);
        }
      } catch (final Exception e) {
        LOGGER.log(Level.WARNING, "Unable to call getUserProfileByIndex("
            + index + "). endpoint [ " + endpoint + " ].", e);
      }

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
      LOGGER.info(myLinksSet.size() + " MyLinks returned using endpoint "
          + endpoint);
    } else {
      LOGGER.config("No MyLinks found using endpoint " + endpoint);
    }
    return myLinksSet;
  }
}
