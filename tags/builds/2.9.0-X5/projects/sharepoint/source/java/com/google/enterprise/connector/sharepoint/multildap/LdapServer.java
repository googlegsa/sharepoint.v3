package com.google.enterprise.connector.sharepoint.multildap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import com.google.enterprise.connector.sharepoint.ldap.LdapConstants;
import com.google.enterprise.connector.sharepoint.ldap.LdapConstants.AuthType;
import com.google.enterprise.connector.sharepoint.ldap.LdapConstants.Method;

public class LdapServer {
	private static final Logger LOGGER = Logger.getLogger(LdapServer.class.getName());
	
	private LdapContext ldapContext = null;
	private SearchControls searchCtls;
	private Control[] resultControls; 
	
	public String hostName;
	private int port = 389;
	public String nETBIOSName;
	private AuthType authType;
	private String userName;
	private String password;
	private Method connectMethod;
	public String dn;
	private MultiCrawl multiCrawl;
	private String configurationNamingContext;
	
	public LdapServer(MultiCrawl multiCrawl, Method connectMethod, AuthType authType, String hostName, int port, String nETBIOSName, String userName, String password, String dn)
	{
	  this.multiCrawl = multiCrawl;
	  this.hostName = hostName;
	  this.port = port;
	  this.nETBIOSName = nETBIOSName;
	  this.userName = userName;
	  this.password = password;
	  this.dn = dn;
	  searchCtls = new SearchControls();
	  searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	  this.connectMethod =connectMethod;
	  this.authType = authType;
	}
	
	public void connect() {
		if (ldapContext == null) {
			Hashtable<String, String> env = new Hashtable<String, String>();
			// Use the built-in LDAP support.
			env.put(Context.INITIAL_CONTEXT_FACTORY, LdapConstants.COM_SUN_JNDI_LDAP_LDAP_CTX_FACTORY);

			// Set our authentication settings.
			//TODO: detect this setting
			authType = AuthType.SIMPLE;
			if (authType == AuthType.SIMPLE) {
				env.put(Context.SECURITY_AUTHENTICATION, authType.toString().toLowerCase());
				env.put(Context.SECURITY_PRINCIPAL, nETBIOSName + "\\" + userName);
				env.put(Context.SECURITY_CREDENTIALS, password);
				LOGGER.info("Using simple authentication.");
			} else {
				if (authType != AuthType.ANONYMOUS) {
					LOGGER.warning("Unknown authType - falling back to anonymous.");
				} else {
					LOGGER.info("Using anonymous authentication.");
				}
				env.put(Context.SECURITY_AUTHENTICATION, "none"); //$NON-NLS-1$

			}
			String ldapUrl = (connectMethod == Method.STANDARD) ? "ldap://" : "ldaps://";
			ldapUrl += hostName + ":" + port;
			LOGGER.info(ldapUrl);
			env.put(Context.PROVIDER_URL, ldapUrl);
			
			try {
				ldapContext = new InitialLdapContext(env, null);
				
				/*
				NamingEnumeration<String> nam = ldapContext.getAttributes("").getIDs();
				while (nam.hasMore()) { LOGGER.info(nam.next()); }
				*/
				
				dn = ldapContext.getAttributes("").get("defaultNamingContext").get(0).toString();
				configurationNamingContext = ldapContext.getAttributes("").get("configurationNamingContext").get(0).toString(); 
				resultControls = new Control[]{new PagedResultsControl(1000, false)};
				ldapContext.setRequestControls(resultControls);
			} catch (CommunicationException e) {
				LOGGER.log(Level.WARNING, "Could not obtain an initial context to query LDAP (Active Directory) due to a communication failure.", e);
			} catch (AuthenticationNotSupportedException e) {
				LOGGER.log(Level.WARNING, "Could not obtain an initial context to query LDAP (Active Directory) due to authentication not supported exception.", e);
			} catch (AuthenticationException ae) {
				LOGGER.log(Level.WARNING, "Could not obtain an initial context to query LDAP (Active Directory) due to authentication exception.", ae);
			} catch (NamingException e) {
				LOGGER.log(Level.WARNING, "Could not obtain an initial context to query LDAP (Active Directory) due to a naming exception.", e);
			} catch (IOException e) {
              LOGGER.log(Level.WARNING, "Couldn't initialize LDAP paging control. Will continue without paging - this can cause issue if there are more than 1000 members in one group.");
			}
			if (ldapContext != null) {
				LOGGER.info("Sucessfully created an Initial LDAP context");
			}
		}
		nETBIOSName = get("(ncName=" + dn +")", new String[]{"nETBIOSName"}, configurationNamingContext).get("nETBIOSName");
		LOGGER.log(Level.INFO, "Connected to domain (dn = " + dn + ", netbios = " + nETBIOSName + ", hostname = "+ hostName + ")");
	}
	
	public HashMap<String, String> get(String filter, String[] attributes) {
		return get(filter, attributes, dn);
	}
	
	public HashMap<String, String> get(String filter, String[] attributes, String dn) {
		System.out.println("DN: " + dn);
		System.out.println("Filter: " + filter);
		HashMap<String, String> result = new HashMap<String, String>();
		searchCtls.setReturningAttributes(attributes);
		try {
			NamingEnumeration<SearchResult> ldapResults = ldapContext.search(dn, filter, searchCtls);
			SearchResult sr = ldapResults.next();
			Attributes attrs = sr.getAttributes(); 
			result.put("dn", sr.getNameInNamespace());
			for(String attribute : attributes) {
				Attribute at = attrs.get(attribute);
				if (at != null)
					result.put(attribute, (String)attrs.get(attribute).get(0));
			}
		} catch (NamingException ex) {
			System.out.println("oh no");
		}
		
		return result;
	}
	
	public ArrayList<LdapEntity> search(String filter, String[] attributes) {
		ArrayList<LdapEntity> results = new ArrayList<LdapEntity>();
		searchCtls.setReturningAttributes(attributes);
		try {
		  byte[] cookie = null;
		  do {
			NamingEnumeration<SearchResult> ldapResults = ldapContext.search(dn, filter, searchCtls);
			while (ldapResults.hasMoreElements()) {
				SearchResult sr = ldapResults.next();
                
				LdapEntity e = multiCrawl.get(sr.getNameInNamespace());
                Attributes attrs = sr.getAttributes();

				e.sAMAccountName = (String)attrs.get("sAMAccountName").get(0);
                e.setSid((byte[])attrs.get("objectSid;binary").get(0));
                
                if (attrs.get("primaryGroupId") != null) {
                  e.primaryGroupId = (String)attrs.get("primaryGroupId").get(0);
                }
                
                Attribute member = attrs.get("member");
                if (member != null) {
                    for (int i = 0; i < member.size(); ++i) {
                        LdapEntity user = multiCrawl.get(member.get(i).toString());
                        user.memberOf.add(e);
                    }
                }

				results.add(e);
			}
			
			cookie = null;
			
			Control[] resultResponseControls = ldapContext.getResponseControls();
			
			for (int i = 0; i < resultResponseControls.length; ++i) {
			  if (resultResponseControls[i] instanceof PagedResultsResponseControl) {
			    cookie = ((PagedResultsResponseControl)resultResponseControls[i]).getCookie();
			    ldapContext.setRequestControls(new Control[]{new PagedResultsControl(1000, cookie, Control.CRITICAL)});
			  }
			}
			
		  } while ((cookie != null) && (cookie.length != 0));

		} catch (NamingException e) 
		{
			System.out.println("TODO:" + e);
		} catch (IOException e) {
		  LOGGER.log(Level.WARNING, "Couldn't initialize LDAP paging control. Will continue without paging - this can cause issue if there are more than 1000 members in one group. " + e);
		}
		
		return results;
	}
}
