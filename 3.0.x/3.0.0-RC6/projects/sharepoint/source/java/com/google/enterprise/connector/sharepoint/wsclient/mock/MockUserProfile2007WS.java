// Copyright 2011 Google Inc.
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
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfile2007WS;

import java.util.logging.Logger;
import java.util.Set;

public class MockUserProfile2007WS implements UserProfile2007WS {
  private static final Logger LOGGER = Logger.getLogger(MockUserProfile2007WS.class.getName());
  private final SharepointClientContext sharepointClientContext;

  /**
   * @param ctx The Sharepoint context is passed so that necessary
   *    information can be used to create the instance of current class
   *    web service endpoint is set to the default SharePoint URL stored
   *    in SharePointClientContext.
   * @throws SharepointException
   */
  public MockUserProfile2007WS(final SharepointClientContext ctx) {
    sharepointClientContext = ctx;
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public boolean isSPS() throws SharepointException {
    return true;
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public Set<String> getMyLinks() throws SharepointException {
    return null;
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public Set<String> getPersonalSiteList() throws SharepointException {
    return null;
  }
}
