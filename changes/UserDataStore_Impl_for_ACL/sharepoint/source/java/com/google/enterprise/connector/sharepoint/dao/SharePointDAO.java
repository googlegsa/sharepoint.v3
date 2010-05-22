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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

/**
 * A base class for doing database related operations
 *
 * @author nitendra_thakur
 */
public class SharePointDAO extends TestCase {
    private final Logger LOGGER = Logger.getLogger(SharePointDAO.class.getName());
    private DBConfig dbConfig;
    protected Connection con;

    public SharePointDAO(DBConfig dbConfig) throws SharepointException {
        this.dbConfig = dbConfig;
        boolean status = connect();
        LOGGER.log(Level.INFO, "DataBase Connection Status: " + status);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        closeConnection();
    }

    /**
     * Establishes a connection to the database and return the status
     *
     * @return
     */
    public boolean connect() throws SharepointException {
        try {
            Class.forName(dbConfig.getDriverClass());
        } catch (Exception e) {
            throw new SharepointException("Failed to load mSQL driver [ "
                    + dbConfig.getDriverClass(), e);
        }
        try {
            con = DriverManager.getConnection(dbConfig.getDbUrl(), dbConfig.getUsername(), dbConfig.getPassword());
        } catch (Exception e) {
            throw new SharepointException(
                    "Failed to connect to the database. ", e);
        }
        return (null == con) ? false : true;
    }

    /**
     * Closes the database connection if open
     */
    public void closeConnection() {
        if (null != con) {
            try {
                con.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to close database connection. ", e);
            }
        }
    }

    public void commit() throws SharepointException {
        try {
            if (con.getAutoCommit()) {
                return;
            }
            con.commit();
        } catch (SQLException e) {
            throw new SharepointException(
                    "Exception occurred while commiting... ", e);
        } catch (Exception e) {
            throw new SharepointException(
                    "Exception occurred while commiting... ", e);
        }
    }

    /**
     * Contains configuration values to connect to the database
     *
     * @author nitendra_thakur
     */
    public static class DBConfig {
        private String driverClass;
        private String dbUrl;
        private String username;
        private String password;

        public DBConfig(String driverClass, String dbUrl, String username,
                String password) {
            this.driverClass = driverClass;
            this.dbUrl = dbUrl;
            this.username = username;
            this.password = password;
        }

        public String getDriverClass() {
            return driverClass;
        }

        public String getDbUrl() {
            return dbUrl;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }


    /**
     * Executes the given SQL query
     *
     * @param query The SQL query to be executed
     * @return typically a bean specified as a template during the object
     *         creation
     */
    public ResultSet executeQuery(String query) throws SharepointException {
        try {
            Statement statement = con.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            throw new SharepointException("Failed to execute the query [ "
                    + query + " ]. ", e);
        } catch (Exception e) {
            throw new SharepointException("Failed to execute the query [ "
                    + query + " ]. ", e);
        }
    }

    /**
     * Executes a query statement and returns the no. of rows affected
     *
     * @param query
     * @return
     * @throws SharepointException
     */
    public int executeUpdate(String query) throws SharepointException {
        try {
            Statement statement = con.createStatement();
            return statement.executeUpdate(query);
        } catch (SQLException e) {
            throw new SharepointException("Failed to execute the query [ "
                    + query + " ]. ", e);
        } catch (Exception e) {
            throw new SharepointException("Failed to execute the query [ "
                    + query + " ]. ", e);
        }
    }


    public DatabaseMetaData getDatabaseMetadata() throws SharepointException {
        if (null == con) {
            throw new SharepointException("Connection not established! ");
        }
        try {
            return con.getMetaData();
        } catch (Exception e) {
            throw new SharepointException(
                    "Exception occurred while getting the database information. ",
                    e);
        }
    }

    public DBConfig getDbConfig() {
        return dbConfig;
    }
}
