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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.enterprise.connector.sharepoint.generated.sp2010.userprofileservice.ContactData;
import com.google.enterprise.connector.sharepoint.generated.sp2010.userprofileservice.GetUserProfileByIndexResult;
import com.google.enterprise.connector.sharepoint.generated.sp2010.userprofileservice.Privacy;
import com.google.enterprise.connector.sharepoint.generated.sp2010.userprofileservice.PropertyData;
import com.google.enterprise.connector.sharepoint.generated.sp2010.userprofileservice.ValueData;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SocialUserProfileDocument;
import com.google.enterprise.connector.spi.SocialUserProfileDocument.ColleagueData;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
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

  private static final SimpleDateFormat utcDateFormatter;

  static {
    utcDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    utcDateFormatter.setTimeZone(TimeZone.getTimeZone("GMT+0"));
  }

  private static synchronized String formatAsUtc(Calendar c) {
    return utcDateFormatter.format(c.getTime());
  }

  private SharepointSocialClientContext ctxt;
  private SharepointConfig config;
  private UserProfileServiceFactory serviceFactory;
  private UserProfileServiceGenerator service;

  public static ArrayList<String> readProfileProperty(
      PropertyData propertyData) {
    ArrayList<String> valuesVector = new ArrayList<String>();
    ValueData[] values = propertyData.getValues();
    for (ValueData valueData : values) {
      try {
        if (valueData.getValue() instanceof Calendar) {
          String dateAsUtc = formatAsUtc((Calendar) valueData.getValue());
          LOGGER.finest("UTC Date value from user profile: " + dateAsUtc);
          valuesVector.add(dateAsUtc);
        } else {
          valuesVector.add(valueData.getValue().toString());
        }
      } catch (Exception e) {
        LOGGER.warning("Error converting fieldData for property "
            + propertyData.getName() + " of type = "
            + valueData.getClass().getCanonicalName());
      }
    }
    return valuesVector;
  }

  public SharepointUserProfileConnection(SharepointSocialClientContext ctxt) {
    this.ctxt = ctxt;
    this.config = ctxt.getConfig();
    if (ctxt.getUserProfileServiceFactory() == null) {
      this.serviceFactory = new SharepointUserProfileServiceFactory();
    } else {
      this.serviceFactory = ctxt.getUserProfileServiceFactory();
    }
  }

  @VisibleForTesting
  PropertyData[] getUserProfileByName(String accountName)
      throws RemoteException {
    return service.getUserProfileByName(accountName);
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

  public SocialUserProfileDocument getProfileByName(String accountName)
      throws RepositoryException, RemoteException {
    PropertyData[] props = null;
    try {
      props = getUserProfileByName(accountName);
    } catch (RemoteException ex) {
      LOGGER.log(Level.WARNING,
          "Error Geting User Profile for " + accountName, ex);
      // User profile service throws exception if profile
      // with account name is not available.
      // SharePoint 2010 : could not be found
      // MOSS 2007 : User Not Found:
      if (ex.getMessage() == null ||
          (!ex.getMessage().contains("could not be found") &&
              !ex.getMessage().contains("User Not Found:"))) {
        throw new RepositoryException(ex);
      } else {
        LOGGER.log(Level.INFO, "User profile not available for " + accountName);
      }
    }
    if (props == null) {
      return null;
    }
    ContactData[] userColleagues = null;
    try {
      userColleagues = service.getUserColleagues(accountName);
    } catch (RemoteException ex) {
      LOGGER.log(Level.WARNING,
          "Error Geting Colleagues for " + accountName, ex);
      // Connector will ignore this error and proceed.
    }

    SharePointSocialUserProfileDocument userProfile =
        new SharePointSocialUserProfileDocument(ctxt
            .getUserProfileCollection());

    //This is a place holder property for ACL
    userProfile.setProperty(SpiConstants.PROPNAME_ACLGROUPS, "Secured");
    String globalNamespace =
        ctxt.getSpClientContext().getGoogleGlobalNamespace();
    // Default ACL for User profile document
    userProfile.addAclGroupToDocument(
        globalNamespace, "NT AUTHORITY\\Authenticated Users");
    populateProfileProperties(userProfile, props, userColleagues);
    return userProfile;
  }

  private void populateProfileProperties(
      SharePointSocialUserProfileDocument userProfile, PropertyData[] props,
      ContactData[] userColleagues)  throws RepositoryException {
    List<String> propertiesToIndex = config.getPropertiesToIndex();
    boolean secure = config.isSecureSearch();
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

            ArrayList<String> valuesVector = readProfileProperty(propertyData);
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
      userProfile.setProperty(SpiConstants.PROPNAME_CONTENT, "");
      userProfile.setProperty(SpiConstants.PROPNAME_MIMETYPE, "text/plain");
      userProfile.setProperty(SpiConstants.PROPNAME_DISPLAYURL, url);
    }
  }

  private SocialUserProfileDocument buildSPUserProfile(
      GetUserProfileByIndexResult getUserProfileByIndexResult,
      UserProfileServiceGenerator svc) throws RepositoryException {

    PropertyData[] props = getUserProfileByIndexResult.getUserProfile();
    SharePointSocialUserProfileDocument userProfile =
        new SharePointSocialUserProfileDocument(ctxt
        .getUserProfileCollection());

    //This is a place holder property for ACL
    userProfile.setProperty(SpiConstants.PROPNAME_ACLGROUPS, "Secured");
    String globalNamespace =
        ctxt.getSpClientContext().getGoogleGlobalNamespace();
    // Default ACL for User profile document
    userProfile.addAclGroupToDocument(
        globalNamespace, "NT AUTHORITY\\Authenticated Users");

    String nextValue = getUserProfileByIndexResult.getNextValue();
    LOGGER.fine("Next User Profile Index : " + nextValue);
    if (!Strings.isNullOrEmpty(nextValue)) {
      userProfile.setNextValue(Integer.parseInt(nextValue));
    }
    if (props == null) {
      // returning null as properties are empty.
      // This is an indication that no more profiles are available
      // at and beyond index value.
      return null;
    }
    populateProfileProperties(userProfile, props,
        getUserProfileByIndexResult.getColleagues());
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
