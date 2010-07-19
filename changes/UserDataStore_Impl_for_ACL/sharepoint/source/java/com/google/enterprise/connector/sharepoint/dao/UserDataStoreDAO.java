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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import com.google.enterprise.connector.sharepoint.dao.QueryBuilder.QueryType;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

/**
 * Data Access Object layer for accessing the user data store
 *
 * @author nitendra_thakur
 */
public class UserDataStoreDAO extends SimpleSharePointDAO {
    private final Logger LOGGER = Logger.getLogger(UserDataStoreDAO.class.getName());

    public UserDataStoreDAO(DataSource dataSource, QueryBuilder queryBuilder)
            throws SharepointException {
        super(dataSource, queryBuilder);
    }

    /**
     * A utility class to create the {@link UserGroupMembership} bean from
     * Result Set
     *
     * @author nitendra_thakur
     */
    class CustomRowMapper implements
            ParameterizedRowMapper<UserGroupMembership> {
        public UserGroupMembership mapRow(ResultSet result, int rowNum)
                throws SQLException {
            try {
                int columnIndexUserId = result.findColumn(UserDataStoreQueryBuilder.COLUMNUSER);
                int columnIndexGroupId = result.findColumn(UserDataStoreQueryBuilder.COLUMNGROUP);
                int columnIndexNamespace = result.findColumn(UserDataStoreQueryBuilder.COLUMNNAMESPACE);
                UserGroupMembership membership = new UserGroupMembership(
                        result.getString(columnIndexUserId),
                        result.getString(columnIndexGroupId),
                        result.getString(columnIndexNamespace));
                return membership;

            } catch (SharepointException e) {
                LOGGER.log(Level.WARNING, "Failed to parse and map the result set. ", e);
                return null;
            }
        }
    }

    /**
     * Retrieves all the membership information pertaining to a user. This would
     * be useful to serve the GSA -> CM requests during session channel creation
     *
     * @param username the user's login name, NOT the ID
     * @return list of {@link UserGroupMembership} representing memberships of
     *         the user
     */
    public List<UserGroupMembership> getAllGroupsForUser(final String username)
            throws SharepointException {
        List<UserGroupMembership> memberships = new ArrayList<UserGroupMembership>();
        SqlParameterSource namedParams = new MapSqlParameterSource(
                UserDataStoreQueryBuilder.COLUMNUSER,
                UserGroupMembership.getNamePattern(username));
        memberships = simpleJdbcTemplate.query(queryBuilder.createQuery(QueryType.SELECTQUERYFORUSER).getQuery(), new CustomRowMapper(), namedParams);
        LOGGER.log(Level.CONFIG, memberships.size()
                + " Memberships identified for user [ " + username + " ]. ");
        return memberships;
    }

    /**
     * adds a list of {@link UserGroupMembership} into the user data store
     *
     * @param memberships
     * @return status of each membership's update in store in the same order in
     *         which queries were specified
     * @throws SharepointException
     */
    public int[] addMemberships(List<UserGroupMembership> memberships)
            throws SharepointException {
        SqlParameterSource[] namedParams = new SqlParameterSource[memberships.size()];
        int i = 0;
        for (UserGroupMembership membership : memberships) {
            MapSqlParameterSource param = new MapSqlParameterSource(
                    UserDataStoreQueryBuilder.COLUMNUSER,
                    membership.getComplexUserId());
            param.addValue(UserDataStoreQueryBuilder.COLUMNGROUP, membership.getComplexGroupId());
            param.addValue(UserDataStoreQueryBuilder.COLUMNNAMESPACE, membership.getNameSpace());
            namedParams[i++] = param;
        }
        return batchUpdate(namedParams, QueryType.INSERTQUERY);
    }

    /**
     * removes all the membership info of a list of users belonging from a
     * specified namespace, from the user data store
     *
     * @param users list of userIds whose memberships are to be remove
     * @param namespace the namespace to which all the users belong
     * @return status of each membership's deletion from store in the same order
     *         in which queries were specified
     * @throws SharepointException
     */
    public int[] removeUserMembershipsFromNamespace(List<Integer> users,
            String namespace)
            throws SharepointException {
        SqlParameterSource[] namedParams = new SqlParameterSource[users.size()];
        int i = 0;
        for (int userId : users) {
            MapSqlParameterSource param = new MapSqlParameterSource(
                    UserDataStoreQueryBuilder.COLUMNUSER,
                    UserGroupMembership.getIdPattern(userId));
            param.addValue(UserDataStoreQueryBuilder.COLUMNNAMESPACE, namespace);
            namedParams[i++] = param;
        }
        return batchUpdate(namedParams, QueryType.DELETE_QUERY_FOR_USER_NAMESPACE);
    }

    /**
     * removes all the membership info of a list of groups belonging from a
     * specified namespace, from the user data store
     *
     * @param groups list of groupIds whose memberships are to be remove
     * @param namespace the namespace to which all the groups belong
     * @return status of each membership's deletion from store in the same order
     *         in which queries were specified
     * @throws SharepointException
     */
    public int[] removeGroupMembershipsFromNamespace(List<Integer> groups,
            String namespace) throws SharepointException {
        SqlParameterSource[] namedParams = new SqlParameterSource[groups.size()];
        int i = 0;
        for (int groupId : groups) {
            MapSqlParameterSource param = new MapSqlParameterSource(
                    UserDataStoreQueryBuilder.COLUMNGROUP,
                    UserGroupMembership.getIdPattern(groupId));
            param.addValue(UserDataStoreQueryBuilder.COLUMNNAMESPACE, namespace);
            namedParams[i++] = param;
        }
        return batchUpdate(namedParams, QueryType.DELETE_QUERY_FOR_GROUP_NAMESPACE);
    }

    /**
     * removes all the membership info belonging to a given list of namespaces,
     * from the user data store
     *
     * @param namespaces list of namespaces whose membeships are to be removed
     * @return status of each membership's deletion from store in the same order
     *         in which queries were specified
     * @throws SharepointException
     */
    public int[] removeAllMembershipsFromNamespace(List<String> namespaces)
            throws SharepointException {
        SqlParameterSource[] namedParams = new SqlParameterSource[namespaces.size()];
        int i = 0;
        for (String namespace : namespaces) {
            MapSqlParameterSource param = new MapSqlParameterSource(
                    UserDataStoreQueryBuilder.COLUMNNAMESPACE, namespace);
            namedParams[i++] = param;
        }
        return batchUpdate(namedParams, QueryType.DELETE_QUERY_FOR_NAMESPACE);
    }
}
