// Copyright 2012 Google Inc.
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

import com.google.enterprise.connector.sharepoint.generated.userprofilechangeservice.UserProfileChangeDataContainer;
import com.google.enterprise.connector.sharepoint.generated.userprofilechangeservice.UserProfileChangeQuery;

import java.rmi.RemoteException;

public interface UserProfileChangeWS extends BaseWS {
  /**
   * Gets changes made to the user profiles using the specified change 
   * query and the specified change token.
   *
   * @param changeToken The change token of the changes in the user profile
   * @param changeQuery The change query specifying the requested changes
   * @return The changes made to the user profiles that match the filter 
   *         defined by the specified change token and change query.
   * @throws RemoteException
   */
  public UserProfileChangeDataContainer getChanges(String changeToken,
      UserProfileChangeQuery changeQuery) throws RemoteException;

  /**
   * Gets latest change token from SharePoint for User Profile Changes 
   *
   * @return latest change token for user profiles
   * @throws RemoteException
   */
  public String getCurrentChangeToken() throws RemoteException;
}
