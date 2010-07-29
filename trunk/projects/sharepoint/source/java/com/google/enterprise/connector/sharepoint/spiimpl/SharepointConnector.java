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

package com.google.enterprise.connector.sharepoint.spiimpl;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.wsclient.GssAclWS;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

/**
 * Implementation of the Connector interface from the spi for SharePoint This is
 * the primary class which represents a new connector instance. Every time a new
 * connector instance is created, an object of this class is created.
 *
 * @author nitendra_thakur
 */
public class SharepointConnector implements Connector {
    private static final Logger LOGGER = Logger.getLogger(SharepointConnector.class.getName());
    private SharepointClientContext sharepointClientContext = null;

    private String sharepointUrl;
    private String kdcserver;
    private String domain;
    private String username;
    private String password;
    private String googleConnectorWorkDir = null;
    private String excludedURls = null;
    private String includedURls = null;
    private String mySiteBaseURL = null;
    private boolean FQDNConversion = false;
    private ArrayList<String> included_metadata = null;
    private ArrayList<String> excluded_metadata = null;
    private String aliasMap = null;
    private String authorizationAsfeedType = null;
    private boolean pushAcls = true;
    private boolean stripDomainFromAces = true;
    private boolean useSPSearchVisibility = true;
    private String infoPathBaseTemplate = SPConstants.ORIGINAL_BT_FORMLIBRARY;

    public SharepointConnector() {

    }

    /**
     * sets the FQDNConversion parameter.
     *
     * @param conversion If true: tries to convert the non-FQDN URLs to FQDN If
     *            false: no conversion takes place
     */
    public void setFQDNConversion(final boolean conversion) {
        FQDNConversion = conversion;
        if (sharepointClientContext != null) {
            sharepointClientContext.setFQDNConversion(conversion);
        }
        LOGGER.config("FQDN Value Set to [" + conversion + "]");
    }

    /**
     * returns a session object for the current connector instance
     */
    public Session login() throws RepositoryException {
        LOGGER.info("login()");
        // Check the availability of GSS services and set the flags in context
        // so that connector does not attempt to make any calls to these
        // services in the current session
        // For now, checking for ACL only
        if (sharepointClientContext.isPushAcls()) {
            GssAclWS aclWs = new GssAclWS(sharepointClientContext, null);
            String status = aclWs.checkConnectivity();
            if (!SPConstants.CONNECTIVITY_SUCCESS.equals(status)) {
                sharepointClientContext.setPushAcls(false);
                LOGGER.log(Level.WARNING, "ACL will not be sent for the documents because the ACL web service is not accessible.");
            }
        }
        return new SharepointSession(this, sharepointClientContext);
    }

    /**
     * Sets the metadata to be included
     *
     * @param inExcluded_metadata
     */
    public void setExcluded_metadata(final ArrayList<String> inExcluded_metadata) {
        excluded_metadata = inExcluded_metadata;
        if (sharepointClientContext != null) {
            sharepointClientContext.setExcluded_metadata(inExcluded_metadata);
        }
        LOGGER.config("excluded_metadata Set to ["
                + inExcluded_metadata.toString() + "]");
    }

    /**
     * Sets the excluded metadata
     *
     * @param inIncluded_metadata
     */
    public void setIncluded_metadata(final ArrayList<String> inIncluded_metadata) {
        included_metadata = inIncluded_metadata;
        if (sharepointClientContext != null) {
            sharepointClientContext.setIncluded_metadata(inIncluded_metadata);
        }
        LOGGER.config("included_metadata Set to ["
                + inIncluded_metadata.toString() + "]");
    }

    /**
     * @return the sharepointUrl
     */
    public String getSharepointUrl() {
        return sharepointUrl;
    }

    /**
     * @param sharepointUrl the sharepointUrl to set
     */
    public void setSharepointUrl(final String sharepointUrl) {
        this.sharepointUrl = sharepointUrl;
    }

    /**
     * @return the domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * @param domain the domain to set
     */
    public void setDomain(final String domain) {
        this.domain = domain;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * @return the googleConnectorWorkDir
     */
    public String getGoogleConnectorWorkDir() {
        return googleConnectorWorkDir;
    }

    /**
     * @param googleConnectorWorkDir the googleConnectorWorkDir to set
     */
    public void setGoogleConnectorWorkDir(final String googleConnectorWorkDir) {
        this.googleConnectorWorkDir = googleConnectorWorkDir;
    }

    /**
     * @return the excludedURls
     */
    public String getExcludedURls() {
        return excludedURls;
    }

    /**
     * @param excludedURls the excludedURls to set
     */
    public void setExcludedURls(final String excludedURls) {
        this.excludedURls = excludedURls;
    }

    /**
     * @return the includedURls
     */
    public String getIncludedURls() {
        return includedURls;
    }

    /**
     * @param includedURls the includedURls to set
     */
    public void setIncludedURls(final String includedURls) {
        this.includedURls = includedURls;
    }

    /**
     * @return the mySiteBaseURL
     */
    public String getMySiteBaseURL() {
        return mySiteBaseURL;
    }

    /**
     * @param mySiteBaseURL the mySiteBaseURL to set
     */
    public void setMySiteBaseURL(final String mySiteBaseURL) {
        this.mySiteBaseURL = mySiteBaseURL;
    }

    /**
     * @return the aliasMap
     */
    public String getAliasMap() {
        return aliasMap;
    }

    /**
     * @param aliasMap the aliasMap to set
     */
    public void setAliasMap(final String aliasMap) {
        this.aliasMap = aliasMap;
    }

    /**
     * @return the authorization
     */
    public String getAuthorization() {
        return authorizationAsfeedType;
    }

    /**
     * @param authorization the authorization to set
     */
    public void setAuthorization(final String authorization) {
        this.authorizationAsfeedType = authorization;
    }

    public void init() throws SharepointException {
        LOGGER.config("sharepointUrl = [" + sharepointUrl + "] , domain = ["
                + domain + "] , username = [" + username + "] , "
                + "googleConnectorWorkDir = [" + googleConnectorWorkDir
                + "] , includedURls = [" + includedURls + "] , "
                + "excludedURls = [" + excludedURls + "] , mySiteBaseURL = ["
                + mySiteBaseURL + "] , aliasHostPort = [" + aliasMap + "]");
        sharepointClientContext = new SharepointClientContext(sharepointUrl,
                domain, kdcserver, username, password, googleConnectorWorkDir,
                includedURls, excludedURls, mySiteBaseURL, aliasMap,
                FeedType.getFeedType(authorizationAsfeedType),
                useSPSearchVisibility);
        sharepointClientContext.setFQDNConversion(FQDNConversion);
        sharepointClientContext.setIncluded_metadata(included_metadata);
        sharepointClientContext.setExcluded_metadata(excluded_metadata);
        sharepointClientContext.setStripDomainFromAces(stripDomainFromAces);
        sharepointClientContext.setPushAcls(pushAcls);
        sharepointClientContext.setInfoPathBaseTemplate(infoPathBaseTemplate);
    }

    /**
     * @return the included_metadata
     */
    public ArrayList<String> getIncluded_metadata() {
        return included_metadata;
    }

    /**
     * @return the excluded_metadata
     */
    public ArrayList<String> getExcluded_metadata() {
        return excluded_metadata;
    }

public String getKdcserver() {
        return kdcserver;
    }

    public void setKdcserver(String kdcserver) {
        this.kdcserver = kdcserver;
    }

    public boolean isPushAcls() {
        return pushAcls;
    }

    public void setPushAcls(boolean pushAcls) {
        this.pushAcls = pushAcls;
    }

    public boolean isStripDomainFromAces() {
        return stripDomainFromAces;
    }

    public void setStripDomainFromAces(boolean stripDomainFromAces) {
        this.stripDomainFromAces = stripDomainFromAces;
    }

    public boolean isUseSPSearchVisibility() {
        return useSPSearchVisibility;
    }

    public void setUseSPSearchVisibility(boolean useSPSerachVisibility) {
        this.useSPSearchVisibility = useSPSerachVisibility;
    }

    public String getInfoPathBaseTemplate() {
        return infoPathBaseTemplate;
    }

    public void setInfoPathBaseTemplate(String infoPathBaseTemplate) {
        this.infoPathBaseTemplate = infoPathBaseTemplate;
    }
}
