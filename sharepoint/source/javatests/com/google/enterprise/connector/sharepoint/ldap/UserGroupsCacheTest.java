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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test class for {@link UserGroupsCache}
 *
 * @author nageswara_sura
 */
public class UserGroupsCacheTest {
  private UserGroupsCache<Object, ConcurrentHashMap<String, Set<String>>> lugCacheStore;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    this.lugCacheStore = new UserGroupsCache<Object, ConcurrentHashMap<String, Set<String>>>(
        TestConfiguration.refreshInterval, getAvailableMemory() * 500);
  }

  @Test
  public void perf() throws InterruptedException {
    long start = System.currentTimeMillis();
    ConcurrentHashMap<String, Set<String>> members = null;
    Set<String> membership;
    // int availableMemory = getAvailableMemory();
    try {
      for (int i = 1; i <= 500; i++) {
        members = new ConcurrentHashMap<String, Set<String>>();
        membership = new HashSet<String>();
        for (int j = 0; j <= 2; j++) {
          membership.add("group" + j);
        }
        members.put("type1", membership);
        for (int j = 0; j <= 2; j++) {
          membership.add("group" + j);
        }
        members.put("type2", membership);
        this.lugCacheStore.put("searchuser" + i, members);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
    long end = System.currentTimeMillis() - start;
    System.out.println("Adding took: " + end + "ms");

    start = System.currentTimeMillis();
    for (int i = 1; i <= 500; i++) {
      assertEquals(new Boolean(true), this.lugCacheStore.contains("searchuser"
          + i));
    }

    end = System.currentTimeMillis() - start;
    System.out.println("Getting took: " + end + "ms");
  }

  private int getAvailableMemory() {
    Runtime rt = Runtime.getRuntime();
    // test for free amount of memory * 500 number of entries.
    // it assumes that for a given 1 MB HEAP memory, the cache can group up
    // to 500 memberships.

    long finalM = (rt.maxMemory() - (rt.totalMemory() - rt.freeMemory()));
    int availableMemory = (int) ((finalM / 1024) / 1024);
    System.out.println(availableMemory);
    return availableMemory;
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
   * {@link com.google.enterprise.connector.sharepoint.ldap.UserGroupsCache#put(java.lang.Object, java.lang.Object)}
   * .
   */
  @Test
  public final void testPutAngGet() {
    ConcurrentHashMap<String, Set<String>> members = null;
    Set<String> membership;
    for (int i = 1; i <= 10; i++) {
      members = new ConcurrentHashMap<String, Set<String>>();
      membership = new HashSet<String>();
      for (int j = 0; j <= 2; j++) {
        membership.add("group" + j);
      }
      members.put("type1", membership);
      for (int j = 0; j <= 2; j++) {
        membership.add("group" + j);
      }
      members.put("type2", membership);
      this.lugCacheStore.put("searchuser" + i, members);
    }
    assertTrue(this.lugCacheStore.contains("searchuser1"));
    assertNull(this.lugCacheStore.get(TestConfiguration.ldapuser6, ConcurrentHashMap.class));
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.sharepoint.ldap.UserGroupsCache#contains(java.lang.Object)}
   * .
   */
  @Test
  public final void testContains() {
    ConcurrentHashMap<String, Set<String>> members = null;
    Set<String> membership;
    for (int i = 1; i <= 10; i++) {
      members = new ConcurrentHashMap<String, Set<String>>();
      membership = new HashSet<String>();
      for (int j = 0; j <= 2; j++) {
        membership.add("group" + j);
      }
      members.put("type1", membership);
      for (int j = 0; j <= 2; j++) {
        membership.add("group" + j);
      }
      members.put("type2", membership);
      this.lugCacheStore.put("searchuser" + i, members);
    }
    assertTrue(this.lugCacheStore.contains("searchuser1"));
    assertFalse(this.lugCacheStore.contains(TestConfiguration.ldapuser));
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.sharepoint.ldap.UserGroupsCache#clearCache()}
   * .
   */
  @Test
  public final void testClearCache() {
    this.lugCacheStore.clearCache();
    ConcurrentHashMap<String, Set<String>> members = null;
    Set<String> membership;
    for (int i = 1; i <= 10; i++) {
      members = new ConcurrentHashMap<String, Set<String>>();
      membership = new HashSet<String>();
      for (int j = 0; j <= 2; j++) {
        membership.add("group" + j);
      }
      members.put("type1", membership);
      for (int j = 0; j <= 2; j++) {
        membership.add("group" + j);
      }
      members.put("type2", membership);
      this.lugCacheStore.put("searchuser" + i, members);
    }
    assertEquals(10, this.lugCacheStore.getSize());
    this.lugCacheStore.clearCache();
    assertEquals(0, this.lugCacheStore.getSize());
  }

  @Test
  public void expire() throws InterruptedException {
    ConcurrentHashMap<String, Set<String>> members = null;
    Set<String> membership;
    for (int i = 1; i <= 10; i++) {
      members = new ConcurrentHashMap<String, Set<String>>();
      membership = new HashSet<String>();
      for (int j = 0; j <= 2; j++) {
        membership.add("group" + j);
      }
      members.put("type1", membership);
      for (int j = 0; j <= 2; j++) {
        membership.add("group" + j);
      }
      members.put("type2", membership);
      this.lugCacheStore.put("searchuser" + i, members);
    }
    assertEquals(new Boolean(true), this.lugCacheStore.contains("searchuser1"));
    // wait until this object is expired. Commented the below line not wait
    // so long time to get Cobertura reports.
    // Thread.sleep(this.lugCacheStore.getExpire() * 1010);
    this.lugCacheStore.clearCache();
    assertNull(this.lugCacheStore.get("searchuser1", ConcurrentHashMap.class));
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.sharepoint.ldap.UserGroupsCache#put(java.lang.Object, java.lang.Object)}
   * .
   *
   * @throws InterruptedException
   */
  @Test
  public final void testPutAndGetConcurrentHashMap()
      throws InterruptedException {
    ConcurrentHashMap<String, Set<String>> members;
    Set<String> membership;
    for (int i = 1; i <= 10; i++) {
      members = new ConcurrentHashMap<String, Set<String>>();
      membership = new HashSet<String>();
      for (int j = 0; j <= 2; j++) {
        membership.add("group" + j);
      }
      members.put("type1", membership);
      for (int j = 0; j <= 2; j++) {
        membership.add("group" + j);
      }
      members.put("type2", membership);
      this.lugCacheStore.put("searchuser" + i, members);
    }
    // wait until this object is expired. Commented the below line not wait
    // so long time to get Cobertura reports.
    // Thread.sleep(this.lugCacheStore.getExpire() * 1010);
    this.lugCacheStore.clearCache();
    assertNull(this.lugCacheStore.get("searchuser1", ConcurrentHashMap.class));
  }
}
