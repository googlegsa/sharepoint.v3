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

import com.google.enterprise.connector.sharepoint.cache.UserDataStoreCache;
import com.google.enterprise.connector.sharepoint.dao.QueryBuilder.Query;
import com.google.enterprise.connector.sharepoint.dao.QueryBuilder.QueryType;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * Data Access Object layer for accessing the user data store
 *
 * @author nitendra_thakur
 */
public class UserDataStoreDAO extends SimpleSharePointDAO {
    private final Logger LOGGER = Logger.getLogger(UserDataStoreDAO.class.getName());
    private UserDataStoreCache<UserGroupMembership> udsCache;

    public UserDataStoreDAO(DataSource dataSource, QueryBuilder queryBuilder)
            throws SharepointException {
        super(dataSource, queryBuilder);
        udsCache = new UserDataStoreCache<UserGroupMembership>();
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
                    getSimpleJdbcTemplate().update(queryBuilder.createQuery(QueryType.UDS_CREATE_TABLE).getQuery());
                    getSimpleJdbcTemplate().update(queryBuilder.createQuery(QueryType.UDS_CREATE_INDEX).getQuery());
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
    private class CustomRowMapper implements
            ParameterizedRowMapper<UserGroupMembership> {
        public UserGroupMembership mapRow(ResultSet result, int rowNum)
                throws SQLException {
            return UserDataStoreQueryBuilder.createMembership(result);
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

        UserGroupMembership paramMembership = new UserGroupMembership();
        paramMembership.setUserName(username);
        List<UserGroupMembership> lstParamMembership = new ArrayList<UserGroupMembership>();
        lstParamMembership.add(paramMembership);

        QueryType queryType = QueryType.UDS_SELECT_FOR_USERNAME;
        Query query = queryBuilder.createQuery(queryType);
        SqlParameterSource[] params = UserDataStoreQueryBuilder.createParameter(queryType, lstParamMembership);

        List<UserGroupMembership> memberships = null;
        try {
            memberships = getSimpleJdbcTemplate().query(query.getQuery(), new CustomRowMapper(), params[0]);
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
     * Adds a list of {@link UserGroupMembership} into the user data store. From
     * the passed in collection, only those memberships which are not in cache
     * are picked up for the SQL insert. The rest other memberships are removed
     * from the collection.
     * <p/>
     * Note: Hence, this method may (and often do) modifies the passed in
     * collection. After the method returns, the caller can ensure that the
     * collection contains only those memberships which the connector really
     * attempted insertion. But, it does not ensure if it was successful or not.
     *
     * @param memberships
     * @throws SharepointException
     */
    public void addMemberships(Set<UserGroupMembership> memberships)
            throws SharepointException {
        if (null != udsCache) {
            removeAllCached(memberships);
        }

        QueryType queryType = QueryType.UDS_INSERT;
        SqlParameterSource[] params = UserDataStoreQueryBuilder.createParameter(queryType, memberships);
        int[] status = batchUpdate(params, queryType);

        if (null != udsCache) {
            addAllSucceeded(status, memberships);
        }
    }

    /**
     * removes all the membership info of a list of users belonging from a
     * specified namespace, from the user data store
     *
     * @param users list of userIds whose memberships are to be remove
     * @param namespace the namespace to which all the users belong
     * @throws SharepointException
     */
    public void removeUserMembershipsFromNamespace(Set<Integer> userIds,
            String namespace) throws SharepointException {
        Set<UserGroupMembership> memberships = new HashSet<UserGroupMembership>();
        for (int userId : userIds) {
            UserGroupMembership membership = new UserGroupMembership();
            membership.setUserId(userId);
            membership.setNamespace(namespace);
            memberships.add(membership);
        }

        if (memberships.size() == 0) {
            return;
        }

        QueryType queryType = QueryType.UDS_DELETE_FOR_USERID_NAMESPACE;
        SqlParameterSource[] params = UserDataStoreQueryBuilder.createParameter(queryType, memberships);
        batchUpdate(params, queryType);

        for (UserGroupMembership membership : memberships) {
            udsCache.removeUsingNamespaceView(membership);
        }
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
    public void removeGroupMembershipsFromNamespace(Set<Integer> groupIds,
            String namespace) throws SharepointException {
        Set<UserGroupMembership> memberships = new HashSet<UserGroupMembership>();
        for (int groupId : groupIds) {
            UserGroupMembership membership = new UserGroupMembership();
            membership.setGroupId(groupId);
            membership.setNamespace(namespace);
            memberships.add(membership);
        }

        if (memberships.size() == 0) {
            return;
        }

        QueryType queryType = QueryType.UDS_DELETE_FOR_GROUPID_NAMESPACE;
        SqlParameterSource[] params = UserDataStoreQueryBuilder.createParameter(queryType, memberships);
        batchUpdate(params, queryType);

        for (UserGroupMembership membership : memberships) {
            udsCache.removeUsingGroupNamespaceView(membership);
        }
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
    public void removeAllMembershipsFromNamespace(Set<String> namespaces)
            throws SharepointException {

        Set<UserGroupMembership> memberships = new HashSet<UserGroupMembership>();
        for (String namespace : namespaces) {
            UserGroupMembership membership = new UserGroupMembership();
            membership.setNamespace(namespace);
            memberships.add(membership);
        }

        if (memberships.size() == 0) {
            return;
        }

        QueryType queryType = QueryType.UDS_DELETE_FOR_NAMESPACE;
        SqlParameterSource[] params = UserDataStoreQueryBuilder.createParameter(queryType, memberships);
        batchUpdate(params, queryType);

        for (UserGroupMembership membership : memberships) {
            udsCache.removeUsingNamespaceView(membership);
        }
    }

    /**
     * Synchronizes the membership information of all groups identified by the
     * keyset of the passed in map. The groups are picked up as group-namespace
     * view. The synchronization involves deleting all the persisted memberships
     * and adding the most latest ones </p> This synchronization is performed as
     * one atomic operation using transaction
     *
     * @param groupToMemberships identifies groups and their corresponding most
     *            latest membership information
     * @param namespace
     * @throws SharepointException
     */
    public void syncGroupMemberships(
            Map<Integer, Set<UserGroupMembership>> groupToMemberships,
            String namespace)
            throws SharepointException {

        if (null == groupToMemberships || groupToMemberships.size() == 0) {
            return;
        }

        Set<UserGroupMembership> membershipsToDelete = new TreeSet<UserGroupMembership>();
        Set<UserGroupMembership> membershipsToInsert = new TreeSet<UserGroupMembership>();
        for (Integer groupId : groupToMemberships.keySet()) {
            UserGroupMembership membership = new UserGroupMembership();
            membership.setGroupId(groupId);
            membership.setNamespace(namespace);

            membershipsToDelete.add(membership);
            membershipsToInsert.addAll(groupToMemberships.get(membership));
        }

        List<QueryType> queryType = new ArrayList<QueryType>();
        List<SqlParameterSource[]> params = new ArrayList<SqlParameterSource[]>();

        QueryType type = QueryType.UDS_DELETE_FOR_GROUPID_NAMESPACE;
        queryType.add(type);
        params.add(UserDataStoreQueryBuilder.createParameter(type, membershipsToDelete));

        type = QueryType.UDS_INSERT;
        queryType.add(type);
        params.add(UserDataStoreQueryBuilder.createParameter(type, membershipsToInsert));


        int[][] batchStatus = executeAsTransaction(queryType, params);

        // Removal from cache is lenient because it does not harm any
        // functionality. At worst, duplicate insertion will occur
        if (batchStatus != null && batchStatus.length == 2) {
            for (UserGroupMembership membership : membershipsToDelete) {
                udsCache.removeUsingGroupNamespaceView(membership);
            }

            // Unlike removal, adding into cache should be strict. A wrong
            // insertion will mean that such records will never be able to reach
            // up to the database.
            addAllSucceeded(batchStatus[1], membershipsToInsert);
        }
    }

    /**
     * To cleanup the cache
     */
    public void cleanupCache() {
        udsCache.clearCache();
        LOGGER.log(Level.INFO, "Current cache size , after cleanup "
                + udsCache.size());
    }

    /**
     * cache can be disabled using this. By default, it's enabled.
     *
     * @param useCache
     */
    public void isUseCache(boolean useCache) {
        if (!useCache) {
            udsCache = null;
        }
    }

    /**
     * Removes all those elements from the passed-in collection that are found
     * in cache
     *
     * @param elems
     */
    private void removeAllCached(Collection<UserGroupMembership> memberships) {
        Iterator<UserGroupMembership> itr = memberships.iterator();
        while (itr.hasNext()) {
            UserGroupMembership membership = itr.next();
            if (udsCache.contains(membership)) {
                itr.remove();
            }
        }
    }

    /**
     * Adds element into cache after performing strict checking about the status
     * of the query execution.
     */
    private void addAllSucceeded(int[] status,
            Collection<UserGroupMembership> memberships) {
        if (null != udsCache && null != status
                && status.length == memberships.size()) {
            int i = 0;
            for (UserGroupMembership membership : memberships) {
                if (status[i] > 0) {
                    udsCache.add(membership);
                }
            }
        }
    }
}
