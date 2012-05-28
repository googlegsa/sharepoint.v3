// Copyright 2012 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.social;

import com.google.enterprise.connector.sharepoint.generated.sp2010.userprofileservice.ContactData;
import com.google.enterprise.connector.sharepoint.generated.sp2010.userprofileservice.GetUserProfileByIndexResult;
import com.google.enterprise.connector.sharepoint.generated.sp2010.userprofileservice.Privacy;
import com.google.enterprise.connector.sharepoint.generated.sp2010.userprofileservice.PropertyData;
import com.google.enterprise.connector.sharepoint.generated.sp2010.userprofileservice.ValueData;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SocialUserProfileDocument;
import com.google.enterprise.connector.spi.SocialUserProfileDocument.ColleagueData;
import com.google.enterprise.connector.spi.SpiConstants;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

/**
 * Gateway to the user profile service proxy for SharePoint This gateway uses a
 * factory to create the actual proxy to talk to The factory pattern helps us
 * test it with mock factory.
 * 
 * @author tapasnay
 */
public class SharepointUserProfileConnection {

  private static final Logger LOGGER = SharepointSocialConnector.LOGGER;
  private SharepointSocialClientContext ctxt;
  private SharepointConfig config;
  private UserProfileServiceFactory serviceFactory;
  private UserProfileServiceGenerator service;

  public SharepointUserProfileConnection(SharepointSocialClientContext ctxt) {
    this.ctxt = ctxt;
    this.config = ctxt.getConfig();
    if (ctxt.getUserProfileServiceFactory() == null) {
      this.serviceFactory = new SharepointUserProfileServiceFactory();
    } else {
      this.serviceFactory = ctxt.getUserProfileServiceFactory();
    }
  }

  public int openConnection() throws RepositoryException, RemoteException {

    try {
      LOGGER.fine("in openConnection call");

      URL wsdlURI = new URL(ctxt.getSiteUrl()
          + SharepointSocialConstants.SHAREPOINT_USERPROFILESERVICE);
      LOGGER.fine("Going to new UserProfileServiceSoapStub");
      service = serviceFactory.createService(wsdlURI);
      if (service == null) {
        throw new RepositoryException(
            "FATAL: Internal Error: Invalid user profile generator");
      }
      LOGGER.fine("Created soap stub instance");
      service.setUsername(SharepointUtils.makeDomainUsername(ctxt.getDomain(),
          ctxt.getUserName()));
      service.setPassword(ctxt.getPassword());

      LOGGER.fine("Calling soap service");
      long numProfiles = service.getUserProfileCount();
      LOGGER.info("Number of SharePoint User Profiles: " + numProfiles);
      return (int) numProfiles;
    } catch (MalformedURLException e) {
      throw new RepositoryException(e);
    }

  }

  /**
   * Gets a specific profile by index. While getting if we get an exception from
   * the service connection, we retry once by reopening the connection since the
   * connection could have been stale due to delay over fetching many profiles.
   * However, if it still does not work then we throw the exception up.
   * 
   * @throws RepositoryException
   * @throws RemoteException
   */
  public SocialUserProfileDocument getProfile(int index)
      throws RepositoryException, RemoteException {
    return buildSPUserProfile(service.getUserProfileByIndex(index), service);
  }

  private SocialUserProfileDocument buildSPUserProfile(
      GetUserProfileByIndexResult getUserProfileByIndexResult,
      UserProfileServiceGenerator svc) throws RepositoryException {

    List<String> propertiesToIndex = config.getPropertiesToIndex();
    boolean secure = config.isSecureSearch();

    PropertyData[] props = getUserProfileByIndexResult.getUserProfile();
    SocialUserProfileDocument userProfile = new SocialUserProfileDocument(ctxt
        .getUserProfileCollection());
    userProfile.setPublic(true);

    for (PropertyData propertyData : props) {

      if (propertyData.getName().equals(config.getSpPropertyUserKey())) {
        String userKey = (String) propertyData.getValues()[0].getValue();
        LOGGER.fine("User Key Property: " + propertyData.getName());
        LOGGER.fine("User Key Value: " + userKey);
        userProfile.setUserKey(userKey);
      } else if (propertyData.getName().equals(
          config.getSpPropertyUserContent())
          && !(propertyData.getValues().length == 0)) {
        String userContent = (String) propertyData.getValues()[0].getValue();
        LOGGER.fine("User Content Property: " + propertyData.getName());
        LOGGER.fine("User Content Value: " + userContent);
        userProfile.setUserContent(userContent);
      } else if (isAll(propertiesToIndex)
          || propertiesToIndex.contains(propertyData.getName())) {
        if (!secure) {
          if (propertyData.getPrivacy().equals(Privacy.Public)) {
            LOGGER.fine("Including Property: " + propertyData.getName());

            ValueData[] values = propertyData.getValues();

            ArrayList<String> valuesVector = new ArrayList<String>();
            for (ValueData valueData : values) {
              try {
                if (valueData.getValue() instanceof GregorianCalendar) {
                  GregorianCalendar dt = (GregorianCalendar) valueData
                      .getValue();
                  valuesVector.add(String.format("$1tm:$1td:$1tY", dt));
                }
                valuesVector.add(valueData.getValue().toString());
              } catch (Exception e) {
                LOGGER.warning("Error converting fieldData for property "
                    + propertyData.getName() + " of type = "
                    + valueData.getClass().getCanonicalName());
              }
            }
            String propName = normalizePropertyName(propertyData.getName());

            if (valuesVector.size() > 0) {
              if (propName
                  .equals(SharepointSocialConstants.PROPNAME_RESPONSIBILITY)) {
                userProfile.setAskmeAbout(valuesVector);
              } else if (propName
                  .equals(SharepointSocialConstants.PROPNAME_PASTPROJECTS)) {
                userProfile.setPastProjects(valuesVector);
              } else if (propName
                  .equals(SharepointSocialConstants.PROPNAME_SKILL)) {
                userProfile.setSkills(valuesVector);
              }
              // preserve the original properties even if they may have been
              // mapped above
              userProfile.setProperty(propName, valuesVector);
            }
          } else {
            LOGGER.fine("ignoring private property");
          }
        }
      } else {
        LOGGER.fine("Ignoring Property: " + propertyData.getName());
      }
    }
    ContactData[] userColleagues = null;
    try {
      if (userProfile.getUserKey() != null) {
        userColleagues = svc.getUserColleagues(userProfile.getUserKey()
            .toString());
      }
    } catch (RemoteException ex) {
      throw new RepositoryException(ex);
    }
    if (userColleagues != null) {
      ArrayList<ColleagueData> socialColleagues = new ArrayList<ColleagueData>();
      for (ContactData contact : userColleagues) {
        if ((!secure) && (contact.getPrivacy() == Privacy.Public)) {
          ColleagueData oneColleague = new ColleagueData();
          oneColleague.setIdentity(contact.getAccountName());
          oneColleague.setEmail(contact.getEmail());
          oneColleague.setGroup(contact.getGroup());
          oneColleague.setInWorkGroup(contact.isIsInWorkGroup());
          oneColleague.setName(contact.getName());
          oneColleague.setTitle(contact.getTitle());
          oneColleague.setUrl(contact.getUrl());
          socialColleagues.add(oneColleague);
        }
      }
      userProfile.setColleagues(socialColleagues);
      String url;
      try {
        if (userProfile.getUserKey() != null) {
          url = makeItemUrl(ctxt, URLEncoder.encode(userProfile.getUserKey()
              .toString(), "UTF-8"));
        } else {
          url = makeItemUrl(ctxt, ""); // profile is still good for searching
        }
      } catch (UnsupportedEncodingException e) {
        throw new AssertionError();
      }
      userProfile.setProperty(SpiConstants.PROPNAME_CONTENTURL, url);
      userProfile.setProperty(SpiConstants.PROPNAME_CONTENT, "");
      userProfile.setProperty(SpiConstants.PROPNAME_MIMETYPE, "text/plain");
      userProfile.setProperty(SpiConstants.PROPNAME_DISPLAYURL, url);
    }
    LOGGER.fine("Processed user: " + userProfile.getUserKey());
    return userProfile;

  }

  /**
   * Normalize propertynames so that they become queryable in GSA. Replacing '-'
   * with '_'. If there are other quirky restrictions we need to add them here.
   */
  private String normalizePropertyName(String name) {
    return name.replace('-', '_');
  }

  private boolean isAll(List<String> propertiesToIndex) {
    return ((propertiesToIndex == null) || (propertiesToIndex.size() == 0) || 
        propertiesToIndex.get(0).equals("*"));
  }

  private String makeItemUrl(SharepointSocialClientContext ctxt, String key) {
    String myProfileUrl = ctxt.getConfig().getMyProfileBaseURL();
    if (myProfileUrl == null) {
      myProfileUrl = ctxt.getSiteUrl() + "/Person.aspx";
    }
    return myProfileUrl + "?accountname=" + key;
  }
}
