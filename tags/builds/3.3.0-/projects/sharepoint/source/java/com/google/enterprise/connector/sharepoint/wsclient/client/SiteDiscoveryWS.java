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

import com.google.enterprise.connector.sharepoint.generated.gssitediscovery.ListCrawlInfo;
import com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo;

import java.rmi.RemoteException;

public interface SiteDiscoveryWS extends BaseWS {
  /**
   * Returns the top level URL of all site collections form all web 
   * applications for a given sharepoint installation.   
   *
   * @return an Object array where each entry is a URL String
   * @throws RemoteException
   */
  public Object[] getAllSiteCollectionFromAllWebApps() throws RemoteException;

  /**
   * Returns the crawl info of the current web.
   *
   * @return WebCrawlInfo of the web whose URL was used to construct the
   *         endpoint
   * @return a WebCrawlInfo
   * @throws RemoteException
   */
  public WebCrawlInfo getWebCrawlInfo() throws RemoteException;

  /**
   * Retrieves the information about crawl behavior of a list of webs
   * corresponding to the passed in web urls
   *
   * @param weburls All web URLs whose crawl info is to be found
   * @return an WebCrawlInfo array
   * @throws RemoteException
   */
  public WebCrawlInfo[] getWebCrawlInfoInBatch(String[] weburls)
      throws RemoteException;

  /**
   * Get the lists crawl info for the the current web.
   *
   * @param listGuids An array of list guids
   * @return an ListCrawlInfo array
   * @throws RemoteException
   */
  public ListCrawlInfo[] getListCrawlInfo(String[] listGuids)
      throws RemoteException;
}
