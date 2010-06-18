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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.enterprise.connector.sharepoint.client.SPConstants.SPDAOConstants;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

/**
 * Data Access Object layer for accessing the user data store
 *
 * @author nitendra_thakur
 */
public class UserDataStoreDAO extends JdbcDaoSupport {
    private final Logger LOGGER = Logger.getLogger(UserDataStoreDAO.class.getName());
    private SimpleJdbcTemplate simpleJdbcTemplate;
    private TransactionTemplate transactionTemplate;

    /**
     * Returns an instance of this class for the passed in data source
     *
     * @param dataSource
     * @return
     * @throws SharepointException
     */
    public static UserDataStoreDAO getInstance(DataSource dataSource)
            throws SharepointException {
        return new UserDataStoreDAO(dataSource);
    }

    UserDataStoreDAO(DataSource dataSource)
            throws SharepointException {
        setDataSource(dataSource);
        setJdbcTemplate(new JdbcTemplate(dataSource));
        this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
        confirmDBExistence();

        // Once, the existence of the user_data_store db is confirmed, now
        // append the db name in the db url so that actual queries can be
        // executed
        /*
         * XXX: This currently is not needed as the connector does not have to
         * create the database
         */
        /*
         * DriverManagerDataSource dbDatSource = (DriverManagerDataSource)
         * dataSource; dbDatSource.setUrl(dbDatSource.getUrl() +
         * SPDAOConstants.DBNAME); setDataSource(dbDatSource);
         * setJdbcTemplate(new JdbcTemplate(dbDatSource));
         * this.simpleJdbcTemplate = new SimpleJdbcTemplate(dbDatSource);
         */
        this.transactionTemplate = new TransactionTemplate(
                new DataSourceTransactionManager(dataSource));
        this.transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
        confirmEntitiesExistence();
    }

    /**
     * Check if the user data store DB exists or not. If not, creates one.
     *
     * @throws SharepointException
     */
    private void confirmDBExistence() throws SharepointException {
        DatabaseMetaData dbm = null;
        try {
            dbm = getConnection().getMetaData();
            ResultSet dbs = dbm.getCatalogs();
            if (null != dbs) {
                boolean dbExists = false;
                while (dbs.next()) {
                    if (SPDAOConstants.DBNAME.equalsIgnoreCase(dbs.getString("TABLE_CAT"))) {
                        dbExists = true;
                    }
                }
                if (!dbExists) {
                    /*
                     * LOGGER.log(Level.INFO, "Creating database... " +
                     * SPDAOConstants.DBNAME);
                     * this.simpleJdbcTemplate.update(SPDAOConstants
                     * .CREATEDBQUERY);
                     */
                    throw new SharepointException("Database does not exist");
                }
            }
        } catch (Exception e) {
            throw new SharepointException(
                    "Exception occurred while confirming the existance of the user_data_store database ",
                    e);
        }
    }

    /**
     * Checks if all the required entities exist in the user data store DB. If
     * not, creates them. The currently supported entities are
     * <ul>
     * <li>user_group_memberships table and two functions:</li>
     * <li>DoesGroupExists</li>
     * <li>DoesUserExists</li>
     * </ul>
     *
     * @throws SharepointException
     */
    private void confirmEntitiesExistence() throws SharepointException {
        DatabaseMetaData dbm = null;
        try {
            dbm = getConnection().getMetaData();
            ResultSet resultSet = dbm.getTables(SPDAOConstants.DBNAME, null, SPDAOConstants.TABLENAME, null);
            if (null == resultSet || !resultSet.next()) {
                this.simpleJdbcTemplate.getJdbcOperations().batchUpdate(new String[] {
                        SPDAOConstants.CREATETABLEQUERY,
                        SPDAOConstants.CREATEGROUPCHKFUNCQUERY,
                        SPDAOConstants.CREATEUSERCHKFUNCQUERY });
            }
            resultSet.close();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception occurred while getting the table information from the database metadata. ", e);
        }
    }

    /**
     * A utility class used to create CallableStatements for calling functions
     * and procedures using Spring's jdbcTemplate or simpleJdbcTemplate
     *
     * @author nitendra_thakur
     */
    class CustomCallableStatementCreator implements CallableStatementCreator {
        String idPattern;
        String namespace;

        public CustomCallableStatementCreator(String idPattern, String namespace) {
            this.idPattern = idPattern;
            this.namespace = namespace;
        }

        public CallableStatement createCallableStatement(Connection con)
                throws SQLException {
            CallableStatement cs = con.prepareCall(SPDAOConstants.GROUPCHKFUNCALL);
            cs.registerOutParameter(1, java.sql.Types.BOOLEAN);
            cs.setString(2, idPattern);
            cs.setString(3, namespace);
            return cs;
        }
    }

    /**
     * A utility class used to create CallableStatementsCallback for calling
     * functions and procedures using Spring's jdbcTemplate or
     * simpleJdbcTemplate
     *
     * @author nitendra_thakur
     */
    class CustomCallableStatementCallback implements CallableStatementCallback {
        public Boolean doInCallableStatement(CallableStatement cs)
                throws SQLException, DataAccessException {
            cs.execute();
            return cs.getBoolean(1);
        }
    }

    /**
     * Creates a custom transaction callback class to be used while executing
     * the update queries to user data store as a transaction. This also handles
     * the table lock by acquiring a WRITE lock on the table before executing
     * the update query
     *
     * @author nitendra_thakur
     */
    class CustomTransactionCallback implements TransactionCallback {
        String sqlQuery;
        SqlParameterSource param;

        CustomTransactionCallback(String sqlQuery, SqlParameterSource param) {
            this.sqlQuery = sqlQuery;
            this.param = param;
        }

        public Integer doInTransaction(TransactionStatus status) {
            UserDataStoreDAO.this.simpleJdbcTemplate.update(SPDAOConstants.LOCKTABLEFORWRITE);
            int count = UserDataStoreDAO.this.simpleJdbcTemplate.update(sqlQuery, param);
            return count;
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
                int columnIndexUserId = result.findColumn(SPDAOConstants.COLUMNUSER);
                int columnIndexGroupId = result.findColumn(SPDAOConstants.COLUMNGROUP);
                int columnIndexNamespace = result.findColumn(SPDAOConstants.COLUMNNAMESPACE);
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
    public List<UserGroupMembership> getAllGroupsForUser(final String username) {
        List<UserGroupMembership> memberships = new ArrayList<UserGroupMembership>();
        SqlParameterSource namedParams = new MapSqlParameterSource(
                SPDAOConstants.COLUMNUSER,
                UserGroupMembership.getNamePattern(username));
        memberships = simpleJdbcTemplate.query(SPDAOConstants.SELECTQUERYFORUSER, new CustomRowMapper(), namedParams);
        LOGGER.log(Level.CONFIG, memberships.size()
                + " Memberships identified for user [ " + username + " ]. ");
        return memberships;
    }

    /**
     * Adds a new membership information.
     *
     * @param membership
     * @return
     */
    public boolean addMembership(UserGroupMembership membership) {
        if (null == membership || !membership.isValid()) {
            return false;
        }
        MapSqlParameterSource namedParams = new MapSqlParameterSource(
                SPDAOConstants.COLUMNUSER, membership.getComplexUserId());
        namedParams.addValue(SPDAOConstants.COLUMNGROUP, membership.getComplexGroupId());
        namedParams.addValue(SPDAOConstants.COLUMNNAMESPACE, membership.getNameSpace());
        int count = 0;
        try {
            count = (Integer) this.transactionTemplate.execute(new CustomTransactionCallback(
                    SPDAOConstants.INSERTQUERY, namedParams));
        } catch (DataIntegrityViolationException e) {
            // During the connector's crawl, case of duplicate insertion is
            // going to occur very frequently. This need not be considered as a
            // severe case.
            LOGGER.log(Level.FINE, "entry already exists for " + membership, e);
        }
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Remove all the memberships from an specified namespace pertaining to a
     * given of a user.
     *
     * @param userId user ID, NOT the login name
     * @param namespace
     * @return no. of records deleted
     */
    public int removeUserMemberships(final int userId, final String namespace) {
        if (null == namespace) {
            return 0;
        }
        final MapSqlParameterSource namedParams = new MapSqlParameterSource(
                SPDAOConstants.COLUMNUSER,
                UserGroupMembership.getIdPattern(userId));
        namedParams.addValue(SPDAOConstants.COLUMNNAMESPACE, namespace);
        return (Integer) this.transactionTemplate.execute(new CustomTransactionCallback(
                SPDAOConstants.DELETE_QUERY_FOR_USER_NAMESPACE, namedParams));
    }

    /**
     * Remove all the memberships from an specified namespace pertaining to a
     * given of a group.
     *
     * @param groupId group ID, NOT the group name
     * @param namespace
     * @return no. of records deleted
     */
    public int removeGroupMemberships(final int groupId, final String namespace) {
        if (null == namespace) {
            return 0;
        }
        MapSqlParameterSource namedParams = new MapSqlParameterSource(
                SPDAOConstants.COLUMNGROUP,
                UserGroupMembership.getIdPattern(groupId));
        namedParams.addValue(SPDAOConstants.COLUMNNAMESPACE, namespace);
        return (Integer) this.transactionTemplate.execute(new CustomTransactionCallback(
                SPDAOConstants.DELETE_QUERY_FOR_GROUP_NAMESPACE, namedParams));
    }

    /**
     * Remove all the memberships that belongs to as specified namespace
     *
     * @param namespace
     * @return no. of records deleted
     */
    public int removeAllMembershipsFromNamespace(final String namespace) {
        if (null == namespace) {
            return 0;
        }
        MapSqlParameterSource namedParams = new MapSqlParameterSource(
                SPDAOConstants.COLUMNNAMESPACE, namespace);
        return (Integer) this.transactionTemplate.execute(new CustomTransactionCallback(
                SPDAOConstants.DELETE_QUERY_FOR_NAMESPACE, namedParams));
    }

    /**
     * Check whether a group-namespace pair exists in user data store.
     *
     * @param groupId group ID, NOT the group name
     * @param namespace
     * @return
     */
    public boolean doesGroupExist(final int groupId, final String namespace) {
        return (null == namespace) ? null
                : (Boolean) this.simpleJdbcTemplate.getJdbcOperations().execute(new CustomCallableStatementCreator(
                UserGroupMembership.getIdPattern(groupId), namespace), new CustomCallableStatementCallback());
    }

    /**
     * Check whether a user-namespace pair exists in user data store.
     *
     * @param userId user ID, NOT the user's login name
     * @param namespace
     * @return
     */
    public boolean doesUserExist(final int userId, final String namespace) {
        return (null == namespace) ? false
                : (Boolean) this.simpleJdbcTemplate.getJdbcOperations().execute(new CustomCallableStatementCreator(
                UserGroupMembership.getIdPattern(userId), namespace), new CustomCallableStatementCallback());
    }
}
