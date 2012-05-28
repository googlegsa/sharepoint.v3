// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.wsclient.handlers;

import org.apache.axis.MessageContext;

import junit.framework.TestCase;

public class InvalidXmlCharacterHandlerTest extends TestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  /**
   * Test for handling of character entity references
   */
  public void testInvalidReferenceHandler() {
    int ref1 = 10, ref2 = 11;
    String charRef1 = "&#10;", hexCharRef1 = "&#xA;";
    String charRef2 = "&#11;", hexCharRef2 = "&#xB;";

    String str = "ILOG" + charRef1 + " in" + charRef2 + "Lending" + hexCharRef1
        + " &amp; " + hexCharRef2 + "Credit";

    InvalidXmlCharacterHandler handler = new InvalidXmlCharacterHandler();
    str = handler.filterInvalidReferences(str);

    if (InvalidXmlCharacterHandler.isInvalidReference(ref1)) {
      assertFalse(str.contains(charRef1));
      assertFalse(str.contains(hexCharRef1));
    } else {
      assertTrue(str.contains(charRef1));
      assertTrue(str.contains(hexCharRef1));
    }

    if (InvalidXmlCharacterHandler.isInvalidReference(ref2)) {
      assertFalse(str.contains(charRef2));
      assertFalse(str.contains(hexCharRef2));
    } else {
      assertTrue(str.contains(charRef2));
      assertTrue(str.contains(hexCharRef2));
    }
  }

  /**
   * Test for handling of custom patterns
   */
  public void testCustomPatternHandler() {
    MessageContext msgContext = new MessageContext(null);
    msgContext.setProperty("FilterPattern_1", "ows_");
    msgContext.setProperty("FilterPattern_2", "_x20_");

    String msgPayload = "ows_Author_x20_Name=self";
    InvalidXmlCharacterHandler handler = new InvalidXmlCharacterHandler();

    handler.initPatterns(msgContext);

    msgPayload = handler.filterCustomPatterns(msgPayload);
    assertFalse(msgPayload.contains("ows_"));
    assertFalse(msgPayload.contains("_x20_"));
  }
}
