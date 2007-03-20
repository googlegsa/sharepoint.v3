// Copyright 2006 Google Inc.

/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.enterprise.connector.sharepoint.state;

import junit.framework.TestCase;
import com.google.enterprise.connector.sharepoint.client.Document;
import com.google.enterprise.connector.sharepoint.client.SharepointException;

import org.joda.time.DateTime;

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
    try {
      GlobalState.injectDependency(ListState.class);
      state = new GlobalState();
    } catch (SharepointException e) {
      fail("failed to inject dependencies");
    }
  }
  
  public final void testBasic() {
    DateTime time1 = new DateTime();
    DateTime time2 = new DateTime();
    time2.plusHours(1);
    ListState list1 = null, list2 = null;
    try {
      list1 = (ListState) 
        state.makeDependentObject("ListState", "foo", time1);
      list2 = (ListState)
        state.makeDependentObject("ListState", "bar", time2);
    } catch (SharepointException e) {
      e.printStackTrace();
      fail();
    }
    try {
      String output = state.saveStateXML();
      System.out.println(output + "\n\n");
      state.saveState();
      GlobalState state2 = null;

      state2 = new GlobalState();
      state2.loadState(); // load from the old GlobalState's XML
      // output of the new GlobalState should match the old one's:
      String output2 = state2.saveStateXML();

      assertEquals(output, output2);

    } catch (SharepointException e1) {
      fail(e1.toString());
    }
    
    state.startRefresh();
    try {
      state.updateStatefulObject(list1, list1.getLastMod());
    } catch (SharepointException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    state.endRefresh();
    
    // list1 should still be there, list2 should be gone
    HashMap<String, StatefulObject> keyMap = state.keyMaps.get("ListState");
    assertNotNull(keyMap);
    StatefulObject obj = keyMap.get("bar");
    assertNull(obj);
    obj = keyMap.get("foo");
    assertNotNull(obj);
  }
  
  public final void testLastDocCrawled() {
    DateTime time1 = new DateTime();
    DateTime time2 = new DateTime();
    time2.plusHours(1);
    ListState list1 = null, list2 = null;
    try {
      list1 = (ListState) state.makeDependentObject("ListState", "foo", time1);
      list2 = (ListState) state.makeDependentObject("ListState", "bar", time2);
      Document doc1 = new Document("id1", "url1", new GregorianCalendar());
      list1.setLastDocCrawled(doc1);
      state.setCurrentObject("ListState", list1);
      Document doc2 = new Document("id2", "url2", new GregorianCalendar());
      list2.setLastDocCrawled(doc2);

      String output = state.saveStateXML();
      System.out.println(output);
      state.saveState();
      GlobalState state2 = null;

      state2 = new GlobalState();
      state2.loadState(); // load from the old GlobalState's XML
      // output of the new GlobalState should match the old one's:
      String output2 = state2.saveStateXML();
      assertEquals(output, output2);
    } catch (SharepointException e) {
      e.printStackTrace();
      fail();
    }   
  }
  
  public final void testCrawlQueue() {
    DateTime time1 = new DateTime();
    DateTime time2 = new DateTime();
    time2.plusHours(1);
    ListState list1 = null, list2 = null;
    try {
      list1 = (ListState) state.makeDependentObject("ListState", "foo", time1);
      list2 = (ListState) state.makeDependentObject("ListState", "bar", time2);
      Document doc1 = new Document("id1", "url1", new GregorianCalendar());
      list1.setLastDocCrawled(doc1);
      state.setCurrentObject("ListState", list1);
      
      Document doc2 = new Document("id2", "url2", new GregorianCalendar());
      list2.setLastDocCrawled(doc2);

      // make a crawl queue & store it in our "current" ListState
      TreeSet<Document> docTree = new TreeSet<Document>();
      Document doc3 = new Document("id3", "url3", new GregorianCalendar());
      docTree.add(doc3);
      Document doc4 = new Document("id4", "url4", new GregorianCalendar());
      docTree.add(doc4);
      list1.setCrawlQueue(docTree);
      
      String output = state.saveStateXML();
      System.out.println(output);
      state.saveState();
      GlobalState state2 = null;

      state2 = new GlobalState();
      state2.loadState(); // load from the old GlobalState's XML
      // output of the new GlobalState should match the old one's:
      String output2 = state2.saveStateXML();
      assertEquals(output, output2);
    } catch (SharepointException e) {
      e.printStackTrace();
      fail();
    }      
  }
}
