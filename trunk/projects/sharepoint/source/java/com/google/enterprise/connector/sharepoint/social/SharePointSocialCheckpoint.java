package com.google.enterprise.connector.sharepoint.social;

import com.google.common.base.Strings;
import com.google.enterprise.connector.spi.RepositoryException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.ParseException;
import org.json.JSONException;
import org.json.JSONObject;

public class SharePointSocialCheckpoint {
  private static final Logger LOGGER =
      Logger.getLogger(SharePointSocialCheckpoint.class.getName());

  private static final String USER_PROFILE_NEXT_INDEX = "userProfileNextIndex";
  private static final String USER_PROFILE_LAST_FULL_SYNC =
      "userProfileLastFullSync";
  private static final String USER_PROFILE_CHANGE_TOKEN =
      "userProfileChangeToken";
  
  private int userProfileNextIndex = -1;
  private long userProfileLastFullSync = 0L;
  private boolean emptyUserProfileCheckpoint = false;
  private String userProfileChangeToken = "";

  public SharePointSocialCheckpoint (String checkpoint) {
    try {
      if (!Strings.isNullOrEmpty(checkpoint)) {
        JSONObject jo = new JSONObject(checkpoint);        
        if (jo.has(USER_PROFILE_NEXT_INDEX)) {
          this.userProfileNextIndex = jo.getInt(USER_PROFILE_NEXT_INDEX);
        } else {
          this.userProfileNextIndex = -1;
          this.emptyUserProfileCheckpoint = true;
        }
        if (jo.has(USER_PROFILE_LAST_FULL_SYNC)) {
          this.userProfileLastFullSync =
              jo.getLong(USER_PROFILE_LAST_FULL_SYNC);
        } else {
          this.userProfileLastFullSync = 0L;
        }
        if (jo.has(USER_PROFILE_CHANGE_TOKEN)) {
          this.userProfileChangeToken =
              jo.getString(USER_PROFILE_CHANGE_TOKEN);
        } else {
          this.userProfileChangeToken = "";
        }
      } else {
        initializeEmptyCheckpoint();
      }
    } catch (IllegalArgumentException e) {
      LOGGER.log(Level.WARNING, "Invalid Social Checkpoint [ " + checkpoint
          + "]. Reinitializing Social Checkpoint" ,e);
      initializeEmptyCheckpoint();
    } catch (JSONException e) {
      LOGGER.log(Level.WARNING, "Invalid Social Checkpoint [ " + checkpoint
          + "]. Reinitializing Social Checkpoint" ,e);
      initializeEmptyCheckpoint();
    }
  }

  public int getUserProfileNextIndex() {
    return userProfileNextIndex;
  }

  public void setUserProfileNextIndex(int userProfileNextIndex) {
    this.userProfileNextIndex = userProfileNextIndex;
  }

  public long getUserProfileLastFullSync() {
    return userProfileLastFullSync;
  }

  public void setUserProfileLastFullSync(long userProfileLastFullSync) {
    this.userProfileLastFullSync = userProfileLastFullSync;
  }

  public String getUserProfileChangeToken() {
    return userProfileChangeToken;
  }

  public void setUserProfileChangeToken(String userProfileChangeToken) {
    this.userProfileChangeToken = userProfileChangeToken;
  }
  
  private void initializeEmptyCheckpoint()
  {
    this.userProfileNextIndex = -1;
    this.emptyUserProfileCheckpoint = true;
    this.userProfileLastFullSync = 0L;
    this.userProfileChangeToken = "";
  }

  /**
   * Gets the checkpoint as a string.
   *
   * @return the checkpoint as a string, or null if a JSON error occurs
   */
  public String toString() {
    try {
      return asString();
    } catch (RepositoryException re) {
      // Already logged in asString.
      return null;
    }
  }

  private String asString() throws RepositoryException {
    try {
      JSONObject jo = new JSONObject();      
        jo.put(USER_PROFILE_NEXT_INDEX, userProfileNextIndex);
        jo.put(USER_PROFILE_LAST_FULL_SYNC, userProfileLastFullSync);  
        jo.put(USER_PROFILE_CHANGE_TOKEN, userProfileChangeToken);   
      return jo.toString();
    } catch (JSONException e) {
      LOGGER.severe("JSON problem creating Checkpoint: " + e.toString());
      throw new RepositoryException("JSON problem creating Checkpoint", e);
    }  
  }

  public boolean isEmptyUserProfileCheckpoint() {
    return emptyUserProfileCheckpoint;
  }

  public void setEmptyUserProfileCheckpoint(
      boolean emptyUserProfileCheckpoint) {
    this.emptyUserProfileCheckpoint = emptyUserProfileCheckpoint;
  }
}
