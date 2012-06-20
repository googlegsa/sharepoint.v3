// Copyright 2012 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.social;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointConnector;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.TraversalManager;

import com.google.enterprise.connector.database.ConnectorPersistentStoreFactory;
import com.google.enterprise.connector.spi.ConnectorPersistentStore;
import com.google.enterprise.connector.util.database.testing.TestJdbcDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SharepointSocialTraversalManagerTest {

  private SharepointConnector connector;
  private Session session;

  static SharepointConnector createSharepointConnector(String socialOption)
      throws Exception {
    SharepointConnector connector = TestConfiguration.getSmallDomainConnectorInstance();
    connector.setUserProfileServiceFactory(new MockUserProfileServiceFactory());
    connector.setFeedUnPublishedDocuments(true);
    connector.setPushAcls(false);
    connector.setSocialOption(socialOption);
    
    return connector;
  }

  @Before
  public void setUp() throws Exception {
    connector = createSharepointConnector("only");
    connector.init();

    TestJdbcDatabase database = new TestJdbcDatabase();
    ConnectorPersistentStoreFactory factory =
        new ConnectorPersistentStoreFactory(database);
    ConnectorPersistentStore store =
        factory.newConnectorPersistentStore("test", "Sharepoint", null);
    connector.setUserGroupMembershipRowMapper(
        TestConfiguration.getUserGroupMembershipRowMapper());
    connector.setQueryProvider(
        TestConfiguration.getUserDataStoreQueryProvider());
    connector.setDatabaseAccess(store);

    session = connector.login();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testStartTraversaSocial() throws RepositoryException {
    TraversalManager trav = session.getTraversalManager();
    trav.setBatchHint(500);
    DocumentList docList = trav.startTraversal();
    assertNotNull(docList);
    Document doc = docList.nextDocument();
    assertNotNull(doc);
    while (doc != null) {
      Property propDocId = doc.findProperty(SpiConstants.PROPNAME_DOCID);
      assertNotNull(propDocId);
      assertTrue(propDocId.nextValue().toString().contains("social"));
      doc = docList.nextDocument();
    }
  }

  @Test
  public void testResumeTraversal() throws RepositoryException {
    TraversalManager trav = session.getTraversalManager();
    trav.setBatchHint(500);
    DocumentList docList = trav.startTraversal();
    assertNotNull(docList);
    docList = trav.resumeTraversal("");
    assertNotNull(docList);
  }
}
