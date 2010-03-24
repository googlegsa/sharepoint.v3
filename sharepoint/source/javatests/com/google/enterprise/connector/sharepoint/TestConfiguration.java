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

import org.joda.time.DateTime;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

public class TestConfiguration {
    public static String googleConnectorWorkDir;
    public static String googleWorkDir;

    public static String sharepointUrl;
    public static String AliasMap;
    public static String domain;
    public static String kdcserver;
    public static String username;
    public static String Password;
    public static String mySiteBaseURL;
    public static String includedURls;
    public static String excludedURls;

    public static String searchUserID;
    public static String searchUserPwd;
    public static String SearchDocID1;
    public static String SearchDocID2;
    public static String SearchDocID3;

    public static String Site1_URL;
    public static String Site1_List1_GUID;
    public static String Site1_List1_URL;
    public static String Site1_List1_Item1_URL;
    public static String Site1_List1_Item2_URL;
    public static String Site1_List2_GUID;
    public static String Site1_List2_URL;
    public static String Site1_List2_Item1_URL;
    public static String Site1_List2_Item2_URL;

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

    public static ArrayList<String> blackList = new ArrayList<String>();
    public static ArrayList<String> whiteList = new ArrayList<String>();
    public static boolean FQDNflag;
    public static FeedType feedType;

    // For FBA Authentication support
    public static String fbaLoginPath = null;
    public static String fbaUsernameField = null;
    public static String fbaPasswordField = null;
    public static String fbaUserAgent = null;

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
        Password = properties.getProperty("Password");
        mySiteBaseURL = properties.getProperty("mySiteBaseURL");
        includedURls = properties.getProperty("includedURls");
        excludedURls = properties.getProperty("excludedURls");

        searchUserID = properties.getProperty("SearchUserID");
        searchUserPwd = properties.getProperty("SearchUserPwd");
        SearchDocID1 = properties.getProperty("SearchDocID1");
        SearchDocID2 = properties.getProperty("SearchDocID2");
        SearchDocID3 = properties.getProperty("SearchDocID3");

        Site1_URL = properties.getProperty("Site1_URL");
        Site1_List1_GUID = properties.getProperty("Site1_List1_GUID");
        Site1_List1_URL = properties.getProperty("Site1_List1_URL");
        Site1_List1_Item1_URL = properties.getProperty("Site1_List1_Item1_URL");
        Site1_List1_Item2_URL = properties.getProperty("Site1_List1_Item2_URL");
        Site1_List2_GUID = properties.getProperty("Site1_List2_GUID");
        Site1_List2_URL = properties.getProperty("Site1_List2_URL");
        Site1_List2_Item1_URL = properties.getProperty("Site1_List2_Item1_URL");
        Site1_List2_Item2_URL = properties.getProperty("Site1_List2_Item2_URL");

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
        feedType = FeedType.METADATA_URL_FEED;

        fbaLoginPath = properties.getProperty("fbaLoginPath");
        fbaUsernameField = properties.getProperty("fbaUsernameField");
        fbaPasswordField = properties.getProperty("fbaPasswordField");
        fbaUserAgent = properties.getProperty("fbaUserAgent");
    }

    public static Map<String, String> getConfigMap() {
        final Map<String, String> configMap = new HashMap<String, String>();

        configMap.put("sharepointUrl", sharepointUrl);
        configMap.put("AliasMap", AliasMap);
        configMap.put("domain", domain);
        configMap.put("kdcserver", kdcserver);
        configMap.put("username", username);
        configMap.put("Password", Password);
        configMap.put("mySiteBaseURL", mySiteBaseURL);
        configMap.put("includedURls", includedURls);
        configMap.put("excludedURls", excludedURls);

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
                SPConstants.GENERIC_LIST, dt.toCalendar(Locale.ENGLISH), "",
                url, webState);

        ls.setPrimaryKey(primaryKey);
        ls.setType(SPConstants.GENERIC_LIST);
        SPDocument doc = new SPDocument(new Integer(docId).toString(), "X",
                Calendar.getInstance(), null, null);
        ls.setLastDocProcessedForWS(doc);
        ls.setUrl(url);

        ls.setLastMod(dt);

        ls.setCrawlQueue(getDocuments(webState, ls));

        return ls;
    }

    /**
     * Creates a web state
     *
     * @param indexOfLastCrawledList The index value of the list that should be
     *            marked as last crawled list
     * @return instance of {@link WebState}
     */
    public static WebState createWebState(GlobalState globalState,
            SharepointClientContext spContext, String url,
            int indexOfLastCrawledList) throws SharepointException {
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

    public static SharepointClientContext initContext()
            throws SharepointException {
        final SharepointClientContext sharepointClientContext = new SharepointClientContext(
                TestConfiguration.sharepointUrl, TestConfiguration.domain,
                TestConfiguration.kdcserver, TestConfiguration.username,
                TestConfiguration.Password,
                TestConfiguration.googleConnectorWorkDir,
                TestConfiguration.includedURls, TestConfiguration.excludedURls,
                TestConfiguration.mySiteBaseURL, TestConfiguration.AliasMap,
                TestConfiguration.feedType);

        sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
        sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);
        return sharepointClientContext;
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
                doc = new SPDocument(docId.toString(), "X",
                        Calendar.getInstance(),
                        null, ActionType.DELETE);
            } else {
                doc = new SPDocument(docId.toString(), "X",
                        Calendar.getInstance(),
                        null, ActionType.ADD);
            }
            doc.setParentWeb(web);
            doc.setParentList(list);

            listOfDocs.add(doc);
        }

        return listOfDocs;
    }
}
