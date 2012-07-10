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
    TEST_ENTITIES("TEST_ENTITIES"),
    CREATE_ENTITIES_SEQUENCE("CREATE_ENTITIES_SEQUENCE"),
    CREATE_ENTITIES("CREATE_ENTITIES"),
    TEST_MEMBERS("TEST_MEMBERS"),
    CREATE_MEMBERS_SEQUENCE("CREATE_MEMBERS_SEQUENCE"),
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
    SELECT_WELLKNOWN_MEMBERSHIPS("SELECT_WELLKNOWN_MEMBERSHIPS"),
    SELECT_MEMBERSHIPS_BY_ENTITYID("SELECT_MEMBERSHIPS_BY_ENTITYID"),
    SELECT_MEMBERSHIPS_BY_DN("SELECT_MEMBERSHIPS_BY_DN"),
    DELETE_MEMBERSHIPS_BY_DN_AND_MEMBERDN
        ("DELETE_MEMBERSHIPS_BY_DN_AND_MEMBERDN"),
    SELECT_ALL_ENTITIES_BY_SID("SELECT_ALL_ENTITIES_BY_SID");

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
   * Merges memberships from Active Directory to the database
   * @param entities list of entities whose memberships we should update
   */
  public void mergeMemberships(final Set<AdEntity> entities)
      throws SQLException {
    for (AdEntity e : entities) {
      Set<String> dbMemberships = new HashSet<String>();
      for (HashMap<String, Object> dbMembership: 
        select(Query.SELECT_MEMBERSHIPS_BY_DN, e.getSqlParams())) {
        dbMemberships.add((String) dbMembership.get(AdConstants.DB_MEMBERDN));
      }
      Set<String> adMemberships = e.getMembers();
      
      if (LOGGER.isLoggable(Level.FINE)) {
        StringBuffer sb = new StringBuffer("For user [").append(e).append(
            "] identified "+ dbMemberships.size() +" memberships in Database:");
        for (String dbMembership : dbMemberships) {
          sb.append("[").append(dbMembership).append("] ");
        }
        sb.append(" and " + adMemberships.size()
            + " memberships in Active Directory:");
        for (String adMembership : adMemberships) {
          sb.append("[").append(adMembership).append("] ");
        }
        LOGGER.fine(sb.toString());
      }

      PreparedStatement insertStatement = null;
      try {
        List<String> identifiers = new ArrayList<String>();
        insertStatement = connection.prepareStatement(
            sortParams(Query.ADD_MEMBERSHIPS, identifiers));
  
        int batch = 0;
        for (String s: adMemberships) {
          if (!dbMemberships.contains(s)) {
            LOGGER.finer("Adding [" + s + "] as member to group [" + e + "]");
            Map<String, Object> addParams = e.getSqlParams();
            addParams.put(AdConstants.DB_MEMBERDN, s);
            addParams(insertStatement, identifiers, addParams);
            insertStatement.addBatch();
            if (++batch == batchHint) {
              insertStatement.executeBatch();
              batch = 0;
            }
          }
          dbMemberships.remove(s);
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

        for (String s : dbMemberships) {
          LOGGER.finer("Removing [" + s + "] from group [" + e + "]");
          delParams.put(AdConstants.DB_MEMBERDN, s);
          addParams(delStatement, identifiers, delParams);
          delStatement.addBatch();
          if (++batch == batchHint) {
            delStatement.executeBatch();
            batch = 0;
          }
        }
      } finally {
        if (delStatement != null) {
          delStatement.close();
        }
      }
    }
  }

  public void setBatchHint(int batchHint) {
    this.batchHint = batchHint;
  }
}
