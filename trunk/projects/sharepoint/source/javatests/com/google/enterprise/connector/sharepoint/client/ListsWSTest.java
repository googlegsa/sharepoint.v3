package com.google.enterprise.connector.sharepoint.client;

import junit.framework.TestCase;

import java.text.ParseException;
import java.util.List;

public class ListsWSTest extends TestCase {
  final String sharepointUrl = "http://entpoint05.corp.google.com/unittest";
  final String domain = "ent-qa-d3";
  final String host = "entpoint05.corp.google.com";
  final int port = 80;
  final String username = "testing";
  final String password = "g00gl3";
  final String listInternalName = "{8F2F5129-4380-4932-AE1C-E760C64F5D8F}";
  private ListsWS listsWS;
  
  protected void setUp() throws Exception {
    SharepointClientContext sharepointClientContext = new 
    SharepointClientContext(sharepointUrl, domain, username, password); 
    listsWS = new ListsWS(sharepointClientContext);
    super.setUp();
  }

  public void testGetListItems() { 
    int i;
    boolean found = false;    
    try {
      List listItems = listsWS.getListItems(listInternalName);
      System.out.println("Items found - ");
      for (i=0 ; i<listItems.size(); i++) {
        Document doc = (Document) listItems.get(i);
        System.out.println(doc.getUrl());
        if (doc.getUrl().equals("http://entpoint05.corp.google.com:80/" +
                "unittest/TestDocumentLibrary/TestFolder/TestFolder1/" +
                "webDav.doc")) {
          found = true;
        }
      }
      assertEquals(i, 4);
      assertTrue(found);
    } catch (SharepointException e) {
      e.printStackTrace();
    }
  }
  
  public void testGetListItemChanges() {
    try {
      System.out.println("Changed items found - ");
      List listItemChanges = listsWS.getListItemChanges(listInternalName, 
          Util.listItemsStringToCalendar("2007-03-15 22:00:40"));
      for (int i=0 ; i<listItemChanges.size(); i++) {
        Document doc = (Document) listItemChanges.get(i);
        System.out.println(doc.getUrl());
      }
    } catch (SharepointException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }    
  }
}
