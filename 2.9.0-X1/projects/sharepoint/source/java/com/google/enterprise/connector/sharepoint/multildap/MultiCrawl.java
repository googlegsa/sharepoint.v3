// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sharepoint.multildap;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.ldap.UserGroupsService.LdapConnectionSettings;
import com.google.enterprise.connector.spi.AuthenticationIdentity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiCrawl extends Thread {
  private static final Logger LOGGER = Logger.getLogger(MultiCrawl.class.getName());

  public ArrayList<LdapServer> servers;
  public HashMap<String, LdapServer> serverindex;
  public HashMap<String, LdapEntity> crawlEntities;
  public HashMap<String, LdapEntity> crawlNewEntities;
  public HashMap<String, LdapEntity> serveEntities;
  
  public ArrayList<LdapEntity> wellKnownEntities;
  public SharepointClientContext sharepointClientContext;

  public MultiCrawl(SharepointClientContext sharepointClientContext) {
    servers = new ArrayList<LdapServer>();
    serverindex = new HashMap<String, LdapServer>();
    crawlEntities = new HashMap<String, LdapEntity>();
    crawlNewEntities = new HashMap<String, LdapEntity>();
    serveEntities = new HashMap<String, LdapEntity>();
    this.sharepointClientContext = sharepointClientContext;
    
    wellKnownEntities = new ArrayList<LdapEntity>();
    
    LdapServer ntAuthority = new LdapServer(null, null, null, null, 389, "NT Authority", null, null, null);
    LdapEntity e = new LdapEntity("cn=Authenticated Users,dc=NT Authority");
    e.sAMAccountName="Authenticated Users";
    e.server = ntAuthority;
    wellKnownEntities.add(e);
    
    e = new LdapEntity("cn=Interactive,dc=NT Authority");
    e.sAMAccountName="Interactive";
    e.server = ntAuthority;
    wellKnownEntities.add(e);

    LOGGER.info("Adding servers");

    LdapConnectionSettings lcs = sharepointClientContext.getLdapConnectionSettings();

    String[] hostnames = lcs.getHostname().split("\\|");
    String[] basedns = lcs.getBaseDN().split("\\|");
    String[] domains = lcs.getDomainName().split("\\|");
    String[] usernames = lcs.getUsername().split("\\|");
    String[] passwords = lcs.getPassword().split("\\|");

    for (int i = 0; i < hostnames.length; ++i) {
      if (hostnames[i].trim().length() != 0) {
        LdapServer server = new LdapServer(this,
            lcs.getConnectMethod(),
            lcs.getAuthType(),
            hostnames[i],
            lcs.getPort(),
            domains[i],
            usernames[i],
            passwords[i],
            basedns[i]);
        server.connect();
        servers.add(server);
        serverindex.put(server.dn, server);
      }
    }
  }

  public LdapEntity get(String dn) {
    if (crawlNewEntities.containsKey(dn)) {
      return crawlNewEntities.get(dn);          
    } else if (crawlEntities.containsKey(dn)) {
      return crawlEntities.get(dn);
    } else {
      LdapEntity e = new LdapEntity(dn);
      crawlNewEntities.put(dn, e);
      return e;
    }
  }
  
  public void addPrimaryGroupToUser(LdapEntity user, String primaryGroupId) {
    
    String primaryGroupSid = user.getPrimaryGroupSid(primaryGroupId); 
    
    for (LdapEntity group: crawlNewEntities.values()) {
      if (group.sid != null && group.sid.equals(primaryGroupSid))
      {
        user.memberOf.add(group);
        break;
      }
    }
  }

  public LdapEntity findUser(AuthenticationIdentity identity) {
    LOGGER.info("Authenticating identity: user:" + identity.getUsername() + " domain: " + identity.getDomain());

    String username = identity.getUsername().toLowerCase();
    String domain = identity.getDomain() != null ? identity.getDomain().toUpperCase() : "";

    // sAMAccountName and NETBIOS name match
    for (LdapEntity e : serveEntities.values()) {
      if (e.sAMAccountName != null && e.sAMAccountName.equalsIgnoreCase(username)) {
	  LOGGER.info("sam: " + e.sAMAccountName);

        if (serverindex.get(e.getDC()).nETBIOSName.equalsIgnoreCase(domain)) {
	  LOGGER.info("found: " + e.dn);
          return e;
        }
      }
    }

    // dn matches
    // TODO: check how exactly is dn sent to us
    for (LdapEntity e : serveEntities.values()) {
      if (e.dn.equalsIgnoreCase(identity.getUsername()))
	  LOGGER.info("found: " + e.dn);
        return e;
    }

    // TODO: check how exactly is userPrincipalName name sent to us
    for (LdapEntity e : serveEntities.values()) {
      if (e.userPrincipalName != null && e.userPrincipalName.equalsIgnoreCase(identity.getUsername())) {
	  LOGGER.info("found: " + e.dn);
        return e;
      }
    }

    // only sAMAccountName matches
    ArrayList<LdapEntity> users = new ArrayList<LdapEntity>();
    for (LdapEntity e : serveEntities.values()) {
      if (e.sAMAccountName.equalsIgnoreCase(username)
          || (e.getCommonName().equalsIgnoreCase(username))) {
        users.add(e);
	  LOGGER.info("multimatch: " + e.dn);
      }
    }

    // across all domains we found only one user with specified username
    if (users.size() == 1) {
      return users.get(0);
    } else if (users.size() > 1) {
      StringBuilder sb = new StringBuilder("Domain [" + identity.getDomain()
          + "] couldn't be found. Multiple users found for username [" + identity.getUsername()
          + "] => [");
      for (LdapEntity e : users) {
        sb.append(e.dn);
        sb.append(", ");
      }
      sb.replace(sb.length() - 1, sb.length(), "] aborting.");
      LOGGER.log(Level.SEVERE, sb.toString());
    }

    return null;
  }
  
  private void collapseForeignSecurityPrincipals() {
    
    Set<LdapEntity> toDelete = new HashSet<LdapEntity>();
    
    for (LdapEntity foreign : crawlEntities.values()) {
      if (foreign.dn.endsWith("CN=ForeignSecurityPrincipals," + foreign.getDC())) {
        
        String sid = foreign.getCommonName();
        LdapEntity user = null;
        
        for (LdapEntity u: crawlEntities.values()) {
          if (u.sid != null && u.sid.equals(sid)) {
            user = u;
            break;
          }
        }
        
        if (user != null) {        
          for (LdapEntity group : foreign.memberOf) {
            user.memberOf.add(group);
          }
        
          toDelete.add(foreign);
        }
      }
    }
    
    for (LdapEntity del : toDelete)
      crawlEntities.remove(del);
  }
  
  private void assignToServers() {
    for (LdapEntity e : crawlEntities.values()) {
      e.server = serverindex.get(e.getDC());
    }
  }

  @Override
  public void run() {
    LOGGER.info("Crawling AD");

/*    if (new File(sharepointClientContext.getGoogleConnectorWorkDir(), "ad.xml").exists()) {
      XmlPersist xl = XmlPersist.load(
          new File(sharepointClientContext.getGoogleConnectorWorkDir(), "ad.xml"));
      serveEntities = xl.entities;
      xl.entities = null;
    }
*/
    while (true) {
      crawlEntities = new HashMap<String, LdapEntity>();
      for (LdapServer server : servers) {

        server.search("(objectClass=group)", new String[]{"uSNChanged", "sAMAccountName", "canonicalName", "member", "objectSid;binary"});
        server.search("(objectClass=user)", new String[]{"uSNChanged", "userPrincipalName", "sAMAccountName", "canonicalName", "objectSid;binary", "primaryGroupId"});
        
        LOGGER.info("Entities: " + crawlEntities.size());
        LOGGER.info("NewEntities" + crawlNewEntities.size());
        crawlEntities.putAll(crawlNewEntities);
        crawlNewEntities.clear();
      }
      
      collapseForeignSecurityPrincipals();
      assignToServers();
      
      XmlPersist xs = new XmlPersist();
      xs.entities = crawlEntities;
      LOGGER.info("ServerEntities: " + crawlEntities.size());
      xs.save(new File(sharepointClientContext.getGoogleConnectorWorkDir(), "ad.xml"));
      serveEntities = crawlEntities;
      
      if (System.getenv("USER").charAt(0) == 'o')
        break;
        

      // recrawl every half-day
  	  try {
  		  Thread.sleep(1000 * 60 * 60 * 12);
  	  } catch (InterruptedException e) {
  	  }
    }
  }
}
