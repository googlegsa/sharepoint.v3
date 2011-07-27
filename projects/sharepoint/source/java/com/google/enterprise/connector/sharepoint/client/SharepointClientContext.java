//Copyright 2007 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.client;

import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.dao.UserDataStoreDAO;
import com.google.enterprise.connector.sharepoint.ldap.UserGroupsService.LdapConnectionSettings;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.spi.TraversalContext;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.protocol.Protocol;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//FIXME Should we can get rid of this class since it is unnecessarily creating a hop between SharePointConector and spi implementations
// config values can be passed to the appropriate classes directly using IoC.

/**
 * Class to hold the context information for sharepoint client connection. The
 * information is per connector instance.
 *
 * @author nitendra_thakur
 */
public class SharepointClientContext implements Cloneable {

    private final Logger LOGGER = Logger.getLogger(SharepointClientContext.class.getName());
    private String siteURL;
    private String domain;
    private String username;
    private String password;
    private String kdcServer;
    private String googleConnectorWorkDir = null;
    private String[] excludedURlList = null;
    private String[] includedURlList = null;
    private String mySiteBaseURL = null;

    private Map<String, String> aliasMap = null;
    private FeedType feedType = null;

    private final ArrayList<Pattern> included_metadata = new ArrayList<Pattern>();
    private final ArrayList<Pattern> excluded_metadata = new ArrayList<Pattern>();
    private boolean bFQDNConversion = false;
    private int batchHint = -1;

    private String excludedURL_ParentDir = null;

    // The traversal context
    private TraversalContext traversalContext;

    private boolean pushAcls = true;
    private String usernameFormatInAce;
    private String groupnameFormatInAce;
    private boolean appendNamespaceInSPGroup;

    private UserDataStoreDAO userDataStoreDAO;

    private boolean useSPSearchVisibility = true;
    private List<String> infoPathBaseTemplate = null;

    private boolean reWriteDisplayUrlUsingAliasMappingRules = true;
    private boolean reWriteRecordUrlUsingAliasMappingRules;

    private boolean fetchACLInBatches = false;
    private int aclBatchSizeFactor = 2;
    private int webServiceTimeOut = 300000;
    private int initialCacheSize;
    private boolean useCacheToStoreLdapUserGroupsMembership;
    private long cacheRefreshInterval;
    private LdapConnectionSettings ldapConnectionSettings;
	private boolean feedUnPublishedDocuments;

    public boolean isFeedUnPublishedDocuments() {
		return feedUnPublishedDocuments;
	}

    public void setFeedUnPublishedDocuments(boolean feedUnPublishedDocuments) {
		this.feedUnPublishedDocuments = feedUnPublishedDocuments;
	}

    public LdapConnectionSettings getLdapConnectionSettings() {
        return ldapConnectionSettings;
    }

    public void setLdapConnectionSettings(
            LdapConnectionSettings ldapConnectionSettings) {
        this.ldapConnectionSettings = ldapConnectionSettings;
    }

    /**
     * For cloning
     */
    public Object clone() {
        try {
            final SharepointClientContext spCl = new SharepointClientContext();

            if (null != aliasMap) {
                spCl.setSiteAlias(new LinkedHashMap<String, String>(aliasMap));
            }

            if (null != feedType) {
                spCl.setFeedType(feedType);
            }

            if (null != domain) {
                spCl.setDomain(new String(domain));
            }
            if (null != kdcServer) {
                spCl.setKdcServer((new String(kdcServer)));
            }
            if (null != googleConnectorWorkDir) {
                spCl.setGoogleConnectorWorkDir(new String(
                        googleConnectorWorkDir));
            }

            if (null != mySiteBaseURL) {
                spCl.setMySiteBaseURL(new String(mySiteBaseURL));
            }

            if (null != password) {
                spCl.setPassword(new String(password));
            }

            if (null != siteURL) {
                spCl.setSiteURL(new String(siteURL));
            }

            if (null != excludedURlList) {
                final String[] newExcList = new String[excludedURlList.length];
                for (int i = 0; i < excludedURlList.length; ++i) {
                    newExcList[i] = new String(excludedURlList[i].toString());
                }
                spCl.setExcludedURlList(newExcList);
            }

            if (null != includedURlList) {
                final String[] newIncList = new String[includedURlList.length];
                for (int i = 0; i < includedURlList.length; ++i) {
                    newIncList[i] = new String(includedURlList[i].toString());
                }
                spCl.setIncludedURlList(newIncList);
            }

            if (null != username) {
                spCl.setUsername(new String(username));
            }

            spCl.setFQDNConversion(bFQDNConversion);
            spCl.setBatchHint(batchHint);
            spCl.setPushAcls(pushAcls);

            if (null != included_metadata) {
                spCl.included_metadata.addAll(included_metadata);
            }
            if (null != excluded_metadata) {
                spCl.excluded_metadata.addAll(excluded_metadata);
            }

            if (null != excludedURL_ParentDir) {
                spCl.excludedURL_ParentDir = excludedURL_ParentDir;
            }

            if (null != userDataStoreDAO) {
                // It's ok if we do a shallow copy here
                spCl.userDataStoreDAO = this.userDataStoreDAO;
            }

            spCl.useSPSearchVisibility = useSPSearchVisibility;
            spCl.infoPathBaseTemplate = infoPathBaseTemplate;

            spCl.reWriteDisplayUrlUsingAliasMappingRules = reWriteDisplayUrlUsingAliasMappingRules;
            spCl.reWriteRecordUrlUsingAliasMappingRules = reWriteRecordUrlUsingAliasMappingRules;

            spCl.setUsernameFormatInAce(this.getUsernameFormatInAce());
            spCl.setGroupnameFormatInAce(this.getGroupnameFormatInAce());

            spCl.setAppendNamespaceInSPGroup(this.isAppendNamespaceInSPGroup());
            spCl.setAclBatchSizeFactor(this.aclBatchSizeFactor);
            spCl.setFetchACLInBatches(this.fetchACLInBatches);
            spCl.setWebServiceTimeOut(this.webServiceTimeOut);
            spCl.setLdapConnectionSettings(this.ldapConnectionSettings);
            spCl.setUseCacheToStoreLdapUserGroupsMembership(this.useCacheToStoreLdapUserGroupsMembership);
            spCl.setInitialCacheSize(this.initialCacheSize);
            spCl.setCacheRefreshInterval(this.cacheRefreshInterval);
			spCl.setFeedUnPublishedDocuments(this.feedUnPublishedDocuments);

            return spCl;
        } catch (final Throwable e) {
            LOGGER.log(Level.FINEST, "Unable to clone client context.", e);
            return null;
        }
    }

    /**
     * @param excludedURlList2
     */
    private void setExcludedURlList(final String[] excludedURlList2) {
        if (excludedURlList2 != null) {
            excludedURlList = excludedURlList2;
        }
    }

    /**
     * @param includedURlList2
     */
    private void setIncludedURlList(final String[] includedURlList2) {
        if (includedURlList2 != null) {
            includedURlList = includedURlList2;
        }
    }

    /**
     * Default constructor
     */
    private SharepointClientContext() {
    }

    /**
     * @param sharepointUrl
     * @param inDomain
     * @param inUsername
     * @param inPassword
     * @param inGoogleConnectorWorkDir
     * @param includedURls
     * @param excludedURls
     * @param inMySiteBaseURL
     * @param inAliasMapString
     * @param inFeedType
     * @throws SharepointException
     */
    public SharepointClientContext(String sharepointUrl, final String inDomain,
            final String inKdcHost, final String inUsername,
            final String inPassword, final String inGoogleConnectorWorkDir,
            final String includedURls, final String excludedURls,
            final String inMySiteBaseURL, final String inAliasMapString,
            final FeedType inFeedType, boolean useSPSearchVisibility)
            throws SharepointException {

        Protocol.registerProtocol("https", new Protocol("https",
                new EasySSLProtocolSocketFactory(),
                SPConstants.SSL_DEFAULT_PORT));

        kdcServer = inKdcHost;
        if (sharepointUrl == null) {
            throw new SharepointException("sharepoint URL is null");
        }
        if (inUsername == null) {
            throw new SharepointException("Username is null.");
        }
        if (inPassword == null) {
            throw new SharepointException("Password is null.");
        }
        if (inGoogleConnectorWorkDir == null) {
            throw new SharepointException("Working Directory is null.");
        }
        if (inFeedType == null) {
            throw new SharepointException("Feed Type is null.");
        }

        sharepointUrl = sharepointUrl.trim();

        if (sharepointUrl.endsWith(SPConstants.SLASH)) {
            sharepointUrl = sharepointUrl.substring(0, sharepointUrl.lastIndexOf(SPConstants.SLASH));
        }

        try {
            final URL url = new URL(sharepointUrl);
            int port = url.getPort();
            if (-1 == port) {
                port = url.getDefaultPort();
            }
            siteURL = url.getProtocol() + SPConstants.URL_SEP + url.getHost()
                    + SPConstants.COLON + port + url.getPath();
        } catch (final MalformedURLException e) {
            throw new SharepointException(
                    "Failed to construct sharepoint URL...", e);
        }

        if ((inDomain == null) || inDomain.trim().equals("")) {
            LOGGER.log(Level.CONFIG, "Trying to get domain information from username specified [ "
                    + inUsername
                    + " ] because domain field has not been explicitly specified.");
            domain = Util.getDomainFromUsername(inUsername);
        } else {
            domain = inDomain;
        }

        LOGGER.finest("domain set to " + domain);

        username = Util.getUserFromUsername(inUsername);
        LOGGER.finest("username set to " + username);

        this.setExcludedURlList(excludedURls, SPConstants.SEPARATOR);
        this.setIncludedURlList(includedURls, SPConstants.SEPARATOR);

        password = inPassword;
        googleConnectorWorkDir = inGoogleConnectorWorkDir;
        LOGGER.finest("googleConnectorWorkDir set to " + googleConnectorWorkDir);
        excludedURL_ParentDir = googleConnectorWorkDir + SPConstants.SLASH
                + SPConstants.EXCLUDED_URL_DIR;
        mySiteBaseURL = inMySiteBaseURL;
        LOGGER.finest("mySiteBaseURL set to " + mySiteBaseURL);
        aliasMap = parseAlias(inAliasMapString);

        feedType = inFeedType;
        LOGGER.finest("feedType set to " + feedType);
        LOGGER.finest("bFQDNConversion set to " + bFQDNConversion);

        this.useSPSearchVisibility = useSPSearchVisibility;

        LOGGER.config(" sharepointUrl = [" + sharepointUrl + "] , domain = ["
                + inDomain + "] , username = [" + inUsername
                + "] , googleConnectorWorkDir = [" + inGoogleConnectorWorkDir
                + "] , includedURls = [" + includedURls
                + "] , excludedURls = [" + excludedURls
                + "] , mySiteBaseURL = [" + inMySiteBaseURL
                + "], aliasMapString = [" + inAliasMapString + "], FeedType ["
                + inFeedType + "], useSPSearchVisibility = ["
                + useSPSearchVisibility + "]");
    }

    /**
     * @param sharepointUrl
     * @throws SharepointException
     */
    public void setSiteURL(String sharepointUrl) throws SharepointException {
        sharepointUrl = sharepointUrl.trim();

        if (sharepointUrl.endsWith(SPConstants.SLASH)) {
            sharepointUrl = sharepointUrl.substring(0, sharepointUrl.lastIndexOf(SPConstants.SLASH));
        }

        try {
            final URL url = new URL(sharepointUrl);
            int port = url.getPort();
            if (-1 == port) {
                port = url.getDefaultPort();
            }
            siteURL = url.getProtocol() + SPConstants.URL_SEP + url.getHost()
                    + SPConstants.COLON + port + url.getPath();
        } catch (final MalformedURLException e) {
            throw new SharepointException(
                    "Failed to construct sharepoint URL...", e);
        }
    }

    /**
     * @return the domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the connector instance directory
     */
    public String getGoogleConnectorWorkDir() {
        return googleConnectorWorkDir;
    }

    /**
     * @param indomain
     */
    public void setDomain(final String indomain) {
        domain = indomain;
    }

    /**
     * @param inPassword
     */
    public void setPassword(final String inPassword) {
        password = inPassword;
    }

    /**
     * @param inUsername
     */
    public void setUsername(final String inUsername) {
        username = inUsername;
    }

    /**
     * @param workDir
     */
    public void setGoogleConnectorWorkDir(final String workDir) {
        googleConnectorWorkDir = workDir;
    }

    /**
     * @return the excluded URL list
     */
    public String[] getExcludedURlList() {
        return excludedURlList;
    }

    /**
     * @param excludedURls
     * @param separator
     */
    public void setExcludedURlList(final String excludedURls,
            final String separator) {
        if (excludedURls != null) {
            excludedURlList = excludedURls.split(separator);
        }
    }

    /**
     * @param excludedURls
     */
    public void setExcludedURlList(final String excludedURls) {
        if (excludedURls != null) {
            excludedURlList = excludedURls.split(SPConstants.SEPARATOR);
        }
    }

    /**
     * @param includedURls
     * @param separator
     */
    public void setIncludedURlList(final String includedURls,
            final String separator) {
        if (includedURls != null) {
            includedURlList = includedURls.split(separator);
        }
    }

    /**
     * @param includedURls
     */
    public void setIncludedURlList(final String includedURls) {
        if (includedURls != null) {
            includedURlList = includedURls.split(SPConstants.SEPARATOR);
        }
    }

    /**
     * @return the included URL list
     */
    public String[] getIncludedURlList() {
        return includedURlList;
    }

    /**
     * @return the MySite Base URL
     */
    public String getMySiteBaseURL() {
        return mySiteBaseURL;
    }

    /**
     * @param inMySiteBaseURL
     */
    public void setMySiteBaseURL(final String inMySiteBaseURL) {
        mySiteBaseURL = inMySiteBaseURL;
    }

    /**
     * @return the Site Alias MAp
     */
    public Map<String, String> getAliasMap() {
        return aliasMap;
    }

    /**
     * @param inAliasMap
     */
    public void setSiteAlias(final Map<String, String> inAliasMap) {
        aliasMap = inAliasMap;
    }

    /**
     * @return excluded metadata list
     */
    public ArrayList<Pattern> getExcluded_metadata() {
        return excluded_metadata;
    }

    /**
     * @param inExcluded_metadata
     */
    public void setExcluded_metadata(final ArrayList<String> inExcluded_metadata) {
        if (inExcluded_metadata != null) {
            final int size = inExcluded_metadata.size();
            for (int index = 0; index < size; index++) {
                final String meta = (String) inExcluded_metadata.get(index);
                try {
                    excluded_metadata.add(Pattern.compile(meta));
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "One of the metadata under excluded_metadata is invalid as GNU Regexp. meta ["
                            + meta + "]. ");
                }
            }
        }
    }

    /**
     * @return included metadata list
     */
    public ArrayList<Pattern> getIncluded_metadata() {
        return included_metadata;
    }

    /**
     * @param inIncluded_metadata
     */
    public void setIncluded_metadata(final ArrayList<String> inIncluded_metadata) {
        if (inIncluded_metadata != null) {
            final int size = inIncluded_metadata.size();
            for (int index = 0; index < size; index++) {
                final String meta = (String) inIncluded_metadata.get(index);
                try {
                    included_metadata.add(Pattern.compile(meta));
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "One of the metadata under included_metadata is invalid as GNU Regexp. meta ["
                            + meta + "]. ");
                }
            }
        }
    }

    /**
     * @return FQDN conversion value
     */
    public boolean isFQDNConversion() {
        return bFQDNConversion;
    }

    /**
     * @param conversion
     */
    public void setFQDNConversion(final boolean conversion) {
        bFQDNConversion = conversion;
    }

    /**
     * @return batch hint
     */
    public int getBatchHint() {
        return batchHint;
    }

    /**
     * @param batchHint
     */
    public void setBatchHint(final int batchHint) {
        this.batchHint = batchHint;
    }

    /**
     * Parses the received value of Alias source pattern and values in a string
     * and updates the map.
     *
     * @param aliasMapString The comples string containg all the entries made on
     *            the configuration form. Entries of two consecutive rows are
     *            separated by /$$CRLF$$/ A Source pattern is separated by its
     *            corresponding Alias pattern by /$$EQUAL$$/
     * @return A Map depicting the actual mapping specified by the user on the
     *         configuration page
     */
    private Map<String, String> parseAlias(final String aliasMapString) {
        LOGGER.config("parsing aliasString: " + aliasMapString);
        if ((aliasMapString == null) || aliasMapString.equals("")) {
            return null;
        }
        final Map<String, String> aliasMap = new LinkedHashMap<String, String>();
        final String[] aliasValues = aliasMapString.split(SPConstants.ALIAS_ENTRIES_SEPARATOR);
        for (String element : aliasValues) {
            String alias_url = "";
            String source_url = "";
            if (element != null) {
                final String[] alias_value = element.split(SPConstants.SOURCE_ALIAS_SEPARATOR);
                if (alias_value.length == 2) {
                    source_url = alias_value[0];
                    alias_url = alias_value[1];
                } else {
                    continue;
                }
                LOGGER.config("updating AliasMap Key[alias_source="
                        + source_url + "] Value[alias_host_port=" + alias_url
                        + "]");
                aliasMap.put(source_url, alias_url);
            }
        }
        return aliasMap;
    }

    /**
     * @return the feedType
     */
    public FeedType getFeedType() {
        return feedType;
    }

    /**
     * @param inFeedType the feedType to set
     */
    public void setFeedType(final FeedType inFeedType) {
        feedType = inFeedType;
    }

    /**
     * Check the connectivity to a given URL by making HTTP head request.
     *
     * @param strURL The URL to be checked
     * @return the HTTP response code
     */
    public int checkConnectivity(final String strURL, HttpMethodBase method)
            throws Exception {
        LOGGER.log(Level.CONFIG, "Connecting [ " + strURL + " ] ....");
        int responseCode = 0;
        String username = this.username;
        final String host = Util.getHost(strURL);
        Credentials credentials = null;
        boolean kerberos = false;
        boolean ntlm = true; // We first try to use ntlm

        if (kdcServer != null
                && !kdcServer.equalsIgnoreCase(SPConstants.BLANK_STRING)) {
            credentials = new NTCredentials(username, password, host, domain);
            kerberos = true;
        } else if (!kerberos && null != domain && !domain.equals("")) {
            credentials = new NTCredentials(username, password, host, domain);
        } else {
            credentials = new UsernamePasswordCredentials(username, password);
            ntlm = false;
        }
        final HttpClient httpClient = new HttpClient();

        httpClient.getState().setCredentials(AuthScope.ANY, credentials);
        if (null == method) {
            method = new HeadMethod(strURL);
        }
        responseCode = httpClient.executeMethod(method);
        if (responseCode == 401 && ntlm && !kerberos) {
            LOGGER.log(Level.FINE, "Trying with HTTP Basic.");
            username = Util.getUserNameWithDomain(this.username, domain);
            credentials = new UsernamePasswordCredentials(username, password);
            httpClient.getState().setCredentials(AuthScope.ANY, credentials);
            responseCode = httpClient.executeMethod(method);
        }
        if (responseCode != 200) {
            LOGGER.log(Level.WARNING, "responseCode: " + responseCode);
        }
        return responseCode;
    }

    /**
     * Detect SharePoint type from the URL
     *
     * @param strURL
     * @return the SharePoint Type of the siteURL being passed
     */
    public SPConstants.SPType checkSharePointType(String strURL) {
        LOGGER.log(Level.CONFIG, "Checking [ " + strURL
                + " ] for the SharePoint version.");

        strURL = Util.encodeURL(strURL);
        HttpMethodBase method = null;
        try {
            method = new HeadMethod(strURL);
            checkConnectivity(strURL, method);
            if (null == method) {
                return null;
            }
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Unable to connect " + strURL, e);
            return null;
        }
        if (null == method) {
            return null;
        }
        final Header contentType = method.getResponseHeader("MicrosoftSharePointTeamServices");
        String version = null;
        if (contentType != null) {
            version = contentType.getValue();
        }
        LOGGER.info("SharePoint Version: " + version);
        if (version == null) {
            LOGGER.warning("Sharepoint version not found for the site [ "
                    + strURL + " ]");
            return null;
        }

        /*
          Adding support for SharePoint 2010. For SP2010, version starts with 14.x.x.x
          Since SP2010 supports all web services of SP2007 return SP2007 as version

          Fix Details:
          ------------
          SharePoint connector requires to know the version of the SharePoint repository<br/>
          for following
          a) MySite\Personal Site handling which is different in SP2003 & SP2007
                Note: current mysite handling fails for SP2010.<br/>
                     However mysite URLs can be discovered using the custom site discovery WS.
          b) Content Feed\Bulk AuthZ: This is achieved through custom web services which is supported on SP2007
                Note: Checked that same web services work for SP2010 as well

         */


        if ((version.trim().startsWith("12"))||(version.trim().startsWith("14"))) {
            return SPType.SP2007;
        } else if (version.trim().startsWith("6")) {
            return SPType.SP2003;
        } else {
            LOGGER.warning("Unknown sharepoint version found for the site [ "
                    + strURL + " ]");
            return null;
        }
    }

    /**
     * Check if the String Value can be included or not
     *
     * @param strValue The URL to be checked
     */
    public boolean isIncludedUrl(final String strValue) {
        if (includedURlList == null) {
            LOGGER.log(Level.WARNING, "Can not find include URLs");
            return false;
        }
        try {
            if ((strValue != null) && (strValue.length() != 0)) {
                if (Util.match(includedURlList, strValue, null)) {
                    final StringBuffer matchedPattern = new StringBuffer();
                    if (excludedURlList == null) {
                        return true;
                    } else if (Util.match(excludedURlList, strValue, matchedPattern)) {
                        if (matchedPattern != null) {
                            logExcludedURL("[ "
                                    + strValue
                                    + " ] matched aginst the Excluded URL Pattern: "
                                    + matchedPattern.toString());
                        }
                        return false;
                    }
                    return true;
                } else {
                    logExcludedURL("[ "
                            + strValue
                            + " ] did not match against the Included URL Pattern(s).");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Probelm while metadata filtering. strValue [ "
                    + strValue + " ]. ");
        }
        return false;
    }

    /**
     * Check if the Metadata can be included or not. Applies two level filter:
     * Level1: Only those metadata which are specified under included_metadata
     * should be included. If included_metadata is empty, include all. Level2:
     * exclude all metadata which are spcified under excluded_metadata.
     *
     * @param metadata Metadata to be checked
     */
    public boolean isIncludeMetadata(final String metadata) {
        if (metadata == null) {
            return false;
        }

        boolean status = false;
        Pattern pattern = null;
        if ((included_metadata != null) && (included_metadata.size() > 0)) {
            for (int index = 0; index < included_metadata.size(); ++index) {
                pattern = included_metadata.get(index);
                try {
                    final Matcher match = pattern.matcher(metadata);
                    if (match.find()) {
                        status = true;
                        break;
                    }
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "Unable to match the metadata [ "
                            + metadata
                            + " ] against one of the included_metadata. ", e);
                    continue;
                }
            }
        } else {
            status = true;
        }

        if (status && (excluded_metadata != null)) {
            for (int index = 0; index < excluded_metadata.size(); ++index) {
                pattern = excluded_metadata.get(index);
                try {
                    final Matcher match = pattern.matcher(metadata);
                    if (match.find()) {
                        status = false;
                        break;
                    }
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "Unable to match the metadata [ "
                            + metadata
                            + " ] against one of the excluded_metadata. ", e);
                    continue;
                }
            }
        }

        return status;
    }

    /**
     * Logs the excluded URL
     *
     * @param info
     */
    public void logExcludedURL(final String info) {
        // If the parent directory does not exist, create one
        File file = new File(excludedURL_ParentDir);
        if ((file == null) || !file.exists() || !file.isDirectory()) {
            LOGGER.log(Level.WARNING, "Excluded URL parent directory not found! Creating one... status: "
                    + file.mkdir());
        }

        final String fileName = excludedURL_ParentDir + SPConstants.SLASH
                + SPConstants.EXCLUDED_URL_LOG + 0 + ".txt";
        file = new File(fileName);

        try {
            if (!file.exists()) {
                LOGGER.log(Level.INFO, "creating " + fileName + " ... status: "
                        + file.createNewFile());
            } else if (file.length() > SPConstants.EXCLUDED_URL_MAX_SIZE) { // rotate
                // the
                // logs
                // Delete the oldest log file
                final String tmpFileName = excludedURL_ParentDir
                        + SPConstants.SLASH + SPConstants.EXCLUDED_URL_LOG
                        + SPConstants.EXCLUDED_URL_MAX_COUNT + ".txt";
                File tmpFile = new File(tmpFileName);
                LOGGER.log(Level.INFO, "Deleting " + tmpFileName
                        + " to rotate... status:" + tmpFile.delete());

                for (int i = SPConstants.EXCLUDED_URL_MAX_COUNT - 1; i >= 0; --i) {
                    tmpFile = new File(excludedURL_ParentDir
                            + SPConstants.SLASH + SPConstants.EXCLUDED_URL_LOG
                            + i + ".txt");
                    tmpFile.renameTo(new File(excludedURL_ParentDir
                            + SPConstants.SLASH + SPConstants.EXCLUDED_URL_LOG
                            + (i + 1) + ".txt"));
                }
                LOGGER.log(Level.INFO, "creating " + fileName + " ... status: "
                        + file.createNewFile());
            }
            Util.logInfo(fileName, info);
        } catch (final IOException e) {
            LOGGER.log(Level.WARNING, "Problem while logging excluded URLs", e);
        } catch (final Throwable e) {
            LOGGER.log(Level.WARNING, "Problem while logging excluded URLs", e);
        }
    }

    /**
     * Deletes all the excluded URL logs
     */
    public void clearExcludedURLLogs() {
        LOGGER.log(Level.INFO, "Cleaning all the excluded URL logs...");
        final File file = new File(excludedURL_ParentDir);
        if ((file == null) || !file.exists() || !file.isDirectory()) {
            LOGGER.log(Level.WARNING, "Excluded URL parent directory not found! ");
            return;
        }
        final File[] logs = file.listFiles();
        if (logs == null) {
            return;
        }
        for (File element : logs) {
            if ((element == null) || !element.exists()) {
                continue;
            }
            LOGGER.log(Level.INFO, "Deleting " + element.getAbsolutePath()
                    + " ... status:" + element.delete());
        }
        LOGGER.log(Level.INFO, "Deleting " + excludedURL_ParentDir
                + " ... status:" + file.delete());
    }

    /**
     * @return the siteURL
     */
    public String getSiteURL() {
        return siteURL;
    }

    public void setKdcServer(String kdcServer) {
        this.kdcServer = kdcServer;
    }

    /**
     * Returns the traversal context
     *
     * @return the traversalContext
     */
    public TraversalContext getTraversalContext() {
        return traversalContext;
    }

    /**
     * Sets the traversal context
     *
     * @param traversalContext the traversalContext to set
     */
    public void setTraversalContext(TraversalContext traversalContext) {
        this.traversalContext = traversalContext;
    }

    public boolean isPushAcls() {
        return pushAcls;
    }

    public void setPushAcls(boolean pushAcls) {
        this.pushAcls = pushAcls;
    }

    public UserDataStoreDAO getUserDataStoreDAO() {
        return userDataStoreDAO;
    }

    public void setUserDataStoreDAO(UserDataStoreDAO userDataStoreDAO) {
        this.userDataStoreDAO = userDataStoreDAO;
    }

    public boolean isUseSPSearchVisibility() {
        return useSPSearchVisibility;
    }

    public void setUseSPSearchVisibility(boolean useSPSearchVisibility) {
        this.useSPSearchVisibility = useSPSearchVisibility;
    }

    public List<String> getInfoPathBaseTemplate() {
        if (null == infoPathBaseTemplate) {
            infoPathBaseTemplate = new ArrayList<String>();
            infoPathBaseTemplate.add(SPConstants.ORIGINAL_BT_FORMLIBRARY);
        }
        return infoPathBaseTemplate;
    }

    public void setInfoPathBaseTemplate(List<String> infoPathBaseTemplate) {
        this.infoPathBaseTemplate = infoPathBaseTemplate;
    }

    public void setReWriteDisplayUrlUsingAliasMappingRules(
            boolean reWriteDisplayUrlUsingAliasMappingRules) {
        this.reWriteDisplayUrlUsingAliasMappingRules = reWriteDisplayUrlUsingAliasMappingRules;
    }

    public boolean isReWriteDisplayUrlUsingAliasMappingRules() {
        return reWriteDisplayUrlUsingAliasMappingRules;
    }

    public boolean isReWriteRecordUrlUsingAliasMappingRules() {
        return reWriteRecordUrlUsingAliasMappingRules;
    }

    public void setReWriteRecordUrlUsingAliasMappingRules(
            boolean reWriteRecordUrlUsingAliasMappingRules) {
        this.reWriteRecordUrlUsingAliasMappingRules = reWriteRecordUrlUsingAliasMappingRules;
    }

    public String getUsernameFormatInAce() {
        return usernameFormatInAce;
    }

    public void setUsernameFormatInAce(String usernameFormatInAce) {
        this.usernameFormatInAce = usernameFormatInAce;
    }

    public String getGroupnameFormatInAce() {
        return groupnameFormatInAce;
    }

    public void setGroupnameFormatInAce(String groupnameFormatInAce) {
        this.groupnameFormatInAce = groupnameFormatInAce;
    }

    public boolean isAppendNamespaceInSPGroup() {
        return appendNamespaceInSPGroup;
    }

    public void setAppendNamespaceInSPGroup(boolean appendNamespaceInSPGroup) {
        this.appendNamespaceInSPGroup = appendNamespaceInSPGroup;
    }

    /**
     * @return the fetchACLInBatches
     */
    public boolean isFetchACLInBatches() {
        return fetchACLInBatches;
    }

    /**
     * @param fetchACLInBatches the fetchACLInBatches to set
     */
    public void setFetchACLInBatches(boolean fetchACLInBatches) {
        this.fetchACLInBatches = fetchACLInBatches;
    }

    /**
     * @return the aclBatchSizeFactor
     */
    public int getAclBatchSizeFactor() {
        return aclBatchSizeFactor;
    }

    /**
     * @param aclBatchSizeFactor the aclBatchSizeFactor to set
     */
    public void setAclBatchSizeFactor(int aclBatchSizeFactor) {
        this.aclBatchSizeFactor = aclBatchSizeFactor;
    }

    /**
     * @return the webServiceTimeOut
     */
    public int getWebServiceTimeOut() {
        return webServiceTimeOut;
    }

    /**
     * @param webServiceTimeOut the webServiceTimeOut to set
     */
    public void setWebServiceTimeOut(int webServiceTimeOut) {
        this.webServiceTimeOut = webServiceTimeOut;
    }

    /**
     * @return initial LDAP user groups membership cache size.
     */
    public int getInitialCacheSize() {
        return initialCacheSize;
    }

    /**
     * @param initialCacheSize the initialCacheSize to set.
     */
    public void setInitialCacheSize(int initialCacheSize) {
        this.initialCacheSize = initialCacheSize;
    }

    /**
     * @return true if connector administrator configure to use cache for LDAP
     *         user groups membership.
     */
    public boolean isUseCacheToStoreLdapUserGroupsMembership() {
        return useCacheToStoreLdapUserGroupsMembership;
    }

    /**
     * @param useCacheToStoreLdapUserGroupsMembership the
     *            useCacheToStoreLdapUserGroupsMembership to set.
     */
    public void setUseCacheToStoreLdapUserGroupsMembership(
            boolean useCacheToStoreLdapUserGroupsMembership) {
        this.useCacheToStoreLdapUserGroupsMembership = useCacheToStoreLdapUserGroupsMembership;
    }

    /**
     * @return the interval to which LDAP cache should invalidate its cache.
     */
    public long getCacheRefreshInterval() {
        return cacheRefreshInterval;
    }

    /**
     * @param cacheRefreshInterval the cacheRefreshInterval to set.
     */
    public void setCacheRefreshInterval(long cacheRefreshInterval) {
        this.cacheRefreshInterval = cacheRefreshInterval;
    }

}
