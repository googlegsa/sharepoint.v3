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

import java.net.MalformedURLException;
import java.util.Set;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * Test the functionaltily of UserProfile web service (SP2007).
 * @author amit_kagrawal
 * */
public class UserProfileWSTest extends TestCase{
	SharepointClientContext sharepointClientContext;
	UserProfileWS userProfileWS;
			
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
		
		System.out.println("Initializing UserProfileWS ...");
		this.userProfileWS = new UserProfileWS(this.sharepointClientContext);		
	}
	
	public void testIsSPS() throws MalformedURLException, RepositoryException {
		System.out.println("Testing isSPS()...");		
		this.userProfileWS.isSPS();
		System.out.println("[ isSPS() ] Test Completed.");		
	}
	
	public void testGetPersonalSiteList() throws MalformedURLException, RepositoryException {
		System.out.println("Testing getPersonalSiteList()...");		
		final Set items = this.userProfileWS.getPersonalSiteList();
		assertNotNull(items);
		System.out.println("[ getPersonalSiteList() ] Test Passed.");		
	}

	public void testGetMyLinks() throws MalformedURLException, RepositoryException {
		System.out.println("Testing getMyLinks()...");		
		final Set items = this.userProfileWS.getMyLinks();
		assertNotNull(items);
		System.out.println("[ getMyLinks() ] Test Passed.");		
	}
}
