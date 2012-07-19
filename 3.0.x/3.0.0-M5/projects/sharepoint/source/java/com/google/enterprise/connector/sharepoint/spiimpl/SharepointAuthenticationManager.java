// Copyright 2007 Google Inc.
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

package com.google.enterprise.connector.sharepoint.spiimpl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.enterprise.connector.adgroups.AdGroupsAuthenticationManager;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.dao.UserGroupMembership;
import com.google.enterprise.connector.sharepoint.ldap.LdapService;
import com.google.enterprise.connector.sharepoint.ldap.UserGroupsService;
import com.google.enterprise.connector.sharepoint.ldap.UserGroupsService.LdapConnectionSettings;
import com.google.enterprise.connector.sharepoint.wsclient.client.BulkAuthorizationWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.ClientFactory;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.SpiConstants.PrincipalType;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides an implementation of AuthenticationManager SPI provided
 * by CM for authenticating the search users. To understand how this module fits
 * into the Connector Manager framework refer to
 * http://code.google.com/apis/searchappliance
 * /documentation/connectors/110/connector_dev/cdg_authentication.html
 *
 * @author nitendra_thakur
 */
public class SharepointAuthenticationManager implements AuthenticationManager {
  private static final Logger LOGGER =
      Logger.getLogger(SharepointAuthenticationManager.class.getName());

  private final ClientFactory clientFactory;
  private final SharepointClientContext sharepointClientContext;
  @VisibleForTesting
  final LdapService ldapService;
  private final AdGroupsAuthenticationManager adGroupsAuthenticationManager;

  /**
   * @param inSharepointClientContext Context Information is required to create
   *          the instance of this class
   */
  public SharepointAuthenticationManager(final ClientFactory clientFactory,
      final SharepointClientContext inSharepointClientContext,
      final AdGroupsAuthenticationManager inAdGroupsAuthenticationManager)
      throws SharepointException {
    if (inSharepointClientContext == null) {
      throw new SharepointException("SharePointClientContext can not be null");
    }
    adGroupsAuthenticationManager = inAdGroupsAuthenticationManager;
    this.clientFactory = clientFactory;
    sharepointClientContext = (SharepointClientContext) inSharepointClientContext.clone();
    if (sharepointClientContext.isPushAcls()) {
      LdapConnectionSettings ldapConnectionSettings = sharepointClientContext.getLdapConnectionSettings();
      if (!Strings.isNullOrEmpty(ldapConnectionSettings.getHostname())) {
        ldapService = new UserGroupsService(inSharepointClientContext);
      } else {
        ldapService = null;
      }
    } else {
      ldapService = null;
    }
  }

  @Override
  public AuthenticationResponse authenticate(
      final AuthenticationIdentity identity) throws RepositoryLoginException,
      RepositoryException {
    if (adGroupsAuthenticationManager != null) {
      return authenticateAgainstActiveDirectory(identity);
    } else {
      return authenticateAgainstSharepoint(identity);
    }
  }

  //TODO: make this claims aware - authorize against Sharepoint and resolve
  //groups against AD only if necessary
  private AuthenticationResponse authenticateAgainstActiveDirectory(
      final AuthenticationIdentity identity) throws RepositoryLoginException,
      RepositoryException {
    AuthenticationResponse adAuthResult =
        adGroupsAuthenticationManager.authenticate(identity);
    if (!adAuthResult.isValid()) {
      return adAuthResult;
    }

    @SuppressWarnings("unchecked")
    Collection<Principal> adGroups =
    (Collection<Principal>) adAuthResult.getGroups();
    String domain =
    		adGroupsAuthenticationManager.getNetBiosNameForUser(identity);
   
    Set<Principal> spGroups = sharepointClientContext
        .getUserDataStoreDAO().getSharePointGroupsForSearchUserAndLdapGroups(
            sharepointClientContext.getGoogleLocalNamespace(), adGroups,
            domain + SPConstants.DOUBLEBACKSLASH
            + identity.getUsername());

    Collection<Principal> groups = new ArrayList<Principal>();
    groups.addAll(adGroups);
    groups.addAll(spGroups);

    return new AuthenticationResponse(
        adAuthResult.isValid(), adAuthResult.getData(), groups);
  }

  /**
   * Authenticates the user against the SharePoint server where Crawl URL
   * specified during connector configuration is hosted
   *
   * @param identity AuthenticationIdentity object created by CM while
   *          delegating authentication to the connector. This corresponds to
   *          one specific search user
   * @return AuthenticationResponse Contains the authentication status for the
   *         incoming identity
   */
  private AuthenticationResponse authenticateAgainstSharepoint(
      final AuthenticationIdentity identity) throws RepositoryLoginException,
      RepositoryException {
    if (sharepointClientContext == null) {
      LOGGER.warning("SharePointClientContext is null. Authentication Failed.");
      return null;
    }

    BulkAuthorizationWS bulkAuth = null;

    final String user = identity.getUsername();
    final String password = identity.getPassword();
    String domain = identity.getDomain();

    LOGGER.log(Level.INFO, "Received authN request for Username [ " + user
        + " ], domain [ " + domain + " ]. ");

    LOGGER.log(Level.INFO, "Received authN request for Username [ " + user
        + " ], domain [ " + domain + " ]. ");

    // If domain is not received as part of the authentication request, use
    // the one from SharePointClientContext
    // dnsroot to netbios conversion is required only when PushAcls is true
    // This code will be executed only when connector is configured for
    // late binding (PushAcls = false) Other scenario to cover is when
    // oldldapbehavior is true and connector is configured to feed ACLs.
    // If domain name contains "." char, it will be treated as dnsroot name
    // and it will be replaced with connector domain. Group resolution will
    // work only if connector is configured using netbios name.    
    if ((domain == null) || (domain.length() == 0) ||
        ((domain.indexOf(".") > -1) && sharepointClientContext.isPushAcls())) {
      domain = sharepointClientContext.getDomain();
    }

    if (!Strings.isNullOrEmpty(password)) {
      final String userName = Util.getUserNameWithDomain(user, domain);
      LOGGER.log(Level.INFO, "Authenticating User: " + userName);
      sharepointClientContext.setUsername(userName);
      sharepointClientContext.setPassword(password);
      bulkAuth = clientFactory.getBulkAuthorizationWS(sharepointClientContext);
      if (null == bulkAuth) {
        LOGGER.log(Level.SEVERE, "Failed to initialize BulkAuthorizationWS.");
        return null;
      }

      /*
       * If you can make a call to Web Service with the given credential, the
       * user is valid user. This should not be assumed as a valid SharePoint
       * user. A valid user is any user who is identified on the SharePoint
       * server.The Google Services deployed on the SharePoint server can be
       * called with such user credentials.
       *
       * If Authentication is successful get groups information from
       * UserDataStore data base for a given user and add it to
       * AuthenticationResponse. *
       */
      if (SPConstants.CONNECTIVITY_SUCCESS.equalsIgnoreCase(bulkAuth.checkConnectivity())) {
        LOGGER.log(Level.INFO, "Authentication succeeded for the user : "
            + user + " with identity : " + userName);
        if (sharepointClientContext.isPushAcls() && null != this.ldapService) {
          return getAllGroupsForTheUser(user);
        } else {
          // Handle the cases when connector should just return true
          // indicating successfull authN
          LOGGER.config("No group resolution has been attempted as connector is not set to feed ACL");
          return new AuthenticationResponse(true, "", null);
        }
      }
    } else {
      LOGGER.config("AuthN was not attempted as password is empty and groups are being returned.");
      return getAllGroupsForTheUser(user);
    }
    LOGGER.log(Level.WARNING, "Authentication failed for " + user);
    return new AuthenticationResponse(false, "", null);
  }

  /**
   * This method makes a call to {@link LdapService} to get all AD groups and SP
   * groups of which he/she is a direct or indirect member of and returns
   * {@link AuthenticationResponse}.
   *
   * @param searchUser to fetch all SharePoint groups and Directory groups to
   *          which he/she is a direct or indirect member of.
   * @return {@link AuthenticationResponse}
   * @throws SharepointException
   */
  @VisibleForTesting
  AuthenticationResponse getAllGroupsForTheUser(String searchUser)
      throws SharepointException {
    LOGGER.info("Attempting group resolution for user : " + searchUser);
    Set<Principal> allSearchUserGroups = ldapService.getAllGroupsForSearchUser(
        sharepointClientContext, searchUser);
    if (null != allSearchUserGroups && allSearchUserGroups.size() > 0) {
      // Should return true if there is at least one group returned by
      // LDAP service.
      LOGGER.log(Level.INFO, "Group resolution returned following groups "
          + "for the search user: {0}\n{1}",
          new Object[] { searchUser, allSearchUserGroups.toString() });
      return new AuthenticationResponse(true, "", allSearchUserGroups);
    } else {
      LOGGER.info("Group resolution returned no groups for the search user: "
          + searchUser);
      // Should return true with null groups.
      return new AuthenticationResponse(true, "", null);
    }
  }
}
