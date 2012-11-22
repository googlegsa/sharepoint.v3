// Copyright 2012 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.social;

import com.google.enterprise.connector.sharepoint.generated.sp2010.userprofileservice.ContactData;
import com.google.enterprise.connector.sharepoint.generated.sp2010.userprofileservice.GetUserProfileByIndexResult;

import java.rmi.RemoteException;

/**
 * This class is used for implementing mock user profile service for test.
 * 
 * @author tapasnay
 */
public interface UserProfileServiceGenerator {
  public long getUserProfileCount() throws RemoteException;

  public GetUserProfileByIndexResult getUserProfileByIndex(int index)
      throws RemoteException;

  public ContactData[] getUserColleagues(String key) throws RemoteException;

  public void setUsername(String username);

  public void setPassword(String password);
}
