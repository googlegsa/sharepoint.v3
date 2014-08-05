<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/">
      <All_Results>

      <xsl:for-each select="GSP/RES/R">
        <Result>
          <workid>
            <xsl:value-of select="@N"/>
          </workid>
          <rank>
            <xsl:value-of select="RK"/>  
          </rank>
          <title>
            <xsl:value-of select="T"/>
          </title>
          <author>Author</author>
          <size>
            <xsl:value-of select="HAS/C/@SZ"/>
          </size>

         
          
          <!--
          Note: The will be 3 cases for the extensions
          1. extn with 3 chars (e.g. .aspx)
          2. extn with 4 chars (e.g. .asp)
          3. list items (they always end with ?ID=[1-9][0-9]*
          -->
          <xsl:variable name="FExtn" select="substring(U,string-length(U)-3,4)"/>
            
          <sitename>
            <xsl:value-of select="UD" />
          </sitename>
          <url>
            <xsl:value-of select="U" />
          </url>
          <imageurl>
            <!--/_layouts/images/aspx16.gif-->
           <!--<xsl:copy-of select="$FExtn" />-->
            <xsl:choose>
              <xsl:when test="$FExtn = 'aspx'">
                /_layouts/images/aspx16.gif
              </xsl:when>
              
              <xsl:when test="$FExtn = '.asp'">
                /_layouts/images/ICASP.GIF
              </xsl:when>

              <xsl:when test="$FExtn = '.bmp'">
                /_layouts/images/ICBMP.GIF
              </xsl:when>

              <xsl:when test="$FExtn = '.doc'">
                /_layouts/images/ICDOC.GIF
              </xsl:when>

              <xsl:when test="$FExtn = '.zip'">
                /_layouts/images/ICzip.GIF
              </xsl:when>

              <xsl:when test="$FExtn = '.xls'">
                /_layouts/images/ICxls.GIF
              </xsl:when>

              <xsl:when test="$FExtn = '.txt'">
                /_layouts/images/ICtxt.GIF
              </xsl:when>

              <xsl:when test="$FExtn = '.png'">
                /_layouts/images/ICpng.GIF
              </xsl:when>

              <xsl:when test="$FExtn = 'docx'">
                /_layouts/images/ICDOCX.GIF
              </xsl:when>

			  <xsl:when test="$FExtn = '.ppt'">
                /_layouts/images/ICPPT.GIF
              </xsl:when>
			  
			  <xsl:when test="$FExtn = '.pps'">
                /_layouts/images/ICPPS.GIF
              </xsl:when>
			  
              <xsl:otherwise>
                /_layouts/images/ICGEN.GIF
              </xsl:otherwise>
            </xsl:choose>
          </imageurl>
          <description>
                 <xsl:value-of select="S" disable-output-escaping="no"/>
          </description>
          <write>
            <xsl:value-of select="CRAWLDATE"/>
          </write>

        </Result>
      </xsl:for-each>
    </All_Results>


  </xsl:template>

</xsl:stylesheet>