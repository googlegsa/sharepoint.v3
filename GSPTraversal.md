This section will cover the following points in terms of the SharePoint connector Traversal:

  * Glossary
  * Discover all site collections, sites, lists, folders, attachments given the crawl URL
  * Applying inclusion/exclusion patterns for the discovered URLs
  * Adding/Modifying appropriate entries in Sharepoint\_state.xml file
  * Fetching metadata for the documents and applying the included, excluded metadata list rules
  * Preparing the document list to be sent to the Connector Manager
  * Sending individual documents to Connector Manager
  * CheckPointing in SharePoint Connector
  * Details specific to content feed
  * How does connector handle the case when the number of site collection is quite large and the numbers of update are very few?
  * Impact of batch hint and traversal batch timeout on connector traversal

---

## Glossary ##
**Connector State:** The in-memory state information that connector maintains. A snapshot of this can be seen in the state file Sharepoint\_state.xml

**Traversal Cycle:** One complete Scan of all the SharePoint sites and lists known till the time have been done and there are no more documents to be sent. A complete traversal cycle is said to be complete if it satisfies all the following conditions

  1. All the sites and lists which are discovered till the last crawl cycle and are there in the connector state got crawled and there are no more documents to be discovered from any of them.

> 2. No new intermediate sites have been discovered while crawling the known sites and lists.

**Batch Traversal:** A traversal initiated to get BatchHint no. of documents. Batch traversals are returned when either of the following conditions get satisfied:

  1. Connector has discovered at least (2\*BatchHint) no. of documents

> 2. Connector has visited all the sites and lists which are known to be existent and are there in the connector state


## Discover all site collections, sites, lists, folders, attachments given the crawl URL ##
Connector starts its traversal from the Crawl URL specified on the configuration page. The entire sites and lists discovered by the connector are stored in the connector state along with the last-crawled-site and last-crawled-list information. From the next batch traversals, connector picks the site URL to start crawling from the connector state itself. When a complete traversal cycle is completed, last-crawled-site and last-crawled-list info is reset and in the next batch traversal again starts from the Crawl URL specified on the configuration page.

The completion of a traversal cycle is more conceptual and this info is maintained by the connector by resetting the last-crawled-site and last-crawled-list. As far as the connector is concerned, it always runs a batch traversal.

The site URL that has been picked is processed for lists, child sites, link sites and Site Discovery sites. All discovered site URLs in a batch traversal are collected in a local store and gets updated into the connector state. These newly discovered sites are crawled in the next batch traversal(s). Each discovered list is crawled for documents. Discovery of documents are done per list and their sequence and checkpoint related information are also maintained at list level. All the discovered documents are stored in the crawl queue of the list. If the current list is able to contain attachments, connector tries to get attachments for all the discovered List Items. At the end of a list, connector constructs an extra document for the list itself which will be sent to the GSA along with other documents corresponding to the list items.

At the end of a complete traversal cycle, connector checks for those sites and lists which were discovered sometime in past but is now un-available. All such sites and lists are deleted from the connector state.

Every time a new traversal cycle is initiated from the top level site URL (that means, last batch traversal was a complete traversal cycle leaving no more documents to be discovered and sent), connector also discovers some extra sites which includes MySites, Personal sites and those sites which are returned by Google Services installed on the SharePoint server.

In every batch traversal, connector crawls only those sites which are there in the connector state. Any new site discovered in the current batch traversal is a candidate for crawling in future traversals.

Since, each traversal request initiated by the Connector Manager returns only when either (2\*BatchHint) no. of documents are discovered or all the sites and lists are visited, this can be very time consuming when the no. of sites and lists are very large but the no. of documents added/updated are less. Such time delays are likely to occur more frequently in case of incremental crawl when the frequency of change in content is comparatively less.

Let's say for example, there are a total of 500 sites in the connector state that is being crawled by the connector; the BatchHint for the current crawl cycle is 50 and, no. of document updates are only 10. Now in such case, connector will not return as soon as it discovers those ten document changes. Rather, it'll try visiting each of those 500 site URLs before returning. Hence, user should be patient till the time connector confirms that there are only ten changes and no other documents in other sites have been changed. Depending on the turnaround time taken while a single web service call and the amount of processing connector has to do, this user waiting time can vary.

This delay is a necessary evil for the connector because it needs to scan every site and list before returning the final list of documents that have changed or updated. There has been proposal to introduce timeouts based returns from the traversals in future release of connectors.


## Applying inclusion/exclusion patterns for the discovered URLs ##
All the URLs discovered by connector are checked against the included/excluded URL patterns specified by the user during configuration. Following are a few well known reasons for URL exclusion:
  1. URL does not match any of the specified included URL patterns
> 2. URL matches any of the pattern specified under excluded URL patterns
> 3. URL points to page whose content can not be downloaded because of some reason. This applies only in case of content feed
> 4. URL points to a SharePoint 2003 site and the FeedType being used is content
> 5. SharePoint version can not be determined from the given site URL


## Adding/Modifying appropriate entries in Sharepoint\_state.xml file ##
Connector always remembers its progress with the help of connector state. A snapshot of this state is stored in Sharepoint\_state.xml every time a set of documents is sent to the connector manager and checkpoint is called. If the connector is stopped and restarted after sometime, this state file has all the required information for the connector to continue its traversal from the point where it had stopped last time. As long as the connector is running, it does not bother to read this file and rather uses the in-memory representation of the connector state info. The connector restart is one case when the state file is read by the connector. Technically, every time a new instance of SharePoint Traversal manager is created, connector reads this file to construct an in-memory connector state. For a complete description of the structure of state file, refer to the WIKI article describing the same.


From the above reference, you can get a fair idea of the structure of the state file. Changing the state file is highly discouraged. Though, in certain cases where you want to force a re-crawl of only specific sites or list, changes in the state file is required. Be extra careful while doing any change and do ensure that you are not breaking the XML node hierarchy.

**Forcing re-crawl of a site:**
  1. In the Sharepoint\_state.xml, locate the WebSate node corresponding to the site that you want to re-crawl.
```
<WebState .......>

    ..............
    <ListState .........>
            .....    
        <LastDocCrawled Action ............. />
    </ListState>
</WebState>
```

> 2. Recursively, delete all the child nodes of the WebState node located in the above step. The above entry after the change will look like:
```
    <WebState ..........>

    </WebState>
```

> 3. Save the file and restart the connector

**Forcing re-crawl of a list**
  1. In the Sharepoint\_state.xml, locate the WebSate node corresponding to the site in which the list exists.

> 2. Locate ListState node corresponding to the list that you want to re-crawl.
```
<WebState .......>

    ..............
    <ListState .........>
            .....    
        <LastDocCrawled Action ............. />
    </ListState>
</WebState>
```

> 3. Delete the ListState node and all its child nodes. The above entry after the change will look like:
```
<WebState .......>

    ..............
</WebState>
```

> For locating a WebState or ListState node for a given site/list, use the URL attribute.

Though, the above suggested changes will ensure that a full crawl is initiated for the entity, they do not make any promises on how soon this will be done. Ideally, they should be crawled when the connector traversal will reach to the given web state or list state. You can direct the connector to start its traversal from any specific site; this will make the traversal of your concerned site at earliest. To do this, just change the value of the following nodes in state file:
```
<FullRecrawlFlag ID="false"/>
<LastCrawledWebStateID ID="SiteURL"/>
<LastCrawledListStateID ID="ListID"/>
```

Please not the ListID. It's not List URL, but the GUID of the list. You can leave it blank


## Fetching metadata for the documents and applying the included, excluded metadata list rules ##
For each document that connector discovers and sends to the GSA, it parses the metadata information that is returned by the web service. It creates a name - value pair for each individual metadata attribute. Connector also does some relevant changes in the way these names and values are displayed to make better sense to user. This is required because SharePoint web service does some manipulations in these values. Connector passes all the metadata names through a two level filter of included\_metadata and excluded\_metadata respectively. These filters are explained in detail in the connector documentation. All those attributes which passes through both these filters are sent to GSA for indexing. Though, this filter applied by the connector makes no guarantee of those metadata that are discovered by the GSA itself. Hence, a user who is running the connector in Metadata-and-URL mode, may see some metadata indexed in the GSA which were filtered out by the connector.


## Preparing the document list to be sent to the Connector Manager ##
Connector discovers and sends the documents in a pre-defined order. This sequence is ensured at each step of traversal. Documents are always discovered at list level, these lists are sorted in a pre-defined order; sites are the top level entities which contains list. Connector respects these ordering not only at the time of traversal but also while sending these documents to CM. Ordering of different type of entities are different:

  * Sites are ordered according to their time of discovery
  * Lists are ordered according to their Modified date and IDs
  * Documents are ordered according to their IDs in case of SP2007 and according to their Last Modified Date in case of SP2003

Traversal is started at site level. Once, a site is picked up for traversal and al the lists in that site are discovered, the discovered lists in that site are traversed in the order of their last modified date. At last, the documents discovered from each such lists and stored in the crawl queue of the list are ensured to be in the order of their ID / last modified date.

The final list of documents that gets iterated when Connector Manager asks for the next document is constructed keeping in mind all the above ordering rules. Hence, every time Connector Manager asks for the next document, connector sends the next document that is in the state and comes next in the order defined above. After the document is sent connector removes it from the crawl queue of its parent list. A re-crawl of the SharePoint repository is started only when there is no document left in the crawl queues of lists.

The sequence in which a connector sweeps through the ECM and sends the document to the GSA is a vital concept and is not specific to the SharePoint connector only. Though, the way this sequence is maintained is a bit complex in case of Google SharePoint Connector. The reason being, the actual documents that are sent to the GSA falls deep inside the hierarchy maintained in the connector state. This hierarchy can be very large and complicated. In absence of any well-known sequence, connector will loose the track of which document / list /site is to be crawled next.


## Sending individual documents to Connector Manager ##
Connector performs certain operations for each document that is sent to the Connector Manager and then to the GSA. These include:
  * FQDN resolution of document URL
  * Applying Site Alias Mapping on the document URLs
  * Removing the document from its parent crawl queue

Connector always remembers the last document sent to the Connector Manager and in the subsequent requests returns the document that comes next to the last document sent. There might be the case when the last document sent was the last document itself and there is no more documents to be sent. In such case, connector re-crawl the SharePoint repository to get more documents, new / updated. Every time a document is sent to the Connector Manager, it is dropped from the in memory crawl queue.


## Checkpointing in SharePoint Connector ##
Checkpointing is basically marking a cursor/pointer to identify till what point in the content reporsitory the documents have been sent to the GSA. It is an important event that is initiated by the Connector Manager when it's not ready to receive any more documents from the connector. In an ideal case, this happens when the Connector Manager has collected BatchHint no. of documents from the connector; but there might also be some other cases when the Connector Manager decides to checkpoint. For connector, the reason is immaterial. When checkpoint is called, the connector dumps all its state information onto the physical disk to the state file.


## Details specific to content feed ##
Connector, by default runs in the Metadata-and-URL feed mode but, it can be configured to run in the Content feed mode as well. The top level conceptual differences in these two modes are very clear:
  * Connector crawls and sends the document's content along with its URL and metadata in case of content feed. In M&U, content is crawled by the GSA.
  * Document's security is being handled by the connector at serve time in case of Content feed. This includes search user authentication and authorization against the search results.
Though, apart from the above two, there are certain technical differences:

In case of content feed, the primary key being used for identifying documents is the document IDs whereas in M&U mode it's the document URL. The document IDs is constructed in such a way that it is sufficient for authorizing the document. The document ID as returned by the SharePoint web services are not so informative and unique to identify and do authorization. The reason being, those IDs are unique to a given List only. Connector, hence, constructs a new ID of the document before sending them to the GSA. This newly constructed ID is a combination of the list and document ID where, the list ID is the list URL and document ID is the original ID returned by the web service.

The second important difference that comes with content feed is the ability to recognize document's deletion so that delete feeds could be sent for them and these documents get removed from the GSA's index. For individual document deletion, this is not a problem as a single delete feed is to be sent for the document. But in case of folder deletion, connector needs to send the delete feeds for all those documents which were inside the folder. SharePoint treats folders as documents and returns only the folder's ID when it is deleted. No information is returned about the documents which were inside that folder. Hence, for every folder, connector explicitly maintains the list of all document IDs which are inside the folder so that it can send delete feeds for all those documents. This information is stored as a complex string in the ListState of the connector state.