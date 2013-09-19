// Copyright 2013 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.wsclient.mock;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.generated.userprofilechangeservice.UserProfileChangeDataContainer;
import com.google.enterprise.connector.sharepoint.generated.userprofilechangeservice.UserProfileChangeQuery;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfileChangeWS;

import java.util.List;
import java.util.logging.Logger;

public class MockUserProfileChangeWS implements UserProfileChangeWS {
  private static final Logger LOGGER = Logger.getLogger(
      MockUserProfileChangeWS.class.getName());
  private final SharepointClientContext sharepointClientContext;
  private String username;
  private String password;

  public MockUserProfileChangeWS(SharepointClientContext ctx) {
    sharepointClientContext = ctx;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public void setUsername(final String username) {
    this.username = username;
  }

  @Override
  public void setPassword(final String password) {
    this.password = password;
  }

  @Override
  public void setTimeout(final int timeout) {
  }

  @Override
  public UserProfileChangeDataContainer getChanges(String changeToken,
      UserProfileChangeQuery changeQuery) {
    return null;
  }

  @Override
  public String getCurrentChangeToken() {
    return null;
  }

  @Override
  public void setFormsAuthenticationCookie(List<String> cookie) {   
  }
}
