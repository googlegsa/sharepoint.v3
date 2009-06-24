//Copyright (C) 2006 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint;

import java.util.ArrayList;

import javax.security.auth.login.LoginException;

import junit.framework.TestCase;

import com.google.enterprise.connector.persist.ConnectorStateStore;
import com.google.enterprise.connector.persist.MockConnectorStateStore;
import com.google.enterprise.connector.pusher.MockPusher;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.traversal.QueryTraverser;
import com.google.enterprise.connector.traversal.Traverser;

/**
 * @author amit_kagrawal
 */
public class IntegrationTest extends TestCase {
	static{
		System.setProperty("java.util.logging.config.file","logging.properties");//set an external configuration file for controlling logging
	}

//	-------------v07ps45372k3(sps 2003)---------------------------------
	/*final String sharepointType = SharepointConnectorType.SP2003;
  final String sharepointUrl = "http://v07ps45372k3.persistent.co.in:8081";
  final String domain = "v07ps45372k3";
  final String host = "v07ps45372k3";
  final int port = 8081;
  final String username = "Administrator";
  final String password = "Pspl!@#";
  final String mySiteBaseURL=null;
  final String googleConnWorkDir = null;
  final String exclURLs =null ;
  final String aliasHost = null;
  final String aliasPort = null;
  final String inclURLs ="^http";*/
//	-------------END: v07ps45372k3---------------------------------
//	-------------v-ecsc5(sps 2003)---------------------------------
	/* final String sharepointType = SharepointConnectorType.SP2007;
//  final String sharepointUrl = "http://gsp01ps5265.persistent.co.in:5001/C3/Services/";
//  final String sharepointUrl = "http://ps4312.persistent.co.in:2905/xyz/subxyz/";
//  final String sharepointUrl = "http://ps4312.persistent.co.in:2905/Orangesite/abc/";
  final String sharepointUrl = "http://gsp02ps5265.persistent.co.in:36022/VolTestLibs1/";
  final String domain = "persistent";
  final String username = "googlesp";
  final String password = "Passw0rd@456"; 
// final String mySiteBaseURL="http://ps4312.persistent.co.in:30411";
  final String domain = "gsp02ps5265";
  final String username = "administrator";
  final String password = "pspl!@#";

  final String mySiteBaseURL=null;
  final String googleConnWorkDir = null;
  //final String exclURLs ="http://gsp02ps5265.persistent.co.in:36022/SubSiteLevel/ http://gsp02ps5265.persistent.co.in:36022/SiteTest http://gsp02ps5265.persistent.co.in:36022/OneMoreSite";
  final String exclURLs =null;
  final String aliasHost = null;
  final String aliasPort = null;
  final String inclURLs ="^http://";*/

//	-------------END: v07ps45372k3---------------------------------
	/* final String sharepointType = SharepointConnectorType.SP2007;
//  final String sharepointUrl = "https://ps4312.persistent.co.in/";
//  final String sharepointUrl = "https://ps4312.persistent.co.in/Test/BlankSite/";
  final String sharepointUrl = "http://ps4312.persistent.co.in:2905/xyz/subxyz/";

  final String domain = "ps4312";
  final String username = "administrator";
  final String password = "pspl!@#";

//  final String mySiteBaseURL="http://ps4312.persistent.co.in:30411";
  final String mySiteBaseURL=null;
  final String googleConnWorkDir = null;
  final String exclURLs ="";
//  final String exclURLs ="https://ps4312.persistent.co.in/Test/ https://ps4312.persistent.co.in:443/Test/";
  final String aliasHost = null;
  final String aliasPort = null;
//  final String inclURLs ="http://gsp02ps5265.persistent.co.in:36022/VolTestLibs1/Sub4/";
  final String inclURLs ="regexp:.*";*/
//	final String inclURLs ="http://gsp02ps5265.persistent.co.in:36022/VolTestLibs1/Sub1/";

	//-------------------sps2003 =gsp01ps5265

	final String sharepointType = SharepointConnectorType.SP2003;
	final String sharepointUrl = "http://gsp01ps5265.persistent.co.in:6500/voltest/";
	final String domain = "gsp01ps5265";
	final String username = "administrator";
	final String password = "pspl!@#";
	final String mySiteBaseURL=null;
	final String googleConnWorkDir = null;
	final String exclURLs ="";
	final String aliasHost = null;
	final String aliasPort = null;
//	final String inclURLs ="regexp:.*";
	final String inclURLs ="http://gsp01ps5265.persistent.co.in:6500/voltest/";
	//end: ---------------------sps2003 =gsp01ps5265
	
//	-------------PS4312(MOSS 2007)---------------------------------
	/*final String sharepointType = SharepointConnectorType.SP2007;
//  final String sharepointUrl = "http://ps4312.persistent.co.in:30411/personal/ps4312_administrator";
//  final String sharepointUrl = "http://ps4312.persistent.co.in:43386/test/";
//  final String sharepointUrl = "http://ps4312.persistent.co.in:43386/am1";
  final String sharepointUrl = "https://ps4312.persistent.co.in:443";

//  final String sharepointUrl = "http://ps4312.persistent.co.in:23750/sites/abc/";
  final String domain = "ps4312";
//  final String domain = "ps4312.persistent.co.in";
  final String host = "ps4312";
  final int port = 443;
  final String username = "Administrator";
  final String password = "pspl!@#";
  final String mySiteBaseURL= "http://ps4312.persistent.co.in:30411";//"http://ps4312:23508"
  final String googleConnWorkDir = null;
  final String exclURLs ="http://ps4312.persistent.co.in:43386" ;
  final String aliasHost = null;
  final String aliasPort = null;

//  final String inclURLs ="http://ps4312:43386,http://ps4312:23508";
//  final String inclURLs ="http://ps4312.persistent.co.in:43386,http://ps4312.persistent.co.in:30411";
//  final String inclURLs ="http://ps4312.persistent.co.in:30411/personal/ps4312_administrator";
  final String inclURLs =".*";*/
//	final String inclURLs ="http://ps4312.persistent.co.in:30411/personal/ps4312_administrator";
//	final String inclURLs ="http://ps4312.persistent.co.in:43386/,http://ps4312:43386";

//	-------------END: PS4312---------------------------------
//	-------------PS4312(MOSS 2007)---------------------------------
//	final String sharepointUrl = "http://ps4312:43386/amitsite";
	/*final String sharepointUrl = "http://ps4312.persistent.co.in:43386/test";
//  final String sharepointUrl = "http://ps4312.persistent.co.in:43386/amitsite";

  final String host = "ps4312";
  final int port = 43386;

  final String username = "amit_kagrawal";
  final String password = "Agrawal!@#";
  final String domain = "persistent";

  final String username = "Administrator";
  final String password = "pspl!@#";
  final String domain = "ps4312";

//  final String mySiteBaseURL= "http://ps4312.persistent.co.in:23508";
  final String mySiteBaseURL= null;
  final String googleConnWorkDir = null;
  final String exclURLs =null ;
//  final String inclURLs ="http://ps4312.persistent.co.in:43386,http://ps4312.persistent.co.in:23508,http://ps4312:43386,http://ps4312:23508";
  final String inclURLs ="http://ps4312.persistent.co.in:43386/test";

  final String aliasHost = "59.163.69.23";
  final String aliasPort = "5082";
  final String aliasHost = null;
  final String aliasPort = null;
//  final String inclURLs ="http://ps4312.persistent.co.in:43386/amitsite";
	 */
//	-------------END: PS4312---------------------------------

//	-------------japanese(MOSS 2007)---------------------------------

//	final String sharepointUrl = "http://v-ecsc6:25000/Japanese/?????";
//	String sharepointUrl =null ;
//	final String sharepointUrl = "http://v-ecsc6.persistent.co.in:25000/\u0082\u00BB\u0082\u00CC\u0082\u00D9\u0082\u00A9\u0082\u00CD";

//	http\://v-ecsc6.persistent.co.in\:25000/\u0082\u00BB\u0082\u00CC\u0082\u00D9\u0082\u00A9\u0082\u00CD
	/*final String sharepointUrl = "http://v-ecsc6.persistent.co.in:25000/%e3%81%9d%e3%81%ae%e3%81%bb%e3%81%8b%e3%81%af";
  final String domain = "v-ecsc6";
  final String host = "v-ecsc6";
  final int port = 25000;
  final String username = "Administrator";
  final String password = "pspl!@#";
  final String aliasHost = null;
  final String aliasPort = null;
  final String mySiteBaseURL= null;
  final String googleConnWorkDir = null;
  final String exclURLs =null ;
  final String inclURLs ="http://v-ecsc6:25000/Japanese";*/
//	final String inclURLs ="http://v-ecsc6.persistent.co.in:25000/%e3%81%9d%e3%81%ae%e3%81%bb%e3%81%8b%e3%81%af";
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

//	private SharepointClient sharepointClient;
	private SharepointConnector connector;  

	public static final int TOTAL_DOCS = 185;//set the total expected documents

	public void setUp() throws Exception {
//		SharepointClientContext sharepointClientContext = new SharepointClientContext(sharepointUrl, domain, username, password, googleConnWorkDir,inclURLs,exclURLs,mySiteBaseURL,null,null);
//		sharepointClient = new SharepointClient(sharepointClientContext);
		/* Properties p = new Properties();
	    p.load(new FileInputStream("source/javatests/amit.properties"));
	    sharepointUrl = p.getProperty("url");*/

		connector = new SharepointConnector(sharepointUrl, domain, username, password, googleConnWorkDir,inclURLs,exclURLs,mySiteBaseURL,aliasHost,aliasPort,sharepointType);
		connector.setWhiteList(WHITE_LIST);
		connector.setBlackList(BLACK_LIST);
		connector.setFQDNConversion(true);
		super.setUp();    
	}
	/**
	 * Test method for
	 * {@link com.google.enterprise.connector.traversal.QueryTraverser
	 * #runBatch(int)}.
	 * @throws InterruptedException 
	 * @throws RepositoryException 
	 * @throws LoginException 
	 */
	public final void testRunBatch() throws InterruptedException,RepositoryException {
		final int iBatch =100; 
		runTestBatches(iBatch);
	}

	private void runTestBatches(int batchSize) throws InterruptedException,RepositoryException {
		String connectorName = "sharepoint";
		Session session = connector.login();
		GlobalState.forgetState(null); //used to delete the connector state file.. testing purpose
		SharepointTraversalManager manager = 
			(SharepointTraversalManager) session.getTraversalManager(); 
		MockPusher pusher = new MockPusher(System.out);
		ConnectorStateStore connectorStateStore = new MockConnectorStateStore();

		Traverser traverser =new QueryTraverser(pusher, manager, connectorStateStore, connectorName);
		System.out.println("\nRunning batch test batchsize " + batchSize);

		int docsProcessed = -1;
		int totalDocsProcessed = 0;
		int batchNumber = 0;
//		while (docsProcessed != 0) {
		while (true) {
			docsProcessed = traverser.runBatch(batchSize);//do the traversal
			totalDocsProcessed += docsProcessed;//do the checkpointing after the traversal
			System.out.println("Batch# " + batchNumber + " docs " + docsProcessed 
					+" checkpoint " + connectorStateStore.getConnectorState(connectorName));
			batchNumber++;

			//start recrawl cycle
			if(docsProcessed==0){
				System.out.println("No new documents discovered");
			}

		}    
//		Assert.assertEquals(TOTAL_DOCS,totalDocsProcessed);
	}
}
