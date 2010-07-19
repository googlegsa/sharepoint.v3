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
 * Query Builder for {@link UserDataStoreDAO}
 *
 * @author nitendra_thakur
 */
public class UserDataStoreQueryBuilder implements QueryBuilder {
    // The encoding scheme to be used in the table
    private static final String characterSet = "UTF8";

    // names used to indicate the columns when the values are passed to a query.
    // Typically used for constructing named parameters, to avoid traditional ?
    // in prepared statements. Hence, these should not be confused with the
    // actual names of the column which are passed at run time
    public static final String COLUMNUSER = "user";
    public static final String COLUMNGROUP = "group";
    public static final String COLUMNNAMESPACE = "namespace";

    // Various literals used for constructing queries. These all are just names
    // and not the actual entities
    private String database;
    private String table;
    private String user;
    private String group;
    public String namespace;

    public UserDataStoreQueryBuilder(String database, String table,
            String user,
            String group, String namespace) {
        super();
        this.database = database;
        this.table = table;
        this.user = user;
        this.group = group;
        this.namespace = namespace;
    }

    public Query createQuery(QueryType type)
            throws SharepointException {
        Query udsQuery = new Query();
        udsQuery.setQueryType(type);
        switch (type) {
        case CREATEDBQUERY:
            udsQuery.setQuery("CREATE DATABASE " + database);
            break;

            case CREATETABLEQUERY:
            udsQuery.setQuery("CREATE TABLE " + table + " ( " + user
                    + " varchar(40) NOT NULL, " + group
                    + " varchar(40) NOT NULL, " + namespace
                    + " varchar(200) NOT NULL, PRIMARY KEY(" + user + ","
                    + group + "," + namespace + ") )  CHARACTER SET "
                    + characterSet);
            break;

            case SELECTQUERYFORUSER:
            udsQuery.setQuery("SELECT " + user + ", " + group + ", "
                    + namespace + " FROM " + table + " WHERE " + user
                    + " LIKE :" + COLUMNUSER);
            break;

            case INSERTQUERY:
            udsQuery.setQuery("INSERT INTO " + table + " VALUES(:" + COLUMNUSER
                    + ",:" + COLUMNGROUP + ",:" + COLUMNNAMESPACE + ")");
            break;

            case DELETE_QUERY_FOR_USER_NAMESPACE:
            udsQuery.setQuery("DELETE FROM " + table + " WHERE " + user
                    + " LIKE :" + COLUMNUSER + " AND " + namespace + "=:"
                    + COLUMNNAMESPACE);
            break;

            case DELETE_QUERY_FOR_GROUP_NAMESPACE:
            udsQuery.setQuery("DELETE FROM " + table + " WHERE " + group
                    + " LIKE :" + COLUMNGROUP + " AND " + namespace + "=:"
                    + COLUMNNAMESPACE);
            break;

            case DELETE_QUERY_FOR_NAMESPACE:
            udsQuery.setQuery("DELETE FROM " + table + " WHERE " + namespace
                    + "=:" + COLUMNNAMESPACE);
            break;

            default:
            throw new SharepointException("Query not supported!! ");
        }
        return udsQuery;
    }

    public void addSuffix(String suffix) {
        this.table += "_" + suffix;

    }
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String[] getTables() {
        return new String[] { table };
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
