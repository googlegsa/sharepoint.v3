//Copyright 2010 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.dao;

import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

/**
 * Provides an strategy to the QueryBuilder so that it can load the SQL queries
 *
 * @author nitendra_thakur
 */
public interface QueryProvider {

    /**
     * loads SQL queries
     *
     * @throws SharepointException
     */
    void load() throws SharepointException;

    /**
     * Returns the query identified by the key
     *
     * @param key
     * @return
     */
    String getQuery(String key);
}
