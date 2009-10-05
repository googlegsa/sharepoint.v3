//Copyright 2007 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.spiimpl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.spi.Property;
import com.sun.jndi.toolkit.url.UrlUtil;

public class SPDocumentTest extends TestCase {

    SPDocument doc;

    protected void setUp() throws Exception {
        super.setUp();
        final String docURL = "http://myHost.myDomain:port/path/";
        this.doc = new SPDocument("docID", docURL, Calendar.getInstance(),
                SPConstants.NO_AUTHOR, SPConstants.NO_OBJTYPE,
                SPConstants.PARENT_WEB_TITLE, SPConstants.CONTENT_FEED,
                SPConstants.SP2007);
        try {
            String str = UrlUtil.encode(docURL, "UTF-8");
            String charset = new GetMethod(str).getParams().getUriCharset();
            URI uri = new URI(docURL, true, charset);
            System.out.println(str);
            System.out.println(uri.toString());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void testCompare() {
        final SPDocument tmpdoc1 = new SPDocument("1", "HTTP://MYCOMP.COM",
                Calendar.getInstance(), SPConstants.NO_AUTHOR,
                SPConstants.NO_OBJTYPE, SPConstants.PARENT_WEB_TITLE,
                SPConstants.CONTENT_FEED, SPConstants.SP2007);
        final SPDocument tmpdoc2 = new SPDocument("2", "HTTP://MYCOMP.COM",
                Calendar.getInstance(), SPConstants.NO_AUTHOR,
                SPConstants.NO_OBJTYPE, SPConstants.PARENT_WEB_TITLE,
                SPConstants.CONTENT_FEED, SPConstants.SP2007);
        final List<SPDocument> testSet = new ArrayList<SPDocument>();
        testSet.add(tmpdoc1);
        testSet.add(tmpdoc2);
        Collections.sort(testSet);
        System.out.println(testSet);
    }

    public final void testFindProperty() {
        System.out.println("Testing findProperty()..");
        try {
            final Property prop = this.doc.findProperty("docID");
            assertNotNull(prop);
            System.out.println("[ findProperty(() ] Test passd");
        } catch (final Exception e) {
            System.out.println("[ findProperty(() ] Test failed");
        }
    }

    /*
     * public final void testDownloadContents() {
     * System.out.println("Testing downloadContents().."); try { final
     * SharepointClientContext sharepointClientContext = new
     * SharepointClientContext(TestConfiguration.sharepointUrl,
     * TestConfiguration.domain, TestConfiguration.username,
     * TestConfiguration.Password, TestConfiguration.googleConnectorWorkDir,
     * TestConfiguration.includedURls, TestConfiguration.excludedURls,
     * TestConfiguration.mySiteBaseURL, TestConfiguration.AliasMap,
     * TestConfiguration.feedType);
     *
     * final String responseCode =
     * this.doc.downloadContents(sharepointClientContext);
     * assertEquals(responseCode, "200"); } catch (final Exception e) {
     * assertTrue(false); }
     * System.out.println("[ downloadContents(() ] Test passd"); }
     */
}
