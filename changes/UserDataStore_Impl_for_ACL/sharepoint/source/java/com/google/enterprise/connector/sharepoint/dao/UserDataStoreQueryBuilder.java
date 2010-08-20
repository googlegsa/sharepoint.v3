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
public class UserDataStoreQueryBuilder implements QueryBuilder {
    private QueryProvider queryProvider;
    String tablename = SPConstants.UDS_TABLENAME;
    String indexname = SPConstants.UDS_INDEXNAME;

    public UserDataStoreQueryBuilder(QueryProvider queryProvider)
            throws SharepointException {
        if (null == queryProvider) {
            throw new SharepointException("queryProvider cannot be null! ");
        }
        this.queryProvider = queryProvider;
    }

    /**
     * Creates queries in a format as expected by Spring. The placeholsders are
     * replaced by a literal which is in a format :literal. The actual value can
     * be passed through named parameter see
     * {@link UserDataStoreQueryBuilder#createParameter(com.google.enterprise.connector.sharepoint.dao.QueryBuilder.QueryType, Collection)}
     */
    public Query createQuery(QueryType queryType) throws SharepointException {
        Query udsQuery = new Query();
        udsQuery.setQueryType(queryType);
        switch (queryType) {
            case UDS_CREATE_TABLE:
            udsQuery.setQuery(MessageFormat.format(queryProvider.getQuery(queryType.name()), tablename));
            break;

            case UDS_CREATE_INDEX:
            udsQuery.setQuery(MessageFormat.format(queryProvider.getQuery(queryType.name()), tablename, indexname));
                break;

            case UDS_SELECT_FOR_USERNAME:
            udsQuery.setQuery(MessageFormat.format(queryProvider.getQuery(queryType.name()), tablename, (":" + SPConstants.UDS_COLUMNUSERNAME)));
                break;

            case UDS_INSERT:
            udsQuery.setQuery(MessageFormat.format(queryProvider.getQuery(queryType.name()), tablename, (":" + SPConstants.UDS_COLUMNUSERID), (":" + SPConstants.UDS_COLUMNUSERNAME), (":" + SPConstants.UDS_COLUMNGROUPID), (":" + SPConstants.UDS_COLUMNGROUPNAME), (":" + SPConstants.UDS_COLUMNNAMESPACE)));
                break;

            case UDS_DELETE_FOR_USERID_NAMESPACE:
            udsQuery.setQuery(MessageFormat.format(queryProvider.getQuery(queryType.name()), tablename, (":" + SPConstants.UDS_COLUMNUSERID), (":" + SPConstants.UDS_COLUMNNAMESPACE)));
                break;

            case UDS_DELETE_FOR_GROUPID_NAMESPACE:
            udsQuery.setQuery(MessageFormat.format(queryProvider.getQuery(queryType.name()), tablename, (":" + SPConstants.UDS_COLUMNGROUPID), (":" + SPConstants.UDS_COLUMNNAMESPACE)));
                break;

            case UDS_DELETE_FOR_NAMESPACE:
            udsQuery.setQuery(MessageFormat.format(queryProvider.getQuery(queryType.name()), tablename, (":" + SPConstants.UDS_COLUMNNAMESPACE)));
                break;

            case UDS_DROP_TABLE:
            udsQuery.setQuery(MessageFormat.format(queryProvider.getQuery(queryType.name()), tablename));
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
                        SPConstants.UDS_COLUMNUSERNAME,
                        membership.getUserName());
            }
            break;

        case UDS_INSERT:
            for (UserGroupMembership membership : memberships) {
                MapSqlParameterSource param = new MapSqlParameterSource();
                param.addValue(SPConstants.UDS_COLUMNUSERID, membership.getUserId());
                param.addValue(SPConstants.UDS_COLUMNUSERNAME, membership.getUserName());
                param.addValue(SPConstants.UDS_COLUMNGROUPID, membership.getGroupId());
                param.addValue(SPConstants.UDS_COLUMNGROUPNAME, membership.getGroupName());
                param.addValue(SPConstants.UDS_COLUMNNAMESPACE, membership.getNamespace());
                namedParams[count++] = param;
            }
            break;

        case UDS_DELETE_FOR_USERID_NAMESPACE:
            for (UserGroupMembership membership : memberships) {
                MapSqlParameterSource param = new MapSqlParameterSource();
                param.addValue(SPConstants.UDS_COLUMNUSERID, membership.getUserId());
                param.addValue(SPConstants.UDS_COLUMNNAMESPACE, membership.getNamespace());
                namedParams[count++] = param;
            }
            break;

        case UDS_DELETE_FOR_GROUPID_NAMESPACE:
            for (UserGroupMembership membership : memberships) {
                MapSqlParameterSource param = new MapSqlParameterSource();
                param.addValue(SPConstants.UDS_COLUMNGROUPID, membership.getGroupId());
                param.addValue(SPConstants.UDS_COLUMNNAMESPACE, membership.getNamespace());
                namedParams[count++] = param;
            }
            break;

        case UDS_DELETE_FOR_NAMESPACE:
            for (UserGroupMembership membership : memberships) {
                MapSqlParameterSource param = new MapSqlParameterSource();
                param.addValue(SPConstants.UDS_COLUMNNAMESPACE, membership.getNamespace());
                namedParams[count++] = param;
            }
            break;

        default:
            throw new SharepointException("Query Type not supported!! ");
        }
        return namedParams;
    }

    public void addSuffix(String suffix) {
        this.tablename += "_" + suffix;
        this.indexname += "_" + suffix;
    }

    // XXX This is temporary
    public String getDatabase() {
        return "User_Data_Store";
    }

    public String[] getTables() {
        return new String[] { tablename };
    }

    /**
     * Construct an instance of {@link UserGroupMembership} class from a DB
     * result set
     *
     * @param result
     * @return
     * @throws SQLException
     */
    public static UserGroupMembership createMembership(ResultSet result)
            throws SQLException {
        UserGroupMembership membership = new UserGroupMembership(
                result.getInt(SPConstants.UDS_COLUMNUSERID),
                result.getString(SPConstants.UDS_COLUMNUSERNAME),
                result.getInt(SPConstants.UDS_COLUMNGROUPID),
                result.getString(SPConstants.UDS_COLUMNGROUPNAME),
                result.getString(SPConstants.UDS_COLUMNNAMESPACE));
        return membership;
    }
}
