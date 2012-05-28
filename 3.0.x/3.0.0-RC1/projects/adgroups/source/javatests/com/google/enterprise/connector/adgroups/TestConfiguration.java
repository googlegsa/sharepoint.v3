//Copyright 2007 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.adgroups;

import com.google.enterprise.connector.sharepoint.dao.QueryProvider;
import com.google.enterprise.connector.sharepoint.dao.SimpleQueryProvider;
import com.google.enterprise.connector.sharepoint.dao.UserGroupMembership;
import com.google.enterprise.connector.sharepoint.dao.UserGroupMembershipRowMapper;
import com.google.enterprise.connector.adgroups.UserGroupsService;
import com.google.enterprise.connector.adgroups.LdapConstants.AuthType;
import com.google.enterprise.connector.adgroups.LdapConstants.Method;
import com.google.enterprise.connector.adgroups.UserGroupsService.LdapConnectionSettings;
import com.google.enterprise.connector.spi.RepositoryException;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.ldap.LdapContext;
import javax.sql.DataSource;

public class TestConfiguration {
  public static String googleConnectorWorkDir;
  public static String googleWorkDir;

  public static String domain;
  public static String username;
  public static String testuser;
  public static String Password;

  public static String appendNamespaceInSPGroup;
  public static String usernameFormatInAce;
  public static String groupnameFormatInAce;
  public static String ldapServerHostAddress;
  public static String portNumber = "389";
  public static String authenticationType;
  public static String connectMethod;
  public static String searchBase;
  public static String initialCacheSize;
  public static boolean useCacheToStoreLdapUserGroupsMembership = false;
  public static String cacheRefreshInterval;

  public static String searchUserID;
  public static String searchUserPwd;

  public static String driverClass;
  public static String dbUrl;
  public static String dbUsername;
  public static String dbPassword;
  public static String dbVendor;
  public static String connectorName;
  private static String UDS_TABLE_NAME;
  private static String UDS_INDEX_NAME;
  public static String userNameFormat1;
  public static String userNameFormat2;
  public static String userNameFormat3;
  // LDAP
  public static long refreshInterval;
  public static int cacheSize;
  public static String ldapuser1;
  public static String ldapuser2;
  public static String ldapuser3;
  public static String ldapuser4;
  public static String ldapuser5;
  public static String ldapuser6;
  public static String nullldapuser;
  public static String ldapgroupname;
  public static String fakeoremptyldapgroupname;
  public static String expectedParentGroup;
  public static Object google;
  public static String ldapgroup;
  public static String ldapuser;
  public static String fakeusername;
  public static String searchUser1;
  public static String searchUser2;
  public static String ldapGroup1;

  static {
    final Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(
          "source/javatests/TestConfig.properties"));
    } catch (final IOException e) {
      System.out.println("Unable to load the property file." + e);
    }
    googleConnectorWorkDir = properties.getProperty("googleConnectorWorkDir");
    googleWorkDir = properties.getProperty("googleWorkDir");
    domain = properties.getProperty("domain");
    username = properties.getProperty("username");
    Password = properties.getProperty("password");

    searchUserID = properties.getProperty("SearchUserID");
    searchUserPwd = properties.getProperty("SearchUserPwd");

    testuser = properties.getProperty("testuser");

    driverClass = properties.getProperty("DriverClass");
    dbUrl = properties.getProperty("DBURL");
    dbUsername = properties.getProperty("DBUsername");
    dbPassword = properties.getProperty("DBPassword");
    dbVendor = properties.getProperty("DBVendor");
    connectorName = properties.getProperty("ConnectorName");
    UDS_TABLE_NAME = properties.getProperty("UDS_TABLE_NAME");
    UDS_INDEX_NAME = properties.getProperty("UDS_INDEX_NAME");
    userNameFormat1 = properties.getProperty("userNameFormat1");
    userNameFormat2 = properties.getProperty("userNameFormat2");
    userNameFormat3 = properties.getProperty("userNameFormat3");

    refreshInterval = new Long(properties.getProperty("refreshInterval")).longValue();
    cacheSize = new Integer(properties.getProperty("cacheSize")).intValue();

    ldapuser1 = properties.getProperty("ldapuser1");
    ldapuser2 = properties.getProperty("ldapuser2");
    ldapuser3 = properties.getProperty("ldapuser3");
    ldapuser4 = properties.getProperty("ldapuser4");
    ldapuser5 = properties.getProperty("ldapuser5");
    ldapuser5 = properties.getProperty("ldapuser5");
    ldapuser5 = properties.getProperty("ldapuser6");
    nullldapuser = properties.getProperty("nullldapuser");

    ldapgroupname = properties.getProperty("ldapgroupname");
    expectedParentGroup = properties.getProperty("expectedParentGroup");
    google = properties.getProperty("google");
    fakeoremptyldapgroupname = properties.getProperty("fakeoremptyldapgroupname");

    ldapgroup = properties.getProperty("ldapgroup");
    ldapuser = properties.getProperty("ldapuser");
    searchUser2 = properties.getProperty("searchUser2");
    searchUser1 = properties.getProperty("searchUser1");
    ldapGroup1 = properties.getProperty("ldapGroup1");
    ldapServerHostAddress = properties.getProperty("ldapServerHostAddress");
    portNumber = properties.getProperty("portNumber");
    authenticationType = properties.getProperty("authenticationType");
    connectMethod = properties.getProperty("connectMethod");
    initialCacheSize = properties.getProperty("initialCacheSize");
    useCacheToStoreLdapUserGroupsMembership = new Boolean(
        properties.getProperty("useCacheToStoreLdapUserGroupsMembership")).booleanValue();
    appendNamespaceInSPGroup = properties.getProperty("appendNamespaceInSPGroup");
    usernameFormatInAce = properties.getProperty("usernameFormatInAce");
    groupnameFormatInAce = properties.getProperty("groupnameFormatInAce");
  }

  public static Map<String, String> getConfigMap() {
    final Map<String, String> configMap = new HashMap<String, String>();

    configMap.put("domain", domain);
    configMap.put("username", username);
    configMap.put("password", Password);
    configMap.put("usernameFormatInAce", usernameFormatInAce);
    configMap.put("groupnameFormatInAce", groupnameFormatInAce);
    configMap.put("ldapServerHostAddress", ldapServerHostAddress);
    configMap.put("portNumber", portNumber);
    configMap.put("authenticationType", authenticationType);
    configMap.put("connectMethod", connectMethod);
    configMap.put("searchBase", searchBase);
    configMap.put("appendNamespaceInSPGroup", appendNamespaceInSPGroup);
    configMap.put("initialCacheSize", initialCacheSize);
    configMap.put("cacheRefreshInterval", cacheRefreshInterval);
    configMap.put("useCacheToStoreLdapUserGroupsMembership", Boolean.toString(useCacheToStoreLdapUserGroupsMembership));

    return configMap;
  }

  /**
   * Returns an instance of the client context with the given parameters
   *
   * @return Instance of client context
   * @throws RepositoryException
   */
  public static ConnectorContext initContext()
      throws RepositoryException {
    final ConnectorContext sharepointClientContext = new ConnectorContext(
        TestConfiguration.domain, TestConfiguration.username,
        TestConfiguration.Password, TestConfiguration.googleConnectorWorkDir);

    sharepointClientContext.setLdapConnectionSettings(TestConfiguration.getLdapConnectionSettings());
    sharepointClientContext.setLdapConnectionSettings(TestConfiguration.getLdapConnectionSettings());
    sharepointClientContext.setUseCacheToStoreLdapUserGroupsMembership(new Boolean(
        useCacheToStoreLdapUserGroupsMembership));
    sharepointClientContext.setInitialCacheSize(TestConfiguration.cacheSize);
    sharepointClientContext.setCacheRefreshInterval(TestConfiguration.refreshInterval);
    return sharepointClientContext;
  }

  /**
   * Returns an instance of {@link AdGroupsConnector} for testing purpose
   *
   * @return Instance of {@link AdGroupsConnector}
   */
  public static AdGroupsConnector getConnectorInstance()
      throws RepositoryException {
    AdGroupsConnector connector = new AdGroupsConnector();
    connector.setDomain(TestConfiguration.domain);
    connector.setUsername(TestConfiguration.username);
    connector.setPassword(TestConfiguration.Password);
    connector.setGoogleConnectorWorkDir(TestConfiguration.googleConnectorWorkDir);
    connector.setCacheRefreshInterval("7200");
    connector.setInitialCacheSize("1000");
    connector.setPortNumber("389");
    connector.setLdapServerHostAddress("10.88.33.159");
    connector.setAuthenticationType("simple");
    connector.setConnectMethod("standard");
    connector.setSearchBase("DC=gdc-psl,DC=net");
    connector.setLdapConnectionSettings(TestConfiguration.getLdapConnectionSettings());
    connector.init();
    return connector;
  }

  /**
   * gets a sample data source for user data store
   *
   * @return
   */
  public static DataSource getUserDataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName(TestConfiguration.driverClass);
    dataSource.setUrl(TestConfiguration.dbUrl);
    dataSource.setUsername(TestConfiguration.dbUsername);
    dataSource.setPassword(TestConfiguration.dbPassword);
    return dataSource;
  }

  public static UserGroupMembershipRowMapper getUserGroupMembershipRowMapper() {
    UserGroupMembershipRowMapper rowMapper = new UserGroupMembershipRowMapper();
    rowMapper.setUserID("SPUserID");
    rowMapper.setUserName("SPUserName");
    rowMapper.setGroupID("SPGroupID");
    rowMapper.setGroupName("SPGroupName");
    rowMapper.setNamespace("SPSite");
    return rowMapper;
  }

  public static QueryProvider getUserDataStoreQueryProvider()
      throws RepositoryException {
    SimpleQueryProvider queryProvider = new SimpleQueryProvider(
        "com.google.enterprise.connector.sharepoint.sql.sqlQueries");
    queryProvider.setUdsTableName(TestConfiguration.UDS_TABLE_NAME);
    queryProvider.setUdsIndexName(TestConfiguration.UDS_INDEX_NAME);
    queryProvider.setDatabase(TestConfiguration.dbVendor);
    queryProvider.init(TestConfiguration.dbVendor);
    return queryProvider;
  }

  public static Set<UserGroupMembership> getMembershipsForNameSpace(
      String namespace) throws RepositoryException {
    Set<UserGroupMembership> memberships = new TreeSet<UserGroupMembership>();
    UserGroupMembership membership1 = new UserGroupMembership(1, "user1", 2,
        "group1", namespace);
    memberships.add(membership1);
    UserGroupMembership membership2 = new UserGroupMembership(2, "user2", 2,
        "group1", namespace);
    memberships.add(membership2);
    UserGroupMembership membership3 = new UserGroupMembership(3, "user3", 2,
        "group2", namespace);
    memberships.add(membership3);

    return memberships;
  }

  public static LdapConnectionSettings getLdapConnectionSettings() {
    LdapConnectionSettings settings = new LdapConnectionSettings(
        Method.STANDARD, "xxx.xxx.xxx.xxx", 389, "DC=gdc-psl,DC=net",
        AuthType.SIMPLE, "googlesp", "xxxx", "gdc-psl.net");
    return settings;
  }

  public static LdapContext getLdapContext() {
    LdapConnectionSettings ldapConnectionSettings = getLdapConnectionSettings();
    UserGroupsService serviceImpl = new UserGroupsService(
        ldapConnectionSettings, TestConfiguration.cacheSize,
        TestConfiguration.refreshInterval, true);
    return serviceImpl.getLdapContext();
  }
}
