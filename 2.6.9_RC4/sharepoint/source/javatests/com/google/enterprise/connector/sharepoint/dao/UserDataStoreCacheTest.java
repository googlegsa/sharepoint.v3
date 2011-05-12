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
import com.google.enterprise.connector.sharepoint.cache.UserDataStoreCache;

import java.util.Collection;

import junit.framework.TestCase;

public class UserDataStoreCacheTest extends TestCase {
    UserDataStoreCache<UserGroupMembership> testCache;

    protected void setUp() throws Exception {
        super.setUp();
        testCache = new UserDataStoreCache<UserGroupMembership>();
    }

    public void testAddMemberships() {
        String namespace = TestConfiguration.sharepointUrl;
        Collection<UserGroupMembership> memberships = null;
        try {
            memberships = TestConfiguration.getMembershipsForNameSpace(namespace);
        } catch (Exception e) {
            fail("Could not get test memberships...");
        }

        for (UserGroupMembership membership : memberships) {
            testCache.add(membership);
        }

         for (UserGroupMembership membership : memberships) {
            assertTrue(testCache.contains(membership));
        }
    }

    public void testRemoveUsingUserNamespaceView() {
        String namespace = TestConfiguration.sharepointUrl;
        Collection<UserGroupMembership> memberships = null;
        try {
            memberships = TestConfiguration.getMembershipsForNameSpace(namespace);
        } catch (Exception e) {
            fail("Could not get test memberships...");
        }

        for (UserGroupMembership membership : memberships) {
            testCache.add(membership);
        }

        for (UserGroupMembership membership : memberships) {
            assertTrue(testCache.contains(membership));
        }

        UserGroupMembership membership = memberships.iterator().next();
        UserGroupMembership tmpMembership = new UserGroupMembership();
        tmpMembership.setUserId(membership.getUserId());
        tmpMembership.setUserName(membership.getUserName());
        tmpMembership.setNamespace(namespace);
        testCache.removeUsingUserNamespaceView(tmpMembership);
        assertFalse(testCache.contains(membership));
    }

    public void testRemoveUsingGroupNamespaceView() {
        String namespace = TestConfiguration.sharepointUrl;
        Collection<UserGroupMembership> memberships = null;
        try {
            memberships = TestConfiguration.getMembershipsForNameSpace(namespace);
        } catch (Exception e) {
            fail("Could not get test memberships...");
        }

        for (UserGroupMembership membership : memberships) {
            testCache.add(membership);
        }

        for (UserGroupMembership membership : memberships) {
            assertTrue(testCache.contains(membership));
        }

        UserGroupMembership membership = memberships.iterator().next();
        UserGroupMembership tmpMembership = new UserGroupMembership();
        tmpMembership.setGroupId(membership.getGroupId());
        tmpMembership.setGroupName(membership.getGroupName());
        tmpMembership.setNamespace(namespace);
        testCache.removeUsingGroupNamespaceView(tmpMembership);
        assertFalse(testCache.contains(membership));
    }

    public void testRemoveUsingNamespaceView() {
        String namespace = TestConfiguration.sharepointUrl;
        Collection<UserGroupMembership> memberships = null;
        try {
            memberships = TestConfiguration.getMembershipsForNameSpace(namespace);
        } catch (Exception e) {
            fail("Could not get test memberships...");
        }

        for (UserGroupMembership membership : memberships) {
            testCache.add(membership);
        }

        for (UserGroupMembership membership : memberships) {
            assertTrue(testCache.contains(membership));
        }

        UserGroupMembership tmpMembership = new UserGroupMembership();
        tmpMembership.setNamespace(namespace);
        testCache.removeUsingNamespaceView(tmpMembership);

        for (UserGroupMembership membership : memberships) {
            assertFalse(testCache.contains(membership));
        }
    }

    public void testGC() {
        int trial = 100000;
        // try adding memberships till the time memory gets low. Ensure that no
        // strong reference is kept for the memberships
        int i = 1;
        try {
            do {
                UserGroupMembership membership = new UserGroupMembership(1,
                        "user" + i, i, "group" + i, "namespace" + (i % 50));
                testCache.add(membership);
                assertTrue(testCache.size() <= i);
                if (i % 2000 == 0) {
                    testCache.clearCache();
                }
                ++i;
            } while (trial-- != 0);
            // after handleEnqued, the elements in cache will go down and the
            // loop will exit
            assertTrue(true);
        } catch (Throwable e) {
            // probably OutOfMemory error
            fail(e.getMessage());
        }
    }
}
