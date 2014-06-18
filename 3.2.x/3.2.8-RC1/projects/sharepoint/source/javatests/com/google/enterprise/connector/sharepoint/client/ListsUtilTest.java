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

package com.google.enterprise.connector.sharepoint.client;

import junit.framework.TestCase;

import org.apache.axis.message.MessageElement;

public class ListsUtilTest extends TestCase {
  public void testIsFeedableListItem() throws Exception {
    String NO_ATTRIBUTE = "<tag/>";
    String EMPTY_ATTRIBUTE = String.format("<tag %s=''/>",
        SPConstants.MODERATION_STATUS);
    String APPROVED = String.format("<tag %s='%s'/>",
        SPConstants.MODERATION_STATUS, SPConstants.DocVersion.APPROVED);
    String REJECTED = String.format("<tag %s='%s'/>",
        SPConstants.MODERATION_STATUS, SPConstants.DocVersion.REJECTED);

    assertIsFeedable(true, true, NO_ATTRIBUTE);
    assertIsFeedable(true, true, EMPTY_ATTRIBUTE);
    assertIsFeedable(true, true, APPROVED);
    assertIsFeedable(true, true, REJECTED);

    assertIsFeedable(true, false, NO_ATTRIBUTE);
    assertIsFeedable(true, false, EMPTY_ATTRIBUTE);
    assertIsFeedable(true, false, APPROVED);
    assertIsFeedable(false, false, REJECTED);
  }

  private void assertIsFeedable(boolean expected,
      boolean isFeedUnpublishedDocuments, String tag) throws Exception {
    String URL = "http://..."; // Only used for logging.
    assertEquals(isFeedUnpublishedDocuments + ", " + tag, expected,
        ListsUtil.isFeedableListItem(isFeedUnpublishedDocuments,
            ListsUtil.getMeFromString(tag), URL));
  }
}
