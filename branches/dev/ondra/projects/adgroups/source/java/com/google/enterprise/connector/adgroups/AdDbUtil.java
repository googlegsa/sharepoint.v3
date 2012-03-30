// Copyright 2012 Google Inc. All Rights Reserved.

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
    CREATE_SERVERS("CREATE_SERVERS"),
    CREATE_ENTITIES("CREATE_ENTITIES"),
    CREATE_MEMBERS("CREATE_MEMBERS"),
    SELECT_SERVER("SELECT_SERVER"),
    UPDATE_SERVER("UPDATE_SERVER"),
    MERGE_ENTITIES("MERGE_ENTITIES"),
    DELETE_MEMBERSHIPS("DELETE_MEMBERSHIPS"),
    ADD_MEMBERSHIPS("ADD_MEMBERSHIPS"),
    MATCH_ENTITIES("MATCH_ENTITIES"),
    RESOLVE_PRIMARY_GROUP("RESOLVE_PRIMARY_GROUPS"),
    RESOLVE_FOREIGN_SECURITY_PRINCIPALS("RESOLVE_FOREIGN_SECURITY_PRINCIPALS"),
    SELECT_USER_BY_SAMACCOUNTNAME("SELECT_USER_BY_SAMACCOUNTNAME"),
    SELECT_USER_BY_NETBIOS_SAMACCOUNTNAME
        ("SELECT_USER_BY_NETBIOS_SAMACCOUNTNAME"),
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

  //TODO: load table names from bean
  private HashMap<String, String> tables = new HashMap<String, String>() {{
    put("servers", "servers");
    put("entities", "entities");
    put("members", "members");
  }};

  private ResourceBundle queries;

  public AdDbUtil(DataSource dataSource) {
    //TODO: load locale for SQL queries from bean
    queries =
      ResourceBundle.getBundle(getClass().getPackage().getName() + ".sql");

    this.dataSource = dataSource;
    try {
      //TODO: do not execute any DDL if database is not H2
      connection = dataSource.getConnection();
      connection.setAutoCommit(false);
      execute(Query.CREATE_SERVERS, null);
      execute(Query.CREATE_ENTITIES, null);
      execute(Query.CREATE_MEMBERS, null);
    } catch (SQLException e) {
      LOGGER.log(
          Level.SEVERE, "Cannot establish connection to the database.", e);
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
            if (i < sql.length() + 1 && sql.charAt(i+1) == quote) {
              ++i;
            } else {
              break;
            }
          }
          ++i;
        }
      } else if (sql.charAt(i) == AdConstants.COLON_CHAR) {
        if (i < sql.length() + 1 &&
              Character.isJavaIdentifierStart(sql.charAt(i+1))) {
          int start = ++i;
          while (i < sql.length() &&
              Character.isJavaIdentifierPart(sql.charAt(i))) {
            ++i;
          }

          String identifier = sql.substring(start, i);
          // replace current identifier with ? or table name
          if (tables.containsKey(identifier)) {
            finalSql.append(tables.get(identifier));
          } else {
            finalSql.append("?");
            identifiers.add(identifier);
          }
          // skip placeholder name
          current = i;
        }
      }
      // add to final string whatever we already scanned over
      finalSql.append(sql.substring(current, i+1));
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
    for (String identifier: identifiers) {
      statement.setObject(identifiers.indexOf(identifier) + 1,
        params.get(identifier));
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
      List<String> identifiers = new ArrayList<String>();
      // function sortParams fills identifiers variable
      String sql = sortParams(query, identifiers);
      PreparedStatement statement = connection.prepareStatement(sql);
      addParams(statement, identifiers, params);

      ResultSet rs = statement.executeQuery();
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
    List<String> identifiers = new ArrayList<String>();
    String sql = sortParams(query, identifiers);
    PreparedStatement statement = connection.prepareStatement(sql);
    addParams(statement, identifiers, params);
    return statement.execute();
  }

  /**
   * Executes a batch on list of AdEntities
   * @param query to be executed on each entity
   * @param entities list of entities
   * @throws SQLException
   */
  public void executeBatch(Query query, List<AdEntity> entities)
      throws SQLException {
    List<String> identifiers = new ArrayList<String>();
    String sql = sortParams(query, identifiers);
    PreparedStatement statement = connection.prepareStatement(sql);

    for (AdEntity e : entities) {
      addParams(statement, identifiers, e.getSqlParams());
      statement.addBatch();
    }
    statement.executeBatch();
  }

  /**
   * Query to execute
   * @param remove query to remove all memberships from the database
   * @param insert insert query to inse
   * @param entities list of entities who's memberships we should run the
   *        insert query on
   * @return it membership merging was successful
   */
  public void mergeMemberships(
      final Query remove, final Query insert, final List<AdEntity> entities)
      throws SQLException {
    executeBatch(remove, entities);

    List<String> addIdentifiers = new ArrayList<String>();
    String insertSql = sortParams(insert, addIdentifiers);
    PreparedStatement addStatement = connection.prepareStatement(insertSql);

    for (AdEntity e: entities) {
      for (String s: e.getMembers()) {
        Map<String, Object> params = e.getSqlParams();
        params.put("memberdn", s);
        addParams(addStatement, addIdentifiers, params);
        addStatement.addBatch();
      }
    }

    addStatement.executeBatch();
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
}
