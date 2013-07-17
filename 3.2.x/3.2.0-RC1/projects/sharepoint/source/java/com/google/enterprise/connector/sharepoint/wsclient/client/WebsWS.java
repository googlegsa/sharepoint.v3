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

package com.google.enterprise.connector.sharepoint.wsclient.client;

import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.generated.webs.GetWebCollectionResponseGetWebCollectionResult;
import com.google.enterprise.connector.sharepoint.generated.webs.GetWebResponseGetWebResult;

import java.rmi.RemoteException;
import java.util.Set;

public interface WebsWS extends BaseWS {
  /**
   * Returns the titles and urls of all sites directly beneath the 
   * current site.
   *
   * @return a GetWebCollectionResponseGetWebCollectionResult
   */
  public GetWebCollectionResponseGetWebCollectionResult getWebCollection()
      throws RemoteException;

  /**
   * To get the web URL from any page URL of the web
   *
   * @param pageUrl
   * @return the well formed web URL to be used for WS calls
   * @throws RemoteException
   */
  public String webUrlFromPageUrl(String pageUrl) throws RemoteException;

  /**
   * Returns properties of a site (for example, name, description, and theme).
   *
   * @param webURL The Sharepoint web URL to get the properties of
   * @return a GetWebResponseGetWebResult
   * @throws RemoteException
   */
  public GetWebResponseGetWebResult getWeb(final String webURL)
      throws RemoteException;
}
