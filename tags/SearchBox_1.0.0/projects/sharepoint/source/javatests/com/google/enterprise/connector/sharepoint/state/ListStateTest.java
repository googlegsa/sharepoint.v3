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

package com.google.enterprise.connector.sharepoint.state;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.SiteDataWS;

public class ListStateTest extends TestCase{
	
	ListState testList;
	
	public void setUp() throws Exception {
		System.out.println("\n...Setting Up...");		
		System.out.println("Initializing SharepointClientContext ...");
		final SharepointClientContext sharepointClientContext = new SharepointClientContext(TestConfiguration.sharepointUrl, TestConfiguration.domain, 
				  TestConfiguration.username, TestConfiguration.Password, TestConfiguration.googleConnectorWorkDir, 
				  TestConfiguration.includedURls, TestConfiguration.excludedURls, TestConfiguration.mySiteBaseURL, 
				  TestConfiguration.AliasMap, TestConfiguration.feedType);		

		assertNotNull(sharepointClientContext);
		
		System.out.println("Creating test List ...");
		final SiteDataWS siteDataWS = new SiteDataWS(sharepointClientContext);
		
		final List listCollection = siteDataWS.getNamedLists(new WebState(sharepointClientContext,TestConfiguration.sharepointUrl));
		
		assertNotNull(listCollection);
		for (int i = 0; i < listCollection.size(); i++) {
			final ListState baseList = (ListState) listCollection.get(i);
			if(baseList.getPrimaryKey().equals(TestConfiguration.BaseListID)){
				this.testList = baseList;				
			}
		}
			
		final SPDocument doc1 = new SPDocument("1", "http://www.host.mycomp.com/test1",new GregorianCalendar(2007, 1, 1), SPConstants.NO_AUTHOR, SPConstants.NO_OBJTYPE, SPConstants.PARENT_WEB_TITLE, SPConstants.CONTENT_FEED,SPConstants.SP2007);
		final SPDocument doc2 = new SPDocument("2", "http://www.host.mycomp.com/test2",new GregorianCalendar(2007, 1, 2), SPConstants.NO_AUTHOR, SPConstants.NO_OBJTYPE, SPConstants.PARENT_WEB_TITLE, SPConstants.CONTENT_FEED,SPConstants.SP2007);
		final SPDocument doc3 = new SPDocument("3", "http://www.host.mycomp.com/test3",new GregorianCalendar(2007, 1, 3), SPConstants.NO_AUTHOR, SPConstants.NO_OBJTYPE, SPConstants.PARENT_WEB_TITLE, SPConstants.CONTENT_FEED,SPConstants.SP2007);
		
		//final DateTime time = Util.parseDate("20080702T140516.411+0000");
		
		//testList =new ListState("{001-002-003}", time);
		this.testList =new ListState(SPConstants.SP2007, SPConstants.CONTENT_FEED);
		this.testList.setType(SPConstants.GENERIC_LIST);
		
		final ArrayList<SPDocument> crawlQueueList1 = new ArrayList<SPDocument>();
		crawlQueueList1.add(doc1);
		crawlQueueList1.add(doc2);
		crawlQueueList1.add(doc3);
		
		this.testList.setCrawlQueue(crawlQueueList1);		
		
		this.testList.dumpCrawlQueue();

		System.out.println("Test List being used: "+this.testList.getPrimaryKey());

		System.out.println();
	}
	
	public void testCompareTo() {
		System.out.println("Testing compareTo()...");
		System.out.println("Creating temporary listState to compare");
		//DateTime time = Util.parseDate("20080702T140520.411+0000");
		final ListState lst1 =new ListState(SPConstants.SP2007, SPConstants.CONTENT_FEED);
		lst1.setLastMod(this.testList.getLastMod());
		final int i = this.testList.compareTo(lst1);
		assertEquals(i, 0);
		System.out.println("[ compareTo() ] Test completed.");
	}
	
	public void testGetDateForWSRefresh() {
		System.out.println("Testing getDateForWSRefresh()..");
		final Calendar cal = this.testList.getDateForWSRefresh();
		assertNotNull(cal);
		System.out.println("[ getDateForWSRefresh() ] Test Completed.");
	}
	
	public void testDOCToDOMToDOC() {
		System.out.println("Testing Basic conversion from DOM to DOC and Vice Versa ...");
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		org.w3c.dom.Document doc = null;
		Node node = null;
		try {
			final DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.newDocument();
			node = this.testList.dumpToDOM(doc);
			assertNotNull(node);			
		} catch (final ParserConfigurationException e) {
			System.out.println("Unable to get state XML");
			return;
		} catch (final SharepointException spe) {
			System.out.println("[ dumpToDOM() ] Test Failed.");
			return;
		}
		
		final ListState tempList = new ListState(SPConstants.SP2007, SPConstants.CONTENT_FEED);
		try {
			tempList.loadFromDOM((Element)node);
			final int i = this.testList.compareTo(tempList);
			assertEquals(i, 0);
			System.out.println("[ Basic conversion from DOM to DOC and Vice Versa ] Test Passed.");
		} catch(final SharepointException spe) {
			System.out.println("[ loadFromDOM() ] Test Failed.");
		}		
	}
	
	public void testCachedDeletedID() {
		System.out.println("Testing the caching of deleted IDs ...");
		this.testList.addToDeleteCache("1");
		final boolean bl = this.testList.isInDeleteCache("1");
		assertTrue(bl);
		System.out.println("[ cachedDeletedIDs() ] Test Completed.");
	}
	
	public void testExtraIDs() {
		System.out.println("Testing ExtraIDs handling...");
		final ListState state = new ListState(SPConstants.SP2007, SPConstants.CONTENT_FEED);
		state.setType(SPConstants.DOC_LIB);
		state.setUrl("http://host.mycom.co.in:25000/sanity/Test Library/Forms/AllItems.aspx");
		state.setListConst("sanity/Test Library/");
		state.setIDs(new StringBuffer("#1~Forms1#2#33~Forms2#4#5~Forms3#3/#5/#33/#1"));
		final String docURL = "http://gdc05.persistent.co.in:25000/sanity/Test Library/Forms1/Forms2/Forms3/AllItems.aspx";
		try {
			System.out.println("Current ExtraID value: " + state.getIDs());
			state.updateExtraIDs(docURL, "30", false);
			System.out.println("ExtraID value after updation: "+state.getIDs());
			final Set ids = state.getExtraIDs("33");
			System.out.println("Dependednt IDs: "+ids);
			state.removeExtraID("3");
			System.out.println("Extra IDs after removal: "+state.getIDs());
		} catch(final Exception se) {
			System.out.println("Test failed with following error:\n"+se);
		}		
	}	
	
	public void testUpdateExtraIDAsAttachment() {
		System.out.println("Testing the attachment count handling ...");
		this.testList.updateExtraIDAsAttachment("1","http://host.mycom.co.in:25000/sanity/Test Library/Forms/AllItems.aspx");
		this.testList.updateExtraIDAsAttachment("1","http://host.mycom.co.in:25000/sanity/Test Library2/Forms/AllItems.aspx");
		final List attchmnts = this.testList.getAttachmntURLsFor("1");
		System.out.println(attchmnts);
		this.testList.removeAttachmntURLFor("1","http://host.mycom.co.in:25000/sanity/Test Library/Forms/AllItems.aspx");
		assertNotNull(attchmnts);
		
		final Pattern pat = Pattern.compile("$");
		final Matcher match = pat.matcher("$");
		System.out.println(match.find());
		System.out.println("[ updateExtraIDAsAttachment() ] Test Completed.");
	}
}
