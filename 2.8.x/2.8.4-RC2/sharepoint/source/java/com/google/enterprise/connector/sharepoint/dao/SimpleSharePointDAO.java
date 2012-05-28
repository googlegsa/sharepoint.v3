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

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.SQLException;
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
public class SimpleSharePointDAO extends SimpleJdbcDaoSupport implements
    SharePointDAO {
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
      LOGGER.config("Created data base connection for specified data source.");
    } catch (Exception e) {
      throw new SharepointException(
          "Could not create the database conection for specified data source",
          e);
    }
    if (null == con) {
      throw new SharepointException(
          "Could not create the database conection for specified data source");
    }
  }

  /**
   * Uses Spring's SimpleJdbcTemplate's batch update feature for executing
   * multiple update queries in one go. It also takes care of any failure that
   * might occur during the execution. Driver implementation may or may not
   * proceed to the next query execution in a batch if any one fails in between.
   * Here is a description of what happens when a failure occurs:
   * <p>
   * If the driver, at least, has attempted execution of all the queries in
   * batch, nothing will be done. exception will be just logged into the log as
   * a warning.
   * </p>
   * <p>
   * If the driver has stopped processing the queries because of any failure at
   * a particular index, than all the queries starting from that index are
   * executed individually.
   * </p>
   * <p>
   * If the driver has failed to execute the whole request due to any reason viz
   * it does not support batchUpdate etc., the queries are still send for
   * individual execution
   * </p>
   * One important of batchUpdate queries are that the exact failure of an
   * individual query is not known. However, whatever the reason is, it is
   * ensured to be unrecoverable. So, the best the connector can do and it does
   * is to log such events and proceed. Such scenarios, of course, can leave the
   * user data store in a bad state.
   *
   * @param params an array of {@link SqlParameterSource}; each representing the
   *          parameters to construct one SQL query. Hence, The length of the
   *          array will indicate the number of SQL queries executed in batch.
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
          || (!(e.getCause() instanceof BatchUpdateException) && !(e.getCause() instanceof SQLException))) {
        LOGGER.log(Level.WARNING, "BatchUpdate failed for query [  " + query
            + " ]", e);

      } else {
        if ((e.getCause() instanceof BatchUpdateException)) {
          batchStatus = handleBatchUpdateExceptionForMSSQLAndMySQL((BatchUpdateException) e.getCause(), query, params);
          LOGGER.info("BatchUpdate completed with a fallback for #"
              + batchStatus.length + " records. Query [ " + query + " ] ");
        } else {
          batchStatus = handleBatchUpdateExceptionForOracle((SQLException) e.getCause(), query, params);
          LOGGER.info("BatchUpdate completed with a fallback for #"
              + batchStatus.length + " records. Query [ " + query + " ] ");
        }
      }
    } catch (Throwable t) {
      // This would be an error. No point in retrying, so no
      // fall-back..
      throw new SharepointException("Batch execution failed abruptly!! ", t);
    }

    return batchStatus;
  }

  /**
   * Executes a single update query. Used after the fall back from batch mode
   *
   * @param query query to be executed specified as {@link Query}
   * @param param {@link SqlParameterSource} query parameter to construct the
   *          SQL query
     * @return status of the query execution (=no. of rows updated)
   * @throws SharepointException
   */
  public int update(Query query, SqlParameterSource param)
      throws SharepointException {
    int count = -1;
    try {
      count = getSimpleJdbcTemplate().update(getSqlQuery(query), param);
    } catch (DataIntegrityViolationException e) {
      LOGGER.log(Level.FINE, "entry already exists in user data store for the group name ["
          + param.getValue(SPConstants.GROUP_NAME)
          + "], and user name ["
          + param.getValue(SPConstants.USER_NAME) + "]");
    } catch (Throwable e) {
      throw new SharepointException("Failed to add the record for parameter [ "
          + param.getValue(SPConstants.GROUP_NAME) + "], user name ["
          + param.getValue(SPConstants.USER_NAME) + "]", e);
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

  /**
   * Oracle specific method to handle specific exception while executing batch
   * update. Oracle official document says that If any one of the batched
   * operations fails to complete successfully, execution stops and a
   * {@link BatchUpdateException} throws, but is it not true. Hence need to fall
   * back to individual query execution.
   *
   * @param batchUpdateException the exception to be handled
   * @param params query an array of {@link SqlParameterSource}; each
   *          representing the parameters to construct one SQL query. Hence, The
   *          length of the array will indicate the number of SQL queries
   *          executed in batch. status of each query execution (=no. of rows
   *          updated) in the same order in which the queries were specified
   * @throws SharepointException
   */
  public int[] handleBatchUpdateExceptionForOracle(
      SQLException batchUpdateException, Query query,
      SqlParameterSource[] params) throws SharepointException {
    int[] batchStatus = new int[params.length];
    for (int i = 0; i < params.length; ++i) {
      batchStatus[i] = update(query, params[i]);
    }
    return batchStatus;
  }

  /**
   * <p>
   * Analyze the batch exception, identifies the return values from various
   * driver implementations during {@link BatchUpdateException}and fall-back to
   * individual query execution.
   * </p>
   * <p>
   * In case of MS SQL, the driver implementation never try to execute insert
   * query if there is at least one row matches against given memberships and
   * returns -3 always.
   * </p>
   * <p>
   * In case of MYSQL, it attempt to insert all new records and return -3 for
   * existing memberships.
   * </p>
   *
   * @param batchUpdateException the exception to be handled
   * @param params an array of {@link SqlParameterSource}; each representing the
   *          parameters to construct one SQL query. Hence, The length of the
   *          array will indicate the number of SQL queries executed in batch.
   * @param query query to be executed specified as {@link Query}
   * @return status of each query execution (=no. of rows updated) in the same
   *         order in which the queries were specified
   */
  public int[] handleBatchUpdateExceptionForMSSQLAndMySQL(
      BatchUpdateException batchUpdateException, Query query,
      SqlParameterSource[] params) {
    boolean fallBack = false;
    // In case of MS SQL need to analyze the batch status array as it
    // returns -3 always and then fall back to individual query execution.
    int[] optimisticBatchStatus = batchUpdateException.getUpdateCounts();
    if (null != optimisticBatchStatus) {
      for (int i = 0; i < optimisticBatchStatus.length; i++) {
        if (optimisticBatchStatus[i] == SPConstants.MINUS_THREE) {
          fallBack = true;
        } else {
          LOGGER.info("Fall back set to false.");
          fallBack = false;
          break;
        }
      }
      // getUpdateCount() should return params.lenth.
      if (optimisticBatchStatus.length == params.length && !fallBack) {
        // for MySQL since all queries were tried and few/all failed, it
        // does not make sense to execute them again individually rather
        // than batch as they will fail with same error. However, for
        // MSSQL the connector should retry.
        LOGGER.log(Level.FINE, "Not all the queries executed successfully however, the attempt for execution was made for all of them.", batchUpdateException);
        return optimisticBatchStatus;
      } else {
        if (fallBack) {
          LOGGER.log(Level.WARNING, "Falling back to individual query execution.");
          for (int i = 0; i < optimisticBatchStatus.length; i++) {
            try {
              optimisticBatchStatus[i] = update(query, params[i]);
            } catch (SharepointException e) {
              LOGGER.log(Level.WARNING, "Failed to add record to user data store with the group name ["
                  + params[i].getValue(SPConstants.GROUP_NAME)
                  + ", user name ["
                  + params[i].getValue(SPConstants.USERNAME)
                  + "]", e);
            }
          }
        }
        return optimisticBatchStatus;
      }
    }
    return null;
  }
}
