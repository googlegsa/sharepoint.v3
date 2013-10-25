// Copyright 2013 Google Inc.
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
import com.google.enterprise.connector.sharepoint.generated.userprofilechangeservice.UserProfileChangeData;
import com.google.enterprise.connector.sharepoint.generated.userprofilechangeservice.UserProfileChangeDataContainer;
import com.google.enterprise.connector.sharepoint.generated.userprofilechangeservice.UserProfileChangeQuery;
import com.google.enterprise.connector.sharepoint.social.SharePointSocialCheckpoint;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.client.BaseWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfileChangeWS;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;

public class UserProfileChangeHelper {
  private final Logger LOGGER = Logger.getLogger(
      UserProfileChangeHelper.class.getName());
  private SharepointClientContext sharepointClientContext;
  private UserProfileChangeWS changeWS;

  public Map<String, ActionType> getChangedUserProfiles(
      SharePointSocialCheckpoint checkpoint) {
    Map<String, ActionType> updatedProfiles =
        new HashMap<String, ActionType>();
    final UserProfileChangeQuery changeQuery = new UserProfileChangeQuery();
    changeQuery.setDelete(true);
    changeQuery.setUserProfile(true);
    changeQuery.setUpdate(true);
    changeQuery.setUpdateMetadata(true);
    changeQuery.setSingleValueProperty(true);
    changeQuery.setMultiValueProperty(true);
    changeQuery.setColleague(true);

    final String changeToken = checkpoint.getUserProfileChangeToken();
    final UserProfileChangeDataContainer changeContainer = 
        Util.makeWSRequest(sharepointClientContext, changeWS,
            new Util.RequestExecutor<UserProfileChangeDataContainer>() {
          public UserProfileChangeDataContainer onRequest(final BaseWS ws)
              throws Throwable {
            return ((UserProfileChangeWS) ws).getChanges(changeToken,
                changeQuery);
          }

          public void onError(final Throwable e) {
            LOGGER.log(Level.WARNING, "Call to getChanges failed.", e);
          }
        });

    if (changeContainer != null) {
      UserProfileChangeData[] changes = changeContainer.getChanges();
      if (changes != null && changes.length > 0) {
        for(UserProfileChangeData change : changes) {
          String userAccountName = change.getUserAccountName();
          if (Strings.isNullOrEmpty(userAccountName)) {
            continue;
          }
          for(String changeType : change.getChangeType()) {
            LOGGER.log(Level.INFO,
                "User Profile Change Type Recevied = "+ changeType);
            if (SPConstants.DELETE.equalsIgnoreCase(changeType)) {
              LOGGER.log(Level.INFO,"User Profile Deleted for user = "
                  + change.getUserAccountName());
              updatedProfiles.put(userAccountName, ActionType.DELETE);
            } else {
              updatedProfiles.put(userAccountName, ActionType.ADD);
            }
          }
        }
      }
      LOGGER.log(Level.INFO, "User Profile Change Token Recevied = "
          + changeContainer.getChangeToken());
      if (changeContainer.isHasExceededCountLimit()) {
        checkpoint.setUserProfileChangeToken(
            changeContainer.getChangeToken());
      } else {
        try {
          checkpoint.setUserProfileChangeToken(getCurrentChangeToken());
        } catch (final Exception e) {
          LOGGER.log(Level.WARNING,
              "Unable to update checkpoint with user profile change token.",
              e);
          return null;
        }
      }
    }
    return updatedProfiles;
  }

  public UserProfileChangeHelper(SharepointClientContext 
      inSharepointClientContext) throws SharepointException {
    if (null == inSharepointClientContext) {
      throw new SharepointException(
          "SharePointClient context cannot be null.");
    }
    sharepointClientContext = inSharepointClientContext;
    changeWS = sharepointClientContext.getClientFactory()
        .getUserProfileChangeWS(sharepointClientContext);

    final String strDomain = sharepointClientContext.getDomain();
    String strUser = sharepointClientContext.getUsername();
    final String strPassword = sharepointClientContext.getPassword();
    final int timeout = sharepointClientContext.getWebServiceTimeOut();
    LOGGER.fine("Setting time-out to " + timeout + " milliseconds.");

    strUser = Util.getUserNameWithDomain(strUser, strDomain);
    changeWS.setUsername(strUser);
    changeWS.setPassword(strPassword);
    changeWS.setTimeout(timeout);
  }

  public String getCurrentChangeToken() throws Exception {
    return Util.makeWSRequest(sharepointClientContext, changeWS,
        new Util.RequestExecutor<String>() {
      public String onRequest(final BaseWS ws) throws Throwable {
        return ((UserProfileChangeWS) ws).getCurrentChangeToken();
      }
      
      public void onError(final Throwable e) {
        LOGGER.log(Level.WARNING, "Call to getCurrentChangeToken failed.", e);
      }
    });
  }
}
