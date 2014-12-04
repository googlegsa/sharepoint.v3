// Copyright 2010 Google Inc.
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

package com.google.enterprise.connector.sharepoint.dao;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.LinkedList;
import java.util.List;

/**
 * Enumerates all the queries used by connector. The values are keys which
 * identifies a query in {@literal sqlQueries.properties}. To support a new
 * query, the query must be added here along with {@literal
 * sqlQueries.properties}. Actual SQL queries are constructed using these
 * values.
 * <p/>
 * Notes to Programmers: While specifying the placeholders, only consider the
 * parameter values which will be passes during batch execution. Entities and
 * Attributes required in the query must not be mentioned as placeholders.
 *
 * @see QueryProvider#registerQuery(Query)
 */
enum Query {
  UDS_CREATE_TABLE, UDS_CREATE_INDEX, UDS_DROP_TABLE, UDS_CHECK_TABLES,
  UDS_SELECT_FOR_ADGROUPS, // groups param is substituted by UserDataStoreDAO.
  UDS_UPGRADE_COL_USERNAME, UDS_UPGRADE_COL_GROUPNAME,

  UDS_INSERT("user_id", "user_name", "group_id", "group_name", "namespace"),

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
   * Creates a name-value map that can be used to execute a query.
   *
   * @param values the values for the parameters
   * @return a {@link SqlParameterSource}
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
   * Creates placeholder names that should be used while constructing the
   * actual SQL query.
   */
  public List<String> getParameterPlaceholders() {
    List<String> placeholders = new LinkedList<String>();
    for (String parameter : parameters) {
      placeholders.add(":" + parameter);
    }
    return placeholders;
  }
}
