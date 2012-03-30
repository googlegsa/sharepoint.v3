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
import com.google.enterprise.connector.spi.SimpleAuthenticationIdentity;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

public class AdGroupsConnector implements
      Connector, ConnectorPersistentStoreAware {
  private static final Logger LOGGER =
      Logger.getLogger(AdServer.class.getName());

  public String method;
  public String hostname;
  public String port;
  public String domain;
  public String username;
  public String password;

  public AdCrawler crawler;

  public DataSource datasource;

  @Override
  public Session login() throws RepositoryLoginException, RepositoryException {
    crawler = new AdCrawler(
        datasource, method, hostname, port, domain, username, password);
    return new AdGroupsSession(this);
  }

  public void init() throws Exception {
  }

  @Override
  public void setDatabaseAccess(ConnectorPersistentStore store) {
    datasource = store.getLocalDatabase().getDataSource();
  }

  public static void main(String args[]) throws Exception {
    AdGroupsConnector con = new AdGroupsConnector();

    con.method = "STANDARD";
    con.hostname = "domain";
    con.port = "389";
    con.domain = "DOMAIN";
    con.username = "username";
    con.password = "password";

    Class.forName("org.h2.Driver");    
    JdbcDataSource h2 = new JdbcDataSource();
    h2.setURL("jdbc:h2:~/adtest;AUTO_SERVER=TRUE");
    h2.setUser("sa");
    h2.setPassword("");
    con.datasource = h2;

    Session s = con.login();
    con.crawler.run();
    s.getAuthenticationManager().authenticate(new SimpleAuthenticationIdentity("dlouhojmennyuzivatel", null, "SIMPLE"));
  }
}
