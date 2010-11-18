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

package com.google.enterprise.connector.sharepoint.state;

/**
 * Identifies a folder on SharePoint
 *
 * @author nitendra_thakur
 */
public final class Folder implements Comparable<Folder> {
    private final String path;
    private final String id;
    private final int intId;

    public Folder(String path, String id) {
        if (null == id || null == path) {
            throw new NullPointerException("Folder path/Id cannot be null! ");
        }
        intId = Integer.parseInt(id);
        this.path = path;
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public String getId() {
        return id;
    }

    public int getIntId() {
        return intId;
    }

    public int compareTo(Folder folder) {
        return getIntId() - folder.getIntId();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Folder)) {
            return false;
        }
        Folder folder = (Folder) obj;
        return getIntId() == folder.getIntId();
    }

    @Override
    public int hashCode() {
        return intId * path.hashCode();
    }

    @Override
    public String toString() {
        return "Path [ " + path + " ], Id [ " + id + " ] ";
    }
}