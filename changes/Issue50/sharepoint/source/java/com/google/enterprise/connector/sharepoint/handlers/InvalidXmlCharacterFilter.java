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

package com.google.enterprise.connector.sharepoint.handlers;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.SOAPPart;
import org.apache.axis.handlers.BasicHandler;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
 * <p/>
 * Refer Code Site Issue50
 *
 * @author nitendra_thakur
 */
public class InvalidXmlCharacterFilter extends BasicHandler {
    private final Logger LOGGER = Logger.getLogger(InvalidXmlCharacterFilter.class.getName());

    /**
     * All the parameters in Axis's globalconfiguration whose name starts with
     * this prefix will be treated as a apttern to be filtered out from the WS
     * response
     */
    private final static String rulesPrefix = "FilterPattern_";

    /**
     * User defined pattern to be filtered out
     */
    private Set<Pattern> filterPatterns = new HashSet<Pattern>();

    /**
     * This is to identify any character entity references in the response. If
     * the reference is to an invalid character, it will be filtered out.
     */
    private final static Pattern referencesFilterPattern = Pattern.compile("&#([0-9]+);");

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
        if (null == messageContext) {
            return;
        }
        Message message = messageContext.getResponseMessage();
        if (null == message) {
            return;
        }

        // Load filter rules
        initPatterns(messageContext);

        // Get the payload
        SOAPPart soapPart = null;
        String messagePayload = null;
        try {
            soapPart = (SOAPPart) message.getSOAPPart();
            messagePayload = message.getSOAPPartAsString();
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "Failed to get SOAPPart/messagePayload from the response.", t);
        }

        if (null == messagePayload) {
            return;
        }

        // Filter invalid references
        messagePayload = filterInvalidReferences(messagePayload);

        // Filter user defined patterns
        if (null != filterPatterns && filterPatterns.size() > 0) {
            for (Pattern pattern : filterPatterns) {
                try {
                    Matcher matcher = pattern.matcher(messagePayload);
                    messagePayload = matcher.replaceAll(replacementValue);
                } catch (Throwable t) {
                    LOGGER.log(Level.WARNING, "Failed to filter pattern [ "
                            + pattern + " ] ", t);
                }
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

    /**
     * Creates java Patterns for the user values specified in Axis configuration
     * and puts them in a Set to be processed later
     *
     * @param messageContext
     */
    public void initPatterns(MessageContext messageContext) {
        Object obj = null;
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
                    if (null == obj) {
                        continue;
                    }
                    filterPatterns.add(Pattern.compile(pattern.toString()));
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Problem occured while adding filter [ " + name + " ] ", e);
                }
            }
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
     * @return
     */
    private String filterInvalidReferences(String messagePayload) {
        StringBuffer afterFilter = new StringBuffer();
        try {
            Matcher matcher = referencesFilterPattern.matcher(messagePayload);
            while (matcher.find()) {
                Integer ref = Integer.parseInt(matcher.group(1));
                if (isInavlidReference(ref)) {
                    matcher.appendReplacement(afterFilter, replacementValue);
                }
            }
            matcher.appendTail(afterFilter);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Problem occured while filtering invalid references from XML. returning the actual string... ", e);
            return messagePayload;
        }
        return afterFilter.toString();
    }

    /**
     * Checks if a number refers to a valid XML characters. This has been
     * derived by experimenting with all the values in range 0 to 65533. Numbers
     * not falling in this range are not valid. Refer
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
}
