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
 * Class to hold data regarding a sharepoint list.
 * internalName is the GUID of the list and type is the type of the list 
 * e.g., DocumentLibrary.
 *
 */
public class BaseList implements Comparable<BaseList> {
  private String internalName;
  private String title;
  private String type;
  private Calendar lastMod;
  
  public BaseList(String internalName, String title, String type,
      Calendar lastMod) {
    this.internalName = internalName;
    this.title = title;
    this.type = type;
    this.lastMod = lastMod;
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

  public Calendar getLastMod() {
    return lastMod;
  }
 
  public int compareTo(BaseList list) {
    int comparison = this.lastMod.getTime().compareTo(list.lastMod.getTime());
    if (0 == comparison) {
      comparison = this.internalName.compareTo(list.internalName);
    }    
    return comparison;
  }
}
