//Copyright 2010 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.dao;

import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.LinkedList;
import java.util.List;

/**
 * Provides the actual SQL queries for execution for all the queries (
 * {@link Query}) registered with the connector. This mainly includes loading of
 * queries from {@literal sqlQueries.properties} and resolving all the
 * placeholders to construct the final executable SQL query.
 *
 * @author nitendra_thakur
 */
public interface QueryProvider {

  /**
   * Initializes the QueryProvider to serve SQL queries. ConnectorNames are used
   * to ensure that that every connector possess its own schema. XXX Essence of
   * this constraint may be re-thought in future.
   *
   * @param connectorName Connector that will be using this QueryProvider
   * @param vendor specifies the vendor for which the queries will be provided
   * @param attr specifies additional attributes that should be considered along
   *          with vendor name while loading the queries
   * @throws SharepointException
   */
  void init(String vendor, String... attr) throws SharepointException;

  /**
   * Returns the actual SQL query that can be executed
   *
   * @param query
   * @return
   */
  String getSqlQuery(Query query);

  /**
   * The database to be used
   *
   * @return
   */
  String getDatabase();

  void setDatabase(String database);

  /**
   * Name of the table representing User Group Memberships
   *
   * @return
   */
  String getUdsTableName();

  /**
   * Name of the index to be created in for user data store. Currently, there is
   * only one such index.
   *
   * @return
   */
  String getUdsIndexName();

  String getCnTableName();
}

/**
 * Enumerates all the queries used by connector. The values are keys which
 * identifies a query in {@literal sqlQueries.properties}. To support a new
 * query, the query must be added here along with {@literal
 * sqlQueries.properties}. Actual SQL queries are constructed using these
 * values.
 * <p/>
 * Notes to Programmers: While specifying the placeholders, only consider the
 * parameter values which will be passes during batch execution. Entities and
 * Attributes required ion the query must not be mentioned as placeholders.
 *
 * @see {@code SimpleQueryProvider#registerQuery(Query)}
 *      <p/>
 * @author nitendra_thakur
 */
enum Query {
  UDS_CREATE_TABLE, UDS_CREATE_INDEX, UDS_DROP_TABLE, UDS_CHECK_TABLES, UDS_SELECT_FOR_ADGROUPS(
      "groups"),

  UDS_INSERT("user_id", "user_name", "group_id", "group_name", "namespace"), UDS_SELECT_FOR_USERNAME(
      "user_name"),

  UDS_SELECT_FOR_USERID_NAMESPACE("user_id", "namespace"), UDS_DELETE_FOR_USERID_NAMESPACE(
      "user_id", "namespace"),

  UDS_SELECT_FOR_GROUPID_NAMESPACE("group_id", "namespace"), UDS_DELETE_FOR_GROUPID_NAMESPACE(
      "group_id", "namespace"),

  UDS_SELECT_FOR_NAMESPACE("namespace"), UDS_DELETE_FOR_NAMESPACE("namespace"), CN_CREATE_TABLE, CN_INSERT(
      "connectorname"), CN_SELECT, CN_DELETE("connectorname"), CN_DROP_TABLE;

  String[] parameters;

  Query(String... parameters) {
    this.parameters = parameters;
  }

  /**
   * Creates a name-value map to that can be used to execute the query
   *
   * @param values
   * @return {@link MapSqlParameterSource}
   */
  public SqlParameterSource createParameter(Object... values) {
    check(values);
    MapSqlParameterSource namedParam = new MapSqlParameterSource();
    int i = 0;
    for (String placeholder : parameters) {
      namedParam.addValue(placeholder, values[i++]);
    }
    return namedParam;
  }

  /**
   * Checks if the no. of passed-in values is equal to the parameters that the
   * query uses
   */
  private void check(Object... param) {
    if (null == parameters && param.length == 0) {
      return;
    }
    if (param.length != parameters.length) {
      throw new IllegalArgumentException("No. of expected parameters "
          + parameters.length + " ] is not equal to the passed-in values "
          + param.length);
    }
  }

  /**
   * Creates placeholder names that should be used while construction of the
   * actual SQL query
   *
   * @return
   */
  public List<String> getParameterPlaceholders() {
    List<String> placeholders = new LinkedList<String>();
    for (String parameter : parameters) {
      placeholders.add(":" + parameter);
    }
    return placeholders;
  }
}
