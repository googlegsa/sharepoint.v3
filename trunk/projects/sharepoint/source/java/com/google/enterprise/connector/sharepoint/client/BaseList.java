// Copyright 2007 Google Inc.  All Rights Reserved.
package com.google.enterprise.connector.sharepoint.client;

/**
 * Class to hold data regarding a sharepoint list.
 *
 */
public class BaseList {
    private String internalName;
    private String title;
    private String type;
    
    public BaseList(String internalName, String title, String type) {
      this.internalName = internalName;
      this.title = title;
      this.type = type;
    }

    public String getInternalName() {
      return internalName;
    }

    public String getTitle() {
      return title;
    }

    public String getType() {
      return type;
    }
    
}
