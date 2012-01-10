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

package com.google.enterprise.connector.sharepoint.wsclient.mock;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.AuthDataPacket;
import com.google.enterprise.connector.sharepoint.wsclient.client.BulkAuthorizationWS;

import java.util.logging.Logger;
import java.rmi.RemoteException;

public class MockBulkAuthorizationWS implements BulkAuthorizationWS {
  private static final Logger LOGGER = Logger.getLogger(MockBulkAuthorizationWS.class.getName());
  private final SharepointClientContext sharepointClientContext;

  /**
   * @param ctx The Sharepoint context is passed so that necessary
   *    information can be used to create the instance of current class
   *    web service endpoint is set to the default SharePoint URL stored
   *    in SharePointClientContext.
   * @throws SharepointException
   */
  public MockBulkAuthorizationWS(final SharepointClientContext ctx) {
    sharepointClientContext = ctx;
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public AuthDataPacket[] authorize(final AuthDataPacket[] authDataPacketArray,
      final String userId) throws RemoteException {
    return null;
  }

  /**
   * (@inheritDoc)
   *
   * This simply returns SPConstants.CONNECTIVITY_SUCCESS. The connector
   * code checks for this result.
   */
  public String checkConnectivity() {
    return SPConstants.CONNECTIVITY_SUCCESS;
  }
}
