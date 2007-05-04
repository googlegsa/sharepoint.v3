package com.google.enterprise.connector.sharepoint.state;

import junit.framework.TestCase;


import junit.framework.TestCase;
import com.google.enterprise.connector.sharepoint.client.SPDocument;
import com.google.enterprise.connector.sharepoint.client.SharepointException;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * Tests the GlobalState object. In most cases, it creates the object,
 * dumps it to XML file 1, loads from XML file 1, dumps that to XML file 2,
 * and asserts that XML file 1 is identical to XML file 2.
 */
public class GlobalStateTest extends TestCase {

  private GlobalState state;
  
  public final void setUp() {
    state = new GlobalState();
  }

  /**
   * Test basic functionality: create a GlobalState, save it to XML,
   * load another from XML, save THAT to XML, and verify that the XML
   * is the same.
   */
  public final void testBasic() {
    DateTime time1 = new DateTime();
    DateTime time2 = new DateTime();
    time2.plusHours(1);
    ListState list1 = null, list2 = null;

    list1 = (ListState) state.makeListState("foo", time1);
    list2 = (ListState) state.makeListState("bar", time2);

    try {
      String output = state.getStateXML();
      System.out.println(output + "\n\n");
      state.saveState();
      GlobalState state2 = null;

      state2 = new GlobalState();
      state2.loadState(); // load from the old GlobalState's XML
      // output of the new GlobalState should match the old one's:
      String output2 = state2.getStateXML();

      assertEquals(output, output2);

    } catch (SharepointException e1) {
      fail(e1.toString());
    }

    state.startRefresh();
     state.updateList(list1, list1.getLastMod());
    state.endRefresh();

    // list1 should still be there, list2 should be gone
    StatefulObject obj = state.keyMap.get("bar");
    assertNull(obj);
    obj = state.keyMap.get("foo");
    assertNotNull(obj);
  }

  public final void testLastDocCrawled() {
    DateTime time1 = new DateTime();
    DateTime time2 = new DateTime();
    time2.plusHours(1);
    ListState list1 = null, list2 = null;
    try {
      list1 = (ListState) state.makeListState("foo", time1);
      list2 = (ListState) state.makeListState("bar", time2);
      SPDocument doc1 = new SPDocument("id1", "url1", new GregorianCalendar());
      list1.setLastDocCrawled(doc1);
      state.setCurrentList(list1);
      SPDocument doc2 = new SPDocument("id2", "url2", new GregorianCalendar());
      list2.setLastDocCrawled(doc2);

      String output = state.getStateXML();
      System.out.println(output);
      state.saveState();
      GlobalState state2 = null;

      state2 = new GlobalState();
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
    DateTime time1 = new DateTime();
    DateTime time2 = new DateTime();
    time2.plusHours(1);
    ListState list1 = null, list2 = null;
    try {
      list1 = (ListState) state.makeListState("foo", time1);
      list2 = (ListState) state.makeListState("bar", time2);
      SPDocument doc1 = new SPDocument("id1", "url1", new GregorianCalendar());
      list1.setLastDocCrawled(doc1);
      state.setCurrentList(list1);

      SPDocument doc2 = new SPDocument("id2", "url2", new GregorianCalendar());
      list2.setLastDocCrawled(doc2);

      // make a crawl queue & store it in our "current" ListState
      ArrayList<SPDocument> docTree = new ArrayList<SPDocument>();
      SPDocument doc3 = new SPDocument("id3", "url3", new GregorianCalendar());
      docTree.add(doc3);
      SPDocument doc4 = new SPDocument("id4", "url4", new GregorianCalendar());
      docTree.add(doc4);
      list1.setCrawlQueue(docTree);

      String output = state.getStateXML();
      System.out.println(output);
      state.saveState();
      GlobalState state2 = null;

      state2 = new GlobalState();
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
