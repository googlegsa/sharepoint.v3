// Copyright 2012 Google Inc.
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

import org.apache.xerces.jaxp.SAXParserFactoryImpl;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

/**
 * This is a SAX parser factory. This class returns our SAX error parser that
 * handles fatal errors and forwards all other requests to the Axis default SAX parser.
 */
public class SaxErrorFactory extends SAXParserFactoryImpl {
  @Override
  public SAXParser newSAXParser() throws ParserConfigurationException {
    return new SaxErrorParser(super.newSAXParser());
  }
}
