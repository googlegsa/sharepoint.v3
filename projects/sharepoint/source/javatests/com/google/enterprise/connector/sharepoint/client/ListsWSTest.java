package com.google.enterprise.connector.sharepoint.client;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.SharepointConnector;
import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * Test cases for {@link ListsWS}.
 * @author amit_kagrawal
 * */
public class ListsWSTest extends TestCase {
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
	private Map listInternalNames = new HashMap();       
	private ListsWS listsWS;
	private SiteDataWS sitedataWS;
	protected void setUp() throws Exception {
		logger.config("Inside Setup...");

		sharepointClientContext = new SharepointClientContext(sharepointType,sharepointUrl, domain, username, password, googleConnWorkDir,inclURLs,exclURLs,mySiteBaseURL,null,null,WHITE_LIST,BLACK_LIST);
		connector = new SharepointConnector(sharepointUrl, domain, username, password, googleConnWorkDir,inclURLs,exclURLs,mySiteBaseURL,aliasHost,aliasPort,sharepointType);
		connector.setWhiteList(WHITE_LIST);
		connector.setBlackList(BLACK_LIST);
		connector.setFQDNConversion(true);//do the FQDN conversion for non-FQDN URLs 

		

		listInternalNames.put("{34EA4B8C-3663-41C7-A0EA-13E42D44D404}","Calendar");//
		listInternalNames.put("{25C1E978-FA4B-4076-BA16-B125DEEA26C2}","Tasks");//
		listInternalNames.put("{5D1BB107-7C38-446A-B5F6-FF94624390E9}","Announcements");//
		listInternalNames.put("{6858021D-005E-4AEC-8AD4-D23B12D0170D}","Links");//
		
		
		sitedataWS = new SiteDataWS(sharepointClientContext);
		super.setUp();
	}
	/**
	 * Test method for {@link 
	 * com.google.enterprise.connector.sharepoint.client.SiteDataWS
	 * #getDocumentLibraries()}.
	 */
	private BaseList getBaseList() {
		String baselistID = "{9FD33EE3-FAEE-4C93-A799-119AD6398D19}";

		try {
			List listCollection = sitedataWS.getDocumentLibraries("");
			
			for (int i = 0; i < listCollection.size(); i++) {
				BaseList baseList = (BaseList) listCollection.get(i);
				if(baseList.getInternalName().equals(baselistID)){
					return baseList;
				}
			}

		} catch (SharepointException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Get the Items for the document Library
	 * @throws RepositoryException 
	 * */
	public void testGetDocLibListItems() throws MalformedURLException, RepositoryException {
		try {
			listsWS = new ListsWS(sharepointClientContext);
			BaseList baseList = getBaseList();
			List listItemChanges = listsWS.getDocLibListItems(baseList, null, null);
			
			for (int i=0 ; i<listItemChanges.size(); i++) {
				SPDocument doc = (SPDocument) listItemChanges.get(i);
				System.out.println(doc.getUrl());
			}
		} catch (SharepointException e) {
			e.printStackTrace();
		}  
	}
}
