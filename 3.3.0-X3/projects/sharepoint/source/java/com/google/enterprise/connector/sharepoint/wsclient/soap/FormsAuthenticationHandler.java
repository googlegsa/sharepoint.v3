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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationMode;

public class FormsAuthenticationHandler {
  private final static Logger LOGGER =
      Logger.getLogger(FormsAuthenticationHandler.class.getName());
  private final List<String> authenticationCookiesList =
      new CopyOnWriteArrayList<String>();
  private final Runnable refreshRunnable = new RefreshRunnable();
  private AuthenticationMode authenticationMode;
  private final ScheduledExecutorService executor;
  private final SPAuthenticationWS authenWS;
  private final String webAppUrl;
  
  public FormsAuthenticationHandler(String webAppUrl, 
      ScheduledExecutorService executor, SharepointClientContext ctx)
          throws ServiceException {  
    this.executor = executor;
    this.webAppUrl = webAppUrl;
    authenWS = new SPAuthenticationWS(webAppUrl, ctx);
  }
  
  public void start() throws Exception {
    authenticationMode = authenWS.getAuthenticationMode();
    if (authenticationMode == AuthenticationMode.Forms) {
      refreshCookies();
    }
  }
  
  public List<String> getAuthenticationCookies() {
    return Collections.unmodifiableList(authenticationCookiesList);
  }
  
  public boolean isFormsAuthentication() {    
    return authenticationMode == AuthenticationMode.Forms;
  }
  
  private void refreshCookies() throws Exception {
    LOGGER.info("Refreshing forms authentication cookies for " + webAppUrl);
    String cookie = authenWS.getFormsAuthenCookie();
    if (cookie == null) {
      authenticationMode = AuthenticationMode.Windows;
      return;
    }
    
    if (authenticationCookiesList.isEmpty()) {
      authenticationCookiesList.add(cookie);
    } else {
      authenticationCookiesList.set(0, cookie);
    }
    
    int cookieRefreshDuration = (authenWS.getCookieTimeOut() + 1) / 2;
    LOGGER.info("forms authentication cookies refresh duration "
        + cookieRefreshDuration);
    executor.schedule(
        refreshRunnable, cookieRefreshDuration, TimeUnit.SECONDS);
  }
  
  private class RefreshRunnable implements Runnable {
    @Override
    public void run() {
      try {
        refreshCookies();
      } catch(Exception ex) {
        LOGGER.log(Level.WARNING, 
            "Error refreshing forms authentication cookies for Web App"
            + webAppUrl, ex);        
        executor.schedule(this, 5, TimeUnit.MINUTES);
      }
    }
  }
}
