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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

/**
 * @author nitendra_thakur
 *
 */
public class UserDataStoreDAOTest extends TestCase {
    String namespace;
    UserDataStoreDAO userDataStoreDAO;
    Set<UserGroupMembership> memberships;
    /*
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        userDataStoreDAO = new UserDataStoreDAO(
                TestConfiguration.getUserDataSource(),
                TestConfiguration.getUserDataStoreQueryBuilder());
        namespace = TestConfiguration.sharepointUrl;
        memberships = TestConfiguration.getMembershipsForNameSpace(namespace);
        userDataStoreDAO.addMemberships(memberships);

    }

    @Override
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
    }

    public void testAddMemberships() {
        try {
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
        try {
            Set<Integer> userIds = new TreeSet<Integer>();
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
        try {
            Set<Integer> groupIds = new TreeSet<Integer>();
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
        try {
            Set<String> namespaces = new TreeSet<String>();
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

    public void testSyncGroupMembership() {
        try {
            userDataStoreDAO.syncGroupMemberships(memberships, namespace);
            for (UserGroupMembership membership : memberships) {
                List<UserGroupMembership> userMemberships = userDataStoreDAO.getAllMembershipsForUser(membership.getUserName());
                assertNotNull(userMemberships);
                assertTrue(userMemberships.contains(membership));
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
