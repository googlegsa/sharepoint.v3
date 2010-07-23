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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

/**
 * Query Builder for {@link UserDataStoreDAO}
 *
 * @author nitendra_thakur
 */
public class UserDataStoreQueryBuilder implements QueryBuilder {
    // names used to indicate the columns when the values are passed to a query.
    // Typically used for constructing named parameters, to avoid traditional ?
    // in prepared statements. Hence, these should not be confused with the
    // actual names of the column which are passed at run time
    public static final String COLUMNUSER = "SPUser";
    public static final String COLUMNGROUP = "SPGroup";
    public static final String COLUMNNAMESPACE = "NameSpace";

    private String table;
    Properties sqlQueries;

    public UserDataStoreQueryBuilder(File fileSqlQueries, String table)
            throws SharepointException {
        super();
        if (null == fileSqlQueries) {
            throw new SharepointException(
                    "No sqlQueries.properties file specified! ");
        }
        if (table == null || table.trim().length() == 0) {
            throw new SharepointException("Invalid table name! ");
        }
        this.table = table;
        try {
            sqlQueries = new Properties();
            InputStream inQuery = new FileInputStream(fileSqlQueries);
            sqlQueries.load(inQuery);
        } catch (Exception e) {
            throw new SharepointException(
                    "Could not load sqlQueries.properties file from "
                            + fileSqlQueries.getAbsolutePath());
        }
    }

    public Query createQuery(QueryType type) throws SharepointException {
        Query udsQuery = new Query();
        udsQuery.setQueryType(type);
        switch (type) {
        case UDS_CREATE_TABLE:
            udsQuery.setQuery(MessageFormat.format(sqlQueries.getProperty(type.name()), table));
            break;

        case UDS_SELECT_FOR_USER:
            udsQuery.setQuery(MessageFormat.format(sqlQueries.getProperty(type.name()), table, ":"
                    + COLUMNUSER));
            break;

        case UDS_INSERT:
            udsQuery.setQuery(MessageFormat.format(sqlQueries.getProperty(type.name()), table, ":"
                    + COLUMNUSER, ":" + COLUMNGROUP, ":" + COLUMNNAMESPACE));
            break;

        case UDS_DELETE_FOR_USER_NAMESPACE:
            udsQuery.setQuery(MessageFormat.format(sqlQueries.getProperty(type.name()), table, ":"
                    + COLUMNUSER, ":" + COLUMNNAMESPACE));
            break;

        case UDS_DELETE_FOR_GROUP_NAMESPACE:
            udsQuery.setQuery(MessageFormat.format(sqlQueries.getProperty(type.name()), table, ":"
                    + COLUMNGROUP, ":" + COLUMNNAMESPACE));
            break;

        case UDS_DELETE_FOR_NAMESPACE:
            udsQuery.setQuery(MessageFormat.format(sqlQueries.getProperty(type.name()), table, ":"
                    + COLUMNNAMESPACE));
            break;

        case UDS_DROP_TABLE:
            udsQuery.setQuery(MessageFormat.format(sqlQueries.getProperty(type.name()), table, ":"
                    + table));
            break;

            default:
            throw new SharepointException("Query not supported!! ");
        }
        return udsQuery;
    }

    // XXX This is temporary
    public String getDatabase() {
        return "User_Data_Store";
    }

    public void addSuffix(String suffix) {
        this.table += "_" + suffix;
    }

    public String[] getTables() {
        return new String[] { table };
    }

    public void setTable(String table) {
        this.table = table;
    }
}
