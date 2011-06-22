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

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Store names of columns of User Group Membership table. Used to create
 * {@link UserGroupMembership} objects from {@link ResultSet}
 *
 * @author nitendra_thakur
 */
public final class UserGroupMembershipRowMapper implements
        ParameterizedRowMapper<UserGroupMembership> {
    private String userID;
    private String userName;
    private String groupID;
    private String groupName;
    private String namespace;

    public UserGroupMembership mapRow(ResultSet result, int rowNum)
            throws SQLException {
        return new UserGroupMembership(result.getInt(userID),
                result.getString(userName), result.getInt(groupID),
                result.getString(groupName), result.getString(namespace));
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
