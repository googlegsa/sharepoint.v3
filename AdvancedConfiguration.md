# Advanced configuration for the SharePoint connector #
## Configure the connectorInstance.xml file ##

The advanced configuration is done through the modification of the file "connectorInstance.xml". This file is available inside "connector-sharepoint.jar". You should extract this file and place it in the folder /connector-manager/WEB-INF/connectors/sharepoint-connector/`instance_of_connector`/

By changing this file, you can change the metadata Blacklist and Whitelist for the sharepoint documents. You can also enable/disable FQDN (Fully Qualified Domain Name) resolution for host names.



#### Change BlackList and WhiteList ####

By modifying the value of the property named “blackList”, you can provide the list of metadata which should not be fed to the appliance (blacklisted-metadata).

For e.g.
```
<property name="blackList">
	<list>
		<value>vti_cachedcustomprops</value>
		<value>vti_parserversion</value>

	</list>
</property>
```

By modifying the value of the property named “whiteList”, you can provide the list of metadata which appliance can crawl for a document.

For e.g.
```
  <property name="whiteList">
	<list>
		<value>vti_title</value>
		<value>vti_author</value>
	</list>
</property>
```

The connector does not crawl the list of metadata specified in blacklist and whitelist.
If you wish to add a metadata in the included list or excluded list for the connector, you have to add a line between the tags and : `name_of_the_metadata`
Once the modification of the file is done, you have to restart the connector service.



#### Enable/Disable FQDN Resolution for Host Names ####

'FQDNConversion' property, if set to true would convert the document and attachment URLs as fetched by the connector to FQDN format.
For e.g. [http://moss_host1/]  will be converted to [http://moss_host1.yourdomain.com/].
The default value is set to false.

Note: The FQDN resolution will happen using the Connector system's name resolution. Hosts not accessible from the connector machine will not be resolved.

#### How to feed ACL to GSA using SharePoint connector? (Version 2.6.0 and 2.6.8) ####

Following two properties in connectorInstance.xml will dictate the way ACLs will be fed to GSA.

```
<property name="pushAcls"><value>false</value></property> 
```

Should domain name be removed from domain\username?
```
<property name="stripDomainFromAces"><value>true</value></property> 
```

In connector version 2.8 and above, above options are exposed on the configuration UI. You just need to select the "Feed ACLs" option and provide LDAP settings if you want the connector to send ACLs and resolve SharePoint and LDAP group at serve time.

#### Specify the URLs that should be mapped as per the alias mapping rule specified on connector configuration page (Version 2.6.6 onwards) ####
```
<property name="reWriteDisplayUrlUsingAliasMappingRules"><value>true</value></property>
```
Applicable for both Metadata-and-URL and content feed. If set to true, the document display URL (shown to the search users) will be re-written using alias mapping rule specified on connector configuration page.

```
<property name="reWriteRecordUrlUsingAliasMappingRules"><value>false</value></property>
```
Applicable only for Metadata-and-URL feed. If set to true, the document record URL will also be re-written using alias mapping rule specified on connector configuration page.

#### The following two properties define if the ACLs for documents need to be fetched in batches and what should be the factor to determine an appropriate batch size in case out of memory exception in the connector logs ####

This configuration will be helpful where there are large ACE for the SharePoint URLs. True indicates ACL for documents will be retrieved in batches.The batchSize will be less than the batchHint. This is typically required when the ACL per document is large, implying fetching ACL for
batchHint number of documents will result in a large WS response and
result in OutOfMemoryError

```
    <property name="fetchACLInBatches"> <value>true</value> </property>
```

The batchSizeFactor is used to arrive at an appropriate batch size
value. The formula used is: n/batchSizeFactor where 'n' is the number of documents discovered in each batch traversal. Value should be > 0

```
  <property name="aclBatchSizeFactor"> <value>2</value> </property>
```

#### ACL  Web service time out value ####

Value should be mentioned in milliseconds and should be greater than 1000 milliseconds.By default set to 5 minutes.

```
  <property name="webServiceTimeOut"><value>300000</value></property>
```