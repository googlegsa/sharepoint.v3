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
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.webs.GetWebCollectionResponseGetWebCollectionResult;
import com.google.enterprise.connector.sharepoint.generated.webs.GetWebResponseGetWebResult;
import com.google.enterprise.connector.sharepoint.wsclient.client.WebsWS;

import java.util.logging.Logger;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MockWebsWS implements WebsWS {
  private static final Logger LOGGER = Logger.getLogger(MockWebsWS.class.getName());
  private final SharepointClientContext sharepointClientContext;
  private String username;
  private String password;

  /**
   * @param ctx The Sharepoint context is passed so that necessary
   *    information can be used to create the instance of current class
   *    web service endpoint is set to the default SharePoint URL stored
   *    in SharePointClientContext.
   * @throws SharepointException
   */
  public MockWebsWS(final SharepointClientContext ctx) {
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

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  @Override
  public GetWebCollectionResponseGetWebCollectionResult getWebCollection() {
    return null;
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  @Override
  public String webUrlFromPageUrl(String pageUrl) {
    return Util.getWebURLForWSCall(pageUrl);
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  @Override
  public GetWebResponseGetWebResult getWeb(final String webURL) {
    return new GetWebResponseGetWebResult();
  }

  @Override
  public void setFormsAuthenticationCookie(List<String> cookie) {
  }
}
