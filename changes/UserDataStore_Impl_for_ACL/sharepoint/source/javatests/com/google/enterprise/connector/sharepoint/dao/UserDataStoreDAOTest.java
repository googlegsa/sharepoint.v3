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

import java.util.List;

import junit.framework.TestCase;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.google.enterprise.connector.sharepoint.TestConfiguration;

/**
 * @author nitendra_thakur
 *
 */
public class UserDataStoreDAOTest extends TestCase {
    UserDataStoreDAO userDataStoreDAO;

    /*
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(TestConfiguration.driverClass);
        dataSource.setUrl(TestConfiguration.dbUrl);
        dataSource.setUsername(TestConfiguration.dbUsername);
        dataSource.setPassword(TestConfiguration.dbPassword);
        userDataStoreDAO = new UserDataStoreDAO(dataSource);
    }

    /**
     * Test method for
     * {@link com.google.enterprise.connector.sharepoint.dao.UserDataStoreDAO#getAllGroupsForUser(java.lang.String)}
     * .
     */
    public void testMemberships() {
        try {
            UserGroupMembership membership = new UserGroupMembership("[1]user",
                    "[2]group", "namespace");
            userDataStoreDAO.addMembership(membership);

            List<UserGroupMembership> testGroups = userDataStoreDAO.getAllGroupsForUser(membership.getUserName());
            assertNotNull(testGroups);
            // Assuming there were no data in the table when the earlier record
            // was added
            assertEquals(testGroups.size(), 1);
            assertEquals(membership, testGroups.get(0));

            int i = userDataStoreDAO.removeUserMemberships(membership.getUserId(), membership.getNameSpace());
            assertEquals(1, i);

            userDataStoreDAO.addMembership(membership);
            i = userDataStoreDAO.removeGroupMemberships(membership.getGroupId(), membership.getNameSpace());
            assertEquals(1, i);

            userDataStoreDAO.addMembership(membership);
            i = userDataStoreDAO.removeAllMembershipsFromNamespace(membership.getNameSpace());
            assertEquals(1, i);

            testGroups = userDataStoreDAO.getAllGroupsForUser(membership.getUserName());
            assertNotNull(testGroups);
            assertEquals(testGroups.size(), 0);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
