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

import com.google.enterprise.connector.adgroups.AdConstants.Method;
import com.google.enterprise.connector.adgroups.AdDbUtil.Query;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.TraversalManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdGroupsTraversalManager implements TraversalManager {
  private static final Logger LOGGER =
      Logger.getLogger(AdGroupsTraversalManager.class.getName());

  private ArrayList<AdServer> servers;
  private ArrayList<AdEntity> wellKnownEntities;
  private AdDbUtil db;

  public AdGroupsTraversalManager(AdGroupsConnector connector) 
      throws RepositoryException {
    servers = new ArrayList<AdServer>();
    wellKnownEntities = new ArrayList<AdEntity>();

    db = new AdDbUtil(connector.getDataSource(), connector.getDatabaseType());

    ResourceBundle rb = ResourceBundle.getBundle(
        getClass().getPackage().getName() + ".wellknowidentifiers");
    Enumeration<String> keys = rb.getKeys();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      wellKnownEntities.add(new AdEntity(key, rb.getString(key)));
    }

    LOGGER.info("Adding servers");

    // I will leave this as undocumented hack to get crawling of multiple ADs
    // with one instance of the connector until better solution is created
    // via the configuration
    String[] methods = connector.getMethod().split("\\|");
    String[] hostnames = connector.getHostname().split("\\|");
    String[] ports = connector.getPort().split("\\|");
    String[] principals = connector.getPrincipal().split("\\|");
    String[] passwords = connector.getPassword().split("\\|");

    for (int i = 0; i < hostnames.length; ++i) {
      if (hostnames[i].trim().length() != 0) {
        AdServer server = new AdServer(
            methods[i].equals("SSL") ? Method.SSL : Method.STANDARD,
            hostnames[i],
            Integer.parseInt(ports[i]),
            principals[i],
            passwords[i]);
        server.initialize();
        servers.add(server);
      }
    }
  }

  public long getHighestCommitedUsn(AdServer server) {
    List<HashMap<String, Object>> dbServers;
    try {
      dbServers = db.select(AdDbUtil.Query.SELECT_SERVER, server.getSqlParams());
    } catch (SQLException e) {
      LOGGER.log(Level.INFO, "Other connector is currently crawling the dn ["
          + server.getDn() + "]", e);
      return -1;
    }

    if (dbServers.size() == 0) {
      LOGGER.fine(
          "Connected to Directory Controller [" + server.getDsServiceName()
          + "] for the first time. Performing full crawl.");
      return 0;
    } else if (dbServers.size() == 1) {
      HashMap<String, Object> dbServer = dbServers.get(0);
      if (dbServer.get("dsservicename").equals(server.getDsServiceName())) {
        if (dbServer.get("invocationid").equals(server.getInvocationID())) {
          return ((Number) dbServer.get("highestcommittedusn")).longValue();
        } else {
          LOGGER.warning("Directory Controller [" + server.getDsServiceName()
              + "] has been restored from backup. Performing full recrawl.");
        }
      } else {
        LOGGER.warning("Directory Controller changed!!! Connected to ["
            + dbServer.get("invocationid") + "], but expected ["
            + server.getDsServiceName()
            + "]. Not able to perform partial updates - performing full"
            + "recrawl. Consider configuring AdGroups connector to connect"
            + "directly to FQDN address of one domain controller for partial"
            + "updates support.");
      }
    } else {
      LOGGER.warning("Multiple servers for dn [" + server.getDn() + "] found");
      // TODO: think what to do in this case - high availability
    }

    return 0;
  }

  public void run() {
    try {
      db.executeBatch(AdDbUtil.Query.MERGE_ENTITIES, wellKnownEntities);
      Map<String, Object> wellKnownServer = new HashMap<String, Object>();
      wellKnownServer.put(AdConstants.DB_DN, "DC=NT Authority");
      wellKnownServer.put(AdConstants.DB_DSSERVICENAME, "S-1-5");
      wellKnownServer.put(AdConstants.DB_INVOCATIONID, "S-1-5");
      wellKnownServer.put(AdConstants.DB_HIGHESTCOMMITTEDUSN, "0");
      wellKnownServer.put(AdConstants.DB_NETBIOSNAME, "NT AUTHORITY");
      wellKnownServer.put(AdConstants.DB_SID, "S-1-5");
      wellKnownServer.put(AdConstants.DB_DNSROOT, "");
      db.execute(Query.UPDATE_SERVER, wellKnownServer);

      wellKnownServer.put(AdConstants.DB_DN, "DC=BUILTIN");
      wellKnownServer.put(AdConstants.DB_DSSERVICENAME, "S-1-5-32");
      wellKnownServer.put(AdConstants.DB_INVOCATIONID, "S-1-5-32");
      wellKnownServer.put(AdConstants.DB_HIGHESTCOMMITTEDUSN, "0");
      wellKnownServer.put(AdConstants.DB_NETBIOSNAME, "BUILTIN");
      wellKnownServer.put(AdConstants.DB_SID, "S-1-5-32");
      wellKnownServer.put(AdConstants.DB_DNSROOT, "");
      db.execute(Query.UPDATE_SERVER, wellKnownServer);
    } catch (SQLException e) {
      LOGGER.log(Level.WARNING, "Merging of well known identifiers failed", e);
    }

    LOGGER.info("Starting AD crawl");

    Random random = new Random();
    List<AdServer> serversToCrawl = new ArrayList<AdServer>(servers);

    int runs = 0;
    while (!serversToCrawl.isEmpty() && runs < 10) {
      runs++;
      for (Iterator<AdServer> i = serversToCrawl.iterator(); i.hasNext();) {
        AdServer server = i.next();
        try {
          String ldapQuery;
          long last = getHighestCommitedUsn(server);
          if (last == -1) {
            continue;
          } else if (last == 0) {
            LOGGER.info("Full recrawl start");
            // Delete all memberships on this server
            db.execute(Query.CLEAN_MEMBERS, server.getSqlParams());

            // Remove all foreign memberships - these will be regenerated
            // by MATCH_ENTITIES later
            db.execute(Query.CLEAN_FOREIGN_MEMBERS, server.getSqlParams());

            // Remove all entities from this server
            db.execute(Query.CLEAN_ENTITIES, server.getSqlParams());
            ldapQuery = AdConstants.LDAP_QUERY;
          } else {
            LOGGER.info("Partial recrawl start");
            ldapQuery = AdConstants.PARTIAL_QUERY_START
                + last + AdConstants.PARTIAL_QUERY_END;
          }

          LOGGER.info("Querying server " + ldapQuery);
          List<AdEntity> entities = server.search(ldapQuery, new String[] {
              AdConstants.ATTR_USNCHANGED,
              AdConstants.ATTR_SAMACCOUNTNAME,
              AdConstants.ATTR_OBJECTSID,
              AdConstants.ATTR_OBJECTGUID,
              AdConstants.ATTR_UPN,
              AdConstants.ATTR_PRIMARYGROUPID,
              AdConstants.ATTR_MEMBER});

          LOGGER.info("Found " + entities.size() +
              " entities to update in the database.");

          // Merge entities discovered on current server into the database
          LOGGER.info("[" + server.getnETBIOSName()
              + "] update 1/8 - Inserting AD Entities into database.");
          db.executeBatch(Query.MERGE_ENTITIES, entities);

          // Merge group memberships into the database
          LOGGER.info("[" + server.getnETBIOSName()
              + "] update 2/8 - Inserting relationships into database.");
          db.mergeMemberships(
            Query.DELETE_MEMBERSHIPS, Query.ADD_MEMBERSHIPS, entities);

          // Update the members table to include link to primary key of
          // entities table for faster lookup during serve time
          LOGGER.info("[" + server.getnETBIOSName()
              + "] update 3/8 - Crossreferencing entity relationships.");
          db.execute(Query.MATCH_ENTITIES, null);

          // Resolve primary user's groups
          LOGGER.info("[" + server.getnETBIOSName()
              + "] update 4/8 - Resolving user primary groups.");
          db.execute(Query.RESOLVE_PRIMARY_GROUP, null);

          // Resolve foreign security principals
          LOGGER.info("[" + server.getnETBIOSName()
              + "] update 5/8 - Resolving foreign security principals.");
          db.execute(Query.RESOLVE_FOREIGN_SECURITY_PRINCIPALS, null);

          // Update the server information
          LOGGER.info("[" + server.getnETBIOSName()
              + "] update 6/8 - Updating Domain controller info.");
          db.execute(Query.UPDATE_SERVER, server.getSqlParams());
          
          // Commit the transaction
          LOGGER.info("[" + server.getnETBIOSName()
              + "] update 7/8 - Committing all modifications.");
          db.commit();
          
          // All done
          LOGGER.info("[" + server.getnETBIOSName()
              + "] update 8/8 - domain information updated.");
          
          i.remove();
        } catch (SQLException e) {
          LOGGER.log(Level.WARNING, "Merging data into database failed\n:", e);
          db.rollback();
        }
      }

      if (!serversToCrawl.isEmpty()) {
        try {
          Thread.sleep(10000 + random.nextInt(50000));
        } catch (InterruptedException e) {
          LOGGER.log(Level.INFO, "Sleep interrupted", e);
          return;
        }
      }
    }
  }

  @Override
  public DocumentList resumeTraversal(String checkpoint)
      throws RepositoryException {
    run();
    return null;
  }

  @Override
  public void setBatchHint(int batchHint) throws RepositoryException {
    db.setBatchHint(batchHint);
  }

  @Override
  public DocumentList startTraversal() throws RepositoryException {
    run();
    return null;
  }
}
