// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sharepoint.client;

import com.google.enterprise.connector.sharepoint.IntegrationTest;
import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.PropertyMapList;
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
  private GlobalState globalState;
  
  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    SharepointClientContext sharepointClientContext = new 
      SharepointClientContext(sharepointUrl, domain, username, password, null);
    sharepointClient = new SharepointClient(sharepointClientContext);
    globalState = new GlobalState(
        sharepointClientContext.getGoogleConnectorWorkDir());
    super.setUp();
  }

  /**
   * Test method for {@link com.google.enterprise.connector.sharepoint.client.
   * SharepointClient#getDocsFromDocumentLibrary()}.
   */
  public void testTraverse() {
    sharepointClient.updateGlobalState(globalState);
    PropertyMapList rs = sharepointClient.traverse(globalState, 100);
    boolean found = false;
    int numDocs = 0;
    try {
      Iterator<PropertyMap> it = rs.iterator();
      System.out.println("Documents found - ");
      while(it.hasNext()) {
        PropertyMap pm = it.next();
        Property lastModProp = pm.getProperty(SpiConstants.PROPNAME_LASTMODIFIED);
        Property docProp = pm.getProperty(SpiConstants.PROPNAME_DOCID);
        Property contentUrlProp = 
            pm.getProperty(SpiConstants.PROPNAME_CONTENTURL);
        Property searchUrlProp = 
            pm.getProperty(SpiConstants.PROPNAME_SEARCHURL);                
        Property listGuidProp = pm.getProperty(Util.LIST_GUID);
        System.out.println("<document>");
        System.out.println("<docId>" + docProp.getValue().getString() + 
            "</docId>");
        System.out.println("<searchUrl>" + searchUrlProp.getValue().getString() 
            + "</searchUrl>");
        System.out.println("<contentUrl>" + 
            contentUrlProp.getValue().getString() + "</contentUrl>");
        System.out.println("<lastModify>" + 
            lastModProp.getValue().getDate().getTime() + "</lastModify>");
        System.out.println("<listGuid>" + listGuidProp.getValue().getString() +
            "</listguid>");  
        System.out.println("</document>");
        String url = searchUrlProp.getValue().getString();        
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
    assertEquals(IntegrationTest.TOTAL_DOCS, numDocs);
  }
}
