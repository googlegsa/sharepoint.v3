// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.adgroups;

import com.google.enterprise.connector.adgroups.AdServer;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorPersistentStore;
import com.google.enterprise.connector.spi.ConnectorPersistentStoreAware;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.Session;

import java.util.logging.Logger;

import javax.sql.DataSource;

public class AdGroupsConnector implements
    Connector, ConnectorPersistentStoreAware {

  private static final Logger LOGGER =
      Logger.getLogger(AdServer.class.getName());

  public String method;
  public String hostname;
  public String port;
  public String principal;
  public String password;

  public DataSource dataSource;
  public String dbType;

  @Override
  public Session login() throws RepositoryLoginException, RepositoryException {
    return new AdGroupsSession(this);
  }

  public void init() throws Exception {
  }

  @Override
  public void setDatabaseAccess(ConnectorPersistentStore store) {
    setDataSource(store.getLocalDatabase().getDatabaseType().name(),
        store.getLocalDatabase().getDataSource());
  }

  // this method exists only to simplify testing
  public void setDataSource(String dbType, DataSource dataSource) {
    this.dataSource = dataSource;
    this.dbType = dbType;
  }

  public static void main(String args[]) throws Exception {

  }

  public void setPrincipal(String principal) {
    this.principal = principal;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getPrincipal() {
    return principal;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }
}
