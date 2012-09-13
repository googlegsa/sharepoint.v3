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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointConnector;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SocialUserProfileDocument;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.TraversalManager;

import com.google.enterprise.connector.database.ConnectorPersistentStoreFactory;
import com.google.enterprise.connector.spi.ConnectorPersistentStore;
import com.google.enterprise.connector.util.database.testing.TestJdbcDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SharepointSocialTraversalManagerTest {
  private static final List<String> EXPECTED_NAMES =
      Arrays.asList(MockUserProfileGenerator.names);

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

  private void testUserProfiles(DocumentList docList)
      throws RepositoryException {
    ArrayList<String> names = new ArrayList<String>();
    testUserProfiles(docList, names);
    assertEquals(EXPECTED_NAMES, names);
  }

  private void testUserProfiles(DocumentList docList, ArrayList<String> names)
      throws RepositoryException {
    assertNotNull(docList);
    Document doc;
    while ((doc = docList.nextDocument()) != null) {
      assertTrue(doc.getClass().toString(),
          doc instanceof SocialUserProfileDocument);
      SocialUserProfileDocument socialDoc = (SocialUserProfileDocument) doc;
      // I'm using getUserContent to simplify the comparisons.
      // MockUserProfileGenerator happens to return bare usernames here.
      names.add(socialDoc.getUserContent().toString());

      Property propDocId = doc.findProperty(SpiConstants.PROPNAME_DOCID);
      assertNotNull(propDocId);
      String docid = propDocId.nextValue().toString();
      assertTrue(docid, docid.startsWith("social"));
    }
  }


  @Test
  public void testStartTraversal() throws RepositoryException {
    TraversalManager trav = session.getTraversalManager();
    trav.setBatchHint(500);
    DocumentList docList = trav.startTraversal();
    testUserProfiles(docList);
  }

  @Test
  public void testResumeTraversal_whole() throws RepositoryException {
    TraversalManager trav = session.getTraversalManager();
    trav.setBatchHint(500);
    DocumentList docList = trav.resumeTraversal(
        SharepointSocialUserProfileDocumentList.CHECKPOINT_PREFIX);
    testUserProfiles(docList);
  }

  @Test
  public void testResumeTraversal_partial() throws RepositoryException {
    TraversalManager trav = session.getTraversalManager();
    trav.setBatchHint(500);
    int halfSize = EXPECTED_NAMES.size() / 2;
    int halfway = new MockUserProfileGenerator().getNextValue(halfSize);
    String checkpoint =
        SharepointSocialUserProfileDocumentList.CHECKPOINT_PREFIX
        + ",0," + halfway;
    DocumentList docList = trav.resumeTraversal(checkpoint);
    ArrayList<String> names = new ArrayList<String>();
    testUserProfiles(docList, names);
    assertEquals(EXPECTED_NAMES.subList(halfSize + 1, EXPECTED_NAMES.size()),
        names);
  }

  @Test
  public void testMultipleBatches() throws RepositoryException {
    TraversalManager trav = session.getTraversalManager();
    ArrayList<String> names = new ArrayList<String>();

    int halfSize = EXPECTED_NAMES.size() / 2;
    trav.setBatchHint(halfSize);
    DocumentList docList = trav.startTraversal();
    testUserProfiles(docList, names);
    assertEquals(EXPECTED_NAMES.subList(0, halfSize), names);

    String checkpoint = docList.checkpoint();
    assertTrue(checkpoint, checkpoint.startsWith(
        SharepointSocialUserProfileDocumentList.CHECKPOINT_PREFIX + ",0,"));
    trav.setBatchHint(500);
    docList = trav.resumeTraversal(checkpoint);
    testUserProfiles(docList, names);
    assertEquals(EXPECTED_NAMES, names);
  }

  @Test
  public void testResumeTraversal_nonsocial() throws RepositoryException {
    TraversalManager trav = session.getTraversalManager();
    trav.setBatchHint(500);
    DocumentList docList = trav.resumeTraversal("");
    // We expect no results here because the social option is ONLY.
    assertNull(docList);
  }
}
