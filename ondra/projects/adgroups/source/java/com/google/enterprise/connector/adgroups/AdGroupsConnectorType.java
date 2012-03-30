// Copyright 2012 Google Inc. All Rights Reserved.

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

  @Override
  public ConfigureResponse getConfigForm(Locale locale) {
    StringBuilder sb = new StringBuilder();
    //for (int i = 0; i < 3; ++i) 
    int i = 0;
    {
      sb.append("<tr><td colspan='2'><strong>Active Directory Server ").append(i+1).append("</strong></td></tr>")
        .append("<tr><td>Hostname:</td>")
        .append("<td><input type='text' name='hostname_").append(i).append("' value='' /></td></tr>")
        .append("<tr><td>Port:</td>")
        .append("<td><input type='text' name='port_").append(i).append("' value='' /></td></tr>")
        .append("<tr><td>Method:</td>")
        .append("<td><select name='method_").append(i).append("'><option value='STANDARD'>Standard</option><option value='SSL'>SSL</option></select></td></tr>")
        .append("<tr><td>Username:</td>")
        .append("<td><input type='text' name='username_").append(i).append("' value='' /></td></tr>")
        .append("<tr><td>Password:</td>")
        .append("<td><input type='text' name='password_").append(i).append("' value='' /></td></tr>")
        .append("<tr><td>Domain:</td>")
        .append("<td><input type='text' name='domain_").append(i).append("' value='' /></td></tr>");
    }

    return new ConfigureResponse("", sb.toString(), new HashMap<String, String>(){{
      put("ahoj", "bhoj");
    }});
  }

  @Override
  public ConfigureResponse getPopulatedConfigForm(Map<String, String> conf, Locale arg1) {
    for (String s : conf.keySet()) {
      System.out.println(s + ": " + conf.get(s));
    }
    return null;
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
