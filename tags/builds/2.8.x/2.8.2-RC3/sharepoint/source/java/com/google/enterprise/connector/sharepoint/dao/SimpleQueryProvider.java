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

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Provides basic implementation for all the operations related to accessing
 * queries. Uses Java's Locale/ResourceBundle concept to load vendor specific
 * SQL queries from {@literal sqlQueries.properties}. This class can be safely
 * extended to override any of the functionality e.g loading of queries,
 * construction of queries etc.
 *
 * @author nitendra_thakur
 */
public class SimpleQueryProvider implements QueryProvider {

  private ResourceBundle sqlQueries;
  private String basename;

  // Required for constructing User Data Store queries
  private String udsTableName;
  private String udsIndexName;

  // Queries that can be served
  private Map<Query, String> sqlQueryMap = new HashMap<Query, String>();

  private String database;

  public SimpleQueryProvider(String basename) {
    this.basename = basename;
  }

  public String getSqlQuery(Query query) {
    return sqlQueryMap.get(query);
  }

  /**
   * {@inheritDoc} Constructs a {@link Locale} for the given vendor/attributes
   * and loads the corresponding {@literal sqlQueries.properties}. For all the
   * queries registered in {@link Query}, reads the query string and resolves
   * all the placeholders. In fact, only entities and attribute names are
   * resolved here; resolution of the parameter names are delegated to the
   * queries themselves.
   *
   * @see {@code Query#getParameterPlaceholders()}
   * @param connectorName Connector that will be using this QueryProvider. This
   *          is appended to all entity/attribute names
   * @param vendor specifies the vendor for which the queries will be provided
   * @param attr specifies additional attributes that should be considered along
   *          with vendor name while loading the queries. At max three
   *          attributes are allowed. Anything more than that will be ignored
   * @throws SharepointException
   */
  public void init(String vendor, String... attr) throws SharepointException {
    Locale locale = null;
    if (attr.length == 0) {
      locale = new Locale(vendor);
    } else if (attr.length == 1) {
      locale = new Locale(vendor, attr[0]);
    } else if (attr.length == 2) {
      locale = new Locale(vendor, attr[0], attr[1]);
    }

    try {
      sqlQueries = ResourceBundle.getBundle(basename, locale);
    } catch (Exception e) {
      throw new SharepointException(
          "Could not load sqlQueries.properties for locale [ " + locale + " ] ");
    }

    for (Query query : Query.values()) {
      registerQuery(query);
    }
  }

  /**
   * Iterate over each {@code Query} that connector needs to support. Construct
   * the actual SQL query for each of them and register the query in a local map
   * that will be used for serving the queries.
   *
   * @param query
   */
  protected void registerQuery(Query query) {
    String sqlQuery = null;
    List<String> placeholders = query.getParameterPlaceholders();
    switch (query) {
    case UDS_CREATE_TABLE:
    case UDS_DROP_TABLE:

    case UDS_INSERT:
    case UDS_SELECT_FOR_USERNAME:

    case UDS_SELECT_FOR_USERID_NAMESPACE:
    case UDS_DELETE_FOR_USERID_NAMESPACE:

    case UDS_SELECT_FOR_GROUPID_NAMESPACE:
    case UDS_DELETE_FOR_GROUPID_NAMESPACE:

    case UDS_SELECT_FOR_NAMESPACE:
    case UDS_DELETE_FOR_NAMESPACE:
      placeholders.add(0, udsTableName);
      break;
    case UDS_CREATE_INDEX:
      placeholders.add(0, udsIndexName);
      placeholders.add(1, udsTableName);
      break;
    case UDS_CHECK_TABLES:
      placeholders.add(0, SPConstants.UDS_TABLE);
      break;
    case UDS_SELECT_FOR_ADGROUPS:
      placeholders.add(0, udsTableName);
    }
    sqlQuery = MessageFormat.format(sqlQueries.getString(query.name()), placeholders.toArray());
    sqlQueryMap.put(query, sqlQuery);
  }

  // XXX This is temporary
  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public String getUdsTableName() {
    return udsTableName;
  }

  public void setUdsTableName(String udsTableName) {
    this.udsTableName = udsTableName;
  }

  public String getUdsIndexName() {
    return udsIndexName;
  }

  public void setUdsIndexName(String udsIndexName) {
    this.udsIndexName = udsIndexName;
  }

}
