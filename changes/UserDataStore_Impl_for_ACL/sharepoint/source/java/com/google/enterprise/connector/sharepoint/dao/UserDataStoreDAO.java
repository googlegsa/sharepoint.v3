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

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

public class UserDataStoreDAO extends SharePointDAO {
    private final Logger LOGGER = Logger.getLogger(UserDataStoreDAO.class.getName());
    private static UserDataStoreDAO instance;
    private final String TABLENAME = "UserGroupMemberships";
    private final String CREATETABLEQUERY = "CREATE TABLE "
            + TABLENAME
            + "( UserId varchar(50), GroupId varchar(40), Namespace varchar(200), PRIMARY KEY(UserId,GroupId,Namespace) )";

    private final String INSERTQUERY = "INSERT INTO " + TABLENAME
            + " VALUES(?,?,?)";

    // When the deletion is based on UserId and Namespace
    private final String DELETEQUERY1 = "DELETE FROM " + TABLENAME
            + " WHERE UserId=? AND Namespace=?";

    // When the deletion is based on GroupId and Namespace
    private final String DELETEQUERY2 = "DELETE FROM " + TABLENAME
            + " WHERE GroupId=? AND Namespace=?";

    // When the deletion is based on Namespace only
    private final String DELETEQUERY3 = "DELETE FROM " + TABLENAME
            + " WHERE Namespace=?";

    // When the selection is based on UserId only
    private final String SELECTQUERY1 = "SELECT GroupId FROM " + TABLENAME
            + " WHERE UserId=?";

    // When the selection is based on all the keys
    private final String SELECTQUERY2 = "SELECT * FROM " + TABLENAME
            + " WHERE UserId=? AND GroupId=? AND Namespace=?";

    private PreparedStatement createQuery;
    private PreparedStatement insertQuery;
    private PreparedStatement deleteQuery1;
    private PreparedStatement deleteQuery2;
    private PreparedStatement deleteQuery3;
    private PreparedStatement selectQuery1;
    private PreparedStatement selectQuery2;

    public static UserDataStoreDAO getInstance(DBConfig dbConfig)
            throws SharepointException {
        return (null == instance) ? new UserDataStoreDAO(dbConfig) : instance;
    }

    protected UserDataStoreDAO(DBConfig dbConfig) throws SharepointException {
        super(dbConfig);
        init();
    }

    /**
     * Initializes all the PreparedStatements. Checks whether all the required
     * tables exists or not. If not, creates them
     */
    private void init() throws SharepointException {
        DatabaseMetaData dbm = getDatabaseMetadata();
        ResultSet tables = null;
        boolean createNewMembershipTable = false;
        // TODO: Externalize table name
        try {
            tables = dbm.getTables(null, null, TABLENAME, null);
            if (null == tables || !tables.next()) {
                createQuery = con.prepareStatement(CREATETABLEQUERY);
                createNewMembershipTable = true;
            }
        } catch (Exception e) {
            throw new SharepointException(
                    "Exception occurred while getting the table information from the database metadata. ",
                    e);
        }

        if (createNewMembershipTable) {
            LOGGER.log(Level.INFO, "Creating new UserGroupMemberships table...");
            try {
                createQuery.execute();
            } catch (Throwable e) {
                throw new SharepointException(
                        "Exception occurred when trying to create UserGroupMemberships Table.. ",
                        e);
            }
        }

        try {
            insertQuery = con.prepareStatement(INSERTQUERY);
            deleteQuery1 = con.prepareStatement(DELETEQUERY1);
            deleteQuery2 = con.prepareStatement(DELETEQUERY2);
            deleteQuery3 = con.prepareStatement(DELETEQUERY3);
            selectQuery1 = con.prepareStatement(SELECTQUERY1);
            selectQuery2 = con.prepareStatement(SELECTQUERY2);
        } catch (Exception e) {
            throw new SharepointException(
                    "Exception occurred while initializing the prepared statements... ",
                    e);
        }
    }

    /**
     * Retrieves all the groups to which a user belongs
     *
     * @param userId
     * @return
     * @throws SharepointException
     */
    public List<String> getAllGroupsForUser(String userId)
            throws SharepointException {
        List<String> groupIds = new ArrayList<String>();
        ResultSet result = null;
        try {
            selectQuery1.setString(1, userId);
            result = selectQuery1.executeQuery();
        } catch (Exception e) {
            throw new SharepointException(
                    "Could not get the membership information due to following exception ",
                    e);
        }
        if(null == result) {
            return groupIds;
        }

        // TODO: Externalize the column name
        try {
            int columnIndex = result.findColumn("GroupId");
            for (int i = 0; result.next(); ++i) {
                String groupId = result.getString(columnIndex);
                groupIds.add(groupId);
            }
        } catch (Exception e) {
            throw new SharepointException(
                    "Failed to parse the result set to get GroupIds. ", e);
        }
        LOGGER.log(Level.CONFIG, "Groups identified for user [ " + userId
                + " ] are " + groupIds);
        return groupIds;
    }

    /**
     * Add a user group membership into the User Data Store
     *
     * @param membership
     * @return
     * @throws SharepointException
     */
    public boolean addMembership(UserGroupMembership membership)
            throws SharepointException {
        if (null == membership || !membership.isValid()) {
            return false;
        }
        try {
            // We must first check if the entry already exists. During the
            // connector's crawl, case of duplicate insertion is going to occur
            // very frequently
            selectQuery2.setString(1, membership.getUserId());
            selectQuery2.setString(2, membership.getGroupId());
            selectQuery2.setString(3, membership.getNameSpace());
            ResultSet result = selectQuery2.executeQuery();
            if (!result.next()) {
                insertQuery.setString(1, membership.getUserId());
                insertQuery.setString(2, membership.getGroupId());
                insertQuery.setString(3, membership.getNameSpace());
                insertQuery.execute();
                commit();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new SharepointException(
                    "Could not get the membership information due to following exception ",
                    e);
        }
    }

    /**
     * Remove all the memberships of a user
     *
     * @param userId
     * @param namespace
     * @return
     * @throws SharepointException
     */
    public int removeUserMemberships(String userId, String namespace)
            throws SharepointException {
        try {
            deleteQuery1.setString(1, userId);
            deleteQuery1.setString(2, namespace);
            int count = deleteQuery1.executeUpdate();
            commit();
            LOGGER.log(Level.INFO, "#" + count
                    + " rows deleted corresponding to UserId [ " + userId
                    + " ], Namespace [ " + namespace + " ] ");
            return count;
        } catch (Exception e) {
            throw new SharepointException(
                    "Could not get the membership information due to following exception ",
                    e);
        }
    }

    /**
     * Remove all the memberships of a group
     *
     * @param groupId
     * @param namespace
     * @return
     * @throws SharepointException
     */
    public int removeGroupMemberships(String groupId, String namespace)
            throws SharepointException {
        try {
            deleteQuery2.setString(1, groupId);
            deleteQuery2.setString(2, namespace);
            int count = deleteQuery2.executeUpdate();
            LOGGER.log(Level.INFO, "#" + count
                    + " rows deleted corresponding to GroupId [ " + groupId
                    + " ], Namespace [ " + namespace + " ] ");
            return count;
        } catch (Exception e) {
            throw new SharepointException(
                    "Could not get the membership information due to following exception ",
                    e);
        }
    }

    /**
     * Remove all the memberships that belongs to a namespace
     *
     * @param namespace
     * @return
     * @throws SharepointException
     */
    public int removeAllMembershipsFromNamespace(String namespace)
            throws SharepointException {
        try {
            deleteQuery3.setString(1, namespace);
            int count = deleteQuery3.executeUpdate();
            LOGGER.log(Level.INFO, "#" + count
                    + " rows deleted corresponding to Namespace [ " + namespace
                    + " ] ");
            return count;
        } catch (Exception e) {
            throw new SharepointException(
                    "Could not get the membership information due to following exception ",
                    e);
        }
    }
}
