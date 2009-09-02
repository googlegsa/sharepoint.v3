package com.google.enterprise.connector.sharepoint.spiimpl;

//Copyright (C) 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.IntegrationTest;
import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointConnector;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointTraversalManager;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;


/**
 * @author amit_kagrawal
 * */
public class SharepointQueryTraversalManagerTest extends TestCase {

  private SharepointConnector connector;
  private SharepointTraversalManager manager;
  
  public void setUp() throws Exception {
	super.setUp(); 
    
	//forget any global state left over from previous runs
    GlobalState.forgetState(null);
    
    System.out.println("Initializing SharepointClientContext ...");
    final SharepointClientContext sharepointClientContext = new SharepointClientContext(TestConfiguration.sharepointUrl, TestConfiguration.domain, 
			  TestConfiguration.username, TestConfiguration.Password, TestConfiguration.googleConnectorWorkDir, 
			  TestConfiguration.includedURls, TestConfiguration.excludedURls, TestConfiguration.mySiteBaseURL, 
			  TestConfiguration.AliasMap, TestConfiguration.feedType);		
    assertNotNull(sharepointClientContext);
    this.connector.setIncluded_metadata(TestConfiguration.whiteList);
	this.connector.setExcluded_metadata(TestConfiguration.blackList);		
	
	System.out.println("Initializing Sharepoint Connector Instance ...");
	this.connector = new SharepointConnector(TestConfiguration.sharepointUrl, TestConfiguration.domain,
			TestConfiguration.username, TestConfiguration.Password, 
			  TestConfiguration.googleConnectorWorkDir, TestConfiguration.includedURls, 
			  TestConfiguration.excludedURls, TestConfiguration.mySiteBaseURL, TestConfiguration.AliasMap,TestConfiguration.feedType);
	assertNotNull(this.connector);
	this.connector.setIncluded_metadata(TestConfiguration.whiteList);
	this.connector.setExcluded_metadata(TestConfiguration.blackList);		
	this.connector.setFQDNConversion(true);
	  
	System.out.println("Initializing Sharepoint Traversal Manager...");
    this.manager = new SharepointTraversalManager(this.connector, sharepointClientContext);
    assertNotNull(this.manager);
  }
  
  /**
   * Basic traversal; no hints, no checkpointing.
   */
  public void testBasic() {
    try {
      final DocumentList docList = this.manager.startTraversal();
      boolean found = false;
      int numDocs = 0;
      SPDocument pm ;
      while((pm = (SPDocument) docList.nextDocument())!=null){
        final Property urlProp = pm.findProperty(SpiConstants.PROPNAME_SEARCHURL);
        final String url = urlProp.nextValue().toString();
        if (url.equals("http://ps4312.persistent.co.in:43386/amitsite/Lists/Announcements/DispForm.aspx?ID=1")) {
          found = true;
        }
        numDocs++;         
       }
      assertTrue(found);
      assertEquals(IntegrationTest.TOTAL_DOCS, numDocs);
    } catch (final RepositoryException e) {
      e.printStackTrace();
      fail("caught exception " + e.toString());
    }
  }
  

  public void testHints() {
    try {
      this.manager.setBatchHint(3);//set the batch hint
      DocumentList docList = this.manager.startTraversal();
      boolean found = false;
      int numDocs = 0;
      SPDocument pm ;
      while((pm = (SPDocument) docList.nextDocument())!=null){
        final Property urlProp = pm.findProperty(SpiConstants.PROPNAME_SEARCHURL);
        final String url = urlProp.nextValue().toString();
        System.out.println(url + " from " 
        		+pm.findProperty(Util.LIST_GUID).nextValue().toString());
        if (url.equals("http://ps4312.persistent.co.in:43386/amitsite/Lists/Announcements/DispForm.aspx?ID=1")) {
            found = true;
        }
        numDocs++;         
      }
      assertTrue(found); //check if the particular doc is found
      assertEquals(numDocs, 4); 
      
      // now, get the next bunch:
      docList = this.manager.startTraversal();
      found = false;
      numDocs = 0;
      while((pm = (SPDocument) docList.nextDocument())!=null){
        final Property urlProp = pm.findProperty(SpiConstants.PROPNAME_SEARCHURL);
        final String url = urlProp.nextValue().toString();
        System.out.println(url + " from " 
        		+pm.findProperty(Util.LIST_GUID).nextValue().toString());
        if (url.equals("http://ps4312.persistent.co.in:43386/amitsite/Lists/issue1/Attachments/2/csej.pdf")){
          found = true;
        }
        numDocs++;         
      }
      assertTrue(found);     
      assertEquals(3, numDocs); 
    } catch (final RepositoryException e) {
      e.printStackTrace();
      fail("caught exception " + e.toString());
    }
  }
  
  /**
   * similar to above, but we take a checkpoint in the middle. Results should
   * be the same.
   */
  public void testHintsWithCheckpoint() {
	final int iDocs=14;
    System.out.println("\n\ntestHintsWithCheckpoint\n\n");
    try {
      this.manager.setBatchHint(3);
      DocumentList rs = this.manager.startTraversal();
      boolean found = false;
      int numDocs = 0;
      
      SPDocument pm ;
      while((pm = (SPDocument) rs.nextDocument())!=null){
        final Property urlProp = pm.findProperty(SpiConstants.PROPNAME_SEARCHURL);
        final String url = urlProp.nextValue().toString();
        System.out.println(url + " from " 
        		+pm.findProperty(Util.LIST_GUID).nextValue().toString());
        if (url.equals("http://ps4312.persistent.co.in:43386/amitsite/Lists/Announcements/DispForm.aspx?ID=1")) {
          found = true;
        }
        numDocs++;         
      }
      assertTrue(found);
      assertEquals(numDocs, 4); 
      
      // use last item for the checkpoint()
      System.out.println("Before checkpoint: ");
      System.out.println(this.manager.globalState.getStateXML());
      rs.checkpoint();
      System.out.println("\n\nAfter checkpoint: ");
      System.out.println(this.manager.globalState.getStateXML());
      
      // now, get the next bunch:
      rs = this.manager.startTraversal();
      found = false;
      numDocs = 0;
      while((pm = (SPDocument) rs.nextDocument())!=null){
        final Property urlProp = pm.findProperty(SpiConstants.PROPNAME_SEARCHURL);
        final String url = urlProp.nextValue().toString();
        System.out.println(url + " from " 
        		+pm.findProperty(Util.LIST_GUID).nextValue().toString());
        if (url.equals("http://ps4312.persistent.co.in:43386/amitsite/Shared Documents/config.xml")) {
          found = true;
        }
        numDocs++;         
      }
      assertTrue(found);
      assertEquals(numDocs, iDocs); 
    } catch (final RepositoryException e) {
      e.printStackTrace();
      fail("caught exception " + e.toString());
    }
  }
  
}
