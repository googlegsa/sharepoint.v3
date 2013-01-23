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

import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOf_sListHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders._sWebMetadataHolder;

import java.rmi.RemoteException;
import java.util.List;

public interface SiteDataWS extends BaseWS {
  /**
   * Gets the collection of all the lists on the sharepoint server.
   *
   * @return ArrayOf_sListHolder
   * @throws RemoteException
   */
  public ArrayOf_sListHolder getListCollection() throws RemoteException;

  /**
   * Makes a call to Site Data web service to retrieve site meta data.
   *
   * @return _sWebMetadataHolder
   * @throws RemoteException
   */
  public _sWebMetadataHolder getSiteData() throws RemoteException;
}
