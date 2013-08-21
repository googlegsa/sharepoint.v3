// Copyright 2012 Google Inc.
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

package com.google.enterprise.connector.adgroups;

import com.google.enterprise.connector.spi.AuthenticationIdentity;

/**
 * Provides a mutable identity implementation so that this connector
 * can return a modified identity (e.g., mapping NETBIOS to DNS domain
 * names) to the caller.
 *
 * @since 3.0
 */
public class MutableIdentity implements AuthenticationIdentity {
  private String domain;
  private String username;
  private String password;

  public MutableIdentity(AuthenticationIdentity identity) {
    this.domain = identity.getDomain();
    this.username = identity.getUsername();
    this.password = identity.getPassword();
  }

  @Override
  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  @Override
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
