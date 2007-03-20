// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sharepoint.client;

import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.SpiConstants;

import junit.framework.TestCase;

import java.util.Iterator;


public class SharepointClientTest extends TestCase {
  final String sharepointUrl = "http://entpoint05.corp.google.com/unittest";
  final String domain = "ent-qa-d3";
  final String host = "entpoint05.corp.google.com";
  final int port = 80;
  final String username = "testing";
  final String password = "g00gl3";
  private SharepointClient sharepointClient;
  
  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    SharepointClientContext sharepointClientContext = new 
      SharepointClientContext(sharepointUrl, domain, username, password);
    sharepointClient = new SharepointClient(sharepointClientContext);
    super.setUp();
  }

  /**
   * Test method for {@link com.google.enterprise.connector.sharepoint.client.
   * SharepointClient#getSites()}.
   */
  public void testGetSites() {
    ResultSet rs = sharepointClient.getSites();
    int numSites = 0;
    boolean found = false;
    try {
      Iterator<PropertyMap> it = rs.iterator();
      while(it.hasNext()) {
        PropertyMap pm = it.next();
        Property urlProp = pm.getProperty(SpiConstants.PROPNAME_SEARCHURL);
        String url = urlProp.getValue().getString();
        System.out.println(url);
        if (url.equals("http://entpoint05.corp.google.com/unittest/" +
                "site1/site11")) {
          found = true;
        }
        numSites++;
      }
    } catch (RepositoryException e) {
      e.printStackTrace();
    }
    assertTrue(found);
    assertEquals(numSites, 5);
  }

  /**
   * Test method for {@link com.google.enterprise.connector.sharepoint.client.
   * SharepointClient#getDocsFromDocumentLibrary()}.
   */
  public void testGetDocsFromDocumentLibrary() {
    ResultSet rs = sharepointClient.getDocsFromDocumentLibrary();
    boolean found = false;
    int numDocs = 0;
    try {
      Iterator<PropertyMap> it = rs.iterator();
      System.out.println("Documents found - ");
      while(it.hasNext()) {
        PropertyMap pm = it.next();
        Property urlProp = pm.getProperty(SpiConstants.PROPNAME_SEARCHURL);
        String url = urlProp.getValue().getString();
        System.out.println(url);
        if (url.equals("http://entpoint05.corp.google.com:80/unittest/" +
                "TestDocumentLibrary/TestFolder/TestFolder1/webDav.doc")) {
          found = true;
        }
        numDocs++;
      }
    } catch (RepositoryException e) {
      e.printStackTrace();
    }
    assertTrue(found);
    assertEquals(numDocs, 7);
  }
}
