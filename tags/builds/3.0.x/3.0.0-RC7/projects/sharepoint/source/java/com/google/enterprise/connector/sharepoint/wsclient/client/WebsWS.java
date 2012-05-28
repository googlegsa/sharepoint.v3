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

import java.util.Set;

public interface WebsWS {
  /**
   * To get the Web URL from any Page URL of the web
   *
   * @param pageURL
   * @return the well formed Web URL to be used for WS calls
   */
  public String getWebURLFromPageURL(String pageURL);

  /**
   * Discovers all the sites from the current site collection which are in
   * hierarchy lower to the current web.
   *
   * @return The set of child sites
   */
  public Set<String> getDirectChildsites();

  /**
   * To get the Web Title of a given web
   *
   * @param webURL To identiy the web whose Title is to be discovered
   * @param spType The SharePOint type for this web
   * @return the web title
   */
  public String getWebTitle(String webURL, SPType spType);

  /**
   * For checking the Web Service connectivity
   *
   * @return the Web Service connectivity status
   */
  public String checkConnectivity();
}
