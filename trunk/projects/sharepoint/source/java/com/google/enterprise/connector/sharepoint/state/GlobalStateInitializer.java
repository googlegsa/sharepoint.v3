// Copyright 2006 Google Inc.

/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.enterprise.connector.sharepoint.state;

import com.google.enterprise.connector.sharepoint.client.SharepointException;
/**
 * This must called before any methods in GlobalState are called (or the
 * GlobalState constructor will fail).
 *
 */
public class GlobalStateInitializer {

  /**
   * Initializes GlobalState with its dependencies (in this case, those
   * objects which it indexes and maintains state for). 
   * @throws SharepointException (only if ListState is no longer a subclass
   * of GlobalStateObject)
   */
  public static void init() throws SharepointException {
    GlobalState.injectDependency(ListState.class);
  }

  private GlobalStateInitializer() {}
}
