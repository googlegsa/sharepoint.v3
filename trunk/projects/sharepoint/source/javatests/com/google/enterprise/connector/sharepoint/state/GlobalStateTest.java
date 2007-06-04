// Copyright 2006 Google Inc.
package com.google.enterprise.connector.sharepoint.state;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.sharepoint.client.SPDocument;
import com.google.enterprise.connector.sharepoint.client.SharepointException;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Tests the GlobalState object. In most cases, it creates the object,
 * dumps it to XML file 1, loads from XML file 1, dumps that to XML file 2,
 * and asserts that XML file 1 is identical to XML file 2.
 */
public class GlobalStateTest extends TestCase {

  private GlobalState state;
  
  /**
   * Directory where the GlobalState file will be stored.
   * Replace this with a suitable temporary directory if not running on 
   * a Linux or Unix-like system.
   */
  private static final String TMP_DIR = "/tmp";
  
  public final void setUp() {
    state = new GlobalState(TMP_DIR);
  }

  /**
   * Test basic functionality: create a GlobalState, save it to XML,
   * load another from XML, save THAT to XML, and verify that the XML
   * is the same.
   */
  public final void testBasic() {
    // We feed in specific dates for repeatability of the test:
    DateTime time1 = Util.parseDate("20070504T142925.499-0700");
    DateTime time2 = Util.parseDate("20070504T142925.772-0700");
    ListState list1 = null, list2 = null;

    list1 = (ListState) state.makeListState("foo", time1);
    list2 = (ListState) state.makeListState("bar", time2);

    try {
      String output = state.getStateXML();
      String expected1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
            "<state>\n" + 
            "<ListState id=\"foo\">\n" + 
            "<lastMod>20070504T142925.499-0700</lastMod>\n" + 
            "<URL/>\n" + 
            "</ListState>\n" + 
            "<ListState id=\"bar\">\n" + 
            "<lastMod>20070504T142925.772-0700</lastMod>\n" + 
            "<URL/>\n" + 
            "</ListState>\n" + 
            "</state>\n";
      System.out.println(output + "\n\n");
      assertEquals(expected1, output);
      state.saveState();
      GlobalState state2 = null;

      state2 = new GlobalState(TMP_DIR);
      state2.loadState(); // load from the old GlobalState's XML
      // output of the new GlobalState should match the old one's:
      String output2 = state2.getStateXML();

      assertEquals(output, output2);

    } catch (SharepointException e1) {
      fail(e1.toString());
    }

    state.startRecrawl();
    state.updateList(list1, list1.getLastMod());
    state.endRecrawl();

    // list1 should still be there, list2 should be gone
    StatefulObject obj = state.keyMap.get("bar");
    assertNull(obj);
    obj = state.keyMap.get("foo");
    assertNotNull(obj);
  }

  public final void testLastDocCrawled() {
    DateTime time1 = Util.parseDate("20070504T144419.403-0700");
    DateTime time2 = Util.parseDate("20070504T144419.867-0700");
    ListState list1 = null, list2 = null;
    try {
      list1 = (ListState) state.makeListState("foo", time1);
      list2 = (ListState) state.makeListState("bar", time2);
      SPDocument doc1 = new SPDocument("id1", "url1", 
          new GregorianCalendar(2007, 1, 1));
      list1.setLastDocCrawled(doc1);
      state.setCurrentList(list1);
      SPDocument doc2 = new SPDocument("id2", "url2", 
          new GregorianCalendar(2007, 1, 2));
      list2.setLastDocCrawled(doc2);
      String expected1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
            "<state>\n" + 
            "<current id=\"foo\" type=\"ListState\"/>\n" + 
            "<ListState id=\"foo\">\n" + 
            "<lastMod>20070504T144419.403-0700</lastMod>\n" + 
            "<URL/>\n" + 
            "<lastDocCrawled>\n" + 
            "<document id=\"id1\">\n" + 
            "<lastMod>20070201T000000.000-0800</lastMod>\n" + 
            "<url>url1</url>\n" + 
            "</document>\n" + 
            "</lastDocCrawled>\n" + 
            "</ListState>\n" + 
            "<ListState id=\"bar\">\n" + 
            "<lastMod>20070504T144419.867-0700</lastMod>\n" + 
            "<URL/>\n" + 
            "<lastDocCrawled>\n" + 
            "<document id=\"id2\">\n" + 
            "<lastMod>20070202T000000.000-0800</lastMod>\n" + 
            "<url>url2</url>\n" + 
            "</document>\n" + 
            "</lastDocCrawled>\n" + 
            "</ListState>\n" + 
            "</state>\n";
      String output = state.getStateXML();
      System.out.println(output);
      assertEquals(expected1, output);
      state.saveState();
      GlobalState state2 = null;

      state2 = new GlobalState(TMP_DIR);
      state2.loadState(); // load from the old GlobalState's XML
      // output of the new GlobalState should match the old one's:
      String output2 = state2.getStateXML();
      assertEquals(output, output2);
    } catch (SharepointException e) {
      e.printStackTrace();
      fail();
    }   
  }

  /**
   * tests that we can persistify the crawl queue, and reload from it
   */
  public final void testCrawlQueue() {
    DateTime time1 = Util.parseDate("20070504T144419.403-0700");
    DateTime time2 = Util.parseDate("20070504T144419.867-0700");
    ListState list1 = null, list2 = null;
    try {
      list1 = (ListState) state.makeListState("foo", time1);
      list2 = (ListState) state.makeListState("bar", time2);
      SPDocument doc1 = new SPDocument("id1", "url1", 
          new GregorianCalendar(2007, 1, 1));
      list1.setLastDocCrawled(doc1);
      state.setCurrentList(list1);

      SPDocument doc2 = new SPDocument("id2", "url2", 
          new GregorianCalendar(2007, 1, 2));
      list2.setLastDocCrawled(doc2);

      // make a crawl queue & store it in our "current" ListState
      ArrayList<SPDocument> docTree = new ArrayList<SPDocument>();
      SPDocument doc3 = new SPDocument("id3", "url3", 
          new GregorianCalendar(2007, 1, 3));
      docTree.add(doc3);
      SPDocument doc4 = new SPDocument("id4", "url4", 
          new GregorianCalendar(2007, 1, 4));
      docTree.add(doc4);
      list1.setCrawlQueue(docTree);

      String output = state.getStateXML();
      String expected1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
            "<state>\n" + 
            "<current id=\"foo\" type=\"ListState\"/>\n" + 
            "<ListState id=\"foo\">\n" + 
            "<lastMod>20070504T144419.403-0700</lastMod>\n" + 
            "<URL/>\n" + 
            "<lastDocCrawled>\n" + 
            "<document id=\"id1\">\n" + 
            "<lastMod>20070201T000000.000-0800</lastMod>\n" + 
            "<url>url1</url>\n" + 
            "</document>\n" + 
            "</lastDocCrawled>\n" + 
            "<crawlQueue>\n" + 
            "<document id=\"id3\">\n" + 
            "<lastMod>20070203T000000.000-0800</lastMod>\n" + 
            "<url>url3</url>\n" + 
            "</document>\n" + 
            "<document id=\"id4\">\n" + 
            "<lastMod>20070204T000000.000-0800</lastMod>\n" + 
            "<url>url4</url>\n" + 
            "</document>\n" + 
            "</crawlQueue>\n" + 
            "</ListState>\n" + 
            "<ListState id=\"bar\">\n" + 
            "<lastMod>20070504T144419.867-0700</lastMod>\n" + 
            "<URL/>\n" + 
            "<lastDocCrawled>\n" + 
            "<document id=\"id2\">\n" + 
            "<lastMod>20070202T000000.000-0800</lastMod>\n" + 
            "<url>url2</url>\n" + 
            "</document>\n" + 
            "</lastDocCrawled>\n" + 
            "</ListState>\n" + 
            "</state>\n";
      System.out.println(output);
      assertEquals(expected1, output);
      state.saveState();
      GlobalState state2 = null;

      state2 = new GlobalState(TMP_DIR);
      state2.loadState(); // load from the old GlobalState's XML
      // output of the new GlobalState should match the old one's:
      String output2 = state2.getStateXML();
      assertEquals(output, output2);
    } catch (SharepointException e) {
      e.printStackTrace();
      fail();
    }      
  }

}
