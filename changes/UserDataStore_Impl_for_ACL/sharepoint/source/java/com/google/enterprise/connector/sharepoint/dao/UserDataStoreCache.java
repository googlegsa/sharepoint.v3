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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

/**
 * A cache to hold {@link UserGroupMembership}
 *
 * @author nitendra_thakur
 * @param <T>
 */
public class UserDataStoreCache<T extends UserGroupMembership> {
    private final Logger LOGGER = Logger.getLogger(UserDataStoreCache.class.getName());
    int capacity = 100;
    Set<T> cachedMemberships;

    UserDataStoreCache(int capacity) {
        this.capacity = capacity;
        cachedMemberships = new HashSet<T>(capacity);
        LOGGER.log(Level.INFO, "User Data Store Cache size set to " + capacity);
    }

    /**
     * Add a set of memberships into the cache. If the cache is full or
     *
     * @param memberships
     * @return
     * @throws SharepointException
     */
    public boolean addMemberships(Collection<? extends T> memberships)
            throws SharepointException {
        if (cachedMemberships.size() >= capacity
                || memberships.size() > capacity) {
            LOGGER.log(Level.WARNING, "UserDataStoreCache is full ");
            return false;
        }
        return cachedMemberships.addAll(memberships);
    }

    public boolean contains(T t) {
        return cachedMemberships.contains(t);
    }

    public void removeUserMembershipsFromNamespace(Collection<Integer> userIds,
            String namespace) {
        Iterator<T> itr = cachedMemberships.iterator();
        while (itr.hasNext()) {
            T membership = itr.next();
            if (null == membership) {
                continue;
            }
            String nmspc = membership.getNameSpace();
            int userId = membership.getUserId();
            if (userIds.contains(userId) && namespace.equals(nmspc)) {
                itr.remove();
            }
        }
    }

    public void removeGroupMembershipsFromNamespace(
            Collection<Integer> groupIds,
            String namespace) {
        Iterator<T> itr = cachedMemberships.iterator();
        while (itr.hasNext()) {
            T membership = itr.next();
            if (null == membership) {
                continue;
            }
            String nmspc = membership.getNameSpace();
            int groupId = membership.getGroupId();
            if (groupIds.contains(groupId) && namespace.equals(nmspc)) {
                itr.remove();
            }
        }
    }

    public void removeAllMembershipsFromNamespace(Collection<String> namespaces) {
        Iterator<T> itr = cachedMemberships.iterator();
        while(itr.hasNext()) {
            UserGroupMembership membership = itr.next();
            if(null == membership) {
                continue;
            }
            String namespace = membership.getNameSpace();
            if (namespaces.contains(namespace)) {
                itr.remove();
            }
        }
    }
}
