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
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.joda.time.DateTime;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
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

    public static String DocID1;
    public static String DocID2;
    public static String DocID3;

    public static String ParentWebURL;
    public static String ParentWebTitle;
    public static String BaseListID;
    public static String LastModified;
    public static String LastItemID;
    public static String lastItemURL;

    public static ArrayList<String> blackList = new ArrayList<String>();
    public static ArrayList<String> whiteList = new ArrayList<String>();
    public static boolean FQDNflag;
    public static String feedType;

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

        DocID1 = properties.getProperty("DocID1");
        DocID2 = properties.getProperty("DocID2");
        DocID3 = properties.getProperty("DocID3");

        ParentWebURL = properties.getProperty("ParentWebURL");
        ParentWebTitle = properties.getProperty("ParentWebTitle");
        BaseListID = properties.getProperty("BaseListID");
        LastModified = properties.getProperty("LastModified");
        LastItemID = properties.getProperty("LastItemID");
        lastItemURL = properties.getProperty("LastItemURL");

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
        feedType = "metadata-and-url";
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
            String primaryKey, String webId) {
        ListState ls = new ListState(SPConstants.SP2007,
                SPConstants.METADATA_URL_FEED);

        ls.setPrimaryKey(primaryKey);
        ls.setType(SPConstants.GENERIC_LIST);
        SPDocument doc = new SPDocument(new Integer(docId).toString(),
                Calendar.getInstance(), null, null);
        ls.setLastDocument(doc);
        ls.setChangeToken("1;3;d0266ee5-8769-44df-8fb4-31b998f9f006;633857711707900000;10405618");
        ls.setUrl(url);
        DateTime dt = new DateTime(2009, 9, dayOfMonth, 11, 26, 38, 100);
        ls.setLastMod(dt);

        ls.setCrawlQueue(getDocuments(webId, ls.getPrimaryKey()));

        return ls;
    }

    /**
     * Creates a web state
     *
     * @param indexOfLastCrawledList The index value of the list that should be
     *            marked as last crawled list
     * @return instance of {@link WebState}
     */
    public static WebState createWebState(int indexOfLastCrawledList) {
        WebState ws = new WebState(SPConstants.METADATA_URL_FEED);
        ws.setPrimaryKey("http://testcase.com:22819/sites/testissue85");
        DateTime dt = new DateTime();
        ws.setInsertionTime(dt);
        ListState ls = getListState("http://testcase.com:22819/tempSite/Lists/Announcements/AllItems.aspx", 10, 156790, "{872819FC-6FA7-42AF-A71F-DCF7B8CD1E4A}", ws.getPrimaryKey());
        ListState ls2 = getListState("http://testcase.com:22819/tempSite2/Lists/Announcements/AllItems.aspx", 11, 122790, "{872819FC-6FA7-42AF-A71F-DCF7B8CD1G4A}", ws.getPrimaryKey());
        ListState ls3 = getListState("http://testcase.com/tempSite2/Lists/Announcements/AllItems.aspx", 12, 157790, "{872819FC-6FA7-42AF-A71F-DCF7B8CD1T4A}", ws.getPrimaryKey());
        ListState ls4 = getListState("http://testcase.com/tempSite4/Lists/Announcements/AllItems.aspx", 22, 158790, "{872819FC-6FA7-42AF-A71F-DCF7B8RT1T4A}", ws.getPrimaryKey());

        ws.updateList(ls, ls.getLastMod());
        ws.updateList(ls2, ls2.getLastMod());
        ws.updateList(ls3, ls3.getLastMod());
        ws.updateList(ls4, ls4.getLastMod());

        switch (indexOfLastCrawledList) {
        case 1:
            ws.setLastCrawledListID(ls.getPrimaryKey());
            ws.setCurrentList(ls);
            break;
        case 2:
            ws.setLastCrawledListID(ls2.getPrimaryKey());
            ws.setCurrentList(ls2);
            break;
        case 3:
            ws.setLastCrawledListID(ls3.getPrimaryKey());
            ws.setCurrentList(ls3);
            break;
        case 4:
            ws.setLastCrawledListID(ls4.getPrimaryKey());
            ws.setCurrentList(ls4);
            break;
        }

        ws.setWebUrl("http://testcase.com:22819/sites/testissue85");

        return ws;
    }

    /**
     * Returns a list of documents with the given webId and listId
     *
     * @param webId The web-id
     * @param listId The list-id
     * @return The list of documents
     */
    public static List<SPDocument> getDocuments(String webId, String listId) {
        List<SPDocument> listOfDocs = new ArrayList<SPDocument>();

        Random r = new Random();

        for (int i = 0; i < 10; i++) {

            Integer docId = r.nextInt(200000);
            SPDocument doc = null;
            if (1 % 3 == 0) {
                doc = new SPDocument(docId.toString(), Calendar.getInstance(),
                        null, ActionType.DELETE);
            } else {
                doc = new SPDocument(docId.toString(), Calendar.getInstance(),
                        null, ActionType.ADD);
            }
            doc.setWebid(webId);
            doc.setListGuid(listId);

            listOfDocs.add(doc);
        }

        return listOfDocs;
    }
}
