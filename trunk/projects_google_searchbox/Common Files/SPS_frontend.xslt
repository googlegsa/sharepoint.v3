
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

<!-- *** Sharepoint GUI Framework Modified styleSheet          ***
     *** Key components are marked in the file with SPSMod     *** -->
     
     
<!-- *** START OF STYLESHEET *** -->

<!-- **********************************************************************
 XSL to format the search output for Google Search Appliance
     ********************************************************************** -->

<!-- **********************************************************************
 include customer-onebox.xsl, which is auto-generated from the customer's
 set of OneBox Module definitions, and in turn invokes either the default
 OneBox template, or the customer's:
********************************************************************** -->
  <xsl:include href="customer-onebox.xsl"/>

  <xsl:output method="html"/>

<!-- **********************************************************************
 Logo setup (can be customized)
     - whether to show logo: 0 for FALSE, 1 (or non-zero) for TRUE
     - logo url
     - logo size: '' for default image size
     ********************************************************************** -->
  <xsl:variable name="show_logo">0</xsl:variable>
  <xsl:variable name="logo_url">images/Google_Left.gif</xsl:variable>
  <xsl:variable name="logo_width">100</xsl:variable>
  <xsl:variable name="logo_height">37</xsl:variable>

<!-- **********************************************************************
 Global Style variables (can be customized): '' for using browser's default
     ********************************************************************** -->
<!-- *** SPSMod:  Remove unecessary colours/formatting - will be controlled 
		largely by Sharepoint					*** -->


  <xsl:variable name="global_font">Tahoma</xsl:variable>
  <xsl:variable name="global_font_size"></xsl:variable>
  <xsl:variable name="global_bg_color"></xsl:variable>
  <xsl:variable name="global_text_color">#333333</xsl:variable>
  <xsl:variable name="global_link_color"></xsl:variable>
  <xsl:variable name="global_vlink_color"></xsl:variable>
  <xsl:variable name="global_alink_color"></xsl:variable>


<!-- **********************************************************************
 Result page components (can be customized)
     - whether to show a component: 0 for FALSE, non-zero (e.g., 1) for TRUE
     - text and style
     ********************************************************************** -->

<!-- *** choose result page header: '', 'provided', 'mine', or 'both' *** -->
  <xsl:variable name="choose_result_page_header">both</xsl:variable>

<!-- *** customize provided result page header *** -->
  <xsl:variable name="show_swr_link">1</xsl:variable>
  <xsl:variable name="swr_search_anchor_text">Search Within Results</xsl:variable>
  <xsl:variable name="show_result_page_adv_link">0</xsl:variable>
  <xsl:variable name="adv_search_anchor_text">Advanced Search</xsl:variable>
  <xsl:variable name="show_result_page_help_link">0</xsl:variable>
  <xsl:variable name="search_help_anchor_text">Search Tips</xsl:variable>
  <xsl:variable name="show_alerts_link">0</xsl:variable>
  <xsl:variable name="alerts_anchor_text">Alerts</xsl:variable>

<!-- *** search boxes *** -->
  <xsl:variable name="show_top_search_box">0</xsl:variable>
  <xsl:variable name="show_bottom_search_box">0</xsl:variable>
  <xsl:variable name="search_box_size">32</xsl:variable>

<!-- *** choose search button type: 'text' or 'image' *** -->
  <xsl:variable name="choose_search_button">text</xsl:variable>
  <xsl:variable name="search_button_text">Google Search</xsl:variable>
  <xsl:variable name="search_button_image_url"></xsl:variable>
  <xsl:variable name="search_collections_xslt"></xsl:variable>

<!-- *** search info bars *** -->
  <xsl:variable name="show_search_info">1</xsl:variable>

<!-- *** choose separation bar: 'ltblue', 'blue', 'line', 'nothing' *** -->
  <xsl:variable name="choose_sep_bar">nothing</xsl:variable>
  <xsl:variable name="sep_bar_std_text"></xsl:variable>
  <xsl:variable name="sep_bar_adv_text">Advanced Search</xsl:variable>
  <xsl:variable name="sep_bar_error_text">Error</xsl:variable>

<!-- *** navigation bars: '', 'google', 'link', or 'simple'*** -->
  <xsl:variable name="show_top_navigation">1</xsl:variable>
  <xsl:variable name="choose_bottom_navigation">link</xsl:variable>
  <xsl:variable name="my_nav_align">right</xsl:variable>
  <xsl:variable name="my_nav_size">-1</xsl:variable>
  <xsl:variable name="my_nav_color">#6f6f6f</xsl:variable>

<!-- *** sort by date/relevance *** -->
  <xsl:variable name="show_sort_by">0</xsl:variable>

<!-- *** spelling suggestions *** -->
  <xsl:variable name="show_spelling">1</xsl:variable>
  <xsl:variable name="spelling_text">Did you mean:</xsl:variable>
  <xsl:variable name="spelling_text_color">#cc0000</xsl:variable>

<!-- *** synonyms suggestions *** -->
  <xsl:variable name="show_synonyms">1</xsl:variable>
  <xsl:variable name="synonyms_text">You could also try:</xsl:variable>
  <xsl:variable name="synonyms_text_color">#cc0000</xsl:variable>

<!-- *** keymatch suggestions *** -->
  <xsl:variable name="show_keymatch">1</xsl:variable>
  <xsl:variable name="keymatch_text">KeyMatch</xsl:variable>
  <xsl:variable name="keymatch_text_color">#2255aa</xsl:variable>
  <xsl:variable name="keymatch_bg_color">#e8e8ff</xsl:variable>

<!-- *** Google Desktop integration *** -->
  <xsl:variable name="egds_show_search_tabs">1</xsl:variable>
  <xsl:variable name="egds_appliance_tab_label">Appliance</xsl:variable>
  <xsl:variable name="egds_show_desktop_results">1</xsl:variable>

<!-- *** onebox information *** -->
  <xsl:variable name="show_onebox">1</xsl:variable>

<!-- *** analytics information *** -->
  <xsl:variable name="analytics_account"></xsl:variable>
  
<!-- *** Dynamic Navigation *** -->
<!-- *** show_dynamic_navigation will decide whether dynamic navigation will be displayed to the end user or not. Value 1 means it will be shown and value 0 means it will not be shown. *** -->
<xsl:variable name="show_dynamic_navigation">1</xsl:variable>
<xsl:variable name="dyn_nav_max_rows">6</xsl:variable>
<xsl:variable name="render_dynamic_navigation"><xsl:if
  test="$show_dynamic_navigation != '0' and count(/GSP/RES/PARM) > 0">1</xsl:if>
</xsl:variable>

<!-- *** Sidebar for holding elements that can load data asynchronously *** -->
<xsl:variable name="show_sidebar">
  <xsl:choose>
    <xsl:when test="($show_dynamic_navigation != '1' and /GSP/Q != '')">
      <xsl:value-of select="'1'"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="'0'"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:variable>

<!-- **********************************************************************
 Result elements (can be customized)
     - whether to show an element ('1' for yes, '0' for no)
     - font/size/color ('' for using style of the context)
     ********************************************************************** -->

<!-- *** result title and snippet *** -->
  <xsl:variable name="show_res_title">1</xsl:variable>
  <xsl:variable name="res_title_color">#003399</xsl:variable>
  <xsl:variable name="res_title_size">0.8em</xsl:variable>
  <xsl:variable name="show_res_snippet">1</xsl:variable>
  <xsl:variable name="res_snippet_size">80%</xsl:variable>

<!-- *** keyword match (in title or snippet) *** -->
  <xsl:variable name="res_keyword_color"></xsl:variable>
  <xsl:variable name="res_keyword_size"></xsl:variable>
  <xsl:variable name="res_keyword_format">b</xsl:variable>  <!-- 'b' for bold -->

<!-- *** link URL *** -->
  <xsl:variable name="show_res_url">1</xsl:variable>
  <xsl:variable name="res_url_color">#008000</xsl:variable>
  <xsl:variable name="res_url_size">2</xsl:variable>
  <xsl:variable name="truncate_result_urls">1</xsl:variable>
  <xsl:variable name="truncate_result_url_length">100</xsl:variable>

<!-- *** misc elements *** -->
<!-- *** Value for show_meta_tags is set to zero, to reduce scrolling for the users *** -->
  <xsl:variable name="show_meta_tags">0</xsl:variable>
  <xsl:variable name="show_res_size">1</xsl:variable>
  <xsl:variable name="show_res_date">1</xsl:variable>
  <xsl:variable name="show_res_cache">1</xsl:variable>

<!-- *** used in result cache link, similar pages link, and description *** -->
  <xsl:variable name="faint_color">#7777cc</xsl:variable>

<!-- *** show secure results radio button *** -->
  <xsl:variable name="show_secure_radio">1</xsl:variable>

<!-- **********************************************************************
 Other variables (can be customized)
     ********************************************************************** -->

<!-- *** SPSMod:  Sharepoint Variables *** -->
  <xsl:variable name="show_sps_icons">1</xsl:variable>
  <xsl:variable name="show_sps_linebreaks">1</xsl:variable>

<!-- *** page title *** -->
  <xsl:variable name="front_page_title">Search Home</xsl:variable>
  <xsl:variable name="result_page_title">Search Results</xsl:variable>
  <xsl:variable name="adv_page_title">Advanced Search</xsl:variable>
  <xsl:variable name="error_page_title">Error</xsl:variable>
  <xsl:variable name="swr_page_title">Search Within Results</xsl:variable>

<!-- *** choose adv_search page header: '', 'provided', 'mine', or 'both' *** -->
  <xsl:variable name="choose_adv_search_page_header">both</xsl:variable>

<!-- *** cached page header text *** -->
  <xsl:variable name="cached_page_header_text">This is the cached copy of</xsl:variable>

<!-- *** error message text *** -->
  <xsl:variable name="server_error_msg_text">A server error has occurred.</xsl:variable>
  <xsl:variable name="server_error_des_text">Check server response code in details.</xsl:variable>
  <xsl:variable name="xml_error_msg_text">Unknown XML result type.</xsl:variable>
  <xsl:variable name="xml_error_des_text">View page source to see the offending XML.</xsl:variable>

<!-- *** advanced search page panel background color *** -->
  <xsl:variable name="adv_search_panel_bgcolor">#cbdced</xsl:variable>

<!-- *** dyanmic result cluster options *** -->
  <xsl:variable name="show_res_clusters">0</xsl:variable>
  <xsl:variable name="res_cluster_position">right</xsl:variable>

<!-- **********************************************************************
 My global page header/footer (can be customized)
     ********************************************************************** -->
<xsl:template name="my_page_header">
    <!-- *** replace the following with your own xhtml code or replace the text
   between the xsl:text tags with html escaped html code *** -->
    <xsl:text disable-output-escaping="yes"> &lt;!-- Please enter html code below. --&gt;</xsl:text>
  </xsl:template>

<xsl:template name="my_page_footer">
    <span class="p">
      <xsl:text disable-output-escaping="yes"> &lt;!-- Please enter html code below. --&gt;</xsl:text>
    </span>
</xsl:template>


<!-- **********************************************************************
 Logo template (can be customized)
     ********************************************************************** -->
<xsl:template name="logo">
    <a href="{$home_url}"><img src="{$logo_url}"
      width="{$logo_width}" height="{$logo_height}"
      alt="Go to Google Home" border="0" />
    </a>
</xsl:template>


<!-- **********************************************************************
 Search result page header (can be customized): logo and search box
     ********************************************************************** -->
<xsl:template name="result_page_header">
    <table border="0" cellpadding="0" cellspacing="0">
      <xsl:if test="$show_logo != '0'">
        <tr>
          <td rowspan="3" valign="top">
            <xsl:call-template name="logo"/>
            <xsl:call-template name="nbsp3"/>
          </td>
        </tr>
      </xsl:if>
      <xsl:if test="$show_top_search_box != '0'">
        <tr>
          <td valign="middle">
            <xsl:call-template name="search_box">
              <xsl:with-param name="type" select="'std_top'"/>
            </xsl:call-template>
          </td>
        </tr>
      </xsl:if>
      <xsl:if test="/GSP/CT">
        <tr>
          <td valign="top">
            <br/>
            <xsl:call-template name="stopwords"/>
            <br/>
          </td>
        </tr>
      </xsl:if>
    </table>
</xsl:template>


<!-- **********************************************************************
 Search within results page header (can be customized): logo and search box
     ********************************************************************** -->
  <xsl:template name="swr_page_header">
    <table border="0" cellpadding="0" cellspacing="0">
      <xsl:if test="$show_logo != '0'">
        <tr>
          <td rowspan="3" valign="top">
            <xsl:call-template name="logo"/>
            <xsl:call-template name="nbsp3"/>
          </td>
        </tr>
      </xsl:if>
      <xsl:if test="$show_top_search_box != '0'">
        <tr>
          <td valign="middle">
            <xsl:call-template name="search_box">
              <xsl:with-param name="type" select="'swr'"/>
            </xsl:call-template>
          </td>
        </tr>
      </xsl:if>
    </table>
</xsl:template>


<!-- **********************************************************************
 Home search page header (can be customized): logo and search box
     ********************************************************************** -->
<xsl:template name="home_page_header">
    <table border="0" cellpadding="0" cellspacing="0">
      <xsl:if test="$show_logo != '0'">
        <tr>
          <td rowspan="3" valign="top">
            <xsl:call-template name="logo"/>
            <xsl:call-template name="nbsp3"/>
          </td>
        </tr>
      </xsl:if>
      <xsl:if test="$show_top_search_box != '0'">
        <tr>
          <td valign="middle">
            <xsl:call-template name="search_box">
              <xsl:with-param name="type" select="'home'"/>
            </xsl:call-template>
          </td>
        </tr>
      </xsl:if>
    </table>
</xsl:template>


<!-- **********************************************************************
 Separation bar variables (used in advanced search header and result page)
     ********************************************************************** -->
<xsl:variable name="sep_bar_border_color">
    <xsl:choose>
      <xsl:when test="$choose_sep_bar = 'ltblue'">#3366cc</xsl:when>
      <xsl:when test="$choose_sep_bar = 'blue'">#3366cc</xsl:when>
      <xsl:otherwise><xsl:value-of select="$global_bg_color"/></xsl:otherwise>
    </xsl:choose>
</xsl:variable>

<xsl:variable name="sep_bar_bg_color">
    <xsl:choose>
      <xsl:when test="$choose_sep_bar = 'ltblue'">#e5ecf9</xsl:when>
      <xsl:when test="$choose_sep_bar = 'blue'">#3366cc</xsl:when>
      <xsl:otherwise><xsl:value-of select="$global_bg_color"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

<xsl:variable name="sep_bar_text_color">
    <xsl:choose>
      <xsl:when test="$choose_sep_bar = 'ltblue'">#000000</xsl:when>
      <xsl:when test="$choose_sep_bar = 'blue'">#ffffff</xsl:when>
      <xsl:otherwise><xsl:value-of select="$global_text_color"/></xsl:otherwise>
    </xsl:choose>
</xsl:variable>

<!-- **********************************************************************
 Advanced search page header HTML (can be customized)
     ********************************************************************** -->
<xsl:template name="advanced_search_header">
    <table border="0" cellpadding="0" cellspacing="0">

      <tr>

        <td valign="top">
          <xsl:if test="$show_logo != '0'">
            <xsl:call-template name="logo"/>
          </xsl:if>
        </td>
      </tr>
    </table>
</xsl:template>


<!-- **********************************************************************
 Cached page header (can be customized)
     ********************************************************************** -->
<xsl:template name="cached_page_header">
    <xsl:param name="cached_page_url"/>
    <xsl:variable name="stripped_url" select="substring-after($cached_page_url,
                                                            '://')"/>
<table border="1" width="100%">
      <tr>
        <td>
          <table border="1" width="100%" cellpadding="10" cellspacing="0"
            bgcolor="{$global_bg_color}" color="{$global_bg_color}">
            <tr>
              <td>
                <font face="{$global_font}" color="{$global_text_color}" size="-1">
                  <xsl:value-of select="$cached_page_header_text"/>
                  <xsl:call-template name="nbsp"/>
                  <xsl:choose>
                    <xsl:when test="starts-with($cached_page_url,
                                          $db_url_protocol)">
                      <a href="{concat('/db/',$stripped_url)}">
                        <font color="{$global_link_color}">
                          <xsl:value-of select="$cached_page_url"/></font></a>.<br/>
                    </xsl:when>
                    <xsl:when test="starts-with($cached_page_url,
                                          $nfs_url_protocol)">
                      <a href="{concat('/nfs/',$stripped_url)}">
                        <font color="{$global_link_color}">
                          <xsl:value-of select="$cached_page_url"/></font></a>.<br/>
                    </xsl:when>
                    <xsl:when test="starts-with($cached_page_url,
                                          $smb_url_protocol)">
                      <a href="{concat('/smb/',$stripped_url)}">
                        <font color="{$global_link_color}">
                          <xsl:value-of select="$cached_page_url"/></font></a>.<br/>
                    </xsl:when>
                    <xsl:when test="starts-with($cached_page_url,
                                          $unc_url_protocol)">
                      <xsl:variable name="display_url">
                        <xsl:call-template name="convert_unc">
                          <xsl:with-param name="string" select="$stripped_url"/>
                        </xsl:call-template>
                      </xsl:variable>
                      <a href="{concat('file://',$stripped_url)}">
                        <font color="{$global_link_color}">
                          <xsl:value-of select="$display_url"/>
                        </font>
                      </a>.<br/>
                    </xsl:when>
                    <xsl:otherwise>
                      <a href="{$cached_page_url}">
                        <font color="{$global_link_color}">
                          <xsl:value-of select="$cached_page_url"/></font></a>.<br/>
                    </xsl:otherwise>
                  </xsl:choose>
                </font>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
    <hr/>
</xsl:template>



  <!-- **********************************************************************
 "Search Within Results" search input page (can be customized)
     ********************************************************************** -->
  <xsl:template name="swr_search">
    <html>
      <xsl:call-template name="langHeadStart"/>
      <title>
        <xsl:value-of select="$swr_page_title"/>
      </title>
      <xsl:call-template name="style"/>
      <xsl:call-template name="langHeadEnd"/>

      <body dir="ltr">
        <xsl:call-template name="analytics"/>

        <xsl:call-template name="my_page_header"/>
        <xsl:call-template name="swr_page_header"/>
        <hr/>
        <xsl:call-template name="copyright"/>
        <xsl:call-template name="my_page_footer"/>

      </body>
    </html>
  </xsl:template>


  <!-- **********************************************************************
 "Front door" search input page (can be customized)
     ********************************************************************** -->
  <xsl:template name="front_door">
    <html>
      <xsl:call-template name="langHeadStart"/>
      <title>
        <xsl:value-of select="$front_page_title"/>
      </title>
      <xsl:call-template name="style"/>
      <xsl:call-template name="langHeadEnd"/>

      <body dir="ltr">
        <xsl:call-template name="analytics"/>

        <xsl:call-template name="my_page_header"/>
        <xsl:call-template name="home_page_header"/>
        <hr/>
        <xsl:call-template name="copyright"/>
        <xsl:call-template name="my_page_footer"/>

      </body>
    </html>
  </xsl:template>


  <!-- **********************************************************************
 Empty result set (can be customized)
     ********************************************************************** -->
  <xsl:template name="no_RES">
    <xsl:param name="query"/>

    <!-- *** Output Google Desktop results (if enabled and any available) *** -->
    <xsl:if test="$egds_show_desktop_results != '0'">
      <xsl:call-template name="desktop_results"/>
    </xsl:if>

    <span class="p">
      <br/>
      Your search - <b>
        <xsl:value-of select="$query"/>
      </b> - did not match any documents.
      <br/>
      No pages were found containing <b>
        "<xsl:value-of select="$query"/>"
      </b>.
      <br/>
      <br/>
      Suggestions:
      <ul>
        <li>Make sure all words are spelled correctly.</li>
        <li>Try different keywords.</li>
        <xsl:if test="/GSP/PARAM[(@name='access') and(@value='a')]">
          <li>Make sure your security credentials are correct.</li>
        </xsl:if>
        <li>Try more general keywords.</li>
      </ul>
    </span>

  </xsl:template>


  <!-- ######################################################################
 We do not recommend changes to the following code.  Google Technical
 Support Personnel currently do not support customization of XSLT under
 these Technical Support Services Guidelines.  Such services may be
 provided on a consulting basis, at Google's then-current consulting
 services rates under a separate agreement, if Google personnel are
 available.  Please ask your Google Account Manager for more details if
 you are interested in purchasing consulting services.
     ###################################################################### -->


  <!-- **********************************************************************
 Global Style (do not customize)
        default font type/size/color, background color, link color
         using HTML CSS (Cascading Style Sheets)
     ********************************************************************** -->
  <xsl:template name="style">
    <style>
      <xsl:comment>
        body,td,div,.p,a,.d,.s{font-family:<xsl:value-of select="$global_font"/>}
        body,td,div,.p,a,.d{font-size: <xsl:value-of select="$global_font_size"/>}
        body,div,td,.p,.s{color:<xsl:value-of select="$global_text_color"/>}
        body,.d,.p,.s{background-color:<xsl:value-of select="$global_bg_color"/>}
        .s{font-size: <xsl:value-of select="$res_snippet_size"/>}
        .g{margin-top: 2px; margin-bottom: 2px}
        .s td{width:34em}
        .l{font-size: <xsl:value-of select="$res_title_size"/>}
        .l{color: <xsl:value-of select="$res_title_color"/>}
        a:link,.w,.w a:link{color:<xsl:value-of select="$global_link_color"/>}
        .f,.f:link,.f a:link{color:<xsl:value-of select="$faint_color"/>}
        a:visited,.f a:visited{color:<xsl:value-of select="$global_vlink_color"/>}
        a:active,.f a:active{color:<xsl:value-of select="$global_alink_color"/>}
        .t{color:<xsl:value-of select="$sep_bar_text_color"/>}
        .t{background-color:<xsl:value-of select="$sep_bar_bg_color"/>}
        .z{display:none}
        .i,.i:link{color:#a90a08}
        .a,.a:link{color:<xsl:value-of select="$res_url_color"/>}
        div.n {margin-top: 1ex}
        .n a{font-size: 10pt; color:<xsl:value-of select="$global_text_color"/>}
        .n .i{font-size: 10pt; font-weight:bold}
        .q a:visited,.q a:link,.q a:active,.q {color:#0000cc;}
        .b,.b a{font-size: 9pt; color:#003399;}
        .d{margin-right:1em; margin-left:1em;}
        div.oneboxResults {max-height:150px;overflow:hidden;}
        <xsl:if test="$show_res_clusters = '1'">
          <xsl:choose>
            <xsl:when test="$res_cluster_position = 'top'">
              div#clustering {font-size: 84%; line-height: 140%; min-height: 4.6em; _height: 4.6em; margin-top: 1em;}
              div#clustering h3 {font-size: 100%; font-weight: bold; margin: 0; padding: 0;}
              div#clustering table {margin-left: 2em; font-size: 100%;}
              div#clustering table a {white-space: nowrap;}
              div#clustering table td {padding-right: 1em;}
              div#clustering #cluster_status {color: #666666; margin-left: 2em;}
            </xsl:when>
            <xsl:when test="$res_cluster_position = 'right'">
              div#clustering {font-size: 84%; line-height: 140%; float: right; width: 15em; margin: 2em 0 0 1em; padding-left: 1em; border-left: 1px solid #cccccc;}
              div#clustering h3 {font-size: 100%; font-weight: bold; margin: 0 0 0.6em 0; padding: 0;}
              div#clustering ul {list-style: none; margin: 0; padding: 0;}
              div#clustering li {margin-left: 2em; text-indent: -2em;}
              div#clustering #cluster_status {color: #666666;}
            </xsl:when>
          </xsl:choose>
        </xsl:if>

      </xsl:comment>
    </style>
	
<xsl:if test="$render_dynamic_navigation = '1'">
<style type="text/css">
<xsl:comment>
  /**
   * CSS for dynamic navigation.
   */
  div#main_res {
    background: #FFF none repeat scroll 0 0;
    border-left: 1px solid #D3E1F9;
    margin-left: 209px;
    overflow: hidden;
    padding-left: 5px;
    padding-right: 4px;
  }
  div#main_res p {
    margin-top: 0;
  }
  div#dyn_nav {
    background: #FFF none repeat scroll 0 0;
    position: absolute;
    padding-top: 1px;
    top: 0;
    width: 205px;
  }
  div.dn-hdr {
    background-color: #3366FF;
    color: #FFF;
    font-size: 13px;
    height: 19px;
    line-height: 19px;
    margin: 0;
    padding: 0;
  }
  .dn-img {
    background: transparent url(/_layouts/images/remove.gif) no-repeat scroll 0 0;
    border: 0 none;
    height: 9px;
    position: relative;
    width: 11px;
  }
  a.dn-r-img {
    float: right;
    margin: 3px 4px 0 4px;
  }
  #dyn_nav ul, li {
    list-style-image: none;
    list-style-position: outside;
    list-style-type: none;
    vertical-align: middle;
  }
  #dyn_nav li {
    margin: 0 5px 4px 0;
    width: 100%;
  }
  ul.dn-attr {
    background: #FFF none repeat scroll 0 0;
    font-size: 12px;
    margin: 8px 0 4px 0;
    padding-left: 6px;
  }
  ul.dn-attr-hidden {
    background: #FFF none repeat scroll 0 0;
    border-top: 1px solid #DFDFFF;
    margin: 0;
    padding: 4px 0 0 0;
  }
  .label-input-label {
    color: GrayText;
  }
  li.dn-attr-hdr {
    background-color: #E5ECF9;
    font-weight: bold;
    line-height: 1.1;
    outline-style: none;
    padding-bottom: 2px;
    padding-top: 2px;
  }
  .dn-attr-hdr-txt {
    display: inline-block;
    overflow: hidden;
    width: 85%;
  }
  li.dn-attr-hdr div {
    width: 100%;
  }
  input.dn-zippy-input {
    border-style: none;
    font-size: 95%;
    margin-bottom: 2px;
    margin-left: 3px;
    margin-top: 1px;
    width: 97%;
  }
  div.dn-zippy-hdr {
    cursor: pointer;
    outline-style: none;
    margin-left: 2px;
  }
  li.dn-attr-hdr div.dn-zippy-hdr-img {
    background: url("/_layouts/images/ic_search.png") no-repeat scroll 0 0 transparent;
    float: right;
    height: 12px;
    margin-right: 4px;
    width: 10px;
  }
  ul.dn-attr a, a.dn-bar-link {
    color: #003399;
    text-decoration: none;
  }
  .dn-hidden {
    display: none;
  }
  .dn-inline-block, .dn-bar-rt, .dn-bar-rt table, .dn-img, span.dn-more-img {
    display: inline-block;
  }
  .dn-block {
    display: block;
  }
  .ac-renderer {
    background: white;
    border-bottom: 1px solid #558BE3;
    border-left: 1px solid #A2BFF0;
    border-right: 1px solid #558BE3;
    border-top: 1px solid #A2BFF0;
    min-width: 200px;
    max-width: 400px;
    overflow-x: hidden;
    position: absolute;
  }
  .ac-renderer div {
    cursor: pointer;
    font-size: <xsl:value-of select="$res_snippet_size"/>;
    margin: 3px;
    padding: 1px 2px;
    position: relative;
  }
  .ac-renderer div b {
    color: #3366FF;
  }
  .ac-renderer div.active {
    background-color: #D5E2FF;
    color: #000;
  }
  span.dn-attr-c {
    color: #777;
  }
  a.dn-attr-v {
    display: block;
    overflow-x: hidden;
    width: 99%;
  }
  a.dn-attr-v:visited, a.dn-bar-link:visited {
    color: #1111CC;
  }
  a.dn-attr-v:hover {
    text-decoration: underline;
  }
  a.dn-link, .dn-img {
    outline-style: none;
  }
  .dn-overflow {
    overflow-x: hidden;
  }
  .dn-bar-v {
    color: #000;
  }
  .dn-bar-rt {
    border: 0 none;
    float: right;
    margin: -2px 5px 0 20px;
  }
  .dn-bar-nav {
    font-size: <xsl:value-of select="$res_snippet_size"/>;
  }
  span.dn-more-img {
    height: 15px;
    margin-right: 1px;
    overflow: hidden;
    position: relative;
    vertical-align: text-bottom;
    width: 15px;
  }
  span.dn-limg {
    background: transparent url(/_layouts/images/less.gif) no-repeat scroll 0 0;
  }
  span.dn-mimg {
    background: transparent url(/_layouts/images/more.gif) no-repeat scroll 0 0;
  }
  div.dn-bar {
    background-color: #E5ECF9;
    clear: both;
    font-size: 12px;
    padding: 3px;
    width: 100%;
  }
  div.dn-bar dfn {
    font-size: 1.2em;
    padding: 4px;
  }
  div.dn-bar a.cancel-url:hover {
    text-decoration: line-through;
  }
  div.main-results {
    margin-left: 8px;
    margin-top: 8px;
  }
  div.oneboxResults table {
    width: 100%;
  }
</xsl:comment>
</style>
</xsl:if>

<xsl:if test="$show_sidebar = '1'">
<style type="text/css">
<xsl:comment>
  /** Common CSS for sidebar elements. */
  .sb-r {
    padding: 5px 0 0 5px;
    width: 45%;
  }
  .sb-r-alt {
    padding-top: 5px;
    width: 100%;
  }
  .sb-r-lbl,
  .sb-r-lbl-apps {
    color: #676767;
    font-size: small;
    font-weight: normal;
    margin: 0 0 10px 10px;
    text-align: left;
  }
  .sb-r-lbl-apps {
    margin: 0;
  }
  .sb-r-border {
    border-left: 1px solid #C9D7F1;
  }
  .sb-r-ld-msg-c {
    margin-bottom: 30px;
  }
  .sb-r-ld-msg {
    background-color: #3366CC;
    color: #FFF;
    font-size: 13px;
    padding: 2px;
  }
  .sb-r-res {
    font-size: 13px;
    margin-bottom: 10px;
    margin-left: 10px;
  }
</xsl:comment>
</style>
</xsl:if>
  </xsl:template>

  <!-- **********************************************************************
 URL variables (do not customize)
     ********************************************************************** -->
  <!-- *** if this is a test search (help variable)-->
  <xsl:variable name="is_test_search"
    select="/GSP/PARAM[@name='testSearch']/@value"/>

  <!-- *** if this is a search within results search *** -->
  <xsl:variable name="swrnum">
    <xsl:choose>
      <xsl:when test="/GSP/PARAM[(@name='swrnum') and (@value!='')]">
        <xsl:value-of select="/GSP/PARAM[@name='swrnum']/@value"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="0"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!-- *** help_url: search tip URL (html file) *** -->
  <xsl:variable name="help_url">/user_help.html</xsl:variable>

  <!-- *** alerts_url: Alerts URL (html file) *** -->
  <xsl:variable name="alerts_url">/alerts</xsl:variable>

  <!-- *** base_url: collection info *** -->
  <xsl:variable name="base_url">
    <xsl:for-each
      select="/GSP/PARAM[@name = 'client' or

                     @name = 'site' or
                     @name = 'num' or
                     @name = 'output' or
                     @name = 'proxystylesheet' or
                     @name = 'access' or
                     @name = 'lr' or
                     @name = 'sitesearch' or
                     @name = 'ie']">
      <xsl:value-of select="@name"/>=<xsl:value-of select="@original_value"/>
      <xsl:if test="position() != last()">&amp;</xsl:if>
    </xsl:for-each>
  </xsl:variable>


  <!-- added code start-->

  <!-- *** alerts_url: Alerts URL (html file) *** -->
  <xsl:variable name="suggestion_sitesearchParam">
    <xsl:value-of select="/GSP/PARAM[@name = 'sitesearch']/@original_value"/>

  </xsl:variable>




  <!-- added code end-->

  <!-- *** home_url: GSASearchresults.aspx? + collection info + &proxycustom=<HOME/> *** -->
  <xsl:variable name="home_url">
    GSASearchresults.aspx?<xsl:value-of select="$base_url"
  />&amp;proxycustom=&lt;HOME/&gt;
  </xsl:variable>


  <!-- *** synonym_url: does not include q, as_q, and start elements *** -->
  <xsl:variable name="synonym_url">
    <xsl:for-each
  select="/GSP/PARAM[(@name != 'q') and
                     (@name != 'as_q') and
                     (@name != 'swrnum') and
                     (@name != 'dnavs') and
                     (@name != 'ie') and
                     (@name != 'start') and
                     (@name != 'epoch' or $is_test_search != '') and
                     not(starts-with(@name, 'metabased_'))]">
      <xsl:value-of select="@name"/>
      <xsl:text>=</xsl:text>
      <xsl:value-of select="@original_value"/>
      <xsl:if test="position() != last()">
        <xsl:text disable-output-escaping="yes">&amp;</xsl:text>
      </xsl:if>
    </xsl:for-each>
  </xsl:variable>

  <!-- *** search_url *** -->
  <xsl:variable name="search_url">
    <xsl:for-each select="/GSP/PARAM[(@name != 'start') and
                                   (@name != 'swrnum') and
                     (@name != 'epoch' or $is_test_search != '') and
                     not(starts-with(@name, 'metabased_'))]">
      <xsl:value-of select="@name"/>
      <xsl:text>=</xsl:text>
      <xsl:value-of select="@original_value"/>
      <xsl:if test="position() != last()">
        <xsl:text disable-output-escaping="yes">&amp;</xsl:text>
      </xsl:if>
    </xsl:for-each>
  </xsl:variable>
  
  <!-- *** search_url minus any dynamic navigation filters *** -->
<xsl:variable name="search_url_no_dnavs">
  <xsl:for-each select="/GSP/PARAM[(@name != 'start') and
                                   (@name != 'swrnum') and
                                   (@name != 'dnavs') and
                     (@name != 'epoch' or $is_test_search != '') and
                     not(starts-with(@name, 'metabased_'))]">
    <xsl:choose>
        <xsl:when test="@name = 'q' and /GSP/PARAM[@name='dnavs']">
        <xsl:value-of select="@name"/><xsl:text>=</xsl:text>
        <xsl:value-of select="substring-before(@original_value,
          concat('+', /GSP/PARAM[@name='dnavs']/@original_value))"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@name"/><xsl:text>=</xsl:text>
        <xsl:value-of select="@original_value"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="position() != last()">
      <xsl:text disable-output-escaping="yes">&amp;</xsl:text>
    </xsl:if>
  </xsl:for-each>
</xsl:variable>

<!-- *** url without q and dnavs param *** -->
<xsl:variable name="no_q_dnavs_params">
  <xsl:for-each select="/GSP/PARAM[(@name != 'start') and
                                   (@name != 'swrnum') and
                                   (@name != 'q') and
                                   (@name != 'dnavs') and
                     (@name != 'epoch' or $is_test_search != '') and
                     not(starts-with(@name, 'metabased_'))]">
    <xsl:value-of select="@name"/><xsl:text>=</xsl:text>
    <xsl:value-of select="@original_value"/>
    <xsl:if test="position() != last()">
      <xsl:text disable-output-escaping="yes">&amp;</xsl:text>
    </xsl:if>
  </xsl:for-each>
</xsl:variable>

 <!-- *** search_url_escaped: safe for inclusion in javascript *** -->
<xsl:variable name="search_url_escaped">
  <xsl:call-template name="replace_string">
    <xsl:with-param name="find" select='"&apos;"'/>
    <xsl:with-param name="replace" select='"%27"'/>
    <xsl:with-param name="string" select="$search_url_no_dnavs"/>
  </xsl:call-template>
</xsl:variable>

  <!-- *** filter_url: everything except resetting "filter=" *** -->
  <!-- The filter URL logic is modified so as to work when dynamic navigation feature is enabled for search box. The url starting with "GSASearchresults.aspx" represents the search box URL and is added elsehere. -->
  <xsl:variable name="filter_url"><xsl:for-each
  select="/GSP/PARAM[(@name != 'filter') and
                     (@name != 'epoch' or $is_test_search != '') and
                     not(starts-with(@name, 'metabased_'))]">
      <xsl:value-of select="@name"/>
      <xsl:text>=</xsl:text>
      <xsl:value-of select="@original_value"/>
      <xsl:if test="position() != last()">
        <xsl:text disable-output-escaping="yes">&amp;</xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text disable-output-escaping='yes'>&amp;filter=</xsl:text>
  </xsl:variable>

  <!-- *** adv_search_url: GSASearchresults.aspx? + $search_url + as_q=$q *** -->
  <xsl:variable name="adv_search_url">
    GSASearchresults.aspx?<xsl:value-of
  select="$search_url_no_dnavs"/>&amp;proxycustom=&lt;ADVANCED/&gt;
  </xsl:variable>

  <!-- *** db_url_protocol: googledb:// *** -->
  <xsl:variable name="db_url_protocol">googledb://</xsl:variable>

  <!-- *** googleconnector_protocol: googleconnector:// *** -->
  <xsl:variable name="googleconnector_protocol">googleconnector://</xsl:variable>

  <!-- *** nfs_url_protocol: nfs:// *** -->
  <xsl:variable name="nfs_url_protocol">nfs://</xsl:variable>

  <!-- *** smb_url_protocol: smb:// *** -->
  <xsl:variable name="smb_url_protocol">smb://</xsl:variable>

  <!-- *** unc_url_protocol: unc:// *** -->
  <xsl:variable name="unc_url_protocol">unc://</xsl:variable>

  <!-- *** swr_search_url: GSASearchresults.aspx? + $search_url + as_q=$q *** -->
  <xsl:variable name="swr_search_url">
    GSASearchresults.aspx?<xsl:value-of
  select="$search_url_no_dnavs"/>&amp;swrnum=<xsl:value-of select="/GSP/RES/M"/>
  </xsl:variable>

  <!-- *** analytics_script_url: http://www.google-analytics.com/urchin.js *** -->
  <xsl:variable
   name="analytics_script_url">http://www.google-analytics.com/urchin.js</xsl:variable>

  <!-- **********************************************************************
 Search Parameters (do not customize)
     ********************************************************************** -->

  <!-- *** num_results: actual num_results per page *** -->
  <xsl:variable name="num_results">
    <xsl:choose>
      <xsl:when test="/GSP/PARAM[(@name='num') and (@value!='')]">
        <xsl:value-of select="/GSP/PARAM[@name='num']/@value"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="10"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!-- *** form_params: parameters carried by the search input form *** -->
  <xsl:template name="form_params">
    <xsl:for-each
      select="PARAM[@name != 'q' and
                  @name != 'ie' and
                  not(contains(@name, 'as_')) and
                  @name != 'btnG' and
                  @name != 'btnI' and
                  @name != 'site' and
                  @name != 'filter' and
                  @name != 'swrnum' and
                  @name != 'start' and
                  @name != 'access' and
                  @name != 'sitesearch' and
                  @name != 'ip' and
				  @name != 'dnavs' and
                  (@name != 'epoch' or $is_test_search != '') and
                  not(starts-with(@name ,'metabased_'))]">
      <input type="hidden" name="{@name}" value="{@value}" />

      <xsl:if test="@name = 'oe'">
        <input type="hidden" name="ie" value="{@value}" />
      </xsl:if>
      <xsl:text>
    </xsl:text>
    </xsl:for-each>
    <xsl:if test="$search_collections_xslt = '' and PARAM[@name='site']">
      <input type="hidden" name="site" value="{PARAM[@name='site']/@value}"/>
    </xsl:if>
  </xsl:template>
  
  <!-- *** original query without any dynamic navigation filters *** -->
<xsl:variable name="qval">
  <xsl:choose>
    <xsl:when test="/GSP/PARAM[@name='dnavs']">
      <xsl:value-of select="concat(substring-before(/GSP/Q,
        /GSP/PARAM[@name='dnavs']/@value), ' ', substring-after(/GSP/Q,
        /GSP/PARAM[@name='dnavs']/@value))"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="/GSP/Q"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:variable>

<xsl:variable name="original_q">
  <xsl:choose>
    <xsl:when test="count(/GSP/PARAM[@name='dnavs']) > 0">
      <xsl:value-of
        select="substring-before(/GSP/PARAM[@name='q']/@original_value,
        concat('+', /GSP/PARAM[@name='dnavs']/@original_value))"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="/GSP/PARAM[@name='q']/@original_value"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:variable>

  <!-- *** space_normalized_query: q = /GSP/Q *** -->

  <xsl:variable name="space_normalized_query">
    <xsl:value-of select="normalize-space($qval)"
      disable-output-escaping="yes"/>
  </xsl:variable>

  <!-- *** stripped_search_query: q, as_q, ... for cache highlight *** -->
  <xsl:variable name="stripped_search_query">
    <xsl:for-each
  select="/GSP/PARAM[(@name = 'q') or
                     (@name = 'as_q') or
                     (@name = 'as_oq') or
                     (@name = 'as_epq')]">
      <xsl:value-of select="@original_value"
  />
      <xsl:if test="position() != last()"
    >
        <xsl:text disable-output-escaping="yes">+</xsl:text
     >
      </xsl:if>
    </xsl:for-each>
  </xsl:variable>

  <xsl:variable name="access">
    <xsl:choose>
      <xsl:when test="/GSP/PARAM[(@name='access') and ((@value='s') or (@value='a'))]">
        <xsl:value-of select="/GSP/PARAM[@name='access']/@original_value"/>
      </xsl:when>
      <xsl:otherwise>p</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="sitesearch">
    <xsl:choose>
      <xsl:when test="/GSP/PARAM[(@name='sitesearch')]">
        <xsl:value-of select="/GSP/PARAM[@name='sitesearch']/@original_value"/>
      </xsl:when>
          </xsl:choose>
  </xsl:variable>

  <!-- **********************************************************************
 Figure out what kind of page this is (do not customize)
     ********************************************************************** -->
  <xsl:template match="GSP">
    <xsl:choose>
      <xsl:when test="Q">
        <xsl:choose>
          <xsl:when test="$swrnum != 0">
            <xsl:call-template name="swr_search"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="search_results"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="CACHE">
        <xsl:call-template name="cached_page"/>
      </xsl:when>
      <xsl:when test="CUSTOM/HOME">
        <xsl:call-template name="front_door"/>
      </xsl:when>
      <xsl:when test="CUSTOM/ADVANCED">
        <xsl:call-template name="advanced_search"/>
      </xsl:when>
      <xsl:when test="ERROR">
        <xsl:call-template name="error_page">
          <xsl:with-param name="errorMessage" select="$server_error_msg_text"/>
          <xsl:with-param name="errorDescription" select="$server_error_des_text"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="error_page">
          <xsl:with-param name="errorMessage" select="$xml_error_msg_text"/>
          <xsl:with-param name="errorDescription" select="$xml_error_des_text"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- **********************************************************************
 Cached page (do not customize)
     ********************************************************************** -->
  <xsl:template name="cached_page">
    <xsl:variable name="cached_page_url" select="CACHE/CACHE_URL"/>
    <xsl:variable name="cached_page_html" select="CACHE/CACHE_HTML"/>

    <!-- *** decide whether to load html page or pdf file *** -->
    <xsl:if test="'.pdf' != substring($cached_page_url,
              1 + string-length($cached_page_url) - string-length('.pdf')) and
              not(starts-with($cached_page_url, $db_url_protocol)) and
              not(starts-with($cached_page_url, $nfs_url_protocol)) and
              not(starts-with($cached_page_url, $smb_url_protocol)) and
              not(starts-with($cached_page_url, $unc_url_protocol))">
      <base href="{$cached_page_url}"/>
    </xsl:if>












    <!-- *** display cache page header *** -->
    <xsl:call-template name="cached_page_header">
      <xsl:with-param name="cached_page_url" select="$cached_page_url"/>
    </xsl:call-template>

    <!-- *** display cached contents *** -->
    <xsl:value-of select="$cached_page_html" disable-output-escaping="yes"/>
  </xsl:template>

  <xsl:template name="escape_quot">
    <xsl:param name="string"/>
    <xsl:call-template name="replace_string">
      <xsl:with-param name="find" select="'&quot;'"/>
      <xsl:with-param name="replace" select="'&amp;quot;'"/>
      <xsl:with-param name="string" select="$string"/>
    </xsl:call-template>
  </xsl:template>

  <!-- **********************************************************************
 Advanced search page (do not customize)
     ********************************************************************** -->
  <xsl:template name="advanced_search">

    <xsl:variable name="html_escaped_as_q">
      <xsl:call-template name="escape_quot">
        <xsl:with-param name="string" select="/GSP/PARAM[@name='q']/@value"/>
      </xsl:call-template>
      <xsl:value-of select="' '"/>
      <xsl:call-template name="escape_quot">
        <xsl:with-param name="string" select="/GSP/PARAM[@name='as_q']/@value"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="html_escaped_as_epq">
      <xsl:call-template name="escape_quot">
        <xsl:with-param name="string" select="/GSP/PARAM[@name='as_epq']/@value"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="html_escaped_as_oq">
      <xsl:call-template name="escape_quot">
        <xsl:with-param name="string" select="/GSP/PARAM[@name='as_oq']/@value"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="html_escaped_as_eq">
      <xsl:call-template name="escape_quot">
        <xsl:with-param name="string" select="/GSP/PARAM[@name='as_eq']/@value"/>
      </xsl:call-template>
    </xsl:variable>

    <html>
      <xsl:call-template name="langHeadStart"/>
      <title>
        <xsl:value-of select="$adv_page_title"/>
      </title>
      <xsl:call-template name="style"/>

      <!-- script type="text/javascript" -->
      <script>
        <xsl:comment>
          function setFocus() {
          document.f.as_q.focus(); }
          function esc(x){
          x = escape(x).replace(/\+/g, "%2b");
          if (x.substring(0,2)=="\%u") x="";
          return x;
          }
          function collecturl(target, custom) {
          var p = new Array();var i = 0;var url="";var z = document.f;
          if (z.as_q.value.length) {p[i++] = 'as_q=' + esc(z.as_q.value);}
          if (z.as_epq.value.length) {p[i++] = 'as_epq=' + esc(z.as_epq.value);}
          if (z.as_oq.value.length) {p[i++] = 'as_oq=' + esc(z.as_oq.value);}
          if (z.as_eq.value.length) {p[i++] = 'as_eq=' + esc(z.as_eq.value);}
          if (z.as_sitesearch.value.length)
          {p[i++]='as_sitesearch='+esc(z.as_sitesearch.value);}
          if (z.as_lq.value.length) {p[i++] = 'as_lq=' + esc(z.as_lq.value);}
          if (z.as_occt.options[z.as_occt.selectedIndex].value.length)
          {p[i++]='as_occt='+esc(z.as_occt.options[z.as_occt.selectedIndex].value);}
          if (z.as_dt.options[z.as_dt.selectedIndex].value.length)
          {p[i++]='as_dt='+esc(z.as_dt.options[z.as_dt.selectedIndex].value);}
          if (z.lr.options[z.lr.selectedIndex].value != '') {p[i++] = 'lr=' +
          z.lr.options[z.lr.selectedIndex].value;}
          if (z.num.options[z.num.selectedIndex].value != '10')
          {p[i++] = 'num=' + z.num.options[z.num.selectedIndex].value;}
          if (z.sort.options[z.sort.selectedIndex].value != '')
          {p[i++] = 'sort=' + z.sort.options[z.sort.selectedIndex].value;}
          if (typeof(z.client) != 'undefined')
          {p[i++] = 'client=' + esc(z.client.value);}
          if (typeof(z.site) != 'undefined')
          {p[i++] = 'site=' + esc(z.site.value);}
          if (typeof(z.output) != 'undefined')
          {p[i++] = 'output=' + esc(z.output.value);}
          if (typeof(z.proxystylesheet) != 'undefined')
          {p[i++] = 'proxystylesheet=' + esc(z.proxystylesheet.value);}
          if (typeof(z.ie) != 'undefined')
          {p[i++] = 'ie=' + esc(z.ie.value);}
          if (typeof(z.oe) != 'undefined')
          {p[i++] = 'oe=' + esc(z.oe.value);}

          if (typeof(z.access) != 'undefined')
          {p[i++] = 'access=' + esc(z.access.value);}
          if (typeof(z.sitesearch) != 'undefined')
          {p[i++] = 'sitesearch=' + esc(z.sitesearch.value);}
          if (custom != '')
          {p[i++] = 'proxycustom=' + '&lt;ADVANCED/&gt;';}
          if (p.length &gt; 0) {
          url = p[0];
          for (var j = 1; j &lt; p.length; j++) { url += "&amp;" + p[j]; }}
          location.href = target + '?' + url;
          }
          //
        </xsl:comment>
      </script>

      <xsl:call-template name="langHeadEnd"/>

      <body onload="setFocus()" dir="ltr">
        <xsl:call-template name="analytics"/>

        <!-- *** Customer's own advanced search page header *** -->
        <xsl:if test="$choose_adv_search_page_header = 'mine' or
                    $choose_adv_search_page_header = 'both'">
          <xsl:call-template name="my_page_header"/>
        </xsl:if>

        <!--====Advanced Search Header======-->
        <xsl:if test="$choose_adv_search_page_header = 'provided' or
                    $choose_adv_search_page_header = 'both'">
          <xsl:call-template name="advanced_search_header"/>
        </xsl:if>

        <xsl:call-template name="top_sep_bar">
          <xsl:with-param name="text" select="$sep_bar_adv_text"/>
          <xsl:with-param name="show_info" select="0"/>
          <xsl:with-param name="time" select="0"/>
        </xsl:call-template>

        <!--====Carry over Search Parameters======-->
        <form method="get" action="search" name="f">
          <xsl:if test="PARAM[@name='client']">
            <input type="hidden" name="client"
              value="{PARAM[@name='client']/@value}" />
          </xsl:if>

          <xsl:if test="PARAM[@name='sitesearch']">
            <input type="hidden" name="sitesearch"
              value="{PARAM[@name='sitesearch']/@value}" />
          </xsl:if>

		  <xsl:if test="PARAM[@name='dnavs']">
            <input type="hidden" name="dnavs"
              value="{PARAM[@name='dnavs']/@value}" />
          </xsl:if>

          <!--==== site is carried over in the drop down if the menu is used =====-->
          <xsl:if test="$search_collections_xslt = '' and PARAM[@name='site']">
            <input type="hidden" name="site" value="{PARAM[@name='site']/@value}"/>
          </xsl:if>
          <xsl:if test="PARAM[@name='output']">
            <input type="hidden" name="output"
              value="{PARAM[@name='output']/@value}" />
          </xsl:if>
          <xsl:if test="PARAM[@name='proxystylesheet']">
            <input type="hidden" name="proxystylesheet"
              value="{PARAM[@name='proxystylesheet']/@value}" />
          </xsl:if>
          <xsl:if test="PARAM[@name='ie']">
            <input type="hidden" name="ie"
              value="{PARAM[@name='ie']/@value}" />
          </xsl:if>
          <xsl:if test="PARAM[@name='oe']">
            <input type="hidden" name="oe"
              value="{PARAM[@name='oe']/@value}" />
          </xsl:if>
          <xsl:if test="PARAM[@name='hl']">
            <input type="hidden" name="hl"
              value="{PARAM[@name='hl']/@value}" />
          </xsl:if>
          <xsl:if test="PARAM[@name='getfields']">
            <input type="hidden" name="getfields"
              value="{PARAM[@name='getfields']/@value}" />
          </xsl:if>
          <xsl:if test="PARAM[@name='requiredfields']">
            <input type="hidden" name="requiredfields"
              value="{PARAM[@name='requiredfields']/@value}" />
          </xsl:if>
          <xsl:if test="PARAM[@name='partialfields']">
            <input type="hidden" name="partialfields"
              value="{PARAM[@name='partialfields']/@value}" />
          </xsl:if>

          <!--====Advanced Search Options======-->

          <table cellspacing="0" cellpadding="3" border="0" width="100%">
            <tr bgcolor="{$adv_search_panel_bgcolor}">
              <td>
                <table width="100%" cellspacing="0" cellpadding="0" border="0">
                  <tr bgcolor="{$adv_search_panel_bgcolor}">
                    <td>
                      <table width="100%" cellspacing="0" cellpadding="2"
                      border="0">
                        <tr>
                          <td valign="top" width="15%">
                            <font size="-1">
                              <br />
                              <b>Find results</b>
                            </font>
                          </td>

                          <td width="85%">
                            <table width="100%" cellpadding="2"
                            border="0" cellspacing="0">
                              <tr>
                                <td>
                                  <font size="-1">
                                    with <b>all</b> of the words
                                  </font>
                                </td>

                                <td>
                                  <xsl:text disable-output-escaping="yes">
                             &lt;input type=&quot;text&quot;
                             name=&quot;as_q&quot;
                             size=&quot;25&quot; value=&quot;</xsl:text>
                                  <xsl:value-of disable-output-escaping="yes"
                                   select="$html_escaped_as_q"/>
                                  <xsl:text disable-output-escaping="yes">&quot;&gt;</xsl:text>



                                  <script type="text/javascript">
                                    <xsl:comment>
                                      document.f.as_q.focus();
                                      //
                                    </xsl:comment>
                                  </script>
                                </td>

                                <td valign="top" rowspan="4">
                                  <font size="-1">
                                    <select name="num">
                                      <xsl:choose>
                                        <xsl:when test="PARAM[(@name='num') and (@value!='10')]">
                                          <option value="10">10 results</option>
                                        </xsl:when>
                                        <xsl:otherwise>
                                          <option value="10" selected="selected">10 results</option>
                                        </xsl:otherwise>
                                      </xsl:choose>
                                      <xsl:choose>
                                        <xsl:when test="PARAM[(@name='num') and (@value='20')]">
                                          <option value="20" selected="selected">20 results</option>
                                        </xsl:when>
                                        <xsl:otherwise>
                                          <option value="20">20 results</option>
                                        </xsl:otherwise>
                                      </xsl:choose>
                                      <xsl:choose>
                                        <xsl:when test="PARAM[(@name='num') and (@value='30')]">
                                          <option value="30" selected="selected">30 results</option>
                                        </xsl:when>
                                        <xsl:otherwise>
                                          <option value="30">30 results</option>
                                        </xsl:otherwise>
                                      </xsl:choose>
                                      <xsl:choose>
                                        <xsl:when test="PARAM[(@name='num') and (@value='50')]">
                                          <option value="50" selected="selected">50 results</option>
                                        </xsl:when>
                                        <xsl:otherwise>
                                          <option value="50">50 results</option>
                                        </xsl:otherwise>
                                      </xsl:choose>
                                      <xsl:choose>
                                        <xsl:when test="PARAM[(@name='num') and (@value='100')]">
                                          <option value="100" selected="selected">100 results</option>
                                        </xsl:when>
                                        <xsl:otherwise>
                                          <option value="100">100 results</option>
                                        </xsl:otherwise>
                                      </xsl:choose>
                                    </select>
                                  </font>
                                </td>
                                <xsl:call-template name="collection_menu"/>
                                <td>
                                  <font size="-1">
                                    <input type="submit" name="btnG"
                                      value="{$search_button_text}" />
                                  </font>
                                </td>
                              </tr>

                              <tr>
                                <td nowrap="nowrap">
                                  <font size="-1">
                                    with the <b>exact phrase</b>
                                  </font>
                                </td>

                                <td>
                                  <xsl:text disable-output-escaping="yes">

                             &lt;input type=&quot;text&quot;
                             name=&quot;as_epq&quot;
                             size=&quot;25&quot; value=&quot;</xsl:text>
                                  <xsl:value-of disable-output-escaping="yes"
                                   select="$html_escaped_as_epq"/>
                                  <xsl:text disable-output-escaping="yes">&quot;&gt;</xsl:text>
                                </td>
                              </tr>

                              <tr>

                                <td nowrap="nowrap">
                                  <font size="-1">
                                    with <b>at least one</b> of the words
                                  </font>
                                </td>

                                <td>
                                  <xsl:text disable-output-escaping="yes">

                             &lt;input type=&quot;text&quot;
                             name=&quot;as_oq&quot;
                             size=&quot;25&quot; value=&quot;</xsl:text>
                                  <xsl:value-of disable-output-escaping="yes"
                                   select="$html_escaped_as_oq"/>
                                  <xsl:text disable-output-escaping="yes">&quot;&gt;</xsl:text>
                                </td>
                              </tr>

                              <tr>
                                <td nowrap="nowrap">
                                  <font size="-1">
                                    <b>without</b> the words
                                  </font>
                                </td>

                                <td>
                                  <xsl:text disable-output-escaping="yes">

                             &lt;input type=&quot;text&quot;
                             name=&quot;as_eq&quot;
                             size=&quot;25&quot; value=&quot;</xsl:text>
                                  <xsl:value-of disable-output-escaping="yes"
                                   select="$html_escaped_as_eq"/>
                                  <xsl:text disable-output-escaping="yes">&quot;&gt;</xsl:text>
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>

                  <tr bgcolor="{$global_bg_color}">
                    <td>
                      <table width="100%" cellspacing="0"
                      cellpadding="2" border="0">
                        <tr>
                          <td width="15%">
                            <font size="-1">
                              <b>Language</b>
                            </font>
                          </td>

                          <td width="40%">
                            <font size="-1">Return pages written in</font>
                          </td>

                          <td>
                            <font size="-1">



                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='oe') and (@value!='')]">
                                  <xsl:text disable-output-escaping="yes">&lt;select name=&quot;lr&quot;&gt;</xsl:text>
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:text disable-output-escaping="yes">&lt;select name=&quot;lr&quot; onchange=&quot;javascript:collecturl('search', 'adv');&quot;&gt;</xsl:text>
                                </xsl:otherwise>
                              </xsl:choose>

                              <option value="">any language</option>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_ar')]">
                                  <option value="lang_ar"
                                    selected="selected">Arabic</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_ar">Arabic</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_zh-CN')]">
                                  <option value="lang_zh-CN"
                                    selected="selected">Chinese (Simplified)</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_zh-CN">Chinese (Simplified)</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_zh-TW')]">
                                  <option value="lang_zh-TW"
                                    selected="selected">Chinese (Traditional)</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_zh-TW">Chinese (Traditional)</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_cs')]">
                                  <option value="lang_cs" selected="selected">Czech</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_cs">Czech</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_da')]">
                                  <option value="lang_da" selected="selected">Danish</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_da">Danish</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_nl')]">
                                  <option value="lang_nl" selected="selected">Dutch</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_nl">Dutch</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_en')]">
                                  <option value="lang_en" selected="selected">English</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_en">English</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_et')]">
                                  <option value="lang_et" selected="selected">Estonian</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_et">Estonian</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_fi')]">
                                  <option value="lang_fi" selected="selected">Finnish</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_fi">Finnish</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_fr')]">
                                  <option value="lang_fr" selected="selected">French</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_fr">French</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_de')]">
                                  <option value="lang_de" selected="selected">German</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_de">German</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_el')]">
                                  <option value="lang_el" selected="selected">Greek</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_el">Greek</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_iw')]">

                                  <option value="lang_iw" selected="selected">Hebrew</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_iw">Hebrew</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_hu')]">
                                  <option value="lang_hu" selected="selected">Hungarian</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_hu">Hungarian</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_is')]">
                                  <option value="lang_is" selected="selected">Icelandic</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_is">Icelandic</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_it')]">
                                  <option value="lang_it" selected="selected">Italian</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_it">Italian</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_ja')]">
                                  <option value="lang_ja" selected="selected">Japanese</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_ja">Japanese</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_ko')]">
                                  <option value="lang_ko" selected="selected">Korean</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_ko">Korean</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_lv')]">
                                  <option value="lang_lv" selected="selected">Latvian</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_lv">Latvian</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_lt')]">
                                  <option value="lang_lt" selected="selected">Lithuanian</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_lt">Lithuanian</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_no')]">
                                  <option value="lang_no" selected="selected">Norwegian</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_no">Norwegian</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_pl')]">
                                  <option value="lang_pl" selected="selected">Polish</option>

                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_pl">Polish</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_pt')]">
                                  <option value="lang_pt" selected="selected">Portuguese</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_pt">Portuguese</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_ro')]">
                                  <option value="lang_ro" selected="selected">Romanian</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_ro">Romanian</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_ru')]">
                                  <option value="lang_ru" selected="selected">Russian</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_ru">Russian</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_es')]">
                                  <option value="lang_es" selected="selected">Spanish</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_es">Spanish</option>
                                </xsl:otherwise>
                              </xsl:choose>

                              <xsl:choose>
                                <xsl:when test="PARAM[(@name='lr') and (@value='lang_sv')]">
                                  <option value="lang_sv" selected="selected">Swedish</option>
                                </xsl:when>
                                <xsl:otherwise>
                                  <option value="lang_sv">Swedish</option>
                                </xsl:otherwise>
                              </xsl:choose>
                              <xsl:text disable-output-escaping="yes">&lt;/select&gt;</xsl:text>
                            </font>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>

                  <tr bgcolor="{$global_bg_color}">
                    <td>
                      <table width="100%" cellpadding="2"
                      cellspacing="0" border="0">
                        <tr>
                          <td width="15%">
                            <font size="-1">
                              <b>File Format</b>
                            </font>
                          </td>

                          <td width="40%" nowrap="nowrap">
                            <font size="-1">
                              <select name="as_ft">
                                <xsl:choose>
                                  <xsl:when test="PARAM[(@name='as_ft') and (@value='i')]">
                                    <option value="i" selected="selected">Only</option>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <option value="i">Only</option>
                                  </xsl:otherwise>
                                </xsl:choose>
                                <xsl:choose>
                                  <xsl:when test="PARAM[(@name='as_ft') and (@value='e')]">
                                    <option value="e" selected="selected">Don't</option>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <option value="e">Don't</option>
                                  </xsl:otherwise>
                                </xsl:choose>
                              </select>return results of the file format
                            </font>
                          </td>

                          <td>
                            <font size="-1">
                              <select name="as_filetype">

                                <xsl:choose>
                                  <xsl:when test="PARAM[(@name='as_filetype') and (@value!='')]">
                                    <option value="">any format</option>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <option value="" selected="selected">any format</option>
                                  </xsl:otherwise>
                                </xsl:choose>
                                <xsl:choose>
                                  <xsl:when test="PARAM[(@name='as_filetype') and (@value='pdf')]">
                                    <option value="pdf" selected="selected">Adobe Acrobat PDF (.pdf)</option>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <option value="pdf">Adobe Acrobat PDF (.pdf)</option>
                                  </xsl:otherwise>
                                </xsl:choose>
                                <xsl:choose>
                                  <xsl:when test="PARAM[(@name='as_filetype') and (@value='ps')]">
                                    <option value="ps" selected="selected">Adobe Postscript (.ps)</option>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <option value="ps">Adobe Postscript (.ps)</option>
                                  </xsl:otherwise>
                                </xsl:choose>
                                <xsl:choose>
                                  <xsl:when test="PARAM[(@name='as_filetype') and (@value='doc')]">
                                    <option value="doc" selected="selected">Microsoft Word (.doc)</option>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <option value="doc">Microsoft Word (.doc)</option>
                                  </xsl:otherwise>
                                </xsl:choose>
                                <xsl:choose>
                                  <xsl:when test="PARAM[(@name='as_filetype') and (@value='xls')]">
                                    <option value="xls" selected="selected">Microsoft Excel (.xls)</option>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <option value="xls">Microsoft Excel (.xls)</option>
                                  </xsl:otherwise>
                                </xsl:choose>
                                <xsl:choose>
                                  <xsl:when test="PARAM[(@name='as_filetype') and (@value='ppt')]">
                                    <option value="ppt" selected="selected">Microsoft Powerpoint (.ppt)</option>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <option value="ppt">Microsoft Powerpoint (.ppt)</option>
                                  </xsl:otherwise>
                                </xsl:choose>
                                <xsl:choose>
                                  <xsl:when test="PARAM[(@name='as_filetype') and (@value='rtf')]">
                                    <option value="rtf" selected="selected">Rich Text Format (.rtf)</option>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <option value="rtf">Rich Text Format (.rtf)</option>
                                  </xsl:otherwise>
                                </xsl:choose>
                              </select>
                            </font>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>

                  <tr bgcolor="{$global_bg_color}">
                    <td>
                      <table width="100%" cellspacing="0"
                      cellpadding="2" border="0">
                        <tr>
                          <td width="15%">
                            <font size="-1">
                              <b>Occurrences</b>
                            </font>
                          </td>

                          <td nowrap="nowrap" width="40%">
                            <font size="-1">Return results where my terms occur</font>
                          </td>

                          <td>
                            <font size="-1">
                              <select
          name="as_occt">
                                <xsl:choose>
                                  <xsl:when test="PARAM[(@name='as_occt') and (@value!='any')]">
                                    <option value="any"> anywhere in the page </option>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <option value="any" selected="selected">
                                      anywhere in the page
                                    </option>
                                  </xsl:otherwise>
                                </xsl:choose>
                                <xsl:choose>
                                  <xsl:when test="PARAM[(@name='as_occt') and (@value='title')]">
                                    <option value="title" selected="selected">in the title of the page</option>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <option value="title">in the title of the page</option>
                                  </xsl:otherwise>
                                </xsl:choose>
                                <xsl:choose>
                                  <xsl:when test="PARAM[(@name='as_occt') and (@value='url')]">
                                    <option value="url" selected="selected">in the URL of the page</option>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <option value="url">in the URL of the page</option>
                                  </xsl:otherwise>
                                </xsl:choose>
                              </select>
                            </font>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>

                  <tr bgcolor="{$global_bg_color}">
                    <td>
                      <table width="100%" cellpadding="2"
                      cellspacing="0" border="0">
                        <tr>
                          <td width="15%">
                            <font size="-1">
                              <b>Domain</b>
                            </font>
                          </td>

                          <td width="40%" nowrap="nowrap">
                            <font size="-1">
                              <select
                      name="as_dt">
                                <xsl:choose>
                                  <xsl:when test="PARAM[(@name='as_dt') and (@value='i')]">
                                    <option value="i" selected="selected">Only</option>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <option value="i">Only</option>
                                  </xsl:otherwise>
                                </xsl:choose>
                                <xsl:choose>
                                  <xsl:when test="PARAM[(@name='as_dt') and (@value='e')]">
                                    <option value="e" selected="selected">Don't</option>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <option value="e">Don't</option>
                                  </xsl:otherwise>
                                </xsl:choose>
                              </select>return results from the site or domain
                            </font>
                          </td>

                          <td>
                            <table cellpadding="0" cellspacing="0"
                            border="0">
                              <tr>
                                <td>
                                  <xsl:choose>
                                    <xsl:when test="PARAM[@name='as_sitesearch']">
                                      <input type="text" size="25"
                                      value="{PARAM[@name='as_sitesearch']/@value}"
                                      name="as_sitesearch" />
                                    </xsl:when>
                                    <xsl:otherwise>
                                      <input type="text" size="25" value="" name="as_sitesearch" />
                                    </xsl:otherwise>
                                  </xsl:choose>
                                </td>
                              </tr>

                              <tr>
                                <td valign="top" nowrap="nowrap">
                                  <font size="-1">
                                    <i>e.g. google.com, .org</i>
                                  </font>
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>

                        <!-- Sort by Date feature -->
                        <tr>
                          <td width="15%">
                            <font size="-1">
                              <b>Sort</b>
                            </font>
                          </td>

                          <td colspan="2" nowrap="nowrap">
                            <font size="-1">
                              <select
                      name="sort">
                                <xsl:choose>
                                  <xsl:when test="PARAM[(@name='sort') and (@value='')]">
                                    <option value="" selected="selected">Sort by relevance</option>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <option value="">Sort by relevance</option>
                                  </xsl:otherwise>
                                </xsl:choose>
                                <xsl:choose>
                                  <xsl:when test="PARAM[(@name='sort') and (@value='date:D:S:d1')]">
                                    <option value="date:D:S:d1" selected="selected">Sort by date</option>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <option value="date:D:S:d1">Sort by date</option>
                                  </xsl:otherwise>
                                </xsl:choose>
                              </select>
                            </font>
                          </td>
                        </tr>
                        <!-- Secure Search feature -->
                        <xsl:if test="$show_secure_radio != '0'">

                          <tr>
                            <td width="15%">
                              <font size="-1">
                                <b>Security</b>
                              </font>
                            </td>

                            <td colspan="2" nowrap="nowrap">
                              <font size="-1">
                                <xsl:choose>
                                  <xsl:when test="$access='p'">
                                    <label>
                                      <input type="radio" name="access" value="p" checked="checked" />Search public content only
                                    </label>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <label>
                                      <input type="radio" name="access" value="p"/>Search public content only
                                    </label>
                                  </xsl:otherwise>
                                </xsl:choose>
                                <xsl:choose>
                                  <xsl:when test="$access='a'">
                                    <label>
                                      <input type="radio" name="access" value="a" checked="checked" />Search public and secure content (login required)
                                    </label>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <label>
                                      <input type="radio" name="access" value="a"/>Search public and secure content (login required)
                                    </label>
                                  </xsl:otherwise>
                                </xsl:choose>
                              </font>
                            </td>
                          </tr>
                        </xsl:if>
                      </table>
                    </td>
                  </tr>

                </table>
              </td>
            </tr>
          </table>
          <br />
          <br />

          <!--====Page-Specific Search======-->
          <table cellpadding="6" cellspacing="0" border="0">
            <tr>
              <td>
                <b>Page-Specific Search</b>
              </td>
            </tr>
          </table>

          <table cellspacing="0" cellpadding="3" border="0" width="100%">
            <tr bgcolor="{$adv_search_panel_bgcolor}">
              <td>
                <table width="100%" cellpadding="0" cellspacing="0"
                border="0">
                  <tr bgcolor="{$adv_search_panel_bgcolor}">
                    <td>

                      <table width="100%" cellpadding="2"
                      cellspacing="0" border="0">
                        <form method="get" action="search" name="h">

                          <tr bgcolor="{$global_bg_color}">
                            <td width="15%">
                              <font size="-1">
                                <b>Links</b>
                              </font>
                            </td>

                            <td width="40%" nowrap="nowrap">
                              <font size="-1">Find pages that link to the page</font>
                            </td>

                            <td nowrap="nowrap">
                              <xsl:choose>
                                <xsl:when test="PARAM[@name='as_lq']">
                                  <input type="text" size="30"
                                   value="{PARAM[@name='as_lq']/@value}"
                                           name="as_lq" />
                                </xsl:when>
                                <xsl:otherwise>
                                  <input type="text" size="30" value="" name="as_lq" />
                                </xsl:otherwise>

                              </xsl:choose>
                              <font size="-1">
                                <input type="submit" name="btnG" value="{$search_button_text}" />
                              </font>
                            </td>
                          </tr>
                        </form>
                      </table>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>

          <xsl:call-template name="copyright"/>

        </form>

        <!-- *** Customer's own advanced search page footer *** -->
        <xsl:call-template name="my_page_footer"/>

      </body>
    </html>
  </xsl:template>

  <!-- **********************************************************************
 Resend query with filter=p to disable path_filtering
 if there is only one result cluster (do not customize)
     ********************************************************************** -->
  <xsl:template name="redirect_if_few_results">
    <xsl:variable name="count" select="count(/GSP/RES/R)"/>
    <xsl:variable name="start" select="/GSP/RES/@SN"/>
    <xsl:variable name="filterall"
      select="count(/GSP/PARAM[@name='filter']) = 0"/>
    <xsl:variable name="filter" select="/GSP/PARAM[@name='filter']/@value"/>

  </xsl:template>

  <!-- **********************************************************************
 Search results (do not customize)
     ********************************************************************** -->
  <xsl:template name="search_results">
    <html>

      <!-- *** HTML header and style *** -->
      <xsl:call-template name="langHeadStart"/>
      <xsl:call-template name="redirect_if_few_results"/>
      <title>
        <xsl:value-of select="$result_page_title"/>:
        <xsl:value-of select="$space_normalized_query"/>
      </title>
      <xsl:call-template name="style"/>
	  <xsl:if test="$render_dynamic_navigation = '1'">
      <script type="text/javascript" src="dnav_gsbs_priya.js"></script>
	  <script type="text/javascript">
        <xsl:variable name="dnavs_param">
          <xsl:if test="/GSP/PARAM[@name='dnavs']"><xsl:value-of
              select="/GSP/PARAM[@name='dnavs']/@original_value"/></xsl:if>
        </xsl:variable>
        var dynNavMgr = new gsa.search.DynNavManager(
          "<xsl:value-of select="$dnavs_param"/>",
          "<xsl:value-of select="/GSP/PARAM[@name='q']/@original_value"/>",
          "<xsl:value-of select='$original_q'/>",
          "<xsl:value-of select='$no_q_dnavs_params'/>",
          <xsl:value-of select='/GSP/RES/PARM/PC'/>
        );
      </script>
	  </xsl:if>
      <script type="text/javascript">
        <xsl:comment>
         
          function resetForms() {
          for (var i = 0; i &lt; document.forms.length; i++ ) {
          document.forms[i].reset();
          }
          }
		  
		  // Search query
        var page_query = &quot;<xsl:value-of select="$stripped_search_query"/>&quot;
        // Starting page offset, usually 0 for 1st page, 10 for 2nd, 20 for 3rd.
        var page_start = &quot;<xsl:value-of select="/GSP/PARAM[@name='start']/@value"/>&quot;
        // Front end that served the page.
        var page_site = &quot;<xsl:value-of select="/GSP/PARAM[@name='site']/@value"/>&quot;
          //
        </xsl:comment>
      </script>
      <xsl:call-template name="langHeadEnd"/>

  <xsl:choose>
    <xsl:when test="$show_res_clusters != '1'">
      <script language='javascript' src='common.js'></script>
      <script language='javascript' src='xmlhttp.js'></script>
      <script language='javascript' src='uri.js'></script>
      

      <body onLoad="resetForms(); ss_sf();" dir="ltr">
        <xsl:call-template name="search_results_body"/>
      </body>
    </xsl:when>
    <xsl:otherwise>
      <body onLoad="resetForms()" dir="ltr">
        <xsl:call-template name="search_results_body"/>
      </body>
    </xsl:otherwise>
  </xsl:choose>



    </html>
  </xsl:template>

  <xsl:template name="search_results_body">
    <xsl:call-template name="analytics"/>

    <!-- *** Customer's own result page header *** -->
    <xsl:if test="$choose_result_page_header = 'mine' or
                $choose_result_page_header = 'both'">
      <xsl:call-template name="my_page_header"/>
    </xsl:if>

    <!-- *** Result page header *** -->
    <xsl:if test="$choose_result_page_header = 'provided' or
                $choose_result_page_header = 'both'">
      <xsl:call-template name="result_page_header" />
    </xsl:if>

    <!-- *** Top separation bar *** -->
    <xsl:if test="Q != ''">
      <xsl:call-template name="top_sep_bar">
        <xsl:with-param name="text" select="$sep_bar_std_text"/>
        <xsl:with-param name="show_info" select="$show_search_info"/>
        <xsl:with-param name="time" select="TM"/>
      </xsl:call-template>
    </xsl:if>

    <!-- *** Handle results (if any) *** -->
    <xsl:choose>
      <xsl:when test="RES or GM or Spelling or Synonyms or CT or /GSP/ENTOBRESULTS">
        <xsl:call-template name="results">
          <xsl:with-param name="query" select="Q"/>
          <xsl:with-param name="time" select="TM"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="Q=''">
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="no_RES">
          <xsl:with-param name="query" select="Q"/>

        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>

    <!-- *** Google footer *** -->
    <xsl:call-template name="copyright"/>

    <!-- *** Customer's own result page footer *** -->
    <xsl:call-template name="my_page_footer"/>

	<xsl:if test="$render_dynamic_navigation = '1'">
      <script type="text/javascript">
        dynNavMgr.init();
      </script>
    </xsl:if>

    <!-- *** HTML footer *** -->
  </xsl:template>


  <!-- **********************************************************************
  Collection menu beside the search box
     ********************************************************************** -->
  <xsl:template name="collection_menu">
    <xsl:if test="$search_collections_xslt != ''">
      <td valign="middle">

        <select name="site">
          <xsl:choose>
            <xsl:when test="PARAM[(@name='site') and (@value='default_collection')]">
              <option value="default_collection" selected="selected">default_collection</option>
            </xsl:when>
            <xsl:otherwise>
              <option value="default_collection">default_collection</option>
            </xsl:otherwise>
          </xsl:choose>
        </select>

      </td>
    </xsl:if>
  </xsl:template>

  <!-- **********************************************************************
  Search box input form (Types: std_top, std_bottom, home, swr)
     ********************************************************************** -->
  <xsl:template name="search_box">
    <xsl:param name="type"/>

    <form name="gs" method="GET" action="search">
      <table border="0" cellpadding="0" cellspacing="0">
        <xsl:if test="($egds_show_search_tabs != '0') and (($type = 'home') or ($type = 'std_top'))">
          <tr>
            <td>
              <table cellpadding="4" cellspacing="0">
                <tr>
                  <td>
                    <xsl:call-template name="desktop_tab"/>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </xsl:if>
        <xsl:if test="($type = 'swr')">
          <tr>
            <td>
              <table cellpadding="4" cellspacing="0">
                <tr>
                  <td>
                    There were about <b>
                      <xsl:value-of select="RES/M"/>
                    </b> results for <b>
                      <xsl:value-of select="$space_normalized_query"/>
                    </b>.
                    <br/>
                    Use the search box below to search within these results.
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </xsl:if>
        <tr>
          <td>
            <table cellpadding="0" cellspacing="0">
              <tr>
                <td valign="middle">
                  <font size="-1">
                    <xsl:choose>
                      <xsl:when test="($type = 'swr')">
                        <input type="text" name="as_q" size="{$search_box_size}" maxlength="256" value=""/>
                        <input type="hidden" name="q" value="{$qval}"/>
                      </xsl:when>
                      <xsl:otherwise>
                        <input type="text" name="q" size="{$search_box_size}" maxlength="256" value="{$space_normalized_query}"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </font>
                </td>
                <xsl:call-template name="collection_menu"/>
                <td valign="middle">
                  <font size="-1">
                    <xsl:call-template name="nbsp"/>
                    <xsl:choose>
                      <xsl:when test="$choose_search_button = 'image'">
                        <input type="image" name="btnG" src="{$search_button_image_url}"
                       valign="bottom" width="60" height="26"
                       border="0" value="{$search_button_text}"/>
                      </xsl:when>
                      <xsl:otherwise>
                        <input type="submit" name="btnG" value="{$search_button_text}"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </font>
                </td>
                <td nowrap="1">
                  <font size="-2">
                    <xsl:if test="(/GSP/RES/M > 0) and ($show_swr_link != '0') and ($type = 'std_bottom')">
                      <xsl:call-template name="nbsp"/>
                      <xsl:call-template name="nbsp"/>
                      <a href="{$swr_search_url}">
                        <xsl:value-of select="$swr_search_anchor_text"/>
                      </a>
                      <br/>
                    </xsl:if>
                    <xsl:if test="$show_result_page_adv_link != '0'">
                      <xsl:call-template name="nbsp"/>
                      <xsl:call-template name="nbsp"/>
                      <a href="{$adv_search_url}">
                        <xsl:value-of select="$adv_search_anchor_text"/>
                      </a>
                      <br/>
                    </xsl:if>
                    <xsl:if test="$show_alerts_link != '0'">
                      <xsl:call-template name="nbsp"/>
                      <xsl:call-template name="nbsp"/>
                      <a href="{$alerts_url}">
                        <xsl:value-of select="$alerts_anchor_text"/>
                      </a>
                      <br/>
                    </xsl:if>
                    <xsl:if test="$show_result_page_help_link != '0'">
                      <xsl:call-template name="nbsp"/>
                      <xsl:call-template name="nbsp"/>
                      <a href="{$help_url}">
                        <xsl:value-of select="$search_help_anchor_text"/>
                      </a>
                    </xsl:if>
                    <br/>
                  </font>
                </td>
              </tr>
              <xsl:if test="$show_secure_radio != '0'">
                <tr>
                  <td colspan="2">
                    <font size="-1">
                      Search:
                      <xsl:choose>
                        <xsl:when test="$access='p'">
                          <label>
                            <input type="radio" name="access" value="p" checked="checked" />public content
                          </label>
                        </xsl:when>
                        <xsl:otherwise>
                          <label>
                            <input type="radio" name="access" value="p"/>public content
                          </label>
                        </xsl:otherwise>
                      </xsl:choose>
                      <xsl:choose>
                        <xsl:when test="$access='a'">
                          <label>
                            <input type="radio" name="access" value="a" checked="checked" />public and secure content
                          </label>
                        </xsl:when>
                        <xsl:otherwise>
                          <label>
                            <input type="radio" name="access" value="a"/>public and secure content
                          </label>
                        </xsl:otherwise>
                      </xsl:choose>
                    </font>
                  </td>
                </tr>
              </xsl:if>
            </table>
          </td>
        </tr>
      </table>
      <xsl:text>
    </xsl:text>
      <xsl:call-template name="form_params"/>
    </form>
  </xsl:template>


  <!-- **********************************************************************
  Bottom search box (do not customized)
     ********************************************************************** -->
  <xsl:template name="bottom_search_box">
    <br clear="all"/>
    <br/>
    <table width="100%" border="0" cellpadding="0" cellspacing="0">
      <tr>
        <td bgcolor="{$sep_bar_border_color}">
          <img width="1" height="1" alt=""/>
        </td>
      </tr>
    </table>
    <table width="100%" border="0" cellpadding="0" cellspacing="0" bgcolor="{$sep_bar_bg_color}">
      <tr>
        <td nowrap="1" bgcolor="{$sep_bar_bg_color}" align="center">
          <br/>
          <xsl:call-template name="search_box">
            <xsl:with-param name="type" select="'std_bottom'"/>
          </xsl:call-template>
          <br/>
        </td>
      </tr>
    </table>
    <table width="100%" border="0" cellpadding="0" cellspacing="0">
      <tr>
        <td bgcolor="{$sep_bar_border_color}">
          <img width="1" height="1" alt=""/>
        </td>
      </tr>
    </table>
  </xsl:template>


  <!-- **********************************************************************
 Sort-by criteria: sort by date/relevance
     ********************************************************************** -->
  <xsl:template name="sort_by">
    <xsl:variable name="sort_by_url">
      <xsl:for-each
    select="/GSP/PARAM[(@name != 'sort') and
                       (@name != 'start') and
                       (@name != 'epoch' or $is_test_search != '') and
                       not(starts-with(@name, 'metabased_'))]">
        <xsl:value-of select="@name"/>
        <xsl:text>=</xsl:text>
        <xsl:value-of select="@original_value"/>
        <xsl:if test="position() != last()">
          <xsl:text disable-output-escaping="yes">&amp;</xsl:text>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <xsl:variable name="sort_by_relevance_url">
      <xsl:value-of select="$sort_by_url"
      />&amp;sort=date%3AD%3AL%3Ad1
    </xsl:variable>

    <xsl:variable name="sort_by_date_url">
      <xsl:value-of select="$sort_by_url"
      />&amp;sort=date%3AD%3AS%3Ad1
    </xsl:variable>

    <table>
      <tr valign='top'>
        <td>
          <span class="s">
            <xsl:choose>
              <xsl:when test="/GSP/PARAM[@name = 'sort' and starts-with(@value,'date:D:S')]">
                <font color="{$global_text_color}">
                  <xsl:text>Sort by date / </xsl:text>
                </font>
                <a href="GSASearchresults.aspx?{$sort_by_relevance_url}">Sort by relevance</a>
              </xsl:when>
              <xsl:when test="/GSP/PARAM[@name = 'sort' and starts-with(@value,'date:A:S')]">
                <font color="{$global_text_color}">
                  <xsl:text>Sort by date / </xsl:text>
                </font>
                <a href="GSASearchresults.aspx?{$sort_by_relevance_url}">Sort by relevance</a>
              </xsl:when>
              <xsl:otherwise>
                <a href="GSASearchresults.aspx?{$sort_by_date_url}">Sort by date</a>
                <font color="{$global_text_color}">
                  <xsl:text> / Sort by relevance</xsl:text>
                </font>
              </xsl:otherwise>
            </xsl:choose>
          </span>
        </td>
      </tr>
    </table>
  </xsl:template>

  <!-- Generates search results navigation bar to be placed at the bottom. -->
<xsl:template name="gen_bottom_navigation">
  <xsl:if test="RES">
    <xsl:variable name="nav_style">
      <xsl:choose>
        <xsl:when test="($access='s') or ($access='a')">simple</xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$choose_bottom_navigation"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:call-template name="google_navigation">
      <xsl:with-param name="prev" select="RES/NB/PU"/>
      <xsl:with-param name="next" select="RES/NB/NU"/>
      <xsl:with-param name="view_begin" select="RES/@SN"/>
      <xsl:with-param name="view_end" select="RES/@EN"/>
      <xsl:with-param name="guess" select="RES/M"/>
      <xsl:with-param name="navigation_style" select="$nav_style"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>
  
  
  <!-- **********************************************************************
 Output all results
     ********************************************************************** -->
  <xsl:template name="results">
    <xsl:param name="query"/>
    <xsl:param name="time"/>

	<xsl:choose>
    <xsl:when test="$render_dynamic_navigation = '1'">
      <xsl:call-template name="dynamic_navigation_results">
        <xsl:with-param name="query" select="$query"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
    <!-- *** Add top navigation/sort-by bar *** -->
    <xsl:if test="($show_top_navigation != '0') or ($show_sort_by != '0')">
      <xsl:if test="RES">
        <!-- there might be onebox results but no RES  -->
        <table width="100%">
          <tr>

            <!--  *** SPSMod: Only show Google Navigation if Sharepoint is disabled   *** -->
            <xsl:if test="($show_top_navigation != '0') and (count(/GSP/PARAM[@name='wss']) = 0)">

              <td align="right">

                <!-- *** Add bottom navigation *** -->
                <xsl:variable name="nav_style">
                  <xsl:choose>
                    <xsl:when test="($access='s') or ($access='a')">simple</xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="$choose_bottom_navigation"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:variable>



                <xsl:call-template name="google_navigation">
                  <xsl:with-param name="prev" select="RES/NB/PU"/>
                  <xsl:with-param name="next" select="RES/NB/NU"/>
                  <xsl:with-param name="view_begin" select="RES/@SN"/>
                  <xsl:with-param name="view_end" select="RES/@EN"/>
                  <xsl:with-param name="guess" select="RES/M"/>
                  <xsl:with-param name="navigation_style" select="top"/>
                </xsl:call-template>


              </td>
            </xsl:if>


            <xsl:if test="$show_sort_by != '0'">
              <td align="right">
                <xsl:call-template name="sort_by"/>
              </td>
            </xsl:if>
          </tr>
        </table>
      </xsl:if>
    </xsl:if>

    <!-- *** Handle OneBox results, if any ***-->
    <xsl:if test="$show_onebox != '0'">
      <xsl:if test="/GSP/ENTOBRESULTS">
        <xsl:call-template name="onebox"/>
      </xsl:if>
    </xsl:if>

    <!-- *** Handle spelling suggestions, if any *** -->
    <xsl:if test="$show_spelling != '0'">
      <xsl:call-template name="spelling"/>
    </xsl:if>

    <!-- *** Handle synonyms, if any *** -->
    <xsl:if test="$show_synonyms != '0'">
      <xsl:call-template name="synonyms"/>
    </xsl:if>

    <!-- *** Output Google Desktop results (if enabled and any available) *** -->
    <xsl:if test="$egds_show_desktop_results != '0'">
      <xsl:call-template name="desktop_results"/>
    </xsl:if>

    <!-- *** Output results details *** -->

    <xsl:if test="$show_res_clusters = '1'">
      <div id='clustering'>
        <h3>Narrow your search</h3>

        <span id='cluster_status'>
          <span id='cluster_message' style="display:none">Loading...</span>
          <noscript>
            Javascript must be enabled for narrowing.
          </noscript>
        </span>

        <xsl:choose>
          <xsl:when test="$res_cluster_position = 'top'">
            <table>
              <tr>
                <td id='cluster_label0'></td>
                <td id='cluster_label2'></td>
                <td id='cluster_label4'></td>
                <td id='cluster_label6'></td>
                <td id='cluster_label8'></td>
              </tr>
              <tr>
                <td id='cluster_label1'></td>
                <td id='cluster_label3'></td>
                <td id='cluster_label5'></td>
                <td id='cluster_label7'></td>
                <td id='cluster_label9'></td>
              </tr>
            </table>
          </xsl:when>
          <xsl:when test="$res_cluster_position = 'right'">
            <ul>
              <li id='cluster_label0'></li>
              <li id='cluster_label1'></li>
              <li id='cluster_label2'></li>
              <li id='cluster_label3'></li>
              <li id='cluster_label4'></li>
              <li id='cluster_label5'></li>
              <li id='cluster_label6'></li>
              <li id='cluster_label7'></li>
              <li id='cluster_label8'></li>
              <li id='cluster_label9'></li>
            </ul>
          </xsl:when>
        </xsl:choose>
      </div>
    </xsl:if>

    <div>
      <!-- for keymatch results -->
      <xsl:if test="$show_keymatch != '0'">
        <xsl:apply-templates select="/GSP/GM"/>
      </xsl:if>

      <!-- for real results -->
      <xsl:apply-templates select="RES/R">
        <xsl:with-param name="query" select="$query"/>
      </xsl:apply-templates>

     
    </div>



    <!-- *** SPSMod:   Using SPS controls if enabled ***  -->
    <xsl:if test="count(/GSP/PARAM[@name='wss']) = 0">

      <!-- *** Add bottom navigation *** -->
      <xsl:variable name="nav_style">
        <xsl:choose>
          <xsl:when test="($access='s') or ($access='a')">simple</xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$choose_bottom_navigation"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:call-template name="google_navigation">
        <xsl:with-param name="prev" select="RES/NB/PU"/>
        <xsl:with-param name="next" select="RES/NB/NU"/>
        <xsl:with-param name="view_begin" select="RES/@SN"/>
        <xsl:with-param name="view_end" select="RES/@EN"/>
        <xsl:with-param name="guess" select="RES/M"/>
        <xsl:with-param name="navigation_style" select="$nav_style"/>
      </xsl:call-template>

    </xsl:if>



    <!-- *** Bottom search box *** -->
    <xsl:if test="$show_bottom_search_box != '0'">
      <xsl:call-template name="bottom_search_box"/>
    </xsl:if>

	</xsl:otherwise>
	</xsl:choose>
  </xsl:template>


  
<xsl:template name="dynamic_navigation_results">
  <xsl:param name="query"/>

  <!-- show sort-by -->
  <xsl:if test="$show_sort_by != '0' or $show_spelling != '0' or $show_synonyms != '0'">
    <xsl:if test="RES"> <!-- there might be onebox results but no RES  -->
      <table width="100%">
      <tr>
        <xsl:if test="$show_spelling != '0' or $show_synonyms != '0'">
          <td align="left">
            <xsl:choose>
              <!-- *** handle spelling suggestions, if any *** -->
              <xsl:when test="$show_spelling != '0'">
                <xsl:call-template name="spelling"/>
              </xsl:when>
              <!-- *** handle synonyms, if any *** -->
              <xsl:otherwise>
                <xsl:call-template name="synonyms"/>
              </xsl:otherwise>
            </xsl:choose>
          </td>
        </xsl:if>

        <xsl:if test="$show_sort_by != '0'">
          <td align="right">
            <xsl:call-template name="sort_by"/>
          </td>
        </xsl:if>
      </tr>
      </table>
    </xsl:if>
  </xsl:if>

  <xsl:if test="$show_spelling != '0' and $show_synonyms != '0'">
    <xsl:call-template name="synonyms"/>
  </xsl:if>

  <xsl:variable name="dn_tokens"
    select="tokenize(/GSP/PARAM[@name='dnavs']/@original_value, '\+')"/>
  <xsl:variable name="partial_count" select="/GSP/RES/PARM/PC"/>

  <xsl:variable name="div_pos">
    <xsl:choose>
      <xsl:when test="$show_sort_by != '0'">
        <xsl:text>position: relative;</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>position: relative; margin-top: 10px;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <div id="main" style="{$div_pos}">
    <div id="main_res">
      <xsl:call-template name="main_results">
        <xsl:with-param name="query" select="$query"/>
        <xsl:with-param name="dn_tokens" select="$dn_tokens"/>
      </xsl:call-template>
    </div>
    <div id="dyn_nav">
      <div class="dn-hdr">
        <span style="padding-left: 6px;">
          <b>Navigate</b>
        </span>
      </div>
      <div id="dyn_nav_col" style="height: 100%">
        <xsl:apply-templates select="/GSP/RES/PARM/PMT">
          <xsl:with-param name="dn_tokens" select="$dn_tokens"/>
          <xsl:with-param name="partial_count" select="/GSP/RES/PARM/PC"/>
        </xsl:apply-templates>

        <script type="text/javascript">
          <xsl:for-each select="$dn_tokens">
            dynNavMgr.addSelectedAttr("<xsl:value-of select='.'/>");
          </xsl:for-each>

          <xsl:apply-templates select="/GSP/RES/PARM/PMT[@IR = 0]" mode="hidden"/>
        </script>
      </div>
    </div>
  </div>
</xsl:template>

<xsl:template name="escapeQuot">
  <xsl:param name="text"/>
  <xsl:choose>
    <xsl:when test="contains($text, '&quot;')">
      <xsl:variable name="bufferBefore" select="substring-before($text, '&quot;')"/>
      <xsl:variable name="newBuffer" select="substring-after($text, '&quot;')"/>
      <xsl:value-of select="$bufferBefore"/><xsl:text>\"</xsl:text>
      <xsl:call-template name="escapeQuot">
        <xsl:with-param name="text" select="$newBuffer"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$text"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="PMT" mode="hidden">
  <xsl:variable name="name"><xsl:value-of select="normalize-space(@NM)"/></xsl:variable>
  <xsl:variable name="values">
    [<xsl:for-each select="PV[position() &gt; $dyn_nav_max_rows and @C != '0']">["<xsl:call-template
        name='escapeQuot'><xsl:with-param name="text" select="@V"/>
        </xsl:call-template>", <xsl:value-of select='@C'/>],</xsl:for-each>]
  </xsl:variable>

  dynNavMgr.addAttrValues("<xsl:value-of select='$name'/>", <xsl:value-of select='$values'/>);
</xsl:template>

<xsl:template match="PMT">
  <xsl:param name="dn_tokens"/>
  <xsl:param name="partial_count"/>

  <xsl:variable name="name"><xsl:value-of select="normalize-space(@NM)"/></xsl:variable>
  <xsl:variable name="pmt_name"><xsl:call-template
      name="term-escape"><xsl:with-param name="val" select="@NM"/></xsl:call-template>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="@IR = 1">
      <ul id="{$name}" class="dn-attr">
        <li class="dn-attr-hdr"><span class="dn-attr-hdr-txt"><xsl:attribute
            name="title"><xsl:value-of select="@DN" disable-output-escaping="yes"/>
        </xsl:attribute><xsl:value-of select="@DN"/></span></li>
        <xsl:apply-templates select="PV">
          <xsl:with-param name="pmt_name" select="$pmt_name"/>
          <xsl:with-param name="dn_tokens" select="$dn_tokens"/>
          <xsl:with-param name="partial_count" select="$partial_count"/>
        </xsl:apply-templates>
      </ul>
    </xsl:when>
    <xsl:otherwise>
      <xsl:variable name="total" select="count(PV[@C != '0'])"/>
      <xsl:variable name="attr_class">
        <xsl:choose>
          <xsl:when test="$total &lt; $dyn_nav_max_rows + 1">
            <xsl:value-of select="'dn-attr'"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="'dn-attr dn-attr-more'"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <ul id="{$name}" class="{$attr_class}">
        <xsl:choose>
          <xsl:when test="$total &lt; $dyn_nav_max_rows + 1">
            <li class="dn-attr-hdr"><span class="dn-attr-hdr-txt"><xsl:attribute
              name="title"><xsl:value-of select="@DN" disable-output-escaping="yes"/>
            </xsl:attribute><xsl:value-of select="@DN"/></span></li>
          </xsl:when>
          <xsl:otherwise>
            <li class="dn-attr-hdr"><div class="dn-zippy-hdr"><div class="dn-zippy-hdr-img"></div>
              <span class="dn-attr-hdr-txt"><xsl:attribute
                name="title"><xsl:value-of select="@DN" disable-output-escaping="yes"/>
              </xsl:attribute><xsl:value-of select="@DN"/></span></div></li>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="PV[position() &lt; $dyn_nav_max_rows + 1]">
          <xsl:with-param name="pmt_name" select="$pmt_name"/>
          <xsl:with-param name="dn_tokens" select="$dn_tokens"/>
          <xsl:with-param name="partial_count" select="$partial_count"/>
        </xsl:apply-templates>

        <xsl:variable name="total_left" select="$total - $dyn_nav_max_rows"/>
        <xsl:if test="$total &gt; $dyn_nav_max_rows">
          <li id="{$name}_more_less">
            <a id="more_{$name}" class="dn-link" style="margin-right: 10px; outline-style: none;"
              onclick="dynNavMgr.displayMore('{$name}', true); return false;"
              href="javascript:;">
              <span class="dn-more-img dn-mimg"></span>
              <span id="disp_{$name}"><xsl:value-of select="$total_left"/> More</span>
            </a>
            <a id="less_{$name}" class="dn-link dn-hidden" style="outline-style: none;"
              onclick="dynNavMgr.displayMore('{$name}', false, {$total_left}); return false;"
              href="javascript:;">
              <span class="dn-more-img dn-limg"></span>
              <span>Less</span>
            </a>
          </li>
          <label class="dn-hidden dn-name"><xsl:value-of select="$name"/></label>
          <label id="{$name}_total" class="dn-hidden"><xsl:value-of select="$total"/></label>
          <label id="{$name}_total_left" class="dn-hidden"><xsl:value-of select="$total_left"/></label>
        </xsl:if>
      </ul>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<xsl:template match="PV">
  <xsl:param name="pmt_name"/>
  <xsl:param name="dn_tokens"/>
  <xsl:param name="partial_count"/>

  <xsl:if test="@C != 0">
    <xsl:apply-templates select="." mode="construct">
      <xsl:with-param name="dn_tokens" select="$dn_tokens"/>
      <xsl:with-param name="partial_count" select="$partial_count"/>
      <xsl:with-param name="current_token">
        <xsl:choose>
          <xsl:when test="../@IR = '1'"><xsl:variable
            name="stripped_l" select="normalize-space(@L)"/><xsl:variable
            name="stripped_h" select="normalize-space(@H)"/>inmeta:<xsl:value-of
            select="$pmt_name"/>:<xsl:choose><xsl:when test="../@T = 3"><xsl:if
            test="$stripped_l != ''">$<xsl:value-of select="$stripped_l"/></xsl:if>..<xsl:if
            test="$stripped_h != ''">$<xsl:value-of
            select="$stripped_h"/></xsl:if></xsl:when><xsl:otherwise><xsl:value-of
            select="$stripped_l"/>..<xsl:value-of select="$stripped_h"/></xsl:otherwise></xsl:choose>
          </xsl:when>
          <xsl:otherwise>inmeta:<xsl:value-of select="$pmt_name"/>%3D<xsl:call-template
              name="term-escape"><xsl:with-param name="val" select="@V"/></xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:if>
</xsl:template>

<xsl:template match="PV" mode="construct">
  <xsl:param name="dn_tokens"/>
  <xsl:param name="current_token"/>
  <xsl:param name="partial_count"/>

  <xsl:variable name="dispval">
    <xsl:apply-templates select="." mode="display_value"/>
  </xsl:variable>

  <xsl:variable name="dispcount">
    <xsl:text> (</xsl:text><xsl:if
       test="$partial_count=1"><xsl:text>&gt; </xsl:text></xsl:if>
      <xsl:value-of select="@C"/><xsl:text>)</xsl:text>
  </xsl:variable>

  <xsl:variable name="is_selected" select="index-of($dn_tokens, $current_token)"/>
  <li>
    <xsl:choose>
      <xsl:when test="exists($is_selected)">
        <xsl:variable name="other_tokens">
          <xsl:value-of select="string-join(remove($dn_tokens, $is_selected[position()=1]), '+')"/>
        </xsl:variable>

        <xsl:variable name="cancel_url">
          <xsl:value-of select="$no_q_dnavs_params"/>&amp;q=<xsl:value-of
            select="$original_q"/><xsl:if test="$other_tokens != ''">+<xsl:value-of
            select="$other_tokens"/>&amp;dnavs=<xsl:value-of select="$other_tokens"/></xsl:if>
        </xsl:variable>
		
		<a class="dn-img dn-r-img" href="GSASearchresults.aspx?{$cancel_url}"
            title="Clear"></a>
        <span class="dn-overflow dn-inline-block" style="width: 86%;">
          <xsl:if test="../@IR != 1">
            <xsl:attribute name="title"><xsl:value-of select="$dispval"
                disable-output-escaping="yes"/></xsl:attribute>
          </xsl:if>
          <xsl:value-of select="concat($dispval, $dispcount)"
              disable-output-escaping="yes"/>
        </span>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="pmts_var">dnavs=<xsl:if test="/GSP/PARAM[@name='dnavs']"><xsl:value-of
            select="/GSP/PARAM[@name='dnavs']/@original_value"/>+</xsl:if><xsl:value-of
            select="$current_token"/>
        </xsl:variable>
        <xsl:variable name="qurl"><xsl:value-of select="$no_q_dnavs_params"/>&amp;q=<xsl:value-of
            select="/GSP/PARAM[@name='q']/@original_value"/>+<xsl:value-of
            select="$current_token"/>&amp;<xsl:value-of select="$pmts_var"/>
        </xsl:variable>
        <a class="dn-attr-v" href="GSASearchresults.aspx?{encode-for-uri($qurl)}">
          <xsl:if test="../@IR != 1">
            <xsl:attribute name="title"><xsl:value-of select="$dispval"
                disable-output-escaping="no"/></xsl:attribute>
          </xsl:if>
          <xsl:value-of select="$dispval" disable-output-escaping="yes"/>
          <span class="dn-attr-c"><xsl:value-of select="$dispcount"
              disable-output-escaping="yes"/></span>
        </a>
      </xsl:otherwise>
    </xsl:choose>
  </li>
</xsl:template>

<xsl:template match="PV" mode="display_value">
  <xsl:choose>
    <xsl:when test="../@IR = 1">
      <xsl:variable name="disp_l">
        <xsl:call-template name="pmt_range_display_val">
          <xsl:with-param name="val" select="@L"/>
          <xsl:with-param name="type" select="../@T"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="disp_h">
        <xsl:call-template name="pmt_range_display_val">
          <xsl:with-param name="val" select="@H"/>
          <xsl:with-param name="type" select="../@T"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$disp_l = ''">
          <xsl:value-of select="$disp_h"/><xsl:text> </xsl:text>
          <xsl:choose>
            <xsl:when test="../@T = 4">or earlier</xsl:when>
            <xsl:otherwise>or less</xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="$disp_h = ''">
          <xsl:value-of select="$disp_l"/><xsl:text> </xsl:text>
          <xsl:choose>
            <xsl:when test="../@T = 4">or later</xsl:when>
            <xsl:otherwise>or more</xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise><xsl:value-of
          select="$disp_l"/><xsl:text> </xsl:text><xsl:call-template
          name="endash"/><xsl:text> </xsl:text><xsl:value-of select="$disp_h"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="@V"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:variable name="hex">0123456789ABCDEF</xsl:variable>
<xsl:template name="term-escape">
  <xsl:param name="val"/>
  <xsl:variable name="first-char" select="substring($val, 1, 1)"/>
  <xsl:variable name="code"
    select="string-to-codepoints($first-char)[position()=1]"/>
  <xsl:choose>
    <xsl:when test="not(($code >= 48 and $code &lt;= 57) or
      ($code >= 65 and $code &lt;= 90) or ($code = 95) or
      ($code >= 97 and $code &lt;= 122))">
      <xsl:choose>
        <xsl:when test="$code > 128">
          <xsl:value-of select="encode-for-uri($first-char)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="hex-digit1"
            select="substring($hex, floor($code div 16) + 1, 1)"/>
          <xsl:variable name="hex-digit2"
            select="substring($hex, $code mod 16 + 1, 1)"/>
          <xsl:value-of select="concat('%25', $hex-digit1 ,$hex-digit2)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$first-char"/>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:if test="string-length($val) > 1">
    <xsl:call-template name="term-escape">
      <xsl:with-param name="val" select="substring($val, 2)"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>


<xsl:template name="pmt_range_display_val">
  <xsl:param name="val"/>
  <xsl:param name="type"/>
  <xsl:choose>
    <xsl:when test="$val != '' and ($type = 2 or $type = 3)">
      <xsl:value-of select="format-number($val, '#.##')"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$val"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

 
<xsl:template name="main_results">
  <xsl:param name="query"/>
  <xsl:param name="dn_tokens"/>

  <xsl:if test="$render_dynamic_navigation = '1'">
    <div class="dn-bar">
      <xsl:variable name="all_results_url"><xsl:value-of
          select="$no_q_dnavs_params"/>&amp;q=<xsl:value-of select="$original_q"/>
      </xsl:variable>

      <!-- Add next/prev navigation -->
      <xsl:if test="$show_top_navigation != '0' and /GSP/RES">
        <span class="dn-bar-rt">
          <xsl:call-template name="google_navigation">
            <xsl:with-param name="prev" select="/GSP/RES/NB/PU"/>
            <xsl:with-param name="next" select="/GSP/RES/NB/NU"/>
            <xsl:with-param name="view_begin" select="/GSP/RES/@SN"/>
            <xsl:with-param name="view_end" select="/GSP/RES/@EN"/>
            <xsl:with-param name="guess" select="/GSP/RES/M"/>
            <xsl:with-param name="navigation_style" select="'top'"/>
            <xsl:with-param name="dynamic_nav_bar" select="'1'"/>
          </xsl:call-template>
        </span>
      </xsl:if>

      <a class="dn-link" style="text-decoration: underline; color: #000;"
        href="GSASearchresults.aspx?{$all_results_url}">All results</a>

      <xsl:if test="exists($dn_tokens)">
        <xsl:call-template name="rsaquo"/>
        <xsl:variable name="root_node" select="/GSP"/>
        <xsl:for-each select="$dn_tokens">
          <xsl:variable name="other_pmts_tokens"
            select="string-join(remove($dn_tokens, position()), '+')"/>

          <xsl:variable name="cancel_url">
            <xsl:value-of select="$all_results_url"/>
            <xsl:if test="$other_pmts_tokens != ''">+<xsl:value-of
                select="$other_pmts_tokens"/>&amp;dnavs=<xsl:value-of select="$other_pmts_tokens"/>
            </xsl:if>
          </xsl:variable>

          <a class="dn-link cancel-url dn-bar-link" href="GSASearchresults.aspx?{$cancel_url}"
              title="Clear">
            <xsl:variable name="range_val" select="substring-after(., ':')"/>
            <xsl:choose>
              <xsl:when test="contains(., '..')">
                <xsl:for-each select="$root_node/RES/PARM/PMT">
                  <xsl:variable name="escaped_name"><xsl:call-template name="term-escape">
                    <xsl:with-param name="val" select="@NM"/>
                  </xsl:call-template></xsl:variable>
                  <xsl:if test="$escaped_name=substring-before($range_val, ':')">
                    <span class="dn-bar-v"><xsl:value-of select="@DN"/>:</span><xsl:call-template
                      name="nbsp"/><xsl:choose>
                      <xsl:when test="@T = '3'">
                        <xsl:for-each select="PV">
                          <xsl:variable name="check_val"><xsl:if
                            test="normalize-space(@L) != ''">$<xsl:value-of
                            select="normalize-space(@L)"/></xsl:if>..<xsl:if
                            test="normalize-space(@H) != ''">$<xsl:value-of
                              select="normalize-space(@H)"/></xsl:if>
                          </xsl:variable>
                          <xsl:if test="$check_val=substring-after($range_val, ':')">
                            <xsl:apply-templates select="current()" mode="display_value"/>
                          </xsl:if>
                        </xsl:for-each>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:apply-templates select="PV[concat(normalize-space(@L), '..',
                          normalize-space(@H))=substring-after($range_val, ':')]" mode="display_value"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:if>
                </xsl:for-each>
              </xsl:when>
              <xsl:otherwise>
                <xsl:for-each select="$root_node/RES/PARM/PMT">
                  <xsl:variable name="escaped_name"><xsl:call-template name="term-escape">
                    <xsl:with-param name="val" select="@NM"/>
                  </xsl:call-template></xsl:variable>
                  <xsl:if test="$escaped_name=substring-before($range_val, '%3D')">
                    <span class="dn-bar-v"><xsl:value-of select="./@DN"/>:</span><xsl:call-template
                      name="nbsp"/><xsl:for-each select="./PV"><xsl:variable
                        name="pv_val"><xsl:call-template name="term-escape">
                          <xsl:with-param name="val" select="./@V"/>
                        </xsl:call-template></xsl:variable>
                        <xsl:if test="$pv_val=substring-after($range_val, '%3D')">
                          <xsl:apply-templates select="." mode="display_value"/>
                        </xsl:if>
                    </xsl:for-each>
                  </xsl:if>
                </xsl:for-each>
              </xsl:otherwise>
            </xsl:choose>
          </a>

          <xsl:if test="position() != last()">
            <xsl:call-template name="rsaquo"/>
          </xsl:if>
        </xsl:for-each>
      </xsl:if>
    </div>

    <!-- *** Handle OneBox results, if any ***-->
    <xsl:if test="$show_onebox != '0' and count(/GSP/ENTOBRESULTS) &gt; 0">
      <xsl:call-template name="onebox"/>

      <script>
      <xsl:comment>
        if (populateUarMessages) {
	  populateUarMessages();
	}
      //</xsl:comment>
      </script>
    </xsl:if>

    <!-- *** output google desktop results (if enabled and any available) *** -->
    <xsl:if test="$egds_show_desktop_results != '0'">
      <xsl:call-template name="desktop_results"/>
    </xsl:if>
  </xsl:if>

  <xsl:choose>
    <xsl:when test="$show_sidebar = '1'">
      <table cellpadding="0" cellspacing="0" width="100%">
        <tr>
          
          <td id="left-side-container" width="55%" valign="top">
            <xsl:call-template name="render_main_results">
              <xsl:with-param name="query" select="$query"/>
            </xsl:call-template>
          </td>

          
          <td id="sidebar-container" class="sb-r" valign="top">
            <div id="sidebar">
            </div>
          </td>
        </tr>
      </table>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="render_main_results">
        <xsl:with-param name="query" select="$query"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>

  <!-- *** Filter note (if needed) *** -->
  <xsl:if test="(RES/FI) and (not(RES/NB/NU))">
    <p>
      <i>
      In order to show you the most relevant results, we have omitted some entries very similar to the <xsl:value-of select="RES/@EN"/> already displayed.<br/>If you like, you can <a href="GSASearchresults.aspx?{encode-for-uri($filter_url)}0">repeat the search with the omitted results included</a>.
      </i>
    </p>
  </xsl:if>

  <!-- *** Add bottom navigation *** -->
  <div id="bottom-navigation">
    <xsl:call-template name="gen_bottom_navigation" />
  </div>

  <!-- *** Bottom search box *** -->
  <div id="bottom-search-box">
    <xsl:if test="$show_bottom_search_box != '0' and RES">
      <xsl:call-template name="bottom_search_box"/>
    </xsl:if>
  </div>


</xsl:template>


<xsl:template name="render_main_results">
  <xsl:param name="query"/>
  <xsl:variable name="main_results_class">
    <xsl:if test="$render_dynamic_navigation = '1'">main-results</xsl:if>
  </xsl:variable>

  <div class="{$main_results_class}">
    <!-- for keymatch results -->
    <xsl:if test="$show_keymatch != '0'">
      <xsl:apply-templates select="/GSP/GM"/>
    </xsl:if>

    <xsl:apply-templates select="RES/R">
      <xsl:with-param name="query" select="$query"/>
    </xsl:apply-templates>
  </div>
</xsl:template>
  
  <!-- **********************************************************************
 Stopwords suggestions in result page (do not customize)
     ********************************************************************** -->
  <xsl:template name="stopwords">
    <xsl:variable name="stopwords_suggestions1">
      <xsl:call-template name="replace_string">
        <xsl:with-param name="find" select="'/help/basics.html#stopwords'"/>
        <xsl:with-param name="replace" select="'user_help.html#stop'"/>
        <xsl:with-param name="string" select="/GSP/CT"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="stopwords_suggestions">
      <xsl:call-template name="replace_string">
        <xsl:with-param name="find" select="'/help/basics.html'"/>
        <xsl:with-param name="replace" select="'user_help.html'"/>

        <xsl:with-param name="string" select="$stopwords_suggestions1"/>
      </xsl:call-template>

    </xsl:variable>

    <xsl:if test="/GSP/CT">
      <font size="-1" color="gray">
        <xsl:value-of disable-output-escaping="yes"
          select="$stopwords_suggestions"/>
      </font>
    </xsl:if>
  </xsl:template>


  <!-- **********************************************************************
 Spelling suggestions in result page (do not customize)
     ********************************************************************** -->
  <xsl:template name="spelling">
    <xsl:if test="/GSP/Spelling/Suggestion">
      <p>
        <span class="p">
          <font color="{$spelling_text_color}">
            <xsl:value-of select="$spelling_text"/>
            <xsl:call-template name="nbsp"/>
          </font>
        </span>
        <a href="GSASearchresults.aspx?k={/GSP/Spelling/Suggestion[1]/@qe}&amp;spell=1&amp;{$base_url}">
          <xsl:value-of disable-output-escaping="yes"
            select="/GSP/Spelling/Suggestion[1]"/>

        </a>

      </p>
    </xsl:if>
  </xsl:template>


  <!-- **********************************************************************
 Synonym suggestions in result page (do not customize)
     ********************************************************************** -->
  <xsl:template name="synonyms">
    <xsl:if test="/GSP/Synonyms/OneSynonym">
      <p>
        <span class="p">
          <font color="{$synonyms_text_color}">
            <xsl:value-of select="$synonyms_text"/>
            <xsl:call-template name="nbsp"/>
          </font>
        </span>
        <xsl:for-each select="/GSP/Synonyms/OneSynonym">
          <a href="GSASearchresults.aspx?k={@q}&amp;{$synonym_url}">
            <xsl:value-of disable-output-escaping="yes" select="."/>
          </a>
          <xsl:text> </xsl:text>
        </xsl:for-each>
      </p>
    </xsl:if>
  </xsl:template>


  <!-- **********************************************************************
 Truncation functions (do not customize)
     ********************************************************************** -->
  <xsl:template name="truncate_url">
    <xsl:param name="t_url"/>

    <xsl:choose>
      <xsl:when test="string-length($t_url) &lt; $truncate_result_url_length">
        <xsl:value-of select="$t_url"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="first" select="substring-before($t_url, '/')"/>
        <xsl:variable name="last">
          <xsl:call-template name="truncate_find_last_token">
            <xsl:with-param name="t_url" select="$t_url"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="path_limit" select="$truncate_result_url_length - (string-length($first) + string-length($last) + 1)"/>

        <xsl:choose>
          <xsl:when test="$path_limit &lt;= 0">
            <xsl:value-of select="concat(substring($t_url, 1, $truncate_result_url_length), '...')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:variable name="chopped_path">
              <xsl:call-template name="truncate_chop_path">
                <xsl:with-param name="path" select="substring($t_url, string-length($first) + 2, string-length($t_url) - (string-length($first) + string-length($last) + 1))"/>
                <xsl:with-param name="path_limit" select="$path_limit"/>
              </xsl:call-template>
            </xsl:variable>
            <xsl:value-of select="concat($first, '/.../', $chopped_path, $last)"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <xsl:template name="truncate_find_last_token">
    <xsl:param name="t_url"/>

    <xsl:choose>
      <xsl:when test="contains($t_url, '/')">
        <xsl:call-template name="truncate_find_last_token">
          <xsl:with-param name="t_url" select="substring-after($t_url, '/')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$t_url"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <xsl:template name="truncate_chop_path">
    <xsl:param name="path"/>
    <xsl:param name="path_limit"/>

    <xsl:choose>
      <xsl:when test="string-length($path) &lt;= $path_limit">
        <xsl:value-of select="$path"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="truncate_chop_path">
          <xsl:with-param name="path" select="substring-after($path, '/')"/>
          <xsl:with-param name="path_limit" select="$path_limit"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>


  <!-- **********************************************************************
  A single result (do not customize)
     ********************************************************************** -->
  <xsl:template match="R">
    <xsl:param name="query"/>

    <xsl:variable name="protocol"     select="substring-before(U, '://')"/>
    <xsl:variable name="temp_url"     select="substring-after(U, '://')"/>
    <xsl:variable name="display_url1" select="substring-after(UD, '://')"/>
    <xsl:variable name="escaped_url"  select="substring-after(UE, '://')"/>

    <xsl:variable name="display_url2">
      <xsl:choose>
        <xsl:when test="$display_url1">
          <xsl:value-of select="$display_url1"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$temp_url"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="display_url">
      <xsl:choose>
        <xsl:when test="$protocol='unc'">
          <xsl:call-template name="convert_unc">
            <xsl:with-param name="string" select="$display_url2"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$display_url2"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="stripped_url">
      <xsl:choose>
        <xsl:when test="$truncate_result_urls != '0'">
          <xsl:call-template name="truncate_url">
            <xsl:with-param name="t_url" select="$display_url"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$display_url"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="crowded_url" select="HN/@U"/>
    <xsl:variable name="crowded_display_url1" select="HN"/>
    <xsl:variable name="crowded_display_url">
      <xsl:choose>
        <xsl:when test="$protocol='unc'">
          <xsl:call-template name="convert_unc">
            <xsl:with-param name="string" select="substring-after($crowded_display_url1,'://')"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$crowded_display_url1"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="lower" select="'abcdefghijklmnopqrstuvwxyz'"/>
    <xsl:variable name="upper" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>

    <xsl:variable name="url_indexed" select="not(starts-with($temp_url, 'noindex!/'))"/>

    <!-- *** Indent as required (only supports 2 levels) *** -->


    <!-- *** SPSMod: Format Indenting like Sharepoint *** -->
    <xsl:if test="$show_sps_linebreaks = '0'">
      <xsl:if test="@L='2'">
        <xsl:text disable-output-escaping="yes">&lt;blockquote class=&quot;g&quot;&gt;</xsl:text>
      </xsl:if>
    </xsl:if>


    <!-- *** Result Header *** -->
    <p class="g">
      <!-- *** SPSMod:  Google Sites icon *** -->
      <xsl:if test="starts-with($stripped_url, 'sites.google.com/')">
        <img src="http://www.google.com/sites/images/sites_favicon.ico"/>&#xA0;
      </xsl:if>

      <!-- *** SPSMod:  SPS Icons  **** -->

      <!--amit has commented this -->

      <xsl:if test="$show_sps_icons != '0'">
        <xsl:choose>
          <xsl:when test="MT[@N='google:objecttype']/@V='Wiki Page'">
            <img src="/_layouts/images/wiki.png" height='16' width='43' alt="Wiki Page"></img>
          </xsl:when>
        </xsl:choose>

        <xsl:if test="contains($stripped_url, '/Lists/Links/')">
          <img src="/_layouts/images/STS_List_Links16.gif"/>&#xA0;
        </xsl:if>

        <xsl:if test="contains($stripped_url, '/Lists/Tasks/')">
          <img src="/_layouts/images/STS_List_Tasks16.gif"/>&#xA0;
        </xsl:if>

        <xsl:if test="contains($stripped_url, '/Lists/Calendar/')">
          <img src="/_layouts/images/STS_List_Events16.gif"/>&#xA0;
        </xsl:if>

      </xsl:if>




      <!-- *** SPSMod:  Result Title (including PDF tag and hyperlink) *** -->

      <xsl:if test="$show_res_title != '0'">
        <font size="-2">
          <b>

            <!--Amit has modified this to avoid additional errors while displaying the search results-->

            <xsl:choose>

              <xsl:when test="@MIME='text/html' or @MIME='' or not(@MIME)"></xsl:when>
              <xsl:when test="@MIME='text/plain'">
                <img src="/_layouts/images/iclog.gif" alt="Text Doc"></img>
              </xsl:when>
              <xsl:when test="@MIME='application/rtf'">
                <img src="/_layouts/images/icrtf.gif" alt="Richtext Doc"></img>
              </xsl:when>
              <!--
      <xsl:when test="@MIME='application/pdf'"><img src="/_layouts/images/icpdf.gif" alt="PDF"></img></xsl:when>
      -->
              <xsl:when test="@MIME='application/postscript'">[PS]</xsl:when>
              <xsl:when test="@MIME='application/vnd.ms-powerpoint'">
                <img src="/_layouts/images/icpot.gif" alt="MS Powerpoint"></img>
              </xsl:when>
              <xsl:when test="@MIME='application/vnd.ms-excel'">
                <img src="/_layouts/images/icxls.gif" alt="MS Excel"></img>
              </xsl:when>
              <xsl:when test="@MIME='application/msword'">
                <img src="/_layouts/images/icdoc.gif" alt="MS Word"></img>
              </xsl:when>


              <xsl:otherwise>
                <xsl:variable name="extension">
                  <xsl:call-template name="last_substring_after">
                    <xsl:with-param name="string" select="substring-after(
                                                  $temp_url,
                                                  '/')"/>
                    <xsl:with-param name="separator" select="'.'"/>
                    <xsl:with-param name="fallback" select="'UNKNOWN'"/>
                  </xsl:call-template>
                </xsl:variable>
                [<xsl:value-of select="translate($extension,$lower,$upper)"/>]
              </xsl:otherwise>
            </xsl:choose>

          </b>
        </font>
        <xsl:text> </xsl:text>

        <xsl:variable name="link"
         select="$url_indexed and not(starts-with(U, $googleconnector_protocol))"/>

        <xsl:if test="$link">

          <xsl:text disable-output-escaping='yes'>&lt;a href="</xsl:text>

          <xsl:choose>
            <xsl:when test="starts-with(U, $db_url_protocol)">
              <xsl:value-of disable-output-escaping='yes'
                            select="concat('db/', $temp_url)"/>
            </xsl:when>
            <!-- *** URI for smb or NFS must be escaped because it appears in the URI query *** -->
            <xsl:when test="$protocol='nfs' or $protocol='smb'">
              <xsl:value-of disable-output-escaping='yes'
                            select="concat($protocol,'/',$temp_url)"/>
            </xsl:when>
            <xsl:when test="$protocol='unc'">
              <xsl:value-of disable-output-escaping='yes' select="concat('file://', $display_url2)"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of disable-output-escaping='yes' select="U"/>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:text disable-output-escaping='yes'>"&gt;</xsl:text>
        </xsl:if>
        <span class="b">
          <xsl:choose>
            <xsl:when test="T">
              <xsl:call-template name="reformat_keyword">
                <xsl:with-param name="orig_string" select="T"/>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$stripped_url"/>
            </xsl:otherwise>
          </xsl:choose>
        </span>
        <xsl:if test="$link">
          <xsl:text disable-output-escaping='yes'>&lt;/a&gt;</xsl:text>
        </xsl:if>
      </xsl:if>


      <!-- *** Snippet Box *** -->
      <table cellpadding="0" cellspacing="0" border="0">
        <tr>
          <td class="s">
            <xsl:if test="$show_res_snippet != '0'">
              <xsl:call-template name="reformat_keyword">
                <xsl:with-param name="orig_string" select="S"/>
              </xsl:call-template>
            </xsl:if>

            <!-- *** Meta tags *** -->
            <xsl:if test="$show_meta_tags != '0'">
              <xsl:apply-templates select="MT"/>
            </xsl:if>


            <!-- *** SPSMod:  Create HR breaks like Sharepoint *** -->
            <xsl:if test="$show_sps_linebreaks != '0'">
              <hr style="color:#D6D6D6;background-color:#D6D6D6;height:1px;border:none;"></hr>
            </xsl:if>
            <xsl:if test="$show_sps_linebreaks = '0'">
              <br/>
            </xsl:if>

            <!-- *** URL *** -->

            <font color="{$res_url_color}" size="{$res_url_size}">
              <xsl:choose>
                <xsl:when test="not($url_indexed)">
                  <xsl:if test="($show_res_size!='0') or
                        ($show_res_date!='0') or
                        ($show_res_cache!='0')">
                    <xsl:text>Not Indexed:</xsl:text>
                    <xsl:value-of select="$stripped_url"/>
                  </xsl:if>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:if test="$show_res_url != '0'">
                    <xsl:value-of select="$stripped_url"/>
                  </xsl:if>
                </xsl:otherwise>
              </xsl:choose>
            </font>

            <!-- *** Miscellaneous (- size - date - cache) *** -->
            <xsl:if test="$url_indexed">
              <xsl:apply-templates select="HAS/C">
                <xsl:with-param name="stripped_url" select="$stripped_url"/>
                <xsl:with-param name="escaped_url" select="$escaped_url"/>
                <xsl:with-param name="query" select="$query"/>
                <xsl:with-param name="mime" select="@MIME"/>
                <xsl:with-param name="date" select="FS[@NAME='date']/@VALUE"/>
              </xsl:apply-templates>
            </xsl:if>


            <!-- *** Link to more links from this site *** -->
            <xsl:if test="HN">
              <br/>
              [
              <a class="f" href="GSASearchresults.aspx?as_sitesearch={$crowded_url}&amp;{
            encode-for-uri($search_url)}">
                More results from <xsl:value-of select="$crowded_display_url"/>
              </a>
              ]



              <!-- *** Link to aggregated results from database source *** -->
              <xsl:if test="starts-with($crowded_url, $db_url_protocol)">
                [
                <a class="f" href="dbaggr?sitesearch={$crowded_url}&amp;{
          $search_url}">View all data</a>
                ]
              </xsl:if>
            </xsl:if>


            <!-- *** Result Footer *** -->
            <xsl:if test="$show_sps_linebreaks != '0'">
              <br/>
              <br/>
            </xsl:if>

          </td>
        </tr>
      </table>
    </p>

    <!-- *** End indenting as required (only supports 2 levels) *** -->

    <xsl:if test="$show_sps_linebreaks = '0'">
      <xsl:if test="@L='2'">
        <xsl:text disable-output-escaping="yes">&lt;/blockquote&gt;</xsl:text>
      </xsl:if>
    </xsl:if>


  </xsl:template>

  <!-- **********************************************************************
  Meta tag values within a result (do not customize)
     ********************************************************************** -->
  <xsl:template match="MT">
    <br/>
    <span class="f">
      <xsl:value-of select="@N"/>:
    </span>
    <xsl:value-of select="@V"/>
  </xsl:template>

  <!-- **********************************************************************
  A single keymatch result (do not customize)
     ********************************************************************** -->
  <xsl:template match="GM">
    <p>
      <table cellpadding="4" cellspacing="0" border="0" height="40" width="100%">
        <tr>
          <td nowrap="0" bgcolor="{$keymatch_bg_color}" height="40">
            <a href="{GL}">
              <xsl:value-of select="GD"/>
            </a>
            <br/>
            <font size="-1" color="{$res_url_color}">
              <span class="a">
                <xsl:value-of select="GL"/>
              </span>
            </font>
          </td>
          <td bgcolor="{$keymatch_bg_color}" height="40"
            align="right" valign="top">
            <b>
              <font size="-1" color="{$keymatch_text_color}">
                <xsl:value-of select="$keymatch_text"/>
              </font>
            </b>
          </td>
        </tr>
      </table>
    </p>
  </xsl:template>


  <!-- **********************************************************************
  Variables for reformatting keyword-match display (do not customize)
     ********************************************************************** -->
  <xsl:variable name="keyword_orig_start" select="'&lt;b&gt;'"/>
  <xsl:variable name="keyword_orig_end" select="'&lt;/b&gt;'"/>

  <xsl:variable name="keyword_reformat_start">
    <xsl:if test="$res_keyword_format">
      <xsl:text>&lt;</xsl:text>
      <xsl:value-of select="$res_keyword_format"/>
      <xsl:text>&gt;</xsl:text>
    </xsl:if>
    <xsl:if test="($res_keyword_size) or ($res_keyword_color)">
      <xsl:text>&lt;font</xsl:text>
      <xsl:if test="$res_keyword_size">
        <xsl:text> size="</xsl:text>
        <xsl:value-of select="$res_keyword_size"/>
        <xsl:text>"</xsl:text>
      </xsl:if>
      <xsl:if test="$res_keyword_color">
        <xsl:text> color="</xsl:text>
        <xsl:value-of select="$res_keyword_color"/>
        <xsl:text>"</xsl:text>
      </xsl:if>
      <xsl:text>&gt;</xsl:text>
    </xsl:if>
  </xsl:variable>

  <xsl:variable name="keyword_reformat_end">
    <xsl:if test="($res_keyword_size) or ($res_keyword_color)">
      <xsl:text>&lt;/font&gt;</xsl:text>
    </xsl:if>
    <xsl:if test="$res_keyword_format">
      <xsl:text>&lt;/</xsl:text>
      <xsl:value-of select="$res_keyword_format"/>
      <xsl:text>&gt;</xsl:text>
    </xsl:if>
  </xsl:variable>

  <!-- **********************************************************************
  Reformat the keyword match display in a title/snippet string
     (do not customize)
     ********************************************************************** -->
  <xsl:template name="reformat_keyword">
    <xsl:param name="orig_string"/>

    <xsl:variable name="reformatted_1">
      <xsl:call-template name="replace_string">
        <xsl:with-param name="find" select="$keyword_orig_start"/>
        <xsl:with-param name="replace" select="$keyword_reformat_start"/>
        <xsl:with-param name="string" select="$orig_string"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="reformatted_2">
      <xsl:call-template name="replace_string">
        <xsl:with-param name="find" select="$keyword_orig_end"/>
        <xsl:with-param name="replace" select="$keyword_reformat_end"/>
        <xsl:with-param name="string" select="$reformatted_1"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:value-of disable-output-escaping='yes' select="$reformatted_2"/>

  </xsl:template>


  <!-- **********************************************************************
  Helper templates for generating a result item (do not customize)
     ********************************************************************** -->

  <!-- *** Miscellaneous: - size - date - cache *** -->
  <xsl:template match="C">
    <xsl:param name="stripped_url"/>
    <xsl:param name="escaped_url"/>
    <xsl:param name="query"/>
    <xsl:param name="mime"/>
    <xsl:param name="date"/>

    <xsl:variable name="docid">
      <xsl:value-of select="@CID"/>
    </xsl:variable>

    <xsl:if test="$show_res_size != '0'">
      <xsl:if test="not(@SZ='')">
        <font color="{$res_url_color}" size="{$res_url_size}">
          <xsl:text> - </xsl:text>
          <xsl:value-of select="@SZ"/>
        </font>
      </xsl:if>
    </xsl:if>

    <xsl:if test="$show_res_date != '0'">
      <xsl:if test="($date != '')"> 
        <font color="{$res_url_color}" size="{$res_url_size}">
          <xsl:text> - </xsl:text>
          <xsl:value-of select="$date"/>
        </font>
      </xsl:if>
    </xsl:if>

    <xsl:if test="$show_res_cache != '0'">
      <font color="{$res_url_color}" size="{$res_url_size}">
        <xsl:text> - </xsl:text>
      </font>
      <xsl:variable name="cache_encoding">
        <xsl:choose>
          <xsl:when test="'' != @ENC">
            <xsl:value-of select="@ENC"/>
          </xsl:when>
          <xsl:otherwise>UTF-8</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <a class="f" href="GSASearchresults.aspx?k=cache:{$docid}:{$escaped_url}+{
                           $stripped_search_query}&amp;{$base_url}&amp;oe={
                           $cache_encoding}">
        <xsl:choose>
          <xsl:when test="not($mime)">Cached</xsl:when>
          <xsl:when test="$mime='text/html'">Cached</xsl:when>
          <xsl:when test="$mime='text/plain'">Cached</xsl:when>
          <xsl:otherwise>Text Version</xsl:otherwise>
        </xsl:choose>
      </a>
    </xsl:if>

  </xsl:template>


  <!-- **********************************************************************
 Google navigation bar in result page (do not customize)
     ********************************************************************** -->
  <xsl:template name="google_navigation">
    <xsl:param name="prev"/>
    <xsl:param name="next"/>
    <xsl:param name="view_begin"/>
    <xsl:param name="view_end"/>
    <xsl:param name="guess"/>
    <xsl:param name="navigation_style"/>
	<xsl:param name="dynamic_nav_bar"/>


    <xsl:variable name="fontclass">
      <xsl:choose>
        <xsl:when test="$navigation_style = 'top'">s</xsl:when>
        <xsl:otherwise>b</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- *** Test to see if we should even show navigation *** -->
    <xsl:if test="($prev) or ($next)">

      <!-- *** Start Google result navigation bar *** -->

      <xsl:if test="$navigation_style != 'top'">
        <xsl:text disable-output-escaping="yes">&lt;center&gt;
        &lt;div class=&quot;n&quot;&gt;</xsl:text>
      </xsl:if>

      <table border="0" cellpadding="0" width="1%" cellspacing="0">
        <tr align="center" valign="top">
          <xsl:if test="$navigation_style != 'top'">
            <td valign="bottom" nowrap="1">
              <font size="-1">
                Result Page<xsl:call-template name="nbsp"/>
              </font>
            </td>
          </xsl:if>


          <!-- *** Show previous navigation, if available *** -->
          <xsl:choose>
            <xsl:when test="$prev">
              <td nowrap="1">

                <span class="b">
                  <a href="GSASearchresults.aspx?{encode-for-uri($search_url)}&amp;start={$view_begin -
                      $num_results - 1}">
                    <xsl:if test="$navigation_style = 'google'">

                      <img src="/_layouts/images/PLPLAYR2.GIF" width="68" height="26"
                        alt="Previous" border="0"/>
                      <br/>
                    </xsl:if>
                    <xsl:if test="$navigation_style = 'top'">
                      <xsl:text>&lt;</xsl:text>
                    </xsl:if>
                    <xsl:text>Previous</xsl:text>
                  </a>
                </span>
                <xsl:if test="$navigation_style != 'google'">
                  <xsl:call-template name="nbsp"/>
                </xsl:if>
              </td>
            </xsl:when>
            <xsl:otherwise>
              <td nowrap="1">
                <xsl:if test="$navigation_style = 'google'">
                  <img src="/_layouts/images/PLPREV2.GIF" width="18" height="26"
                    alt="First" border="0"/>
                  <br/>
                </xsl:if>
              </td>
            </xsl:otherwise>
          </xsl:choose>

          <xsl:if test="($navigation_style = 'google') or
                      ($navigation_style = 'link')">
            <!-- *** Google result set navigation *** -->
            <xsl:variable name="mod_end">
              <xsl:choose>
                <xsl:when test="$next">
                  <xsl:value-of select="$guess"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$view_end"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>

            <xsl:call-template name="result_nav">
              <xsl:with-param name="start" select="0"/>
              <xsl:with-param name="end" select="$mod_end"/>
              <xsl:with-param name="current_view" select="($view_begin)-1"/>
              <xsl:with-param name="navigation_style" select="$navigation_style"/>
            </xsl:call-template>
          </xsl:if>

          <!-- *** Show next navigation, if available *** -->
          <xsl:choose>
            <xsl:when test="$next">
              <td nowrap="1">
                <xsl:if test="$navigation_style != 'google'">
                  <xsl:call-template name="nbsp"/>
                </xsl:if>
                <span class="b">
                  <a   href="GSASearchresults.aspx?{encode-for-uri($search_url)}&amp;start={$view_begin +
                $num_results - 1}">
                    <xsl:if test="$navigation_style = 'google'">

                      <img src="/_layouts/images/PLPLAY2.GIF" width="100" height="26"

                        alt="Next" border="0"/>
                      <br/>
                    </xsl:if>
                    <xsl:text>Next</xsl:text>
                    <xsl:if test="$navigation_style = 'top'">
                      <xsl:text>&gt;</xsl:text>
                    </xsl:if>
                  </a>
                </span>
              </td>
            </xsl:when>
            <xsl:otherwise>
              <td nowrap="1">
                <xsl:if test="$navigation_style != 'google'">
                  <xsl:call-template name="nbsp"/>
                </xsl:if>
                <xsl:if test="$navigation_style = 'google'">
                  <img src="/_layouts/images/PLNEXT2.GIF" width="46" height="26"

                    alt="Last" border="0"/>
                  <br/>
                </xsl:if>
              </td>
            </xsl:otherwise>
          </xsl:choose>

          <!-- *** End Google result bar *** -->
        </tr>
      </table>

      <xsl:if test="$navigation_style != 'top'">
        <xsl:text disable-output-escaping="yes">&lt;/div&gt;
        &lt;/center&gt;</xsl:text>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <!-- **********************************************************************
 Helper templates for generating Google result navigation (do not customize)
   only shows 10 sets up or down from current view
     ********************************************************************** -->
  <xsl:template name="result_nav">
    <xsl:param name="start" select="'0'"/>
    <xsl:param name="end"/>
    <xsl:param name="current_view"/>
    <xsl:param name="navigation_style"/>

    <!-- *** Choose how to show this result set *** -->
    <xsl:choose>
      <xsl:when test="($start)&lt;(($current_view)-(10*($num_results)))">
      </xsl:when>
      <xsl:when test="(($current_view)&gt;=($start)) and
                    (($current_view)&lt;(($start)+($num_results)))">
        <td>
          <xsl:if test="$navigation_style = 'google'">
            <img src="/_layouts/images/PLSTOP2.GIF" width="16" height="26" alt="Current"/>
            <br/>
          </xsl:if>
          <xsl:if test="$navigation_style = 'link'">
            <xsl:call-template name="nbsp"/>
          </xsl:if>
          <span class="i">
            <xsl:value-of
          select="(($start)div($num_results))+1"/>
          </span>
          <xsl:if test="$navigation_style = 'link'">
            <xsl:call-template name="nbsp"/>
          </xsl:if>
        </td>
      </xsl:when>
      <xsl:otherwise>
        <td>
          <xsl:if test="$navigation_style = 'link'">
            <xsl:call-template name="nbsp"/>
          </xsl:if>
          <a href="GSASearchresults.aspx?{encode-for-uri($search_url)}&amp;start={$start}">
            <xsl:if test="$navigation_style = 'google'">
              <img src="/_layouts/images/SEARCH.GIF" width="16" height="26" alt="Navigation"
                   border="0"/>
              <br/>
            </xsl:if>
            <xsl:value-of select="(($start)div($num_results))+1"/>
          </a>
          <xsl:if test="$navigation_style = 'link'">
            <xsl:call-template name="nbsp"/>
          </xsl:if>
        </td>
      </xsl:otherwise>
    </xsl:choose>

    <!-- *** Recursively iterate through result sets to display *** -->
    <xsl:if test="((($start)+($num_results))&lt;($end)) and
                ((($start)+($num_results))&lt;(($current_view)+
                (10*($num_results))))">
      <xsl:call-template name="result_nav">
        <xsl:with-param name="start" select="$start+$num_results"/>
        <xsl:with-param name="end" select="$end"/>
        <xsl:with-param name="current_view" select="$current_view"/>
        <xsl:with-param name="navigation_style" select="$navigation_style"/>
      </xsl:call-template>
    </xsl:if>

  </xsl:template>


  <!-- **********************************************************************
 Top separation bar (do not customize)
     ********************************************************************** -->
  <xsl:template name="top_sep_bar">
    <xsl:param name="text"/>
    <xsl:param name="show_info"/>
    <xsl:param name="time"/>


    <table width="100%" border="0" cellpadding="0" cellspacing="0">
      <tr>
        <td bgcolor="{$sep_bar_border_color}">
          <img width="1" height="1" alt=""/>
        </td>
      </tr>
    </table>
    <table width="100%" cellpadding="0" cellspacing="0" border="0" bgcolor="{$sep_bar_bg_color}">
      <tr>
        <td nowrap="1" width="1%" bgcolor="{$sep_bar_bg_color}">
          <font size="+1">
            <xsl:call-template name="nbsp"/>
            <b>
              <xsl:value-of select="$text"/>
            </b>
          </font>
        </td>
        <td nowrap="1" align="right" bgcolor="{$sep_bar_bg_color}">

          <xsl:if test="$show_info != '0'">
            <!-- SPSMod:   Resizing -->
            <font size="-4">
              <xsl:if test="count(/GSP/RES/R)>0 ">
                <xsl:choose>
                  <xsl:when test="$access = 's' or $access = 'a'">
                    Results <b>
                      <xsl:value-of select="RES/@SN"/>
                    </b> - <b>
                      <xsl:value-of select="RES/@EN"/>
                    </b> for <b>
                      <xsl:value-of select="$space_normalized_query"/>
                    </b>.
                  </xsl:when>
                  <xsl:otherwise>
                    Results <b>
                      <xsl:value-of select="RES/@SN"/>
                    </b> - <b>
                      <xsl:value-of select="RES/@EN"/>
                    </b> of about <b>
                      <xsl:value-of select="RES/M"/>
                    </b> for <b>
                      <xsl:value-of select="$space_normalized_query"/>
                    </b>.
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:if>
              Search took <b>
                <xsl:value-of select="round($time * 100.0) div 100.0"/>
              </b> seconds.
            </font>
          </xsl:if>
        </td>
      </tr>
    </table>
    <hr class="z"/>
    <xsl:if test="$choose_sep_bar = 'line'">
      <hr size="1" color="gray"/>
    </xsl:if>
  </xsl:template>

  <!-- **********************************************************************
 Analytics script (do not customize)
     ********************************************************************** -->
  <xsl:template name="analytics">
    <xsl:if test="string-length($analytics_account) != 0">
      <script src="{$analytics_script_url}" type="text/javascript"></script>
      <script type="text/javascript">
        <xsl:comment>
          _uacct = "<xsl:value-of select='$analytics_account'/>";
          urchinTracker();
          //
        </xsl:comment>
      </script>
    </xsl:if>
  </xsl:template>

  <!-- **********************************************************************
 Utility function for constructing copyright text (do not customize)
     ********************************************************************** -->
  <xsl:template name="copyright">
    <center>
      <br/>
      <br/>
      <p>
        <font face="arial,sans-serif" size="-1" color="#2f2f2f">
          Powered by Google Search Appliance
        </font>
      </p>
    </center>
  </xsl:template>


  <!-- **********************************************************************
 Utility functions for generating html entities
     ********************************************************************** -->
  <xsl:template name="nbsp">
    <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
  </xsl:template>
  <xsl:template name="nbsp3">
    <xsl:call-template name="nbsp"/>
    <xsl:call-template name="nbsp"/>
    <xsl:call-template name="nbsp"/>
  </xsl:template>
  <xsl:template name="nbsp4">
    <xsl:call-template name="nbsp3"/>
    <xsl:call-template name="nbsp"/>
  </xsl:template>
  <xsl:template name="quot">
    <xsl:text disable-output-escaping="yes">&amp;quot;</xsl:text>
  </xsl:template>
  <xsl:template name="rsaquo">
  <dfn><xsl:text disable-output-escaping="yes">&amp;#8250;</xsl:text></dfn>
</xsl:template>
  <xsl:template name="copy">
    <xsl:text disable-output-escaping="yes">&amp;copy;</xsl:text>
  </xsl:template>
  <xsl:template name="endash">
  <xsl:text disable-output-escaping="yes">&amp;#8211;</xsl:text>
</xsl:template>

  <!-- **********************************************************************
 Utility functions for generating head elements so that the XSLT processor
 won't add a meta tag to the output, since it may specify the wrong
 encoding (utf8) in the meta tag.
     ********************************************************************** -->
  <xsl:template name="plainHeadStart">
    <xsl:text disable-output-escaping="yes">&lt;head&gt;</xsl:text>
    <meta name="robots" content="NOINDEX,NOFOLLOW"/>
    <xsl:text>
  </xsl:text>
  </xsl:template>
  <xsl:template name="plainHeadEnd">
    <xsl:text disable-output-escaping="yes">&lt;/head&gt;</xsl:text>
    <xsl:text>
  </xsl:text>
  </xsl:template>


  <!-- **********************************************************************
 Utility functions for generating head elements with a meta tag to the output
 specifying the character set as requested
     ********************************************************************** -->
  <xsl:template name="langHeadStart">
    <xsl:text disable-output-escaping="yes">&lt;head&gt;</xsl:text>
    <meta name="robots" content="NOINDEX,NOFOLLOW"/>
    <xsl:choose>
      <xsl:when test="PARAM[(@name='oe') and (@value='utf8')]">
        <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='oe') and (@value!='')]">
        <meta http-equiv="content-type" content="text/html; charset={PARAM[@name='oe']/@value}"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_zh-CN')]">
        <meta http-equiv="content-type" content="text/html; charset=GB2312"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_zh-TW')]">
        <meta http-equiv="content-type" content="text/html; charset=Big5"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_cs')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-2"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_da')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_nl')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_en')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_et')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_fi')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_fr')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_de')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_el')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-7"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_iw')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-8-I"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_hu')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-2"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_is')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_it')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_ja')]">
        <meta http-equiv="content-type" content="text/html; charset=Shift_JIS"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_ko')]">
        <meta http-equiv="content-type" content="text/html; charset=EUC-KR"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_lv')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_lt')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_no')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_pl')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-2"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_pt')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_ro')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-2"/>
      </xsl:when>

      <xsl:when test="PARAM[(@name='lr') and (@value='lang_ru')]">
        <meta http-equiv="content-type" content="text/html; charset=windows-1251"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_es')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1"/>
      </xsl:when>
      <xsl:when test="PARAM[(@name='lr') and (@value='lang_sv')]">
        <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1"/>
      </xsl:when>
      <xsl:otherwise>
        <meta http-equiv="content-type" content="text/html; charset="/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>
  </xsl:text>
  </xsl:template>

  <xsl:template name="langHeadEnd">
    <xsl:text disable-output-escaping="yes">&lt;/head&gt;</xsl:text>
    <xsl:text>
  </xsl:text>
  </xsl:template>


  <!-- **********************************************************************
 Utility functions (do not customize)
     ********************************************************************** -->

  <!-- *** Find the substring after the last occurence of a separator *** -->
  <xsl:template name="last_substring_after">

    <xsl:param name="string"/>
    <xsl:param name="separator"/>
    <xsl:param name="fallback"/>

    <xsl:variable name="newString"
      select="substring-after($string, $separator)"/>

    <xsl:choose>
      <xsl:when test="$newString!=''">
        <xsl:call-template name="last_substring_after">
          <xsl:with-param name="string" select="$newString"/>
          <xsl:with-param name="separator" select="$separator"/>
          <xsl:with-param name="fallback" select="$newString"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$fallback"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <!-- *** Find and replace *** -->
  <xsl:template name="replace_string">
    <xsl:param name="find"/>
    <xsl:param name="replace"/>
    <xsl:param name="string"/>
    <xsl:choose>
      <xsl:when test="contains($string, $find)">
        <xsl:value-of select="substring-before($string, $find)"/>
        <xsl:value-of select="$replace"/>
        <xsl:call-template name="replace_string">
          <xsl:with-param name="find" select="$find"/>
          <xsl:with-param name="replace" select="$replace"/>
          <xsl:with-param name="string"
            select="substring-after($string, $find)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- *** Decode hex encoding *** -->
  <xsl:template name="decode_hex">
    <xsl:param name="encoded" />

    <xsl:variable name="hex" select="'0123456789ABCDEF'" />
    <xsl:variable name="ascii"> !"#$%&amp;'()*+,-./0123456789:;&lt;=&gt;?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~</xsl:variable>

    <xsl:choose>
      <xsl:when test="contains($encoded,'%')">
        <xsl:value-of select="substring-before($encoded,'%')" />
        <xsl:variable name="hexpair" select="translate(substring(substring-after($encoded,'%'),1,2),'abcdef','ABCDEF')" />
        <xsl:variable name="decimal" select="(string-length(substring-before($hex,substring($hexpair,1,1))))*16 + string-length(substring-before($hex,substring($hexpair,2,1)))" />
        <xsl:choose>
          <xsl:when test="$decimal &lt; 127 and $decimal &gt; 31">
            <xsl:value-of select="substring($ascii,$decimal - 31,1)" />
          </xsl:when>
          <xsl:when test="$decimal &gt; 159">
            <xsl:text disable-output-escaping="yes">%</xsl:text>
            <xsl:value-of select="$hexpair" />
          </xsl:when>
          <xsl:otherwise>?</xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="decode_hex">
          <xsl:with-param name="encoded" select="substring(substring-after($encoded,'%'),3)" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$encoded" />
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <!-- *** Convert UNC *** -->
  <xsl:template name="convert_unc">
    <xsl:param name="string"/>
    <xsl:variable name="slash">/</xsl:variable>
    <xsl:variable name="backslash">\</xsl:variable>
    <xsl:variable name="escaped_ampersand">&amp;amp;</xsl:variable>
    <xsl:variable name="unescaped_ampersand">&amp;</xsl:variable>

    <xsl:variable name="converted_1">
      <xsl:call-template name="replace_string">
        <xsl:with-param name="find"    select="$slash"/>
        <xsl:with-param name="replace" select="$backslash"/>
        <xsl:with-param name="string"  select="$string"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="converted_2">
      <xsl:call-template name="decode_hex">
        <xsl:with-param name="encoded" select="$converted_1"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="converted_3">
      <xsl:call-template name="replace_string">
        <xsl:with-param name="find"    select="$escaped_ampersand"/>
        <xsl:with-param name="replace" select="$unescaped_ampersand"/>
        <xsl:with-param name="string"  select="$converted_2"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:value-of disable-output-escaping='yes' select="concat($backslash,$backslash,$converted_3)"/>

  </xsl:template>

  <!-- **********************************************************************
 Display error messages
     ********************************************************************** -->
  <xsl:template name="error_page">
    <xsl:param name="errorMessage"/>
    <xsl:param name="errorDescription"/>

    <html>
      <xsl:call-template name="plainHeadStart"/>
      <title>
        <xsl:value-of select="$error_page_title"/>
      </title>
      <xsl:call-template name="style"/>
      <xsl:call-template name="plainHeadEnd"/>
      <body dir="ltr">
        <xsl:call-template name="analytics"/>

        <xsl:call-template name="my_page_header"/>

        <xsl:if test="$show_logo != '0'">
          <table border="0" cellpadding="0" cellspacing="0">
            <tr>
              <td rowspan="3" valign="top">
                <xsl:call-template name="logo"/>
                <xsl:call-template name="nbsp3"/>
              </td>
            </tr>
          </table>
        </xsl:if>

        <xsl:call-template name="top_sep_bar">
          <xsl:with-param name="text" select="$sep_bar_error_text"/>
          <xsl:with-param name="show_info" select="0"/>
          <xsl:with-param name="time" select="0"/>
        </xsl:call-template>

        <p>
          <table width="99%" border="0" cellpadding="2" cellspacing="0">
            <tr>
              <td>
                <font color="#990000" size="+1">Message:</font>
              </td>
              <td>
                <font color="#990000" size="+1">
                  <xsl:value-of select="$errorMessage"/>
                </font>
              </td>
            </tr>
            <tr>
              <td>
                <font color="#990000">Description:</font>
              </td>
              <td>
                <font color="#990000">
                  <xsl:value-of select="$errorDescription"/>
                </font>
              </td>
            </tr>
            <tr>
              <td>
                <font color="#990000">Details:</font>
              </td>
              <td>
                <font color="#990000">
                  <xsl:copy-of select="/"/>
                </font>
              </td>
            </tr>
          </table>
        </p>

        <hr/>
        <xsl:call-template name="copyright"/>
        <xsl:call-template name="my_page_footer"/>

      </body>
    </html>
  </xsl:template>


  <!-- **********************************************************************
 Google Desktop for Enterprise integration templates
     ********************************************************************** -->
  <xsl:template name="desktop_tab">

    <!-- *** Show the Google tabs *** -->

    <font size="-1">
      <a class="q" onClick="return window.qs?qs(this):1" href="http://www.google.com/search?q={$qval}">Web</a>
    </font>

    <xsl:call-template name="nbsp4"/>

    <font size="-1">
      <a class="q" onClick="return window.qs?qs(this):1" href="http://images.google.com/images?q={$qval}">Images</a>
    </font>

    <xsl:call-template name="nbsp4"/>

    <font size="-1">
      <a class="q" onClick="return window.qs?qs(this):1" href="http://groups.google.com/groups?q={$qval}">Groups</a>
    </font>

    <xsl:call-template name="nbsp4"/>

    <font size="-1">
      <a class="q" onClick="return window.qs?qs(this):1" href="http://news.google.com/news?q={$qval}">News</a>
    </font>

    <xsl:call-template name="nbsp4"/>

    <font size="-1">
      <a class="q" onClick="return window.qs?qs(this):1" href="http://local.google.com/local?q={$qval}">Local</a>
    </font>

    <xsl:call-template name="nbsp4"/>

    <!-- *** Show the desktop and web tabs *** -->

    <xsl:if test="CUSTOM/HOME">
      <xsl:comment>trh2</xsl:comment>
    </xsl:if>
    <xsl:if test="Q">
      <xsl:comment>trl2</xsl:comment>
    </xsl:if>

    <!-- *** Show the appliance tab *** -->
    <font size="-1">
      <b>
        <xsl:value-of select="$egds_appliance_tab_label"/>
      </b>
    </font>

  </xsl:template>

  <xsl:template name="desktop_results">
    <xsl:comment>tro2</xsl:comment>
  </xsl:template>

  <!-- **********************************************************************
  OneBox results (if any)
     ********************************************************************** -->
  <xsl:template name="onebox">
    <xsl:for-each select="/GSP/ENTOBRESULTS">
      <xsl:apply-templates/>
    </xsl:for-each>
  </xsl:template>

  <!-- **********************************************************************
 Swallow unmatched elements
     ********************************************************************** -->
  <xsl:template match="@*|node()"/>
</xsl:stylesheet>


<!-- *** END OF STYLESHEET *** -->
