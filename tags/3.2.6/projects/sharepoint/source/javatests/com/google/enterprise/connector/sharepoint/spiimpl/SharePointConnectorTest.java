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

package com.google.enterprise.connector.sharepoint.spiimpl;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.spi.RepositoryException;

import junit.framework.TestCase;

public class SharePointConnectorTest extends TestCase {

  public void testInit() throws RepositoryException {
    assertNotNull(TestConfiguration.getConnectorInstance());
  }

  public void testReWriteDisplayUrl_default() throws RepositoryException {
    SharepointConnector connector = TestConfiguration.createConnectorInstance();
    connector.init();
    SharepointClientContext context = connector.getSharepointClientContext();
    assertTrue(context.isReWriteDisplayUrlUsingAliasMappingRules());
  }

  public void testReWriteDisplayUrl_false() throws RepositoryException {
    SharepointConnector connector = TestConfiguration.createConnectorInstance();
    connector.setReWriteDisplayUrlUsingAliasMappingRules(false);
    connector.init();
    SharepointClientContext context = connector.getSharepointClientContext();
    assertFalse(context.isReWriteDisplayUrlUsingAliasMappingRules());
  }

  public void testReWriteRecordUrl_default() throws RepositoryException {
    SharepointConnector connector = TestConfiguration.createConnectorInstance();
    connector.init();
    SharepointClientContext context = connector.getSharepointClientContext();
    assertFalse(context.isReWriteRecordUrlUsingAliasMappingRules());
  }

  public void testReWriteRecordUrl_true() throws RepositoryException {
    SharepointConnector connector = TestConfiguration.createConnectorInstance();
    connector.setReWriteRecordUrlUsingAliasMappingRules(true);
    connector.init();
    SharepointClientContext context = connector.getSharepointClientContext();
    assertTrue(context.isReWriteRecordUrlUsingAliasMappingRules());
  }
}
