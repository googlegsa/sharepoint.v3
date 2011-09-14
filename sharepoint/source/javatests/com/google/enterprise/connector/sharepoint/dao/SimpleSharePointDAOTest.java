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

import com.google.enterprise.connector.sharepoint.TestConfiguration;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.BatchUpdateException;
import java.sql.Statement;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @author nitendra_thakur
 */
public class SimpleSharePointDAOTest extends TestCase {
  SimpleSharePointDAO simpleSPDAO;
  QueryProvider queryProvider;
  Set<UserGroupMembership> memberships = null;
  String namespace;

  /*
   * (non-Javadoc)
   *
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    queryProvider = TestConfiguration.getUserDataStoreQueryProvider();
    simpleSPDAO = new SimpleSharePointDAO(
        TestConfiguration.getUserDataSource(), queryProvider);
    namespace = TestConfiguration.sharepointUrl;
    memberships = TestConfiguration.getMembershipsForNameSpace(namespace);
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.sharepoint.dao.SimpleSharePointDAO#batchUpdate(org.springframework.jdbc.core.namedparam.SqlParameterSource[], com.google.enterprise.connector.sharepoint.dao.QueryBuilder.Query)}
   * .
   */
  public void testBatchUpdate() {
    Query query = Query.UDS_INSERT;
    try {
      SqlParameterSource[] namedParams = new SqlParameterSource[memberships.size()];
      int count = 0;
      for (UserGroupMembership membership : memberships) {
        namedParams[count++] = query.createParameter(membership.getUserId(), membership.getUserName(), membership.getGroupId(), membership.getGroupName(), membership.getNamespace());
      }
      int[] status = simpleSPDAO.batchUpdate(query, namedParams);
      assertNotNull(status);
      assertEquals(status.length, namedParams.length);
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testHandleBatchUpdateException() {
    Query query = Query.UDS_INSERT;
    SqlParameterSource[] namedParams = new SqlParameterSource[memberships.size()];
    int count = 0;
    for (UserGroupMembership membership : memberships) {
      namedParams[count++] = query.createParameter(membership.getUserId(), membership.getUserName(), membership.getGroupId(), membership.getGroupName(), membership.getNamespace());
    }
    String sqlState = "";
    String reason = "";
    int vendorCode = 0;
    int[] updateCounts = null;
    // Scenario 1: When driver executes all the queries
    try {
      updateCounts = new int[namedParams.length];
      for (int i = 0; i < namedParams.length; ++i) {
        if (i % 2 == 0) {
          updateCounts[i] = Statement.EXECUTE_FAILED;
        } else {
          updateCounts[i] = 1;
        }
      }
      BatchUpdateException batchUpdateException = new BatchUpdateException(
          reason, sqlState, vendorCode, updateCounts);
      int[] status = simpleSPDAO.handleBatchUpdateExceptionForMSSQLAndMySQL(batchUpdateException, Query.UDS_INSERT, namedParams);
      assertNotNull(status);
      assertEquals(status.length, namedParams.length);
    } catch (Exception e) {
      fail(e.getMessage());
    }

    // Scenario 2: When driver stops execution when a query fails
    try {
      updateCounts = new int[namedParams.length - 1];
      for (int i = 0; i < namedParams.length - 1; ++i) {
        if (i % 2 == 0) {
          updateCounts[i] = Statement.EXECUTE_FAILED;
        } else {
          updateCounts[i] = 1;
        }
      }

      BatchUpdateException batchUpdateException = new BatchUpdateException(
          reason, sqlState, vendorCode, updateCounts);
      int[] status = simpleSPDAO.handleBatchUpdateExceptionForMSSQLAndMySQL(batchUpdateException, Query.UDS_INSERT, namedParams);
      assertNotNull(status);
      assertEquals(status.length, namedParams.length - 1);
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }
}
