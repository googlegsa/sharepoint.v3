// Copyright 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.state;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

public class ListStateTest extends TestCase {

  SharepointClientContext sharepointClientContext;

  public void setUp() throws Exception {
    sharepointClientContext = TestConfiguration.initContext();
  }

  public void testComparison() throws SharepointException {
    ListState list1 = new ListState(TestConfiguration.Site1_List1_GUID,
        "No Title", SPConstants.DOC_LIB, Calendar.getInstance(),
        SPConstants.NO_TEMPLATE, TestConfiguration.Site1_List1_URL, null);
    list1.setLastMod(new DateTime(2009, 9, 07, 10, 25, 36, 100));

    ListState list2 = new ListState(TestConfiguration.Site2_List1_GUID,
        "No Title", SPConstants.DOC_LIB, Calendar.getInstance(),
        SPConstants.NO_TEMPLATE, TestConfiguration.Site2_List1_URL, null);
    list2.setLastMod(new DateTime(2009, 9, 05, 10, 25, 36, 100));

    assertEquals(1, list1.compareTo(list2));

    list1.setLastMod(new DateTime(2009, 9, 04, 10, 25, 36, 100));
    assertEquals(-1, list1.compareTo(list2));

    list1.setLastMod(list2.getLastMod());
    assertEquals(list1.getPrimaryKey().compareTo(list2.getPrimaryKey()), list1.compareTo(list2));
  }

  public void testGetDateForWSRefresh() throws SharepointException {
    Calendar cal1 = Calendar.getInstance();
    ListState list1 = new ListState(TestConfiguration.Site1_List1_GUID,
        "No Title", SPConstants.DOC_LIB, cal1, SPConstants.NO_TEMPLATE,
        TestConfiguration.Site1_List1_URL, null);
    assertEquals(cal1, list1.getDateForWSRefresh());

    Calendar cal2 = Calendar.getInstance();
    cal2.add(Calendar.MONTH, -1);
    list1.setLastDocProcessed(new SPDocument("1", "X", cal2, ActionType.ADD));
    assertEquals(cal2, list1.getDateForWSRefresh());
  }

  public void testCachedDeletedID() throws SharepointException {
    ListState list1 = new ListState(TestConfiguration.Site1_List1_GUID,
        "No Title", SPConstants.DOC_LIB, null, SPConstants.NO_TEMPLATE,
        TestConfiguration.Site1_List1_URL, null);
    list1.addToDeleteCache("1");
    list1.addToDeleteCache("1");
    list1.addToDeleteCache("3");
    list1.addToDeleteCache("2");
    assertTrue(list1.isInDeleteCache("1"));
    list1.removeFromDeleteCache("1");
    assertFalse(list1.isInDeleteCache("1"));
  }

  public void testExtraIDs() throws SharepointException {
    System.out.println("Testing ExtraIDs handling...");
    final ListState state = new ListState("", "", "", null, "", "", null);
    state.setType(SPConstants.DOC_LIB);
    state.setUrl("http://host.mycom.co.in:25000/sanity/Test Library/Forms/AllItems.aspx");
    state.setListConst("sanity/Test Library/");
    String str1 = "#1~Forms1#2#33~Forms2#4#5~Forms3#3/#5/#33/#1";
    state.setIDs(new StringBuffer(str1));

    // Expected -> #1~Forms1#2#33~Forms2#4#5~Forms3#3/#5/#33/#1
    assertExtraIds(state.getExtraIDs("1"), "3", "2", "1", "5", "4", "33");
    assertExtraIds(state.getExtraIDs("33"), "3", "4", "5", "33");
    assertExtraIds(state.getExtraIDs("5"), "3", "5");
    assertExtraIds(state.getExtraIDs("2"), "2");
    assertExtraIds(state.getExtraIDs("4"), "4");
    assertExtraIds(state.getExtraIDs("3"), "3");

    final String docURL = "http://host.mycom.co.in:25000/sanity/Test Library/Forms1/Forms2/Forms3/AllItems.aspx";
    state.updateExtraIDs(docURL, "30", false);
    // Expected -> #1~Forms1#2#33~Forms2#4#5~Forms3#30#3/#5/#33/#1
    assertExtraIds(state.getExtraIDs("1"), "3", "2", "1", "5", "4", "33", "30");
    assertExtraIds(state.getExtraIDs("33"), "3", "4", "5", "33", "30");
    assertExtraIds(state.getExtraIDs("5"), "3", "5", "30");
    assertExtraIds(state.getExtraIDs("2"), "2");
    assertExtraIds(state.getExtraIDs("4"), "4");
    assertExtraIds(state.getExtraIDs("3"), "3");
    assertExtraIds(state.getExtraIDs("30"), "30");

    state.removeExtraID("3");
    // Expected -> #1~Forms1#2#33~Forms2#4#5~Forms3#30/#5/#33/#1
    assertExtraIds(state.getExtraIDs("1"), "2", "1", "5", "4", "33", "30");
    assertExtraIds(state.getExtraIDs("33"), "4", "5", "33", "30");
    assertExtraIds(state.getExtraIDs("5"), "5", "30");
    assertExtraIds(state.getExtraIDs("2"), "2");
    assertExtraIds(state.getExtraIDs("4"), "4");
    assertExtraIds(state.getExtraIDs("3"), "3");
    assertExtraIds(state.getExtraIDs("30"), "30");

    state.removeExtraID("30");
    state.removeExtraID("4");
    // Expected -> #1~Forms1#2#33~Forms2#5~Forms3/#5/#33/#1
    assertExtraIds(state.getExtraIDs("1"), "2", "1", "5", "33");
    assertExtraIds(state.getExtraIDs("33"), "5", "33");
    assertExtraIds(state.getExtraIDs("5"), "5");
    assertExtraIds(state.getExtraIDs("2"), "2");
    assertExtraIds(state.getExtraIDs("4"), "4");
    assertExtraIds(state.getExtraIDs("3"), "3");
    assertExtraIds(state.getExtraIDs("30"), "30");
  }

  public void testExtraIDs2() throws SharepointException {
    assertExtraIds(
        "#4~#41#5/#4",
        "4", "4", "5", "41");
  }

  public void testExtraIDs3() throws SharepointException {
    // Note: #1 was added here between #1~ and /#1 to validate the parser.
    // This is not an extra IDs string that can be generated.
    assertExtraIds(
        "#1~docs#139~2010 NA#141~2010 CA/#141#1#555/#139/#1#3~ppts/#3#4~pdfs/#4",
        "1", "1", "139", "141", "555");
  }

  public void testExtraIDs4() throws SharepointException {
    assertExtraIds(
        "#1~xls#21#20/#1#2~docs#170#165#104#103#12#7/#2#3~pdfs#191#190#189"
        + "#188#187#186#185#184#183#182#181#174#173#172#169#168#164#163#160"
        + "#148#147#142#141#102#83#82#81#34#33#32#31#30#29#28#27#26#25#24#23"
        + "#22#19#18#17#16#15#14#13#11#10#9#8#6#5#115~travel#144#143#138#134"
        + "#133#132#131#130#129#128#127#126#125#124#123#122#121#120#119#118"
        + "#117#116/#115#91~SPFC#100#99#98#95#94#93#92/#91#85~events#192#180"
        + "#179#178#177#176#175#171#167#162#157#156#154/#85#47~menu#193#84#71"
        + "#69#67#65#63#61#59#57#55#53#51#49/#47/#3#4~ppts#149~ParkingLot#151"
        + "#150/#149/#4",
        "4", "151", "150", "149", "4");
  }

  public void testExtraIDs5() throws SharepointException {
    assertExtraIds(
        "#1~leader#414/#1#2~org#242#241#240#236#235#234#226#225#115#421"
        + "~SITE#431#422/#421#283~EP#430#309#294#285/#283#237~SEP#239#238/"
        + "#237#227~SE#384#383#382#381#380#233#232#231#230#229#228/#227/#2"
        + "#3~pdfs#404#387#327#317#220#160#131#114#113/#3#4~ppts#449#406"
        + "#396#282#125#119#450~IC#458#456#455#454#453#452#451/#450/#4",
        "4", "449", "406", "396", "282", "125", "119", "458", "456", "455",
        "454", "453", "452", "451", "450", "4");
  }

  public void testExtraIDs6() throws SharepointException {
    assertExtraIds(
        "#1~IP#610~ST/#610#61~Proposals/#61#53~PP/#53#23~RP/#23/#1",
        "61", "61");
  }

  private void assertExtraIds(String ids, String id, String... expectedIds)
      throws SharepointException {
    final ListState state = new ListState("", "", "", null, "", "", null);
    state.setType(SPConstants.DOC_LIB);
    state.setUrl(TestConfiguration.sharepointUrl);
    state.setIDs(new StringBuffer(ids));
    assertExtraIds(state.getExtraIDs(id), expectedIds);
  }

  /**
   * Asserts that the set of extraIds contains the expected values.
   */
  private void assertExtraIds(Set<String> actualIds, String... expectedIds) {
    assertEquals(expectedIds.length, actualIds.size());
    for (String id : expectedIds) {
      assertTrue(id + " not in " + actualIds, actualIds.contains(id));
    }
  }

  public void testUpdateExtraIDAsAttachment() throws SharepointException {
    String attchmnt1 = "http://host.mycom.co.in:25000/sanity/Test Library1/Forms/AllItems.aspx";
    String attchmnt2 = "http://host.mycom.co.in:25000/sanity/Test Library2/Forms/AllItems.aspx";
    String attchmnt3 = "http://host.mycom.co.in:25000/sanity/Test Library3/Forms/AllItems.aspx";
    String attchmnt4 = "http://host.mycom.co.in:25000/sanity/Test Library4/Forms/AllItems.aspx";
    String attchmnt5 = "http://host.mycom.co.in:25000/sanity/Test Library5/Forms/AllItems.aspx";
    String attchmnt6 = "http://host.mycom.co.in:25000/sanity/Test Library6/Forms/AllItems.aspx";
    String attchmnt7 = "http://host.mycom.co.in:25000/sanity/Test Library7/Forms/AllItems.aspx";
    String attchmnt8 = "http://host.mycom.co.in:25000/sanity/Test Library8/Forms/AllItems.aspx";

    final ListState testList = new ListState("", "", SPConstants.GENERIC_LIST,
        null, "", "", null);
    testList.updateExtraIDAsAttachment("1", attchmnt1);
    testList.updateExtraIDAsAttachment("1", attchmnt2);
    testList.updateExtraIDAsAttachment("1", attchmnt3);
    testList.updateExtraIDAsAttachment("2", attchmnt4);
    testList.updateExtraIDAsAttachment("2", attchmnt5);
    testList.updateExtraIDAsAttachment("3", attchmnt6);
    testList.updateExtraIDAsAttachment("4", attchmnt7);
    testList.updateExtraIDAsAttachment("4", attchmnt8);

    List<String> attchmnts = testList.getAttachmntURLsFor("1");
    assertEquals(3, attchmnts.size());
    assertTrue(attchmnts.contains(attchmnt1));
    assertTrue(attchmnts.contains(attchmnt2));
    assertTrue(attchmnts.contains(attchmnt3));
    attchmnts = testList.getAttachmntURLsFor("2");
    assertEquals(2, attchmnts.size());
    assertTrue(attchmnts.contains(attchmnt4));
    assertTrue(attchmnts.contains(attchmnt5));
    attchmnts = testList.getAttachmntURLsFor("3");
    assertEquals(1, attchmnts.size());
    assertTrue(attchmnts.contains(attchmnt6));
    attchmnts = testList.getAttachmntURLsFor("4");
    assertEquals(2, attchmnts.size());
    assertTrue(attchmnts.contains(attchmnt7));
    assertTrue(attchmnts.contains(attchmnt8));

    testList.removeAttachmntURLFor("1", attchmnt3);
    attchmnts = testList.getAttachmntURLsFor("1");
    assertEquals(2, attchmnts.size());
    assertTrue(attchmnts.contains(attchmnt1));
    assertTrue(attchmnts.contains(attchmnt2));
    attchmnts = testList.getAttachmntURLsFor("2");
    assertEquals(2, attchmnts.size());
    assertTrue(attchmnts.contains(attchmnt4));
    assertTrue(attchmnts.contains(attchmnt5));
    attchmnts = testList.getAttachmntURLsFor("3");
    assertEquals(1, attchmnts.size());
    assertTrue(attchmnts.contains(attchmnt6));
    attchmnts = testList.getAttachmntURLsFor("4");
    assertEquals(2, attchmnts.size());
    assertTrue(attchmnts.contains(attchmnt7));
    assertTrue(attchmnts.contains(attchmnt8));

    testList.removeAttachmntURLFor("2", attchmnt4);
    testList.removeAttachmntURLFor("2", attchmnt5);
    attchmnts = testList.getAttachmntURLsFor("1");
    assertEquals(2, attchmnts.size());
    assertTrue(attchmnts.contains(attchmnt1));
    assertTrue(attchmnts.contains(attchmnt2));
    attchmnts = testList.getAttachmntURLsFor("2");
    assertEquals(0, attchmnts.size());
    attchmnts = testList.getAttachmntURLsFor("3");
    assertEquals(1, attchmnts.size());
    assertTrue(attchmnts.contains(attchmnt6));
    attchmnts = testList.getAttachmntURLsFor("4");
    assertEquals(2, attchmnts.size());
    assertTrue(attchmnts.contains(attchmnt7));
    assertTrue(attchmnts.contains(attchmnt8));
  }

  public void testGetLastDocForWSRefresh() throws SharepointException {
    Calendar cal1 = Calendar.getInstance();
    ListState list1 = new ListState(TestConfiguration.Site1_List1_GUID,
        "No Title", SPConstants.DOC_LIB, cal1, SPConstants.NO_TEMPLATE,
        TestConfiguration.Site1_List1_URL, null);
    assertNull(list1.getLastDocForWSRefresh());

    SPDocument doc1 = new SPDocument("1", "X", cal1, SPConstants.NO_AUTHOR,
        SPConstants.DOCUMENT, "XX", FeedType.METADATA_URL_FEED, SPType.SP2007);
    list1.setLastDocProcessed(doc1);
    assertEquals(doc1, list1.getLastDocForWSRefresh());

    SPDocument doc2 = new SPDocument("2", "X", cal1, SPConstants.NO_AUTHOR,
        SPConstants.DOCUMENT, "XX", FeedType.METADATA_URL_FEED, SPType.SP2007);
    SPDocument doc3 = new SPDocument("3", "X", cal1, SPConstants.NO_AUTHOR,
        SPConstants.DOCUMENT, "XX", FeedType.METADATA_URL_FEED, SPType.SP2007);
    SPDocument doc4 = new SPDocument("4", "X", cal1, SPConstants.NO_AUTHOR,
        SPConstants.DOCUMENT, "XX", FeedType.METADATA_URL_FEED, SPType.SP2007);
    List<SPDocument> docs = new LinkedList<SPDocument>();
    docs.add(doc2);
    docs.add(doc3);
    docs.add(doc4);
    list1.setCrawlQueue(docs);
    assertEquals(doc4, list1.getLastDocForWSRefresh());

    doc4.setAction(ActionType.DELETE);
    assertEquals(doc3, list1.getLastDocForWSRefresh());
  }
}
