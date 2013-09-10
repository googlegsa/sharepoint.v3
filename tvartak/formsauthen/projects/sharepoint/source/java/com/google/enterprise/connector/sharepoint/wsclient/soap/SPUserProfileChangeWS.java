// Copyright 2013 Google Inc.
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
import com.google.enterprise.connector.sharepoint.generated.userprofilechangeservice.UserProfileChangeDataContainer;
import com.google.enterprise.connector.sharepoint.generated.userprofilechangeservice.UserProfileChangeQuery;
import com.google.enterprise.connector.sharepoint.generated.userprofilechangeservice.UserProfileChangeService;
import com.google.enterprise.connector.sharepoint.generated.userprofilechangeservice.UserProfileChangeServiceLocator;
import com.google.enterprise.connector.sharepoint.generated.userprofilechangeservice.UserProfileChangeServiceSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfileChangeWS;

import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import org.apache.axis.transport.http.HTTPConstants;

public class SPUserProfileChangeWS implements UserProfileChangeWS {
  private final Logger LOGGER = Logger.getLogger(
      SPUserProfileChangeWS.class.getName());
  private UserProfileChangeServiceSoap_BindingStub stub;
  private List<String> authenticationCookies;

  public SPUserProfileChangeWS(SharepointClientContext 
      inSharepointClientContext) throws SharepointException {
    String endpoint = Util.encodeURL(inSharepointClientContext.getSiteURL())
        + SPConstants.USERPROFILECHANGEENDPOINT;
    LOGGER.config("Endpoint set to: " + endpoint);

    final UserProfileChangeServiceLocator loc =
        new UserProfileChangeServiceLocator();
    loc.setUserProfileChangeServiceSoapEndpointAddress(endpoint);

    final UserProfileChangeService service = loc;
    try {
      stub = (UserProfileChangeServiceSoap_BindingStub)
          service.getUserProfileChangeServiceSoap();
    } catch (final ServiceException e) {
      LOGGER.log(Level.WARNING, e.getMessage(), e);
      throw new SharepointException("Unable to create the userprofile stub.");
    }
  }

  @Override
  public String getUsername() {
    return stub.getUsername();
  }

  @Override
  public void setUsername(final String username) {
    stub.setUsername(username);
  }

  @Override
  public void setPassword(final String password) {
    stub.setPassword(password);
  }

  @Override
  public void setTimeout(final int timeout) {
    stub.setTimeout(timeout);
  }

  @Override
  public UserProfileChangeDataContainer getChanges(String changeToken,
      UserProfileChangeQuery changeQuery) throws RemoteException {
    setCookie();
    return stub.getChanges(changeToken, changeQuery);
  }

  @Override
  public String getCurrentChangeToken() throws RemoteException {
    setCookie();
    return stub.getCurrentChangeToken();
  }

  private List<String> cookie;
  @Override
  public void setFormsAuthenticationCookie(List<String> cookie) {
    this.cookie = cookie;
  }
  
  private void setCookie() {
    if (cookie != null) {
      stub._setProperty(HTTPConstants.HEADER_COOKIE, cookie.get(0));
      stub.setMaintainSession(true);
    }
  }
}
