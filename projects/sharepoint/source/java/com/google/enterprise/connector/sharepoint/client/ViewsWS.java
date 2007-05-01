// Copyright 2007 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.client;

import com.google.enterprise.connector.sharepoint.generated.ViewsStub;
import com.google.enterprise.connector.sharepoint.generated.ListsStub.ViewFields_type14;
import com.google.enterprise.connector.sharepoint.generated.ViewsStub.GetView;
import com.google.enterprise.connector.sharepoint.generated.ViewsStub.GetViewCollection;
import com.google.enterprise.connector.sharepoint.generated.ViewsStub.GetViewResult_type0;
import com.google.enterprise.connector.sharepoint.generated.ViewsStub.ViewFields_type37;
import com.google.enterprise.connector.spi.SimpleValue;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * This class holds data and methods for any call to Views Web Service.
 *
 */
public class ViewsWS {
  private static final String viewsEndpoint = "/_vti_bin/Views.asmx";
  private SharepointClientContext sharepointClientContext;
  private String endpoint;
  private ViewsStub stub;
  
  // experimental code: keep the list of available viewFields for each list
  public static HashMap<String, ArrayList<String>> viewFields = 
    new HashMap<String, ArrayList<String>>();
  
  public static ViewFields_type14 getViewFields(String listName) {
    List<String> fields = viewFields.get(listName);
    if (fields == null) {
      return null;
    }
    return null; // TODO(bpurvy): need to implement this
  }
  
  public ViewsWS(SharepointClientContext sharepointClientContext)
      throws SharepointException {
    this.sharepointClientContext = sharepointClientContext;
    endpoint = "http://" + sharepointClientContext.getHost() + ":" +
        sharepointClientContext.getPort() +
        sharepointClientContext.getsiteName() + viewsEndpoint;
    try {
      stub = new ViewsStub(endpoint);
      sharepointClientContext.setStubWithAuth(stub, endpoint);
    } catch (AxisFault e) {
      throw new SharepointException(e.toString());
    }
  }

  public ViewsWS(SharepointClientContext sharepointClientContext,
      String siteName) throws SharepointException {
  this.sharepointClientContext = sharepointClientContext;
  endpoint = siteName + viewsEndpoint;
  try {
    stub = new ViewsStub(endpoint);
    sharepointClientContext.setStubWithAuth(stub, endpoint);
  } catch (AxisFault e) {
    throw new SharepointException(e.toString());
  }
}

  /**
   * Gets the viewFields for a given list. This is a partial (unfortunately!)
   * list of the metadata available for members of the list.
   * @param listName internal name of the list
   * @return  List of the viewFields on this list
   * @throws SharepointException
   */
  public List<String> getViewNames(String listName) throws SharepointException {
    ArrayList<String> listItems = new ArrayList<String>();
    String urlPrefix = "http://" + sharepointClientContext.getHost() + ":" +
        sharepointClientContext.getPort() + "/";

    ViewsStub.GetView req = new ViewsStub.GetView();
    req.setListName(listName);
    try {
      ViewsStub.GetViewResponse res = stub.GetView(req);
      try {
        XMLStreamReader reader = res.getGetViewResult().getPullParser(
            new QName("ViewFields"));
        boolean inViewFields = false;
        while (reader.hasNext()) {
          reader.next();
          if (reader.isStartElement()) {
            if (reader.getName().getLocalPart().equals("ViewFields")) {
              inViewFields = true;
              continue;
            }
            if (inViewFields &&
                reader.getName().getLocalPart().equals("FieldRef")) {
              listItems.add(reader.getAttributeValue(0));
            }
          } else if (reader.isEndElement()) {
            if (inViewFields) {
              if (reader.getName().getLocalPart().equals("ViewFields")) {
                inViewFields = false;
              }
            }
          }
        }
      } catch (XMLStreamException e) {
        throw new SharepointException(e.toString());
      }

    } catch (RemoteException e) {
      throw new SharepointException(e.toString());
    }
    viewFields.put(listName, listItems);
    return listItems;
  }

}
