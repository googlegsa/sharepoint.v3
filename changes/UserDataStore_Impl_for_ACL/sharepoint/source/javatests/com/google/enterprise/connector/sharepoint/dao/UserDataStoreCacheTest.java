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

public class UserDataStoreCacheTest extends TestCase {
    UserDataStoreCache<UserGroupMembership> testCache;

    protected void setUp() throws Exception {
        super.setUp();
        testCache = new UserDataStoreCache<UserGroupMembership>(3);
    }

    public void testAddMemberships() {
        UserGroupMembership membership1 = null;
        UserGroupMembership membership2 = null;
        UserGroupMembership membership3 = null;
        try {
            String namespace = TestConfiguration.sharepointUrl;

            List<UserGroupMembership> memberships = new ArrayList<UserGroupMembership>();
            membership1 = new UserGroupMembership("[1]user1", "[2]group1",
                    namespace);
            memberships.add(membership1);
            membership2 = new UserGroupMembership("[2]user2", "[2]group1",
                    namespace);
            memberships.add(membership2);
            membership3 = new UserGroupMembership("[3]user3", "[2]group2",
                    namespace);
            memberships.add(membership3);
            testCache.addMemberships(memberships);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertTrue(testCache.contains(membership1));
        assertTrue(testCache.contains(membership2));
        assertTrue(testCache.contains(membership3));
    }

    public void removeUserMembershipsFromNamespace() {
        UserGroupMembership membership1 = null;
        UserGroupMembership membership2 = null;
        UserGroupMembership membership3 = null;
        String namespace = TestConfiguration.sharepointUrl;
        try {
            List<UserGroupMembership> memberships = new ArrayList<UserGroupMembership>();
            membership1 = new UserGroupMembership("[1]user1", "[2]group1",
                    namespace);
            memberships.add(membership1);
            membership2 = new UserGroupMembership("[2]user2", "[2]group1",
                    namespace);
            memberships.add(membership2);
            membership3 = new UserGroupMembership("[3]user3", "[2]group2",
                    namespace);
            memberships.add(membership3);
            testCache.addMemberships(memberships);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertTrue(testCache.contains(membership1));
        assertTrue(testCache.contains(membership2));
        assertTrue(testCache.contains(membership3));

        try {
            List<Integer> userIds = new ArrayList<Integer>();
            userIds.add(membership1.getUserId());
            userIds.add(membership2.getUserId());
            userIds.add(membership3.getUserId());
            testCache.removeUserMembershipsFromNamespace(userIds, namespace);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertFalse(testCache.contains(membership1));
        assertFalse(testCache.contains(membership2));
        assertFalse(testCache.contains(membership3));
    }

    public void removeGroupMembershipsFromNamespace() {
        UserGroupMembership membership1 = null;
        UserGroupMembership membership2 = null;
        UserGroupMembership membership3 = null;
        String namespace = TestConfiguration.sharepointUrl;
        try {
            List<UserGroupMembership> memberships = new ArrayList<UserGroupMembership>();
            membership1 = new UserGroupMembership("[1]user1", "[2]group1",
                    namespace);
            memberships.add(membership1);
            membership2 = new UserGroupMembership("[2]user2", "[2]group1",
                    namespace);
            memberships.add(membership2);
            membership3 = new UserGroupMembership("[3]user3", "[2]group2",
                    namespace);
            memberships.add(membership3);
            testCache.addMemberships(memberships);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertTrue(testCache.contains(membership1));
        assertTrue(testCache.contains(membership2));
        assertTrue(testCache.contains(membership3));

        try {
            List<Integer> groupIds = new ArrayList<Integer>();
            groupIds.add(membership1.getGroupId());
            groupIds.add(membership2.getGroupId());
            groupIds.add(membership3.getGroupId());
            testCache.removeUserMembershipsFromNamespace(groupIds, namespace);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertFalse(testCache.contains(membership1));
        assertFalse(testCache.contains(membership2));
        assertFalse(testCache.contains(membership3));
    }

    public void removeAllMembershipsFromNamespace() {
        UserGroupMembership membership1 = null;
        UserGroupMembership membership2 = null;
        UserGroupMembership membership3 = null;
        String namespace = TestConfiguration.sharepointUrl;
        try {
            List<UserGroupMembership> memberships = new ArrayList<UserGroupMembership>();
            membership1 = new UserGroupMembership("[1]user1", "[2]group1",
                    namespace);
            memberships.add(membership1);
            membership2 = new UserGroupMembership("[2]user2", "[2]group1",
                    namespace);
            memberships.add(membership2);
            membership3 = new UserGroupMembership("[3]user3", "[2]group2",
                    namespace);
            memberships.add(membership3);
            testCache.addMemberships(memberships);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertTrue(testCache.contains(membership1));
        assertTrue(testCache.contains(membership2));
        assertTrue(testCache.contains(membership3));

        try {
            List<String> namespaces = new ArrayList<String>();
            namespaces.add(namespace);
            testCache.removeAllMembershipsFromNamespace(namespaces);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertFalse(testCache.contains(membership1));
        assertFalse(testCache.contains(membership2));
        assertFalse(testCache.contains(membership3));
    }
}
