//Copyright 2007 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.spiimpl;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.spiimpl.SPProperty;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spiimpl.StringValue;

public class SPPropertyTest extends TestCase {

    SPProperty prop;

    protected void setUp() throws Exception {
        super.setUp();
        this.prop = new SPProperty("prop1", new StringValue("Value"));
    }

    public final void testNextValue() {
        System.out.println("Testing nextValue()..");
        try {
            final Value val = this.prop.nextValue();
            assertNotNull(val);
            System.out.println("[ nextValue() ] Test Passed.");
        } catch (final Exception e) {
            System.out.println("[ nextValue() ] Test Failed.");
        }
    }

    public final void testGetName() {
        System.out.println("Testing nextValue()..");
        final String propName = this.prop.getName();
        assertNotNull(propName);
        System.out.println("[ nextValue() ] Test Passed.");
    }
}
