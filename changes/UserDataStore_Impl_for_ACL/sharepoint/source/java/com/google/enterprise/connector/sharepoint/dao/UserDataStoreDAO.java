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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
    UserDataStoreCache<UserGroupMembership> cachedMemberships;

    public UserDataStoreDAO(DataSource dataSource, QueryBuilder queryBuilder,
            int cacheCapacity)
            throws SharepointException {
        super(dataSource, queryBuilder);
        cachedMemberships = new UserDataStoreCache<UserGroupMembership>(
                cacheCapacity);
    }

    void confirmEntitiesExistence() throws SharepointException {
        if (null == queryBuilder.getTables()
                || queryBuilder.getTables().length == 0) {
            return;
        }
        DatabaseMetaData dbm = null;
        try {
            dbm = getConnection().getMetaData();
            for (String table : queryBuilder.getTables()) {
                ResultSet resultSet = dbm.getTables(queryBuilder.getDatabase(), null, table, null);
                if (null == resultSet || !resultSet.next()) {
                    this.simpleJdbcTemplate.update(queryBuilder.createQuery(QueryType.UDS_CREATE_TABLE).getQuery());
                }
                resultSet.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception occurred while getting the table information from the database metadata. ", e);
        }
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
    public List<UserGroupMembership> getAllMembershipsForUser(
            final String username)
            throws SharepointException {
        List<UserGroupMembership> memberships = new ArrayList<UserGroupMembership>();
        SqlParameterSource namedParams = new MapSqlParameterSource(
                UserDataStoreQueryBuilder.COLUMNUSER,
                UserGroupMembership.getNamePattern(username));
        try {
            memberships = simpleJdbcTemplate.query(queryBuilder.createQuery(QueryType.UDS_SELECT_FOR_USER).getQuery(), new CustomRowMapper(), namedParams);
        } catch (Throwable t) {
            throw new SharepointException(
                    "Query execution failed while getting the membership info of a given user ",
                    t);
        }
        LOGGER.log(Level.CONFIG, memberships.size()
                + " Memberships identified for user [ " + username + " ]. ");
        return memberships;
    }

    /**
     * adds a list of {@link UserGroupMembership} into the user data store
     *
     * @param memberships
     * @throws SharepointException
     */
    public void addMemberships(List<UserGroupMembership> memberships)
            throws SharepointException {

        int skipCount = 0;

        // Here, we need both ordering and lookup
        Set<UserGroupMembership> validMemberships = new TreeSet<UserGroupMembership>();
        for (UserGroupMembership membership : memberships) {
            if (cachedMemberships.contains(membership)
                    || validMemberships.contains(membership)) {
                ++skipCount;
                LOGGER.log(Level.FINEST, "Skipping duplicate membership "
                        + membership);
                continue;
            }
            validMemberships.add(membership);
        }

        LOGGER.log(Level.INFO, "#"
                + validMemberships.size()
                + " insert queries are selected for execution. #"
                + skipCount
                + " queries were skipped either becasue they were duplicate or were found in cache. Total queries received was #"
                + memberships.size());

        if (validMemberships.size() == 0) {
            return;
        }

        int paramCount = 0;
        SqlParameterSource[] namedParams = new SqlParameterSource[validMemberships.size()];
        for (UserGroupMembership membership : validMemberships) {
            MapSqlParameterSource param = new MapSqlParameterSource(
                    UserDataStoreQueryBuilder.COLUMNUSER,
                    membership.getComplexUserId());
            param.addValue(UserDataStoreQueryBuilder.COLUMNGROUP, membership.getComplexGroupId());
            param.addValue(UserDataStoreQueryBuilder.COLUMNNAMESPACE, membership.getNameSpace());
            namedParams[paramCount++] = param;
        }
        int[] status = batchUpdate(namedParams, QueryType.UDS_INSERT);

        // Add all the successfully executed memberships into the cache.
        cachedMemberships.addMemberships(getAllSucceded(validMemberships, status));
    }

    /**
     * removes all the membership info of a list of users belonging from a
     * specified namespace, from the user data store
     *
     * @param users list of userIds whose memberships are to be remove
     * @param namespace the namespace to which all the users belong
     * @throws SharepointException
     */
    public void removeUserMembershipsFromNamespace(List<Integer> userIds,
            String namespace) throws SharepointException {

        SqlParameterSource[] namedParams = new SqlParameterSource[userIds.size()];
        int paramCount = 0;

        for (int userId : userIds) {
            MapSqlParameterSource param = new MapSqlParameterSource(
                    UserDataStoreQueryBuilder.COLUMNUSER,
                    UserGroupMembership.getIdPattern(userId));
            param.addValue(UserDataStoreQueryBuilder.COLUMNNAMESPACE, namespace);
            namedParams[paramCount++] = param;
        }

        LOGGER.log(Level.INFO, "#"
                + paramCount
                + " delete queries are selected for execution.");

        if (paramCount == 0) {
            return;
        }

        int[] status = batchUpdate(namedParams, QueryType.UDS_DELETE_FOR_USER_NAMESPACE);

        if (status.length != paramCount) {
            throw new SharepointException("No. of execution status returned [ "
                    + status.length
                    + " ] is not equal to no. of qieries passed [ "
                    + paramCount + " ] ");
        }

        // Remove all the successfully executed memberships from the cache.
        cachedMemberships.removeUserMembershipsFromNamespace(getAllSucceded(userIds, status), namespace);
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
    public void removeGroupMembershipsFromNamespace(List<Integer> groupIds,
            String namespace) throws SharepointException {

        SqlParameterSource[] namedParams = new SqlParameterSource[groupIds.size()];
        int paramCount = 0;

        for (int groupId : groupIds) {
            MapSqlParameterSource param = new MapSqlParameterSource(
                    UserDataStoreQueryBuilder.COLUMNGROUP,
                    UserGroupMembership.getIdPattern(groupId));
            param.addValue(UserDataStoreQueryBuilder.COLUMNNAMESPACE, namespace);
            namedParams[paramCount++] = param;
        }

        LOGGER.log(Level.INFO, "#"
                + paramCount
                + " delete queries are selected for execution");

        if (paramCount == 0) {
            return;
        }

        int[] status = batchUpdate(namedParams, QueryType.UDS_DELETE_FOR_GROUP_NAMESPACE);

        // Remove all the successfully executed memberships from the cache.
        cachedMemberships.removeGroupMembershipsFromNamespace(getAllSucceded(groupIds, status), namespace);
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
    public void removeAllMembershipsFromNamespace(List<String> namespaces)
            throws SharepointException {

        SqlParameterSource[] namedParams = new SqlParameterSource[namespaces.size()];
        int paramCount = 0;

        for (String namespace : namespaces) {
            MapSqlParameterSource param = new MapSqlParameterSource(
                    UserDataStoreQueryBuilder.COLUMNNAMESPACE, namespace);
            namedParams[paramCount++] = param;
        }

        LOGGER.log(Level.INFO, "Sending #"
                + paramCount
                + " delete queries are selected for execution.");

        if (paramCount == 0) {
            return;
        }

        int[] status = batchUpdate(namedParams, QueryType.UDS_DELETE_FOR_NAMESPACE);

        if (status.length != paramCount) {
            throw new SharepointException("No. of execution status returned [ "
                    + status.length
                    + " ] is not equal to no. of qieries passed [ "
                    + paramCount + " ] ");
        }

        LOGGER.log(Level.CONFIG, "Execution status of the #" + paramCount
                + " delete queries are " + status);

        // Remove all the successfully executed memberships from the cache.
        cachedMemberships.removeAllMembershipsFromNamespace(getAllSucceded(namespaces, status));
    }

    /**
     * Returns all the queries executed successfully by looking ate their
     * execution status. The order of the two are assumed to be in sync. This
     * method expect the queries collection to maintain an ordering of its
     * elements. However, it does not make any restriction for the same. Hence,
     * the caller should ensure that the collection is ordered
     *
     * @param <T>
     * @param queries
     * @param executionStatus
     * @return
     * @throws SharepointException
     */
    private <T> Set<T> getAllSucceded(Collection<T> queries,
            int[] executionStatus)
            throws SharepointException {
        if (executionStatus.length != queries.size()) {
            throw new SharepointException("No. of execution status returned [ "
                    + executionStatus.length
                    + " ] is not equal to no. of qieries passed [ "
                    + queries.size() + " ] ");
        }
        Set<T> allSucceeded = new HashSet<T>();
        int i = 0;
        for (T t : queries) {
            if (executionStatus[i++] >= 0) {
                allSucceeded.add(t);
            }
        }
        return allSucceeded;
    }
}
