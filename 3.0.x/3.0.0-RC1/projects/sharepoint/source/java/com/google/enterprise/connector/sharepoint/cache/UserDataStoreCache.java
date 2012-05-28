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

package com.google.enterprise.connector.sharepoint.cache;

import com.google.enterprise.connector.sharepoint.dao.UserGroupMembership;

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
  private static class UserNamespaceView implements
      Comparable<UserNamespaceView>, View {
    int userId;
    String namespace;

    private UserNamespaceView(UserGroupMembership membership) {
      userId = membership.getUserId();
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
        int len1 = (null != getNamespace()) ? 0 : getNamespace().hashCode();
        int len2 = (null != o.getNamespace()) ? 0 : o.getNamespace().hashCode();
        if (len1 != len2) {
          return (len1 > len2) ? 1 : -1;
        } else {
          return 0;
        }
      }
    }

    private int getUserId() {
      return userId;
    }

    private String getNamespace() {
      return namespace;
    }
  }

  /**
   * Provides the group-namespace view of the {@link UserGroupMembership}
   *
   * @author nitendra_thakur
   */
  private static class GroupNamespaceView implements
      Comparable<GroupNamespaceView>, View {
    int groupId;
    String namespace;

    private GroupNamespaceView(UserGroupMembership membership) {
      groupId = membership.getGroupId();
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
        int len2 = (null != o.getNamespace()) ? 0 : o.getNamespace().hashCode();
        if (len1 != len2) {
          return (len1 > len2) ? 1 : -1;
        } else {
          return 0;
        }
      }
    }

    private int getGroupId() {
      return groupId;
    }

    private String getNamespace() {
      return namespace;
    }
  }

  /**
   * Provides the namespace view of the {@link UserGroupMembership}
   *
   * @author nitendra_thakur
   */
  private static class NamespaceView implements Comparable<NamespaceView>, View {
    String namespace;

    private NamespaceView(UserGroupMembership membership) {
      this.namespace = membership.getNamespace();
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
      if (null == namespace) {
        return super.hashCode();
      }
      int len = (null == getNamespace()) ? 0 : getNamespace().hashCode();
      return 19 * len;
    }

    public int compareTo(NamespaceView o) {
      int len1 = (null != getNamespace()) ? 0 : getNamespace().hashCode();
      int len2 = (null != o.getNamespace()) ? 0 : o.getNamespace().hashCode();
      if (len1 != len2) {
        return (len1 > len2) ? 1 : -1;
      } else {
        return 0;
      }
    }

    public String getNamespace() {
      return namespace;
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
