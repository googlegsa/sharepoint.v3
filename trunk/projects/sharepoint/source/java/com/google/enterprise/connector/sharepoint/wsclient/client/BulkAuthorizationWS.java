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

import com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.AuthDataPacket;

import java.rmi.RemoteException;

public interface BulkAuthorizationWS {
  /**
   * To call the Authorize() Web Method of GSBulkAuthorization Web Service
   *
   * @param authDataPacketArray Contains the list of documents to be authorized
   * @param userId The username to be authorized
   * @return the updated {@link AuthDataPacket} object reflecting the
   *         authorization status for each document
   * @throws RemoteException
   */
  public AuthDataPacket[] authorize(AuthDataPacket[] authDataPacketArray,
      String userId) throws RemoteException;

  /**
   * For checking connectivity to the bulk authorization web service
   *
   * @return The connectivity status "success" if succeed or the reason for
   *         failure.
   */
  public String checkConnectivity();

  /**
   * For checking the version of Google Services for SharePoint server.
   *
   * @return The version of Google SharePoint services deployed on to the
   *         SharePoint server or return error messages to it's caller, which 
   *         intern shown on the connector configuration UI.
   */
  public String getVersion();
}
