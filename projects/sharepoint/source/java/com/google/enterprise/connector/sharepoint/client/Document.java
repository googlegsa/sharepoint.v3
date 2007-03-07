// Copyright 2007 Google Inc.  All Rights Reserved.
package com.google.enterprise.connector.sharepoint.client;

import java.util.Calendar;

/**
 * Class to hold data regarding a sharepoint document.
 *
 */
public class Document {
  
  private String docId;
  private String url;
  private Calendar date;
  
  public Document(String docId, String url, Calendar date) {
    this.docId = docId;
    this.url = url;
    this.date = date;
  }

  public Calendar getDate() {
    return date;
  }

  public String getDocId() {
    return docId;
  }

  public String getUrl() {
    return url;
  }   
}
