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

package com.google.enterprise.connector.adgroups;

import com.google.common.base.Strings;
import com.google.enterprise.connector.adgroups.AdConstants.Method;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.ConnectorType;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.CommunicationException;
import javax.naming.NamingException;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class AdGroupsConnectorType implements ConnectorType {
  private static final Logger LOGGER =
      Logger.getLogger(AdDbUtil.class.getName());

  private List<String> keys = null;

  private String[] configKeys =
    { "hostname", "port", "method", "principal", "password" };
  private String config =
      "<tr><td colspan='2'><strong>Active Directory Server</strong></td></tr>"
      + "<tr><td>Hostname:</td>"
      + "<td><input type='text' name='hostname' value='$hostname' /></td></tr>"
      + "<tr><td>Port:</td>"
      + "<td><input type='text' name='port' value='$port' /></td></tr>"
      + "<tr><td>Method:</td>"
      + "<td><select name='method'><option $standard value='STANDARD'>Standard</option><option $ssl value='SSL'>SSL</option></select></td></tr>"
      + "<tr><td>Principal (e.g. DOMAIN\\username, userPrincipalName):</td>"
      + "<td><input type='text' name='principal' value='$principal' /></td></tr>"
      + "<tr><td>Password:</td>"
      + "<td><input type='password' name='password' value='$password' /></td></tr>";

  @Override
  public ConfigureResponse getConfigForm(Locale locale) {
    Map<String, String> conf = new HashMap<String, String>();
    for (String s : configKeys) {
      conf.put(s, "");
    }
    return getPopulatedConfigForm(conf, null);
  }

  @Override
  public ConfigureResponse getPopulatedConfigForm(Map<String, String> conf, Locale arg1) {
    String configuration = new String(config);
    for (String s : conf.keySet()) {
      if (s.equals("method")) {
        configuration = configuration.replace("$ssl", conf.get(s).equals("SSL") ? "selected='selected'" : "");
        configuration = configuration.replace("$standard", conf.get(s).equals("SSL") ? "" : "selected='selected'");
      } else {
        configuration = configuration.replace("$" + s, conf.get(s));
      }
    }
    return new ConfigureResponse("", configuration);
  }

  /**
   * Modifies HTML form to be displayed upon unsuccessful configuration 
   * @param form contains the form HTML
   * @param field after which we will add message
   * @param message description of the error
   * @param e exception to be logged
   */
  private void insertMessage(StringBuffer form, String field, String message,
      Throwable e) {
    if (e != null) {
      LOGGER.log(Level.CONFIG, message, e);
    }
    int position = form.indexOf("</tr>", form.indexOf("name='" + field)) + 5;
    form.insert(position, "<tr><td colspan='2'><strong><font color='red'>" 
        + message + "</font></strong></td></tr>").toString();
  }

  @Override
  public ConfigureResponse validateConfig(
      Map<String, String> conf, Locale arg1, ConnectorFactory arg2) {
    StringBuffer form = new StringBuffer(getPopulatedConfigForm(conf, null)
        .getFormSnippet());
    boolean connectivity = false;

    int port = 0;
    try {
      port = Integer.parseInt(conf.get("port"));
    } catch (NumberFormatException e) {
      insertMessage(form, "port", "Invalid port number", e);
    }

    String hostname = conf.get("hostname");
    try {
      Socket socket = new Socket();
      socket.connect(new InetSocketAddress(hostname, port), 3000);
      connectivity = true;
    } catch (IllegalArgumentException e) {
      insertMessage(form, "hostname", e.getMessage(), e);
    } catch (UnknownHostException e) {
      insertMessage(form, "hostname", "Can't resolve hostname", e);
    } catch (ConnectException e) {
      insertMessage(form, "hostname", "Connection refused", e);
    } catch (SocketTimeoutException e) {
      insertMessage(form, "hostname", "Connection too slow. " + hostname + ":"
          + port + " didn't reply within 3 seconds", e);
    } catch (IOException e) {
      insertMessage(form, "hostname", "Connection failed", e);
    }

    Method method =
        conf.get("method").equals("SSL") ? Method.SSL : Method.STANDARD;
    String principal = conf.get("principal");
    String password = conf.get("password");
    if (connectivity && method == Method.SSL) {
      SSLSocketFactory socketFactory =
          (SSLSocketFactory) SSLSocketFactory.getDefault();
      try {
        SSLSocket socket = (SSLSocket) socketFactory.createSocket();
        socket.connect(new InetSocketAddress(hostname, port), 3000);
        socket.setSoTimeout(3000);
        socket.getInputStream().read();
        insertMessage(form, "method",
            "Is this server Active Directory Controller?", null);
      } catch (SocketTimeoutException e) {
        // This is okay
      } catch (SSLException e) {
        insertMessage(form, "method",
            "Error enabling SSL. Certificate problem " + e.getMessage(), e);
      } catch (IOException e) {
        insertMessage(form, "method", "Error enabling SSL. " + e.getMessage(),
            e);
      }
    }

    // empty principal and password might be correct configuration if AD allows
    // anonymous users
    if (Strings.isNullOrEmpty(principal)) {
      insertMessage(form, "principal",
          "Active Directory doesn't allow anonymous binds", null);
    }
    if (Strings.isNullOrEmpty(password)) {
      insertMessage(form, "password", "Password is empty", null);
    }

    boolean success = false;
    if (connectivity) {
      AdServer server =
          new AdServer(method, hostname, port, principal, password);
      try {
        server.connect();
        success = true;
      } catch (CommunicationException e) {
        insertMessage(form, "principal",
            "Error authenticating to the AD. " + e.getMessage(), e);
      } catch (NamingException e) {
        if (e.getMessage().contains("80090308")) {
          insertMessage(form, "principal",
              "Invalid credentials. Username and/or password incorrect", e);
        } else {
          insertMessage(form, "principal", "Error authenticating to the AD. "
              + e.getMessage(), e);
        }
      }
    }

    return success ? null : new ConfigureResponse(
        "Error creating AD connector", form.toString(), conf); 
  }

  public void setConfigKeys(final List<String> inKeys) {
    if (keys != null) {
      throw new IllegalStateException();
    }
    if (inKeys != null) {
      keys = inKeys;
    }
  }

  public static void main(String args[]) throws Exception {
    AdGroupsConnectorType type = new AdGroupsConnectorType();

    System.out.println(type.getConfigForm(null).getFormSnippet());
  }
}
