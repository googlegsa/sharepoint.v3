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

package com.google.enterprise.connector.sharepoint.client;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.wsclient.mock.MockClientFactory;
import com.google.enterprise.connector.spi.SimpleTraversalContext;

import junit.framework.TestCase;

public class SharePointClientContextTest extends TestCase {

  SharepointClientContext sharepointClientContext;

  protected void setUp() throws Exception {
    sharepointClientContext = TestConfiguration.initContext();
    assertNotNull(this.sharepointClientContext);
  }
  
  protected SimpleTraversalContext setSupportsInheritedAcls(
      boolean supportsInheritedAcls) {
    SimpleTraversalContext traversalContext = new SimpleTraversalContext();
    traversalContext.setSupportsInheritedAcls(supportsInheritedAcls);
    return traversalContext;
}

  public void testClone() throws Exception {
    final SharepointClientContext spc =
        (SharepointClientContext) sharepointClientContext.clone();
    assertNotSame(sharepointClientContext, spc);
  }

  public void testCheckConnectivity() throws Exception {
    final int responseCode = sharepointClientContext.checkConnectivity(
        TestConfiguration.sharepointUrl, null);
    assertEquals(responseCode, 200);
  }

  public void testCheckSharePointType() throws Exception {
    final SPType spType = sharepointClientContext.checkSharePointType(
        TestConfiguration.sharepointUrl);
    assertEquals(SPType.SP2007, spType);
  }

  public void testIsIncludeMetadata() throws Exception {
    final boolean include =
        sharepointClientContext.isIncludeMetadata("file type");
    assertTrue(include);
  }

  public void testIsIncludedUrl() throws Exception {
    final boolean include = sharepointClientContext.isIncludedUrl(
        TestConfiguration.sharepointUrl);
    assertTrue(include);
  }

  public void testLogExcludedURL() throws Exception {
    sharepointClientContext.logExcludedURL("testLog");
  }

  public void testRemoveExcludedURLLogs() throws Exception {
    sharepointClientContext.clearExcludedURLLogs();
  }
  
  public void testGetAclBatchSizeNoBatchingInheritedAcls() throws Exception {
    SharepointClientContext spc 
        = new SharepointClientContext(new MockClientFactory());
    spc.setTraversalContext(setSupportsInheritedAcls(true));
    spc.setPushAcls(true);
    spc.setAclBatchSizeFactor(10);
    spc.setFetchACLInBatches(false);
    
    assertEquals(-1, spc.getAclBatchSize());    
  }
  
  public void testGetAclBatchSizeFlattenedAcls() throws Exception {
    SharepointClientContext spc 
        = new SharepointClientContext(new MockClientFactory());
    spc.setTraversalContext(setSupportsInheritedAcls(false));   
    spc.setPushAcls(true);
    spc.setAclBatchSizeFactor(10);
    spc.setFetchACLInBatches(false);
    
    assertEquals(50, spc.getAclBatchSize());    
  }
  
  public void testGetAclBatchSizeFlattenedAclsNoBatching() throws Exception {
    SharepointClientContext spc 
        = new SharepointClientContext(new MockClientFactory());
    spc.setTraversalContext(setSupportsInheritedAcls(false));    
    spc.setPushAcls(true);
    spc.setAclBatchSizeFactor(0);
    spc.setFetchACLInBatches(false);
    
    assertEquals(-1, spc.getAclBatchSize());    
  }
  
  public void testGetAclBatchSizeBatching() throws Exception {
    SharepointClientContext spc 
        = new SharepointClientContext(new MockClientFactory());
    spc.setTraversalContext(setSupportsInheritedAcls(true));   
    spc.setPushAcls(true);
    spc.setAclBatchSizeFactor(10);
    spc.setFetchACLInBatches(true);
    
    assertEquals(50, spc.getAclBatchSize());    
  }
  
  // ACL Batch size factor > 500 will result in batch size 1.
  public void testGetAclBatchSizeLargeBatchSizeFatcor() throws Exception {
    SharepointClientContext spc 
        = new SharepointClientContext(new MockClientFactory());
    spc.setTraversalContext(setSupportsInheritedAcls(true));   
    spc.setPushAcls(true);
    spc.setAclBatchSizeFactor(1000);
    spc.setFetchACLInBatches(true);
    
    assertEquals(1, spc.getAclBatchSize());
  }
   
  public void testGetAclBatchSizeNoAcls() throws Exception {
    SharepointClientContext spc 
        = new SharepointClientContext(new MockClientFactory());    
    spc.setTraversalContext(setSupportsInheritedAcls(true));   
    spc.setPushAcls(false);
    spc.setAclBatchSizeFactor(10);
    spc.setFetchACLInBatches(true);
    
    assertEquals(-1, spc.getAclBatchSize());
  }
}
