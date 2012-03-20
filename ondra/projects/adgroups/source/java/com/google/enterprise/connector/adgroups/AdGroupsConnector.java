// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.adgroups;

import com.google.enterprise.connector.adgroups.AdCrawler;
import com.google.enterprise.connector.adgroups.AdServer;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorPersistentStore;
import com.google.enterprise.connector.spi.ConnectorPersistentStoreAware;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.Session;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

public class AdGroupsConnector implements Connector, ConnectorPersistentStoreAware {
  private static final Logger LOGGER = Logger.getLogger(AdServer.class.getName());

  public String method;
  public String hostname;
  public String port;
  public String domain;
  public String username;
  public String password;
  
  public AdCrawler crawler;
  
  public DataSource db;

  @Override
  public Session login() throws RepositoryLoginException, RepositoryException {
    crawler = new AdCrawler(db, method, hostname, port, domain, username, password);
    return new AdGroupsSession(this);
  }
  
  public void init() throws Exception {
  }
  
  public boolean setupDatabaseTables() {
    try {
      Statement statement = db.getConnection().createStatement();
      statement.execute("CREATE TABLE IF NOT EXISTS servers (serverid IDENTITY, dn VARCHAR NOT NULL, dsservicename VARCHAR NOT NULL, netbiosname VARCHAR NOT NULL, invocationid VARCHAR NOT NULL, highestcommittedusn INT NOT NULL, lastsync DATETIME);");
      statement.execute("CREATE TABLE IF NOT EXISTS entities (entityid IDENTITY, serverid INT, dn VARCHAR, samaccountname VARCHAR, userprincipalname VARCHAR, primarygroupid INT, sid VARCHAR NOT NULL, objectguid VARCHAR, usnchanged INT, PRIMARY KEY (entityid));");
      statement.execute("CREATE TABLE IF NOT EXISTS members (groupid INT, memberdn VARCHAR, memberid INT, PRIMARY KEY (groupid, memberdn), FOREIGN KEY(groupid) REFERENCES entities (entityid), FOREIGN KEY(memberid) REFERENCES entities (entityid));");
    } catch (SQLException e ){
      LOGGER.warning(e.getMessage());
    }
    return true;
  }

  @Override
  public void setDatabaseAccess(ConnectorPersistentStore store) {
    db = store.getLocalDatabase().getDataSource();
    setupDatabaseTables();
  }
  
  public static void main(String args[]) throws Exception {
    AdGroupsConnector con = new AdGroupsConnector();
    
    con.method = "STANDARD|SSL|";
    con.hostname = "domain1|domain2";
    con.port = "389|636";
    con.domain = "DOMAIN1|DOMAIN2";
    con.username = "username|username";
    con.password = "Password123|Password123";
    
    Class.forName("org.h2.Driver");    
    JdbcDataSource h2 = new JdbcDataSource();
    h2.setURL("jdbc:h2:~/adtest;AUTO_SERVER=TRUE");
    h2.setUser("sa");
    h2.setPassword("");
    con.db = h2;

    con.login();
    con.setupDatabaseTables();
    con.crawler.run();
  }
}
