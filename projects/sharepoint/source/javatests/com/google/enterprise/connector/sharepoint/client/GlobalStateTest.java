package com.google.enterprise.connector.sharepoint.client;

import junit.framework.TestCase;
import com.google.common.base.GoogleException;
import com.google.common.collect.TreeMultimap;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

public class GlobalStateTest extends TestCase {

  private GlobalState state;

  public final void setUp() {
    state = new GlobalState();
  }
  
  public final void testBasic() {
    DateTime time1 = new DateTime();
    DateTime time2 = new DateTime();
    time2.plusHours(1);

    state.updateSite("foo", time1);
    state.updateSite("bar", time2);
    
    DateTime time3 = new DateTime();
    DateTime time4 = new DateTime();
    time4.plusHours(1);
    state.updateList("guid1", time3);
    state.updateList("guid2", time4);
    
    String output = state.dump();
    System.out.println(output + "\n\n");
    // now load that into another GlobalState, and make sure we get the
    // same state. (the dump() XML may not be identical because of ordering) 
    GlobalState stateNew = new GlobalState();
    stateNew.load(output);
    String output2 = stateNew.dump();
    System.out.println(output2);
    assert(state.listDateMap.equals(stateNew.listDateMap));
    assert(state.siteDateMap.equals(stateNew.siteDateMap));
    // asserting equals on the HashMaps doesn't work, for some reason.
    
    state.startRefresh();
    state.updateList("guid1", time3);
    state.updateSite("foo", time1);
    state.endRefresh();
    
    // the ones we didn't touch should now be gone
    assert(state.listGuidMap.get("guid2") == null);
    assert(state.siteUrlMap.get("bar") == null);
  }

}
