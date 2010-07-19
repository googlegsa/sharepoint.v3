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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

/**
 * A java bean to store the results of UserDataStore table.
 * <p>
 * This class does not have a one to one mapping from the fields used in the
 * database table. Rather, [userId]userName and [groupId]groupName are used as
 * concatenated strings for UserId and GroupId values.
 *
 * @author nitendra_thakur
 */
public class UserGroupMembership {
    private int userId;
    private String userName;
    private int groupId;
    private String groupName;
    private String nameSpace;

    /**
     * @param user if not null, must be in a format [int]string
     * @param group
     * @param nameSpace
     */
    protected UserGroupMembership(String user, String group, String nameSpace)
            throws SharepointException {
        final Pattern pattern = Pattern.compile("^\\[\\-{0,1}\\d+\\]");
        Matcher matcher = null;
        try {
            if (null != user) {
                matcher = pattern.matcher(user);
                if (matcher.find()) {
                    String strId = matcher.group();
                    this.userId = Integer.parseInt(strId.substring(matcher.start() + 1, matcher.end() - 1));
                    this.userName = user.substring(matcher.end());
                } else {
                    throw new SharepointException("Wrongly formatted value [ "
                            + user + " ] for user. Expected format is [ID]Name");
                }
            }
            if (null != group) {
                matcher = pattern.matcher(group);
                if (matcher.find()) {
                    String strId = matcher.group();
                    this.groupId = Integer.parseInt(strId.substring(matcher.start() + 1, matcher.end() - 1));
                    this.groupName = group.substring(matcher.end());
                } else {
                    throw new SharepointException("Wrongly formatted value [ "
                            + user + " ] for user. Expected format is [ID]Name");
                }
            }
        } catch (Exception e) {
            throw new SharepointException(
                    "Unable to parse the paased-in user/group value");
        }
        this.nameSpace = nameSpace;
    }

    public UserGroupMembership(String userName, int userId, String groupName,
            int groupId, String nameSpace) {
        this.userName = userName;
        this.userId = userId;
        this.groupId = groupId;
        this.groupName = groupName;
        this.nameSpace = nameSpace;
    }

    /**
     * Checks if this object is refering to a blank row
     *
     * @return
     */
    public boolean isEmpty() {
        if ((null == userName || userName.trim().length() == 0)
                && (null == groupName || groupName.trim().length() == 0)
                && (null == nameSpace || nameSpace.trim().length() == 0)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        UserGroupMembership inMembership = null;
        if(obj instanceof UserGroupMembership) {
            inMembership = (UserGroupMembership) obj;
            if ((null != userName && userName.equals(inMembership.userName) && userId == inMembership.userId)
                    && (null != groupName
                            && groupName.equals(inMembership.groupName) && groupId == inMembership.groupId)
                    && (null != nameSpace && nameSpace.equals(inMembership.nameSpace))) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        int len = (null != nameSpace) ? 0 : nameSpace.length();
        return 13 * ((userId * groupId) / len);
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

    public String getNameSpace() {
        return nameSpace;
    }

    /**
     * Returns the complex userId which is a concatenation of actual UserId and
     * UserName. This format is used to store the information into the user data
     * store
     *
     * @return
     */
    public String getComplexUserId() {
        return "[" + userId + "]" + userName;
    }

    /**
     * Returns the complex GroupId which is a concatenation of actual GroupId
     * and GroupName. This format is used to store the information into the user
     * data store
     *
     * @return
     */
    public String getComplexGroupId() {
        return "[" + groupId + "]" + groupName;
    }

    /**
     * returns the id pattern to be used for selecting the specific user/group
     * records from the user data store
     *
     * @return
     */
    public static String getIdPattern(int id) {
        return "[" + id + "]%";
    }

    /**
     * returns the name pattern to be used for selecting the specific user/group
     * records from the user data store
     *
     * @return
     */
    public static String getNamePattern(String name) {
        return "%" + name;
    }

    public String toString() {
        return "UserId [ " + getComplexUserId() + " ], GroupId [ "
                + getComplexGroupId() + " ], Namespace [ " + getNameSpace()
                + " ] ";
    }
}
