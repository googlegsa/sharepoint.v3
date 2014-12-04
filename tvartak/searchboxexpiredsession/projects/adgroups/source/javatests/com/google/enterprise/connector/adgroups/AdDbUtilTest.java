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
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import com.google.common.collect.ImmutableMap;
import com.google.enterprise.connector.adgroups.AdConstants.Method;
import com.google.enterprise.connector.adgroups.AdDbUtil.Query;

import org.easymock.EasyMock;
import org.easymock.IAnswer;

import junit.framework.TestCase;

import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    ds.setURL("jdbc:h2:mem:");
    ds.setUser("sa");
    ds.setPassword("");
    AdDbUtil util = new AdDbUtil(ds, "H2");
    util.select(AdDbUtil.Query.TEST_CONNECTORNAME, null);
    util.select(AdDbUtil.Query.TEST_SERVERS, null);
    util.select(AdDbUtil.Query.TEST_MEMBERS, null);
    util.select(AdDbUtil.Query.TEST_ENTITIES, null);
  }

  /**
   * Tests that a 64-bit value greater than 2^31 - 1 can be written to
   * and read from the database.
   */
  public void testHighestCommittedUsn() throws SQLException {
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:");
    ds.setUser("sa");
    ds.setPassword("");
    AdDbUtil util = new AdDbUtil(ds, "H2");

    Map<String, Object> queryParams =
        ImmutableMap.<String, Object>of(AdConstants.DB_DN, "DC=example,DC=com");

    List<HashMap<String, Object>> dbServers =
        util.select(AdDbUtil.Query.SELECT_SERVER, queryParams);
    assertServerCountEquals(0, dbServers);

    Map<String, Object> serverParams = new HashMap<String, Object>();
    serverParams.put(AdConstants.DB_DN, "DC=example,DC=com");
    serverParams.put(AdConstants.DB_HIGHESTCOMMITTEDUSN, Long.MAX_VALUE);
    serverParams.put(AdConstants.DB_DSSERVICENAME, "notnull");
    serverParams.put(AdConstants.DB_INVOCATIONID, "notnull");
    serverParams.put(AdConstants.DB_NETBIOSNAME, "notnull");
    serverParams.put(AdConstants.DB_SID, "notnull");
    serverParams.put(AdConstants.DB_DNSROOT, "notnull");
    util.execute(Query.UPDATE_SERVER, serverParams);

    dbServers = util.select(AdDbUtil.Query.SELECT_SERVER, queryParams);
    assertServerCountEquals(1, dbServers);
    assertEntriesEqual(serverParams, dbServers.get(0),
        AdConstants.DB_DN, AdConstants.DB_HIGHESTCOMMITTEDUSN);
  }

  /**
   * Produces a useful message when the wrong number of servers are
   * returned by a query.
   */
  private void assertServerCountEquals(int expected,
      List<HashMap<String, Object>> dbServers) {
    if (dbServers.size() != expected) {
      List<Object> dns = new ArrayList<Object>(dbServers.size());
      for (Map<String, Object> entry : dbServers) {
        dns.add(entry.get(AdConstants.DB_DN));
      }
      fail("Expected " + expected + " but got " + dbServers.size()
          + " servers: " + dns);
    }
  }

  /** Asserts partial equality of two maps, comparing only the given keys. */
  private void assertEntriesEqual(Map<String, Object> expected,
      Map<String, Object> actual, String... keys) {
    for (String key : keys) {
      assertEquals(key, expected.get(key), actual.get(key));
    }
  }

  public void testSQLExceptionWith601Error2Times() throws SQLException {   
    Connection c = getValidConnection();
    DataSource ds = getDataSourceForConnection(c);
    final PreparedStatement nolockStatement = getMockPreparedStatement(null);
    ResultSet rs = getMockResultSet();
    final PreparedStatement statement = getMockPreparedStatement(rs);
    SQLException e601 = getSQLExceptionFor601();
    final String queryWithNoLock =
        "SELECT entityid, netbiosname, samaccountname"
        + " FROM entities WITH(NOLOCK) LEFT JOIN servers WITH(NOLOCK) "
        + "ON domainsid = sid WHERE wellknown = 1;";
    expect(c.prepareStatement(isA(String.class)))
        .andAnswer(new IAnswer<PreparedStatement> (){
            @Override
            public PreparedStatement answer() {
              String query = getCurrentArguments()[0].toString();            
              if (queryWithNoLock.equalsIgnoreCase(query)) {
                return nolockStatement;
              } else {
                return statement;
              }
            }
    }).anyTimes();
    expect(nolockStatement.executeQuery()).andThrow(e601).andThrow(e601)
        .andReturn(rs); 
    replay(ds, c, statement, rs, e601, nolockStatement);

    AdDbUtil util = new AdDbUtil(ds, "sqlserver");
    util.select(AdDbUtil.Query.SELECT_WELLKNOWN_MEMBERSHIPS, null);
    verify(ds, c, statement, rs, e601, nolockStatement);
  }

  public void testSQLExceptionWith601Error3Times() throws SQLException {
    Connection c = getValidConnection();
    DataSource ds = getDataSourceForConnection(c);
    final PreparedStatement nolockStatement = getMockPreparedStatement(null);
    ResultSet rs = getMockResultSet();
    final PreparedStatement statement = getMockPreparedStatement(rs);
    SQLException e601 = getSQLExceptionFor601();
    final String queryWithNoLock =
        "SELECT entityid, netbiosname, samaccountname"
        + " FROM entities WITH(NOLOCK) LEFT JOIN servers WITH(NOLOCK) "
        + "ON domainsid = sid WHERE wellknown = 1;";
    expect(c.prepareStatement(isA(String.class)))
        .andAnswer(new IAnswer<PreparedStatement> (){
            @Override
            public PreparedStatement answer() {
              String query = getCurrentArguments()[0].toString();            
              if (queryWithNoLock.equalsIgnoreCase(query)) {
                return nolockStatement;
              } else {
                return statement;
              }
            }
    }).anyTimes();
    expect(nolockStatement.executeQuery()).andThrow(e601).andThrow(e601)
        .andThrow(e601);   
    replay(ds, c, statement, rs, e601, nolockStatement);

    AdDbUtil util = new AdDbUtil(ds, "sqlserver");
    try {
      util.select(AdDbUtil.Query.SELECT_WELLKNOWN_MEMBERSHIPS, null);
      fail("Did not throw expected sql exception");
    } catch (SQLException expected) {
      
    }
    verify(ds, c, statement, rs, e601, nolockStatement);
  }

  public void testSQLExceptionWithNon601Error() throws SQLException {
    SQLException sqlException = createNiceMock(SQLException.class);
    Connection c = getValidConnection();
    DataSource ds = getDataSourceForConnection(c);
    final PreparedStatement nolockStatement = getMockPreparedStatement(null);
    ResultSet rs = getMockResultSet();
    final PreparedStatement statement = getMockPreparedStatement(rs);    
    final String queryWithNoLock =
        "SELECT entityid, netbiosname, samaccountname"
        + " FROM entities WITH(NOLOCK) LEFT JOIN servers WITH(NOLOCK) "
        + "ON domainsid = sid WHERE wellknown = 1;";
    expect(c.prepareStatement(isA(String.class)))
        .andAnswer(new IAnswer<PreparedStatement> (){
            @Override
            public PreparedStatement answer() {
              String query = getCurrentArguments()[0].toString();            
              if (queryWithNoLock.equalsIgnoreCase(query)) {
                return nolockStatement;
              } else {
                return statement;
              }
            }
    }).anyTimes();    
    expect(nolockStatement.executeQuery()).andThrow(sqlException);   
    replay(ds, c, statement, rs, sqlException, nolockStatement);
    
    AdDbUtil util = new AdDbUtil(ds, "sqlserver");
    try {
      util.select(AdDbUtil.Query.SELECT_WELLKNOWN_MEMBERSHIPS, null);
      fail("Did not throw expected sql exception");
    } catch (SQLException expected) {
      
    }
    verify(ds, c, statement, rs, sqlException, nolockStatement);
  }
  
  private SQLException getSQLExceptionFor601() {
    SQLException sqlException = createNiceMock(SQLException.class);
    expect(sqlException.getMessage()).andReturn("Some Exception").anyTimes();
    expect(sqlException.getErrorCode()).andReturn(601).anyTimes();
    return sqlException;
  }
  
  private ResultSet getMockResultSet() throws SQLException {
    ResultSet rs = createMock(ResultSet.class);
    ResultSetMetaData meta = createNiceMock(ResultSetMetaData.class);    
    expect(rs.getMetaData()).andReturn(meta).anyTimes();
    expect(rs.next()).andReturn(false).anyTimes();
    rs.close();
    EasyMock.expectLastCall().anyTimes();
    return rs;
  }
  
  private Connection getValidConnection() throws SQLException {
    Connection c = createMock(Connection.class);
    expect(c.isValid(1)).andReturn(true).anyTimes();
    return c;
  }
  
  private PreparedStatement getMockPreparedStatement(ResultSet rs) 
      throws SQLException {
    PreparedStatement statement = createMock(PreparedStatement.class);
    if (rs != null) {
      expect(statement.executeQuery()).andReturn(rs).anyTimes();
    }
    statement.close();
    EasyMock.expectLastCall().anyTimes();
    return statement;
  }
  
  private DataSource getDataSourceForConnection(Connection c)
      throws SQLException {
    DataSource ds = createMock(DataSource.class);
    expect(ds.getConnection()).andReturn(c).anyTimes();
    return ds;
  }
}
