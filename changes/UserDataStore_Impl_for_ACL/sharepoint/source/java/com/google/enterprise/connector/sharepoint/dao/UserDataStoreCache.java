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

import com.google.enterprise.connector.sharepoint.client.CacheProvider;

import java.util.HashSet;
import java.util.Set;

/**
 * A cache to hold {@link UserGroupMembership}
 *
 * @author nitendra_thakur
 */
public class UserDataStoreCache<T extends UserGroupMembership> extends
        CacheProvider<T> {

    /**
     * Provides the user-namespace view of the {@link UserGroupMembership}
     *
     * @author nitendra_thakur
     */
    class UserNamespaceView implements Comparable<UserNamespaceView>, View {
        int userId;
        String userName;
        String namespace;

        private UserNamespaceView(T membership) {
            userId = membership.getUserId();
            userName = membership.getUserName();
            namespace = membership.getNamespace();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof UserDataStoreCache.UserNamespaceView) {
                UserNamespaceView view = (UserNamespaceView) obj;
                if (getUserId() == view.getUserId()) {
                    if (null == getNamespace()) {
                        if (null == view.getNamespace()) {
                            return true;
                        }
                    } else if (getNamespace().equals(view.getNamespace())) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            int len = (null == getNamespace()) ? 0 : getNamespace().hashCode();
            return 11 * ((getUserId() * 3) + len);
        }

        public int compareTo(UserNamespaceView o) {
            if (getUserId() != o.getUserId()) {
                return (getUserId() > o.getUserId()) ? 1 : -1;
            } else {
                int len1 = (null != getNamespace()) ? 0
                        : getNamespace().hashCode();
                int len2 = (null != o.getNamespace()) ? 0
                        : o.getNamespace().hashCode();
                if (len1 != len2) {
                    return (len1 > len2) ? 1 : -1;
                } else {
                    return 0;
                }
            }
        }

        public int getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }

        public String getNamespace() {
            return namespace;
        }
    }

    /**
     * Provides the group-namespace view of the {@link UserGroupMembership}
     *
     * @author nitendra_thakur
     */
    class GroupNamespaceView implements Comparable<GroupNamespaceView>, View {
        int groupId;
        String groupName;
        String namespace;

        private GroupNamespaceView(UserGroupMembership membership) {
            groupId = membership.getGroupId();
            groupName = membership.getGroupName();
            namespace = membership.getNamespace();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof UserDataStoreCache.GroupNamespaceView) {
                GroupNamespaceView view = (GroupNamespaceView) obj;
                if (getGroupId() == view.getGroupId()) {
                    if (null == getNamespace()) {
                        if (null == view.getNamespace()) {
                            return true;
                        }
                    } else if (getNamespace().equals(view.getNamespace())) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            int len = (null == getNamespace()) ? 0 : getNamespace().hashCode();
            return 17 * ((getGroupId() * 3) + len);
        }

        public int compareTo(GroupNamespaceView o) {
            if (getGroupId() != o.getGroupId()) {
                return (getGroupId() > o.getGroupId()) ? 1 : -1;
            } else {
                int len1 = (null != getNamespace()) ? 0 : getNamespace().hashCode();
                int len2 = (null != o.getNamespace()) ? 0
                        : o.getNamespace().hashCode();
                if (len1 != len2) {
                    return (len1 > len2) ? 1 : -1;
                } else {
                    return 0;
                }
            }
        }

        public int getGroupId() {
            return groupId;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getNamespace() {
            return namespace;
        }
    }

    /**
     * Provides the namespace view of the {@link UserGroupMembership}
     *
     * @author nitendra_thakur
     */
    class NamespaceView implements Comparable<NamespaceView>, View {
        UserGroupMembership _membership;

        private NamespaceView(UserGroupMembership membership) {
            _membership = membership;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof UserDataStoreCache.NamespaceView) {
                NamespaceView view = (NamespaceView) obj;
                if (null == getNamespace()) {
                    if (null == view.getNamespace()) {
                        return true;
                    }
                } else if (getNamespace().equals(view.getNamespace())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            if (null == _membership) {
                return super.hashCode();
            }
            int len = (null == getNamespace()) ? 0 : getNamespace().hashCode();
            return 19 * len;
        }

        public int compareTo(NamespaceView o) {
            int len1 = (null != getNamespace()) ? 0 : getNamespace().hashCode();
            int len2 = (null != o.getNamespace()) ? 0
                    : o.getNamespace().hashCode();
            if (len1 != len2) {
                return (len1 > len2) ? 1 : -1;
            } else {
                return 0;
            }
        }

        public String getNamespace() {
            return _membership.getNamespace();
        }
    }

    protected Set<View> getViews(T t) {
        Set<View> supportedViews = new HashSet<View>();
        supportedViews.add(new UserNamespaceView(t));
        supportedViews.add(new GroupNamespaceView(t));
        supportedViews.add(new NamespaceView(t));
        return supportedViews;
    }

    /**
     * removal of memberships from cache based on user-namespace view
     *
     * @param t
     */
    public void removeUsingUserNamespaceView(T t) {
        UserNamespaceView view = new UserNamespaceView(t);
        removeUsingView(view);
    }

    /**
     * removal of memberships from cache based on user-namespace view
     *
     * @param t
     */
    public void removeUsingGroupNamespaceView(T t) {
        GroupNamespaceView view = new GroupNamespaceView(t);
        removeUsingView(view);
    }

    /**
     * removal of memberships from cache based on user-namespace view
     *
     * @param t
     */
    public void removeUsingNamespaceView(T t) {
        NamespaceView view = new NamespaceView(t);
        removeUsingView(view);
    }
}
