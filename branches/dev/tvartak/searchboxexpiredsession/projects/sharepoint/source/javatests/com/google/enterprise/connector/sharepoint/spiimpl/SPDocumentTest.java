// Copyright 2007 Google Inc.
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

package com.google.enterprise.connector.sharepoint.spiimpl;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;

import com.sun.jndi.toolkit.url.UrlUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class SPDocumentTest extends TestCase {

  SPDocument doc;

  protected void setUp() throws Exception {
    super.setUp();
    SharepointClientContext spContext = TestConfiguration.initContext();
    List<SPDocument> allDocs = TestConfiguration.initState(spContext).lookupList(
        TestConfiguration.Site1_URL, TestConfiguration.Site1_List1_GUID).getCrawlQueue();
    assertTrue(allDocs.size() > 0);
    this.doc = allDocs.get(0);
    assertNotNull(this.doc);
    this.doc.setSharepointClientContext(spContext);
    this.doc.setContentDwnldURL(doc.getUrl());

    // TODO(jlacey): Replace this use of UrlUtil, which generates a
    // warning on Java 7 when not using a bootclasspath. Also, this
    // code is either pointless or it's a smoke test that belongs in a
    // test method.
    String str = UrlUtil.encode(doc.getUrl(), "UTF-8");
    String charset = new GetMethod(str).getParams().getUriCharset();
    URI uri = new URI(doc.getUrl(), true, charset);
  }

  public void testCompare() {
    final SPDocument tmpdoc1 = new SPDocument("1", "HTTP://MYCOMP.COM",
        Calendar.getInstance(), SPConstants.NO_AUTHOR, SPConstants.NO_OBJTYPE,
        SPConstants.PARENT_WEB_TITLE, FeedType.CONTENT_FEED, SPType.SP2007);
    final SPDocument tmpdoc2 = new SPDocument("2", "HTTP://MYCOMP.COM",
        Calendar.getInstance(), SPConstants.NO_AUTHOR, SPConstants.NO_OBJTYPE,
        SPConstants.PARENT_WEB_TITLE, FeedType.CONTENT_FEED, SPType.SP2007);
    final List<SPDocument> testSet = new ArrayList<SPDocument>();
    testSet.add(tmpdoc1);
    testSet.add(tmpdoc2);
    Collections.sort(testSet);
    System.out.println(testSet);
  }

  public final void testFindProperty() throws Exception {
    final Property prop = this.doc.findProperty(SpiConstants.PROPNAME_DOCID);
    assertNotNull(prop);
  }

  public final void testDownloadContents() throws Exception {
    final SPDocument.SPContent content = this.doc.downloadContents();
    assertEquals(content.getStatus(), SPConstants.CONNECTIVITY_SUCCESS);
  }

  public final void testDownloadContentsForMsgFile() throws Exception {
    this.doc.setContentDwnldURL(TestConfiguration.Site1_List_Item_MSG_File_URL);
    final SPDocument.SPContent content = this.doc.downloadContents();
    assertEquals(content.getStatus(), SPConstants.CONNECTIVITY_SUCCESS);
    assertEquals("application/vnd.ms-outlook", content.getContentType());
  }

  public final void testStatusCodeForInvalidUrl() throws Exception {
    // A temporary test document is needed since we need to use a content
    // feed when retrieving the property HTTP_STATUS_CODE. If we used a
    // metadata-and-url, SPDocument will return a null for HTTP_STATUS_CODE.
    SPDocument testDoc = new SPDocument(doc.getDocId(), doc.getDisplayUrl(),
        doc.getLastMod(), doc.getAuthor(), doc.getObjType(),
        doc.getParentWebTitle(), FeedType.CONTENT_FEED, doc.getSPType());
    testDoc.setSharepointClientContext(doc.getSharepointClientContext());
    testDoc.setContentDwnldURL(TestConfiguration.Site1_List_Item_MSG_File_URL
        + ".invalid.txt");
    assertNull(testDoc.getContent());

    final Property prop = testDoc.findProperty(SPConstants.HTTP_STATUS_CODE);
    assertNotNull(prop);
    assertEquals("404", prop.nextValue().toString());
    assertNull(prop.nextValue());
  }

  public final void testDownloadContentsForInvalidUrl() throws Exception {
    this.doc.setContentDwnldURL(TestConfiguration.Site1_List_Item_MSG_File_URL
        + ".invalid.txt");
    final SPDocument.SPContent content = this.doc.downloadContents();
    assertEquals("404", content.getStatus());
    assertEquals(404, content.getStatusCode());
  }

  public void testGetPropertyNamesWithoutExcludedMetadata() throws Exception {
    Set<String> documentMetadata;
    documentMetadata = this.doc.getPropertyNames();
    assertTrue(documentMetadata.contains(SPConstants.PARENT_WEB_TITLE));
  }

  public void testGetPropertyNamesWithExcludedMetadata() throws Exception {
    this.doc.getSharepointClientContext().getExcluded_metadata().add(Pattern.compile(".*title$"));
    Set<String> documentMetadata = this.doc.getPropertyNames();
    assertFalse(documentMetadata.contains(SPConstants.PARENT_WEB_TITLE));
  }
}
