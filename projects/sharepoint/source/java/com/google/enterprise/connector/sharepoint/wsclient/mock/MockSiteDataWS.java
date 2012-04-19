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
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDataWS;

import java.util.logging.Logger;
import java.util.Collections;
import java.util.List;

public class MockSiteDataWS implements SiteDataWS {
  private static final Logger LOGGER = Logger.getLogger(MockSiteDataWS.class.getName());
  private final SharepointClientContext sharepointClientContext;

  /**
   * @param ctx The Sharepoint context is passed so that necessary
   *    information can be used to create the instance of current class
   *    web service endpoint is set to the default SharePoint URL stored
   *    in SharePointClientContext.
   * @throws SharepointException
   */
  public MockSiteDataWS(final SharepointClientContext ctx) {
    sharepointClientContext = ctx;
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public List<ListState> getNamedLists(final WebState webstate)
      throws SharepointException {
    return Collections.emptyList();
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public SPDocument getSiteData(final WebState webState)
      throws SharepointException {
    return null;
  }
}
