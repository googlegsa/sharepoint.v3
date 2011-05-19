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

package com.google.enterprise.connector.sharepoint.wsclient;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.sitedata.SiteData;
import com.google.enterprise.connector.sharepoint.generated.sitedata.SiteDataLocator;
import com.google.enterprise.connector.sharepoint.generated.sitedata.SiteDataSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.generated.sitedata._sList;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOfStringHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOf_sFPUrlHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOf_sListHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOf_sListWithTimeHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOf_sWebWithTimeHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders._sWebMetadataHolder;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;

import org.apache.axis.AxisFault;
import org.apache.axis.holders.UnsignedIntHolder;

import java.rmi.RemoteException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.holders.StringHolder;

/**
 * This class holds data and methods for any call to SiteData web service.
 *
 * @author amit_kagrawal
 */
public class SiteDataWS {

    private final Logger LOGGER = Logger.getLogger(SiteDataWS.class.getName());
    private SharepointClientContext sharepointClientContext;
    private String endpoint;
    private SiteDataSoap_BindingStub stub = null;
    private static final String ATTR_READSECURITY = "ReadSecurity";

    /**
     * @param inSharepointClientContext The Context is passed so that necessary
     *            information can be used to create the instance of current
     *            class Web Service endpoint is set to the default SharePoint
     *            URL stored in SharePointClientContext.
     * @throws SharepointException
     */
    public SiteDataWS(final SharepointClientContext inSharepointClientContext)
            throws SharepointException {
        if (inSharepointClientContext != null) {
            sharepointClientContext = inSharepointClientContext;
            endpoint = Util.encodeURL(sharepointClientContext.getSiteURL())
                    + SPConstants.SITEDATAENDPOINT;
            LOGGER.log(Level.CONFIG, "Endpoint set to: " + endpoint);

            final SiteDataLocator loc = new SiteDataLocator();
            loc.setSiteDataSoapEndpointAddress(endpoint);
            final SiteData servInterface = loc;

            try {
                stub = (SiteDataSoap_BindingStub) servInterface.getSiteDataSoap();
            } catch (final ServiceException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
                throw new SharepointException("unable to create sitedata stub");
            }

            final String strDomain = inSharepointClientContext.getDomain();
            String strUser = inSharepointClientContext.getUsername();
            final String strPassword = inSharepointClientContext.getPassword();

            strUser = Util.getUserNameWithDomain(strUser, strDomain);
            stub.setUsername(strUser);
            stub.setPassword(strPassword);
            // The web service time-out value
            stub.setTimeout(sharepointClientContext.getWebServiceTimeOut());
            LOGGER.fine("Set time-out of : "
                    + sharepointClientContext.getWebServiceTimeOut()
                    + " milliseconds");
        }
    }

    /**
     * Gets the collection of all the lists on the sharepoint server which are
     * of a given type. E.g., DocumentLibrary
     *
     * @param webstate The web from which the list/libraries are to be
     *            discovered
     * @return list of BaseList objects.
     */
    public List<ListState> getNamedLists(final WebState webstate)
            throws SharepointException {
        final ArrayList<ListState> listCollection = new ArrayList<ListState>();
        if (stub == null) {
            LOGGER.warning("Unable to get the list collection because stub is null");
            return listCollection;
        }

        if (webstate == null) {
            LOGGER.warning("Unable to get the list collection because webstate is null");
            return listCollection;
        }

        final Collator collator = Util.getCollator();
        final ArrayOf_sListHolder vLists = new ArrayOf_sListHolder();
        final UnsignedIntHolder getListCollectionResult = new UnsignedIntHolder();

        try {
            stub.getListCollection(getListCollectionResult, vLists);
        } catch (final AxisFault af) { // Handling of username formats for
            // different authentication models.
            if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
                    && (sharepointClientContext.getDomain() != null)) {
                final String username = Util.switchUserNameFormat(stub.getUsername());
                LOGGER.log(Level.CONFIG, "Web Service call failed for username [ "
                        + stub.getUsername() + " ]. Trying with " + username);
                stub.setUsername(username);
                try {
                    stub.getListCollection(getListCollectionResult, vLists);
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "Unable to get List Collection for stubURL[ "
                            + sharepointClientContext.getSiteURL() + " ].", e);
                }
            } else {
                LOGGER.log(Level.WARNING, "Unable to get List Collection for stubURL[ "
                        + sharepointClientContext.getSiteURL() + " ].", af);
            }
        } catch (final Throwable e) {
            LOGGER.log(Level.WARNING, "Unable to get List Collection for stubURL[ "
                    + sharepointClientContext.getSiteURL() + " ].", e);
        }

        if (vLists == null) {
            LOGGER.log(Level.WARNING, "Unable to get the list collection");
            return listCollection;
        }

        try {
            final _sList[] sl = vLists.value;

            if (sl != null) {
                webstate.setExisting(true);
                for (_sList element : sl) {
                    String url = null;
                    String strBaseTemplate = null;

                    if (element == null) {
                        continue;
                    }

                    final String baseType = element.getBaseType();
                    LOGGER.log(Level.FINE, "Base Type returned by the Web Service : "
                            + baseType);
                    if (!collator.equals(baseType, (SPConstants.DISCUSSION_BOARD))
                            && !collator.equals(baseType, (SPConstants.DOC_LIB))
                            && !collator.equals(baseType, (SPConstants.GENERIC_LIST))
                            && !collator.equals(baseType, (SPConstants.ISSUE))
                            && !collator.equals(baseType, (SPConstants.SURVEYS))) {
                        continue;
                    }

                    url = Util.getWebApp(sharepointClientContext.getSiteURL())
                            + element.getDefaultViewUrl();

                    strBaseTemplate = element.getBaseTemplate();
                    if (strBaseTemplate == null) {
                        strBaseTemplate = SPConstants.NO_TEMPLATE;
                    } else if (collator.equals(strBaseTemplate, SPConstants.ORIGINAL_BT_SLIDELIBRARY)) {// for
                        // SlideLibrary
                        strBaseTemplate = SPConstants.BT_SLIDELIBRARY;
                    } else if (collator.equals(strBaseTemplate, SPConstants.ORIGINAL_BT_TRANSLATIONMANAGEMENTLIBRARY)) {// for
                        // TranslationManagementLibrary
                        strBaseTemplate = SPConstants.BT_TRANSLATIONMANAGEMENTLIBRARY;
                    } else if (collator.equals(strBaseTemplate, SPConstants.ORIGINAL_BT_TRANSLATOR)) {// for
                        // Translator
                        strBaseTemplate = SPConstants.BT_TRANSLATOR;
                    } else if (collator.equals(strBaseTemplate, SPConstants.ORIGINAL_BT_REPORTLIBRARY)) {// for
                        // ReportLibrary
                        strBaseTemplate = SPConstants.BT_REPORTLIBRARY;
                    } else if (collator.equals(strBaseTemplate, SPConstants.ORIGINAL_BT_PROJECTTASK)) {// for
                        // ReportLibrary
                        strBaseTemplate = SPConstants.BT_PROJECTTASK;
                    } else if (collator.equals(strBaseTemplate, SPConstants.ORIGINAL_BT_SITESLIST)) {// for
                        // ReportLibrary
                        strBaseTemplate = SPConstants.BT_SITESLIST;
                    } else {
                        // for FormLibrary
                        for(String formTemplate : sharepointClientContext.getInfoPathBaseTemplate()) {
                            if (collator.equals(strBaseTemplate, formTemplate)) {
                                strBaseTemplate = SPConstants.BT_FORMLIBRARY;
                                break;
                            }
                        }
                    }

                    LOGGER.config("List URL :" + url);

                    // Children of all URLs are discovered
                    ListState list = new ListState(
                            element.getInternalName(),
                            element.getTitle(),
                            element.getBaseType(),
                            Util.siteDataStringToCalendar(element.getLastModified()),
                            strBaseTemplate, url, webstate);

                    list.setInheritedSecurity(element.isInheritedSecurity());

                    String myNewListConst = "";
                    final String listUrl = element.getDefaultViewUrl();// e.g.
                    // /sites/abc/Lists/Announcements/AllItems.aspx
                    LOGGER.log(Level.FINE, "getting listConst for list URL [ "
                            + listUrl + " ] ");
                    if ((listUrl != null) /* && (siteRelativeUrl!=null) */) {
                        final StringTokenizer strTokList = new StringTokenizer(
                                listUrl, SPConstants.SLASH);
                        if (null != strTokList) {
                            while ((strTokList.hasMoreTokens())
                                    && (strTokList.countTokens() > 1)) {
                                final String listToken = strTokList.nextToken();
                                if (list.isDocumentLibrary()
                                        && listToken.equals(SPConstants.FORMS_LIST_URL_SUFFIX)
                                        && (strTokList.countTokens() == 1)) {
                                    break;
                                }
                                if (null != listToken) {
                                    myNewListConst += listToken
                                            + SPConstants.SLASH;
                                }
                            }
                            list.setListConst(myNewListConst);
                            LOGGER.log(Level.CONFIG, "using listConst [ "
                                    + myNewListConst + " ] for list URL [ "
                                    + listUrl + " ] ");

                            // Apply the URL filter here

                            // check if the entire list subtree is to excluded
                            // by comparing the prefix of the list URL with the
                            // patterns
                            if (sharepointClientContext.isIncludedUrl(webstate.getWebUrl()
                                    + SPConstants.SLASH + myNewListConst)) {
                                // is included check if actual list url itself
                                // is to be excluded
                                if (sharepointClientContext.isIncludedUrl(url)) {
                                    // if a List URL is included, it WILL be
                                    // sent as a
                                    // Document
                                    list.setSendListAsDocument(true);
                                } else {
                                    // if a List URL is EXCLUDED, it will NOT be
                                    // sent as a
                                    // Document
                                    list.setSendListAsDocument(false);
                                    LOGGER.warning("excluding "
                                            + url.toString());
                                }
                                // add the attribute(Metadata to the list )
                                list = getListWithAllAttributes(list, element);

                                listCollection.add(list);
                            } else {
                                // entire subtree is to be excluded
                                // do not construct list state
                                LOGGER.warning("Excluding " + url
                                        + " because entire subtree of "
                                        + myNewListConst + " is excluded");
                            }
                        }
                    }

                    // Sort the base list
                    Collections.sort(listCollection);
                    // dumpcollection(listCollection);
                }
            }
        } catch (final Throwable e) {
            LOGGER.log(Level.FINER, e.getMessage(), e);
            return listCollection;
        }

        if (listCollection.size() > 0) {
            LOGGER.info("Discovered " + listCollection.size()
                    + " lists/libraries under site [ " + webstate
                    + " ] for crawling");
        } else {
            LOGGER.config("No lists/libraries to crawl under site [ "
                    + webstate + " ]");
        }
        return listCollection;
    }

    /**
     * The metadata for the list/library are set. This is required because lists
     * are also send as a document for indexing.
     *
     * @param list
     * @param documentLibrary
     * @return the {@link ListState}
     */
    private ListState getListWithAllAttributes(final ListState list,
            final _sList documentLibrary) {
        if ((list == null) || (documentLibrary == null)) {
            return list;
        }

        String str = "";
        str = documentLibrary.getDefaultViewUrl();
        if ((str != null) && (!str.trim().equals(""))) {
            list.setAttribute(SPConstants.ATTR_DEFAULTVIEWURL, str);
        }
        str = "";
        str = documentLibrary.getDescription();
        if ((str != null) && (!str.trim().equals(""))) {
            list.setAttribute(SPConstants.ATTR_DESCRIPTION, str);
        }
        str = "";
        str = documentLibrary.getTitle();
        if ((str != null) && (!str.trim().equals(""))) {
            list.setAttribute(SPConstants.ATTR_TITLE, str);
        }
        str = "";
        str += documentLibrary.getReadSecurity();
        if ((str != null) && (!str.trim().equals(""))) {
            list.setAttribute(ATTR_READSECURITY, str);
        }

        return list;
    }

    // for debugging purpose
    /*
     * private void dumpcollection(ArrayList colln){ if(colln==null){ return; }
     * LOGGER.config("-----------------------------------"); for(int
     * i=0;i<colln.size();++i){ BaseList list = (BaseList) colln.get(i);
     * LOGGER.config("Internal Name: "+list.getInternalName());
     * LOGGER.config("Title: "+list.getTitle());
     * LOGGER.config("Type: "+list.getType());
     * LOGGER.config("Type: "+list.getLastMod()); }
     * LOGGER.config("-----------------------------------"); }
     */

    /**
     * Retrieves the title of a Web Site. Should only be used in case of SP2003
     * Top URL. For all other cases, WebWS.getTitle() is the preffered method.
     */
    String getTitle() throws RemoteException {
        final UnsignedIntHolder getWebResult = new UnsignedIntHolder();
        final _sWebMetadataHolder sWebMetadata = new _sWebMetadataHolder();
        final ArrayOf_sWebWithTimeHolder vWebs = new ArrayOf_sWebWithTimeHolder();
        final ArrayOf_sListWithTimeHolder vLists = new ArrayOf_sListWithTimeHolder();
        final ArrayOf_sFPUrlHolder vFPUrls = new ArrayOf_sFPUrlHolder();
        final StringHolder strRoles = new StringHolder();
        final ArrayOfStringHolder vRolesUsers = new ArrayOfStringHolder();
        final ArrayOfStringHolder vRolesGroups = new ArrayOfStringHolder();
        try {
            stub.getWeb(getWebResult, sWebMetadata, vWebs, vLists, vFPUrls, strRoles, vRolesUsers, vRolesGroups);
            return sWebMetadata.value.getTitle();
        } catch (final AxisFault af) { // Handling of username formats for
            // different authentication models.
            if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
                    && (sharepointClientContext.getDomain() != null)) {
                final String username = Util.switchUserNameFormat(stub.getUsername());
                LOGGER.log(Level.CONFIG, "Web Service call failed for username [ "
                        + stub.getUsername() + " ]. Trying with " + username);
                stub.setUsername(username);
                try {
                    stub.getWeb(getWebResult, sWebMetadata, vWebs, vLists, vFPUrls, strRoles, vRolesUsers, vRolesGroups);
                    return sWebMetadata.value.getTitle();
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "Unable to get the title for web beacuse call to the WS failed. endpoint [ "
                            + endpoint + " ]", e);
                    return "";
                }
            } else {
                LOGGER.log(Level.WARNING, "Unable to get the title for web beacuse call to the WS failed. endpoint [ "
                        + endpoint + " ]", af);
                return "";
            }
        } catch (final Throwable e) {
            LOGGER.log(Level.WARNING, "Unable to get the title for web beacuse call to the WS failed. endpoint [ "
                    + endpoint + " ]", e);
            return "";
        }
    }

    /**
     * Makes a call to Site Data web service to retrieve site meta data and create a SPDocuemnt
     * and it returns a single SPDcoument.This method returns null if and only if any one of
     * SiteData stub or webState is null.
     *
     * @param webState The web from which we need to construct SPDcoument for it's landing page.
     * @return a single SPDocument for the given web.
     * @throws SharepointException
     */
    public SPDocument getSiteData(final WebState webState) throws SharepointException {
        final UnsignedIntHolder getWebResult = new UnsignedIntHolder();
        final _sWebMetadataHolder sWebMetadata = new _sWebMetadataHolder();
        final ArrayOf_sWebWithTimeHolder vWebs = new ArrayOf_sWebWithTimeHolder();
        final ArrayOf_sListWithTimeHolder vLists = new ArrayOf_sListWithTimeHolder();
        final ArrayOf_sFPUrlHolder vFPUrls = new ArrayOf_sFPUrlHolder();
        final StringHolder strRoles = new StringHolder();
        final ArrayOfStringHolder vRolesUsers = new ArrayOfStringHolder();
        final ArrayOfStringHolder vRolesGroups = new ArrayOfStringHolder();

        if (stub == null) {
            LOGGER.warning("Unable to get the list collection because stub is null");
            //in case if SiteData web service end point is not created properly.
            return null;
        }

        if (webState == null) {
            LOGGER.warning("Unable to get the list collection because webstate is null");
            //in case if the web state is null and is not existing in SharePoint server.
            return null;
        }
        try {
            stub.getWeb(getWebResult, sWebMetadata, vWebs, vLists, vFPUrls, strRoles, vRolesUsers, vRolesGroups);
        } catch (final AxisFault af) {
            // Handling of username formats for
            // different authentication models.
            if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
                    && (sharepointClientContext.getDomain() != null)) {
                final String username = Util.switchUserNameFormat(stub.getUsername());
                LOGGER.log(Level.CONFIG, "Web Service call failed for username [ "
                        + stub.getUsername() + " ]. Trying with " + username);
                stub.setUsername(username);
                try {
                    stub.getWeb(getWebResult, sWebMetadata, vWebs, vLists, vFPUrls, strRoles, vRolesUsers, vRolesGroups);
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "Unable to get site data for web beacuse call to the WS failed. endpoint [ "
                            + endpoint + " ]", e);
                }
            } else {
                LOGGER.log(Level.WARNING, "Unable to get site data for web beacuse call to the WS failed. endpoint [ "
                        + endpoint + " ]", af);
            }
        } catch (final Throwable e) {
            LOGGER.log(Level.WARNING, "Unable to get the data for web beacuse call to the WS failed. endpoint [ "
                    + endpoint + " ]", e);
        }

        final SPDocument siteDataDocument = new SPDocument(webState.getPrimaryKey()
                + SPConstants.DEFAULT_SITE_LANDING_PAGE + SPConstants.DOC_TOKEN
                + sWebMetadata.value.getWebID(),
                webState.getWebUrl()+ SPConstants.DEFAULT_SITE_LANDING_PAGE, sWebMetadata.value.getLastModified().getInstance(),
                sWebMetadata.value.getAuthor(), SPConstants.SITE, webState.getTitle(),
                sharepointClientContext.getFeedType(),
                webState.getSharePointType());

        return siteDataDocument;
    }

}
