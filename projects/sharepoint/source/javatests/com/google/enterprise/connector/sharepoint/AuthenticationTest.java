package com.google.enterprise.connector.sharepoint;

import java.util.ArrayList;
import java.util.logging.Logger;
import junit.framework.TestCase;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.WebsWS;
import com.google.enterprise.connector.sharepoint.client.sp2003.UserProfileWSTest;

public class AuthenticationTest extends TestCase{
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
	final String sharepointUrl = "http://";
	
	final String domain = null;//blank domain-for basic
	
	
	final String username = "administrator";
	final String password = "<password>";
	final String mySiteBaseURL=null;
	final String googleConnWorkDir = null;
	final String exclURLs =null;
	final String aliasHost = null;
	final String aliasPort = null;
	final String inclURLs ="^http";

	private SharepointConnector connector;  
	SharepointClientContext sharepointClientContext =null;
	com.google.enterprise.connector.sharepoint.client.sp2003.UserProfileWS stubSP2003= null;



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
		WebsWS webws = new WebsWS(sharepointClientContext);
		assertNotNull(webws);
		
		webws.getDirectChildsites();
	}
}
