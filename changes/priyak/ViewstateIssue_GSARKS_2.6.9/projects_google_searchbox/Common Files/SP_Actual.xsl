<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >
  <xsl:param name="ResultsBy" />
  <xsl:param name="ViewByUrl" />
  <xsl:param name="ViewByValue" />
  <xsl:param name="IsNoKeyword" />
  <xsl:param name="IsFixedQuery" />
  <xsl:param name="ShowActionLinks" />
  <xsl:param name="MoreResultsText" />
  <xsl:param name="MoreResultsLink" />
  <xsl:param name="CollapsingStatusLink" />
  <xsl:param name="CollapseDuplicatesText" />
  <xsl:param name="AlertMeLink" />
  <xsl:param name="AlertMeText" />
  <xsl:param name="SrchRSSText" />
  <xsl:param name="SrchRSSLink" />
  <xsl:param name="ShowMessage" />
  <xsl:param name="IsThisListScope" />
  <xsl:param name="DisplayDiscoveredDefinition" select="True" />
  <xsl:param name="NoFixedQuery" />
  <xsl:param name="NoKeyword" />
  <xsl:param name="NoResults" />
  <xsl:param name="NoResults1" />
  <xsl:param name="NoResults2" />
  <xsl:param name="NoResults3" />
  <xsl:param name="NoResults4" />
  <xsl:param name="DefinitionIntro" />

  <!-- When there is keywory to issue the search -->
  <xsl:template name="dvt_1.noKeyword">
    <span class="srch-description">
      <xsl:choose>
        <xsl:when test="$IsFixedQuery">
          <xsl:value-of select="$NoFixedQuery" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$NoKeyword" />
        </xsl:otherwise>
      </xsl:choose>
    </span>
  </xsl:template>


  <!-- When empty result set is returned from search -->
  <xsl:template name="dvt_1.empty">
    <div class="srch-sort">
      <xsl:if test="$AlertMeLink and $ShowActionLinks">
        <span class="srch-alertme" >
          <a href ="{$AlertMeLink}" id="CSR_AM1" title="{$AlertMeText}">
            <img style="vertical-align: middle;" src="/_layouts/images/bell.gif" alt="" border="0"/>
            <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
            <xsl:value-of select="$AlertMeText" />
          </a>
        </span>
      </xsl:if>

      <xsl:if test="string-length($SrchRSSLink) &gt; 0 and $ShowActionLinks">
        <xsl:if test="$AlertMeLink">
          |
        </xsl:if>
        <a type="application/rss+xml" href ="{$SrchRSSLink}" title="{$SrchRSSText}" id="SRCHRSSL">
          <img style="vertical-align: middle;" border="0" src="/_layouts/images/rss.gif" alt=""/>
          <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
          <xsl:value-of select="$SrchRSSText"/>
        </a>
      </xsl:if>
    </div>
    <br/>
    <br/>

    <span class="srch-description" id="CSR_NO_RESULTS">
      <xsl:value-of select="$NoResults" />

      <ol>
        <li>
          <xsl:value-of select="$NoResults1" />
        </li>
        <li>
          <xsl:value-of select="$NoResults2" />
        </li>
        <li>
          <xsl:value-of select="$NoResults3" />
        </li>
        <li>
          <xsl:value-of select="$NoResults4" />
        </li>
      </ol>
    </span>
  </xsl:template>


  <!-- Main body template. Sets the Results view (Relevance or date) options -->
  <xsl:template name="dvt_1.body">
    <div class="srch-results">
      <xsl:if test="$ShowActionLinks">
        <div class="srch-sort">
          <xsl:value-of select="$ResultsBy" />
          <xsl:if test="$ViewByUrl">
            |
            <a href ="{$ViewByUrl}" id="CSR_RV" title="{$ViewByValue}">
              <xsl:value-of select="$ViewByValue" />
            </a>
          </xsl:if>
          <xsl:if test="$AlertMeLink">
            |
            <span class="srch-alertme" >
              <a href ="{$AlertMeLink}" id="CSR_AM2" title="{$AlertMeText}">
                <img style="vertical-align: middle;" src="/_layouts/images/bell.gif" alt="" border="0"/>
                <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
                <xsl:value-of select="$AlertMeText" />
              </a>
            </span>
          </xsl:if>
          <xsl:if test="string-length($SrchRSSLink) &gt; 0">
            |
            <a type="application/rss+xml" href ="{$SrchRSSLink}" title="{$SrchRSSText}" id="SRCHRSSL">
              <img style="vertical-align: middle;" border="0" src="/_layouts/images/rss.gif" alt=""/>
              <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
              <xsl:value-of select="$SrchRSSText"/>
            </a>
          </xsl:if>
        </div>
        <br />
        <br />
      </xsl:if>
      <xsl:apply-templates />

    </div>
    <xsl:call-template name="DisplayMoreResultsAnchor" />
  </xsl:template>
  <!-- This template is called for each result -->
  <xsl:template match="Result">
    <xsl:variable name="id" select="id"/>
    <xsl:variable name="url" select="url"/>
    <!--<xsl:variable name="managedcategorytype" select="managedcategorytype"/>-->
    <span class="srch-Icon">
      <a href="{$url}" id="{concat('CSR_IMG_',$id)}" title="{$url}">
        <img align="absmiddle" src="{imageurl}" border="0" alt="{imageurl/@imageurldescription}" />
      </a>
    </span>
    <span class="srch-Title">
      <a href="{$url}" id="{concat('CSR_',$id)}" title="{$url}">
        <xsl:choose>
          <xsl:when test="hithighlightedproperties/HHTitle[. != '']">
            <xsl:call-template name="HitHighlighting">
              <xsl:with-param name="hh" select="hithighlightedproperties/HHTitle" />
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="title" disable-output-escaping="yes"/>
          </xsl:otherwise>
        </xsl:choose>
      </a>
      <br/>
    </span>

    <xsl:choose>
      <xsl:when test="$IsThisListScope = 'True' and contentclass[. = 'STS_ListItem_PictureLibrary'] and picturethumbnailurl[. != '']">
        <div style="padding-top: 2px; padding-bottom: 2px;">
          <a href="{$url}" id="{concat('CSR_P',$id)}" title="{title}">
            <img src="{picturethumbnailurl}" alt="" />
          </a>
        </div>
      </xsl:when>
    </xsl:choose>
    <div class="srch-Description">
      <xsl:choose>
        <xsl:when test="hithighlightedsummary[. != '']">
          <xsl:call-template name="HitHighlighting">
            <xsl:with-param name="hh" select="hithighlightedsummary" />
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="description[. != '']">
          <xsl:value-of select="description" disable-output-escaping="yes"/>
        </xsl:when>
      </xsl:choose>
    </div >
    <p class="srch-Metadata">
           
    </p>


  </xsl:template>

  <xsl:template name="HitHighlighting">
    <xsl:param name="hh" />
    <xsl:apply-templates select="$hh"/>
  </xsl:template>

  <xsl:template match="ddd">
    &#8230;
  </xsl:template>
  <xsl:template match="c0">
    <b>
      <xsl:value-of select="."/>
    </b>
  </xsl:template>
  <xsl:template match="c1">
    <b>
      <xsl:value-of select="."/>
    </b>
  </xsl:template>
  <xsl:template match="c2">
    <b>
      <xsl:value-of select="."/>
    </b>
  </xsl:template>
  <xsl:template match="c3">
    <b>
      <xsl:value-of select="."/>
    </b>
  </xsl:template>
  <xsl:template match="c4">
    <b>
      <xsl:value-of select="."/>
    </b>
  </xsl:template>
  <xsl:template match="c5">
    <b>
      <xsl:value-of select="."/>
    </b>
  </xsl:template>
  <xsl:template match="c6">
    <b>
      <xsl:value-of select="."/>
    </b>
  </xsl:template>
  <xsl:template match="c7">
    <b>
      <xsl:value-of select="."/>
    </b>
  </xsl:template>
  <xsl:template match="c8">
    <b>
      <xsl:value-of select="."/>
    </b>
  </xsl:template>
  <xsl:template match="c9">
    <b>
      <xsl:value-of select="."/>
    </b>
  </xsl:template>


  <!-- The size attribute for each result is prepared here -->
  <xsl:template name="DisplaySize">
    <xsl:param name="size" />
    <xsl:if test='string-length($size) &gt; 0'>
      <xsl:if test="number($size) &gt; 0">
        -
        <xsl:choose>
          <xsl:when test="round($size div 1024) &lt; 1">
            <xsl:value-of select="$size" /> Bytes
          </xsl:when>
          <xsl:when test="round($size div (1024 *1024)) &lt; 1">
            <xsl:value-of select="round($size div 1024)" />KB
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="round($size div (1024 * 1024))"/>MB
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:if>
  </xsl:template>

 
  <!-- A generic template to display string with non 0 string length (used for author and lastmodified time -->
  <xsl:template name="DisplayString">
    <xsl:param name="str" />
    <xsl:if test='string-length($str) &gt; 0'>
      -
      <xsl:value-of select="$str" />
    </xsl:if>
  </xsl:template>

  <!-- document collapsing link setup -->
  <xsl:template name="DisplayCollapsingStatusLink">
    <xsl:param name="status"/>
    <xsl:param name="urlEncoded"/>
    <xsl:param name="id"/>
    <xsl:if test="$CollapsingStatusLink">
      <xsl:choose>
        <xsl:when test="$status=1">
          <br/>
          <xsl:variable name="CollapsingStatusHref" select="concat(substring-before($CollapsingStatusLink, '$$COLLAPSE_PARAM$$'), 'duplicates:&quot;', $urlEncoded, '&quot;', substring-after($CollapsingStatusLink, '$$COLLAPSE_PARAM$$'))"/>
          <span class="srch-dup">
            [<a href="{$CollapsingStatusHref}" id="$id" title="{$CollapseDuplicatesText}">
              <xsl:value-of select="$CollapseDuplicatesText"/>
            </a>]
          </span>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
  </xsl:template>
  <!-- The "view more results" for fixed query -->
  <xsl:template name="DisplayMoreResultsAnchor">
    <xsl:if test="$MoreResultsLink">
      <a href="{$MoreResultsLink}" id="CSR_MRL">
        <xsl:value-of select="$MoreResultsText"/>
      </a>
    </xsl:if>
  </xsl:template>

  <xsl:template match="All_Results/DiscoveredDefinitions">
    <xsl:variable name="FoundIn" select="DDFoundIn" />
    <xsl:variable name="DDSearchTerm" select="DDSearchTerm" />
    <xsl:if test="$DisplayDiscoveredDefinition = 'True' and string-length($DDSearchTerm) &gt; 0">
      <script language="javascript">
        function ToggleDefinitionSelection()
        {
        var selection = document.getElementById("definitionSelection");
        if (selection.style.display == "none")
        {
        selection.style.display = "inline";
        }
        else
        {
        selection.style.display = "none";
        }
        }
      </script>
      <div>
        <a href="#" onclick="ToggleDefinitionSelection(); return false;">
          <xsl:value-of select="$DefinitionIntro" />
          <b>
            <xsl:value-of select="$DDSearchTerm"/>
          </b>
        </a>
        <div id="definitionSelection" class="srch-Description" style="display:none;">
          <xsl:for-each select="DDefinitions/DDefinition">
            <br/>
            <xsl:variable name="DDUrl" select="DDUrl" />
            <xsl:value-of select="DDStart"/>
            <b>
              <xsl:value-of select="DDBold"/>
            </b>
            <xsl:value-of select="DDEnd"/>
            <br/>
            <xsl:value-of select="$FoundIn"/>
            <a href="{$DDUrl}">
              <xsl:value-of select="DDTitle"/>
            </a>
          </xsl:for-each>
        </div>
      </div>
    </xsl:if>
  </xsl:template>

  <!-- XSL transformation starts here -->
  <xsl:template match="/">
    <xsl:if test="$AlertMeLink">
      <input type="hidden" name="P_Query" />
      <input type="hidden" name="P_LastNotificationTime" />
    </xsl:if>
    <xsl:choose>
      <xsl:when test="$IsNoKeyword = 'True'" >
        <xsl:call-template name="dvt_1.noKeyword" />
      </xsl:when>
      <xsl:when test="$ShowMessage = 'True'">
        <xsl:call-template name="dvt_1.empty" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="dvt_1.body"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- End of Stylesheet -->
</xsl:stylesheet>