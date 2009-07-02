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

package com.google.enterprise.connector.sharepoint.wsclient;

import java.util.Set;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;

/**
 * Test the functionality of webs web service.
 * @author amit_kagrawal
 * */
public class WebWSTest extends TestCase{
	SharepointClientContext sharepointClientContext;
	WebsWS websWS;	
	
	protected void setUp() throws Exception {
		System.out.println("\n...Setting Up...");
		System.out.println("Initializing SharepointClientContext ...");
		this.sharepointClientContext = new SharepointClientContext(TestConfiguration.sharepointUrl, TestConfiguration.domain, 
				  TestConfiguration.username, TestConfiguration.Password, TestConfiguration.googleConnectorWorkDir, 
				  TestConfiguration.includedURls, TestConfiguration.excludedURls, TestConfiguration.mySiteBaseURL, 
				  TestConfiguration.AliasMap, TestConfiguration.feedType);		
		assertNotNull(this.sharepointClientContext);
		sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
        sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);		
		
		System.out.println("Initializing WebsWS ...");
		this.websWS = new WebsWS(this.sharepointClientContext);
	}
	
	public final void testWebsWS() throws Throwable {
		System.out.println("Testing WebsWS(SharepointClientContext, siteName)...");
		sharepointClientContext.setSiteURL(TestConfiguration.ParentWebURL);
		this.websWS = new WebsWS(this.sharepointClientContext);
		assertNotNull(this.websWS);
		System.out.println("[ WebsWS(SharepointClientContext, siteName) ] Test Passed");
	}
	
	public final void testGetDirectChildsites() throws Throwable {
		System.out.println("Testing getDirectChildsites()...");
		final Set sites = this.websWS.getDirectChildsites();
		assertNotNull(sites);
		System.out.println("[ getDirectChildsites() ] Test Passed");
	}
		
	public final void testGetWebURLFromPageURL() throws Throwable {
		System.out.println("Testing getWebURLFromPageURL()...");
		final String siteURL = this.websWS.getWebURLFromPageURL(TestConfiguration.DocID1);
		assertNotNull(siteURL);
		System.out.println("[ getWebURLFromPageURL() ] Test Passed");
	}
	
	public final void testGetWebTitle() throws Throwable {
		System.out.println("Testing getWebTitle()...");
		final String siteURL = this.websWS.getWebTitle(TestConfiguration.sharepointUrl,SPConstants.SP2007);
		assertNotNull(siteURL);
		System.out.println("[ getWebTitle() ] Test Passed");
	}
	
	public final void testCheckConnectivity() throws Throwable {
		System.out.println("Testing checkConnectivity()...");
		this.websWS.checkConnectivity();
		System.out.println("[ checkConnectivity() ] Test Completed.");
	}
}
