//Copyright 2007 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.adgroups;

import com.google.common.base.Strings;
import com.google.enterprise.connector.adgroups.AdDbUtil.Query;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
public class AdGroupsAuthenticationManager implements AuthenticationManager {
  Logger LOGGER = Logger.getLogger(AdGroupsAuthenticationManager.class.getName());

  AdGroupsConnector connector = null;

  /**
   * @param inSharepointClientContext Context Information is required to create
   *          the instance of this class
   */
  public AdGroupsAuthenticationManager(
      final AdGroupsConnector connector)
      throws RepositoryException {
    this.connector = connector;
  }

  /**
    * Authenticates the user against Active Directory server. Each instance of
    * the connector must be able to authenticate users regardless of which
    * domain he belongs to. The necessary data where to connect to have to
    * be stored in the database
    *
    * @param identity AuthenticationIdentity object created by CM while
    *          delegating authentication to the connector. This corresponds to
    *          one specific search user
    * @return AuthenticationResponse Contains the authentication status for the
    *         incoming identity
    */
    public AuthenticationResponse authenticate(
        final AuthenticationIdentity identity) throws RepositoryLoginException,
        RepositoryException {
      //TODO: encapsulate this properly
      AdDbUtil db = connector.crawler.db;
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
        sqlIdentity.put(AdConstants.DB_NETBIOSNAME, domain);
        query = Query.SELECT_USER_BY_NETBIOS_SAMACCOUNTNAME;
      }
      try {
        List<HashMap<String, Object>> users = db.select(query, sqlIdentity);
        if (users.size() == 0) {
          LOGGER.log(Level.WARNING, "User not found in the database ["
              + username + "] domain [" + domain + "]");
          return new AuthenticationResponse(false, "", null);
        } else if (users.size() > 1) {
          LOGGER.log(Level.WARNING,
              "Multiple users found in the database matching [" + username +
              "] domain [" + domain + "]");
          //TODO: log out information about all users found
          return new AuthenticationResponse(false, "", null);
        }
        HashMap<String, Object> user = users.get(0);
        List<String> groups = getAllGroupsForTheUser(
            (Long)user.get(AdConstants.DB_ENTITYID));
        return new AuthenticationResponse(true, "", groups);
      } catch (SQLException e) {
        LOGGER.log(Level.WARNING,
            "Failed to retrieve information about user from database ["
            + username + "] domain [" + domain + "].", e);
        return new AuthenticationResponse(false, "", null);
      }

      //TODO: connect to LDAP server and check password - requires additional
      // table in the database with LDAP configuration to enable multiple
      // connector instances

      //TODO: check what exactly to do if password is empty/null
    }

    List<String> getAllGroupsForTheUser(Long entityId) throws SQLException {
      //TODO: encapsulate this properly
      AdDbUtil db = connector.crawler.db;

      List<String> groups = new ArrayList<String>();
      List<Long> entities = new ArrayList<Long>();
      entities.add(entityId);

      HashMap<String, Object> params = new HashMap<String, Object>();

      // The loop goes over the entities list appending newly found groups
      // at the end of the list this way we will resolve each group only once
      for (int i = 0; i < entities.size(); ++i) {
        params.put(AdConstants.DB_ENTITYID, entities.get(i));
        List<HashMap<String, Object>> results =
            db.select(Query.SELECT_MEMBERSHIPS_BY_ENTITYID, params);
        for (HashMap<String, Object> result : results) {
          Long groupId = (Long) result.get(AdConstants.DB_ENTITYID);
          if (entities.indexOf(groupId) == -1)
            //TODO: group formatting if necessary
            groups.add((String) result.get(AdConstants.DB_NETBIOSNAME)
                + AdConstants.BACKSLASH
                + (String) result.get(AdConstants.DB_SAMACCOUNTNAME));
            entities.add(groupId);
        }
      }

      return groups;
    }
}
