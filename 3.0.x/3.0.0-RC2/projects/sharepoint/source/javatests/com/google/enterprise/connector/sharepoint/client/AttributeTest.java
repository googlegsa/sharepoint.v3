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

import junit.framework.TestCase;

public class AttributeTest extends TestCase {

  Attribute attr;

  protected void setUp() throws Exception {
    super.setUp();
    System.out.println("Creating new temporary attribute with name=Key and value=Value");
    this.attr = new Attribute("Key", "value");
  }

  public final void testHashCode() {
    System.out.println("Testing hashCode()...");
    System.out.println("Generated Has Code: " + this.attr.hashCode());
    System.out.println("[ hashCode() ] Test Completed");
  }

  public final void testGetName() {
    System.out.println("Testing getName()...");
    System.out.println("Attribute Name: " + this.attr.hashCode());
    System.out.println("[ getName() ] Test Completed");
  }

  public final void testGetValue() {
    System.out.println("Testing getValue()...");
    System.out.println("Attribute Value: " + this.attr.hashCode());
    System.out.println("[ getValue() ] Test Completed");
  }

  public final void testEqualsObject() {
    System.out.println("Testing equals()...");
    System.out.println("Creating new temporary attribute with same key and value..");
    final Attribute attr1 = new Attribute(this.attr.getName(),
        this.attr.getValue());
    System.out.println("Equality: " + this.attr.equals(attr1));
    System.out.println("Creating new temporary attribute with different key and value..");
    final Attribute attr2 = new Attribute("sdrg", "dfgedr");
    System.out.println("Equality: " + this.attr.equals(attr2));
    System.out.println("[ equals() ] Test Completed");
  }
}
