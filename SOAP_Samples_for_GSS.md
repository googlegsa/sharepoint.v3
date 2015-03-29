# Introduction #

This document is designed to explain the meaning and purpose of all the input parameters used by various web methods exposed by Google Services for SharePoint. This will help you to test the GSS web services through any tool like SOAP UI.


# Details #
Following is the SOAP request format used by BulkAuthorization web method BulkAuthorize():

```
<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:gsb="gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com">
   <soap:Header/>
   <soap:Body>
      <gsb:BulkAuthorize>
         <!--Optional:-->
         <gsb:authData>
            <!--Zero or more repetitions:-->
            <gsb:AuthData>
               <!--Optional:-->
               <gsb:listURL>?</gsb:listURL>
               <!--Optional:-->
               <gsb:listItemId>?</gsb:listItemId>
               <gsb:isAllowed>?</gsb:isAllowed>
               <!--Optional:-->
               <gsb:error>?</gsb:error>
               <!--Optional:-->
               <gsb:complexDocId>?</gsb:complexDocId>
            </gsb:AuthData>
         </gsb:authData>
         <!--Optional:-->
         <gsb:loginId>?</gsb:loginId>
      </gsb:BulkAuthorize>
   </soap:Body>
</soap:Envelope>
```


The meaning of all the placeholders are as follows;



&lt;gsb:listURL&gt;

**: The SharePoint List URL to which the document to be authorized belongs to. This URL can be easily retreived from docId (on GSA). The docId on GSA are complex IDs sent by connector for every documents. These IDs are in the form: 'Parent\_List\_URL|ListItem\_ID'. The Parent\_List\_URL can be used as the value for**

&lt;gsb:listURL&gt;

 placeholder in the request.



&lt;gsb:listItemId&gt;

**: The ListItem ID of the item/document. In case of list items, it's a numeric ID. If the item represents the list itself, it's the GUID of the list. From a given GSA's docId 'Parent\_List\_URL|ListItem\_ID', 'listItemId' value can be easily retrieved by getting everything after |.**



&lt;gsb:isAllowed&gt;

**: Identifies the authorization status. Keep it false by default in the request that is sent.**



&lt;gsb:error&gt;

**: Keeps track of any problems occurred while authorizing the document. Mainly used for logging purpose. Keep it blank in the request.**



&lt;gsb:complexDocId&gt;

**: The actual decoded docId value recieved from GSA. Decoding is required because GSA encodes the docIds. So, 'Parent\_List\_URL|ListItem\_ID' may look like 'Parent\_List\_URL%7CListItem\_ID'**

Note: Set username, password and domain in the request properties.

Following is a sample SOAP request
```
<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:gsb="gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com">
   <soap:Header/>
   <soap:Body>
      <gsb:BulkAuthorize>
         <!--Optional:-->
         <gsb:authData>
            <!--Zero or more repetitions:-->
            <gsb:AuthData>
               <!--Optional:-->
               <gsb:listURL>http://gdc04.gdc-psl.net:6666/Shared%20Documents/Forms/AllItems.aspx</gsb:listURL>
               <!--Optional:-->
               <gsb:listItemId>1</gsb:listItemId>
               <gsb:isAllowed>false</gsb:isAllowed>
               <!--Optional:-->
               <gsb:error></gsb:error>
               <!--Optional:-->
               <gsb:complexDocId>http://gdc04.gdc-psl.net:6666/Shared%20Documents/Forms/AllItems.aspx|1</gsb:complexDocId>
            </gsb:AuthData>
         </gsb:authData>
         <!--Optional:-->
         <gsb:loginId>?</gsb:loginId>
      </gsb:BulkAuthorize>
   </soap:Body>
</soap:Envelope>
```