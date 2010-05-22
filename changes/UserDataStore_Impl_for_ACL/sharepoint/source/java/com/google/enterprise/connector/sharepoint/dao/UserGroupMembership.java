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


/**
 * A java bean to store the results of UserDataStore table
 *
 * @author nitendra_thakur
 */
public class UserGroupMembership {
    private String userId;
    private String groupId;
    private String nameSpace;

    public UserGroupMembership(String userId, String groupId, String nameSpace) {
        this.userId = userId;
        this.groupId = groupId;
        this.nameSpace = nameSpace;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    /**
     * Checks if this object is refering to a blank row
     *
     * @return
     */
    public boolean isEmpty() {
        if ((null == userId || userId.trim().length() == 0)
                && (null == groupId || groupId.trim().length() == 0)
                && (null == nameSpace || nameSpace.trim().length() == 0)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if all the required values are present or not
     *
     * @return
     */
    public boolean isValid() {
        if (null == userId || userId.trim().length() == 0 || null == groupId
                || groupId.trim().length() == 0 || null == nameSpace
                || nameSpace.trim().length() == 0) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        UserGroupMembership inMembership = null;
        if(obj instanceof UserGroupMembership) {
            inMembership = (UserGroupMembership) obj;
            if ((null != userId && userId.equals(inMembership.getUserId()))
                    && (null != groupId && groupId.equals(inMembership.getGroupId()))
                    && (null != nameSpace && nameSpace.equals(inMembership.getNameSpace()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int part1 = (null != userId) ? 0 : userId.length();
        int part2 = (null != groupId) ? 0 : groupId.length();
        int part3 = (null != nameSpace) ? 0 : nameSpace.length();
        return 13 * ((part1 * part2) / part3);
    }
}
