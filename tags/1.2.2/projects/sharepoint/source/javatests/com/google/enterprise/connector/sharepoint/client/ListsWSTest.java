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

	//set the connector configuration information
//	-------------gsp02ps5265(sps 2007)---------------------------------
	final String sharepointType = SharepointConnectorType.SP2007;
	  final String sharepointUrl = "gsp02ps5265";
	  final String domain = "gsp02ps5265";
	  final String username = "administrator";
	  final String password = "pspl!@#";

	  final String mySiteBaseURL=null;
	  final String googleConnWorkDir = null;

	  final String exclURLs =null;
	  final String aliasHost = null;
	  final String aliasPort = null;
	  final String inclURLs ="^http://";
//	-------------END: gsp02ps5265---------------------------------


//	-------------ps4312(sps 2007)---------------------------------
	/*final String sharepointType = SharepointConnectorType.SP2007;
	final String sharepointUrl = "http://ps4312.persistent.co.in:2905/Orangesite/abc/";
	final String domain = "ps4312";
	final String username = "administrator";
	final String password = "pspl!@#";

	final String mySiteBaseURL=null;
	final String googleConnWorkDir = null;

	final String exclURLs =null;
	final String aliasHost = null;
	final String aliasPort = null;
	final String inclURLs ="^http://";*/
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
			List listCollection = sitedataWS.getDocumentLibraries();
			
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
//			System.out.println("Items found (Document Libraries) - ");
//			String docLibLInternalName="{CBA3C548-D0CA-47EC-B91B-4885F86C23DE}";
//
//			BaseList baseList = new BaseList(docLibLInternalName, "aaa","DocumentLibrary","20080229 04:26:22","DocumentLibrary","/Lists/aaa/AllItems.aspx");
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

	/*public void testGetDocLibListItemChanges() throws MalformedURLException {
		int num = 0;
		try {
			System.out.println("Changed items found (Document Libraries) - ");

			BaseList baseList = new BaseList(docLibLInternalName, "Shared Documents",docLibLInternalName,Util.listItemsStringToCalendar("2007-08-1 23:00:40"));
			List listItemChanges = listsWS.getDocLibListItemChanges(baseList, Util.listItemsStringToCalendar("2007-08-1 23:00:40"));
			for (int i=0 ; i<listItemChanges.size(); i++) {
				SPDocument doc = (SPDocument) listItemChanges.get(i);
				System.out.println(doc.getUrl());
				num++;
			}
		} catch (SharepointException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}    


     Total Docs: 13
     Changed Docs: 9 


		System.out.println("Totals Documents: "+num);
		Assert.assertEquals(9, num);
	}*/

	/*public void testGetGenericListItemChanges() throws MalformedURLException {
		int num = 0;
		final int iDoc=19;
		Set ks = listInternalNames.keySet();
		Iterator it = ks.iterator();
		while(it.hasNext()) {

			try {
				String listInternalName =(String) it.next();
				BaseList baseList = new BaseList(listInternalName, (String) listInternalNames.get(listInternalName),"GenericList",
						Util.listItemsStringToCalendar("2007-03-15 23:00:40"));
				System.out.println("Changed items found (Generic Lists) - " 
						+listInternalNames.get(listInternalName));
				List listItemChanges = listsWS.getGenericListItemChanges(baseList,Util.listItemsStringToCalendar("2006-03-15 22:00:40"));
				for (int i=0 ; i<listItemChanges.size(); i++) {
					SPDocument doc = (SPDocument) listItemChanges.get(i);
					System.out.println(doc.getUrl());
					num++;
				}
			} catch (SharepointException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}    
		}
		System.out.println("Total: "+num);
		Assert.assertEquals(iDoc, num);
	}*/

	/*public void testGetAttachments() throws MalformedURLException {
		int num = 0;
		//for (String listInternalName : listInternalNames.keySet()) {
		Set ks =listInternalNames.keySet();
		Iterator it = ks.iterator();
		while(it.hasNext()){
			String listInternalName = (String) it.next();
			try {
				BaseList baseList = new BaseList(
						listInternalName, (String) listInternalNames.get(listInternalName),
						"Test Type",
						Util.listItemsStringToCalendar("2006-03-15 23:00:40"));
				SPDocument listItem = new SPDocument(
						"1;#{B686ADD8-0AF8-40E2-A997-0FDFD90E3CD7}", "http://ps4312.persistent.co.in:43386/amitsite/Lists/Announcements/DispForm.aspx?ID=1", 
						Util.listItemsStringToCalendar("2006-03-15 23:00:40"), "author_foo",
				"1");      
				List attachments = listsWS.getAttachments(baseList, listItem);
				System.out.println("Attachments found for " +listInternalNames.get(listInternalName));
				for (int i = 0; i < attachments.size(); i++) {
					SPDocument doc = (SPDocument) attachments.get(i);
					System.out.println(doc.getUrl());
					num++;
				}
			} catch (ParseException e1){
				e1.printStackTrace();
			} catch (SharepointException e){
				e.printStackTrace();
			}
		}
		System.out.println("Total: "+num);
		Assert.assertEquals(0, num);
	}*/
}
