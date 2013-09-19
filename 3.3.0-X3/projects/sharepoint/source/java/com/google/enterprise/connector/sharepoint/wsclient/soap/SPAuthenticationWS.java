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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;
import javax.xml.ws.WebServiceException;

import org.apache.axis.transport.http.HTTPConstants;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationLocator;
import com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationMode;
import com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.generated.authentication.LoginErrorCode;
import com.google.enterprise.connector.sharepoint.generated.authentication.LoginResult;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.client.AuthenticationWS;

public class SPAuthenticationWS implements AuthenticationWS {

  AuthenticationSoap_BindingStub stub = null;
  private final static Logger LOGGER =
      Logger.getLogger(SPAuthenticationWS.class.getName());
  private String username;
  private String password;
  private String siteurl;
  private int cookieTimeOut = 1800;
  
  public SPAuthenticationWS(String siteurl, SharepointClientContext ctx)
      throws ServiceException {
    this.siteurl = siteurl;
    AuthenticationLocator  loc = new AuthenticationLocator();
    loc.setAuthenticationSoapEndpointAddress(Util.encodeURL(siteurl) 
        + SPConstants.AUTHENTICATION_END_POINT);
    stub = (AuthenticationSoap_BindingStub) loc.getAuthenticationSoap();
    stub.setMaintainSession(true);
    stub.setUsername(
        Util.getUserNameWithDomain(ctx.getUsername(), ctx.getDomain()));
    stub.setPassword(ctx.getPassword());
    stub.setTimeout(ctx.getWebServiceTimeOut());
    this.username = ctx.getUsername();
    this.password = ctx.getPassword();    
    
  }
  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public void setUsername(String username) {
    this.username = username;
    
  }

  @Override
  public void setPassword(String password) {
    this.password = password;
    
  }

  @Override
  public void setTimeout(int timeout) {
    stub.setTimeout(timeout);    
  }

  @Override
  public void setFormsAuthenticationCookie(List<String> cookie) {
    
    
  }

  @Override
  public AuthenticationMode getAuthenticationMode() throws Exception {
    return stub.mode();
  }

  @Override
  public String getFormsAuthenCookie() throws Exception {
    AuthenticationMode mode = stub.mode();
    if (mode != AuthenticationMode.Forms) {
      LOGGER.info("Site [" + siteurl
          + "] is protected with authentication mode [" + mode 
          +"]. Returning authen cookie null");
      return null;
    }
    LoginResult result;
    try {
      result = stub.login(username, password);
    } catch (WebServiceException e) {
      LOGGER.log(Level.WARNING, 
          "Possible SP2013 environment with windows authentication", e);
      return null;
    }
    if (result.getErrorCode() != LoginErrorCode.NoError) {
      throw new SharepointException(
          "Forms Authentication Login failed with Error Code ["
          + result.getErrorCode() + "].");
    }
    if (result.getTimeoutSeconds() != null) {
      cookieTimeOut = result.getTimeoutSeconds();
    }
    String[] cookies = stub._getCall().getMessageContext().getResponseMessage()
        .getMimeHeaders().getHeader("Set-Cookie");
    for(String c : cookies) {
      if (c.startsWith(result.getCookieName())) {
        return c;
      }
    }
    LOGGER.log(Level.WARNING, "Forms authentication cookie {0} not available "
        + "in response", result.getCookieName());
    return null;
  }
  
  public int getCookieTimeOut() {
    return cookieTimeOut;
  }
}
