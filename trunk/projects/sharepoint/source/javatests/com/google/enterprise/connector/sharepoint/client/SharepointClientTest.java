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
  /*final String sharepointUrl = "http://entpoint05.corp.google.com/unittest";
  final String domain = "ent-qa-d3";
  final String host = "entpoint05.corp.google.com";
  final int port = 80;
  final String username = "testing";
  final String password = "g00gl3";*/
//	credentials of ps4312 site -- moss 2007
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
	  final String SPType = SharepointConnectorType.SP2007;
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
    	        if (url.equals("http://ps4312.persistent.co.in:43386/amitsite/Shared Documents/config.xml")){
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
