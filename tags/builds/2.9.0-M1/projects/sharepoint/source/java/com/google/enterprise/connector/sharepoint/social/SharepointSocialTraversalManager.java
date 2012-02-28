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
    UserProfileCheckpoint profileCheckpoint = new UserProfileCheckpoint(
        checkpoint);
    if ((!profileCheckpoint.isNoCheckpoint())
        && (profileCheckpoint.getOffset() >= profileCheckpoint.getProfileCount())) {
      return null; // end of traverse
    }
    SharepointSocialUserProfileDocumentList docList = new SharepointSocialUserProfileDocumentList(
        cxn, profileCheckpoint);
    LOGGER
        .info("SharepointSocialDocumentList for UserProfiles created and returned");
    return docList;
  }

}
