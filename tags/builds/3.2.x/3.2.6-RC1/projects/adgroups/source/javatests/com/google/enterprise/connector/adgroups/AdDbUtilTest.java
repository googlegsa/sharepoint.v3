// Copyright 2014 Google Inc.
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

package com.google.enterprise.connector.adgroups;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import junit.framework.TestCase;

import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

public class AdDbUtilTest extends TestCase {
  public void testConstructor() throws SQLException {
    DataSource ds = createMock(DataSource.class);
    Connection c = createMock(Connection.class);
    PreparedStatement statement = createNiceMock(PreparedStatement.class);
    ResultSet rs = createNiceMock(ResultSet.class);
    // This should take connection only once.
    expect(ds.getConnection()).andReturn(c).times(1);
    expect(c.isValid(1)).andReturn(true).anyTimes();
    expect(c.prepareStatement(isA(String.class)))
        .andReturn(statement).anyTimes();
    expect(statement.executeQuery()).andReturn(rs).anyTimes();
    replay(ds, c, statement, rs);
    AdDbUtil util = new AdDbUtil(ds, "H2");   
    util.select(AdDbUtil.Query.SELECT_CONNECTORNAME, null);
    verify(ds, c, statement, rs);
  }
  
  public void testConstructorWithH2() throws SQLException {
    // Setup in-memory H2 JDBC DataSource;
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem");
    ds.setUser("sa");
    ds.setPassword("sa");
    AdDbUtil util = new AdDbUtil(ds, "H2");
    util.select(AdDbUtil.Query.TEST_CONNECTORNAME, null);
    util.select(AdDbUtil.Query.TEST_SERVERS, null);
    util.select(AdDbUtil.Query.TEST_MEMBERS, null);
    util.select(AdDbUtil.Query.TEST_ENTITIES, null);
  }
}
