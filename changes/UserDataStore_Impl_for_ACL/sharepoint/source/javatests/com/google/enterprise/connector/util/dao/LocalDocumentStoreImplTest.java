//Copyright 2010 Google Inc.
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

package com.google.enterprise.connector.util.dao;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.spi.LocalDocumentStore;

import java.util.Calendar;

import javax.sql.DataSource;

import junit.framework.TestCase;

/**
 * @author nitendra_thakur
 *
 */
public class LocalDocumentStoreImplTest extends TestCase {

    LocalDocumentStore docStore;

    /*
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        DataSource dataSource = TestConfiguration.getDataSource();
        SimpleQueryProvider queryProvider = new SimpleQueryProvider("com.google.enterprise.connector.util.dao.sqlQueries");
        queryProvider.setDocTableName("Local_Document_Store");
        queryProvider.init("Test", "mssql");
        docStore = LocalDocumentStoreImpl.getInstance(dataSource, queryProvider);
    }

    public void testStoreDocuemnt() {
        docStore.storeDocument(new SPDocument("1", "http://mycompany/doc1",
                Calendar.getInstance(), "nitin", "DocLib", "TestSite",
                FeedType.METADATA_URL_FEED, SPType.SP2007));
        docStore.storeDocument(new SPDocument("2", "http://mycompany/doc1",
                Calendar.getInstance(), "nitin", "DocLib", "TestSite",
                FeedType.METADATA_URL_FEED, SPType.SP2007));
        docStore.storeDocument(new SPDocument("3", "http://mycompany/doc1",
                Calendar.getInstance(), "nitin", "DocLib", "TestSite",
                FeedType.METADATA_URL_FEED, SPType.SP2007));
        docStore.storeDocument(new SPDocument("4", "http://mycompany/doc1",
                Calendar.getInstance(), "nitin", "DocLib", "TestSite",
                FeedType.METADATA_URL_FEED, SPType.SP2007));
        docStore.flush();
    }
}
