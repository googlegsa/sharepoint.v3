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

package com.google.enterprise.connector.sharepoint.wsclient.handlers;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * A handler for SAX events. It forwards all of the requests to another handler. 
 * This class only checks for fatal errors. Currently, it ignores fatal parsing 
 * errors for duplication attributes.
 */
public class SaxErrorHandler extends DefaultHandler {
  private static final Logger LOGGER = Logger.getLogger(SaxErrorHandler.class.getName());
  private final DefaultHandler handler;

  public SaxErrorHandler(DefaultHandler dh) {
    handler = dh;
  }

  @Override
  public void fatalError(SAXParseException e) throws SAXParseException {
    String msg = e.getMessage();
    if (msg.indexOf("Attribute") == -1 && msg.indexOf("was already specified for element") == -1)
      throw e;
    LOGGER.info("Ignoring fatal SAX parsing error: " + msg);
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    handler.characters(ch, start, length);
  }

  @Override
  public void endDocument() throws SAXException {
    handler.endDocument();
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    handler.endElement(uri, localName, qName);
  }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException {
    handler.endPrefixMapping(prefix);
  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    handler.ignorableWhitespace(ch, start, length);
  }

  @Override
  public void notationDecl(String name, String publicId, String systemId) throws SAXException {
    handler.notationDecl(name, publicId, systemId);
  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException {
    handler.processingInstruction(target, data);
  }

  @Override
  public InputSource resolveEntity(String publicId, String systemId)
      throws SAXException, IOException {
    return handler.resolveEntity(publicId, systemId);
  }

  @Override
  public void setDocumentLocator(Locator locator) {
    handler.setDocumentLocator(locator);
  }

  @Override
  public void skippedEntity(String name) throws SAXException {
    handler.skippedEntity(name);
  }

  @Override
  public void startDocument() throws SAXException {
    handler.startDocument();
  }

  @Override
  public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws SAXException {
    handler.startElement(uri, localName, qName, attributes);
  }

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    handler.startPrefixMapping(prefix, uri);
  }

  @Override
  public void unparsedEntityDecl(String name, String publicId, String systemId,
      String notationName) throws SAXException {
    handler.unparsedEntityDecl(name, publicId, systemId, notationName);
  }

  @Override
  public void warning(SAXParseException e) {
  }

  @Override
  public void error(SAXParseException e) {
  }
}
