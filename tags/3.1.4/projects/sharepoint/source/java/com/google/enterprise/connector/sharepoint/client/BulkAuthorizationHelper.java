// Copyright 2007 Google Inc.
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

package com.google.enterprise.connector.sharepoint.client;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.AuthDataPacket;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.client.BaseWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.BulkAuthorizationWS;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

/**
 * Java Client for calling GSBulkAuthorization.asmx. Provides a layer to talk to
 * the GSBulkAuthorization Web Service deployed on the SharePoint server. Any
 * call to this Web Service must go through this layer.
 *
 * @author nitendra_thakur
 */
public class BulkAuthorizationHelper {
  private final Logger LOGGER =
      Logger.getLogger(BulkAuthorizationHelper.class.getName());
  private SharepointClientContext sharepointClientContext;
  private BulkAuthorizationWS bulkAuthWS;

  /**
   * @param inSharepointClientContext The Context is passed so that necessary
   *          information can be used to create the instance of current class.
   *          Web Service endpoint is set to the default SharePoint URL stored
   *          in SharePointClientContext.
   * @throws SharepointException
   */
  public BulkAuthorizationHelper(
      final SharepointClientContext inSharepointClientContext)
      throws SharepointException {
    if (null == inSharepointClientContext) {
      throw new SharepointException("SharePointClient context cannot be null ");
    }

    sharepointClientContext = inSharepointClientContext;
    bulkAuthWS = sharepointClientContext.getClientFactory()
        .getBulkAuthorizationWS(sharepointClientContext);

    final String strDomain = sharepointClientContext.getDomain();
    String strUser = sharepointClientContext.getUsername();
    final String strPassword = sharepointClientContext.getPassword();
    final int timeout = sharepointClientContext.getWebServiceTimeOut();
    LOGGER.fine("Setting time-out to " + timeout + " milliseconds.");

    strUser = Util.getUserNameWithDomain(strUser, strDomain);
    bulkAuthWS.setUsername(strUser);
    bulkAuthWS.setPassword(strPassword);
    bulkAuthWS.setTimeout(timeout);
  }

  /**
   * To call the Authorize() Web Method of GSBulkAuthorization Web Service
   *
   * @param authDataPacketArray Contains the list of documents to be authorized
   * @param userId The username to be authorized
   * @return the updated {@link AuthDataPacket} object reflecting the
   *         authorization status for each document
   * @throws RemoteException
   */
  public AuthDataPacket[] authorize(final AuthDataPacket[] authDataPacketArray,
      final String userId) throws RemoteException {
    return Util.makeWSRequest(sharepointClientContext, bulkAuthWS,
        new Util.RequestExecutor<AuthDataPacket[]>() {
      public AuthDataPacket[] onRequest(final BaseWS ws) throws Throwable {
        return ((BulkAuthorizationWS) ws).authorize(authDataPacketArray,
            userId);
      }
      
      public void onError(final Throwable e) {
        LOGGER.log(Level.WARNING, "Call to authorize failed.", e);
      }
    });
  }

  /**
   * For checking connectivity to the GSBulkAuthorization web service
   *
   * @return The connectivity status "success" if succeed or the reason for
   *         failure.
   */
  public String checkConnectivity() {
    String status = Util.makeWSRequest(sharepointClientContext, bulkAuthWS,
        new Util.RequestExecutor<String>() {
      public String onRequest(final BaseWS ws) throws Throwable {
        return ((BulkAuthorizationWS) ws).checkConnectivity();
      }
      
      public void onError(final Throwable e) {
        LOGGER.log(Level.WARNING,
            "Can not connect to GSBulkAuthorization web service.", e);
      }
    });
    LOGGER.info("GS Connectivity status: " + status);
    return status;
  }
}
