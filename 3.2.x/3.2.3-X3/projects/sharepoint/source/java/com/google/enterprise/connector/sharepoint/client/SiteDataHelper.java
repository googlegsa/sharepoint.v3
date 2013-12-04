// Copyright 2007 Google Inc.
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

package com.google.enterprise.connector.sharepoint.client;

import com.google.common.base.Strings;
import com.google.enterprise.connector.sharepoint.generated.sitedata._sList;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOfStringHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOf_sListHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders._sWebMetadataHolder;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.sharepoint.wsclient.client.BaseWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDataWS;
import com.google.enterprise.connector.spi.SpiConstants.DocumentType;

import org.apache.axis.message.MessageElement;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.HeadMethod;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

/**
 * This class holds data and methods for any call to SiteData web service.
 *
 * @author amit_kagrawal
 */
public class SiteDataHelper {
  private static final Logger LOGGER =
      Logger.getLogger(SiteDataHelper.class.getName());
  private SharepointClientContext sharepointClientContext;
  private SiteDataWS siteDataWS;
  private static final String ATTR_READSECURITY = "ReadSecurity";

  /**
   * @param inSharepointClientContext The Context is passed so that necessary
   *          information can be used to create the instance of current class
   *          Web Service endpoint is set to the default SharePoint URL stored
   *          in SharePointClientContext.
   * @throws SharepointException
   */
  public SiteDataHelper(final SharepointClientContext 
      inSharepointClientContext) throws SharepointException {
    if (null == inSharepointClientContext) {
      throw new SharepointException("SharePointClient context cannot be null.");
    }
    sharepointClientContext = inSharepointClientContext;
    siteDataWS = sharepointClientContext.getClientFactory().getSiteDataWS(
        sharepointClientContext);

    final String strDomain = sharepointClientContext.getDomain();
    String strUser = sharepointClientContext.getUsername();
    final String strPassword = sharepointClientContext.getPassword();
    final int timeout = sharepointClientContext.getWebServiceTimeOut();
    LOGGER.fine("Setting time-out to " + timeout + " milliseconds.");

    strUser = Util.getUserNameWithDomain(strUser, strDomain);
    siteDataWS.setUsername(strUser);
    siteDataWS.setPassword(strPassword);
    siteDataWS.setTimeout(timeout);
  }

  /**
   * Gets the collection of all the lists on the sharepoint server which are of
   * a given type. E.g., DocumentLibrary
   *
   * @param webstate The web from which the list/libraries are to be discovered
   * @return list of BaseList objects.
   */
  public List<ListState> getNamedLists(final WebState webstate) {
    final ArrayList<ListState> listCollection = new ArrayList<ListState>();
    if (webstate == null) {
      LOGGER.warning("Unable to get the list collection because webstate is null");
      return listCollection;
    }

    final ArrayOf_sListHolder vLists =
        Util.makeWSRequest(sharepointClientContext, siteDataWS,
            new Util.RequestExecutor<ArrayOf_sListHolder>() {
          public ArrayOf_sListHolder onRequest(final BaseWS ws)
              throws Throwable {
            return ((SiteDataWS) ws).getListCollection();
          }
          
          public void onError(final Throwable e) {
            LOGGER.log(Level.WARNING, "Call to getListCollection failed.", e);
          }
        });
    if (vLists == null) {
      LOGGER.log(Level.WARNING, "Unable to get the list collection");
      return listCollection;
    }

    final Collator collator = Util.getCollator();

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
            LOGGER.log(Level.WARNING, 
                "Skipping List [{0}] with unsupported base type [{1}]",
                new Object[] {element.getTitle(), baseType});
            continue;            
          }
          MessageElement listMetadata =
              getListMetadata(element.getInternalName());
          if (listMetadata == null) {
            LOGGER.log(Level.WARNING,
                "Unable to get metadata for List [{0}]. Skipping List",
                element.getTitle());
            continue;
          }
          String rootFolder =
              getMetadataAttributeForList(listMetadata, "RootFolder");
          if (Strings.isNullOrEmpty(rootFolder)) {
            LOGGER.log(Level.WARNING,
                "Unable to get Root Folder for List [{0}]. Skipping List",
                element.getTitle());
            continue;
          }
          String defaultViewItemUrl = getMetadataAttributeForList(
              listMetadata, "DefaultViewItemUrl");
          if (Strings.isNullOrEmpty(defaultViewItemUrl)) {
            LOGGER.log(Level.WARNING, "Unable to get default View Item Url "
                + "for List [{0}]. Skipping List", element.getTitle());
            continue;
          }
          LOGGER.log(Level.FINE,
              "List [{0}] Root Folder [{1}] Default View Item URL [{2}]",
              new Object[] {element.getTitle(), rootFolder,
                defaultViewItemUrl});
          String siteUrl = sharepointClientContext.getSiteURL();
          if (Strings.isNullOrEmpty(element.getDefaultViewUrl())) {
            LOGGER.log(Level.WARNING, "List [{0}] with empty default view URL."
                + " Using root folder for List URL.", element.getTitle());
            StringBuilder listUrl = new StringBuilder(siteUrl);
            if (!siteUrl.endsWith("/")) {
              listUrl.append("/");
            }
            listUrl.append(rootFolder);
            url = listUrl.toString();
          } else {
            url = Util.getWebApp(sharepointClientContext.getSiteURL())
                + element.getDefaultViewUrl();
          }

          LOGGER.log(Level.INFO, "List url for List [{0}] is [{1}]",
              new Object[] {element.getTitle(), url});

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
            for (String formTemplate : sharepointClientContext.getInfoPathBaseTemplate()) {
              if (collator.equals(strBaseTemplate, formTemplate)) {
                strBaseTemplate = SPConstants.BT_FORMLIBRARY;
                break;
              }
            }
          }

          LOGGER.config("List URL :" + url);

          // Children of all URLs are discovered
          ListState list = new ListState(element.getInternalName(),
              element.getTitle(), element.getBaseType(),
              Util.siteDataStringToCalendar(element.getLastModified()),
              strBaseTemplate, url, webstate);

          list.setInheritedSecurity(element.isInheritedSecurity());
          list.setApplyReadSecurity(element.getReadSecurity() == 2);

          String myNewListConst = "";    
          LOGGER.log(Level.FINE, 
              "getting listConst for list URL [{0}]", defaultViewItemUrl);
          if (defaultViewItemUrl != null) {
            final StringTokenizer strTokList = 
                new StringTokenizer(defaultViewItemUrl, SPConstants.SLASH);
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
                  myNewListConst += listToken + SPConstants.SLASH;
                }
              }
              list.setListConst(myNewListConst);
              LOGGER.log(Level.CONFIG, "using listConst [ " + myNewListConst
                  + " ] for list URL [ " + defaultViewItemUrl + " ] ");

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
                  LOGGER.warning("excluding " + url.toString());
                }
                // add the attribute(Metadata to the list )
                list = getListWithAllAttributes(list, element);

                listCollection.add(list);
              } else {
                // entire subtree is to be excluded
                // do not construct list state
                LOGGER.warning("Excluding " + url
                    + " because entire subtree of " + myNewListConst
                    + " is excluded");
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
    }

    if (listCollection.size() > 0) {
      LOGGER.info("Discovered " + listCollection.size()
          + " lists/libraries under site [ " + webstate + " ] for crawling");
    } else {
      LOGGER.config("No lists/libraries to crawl under site [ " + webstate
          + " ]");
    }
    return listCollection;
  }
  
  private String getMetadataAttributeForList(MessageElement list,
      String attribute) {
    MessageElement metadata = list.getChildElement(new QName("Metadata"));
    if (metadata == null) {
      LOGGER.warning("Metadata missing for for List");
      return null;
    }
    return metadata.getAttribute(attribute);
  }
  
  private MessageElement getListMetadata(final String id) {
    final String listMetadata =
       Util.makeWSRequest(sharepointClientContext, siteDataWS,
           new Util.RequestExecutor<String>() {
         public String onRequest(final BaseWS ws) throws Throwable {
           return ((SiteDataWS) ws).getContentList(id);
         }          
         public void onError(final Throwable e) {
           LOGGER.log(Level.WARNING, "Call to getContentList failed.", e);
         }
       });

    if (Strings.isNullOrEmpty(listMetadata)) {
      LOGGER.log(Level.INFO, "Empty List Metadata for List with Id {0}", id);
      return null;
    }
    LOGGER.log(Level.INFO, "List Content for List with Id {0} : {1}",
        new Object[] {id, listMetadata});
    try {
      return ListsUtil.getMeFromString(listMetadata);
    } catch (Exception ex) {
      LOGGER.log(Level.WARNING,
          "Error parsing metadata for List with ID " + id, ex);
      return null;
    }
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

  /**
   * Return the site metadata.
   *
   * @return the {@link _sWebMetadataHolder}
   */
  private _sWebMetadataHolder getWebMetadata() {
    return Util.makeWSRequest(sharepointClientContext, siteDataWS,
        new Util.RequestExecutor<_sWebMetadataHolder>() {
          public _sWebMetadataHolder onRequest(final BaseWS ws)
              throws Throwable {
            return ((SiteDataWS) ws).getSiteData();
          }
          
          public void onError(final Throwable e) {
            LOGGER.log(Level.WARNING, "Call to getSiteData failed.", e);
          }
        });
  }

  /**
   * Retrieves the title of a Web Site. Should only be used in case of SP2003
   * Top URL. For all other cases, WebWS.getTitle() is the preferred method.
   */
  public String getTitle() {
    final _sWebMetadataHolder sWebMetadata = getWebMetadata();
    if (sWebMetadata == null) {
      LOGGER.warning(
          "Unable to get site data. The call to getSiteData returned null.");
      return "";
    }
    return sWebMetadata.value.getTitle();
  }

  /**
   * Makes a call to Site Data web service to retrieve site meta data and create
   * a SPDocuemnt and it returns a single SPDcoument.This method returns null if
   * and only if any one of SiteData stub or webState is null.
   *
   * @param webState The web from which we need to construct SPDcoument for it's
   *          landing page.
   * @return a single SPDocument for the given web.
   */
  public SPDocument getSiteData(final WebState webState) {
    if (webState == null) {
      LOGGER.warning("Unable to get the list collection because webstate is null");
      // in case if the web state is null and is not existing in SharePoint
      // server.
      return null;
    }

    final _sWebMetadataHolder sWebMetadata = getWebMetadata();
    if (sWebMetadata == null) {
      LOGGER.warning(
          "Unable to get site data. The call to getSiteData returned null.");
      return null;
    }

    final SPDocument siteDataDocument = new SPDocument(webState.getPrimaryKey()
        + SPConstants.DEFAULT_SITE_LANDING_PAGE + SPConstants.DOC_TOKEN
        + sWebMetadata.value.getWebID(),
        webState.getWebUrl() + SPConstants.DEFAULT_SITE_LANDING_PAGE,
        sWebMetadata.value.getLastModified(),
        sWebMetadata.value.getAuthor(), SPConstants.SITE, webState.getTitle(),
        sharepointClientContext.getFeedType(), webState.getSharePointType());
    String strUrl = Util.encodeURL(siteDataDocument.getUrl());
    HttpMethodBase method = new HeadMethod(strUrl);
    try {
      int responseCode =
          sharepointClientContext.checkConnectivity(strUrl, method);
      if (responseCode != 200) {
        LOGGER.log(Level.INFO, "Possible Publishing website. Marking Url [ "
            + strUrl + " ] with Document Type as ACL");
        siteDataDocument.setDocumentType(DocumentType.ACL);
      }
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING, "Unable to connect [ " + strUrl
          + " ] marking site home page as ACL document", e);
      siteDataDocument.setDocumentType(DocumentType.ACL);
    } finally {
      method.releaseConnection();
    }

    return siteDataDocument;
  }
}
