// Copyright 2007 Google Inc.  All Rights Reserved.
package com.google.enterprise.connector.sharepoint.client;

import com.google.enterprise.connector.sharepoint.client.BaseList;
import com.google.enterprise.connector.sharepoint.client.Document;
import com.google.enterprise.connector.sharepoint.client.ListsWS;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SharepointException;
import com.google.enterprise.connector.sharepoint.client.SiteDataWS;

import junit.framework.TestCase;

import java.util.List;

/**
 *
 */
public class SiteDataWSTest extends TestCase {
  final String sharepointUrl = "http://entpoint05.corp.google.com/unittest";
  final String domain = "ent-qa-d3";
  final String host = "entpoint05.corp.google.com";
  final int port = 80;
  final String username = "testing";
  final String password = "g00gl3";
  private SiteDataWS siteDataWS;
  private ListsWS listsWS;
  
  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    
    SharepointClientContext sharepointClientContext = new 
      SharepointClientContext(sharepointUrl, domain, username, password); 
    siteDataWS = new SiteDataWS(sharepointClientContext);   
    listsWS = new ListsWS(sharepointClientContext);
    super.setUp();
  }

  /**
   * Test method for {@link 
   * com.google.enterprise.connector.sharepoint.client.SiteDataWS#getSites()}.
   */
  public void testGetSites() {
    boolean foundUnittestSite = false;
    try {
      List sites = siteDataWS.getSites();
      System.out.println("Sites found - ");
      for(int i=0; i< sites.size(); i++) {
        Document doc = (Document) sites.get(i);
        if (doc.getUrl().equals(sharepointUrl));
        foundUnittestSite = true;     
        System.out.println(doc.getUrl());
      }
      assertTrue(foundUnittestSite);
    } catch (SharepointException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }   
  }

  /**
   * Test method for {@link 
   * com.google.enterprise.connector.sharepoint.client.SiteDataWS
   * #getListCollection()}.
   */
  public void testGetListCollection() {
    int numDocLib = 0;
    try {
      List listCollection = siteDataWS.getListCollection();
      for(int i=0; i<listCollection.size(); i++) {
        BaseList baseList = (BaseList) listCollection.get(i);
        if(baseList.getType().equals("DocumentLibrary")) {
          numDocLib++;
        }
      }
      assertEquals(2, numDocLib);
    } catch (SharepointException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
