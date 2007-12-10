package com.google.enterprise.connector.sharepoint.client;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.Util;

/**
 * Test cases for {@link ListsWS}.
 * @author amit_kagrawal
 * */
public class ListsWSTest extends TestCase {
  /*final String sharepointUrl = "http://entpoint05.corp.google.com/unittest";
  final String domain = "ent-qa-d3";
  final String host = "entpoint05.corp.google.com";
  final int port = 80;
  final String username = "testing";
  final String password = "g00gl3";
  final String includeURL = "http://entpoint05.corp.google.com/unittest";
  final String docLibLInternalName = "{8F2F5129-4380-4932-AE1C-E760C64F5D8F}"; 
  final String issuesInternalName = "{2CCEF12F-A3C4-4921-98B6-E4334D1CFB9C}";*/
	
// 	credentials of ps4312 site -- moss 2007
//	--------------------------NON ADMIN CREDENTIALS WITH ALTERNATIVE DOMAIN------------------
	  /*final String sharepointUrl = "http://ps4312:43386/amitsite";
	  final String domain = "persistent";
	  final String host = "ps4312";
	  final int port = 43386;
	  final String username = "amit_kagrawal";
	  final String password = "Agrawal!@#";*/
//	--------------------------END: NON ADMIN CREDENTIALS WITH ALTERNATIVE DOMAIN------------------

//	-------------PS4312(MOSS 2007)---------------------------------
	 /* final String sharepointUrl = "http://ps4312:43386/amitsite";
	  final String domain = "ps4312";
	  final String host = "ps4312";
	  final int port = 43386;
	  final String username = "Administrator";
	  final String password = "pspl!@#";
	  final String mySiteBaseURL= "http://ps4312:23508";
	  final String googleConnWorkDir = null;
	  final String exclURLs =null ;
	  final String inclURLs ="http://ps4312:43386,http://ps4312:23508";*/

//	-------------END: PS4312---------------------------------
//	-------------PS4312(MOSS 2007)---------------------------------
	  final String sharepointUrl = "http://ps4312.persistent.co.in:43386/amitsite";
	  
	  final String host = "ps4312";
	  final int port = 43386;
	  final String username = "Administrator";
	  final String password = "pspl!@#";
	  final String domain = "ps4312";
	  final String mySiteBaseURL= "http://ps4312.persistent.co.in:23508";
	  final String googleConnWorkDir = null;
	  final String exclURLs ="" ;
	  final String inclURLs ="http://ps4312.persistent.co.in:43386,http://ps4312.persistent.co.in:23508,http://ps4312:43386,http://ps4312:23508";
	  final String docLibLInternalName = "{62305F35-71EB-4960-8C21-37A8A7ECD818}"; 
	  final String issuesInternalName = "{62305F35-71EB-4960-8C21-37A8A7ECD818}";
//	-------------END: PS4312---------------------------------

//	-------------japanese(MOSS 2007)---------------------------------
	  
	 /* final String sharepointUrl = "http://v-ecsc6:25000/Japanese";
	  final String domain = "v-ecsc6";
	  final String host = "v-ecsc6";
	  final int port = 25000;
	  final String username = "Administrator";
	  final String password = "pspl!@#";
	*/
//	-------------END: japanese---------------------------------
	  
//	-------------PS2314(WSS 3.0)---------------------------------
	/*  final String sharepointUrl = "http://ps2314:43266/amitsite";
	  final String domain = "ps2314";
	  final String host = "ps2314";
	  final int port = 43266;
	  final String username = "Administrator";
	  final String password = "pspl!@#";
	  final String mySiteBaseURL= "http://ps4312:23508";
	  final String googleConnWorkDir = null;
	  final String exclURLs =null ;
	  final String inclURLs ="http://ps4312:43386/amitsite,http://ps4312:23508,http://ps4312:43386";
	*/
//	-------------END: PS2314---------------------------------
	  
//	-------------v-ecsc3: SSL(ANOTHER MOSS 2007 with SSL)---------------------------------
	  /*final String sharepointUrl = "https://v-ecsc3.persistent.co.in:443/ssl";
	  final String domain = "v-ecsc3";
	  final String host = "v-ecsc3";
	  final int port = 443;//default port is 443 for ssl
	  final String username = "Administrator";
	  final String password = "pspl!@#";
	  //final String mySiteBaseURL= "http://ps4312:23508";
	  final String mySiteBaseURL= null;
	  final String googleConnWorkDir = null;
	  final String exclURLs =null ;
	  final String inclURLs ="https://v-ecsc3.persistent.co.in:443/ssl";*/

//	-------------END: SSL---------------------------------
  
  private Map listInternalNames = new HashMap();       
  private ListsWS listsWS;
  
  protected void setUp() throws Exception {
/*    SharepointClientContext sharepointClientContext = new 
    SharepointClientContext(sharepointUrl, domain, username, password, null,includeURL,null,null);*/
    SharepointClientContext sharepointClientContext = new SharepointClientContext(sharepointUrl, domain, username, password, googleConnWorkDir,inclURLs,exclURLs,mySiteBaseURL,null,null);

    
    
    listsWS = new ListsWS(sharepointClientContext);
    
    listInternalNames.put("{34EA4B8C-3663-41C7-A0EA-13E42D44D404}","Calendar");//
    listInternalNames.put("{25C1E978-FA4B-4076-BA16-B125DEEA26C2}","Tasks");//
    listInternalNames.put("{5D1BB107-7C38-446A-B5F6-FF94624390E9}","Announcements");//
    listInternalNames.put("{6858021D-005E-4AEC-8AD4-D23B12D0170D}","Links");//
    listInternalNames.put(issuesInternalName, "Issues");
    super.setUp();
  }

  public void testGetDocLibListItems() throws MalformedURLException {
    try {
      System.out.println("Items found (Document Libraries) - ");
      BaseList baseList = new BaseList(docLibLInternalName, "DocumentLibrary",docLibLInternalName, null);
      List listItemChanges = listsWS.getDocLibListItemChanges(baseList, null);
      for (int i=0 ; i<listItemChanges.size(); i++) {
        SPDocument doc = (SPDocument) listItemChanges.get(i);
        System.out.println(doc.getUrl());
      }
    } catch (SharepointException e) {
      e.printStackTrace();
    }  
  }
  
  public void testGetDocLibListItemChanges() throws MalformedURLException {
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
    
    /*
     Total Docs: 13
     Changed Docs: 9 
     */
    
    System.out.println("Totals Documents: "+num);
    Assert.assertEquals(9, num);
  }
  
  public void testGetGenericListItemChanges() throws MalformedURLException {
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
  }
  
  public void testGetAttachments() throws MalformedURLException {
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
  }
}
