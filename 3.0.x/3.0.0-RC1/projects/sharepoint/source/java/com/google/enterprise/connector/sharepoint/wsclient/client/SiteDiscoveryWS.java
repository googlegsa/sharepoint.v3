// Copyright 2011 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.wsclient.client;

import com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;

import java.util.Collection;
import java.util.Set;

public interface SiteDiscoveryWS {
  /**
   * Gets all the sitecollections from all the web applications for a given
   * sharepoint installation
   *
   * @return the set of all site colelltions returned bu the GSSiteDiscovery
   */
  public Set<String> getMatchingSiteCollections();

  /**
   * Retrieves and update the information about crawl behavior of a set of webs
   *
   * @param webs
   */
  public void updateWebCrawlInfoInBatch(Set<WebState> webs);

  /**
   * Retrieves the information about crawl behavior of the web whose URL was
   * used to construct the endpoint
   *
   * @return WebCrawlInfo of the web whose URL was used to construct the
   *         endpoint
   */
  public WebCrawlInfo getCurrentWebCrawlInfo();

  /**
   * Retrieves the information about crawl behavior of a the lists and set it
   * into the passed in {@link ListState}
   *
   * @param listCollection ListStates to be be updated
   */
  public void updateListCrawlInfo(Collection<ListState> listCollection);
}
