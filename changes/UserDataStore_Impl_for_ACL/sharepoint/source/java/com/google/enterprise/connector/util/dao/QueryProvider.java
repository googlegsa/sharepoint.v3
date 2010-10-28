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

package com.google.enterprise.connector.util.dao;

import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.spi.SpiConstants;

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
     * Initializes the QueryProvider to serve SQL queries. ConnectorNames are
     * used to ensure that that every connector possess its own schema. XXX
     * Essence of this constraint may be re-thought in future.
     *
     * @param connectorName Connector that will be using this QueryProvider
     * @param vendor specifies the vendor for which the queries will be provided
     * @param attr specifies additional attributes that should be considered
     *            along with vendor name while loading the queries
     * @throws SharepointException
     */
    void init(String vendor, String... attr)
            throws SharepointException;

    /**
     * Returns the actual SQL query that can be executed
     *
     * @param key
     * @return
     */
    String getSqlQuery(Query query);

    /**
     * The database to be used
     *
     * @return
     */
    public String getDatabase();

    public String getDocTableName();
}

enum Query {
    DOC_STORE_CREATE(),

    DOC_STORE_INSERT(
            SpiConstants.PERSISTABLE_ATTRIBUTES.values().toArray(new String[0])) {
        public void hello() {
        }
    },

    DOC_STORE_SELECT_ALL(),

    DOC_STORE_SELECT_ON_DOCID(SpiConstants.PROPNAME_DOCID),

    DOC_STORE_SELECT_ALL_FROM_DOCID(SpiConstants.PROPNAME_DOCID);

    String[] parameters;

    Query(String... parameters) {
        this.parameters = parameters;
    }

    /**
     * Creates a name-value map so that can be used to execute the query
     *
     * @param values
     * @return {@link MapSqlParameterSource}
     */
    public SqlParameterSource createParameter(Object... values) {
        check(values);
        MapSqlParameterSource namedParam = new MapSqlParameterSource();
        int i = 0;
        for (String placeholder : parameters) {
            Object value = values[i++];
            String strValue = (null == value) ? null : value.toString();
            namedParam.addValue(placeholder, strValue);
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
                    + parameters.length
                    + " ] is not equal to the passed-in values " + param.length);
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
