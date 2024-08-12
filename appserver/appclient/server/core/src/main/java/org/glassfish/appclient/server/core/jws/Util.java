/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.appclient.server.core.jws;

import java.net.URI;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.grizzly.http.server.Request;

/**
 *
 * @author tjquinn
 */
public class Util {
    /**
     * pattern is: "${" followed by all chars excluding "}" followed by "}",
     * capturing into group 1 all chars between the "${" and the "}"
     */
    private static final Pattern TOKEN_SUBSTITUTION = Pattern.compile("\\$\\{([^\\}]*)\\}");
    private static final String SLASH_REPLACEMENT = Matcher.quoteReplacement("\\\\");
    private static final String DOLLAR_REPLACEMENT = Matcher.quoteReplacement("\\$");


    /**
     * Searches for placeholders of the form ${token-name} in the input String, retrieves
     * the property with name token-name from the Properties object, and (if
     * found) replaces the token in the input string with the property value.
     * @param s String possibly containing tokens
     * @param values Properties object containing name/value pairs for substitution
     * @return the original string with tokens substituted using their values
     * from the Properties object
     */
    public static String replaceTokens(String s, Properties values) {
        Matcher m = TOKEN_SUBSTITUTION.matcher(s);

        StringBuffer sb = new StringBuffer();
        /*
         * For each match, retrieve group 1 - the token - and use its value from
         * the Properties object (if found there) to replace the token with the
         * value.
         */
        while (m.find()) {
            String propertyName = m.group(1);
            String propertyValue = values.getProperty(propertyName);

            /*
             * Substitute only if the properties object contained a setting
             * for the placeholder we found.
             */
            if (propertyValue != null) {
                /*
                 * The next line quotes any $ signs and backslashes in the replacement string
                 * so they are not interpreted as meta-characters by the regular expression
                 * processor's appendReplacement.
                 */
                String adjustedPropertyValue =
                        propertyValue.replaceAll("\\\\",SLASH_REPLACEMENT).
                            replaceAll("\\$", DOLLAR_REPLACEMENT);
                String x = s.substring(m.start(),m.end());
                try {
                    m.appendReplacement(sb, adjustedPropertyValue);
                } catch (IllegalArgumentException iae) {
                    System.err.println("**** appendReplacement failed: segment is " + x + "; original replacement was " + propertyValue + " and adj. replacement is " + adjustedPropertyValue + "; exc follows");
                    throw iae;
                }
            }
        }
        /*
         * There are no more matches, so append whatever remains of the matcher's input
         * string to the output.
         */
        m.appendTail(sb);

        return sb.toString();
    }

    public static String toXMLEscapedInclAmp(final String content) {
        return toXMLEscaped(content.replaceAll("&", "&amp;"));
    }
    public static String toXMLEscaped(final String content) {
        return content.
                    replaceAll("<", "&lt;").
                    replaceAll(">", "&gt;").
                    replaceAll("\"", "&quot;");
    }

    public static URI getCodebase(final Request gReq) {
        return URI.create(gReq.getScheme() + "://" + gReq.getServerName());
    }
}
