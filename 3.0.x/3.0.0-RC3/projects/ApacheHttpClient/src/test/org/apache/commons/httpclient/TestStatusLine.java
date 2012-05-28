/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/TestStatusLine.java,v 1.9 2004/07/19 20:24:21 olegk Exp $
 * $Revision: 480424 $
 * $Date: 2006-11-29 11:26:49 +0530 (Wed, 29 Nov 2006) $
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.commons.httpclient;

import junit.framework.*;

/**
 * Simple tests for {@link StatusLine}.
 *
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @version $Id: TestStatusLine.java 480424 2006-11-29 05:56:49Z bayard $
 */
public class TestStatusLine extends TestCase {

    private StatusLine statusLine = null;

    // ------------------------------------------------------------ Constructor
    public TestStatusLine(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestStatusLine.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestStatusLine.class);
    }

    // ------------------------------------------------------ Protected Methods


    // ----------------------------------------------------------- Test Methods

    public void testIfStatusLine() throws Exception {
        assertTrue(StatusLine.startsWithHTTP("HTTP"));
        assertTrue(StatusLine.startsWithHTTP("         HTTP"));
        assertTrue(StatusLine.startsWithHTTP("\rHTTP"));
        assertTrue(StatusLine.startsWithHTTP("\tHTTP"));
        assertFalse(StatusLine.startsWithHTTP("crap"));
        assertFalse(StatusLine.startsWithHTTP("HTT"));
        assertFalse(StatusLine.startsWithHTTP("http"));
    }

    public void testSuccess() throws Exception {
        //typical status line
        statusLine = new StatusLine("HTTP/1.1 200 OK");
        assertEquals("HTTP/1.1 200 OK", statusLine.toString());
        assertEquals("HTTP/1.1", statusLine.getHttpVersion());
        assertEquals(200, statusLine.getStatusCode());
        assertEquals("OK", statusLine.getReasonPhrase());

        //status line with multi word reason phrase
        statusLine = new StatusLine("HTTP/1.1 404 Not Found");
        assertEquals(404, statusLine.getStatusCode());
        assertEquals("Not Found", statusLine.getReasonPhrase());

        //reason phrase can be anyting
        statusLine = new StatusLine("HTTP/1.1 404 Non Trouve");
        assertEquals("Non Trouve", statusLine.getReasonPhrase());

        //its ok to end with a \n\r
        statusLine = new StatusLine("HTTP/1.1 404 Not Found\r\n");
        assertEquals("Not Found", statusLine.getReasonPhrase());

        //this is valid according to the Status-Line BNF
        statusLine = new StatusLine("HTTP/1.1 200 ");
        assertEquals(200, statusLine.getStatusCode());
        assertEquals("", statusLine.getReasonPhrase());

        //this is not strictly valid, but is lienent
        statusLine = new StatusLine("HTTP/1.1 200");
        assertEquals(200, statusLine.getStatusCode());
        assertEquals("", statusLine.getReasonPhrase());

        //this is not strictly valid, but is lienent
        statusLine = new StatusLine("HTTP/1.1     200 OK");
        assertEquals(200, statusLine.getStatusCode());
        assertEquals("OK", statusLine.getReasonPhrase());

        //this is not strictly valid, but is lienent
        statusLine = new StatusLine("\rHTTP/1.1 200 OK");
        assertEquals(200, statusLine.getStatusCode());
        assertEquals("OK", statusLine.getReasonPhrase());
        assertEquals("HTTP/1.1", statusLine.getHttpVersion());

        //this is not strictly valid, but is lienent
        statusLine = new StatusLine("  HTTP/1.1 200 OK");
        assertEquals(200, statusLine.getStatusCode());
        assertEquals("OK", statusLine.getReasonPhrase());
        assertEquals("HTTP/1.1", statusLine.getHttpVersion());
    }

    public void testFailure() throws Exception {
        try {
            statusLine = new StatusLine(null);
            fail();
        } catch (NullPointerException e) { /* expected */ }

        try {
            statusLine = new StatusLine("xxx 200 OK");
            fail();
        } catch (HttpException e) { /* expected */ }

        try {
            statusLine = new StatusLine("HTTP/1.1 xxx OK");
            fail();
        } catch (HttpException e) { /* expected */ }

        try {
            statusLine = new StatusLine("HTTP/1.1    ");
            fail();
        } catch (HttpException e) { /* expected */ }
    }

}
