// Copyright 2007 Google Inc.  All Rights Reserved.

package com.google.enterprise.connector.sharepoint;

import com.google.enterprise.connector.sharepoint.client.SharepointClient;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.SimpleResultSet;

import java.util.Iterator;
import java.util.ListIterator;


public class SharepointQueryTraversalManager implements QueryTraversalManager {
  
  private SharepointClientContext sharepointClientContext;
  private SharepointConnector connector;
  
  public SharepointQueryTraversalManager(SharepointConnector connector,
    SharepointClientContext sharepointClientContext) {
    this.connector = connector;
    this.sharepointClientContext = sharepointClientContext;
  }
  /* (non-Javadoc)
   * @see com.google.enterprise.connector.spi.QueryTraversalManager#checkpoint(com.google.enterprise.connector.spi.PropertyMap)
   */
  public String checkpoint(PropertyMap arg0) throws RepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.spi.QueryTraversalManager#resumeTraversal(java.lang.String)
   */
  public ResultSet resumeTraversal(String arg0) throws RepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.spi.QueryTraversalManager#setBatchHint(int)
   */
  public void setBatchHint(int arg0) throws RepositoryException {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.spi.QueryTraversalManager#startTraversal()
   */
  public ResultSet startTraversal() throws RepositoryException {
   
    SharepointClient sharepointClient = new SharepointClient(sharepointClientContext);
    ResultSet rs = sharepointClient.getSites();
    return rs;    
  }

}
