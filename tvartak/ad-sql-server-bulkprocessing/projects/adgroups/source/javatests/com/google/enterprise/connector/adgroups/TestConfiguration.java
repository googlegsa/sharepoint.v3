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

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

public class TestConfiguration {
  public static String oracleUser;
  public static String oraclePassword;
  public static String oracleUrl;

  public static String mssqlUser;
  public static String mssqlPassword;
  public static String mssqlUrl;

  public static String h2User;
  public static String h2Password;
  public static String h2Url;

  public static String d1hostname;
  public static int d1port;
  public static int d1plaintextport;
  public static String d1principal;
  public static String d1upn;
  public static String d1password;

  public static String d2hostname;
  public static int d2port;
  public static String d2principal;
  public static String d2password;

  public static String d3hostname;
  public static int d3port;
  public static String d3principal;
  public static String d3password;

  public static boolean prepared;
  public static String testOu;
  public static int seed;
  public static int groupsPerDomain;
  public static int usersPerDomain;
  public static String password;

  public static String alphabet;
  public static String sAMAccountUsernameAlphabet;
  public static String sAMAccountGroupnameAlphabet;

  public static Map<String, DataSource> dbs;

  static {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < 32768; ++i) {
      if (Character.isValidCodePoint(i)
          && !Character.isISOControl(i)
          && Character.isLetter(i)
          && (UnicodeBlock.of(i) == UnicodeBlock.BASIC_LATIN
              // TODO: establish the correct alphabet supported by AD
              // || UnicodeBlock.of(i) == UnicodeBlock.LATIN_1_SUPPLEMENT
              // || UnicodeBlock.of(i) == UnicodeBlock.LATIN_EXTENDED_A
              // || UnicodeBlock.of(i) == UnicodeBlock.LATIN_EXTENDED_B
             ))
        sb.append(Character.toChars(i));
    }
    alphabet = sb.toString();
    System.out.println(alphabet);
    sAMAccountGroupnameAlphabet = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJK"
        +"LMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
    sAMAccountGroupnameAlphabet = "_0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdef"
        + "ghijklmnopqrstuvwxyz";
    sAMAccountUsernameAlphabet = "_0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefg"
        + "hijklmnopqrstuvwxyz";

    final Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(
          "source/javatests/TestConfig.properties"));
    } catch (final IOException e) {
      System.out.println("Unable to load the property file." + e);
    }

    for (Object s : properties.keySet()) {
      try {
        Field f = TestConfiguration.class.getDeclaredField(s.toString());
        if (f.getType().equals(int.class)) {
          f.set(null, Integer.parseInt((String) properties.get(s)));  
        } else if (f.getType().equals(boolean.class)) {
          f.set(null, Boolean.parseBoolean((String) properties.get(s)));
        } else {
          f.set(null, properties.get(s));
        }
      } catch (IllegalAccessException e) {
        System.out.println("Unable to set field value " + e);
      } catch (NoSuchFieldException e) {
        System.out.println("Unable to set field value " + e);
      }
    }
    dbs = new HashMap<String, DataSource>();
    DriverManagerDataSource dmds = new DriverManagerDataSource();
    dmds.setDriverClassName("oracle.jdbc.pool.OracleDataSource");
    dmds.setUrl(TestConfiguration.oracleUrl);
    dmds.setUsername(TestConfiguration.oracleUser);
    dmds.setPassword(TestConfiguration.oraclePassword);
    dbs.put("oracle", dmds);

    dmds = new DriverManagerDataSource();
    dmds.setDriverClassName(
        "com.microsoft.sqlserver.jdbc.SQLServerDataSource");
    dmds.setUrl(TestConfiguration.mssqlUrl);
    dmds.setUsername(TestConfiguration.mssqlUser);
    dmds.setPassword(TestConfiguration.mssqlPassword);
    dbs.put("sqlserver", dmds);

    dmds = new DriverManagerDataSource();
    dmds.setDriverClassName("org.h2.jdbcx.JdbcDataSource");
    dmds.setUrl(TestConfiguration.h2Url);
    dmds.setUsername(TestConfiguration.h2User);
    dmds.setPassword(TestConfiguration.h2Password);
    dbs.put("h2", dmds);
  }

  /**
  * Returns an instance of {@link AdGroupsConnector} for testing purpose
  *
  * @return Instance of {@link AdGroupsConnector}
  */
  public static AdGroupsConnector getConnectorInstance() {
    AdGroupsConnector connector = new AdGroupsConnector();
    return connector;
  }
}
