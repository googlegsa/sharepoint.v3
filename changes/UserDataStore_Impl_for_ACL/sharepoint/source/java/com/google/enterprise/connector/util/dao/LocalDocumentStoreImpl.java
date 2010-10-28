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

package com.google.enterprise.connector.util.dao;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.LocalDocumentStore;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spiimpl.StringValue;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

public final class LocalDocumentStoreImpl extends SimpleSharePointDAO implements
        LocalDocumentStore {
    private static final Logger LOGGER = Logger.getLogger(LocalDocumentStoreImpl.class.getName());
    private static Object singletonLock = new Object();
    private static LocalDocumentStore instance;

    private Queue<Document> _localDocQueue = new ConcurrentLinkedQueue<Document>();

    // These two are a part of LocalDocumentStore schema. Not sure, how
    // connector manager will pass these info at run time since the documents
    // that connector sends does not contain these info
    private static String connectorInstance;
    private static String connectorType;

    private LocalDocumentStoreImpl(DataSource dataSource,
            QueryProvider queryProvider) throws SQLException {
        super(dataSource, queryProvider);
        if (null != instance) {
            throw new UnsupportedOperationException(
                    "Attempt to create a duplicate instance! ");
        }
        confirmEntitiesExistence();
    }

    private void confirmEntitiesExistence() throws SQLException {
        DatabaseMetaData dbm = null;
        // TODO Devise a better way for ensuring the availability of
        // tables/indexes. Cann't this be incorporated in the query itself
        // e.g CREATE IF NOT AVAILABLE
        dbm = getConnection().getMetaData();
        ResultSet resultSet = dbm.getTables(getQueryProvider().getDatabase(), null, getQueryProvider().getDocTableName(), null);
        if (null == resultSet || !resultSet.next()) {
            getSimpleJdbcTemplate().update(getSqlQuery(Query.DOC_STORE_CREATE));
        }
        resultSet.close();
    }

    public static LocalDocumentStore getInstance(DataSource dataSource,
            QueryProvider queryProvider) throws SQLException {
        LocalDocumentStore instance = LocalDocumentStoreImpl.instance;
        if (null == instance) {
            synchronized (singletonLock) {
                instance = LocalDocumentStoreImpl.instance;
                if (null == instance) {
                    LocalDocumentStoreImpl.instance = instance = new LocalDocumentStoreImpl(
                            dataSource, queryProvider);
                }
            }
        }
        return instance;
    }

    public Document findDocument(String docid) {
        // TODO Auto-generated method stub
        return null;
    }

    public void flush() {
        try {
            SqlParameterSource[] namedParams = createParameter(Query.DOC_STORE_INSERT, _localDocQueue);
            batchUpdate(Query.DOC_STORE_INSERT, namedParams);
        } catch (Exception e) {
            // FIXME DO not catch it. Needs to update the contract.

            LOGGER.log(Level.WARNING, "Could not create query parameters for one of the documents to be stored in Local Document Store. No documents will be saved from the current set! ", e);
            // Caller is expecting all the docs to be fed to DB which cannot be
            // accomplished.
            // throw e;
        }
    }

    public String getDocTableName() {
        // TODO Auto-generated method stub
        return null;
    }

    public Iterator<Document> getDocumentIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    public Iterator<Document> getDocumentIterator(String docid) {
        // TODO Auto-generated method stub
        return null;
    }

    public void storeDocument(Document document) {
        _localDocQueue.add(document);
    }

    private static SqlParameterSource[] createParameter(Query query,
            Collection<Document> documents) throws RepositoryException {
        SqlParameterSource[] namedParams = new SqlParameterSource[documents.size()];
        int count = 0;

        switch (query) {
        case DOC_STORE_INSERT:
            for (Document document : documents) {
                List<Value> attrValues = new LinkedList<Value>();
                for (String attrName : SpiConstants.PERSISTABLE_ATTRIBUTES.keySet()) {
                    if (SpiConstants.PROPNAME_CONNECTOR_INSTANCE.equals(attrName)) {
                        attrValues.add(new StringValue(connectorInstance));
                        continue;
                    } else if (SpiConstants.PROPNAME_CONNECTOR_TYPE.equals(attrName)) {
                        attrValues.add(new StringValue(connectorType));
                        continue;
                    } else {
                        Property property = document.findProperty(attrName);
                        Value value = (null == property) ? null
                                : property.nextValue();
                        attrValues.add(value);
                    }
                }
                namedParams[count++] = Query.DOC_STORE_INSERT.createParameter(attrValues.toArray());
            }
            break;

        default:
            throw new UnsupportedOperationException("Query Not Supported!! ");
        }
        return namedParams;
    }
}
