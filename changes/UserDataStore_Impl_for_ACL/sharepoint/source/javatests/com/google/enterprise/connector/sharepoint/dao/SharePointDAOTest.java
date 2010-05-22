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

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.dao.SharePointDAO.DBConfig;

/**
 * A base class for doing database related operations
 *
 * @author nitendra_thakur
 */
public class SharePointDAOTest extends TestCase {
    private SharePointDAO spDAO;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DBConfig dbConfig = new DBConfig(TestConfiguration.driverClass,
                TestConfiguration.dbUrl, TestConfiguration.dbUsername,
                TestConfiguration.dbPassword);
        spDAO = new SharePointDAO(dbConfig);
    }

    public void testConnect() {
        try {
            boolean status = spDAO.connect();
            assertTrue(status);
            spDAO.closeConnection();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testExecuteQuery() {
        try {
            String query = "SELECT * FROM UserGroupMemberships";
            ResultSet result = spDAO.executeQuery(query);
            assertNotNull(result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
