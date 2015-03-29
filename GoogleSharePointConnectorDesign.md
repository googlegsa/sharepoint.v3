# Connector for SharePoint Design #

## Introduction ##
The Google Search Appliance Connector for SharePoint enables the Google Search Appliance to traverse documents and attachments on Microsoft SharePoint sites. Instances of the connector fetch metadata and URLs for SharePoint documents and attachments using SharePoint Web services and direct the data to the Google Search Appliance as a metadata-and-URL feed.

## Glossary ##
```
* MOSS – Microsoft Office SharePoint Server 
* SPS – SharePoint Portal Server 
* WSS – Windows SharePoint Services 
* GSA – Google Search Appliance
* SPI – Service Provider Interface 
```

## Scope of Design ##
This document provides the detailed design for the content traversal and state maintenance for the Connector for SharePoint.
The connector supports Microsoft SharePoint 2003 (SPS 2003 and WSS 2.0) and Microsoft SharePoint 2007 (MOSS 2007 and WSS 3.0).

### 1. Design Considerations ###

#### 1.1. Assumptions ####

---

```
1. !SharePoint Connector will use the Google Connector Manager’s SPI 
2. The connector will always run in continuous mode. 
3. "meta-url feeds" are sent to the appliance. 
4. Connector will be built using JDK 1.4.2. 
5. Connector will support logging functionality using java.util.logging
```

#### 1.2. Limitations ####

---

The performance and capability of the connector is limited by the Microsoft SharePoint web services APIs which is used by the connector to retrieve URLs and metadata from SharePoint.


### 2. Detailed Design ###

#### 2.1. High – Level Architecture of the SharePoint Connector State ####

---

```
    <Global State>
          LastWebCrawled = #WEBID
          LastListCrawled = #LISTID
          bFullReCrawl = #VALUE(T/F)
     
        <Web State id=#ID>
            <List State id=”#ID”>
                <lastDocCrawled>
                    <document id="…">
                    <lastMod>>#DATE </lastMod>
                    <url> … </url>
                        …
                    </document>
                </lastDocCrawled>    
         
                <crawlQueue>
                   <document id="1”>
                        <lastMod>#DATE</lastMod>
                        <url> … </url>
                           …
                  </document>
                    …
                </crawlQueue>      
            </List State>
       
        </Web State>
        …
    </Global State>
```

**Note:** The diagram above indicates in-memory representation of Global State and while saving it on disk, crawl Queue is dropped.


#### 2.2. SharePoint Connector traversal - Workflow ####

---


##### 2.2.1 Terminologies Used #####

**Crawl Cycle:** This relates to one complete traversal of all the SharePoint sites (both children sites and linked sites), covering all the list and libraries and their documents.

**Partial Crawl Cycle:** At a given time, it is possible to have a large number of documents in the SharePoint sites available for the connector to be traversed. But in order to have efficient utilization of machine resources and performance, the connector discovers only the documents greater than or equal to threshold value and feeds it to appliance for indexing.

**Threshold:** This is an indication of how many documents should be traversed by the SharePoint connector before submitting it to connector manager for feeding it to GSA. The connector submits the documents in batches which get controlled by the batch hint value. The value of threshold is taken to be twice the batch size.

##### 2.2.2 Content Traversal #####
At start of each Crawl Cycle, The connector will traverse all the SharePoint sites and their documents and feed it to appliance for indexing purpose. The connector keeps track of the webs and lists traversed by the connector. This is required especially during partial crawl as at the start of every partial crawl cycle, the connector will resume crawling based on the state information of last web and list traversed.
Additionally connector also maintains one more flag “bFullReCrawl” (initially false) to indicate whether the traversal is a part of Crawl Cycle or partial crawl cycle. This is also used for initiating and completing the garbage collection cycle as the garbage collection is done only during Crawl cycle (and not during Partial Crawl).

##### 2.2.3 State Maintenance #####
The purpose of state maintenance is to restore the connector after a crash or shutdown. While saving connector state to disk, the crawl queue is removed for each list. The crawl queue is maintained per list level and contains a list of documents which are to be processed and fed to the appliance for indexing during the traversal process.