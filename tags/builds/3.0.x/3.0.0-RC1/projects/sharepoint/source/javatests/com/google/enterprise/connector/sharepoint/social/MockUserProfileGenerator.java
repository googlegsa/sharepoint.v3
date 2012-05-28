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

package com.google.enterprise.connector.sharepoint.social;

import com.google.enterprise.connector.sharepoint.generated.sp2010.userprofileservice.ContactData;
import com.google.enterprise.connector.sharepoint.generated.sp2010.userprofileservice.GetUserProfileByIndexResult;
import com.google.enterprise.connector.sharepoint.generated.sp2010.userprofileservice.Privacy;
import com.google.enterprise.connector.sharepoint.generated.sp2010.userprofileservice.PropertyData;
import com.google.enterprise.connector.sharepoint.generated.sp2010.userprofileservice.ValueData;

import java.util.Random;

/**
 * Mock user profile generator for testing.
 * 
 * @author tapasnay
 */
public class MockUserProfileGenerator implements UserProfileServiceGenerator {

  private static final String EXAMPLE_DOMAIN_PREFIX = "EXAMPLE\\";
  private static final String EXAMPLE_COM = "@example.com";
  GetUserProfileByIndexResult[] profiles;
  static String[] names = { "ramankk",
      "arunk",
      "rchandika",
      "shwetapathak",
      "arnavroy",
      "martincochran",
      "tapasnay",
      "shkumar" };
  static String[] skills = { "sql",
      "search",
      "javascript",
      "sharepoint",
      "accounting",
      "java",
      "C++",
      "python",
      "sql server" };
  static String[] titles = { "Software Engineer",
      "Software Engineer",
      "Manager",
      "Software Engineer",
      "Software Engineer",
      "Manager",
      "Software Engineer",
      "Software Engineer" };
  static String[] firstNames = { "Raman",
      "Arun",
      "Radha",
      "Shweta",
      "Arnav",
      "Martin",
      "Tapas",
      "Shailesh" };
  static String[] lastNames = { "Kumar",
      "P G",
      "Chandika",
      "Pathak",
      "Roy",
      "Cochran",
      "Nayak",
      "Kumar" };

  private final Random random;

  MockUserProfileGenerator() {
    random = new Random();
  }

  ValueData[] makeValue(String val) {
    return new ValueData[] { new ValueData(val) };
  }

  ValueData[] makeValue(String val1, String val2) {
    return new ValueData[] { new ValueData(val1), new ValueData(val2) };
  }

  ValueData[] makeValue(int howMany) {
    ValueData[] v = new ValueData[howMany + 1];
    v[0] = new ValueData("engineering");
    for (int i = 0; i < howMany; i++) {
      int next = random.nextInt(skills.length);
      v[i + 1] = new ValueData(skills[next]);
    }
    return v;
  }

  public void setUsername(String user) {
  }

  public void setPassword(String pass) {
  }

  public long getUserProfileCount() {
    return names.length;
  }

  public GetUserProfileByIndexResult getUserProfileByIndex(int index) {
    GetUserProfileByIndexResult profile = new GetUserProfileByIndexResult();
    PropertyData[] profileData = { 
        new PropertyData(false, false,
        SharepointSocialConstants.PROPNAME_RESPONSIBILITY, Privacy.Public,
        makeValue(1)),
        new PropertyData(false, false,
            SharepointSocialConstants.PROPNAME_PASTPROJECTS, Privacy.Public,
            makeValue(3)),
        new PropertyData(false, false,
            SharepointSocialConstants.PROPNAME_SKILL, Privacy.Public,
            makeValue(2)),
        new PropertyData(false, false,
            SharepointSocialConstants.SHAREPOINT_USERKEYDEFAULT,
            Privacy.Public, makeValue(EXAMPLE_DOMAIN_PREFIX + names[index])),
        new PropertyData(false, false,
            SharepointSocialConstants.SHAREPOINT_USERCONTENTDEFAULT,
            Privacy.Public, makeValue(names[index])),
        new PropertyData(false, false, (String) "UserName", Privacy.Public,
            makeValue(names[index])),
        new PropertyData(false, false, (String) "FirstName", Privacy.Public,
            makeValue(firstNames[index])),
        new PropertyData(false, false, (String) "LastName", Privacy.Public,
            makeValue(lastNames[index])),
        new PropertyData(false, false, (String) "Title", Privacy.Public,
            makeValue(titles[index])) };

    profile.setUserProfile(profileData);
    return profile;
  }

  public ContactData[] getUserColleagues(String key) {
    int number = random.nextInt(names.length - 2);
    ContactData[] colleagues = new ContactData[number];
    if (key.startsWith(EXAMPLE_DOMAIN_PREFIX))
      key = key.substring(7);

    // generate as many as "number" colleagues, picking them from the list of
    // names ignoring self
    for (int n = 0; (number > 0) && (n < names.length); n++) {
      if (!names[n].equalsIgnoreCase(key)) {
        ContactData contact = new ContactData();
        contact.setAccountName(EXAMPLE_DOMAIN_PREFIX + names[n]);
        contact.setEmail(names[n] + EXAMPLE_COM);
        colleagues[number - 1] = contact;
        number--;
      }
    }
    return colleagues;
  }

}
