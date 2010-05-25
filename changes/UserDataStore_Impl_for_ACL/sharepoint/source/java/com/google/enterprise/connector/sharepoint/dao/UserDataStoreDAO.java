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

import java.sql.CallableStatement;
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

    // TODO: Externalize table / column / function names
    private final String TABLENAME = "UserGroupMemberships";

    // For creating the user group membership relation
    private final String CREATETABLEQUERY = "CREATE TABLE "
            + TABLENAME
            + "( UserId varchar(50), GroupId varchar(40), Namespace varchar(200), PRIMARY KEY(UserId,GroupId,Namespace) )";

    // For deleting the user group membership relation
    private final String DROPTABLEQUERY = "DROP TABLE IF EXISTS " + TABLENAME;

    // For creating the function to check if a group exists in the membership
    // table
    private final String CREATEGROUPCHKFUNCQUERY = "CREATE FUNCTION DoesGroupExist(InGroupId varchar(40), InNamespace varchar(200)) RETURNS BOOLEAN "
            + " RETURN EXISTS(SELECT GroupId FROM usergroupmemberships WHERE GroupId LIKE InGroupId AND Namespace=InNamespace)";
    private final String DROPGROUPCHKFUNCQUERY = "DROP FUNCTION IF EXISTS DoesGroupExist";
    private final String GROUPCHKFUNCALL = "{? = call DoesGroupExist(?,?) }";
    private CallableStatement grpChkStmt;

    // For function to check if a user exists in the membership table
    private final String CREATEUSERCHKFUNCQUERY = "CREATE FUNCTION DoesUserExist(InUserId varchar(40), InNamespace varchar(200)) RETURNS BOOLEAN "
            + " RETURN EXISTS(SELECT UserId FROM usergroupmemberships WHERE UserId LIKE InUserId AND Namespace=InNamespace)";
    private final String DROPUSERCHKFUNCQUERY = "DROP FUNCTION IF EXISTS DoesUserExist";
    private final String USERCHKFUNCALL = "{? = call DoesUserExist(?,?) }";
    private CallableStatement userChkStmt;



    // For adding records into UserGroupMembership table
    private final String INSERTQUERY = "INSERT INTO " + TABLENAME
            + " VALUES(?,?,?)";
    private PreparedStatement insertQuery;

    // When the deletion is based on UserId and Namespace
    private final String DELETEQUERY1 = "DELETE FROM " + TABLENAME
            + " WHERE UserId LIKE ? AND Namespace=?";
    private PreparedStatement deleteQuery1;

    // When the deletion is based on GroupId and Namespace
    private final String DELETEQUERY2 = "DELETE FROM " + TABLENAME
            + " WHERE GroupId LIKE ? AND Namespace=?";
    private PreparedStatement deleteQuery2;

    // When the deletion is based on Namespace only
    private final String DELETEQUERY3 = "DELETE FROM " + TABLENAME
            + " WHERE Namespace=?";
    private PreparedStatement deleteQuery3;

    // When the selection is based on UserId only
    private final String SELECTQUERY1 = "SELECT * FROM " + TABLENAME
            + " WHERE UserId LIKE ?";
    private PreparedStatement selectQuery1;

    // When the selection is based on all the keys
    private final String SELECTQUERY2 = "SELECT * FROM " + TABLENAME
            + " WHERE UserId=? AND GroupId=? AND Namespace=?";
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
        PreparedStatement createQuery = null;
        try {
            tables = dbm.getTables(null, null, TABLENAME, null);
            if (null == tables || !tables.next()) {
                createNewMembershipTable = true;
            }
        } catch (Exception e) {
            throw new SharepointException(
                    "Exception occurred while getting the table information from the database metadata. ",
                    e);
        }

        try {
            if (createNewMembershipTable) {
                LOGGER.log(Level.INFO, "Creating new UserGroupMemberships table...");
                createQuery = con.prepareStatement(DROPTABLEQUERY);
                createQuery.execute();
                createQuery = con.prepareStatement(CREATETABLEQUERY);
                createQuery.execute();

                LOGGER.log(Level.INFO, "Creating UserGroupMemberships.DoesGroupExist() function...");
                createQuery = con.prepareStatement(DROPGROUPCHKFUNCQUERY);
                createQuery.execute();
                createQuery = con.prepareStatement(CREATEGROUPCHKFUNCQUERY);
                createQuery.execute();

                LOGGER.log(Level.INFO, "Creating UserGroupMemberships.DoesUserExist() function...");
                createQuery = con.prepareStatement(DROPUSERCHKFUNCQUERY);
                createQuery.execute();
                createQuery = con.prepareStatement(CREATEUSERCHKFUNCQUERY);
                createQuery.execute();
            }
        } catch (Exception e) {
            throw new SharepointException(
                    "Exception occurred while creating the UserGroupMemberships schema. ",
                    e);
        }

        try {
            insertQuery = con.prepareStatement(INSERTQUERY);
            deleteQuery1 = con.prepareStatement(DELETEQUERY1);
            deleteQuery2 = con.prepareStatement(DELETEQUERY2);
            deleteQuery3 = con.prepareStatement(DELETEQUERY3);
            selectQuery1 = con.prepareStatement(SELECTQUERY1);
            selectQuery2 = con.prepareStatement(SELECTQUERY2);
            grpChkStmt = con.prepareCall(GROUPCHKFUNCALL);
            grpChkStmt.registerOutParameter(1, java.sql.Types.BOOLEAN);
            userChkStmt = con.prepareCall(USERCHKFUNCALL);
            userChkStmt.registerOutParameter(1, java.sql.Types.BOOLEAN);
        } catch (Exception e) {
            throw new SharepointException(
                    "Exception occurred while initializing the prepared statements... ",
                    e);
        }
    }

    @Override
    public void closeConnection() {
        try {
            if (null != insertQuery)
                insertQuery.close();
            if (null != deleteQuery1)
                deleteQuery1.close();
            if (null != deleteQuery2)
                deleteQuery2.close();
            if (null != deleteQuery3)
                deleteQuery3.close();
            if (null != selectQuery1)
                selectQuery1.close();
            if (null != selectQuery2)
                selectQuery2.close();
            if (null != grpChkStmt)
                grpChkStmt.close();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to close connection. ", e);
        }
        super.closeConnection();
    }

    /**
     * Retrieves all the records pertaining to a user
     *
     * @param userId
     * @return
     * @throws SharepointException
     */
    // TODO: get UserGroupMembership as arguement
    public List<UserGroupMembership> getAllGroupsForUser(String username)
            throws SharepointException {
        List<UserGroupMembership> memberships = new ArrayList<UserGroupMembership>();
        ResultSet result = null;
        try {
            selectQuery1.setString(1, UserGroupMembership.getNamePattern(username));
            result = selectQuery1.executeQuery();
        } catch (Exception e) {
            throw new SharepointException(
                    "Could not get the membership information due to following exception ",
                    e);
        }
        if(null == result) {
            return memberships;
        }

        try {
            int columnIndexUserId = result.findColumn("UserId");
            int columnIndexGroupId = result.findColumn("GroupId");
            int columnIndexNamespace = result.findColumn("Namespace");
            for (int i = 0; result.next(); ++i) {
                UserGroupMembership membership = new UserGroupMembership(
                        result.getString(columnIndexUserId),
                        result.getString(columnIndexGroupId),
                        result.getString(columnIndexNamespace));
                memberships.add(membership);
            }
        } catch (Exception e) {
            throw new SharepointException(
                    "Failed to parse the result set to get GroupIds. ", e);
        }
        LOGGER.log(Level.CONFIG, memberships.size()
                + " Memberships identified for user [ " + username + " ]. ");
        return memberships;
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
            selectQuery2.setString(1, membership.getComplexUserId());
            selectQuery2.setString(2, membership.getComplexGroupId());
            selectQuery2.setString(3, membership.getNameSpace());
            ResultSet result = selectQuery2.executeQuery();
            if (!result.next()) {
                insertQuery.setString(1, membership.getComplexUserId());
                insertQuery.setString(2, membership.getComplexGroupId());
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
     * Remove all the memberships of a user. The passed in UserGroupMembership
     * object must have the UserId and Namespace value set
     *
     * @param userId
     * @param namespace
     * @return
     * @throws SharepointException
     */
    public int removeUserMemberships(int userId, String namespace)
            throws SharepointException {
        try {
            deleteQuery1.setString(1, UserGroupMembership.getIdPattern(userId));
            deleteQuery1.setString(2, namespace);
            int count = deleteQuery1.executeUpdate();
            commit();
            LOGGER.log(Level.INFO, "#" + count
                    + " rows deleted corresponding to UserId [ " + userId
                    + " ], Namespace [ " + namespace + " ] ");
            return count;
        } catch (Exception e) {
            throw new SharepointException(
                    "Could not get the user membership information due to following exception ",
                    e);
        }
    }

    /**
     * Remove all the memberships of a group. The passed in UserGroupMembership
     * object must have the GroupId, GroupName and Namespace value set
     *
     * @param userId
     * @param namespace
     * @return
     * @throws SharepointException
     */
    public int removeGroupMemberships(int groupId, String namespace)
            throws SharepointException {
        try {
            deleteQuery2.setString(1, UserGroupMembership.getIdPattern(groupId));
            deleteQuery2.setString(2, namespace);
            int count = deleteQuery2.executeUpdate();
            LOGGER.log(Level.INFO, "#" + count
                    + " rows deleted corresponding to GroupId [ " + groupId
                    + " ], Namespace [ " + namespace + " ] ");
            return count;
        } catch (Exception e) {
            throw new SharepointException(
                    "Could not get the group membership information due to following exception ",
                    e);
        }
    }

    /**
     * Remove all the memberships that belongs to a Namespace
     *
     * @param namespace
     * @return
     * @throws SharepointException
     */
    public int removeAllMembershipsFromNamespace(String namespace)
            throws SharepointException {
        if (null == namespace) {
            return 0;
        }
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

    /**
     * Check whether a group exists in the UserGroupMemberships relation. The
     * GroupId and Namespace value must be set in the passed-in membership
     *
     * @param membership
     * @return
     * @throws SharepointException
     */
    public boolean doesGroupExist(int groupId, String namespace)
            throws SharepointException {
        try {
            grpChkStmt.setString(2, UserGroupMembership.getIdPattern(groupId));
            grpChkStmt.setString(3, namespace);
            grpChkStmt.execute();
            return grpChkStmt.getBoolean(1);
        } catch (Exception e) {
            throw new SharepointException(
                    "Could not confirm the group existance due to following exception ",
                    e);
        }
    }

    /**
     * Check whether a group exists in the UserGroupMemberships relation. The
     * GroupId and Namespace value must be set in the passed-in membership
     *
     * @param membership
     * @return
     * @throws SharepointException
     */
    public boolean doesUserExist(int userId, String namespace)
            throws SharepointException {
        try {
            userChkStmt.setString(2, UserGroupMembership.getIdPattern(userId));
            userChkStmt.setString(3, namespace);
            userChkStmt.execute();
            return userChkStmt.getBoolean(1);
        } catch (Exception e) {
            throw new SharepointException(
                    "Could not confirm the user existance due to following exception ",
                    e);
        }
    }
}
