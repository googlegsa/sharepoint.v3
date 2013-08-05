package com.google.enterprise.connector.sharepoint.social;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfileChangeWS;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SharePointUserProfileClient {  
  private final Logger LOGGER = Logger.getLogger(
      SharePointUserProfileClient.class.getName());
  private SharepointSocialClientContext ctxt;
  
  public SharePointUserProfileClient(SharepointSocialClientContext inContext) {
    ctxt = inContext;
  }
  
  public List<SharePointSocialUserProfileDocument> getUpdatedDocuments(
      SharePointSocialCheckpoint checkpoint) {
    List<SharePointSocialUserProfileDocument> updatedDocuments =
        new ArrayList<SharePointSocialUserProfileDocument>();
    SharepointClientContext spContext =  ctxt.getSpClientContext();
    UserProfileChangeWS userProfileChangeWS =
        spContext.getClientFactory().getUserProfileChangeWS(spContext);
    if (userProfileChangeWS != null) {
      try {
        Map<String, ActionType> updatedProfiles = 
            userProfileChangeWS.getChangedUserProfiles(checkpoint);
        if (updatedProfiles != null && updatedProfiles.size() > 0) {
          LOGGER.info("Number of Changed User Profiles = "
              + updatedProfiles.size());
          for(String updatedUserProfile : updatedProfiles.keySet()) {
            ActionType action = updatedProfiles.get(updatedUserProfile);       
            LOGGER.info("Processing Updated User Profile for = "
                + updatedUserProfile);
            SharePointSocialUserProfileDocument doc =
                new SharePointSocialUserProfileDocument(ctxt
                    .getUserProfileCollection());
            doc.setUserKey(updatedUserProfile);
            doc.setProperty(
                SpiConstants.PROPNAME_ACTION, action.toString());
            doc.setActionType(action);
            if (action == ActionType.DELETE) {
              String url = makeItemUrl(updatedUserProfile);
              LOGGER.info("Deleted User Profile URL = "
                  + url);
              doc.setUserKey(updatedUserProfile);
              doc.setProperty(SpiConstants.PROPNAME_CONTENTURL, url);
              doc.setProperty(
                  SpiConstants.PROPNAME_ACTION, action.toString());
              doc.setProperty(SpiConstants.PROPNAME_CONTENT, "");
              doc.setProperty(SpiConstants.PROPNAME_MIMETYPE, "text/plain");
              doc.setProperty(SpiConstants.PROPNAME_DISPLAYURL, url);
            }
            updatedDocuments.add(doc);            
          }
        }
        return updatedDocuments;
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, e.getMessage(), e);        
        return null;
      }
    }
    return null;
  }
  
  public String getCurrentChangeTokenOnSharePoint() {
    SharepointClientContext spContext =  ctxt.getSpClientContext();
    UserProfileChangeWS userProfileChangeWS =
        spContext.getClientFactory().getUserProfileChangeWS(spContext);
    if (userProfileChangeWS != null) {
      try {
        // If no updates then last change token and 
        // current change token is same
        return userProfileChangeWS.getCurrentChangeToken();
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, e.getMessage(), e);
        // In case of error. Return null to ensure connector attempts
        // to get updates with existing Change Token
        return null;
      }     
    }
    return null;
  }
  
  private String makeItemUrl(String key) {
    String myProfileUrl = ctxt.getConfig().getMyProfileBaseURL();
    if (myProfileUrl == null) {
      myProfileUrl = ctxt.getSiteUrl() + "/Person.aspx";
    }
    return myProfileUrl + "?accountname=" + key;
  }

}
