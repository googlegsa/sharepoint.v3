//Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.sharepoint.dao;

import com.google.enterprise.connector.sharepoint.TestConfiguration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * @author nageswara_sura
 */
public class ConnectorNamesDAOTest extends TestCase {
  ConnectorNamesDAO connectorNamesDAO;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    super.setUp();
    connectorNamesDAO = new ConnectorNamesDAO(
        TestConfiguration.getUserDataSource(),
        TestConfiguration.getUserDataStoreQueryProvider());
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.sharepoint.dao.ConnectorNamesDAO#addConnectorInstanceName(java.lang.String)}
   * .
   */
  @Test
  public void testAddConnectorInstanceName() {
    String connectorName = "connector";
    try {
      for (int i = 1; i <= 10; i++) {
        connectorNamesDAO.addConnectorInstanceName(connectorName + i);
      }
      assertEquals(10, ConnectorNamesDAO.connectorNames.size());
      assertEquals(10, connectorNamesDAO.getAllConnectorNames().size());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.sharepoint.dao.ConnectorNamesDAO#removeConnectorName(java.lang.String)}
   * .
   */
  @Test
  public void testRemoveConnectorName() {
    String connectorName = "connector";
    try {
      for (int i = 1; i <= 10; i++) {
        connectorNamesDAO.removeConnectorName(connectorName + i);
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
    assertEquals(0, ConnectorNamesDAO.connectorNames.size());
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.sharepoint.dao.ConnectorNamesDAO#dropConnectorNamesTable()}
   * .
   */
  @Test
  public void testDropConnectorNamesTable() {
    int status;
    try {
      status = connectorNamesDAO.dropConnectorNamesTable();
      assertEquals(0, status);
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.sharepoint.dao.ConnectorNamesDAO#getAllConnectorNames()}
   * .
   */
  @Test
  public void testGetAllConnectorNames() {
    String connectorName = "connector";
    try {
      for (int i = 1; i <= 10; i++) {
        connectorNamesDAO.addConnectorInstanceName(connectorName + i);
      }
      assertEquals(ConnectorNamesDAO.connectorNames.size(), connectorNamesDAO.getAllConnectorNames().size());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

}
