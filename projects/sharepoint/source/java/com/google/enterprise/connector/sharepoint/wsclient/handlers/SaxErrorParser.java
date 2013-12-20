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

import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;

/**
 * This class wraps a SAX parser and forwards all of the calls to the parser.
 * The two calls it intercepts is parse so that it can include an error handler
 * and getXMLReader to include the feature to that signals continue on fatal
 * error.
 */
public class SaxErrorParser extends SAXParser {
  private static final Logger LOGGER = Logger.getLogger(SaxErrorParser.class.getName());
  SAXParser parser;

  public SaxErrorParser(SAXParser parser) {
    this.parser = parser;
  }

  @Override
  public void parse(InputStream is, DefaultHandler dh) throws SAXException, IOException {
    parser.parse(is, new SaxErrorHandler(dh));
  }

  @Override
  public void parse(InputSource is, DefaultHandler dh) throws SAXException, IOException {
    parser.parse(is, new SaxErrorHandler(dh));
  }

  @Override
  public Object getProperty(String name)
      throws SAXNotRecognizedException, SAXNotSupportedException {
    return parser.getProperty(name);
  }

  @Override
  public void setProperty(String name, Object value)
      throws SAXNotRecognizedException, SAXNotSupportedException {
    parser.setProperty(name, value);
  }

  @Override
  public XMLReader getXMLReader() throws SAXException {
    XMLReader reader = parser.getXMLReader();
    try {
      reader.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
    } catch (SAXNotRecognizedException e) {
      LOGGER.warning("Unable to set continue-after-fatal-error feature for XMLReader. "
          + "This will disable monitoring of fatal SAX parsing errors.");
    } catch (SAXNotSupportedException e) {
      LOGGER.warning("Unable to set continue-after-fatal-error feature for XMLReader. "
          + "This will disable monitoring of fatal SAX parsing errors.");
    }
    return reader;
  }

  @Override
  public Parser getParser() throws SAXException {
    return parser.getParser();
  }

  @Override
  public boolean isNamespaceAware() {
    return parser.isNamespaceAware();
  }

  @Override
  public boolean isValidating() {
    return parser.isValidating();
  }
}
