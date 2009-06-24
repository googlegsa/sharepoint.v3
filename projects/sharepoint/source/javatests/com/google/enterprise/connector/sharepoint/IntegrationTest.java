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

import com.google.enterprise.connector.instantiator.MockInstantiator;
import com.google.enterprise.connector.persist.ConnectorStateStore;
import com.google.enterprise.connector.persist.MockConnectorStateStore;
import com.google.enterprise.connector.persist.StoreContext;
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

	final String sharepointType = SharepointConnectorType.SP2003;
	final String sharepointUrl = "http://";
	final String domain = "domain";
	final String username = "administrator";
	final String password = "password";
	final String mySiteBaseURL=null;
	final String googleConnWorkDir = null;
	final String exclURLs ="";
	final String aliasHost = null;
	final String aliasPort = null;
	final String inclURLs ="regexp:.*";

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

	private SharepointConnector connector;  
	public static final int TOTAL_DOCS = 185;//set the total expected documents
	public void setUp() throws Exception {
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
		MockInstantiator instantiator = new MockInstantiator();
		Traverser traverser =new QueryTraverser(pusher, manager, instantiator.getTraversalStateStore(connectorName), connectorName);
		System.out.println("\nRunning batch test batchsize " + batchSize);

		int docsProcessed = -1;
		int totalDocsProcessed = 0;
		int batchNumber = 0;
		while (true) {
			docsProcessed = traverser.runBatch(batchSize);//do the traversal
			totalDocsProcessed += docsProcessed;//do the checkpointing after the traversal
			System.out.println("Batch# " + batchNumber + " docs " + docsProcessed 
					+" checkpoint " + connectorStateStore.getConnectorState(new StoreContext(connectorName)));
			batchNumber++;

			//start recrawl cycle
			if(docsProcessed==0){
				System.out.println("No new documents discovered");
			}

		}    
//		Assert.assertEquals(TOTAL_DOCS,totalDocsProcessed);
	}
}
