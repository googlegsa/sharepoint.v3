package com.google.enterprise.connector.sharepoint.client.sp2003;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.SharepointConnector;
import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.UserProfileWS;
import com.google.enterprise.connector.sharepoint.state.WebState;

/**
 * Test the functionaltily of UserProfile web service (SP2003).
 * @author amit_kagrawal
 * */
public class UserProfileWSTest extends TestCase{
	//set the balck list and whitelist
	private static ArrayList BLACK_LIST;
	private static ArrayList WHITE_LIST;

	static{
		System.setProperty("java.util.logging.config.file","logging.properties");//set the logging properties file
	}
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
	static {
		WHITE_LIST = new ArrayList();
		WHITE_LIST.add(".*vti_title$");
		WHITE_LIST.add(".*vti_author$");
	}
	Logger logger= Logger.getLogger(UserProfileWSTest.class.getName());
	final String sharepointType = SharepointConnectorType.SP2003;
	final String sharepointUrl ="http://";//sps site
	
	final String domain = "domain";
	final String username = "username";
	final String password = "password";
	final String mySiteBaseURL=null;
	final String googleConnWorkDir = null;
	final String exclURLs =null;
	final String aliasHost = null;
	final String aliasPort = null;
	final String inclURLs ="^http";

	private SharepointConnector connector;  
	SharepointClientContext sharepointClientContext =null;
	com.google.enterprise.connector.sharepoint.client.sp2003.UserProfileWS stubSP2003= null;

	private void printList(SortedSet personalSitesSet){
		List personalSites = new ArrayList();
		personalSites.addAll(personalSitesSet);

		for(int iList=0;iList<personalSites.size();++iList){
			WebState ws = (WebState) personalSites.get(iList);
			assertNotNull(ws);
			System.out.println("URL: "+ws.getWebUrl()+"|Title: "+ws.getTitle());
		}//for(int iList=0;iList<personalSites.size();++iList){
	}

	protected void setUp() throws Exception {
		logger.config("Inside Setup...");

		sharepointClientContext = new SharepointClientContext(sharepointType,sharepointUrl, domain, username, password, googleConnWorkDir,inclURLs,exclURLs,mySiteBaseURL,null,null,WHITE_LIST,BLACK_LIST);
		connector = new SharepointConnector(sharepointUrl, domain, username, password, googleConnWorkDir,inclURLs,exclURLs,mySiteBaseURL,aliasHost,aliasPort,sharepointType);
		connector.setWhiteList(WHITE_LIST);
		connector.setBlackList(BLACK_LIST);
		connector.setFQDNConversion(true);//do the FQDN conversion for non-FQDN URLs 
		super.setUp();
	}

	public void testPersonalSites() throws Throwable{
		String strSharepointType = sharepointClientContext.getSharePointType();//get the version of sharepoint
		assertNotNull(strSharepointType);//check if sp type is not null
		assertEquals(strSharepointType,SharepointConnectorType.SP2003);//type should be sp2007

		UserProfileWS userProfileWS = new UserProfileWS(sharepointClientContext);
		//assertTrue(userProfileWS.isSPS()); //should be portal site

		SortedSet personalSites = userProfileWS.getPersonalSiteList();
		System.out.println("Personal sites...");
		printList(personalSites);

		personalSites =(TreeSet) userProfileWS.getMyLinks();
		printList(personalSites);
	}
}
