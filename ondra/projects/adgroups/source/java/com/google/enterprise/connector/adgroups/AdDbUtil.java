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
import java.util.logging.Logger;

import javax.sql.DataSource;

public class AdDbUtil {
  private static final Logger LOGGER = Logger.getLogger(AdDbUtil.class.getName());
  
  private DataSource dataSource;
  private Connection connection;
  
  private String serversTable = "servers"; 
  private String entitiesTable = "entities";
  private String membersTable = "members";
  private String workersTable = "workers";

  public AdDbUtil(DataSource dataSource) {
    this.dataSource = dataSource;
    try {
      connection = dataSource.getConnection();
      connection.setAutoCommit(false);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  public String sortParams(final String sql, List<String> identifiers) {
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
      } else if (sql.charAt(i) == ':') {
        if (i < sql.length() + 1 && Character.isJavaIdentifierStart(sql.charAt(i+1))) {
          int start = ++i;
          while (i < sql.length() && Character.isJavaIdentifierPart(sql.charAt(i))) {
            ++i;
          }
          
          String identifier = sql.substring(start, i);
          if (identifiers.indexOf(identifier) == -1) {
            identifiers.add(identifier);
          }
          
          // replace current identifier with ?
          finalSql.append("?");
          // skip placeholder name
          current = i;
        }
      }
      
      // add to final string whatever we already scanned over 
      finalSql.append(sql.substring(current, i+1));
    }
    
    return finalSql.toString();
  }
  
  public void addParams(PreparedStatement statement, List<String> identifiers, Map<String, Object> params) throws SQLException {
    for (String identifier: identifiers) {
      statement.setObject(identifiers.indexOf(identifier) + 1, params.get(identifier));
    }
    
    if (identifiers.contains("servers")) {
      statement.setObject(identifiers.indexOf("servers") + 1, serversTable);
    } 
    
    if (identifiers.contains("entities")) {
      statement.setObject(identifiers.indexOf("entities") + 1, entitiesTable);
    }
    
    if (identifiers.contains("members")) {
      statement.setObject(identifiers.indexOf("members") + 1, membersTable);
    }
    
    if (identifiers.contains("workers")) {
      statement.setObject(identifiers.indexOf("workers") + 1, workersTable);
    }
  }
  
  public List<HashMap<String, Object>> select(String sql, Map<String, Object> params) throws SQLException {
      List<String> identifiers = new ArrayList<String>();
      // function sortParams fills paramsOrder variable
      String finalSql = sortParams(sql, identifiers);
      PreparedStatement statement = connection.prepareStatement(finalSql);
      addParams(statement, identifiers, params);
      
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
      while (rs.next()) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        for (int i = 0; i < rsmd.getColumnCount(); ++i) {
          result.put(rsmd.getColumnName(i + 1).toLowerCase(Locale.ENGLISH), rs.getObject(
              rsmd.getColumnName(i + 1)));
        }
        results.add(result);
      }
      
      return results;
  }
  
  public boolean execute(String sql, Map<String, Object> params) throws SQLException {
    List<String> identifiers = new ArrayList<String>();
    String finalSql = sortParams(sql, identifiers);
    PreparedStatement statement = connection.prepareStatement(finalSql);
    addParams(statement, identifiers, params);
    return statement.execute();
  }
  
  public void mergeEntities(String sql, List<AdEntity> entities) throws SQLException {
    List<String> identifiers = new ArrayList<String>();
    String finalSql = sortParams(sql, identifiers);
    PreparedStatement statement = connection.prepareStatement(finalSql);

    int count = 0;
    for (AdEntity e : entities) {
      addParams(statement, identifiers, e.getSqlParams());
      //statement.addBatch();
      statement.execute();
/*      if (count % 100 == 0) {
        statement.executeBatch();
        statement.clearBatch();
      }
      count++;*/
    }
    //statement.executeBatch();
    LOGGER.info("merging entities finished");
    
  }
  
  public boolean mergeMemberships(final String removeSql, final String addSql, final List<AdEntity> entities) {
    try {
      List<String> removeIdentifiers = new ArrayList<String>();
      String finalRemoveSql = sortParams(removeSql, removeIdentifiers);
      PreparedStatement removeStatement = connection.prepareStatement(finalRemoveSql);
      
      for (AdEntity e: entities) {
        addParams(removeStatement, removeIdentifiers, e.getSqlParams());
        removeStatement.addBatch();
      }
      removeStatement.executeBatch();
      
      List<String> addIdentifiers = new ArrayList<String>();
      String finalAddSql = sortParams(addSql, addIdentifiers);
      PreparedStatement addStatement = connection.prepareStatement(finalAddSql);
      
      for (AdEntity e: entities) {
        for (String s: e.members) {
          Map<String, Object> params = e.getSqlParams();
          params.put("memberdn", s);
          addParams(addStatement, addIdentifiers, params);
          addStatement.addBatch();
        }
      }
      
      addStatement.executeBatch();
      
      return true;
    } catch (SQLException e) {
      LOGGER.warning("Entity membership merging failed. "  + e.getMessage() + e.getStackTrace());
      return false;
    }
  }
  
  public void commit() {
    try {
      connection.commit();
    } catch (SQLException e) {
      LOGGER.warning("Commit failed "  + e.getMessage() + e.getStackTrace());
    }
  }
  
  public void rollback() {
    try {
      connection.rollback();
    } catch (SQLException e) {
      LOGGER.warning("Rollback failed "  + e.getMessage() + e.getStackTrace());
    }
  }
}
