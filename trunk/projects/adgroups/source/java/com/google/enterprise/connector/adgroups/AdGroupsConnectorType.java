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

import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.ConnectorType;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdGroupsConnectorType implements ConnectorType {

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
      + "<tr><td>Princial (e.g. DOMAIN\\username, userPrincipalName):</td>"
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

  @Override
  public ConfigureResponse validateConfig(
      Map<String, String> arg0, Locale arg1, ConnectorFactory arg2) {
    return null;
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
