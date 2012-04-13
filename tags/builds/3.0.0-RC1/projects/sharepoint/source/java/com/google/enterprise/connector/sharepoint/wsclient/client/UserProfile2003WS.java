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

import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

import java.util.Set;

public interface UserProfile2003WS {
  /**
   * Checks to see if the current web to which the web service endpioint 
   * is set is an SPS site.
   *
   * @return if the endpoint being used is an SPS site
   * @throws SharepointException
   */
  public boolean isSPS() throws SharepointException;

  /**
   * To get all the personal sites from the current web.
   *
   * @return the list of personal sites
   * @throws SharepointException
   */
  public Set<String> getPersonalSiteList() throws SharepointException;
}
