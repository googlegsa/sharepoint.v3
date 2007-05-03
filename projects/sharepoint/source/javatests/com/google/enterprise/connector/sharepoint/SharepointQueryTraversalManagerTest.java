package com.google.enterprise.connector.sharepoint;

import com.google.enterprise.connector.sharepoint.SharepointConnector;
import com.google.enterprise.connector.sharepoint.SharepointQueryTraversalManager;
import com.google.enterprise.connector.sharepoint.client.SharepointClient;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.GlobalStateInitializer;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.SpiConstants;

import junit.framework.TestCase;

import java.util.Iterator;


public class SharepointQueryTraversalManagerTest extends TestCase {
  final String sharepointUrl = "http://entpoint05.corp.google.com/unittest";
  final String domain = "ent-qa-d3";
  final String host = "entpoint05.corp.google.com";
  final int port = 80;
  final String username = "testing";
  final String password = "g00gl3";
  private SharepointClient sharepointClient;
  private SharepointConnector connector;
  private SharepointQueryTraversalManager manager;
  
  public void setUp() throws Exception {
    GlobalStateInitializer.init();
    SharepointClientContext sharepointClientContext = new 
      SharepointClientContext(sharepointUrl, domain, username, password);
    sharepointClient = new SharepointClient(sharepointClientContext);
    connector = new SharepointConnector(sharepointUrl, 
        domain, username, password);
    manager = new SharepointQueryTraversalManager(connector, 
        sharepointClientContext);
    
    // important: make it forget whatever it read in from a left-over file
    manager.forgetStateForUnittest();
    super.setUp();    
  }
  
  /**
   * Basic traversal; no hints, no checkpointing
   */
  public void testBasic() {
    try {
      ResultSet rs = manager.startTraversal(null);
      boolean found = false;
      int numDocs = 0;
      for (Iterator<PropertyMap> it = rs.iterator(); it.hasNext(); ) {
        PropertyMap pm = it.next();
        Property urlProp = pm.getProperty(SpiConstants.PROPNAME_SEARCHURL);
        String url = urlProp.getValue().getString();
        if (url.equals("http://entpoint05.corp.google.com:80/unittest/" +
        "TestDocumentLibrary/TestFolder/TestFolder1/webDav.doc")) {
          found = true;
        }
        numDocs++;         
       }
      assertTrue(found);
      assertEquals(IntegrationTest.TOTAL_DOCS, numDocs);
    } catch (RepositoryException e) {
      e.printStackTrace();
      fail("caught exception " + e.toString());
    }
  }
  

  public void testHints() {
    try {
      manager.setBatchHint(3);
      ResultSet rs = manager.startTraversal(null);
      boolean found = false;
      int numDocs = 0;
      for (Iterator<PropertyMap> it = rs.iterator(); it.hasNext(); ) {
        PropertyMap pm = it.next();
        Property urlProp = pm.getProperty(SpiConstants.PROPNAME_SEARCHURL);
        String url = urlProp.getValue().getString();
        System.out.println(url + " from " + 
            pm.getProperty(Util.LIST_GUID).getValue().getString());
        if (url.equals("http://entpoint05.corp.google.com:80/unittest" + 
            "/Shared Documents/sync.doc")) {
          found = true;
        }
        numDocs++;         
      }
      assertTrue(found);
      assertEquals(numDocs, 3); 
      
      // now, get the next bunch:
      rs = manager.startTraversal(null);
      found = false;
      numDocs = 0;
      for (Iterator<PropertyMap> it = rs.iterator(); it.hasNext(); ) {
        PropertyMap pm = it.next();
        Property urlProp = pm.getProperty(SpiConstants.PROPNAME_SEARCHURL);
        String url = urlProp.getValue().getString();
        System.out.println(url + " from " + 
            pm.getProperty(Util.LIST_GUID).getValue().getString());
        if (url.equals("http://entpoint05.corp.google.com:80/unittest" +
            "/TestDocumentLibrary/TestFolder/webDav.doc")) {
          found = true;
        }
        numDocs++;         
      }
      assertTrue(found);
     // TODO: add this back when the unitest site becomes stable.
    //  assertEquals(4, numDocs); 
    } catch (RepositoryException e) {
      e.printStackTrace();
      fail("caught exception " + e.toString());
    }
  }
  
  /**
   * similar to above, but we take a checkpoint in the middle. Results should
   * be the same.
   */
  public void testHintsWithCheckpoint() {
    System.out.println("\n\ntestHintsWithCheckpoint\n\n");
    try {
      manager.setBatchHint(3);
      ResultSet rs = manager.startTraversal(null);
      boolean found = false;
      int numDocs = 0;
      PropertyMap pm = null;
      for (Iterator<PropertyMap> it = rs.iterator(); it.hasNext(); ) {
        pm = it.next();
        Property urlProp = pm.getProperty(SpiConstants.PROPNAME_SEARCHURL);
        String url = urlProp.getValue().getString();
        System.out.println(url + " from " + 
            pm.getProperty(Util.LIST_GUID).getValue().getString());
        if (url.equals("http://entpoint05.corp.google.com:80/unittest" + 
            "/Shared Documents/sync.doc")) {
          found = true;
        }
        numDocs++;         
      }
      assertTrue(found);
      assertEquals(numDocs, 3); 
      
      // use last item for the checkpoint()
      System.out.println("Before checkpoint: ");
      System.out.println(manager.globalState.getStateXML());
      manager.checkpoint(pm);
      System.out.println("\n\nAfter checkpoint: ");
      System.out.println(manager.globalState.getStateXML());
      
      // now, get the next bunch:
      rs = manager.startTraversal(null);
      found = false;
      numDocs = 0;
      for (Iterator<PropertyMap> it = rs.iterator(); it.hasNext(); ) {
        pm = it.next();
        Property urlProp = pm.getProperty(SpiConstants.PROPNAME_SEARCHURL);
        String url = urlProp.getValue().getString();
        System.out.println(url + " from " + 
            pm.getProperty(Util.LIST_GUID).getValue().getString());
        if (url.equals("http://entpoint05.corp.google.com:80/unittest" +
            "/TestDocumentLibrary/TestFolder/webDav.doc")) {
          found = true;
        }
        numDocs++;         
      }
      assertTrue(found);
//    TODO: add this back when the unitest site becomes stable.
//      assertEquals(numDocs, 4); 
    } catch (RepositoryException e) {
      e.printStackTrace();
      fail("caught exception " + e.toString());
    }
  }
  
}
