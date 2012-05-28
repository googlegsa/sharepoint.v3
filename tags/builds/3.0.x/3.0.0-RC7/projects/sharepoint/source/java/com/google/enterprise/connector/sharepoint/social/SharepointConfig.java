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

import java.util.List;

/**
 * This class is under-used at this time. This is where I expect to dump all
 * configurations related to sharepoint objects, like what to pull etc. Right
 * now there is a propertiestoIndex config which can provide a list of
 * sharepoint properties to pull and index. However, not exposing that yet and
 * pulling everything in. We are currently indexing only those properties which
 * are public. Once we have ACL support this class will start looking
 * non-trivial. Well, worst case this can stay as a dummy or get removed in
 * future if we realize there is not much interesting that can be variable.
 * 
 * @author tapasnay
 */

public class SharepointConfig {
  private List<String> propertiesToIndex;
  private String spPropertyUserKey;
  private String spPropertyUserContent;
  private String mySiteBaseURL;
  private String myProfileBaseURL;
  boolean secureSocialSearch;

  private boolean initialized = false;

  public void init() {
    if (initialized)
      return;

    secureSocialSearch = false;
    propertiesToIndex = null; // placeholder for future configurability of
                              // properties to index. currently we index all
    spPropertyUserKey = SharepointSocialConstants.SHAREPOINT_USERKEYDEFAULT;
    spPropertyUserContent = SharepointSocialConstants.SHAREPOINT_USERCONTENTDEFAULT;
    initialized = true;
  }

  public boolean isSecureSearch() {
    return secureSocialSearch;
  }

  public List<String> getPropertiesToIndex() {
    return propertiesToIndex;
  }

  public void setMySiteBaseURL(String value) {
    this.mySiteBaseURL = value;
  }

  public String getMySiteBaseURL() {
    return this.mySiteBaseURL;
  }

  public void setMyProfileBaseURL(String value) {
    this.myProfileBaseURL = value;
  }

  public String getMyProfileBaseURL() {
    return this.myProfileBaseURL;
  }

  public Object getSpPropertyUserKey() {
    return this.spPropertyUserKey;
  }

  public Object getSpPropertyUserContent() {
    return this.spPropertyUserContent;
  }

}
