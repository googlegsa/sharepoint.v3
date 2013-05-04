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

import com.google.common.annotations.VisibleForTesting;
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

  static final String[] names = {
      "ramankk",
      "arunk",
      "rchandika",
      "shwetapathak",
      "arnavroy",
      "martincochran",
      "tapasnay",
      "shkumar" };
  static final String[] skills = {
      "sql",
      "search",
      "javascript",
      "sharepoint",
      "accounting",
      "java",
      "C++",
      "python",
      "sql server" };
  static final String[] titles = {
      "Software Engineer",
      "Software Engineer",
      "Manager",
      "Software Engineer",
      "Software Engineer",
      "Manager",
      "Software Engineer",
      "Software Engineer" };
  static final String[] firstNames = {
      "Raman",
      "Arun",
      "Radha",
      "Shweta",
      "Arnav",
      "Martin",
      "Tapas",
      "Shailesh" };
  static final String[] lastNames = {
      "Kumar",
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


  @VisibleForTesting
  int getNextValue(int arrayIndex) {
    return (arrayIndex + 1 >= names.length) ? -1 : arrayIndex * 2 + 4;
  }

  @VisibleForTesting
  int getArrayIndex(int profileIndex) {
    return (profileIndex <= 2) ? 0 : (profileIndex - 1) / 2;
  }

  public GetUserProfileByIndexResult getUserProfileByIndex(int index) {
    // Create a sparse map of indexes: 2, 4, 6, etc. As with SharePoint,
    // asking for a missing entry returns the next higher one. We skip
    // index 0 just to be mean.
    int arrayIndex = getArrayIndex(index);

    GetUserProfileByIndexResult profile = new GetUserProfileByIndexResult();
    profile.setUserProfile(getProfileData(arrayIndex));
    profile.setNextValue(Integer.toString(getNextValue(arrayIndex)));
    return profile;
  }

  /** @param index a zero-based index into the private data arrays */
  private PropertyData[] getProfileData(int index) {
    return new PropertyData[] {
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
        new PropertyData(false, false, "UserName", Privacy.Public,
            makeValue(names[index])),
        new PropertyData(false, false, "FirstName", Privacy.Public,
            makeValue(firstNames[index])),
        new PropertyData(false, false, "LastName", Privacy.Public,
            makeValue(lastNames[index])),
        new PropertyData(false, false, "Title", Privacy.Public,
            makeValue(titles[index])) };
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

  @Override
  public PropertyData[] getUserProfileByName(String accountName) {    
    for (int i = 0; i < names.length; i++) {
      if (names[i].equalsIgnoreCase(accountName)) {
        return getProfileData(i);
      }
    }
    return null;
  }

}
