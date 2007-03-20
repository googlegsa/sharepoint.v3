// Copyright 2007 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint;

import com.google.enterprise.connector.sharepoint.client.SharepointClient;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;

/**
 * This class is an implementation of the QueryTraversalManager from the spi.
 * All the traversal based logic is invoked through this class.
 *
 */
public class SharepointQueryTraversalManager implements QueryTraversalManager {
  
  private SharepointClientContext sharepointClientContext;
  private SharepointConnector connector;
  
  public SharepointQueryTraversalManager(SharepointConnector connector,
    SharepointClientContext sharepointClientContext) {
    this.connector = connector;
    this.sharepointClientContext = sharepointClientContext;
  }
  
  public String checkpoint(PropertyMap arg0) throws RepositoryException {
//  TODO(meghna) Implement this.
    return null;
  }

  public ResultSet resumeTraversal(String arg0) throws RepositoryException {
    // TODO(meghna) Implement this.
    return null;
  }

  public void setBatchHint(int arg0) throws RepositoryException {
    // TODO(meghna) Implement this.
  }

  public ResultSet startTraversal() throws RepositoryException {
   
    SharepointClient sharepointClient = 
      new SharepointClient(sharepointClientContext);
    ResultSet rs = sharepointClient.getDocsFromDocumentLibrary();
    return rs;    
  }

}
