// Copyright 2012 Google Inc.
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

package com.google.enterprise.connector.sharepoint.wsclient.mock;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Logger;

/**
 * A XML handler used for parse XML with SAX.
 */
class XmlHandler extends DefaultHandler {
  private static final Logger LOGGER =
      Logger.getLogger(XmlHandler.class.getName());

  private Stack<MockItem> items;

  public XmlHandler() {
    items = new Stack<MockItem>();
    items.push(new MockItem("", MockType.Web, null));
  }

  public MockItem getRoot() {
    return items.peek();
  }

  @Override
  public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws SAXException {
    MockType type = getTypeFromString(qName);
    if (MockType.ACL == type) {
      String name = attributes.getValue("name");
      MockItem topItem = items.peek();
      topItem.addAcl(name);
    } else if (MockType.Unknown != type) {
      String name = attributes.getValue("name");
      if (null == name) {
        name = type + "-" + System.currentTimeMillis();
      }

      MockItem topItem = items.peek();
      MockItem newItem = new MockItem(name, type, topItem);
      items.push(newItem);
      topItem.addChild(newItem);
    }
  }

  @Override
  public void endElement(String uri, String localName,
      String qName) throws SAXException {
    MockType type = getTypeFromString(qName);
    if ((MockType.Unknown != type) && (MockType.ACL != type)) {
      items.pop();
    }
  }

  private MockType getTypeFromString(String type) {
    type = type.toLowerCase();
    if (type.equals("web")) {
      return MockType.Web;
    } else if (type.equals("list")) {
      return MockType.List;
    } else if (type.equals("folder")) {
      return MockType.Folder;
    } else if (type.equals("document")) {
      return MockType.Document;
    } else if (type.equals("acl")) {
      return MockType.ACL;
    } else {
      return MockType.Unknown;
    }
  }
}
