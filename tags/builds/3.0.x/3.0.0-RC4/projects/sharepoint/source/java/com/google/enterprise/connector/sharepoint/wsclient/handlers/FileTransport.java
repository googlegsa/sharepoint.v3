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

import org.apache.axis.client.Transport;

/**
 * Sets the transport name to be "FileTransport". The WSDD file for the client
 * needs to specify a transport node with the name "FileTransport" and using the
 * pivot as FileSender.
 */
public class FileTransport extends Transport {
  private static String responseFileName;

  public static String getResponseFileName() {
    return responseFileName;
  }

  public static void setResponseFileName(String fileName) {
    responseFileName = fileName;
  }

  public FileTransport() {
    transportName = "FileTransport";
  }
}
