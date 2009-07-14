//Copyright 2007 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sharepoint.client;

import java.util.Set;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocumentList;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;

public class SharepointClientTest extends TestCase {
 
  private SharepointClient sharepointClient;
  private GlobalState globalState;
   
  protected void setUp() throws Exception {
	  super.setUp();
	  System.out.println("Initializing SharepointClientContext ...");
	  final SharepointClientContext sharepointClientContext = new SharepointClientContext(TestConfiguration.sharepointUrl, TestConfiguration.domain, 
				  TestConfiguration.username, TestConfiguration.Password, TestConfiguration.googleConnectorWorkDir, 
				  TestConfiguration.includedURls, TestConfiguration.excludedURls, TestConfiguration.mySiteBaseURL, 
				  TestConfiguration.AliasMap, TestConfiguration.feedType);		

	  assertNotNull(sharepointClientContext);
	  sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
      sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);		
		 
	  this.sharepointClient = new SharepointClient(sharepointClientContext);
	  this.globalState = new GlobalState(sharepointClientContext.getGoogleConnectorWorkDir(), sharepointClientContext.getFeedType());	  
  }

  public void testUpdateGlbalState() throws SharepointException {
	  System.out.println("Testing updateGlobalState()...");
	  this.sharepointClient.updateGlobalState(this.globalState);
	  assertNotNull(this.globalState);
	  System.out.println("[ updateGlobalState() ] Test Completed.");
  }
  
  /**
   * Test method for {@link com.google.enterprise.connector.sharepoint.client.
   * SharepointClient#getDocsFromDocumentLibrary()}.
   */
  public void testTraverse() throws SharepointException {
	  System.out.println("Testing [ testTraverse() ]...");
	  final int iPageSizeHint = 100;
    this.sharepointClient.updateGlobalState(this.globalState);
    final Set webStates = this.globalState.getAllWebStateSet();
    final WebState curr_webState = (WebState)(webStates.toArray())[0];    
    final SPDocumentList rs = this.sharepointClient.traverse(this.globalState,curr_webState, iPageSizeHint);
    int numDocs = 0;
    try {
      System.out.println("Documents found - ");
      SPDocument pm;
      if(rs!=null){
    	  pm = (SPDocument) rs.nextDocument();
    	  while(pm!=null){
    		  System.out.println("<document>");
    		  	final Property lastModProp = pm.findProperty(SpiConstants.PROPNAME_LASTMODIFIED);
    		  	if(lastModProp!=null) {
    		  		System.out.println("<lastModify>" + lastModProp.nextValue().toString() + "</lastModify>");
    		  	}
    	        final Property docProp = pm.findProperty(SpiConstants.PROPNAME_DOCID);
    	        if(lastModProp!=null) {
    	        	System.out.println("<docId>" + docProp.nextValue().toString() +"</docId>");
    		  	}
    	        final Property contentUrlProp = pm.findProperty(SpiConstants.PROPNAME_CONTENTURL);
    	        if(contentUrlProp!=null) {
    	        	 System.out.println("<contentUrl>" + contentUrlProp.nextValue().toString() + "</contentUrl>");
    		  	}
    	        final Property searchUrlProp = pm.findProperty(SpiConstants.PROPNAME_SEARCHURL);                
    	        if(searchUrlProp!=null) {
    	        	System.out.println("<searchUrl>" +searchUrlProp.nextValue().toString()+ "</searchUrl>");
    		  	}
    	        final Property listGuidProp = pm.findProperty(Util.LIST_GUID);
    	        if(listGuidProp!=null) {
    	        	System.out.println("<listGuid>" + listGuidProp.nextValue().toString() +"</listguid>");
    		  	}
    	        final Property displayUrlProp = pm.findProperty(SpiConstants.PROPNAME_DISPLAYURL);
    	        if(displayUrlProp!=null) {
    	        	System.out.println("<displayUrl>" + displayUrlProp.nextValue().toString() +"</displayUrl>");  
    		  	}
    	        final Property authorProp= pm.findProperty(SPConstants.AUTHOR);
    	        if(authorProp!=null) {
    	        	 System.out.println("<author>" + authorProp.nextValue().toString() +"</author>");
    		  	}
    	        final Property objTypeProp = pm.findProperty(SPConstants.OBJECT_TYPE);
    	        if(objTypeProp!=null) {
    	        	System.out.println("<"+SPConstants.OBJECT_TYPE+">" + objTypeProp.nextValue().toString() +"<"+SPConstants.OBJECT_TYPE+">");
    		  	}
    	        final Property isPublicProp = pm.findProperty(SpiConstants.PROPNAME_ISPUBLIC);
    	        if(isPublicProp!=null) {
    	        	System.out.println("<isPublic>" + isPublicProp.nextValue().toString() +"</isPublic>");
    		  	}
    	      System.out.println("</document>");
    	        
    	        pm.dumpAllAttrs();
    	        
    	        //check crawling coverage.. check if paticular document is found    	        
    	        numDocs++;
    	      
    		  pm = (SPDocument) rs.nextDocument();
    	  }
      }
    	
    } catch (final RepositoryException e) {
      e.printStackTrace();
    }
   System.out.println("Total dos: "+numDocs);
   System.out.println("[ testTraverse() ] Test Completed.");
  }
}
