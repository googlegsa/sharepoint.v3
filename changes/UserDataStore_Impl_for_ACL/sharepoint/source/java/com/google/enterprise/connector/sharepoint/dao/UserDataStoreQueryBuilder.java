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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collection;

/**
 * Query Builder for {@link UserDataStoreDAO}
 *
 * @author nitendra_thakur
 */
public class UserDataStoreQueryBuilder extends LocalizedQueryBuilder {
    // Column Names of User Data Store Membership table.
    private static final String COLUMNUSERID = "SPUserId";
    private static final String COLUMNUSERNAME = "SPUserName";
    private static final String COLUMNGROUPID = "SPGroupId";
    private static final String COLUMNGROUPNAME = "SPGroupName";
    private static final String COLUMNNAMESPACE = "SPSite";

    private String table;
    private String index;

    public UserDataStoreQueryBuilder(String table, String index)
            throws SharepointException {
        if (table == null || table.trim().length() == 0) {
            throw new SharepointException("Invalid table name! ");
        }
        this.table = table;

        if (index == null || index.trim().length() == 0) {
            throw new SharepointException("Invalid index name! ");
        }
        this.index = index;
    }

    public Query createQuery(QueryType queryType) throws SharepointException {
        Query udsQuery = new Query();
        udsQuery.setQueryType(queryType);
        switch (queryType) {
            case UDS_CREATE_TABLE:
            udsQuery.setQuery(MessageFormat.format(getQueryString(queryType.name()), table));
            break;

            case UDS_CREATE_INDEX:
            udsQuery.setQuery(MessageFormat.format(getQueryString(queryType.name()), index, table));
                break;

            case UDS_SELECT_FOR_USERNAME:
            udsQuery.setQuery(MessageFormat.format(getQueryString(queryType.name()), table, (":" + COLUMNUSERNAME)));
                break;

            case UDS_INSERT:
            udsQuery.setQuery(MessageFormat.format(getQueryString(queryType.name()), table, (":" + COLUMNUSERID), (":" + COLUMNUSERNAME), (":" + COLUMNGROUPID), (":" + COLUMNGROUPNAME), (":" + COLUMNNAMESPACE)));
                break;

            case UDS_DELETE_FOR_USERID_NAMESPACE:
            udsQuery.setQuery(MessageFormat.format(getQueryString(queryType.name()), table, (":" + COLUMNUSERID), (":" + COLUMNNAMESPACE)));
                break;

            case UDS_DELETE_FOR_GROUPID_NAMESPACE:
            udsQuery.setQuery(MessageFormat.format(getQueryString(queryType.name()), table, (":" + COLUMNGROUPID), (":" + COLUMNNAMESPACE)));
                break;

            case UDS_DELETE_FOR_NAMESPACE:
            udsQuery.setQuery(MessageFormat.format(getQueryString(queryType.name()), table, (":" + COLUMNNAMESPACE)));
                break;

            case UDS_DROP_TABLE:
            udsQuery.setQuery(MessageFormat.format(getQueryString(queryType.name()), table));
                break;

            default:
            throw new SharepointException("Query Type not supported!! ");
        }
        return udsQuery;
    }

    public static SqlParameterSource[] createParameter(QueryType queryType,
            Collection<UserGroupMembership> memberships)
            throws SharepointException {

        SqlParameterSource[] namedParams = new SqlParameterSource[memberships.size()];
        int count = 0;

        switch (queryType) {
        case UDS_SELECT_FOR_USERNAME:
            for (UserGroupMembership membership : memberships) {
                namedParams[count++] = new MapSqlParameterSource(
                        UserDataStoreQueryBuilder.COLUMNUSERNAME,
                        membership.getUserName());
            }
            break;

        case UDS_INSERT:
            for (UserGroupMembership membership : memberships) {
                MapSqlParameterSource param = new MapSqlParameterSource();
                param.addValue(COLUMNUSERID, membership.getUserId());
                param.addValue(COLUMNUSERNAME, membership.getUserName());
                param.addValue(COLUMNGROUPID, membership.getGroupId());
                param.addValue(COLUMNGROUPNAME, membership.getGroupName());
                param.addValue(UserDataStoreQueryBuilder.COLUMNNAMESPACE, membership.getNamespace());
                namedParams[count++] = param;
            }
            break;

        case UDS_DELETE_FOR_USERID_NAMESPACE:
            for (UserGroupMembership membership : memberships) {
                MapSqlParameterSource param = new MapSqlParameterSource();
                param.addValue(COLUMNUSERID, membership.getUserId());
                param.addValue(COLUMNNAMESPACE, membership.getNamespace());
                namedParams[count++] = param;
            }
            break;

        case UDS_DELETE_FOR_GROUPID_NAMESPACE:
            for (UserGroupMembership membership : memberships) {
                MapSqlParameterSource param = new MapSqlParameterSource();
                param.addValue(COLUMNGROUPID, membership.getGroupId());
                param.addValue(COLUMNNAMESPACE, membership.getNamespace());
                namedParams[count++] = param;
            }
            break;

        case UDS_DELETE_FOR_NAMESPACE:
            for (UserGroupMembership membership : memberships) {
                MapSqlParameterSource param = new MapSqlParameterSource();
                param.addValue(COLUMNNAMESPACE, membership.getNamespace());
                namedParams[count++] = param;
            }
            break;

        default:
            throw new SharepointException("Query Type not supported!! ");
        }
        return namedParams;
    }

    public void addSuffix(String suffix) {
        this.table += "_" + suffix;
        this.index += "_" + suffix;
    }

    // XXX This is temporary
    public String getDatabase() {
        return "User_Data_Store";
    }

    public String[] getTables() {
        return new String[] { table };
    }

    public void setTable(String table) {
        this.table = table;
    }

    /**
     * Construct an instance of this class from a result set
     *
     * @param result
     * @return
     * @throws SQLException
     */
    public static UserGroupMembership getInstance(ResultSet result)
            throws SQLException {
        UserGroupMembership membership = new UserGroupMembership(
                result.getInt(COLUMNUSERID), result.getString(COLUMNUSERNAME),
                result.getInt(COLUMNGROUPID),
                result.getString(COLUMNGROUPNAME),
                result.getString(COLUMNNAMESPACE));
        return membership;
    }
}
