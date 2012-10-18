// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.sharepoint.dao;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * Data Access Object layer for accessing the connector names table.
 *
 * @author nageswara_sura
 */
public class ConnectorNamesDAO extends SimpleSharePointDAO {
  private final Logger LOGGER = Logger.getLogger(ConnectorNamesDAO.class.getName());
  public static Set<String> connectorNames = new HashSet<String>();

  public ConnectorNamesDAO(final DataSource dataSource,
      final QueryProvider queryProvider) throws SharepointException {
    super(dataSource, queryProvider);
    // Check against connector/tomcat restarts, to make sure that connector
    // names gets populated from the database table.
    if (connectorNames.isEmpty()) {
      connectorNames.addAll(getAllConnectorNames());
    }
  }

  /**
   * Adds a given connector name to the database, If it is not available in the
   * connector names list.
   *
   * @param connectorName name to be added to the table.
   * @throws SharepointException
   */
  public void addConnectorInstanceName(String connectorName)
      throws SharepointException {
    int status = -1;
    if (null == connectorName) {
      LOGGER.info("ConnectorNamesDAO recieves null connector name.");
      return;
    }
    if (!connectorNames.contains(connectorName)) {
      connectorNames.add(connectorName);
      Query query = Query.CN_INSERT;
      Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put(SPConstants.CONNECTOR_NAME_COLUMN, connectorName);
      try {
        status = getSimpleJdbcTemplate().update(getSqlQuery(query), parameters);
      } catch (DataIntegrityViolationException e) {
        LOGGER.log(Level.FINE, "Connector name " + connectorName
            + "already exists in connector names table.", e);
      } catch (Throwable e) {
        throw new SharepointException(
            "Failed to add the record for parameter [" + connectorName + "]", e);
      }
      if (status == 1) {
        LOGGER.log(Level.INFO, "Sucessfully inserted the connector name ["
            + connectorName + "] using the query [ " + query + " ]");
      }
    } else {
      LOGGER.log(Level.INFO, "Connector name [" + connectorName
          + "] is already existed in the table.");
    }
  }

  /**
   * Removes the given connector name from the database.
   *
   * @param connectorName connector name to be removed from the database.
   * @throws SharepointException
   */
  public void removeConnectorName(String connectorName)
      throws SharepointException {
    int status = -1;
    if (null == connectorName) {
      LOGGER.info("ConnectorNamesDAO recieved a null connector name.");
      return;
    }
    Query query = Query.CN_DELETE;
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put(SPConstants.CONNECTOR_NAME_COLUMN, connectorName);
    try {
      status = getSimpleJdbcTemplate().update(getSqlQuery(query), parameters);
    } catch (DataIntegrityViolationException e) {
      LOGGER.log(Level.FINE, "Connector name entry " + connectorName
          + "is already exists in the table.", e);
    } catch (Throwable e) {
      throw new SharepointException(
          "Failed to remove the record for parameter [" + connectorName + "]",
          e);
    }
    if (status == 1) {
      connectorNames.remove(connectorName);
      LOGGER.log(Level.INFO, "Sucessfully removed the connector name ["
          + connectorName + "] using the query [" + query + "]");
    } else {
      LOGGER.log(Level.FINE, "Query [ " + query + "executed with status code: "
          + status);
    }
  }

  /**
   * Removes the Connector_Names table form the database.
   */
  public int dropConnectorNamesTable() throws SharepointException {
    int status = -1;
    try {
      status = getSimpleJdbcTemplate().update(getSqlQuery(Query.CN_DROP_TABLE));
    } catch (Throwable e) {
      throw new SharepointException(
          "Failed to remove the table with the query [" + Query.CN_DROP_TABLE
              + "]", e);
    }
    if (status == 0) {
      LOGGER.info("Sucessfully removed the Connector_Names table from the data base using the query ["
          + Query.CN_DROP_TABLE + "]");
    }
    return status;
  }

  /**
   * @return set of all the connector names that are configured.
   */
  public Set<String> getAllConnectorNames() throws SharepointException {
    List<Map<String, Object>> results;
    Query query = Query.CN_SELECT;
    Set<String> connectorNames = new HashSet<String>();
    try {
      results = getSimpleJdbcTemplate().queryForList(getSqlQuery(query), new HashMap<String, Object>());
    } catch (Throwable e) {
      throw new SharepointException(
          "Failed to retrieve all the connector names from the table with the quey ["
              + Query.CN_SELECT + "]", e);
    }
    for (Iterator<Map<String, Object>> iterator = results.iterator(); iterator.hasNext();) {
      Map<String, Object> map = iterator.next();
      connectorNames.add((String) map.get(SPConstants.CONNECTOR_NAME_COLUMN_CAPITAL));
    }
    LOGGER.log(Level.INFO, "Returning all the connector names : "
        + connectorNames);
    return connectorNames;
  }
}
