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

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sets the body of an Axis message response to be the contents of a file. 
 * This is used in conjunction with FileTransport.
 */
public class FileSender extends BasicHandler {
  private static final Logger LOGGER = Logger.getLogger(FileSender.class.getName());

  @Override
  public void invoke(MessageContext msgContext) throws AxisFault {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(FileTransport.getResponseFileName());
      Message msg = new Message(fis);
      // Call msg.getSOAPPartAsBytes to read the fis contents.
      msg.getSOAPPartAsBytes();
      msgContext.setResponseMessage(msg);
    } catch (IOException e) {
      new AxisFault(e.getMessage(), e);
    } finally {
      if (null != fis) {
        try {
          fis.close();
        } catch (Exception e) {
          LOGGER.log(Level.FINE, "Unable to read SOAP response file '" +
              FileTransport.getResponseFileName() + "'.", e);
        }
      }
    }
  }
}
