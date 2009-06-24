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

import java.util.ArrayList;

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
	final String SPType = SharepointConnectorType.SP2007;
	  final String sharepointUrl = "http://";
	  
	  final String host = "host";
	  final int port = 43386;
	  
	  final String username = "useraname";
	  final String password = "password";
	  final String domain = "domain";
	  
	  final String mySiteBaseURL= "http://";
	  final String googleConnWorkDir = null;
	  final String exclURLs ="" ;
	  final String inclURLs ="http://";

  private SharepointConnector connector;
  private SharepointTraversalManager manager;
  private static ArrayList BLACK_LIST;
  static {
		BLACK_LIST = new ArrayList();
		BLACK_LIST.add(".*vti_cachedcustomprops$");
		BLACK_LIST.add(".*vti_parserversion$");
		BLACK_LIST.add(".*ContentType$");
		BLACK_LIST.add(".*vti_cachedtitle$");
		BLACK_LIST.add(".*ContentTypeId$");
		BLACK_LIST.add(".*DocIcon$");
		BLACK_LIST.add(".*vti_cachedhastheme$");
		BLACK_LIST.add(".*vti_metatags$");
		BLACK_LIST.add(".*vti_charset$");
		BLACK_LIST.add(".*vti_cachedbodystyle$");
		BLACK_LIST.add(".*vti_cachedneedsrewrite$");
	}
  
  private static ArrayList WHITE_LIST;
	static {
		WHITE_LIST = new ArrayList();
		WHITE_LIST.add(".*vti_title$");
		WHITE_LIST.add(".*vti_author$");
	}
  
  public void setUp() throws Exception {
	  
    //forget any global state left over from previous runs
    GlobalState.forgetState(null);
    
  SharepointClientContext sharepointClientContext = new SharepointClientContext(sharepointUrl, domain, username, password, googleConnWorkDir,inclURLs,exclURLs,mySiteBaseURL,null,null,SPType,WHITE_LIST,BLACK_LIST);
  connector = new SharepointConnector(sharepointUrl, domain, username, password, googleConnWorkDir,inclURLs,exclURLs,mySiteBaseURL,"","",SPType);
  connector.setWhiteList(WHITE_LIST);
  connector.setBlackList(BLACK_LIST);
  
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
      SPDocument pm ;
      while((pm = (SPDocument) docList.nextDocument())!=null){
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
      
      SPDocument pm ;
      while((pm = (SPDocument) rs.nextDocument())!=null){
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
      System.out.println("\n\nAfter checkpoint: ");
      System.out.println(manager.globalState.getStateXML());
      
      // now, get the next bunch:
      rs = manager.startTraversal();
      found = false;
      numDocs = 0;
      while((pm = (SPDocument) rs.nextDocument())!=null){
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
