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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides a simple cache implementation to store objects with
 * default and custom expire time along with initial custom capacity.
 *
 * @author nageswara_sura
 */
public class UserGroupsCache<K, V> implements IUserGroupsCache<K, V> {

  private static final Logger LOGGER = Logger.getLogger(UserGroupsCache.class.getName());
  // To store LDAP user and its groups (direct , parent)
  private final Map<K, V> cacheStore;

  // To store cache key and it's expire time.
  private final Map<K, Long> expire;

  // Time limit in seconds to maintain entries in cache before the cache is
  // cleared.
  private final long refreshInterval;

  // Used to constructs an LinkedHashMap instance.
  private static final float hashTableLoadFactor = 0.75f;

  // Used to construct an LinkedHashMap instance.
  private long cacheSize;

  /**
   * Constructs the cache with a default refresh interval time for the directory
   * service user group memberships for 2 hours with an initial capacity of
   * 1000+ (depends on the load factor).
   */
  public UserGroupsCache() {
    this(7200, 1000);
  }

  /**
   * Constructs the cache with a supplied refresh interval time for the LDAP/AD
   * groups to which the user belongs to for 2 hours with initial capacity of
   * 10000+ (depends on the load factor)
   *
   * @param refreshInterval to maintain entries in cache time in seconds
   */
  public UserGroupsCache(final long refreshInterval, final int cacheSize) {
    LOGGER.log(Level.CONFIG, "Creating LDAP user groups cache store with refresh interval [ "
        + refreshInterval + " ] and with capacity [ " + cacheSize + " ]");
    if (refreshInterval < 7200) {
      this.refreshInterval = 7200;
      LOGGER.info("Configured refresh interval for user groups cache with "
          + this.refreshInterval + " seconds");
    } else {
      this.refreshInterval = refreshInterval;
    }
    this.cacheSize = cacheSize;
    int hashTableCapacity = (int) Math.ceil(this.cacheSize
        / hashTableLoadFactor) + 1;
    this.cacheStore = Collections.synchronizedMap(new LinkedHashMap<K, V>(
        hashTableCapacity, hashTableLoadFactor, true) {
      private static final long serialVersionUID = 1;

      protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        if (size() > UserGroupsCache.this.cacheSize) {
          LOGGER.info("Removing the cached entry for the search user ["
              + eldest.getKey()
              + "] from the user groups cache since the cache is full.");
          UserGroupsCache.this.expire.remove(eldest.getKey());
          return true;
        } else {
          return false;
        }

      }
    });

    this.expire = Collections.synchronizedMap(new HashMap<K, Long>());

    // Creates a thread pool that can be scheduled to run removeExpiry()
    // command with a fixed set of threads. If any
    // thread terminates due to a failure during execution prior to
    // shutdown, a new one will take its place if needed to execute
    // subsequent tasks.

    Executors.newScheduledThreadPool(20).scheduleWithFixedDelay(this.removeExpired(), this.refreshInterval / 2, this.refreshInterval, TimeUnit.SECONDS);
  }

  /**
   * This Runnable removes expired objects from cache store.
   */
  private final Runnable removeExpired() {
    return new Runnable() {
      public void run() {
        for (final K name : expire.keySet()) {
          synchronized (expire) {
            if (System.currentTimeMillis() > expire.get(name)) {
              removeExpiredObjectFromCache(name);
              LOGGER.log(Level.CONFIG, "Invalidating cache entry for the search user [ "
                  + name + " ] after " + refreshInterval + " seconds. ");
            }
          }
        }
      }
    };
  }

  /**
   * Removes a specific object from the cache.
   *
   * @param name the name of the object
   */
  private void removeExpiredObjectFromCache(final K name) {
    cacheStore.remove(name);
    expire.remove(name);
  }

  /**
   * Returns the default expiration time for the objects in the cache.
   *
   * @return default expiration time in seconds
   */
  public long getExpire() {
    return this.refreshInterval;
  }

  /**
   * Put an object into the cache.
   *
   * @param name the object will be referenced with this name in the cache
   * @param obj the object
   */
  public void put(K key, V obj) {
    this.put(key, obj, this.refreshInterval);
  }

  /**
   * Put an object into the cache with a custom expiration date.
   *
   * @param name the object will be referenced with this name in the cache
   * @param obj the object
   * @param expireTime custom expiration time in seconds
   */
  private void put(K key, V obj, final long expireTime) {
    try {
      this.cacheStore.put(key, obj);
      long currentTime = System.currentTimeMillis() + expireTime * 1000;
      this.expire.put(key, currentTime);
      LOGGER.log(Level.INFO, "Updated cache for the search user [" + key
          + "] with expiry time in seconds [" + currentTime
          + "] and now the cache size is : " + this.getSize());
    } catch (Throwable t) {
      LOGGER.log(Level.WARNING, "Exception is thrown while updating cache for the key : "
          + key, t);
    }
  }

  /**
   * Returns an object from the cache if its expireTime is not null and less
   * than current system time and if it's greater will make a call to a thread
   * to remove its entry from cache store.
   *
   * @param name the name of the object you'd like to get
   * @param type the type of the object you'd like to get
   * @return the object for the given name and type
   */
  public V get(K key) {
    final Long expireTime = this.expire.get(key);
    if (expireTime == null)
      return null;
    if (System.currentTimeMillis() > expireTime) {
      LOGGER.log(Level.CONFIG, "Removing cache entry for the user [ " + key
          + " ] since the key expired in cache");
      this.removeExpiredObjectFromCache(key);
      return null;
    }
    return this.cacheStore.get(key);
  }

  @SuppressWarnings("unchecked")
  public <R extends K> R get(K key, final Class<R> type) {
    return (R) this.get(key);
  }

  /*
   * To clear cache store force fully.
   */
  public void clearCache() {
    this.cacheStore.clear();

  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.google.enterprise.connector.sharepoint.ldap.ILdapUserGroupCache#getSize
   * ()
   */
  public int getSize() {
    return this.cacheStore.size();
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.google.enterprise.connector.sharepoint.ldap.ILdapUserGroupCache#contains
   * (java.lang.Object)
   */
  public boolean contains(K key) {
    return (null != get(key));
  }
}
