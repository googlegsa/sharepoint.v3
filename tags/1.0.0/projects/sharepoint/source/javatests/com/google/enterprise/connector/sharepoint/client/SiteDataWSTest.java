// Copyright 2007 Google Inc.  All Rights Reserved.
package com.google.enterprise.connector.sharepoint.client;

import java.util.List;

import junit.framework.TestCase;

import com.google.enterprise.connector.spi.RepositoryException;

/**
 *@author amit_kagrawal
 *This class contains test methods to check the functionality of {@link SiteDataWS}.
 */
public class SiteDataWSTest extends TestCase {  
  /*final String sharepointUrl = "http://entpoint05.corp.google.com/unittest";
  final String sharepointUrlWithSpaces = 
      "http://entpoint05.corp.google.com/site with spaces";
  final String domain = "ent-qa-d3";
  final String host = "entpoint05.corp.google.com";
  final int port = 80;
  final String username = "testing";
  final String password = "g00gl3";
  final String includeURL= "https://v-ecsc3:443/ssl";*/
	
// 	credentials of ps4312 site -- moss 2007
//	--------------------------NON ADMIN CREDENTIALS WITH ALTERNATIVE DOMAIN------------------
	  /*final String sharepointUrl = "http://ps4312:43386/amitsite";
	  final String domain = "persistent";
	  final String host = "ps4312";
	  final int port = 43386;
	  final String username = "amit_kagrawal";
	  final String password = "Agrawal!@#";*/
//	--------------------------END: NON ADMIN CREDENTIALS WITH ALTERNATIVE DOMAIN------------------

//	-------------PS4312(MOSS 2007)---------------------------------
	 /* final String sharepointUrl = "http://ps4312:43386/amitsite";
	  final String domain = "ps4312";
	  final String host = "ps4312";
	  final int port = 43386;
	  final String username = "Administrator";
	  final String password = "pspl!@#";
	  final String mySiteBaseURL= "http://ps4312:23508";
	  final String googleConnWorkDir = null;
	  final String exclURLs =null ;
	  final String inclURLs ="http://ps4312:43386,http://ps4312:23508";*/

//	-------------END: PS4312---------------------------------
//	-------------PS4312(MOSS 2007)---------------------------------
	  final String sharepointUrl = "http://ps4312.persistent.co.in:43386/amitsite";
	  final String sharepointUrlWithSpaces ="http://ps4312.persistent.co.in:43386/amitsite  ";
	  final String host = "ps4312";
	  final int port = 43386;
	  
	  final String username = "Administrator";
	  final String password = "pspl!@#";
	  final String domain = "ps4312";
	
	  final String mySiteBaseURL= "http://ps4312.persistent.co.in:23508";
	  final String googleConnWorkDir = null;
	  final String exclURLs ="" ;
	  final String inclURLs ="http://ps4312.persistent.co.in:43386,http://ps4312.persistent.co.in:23508,http://ps4312:43386,http://ps4312:23508";
	  final String docLibLInternalName = "{62305F35-71EB-4960-8C21-37A8A7ECD818}"; 
	  final String issuesInternalName = "{62305F35-71EB-4960-8C21-37A8A7ECD818}";
//	-------------END: PS4312---------------------------------


  private SiteDataWS siteDataWS;
  
  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
	SharepointClientContext sharepointClientContext = new SharepointClientContext(sharepointUrl, domain, username, password, googleConnWorkDir,inclURLs,exclURLs,mySiteBaseURL,null,null);
    siteDataWS = new SiteDataWS(sharepointClientContext);   
    super.setUp();
  }

  /**
   * Test method for {@link 
   * com.google.enterprise.connector.sharepoint.client.SiteDataWS#getSites()}.
   */
  public void testGetAllChildrenSites() {
    try {
      List sites = siteDataWS.getAllChildrenSites();
      System.out.println("Sites found - "+((sites!=null)?sites.size():0));
      if(sites!=null){
	      for (int i = 0; i < sites.size(); i++) {
	        SPDocument doc = (SPDocument) sites.get(i);
	        System.out.println(doc.getUrl());
	      }
	      assertEquals(sites.size(), 2);
      }
      
    } catch (SharepointException e) {      
      e.printStackTrace();
      fail();
    }   
  }

  public void testSiteWithSpaces() {
    
    int numDocLib = 0;    
    try {
      SharepointClientContext sharepointClientContextSpaces =  new SharepointClientContext(sharepointUrlWithSpaces, domain, username, password, googleConnWorkDir,inclURLs,exclURLs,mySiteBaseURL,null,null); 
      SiteDataWS siteDataWSSpaces =new SiteDataWS(sharepointClientContextSpaces);   
      List listCollection = siteDataWSSpaces.getDocumentLibraries();
      System.out.println("SPDocument Libraries found - ");
      for (int i = 0; i < listCollection.size(); i++) {
        BaseList baseList = (BaseList) listCollection.get(i);
        System.out.println(baseList.getTitle());        
        numDocLib++;        
      }
      assertEquals(2, numDocLib);
    } catch (SharepointException e) {
      e.printStackTrace();
      fail();
    } catch (RepositoryException e1) {
      e1.printStackTrace();
      fail();
    }    
  }
  /**
   * Test method for {@link 
   * com.google.enterprise.connector.sharepoint.client.SiteDataWS
   * #getDocumentLibraries()}.
   */
  public void testGetDocumentLibraries() {
    int numDocLib = 0;
    try {
      List listCollection = siteDataWS.getDocumentLibraries();
      System.out.println("SPDocument Libraries found - ");
      for (int i = 0; i < listCollection.size(); i++) {
        BaseList baseList = (BaseList) listCollection.get(i);
        System.out.println(baseList.getTitle());    
        System.out.println(baseList.getType());
        System.out.println(baseList.getInternalName());    
        System.out.println(baseList.getLastMod());
        numDocLib++;        
      }
      assertEquals(2, numDocLib);//test the no: of document libraries returned
    } catch (SharepointException e) {
      e.printStackTrace();
      fail();
    }
  }
  
  /**
   * Test method for {@link 
   * com.google.enterprise.connector.sharepoint.client.SiteDataWS
   * #getGenericLists()}.
   */
  public void testGetGenericLists() {
    int numGenList = 0;
    try {
      List listCollection = siteDataWS.getGenericLists();
      System.out.println("Generic Lists found - ");
      for (int i = 0; i < listCollection.size(); i++) {
        BaseList baseList = (BaseList) listCollection.get(i);
        System.out.println(baseList.getTitle());        
        numGenList++;        
      }
      assertEquals(5, numGenList);//count no: of generic lists
    } catch (SharepointException e) {      
      e.printStackTrace();
      fail();
    }
  }
  
  /**
   * Test method for {@link 
   * com.google.enterprise.connector.sharepoint.client.SiteDataWS
   * #getIssues()}.
   */
  public void testIssues() {
    int numIssues = 0;
    try {
      List listCollection = siteDataWS.getIssues();
      System.out.println("Issues found - ");
      for (int i = 0; i < listCollection.size(); i++) {
        BaseList baseList = (BaseList) listCollection.get(i);
        System.out.println(baseList.getTitle());         
        numIssues++;        
      }
      assertEquals(1, numIssues);//to check the no: of issues returned
    } catch (SharepointException e) {      
      e.printStackTrace();
      fail();
    }
  }
}
