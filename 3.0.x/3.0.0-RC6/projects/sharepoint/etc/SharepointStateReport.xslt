<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:msxsl="urn:schemas-microsoft-com:xslt" exclude-result-prefixes="msxsl"
>
  <xsl:output indent="yes" method="html" doctype-public="html"/>

  <xsl:template match="/State">
    <html>
      <head>
        <title>Google Search Appliance Connector for SharePoint - State File Report</title>
      </head>
      <body width="100%">
        <h1>Google Search Appliance Connector for SharePoint - State File Report</h1>

        <a name="Top">
          <h2>Contents</h2>
        </a>
        <ol>
          <li>
            <a href="#ConnectorConfig">Connector Config</a>
          </li>
          <li>
            <a href="#LastCrawled">Last Crawled</a>
          </li>
          <li>
            <a href="#CurrentStatistics">Current Statistics</a>
          </li>
          <li>
            <a href="#WebsCompleted">Web URLs - Completed</a>
          </li>
          <li>
            <a href="#WebsPending">Web URLs - Pending</a>
          </li>
          <li>
            <a href="#ListsCompleted">List URLs - Completed</a>
          </li>
          <li>
            <a href="#ListsPending">List URLs - Pending</a>
          </li>
        </ol>

        <a name="ConnectorConfig">
          <h2>Connector Config</h2>
        </a>
        <b>Feedtype: </b>
        <xsl:value-of select="FeedType/@Type"/>

        <a name="LastCrawled">
          <h2>Last Crawled</h2>
        </a>
        <b>Web:  </b>
        <a>
          <xsl:value-of select="LastCrawledWebStateID/@ID"/>
        </a>
        <br />
        <b>List: </b>
        <xsl:value-of select="LastCrawledListStateID/@ID"/>
        <br />

        <a name="CurrentStatistics">
          <h2>Current Statistics</h2>
        </a>
        <table border="1" cellpadding="2" cellspacing="0" style="text-align:right">
          <thead style="font-weight:bold">
            <td>Node</td>
            <td>Total Found</td>
            <td>Explored</td>
            <td>% Explored</td>
            <td>Remaining</td>
          </thead>
          <tr>
            <td>Webs</td>
            <xsl:variable name="webStateTotalCount" select="count(WebState)"/>
            <xsl:variable name="webStateExploredCount" select="count(WebState[ListState])"/>
            <td>
              <xsl:value-of select="$webStateTotalCount"/>
            </td>
            <td>
              <xsl:value-of select="$webStateExploredCount"/>
            </td>
            <td>
              <xsl:value-of select="floor($webStateExploredCount div $webStateTotalCount * 100)"/>%
            </td>
            <td>
              <xsl:value-of select="$webStateTotalCount - $webStateExploredCount"/>
            </td>
          </tr>
          <tr>
            <td>Lists</td>
            <xsl:variable name="listStateTotalCount" select="count(WebState/ListState)"/>
            <xsl:variable name="listStateExploredCount" select="count(WebState/ListState[@ID=LastDocCrawled/@ID])"/>
            <td>
              <xsl:value-of select="$listStateTotalCount"/>
            </td>
            <td>
              <xsl:value-of select="$listStateExploredCount"/>
            </td>
            <td>
              <xsl:value-of select=" floor($listStateExploredCount div $listStateTotalCount * 100)"/>%
            </td>
            <td>
              <xsl:value-of select="$listStateTotalCount - $listStateExploredCount"/>
            </td>
          </tr>
        </table>

        <a name="WebsCompleted">
          <h2>Web URLs - Completed</h2>
        </a>
        <table border="1" cellpadding="2" cellspacing="0">
          <thead style="font-weight:bold">
            <td>Title</td>
            <td>URL</td>
          </thead>
          <xsl:for-each select="WebState[ListState]">
            <tr>
              <td>
                <xsl:value-of select="@WebTitle"/>
              </td>
              <td>
                <xsl:value-of select="@ID"/>
              </td>
            </tr>
          </xsl:for-each>
        </table>

        <a name="WebsPending">
          <h2>Web URLs - Pending</h2>
        </a>
        <table border="1" cellpadding="2" cellspacing="0">
          <thead style="font-weight:bold">
            <td>Title</td>
            <td>URL</td>
          </thead>
          <xsl:for-each select="WebState[not(ListState)]">
            <tr>
              <td>
                <xsl:value-of select="@WebTitle"/>
              </td>
              <td>
                <xsl:value-of select="@ID"/>
              </td>
            </tr>
          </xsl:for-each>
        </table>

        <a name="ListsCompleted">
          <h2>List URLs - Completed</h2>
        </a>
        <table border="1" cellpadding="2" cellspacing="0">
          <thead style="font-weight:bold">
            <td>URL</td>
          </thead>
          <xsl:for-each select="WebState/ListState[@ID=LastDocCrawled/@ID]">
            <tr>
              <td>
                <xsl:value-of select="@URL"/>
              </td>
            </tr>
          </xsl:for-each>
        </table>

        <a name="ListsPending">
          <h2>List URLs - Pending</h2>
        </a>
        <table border="1" cellpadding="2" cellspacing="0">
          <thead style="font-weight:bold">
            <td>URL</td>
            <td>Last Document ID</td>
          </thead>
          <xsl:for-each select="WebState/ListState[@ID!=LastDocCrawled/@ID]">
            <tr>
              <td>
                <xsl:value-of select="@URL"/>
              </td>
              <td>
                <xsl:value-of select="LastDocCrawled/@ID"/>
              </td>
            </tr>
          </xsl:for-each>
        </table>

      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
