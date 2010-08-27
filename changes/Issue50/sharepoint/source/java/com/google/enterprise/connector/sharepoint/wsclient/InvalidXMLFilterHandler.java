//Copyright 2010 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.wsclient;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.SOAPPart;
import org.apache.axis.handlers.BasicHandler;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.stream.StreamSource;

/**
 * A message handler that can intercept any web service call (typically,
 * responses) before it reaches to the client. The handler checks if there are
 * any invalid XML characters present in the response and filter out all such
 * characters as per the filter rules
 *
 * @author nitendra_thakur
 */
public class InvalidXMLFilterHandler extends BasicHandler {
    private final Logger LOGGER = Logger.getLogger(InvalidXMLFilterHandler.class.getName());
    private static Map<Pattern, String> filterRules = new HashMap<Pattern, String>();

    public void invoke(MessageContext messageContext) throws AxisFault {
        if (null == messageContext) {
            return;
        }
        Message message = messageContext.getResponseMessage();
        if (null == message) {
            return;
        }

        // Get the payload
        SOAPPart soapPart = null;
        String messagePayload = null;
        try {
            soapPart = (SOAPPart) message.getSOAPPart();
            messagePayload = message.getSOAPPartAsString();
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "Failed to get SOAPPart/messagePayload from the response.", t);
        }

        // Apply filter rules
        for (Entry<Pattern, String> rule : filterRules.entrySet()) {
            Pattern pattern = rule.getKey();
            try {
                Matcher matcher = pattern.matcher(messagePayload);
                messagePayload = matcher.replaceAll(rule.getValue());
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "Failed to filter pattern [ "
                        + pattern + " ] ", t);
            }
        }

        // Update payload
        try {
            StreamSource source = (StreamSource) soapPart.getContent();
            source.setInputStream(new ByteArrayInputStream(
                    messagePayload.getBytes(soapPart.getEncoding())));
            soapPart.setContent(source);
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "Failed to update payload after filteration. ", t);
        }
    }

    public static void setFilterRules(Map<Pattern, String> filterRules) {
        InvalidXMLFilterHandler.filterRules = filterRules;
    }
}
