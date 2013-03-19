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

import com.google.enterprise.connector.sharepoint.generated.userprofileservice.GetUserProfileByIndexResult;

import java.rmi.RemoteException;

public interface UserProfile2007WS extends BaseWS {
  /**
   * This method returns the information about the user profile by the specified index.
   *
   * @param index The index of the user profile to be retrieved
   * @return a GetUserProfileByIndexResult
   * @throws RemoteException
   */
  public GetUserProfileByIndexResult getUserProfileByIndex(int index)
      throws RemoteException;
}
