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

package com.google.enterprise.connector.sharepoint;

import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Dump the Version info from the Manifest for the Connector's JAR file.
 * This is set as the default main() for the JAR if running the jar
 * stand-alone.  This makes it easy to dump the Connector's JAR Manifest
 * (including version and build info) simply by running the command:
 *   java -jar /path/to/connector-sharepoint.jar
 */
public class SharePointMain {
  public static void main(String[] args) throws Exception {
    // From our class, get the jar file URL to this class file, and
    // make our way to the the Manifest located in that jar file.
    Class<?> thisClass = SharePointMain.class;
    String resName = "/" + thisClass.getName().replace('.', '/') + ".class";

    // Locate the Jar file containing our class.
    URL url = thisClass.getResource(resName);
    JarURLConnection connection = (JarURLConnection) url.openConnection();

    // Get the Manifest for our Jar and extract the Implementation-Title
    // and Implementation-Version.
    Manifest manifest = connection.getManifest();
    Attributes attrs = manifest.getMainAttributes();
    String name = attrs.getValue("Implementation-Title");
    if (name != null) {
      name = name.replaceAll("[ \t\r\n][ \t\r\n]+", " ");
    }
    String version = attrs.getValue("Implementation-Version");

    System.out.println(name + " v" + version);
  }
}
