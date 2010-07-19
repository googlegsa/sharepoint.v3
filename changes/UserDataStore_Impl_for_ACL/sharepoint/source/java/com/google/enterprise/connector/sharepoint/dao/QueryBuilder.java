//Copyright 2009 Google Inc.
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

/**
 * An object of type QueryBuilder has a sole responsibility of creating queries.
 * Different modules interacting with the database may exhibit different
 * QueryBuilders as per their need
 *
 * @author nitendra_thakur
 */
public interface QueryBuilder {
    /**
     * enumerates the type of all the queries that the QueryBuilder supports
     * supported. For now, registering a new QueryType, at run time is not
     * supported because there is no strong need of that. The newly supported
     * query type has to be added here at the time of coding.
     *
     * @author nitendra_thakur
     */
    enum QueryType {
        CREATEDBQUERY, CREATETABLEQUERY, SELECTQUERYFORUSER, INSERTQUERY, DELETE_QUERY_FOR_USER_NAMESPACE, DELETE_QUERY_FOR_GROUP_NAMESPACE, DELETE_QUERY_FOR_NAMESPACE
    }

    /**
     * Represents a SQL query
     *
     * @author nitendra_thakur
     */
    class Query {
        // actual SQL query
        private String query;

        // type of query
        private QueryType queryType;

        public String getQuery() {
            return query;
        }

        public QueryType getQueryName() {
            return queryType;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public void setQueryType(QueryType queryType) {
            this.queryType = queryType;
        }
    }

    /**
     * Creates the {@link Query} object of the type specified
     *
     * @param type
     * @return
     * @throws SharepointException
     */
    Query createQuery(QueryType type) throws SharepointException;

    // Adds the suffix into the table name whenever the table name is used in the query.
    // A suffix can be specified after initialization.
    void addSuffix(String suffix);

    // XXX This, actually, should not be a part of the QueryBuilder. It's been
    // kept
    // here just because the way database info is passed to the connector and
    // the way database is created is still in dark. At the end, this API should
    // be removed from this interface.
    String getDatabase();

    /**
     * Get all tables that the query builder uses
     *
     * @return
     */
    String[] getTables();
}
