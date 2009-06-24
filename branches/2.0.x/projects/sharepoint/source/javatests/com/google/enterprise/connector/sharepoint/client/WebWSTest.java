package com.google.enterprise.connector.sharepoint.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.SharepointConnector;
import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.state.WebState;

/**
 * Test the functionality of webs web service.
 * @author amit_kagrawal
 * */
public class WebWSTest extends TestCase{
	Logger logger= Logger.getLogger(WebWSTest.class.getName());
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

	final String exclURLs =null;
	final String aliasHost = null;
	final String aliasPort = null;
	final String inclURLs ="^http://";

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

	private SharepointConnector connector;  
	//public static final int TOTAL_DOCS = 185;//set the total expected documents
	SharepointClientContext sharepointClientContext =null;
	WebsWS stub= null;
	
	protected void setUp() throws Exception {
		logger.config("Inside Setup...");

		sharepointClientContext = new SharepointClientContext(sharepointType,sharepointUrl, domain, username, password, googleConnWorkDir,inclURLs,exclURLs,mySiteBaseURL,null,null,WHITE_LIST,BLACK_LIST);
	    connector = new SharepointConnector(sharepointUrl, domain, username, password, googleConnWorkDir,inclURLs,exclURLs,mySiteBaseURL,aliasHost,aliasPort,sharepointType);
	    connector.setWhiteList(WHITE_LIST);
	    connector.setBlackList(BLACK_LIST);
	    connector.setFQDNConversion(true);//do the FQDN conversion for non-FQDN URLs 
		super.setUp();
	}
	
	//testCases
	public void testChildWebs() throws SharepointException{
		String strURL = "http://";
		stub = new WebsWS(sharepointClientContext,strURL);
		Set tsChildWebs = stub.getDirectChildsites();
		
		Iterator itChild = tsChildWebs.iterator();
		if(itChild!=null){
			while(itChild.hasNext()){
				WebState ws =(WebState)itChild.next();
				System.out.println("Child: "+ws.getWebUrl());
			}
		}
		
	}
	
	public void testGetWebURLFromPageURL() throws Throwable{
		stub = new WebsWS(sharepointClientContext);
		String strPageURL ="http://";
		WebState webState = stub.getWebURLFromPageURL(strPageURL);
		String actual = webState.getWebUrl();
		String expected = "http://";
		
		System.out.println("WebUrl: "+webState.getWebUrl());
		System.out.println("InsertionTimeString: "+webState.getInsertionTimeString());
		System.out.println("Title: "+webState.getTitle());
		
		assertNotNull(webState);
		assertEquals(actual, expected);
		
	}
	
	public void testGetWebTitle() throws Throwable{
		stub = new WebsWS(sharepointClientContext);
		String title =stub.getTitle(sharepointUrl);
		assertNotNull(title);
		assertNotSame(title, "No Title");
		System.out.println("Title: "+title);
		
	}
}
