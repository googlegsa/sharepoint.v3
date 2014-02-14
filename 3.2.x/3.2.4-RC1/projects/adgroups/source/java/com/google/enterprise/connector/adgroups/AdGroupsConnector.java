// Copyright 2012 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.adgroups;

import com.google.enterprise.connector.adgroups.AdDbUtil.Query;
import com.google.enterprise.connector.adgroups.AdServer;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorPersistentStore;
import com.google.enterprise.connector.spi.ConnectorPersistentStoreAware;
import com.google.enterprise.connector.spi.ConnectorShutdownAware;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.Session;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

public class AdGroupsConnector implements Connector, 
    ConnectorPersistentStoreAware, ConnectorShutdownAware {

  private static final Logger LOGGER =
      Logger.getLogger(AdServer.class.getName());

  private String method;
  private String hostname;
  private String port;
  private String principal;
  private String password;
  private boolean includeBuiltinGroups;

  private DataSource dataSource;
  private String dbType;

  private String globalNamespace;
  private String connectorName;

  @Override
  public Session login() throws RepositoryLoginException, RepositoryException {
    return new AdGroupsSession(this);
  }

  /** Initializes the connector. This is used in the SharePoint connector. */
  public void init() throws RepositoryException {
  }

  @Override
  public void setDatabaseAccess(ConnectorPersistentStore store) {
    setDataSource(store.getLocalDatabase().getDatabaseType().name(),
        store.getLocalDatabase().getDataSource());

    final AdDbUtil db = new AdDbUtil(getDataSource(), getDatabaseType());
    db.ensureConnectorNameInstanceExists(connectorName);
  }

  // this method exists only to simplify testing
  public void setDataSource(String dbType, DataSource dataSource) {
    this.dataSource = dataSource;
    this.dbType = dbType;
  }

  public String getDatabaseType() {
    return dbType;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public String getPrincipal() {
    return principal;
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

  public String getGoogleGlobalNamespace() {
    return globalNamespace;
  }

  public void setGoogleGlobalNamespace(String globalNamespace) {
    this.globalNamespace = globalNamespace;
  }

  /** Gets whether BUILTIN groups should be included in group lookup. */
  public boolean isIncludeBuiltinGroups() {
    return includeBuiltinGroups;
  }

  /** Sets whether BUILTIN groups should be included in group lookup. */
  public void setIncludeBuiltinGroups(boolean includeBuiltinGroups) {
    this.includeBuiltinGroups = includeBuiltinGroups;
  }

  public String getConnectorName() {
    return connectorName;
  }

  public void setConnectorName(String connectorName) {
    this.connectorName = connectorName;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.spi.ConnectorShutdownAware#shutdown()
   */
  public void shutdown() throws RepositoryException {
    LOGGER.info("Shutting down the connector with the name [" + connectorName
        + "]");
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.spi.ConnectorShutdownAware#delete()
   */
  public void delete() throws RepositoryException {
    LOGGER.info("Deleting the connector with the name [" + connectorName
        + "] from the database table.");
    final AdDbUtil db = new AdDbUtil(getDataSource(), getDatabaseType());
    db.deleteConnectorNameInstance(connectorName);
  }
}
