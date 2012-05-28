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

package com.google.enterprise.connector.sharepoint;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.dao.QueryProvider;
import com.google.enterprise.connector.sharepoint.dao.SimpleQueryProvider;
import com.google.enterprise.connector.sharepoint.dao.UserGroupMembership;
import com.google.enterprise.connector.sharepoint.dao.UserGroupMembershipRowMapper;
import com.google.enterprise.connector.sharepoint.ldap.UserGroupsService;
import com.google.enterprise.connector.sharepoint.ldap.LdapConstants.AuthType;
import com.google.enterprise.connector.sharepoint.ldap.LdapConstants.Method;
import com.google.enterprise.connector.sharepoint.ldap.UserGroupsService.LdapConnectionSettings;
import com.google.enterprise.connector.sharepoint.social.SharepointSocialClientContext;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointConnector;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointConnector.SocialOption;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.sharepoint.wsclient.client.ClientFactory;
import com.google.enterprise.connector.sharepoint.wsclient.soap.SPClientFactory;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import org.joda.time.DateTime;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.ldap.LdapContext;
import javax.sql.DataSource;

public class TestConfiguration {
  public static String googleConnectorWorkDir;
  public static String googleWorkDir;

  public static String sharepointUrl;
  public static String AliasMap;
  public static String domain;
  public static String kdcserver;
  public static String username;
  public static String testuser;
  public static String Password;
  public static String mySiteBaseURL;
  public static String includedURls;
  public static String excludedURls;
  public static String authorization;
  public static boolean useSPSearchVisibility;

  public static boolean pushAcls = true;
  public static String appendNamespaceInSPGroup;
  public static String usernameFormatInAce;
  public static String groupnameFormatInAce;
  public static String ldapServerHostAddress;
  public static String ldapDomainName;
  public static int portNumber;
  public static String authenticationType;
  public static String connectMethod;
  public static String searchBase;
  public static String initialCacheSize;
  public static boolean useCacheToStoreLdapUserGroupsMembership = false;
  public static String cacheRefreshInterval;

  public static String searchUserID;
  public static String searchUserPwd;
  public static String SearchDocID1;
  public static String SearchDocID2;
  public static String SearchDocID3;
  public static String SearchDocID4;
  public static String SearchDocID113;
  public static String SearchDocID114;
  public static String SearchDocID115;
  public static String SearchDocID116;
  public static String searchDocID117;
  public static String searchDocID118;
  public static String searchDocID119;
  public static String searchDocID120;
  public static String searchDocID121;
  public static String searchDocID122;
  public static String searchDocID123;
  public static String searchDocID124;
  public static String SearchDocID25;
  public static String SearchDocID26;

  public static String Site1_URL;
  public static String Site1_List1_GUID;
  public static String Site1_List1_URL;
  public static String Site1_List1_Item1_URL;
  public static String Site1_List1_Item2_URL;
  public static String Site1_List2_GUID;
  public static String Site1_List2_URL;
  public static String Site1_List2_Item1_URL;
  public static String Site1_List2_Item2_URL;
  public static String Site1_List_Item_MSG_File_URL;

  public static String Site2_URL;
  public static String Site2_List1_GUID;
  public static String Site2_List1_URL;
  public static String Site2_List1_Item1_URL;
  public static String Site2_List1_Item2_URL;
  public static String Site2_List2_GUID;
  public static String Site2_List2_URL;
  public static String Site2_List2_Item1_URL;
  public static String Site2_List2_Item2_URL;

  public static String Site3_URL;
  public static String Site3_List1_GUID;
  public static String Site3_List1_URL;
  public static String Site3_List1_Item1_URL;
  public static String Site3_List1_Item2_URL;
  public static String Site3_List2_GUID;
  public static String Site3_List2_URL;
  public static String Site3_List2_Item1_URL;
  public static String Site3_List2_Item2_URL;
  
  // For GssAcl Test Site 4
  public static String Site4_URL;
  public static String Site4_List1_GUID;
  public static String Site4_List1_URL;
  public static String Site4_List1_Item1_URL;
  public static String Site4_List1_Item2_URL;
  public static String Site4_List2_GUID;
  public static String Site4_List2_URL;
  public static String Site4_List2_Item1_URL;
  public static String Site4_List2_Item2_URL; 
  
  public static String SearchDocID401;
  public static String SearchDocID402;
  public static String SearchDocID403;
  public static String SearchDocID404;
  public static String SearchDocID405;
  public static String SearchDocID406;
  public static String SearchDocID407;
  public static String SearchDocID408;
  public static String SearchDocID409;
  public static String SearchDocID410;
  public static String SearchDocID411;
  public static String SearchDocID412;
  public static String SearchDocID413;
  

  public static String validChangeToken;
  public static int changesSinceToken;

  public static ArrayList<String> blackList = new ArrayList<String>();
  public static ArrayList<String> whiteList = new ArrayList<String>();
  public static boolean FQDNflag;
  public static FeedType feedType;

  public static String driverClass;
  public static String dbUrl;
  public static String dbUsername;
  public static String dbPassword;
  public static String dbVendor;
  public static String connectorName;
  private static String UDS_TABLE_NAME;
  private static String UDS_INDEX_NAME;
  private static String UDS_CONNECTOR_NAME;
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
  public static String groupNameFormatInACE;
  public static String userNameFormatInACE;
  private static String gsaHost;
  //Time zone;
  public static String timeZone;

  private static int gsaPort;
  private static String gsaAdmin;
  private static String gsaAdminPassword;
  private static String socialOption;
  
  public static ClientFactory clientFactory = new SPClientFactory();

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
    sharepointUrl = properties.getProperty("sharepointUrl");
    AliasMap = properties.getProperty("AliasMap");
    domain = properties.getProperty("domain");
    kdcserver = properties.getProperty("kdcserver");
    username = properties.getProperty("username");
    Password = properties.getProperty("password");
    mySiteBaseURL = properties.getProperty("mySiteBaseURL");
    includedURls = properties.getProperty("includedURls");
    excludedURls = properties.getProperty("excludedURls");
    authorization = properties.getProperty("authorization");
    useSPSearchVisibility = new Boolean(
        properties.getProperty("useSPSearchVisibility")).booleanValue();

    useSPSearchVisibility = new Boolean(
        properties.getProperty("useSPSearchVisibility")).booleanValue();

    searchUserID = properties.getProperty("SearchUserID");
    searchUserPwd = properties.getProperty("SearchUserPwd");
    SearchDocID1 = properties.getProperty("SearchDocID1");
    SearchDocID2 = properties.getProperty("SearchDocID2");
    SearchDocID3 = properties.getProperty("SearchDocID3");
    SearchDocID25 = properties.getProperty("SearchDocID25");
    SearchDocID26 = properties.getProperty("SearchDocID26");
    SearchDocID4 = properties.getProperty("SearchDocID4");
    SearchDocID113 = properties.getProperty("SearchDocID113");
    SearchDocID114 = properties.getProperty("SearchDocID114");
    SearchDocID115 = properties.getProperty("SearchDocID115");
    SearchDocID116 = properties.getProperty("SearchDocID116");
    searchDocID117 = properties.getProperty("searchDocID117");
    searchDocID118 = properties.getProperty("searchDocID118");
    searchDocID119 = properties.getProperty("searchDocID119");
    searchDocID120 = properties.getProperty("searchDocID120");
    searchDocID121 = properties.getProperty("searchDocID121");
    searchDocID122 = properties.getProperty("searchDocID122");
    searchDocID123 = properties.getProperty("searchDocID123");
    searchDocID124 = properties.getProperty("searchDocID124");

    testuser = properties.getProperty("testuser");
    Site1_URL = properties.getProperty("Site1_URL");
    Site1_List1_GUID = properties.getProperty("Site1_List1_GUID");
    Site1_List1_URL = properties.getProperty("Site1_List1_URL");
    Site1_List1_Item1_URL = properties.getProperty("Site1_List1_Item1_URL");
    Site1_List1_Item2_URL = properties.getProperty("Site1_List1_Item2_URL");
    Site1_List2_GUID = properties.getProperty("Site1_List2_GUID");
    Site1_List2_URL = properties.getProperty("Site1_List2_URL");
    Site1_List2_Item1_URL = properties.getProperty("Site1_List2_Item1_URL");
    Site1_List2_Item2_URL = properties.getProperty("Site1_List2_Item2_URL");
    Site1_List_Item_MSG_File_URL = properties.getProperty("Site1_List_Item_MSG_File_URL");

    Site2_URL = properties.getProperty("Site2_URL");
    Site2_List1_GUID = properties.getProperty("Site2_List1_GUID");
    Site2_List1_URL = properties.getProperty("Site2_List1_URL");
    Site2_List1_Item1_URL = properties.getProperty("Site2_List1_Item1_URL");
    Site2_List1_Item2_URL = properties.getProperty("Site2_List1_Item2_URL");
    Site2_List2_GUID = properties.getProperty("Site2_List2_GUID");
    Site2_List2_URL = properties.getProperty("Site2_List2_URL");
    Site2_List2_Item1_URL = properties.getProperty("Site2_List2_Item1_URL");
    Site2_List2_Item2_URL = properties.getProperty("Site2_List2_Item2_URL");

    Site3_URL = properties.getProperty("Site3_URL");
    Site3_List1_GUID = properties.getProperty("Site3_List1_GUID");
    Site3_List1_URL = properties.getProperty("Site3_List1_URL");
    Site3_List1_Item1_URL = properties.getProperty("Site3_List1_Item1_URL");
    Site3_List1_Item2_URL = properties.getProperty("Site3_List1_Item2_URL");
    Site3_List2_GUID = properties.getProperty("Site3_List2_GUID");
    Site3_List2_URL = properties.getProperty("Site3_List2_URL");
    Site3_List2_Item1_URL = properties.getProperty("Site3_List2_Item1_URL");
    Site3_List2_Item2_URL = properties.getProperty("Site3_List2_Item2_URL");
    
    Site4_URL = properties.getProperty("Site4_URL");
    Site4_List1_GUID = properties.getProperty("Site4_List1_GUID");
    Site4_List1_URL = properties.getProperty("Site4_List1_URL");
    Site4_List1_Item1_URL = properties.getProperty("Site4_List1_Item1_URL");
    Site4_List1_Item2_URL = properties.getProperty("Site4_List1_Item2_URL");
    Site4_List2_GUID = properties.getProperty("Site4_List2_GUID");
    Site4_List2_URL = properties.getProperty("Site4_List2_URL");
    Site4_List2_Item1_URL = properties.getProperty("Site4_List2_Item1_URL");
    Site4_List2_Item2_URL = properties.getProperty("Site4_List2_Item2_URL");  
    
    SearchDocID401 = properties.getProperty("SearchDocID401");
    SearchDocID402 = properties.getProperty("SearchDocID402");
    SearchDocID403 = properties.getProperty("SearchDocID403");
    SearchDocID404 = properties.getProperty("SearchDocID404");
    SearchDocID405 = properties.getProperty("SearchDocID405");
    SearchDocID406 = properties.getProperty("SearchDocID406");
    SearchDocID407 = properties.getProperty("SearchDocID407");
    SearchDocID408 = properties.getProperty("SearchDocID408");
    SearchDocID409 = properties.getProperty("SearchDocID409");
    SearchDocID410 = properties.getProperty("SearchDocID410");
    SearchDocID411 = properties.getProperty("SearchDocID411");
    SearchDocID412 = properties.getProperty("SearchDocID412");
    SearchDocID413 = properties.getProperty("SearchDocID413");

    validChangeToken = properties.getProperty("ValidChangeToken");
    changesSinceToken = new Integer(properties.getProperty("ChangesSinceToken")).intValue();

    blackList.add(".*cachedcustomprops$");
    blackList.add(".*parserversion$");
    blackList.add(".*ContentType$");
    blackList.add(".*cachedtitle$");
    blackList.add(".*ContentTypeId$");
    blackList.add(".*DocIcon$");
    blackList.add(".*cachedhastheme$");
    blackList.add(".*metatags$");
    blackList.add(".*charset$");
    blackList.add(".*cachedbodystyle$");
    blackList.add(".*cachedneedsrewrite$");

    /*
     * whiteList.add(".*file type$"); whiteList.add(".*vti_title$");
     * whiteList.add(".*vti_author$");
     */
    FQDNflag = false;
    feedType = FeedType.CONTENT_FEED;

    driverClass = properties.getProperty("DriverClass");
    dbUrl = properties.getProperty("DBURL");
    dbUsername = properties.getProperty("DBUsername");
    dbPassword = properties.getProperty("DBPassword");
    dbVendor = properties.getProperty("DBVendor");
    connectorName = properties.getProperty("ConnectorName");
    UDS_TABLE_NAME = properties.getProperty("UDS_TABLE_NAME");
    UDS_INDEX_NAME = properties.getProperty("UDS_INDEX_NAME");
    UDS_CONNECTOR_NAME = properties.getProperty("UDS_CONNECTOR_NAME");
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
    ldapDomainName = properties.getProperty("ldapDomainName");
    portNumber = new Integer(properties.getProperty("portNumber", "389")).intValue();
    authenticationType = properties.getProperty("authenticationType");
    connectMethod = properties.getProperty("connectMethod");
    searchBase = properties.getProperty("searchBase");
    initialCacheSize = properties.getProperty("initialCacheSize");
    pushAcls = new Boolean(properties.getProperty("pushAcls")).booleanValue();
    useCacheToStoreLdapUserGroupsMembership = new Boolean(
        properties.getProperty("useCacheToStoreLdapUserGroupsMembership")).booleanValue();
    appendNamespaceInSPGroup = properties.getProperty("appendNamespaceInSPGroup");
    userNameFormatInACE = properties.getProperty("usernameFormatInAce");
    groupNameFormatInACE = properties.getProperty("groupnameFormatInAce");
    
    timeZone = properties.getProperty("timeZone");

    gsaHost = properties.getProperty("GsaHost");
    gsaPort = Integer.parseInt(properties.getProperty("GsaPort"));
    gsaAdmin = properties.getProperty("GsaAdminUsername");
    gsaAdminPassword = properties.getProperty("GsaAdminPassword");
    
    socialOption = properties.getProperty("SocialOption");
  }

  public static Map<String, String> getConfigMap() {
    final Map<String, String> configMap = new HashMap<String, String>();

    configMap.put("sharepointUrl", sharepointUrl);
    configMap.put("aliasMap", AliasMap);
    configMap.put("domain", domain);
    configMap.put("kdcserver", kdcserver);
    configMap.put("username", username);
    configMap.put("password", Password);
    configMap.put("mySiteBaseURL", mySiteBaseURL);
    configMap.put("includedURls", includedURls);
    configMap.put("excludedURls", excludedURls);
    configMap.put("authorization", authorization);
    configMap.put("useSPSearchVisibility", Boolean.toString(useSPSearchVisibility));
    configMap.put("pushAcls", Boolean.toString(pushAcls));
    configMap.put("usernameFormatInAce", usernameFormatInAce);
    configMap.put("groupnameFormatInAce", groupnameFormatInAce);
    configMap.put("ldapServerHostAddress", ldapServerHostAddress);
    configMap.put("portNumber", Integer.toString(portNumber));
    configMap.put("authenticationType", authenticationType);
    configMap.put("connectMethod", connectMethod);
    configMap.put("searchBase", searchBase);
    configMap.put("appendNamespaceInSPGroup", appendNamespaceInSPGroup);
    configMap.put("initialCacheSize", initialCacheSize);
    configMap.put("cacheRefreshInterval", cacheRefreshInterval);
    configMap.put("useCacheToStoreLdapUserGroupsMembership", Boolean.toString(useCacheToStoreLdapUserGroupsMembership));
    configMap.put(SPConstants.SOCIAL_OPTION, socialOption);
    configMap.put(SPConstants.GSAHOSTADDRESS, gsaHost);
    configMap.put(SPConstants.GSAADMINUSER, gsaAdmin);
    configMap.put(SPConstants.GSAADMINPASSWORD, gsaAdminPassword);
    
    return configMap;
  }

  /**
   * Creates a list state with given input
   *
   * @param url The list URL
   * @param dayOfMonth The day of month in the last modified date
   * @param docId The lastCrawled doc-Id
   * @param primaryKey The primary key for the list
   * @param webId The web state id
   * @return instance of {@link ListState}
   */
  public static ListState getListState(String url, int dayOfMonth, int docId,
      String primaryKey, WebState webState) throws SharepointException {
    DateTime dt = new DateTime(2009, 9, dayOfMonth, 11, 26, 38, 100);
    ListState ls = new ListState(primaryKey, "inTitle",
        SPConstants.GENERIC_LIST, dt.toCalendar(Locale.ENGLISH), "", url,
        webState);

    ls.setPrimaryKey(primaryKey);
    ls.setType(SPConstants.GENERIC_LIST);
    SPDocument doc = new SPDocument(new Integer(docId).toString(), "X",
        Calendar.getInstance(), null);
    ls.setLastDocProcessed(doc);
    ls.setUrl(url);

    ls.setLastMod(dt);

    ls.setCrawlQueue(getDocuments(webState, ls));

    return ls;
  }

  /**
   * Creates a web state
   *
   * @param indexOfLastCrawledList The index value of the list that should be
   *          marked as last crawled list
   * @return instance of {@link WebState}
   */
  public static WebState createWebState(GlobalState globalState,
      SharepointClientContext spContext, String url, int indexOfLastCrawledList)
      throws SharepointException {
    WebState ws = globalState.makeWebState(spContext, url);
    ws.setPrimaryKey(url);
    DateTime dt = new DateTime();
    ws.setInsertionTime(dt);
    ListState ls = getListState("http://testcase.com:22819/tempSite/Lists/Announcements/AllItems.aspx", 10, 156790, "{872819FC-6FA7-42AF-A71F-DCF7B8CD1E4A}", ws);
    ListState ls2 = getListState("http://testcase.com:22819/tempSite2/Lists/Announcements/AllItems.aspx", 11, 122790, "{872819FC-6FA7-42AF-A71F-DCF7B8CD1G4A}", ws);
    ListState ls3 = getListState("http://testcase.com/tempSite2/Lists/Announcements/AllItems.aspx", 12, 157790, "{872819FC-6FA7-42AF-A71F-DCF7B8CD1T4A}", ws);
    ListState ls4 = getListState("http://testcase.com/tempSite4/Lists/Announcements/AllItems.aspx", 22, 158790, "{872819FC-6FA7-42AF-A71F-DCF7B8RT1T4A}", ws);

    ws.AddOrUpdateListStateInWebState(ls, ls.getLastMod());
    ws.AddOrUpdateListStateInWebState(ls2, ls2.getLastMod());
    ws.AddOrUpdateListStateInWebState(ls3, ls3.getLastMod());
    ws.AddOrUpdateListStateInWebState(ls4, ls4.getLastMod());

    switch (indexOfLastCrawledList) {
    case 1:
      ws.setLastCrawledList(ls);
      ws.setCurrentList(ls);
      break;
    case 2:
      ws.setLastCrawledList(ls2);
      ws.setCurrentList(ls2);
      break;
    case 3:
      ws.setLastCrawledList(ls3);
      ws.setCurrentList(ls3);
      break;
    case 4:
      ws.setLastCrawledList(ls4);
      ws.setCurrentList(ls4);
      break;
    }

    ws.setWebUrl("http://testcase.com:22819/sites/testissue85");

    return ws;
  }

  /**
   * Returns an instance of the client context with the given parameters
   *
   * @return Instance of client context
   * @throws SharepointException
   */
  public static SharepointClientContext initContext()
      throws SharepointException {
    return initContext(clientFactory);
  }

  /**
   * Returns an instance of the client context with the given parameters
   *
   * @param clientFactory The client factory to use with the client context
   * @return Instance of client context
   * @throws SharepointException
   */
  public static SharepointClientContext initContext(
      ClientFactory clientFactory) throws SharepointException {
    final SharepointClientContext sharepointClientContext = new SharepointClientContext(
        clientFactory, TestConfiguration.sharepointUrl, TestConfiguration.domain,
        TestConfiguration.kdcserver, TestConfiguration.username,
        TestConfiguration.Password, TestConfiguration.googleConnectorWorkDir,
        TestConfiguration.includedURls, TestConfiguration.excludedURls,
        TestConfiguration.mySiteBaseURL, TestConfiguration.AliasMap,
        TestConfiguration.feedType,
        new Boolean(useSPSearchVisibility).booleanValue());

    sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
    sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);
    sharepointClientContext.setLdapConnectionSettings(TestConfiguration.getLdapConnetionSettings());
    sharepointClientContext.setPushAcls(TestConfiguration.pushAcls);
    sharepointClientContext.setLdapConnectionSettings(TestConfiguration.getLdapConnetionSettings());
    sharepointClientContext.setUseCacheToStoreLdapUserGroupsMembership(new Boolean(
        useCacheToStoreLdapUserGroupsMembership));
    sharepointClientContext.setInitialCacheSize(TestConfiguration.cacheSize);
    sharepointClientContext.setCacheRefreshInterval(TestConfiguration.refreshInterval);
    String socialOptionLc = TestConfiguration.getSocialOption().toLowerCase();
    if (socialOptionLc.equals("yes")) {
      sharepointClientContext.setSocialOption(SocialOption.YES); 
    } else if (socialOptionLc.equals("no")) {
       sharepointClientContext.setSocialOption(SocialOption.NO); 
    } else if (socialOptionLc.equals("only")) {
       sharepointClientContext.setSocialOption(SocialOption.ONLY); 
    }
    return sharepointClientContext;
  }
  
  public static SharepointSocialClientContext initSocialContext(SharepointClientContext parent) {
    final SharepointSocialClientContext ctxt = new SharepointSocialClientContext(parent);
    ctxt.setDomain(TestConfiguration.domain);
    ctxt.setUrl(TestConfiguration.sharepointUrl);
    ctxt.setUserName(TestConfiguration.username);
    ctxt.setPassword(TestConfiguration.Password);
    
    return ctxt;
  }

  /**
   * Returns a list of documents with the given webId and listId
   *
   * @param webId The web-id
   * @param listId The list-id
   * @return The list of documents
   */
  public static List<SPDocument> getDocuments(WebState web, ListState list) {
    List<SPDocument> listOfDocs = new ArrayList<SPDocument>();

    Random r = new Random();

    for (int i = 0; i < 10; i++) {

      Integer docId = r.nextInt(200000);
      SPDocument doc = null;
      if (i % 3 == 0) {
        doc = new SPDocument(docId.toString(), "X", Calendar.getInstance(),
            ActionType.DELETE);
      } else {
        doc = new SPDocument(docId.toString(), "X", Calendar.getInstance(),
            ActionType.ADD);
      }
      doc.setParentWeb(web);
      doc.setParentList(list);

      listOfDocs.add(doc);
    }

    return listOfDocs;
  }

  /**
   * Creates a sample connector state {@link GlobalState} from the passed in
   * values in TestConfig.properties
   *
   * @param sharepointClientContext
   * @return
   * @throws SharepointException
   */
  public static GlobalState initState(
      SharepointClientContext sharepointClientContext)
      throws SharepointException {
    GlobalState globalState = new GlobalState(clientFactory,
        TestConfiguration.googleConnectorWorkDir, TestConfiguration.feedType);

    if (null != Site1_URL && Site1_URL.trim().length() > 0) {
      WebState webstate1 = globalState.makeWebState(sharepointClientContext, TestConfiguration.Site1_URL);
      if (null != Site1_List1_URL && Site1_List1_URL.trim().length() > 0) {
        ListState liststate11 = new ListState(Site1_List1_GUID, "inTitle",
            SPConstants.DOC_LIB, Calendar.getInstance(), "", Site1_List1_URL,
            webstate1);
        List<SPDocument> docs = new ArrayList<SPDocument>();
        if (null != Site1_List1_Item1_URL
            && Site1_List1_Item1_URL.trim().length() > 0) {
          SPDocument doc = new SPDocument("111", Site1_List1_Item1_URL,
              Calendar.getInstance(), ActionType.ADD);
          docs.add(doc);
        }
        if (null != Site1_List1_Item2_URL
            && Site1_List1_Item2_URL.trim().length() > 0) {
          SPDocument doc = new SPDocument("112", Site1_List1_Item2_URL,
              Calendar.getInstance(), ActionType.ADD);
          docs.add(doc);
        }
        liststate11.setCrawlQueue(docs);
        webstate1.AddOrUpdateListStateInWebState(liststate11, new DateTime());
      }
      if (null != Site1_List2_URL && Site1_List2_URL.trim().length() > 0) {
        ListState liststate12 = new ListState(Site1_List2_GUID, "inTitle",
            SPConstants.GENERIC_LIST, Calendar.getInstance(), "",
            Site1_List2_URL, webstate1);
        List<SPDocument> docs = new ArrayList<SPDocument>();
        if (null != Site1_List2_Item1_URL
            && Site1_List2_Item1_URL.trim().length() > 0) {
          SPDocument doc = new SPDocument("121", Site1_List2_Item1_URL,
              Calendar.getInstance(), ActionType.ADD);
          docs.add(doc);
        }
        if (null != Site1_List2_Item2_URL
            && Site1_List2_Item2_URL.trim().length() > 0) {
          SPDocument doc = new SPDocument("122", Site1_List2_Item2_URL,
              Calendar.getInstance(), ActionType.ADD);
          docs.add(doc);
        }
        liststate12.setCrawlQueue(docs);
        webstate1.AddOrUpdateListStateInWebState(liststate12, new DateTime());
      }
    }

    if (null != Site2_URL && Site2_URL.trim().length() > 0) {
      WebState webstate2 = globalState.makeWebState(sharepointClientContext, TestConfiguration.Site2_URL);
      if (null != Site2_List1_URL && Site2_List1_URL.trim().length() > 0) {
        ListState liststate21 = new ListState(Site2_List1_GUID, "inTitle",
            SPConstants.DOC_LIB, Calendar.getInstance(), "", Site2_List1_URL,
            webstate2);
        List<SPDocument> docs = new ArrayList<SPDocument>();
        if (null != Site2_List1_Item1_URL) {
          SPDocument doc = new SPDocument("211", Site2_List1_Item1_URL,
              Calendar.getInstance(), ActionType.ADD);
          docs.add(doc);
        }
        if (null != Site2_List1_Item2_URL) {
          SPDocument doc = new SPDocument("212", Site2_List1_Item2_URL,
              Calendar.getInstance(), ActionType.ADD);
          docs.add(doc);
        }
        liststate21.setCrawlQueue(docs);
        webstate2.AddOrUpdateListStateInWebState(liststate21, new DateTime());
      }
      if (null != Site2_List2_URL && Site2_List2_URL.trim().length() > 0) {
        ListState liststate22 = new ListState(Site1_List2_GUID, "inTitle",
            SPConstants.GENERIC_LIST, Calendar.getInstance(), "",
            Site2_List2_URL, webstate2);
        List<SPDocument> docs = new ArrayList<SPDocument>();
        if (null != Site2_List2_Item1_URL) {
          SPDocument doc = new SPDocument("221", Site2_List2_Item1_URL,
              Calendar.getInstance(), ActionType.ADD);
          docs.add(doc);
        }
        if (null != Site2_List2_Item2_URL) {
          SPDocument doc = new SPDocument("222", Site2_List2_Item2_URL,
              Calendar.getInstance(), ActionType.ADD);
          docs.add(doc);
        }
        liststate22.setCrawlQueue(docs);
        webstate2.AddOrUpdateListStateInWebState(liststate22, new DateTime());
      }
    }

    if (null != Site3_URL && Site3_URL.trim().length() > 0) {
      WebState webstate3 = globalState.makeWebState(sharepointClientContext, TestConfiguration.Site3_URL);
      if (null != Site3_List1_URL && Site3_List1_URL.trim().length() > 0) {
        ListState liststate31 = new ListState(Site3_List1_GUID, "inTitle",
            SPConstants.DOC_LIB, Calendar.getInstance(), "", Site3_List1_URL,
            webstate3);
        List<SPDocument> docs = new ArrayList<SPDocument>();
        if (null != Site3_List1_Item1_URL) {
          SPDocument doc = new SPDocument("311", Site3_List1_Item1_URL,
              Calendar.getInstance(), ActionType.ADD);
          docs.add(doc);
        }
        if (null != Site3_List1_Item2_URL) {
          SPDocument doc = new SPDocument("312", Site3_List1_Item2_URL,
              Calendar.getInstance(), ActionType.ADD);
          docs.add(doc);
        }
        liststate31.setCrawlQueue(docs);
        webstate3.AddOrUpdateListStateInWebState(liststate31, new DateTime());
      }
      if (null != Site3_List2_URL && Site3_List2_URL.trim().length() > 0) {
        ListState liststate32 = new ListState(Site3_List2_GUID, "inTitle",
            SPConstants.GENERIC_LIST, Calendar.getInstance(), "",
            Site3_List2_URL, webstate3);
        List<SPDocument> docs = new ArrayList<SPDocument>();
        if (null != Site3_List2_Item1_URL) {
          SPDocument doc = new SPDocument("321", Site3_List2_Item1_URL,
              Calendar.getInstance(), ActionType.ADD);
          docs.add(doc);
        }
        if (null != Site3_List2_Item2_URL) {
          SPDocument doc = new SPDocument("322", Site2_List2_Item2_URL,
              Calendar.getInstance(), ActionType.ADD);
          docs.add(doc);
        }
        liststate32.setCrawlQueue(docs);
        webstate3.AddOrUpdateListStateInWebState(liststate32, new DateTime());
      }
    }
    if (null != Site4_URL && Site4_URL.trim().length() > 0) {
      WebState webstate4 = globalState.makeWebState(sharepointClientContext, TestConfiguration.Site4_URL);
      if (null != Site4_List1_URL && Site4_List1_URL.trim().length() > 0) {
        ListState liststate41 = new ListState(Site4_List1_GUID, "inTitle",
            SPConstants.DOC_LIB, Calendar.getInstance(), "", Site4_List1_URL,
            webstate4);
        List<SPDocument> docs = new ArrayList<SPDocument>();
        if (null != Site4_List1_Item1_URL) {
          SPDocument doc = new SPDocument("411", Site4_List1_Item1_URL,
              Calendar.getInstance(), ActionType.ADD);
          docs.add(doc);
        }
        if (null != Site4_List1_Item2_URL) {
          SPDocument doc = new SPDocument("412", Site4_List1_Item2_URL,
              Calendar.getInstance(), ActionType.ADD);
          docs.add(doc);
        }
        liststate41.setCrawlQueue(docs);
        webstate4.AddOrUpdateListStateInWebState(liststate41, new DateTime());
      }
      if (null != Site4_List2_URL && Site4_List2_URL.trim().length() > 0) {
        ListState liststate42 = new ListState(Site4_List2_GUID, "inTitle",
            SPConstants.GENERIC_LIST, Calendar.getInstance(), "",
            Site4_List2_URL, webstate4);
        List<SPDocument> docs = new ArrayList<SPDocument>();
        if (null != Site4_List2_Item1_URL) {
          SPDocument doc = new SPDocument("421", Site4_List2_Item1_URL,
              Calendar.getInstance(), ActionType.ADD);
          docs.add(doc);
        }
        if (null != Site4_List2_Item2_URL) {
          SPDocument doc = new SPDocument("422", Site2_List2_Item2_URL,
              Calendar.getInstance(), ActionType.ADD);
          docs.add(doc);
        }
        liststate42.setCrawlQueue(docs);
        webstate4.AddOrUpdateListStateInWebState(liststate42, new DateTime());
      }
    }
    return globalState;
  }

  /**
   * Returns an instance of {@link SharepointConnector} for testing purpose
   *
   * @return Instance of {@link SharepointConnector}
   */
  public static SharepointConnector getConnectorInstance()
      throws SharepointException {
    SharepointConnector connector = new SharepointConnector();
    connector.setSharepointUrl(TestConfiguration.sharepointUrl);
    connector.setDomain(TestConfiguration.domain);
    connector.setUsername(TestConfiguration.username);
    connector.setPassword(TestConfiguration.Password);
    connector.setGoogleConnectorWorkDir(TestConfiguration.googleConnectorWorkDir);
    connector.setIncludedURls(TestConfiguration.includedURls);
    connector.setExcludedURls(TestConfiguration.excludedURls);
    connector.setMySiteBaseURL(TestConfiguration.mySiteBaseURL);
    connector.setAliasMap(TestConfiguration.AliasMap);
    connector.setAuthorization(FeedType.METADATA_URL_FEED.toString());
    connector.setUseSPSearchVisibility(TestConfiguration.useSPSearchVisibility);
    connector.setIncluded_metadata(TestConfiguration.whiteList);
    connector.setExcluded_metadata(TestConfiguration.blackList);
    connector.setFQDNConversion(true);
    connector.setPushAcls(true);
    connector.setCacheRefreshInterval("7200");
    connector.setInitialCacheSize("1000");
    connector.setPortNumber("389");
    connector.setLdapServerHostAddress("10.88.33.159");
    connector.setAuthenticationType("simple");
    connector.setConnectMethod("standard");
    connector.setSearchBase("DC=gdc-psl,DC=net");
    connector.setLdapConnectiionSettings(TestConfiguration.getLdapConnetionSettings());
    connector.setSocialOption(TestConfiguration.getSocialOption());
    connector.setGsaAdminUser(TestConfiguration.getGsaAdmin());
    connector.setGsaAdminPassword(TestConfiguration.getGsaAdminPassword());
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
      throws SharepointException {
    SimpleQueryProvider queryProvider = new SimpleQueryProvider(
        "com.google.enterprise.connector.sharepoint.sql.sqlQueries");
    queryProvider.setUdsTableName(TestConfiguration.UDS_TABLE_NAME);
    queryProvider.setUdsIndexName(TestConfiguration.UDS_INDEX_NAME);
    queryProvider.setCnTableName(TestConfiguration.UDS_CONNECTOR_NAME);
    queryProvider.setDatabase(TestConfiguration.dbVendor);
    queryProvider.init(TestConfiguration.dbVendor);
    return queryProvider;
  }

  public static Set<UserGroupMembership> getMembershipsForNameSpace(
      String namespace) throws SharepointException {
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

  public static LdapConnectionSettings getLdapConnetionSettings() {
    LdapConnectionSettings settings = new LdapConnectionSettings(
        Method.STANDARD, ldapServerHostAddress, portNumber, searchBase,
        AuthType.SIMPLE, username, Password, ldapDomainName);
    return settings;
  }

  public static LdapContext getLdapContext() {
    LdapConnectionSettings ldapConnectionSettings = getLdapConnetionSettings();
    UserGroupsService serviceImpl = new UserGroupsService(
        ldapConnectionSettings, TestConfiguration.cacheSize,
        TestConfiguration.refreshInterval, true);
    return serviceImpl.getLdapContext();
  }
  
  public static String getSocialOption() {
    return socialOption;
  }
  
  public static String getGsaAdmin() {
    return gsaAdmin;
  }
  
  public static String getGsaAdminPassword() {
    return gsaAdminPassword;
  }

  public static String getGsaHost() {
    return gsaHost;
  }
}
