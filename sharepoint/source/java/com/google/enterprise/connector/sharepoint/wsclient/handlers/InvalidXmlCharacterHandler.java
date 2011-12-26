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

package com.google.enterprise.connector.sharepoint.wsclient.handlers;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.SOAPPart;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.message.SOAPHeaderElement;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.soap.SOAPHeader;
import javax.xml.transform.stream.StreamSource;

/**
 * A message handler that can intercept any web service call (typically,
 * responses) before it reaches the client. The handler checks if there are
 * any invalid XML characters present in the response and filters out all such
 * characters as per the filter rules
 * <p/>
 * Refer Code Site Issue50
 *
 * @author nitendra_thakur
 */
public class InvalidXmlCharacterHandler extends BasicHandler {
  private static final Logger LOGGER = Logger.getLogger(InvalidXmlCharacterHandler.class.getName());

  /**
   * This header must be present in the request for parsing to be done
   */
  // TODO: remove this header and clean up the code in SPListsWS that 
  // sets and checks for the header.
  public final static SOAPHeaderElement PRECONDITION_HEADER = new org.apache.axis.message.SOAPHeaderElement(
      "http://sharepoint.connector.enterprise.google.com/handlers_v1",
      "InvalidXmlCharacterFilter");

  /**
   * All the parameters in Axis's globalconfiguration whose name starts with
   * this prefix will be treated as a pattern to be filtered out from the WS
   * response
   */
  private final static String rulesPrefix = "FilterPattern_";

  /**
   * User defined pattern to be filtered out
   */
  private Pattern customFilterPattern = null;

  /**
   * This is to identify any character entity references in the response. If the
   * reference is to an invalid character, it will be filtered out.
   */
  private final static Pattern referencesFilterPattern = Pattern.compile("&#(([0-9]+)|([xX]([0-9A-Fa-f]+)));");

  /**
   * All the replacements will be done using this value. Can be configured
   * through Axis configuration file .wsdd
   */
  private String replacementValue = " ";

  /**
   * The parameter name which contains the replacement value in Axis
   * configuration file
   */
  private final static String REPLACEMENT_VALUE = "ReplacementValue";

  /**
   * Intercepts WS responses for checking invalid characters
   */
  public void invoke(MessageContext messageContext) throws AxisFault {
    if (!checkPreconditions(messageContext)) {
      return;
    }

    // Load filter rules. Ideally, this should be done once per instance
    // but, that is not possible due to the current connector design.
    // wherein the Axis container is re-initialized during every WS call.
    initPatterns(messageContext);

    // Get the payload
    SOAPPart soapPart = null;
    String messagePayload = null;
    try {
      Message message = messageContext.getResponseMessage();
      soapPart = (SOAPPart) message.getSOAPPart();
      messagePayload = message.getSOAPPartAsString();
    } catch (Throwable t) {
      LOGGER.log(Level.WARNING, "Failed to get SOAPPart/messagePayload from the response. Returning...", t);
      return;
    }

    // Filter invalid references
    try {
      messagePayload = filterInvalidReferences(messagePayload);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Problem occured while filtering invalid references from WS response. ", e);
    }

    // Filter user defined patterns
    try {
      messagePayload = filterCustomPatterns(messagePayload);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Problem occured while filtering custom patterns from WS response. ", e);
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

  /**
   * Creates java Patterns for the user values specified in Axis configuration
   * and puts them in a Set to be processed later
   *
   * @param messageContext
   */
  void initPatterns(MessageContext messageContext) {
    if (null != customFilterPattern) {
      return;
    }
    Object obj = null;
    String strPattern = "";

    Iterator props = messageContext.getAllPropertyNames();
    while (props.hasNext()) {
      obj = props.next();
      if (null == obj) {
        continue;
      }
      String name = obj.toString();
      if (name.startsWith(rulesPrefix)) {
        try {
          Object pattern = messageContext.getProperty(name);
          if (null == pattern) {
            continue;
          }
          if (strPattern.trim().length() != 0) {
            strPattern += "|";
          }
          strPattern += "(" + pattern.toString() + ")";
        } catch (Exception e) {
          LOGGER.log(Level.WARNING, "Problem occured while adding filter [ "
              + name + " ] ", e);
        }
      }
    }

    if (null != strPattern && strPattern.trim().length() > 0) {
      strPattern = "(" + strPattern + ")";
      customFilterPattern = Pattern.compile(strPattern);
    }

    try {
      replacementValue = messageContext.getProperty(REPLACEMENT_VALUE).toString();
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Could not load replacement value from configuration; using deafult...", e);
    }
  }

  /**
   * Filter out all invalid references from the message payload
   *
   * @param messagePayload
   * @return resulting messagePayload after filtering out all invalid references
   */
  String filterInvalidReferences(String messagePayload) {
    if (null == referencesFilterPattern) {
      return messagePayload;
    }
    StringBuffer afterFilter = new StringBuffer();
    Matcher matcher = referencesFilterPattern.matcher(messagePayload);
    while (matcher.find()) {
      String decimal = matcher.group(2);
      String hexaDecimal = matcher.group(4);
      Integer ref = null;
      try {
        if (null != decimal) {
          ref = Integer.parseInt(decimal);
        } else if (null != hexaDecimal) {
          ref = Integer.parseInt(hexaDecimal, 16);
        } else {
          // This will never happen
          LOGGER.log(Level.SEVERE, "Matcher found an unexpected value [ "
              + matcher.group()
              + " ] in the matched reference. The value is neither decimal nor hexadecimal. ");
          continue;
        }
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, matcher.group(1) + " of " + matcher.group()
            + " is neinther a valid decimal or hexadecimal number! ", e);
        continue;
      }
      if (isInavlidReference(ref)) {
        LOGGER.info("replacing invalid reference " + matcher.group()
            + " from WS response. replacement value [ " + replacementValue
            + " ] ");
        matcher.appendReplacement(afterFilter, replacementValue);
      }
    }
    matcher.appendTail(afterFilter);
    return afterFilter.toString();
  }

  /**
   * Replaces String matching the custom patterns specified in Axis
   * configuration
   *
   * @param messagePayload
   * @return resulting messagePayload after filtering out all custom patterns
   */
  String filterCustomPatterns(String messagePayload) {
    if (null == customFilterPattern) {
      return messagePayload;
    }
    StringBuffer afterFilter = new StringBuffer();
    Matcher matcher = customFilterPattern.matcher(messagePayload);
    while (matcher.find()) {
      LOGGER.info("replacing " + matcher.group()
          + " from WS response.  replacement value [ " + replacementValue
          + " ] ");
      matcher.appendReplacement(afterFilter, replacementValue);
    }
    matcher.appendTail(afterFilter);
    return afterFilter.toString();
  }

  /**
   * Checks if a number refers to a valid XML characters. This has been derived
   * by experimenting with all the values in range 0 to 65533. Numbers not
   * falling in this range are not valid. Refer
   * http://www.w3.org/TR/html4/sgml/entities.html
   *
   * @param ref
   * @return
   */
  static boolean isInavlidReference(int ref) {
    if (ref <= 8 || (ref >= 11 && ref <= 12) || (ref >= 14 && ref <= 31)
        || (ref >= 55296 && ref <= 57343) || ref >= 65534) {
      return true;
    }
    return false;
  }

  /**
   * Checks all the pre-conditions before parsing is done. This mainly includes
   * verifying that the message context, response message and request message
   * is all valid.
   *
   * @param msgContext
   * @return true if all preconditions are satisfied; false otherwise
   */
  @SuppressWarnings("unchecked")
  static boolean checkPreconditions(MessageContext msgContext) {
    if (null == msgContext || null == msgContext.getResponseMessage()
        || null == msgContext.getRequestMessage()) {
      return false;
    }
    return true;
  }
}
