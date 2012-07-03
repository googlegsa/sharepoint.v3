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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class AdDbUtil {
  private static final Logger LOGGER =
    Logger.getLogger(AdDbUtil.class.getName());

  public enum Query {
    CREATE_SERVERS_SEQUENCE("CREATE_SERVERS_SEQUENCE"),
    CREATE_SERVERS("CREATE_SERVERS"),
    CREATE_ENTITIES_SEQUENCE("CREATE_ENTITIES_SEQUENCE"),
    CREATE_ENTITIES("CREATE_ENTITIES"),
    CREATE_MEMBERS_SEQUENCE("CREATE_MEMBERS_SEQUENCE"),
    CREATE_MEMBERS("CREATE_MEMBERS"),
    CLEAN_MEMBERS("CLEAN_MEMBERS"),
    CLEAN_FOREIGN_MEMBERS("CLEAN_FOREIGN_MEMBERS"),
    CLEAN_ENTITIES("CLEAN_ENTITIES"),
    SELECT_SERVER("SELECT_SERVER"),
    UPDATE_SERVER("UPDATE_SERVER"),
    MERGE_ENTITIES("MERGE_ENTITIES"),
    DELETE_MEMBERSHIPS("DELETE_MEMBERSHIPS"),
    ADD_MEMBERSHIPS("ADD_MEMBERSHIPS"),
    MATCH_ENTITIES("MATCH_ENTITIES"),
    RESOLVE_PRIMARY_GROUP("RESOLVE_PRIMARY_GROUPS"),
    RESOLVE_FOREIGN_SECURITY_PRINCIPALS("RESOLVE_FOREIGN_SECURITY_PRINCIPALS"),
    SELECT_USER_BY_SAMACCOUNTNAME("SELECT_USER_BY_SAMACCOUNTNAME"),
    SELECT_USER_BY_DOMAIN_SAMACCOUNTNAME
        ("SELECT_USER_BY_DOMAIN_SAMACCOUNTNAME"),
    SELECT_WELLKNOWN_MEMBERSHIPS("SELECT_WELLKNOWN_MEMBERSHIPS"),
    SELECT_MEMBERSHIPS_BY_ENTITYID("SELECT_MEMBERSHIPS_BY_ENTITYID");

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
  private int batchHint = 1000;

  //TODO: load table names from bean
  private Map<String, String> tables = new HashMap<String, String>() {{
    put("servers", "servers");
    put("entities", "entities");
    put("members", "members");
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
      execute(Query.CREATE_SERVERS_SEQUENCE, null);
      execute(Query.CREATE_SERVERS, null);
      execute(Query.CREATE_ENTITIES_SEQUENCE, null);
      execute(Query.CREATE_ENTITIES, null);
      execute(Query.CREATE_MEMBERS_SEQUENCE, null);
      execute(Query.CREATE_MEMBERS, null);
      commit();
      connection.setAutoCommit(false);
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
          result.put(rsmd.getColumnName(i + 1).toLowerCase(Locale.ENGLISH),
              rs.getObject(rsmd.getColumnName(i + 1)));
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
  public void executeBatch(Query query, List<AdEntity> entities)
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
        if (++batch == batchHint) {
          statement.executeBatch();
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
   * Query to execute
   * @param remove query to remove all memberships from the database
   * @param insert insert query to insert memberships into the database
   * @param entities list of entities whose memberships we should run the
   *        insert query on
   */
  public void mergeMemberships(
      final Query remove, final Query insert, final List<AdEntity> entities)
      throws SQLException {
    executeBatch(remove, entities);
    
    PreparedStatement addStatement = null;
    try {
      List<String> addIdentifiers = new ArrayList<String>();
      String insertSql = sortParams(insert, addIdentifiers);
      addStatement = connection.prepareStatement(insertSql);
  
      int batch = 0;
      for (AdEntity e: entities) {
        for (String s: e.getMembers()) {
          Map<String, Object> params = e.getSqlParams();
          params.put("memberdn", s);
          addParams(addStatement, addIdentifiers, params);
          addStatement.addBatch();
          if (++batch == batchHint) {
            addStatement.executeBatch();
            batch = 0;
          }
        }
      }
      addStatement.executeBatch();
    } finally {
      if (addStatement != null) {
        addStatement.close();
      }
    }
  }

  /**
   * Execute commit of the current transaction
   */
  public void commit() {
    try {
      connection.commit();
    } catch (SQLException e) {
      LOGGER.warning("Commit failed "  + e.getMessage() + e.getStackTrace());
    }
  }

  /**
   * Execute rollback of the current transaction
   */
  public void rollback() {
    try {
      connection.rollback();
    } catch (SQLException e) {
      LOGGER.warning("Rollback failed "  + e.getMessage() + e.getStackTrace());
    }
  }

  public void setBatchHint(int batchHint) {
    this.batchHint = batchHint;
  }
}
