# How to hide all the metadata from search results while using GSA stylesheet in the search box? #

# Introduction #

By default, when you deploy SPS\_Frontend stylesheet on GSA and use GSA stylesheet in the Google Search box for SharePoint, you see all the metadata associated with the search results. Some users may not want to show all the metadata in the search results.


# Solution #

1. Comment following lines in the SPS\_frontend stylesheet:


  * **Meta tags** -->
> <!--

&lt;xsl:if test="$show\_meta\_tags != '0'"&gt;


> > 

&lt;xsl:apply-templates select="MT"/&gt;



> 

&lt;/xsl:if&gt;



2. Save the front end on GSA and perform search from the search box. You should see only the Doc URL, content snippet, and last modified date in the search results.