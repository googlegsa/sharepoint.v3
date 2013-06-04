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

import java.io.Serializable;

/**
 * Represents an attribute of a SharePoint entity and the documents that is sent
 * to GSA
 *
 * @author nitendra_thakur
 */
public class Attribute implements Serializable {
  private static final long serialVersionUID = 0x1L;
  private String name;
  private Object value;

  /**
   * @param s
   * @param obj
   */
  public Attribute(final String s, final Object obj) {
    if (s == null) {
      throw new IllegalArgumentException("Attribute name cannot be null ");
    } else {
      name = s;
      if (obj != null) { // ignore null values
        value = obj;
      }
      return;
    }
  }

  /**
   * @return name of the attribute
   */
  public String getName() {
    return name;
  }

  /**
   * @return value of the attribute
   */
  public Object getValue() {
    return value;
  }

  /**
   * For attribute's equality check
   */
  public boolean equals(final Object obj) {
    if (!(obj instanceof Attribute)) {
      return false;
    }
    final Attribute attribute = (Attribute) obj;
    if (value == null) {
      if (attribute.getValue() == null) {
        return name.equals(attribute.getName());
      } else {
        return false;
      }
    } else {
      return name.equals(attribute.getName())
          && value.equals(attribute.getValue());
    }
  }

  /**
   * @see java.lang.String#hashCode()
   */
  public int hashCode() {
    return super.hashCode();
  }

}
