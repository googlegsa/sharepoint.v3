// Copyright 2012 Google Inc.
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

import com.google.enterprise.connector.spi.RepositoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class AdDbUtil {
  private static final Logger LOGGER =
    Logger.getLogger(AdDbUtil.class.getName());

  public enum Query {
    TEST_SERVERS("TEST_SERVERS"),
    CREATE_SERVERS_SEQUENCE("CREATE_SERVERS_SEQUENCE"),
    CREATE_SERVERS("CREATE_SERVERS"),
    DROP_SERVERS_TABLE("DROP_SERVERS_TABLE"),
    TEST_ENTITIES("TEST_ENTITIES"),
    CREATE_ENTITIES_SEQUENCE("CREATE_ENTITIES_SEQUENCE"),
    CREATE_ENTITIES("CREATE_ENTITIES"),
    DROP_ENTITIES_TABLE("DROP_ENTITIES_TABLE"),
    TEST_MEMBERS("TEST_MEMBERS"),
    CREATE_MEMBERS_SEQUENCE("CREATE_MEMBERS_SEQUENCE"),
    CREATE_MEMBERS("CREATE_MEMBERS"),
    DROP_MEMBERS_TABLE("DROP_MEMBERS_TABLE"),
    SELECT_SERVER("SELECT_SERVER"),
    UPDATE_SERVER("UPDATE_SERVER"),
    MERGE_ENTITIES("MERGE_ENTITIES"),
    ADD_ENTITIES("ADD_ENTITIES"),
    MATCH_ENTITIES("MATCH_ENTITIES"),
    RESOLVE_PRIMARY_GROUPS("RESOLVE_PRIMARY_GROUPS"),
    FIND_ENTITY("FIND_ENTITY"),
    FIND_PRIMARY_GROUP("FIND_PRIMARY_GROUP"),
    FIND_GROUP("FIND_GROUP"),
    FIND_FOREIGN("FIND_FOREIGN"),
    MERGE_MEMBERSHIP("MERGE_MEMBERSHIP"),
    DELETE_MEMBERSHIPS("DELETE_MEMBERSHIPS"),
    SELECT_USER_BY_SAMACCOUNTNAME("SELECT_USER_BY_SAMACCOUNTNAME"),
    SELECT_USER_BY_DOMAIN_SAMACCOUNTNAME
        ("SELECT_USER_BY_DOMAIN_SAMACCOUNTNAME"),
    SELECT_ENTITY_BY_DN_AND_NOT_GUID("SELECT_ENTITY_BY_DN_AND_NOT_GUID"),
    SELECT_WELLKNOWN_MEMBERSHIPS("SELECT_WELLKNOWN_MEMBERSHIPS"),
    SELECT_MEMBERSHIPS_BY_ENTITYID("SELECT_MEMBERSHIPS_BY_ENTITYID"),
    SELECT_MEMBERSHIPS_BY_DN("SELECT_MEMBERSHIPS_BY_DN"),
    DELETE_MEMBERSHIPS_BY_DN_AND_MEMBERDN
        ("DELETE_MEMBERSHIPS_BY_DN_AND_MEMBERDN"),
    SELECT_ALL_ENTITIES_BY_SID("SELECT_ALL_ENTITIES_BY_SID"),
    DELETE_ENTITY("DELETE_ENTITY"),
    TEST_CONNECTORNAME("TEST_CONNECTORNAME"),
    CREATE_CONNECTORNAME("CREATE_CONNECTORNAME"),
    DROP_CONNECTORNAME_TABLE("DROP_CONNECTORNAME_TABLE"),
    ADD_CONNECTORNAME("ADD_CONNECTORNAME"),
    SELECT_CONNECTORNAME("SELECT_CONNECTORNAME"),
    DELETE_CONNECTORNAME("DELETE_CONNECTORNAME");

    private String query;
    Query(String query) {
      this.query = query;
    }
    @Override
    public String toString() {
      return query;
    }
  }

  private DataSource dataSource;
  private Connection connection;
  private int batchHint = 50;

  //TODO: load table names from bean
  private Map<String, String> tables = new HashMap<String, String>() {{
    put("servers", "servers");
    put("entities", "entities");
    put("members", "members");
    put("connectornames", "Connector_Names");
    put("sequence", "_sequence");
    put("index", "_index");
  }};

  private ResourceBundle queries;

  public AdDbUtil(DataSource dataSource, String databaseType) {
    queries =
        ResourceBundle.getBundle(getClass().getPackage().getName() + ".sql",
        new Locale(databaseType));

    this.dataSource = dataSource;
    try {
      connection = dataSource.getConnection();
      try {
        select(Query.TEST_CONNECTORNAME, null);
      } catch (SQLException e) {
        execute(Query.CREATE_CONNECTORNAME, null);
      }
      try {
        select(Query.TEST_SERVERS, null);
      } catch (SQLException e) {
        execute(Query.CREATE_SERVERS_SEQUENCE, null);
        execute(Query.CREATE_SERVERS, null);
      }
      try {
        select(Query.TEST_ENTITIES, null);
      } catch (SQLException e) {
        execute(Query.CREATE_ENTITIES_SEQUENCE, null);
        execute(Query.CREATE_ENTITIES, null);
      }
      try {
        select(Query.TEST_MEMBERS, null);
      } catch (SQLException e) {
        execute(Query.CREATE_MEMBERS_SEQUENCE, null);
        execute(Query.CREATE_MEMBERS, null);
      }
    } catch (SQLException e) {
      LOGGER.log(
          Level.SEVERE, "Errors establishing connection to the database.", e);
    }
  }

  /**
   * Replace named placeholder in the query
   * @param query to have it's parameters bound
   * @param identifiers this list WILL get modified
   * @return SQL query with questionmarks as placeholders
   */
  private String sortParams(Query query, List<String> identifiers) {
    String sql = queries.getString(query.toString());
    StringBuffer finalSql = new StringBuffer(sql.length());

    for (int i = 0; i < sql.length(); ++i) {
      int current = i;

      // when quote is found look for matching quote
      if (sql.charAt(i) == '\'' || sql.charAt(i) == '"') {
        char quote = sql.charAt(i);
        ++i;

        while (i < sql.length()) {
          if (sql.charAt(i) == quote) {
            if (i + 1 < sql.length() && sql.charAt(i + 1) == quote) {
              ++i;
            } else {
              break;
            }
          }
          ++i;
        }
      } else if (sql.charAt(i) == AdConstants.COLON_CHAR) {
        if (i < sql.length() + 1
            && (Character.isJavaIdentifierStart(sql.charAt(i + 1)))
                || (i < sql.length() + 2
                    && sql.charAt(i + 1) == AdConstants.COLON_CHAR)
                    && Character.isJavaIdentifierStart(sql.charAt(i + 2)))
        {
          boolean quoted = sql.charAt(i + 1) == AdConstants.COLON_CHAR;
          int start = i += (quoted ? 2 : 1);
          while (i < sql.length()
              && Character.isJavaIdentifierPart(sql.charAt(i))) {
            ++i;
          }

          String identifier = sql.substring(start, i);
          // replace current identifier with ? or table name
          if (tables.containsKey(identifier) && quoted) {
            // ::tableName - insert table name in quotes
            finalSql.append('\'')
                .append(tables.get(identifier))
                .append('\'');
          } else if (tables.containsKey(identifier)) {
            // :tableName - insert table name
            finalSql.append(tables.get(identifier));
          } else {
            // :identified - insert placeholder and remember the param name
            finalSql.append("?");
            identifiers.add(identifier);
          }
          // repeat on last character
          i--;
          continue;
        }
      }
      // add to final string whatever we already scanned over
      finalSql.append(sql.substring(current, i + 1));
    }

    return finalSql.toString();
  }

  /**
   * Binds parameters to the query
   * @param statement to have it's parameters bound
   * @param identifiers order of the parameters in the query
   * @param params parameter values
   * @throws SQLException
   */
  private void addParams(PreparedStatement statement, List<String> identifiers,
      Map<String, Object> params) throws SQLException {
    for (int i = 0; i < identifiers.size(); ++i) {
      statement.setObject(i + 1,
        params.get(identifiers.get(i)));
    }
  }
  
  /**
   * select statement in the database 
   * @param query to be executed
   * @param params parameter values
   * @return entity id in the database
   * @throws SQLException
   */
  public Long getEntityId(Query query, Map<String, Object> params)
      throws SQLException {
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      List<String> identifiers = new ArrayList<String>();
      // function sortParams fills identifiers variable
      String sql = sortParams(query, identifiers);
      statement = connection.prepareStatement(sql);
      addParams(statement, identifiers, params);

      rs = statement.executeQuery();
      if (!rs.next()) {
        return null;
      }
      Long result = rs.getLong(1);    
      return result;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (statement != null) {
        statement.close();
      }
    }
  }

  public String getSingleString(Query query, Map<String, Object> params,
      String columnName) throws SQLException {
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      List<String> identifiers = new ArrayList<String>();
      // function sortParams fills identifiers variable
      String sql = sortParams(query, identifiers);
      statement = connection.prepareStatement(sql);
      addParams(statement, identifiers, params);

      rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      int column = 1;
      while (rsmd.getColumnName(column).compareToIgnoreCase(columnName) != 0) {
        column++;
      }
      if (!rs.next()) {
        return null;
      }
      return rs.getString(column);
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (statement != null) {
        statement.close();
      }
    }
  }

  public Set<String> selectOne(Query query,
      Map<String, Object> params, String returnColumn) throws SQLException {
    Set<String> result = new HashSet<String>();
    List<HashMap<String, Object>> rows = select(query, params);
    
    for (HashMap<String, Object> row : rows) {
      result.add((String) row.get(returnColumn));
    }
    return result;
  }
  
  /**
   * Executes select statement in the database
   * @param query to be executed
   * @param params parameter values
   * @return list of maps of columns and their values
   * @throws SQLException
   */
  public List<HashMap<String, Object>>
      select(Query query, Map<String, Object> params) throws SQLException {
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      List<String> identifiers = new ArrayList<String>();
      // function sortParams fills identifiers variable
      String sql = sortParams(query, identifiers);
      statement = connection.prepareStatement(sql);
      addParams(statement, identifiers, params);

      rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      List<HashMap<String, Object>> results =
        new ArrayList<HashMap<String, Object>>();
      while (rs.next()) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        for (int i = 0; i < rsmd.getColumnCount(); ++i) {
          if (rsmd.getColumnType(i + 1) == java.sql.Types.TIMESTAMP) {
            result.put(rsmd.getColumnName(i + 1).toLowerCase(Locale.ENGLISH),
                rs.getTimestamp(rsmd.getColumnName(i + 1)));
          } else {
            result.put(rsmd.getColumnName(i + 1).toLowerCase(Locale.ENGLISH),
                rs.getObject(rsmd.getColumnName(i + 1)));
          }
        }
        results.add(result);
      }
      return results;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (statement != null) {
        statement.close();
      }
    }
  }

  /**
   * Executes a query in the database
   * @param query to be executed
   * @param params parameters to the query
   * @return success state of the query
   * @throws SQLException
   */
  public boolean execute(Query query, Map<String, Object> params)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      List<String> identifiers = new ArrayList<String>();
      String sql = sortParams(query, identifiers);
      statement = connection.prepareStatement(sql);
      addParams(statement, identifiers, params);
      boolean result = statement.execute();
      return result;
    } finally {
      if (statement != null) {
        statement.close();
      }
    }
  }

  /**
   * Executes a batch on list of AdEntities
   * @param query to be executed on each entity
   * @param entities list of entities
   * @throws SQLException
   */
  public void executeBatch(Query query, Set<AdEntity> entities)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      List<String> identifiers = new ArrayList<String>();
      String sql = sortParams(query, identifiers);
      statement = connection.prepareStatement(sql);
  
      int batch = 0;
      for (AdEntity e : entities) {
        addParams(statement, identifiers, e.getSqlParams());
        statement.addBatch();
        if (++batch >= batchHint) {
          statement.executeBatch();
          LOGGER.log(
              Level.FINE, "Batch execution done for SQL [" + sql + "]");
          batch = 0;
        }
      }
      statement.executeBatch();
    } finally {
      if (statement != null) {
        statement.close();
      }
    }
  }

  /**
   * Merges memberships from Active Directory to the database
   * @param entities list of entities whose memberships we should update
   */
  public void mergeMemberships(final Set<AdEntity> entities,
      boolean resolveMemberId)
      throws SQLException {
    for (AdEntity e : entities) {
      if (!e.isGroup()) {
        continue;
      }
      Long groupId = getEntityId(Query.FIND_ENTITY, e.getSqlParams());
      Map<String, Number> dbMemberships = new HashMap<String, Number>();
      for (HashMap<String, Object> dbMembership: 
        select(Query.SELECT_MEMBERSHIPS_BY_DN, e.getSqlParams())) {
        dbMemberships.put((String) dbMembership.get(AdConstants.DB_MEMBERDN),
            (Number) dbMembership.get(AdConstants.DB_MEMBERID));
      }
      Set<AdMembership> adMemberships = e.getMembers();

      if (LOGGER.isLoggable(Level.FINE)) {
        StringBuffer sb = new StringBuffer("For user [").append(e).append(
            "] identified " + dbMemberships.size()
            + " memberships in Database:");
        for (String memberDn : dbMemberships.keySet()) {
          sb.append("[").append(memberDn).append("] ");
        }
        sb.append(" and " + adMemberships.size()
            + " memberships in Active Directory:");
        for (AdMembership adMembership : adMemberships) {
          sb.append("[").append(adMembership.memberDn).append("] ");
        }
        LOGGER.fine(sb.toString());
      }

      PreparedStatement insertStatement = null;
      try {
        List<String> identifiers = new ArrayList<String>();
        insertStatement = connection.prepareStatement(
            sortParams(Query.MERGE_MEMBERSHIP, identifiers));

        int batch = 0;
        for (AdMembership m : adMemberships) {
          Map<String, Object> foreign = m.parseForeignSecurityPrincipal();
          if (foreign != null) {
            m.memberId = getEntityId(Query.FIND_FOREIGN, foreign);
          } else if (resolveMemberId){
            m.memberId = getEntityId(Query.FIND_GROUP, m.getSqlParams());
          }
          // If member is missing from group in the DB or present but has a 
          // null memberId
          if (!dbMemberships.containsKey(m.memberDn) || (m.memberId != null 
              && dbMemberships.get(m.memberDn) == null)) {
            if (!dbMemberships.containsKey(m.memberDn)) {
              LOGGER.finer(
                  "Adding [" + m.memberDn + "] id [ " + m.memberId
                  + "] as member to group [" + e + "]");
            } else {
              LOGGER.finer(
                  "Resolving [" + m.memberDn + "] to id [ " + m.memberId
                  + "] as member of group [" + e + "]");
            }
            Map<String, Object> insParams = new HashMap<String, Object>();
            insParams.put(AdConstants.DB_GROUPID, groupId);
            insParams.put(AdConstants.DB_MEMBERDN, m.memberDn);
            insParams.put(AdConstants.DB_MEMBERID, m.memberId);
            addParams(insertStatement, identifiers, insParams);
            insertStatement.addBatch();
            if (++batch == batchHint) {
              insertStatement.executeBatch();
              batch = 0;
            }
          }
          dbMemberships.remove(m.memberDn);
        }
        insertStatement.executeBatch();
      } finally {
        if (insertStatement != null) {
          insertStatement.close();
        }
      }
      
      // whatever remained in dbMemberships must be removed from DB
      Map<String, Object> params = e.getSqlParams();
      PreparedStatement delStatement = null;
      int batch = 0;
      try {
        List<String> identifiers = new ArrayList<String>();
        delStatement = connection.prepareStatement(sortParams(
            Query.DELETE_MEMBERSHIPS_BY_DN_AND_MEMBERDN, identifiers));
        Map<String, Object> delParams = e.getSqlParams();

        for (String memberDn : dbMemberships.keySet()) {
          LOGGER.finer("Removing [" + memberDn + "] from group [" + e + "]");
          delParams.put(AdConstants.DB_MEMBERDN, memberDn);
          addParams(delStatement, identifiers, delParams);
          delStatement.addBatch();
          if (++batch == batchHint) {
            delStatement.executeBatch();
            batch = 0;
          }
        }
        delStatement.executeBatch();
      } finally {
        if (delStatement != null) {
          delStatement.close();
        }
      }
    }
  }

  public void setBatchHint(int batchHint) {
    LOGGER.info("Setting batch size to [" + batchHint + "]");
    this.batchHint = batchHint;
  }

  /**
   * Checks if the connector names tables has any rows.
   */
  public void deleteConnectorNameInstance(String connectorName)
      throws RepositoryException {
    removeConnectorNameInstance(connectorName);

    if (isConnectorNamesTableEmpty()) {
      dropTable(Query.DROP_MEMBERS_TABLE);
      dropTable(Query.DROP_ENTITIES_TABLE);
      dropTable(Query.DROP_SERVERS_TABLE);
      dropTable(Query.DROP_CONNECTORNAME_TABLE);
    }
  }

  /**
   * Checks if the connector names tables has any rows.
   */
  public void ensureConnectorNameInstanceExists(String connectorName) {
    if (!hasConnectorNameInstance(connectorName)) {
      addConnectorNameInstance(connectorName);
    }
  }

  /**
   * Checks if the connector names tables has any rows.
   */
  private boolean isConnectorNamesTableEmpty() {
    boolean isEmpty;
    try {
      List<HashMap<String, Object>> names =
          select(Query.SELECT_CONNECTORNAME, null);
      isEmpty = (names.size() == 0);
    } catch (Exception e) {
      isEmpty = true;
    }
    return isEmpty;
  }

  /**
   * Adds a connector name to the connector names tables.
   */
  private void addConnectorNameInstance(String connectorName) {
    try {
      HashMap<String, Object> params = new HashMap<String, Object>();
      params.put(AdConstants.DB_CONNECTORNAME, connectorName);
      execute(Query.ADD_CONNECTORNAME, params);
    } catch (Throwable e) {
      LOGGER.log(Level.WARNING,
          "Failed to add connector name to connector names table "
          + "with the query [" + Query.ADD_CONNECTORNAME + "]", e);
    }
  }

  /**
   * Checks if a connector name is in the connector names tables.
   */
  private boolean hasConnectorNameInstance(String connectorName) {
    try {
      List<HashMap<String, Object>> names =
          select(Query.SELECT_CONNECTORNAME, null);
      for (HashMap<String, Object> row : names) {
        if (row.get(AdConstants.DB_CONNECTORNAME).equals(connectorName)) {
          return true;
        }
      }
    } catch (SQLException e) {
      LOGGER.log(Level.WARNING, "Unable to query connector names table "
          + "with the query [" + Query.SELECT_CONNECTORNAME + "]", e);
    }
    return false;
  }

  /**
   * Removes the connector name from the connector names tables.
   */
  private void removeConnectorNameInstance(String connectorName) {
    try {
      HashMap<String, Object> params = new HashMap<String, Object>();
      params.put(AdConstants.DB_CONNECTORNAME, connectorName);
      execute(Query.DELETE_CONNECTORNAME, params);
    } catch (Throwable e) {
      LOGGER.log(Level.WARNING,
          "Failed to remove connector name from connector names table "
          + "with the query [" + Query.DELETE_CONNECTORNAME + "]", e);
    }
  }

  /**
   * Removes a table from the database.
   */
  private void dropTable(Query query) throws RepositoryException {
    try {
      execute(query, null);
    } catch (Throwable e) {
      throw new RepositoryException(
          "Failed to remove the table with the query [" + query + "]", e);
    }
    LOGGER.info(
        "Sucessfully removed the table from the database using the query ["
        + query + "]");
  }
}
