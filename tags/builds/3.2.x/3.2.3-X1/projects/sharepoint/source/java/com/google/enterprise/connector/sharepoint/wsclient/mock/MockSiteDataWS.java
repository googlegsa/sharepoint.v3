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
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOf_sListHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders._sWebMetadataHolder;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDataWS;

import java.rmi.RemoteException;
import java.util.logging.Logger;
import java.util.Collections;
import java.util.List;

public class MockSiteDataWS implements SiteDataWS {
  private static final Logger LOGGER = Logger.getLogger(MockSiteDataWS.class.getName());
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
  public MockSiteDataWS(final SharepointClientContext ctx) {
    sharepointClientContext = ctx;
  }

  /* @Override */
  public String getUsername() {
    return username;
  }

  /* @Override */
  public void setUsername(final String username) {
    this.username = username;
  }

  /* @Override */
  public void setPassword(final String password) {
    this.password = password;
  }

  /* @Override */
  public void setTimeout(final int timeout) {
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public ArrayOf_sListHolder getListCollection() throws RemoteException {
    return null;
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public _sWebMetadataHolder getSiteData() throws RemoteException {
    return null;
  }

  public String getContentList(String id) throws RemoteException {
    return "";
  }
}
