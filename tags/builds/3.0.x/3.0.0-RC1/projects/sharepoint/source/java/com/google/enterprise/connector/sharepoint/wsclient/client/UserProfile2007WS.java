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

public interface UserProfile2007WS extends UserProfile2003WS {
  /**
   * To get all the My Sites from the specified MySite BAse URL on configuration
   * page.
   *
   * @return the list of MySites
   * @throws SharepointException
   */
  public Set<String> getMyLinks() throws SharepointException;
}
