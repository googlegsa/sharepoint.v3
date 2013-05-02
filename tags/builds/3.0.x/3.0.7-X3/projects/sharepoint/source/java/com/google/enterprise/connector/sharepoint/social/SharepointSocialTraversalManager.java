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

import com.google.common.base.Strings;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfileChangeWS;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.util.SystemClock;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gets all userprofiles in one shot. There is no checkpoint here. In case of
 * failure and resume it is always a recrawl. It should not be a big issue since
 * the dataset is not expected to be huge
 * 
 * @author tapasnay
 */
public class SharepointSocialTraversalManager implements TraversalManager {
  private static final Logger LOGGER = SharepointSocialConnector.LOGGER;
  private SharepointSocialClientContext ctxt;
  private int batchHint = 500;
  private SharePointUserProfileClient userProfileClient;

  public SharepointSocialTraversalManager(SharepointSocialClientContext ctxt) {
    this.ctxt = ctxt;
    userProfileClient = new SharePointUserProfileClient(ctxt);
  }

  @Override
  public DocumentList resumeTraversal(String checkpoint)
      throws RepositoryException {
    String socialCheckpoint = checkpoint;
    if (checkpoint.startsWith(
        SharepointSocialUserProfileDocumentList.CHECKPOINT_PREFIX)) {
      socialCheckpoint = checkpoint.substring(
          SharepointSocialUserProfileDocumentList.CHECKPOINT_PREFIX.length());
    }
    return traverse(socialCheckpoint);
  }

  @Override
  public void setBatchHint(int batchHint) throws RepositoryException {
    this.batchHint = batchHint;
  }

  @Override
  public DocumentList startTraversal() throws RepositoryException {
    return traverse("");
  }

  private DocumentList traverse(String checkpoint) throws RepositoryException {
    LOGGER.info("Starting traversal: SharepointSocial");
    SharepointUserProfileConnection cxn;
    cxn = new SharepointUserProfileConnection(ctxt);
    SharePointSocialCheckpoint profileCheckpoint;
    SharePointSocialCheckpoint existingProfileCheckpoint =
        new SharePointSocialCheckpoint(checkpoint);
    if (Strings.isNullOrEmpty(
        existingProfileCheckpoint.getUserProfileChangeToken())) {
      // When connector is reset or during first crawl
      // get latest change token from SharePoint and
      // update checkpoint for incremental runs.
      existingProfileCheckpoint.setUserProfileChangeToken(
          userProfileClient.getCurrentChangeTokenOnSharePoint());
    }
    int fullTraversalIntervalInDays = ctxt.getFullTraversalIntervalInDays();
    LOGGER.info(
        "fullTraversalIntervalInDays = " + fullTraversalIntervalInDays);
    if (fullTraversalIntervalInDays == 0) {
      LOGGER.info(
          "Connector configured to perform full crawl each cycle");
      profileCheckpoint = new SharePointSocialCheckpoint(null);
      // This is to ensure delete feeds are generated for previous run.
      profileCheckpoint.setUserProfileChangeToken(
          existingProfileCheckpoint.getUserProfileChangeToken());
    } else if (fullTraversalIntervalInDays < 0) {
      LOGGER.info(
          "Connector not configured to perform full crawl automatically.");
      profileCheckpoint = existingProfileCheckpoint;
    } else {

      // TODO: For manual testing this value can me modified to test various
      // scenarios. Other option is to modify connector checkpoint value
      // and restart CM for new value to take effect.
      long fullTraversalInterval =
          fullTraversalIntervalInDays * 24 * 60 * 60 * 1000L;
      long currentTime = new SystemClock().getTimeMillis();      
      if (existingProfileCheckpoint.getUserProfileLastFullSync() > 0L) {
        // Perform this check only if initial crawl is done and
        // LastFullSync value is available.
        long timeElapsed = currentTime - 
            existingProfileCheckpoint.getUserProfileLastFullSync();
        if (timeElapsed
            > fullTraversalInterval) {
          LOGGER.info("Performing full crawl for social connector "
              + "as Full Traversal Interval elapsed.");
          profileCheckpoint = new SharePointSocialCheckpoint(null);
          // This is to ensure delete feeds are generated for previous run.
          profileCheckpoint.setUserProfileChangeToken(
              existingProfileCheckpoint.getUserProfileChangeToken());
        } else {
          LOGGER.info(
              "Performing incremental crawl with available checkpoint.");
          profileCheckpoint = existingProfileCheckpoint;
        }
      } else {
        LOGGER.info(
            "Part of Initial crawl. " 
                + "Performing incremental crawl with available checkpoint.");
        profileCheckpoint = existingProfileCheckpoint;
      }
    }
    boolean needToProcessUpdates =
        needToProcessProfileUpdates(profileCheckpoint);
    List<SharePointSocialUserProfileDocument> updatedDocs = null;
    if (needToProcessUpdates) {
      updatedDocs = userProfileClient.getUpdatedDocuments(profileCheckpoint);    
    }
    if ((updatedDocs == null || updatedDocs.isEmpty()) 
        && checkpointAtEnd(profileCheckpoint, cxn)) {
      return null;
    }

    SharepointSocialUserProfileDocumentList docList =
        new SharepointSocialUserProfileDocumentList(
            cxn, profileCheckpoint, batchHint, updatedDocs);
    LOGGER.info(
        "SharepointSocialDocumentList for UserProfiles created and returned");
    return docList;
  }

  private boolean checkpointAtEnd (
      SharePointSocialCheckpoint profileCheckpoint,
      SharepointUserProfileConnection cxn) throws RepositoryException {
    if (!profileCheckpoint.isEmptyUserProfileCheckpoint()) {
      try {
        cxn.openConnection();
        Document doc = cxn.getProfile(
            profileCheckpoint.getUserProfileNextIndex());
        // doc null is indicator for no more profiles are available to crawl.
        return (doc == null);
      } catch (RemoteException e) {
        throw new RepositoryException(e);
      }
    }
    return false;
  }

  private boolean needToProcessProfileUpdates (
      SharePointSocialCheckpoint profileCheckpoint) {
    String currentChangeToken = profileCheckpoint.getUserProfileChangeToken();
    if (Strings.isNullOrEmpty(currentChangeToken)) {
      return false;
    } else {
      String currentChangeTokenOnSharePoint =
          userProfileClient.getCurrentChangeTokenOnSharePoint();
      LOGGER.info("Current Change Token on SharePoint = ["
          + currentChangeTokenOnSharePoint
          + "Existing Change Token with Connector = ["
          + currentChangeToken);
      if (!Strings.isNullOrEmpty(currentChangeTokenOnSharePoint)) {
        return !currentChangeToken.equalsIgnoreCase(
            currentChangeTokenOnSharePoint);
      } else {
        // In case of error/ missing latest Token,
        // Return true to ensure connector attempts
        // to get updates with existing Change Token
        return true;
      }      
    }
  } 

}

