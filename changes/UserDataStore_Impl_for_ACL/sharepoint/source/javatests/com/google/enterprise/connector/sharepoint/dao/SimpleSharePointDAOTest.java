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
import java.sql.Statement;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.dao.QueryBuilder.QueryType;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

/**
 * @author nitendra_thakur
 *
 */
public class SimpleSharePointDAOTest extends TestCase {
    SimpleSharePointDAO simpleSPDAO;
    QueryBuilder queryBuilder;
    List<UserGroupMembership> memberships = null;
    SqlParameterSource[] namedParams = null;
    String namespace;

    /*
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        queryBuilder = TestConfiguration.getUserDataStoreQueryBuilder();
        simpleSPDAO = new SimpleSharePointDAO(
                TestConfiguration.getUserDataSource(), queryBuilder) {
            @Override
            void confirmEntitiesExistence() throws SharepointException {
                // Assuming.. all the required entities exists
            }
        };

        namespace = TestConfiguration.sharepointUrl;
        memberships = TestConfiguration.getMembershipsForNameSpace(namespace);
        namedParams = new SqlParameterSource[memberships.size()];
        int paramCount = 0;
        for (UserGroupMembership membership : memberships) {
            MapSqlParameterSource param = new MapSqlParameterSource(
                    UserDataStoreQueryBuilder.COLUMNUSER,
                    membership.getComplexUserId());
            param.addValue(UserDataStoreQueryBuilder.COLUMNGROUP, membership.getComplexGroupId());
            param.addValue(UserDataStoreQueryBuilder.COLUMNNAMESPACE, membership.getNameSpace());
            namedParams[paramCount++] = param;
        }
    }

    /**
     * Test method for
     * {@link com.google.enterprise.connector.sharepoint.dao.SimpleSharePointDAO#batchUpdate(org.springframework.jdbc.core.namedparam.SqlParameterSource[], com.google.enterprise.connector.sharepoint.dao.QueryBuilder.Query)}
     * .
     */
    public void testBatchUpdate() {
        try {
            int[] status = simpleSPDAO.batchUpdate(namedParams, QueryType.UDS_INSERT);
            assertNotNull(status);
            assertEquals(status.length, namedParams.length);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testHandleBatchUpdateException() {
        String reason = "Testing.... ";
        String sqlState = "";
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
            int[] status = simpleSPDAO.handleBatchUpdateException(batchUpdateException, namedParams, queryBuilder.createQuery(QueryType.UDS_INSERT));
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

            BatchUpdateException batchUpdateException = new BatchUpdateException(reason,
                    sqlState, vendorCode, updateCounts);
            int[] status = simpleSPDAO.handleBatchUpdateException(batchUpdateException, namedParams, queryBuilder.createQuery(QueryType.UDS_INSERT));
            assertNotNull(status);
            assertEquals(status.length, namedParams.length);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
