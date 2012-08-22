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

import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;

import java.util.List;

public interface AlertsWS {
  /**
   * Get the list of alerts that the current user has access to on the web
   * represeted by WebState
   *
   * @param parentWeb From which the laerts are to be accessed
   * @param alertListState represents the list that is created for alerts
   * @return list of {@link SPDocument}
   */
  public List<SPDocument> getAlerts(WebState parentWeb,
      ListState alertListState);
}
