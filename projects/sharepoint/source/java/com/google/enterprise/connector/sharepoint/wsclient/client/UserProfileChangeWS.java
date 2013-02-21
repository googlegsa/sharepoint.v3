package com.google.enterprise.connector.sharepoint.wsclient.client;

import java.util.List;

import com.google.enterprise.connector.sharepoint.social.SharePointSocialCheckpoint;

public interface UserProfileChangeWS {
  /**
   * Gets the list of User profiles deleted as per Change Token available in
   * input checkpoint
   * @param checkpoint Checkpoint with change token
   * @return List of User account names for which user profiles are deleted
   */
  public List<String> getDeletedUserProfiles(
      SharePointSocialCheckpoint checkpoint);
  
  /**
   * Gets latest change token from SharePoint for User Profile Changes 
   * @return latest chnage token for user profiles
   * @throws Exception
   */
  public String getCurrentChangeToken() throws Exception;

}
