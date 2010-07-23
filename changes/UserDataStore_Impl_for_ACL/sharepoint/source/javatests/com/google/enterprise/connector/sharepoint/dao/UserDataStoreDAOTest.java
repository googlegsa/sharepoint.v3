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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

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
        userDataStoreDAO = new UserDataStoreDAO(
                TestConfiguration.getUserDataSource(),
                TestConfiguration.getUserDataStoreQueryBuilder(), 100);
    }

    @Override
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
    }

    public void testAddMemberships() {
        String namespace = TestConfiguration.sharepointUrl;
        try {
            List<UserGroupMembership> memberships = TestConfiguration.getMembershipsForNameSpace(namespace);
            userDataStoreDAO.addMemberships(memberships);
            for (UserGroupMembership membership : memberships) {
                List<UserGroupMembership> userMemberships = userDataStoreDAO.getAllMembershipsForUser(membership.getUserName());
                assertNotNull(userMemberships);
                assertTrue(userMemberships.contains(membership));
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testRemoveUserMemberships() {
        String namespace = TestConfiguration.sharepointUrl;
        try {
            List<UserGroupMembership> memberships = TestConfiguration.getMembershipsForNameSpace(TestConfiguration.sharepointUrl);
            List<Integer> userIds = new ArrayList<Integer>();
            for (UserGroupMembership membership : memberships) {
                userIds.add(membership.getUserId());
                userIds.add(membership.getUserId());
                userIds.add(membership.getUserId());
            }
            userDataStoreDAO.removeUserMembershipsFromNamespace(userIds, namespace);
            for (UserGroupMembership membership : memberships) {
                List<UserGroupMembership> userMemberships = userDataStoreDAO.getAllMembershipsForUser(membership.getUserName());
                assertNotNull(userMemberships);
                assertFalse(userMemberships.contains(membership));
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testRemoveGroupMemberships() {
        String namespace = TestConfiguration.sharepointUrl;
        try {
            List<UserGroupMembership> memberships = TestConfiguration.getMembershipsForNameSpace(TestConfiguration.sharepointUrl);
            List<Integer> groupIds = new ArrayList<Integer>();
            for (UserGroupMembership membership : memberships) {
                groupIds.add(membership.getGroupId());
                groupIds.add(membership.getGroupId());
                groupIds.add(membership.getGroupId());
            }
            userDataStoreDAO.removeGroupMembershipsFromNamespace(groupIds, namespace);
            for (UserGroupMembership membership : memberships) {
                List<UserGroupMembership> userMemberships = userDataStoreDAO.getAllMembershipsForUser(membership.getUserName());
                assertNotNull(userMemberships);
                assertFalse(userMemberships.contains(membership));
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testNameSpaceMemberships() {
        String namespace = TestConfiguration.sharepointUrl;
        try {
            List<UserGroupMembership> memberships = TestConfiguration.getMembershipsForNameSpace(TestConfiguration.sharepointUrl);
            List<String> namespaces = new ArrayList<String>();
            namespaces.add(namespace);
            userDataStoreDAO.removeAllMembershipsFromNamespace(namespaces);
            for (UserGroupMembership membership : memberships) {
                List<UserGroupMembership> userMemberships = userDataStoreDAO.getAllMembershipsForUser(membership.getUserName());
                assertNotNull(userMemberships);
                assertFalse(userMemberships.contains(membership));
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
