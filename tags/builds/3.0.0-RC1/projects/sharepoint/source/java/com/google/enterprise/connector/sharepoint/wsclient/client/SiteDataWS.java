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

import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;

import java.util.List;

public interface SiteDataWS {
  /**
   * Gets the collection of all the lists on the sharepoint server which are of
   * a given type. E.g., DocumentLibrary
   *
   * @param webstate The web from which the list/libraries are to be discovered
   * @return list of BaseList objects.
   */
  public List<ListState> getNamedLists(WebState webstate)
      throws SharepointException;

  /**
   * Makes a call to Site Data web service to retrieve site meta data and create
   * a SPDocuemnt and it returns a single SPDcoument.This method returns null if
   * and only if any one of SiteData stub or webState is null.
   *
   * @param webState The web from which we need to construct SPDcoument for it's
   *          landing page.
   * @return a single SPDocument for the given web.
   * @throws SharepointException
   */
  public SPDocument getSiteData(WebState webState)
      throws SharepointException;
}
