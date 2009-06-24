package com.google.enterprise.connector.sharepoint.client;

import java.util.ArrayList;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.SharepointConnector;
import com.google.enterprise.connector.sharepoint.SharepointConnectorType;

/**
 * Test the functionaltily of alerts web service.
 * @author amit_kagrawal
 * */
public class AlertsWSTest extends TestCase{
	Logger logger= Logger.getLogger(AlertsWSTest.class.getName());
	
	//set the logging properties file
	static{
		System.setProperty("java.util.logging.config.file","logging.properties");
	}
	
	//set the connector configuration information
//	-------------gsp02ps5265(sps 2007)---------------------------------
	  /*final String sharepointType = SharepointConnectorType.SP2007;
	  final String sharepointUrl = "http://gsp02ps5265.persistent.co.in:36022/";
	  final String domain = "gsp02ps5265";
	  final String username = "administrator";
	  final String password = "pspl!@#";

	  final String mySiteBaseURL=null;
	  final String googleConnWorkDir = null;

	  final String exclURLs =null;
	  final String aliasHost = null;
	  final String aliasPort = null;
	  final String inclURLs ="^http://";*/
//	-------------END: gsp02ps5265---------------------------------
	
	  
//		-------------ps4312(sps 2007)---------------------------------
	  final String sharepointType = SharepointConnectorType.SP2007;
	  final String sharepointUrl = "http://ps4312.persistent.co.in:2905/Orangesite/abc/";
	  final String domain = "ps4312";
	  final String username = "administrator";
	  final String password = "pspl!@#";

	  final String mySiteBaseURL=null;
	  final String googleConnWorkDir = null;

	  final String exclURLs =null;
	  final String aliasHost = null;
	  final String aliasPort = null;
	  final String inclURLs ="^http://";
//	-------------END: gsp02ps5265---------------------------------
	  
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
	  public void setUp() throws Exception {
		logger.config("Inside Setup...");
		sharepointClientContext = new SharepointClientContext(sharepointType,sharepointUrl, domain, username, password, googleConnWorkDir,inclURLs,exclURLs,mySiteBaseURL,null,null,WHITE_LIST,BLACK_LIST);
	    connector = new SharepointConnector(sharepointUrl, domain, username, password, googleConnWorkDir,inclURLs,exclURLs,mySiteBaseURL,aliasHost,aliasPort,sharepointType);
	    connector.setWhiteList(WHITE_LIST);
	    connector.setBlackList(BLACK_LIST);
	    connector.setFQDNConversion(true);//do the FQDN conversion for non-FQDN URLs 
	    super.setUp();    
	  }	  
	  
	  
	  //test cases
	  public final void testAlerts() throws Throwable{
		  AlertsWS alertWS = new AlertsWS(sharepointClientContext);
		  ArrayList lstAlerts = (ArrayList) alertWS.getAlerts("dfs");
		  
		  if(null!=lstAlerts){
			  for(int i=0;i<lstAlerts.size();++i){
				  System.out.println("Alert: "+lstAlerts.get(i));
			  }
		  }
	  }
	  
}//end:: class
