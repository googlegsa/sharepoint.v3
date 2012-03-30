// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.adgroups;

import com.google.enterprise.connector.adgroups.AdConstants.Method;
import com.google.enterprise.connector.adgroups.AdDbUtil.Query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class AdCrawler {
  private static final Logger LOGGER =
      Logger.getLogger(AdCrawler.class.getName());

  public ArrayList<AdServer> servers;
  public ArrayList<AdEntity> wellKnownEntities;

  public AdDbUtil db;

  public AdCrawler(DataSource dataSource,
      String method,
      String hostname,
      String port,
      String domain,
      String username,
      String password) {
    servers = new ArrayList<AdServer>();
    wellKnownEntities = new ArrayList<AdEntity>();

    db = new AdDbUtil(dataSource);

    //TODO: add all possible well known entities
    // and externalize this to properties file
    wellKnownEntities.add(
        new AdEntity("CN=Authenticated Users,DC=NT Authority", "S-1-5-11"));
    wellKnownEntities.add(
        new AdEntity("CN=Interactive,DC=NT Authority", "S-1-5-4"));

    LOGGER.info("Adding servers");

    //TODO: this doesn't support pipe characters in passwords
    String[] methods = method.split("\\|");
    String[] hostnames = hostname.split("\\|");
    String[] ports = port.split("\\|");
    String[] domains = domain.split("\\|");
    String[] usernames = username.split("\\|");
    String[] passwords = password.split("\\|");

    for (int i = 0; i < hostnames.length; ++i) {
      if (hostnames[i].trim().length() != 0) {
        AdServer server = new AdServer(
            methods[i].equals("SSL") ? Method.SSL : Method.STANDARD,
            hostnames[i],
            Integer.parseInt(ports[i]),
            domains[i],
            usernames[i],
            passwords[i]);
        server.connect();
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
          return (Integer) dbServer.get("highestcommittedusn");
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
      Map<String, Object> ntauthority = new HashMap<String, Object>();
      ntauthority.put(AdConstants.DB_DN, "DC=NT Authority");
      ntauthority.put(AdConstants.DB_DSSERVICENAME, "S-1-5");
      ntauthority.put(AdConstants.DB_INVOCATIONID, "S-1-5");
      ntauthority.put(AdConstants.DB_HIGHESTCOMMITTEDUSN, "0");
      ntauthority.put(AdConstants.DB_NETBIOSNAME, "NT AUTHORITY");
      ntauthority.put(AdConstants.DB_SID, "S-1-5");
      db.execute(Query.UPDATE_SERVER, ntauthority);

      //TODO: research how sharepoint stores builtin groups in ACLs
      ntauthority.put(AdConstants.DB_DN, "DC=BUILTIN");
      ntauthority.put(AdConstants.DB_DSSERVICENAME, "S-1-5-32");
      ntauthority.put(AdConstants.DB_INVOCATIONID, "S-1-5-32");
      ntauthority.put(AdConstants.DB_HIGHESTCOMMITTEDUSN, "0");
      ntauthority.put(AdConstants.DB_NETBIOSNAME, "BUILTIN");
      ntauthority.put(AdConstants.DB_SID, "S-1-5-32");

      db.execute(Query.UPDATE_SERVER, ntauthority);
      db.commit();
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
          db.executeBatch(Query.MERGE_ENTITIES, entities);

          // Merge group memberships into the database
          db.mergeMemberships(
            Query.DELETE_MEMBERSHIPS, Query.ADD_MEMBERSHIPS, entities);

          // Update the members table to include link to primary key of
          // entities table for faster lookup during serve time
          db.execute(Query.MATCH_ENTITIES, null);

          // Resolve primary user's groups
          db.execute(Query.RESOLVE_PRIMARY_GROUP, null);

          // Resolve foreign security principals
          db.execute(Query.RESOLVE_FOREIGN_SECURITY_PRINCIPALS, null);

          // Update the server information
          db.execute(Query.UPDATE_SERVER, server.getSqlParams());

          db.commit();
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
}
