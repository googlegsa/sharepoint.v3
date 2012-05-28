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
import com.google.enterprise.connector.sharepoint.generated.sp2010.userprofileservice.UserProfileServiceSoap12Stub;

import org.apache.axis.AxisFault;

import java.net.URL;
import java.rmi.RemoteException;

/**
 * This is the proxy for the real SharePoint userProfileService Soap Proxy This
 * one is instantiated by production service factory.
 * 
 * @author tapasnay
 */
public class SharepointUserProfileService extends UserProfileServiceSoap12Stub
    implements UserProfileServiceGenerator {

  public SharepointUserProfileService(URL endpointURL) throws AxisFault {
    super(endpointURL, null);
  }

  public long getUserProfileCount() throws RemoteException {
    return super.getUserProfileCount();
  }

  public GetUserProfileByIndexResult getUserProfileByIndex(int index)
      throws RemoteException {
    return super.getUserProfileByIndex(index);
  }

  public ContactData[] getUserColleagues(String key) throws RemoteException {
    return super.getUserColleagues(key);
  }
}
