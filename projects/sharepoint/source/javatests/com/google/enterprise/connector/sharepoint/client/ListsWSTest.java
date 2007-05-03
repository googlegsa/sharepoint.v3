package com.google.enterprise.connector.sharepoint.client;

import com.google.enterprise.connector.sharepoint.Util;

import junit.framework.TestCase;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListsWSTest extends TestCase {
  final String sharepointUrl = "http://entpoint05.corp.google.com/unittest";
  final String domain = "ent-qa-d3";
  final String host = "entpoint05.corp.google.com";
  final int port = 80;
  final String username = "testing";
  final String password = "g00gl3";
  final String docLibLInternalName = "{8F2F5129-4380-4932-AE1C-E760C64F5D8F}"; 
  
  private Map<String, String> listInternalNames = new HashMap<String, String>();       
  private ListsWS listsWS;
  
  protected void setUp() throws Exception {
    SharepointClientContext sharepointClientContext = new 
    SharepointClientContext(sharepointUrl, domain, username, password); 
    listsWS = new ListsWS(sharepointClientContext);
    
    listInternalNames.put("{6E22CD53-4BDF-422F-8BAD-85506558A589}", "Calendar");
    listInternalNames.put("{AF8B8E07-3B75-4506-B77B-77A5CA39C52D}",
        "Meghnas unittest discussion board");
    listInternalNames.put("{E8B9B5C1-A9EE-4803-9544-60802B5CD9FA}", "Tasks");
    listInternalNames.put("{3D97F002-39A2-463B-9343-0830CCC9CE49}", 
        "Announcements");
    listInternalNames.put("{380EC82B-67DC-42DC-99CF-9E38F4050AB4}", "Links");
    super.setUp();
  }

  public void testGetDocLibListItems() {
    try {
      System.out.println("Items found (Document Libraries) - ");
      BaseList baseList = new BaseList(docLibLInternalName, "DocumentLibrary", 
          docLibLInternalName, null);
      List listItemChanges = listsWS.getDocLibListItemChanges(
          baseList, null);
      for (int i=0 ; i<listItemChanges.size(); i++) {
        Document doc = (Document) listItemChanges.get(i);
        System.out.println(doc.getUrl());
      }
    } catch (SharepointException e) {
      e.printStackTrace();
    }  
  }
  
  public void testGetDocLibListItemChanges() {
    try {
      System.out.println("Changed items found (Document Libraries) - ");
      BaseList baseList = new BaseList(docLibLInternalName, "DocumentLibrary", 
          docLibLInternalName, 
          Util.listItemsStringToCalendar("2007-03-15 23:00:40"));
      List listItemChanges = listsWS.getDocLibListItemChanges(
          baseList, Util.listItemsStringToCalendar("2006-03-15 22:00:40"));
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
  
  public void testGetGenericListItemChanges() {
    for (String listInternalName : listInternalNames.keySet()) {
      try {      
        BaseList baseList = new BaseList(
            listInternalName, listInternalNames.get(listInternalName), 
            "GenericList",
            Util.listItemsStringToCalendar("2007-03-15 23:00:40"));
        System.out.println("Changed items found (Generic Lists) - " + 
            listInternalNames.get(listInternalName));
        List listItemChanges = listsWS.getGenericListItemChanges(baseList,  
            Util.listItemsStringToCalendar("2006-03-15 22:00:40"));
        for (int i=0 ; i<listItemChanges.size(); i++) {
          Document doc = (Document) listItemChanges.get(i);
          System.out.println(doc.getUrl() +  "   " + doc.getDocId());
        }
      } catch (SharepointException e) {
        e.printStackTrace();
      } catch (ParseException e) {
        e.printStackTrace();
      }    
    }
  }
  
  public void testGetAttachments() {
    for (String listInternalName : listInternalNames.keySet()) {
      try {
        BaseList baseList = new BaseList(
            listInternalName, listInternalNames.get(listInternalName),
            "Test Type",
            Util.listItemsStringToCalendar("2007-03-15 23:00:40"));
        Document listItem = new Document(
            "1;#", "http://docId", 
            Util.listItemsStringToCalendar("2007-03-15 23:00:40"), "author_foo",
            "1");      
        List attachments = listsWS.getAttachments(baseList, listItem);
        System.out.println("Attachments found for " + 
            listInternalNames.get(listInternalName));
        for (int i = 0; i < attachments.size(); i++) {
          Document doc = (Document) attachments.get(i);
          System.out.println(doc.getUrl());
        }
      } catch (ParseException e1) {
        e1.printStackTrace();
      } catch (SharepointException e) {
        e.printStackTrace();
      }
    }
  }
}
