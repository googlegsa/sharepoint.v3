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

package com.google.enterprise.connector.sharepoint.wsclient.soap.sp2003;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.GetUserProfileByIndexResult;
import com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.PropertyData;
import com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.UserProfileService;
import com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.UserProfileServiceLocator;
import com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.UserProfileServiceSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfile2003WS;

import org.apache.axis.AxisFault;

import java.rmi.RemoteException;
import java.text.Collator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

/**
 * Java Client for calling UserProfile.asmx for SharePoint 2003 Provides a layer
 * to talk to the UserProfile Web Service on the SharePoint server 2003 Any call
 * to this Web Service must go through this layer.
 *
 * @author nitendra_thakur
 */
public class SPUserProfileWS implements UserProfile2003WS {
  private final Logger LOGGER = Logger.getLogger(SPUserProfileWS.class.getName());
  private SharepointClientContext sharepointClientContext;
  private UserProfileServiceSoap_BindingStub stub;
  String endpoint;

  private final String personalSpaceTag = "PersonalSpace";

  /**
   * @param inSharepointClientContext The Context is passed so that necessary
   *          information can be used to create the instance of current class
   *          Web Service endpoint is set to the default SharePoint URL stored
   *          in SharePointClientContext.
   * @throws SharepointException
   */
  public SPUserProfileWS(final SharepointClientContext inSharepointClientContext)
      throws SharepointException {
    if (inSharepointClientContext != null) {
      sharepointClientContext = inSharepointClientContext;
      endpoint = Util.encodeURL(sharepointClientContext.getSiteURL())
          + SPConstants.USERPROFILEENDPOINT;
      LOGGER.log(Level.CONFIG, "Endpoint set to: " + endpoint);

      final UserProfileServiceLocator loc = new UserProfileServiceLocator();
      loc.setUserProfileServiceSoapEndpointAddress(endpoint);

      final UserProfileService service = loc;
      try {
        stub = (UserProfileServiceSoap_BindingStub) service.getUserProfileServiceSoap();
      } catch (final ServiceException e) {
        LOGGER.log(Level.WARNING, e.getMessage(), e);
        throw new SharepointException("Unable to create the userprofile stub");
      }

      final String strDomain = inSharepointClientContext.getDomain();
      String strUserName = inSharepointClientContext.getUsername();
      final String strPassword = inSharepointClientContext.getPassword();

      strUserName = Util.getUserNameWithDomain(strUserName, strDomain);
      stub.setUsername(strUserName);
      stub.setPassword(strPassword);
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
