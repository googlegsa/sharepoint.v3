// Copyright 2012 Google Inc.
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

package com.google.enterprise.connector.sharepoint.wsclient.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

class MockItem {
  private static final Logger LOGGER =
      Logger.getLogger(MockItem.class.getName());

  private final String name;
  private final MockType type;
  private final MockItem parent;
  private final ArrayList<MockItem> children;
  private final ArrayList<String> acls;

  public MockItem(String name, MockType type, MockItem parent) {
    this.name = name;
    this.type = type;
    this.parent = parent;
    this.children = new ArrayList<MockItem>();
    this.acls = new ArrayList<String>();
  }

  public String getName() {
    return name;
  }

  public MockType getType() {
    return type;
  }

  public List<MockItem> getChildren() {
    return children;
  }

  public Boolean isContainer() {
    return (MockType.Web == type) || (MockType.List == type) ||
      (MockType.Folder == type);
  }

  public void addChild(MockItem item) {
    if (!canAddChild(item)) {
      LOGGER.warning("Cannot add " + item.getType() + " to " + 
        type + ".");
    }
    children.add(item);
  }

  public void addChildren(List<MockItem> items) {
    for (MockItem item : items) {
      addChild(item);
    }
  }

  public Boolean canAddChild(MockItem item) {
    switch (type) {
      case Web:
        return (MockType.Web == item.getType()) ||
          (MockType.List == item.getType());

      case Folder:
      case List:
        return (MockType.Folder == item.getType()) ||
          (MockType.Document == item.getType());

      default:
      case Document:
        return false;
    }
  }

  public MockItem getChildByName(final String name) {
    for (MockItem child : children) {
      if (name.equals(child.getName())) {
        return child;
      }
    }
    return null;
  }
  
  public void addAcl(final String name) {
    acls.add(name.toLowerCase());
  }
  
  public Boolean hasPermission(final String name) {
    String nameLower = name.toLowerCase();
    Boolean hasPermission;
    if ((null != acls) && (acls.size() > 0)) {
      hasPermission = false;
      for (String acl : acls) {
        if (acl.equals(nameLower)) {
          hasPermission = true;
          break;
        }
      }
    } else {
      hasPermission = true;
    }
    return hasPermission;
  }

  public void Dump() {
    Dump("");
  }

  public void Dump(String pad) {
    System.out.println(pad + type);

    if (isContainer()) {
      for (MockItem item : children) {
        item.Dump(pad + "  ");
      }
    }
  }
}
