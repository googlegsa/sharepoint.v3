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

package com.google.enterprise.connector.adgroups;

import com.google.common.base.Strings;
import com.google.enterprise.connector.adgroups.AdConstants.Method;
import com.google.enterprise.connector.adgroups.AdDbUtil.Query;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.SpiConstants.PrincipalType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.CommunicationException;
import javax.naming.NamingException;

/**
 * This class provides an implementation of AuthenticationManager SPI provided
 * by CM for authenticating the search users. To understand how this module fits
 * into the Connector Manager framework refer to
 * http://code.google.com/apis/searchappliance
 * /documentation/connectors/110/connector_dev/cdg_authentication.html
 *
 * @author nitendra_thakur
 */
public class AdGroupsAuthenticationManager implements AuthenticationManager {
  private static final Logger LOGGER =
      Logger.getLogger(AdGroupsAuthenticationManager.class.getName());

  private final AdDbUtil db;
  private final String globalNamespace;

  /**
   * @param connector an instance of an {@link AdGroupsConnector}
   */
  public AdGroupsAuthenticationManager(AdGroupsConnector connector)
      throws RepositoryException {
    db = new AdDbUtil(connector.getDataSource(), connector.getDatabaseType());
    globalNamespace = connector.getGoogleGlobalNamespace();
  }

  /**
    * Authenticates the user against Active Directory server. Each instance of
    * the connector must be able to authenticate users regardless of which
    * domain he belongs to. The necessary data where to connect to have to
    * be stored in the database.
    *
    * @param identity AuthenticationIdentity object created by CM while
    *          delegating authentication to the connector. This corresponds to
    *          one specific search user
    * @return AuthenticationResponse Contains the authentication status for the
    *         incoming identity
    */
  @Override
  public AuthenticationResponse authenticate(
      final AuthenticationIdentity identity)
      throws RepositoryLoginException, RepositoryException {
    final String username = identity.getUsername();
    final String password = identity.getPassword();
    final String domain = identity.getDomain();

    LOGGER.log(Level.INFO, "Received authN request for Username [ "
        + username + " ], domain [ " + domain + " ]. ");

    HashMap<String, Object> sqlIdentity = new HashMap<String, Object>();
    sqlIdentity.put(AdConstants.DB_SAMACCOUNTNAME, username);
    Query query;
    if (Strings.isNullOrEmpty(domain)) {
      query = Query.SELECT_USER_BY_SAMACCOUNTNAME;
    } else {
      //TODO: rename the DB bound variable to domain
      sqlIdentity.put(AdConstants.DB_NETBIOSNAME, domain);
      query = Query.SELECT_USER_BY_DOMAIN_SAMACCOUNTNAME;
    }

    try {
      List<HashMap<String, Object>> users = db.select(query, sqlIdentity);
      if (users.size() == 0) {
        LOGGER.warning("User not found in the database ["
            + username + "] domain [" + domain + "]");
        return new AuthenticationResponse(false, "", null);
      } else if (users.size() > 1) {
        StringBuffer sb = new StringBuffer("Multiple users found in the "
            + "database matching [" + domain + "]\\[" + username + "]: ");
        for (HashMap<String, Object> u : users) {
          sb.append("[").append(u.get("dn")).append("] ");
        }
        LOGGER.warning(sb.toString());
        return new AuthenticationResponse(false, "", null);
      }
      HashMap<String, Object>user = users.get(0);
      List<Principal> groups =
          getAllGroupsForTheUser((Number) user.get(AdConstants.DB_ENTITYID));
      if (password != null && !authenticateUser(
              (String) user.get(AdConstants.DB_DNSROOT),
              (String) user.get(AdConstants.DB_NETBIOSNAME)
                  + AdConstants.BACKSLASH
                  + (String) user.get(AdConstants.DB_SAMACCOUNTNAME),
              password)) {
        return new AuthenticationResponse(false, "", null);
      }
      StringBuffer sb = new StringBuffer("Resolved ").append(groups.size())
          .append(" AD group(s) for user [").append(username).append("]")
          .append(" domain [").append(domain).append("]: ");
      for (Principal group : groups) {
        sb.append("[").append(group.getName()).append("] ");
      }
      LOGGER.info(sb.toString());
      if (identity instanceof MutableIdentity) {
        MutableIdentity mutable = (MutableIdentity) identity;
        mutable.setDomain((String) user.get(AdConstants.DB_NETBIOSNAME));
        mutable.setUsername((String) user.get(AdConstants.DB_SAMACCOUNTNAME));
        LOGGER.fine("New identity: [" + domain + "\\" + username
            + "] Active Directory: [" + identity.getDomain()
            + "\\" + identity.getUsername() + "]");
      }
      return new AuthenticationResponse(true, "", groups);
    } catch (SQLException e) {
      LOGGER.log(Level.WARNING,
          "Failed to retrieve information about user from database ["
          + username + "] domain [" + domain + "].", e);
      return new AuthenticationResponse(false, "", null);
    }
  }

  /**
   * Connects to specified AD domain with users principal and password.
   *
   * @param dnsRoot hostname of the domain
   * @param principal userPrincipalName of the user
   * @param password password
   * @return if authentication was successful
   */
  boolean authenticateUser(String dnsRoot, String principal, String password) {
    // Authenticate via SSL
    try {
      new AdServer(Method.SSL, dnsRoot, 636, principal, password).connect();
      // No exception thrown - authentication succeeded
      LOGGER.info("Successfully authenticated user [" + principal + "]");
      return true;
    } catch (CommunicationException e) {
      // network or SSL related, continue without SSL
      LOGGER.log(Level.FINE, "SSL Authentication failed", e);
    } catch (NamingException e) {
      // NamingException - authentication failed
      LOGGER.log(Level.INFO,
          "SSL Authenticated failed for user [" + principal + "]", e);
      return false;
    }

    try {
      new AdServer(Method.STANDARD, dnsRoot, 389, principal, password).connect();
      // No exception thrown - authentication succeeded
      LOGGER.info("Successfully authenticated user [" + principal + "]");
      return true;
    } catch (CommunicationException e) {
      // network related exception
      LOGGER.log(Level.INFO,
          "Plain Authentication failed for user [" + principal + "]", e);
      return false;
    } catch (Exception e) {
      // any other exception - authentication failed
      LOGGER.log(Level.FINE,
          "Authentication failed for user [ " + principal + " ]", e);
      return false;
    }
  }

  Principal formatGroup(HashMap<String, Object> entity) {
    String netbiosName = (String) entity.get(AdConstants.DB_NETBIOSNAME);
    String samAccountName = (String) entity.get(AdConstants.DB_SAMACCOUNTNAME);

    String principalName;
    if (netbiosName != null) {
      principalName = netbiosName + AdConstants.BACKSLASH + samAccountName;
    } else {
      principalName = samAccountName;
    }
    return new Principal(PrincipalType.UNKNOWN, globalNamespace, principalName);
  }

  List<Principal> getAllGroupsForTheUser(Number entityId) throws SQLException {
    List<Principal> groups = new ArrayList<Principal>();
    List<Number> entities = new ArrayList<Number>();
    entities.add(entityId);

    // Add current user to all implicit well known entities
    for (HashMap<String, Object> wellKnown :
             db.select(Query.SELECT_WELLKNOWN_MEMBERSHIPS, null)) {
      groups.add(formatGroup(wellKnown));
      entities.add((Number) wellKnown.get(AdConstants.DB_ENTITYID));
    }

    HashMap<String, Object> params = new HashMap<String, Object>();
    // The loop goes over the entities list appending newly found groups
    // at the end of the list this way we will resolve each group only once
    for (int i = 0; i < entities.size(); ++i) {
      params.put(AdConstants.DB_ENTITYID, entities.get(i));
      List<HashMap<String, Object>> results =
          db.select(Query.SELECT_MEMBERSHIPS_BY_ENTITYID, params);
      for (HashMap<String, Object> result : results) {
        Number groupId = (Number) result.get(AdConstants.DB_ENTITYID);
        if (!entities.contains(groupId)) {
          groups.add(formatGroup(result));
          entities.add(groupId);
        }
      }
    }

    return groups;
  }
}
