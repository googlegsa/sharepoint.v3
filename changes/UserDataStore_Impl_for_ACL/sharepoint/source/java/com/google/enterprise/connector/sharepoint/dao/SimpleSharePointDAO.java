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

import java.sql.BatchUpdateException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.google.enterprise.connector.sharepoint.dao.QueryBuilder.QueryType;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

/**
 * A basic DAO implementation that can be extended for multiple tyopes of DAOs.
 * Currently, there is only one DAO extending this class,
 * {@link UserDataStoreDAO}
 * <p>
 * The most highlighted feature of this class is the fallback implementation in
 * case a batch update fails. The class provides appropriate APIs for executing
 * update queries in batch and also ensures that no loss should occur to other
 * queries when any one of them fails. This is achieved by falling back to
 * individual query execution when the batch execution stops at any index due to
 * the failure. Refer to <a href="http://download.oracle.com/docs/cd/E17476_01/javase1.5.0/docs/api/java/sql/Statement.html#executeBatch%28%29"
 * > http://download.oracle.com/docs/cd/E17476_01/javase1.5.0/docs/api/java/sql/
 * Statement.html#executeBatch%28%29</a> for more details.
 *
 * @author nitendra_thakur
 */
public abstract class SimpleSharePointDAO extends JdbcDaoSupport implements
        SharePointDAO {
    private final Logger LOGGER = Logger.getLogger(SimpleSharePointDAO.class.getName());
    SimpleJdbcTemplate simpleJdbcTemplate;
    QueryBuilder queryBuilder;

    SimpleSharePointDAO(DataSource dataSource, QueryBuilder queryBuilder)
            throws SharepointException {
        if (null == dataSource || null == queryBuilder) {
            throw new SharepointException(
                    "Either data source or query builder was null ");
        }
        this.queryBuilder = queryBuilder;

        setDataSource(dataSource);
        setJdbcTemplate(new JdbcTemplate(dataSource));
        this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
        confirmDBExistence();

        // Once, the existence of database is confirmed, now
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
        confirmEntitiesExistence();
    }

    /**
     * Check if the database exists or not.
     *
     * @throws SharepointException
     */
    void confirmDBExistence() throws SharepointException {
        DatabaseMetaData dbm = null;
        try {
            dbm = getConnection().getMetaData();
            ResultSet dbs = dbm.getCatalogs();
            if (null != dbs) {
                boolean dbExists = false;
                while (dbs.next()) {
                    if (queryBuilder.getDatabase().equalsIgnoreCase(dbs.getString("TABLE_CAT"))) {
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
     * not, creates them. As a minimal check, this method only checks for the
     * existence of tables. Child of this class can extend this for various such
     * checks
     *
     * @throws SharepointException
     */
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
                    this.simpleJdbcTemplate.update(queryBuilder.createQuery(QueryType.CREATETABLEQUERY).getQuery());
                }
                resultSet.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception occurred while getting the table information from the database metadata. ", e);
        }
    }

    /**
     * Uses Spring's SimpleJdbcTemplate's batch update feature for executing
     * multiple update queries in one go. It also takes care of any failure that
     * might occur during the execution. Driver implementation may or may not
     * proceed to the next query execution in a batch if any one fails in
     * between. Here is a description of what happens when a failure occurs:
     * <p>
     * If the driver, at least, has attempted execution of all the queries in
     * batch, nothing will be done. exception will be just logged into the log
     * as a warning.
     * </p>
     * <p>
     * If the driver has stopped processing the queries because of any failure
     * at a particular index, than all the queries starting from that index are
     * executed individually.
     * </p>
     * <p>
     * If the driver has failed to execute the whole request due to any reason
     * viz it does not support batchUpdate etc., the queries are still send for
     * individual execution
     * </p>
     * One important of batchUpdate queries are that the exact failure of an
     * individual query is not known. However, whatever the reason is, it is
     * ensured to be unrecoverable. So, the best the connector can do and it
     * does is to log such events and proceed. Such scenarios, of course, can
     * leave the user data store in a bad state.
     *
     * @param params query parameters
     * @param query SQL query
     * @return status of each query execution (=no. of rows updated) in the same
     *         order in which the queries were specified
     * @throws SharepointException
     */
    int[] batchUpdate(SqlParameterSource[] params, QueryType query)
            throws SharepointException {
        if (null == params || 0 == params.length) {
            return null;
        }
        // used when the batch update fails
        int[] batchStatus = null;

        // the position where the failure occurred
        int[] initPos = new int[1];

        // the execution status received after the batch execution. This can be
        // the final status if the batch execution completed successfully
        int[] optimisticBatchStatus = null;

        try {
            optimisticBatchStatus = simpleJdbcTemplate.batchUpdate(queryBuilder.createQuery(query).getQuery(), params);
            LOGGER.info("BatchUpdate completed successfully for #"
                    + optimisticBatchStatus.length + " records. Query [ "
                    + query + " ] ");
            return optimisticBatchStatus;
        } catch (Exception e) {
            if (null == e.getCause()
                    || !(e.getCause() instanceof BatchUpdateException)) {
                LOGGER.log(Level.WARNING, "BatchUpdate failed for query [  "
                        + query + " ]", e);
            } else {
                BatchUpdateException batchUpdateException = (BatchUpdateException) e.getCause();
                batchStatus = handleBatchUpdateException(batchUpdateException, optimisticBatchStatus, params.length, initPos);
                if (null != batchStatus && batchStatus.length == params.length) {
                    return batchStatus;
                }
            }
        } catch (Throwable t) {
            // This would be an error. No point in retrying, so no
            // fallback..
            throw new SharepointException("Batch execution failed abruptly!! ",
                    t);
        }

        LOGGER.log(Level.WARNING, "Falling back to individual query execution. starting from index "
                + initPos[0]);
        return fallbackAddMemerbships(batchStatus, params, initPos[0], query);
    }

    /**
     * fall back the query execution from batch mode to individual mode
     *
     * @param batchStatus the final status to be returned at the end
     * @param params query parameters
     * @param initPos position from where the queries are to be executed
     *            individually
     * @param query SQL query
     * @return status of each query execution (=no. of rows updated) in the same
     *         order in which the queries were specified
     */
    int[] fallbackAddMemerbships(int[] batchStatus,
            SqlParameterSource[] params, int initPos, QueryType query) {
        if (null == batchStatus) {
            LOGGER.warning("batch status is null after batch update. Initializing it for fallback. All the queries will be executed individually");
            batchStatus = new int[params.length];
        }

        while (initPos < params.length) {
            try {
                batchStatus[initPos] = update(params[initPos], query);
            } catch (SharepointException e) {
                LOGGER.log(Level.WARNING, "", e);
                batchStatus[initPos] = Statement.EXECUTE_FAILED;
            }
            ++initPos;
        }
        return batchStatus;
    }

    /**
     * Executes a single update query. Used after the fall back from batch mode
     *
     * @param params query parameters
     * @param query SQL query
     * @return status of the query execution (=no. of rows updated)
     * @throws SharepointException
     */
    int update(SqlParameterSource param, QueryType query)
            throws SharepointException {
        int count = -1;
        if (null == param) {
            LOGGER.log(Level.WARNING, "Specified record is Invalid [ " + param
                    + " ] ");
            return Statement.EXECUTE_FAILED;
        }
        try {
            count = this.simpleJdbcTemplate.update(queryBuilder.createQuery(query).getQuery(), param);
        } catch (DataIntegrityViolationException e) {
            // During the connector's crawl, case of duplicate insertion is
            // going to occur very frequently. This need not be considered as a
            // severe case.
            // TODO: implement a cache to reduce the integrity violation
            LOGGER.log(Level.FINE, "entry already exists for " + param, e);
        } catch (Throwable e) {
            throw new SharepointException(
                    "Failed to add the record for parameter [ " + param
                            + " ]. ", e);
        }
        return count;
    }

    /**
     * Analyze the batch exception, identifies the exact position where the
     * execution was stopped to determine how and from where the fall back
     * should proceed. Initiates the fall back based on its finding
     *
     * @param batchUpdateException the exception to be handled
     * @param optimisticBatchStatus the status as retrieved after the batch
     *            execution
     * @param totalQueries total no. of queries that was passed for batch
     *            execution
     * @param initPos an array with of length one tends to store the position at
     *            which the batch execution was stopped
     * @return status of each query execution (=no. of rows updated) in the same
     *         order in which the queries were specified
     */
    int[] handleBatchUpdateException(
            BatchUpdateException batchUpdateException,
            int[] optimisticBatchStatus, int totalQueries, int[] initPos) {
        optimisticBatchStatus = batchUpdateException.getUpdateCounts();
        if (null == optimisticBatchStatus) {
            return null;
        }
        if (totalQueries == optimisticBatchStatus.length) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Not all the queries executed successfully however, the attempt for execution was made for all of them.", batchUpdateException);
            } else {
                LOGGER.log(Level.WARNING, "Not all the queries executed successfully however, the attempt for execution was made for all of them.");
            }
            return optimisticBatchStatus;
        }

        while (initPos[0] < optimisticBatchStatus.length
                && optimisticBatchStatus[++initPos[0]] != Statement.EXECUTE_FAILED)
            ;
        LOGGER.log(Level.WARNING, "Batch update processing was stopped at index "
                + initPos
                + " of "
                + totalQueries
                + " because the command at this index could not be executed.  ", batchUpdateException);
        int[] batchStatus = new int[totalQueries];
        for (int i = 0; i < optimisticBatchStatus.length; ++i) {
            batchStatus[i] = optimisticBatchStatus[i];
        }
        return batchStatus;
    }
}
