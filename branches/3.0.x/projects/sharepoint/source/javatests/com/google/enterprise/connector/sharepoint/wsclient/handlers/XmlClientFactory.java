// Copyright 2012 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.wsclient.mock;

import com.google.enterprise.connector.sharepoint.client.ListsUtil;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.Folder;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.sharepoint.wsclient.client.AclWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.AlertsWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.BulkAuthorizationWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.ClientFactory;
import com.google.enterprise.connector.sharepoint.wsclient.client.ListsWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDataWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDiscoveryWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfile2003WS;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfile2007WS;
import com.google.enterprise.connector.sharepoint.wsclient.client.WebsWS;

import org.apache.axis.AxisFault;
import org.apache.commons.lang.StringUtils;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A mock factory for creating a Sharepoint repository from an XML file.
 *
 * The mock XML has the following format:
 *
 *  <!-- The document root node must be a Mock node. -->
 *  <Mock>
 *    <!-- A Mock node can only contain Web nodes. -->
 *    <!-- A Web node defines a mock website, it must specify the
 *         required attribute name. -->
 *    <Web name="WebAuth">
 *      <!-- A Web node can contain Web and List nodes. -->
 *      <!-- A List node defines a mock document list, it must specify the
 *           required attribute name. -->
 *      <List name="ListAt">
 *        <!-- A List node can contain Acl, Document and Folder nodes. -->
 *        <!-- An Acl node defines an Acl for the parent 
 *            (the list "ListAt" in this example), it must specify the
 *             required attribute name. An Acl node does not have any 
 *             child nodes. If an Acl is defined for a node, then only 
 *             that use has access to the object, if no Acl is defined 
 *             for a node then it's public and anyone can access it. -->
 *        <Acl name="good@example.com" />
 *        <!-- A Document node defines mock document, it must specify the
 *             required attribute name. A Document node can have child
 *             Acl nodes. -->
 *        <Document name="SampleDoc" />
 *        <!-- A Folder node defines mock folder, it must specify the
 *             required attribute name. -->
 *        <Folder name="SampleFolder">
 *          <!-- A Folder node can contain Acl, Document and Folder nodes. -->
 *          <Acl name="good@example.com" />
 *          <Document name="SampleDoc" />
 *          <Folder name="SampleFolder" />
 *        </Folder>
 *      </List>
 *      <Web name="Web1">
 *      </Web>
 *    </Web>
 *  </Mock>
 */
public class XmlClientFactory extends MockClientFactory {
  private static final Logger LOGGER =
      Logger.getLogger(XmlClientFactory.class.getName());

  private final MockItem root;

  public XmlClientFactory(String xmlFilePath) {
    XmlHandler handler = new XmlHandler();
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      saxParser.parse(xmlFilePath, handler);
    } catch (Exception e) {
      handler = null;
      LOGGER.log(Level.WARNING, "Unable to load mock xml.", e);
    }

    if (null != handler) {
      root = handler.getRoot();
    } else {
      root = null;
    }
  }

  public MockItem getRoot() {
    return root;
  }

  @Override
  public ListsWS getListsWS(final SharepointClientContext ctx,
      final String rowLimit) {
    return new XmlListsWS(this, ctx);
  }

  @Override
  public SiteDataWS getSiteDataWS(final SharepointClientContext ctx) {
    return new MockSiteDataWS(ctx) {
      @Override
      public List<ListState> getNamedLists(final WebState webstate)
          throws SharepointException {
        final String siteUrl = Util.getWebURLForWSCall(ctx.getSiteURL());
        final ArrayList<ListState> lists = new ArrayList<ListState>();
        try
        {
          appendLists(lists, siteUrl, root, webstate, ctx.getFeedType(), 
              "");
        } catch (AxisFault e) {
          // TODO: This catch should be removed when SiteDataWS gets updated
          // to use BaseWS.
        }
        LOGGER.info("Created " + lists.size() + " lists for URL "
            + siteUrl + ".");
        return lists;
      }

      @Override
      public SPDocument getSiteData(final WebState webState)
          throws SharepointException {
        // TODO: What do we need to return here?
        return null;
      }
    };
  }

  /**
   * Adds the child lists of {@link MockItem} to a list of {@link ListState}.
   *
   * @param lists The list collection to append the lists to
   * @param webUrl The username requesting access
   * @param item The {@link MockItem} that contains the documents to add 
   *        the list
   * @param ws The {@link WebState} to use for the new {@link ListState}
   * @param feedType The {@link FeedType} to use for the new documents
   * @param username The user requesting access
   * @throws AxisFault when the user is not authorized
   */
  private void appendLists(final ArrayList<ListState> lists,
      final String webUrl, final MockItem item, final WebState ws, 
      final FeedType feedType, String username)
      throws SharepointException, AxisFault {
    if (!item.hasPermission(username)) {
      throw new AxisFault(SPConstants.UNAUTHORIZED);
    }

    for (MockItem child : item.getChildren()) {
      if (MockType.List == child.getType()) {
        lists.add(createListState(webUrl, child.getName(), ws, feedType,
            true));
      } else if (MockType.Web == child.getType()) {
        final String childUrl = makeUrl(webUrl, child.getName());
        appendLists(lists, childUrl, child, ws, feedType, username);
      }
    }
  }

  /**
   * Returns a {@link MockItem} that has the specified Url.
   *
   * @param itemUrl The Url to lookup
   * @param username The user requesting access
   * @return a {@link MockItem} if found; null otherwise
   * @throws AxisFault when the user is not authorized
   */
  public MockItem getItemFromUrl(final String itemUrl, String username)
      throws AxisFault {
    if (!root.hasPermission(username)) {
      throw new AxisFault(SPConstants.UNAUTHORIZED);
    }

    String path;
    try {
      final URL url = new URL(itemUrl);
      path = StringUtils.strip(url.getPath(), "/ ");
    } catch (MalformedURLException e) {
      path = "";
    }

    MockItem item = root;
    for (String part : path.split("/")) {
      item = item.getChildByName(part);
      if (null == item) {
        break;
      }
      if (!item.hasPermission(username)) {
        throw new AxisFault(SPConstants.UNAUTHORIZED);
      }
    }

    return item;
  }
}
