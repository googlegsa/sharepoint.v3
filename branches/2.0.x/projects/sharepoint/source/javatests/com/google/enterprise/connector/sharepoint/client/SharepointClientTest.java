// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sharepoint.client;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.IntegrationTest;
import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;

/**
 *@author amit_kagrawal
 *This class contains test methods to check the functionality of {@link SharepointClient}.
 */
public class SharepointClientTest extends TestCase {
	  final String sharepointUrl = "http://";
	  final String sharepointUrlWithSpaces ="http://";
	  final String host = "host";
	  final int port = 8765;
	  final String SPType = SharepointConnectorType.SP2007;
	  final String username = "username";
	  final String password = "password";
	  final String domain = "domain";
	
	  final String mySiteBaseURL= "http://";
	  final String googleConnWorkDir = null;
	  final String exclURLs ="" ;
	  final String inclURLs ="http://";
	  final String docLibLInternalName = "{62305F35-71EB-4960-8C21-37A8A7ECD818}"; 
	  final String issuesInternalName = "{62305F35-71EB-4960-8C21-37A8A7ECD818}";

  private SharepointClient sharepointClient;
  private GlobalState globalState;
 
  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
	SharepointClientContext sharepointClientContext = new SharepointClientContext(sharepointUrl, domain, username, password, googleConnWorkDir,inclURLs,exclURLs,mySiteBaseURL,null,null,SPType, null, null);
	       
    sharepointClient = new SharepointClient(sharepointClientContext);
    globalState = new GlobalState(sharepointClientContext.getGoogleConnectorWorkDir());
    super.setUp();
  }

  /**
   * Test method for {@link com.google.enterprise.connector.sharepoint.client.
   * SharepointClient#getDocsFromDocumentLibrary()}.
   */
  public void testTraverse() {
	  final int iPageSizeHint = 100;
    sharepointClient.updateGlobalState(globalState);
    SPDocumentList rs = sharepointClient.traverse(globalState,null, iPageSizeHint);
    boolean found = false;
    int numDocs = 0;
    try {
      System.out.println("Documents found - ");
      SPDocument pm;
      if(rs!=null){
    	  pm = (SPDocument) rs.nextDocument();
    	  while(pm!=null){
    	        Property lastModProp = pm.findProperty(SpiConstants.PROPNAME_LASTMODIFIED);
    	        Property docProp = pm.findProperty(SpiConstants.PROPNAME_DOCID);
    	        Property contentUrlProp = pm.findProperty(SpiConstants.PROPNAME_CONTENTURL);
    	        Property searchUrlProp = pm.findProperty(SpiConstants.PROPNAME_SEARCHURL);                
    	        Property listGuidProp = pm.findProperty(Util.LIST_GUID);
    	        Property displayUrlProp = pm.findProperty(SpiConstants.PROPNAME_DISPLAYURL);
    	        Property authorProp= pm.findProperty(SPDocument.AUTHOR);
    	        Property objTypeProp = pm.findProperty(SPDocument.OBJECT_TYPE);
    	        Property isPublicProp = pm.findProperty(SpiConstants.PROPNAME_ISPUBLIC);
    	        
    	        
    	        String url = searchUrlProp.nextValue().toString();
    	        System.out.println("<document>");
    	        System.out.println("<docId>" + docProp.nextValue().toString() +"</docId>");
    	        System.out.println("<searchUrl>" +url+ "</searchUrl>");
    	        System.out.println("<contentUrl>" + contentUrlProp.nextValue().toString() + "</contentUrl>");
    	        System.out.println("<lastModify>" + lastModProp.nextValue().toString() + "</lastModify>");
    	        System.out.println("<displayUrl>" + displayUrlProp.nextValue().toString() +"</displayUrl>");  
    	        System.out.println("<listGuid>" + listGuidProp.nextValue().toString() +"</listguid>");
    	        System.out.println("<author>" + authorProp.nextValue().toString() +"</author>");
    	        System.out.println("<isPublic>" + isPublicProp.nextValue().toString() +"</isPublic>");
    	        System.out.println("<"+SPDocument.OBJECT_TYPE+">" + objTypeProp.nextValue().toString() +"<"+SPDocument.OBJECT_TYPE+">");
    	        System.out.println("</document>");
    	        
    	        //check crawling coverage.. check if paticular document is found
    	        if (url.equals("http://")){
    	          found = true;
    	        }
    	        numDocs++;
    	      
    		  pm = (SPDocument) rs.nextDocument();
    	  }
      }
    	
    } catch (RepositoryException e) {
      e.printStackTrace();
    }
    assertTrue(found);
    System.out.println("Total dos: "+numDocs);
    assertEquals(IntegrationTest.TOTAL_DOCS, numDocs);
  }
}
