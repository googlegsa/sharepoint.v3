// Copyright 2013 Google Inc.
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

package com.google.enterprise.connector.sharepoint.social;

import com.google.enterprise.connector.sharepoint.generated.sp2010.userprofileservice.PropertyData;
import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.wsclient.handlers.FileTransport;
import com.google.enterprise.connector.spi.SocialUserProfileDocument;

import junit.framework.TestCase;

import org.apache.axis.client.Call;
import org.apache.axis.transport.http.HTTPTransport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class SharepointUserProfileConnectionTest extends TestCase {
  SharepointUserProfileConnection connection;
  TimeZone defaultTimeZone;

  protected void setUp() throws Exception {
    SharepointSocialClientContext socialContext =
        TestConfiguration.initSocialContext(TestConfiguration.initContext());
    assertNotNull(socialContext);
    connection = new SharepointUserProfileConnection(socialContext);

    defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));

    // A call to Call.initialize is required to reset the Axis HTTP transport
    // back to the orginal set by Axis.
    Call.initialize();
  }

  public void tearDown() throws Exception {
    TimeZone.setDefault(defaultTimeZone);

    // Reset the Axis transport protocol back to HTTP. This is needed
    // since some tests change the Axis transport protocol to a file.
    Call.setTransportForProtocol("http", HTTPTransport.class);
  }

  /**
   * Tests that user profile dates are handled correctly.
   */
  public void testUserProfileDates() throws Exception {
    Call.setTransportForProtocol("http", FileTransport.class);

    // SharepointUserProfileConnection.openConnection makes a web request to
    // UserProfileServiceGenerator.getUserProfileCount so we need to set the
    // correct response file.
    FileTransport.setResponseFileName(
        "source/javatests/data/single-userprofile.xml");
    connection.openConnection();

    FileTransport.setResponseFileName(
        "source/javatests/data/userprofile-dates.xml");

    PropertyData[] props = connection.getUserProfileByName("dummy");
    assertNotNull(props);

    verifyCalendarProperty(props, "SPS-HireDate", "2012-06-19", "2012-06-20");

    verifyCalendarProperty(props, "SPS-LastColleagueAdded",
        "2013-01-24", "2013-01-25");

    verifyCalendarProperty(props, "SPS-LastKeywordAdded",
        "2013-01-25", "2013-01-26");
  }

  private void verifyCalendarProperty(PropertyData[] props, String propName,
      String expectedZone1, String expectedZone2) {
    verifyCalendarProperty("America/Los_Angeles", props, propName,
        expectedZone1, expectedZone2);
    verifyCalendarProperty("America/New_York", props, propName,
        expectedZone1, expectedZone1);
    verifyCalendarProperty("Asia/Kolkata", props, propName, expectedZone2,
        expectedZone1);
    verifyCalendarProperty("UTC", props, propName, expectedZone2,
        expectedZone1);
  }

  private void verifyCalendarProperty(String timeZone, PropertyData[] props,
      String propName, String expectedValue, String expectedValueForGet) {
    TimeZone.setDefault(TimeZone.getTimeZone(timeZone));

    PropertyData prop = getProperty(props, propName);
    assertNotNull(prop);
    Calendar cal = (Calendar) prop.getValues()[0].getValue();
    assertNotNull(cal);
    ArrayList<String> values =
        SharepointUserProfileConnection.readProfileProperty(prop);
    assertNotNull(values);
    assertEquals(1, values.size());
    assertEquals(expectedValueForGet, values.get(0));

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    assertEquals(expectedValue, sdf.format(cal.getTime()));
  }

  private PropertyData getProperty(PropertyData[] props, String propName) {
    for (PropertyData prop : props) {
      if (propName.equals(prop.getName())) {
        return prop;
      }
    }
    return null;
  }
}
