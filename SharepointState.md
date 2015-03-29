# What is the State File and How is it Useful? #

While traversing, connector needs to keep track of things it has traversed so that, afterwards, it only crawls the new/updated entities. Since, there is no direct way of check pointing in SharePoint, connector maintains the state file which stores all the information that connector may require to resume its traversal in future.

**Sharepoint\_state.xml** created under the connector instance directory is always updated with the recent in-memory copy of the state info every time checkpoint is received. Following is the structure of _Sharepoint\_state.xml_

```

<State>   
 <FeedType Type=""/>   
 <FullRecrawlFlag ID=""/>   
 <LastCrawledWebStateID ID=""/>   
 <LastCrawledListStateID ID=""/>    

 <WebState ID="" InsertionTime="" SPType="" URL="" WebTitle="" AclNextChangeToken="" AclChangeToken="">
   <ListState BiggestID="" ChangeToken="" ID="" LastModified="" Type="" URL="" IsAclChanged="" LastDocIdCrawledForAcl=""> 
     <Attachments> </Attachments>               
     <FolderItemIDs> </FolderItemIDs>
     <LastDocCrawled Action="" ID="" LastModified="" FolderLevel=””/> 
   </ListState>
    ……   
 </WebState>

 <WebState ID="" InsertionTime="" SPType="" URL="" WebTitle="" AclNextChangeToken="" AclChangeToken="">
   <ListState BiggestID="" ChangeToken="" ID="" LastModified="" Type="" URL="">
     <Alerts> </Alerts>
     <LastDocCrawled Action="" ID="" LastModified=""/>
   </ListState>
    ……
 </WebState>
 ……
</State> 

```

The above structure displays the names of all the nodes and attributes that may appear in the state file. Also, some of the names might not be displayed depending on the SharePoint version and feed type being used.

Following table describes the purpose and availability of each node:

|**Name**|**Description**|**Allowed Attributes**|**Availability**|
|:-------|:--------------|:---------------------|:---------------|
|State|Root node of the State file|Type|Always|
|FeedType|Contains information about feed type being used|ID|Always|
|FullRecrawlFlag|Indicates whether a complete crawl has been completed or not|ID|Always|
|LastCrawledWebStateID|Contains pointer to the ListState from where the next crawl is started|ID|Always|
|LastCrawledListStateID|Contains pointer to the WebState from where the next crawl is started|ID|Always|
|WebState|Represents a SharePoint web site|ID, InsertionTime, SPType, URL, WebTitle|Always|
|ListState|Represents a SharePoint list/library|BiggestID, ChangeToken, ID, LastModified, Type, URL, FolderLevel|Always|
|LastDocCrawled|Last Document that has been sent from the current list. This acts as a checkpoint per list/library|Action, ID, LastModified, FolderLevel|Mostly|
|FolderItemIDs|Contains the IDs of documents which are inside some folders in a pre-defined format|- |Content Feed, Only for document libraries|
|Attachments|Contains the attachment URLs for all the items in a pre-defined format|- |Content Feed,Only for those lists whose items can contain attachments|
|Alerts|Contains the IDs of the discovered alerts|- |Only if the Web Site contains some alerts for the user whose credentials are specified on the connector configuration page|

Following table describes the purpose and availability of attributes along with the list of nodes for they can be defined:

|Name|Description|Allowed Nodes|Availability|
|:---|:----------|:------------|:-----------|
|ID|The primary key to identify an entity that is its parent node|FeedType, FullRecrawlFlag, LastCrawledWebStateID, LastCrawledListStateID, WebState, ListState, LastDocCrawled|Always|
|Type|Type of the entity that is its parent node|FeedType, ListState|Always for FeedType, Only in ContentFeed for ListState|
|InsertionTime|The time when the WebState has been created|WebState|Always|
|SPType|The SharePoint version (SP2003, SP2007)|WebState|Always|
|WebTitle|The title of the SharePoint web title|WebState|Always|
|BiggestID|The Biggest ID of a document that has been sent from this list|ListState|Content Feed|
|ChangeToken|Connector uses this to be in sync with the changes happening on the SharePoint server|ListState|SP2007|
|LastModified|Last Modified Date of the entity represented as the parent node|WebState, ListState, LastDocCrawled|Always|
|URL|URL of the entity represented as the parent node|WebState, ListState, LastDocCrawled|Always|
|Action|Indicates whether the document has been sent as an ADD feed or DELETE feed|LastDocCrawled|Content Feed|
|FolderLevel|Indicates the path of the document relative to the library if the document is inside a folder|LastDocCrawled|SP2007|
|AclNextChangeToken|The next change token to be used for ACL change detection|WebState|SP2007, SP2010|
|AclChangeToken|Current change token being used for ACL change detection|WebState |SP2007, SP2010|
|IsAclChanged|True, ACL of this list has been changed and all the affected documents have not yet been crawled|SP2007, SP2010|
|LastDocIdCrawledForAcl|Last document crawled from this list due to change in ACL|SP2007, SP2010|



