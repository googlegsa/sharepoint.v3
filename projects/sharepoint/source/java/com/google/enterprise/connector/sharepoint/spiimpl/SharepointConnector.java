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

package com.google.enterprise.connector.sharepoint.spiimpl;

import com.google.common.base.Strings;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.dao.ConnectorNamesDAO;
import com.google.enterprise.connector.sharepoint.dao.QueryProvider;
import com.google.enterprise.connector.sharepoint.dao.UserDataStoreDAO;
import com.google.enterprise.connector.sharepoint.dao.UserGroupMembershipRowMapper;
import com.google.enterprise.connector.sharepoint.ldap.LdapConstants.AuthType;
import com.google.enterprise.connector.sharepoint.ldap.LdapConstants.Method;
import com.google.enterprise.connector.sharepoint.ldap.UserGroupsService.LdapConnectionSettings;
import com.google.enterprise.connector.sharepoint.social.SharepointSocialConnector;
import com.google.enterprise.connector.sharepoint.social.UserProfileServiceFactory;
import com.google.enterprise.connector.sharepoint.wsclient.client.AclWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.ClientFactory;
import com.google.enterprise.connector.sharepoint.wsclient.soap.SPClientFactory;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorPersistentStore;
import com.google.enterprise.connector.spi.ConnectorPersistentStoreAware;
import com.google.enterprise.connector.spi.ConnectorShutdownAware;
import com.google.enterprise.connector.spi.LocalDatabase;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the Connector interface from the spi for SharePoint This is
 * the primary class which represents a new connector instance. Every time a new
 * connector instance is created, an object of this class is created.
 * 
 * @author nitendra_thakur
 */
public class SharepointConnector implements Connector,
    ConnectorPersistentStoreAware, ConnectorShutdownAware {
  private static final Logger LOGGER = Logger.getLogger(SharepointConnector.class.getName());
  private SharepointClientContext sharepointClientContext = null;

  private ClientFactory clientFactory;

  private String sharepointUrl;
  private String kdcserver;
  private String domain;
  private String username;
  private String password;
  private String googleConnectorWorkDir = null;
  private String excludedURls = null;
  private String includedURls = null;
  private String mySiteBaseURL = null;
  private boolean FQDNConversion = false;
  private ArrayList<String> included_metadata = null;
  private ArrayList<String> excluded_metadata = null;
  private String aliasMap = null;
  private String authorizationAsfeedType = null;
  private boolean pushAcls = true;
  private String usernameFormatInAce;
  private String groupnameFormatInAce;
  private QueryProvider queryProvider;
  private UserGroupMembershipRowMapper userGroupMembershipRowMapper;
  private boolean useSPSearchVisibility = true;
  private List<String> infoPathBaseTemplate;
  private boolean reWriteDisplayUrlUsingAliasMappingRules = true;
  private boolean reWriteRecordUrlUsingAliasMappingRules;
  private ConnectorPersistentStore connectorPersistnetStore;
  private boolean fetchACLInBatches = false;
  private int aclBatchSizeFactor = 2;
  private int webServiceTimeOut = 300000;
  private String ldapServerHostAddress;
  private String portNumber;
  private String authenticationType;
  private String connectMethod;
  private String searchBase;
  private String initialCacheSize;
  private boolean useCacheToStoreLdapUserGroupsMembership;
  private String cacheRefreshInterval;
  private LdapConnectionSettings ldapConnectionSettings;
  private boolean feedUnPublishedDocuments;
  private LocalDatabase localDatabseImpl;
  private ConnectorNamesDAO connectorNamesDAO;
  private String connectorName;
  private UserDataStoreDAO userDataStoreDAO;
  private SharepointSocialConnector socialConnector;
  private UserProfileServiceFactory userProfileServiceFactory;

  /**
   * Describes whether user profiles are fetched.
   */
  public enum SocialOption {
    /*
     * Implies user profiles are not fetched by this connector.
     */
    NO,
    /*
     * Implies user profiles fetched along with documents.
     */
    YES,
    /*
     * Implies only user profiles fetched, no documents are fetched.
     */
    ONLY
  };

  private SocialOption socialOption;

  public SharepointConnector() {
    socialConnector = new SharepointSocialConnector(this.sharepointClientContext);
  }

  /**
   * Constructs a connector instance using the specified client factory class.
   */
  SharepointConnector(String clientFactoryClass) {
    this(newClientFactory(clientFactoryClass));
  }

  SharepointConnector(ClientFactory clientFactory) {
    this.clientFactory = clientFactory;
  }

  private static ClientFactory newClientFactory(String clientFactoryClass) {
    if (LOGGER.isLoggable(Level.CONFIG))
      LOGGER.config("NEW INSTANCE: " + clientFactoryClass);
    try {
      return (ClientFactory) Class.forName(clientFactoryClass).newInstance();
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unable to create client factory '" +
        clientFactoryClass + "'. " + e.getMessage(), e);
      return new SPClientFactory();
    }
  }

  /**
   * Returns the client factory for the web services.
   *
   * @return a client factory object
   */
  public ClientFactory getClientFactory() {
    return clientFactory;
  }

  /**
   * Sets the client factory for the web services.
   *
   * @param clientFactory the client factory to use for the web services
   */
  public void setClientFactory(final ClientFactory clientFactory) {
    this.clientFactory = clientFactory;
  }

  /**
   * sets the FQDNConversion parameter.
   * 
   * @param conversion If true: tries to convert the non-FQDN URLs to FQDN If
   *          false: no conversion takes place
   */
  public void setFQDNConversion(final boolean conversion) {
    FQDNConversion = conversion;
    if (sharepointClientContext != null) {
      sharepointClientContext.setFQDNConversion(conversion);
    }
    LOGGER.config("FQDN Value Set to [" + conversion + "]");
  }

  /**
   * returns a session object for the current connector instance
   */
  public Session login() throws RepositoryException {
    LOGGER.info("Connector login()");
    if (sharepointClientContext.isPushAcls()) {
      try {
        this.connectorName = Util
            .getConnectorNameFromDirectoryUrl(googleConnectorWorkDir);
        connectorNamesDAO = new ConnectorNamesDAO(localDatabseImpl
            .getDataSource(), queryProvider);
        // Add current connector instance name to the database table.
        connectorNamesDAO.addConnectorInstanceName(connectorName);
        clientFactory.getAclWS(sharepointClientContext, null).checkConnectivity();
      } catch (Exception e) {
        throw new RepositoryException(
            "Crawling cannot proceed because ACL web service cannot be contacted and hence, "
                + "ACLs cannot be retrieved while crawling. You may still make the connector crawl "
                + "by setting the ACL flag as false in connectorInstance.xml. ",
            e);
      }
    }
    Session socialSession = null;
    if (sharepointClientContext.getSocialOption() != SocialOption.NO) {
      try {
        socialSession = socialConnector.login();
      } catch (RepositoryException e) {
        LOGGER
            .warning("Social Connection login failed with exception message: "
                + e.getMessage());
        LOGGER
            .warning("This can be normal if the target Sharepoint server is pre 2010");
      }
    }
    return new SharepointSession(this, sharepointClientContext, socialSession);
  }

  /**
   * Sets the metadata to be included
   * 
   * @param inExcluded_metadata
   */
  public void setExcluded_metadata(final ArrayList<String> inExcluded_metadata) {
    excluded_metadata = inExcluded_metadata;
    if (sharepointClientContext != null) {
      sharepointClientContext.setExcluded_metadata(inExcluded_metadata);
    }
    LOGGER.config("excluded_metadata Set to [" + inExcluded_metadata.toString()
        + "]");
  }

  /**
   * Sets the excluded metadata
   * 
   * @param inIncluded_metadata
   */
  public void setIncluded_metadata(final ArrayList<String> inIncluded_metadata) {
    included_metadata = inIncluded_metadata;
    if (sharepointClientContext != null) {
      sharepointClientContext.setIncluded_metadata(inIncluded_metadata);
    }
    LOGGER.config("included_metadata Set to [" + inIncluded_metadata.toString()
        + "]");
  }

  /**
   * @return the sharepointUrl
   */
  public String getSharepointUrl() {
    return sharepointUrl;
  }

  /**
   * @param sharepointUrl the sharepointUrl to set
   */
  public void setSharepointUrl(final String sharepointUrl) {
    this.sharepointUrl = sharepointUrl;
    socialConnector.setSiteUrl(sharepointUrl);
  }

  /**
   * @return the domain
   */
  public String getDomain() {
    return domain;
  }

  /**
   * @param domain the domain to set
   */
  public void setDomain(final String domain) {
    this.domain = domain;
    socialConnector.setDomain(domain);
  }

  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param username the username to set
   */
  public void setUsername(final String username) {
    this.username = username;
    socialConnector.setUserName(username);
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password the password to set
   */
  public void setPassword(final String password) {
    this.password = password;
    socialConnector.setPassword(password);
  }

  /**
   * @return the googleConnectorWorkDir
   */
  public String getGoogleConnectorWorkDir() {
    return googleConnectorWorkDir;
  }

  /**
   * @param googleConnectorWorkDir the googleConnectorWorkDir to set
   */
  public void setGoogleConnectorWorkDir(final String googleConnectorWorkDir) {
    this.googleConnectorWorkDir = googleConnectorWorkDir;
  }

  /**
   * @return the excludedURls
   */
  public String getExcludedURls() {
    return excludedURls;
  }

  /**
   * @param excludedURls the excludedURls to set
   */
  public void setExcludedURls(final String excludedURls) {
    this.excludedURls = excludedURls;
  }

  /**
   * @return the includedURls
   */
  public String getIncludedURls() {
    return includedURls;
  }

  /**
   * @param includedURls the includedURls to set
   */
  public void setIncludedURls(final String includedURls) {
    this.includedURls = includedURls;
  }

  /**
   * @return the mySiteBaseURL
   */
  public String getMySiteBaseURL() {
    return mySiteBaseURL;
  }

  /**
   * @param mySiteBaseURL the mySiteBaseURL to set
   */
  public void setMySiteBaseURL(final String mySiteBaseURL) {
    this.mySiteBaseURL = mySiteBaseURL;
    socialConnector.setMySiteBaseURL(mySiteBaseURL);
  }

  /**
   * @return the aliasMap
   */
  public String getAliasMap() {
    return aliasMap;
  }

  /**
   * @param aliasMap the aliasMap to set
   */
  public void setAliasMap(final String aliasMap) {
    this.aliasMap = aliasMap;
  }

  /**
   * @return the authorization
   */
  public String getAuthorization() {
    return authorizationAsfeedType;
  }

  /**
   * @param authorization the authorization to set
   */
  public void setAuthorization(final String authorization) {
    this.authorizationAsfeedType = authorization;
  }

  /**
   * @return SocialOption: no means social crawl wont be done (default) yes
   *         means will be one only means only social crawl will be done, doc
   *         crawl will not be done
   */
  public String getSocialOption() {
    if (socialOption == SocialOption.YES)
      return SPConstants.SOCIAL_OPTION_YES;
    if (socialOption == SocialOption.ONLY)
      return SPConstants.SOCIAL_OPTION_ONLY;
    return SPConstants.SOCIAL_OPTION_NO;
  }

  private SocialOption getSocialOptionInternal() {
    return (socialOption == null) ? SocialOption.NO : socialOption;
  }

  /**
   * @param value
   */
  public void setSocialOption(String value) {
    if (value == null) {
      socialOption = SocialOption.NO;
    } else if (value.equalsIgnoreCase(SPConstants.SOCIAL_OPTION_YES)) {
      socialOption = SocialOption.YES;
    } else if (value.equalsIgnoreCase(SPConstants.SOCIAL_OPTION_ONLY)) {
      socialOption = SocialOption.ONLY;
    } else {
      socialOption = SocialOption.NO;
    }
  }

  public void init() throws SharepointException {
    LOGGER.config("sharepointUrl = [" + sharepointUrl + "] , domain = ["
        + domain + "] , username = [" + username + "] , "
        + "googleConnectorWorkDir = [" + googleConnectorWorkDir
        + "] , includedURls = [" + includedURls + "] , " + "excludedURls = ["
        + excludedURls + "] , mySiteBaseURL = [" + mySiteBaseURL
        + "] , aliasHostPort = [" + aliasMap + "], pushAcls = [" + pushAcls
        + "], useCacheToStoreLdapUserGroupsMembership = ["
        + useCacheToStoreLdapUserGroupsMembership + "], initialCacheSize = ["
        + initialCacheSize + "], cacheRefreshInterval = ["
        + cacheRefreshInterval + "], ldapServerHostAddress = ["
        + ldapServerHostAddress + "], portNumber = [" + portNumber
        + "], authenticationType = [" + authenticationType
        + "], connectMethod = [" + connectMethod + "], searchBase = ["
        + searchBase + " ]" + "], feedUnPublishedDocuments = ["
        + feedUnPublishedDocuments + "]");

    sharepointClientContext = new SharepointClientContext(clientFactory,
        sharepointUrl, domain, kdcserver, username, password, 
        googleConnectorWorkDir, includedURls, excludedURls, mySiteBaseURL, 
        aliasMap, FeedType.getFeedType(authorizationAsfeedType),
        useSPSearchVisibility);
    sharepointClientContext.setFQDNConversion(FQDNConversion);
    sharepointClientContext.setIncluded_metadata(included_metadata);
    sharepointClientContext.setExcluded_metadata(excluded_metadata);
    sharepointClientContext.setInfoPathBaseTemplate(infoPathBaseTemplate);
    sharepointClientContext.setUsernameFormatInAce(getUsernameFormatInAce());
    sharepointClientContext.setGroupnameFormatInAce(this
        .getGroupnameFormatInAce());
    sharepointClientContext.setPushAcls(pushAcls);
    sharepointClientContext.setFetchACLInBatches(this.fetchACLInBatches);
    sharepointClientContext.setAclBatchSizeFactor(this.aclBatchSizeFactor);
    sharepointClientContext.setWebServiceTimeOut(this.webServiceTimeOut);
    sharepointClientContext.setDomain(this.domain);
    sharepointClientContext
        .setFeedUnPublishedDocuments(this.feedUnPublishedDocuments);
    sharepointClientContext.setSocialOption(getSocialOptionInternal());
    if (pushAcls) {
      sharepointClientContext
          .setLdapConnectionSettings(getLdapConnectionSettings());
      sharepointClientContext
          .setUseCacheToStoreLdapUserGroupsMembership(this.useCacheToStoreLdapUserGroupsMembership);
      if (useCacheToStoreLdapUserGroupsMembership) {
        sharepointClientContext.setCacheRefreshInterval(Long
            .parseLong(this.cacheRefreshInterval));
        sharepointClientContext.setInitialCacheSize(Integer
            .parseInt(this.initialCacheSize));
      }
    }
    sharepointClientContext
        .setUserProfileServiceFactory(this.userProfileServiceFactory);
    socialConnector.init(sharepointClientContext);

  }

  /**
   * @return the included_metadata
   */
  public ArrayList<String> getIncluded_metadata() {
    return included_metadata;
  }

  /**
   * @return the excluded_metadata
   */
  public ArrayList<String> getExcluded_metadata() {
    return excluded_metadata;
  }

  public String getKdcserver() {
    return kdcserver;
  }

  public void setKdcserver(String kdcserver) {
    this.kdcserver = kdcserver;
  }

  public boolean isPushAcls() {
    return pushAcls;
  }

  public void setPushAcls(boolean pushAcls) {
    this.pushAcls = pushAcls;
  }

  public boolean isUseSPSearchVisibility() {
    return useSPSearchVisibility;
  }

  public void setUseSPSearchVisibility(boolean useSPSerachVisibility) {
    this.useSPSearchVisibility = useSPSerachVisibility;
  }

  public List<String> getInfoPathBaseTemplate() {
    return infoPathBaseTemplate;
  }

  public void setInfoPathBaseTemplate(List<String> infoPathBaseTemplate) {
    this.infoPathBaseTemplate = infoPathBaseTemplate;
  }

  public void setQueryProvider(QueryProvider queryProvider) {
    this.queryProvider = queryProvider;
  }

  public void setUserGroupMembershipRowMapper(
      UserGroupMembershipRowMapper userGroupMembershipRowMapper) {
    this.userGroupMembershipRowMapper = userGroupMembershipRowMapper;
  }

  public boolean isReWriteDisplayUrlUsingAliasMappingRules() {
    return reWriteDisplayUrlUsingAliasMappingRules;
  }

  public void setReWriteDisplayUrlUsingAliasMappingRules(
      boolean reWriteDisplayUrlUsingAliasMappingRules) {
    this.reWriteDisplayUrlUsingAliasMappingRules = reWriteDisplayUrlUsingAliasMappingRules;
  }

  public boolean isReWriteRecordUrlUsingAliasMappingRules() {
    return reWriteRecordUrlUsingAliasMappingRules;
  }

  public void setReWriteRecordUrlUsingAliasMappingRules(
      boolean reWriteRecordUrlUsingAliasMappingRules) {
    this.reWriteRecordUrlUsingAliasMappingRules = reWriteRecordUrlUsingAliasMappingRules;
  }

  public String getUsernameFormatInAce() {
    return usernameFormatInAce;
  }

  public void setUsernameFormatInAce(String usernameFormatInAce) {
    this.usernameFormatInAce = usernameFormatInAce;
  }

  public String getGroupnameFormatInAce() {
    return groupnameFormatInAce;
  }

  public void setGroupnameFormatInAce(String groupnameFormatInAce) {
    this.groupnameFormatInAce = groupnameFormatInAce;
  }

  public void setDatabaseAccess(ConnectorPersistentStore databaseAccess) {
    this.connectorPersistnetStore = databaseAccess;
    if (sharepointClientContext.isPushAcls()) {
      performUserDataStoreInitialization();
    }
  }

  /**
   * Perform initialization steps that are required to create User Data Store
   * object. It also loads and register corresponding sqlQueries.properties for
   * selected data base.
   */
  private void performUserDataStoreInitialization() {
    localDatabseImpl = connectorPersistnetStore.getLocalDatabase();
    String locale = localDatabseImpl.getDatabaseType().name();
    LOGGER.config("Data base type : " + locale);
    if (null == locale || locale.length() == 0) {
      locale = "sqlserver";
    }
    queryProvider.setDatabase(locale);
    try {
      queryProvider.init(locale);
      userDataStoreDAO = new UserDataStoreDAO(localDatabseImpl.getDataSource(),
          queryProvider, userGroupMembershipRowMapper);
      LOGGER.config("DAO for UserDataStore created successfully");
    } catch (SharepointException se) {
      LOGGER.log(Level.WARNING, "Failed to create UserDataStoreDAO object. ",
          se);
    }
    sharepointClientContext.setUserDataStoreDAO(userDataStoreDAO);
  }

  /**
   * @return the fetchACLInBatches
   */
  public boolean isFetchACLInBatches() {
    return fetchACLInBatches;
  }

  /**
   * @param fetchACLInBatches the fetchACLInBatches to set
   */
  public void setFetchACLInBatches(boolean fetchACLInBatches) {
    this.fetchACLInBatches = fetchACLInBatches;
  }

  /**
   * @return the aclBatchSizeFactor
   */
  public int getAclBatchSizeFactor() {
    return aclBatchSizeFactor;
  }

  /**
   * @param aclBatchSizeFactor the aclBatchSizeFactor to set
   */
  public void setAclBatchSizeFactor(int aclBatchSizeFactor) {
    if (aclBatchSizeFactor <= 0) {
      throw new IllegalArgumentException(
          "The aclBatchSizeFactor should be greater than zero");
    }
    this.aclBatchSizeFactor = aclBatchSizeFactor;
  }

  /**
   * @return the webServiceTimeOut
   */
  public int getWebServiceTimeOut() {
    return webServiceTimeOut;
  }

  /**
   * @param webServiceTimeOut the webServiceTimeOut to set
   */
  public void setWebServiceTimeOut(int webServiceTimeOut) {
    if (webServiceTimeOut < SPConstants.MINIMUM_TIMEOUT_FOR_WS) {
      LOGGER
          .warning("webServiceTimeOut value specified in the advance configuration of "
              + "connector is less than 1 second, Hence setting it to 5 minitus.");
      this.webServiceTimeOut = SPConstants.DEFAULT_TIMEOUT_FOR_WS;
    } else {
      this.webServiceTimeOut = webServiceTimeOut;
    }
  }

  /**
   * @return LDAp directory service host address.
   */
  public String getLdapServerHostAddress() {
    return ldapServerHostAddress;
  }

  /**
   * @param ldapServerHostAddress the ldapServerHostAddress to set.
   */
  public void setLdapServerHostAddress(String ldapServerHostAddress) {
    this.ldapServerHostAddress = ldapServerHostAddress;
  }

  /**
   * @return LDAP directory server port number.
   */
  public String getPortNumber() {
    return portNumber;
  }

  /**
   * @param portNumber the portNumber to set.
   */
  public void setPortNumber(String portNumber) {
    if (Strings.isNullOrEmpty(portNumber)) {
      this.portNumber = SPConstants.LDAP_DEFAULT_PORT_NUMBER;
    } else {
      this.portNumber = portNumber;
    }
  }

  /**
   * @return LDAP Authentication Type used to connect to LDAP directory server.
   */
  public String getAuthenticationType() {
    AuthType authType;
    if (AuthType.ANONYMOUS.toString().equalsIgnoreCase(
        this.authenticationType.toString())) {
      authType = AuthType.ANONYMOUS;
    } else {
      authType = AuthType.SIMPLE;
    }
    return authType.toString();
  }

  /**
   * @param authenticationType the authenticationType to set.
   */
  public void setAuthenticationType(String authenticationType) {
    this.authenticationType = authenticationType;
  }

  /**
   * @return LDAP directory server connect method.
   */
  public String getConnectMethod() {
    Method method;
    if (Method.SSL.toString().equalsIgnoreCase(this.connectMethod.toString())) {
      method = Method.SSL;
    } else {
      method = Method.STANDARD;
    }
    return method.toString();
  }

  /**
   * @param connectMethod the connectMethod to set.
   */
  public void setConnectMethod(String connectMethod) {
    this.connectMethod = connectMethod;
  }

  /**
   * @return LDAP user search base.
   */
  public String getSearchBase() {
    return searchBase;
  }

  /**
   * @param searchBase the searchBase to set.
   */
  public void setSearchBase(String searchBase) {
    this.searchBase = searchBase;
  }

  /**
   * @return LDAP user groups initial cache size.
   */
  public String getInitialCacheSize() {
    return initialCacheSize;
  }

  /**
   * @param initialCacheSize the initialCacheSize to set.
   */
  public void setInitialCacheSize(String initialCacheSize) {
    this.initialCacheSize = initialCacheSize;
  }

  /**
   * @return true indicates to create a LDAP user groups membership cache.
   */
  public boolean isUseCacheToStoreLdapUserGroupsMembership() {
    return useCacheToStoreLdapUserGroupsMembership;
  }

  /**
   * @param useCacheToStoreLdapUserGroupsMembership the
   *          useCacheToStoreLdapUserGroupsMembership to set.
   */
  public void setUseCacheToStoreLdapUserGroupsMembership(
      boolean useCacheToStoreLdapUserGroupsMembership) {
    this.useCacheToStoreLdapUserGroupsMembership = useCacheToStoreLdapUserGroupsMembership;
  }

  /**
   * @return refresh interval time in seconds.
   */
  public String getCacheRefreshInterval() {
    return cacheRefreshInterval;
  }

  /**
   * @param cacheRefreshInterval the cacheRefreshInterval to set.
   */
  public void setCacheRefreshInterval(String cacheRefreshInterval) {
    this.cacheRefreshInterval = cacheRefreshInterval;
  }

  /**
   * @return {@linkplain LdapConnectionSettings}
   */
  public LdapConnectionSettings getLdapConnectionSettings() {
    AuthType authType;
    if (AuthType.ANONYMOUS.toString().equalsIgnoreCase(
        this.authenticationType.toString())) {
      authType = AuthType.ANONYMOUS;
    } else {
      authType = AuthType.SIMPLE;
    }
    Method method;
    if (Method.SSL.toString().equalsIgnoreCase(this.connectMethod.toString())) {
      method = Method.SSL;
    } else {
      method = Method.STANDARD;
    }
    LdapConnectionSettings ldapConnectionSettings = new LdapConnectionSettings(
        method, this.ldapServerHostAddress, Integer.parseInt(this.portNumber),
        this.searchBase, authType, this.username, this.password, this.domain);
    this.ldapConnectionSettings = ldapConnectionSettings;
    return ldapConnectionSettings;
  }

  /**
   * @param ldapConnectionSettings the ldapConnectiionSettings to set.
   */
  public void setLdapConnectiionSettings(
      LdapConnectionSettings ldapConnectionSettings) {
    this.ldapConnectionSettings = ldapConnectionSettings;
  }

  public boolean isFeedUnPublishedDocuments() {
    return feedUnPublishedDocuments;
  }

  public void setFeedUnPublishedDocuments(boolean feedUnPublishedDocuments) {
    this.feedUnPublishedDocuments = feedUnPublishedDocuments;
  }

  public void setGsaHostAddress(String gsaHost) {
    socialConnector.setGsaHost(gsaHost);
  }

  public String getGsaHostAddress() {
    return socialConnector.getGsaHost();
  }

  public void setGsaAdminUser(String gsaAdmin) {
    socialConnector.setGsaAdminUser(gsaAdmin);
  }

  public String getGsaAdminUser() {
    return socialConnector.getGsaAdminUser();
  }

  public void setGsaAdminPassword(String gsaPassword) {
    socialConnector.setGsaAdminPassword(gsaPassword);
  }

  public void setUserProfileCollection(String name) {
    socialConnector.setUserProfileCollection(name);
  }

  public String getUserProfileCollection(String name) {
    return socialConnector.getUserProfileCollection();
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
    if (!ConnectorNamesDAO.connectorNames.isEmpty()
        && connectorNamesDAO.getAllConnectorNames().size() > 0) {
      LOGGER.info("Deleting the connector with the name [" + connectorName
          + "] from the database table.");
      // Removes the connector name from the database table.
      connectorNamesDAO.removeConnectorName(connectorName);
      Set<String> nameSpaceForTheConnector = new HashSet<String>();
      String tempURL = this.sharepointUrl.trim();
      if (tempURL.endsWith(SPConstants.SLASH)) {
        tempURL = tempURL.substring(0, tempURL.length() - 1);
      }
      nameSpaceForTheConnector.add(tempURL);
      LOGGER.info("Deleting all memberships for the connector" + connectorName
          + " using the name space [" + nameSpaceForTheConnector + "]");
      userDataStoreDAO
          .removeAllMembershipsFromNamespace(nameSpaceForTheConnector);
      if (ConnectorNamesDAO.connectorNames.isEmpty()
          && connectorNamesDAO.getAllConnectorNames().size() == 0) {
        LOGGER.log(Level.INFO,
            "Dropping the user data store table from the data base.");
        // Removes the user data store table from the database.
        userDataStoreDAO.dropUserDataStoreTable();
        // Removes the connector names table from the database.
        LOGGER.log(Level.INFO,
            "Dropping the connector names table from the data base.");
        connectorNamesDAO.dropConnectorNamesTable();
      }
    }
  }

  public ConnectorNamesDAO getConnectorNamesDAO() {
    return connectorNamesDAO;
  }

  public void setConnectorNamesDAO(ConnectorNamesDAO connectorNamesDAO) {
    this.connectorNamesDAO = connectorNamesDAO;
  }

  public String getConnectorName() {
    return connectorName;
  }

  public void setConnectorName(String connectorName) {
    this.connectorName = connectorName;
  }

  public UserDataStoreDAO getUserDataStoreDAO() {
    return userDataStoreDAO;
  }

  public void setUserDataStoreDAO(UserDataStoreDAO userDataStoreDAO) {
    this.userDataStoreDAO = userDataStoreDAO;
  }

  public void setUserProfileServiceFactory(UserProfileServiceFactory factory) {
    this.userProfileServiceFactory = factory;
  }
}
