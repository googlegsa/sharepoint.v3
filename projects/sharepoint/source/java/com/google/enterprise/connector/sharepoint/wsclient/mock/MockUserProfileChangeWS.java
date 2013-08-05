package com.google.enterprise.connector.sharepoint.wsclient.mock;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.social.SharePointSocialCheckpoint;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfileChangeWS;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

public class MockUserProfileChangeWS implements UserProfileChangeWS {
  private static final Logger LOGGER = Logger.getLogger(
      MockUserProfileChangeWS.class.getName());
  private final SharepointClientContext sharepointClientContext;

  public MockUserProfileChangeWS(SharepointClientContext ctx) {
    sharepointClientContext = ctx;
  }

  @Override
  public Map<String, ActionType> getChangedUserProfiles(
      SharePointSocialCheckpoint checkpoint) { 
    return null;
  }

  @Override
  public String getCurrentChangeToken() {
    return null;
  }
}
