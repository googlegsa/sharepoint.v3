//Copyright 2007 Google Inc.  All Rights Reserved.
package com.google.enterprise.connector.sharepoint.client;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.state.WebState;

/**
 *@author amit_kagrawal
 *This class contains test methods to check the functionality of {@link SiteDataWS}.
 */
public class SiteDataWSTest extends TestCase {  
	Logger logger= Logger.getLogger(AlertsWSTest.class.getName());

	//set the logging properties file
	static{
		System.setProperty("java.util.logging.config.file","logging.properties");
	}

	final String sharepointType = SharepointConnectorType.SP2007;
	final String sharepointUrl = "http://";
	final String domain = "domain";
	final String username = "username";
	final String password = "password";

	final String mySiteBaseURL=null;
	final String googleConnWorkDir = null;

	final String exclURLs ="http://";
	final String aliasHost = null;
	final String aliasPort = null;
	final String inclURLs ="http://";

	//set the balck list and whitelist
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

	// private SharepointConnector connector;  
	//public static final int TOTAL_DOCS = 185;//set the total expected documents
	SharepointClientContext sharepointClientContext =null;

	private SiteDataWS siteDataWS;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		sharepointClientContext = new SharepointClientContext(sharepointType,sharepointUrl, domain, username, password, googleConnWorkDir,inclURLs,exclURLs,mySiteBaseURL,null,null,WHITE_LIST,BLACK_LIST);
		siteDataWS = new SiteDataWS(sharepointClientContext);   
		super.setUp();
	}

	/**
	 * Test the linked sites
	 * */
	public void testLinks(){
		String site = sharepointUrl;
		SortedSet linkedWebs = null; 
		try {
			linkedWebs =  siteDataWS.getAllLinks(sharepointClientContext, site,"");
		} catch (SharepointException e) {
			e.printStackTrace();
			fail();
		}

		assertNotNull(linkedWebs);
		System.out.println("Links Returned - "+linkedWebs.size());

		printList(linkedWebs);
	}
	private void printList(SortedSet personalSitesSet){
		List personalSites = new ArrayList();
		personalSites.addAll(personalSitesSet);

		for(int iList=0;iList<personalSites.size();++iList){
			WebState ws = (WebState) personalSites.get(iList);
			assertNotNull(ws);
			System.out.println("URL: "+ws.getWebUrl()+"|Title: "+ws.getTitle());
		}//for(int iList=0;iList<personalSites.size();++iList){
	}

	/**
	 * Test method for {@link 
	 * com.google.enterprise.connector.sharepoint.client.SiteDataWS
	 * #getDocumentLibraries()}.
	 */
	public void testGetDocumentLibraries() {
		int numDocLib = 0;
		try {
			List listCollection = siteDataWS.getDocumentLibraries("");
			System.out.println("SPDocument Libraries found - ");
			for (int i = 0; i < listCollection.size(); i++) {
				BaseList baseList = (BaseList) listCollection.get(i);
				System.out.println(baseList.getTitle());    
				System.out.println(baseList.getType());
				System.out.println(baseList.getInternalName());    
				System.out.println(baseList.getLastMod());
				numDocLib++;        
			}
			//assertEquals(2, numDocLib);//test the no: of document libraries returned
		} catch (SharepointException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test method for {@link 
	 * com.google.enterprise.connector.sharepoint.client.SiteDataWS
	 * #getGenericLists()}.
	 */
	public void testGetGenericLists() {
		int numGenList = 0;
		try {
			List listCollection = siteDataWS.getGenericLists("");
			System.out.println("Generic Lists found - ");
			//for (int i = 0; i < listCollection.size(); i++) {
			for (int i = 0; i < 1; i++) {
				BaseList baseList = (BaseList) listCollection.get(i);
				System.out.println(baseList.getTitle());        
				numGenList++;        
			}
			//assertEquals(7, numGenList);//count no: of generic lists
		} catch (SharepointException e) {      
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test method for {@link 
	 * com.google.enterprise.connector.sharepoint.client.SiteDataWS
	 * #getIssues()}.
	 */
	public void testIssues() {
		int numIssues = 0;
		try {
			List listCollection = siteDataWS.getIssues("");
			System.out.println("Issues found - ");
			for (int i = 0; i < listCollection.size(); i++) {
				BaseList baseList = (BaseList) listCollection.get(i);
				System.out.println(baseList.getTitle());         
				numIssues++;        
			}
			//assertEquals(1, numIssues);//to check the no: of issues returned
		} catch (SharepointException e) {      
			e.printStackTrace();
			fail();
		}
	}
}
