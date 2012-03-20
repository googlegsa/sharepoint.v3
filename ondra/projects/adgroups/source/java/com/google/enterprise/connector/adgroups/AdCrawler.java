// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.adgroups;

import com.google.enterprise.connector.adgroups.AdConstants.Method;
import com.google.enterprise.connector.spi.AuthenticationIdentity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class AdCrawler {
  private static final Logger LOGGER = Logger.getLogger(AdCrawler.class.getName());

  public ArrayList<AdServer> servers;
  public ArrayList<AdEntity> wellKnownEntities;

  private AdDbUtil db;

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

    AdServer ntAuthority = new AdServer(null, null, -1, "NT Authority", null, null);
    AdEntity e = new AdEntity("cn=Authenticated Users,dc=NT Authority");
    e.sAMAccountName = "Authenticated Users";
    e.server = ntAuthority;
    wellKnownEntities.add(e);

    e = new AdEntity("cn=Interactive,dc=NT Authority");
    e.sAMAccountName = "Interactive";
    e.server = ntAuthority;
    wellKnownEntities.add(e);

    LOGGER.info("Adding servers");

    String[] methods = method.split("\\|");
    String[] hostnames = hostname.split("\\|");
    String[] ports = port.split("\\|");
    String[] domains = domain.split("\\|");
    String[] usernames = username.split("\\|");
    String[] passwords = password.split("\\|");


    for (int i = 0; i < hostnames.length; ++i) {
      if (hostnames[i].trim().length() != 0) {
        AdServer controller = new AdServer(methods[i].equals("SSL") ? Method.SSL : Method.STANDARD,
            hostnames[i],
            Integer.parseInt(ports[i]),
            domains[i],
            usernames[i],
            passwords[i]);
        controller.connect();
        servers.add(controller);
      }
    }
  }

  /*
   * public AdEntity findUser(AuthenticationIdentity identity) {
   * LOGGER.info("Authenticating identity: user:" + identity.getUsername() + " domain: " +
   * identity.getDomain());
   *
   * String username = identity.getUsername().toLowerCase(); String domain = identity.getDomain() !=
   * null ? identity.getDomain().toUpperCase() : "";
   *
   * // sAMAccountName and NETBIOS name match for (AdEntity e : serveEntities.values()) { if
   * (e.sAMAccountName != null && e.sAMAccountName.equalsIgnoreCase(username)) { LOGGER.info("sam: "
   * + e.sAMAccountName);
   *
   * if (serverindex.get(e.getDC()).nETBIOSName.equalsIgnoreCase(domain)) { LOGGER.info("found: " +
   * e.dn); return e; } } }
   *
   * // dn matches // TODO: check how exactly is dn sent to us for (AdEntity e :
   * serveEntities.values()) { if (e.dn.equalsIgnoreCase(identity.getUsername()))
   * LOGGER.info("found: " + e.dn); return e; }
   *
   * // TODO: check how exactly is userPrincipalName name sent to us for (AdEntity e :
   * serveEntities.values()) { if (e.userPrincipalName != null &&
   * e.userPrincipalName.equalsIgnoreCase( identity.getUsername())) { LOGGER.info("found: " + e.dn);
   * return e; } }
   *
   * // only sAMAccountName matches ArrayList<AdEntity> users = new ArrayList<AdEntity>(); for
   * (AdEntity e : serveEntities.values()) { if (e.sAMAccountName.equalsIgnoreCase(username) ||
   * (e.getCommonName().equalsIgnoreCase(username))) { users.add(e); LOGGER.info("multimatch: " +
   * e.dn); } }
   *
   * // across all domains we found only one user with specified username if (users.size() == 1) {
   * return users.get(0); } else if (users.size() > 1) { StringBuilder sb = new
   * StringBuilder("Domain [" + identity.getDomain() +
   * "] couldn't be found. Multiple users found for username [" + identity.getUsername() +
   * "] => ["); for (AdEntity e : users) { sb.append(e.dn); sb.append(", "); }
   * sb.replace(sb.length() - 1, sb.length(), "] aborting."); LOGGER.log(Level.SEVERE,
   * sb.toString()); }
   *
   * return null; }
   */
  public long getHighestCommitedUsn(AdServer server) throws SQLException {
    List<HashMap<String, Object>> dbServers =
        db.select(
            "SELECT serverid, dn, dsservicename, invocationid, highestcommittedusn, lastsync FROM servers WHERE dn = :dn ORDER BY lastsync DESC",
            server.getSqlParams());
    if (dbServers.size() == 0) {
      LOGGER.fine("Connected to Directory Controller [" + server.dsServiceName
          + "] for the first time. Performing full crawl.");
      return 0;
    } else if (dbServers.size() == 1) {
      HashMap<String, Object> dbServer = dbServers.get(0);
      if (dbServer.get("dsservicename").equals(server.dsServiceName)) {
        if (dbServer.get("invocationid").equals(server.invocationID)) {
          return (Integer) dbServer.get("highestcommittedusn");
        } else {
          LOGGER.warning("Directory Controller [" + server.dsServiceName
              + "] has been restored from backup. Performing full recrawl.");
        }
      } else {
        LOGGER.warning("Directory Controller changed!!! Connected to ["
            + dbServer.get("invocationid") + "], but expected [" + server.dsServiceName
            + "]. Not able to perform partial updates - performing full recrawl. Consider configuring AdGroups connector to connect directly to FQDN address of one domain controller for partial updates support.");
      }
    } else {
      LOGGER.warning("Multiple servers for dn [" + server.getDn() + "] found.");
      // TODO: think what to do in this case - high availability
    }

    return 0;
  }

  public void run() {
    LOGGER.info("Starting AD crawl");

    while (true) {
      for (AdServer server : servers) {
        try {
          String ldapQuery;
          long last = getHighestCommitedUsn(server);
          if (last == 0) {
            LOGGER.info("Full recrawl start");
            ldapQuery = "(|(objectClass=group)(objectClass=user))";
          } else {
            LOGGER.info("Partial recrawl start");
            ldapQuery = "(&(uSNChanged>=" + last + ")(|(objectClass=group)(objectClass=user)))";
          }

          LOGGER.info("Querying server " + ldapQuery);
          List<AdEntity> entities = server.search(ldapQuery, new String[] {"uSNChanged",
              "sAMAccountName",
              "objectSid;binary",
              "objectGUID;binary",
              "userPrincipalName",
              "primaryGroupId",
              "member"});

          LOGGER.info("Found " + entities.size() + " entities to update.");
          
          db.mergeEntities(
              "MERGE INTO entities (serverid, dn, samaccountname, userprincipalname, primarygroupid, sid, objectguid, usnchanged) KEY (objectguid) VALUES (:serverid, :dn, :samaccountname, :userprincipalname, :primarygroupid, :sid, :objectguid, :usnchanged);",
              entities);
          db.mergeMemberships(
              "DELETE FROM members WHERE groupid in (SELECT entityid FROM entities WHERE dn = :dn);",
              "INSERT INTO members (groupid, memberdn) VALUES ((SELECT entityid FROM entities WHERE dn = :dn), :memberdn);",
              entities);
          db.execute(
              "UPDATE members SET members.memberid = (SELECT entities.entityid FROM entities WHERE members.memberdn = entities.dn);",
              null);
          db.execute(
              "MERGE INTO members (groupid, memberdn, memberid) KEY (groupid, memberdn) SELECT b.entityid, a.dn, a.entityid FROM entities a JOIN entities b ON SUBSTRING(a.sid, 1, INSTR(a.sid, '-', -1)) || a.primarygroupid = b.sid;",
              null);
          db.execute(
              "MERGE INTO MEMBERS (groupid, memberid, memberdn) KEY (groupid, memberdn) SELECT groupid, entities.entityid, memberdn FROM members JOIN entities ON SUBSTRING(memberdn, 4, INSTR(memberdn, ',CN') - 4) = sid WHERE memberdn LIKE '%CN=ForeignSecurityPrincipals%';",
              null
              );
          db.execute(
              "MERGE INTO servers (dn, dsservicename, netbiosname, invocationid, highestcommittedusn, lastsync) KEY (dsservicename) VALUES (:dn, :dsservicename, :netbiosname, :invocationid, :highestcommittedusn, NOW())",
              server.getSqlParams());
          db.commit();
        } catch (SQLException e) {
          LOGGER.log(Level.WARNING, "Merging data into database failed\n:", e);
          db.rollback();
        }
      }

      if (System.getenv("USER").charAt(0) == 'o') break;

      // recrawl every half-day
      try {
        Thread.sleep(1000 * 60 * 60 * 12);
      } catch (InterruptedException e) {
      }
    }
  }
}
