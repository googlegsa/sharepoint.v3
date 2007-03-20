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

import com.google.enterprise.connector.spi.RepositoryException;

/**
 * Thrown to indicate connectivity problems with Sharepoint server.
 */
public class SharepointException extends RepositoryException {
  /**
   * Constructs a SharepointException with no message and no root cause.
   */
  public SharepointException() {
    super();
  }

  /**
   * Constructs a SharepointException with a supplied message but no root cause.
   * @param message the message. Can be retrieved by the 
   * {@link #getMessage()} method.
   */
  public SharepointException(String message) {
    super(message);
  }

  /**
   * Constructs a SharepointException with message and root cause.
   * @param message   the message. Can be retrieved by the 
   * {@link #getMessage()} method.
   * @param rootCause root failure cause
   */
  public SharepointException(String message, Throwable rootCause) {
    super(message, rootCause);
  }

  /**
   * Constructs a SharepointException with the specified root cause.
   * @param rootCause root failure cause
   */
  public SharepointException(Throwable rootCause) {
    super(rootCause);
  }
}
