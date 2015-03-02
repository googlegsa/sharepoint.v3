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

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;

/**
 * Client context for getting social information from SharePoint
 * 
 * @author tapasnay
 */
public class SharepointSocialClientContext {
  private String siteUrl;
  private String userName;
  private String password;
  private String domain;
  private String gsaHost;
  private int gsaPort;
  private String connectorName;
  private String gsaAdmin;
  private String gsaAdminPassword;
  private SharepointConfig config;
  private SharepointClientContext spClientContext;
  private String userProfileCollection;

  public SharepointSocialClientContext(SharepointClientContext parentCtxt) {
    config = new SharepointConfig();
    config.init();
    setSpClientContext(parentCtxt);
  }

  public SharepointConfig getConfig() {
    return config;
  }

  public void setGsaHost(String gsaHostInput) {
    String host = gsaHostInput;
    String[] parts = host.split(":");
    if (parts.length > 1) {
      gsaHost = parts[0];
      setGsaPort(Integer.parseInt(parts[1]));
    } else {
      gsaHost = host;
      setGsaPort(SharepointSocialConstants.DEFAULT_GSAADMINPORT);
    }

  }

  public void setParentContext(SharepointClientContext parentCtxt) {
    setSpClientContext(parentCtxt);
  }

  public String getUrl() {
    return getSiteUrl();
  }

  public void setUrl(String url) {
    setSiteUrl(url);
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String user) {
    userName = user;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String pwd) {
    password = pwd;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String dom) {
    domain = dom;
  }

  public String getGsaAdmin() {
    return gsaAdmin;
  }

  public void setGsaAdmin(String gsaAdm) {
    gsaAdmin = gsaAdm;
  }

  public String getGsaPassword() {
    return gsaAdminPassword;
  }

  public void setGsaPassword(String gsaPass) {
    gsaAdminPassword = gsaPass;
  }

  public String getConnectorName() {
    return connectorName;
  }

  public void setConnectorName(String cname) {
    connectorName = cname;
  }

  public UserProfileServiceFactory getUserProfileServiceFactory() {
    return this.spClientContext.getUserProfileServiceFactory();
  }

  public String getSiteUrl() {
    return siteUrl;
  }

  public void setSiteUrl(String siteUrl) {
    this.siteUrl = siteUrl;
  }

  public String getGsaHost() {
    return gsaHost;
  }

  public int getGsaPort() {
    return gsaPort;
  }

  public void setGsaPort(int gsaPort) {
    this.gsaPort = gsaPort;
  }

  public SharepointClientContext getSpClientContext() {
    return spClientContext;
  }

  public void setSpClientContext(SharepointClientContext spClientContext) {
    this.spClientContext = spClientContext;
  }

  public void setUserProfileCollection(String name) {
    userProfileCollection = name;
  }

  public String getUserProfileCollection() {
    return userProfileCollection;
  }

  public int getFullTraversalIntervalInDays() {
    return this.spClientContext.getUserProfileFullTraversalInterval();
  }
}
