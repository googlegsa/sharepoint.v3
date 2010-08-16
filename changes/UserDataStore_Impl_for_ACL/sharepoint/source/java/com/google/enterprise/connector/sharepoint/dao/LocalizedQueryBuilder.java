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

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * An query builder that uses the localization concept for finding the vendor
 * specific queries. Queries are stored like resource bundles files
 *
 * @author nitendra_thakur
 */
public abstract class LocalizedQueryBuilder implements QueryBuilder {

    private ResourceBundle sqlQueries;
    private String basename;

    public void setLocale(Locale sqlQueriesLocale) throws SharepointException {
        if (null == sqlQueriesLocale) {
            throw new SharepointException(
                    "Locale is null. Could not locate sqlQueries.properties file! ");
        }

        if (null == basename) {
            throw new SharepointException("Basename is null! ");
        }

        try {
            sqlQueries = ResourceBundle.getBundle(basename, sqlQueriesLocale);
        } catch (Exception e) {
            throw new SharepointException(
                    "Could not load sqlQueries.properties for locale [ "
                            + sqlQueriesLocale + " ] ");
        }
    }

    protected String getQueryString(String key) {
        return sqlQueries.getString(key);
    }

    public String getBasename() {
        return basename;
    }

    public void setBasename(String basename) {
        this.basename = basename;
    }
}
