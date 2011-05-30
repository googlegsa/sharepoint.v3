//Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.sharepoint.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.enterprise.connector.sharepoint.TestConfiguration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Test class for {@link LdapUserGroupsCache}
 *
 * @author nageswara_sura
 */
public class LdapUserGroupCacheTest {
    private LdapUserGroupsCache<Object, Object> lugCacheStore;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        this.lugCacheStore = new LdapUserGroupsCache<Object, Object>(
                TestConfiguration.refreshInterval, 100000);
    }

    @Test
    public void perf() throws InterruptedException {
        long start = System.currentTimeMillis();
        Set<String> membership = new HashSet<String>();
        Runtime rt = Runtime.getRuntime();
        // test for free amount of memory * 900 number of entries.
        // it assumes that for a given 1 MB RAM memory, the cache can group up
        // to 500 memberships.

        long finalM = (rt.maxMemory() - (rt.totalMemory() - rt.freeMemory()));
        long availableMemory = ((finalM / 1024) / 1024);
        System.out.println(availableMemory);

        try {
            for (int i = 0; i < availableMemory * 900; i++) {
                membership = new HashSet<String>();
                for (int j = 0; j <= 10; j++) {
                    membership.add("group" + j);
                }
                this.lugCacheStore.put("user" + i, membership);
            }
        } catch (Throwable t) {
        }
        long end = System.currentTimeMillis() - start;
		System.out.println("Adding took: " + end + "ms");

        start = System.currentTimeMillis();
        for (int i = 0; i < availableMemory * 900; i++) {
            membership = new HashSet<String>();
            for (int j = 0; j <= 10; j++) {
                membership.add("group" + j);
            }
            assertEquals(membership, this.lugCacheStore.get("user" + i, HashSet.class));
        }
        end = System.currentTimeMillis() - start;
        System.out.println("Getting took: " + end + "ms");
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        this.lugCacheStore.clearCache();
    }

    /**
     * Test method for
     * {@link com.google.enterprise.connector.sharepoint.ldap.LdapUserGroupsCache#put(java.lang.Object, java.lang.Object)}
     * .
     */
    @Test
    public final void testPutAngGet() {
        Set<String> membership;
        for (int i = 1; i <= 10; i++) {
            membership = new HashSet<String>();
            for (int j = 0; j <= 10; j++) {
                membership.add("group" + j);
            }
            this.lugCacheStore.put("user" + i, membership);
        }
        for (int i = 1; i <= 10; i++) {
            membership = new HashSet<String>();
            for (int j = 0; j <= 10; j++) {
                membership.add("group" + j);
            }
            assertEquals(membership, this.lugCacheStore.get("user" + i, HashSet.class));
        }
        this.lugCacheStore.put(TestConfiguration.google, new HashSet<String>().add((String) TestConfiguration.google));
        assertEquals(new Boolean(true), this.lugCacheStore.get(TestConfiguration.google, HashSet.class));
        this.lugCacheStore.clearCache();
        // overwrite
        this.lugCacheStore.put(TestConfiguration.ldapuser1, new HashSet<String>().add((String) TestConfiguration.google));
        this.lugCacheStore.put(TestConfiguration.ldapuser1, new HashSet<String>().add((String) TestConfiguration.google));
        assertEquals(1, this.lugCacheStore.getSize());

    }

    /**
     * Test method for
     * {@link com.google.enterprise.connector.sharepoint.ldap.LdapUserGroupsCache#contains(java.lang.Object)}
     * .
     */
    @Test
    public final void testContains() {
        Set<String> membership = null;
        for (int i = 1; i <= 10; i++) {
            membership = new HashSet<String>();
            for (int j = 1; j <= 10; j++) {
                membership.add(TestConfiguration.ldapgroup + j);
            }
            this.lugCacheStore.put(TestConfiguration.ldapuser + i, membership);
        }
        assertTrue(this.lugCacheStore.contains(TestConfiguration.ldapuser1));
        assertFalse(this.lugCacheStore.contains(TestConfiguration.ldapuser));
    }

    /**
     * Test method for
     * {@link com.google.enterprise.connector.sharepoint.ldap.LdapUserGroupsCache#clearCache()}
     * .
     */
    @Test
    public final void testClearCache() {
        Set<String> membership = null;
        for (int i = 1; i <= 10; i++) {
            membership = new HashSet<String>();
            for (int j = 1; j <= 10; j++) {
                membership.add("group" + j);
            }
            this.lugCacheStore.put("user" + i, membership);
        }
        assertEquals(membership.size(), this.lugCacheStore.getSize());
        this.lugCacheStore.clearCache();
        assertEquals(0, this.lugCacheStore.getSize());
    }

	@Test
	public void expire() throws InterruptedException {
		Set<String> membership = null;
		for (int i = 1; i <= 10; i++) {
			membership = new HashSet<String>();
			for (int j = 1; j <= 10; j++) {
				membership.add("group" + j);
			}
			this.lugCacheStore.put("user" + i, membership);
		}
		assertEquals(membership.size(), this.lugCacheStore.get("test1", HashSet.class));
		// wait until this object is expired
		Thread.sleep(this.lugCacheStore.getExpire() * 1010);
		assertNull(this.lugCacheStore.get("test1", HashSet.class));
	}
}
