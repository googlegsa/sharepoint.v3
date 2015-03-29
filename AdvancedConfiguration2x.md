# Advanced configuration for the SharePoint connector #
## Configure the connectorInstance.xml file ##

There are certain advanced properties that can change connector's behavior. These properties are not available on the configuration UI but they can be changed by editing connectorInstance.xml file which gets copied to every new connector that gets created. Following section explains purpose of each of these properties and enlists all the possible values along with default value for each of them. Please note that these advanced properties in this form are available only in SharePoint connector version 2.8 and above.

Including all the properties mentioned in the below section, SharePoint connector 2.8 version introduces 3 more new properties.
```
<!--
    The following two properties define if the ACLs for documents need to
    be fetched in batches and what should be the factor to determine an
    appropriate batch size
  -->
  <!--
    True indicates ACL for documents will be retrieved in batches. . The
    batchSize will be less than the batchHint. This is typically required
    when the ACL per document is large, implying fetching ACL for
    batchHint number of documents will result in a large WS response and
    result in OutOfMemoryError
  -->
  <!--
    <property name="fetchACLInBatches"> <value>true</value> </property>
  -->
  <!--
    The batchSizeFactor is used to arrive at an appropriate batch size
    value. The formula used is: n/batchSizeFactor where 'n' is the number
    of documents discovered in each batch traversal. Value should be > 0
  -->
  <!--
    <property name="aclBatchSizeFactor"> <value>2</value> </property>
  -->

  <!-- Web service time out value. Value should be mentioned in milliseconds
    Value should be greater than 1000 milliseconds.By default set to 5 minutes. -->
  <!--
  <property name="webServiceTimeOut"><value>300000</value></property>
  -->
```

The advanced configuration is done through the modification of the file "connectorInstance.xml".
This file is available in the connector instance directory \GoogleConnectors\Sharepoint`<number>`\Tomcat\webapps\connector-manager\WEB-INF\sharepoint-connector\`<connector-instance>`

The default values for any attribute that are supposed to be configured as part of the Advanced configuration are set in a file named connectorDefaults.xml in the connector-sharepoint.jar file located in connector-manager/WEB-INF/lib.
The connectorInstance.xml is empty and has a reference to the Spring bean defined in connectorDefaults.xml. For overriding the default value of any attribute, the attribute needs to be uncommented in connectorInstance.xml along with the new value and restart the connector.
The connector will then respect and use the value set in connectorInstance.xml and not connectorDefaults.xml


The details mentioned in this document are applicable to connector version 2.0.0 to 2.6.8.

By changing the connectorInstance file, you can change the included/excluded metadata list for the SharePoint documents. You can also enable/disable FQDN (Fully Qualified Domain Name) resolution for host names.

### Change Included/Excluded metadata ###

By modifying the value of the property named “excluded\_metadata”, you can provide the list of metadata which should not be fed to the appliance
Following is the default list of excluded metadata configured in connectorDefaults.xml:

```
<property name="excluded_metadata">
  <list>
      <!-- Remove any of the following metadata entries if you want them to be indexed in GSA. -->
	  <value>.*cachedcustomprops$</value>
           <value>.*parserversion$</value>
           <value>.*cachedtitle$</value>
           <value>.*ContentTypeId$</value>
           <value>.*DocIcon$</value>
           <value>.*cachedhastheme$</value>
           <value>.*metatags$</value>
           <value>.*charset$</value>
           <value>.*cachedbodystyle$</value>
           <value>.*cachedneedsrewrite$</value>
	</list>
</property>
```

These metadata values are commented in connectorInstance.xml. The `<value>` node contains the metadata attribute name to be excluded. Add/delete any node as required.

By modifying the value of the property named “included\_metadata”, you can provide the list of metadata which appliance can crawl for a document.
Following is the default list of included metadata configured in connectorDefaults.xml:

```
  <property name="included_metadata">
   <list>
      <!-- If you want to index only specific metadata, put them here.	For example, putting the following entry will cause only Title to be sent to GSA. -->
    <!-- <value>Title</value> -->
   </list>
 </property>
```

You can use patterns while specifying the metadata values.

E.g. “.**DocIcon$” matches the metadata “DocIcon”**

### Scenarios while specifying metadata ###
a) If you want to index all the metadata values of the documents, keep "included\_metadata" and "excluded\_metadata" as blank.
E.g.
```
   <property name="included_metadata">
      <set></set>
   </property>
      
  <property name="excluded_metadata">
      <set></set>
  </property>
```
b) If you want to filter\skip specific metadata values of the documents from indexing, add respective metadata values under "excluded\_metadata"

E.g.
```
   <property name="excluded_metadata">
     <set>
       <value>AccessMask</value>
       <value>ObjectType</value>
     </set>
   </property>
```
> This will allow connector to filter out metadata AccessMask & ObjectType while submitting documents to GSA for indexing
c) If you want to index only specific metadata values of the documents, add respective metadata values under ="included\_metadata".

E.g.
```
    <property name="included_metadata">
      <set>
    	<value>Name</value>
        <value>Owner</value>
      </set>
    </property>
```
> This will allow connector to include only metadata Name & Owner while submitting documents to GSA for indexing


**Note:** Following are some well known metadata which are sent for each document; these metadata are always sent irrespective of what inclusion/exclusion rule defined in the connectorInstance.xml:
  * sharepoint:author
  * sharepoint:listguid
  * sharepoint:parentwebtitle
  * google:objecttype

#### Enable/Disable FQDN Resolution for Host Names ####

'FQDNConversion' property, if set to true would convert the document and attachment URLs as fetched by the connector to FQDN format.

For e.g. http://moss_host1/ will be converted to http://moss_host1.yourdomain.com/.

By default this value is set to false. To allow FQDN conversion, uncomment following section in connectorInstance.xml:

```
<property name="FQDNConversion">
   <value>false</value>
</property>
```

And change value from ‘false’ to ‘true’
For more details regarding each of the attributes and their impact on the connector traversal please refer to the connector documentation.