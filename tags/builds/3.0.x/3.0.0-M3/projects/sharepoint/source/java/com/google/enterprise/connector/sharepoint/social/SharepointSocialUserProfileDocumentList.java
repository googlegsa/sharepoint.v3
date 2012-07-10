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
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;

import java.rmi.RemoteException;
import java.util.logging.Logger;

/**
 * Document list is implemented for user profile documents for SharePoint. The
 * checkpoint implementation on this document list simply keeps track of the
 * offset for the next document and the count of the documents. On resume we
 * fetch from here. However, it is possible that profiles have been
 * added/deleted at source in the mean time. Further, SharePoint does not give
 * any explicit guarantee that the next time things will be in the same order.
 * Hence there is a theoretical possibility that by checkpoint/resume we may
 * miss a few profiles once a while. In order to reduce that possibility, on
 * resume, when we validate the checkpoint, we check if profile count has
 * remained same or not. If the count differs then we assume that is the new
 * good value and pretend we had been fetching from the new list. Another
 * approach would be to actually fetch all of them in one shot and save then in
 * a local store and then serve from there. That would be expensive and even
 * that is not fool-proof strategy since for large number of user-profiles
 * connection can break in the middle. For the current strategy a work-around
 * would be to restart the feed and increase checkpoint interval. We will
 * consider the other strategy later, based on observation on how things go.
 * Also assuming, missing a few experts once a while, and rarely, is not
 * critical.
 * 
 * @author tapasnay
 */

public class SharepointSocialUserProfileDocumentList implements DocumentList {

  private int offset;
  private int profileCount;
  final static Logger LOGGER = SharepointSocialConnector.LOGGER;
  public static final String CHECKPOINT_PREFIX = "sp_userprofile";
  private final SharepointUserProfileConnection service;

  public static class UserProfileCheckpoint {
    private int offset;
    private int profileCount;
    private boolean none; // no checkpoint

    public UserProfileCheckpoint() {
      this.none = true;
      this.offset = 0;
      this.profileCount = 0;
    }

    public UserProfileCheckpoint(int offset, int count) {
      this.offset = offset;
      this.profileCount = count;
      this.none = false;
    }

    public UserProfileCheckpoint(String checkpoint) {
      if (Strings.isNullOrEmpty(checkpoint)) {
        this.none = true;
      } else {
        String[] parts = checkpoint.split(",");
        if ((parts.length != 3) || (!parts[0].equals(CHECKPOINT_PREFIX))) {
          // no or invalid checkpoint
          this.none = true;
        } else {
          offset = Integer.parseInt(parts[1]);
          profileCount = Integer.parseInt(parts[2]);
        }
      }
    }

    public int getOffset() {
      return offset;
    }

    public int getProfileCount() {
      return profileCount;
    }

    public boolean isNoCheckpoint() {
      return none;
    }

  }

  public SharepointSocialUserProfileDocumentList(
      SharepointUserProfileConnection connection,
      UserProfileCheckpoint checkpoint) throws RepositoryException {
    service = connection;
    try {
      profileCount = connection.openConnection();
    } catch (RemoteException e) {
      throw new RepositoryException(e);
    }
    if (checkpoint.none || (checkpoint.profileCount != profileCount)) {
      offset = 0;
    } else {
      offset = checkpoint.offset;
    }
  }

  /**
   * Create an empty document list.
   */
  public SharepointSocialUserProfileDocumentList() {
    offset = 0;
    profileCount = 0;
    service = null;
  }

  @Override
  public String checkpoint() throws RepositoryException {
    return CHECKPOINT_PREFIX + "," + Integer.toString(offset) + ","
        + Integer.toString(profileCount);
  }

  @Override
  public Document nextDocument() throws RepositoryException {
    if (offset >= profileCount) {
      return null;
    }
    LOGGER.fine("Returning userprofile at index = " + offset + " of size= "
        + profileCount);
    Document doc;
    try {
      doc = service.getProfile(offset);
    } catch (RemoteException e) {
      LOGGER.warning("There was a failure fetching profile at index = "
          + offset + " retrying...");
      // There was a failure getting the next profile. It is possible that the
      // service connection has been stale and invalid. We will retry once by
      // reopening the connection.
      int newCount;
      try {
        // if openConnection throws service is out of bounds, we release current
        // batch and wait for resume
        newCount = service.openConnection();
      } catch (RemoteException eAgain) {
        LOGGER
            .severe("User profile service not reachable anymore, continuing with "
                + "partial list, to be resumed");
        return null;
      }
      if (newCount != profileCount) {
        LOGGER
            .warning("Count of user profiles changed. Continuing, but the data may be "
                + "stale");
        profileCount = newCount;
        if (offset >= profileCount) {
          offset = profileCount; // set the pointer at the end, so that
                                 // checkpoint is consistent
          return null;
        }
      }
      try {
        doc = service.getProfile(offset);
      } catch (RemoteException ex) {
        // if this throws again, possibly service is out of bounds
        // close the current batch, can be resumed later
        LOGGER
            .severe("User profile service not reachable anymore, continuing with "
                + "partial list, to be resumed");
        return null;
      }
    }
    offset++; // we got a valid item to return, advance the counter
    return doc;
  }
}