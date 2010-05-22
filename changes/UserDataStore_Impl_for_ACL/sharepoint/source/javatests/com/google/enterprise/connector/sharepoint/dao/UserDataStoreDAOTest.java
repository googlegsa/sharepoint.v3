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

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.dao.SharePointDAO.DBConfig;

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
        DBConfig dbConfig = new DBConfig(TestConfiguration.driverClass,
                TestConfiguration.dbUrl, TestConfiguration.dbUsername,
                TestConfiguration.dbPassword);
        userDataStoreDAO = new UserDataStoreDAO(dbConfig);
    }

    /**
     * Test method for
     * {@link com.google.enterprise.connector.sharepoint.dao.UserDataStoreDAO#getAllGroupsForUser(java.lang.String)}
     * .
     */
    public void testMemberships() {
        UserGroupMembership membership = new UserGroupMembership(
                "gdc04\\nitin", "Users", "http://persistent.co.in");
        try {

            userDataStoreDAO.addMembership(membership);

            List<String> testGroups = userDataStoreDAO.getAllGroupsForUser(membership.getUserId());
            assertNotNull(testGroups);
            assertEquals(testGroups.size(), 1);
            assertEquals("Users", testGroups.get(0));

            int i = userDataStoreDAO.removeUserMemberships(membership.getUserId(), membership.getNameSpace());
            assertEquals(1, i);

            userDataStoreDAO.addMembership(membership);
            i = userDataStoreDAO.removeGroupMemberships(membership.getGroupId(), membership.getNameSpace());
            assertEquals(1, i);

            userDataStoreDAO.addMembership(membership);
            i = userDataStoreDAO.removeAllMembershipsFromNamespace(membership.getNameSpace());
            assertEquals(1, i);

            testGroups = userDataStoreDAO.getAllGroupsForUser("domain\\nitin");
            assertNotNull(testGroups);
            assertEquals(testGroups.size(), 0);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
