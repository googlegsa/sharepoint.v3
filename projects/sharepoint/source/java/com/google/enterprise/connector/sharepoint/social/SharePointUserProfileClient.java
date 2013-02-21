package com.google.enterprise.connector.sharepoint.social;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfileChangeWS;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SharePointUserProfileClient {  
  private final Logger LOGGER = Logger.getLogger(
      SharePointUserProfileClient.class.getName());
  private SharepointSocialClientContext ctxt;
  
  public SharePointUserProfileClient(SharepointSocialClientContext inContext) {
    ctxt = inContext;
  }
  
  public List<Document> getUpdatedDocuments(
      SharePointSocialCheckpoint checkpoint) {
    List<Document> updatedDocuments = new ArrayList<Document>();
     SharepointClientContext spContext =  ctxt.getSpClientContext();
     UserProfileChangeWS userProfileChangeWS =
         spContext.getClientFactory().getUserProfileChangeWS(spContext);
     if (userProfileChangeWS != null) {
       try {
         List<String> deletedUsers = 
             userProfileChangeWS.getDeletedUserProfiles(checkpoint);
         if (deletedUsers != null && deletedUsers.size() > 0) {
           LOGGER.info("Total Deleted User Profiles = "
               + deletedUsers.size());
           for(String deletedUserProfile : deletedUsers) {
             LOGGER.info("Processing Deleted User Profile for = "
                 + deletedUserProfile);
             SharePointSocialUserProfileDocument doc =
                 new SharePointSocialUserProfileDocument(ctxt
                     .getUserProfileCollection());
             String url = makeItemUrl(deletedUserProfile);
             LOGGER.info("Deleted User Profile URL = "
                 + url);
             doc.setUserKey(deletedUserProfile);
             doc.setProperty(SpiConstants.PROPNAME_CONTENTURL, url);
             doc.setProperty(
                 SpiConstants.PROPNAME_ACTION, ActionType.DELETE.toString());
             doc.setProperty(SpiConstants.PROPNAME_CONTENT, "");
             doc.setProperty(SpiConstants.PROPNAME_MIMETYPE, "text/plain");
             doc.setProperty(SpiConstants.PROPNAME_DISPLAYURL, url);
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
