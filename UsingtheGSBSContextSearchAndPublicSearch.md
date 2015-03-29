# Introduction #

This document gives a brief overview of the context search and public search in Google Search Box for SharePoint 2.6.8.

SharePoint is a Content Management System. With SharePoint, documents can be uploaded, stored and downloaded for sharing between the different parties for collaboration purpose. Using SharePoint, different web applications can be developed as an internet-based, intranet based or extranet based respectively. Web applications comprise of site collections and site collections in turn have sites in them. Sites may contain various entities like document libraries, lists, features and subsites. The lists may contain large number of documents and folders.

## Abbreviations ##

**GSBS**: Google Search Box for SharePoint

## New Features in GSBS 2.6.8 or later ##

GSBS 2.6.8 supports following new features -

  * Support for context-sensitive search

  * Public search

## Support for context search with Google Search Box for SharePoint ##

Google Search Box for SharePoint 2.6.0 supported intranet search at an Enterprise level i.e. all the data indexed and matching the entered search query term is returned. This may at times be cumbersome if the user is intending to search only within a site/ list/ folder, and that the search results are plenty in number.

The Google Search Box for SharePoint 2.6.8 or later supports context search i.e. user can search for documents within a site/ list/ folder matching the search query term, provided the SharePoint connector is configured in Metadata-URL feed mode. (feed mode essentially works with GSA 6.8, as the display and record urls are different in case of content feed mode. For scope search, the url of the site or list the user is currently browsing is extracted, which is of the form “http://sharepointsite:portnumber/”. In content feed mode, the record URL is of the form “googleconnector://”), hence this feature will not be supported.The site, list and folder levels are termed as scopes. Scopes make it possible to narrow searches to particular locations/ entities.

Note that for metadata and URL feed mode of the connector, following two conditions should be met in order for authorization to work with the Search box:

1. The indexed SharePoint URLs should be Kerberized.

2. You need to configure either Kerberos or SAML authentication along with SAML authorization on the GSA, as connector will not be doing authorization in the meta-URL feed mode.

There are in all six scopes defined in Google Search Box for SharePoint 2.6.8  -

  * Enterprise
  * Current Site
  * Current Site and all subsites
  * Current List
  * Current Folder
  * Current Folder and all subfolders

### Enterprise Scope ###

> Supports search at a global level i.e. all the search results, belonging to SharePoint or other content management systems, and indexed by the GSA will be returned matching the search query term. For SharePoint connector content feeds, users can still use the default "Enterprise" scope.

### Current Site Scope ###

> The SharePoint site url is of the form "http://SharePointSiteURL:portno". The current site scope supports search at the site level.
> Any contents within the subsites of the site  "http://SharePointSiteURL:portno" (in the entire hierarchy) are not searchable. Also, data pertaining to any custom list or document library within the site  "http://SharePointSiteURL:portno" are also not searchable. Contents from sites other than the SharePoint site "http://SharePointSiteURL:portno" are not returned.

### Current Site and all subsites Scope ###

> The SharePoint site url is of the form "http://SharePointSiteURL:portno". The current site and all subsites scope supports search at a site and all subsites level.
> The search results are restricted to the contents of the site "http://SharePointSiteURL:portno". Any contents within the subsites of the site  "http://SharePointSiteURL:portno" (in the entire hierarchy) are also searchable. Also, data pertaining to any custom list or document library within the site  "http://SharePointSiteURL:portno" is searchable. Contents from sites other than the SharePoint site "http://SharePointSiteURL:portno" are not returned.

### Current List ###

> The SharePoint list url is of the form "http://SharePointSiteURL:portno/Lists/listname/AllItems.aspx" or "http://SharePointSiteURL:portno/documentlibraryname/Forms/AllItems.aspx". The current list scope supports search at a list level.

> The search results are restricted to the given list. Results are also returned for any of the list records (applicable for list created using custom list template) or documents (applicable for list created using document library template) present in the list. Contents from sites other than the SharePoint site "http://SharePointSiteURL:portno" are not returned.

### Current Folder ###
> The SharePoint folder url is of the form "http://SharePointSiteURL:portno/documentlibraryname/Forms/AllItems.aspx?RootFolder=Encodedurl&FolderCTID=&View=EncodedURL". The current folder scope supports search at a folder level.

> The search results are restricted to the given folder contents. Contents from the subfolders inside the given folder are not returned.  Contents from sites other than the SharePoint site "http://SharePointSiteURL:portno" are not returned.


### Current Folder and all subfolders ###

> The SharePoint folder url is of the form "http://SharePointSiteURL:portno/documentlibraryname/Forms/AllItems.aspx?RootFolder=Encodedurl&FolderCTID=&View=EncodedURL". The current folder and all subfolders scope supports search at a folder and all subfolders level.
> The search results are restricted to the given folder contents. Contents from the subfolders inside the given folder are also returned. Contents from sites other than the SharePoint site "http://SharePointSiteURL:portno" are not returned.

## User Interface for Context Search ##

> For facilitating user to select scope while searching, a scopes dropdown is provided alongwith Google search box for SharePoint. By default the scopes 'Enterprise', 'Current Site' and 'Current Site and all subsites' are displayed. As and when the user browses through the list/ folder, the remaining scopes get displayed. (For GSBS 2010, the scopes will be disabled at first, and on browsing through the list/ folder, respective scopes corresponding to list/ folder will be enabled).
> The user can perform search only as per the current context selected i.e. as per the current scope that is browsed. By default, the search occurs at a site scope, hence scopes Enterprise, Current Site and Current Site and all subsites are displayed.

## Known Limitations ##

1.Context search works with only metadata and URL feeds in GSA.
2.Context search for list item fails when the SharePoint site collection is created using the “/sites/” path.



## Support for public search with Google Search Box for SharePoint ##

> The Google Search Box for SharePoint 2.6.8 or later supports public search. Earlier version i.e. Google Search Box for SharePoint 2.6.0 had support for public and secure search. For facilitating public search with Google Search Box for SharePoint 2.6.8 or later, a checkbox named 'Public Search' has been provided. User can change the checkbox status before performing a search with search box.

> On checking the public search check-box, a public search is performed by the search box. On unchecking the public search checkbox, a public and secure search is performed by the search box.

## New web.config parameters introduced in GSBS 2.6.8 ##

There are two new web.config parameters introduced into the web.config file for any web application, on which the Google search box is to be installed -

  * accesslevel

> Using this parameter, the user can perform either a 'public search' or a 'public and secure search'. The permissible values for this parameter are 'a' and 'p'. This is one of the name-value pairs that can be used in a search request sent to the GSA, documented on http://code.google.com/apis/searchappliance/documentation/64/xml_reference.html#request_parameters

> Based on the value of accesslevel parameter, the public search check-box is displayed. If the value of accesslevel parameter is 'p', the public search checkbox is not displayed with the GSBS, and all the searches performed will be public searches. If the value of accesslevel parameter is 'a', the public search checkbox is displayed along with the GSBS, and all the searches performed will be public and secure searches.

  * omitSecureCookie

> The parameter is added to overcome the secure cookie issue. This parameter indicates whether the secure cookie should be passed on for processing or should be dropped. Value 'false' indicates that cookie will be not be dropped, and value 'true' indicates that the cookie will be dropped.


## References ##

  * Create/ extend a web application in SharePoint

> http://technet.microsoft.com/en-us/library/cc262668%28office.12%29.aspx

  * Create site collection in SharePoint

> http://technet.microsoft.com/en-us/library/cc263094.aspx
> http://vspug.com/ssa/2006/10/16/moss-2007-creating-a-site-collection/

  * Create a site in SharePoint

> http://www.suryatechnologies.com/content/how-create-site-sharepoint-sites-0

  * Create subsite in SharePoint

> http://technet.microsoft.com/en-us/library/cc261688%28office.12%29.aspx

  * Create document library in SharePoint

> http://www.hosting.com/support/sharepoint3/createdoclib

  * Create a list in SharePoint

> http://office.microsoft.com/en-us/windows-sharepoint-services-help/create-a-list-HA010099248.aspx