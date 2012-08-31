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

import com.google.enterprise.connector.sharepoint.social.SharepointSocialUserProfileDocumentList.UserProfileCheckpoint;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.util.SystemClock;

import java.rmi.RemoteException;
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

  public SharepointSocialTraversalManager(SharepointSocialClientContext ctxt) {
    this.ctxt = ctxt;
  }

  @Override
  public DocumentList resumeTraversal(String checkpoint)
      throws RepositoryException {

    return traverse(checkpoint);
  }

  @Override
  public void setBatchHint(int batchHint) throws RepositoryException {
  }

  @Override
  public DocumentList startTraversal() throws RepositoryException {
    return traverse("");
  }

  private DocumentList traverse(String checkpoint) throws RepositoryException {
    LOGGER.info("Starting traversal: SharepointSocial");
    SharepointUserProfileConnection cxn;
    cxn = new SharepointUserProfileConnection(ctxt);
    UserProfileCheckpoint profileCheckpoint;
    int fullTraversalIntervalInDays = ctxt.getFullTraversalIntervalInDays();
    LOGGER.info(
        "fullTraversalIntervalInDays = " + fullTraversalIntervalInDays);
    if (fullTraversalIntervalInDays == 0) {
      LOGGER.info(
          "Connector configured to perform full crawl each cycle");
      profileCheckpoint = new UserProfileCheckpoint(null);
    } else if (fullTraversalIntervalInDays < 0) {
      LOGGER.info(
          "Connector not configured to perform full crawl automatically.");
      profileCheckpoint = new UserProfileCheckpoint(checkpoint);
    } else {
      UserProfileCheckpoint existingProfileCheckpoint =
          new UserProfileCheckpoint(checkpoint);
      // TODO: For manual testing this value can me modified to test various
      // scenarios. Other option is to modify connector checkpoint value
      // and restart CM for new value to take effect.
      long fullTraversalInterval =
          fullTraversalIntervalInDays * 24 * 60 * 60 * 1000L;
      long currentTime = new SystemClock().getTimeMillis();      
      if (existingProfileCheckpoint.getLastFullSync() > 0L) {
        // Perform this check only if initial crawl is done and
        // LastFullSync value is available.
        if ((currentTime - existingProfileCheckpoint.getLastFullSync())
            > fullTraversalInterval) {
          LOGGER.info(
              "Performing full crawl for social connector "
                  + "as Full Traversal Interval elapsed.");
          profileCheckpoint = new UserProfileCheckpoint(null);
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
    if (checkpointAtEnd(profileCheckpoint, cxn)) {
      return null;
    }
    SharepointSocialUserProfileDocumentList docList =
        new SharepointSocialUserProfileDocumentList(
            cxn, profileCheckpoint);
    LOGGER.info(
        "SharepointSocialDocumentList for UserProfiles created and returned");
    return docList;
  }
  
  private boolean checkpointAtEnd (UserProfileCheckpoint profileCheckpoint,
      SharepointUserProfileConnection cxn) throws RepositoryException {
    if (!profileCheckpoint.isNoCheckpoint()) {
      int profileCount = 0;
      try {
        profileCount = cxn.openConnection();
      } catch (RemoteException e) {
        throw new RepositoryException(e);
      }
      if (profileCheckpoint.getOffset() >= profileCount) {
        // Since offset is greater than or equal to profile count assumption is
        // connector has discovered all profiles. This may not be true always
        // since deletion of User profiles will result in change in count and
        // connector might miss few profiles (maximum number of 
        // missing new profiles equals to number of deletions).
        // Periodic Full sync will take care of this scenario.
        // TODO: Handle deletes.
        return true;
      } 
    }
    return false;
  }  
}

