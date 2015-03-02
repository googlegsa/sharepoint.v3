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

import com.google.common.base.Strings;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SocialCollectionHandler;
import com.google.enterprise.connector.spi.TraversalManager;

import java.util.logging.Logger;

/**
 * Class to implement social connector for SharePoint. This class is designed to
 * be able to serve as in independent connector. However, this is encapsulated
 * inside the base SharePoint connector as a sub-connector. In fact, this object
 * can be encapsulated in any future version of sharepoint connector, if
 * desired. specifically, the code is factorized so that the core can be used
 * from an adapter as well.
 * 
 * @author tapasnay
 */
public class SharepointSocialConnector implements Connector {

  public final static Logger LOGGER = Logger
      .getLogger(SharepointSocialConnector.class.getName());

  SharepointSocialClientContext ctxt;

  public SharepointSocialConnector(SharepointClientContext parentCtxt) {
    ctxt = new SharepointSocialClientContext(parentCtxt);
  }

  public void setUserName(String userId) {
    this.ctxt.setUserName(userId);
  }

  public String getUserName() {
    return this.ctxt.getUserName();
  }

  public void setPassword(String password) {
    this.ctxt.setPassword(password);
  }

  public String getPassword() {
    return this.ctxt.getPassword();
  }

  public void setDomain(String domain) {
    this.ctxt.setDomain(domain);
  }

  public String getDomain() {
    return this.ctxt.getDomain();
  }

  public void setSiteUrl(String siteUrl) {
    this.ctxt.setSiteUrl(siteUrl);
  }

  public String getSiteUrl() {
    return this.ctxt.getSiteUrl();
  }

  public String getGsaHost() {
    return this.ctxt.getGsaHost();
  }

  public void setGsaHost(String gsaHost) {
    this.ctxt.setGsaHost(gsaHost);
  }

  public void setConnectorName(String connector) {
    this.ctxt.setConnectorName(connector);
  }

  public String getConnectorName() {
    return this.ctxt.getConnectorName();
  }

  public void setGsaAdminUser(String gsaAdmin) {
    this.ctxt.setGsaAdmin(gsaAdmin);
  }

  public String getGsaAdminUser() {
    return this.ctxt.getGsaAdmin();
  }

  public void setGsaAdminPassword(String gsaAdminPassword) {
    this.ctxt.setGsaPassword(gsaAdminPassword);
  }

  public String getGsaAdminPassword() {
    return this.ctxt.getGsaPassword();
  }

  public void init(SharepointClientContext parentCtxt) {
    ctxt.setParentContext(parentCtxt);
  }

  @Override
  public Session login() throws RepositoryLoginException, RepositoryException {

    SocialCollectionHandler.initializeSocialCollection(ctxt.getGsaHost(),
        ctxt.getGsaPort(), ctxt.getGsaAdmin(), ctxt.getGsaPassword(),
        ctxt.getUserProfileCollection());
    return new SharepointSocialSession();

  }

  public class SharepointSocialSession implements Session {

    @Override
    public AuthenticationManager getAuthenticationManager()
        throws RepositoryException {
      return null;
    }

    @Override
    public AuthorizationManager getAuthorizationManager()
        throws RepositoryException {
      return null;
    }

    @Override
    public TraversalManager getTraversalManager() throws RepositoryException {
      return new SharepointSocialTraversalManager(ctxt);
    }

  }

  /**
   * Sets mySite Base Url. Also it derives profile Url from mySite Url by
   * replacing the reference to aspx page ( usually default.aspx ) with
   * person.aspx
   */
  public void setMySiteBaseURL(String mySiteBaseURL) {
    ctxt.getConfig().setMySiteBaseURL(mySiteBaseURL);
    if (!Strings.isNullOrEmpty(mySiteBaseURL)) {
      mySiteBaseURL = mySiteBaseURL.trim();
      if (mySiteBaseURL.endsWith("/")) {
        mySiteBaseURL = mySiteBaseURL.substring(0, mySiteBaseURL.length() - 1);
      }
      if (mySiteBaseURL.endsWith(".aspx")) {
        int lastSlash = mySiteBaseURL.lastIndexOf("/");
        ctxt.getConfig().setMyProfileBaseURL(
            mySiteBaseURL.substring(0, lastSlash) + "/person.aspx");
      } else {
        ctxt.getConfig().setMyProfileBaseURL(mySiteBaseURL + "/person.aspx");
      }
    }
  }

  public void setUserProfileCollection(String name) {
    ctxt.setUserProfileCollection(name);
  }

  public String getUserProfileCollection() {
    return ctxt.getUserProfileCollection();
  }
}
