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

/**
 * A java bean to store the results of UserDataStore table.
 * <p>
 * This class does not have a one to one mapping from the fields used in the
 * database table. Rather, [userId]userName and [groupId]groupName are used as
 * concatenated strings for UserId and GroupId values.
 *
 * @author nitendra_thakur
 */
public class UserGroupMembership implements Comparable<UserGroupMembership> {
  private int userId;
  private String userName;
  private int groupId;
  private String groupName;
  private String namespace;

  UserGroupMembership() {

  }

  public UserGroupMembership(int userId, String userName, int groupId,
      String groupName, String namespace) {
    this.userName = userName;
    this.userId = userId;
    this.groupId = groupId;
    this.groupName = groupName;
    this.namespace = namespace;
  }

  @Override
  public boolean equals(Object obj) {
    UserGroupMembership inMembership = null;
    if (obj instanceof UserGroupMembership) {
      inMembership = (UserGroupMembership) obj;

      boolean status = false;

      if (userId == inMembership.userId && groupId == inMembership.groupId) {
        status = true;
      } else {
        return status;
      }

      if (null == userName) {
        if (null != inMembership.userName) {
          status = false;
        }
      } else if (!userName.equals(inMembership.userName)) {
        status = false;
      }

      if (!status) {
        return status;
      }

      if (null == groupName) {
        if (null != inMembership.groupName) {
          status = false;
        }
      } else if (!groupName.equals(inMembership.groupName)) {
        status = false;
      }

      if (!status) {
        return status;
      }

      if (null == namespace) {
        if (null != inMembership.namespace) {
          status = false;
        }
      } else if (!namespace.equals(inMembership.namespace)) {
        status = false;
      }

      return status;
    }
    return false;
  }

  public int hashCode() {
    int len = (null == namespace) ? 0 : namespace.hashCode();
    return (11 * ((userId * 3) + (groupId * 7)) + len);
  }

  public int getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
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

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public void setGroupId(int groupId) {
    this.groupId = groupId;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public String toString() {
    return "userId [ " + getUserId() + " ], userName [ " + getUserName()
        + " ], groupId [ " + getGroupId() + " ], groupName [" + getGroupName()
        + " ], namespace [ " + getNamespace() + " ] ";
  }

  public int compareTo(UserGroupMembership o) {
    if (getUserId() != o.getUserId()) {
      return (getUserId() > o.getUserId()) ? 1 : -1;
    } else if (getGroupId() != o.getGroupId()) {
      return (getGroupId() > o.getGroupId()) ? 1 : -1;
    } else {
      int len1 = (null != getNamespace()) ? 0 : namespace.hashCode();
      int len2 = (null != o.getNamespace()) ? 0 : namespace.hashCode();
      if (len1 != len2) {
        return (len1 > len2) ? 1 : -1;
      } else {
        return 0;
      }
    }
  }
}
