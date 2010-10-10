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

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * A basic DAO implementation that can be extended for multiple types of DAOs.
 * Currently, there is only one DAO extending this class,
 * {@link UserDataStoreDAO}
 * <p>
 * The most highlighted feature of this class is the fall-back implementation in
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
public class SimpleSharePointDAO extends SimpleJdbcDaoSupport
        implements SharePointDAO {
    private final Logger LOGGER = Logger.getLogger(SimpleSharePointDAO.class.getName());
    private QueryProvider queryProvider;

    protected SimpleSharePointDAO(DataSource dataSource,
            QueryProvider queryProvider) throws SharepointException {
        if (null == dataSource || null == queryProvider) {
            throw new NullPointerException("DataSource/QueryProvider is null. ");
        }
        setDataSource(dataSource);
        this.queryProvider = queryProvider;
        Connection con = null;
        try {
            con = getConnection();
        } catch (Exception e) {
            throw new SharepointException(
                    "Could not create the database conection for specified data source",
                    e);
        }
        if(null == con) {
            throw new SharepointException("Could not create the database conection for specified data source");
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
     * @param params an array of {@link SqlParameterSource}; each representing
     *            the parameters to construct one SQL query. Hence, The length
     *            of the array will indicate the number of SQL queries executed
     *            in batch.
     * @param query query to be executed specified as {@link Query}
     * @return status of each query execution (=no. of rows updated) in the same
     *         order in which the queries were specified
     * @throws SharepointException
     */
    public int[] batchUpdate(Query query, SqlParameterSource[] params)
            throws SharepointException {
        if (null == params || 0 == params.length) {
            return null;
        }

        int[] batchStatus = null;
        try {
            batchStatus = getSimpleJdbcTemplate().batchUpdate(getSqlQuery(query), params);
            LOGGER.info("BatchUpdate completed successfully for #"
                    + batchStatus.length + " records. Query [ " + query + " ] ");
        } catch (Exception e) {
            if (null == e.getCause()
                    || !(e.getCause() instanceof BatchUpdateException)) {
                LOGGER.log(Level.WARNING, "BatchUpdate failed for query [  "
                        + query + " ]", e);
            } else {
                batchStatus = handleBatchUpdateException((BatchUpdateException) e.getCause(), query, params);
                LOGGER.info("BatchUpdate completed with a fallback for #"
                        + batchStatus.length + " records. Query [ " + query
                        + " ] ");
            }
        } catch (Throwable t) {
            // This would be an error. No point in retrying, so no
            // fall-back..
            throw new SharepointException("Batch execution failed abruptly!! ",
                    t);
        }

        return batchStatus;
    }

    /**
     * Analyze the batch exception, identifies the exact position where the
     * execution was stopped. Fall-back to individual query execution from this
     * index
     *
     * @param batchUpdateException the exception to be handled
     * @param params an array of {@link SqlParameterSource}; each representing
     *            the parameters to construct one SQL query. Hence, The length
     *            of the array will indicate the number of SQL queries executed
     *            in batch.
     * @param query query to be executed specified as {@link Query}
     * @return status of each query execution (=no. of rows updated) in the same
     *         order in which the queries were specified
     */
    public int[] handleBatchUpdateException(
            BatchUpdateException batchUpdateException, Query query,
            SqlParameterSource[] params) {
        // the position where batch execution was stopped
        int initPos = 0;
        int[] batchStatus = null;
        int[] optimisticBatchStatus = batchUpdateException.getUpdateCounts();
        if (null != optimisticBatchStatus) {
            if (optimisticBatchStatus.length == params.length) {
                LOGGER.log(Level.FINE, "Not all the queries executed successfully however, the attempt for execution was made for all of them.", batchUpdateException);
                return optimisticBatchStatus;
            } else {
                LOGGER.log(Level.WARNING, "Batch update processing was stopped at index "
                        + (optimisticBatchStatus.length - 1)
                        + " of "
                        + (params.length - 1), batchUpdateException);
                initPos = optimisticBatchStatus.length;
                batchStatus = new int[params.length];
                System.arraycopy(optimisticBatchStatus, 0, batchStatus, 0, optimisticBatchStatus.length);
            }
        } else {
            batchStatus = new int[params.length];
        }

        LOGGER.log(Level.WARNING, "Falling back to individual query execution. starting from index "
                + initPos);

        while (initPos < params.length) {
            try {
                batchStatus[initPos] = update(query, params[initPos]);
            } catch (SharepointException e) {
                LOGGER.log(Level.WARNING, "Execution failed for query [ "
                        + query + " ]", e);
                batchStatus[initPos] = Statement.EXECUTE_FAILED;
            }
            ++initPos;
        }

        return batchStatus;
    }

    /**
     * Executes a single update query. Used after the fall back from batch mode
     *
     * @param param {@link SqlParameterSource} query parameter to construct the
     *            SQL query
     * @param query query to be executed specified as {@link Query}
     * @return status of the query execution (=no. of rows updated)
     * @throws SharepointException
     */
    public int update(Query query, SqlParameterSource param)
            throws SharepointException {
        int count = -1;
        try {
            count = getSimpleJdbcTemplate().update(getSqlQuery(query), param);
        } catch (DataIntegrityViolationException e) {
            LOGGER.log(Level.FINE, "entry already exists for " + param, e);
        } catch (Throwable e) {
            throw new SharepointException(
                    "Failed to add the record for parameter [ " + param
                            + " ]. ", e);
        }
        return count;
    }

    /**
     * Shorthand for getting queries through {@link QueryProvider}
     *
     * @param query
     * @return
     */
    protected String getSqlQuery(Query query) {
        return queryProvider.getSqlQuery(query);
    }

    public QueryProvider getQueryProvider() {
        return queryProvider;
    }
}
