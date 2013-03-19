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

package com.google.enterprise.connector.sharepoint.wsclient.soap;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.GetUserProfileByIndexResult;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.UserProfileService;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.UserProfileServiceLocator;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.UserProfileServiceSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfile2007WS;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

/**
 * Java Client for calling UserProfile.asmx for SharePoint2007 Provides a layer
 * to talk to the UserProfile Web Service on the SharePoint server 2007 Any call
 * to this Web Service must go through this layer.
 *
 * @author nitendra_thakur
 */
public class SPUserProfileWS implements UserProfile2007WS {
  private static Logger LOGGER =
      Logger.getLogger(SPUserProfileWS.class.getName());
  private UserProfileServiceSoap_BindingStub stub;

  /**
   * @param inSharepointClientContext The Context is passed so that necessary
   *          information can be used to create the instance of current class
   *          Web Service endpoint is set to the default SharePoint URL stored
   *          in SharePointClientContext.
   * @throws SharepointException
   */

  public SPUserProfileWS(final SharepointClientContext
      inSharepointClientContext) throws SharepointException {
    String endpoint = Util.encodeURL(inSharepointClientContext.getSiteURL())
        + SPConstants.USERPROFILEENDPOINT;
    LOGGER.log(Level.CONFIG, "Endpoint set to: " + endpoint);

    final UserProfileServiceLocator loc = new UserProfileServiceLocator();
    loc.setUserProfileServiceSoapEndpointAddress(endpoint);
    final UserProfileService service = loc;

    try {
      stub = (UserProfileServiceSoap_BindingStub) service.getUserProfileServiceSoap();
    } catch (final ServiceException e) {
      LOGGER.log(Level.WARNING, e.getMessage(), e);
      throw new SharepointException("Unable to create the userprofile stub.");
    }
  }

  /**
   * (@inheritDoc)
   */
  public String getUsername() {
    return stub.getUsername();
  }

  /**
   * (@inheritDoc)
   */
  public void setUsername(final String username) {
    stub.setUsername(username);
  }

  /**
   * (@inheritDoc)
   */
  public void setPassword(final String password) {
    stub.setPassword(password);
  }

  /**
   * (@inheritDoc)
   */
  public void setTimeout(final int timeout) {
    stub.setTimeout(timeout);
  }

  /**
   * This method returns the information about the user profile by the specified index.
   *
   * @param index The index of the user profile to be retrieved
   * @return a GetUserProfileByIndexResult
   * @throws RemoteException
   */
  public GetUserProfileByIndexResult getUserProfileByIndex(int index)
      throws RemoteException {
    return stub.getUserProfileByIndex(index);
  }
}
