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

package com.google.enterprise.connector.sharepoint.client;

import java.util.Calendar;

/**
 * Class to hold data regarding a sharepoint document.
 *
 */
public class SPDocument implements Comparable<SPDocument>{
  
  public final static String NO_OBJ_TYPE = "No Object Type";
  public final static String NO_AUTHOR = "No author";
  private String docId;
  private String url;
  private Calendar lastMod;
  private String author = NO_AUTHOR;
  private String objType = NO_OBJ_TYPE;
  
  public SPDocument(String docId, String url, Calendar lastMod) {
    this.docId = docId;
    this.url = url;
    this.lastMod = lastMod;
  }

  public SPDocument(String docId, String url, Calendar lastMod, String author,
      String objType) {
    this.docId = docId;
    this.url = url;
    this.lastMod = lastMod;
    this.author = author;
    this.objType = objType;
  }
  
  public Calendar getLastMod() {
    return lastMod;
  }

  public String getDocId() {
    return docId;
  }

  public String getUrl() {
    return url;
  }      
  
  public String getAuthor() {
    return author;
  }

  public String getObjType() {
    return objType;
  }
  
  public void setAuthor(String author) {
    this.author = author;
  }
  
  public void setObjType(String objType) {
    this.objType = objType;
  }

  public int compareTo(SPDocument doc) {
    int comparison = this.lastMod.getTime().compareTo(doc.lastMod.getTime());
    if (0 == comparison) {
      comparison = this.docId.compareTo(doc.docId);
      if (0 == comparison) {
        comparison = this.url.compareTo(doc.url);
      }
    }    
    return comparison;
  }
}
