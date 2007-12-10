package com.google.enterprise.connector.sharepoint;

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

import com.google.enterprise.connector.sharepoint.client.SPDocument;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;


/**
 * @author amit_kagrawal
 * */
public class SharepointQueryTraversalManagerTest extends TestCase {
/*  final String sharepointUrl = "http://entpoint05.corp.google.com/unittest";
  final String domain = "ent-qa-d3";
  final String host = "entpoint05.corp.google.com";
  final int port = 80;
  final String username = "testing";
  final String password = "g00gl3";*/
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
//	  final String sharepointUrl = "http://ps4312:43386/amitsite";
	  final String sharepointUrl = "http://ps4312.persistent.co.in:43386/amitsite";
	  
	  final String host = "ps4312";
	  final int port = 43386;
	  
	  final String username = "amit_kagrawal";
	  final String password = "Agrawal!@#";
	  final String domain = "persistent";
	  
	/*  final String username = "Administrator";
	  final String password = "pspl!@#";
	  final String domain = "ps4312";
	*/
	  final String mySiteBaseURL= "http://ps4312.persistent.co.in:23508";
	  final String googleConnWorkDir = null;
	  final String exclURLs ="" ;
	  final String inclURLs ="http://ps4312.persistent.co.in:43386,http://ps4312.persistent.co.in:23508,http://ps4312:43386,http://ps4312:23508";

//	-------------END: PS4312---------------------------------

//	-------------japanese(MOSS 2007)---------------------------------
	  
	 /* final String sharepointUrl = "http://v-ecsc6:25000/Japanese";
	  final String domain = "v-ecsc6";
	  final String host = "v-ecsc6";
	  final int port = 25000;
	  final String username = "Administrator";
	  final String password = "pspl!@#";
	*/
//	-------------END: japanese---------------------------------
	  
//	-------------PS2314(WSS 3.0)---------------------------------
	/*  final String sharepointUrl = "http://ps2314:43266/amitsite";
	  final String domain = "ps2314";
	  final String host = "ps2314";
	  final int port = 43266;
	  final String username = "Administrator";
	  final String password = "pspl!@#";
	  final String mySiteBaseURL= "http://ps4312:23508";
	  final String googleConnWorkDir = null;
	  final String exclURLs =null ;
	  final String inclURLs ="http://ps4312:43386/amitsite,http://ps4312:23508,http://ps4312:43386";
	*/
//	-------------END: PS2314---------------------------------
	  
//	-------------v-ecsc3: SSL(ANOTHER MOSS 2007 with SSL)---------------------------------
	  /*final String sharepointUrl = "https://v-ecsc3.persistent.co.in:443/ssl";
	  final String domain = "v-ecsc3";
	  final String host = "v-ecsc3";
	  final int port = 443;//default port is 443 for ssl
	  final String username = "Administrator";
	  final String password = "pspl!@#";
	  //final String mySiteBaseURL= "http://ps4312:23508";
	  final String mySiteBaseURL= null;
	  final String googleConnWorkDir = null;
	  final String exclURLs =null ;
	  final String inclURLs ="https://v-ecsc3.persistent.co.in:443/ssl";*/

//	-------------END: SSL---------------------------------	
	
//  private SharepointClient sharepointClient;
  private SharepointConnector connector;
  private SharepointTraversalManager manager;
  
  public void setUp() throws Exception {
	  
    //forget any global state left over from previous runs
    GlobalState.forgetState(null);
    
  SharepointClientContext sharepointClientContext = new SharepointClientContext(sharepointUrl, domain, username, password, googleConnWorkDir,inclURLs,exclURLs,mySiteBaseURL,null,null);
//  sharepointClient = new SharepointClient(sharepointClientContext);
  connector = new SharepointConnector(sharepointUrl, domain, username, password, googleConnWorkDir,inclURLs,exclURLs,mySiteBaseURL,"","");

  
    /*SharepointClientContext sharepointClientContext = new 
      SharepointClientContext(sharepointUrl, domain, username, password, null);
    sharepointClient = new SharepointClient(sharepointClientContext);
    connector = new SharepointConnector(sharepointUrl, 
        domain, username, password, null);*/
    manager = new SharepointTraversalManager(connector, 
        sharepointClientContext);
    super.setUp();    
  }
  
  /**
   * Basic traversal; no hints, no checkpointing.
   */
  public void testBasic() {
    try {
      DocumentList docList = manager.startTraversal();
      boolean found = false;
      int numDocs = 0;
//      for (Iterator it = rs.iterator(); it.hasNext(); ) {
//      Iterator it = docList.nextDocument()
      SPDocument pm ;
      while((pm = (SPDocument) docList.nextDocument())!=null){
//        SPDocument pm = (SPDocument) it.next();
        Property urlProp = pm.findProperty(SpiConstants.PROPNAME_SEARCHURL);
        String url = urlProp.nextValue().toString();
        if (url.equals("http://ps4312.persistent.co.in:43386/amitsite/Lists/Announcements/DispForm.aspx?ID=1")) {
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
      manager.setBatchHint(3);//set the batch hint
      DocumentList docList = manager.startTraversal();
      boolean found = false;
      int numDocs = 0;
      /*for (Iterator<PropertyMap> it = rs.iterator(); it.hasNext(); ) {
        PropertyMap pm = it.next();*/
      SPDocument pm ;
      while((pm = (SPDocument) docList.nextDocument())!=null){
        Property urlProp = pm.findProperty(SpiConstants.PROPNAME_SEARCHURL);
        String url = urlProp.nextValue().toString();
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
      docList = manager.startTraversal();
      found = false;
      numDocs = 0;
      //SPDocument pm2 ;
      while((pm = (SPDocument) docList.nextDocument())!=null){
//      for (Iterator<PropertyMap> it = docList.iterator(); it.hasNext(); ) {
//        PropertyMap pm = it.next();
        Property urlProp = pm.findProperty(SpiConstants.PROPNAME_SEARCHURL);
        String url = urlProp.nextValue().toString();
        System.out.println(url + " from " 
        		+pm.findProperty(Util.LIST_GUID).nextValue().toString());
        if (url.equals("http://ps4312.persistent.co.in:43386/amitsite/Lists/issue1/Attachments/2/csej.pdf")){
          found = true;
        }
        numDocs++;         
      }
      assertTrue(found);     
      assertEquals(3, numDocs); 
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
	final int iDocs=14;
    System.out.println("\n\ntestHintsWithCheckpoint\n\n");
    try {
      manager.setBatchHint(3);
      DocumentList rs = manager.startTraversal();
      boolean found = false;
      int numDocs = 0;
      
//      PropertyMap pm = null;
//      for (Iterator<PropertyMap> it = rs.iterator(); it.hasNext(); ) {
      SPDocument pm ;
      while((pm = (SPDocument) rs.nextDocument())!=null){
//        pm = it.next();
        Property urlProp = pm.findProperty(SpiConstants.PROPNAME_SEARCHURL);
        String url = urlProp.nextValue().toString();
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
      System.out.println(manager.globalState.getStateXML());
      rs.checkpoint();
//      manager.checkpoint(pm);
      System.out.println("\n\nAfter checkpoint: ");
      System.out.println(manager.globalState.getStateXML());
      
      // now, get the next bunch:
      rs = manager.startTraversal();
      found = false;
      numDocs = 0;
//      for (Iterator<PropertyMap> it = rs.iterator(); it.hasNext(); ) {
      while((pm = (SPDocument) rs.nextDocument())!=null){
//        pm = it.next();
        Property urlProp = pm.findProperty(SpiConstants.PROPNAME_SEARCHURL);
        String url = urlProp.nextValue().toString();
        System.out.println(url + " from " 
        		+pm.findProperty(Util.LIST_GUID).nextValue().toString());
        if (url.equals("http://ps4312.persistent.co.in:43386/amitsite/Shared Documents/config.xml")) {
          found = true;
        }
        numDocs++;         
      }
      assertTrue(found);
      assertEquals(numDocs, iDocs); 
    } catch (RepositoryException e) {
      e.printStackTrace();
      fail("caught exception " + e.toString());
    }
  }
  
}
