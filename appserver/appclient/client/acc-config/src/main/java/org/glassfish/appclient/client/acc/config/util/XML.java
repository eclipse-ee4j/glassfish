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

package org.glassfish.appclient.client.acc.config.util;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.glassfish.appclient.client.acc.config.Property;

/**
 * Logic used during JAXB conversions between XML and objects.
 *
 * @author tjquinn
 */
public class XML {

    private static final List<String> booleanTrueValues = Arrays.asList("yes", "on", "1", "true");

    private static final List<String> providerTypeValues = Arrays.asList("client", "server", "client-server");

    public static boolean parseBoolean(final String booleanText) {
        return _parseBoolean(booleanText.trim());
    }

    private static boolean isWhiteSpace(char ch) {
        return ch==' ' || ch == '\t';
    }
    /**
     *
     * <code><!ENTITY % boolean "(yes | no | on | off | 1 | 0 | true | false)"></code>
     *
     * @param literal
     * @return
     */
    private static boolean _parseBoolean(final CharSequence literal) {
        int i=0;
        int len = literal.length();
        char ch;
        do {
            ch = literal.charAt(i++);
        } while(isWhiteSpace(ch) && i<len);

        // if we are strict about errors, check i==len. and report an error

        return booleanTrueValues.contains(literal.subSequence(i, len));
    }

    public static String parseProviderType(String providerType) {
        if (providerTypeValues.contains(providerType)) {
            return providerType;
        }
        throw new IllegalArgumentException(providerType);
    }

    /**
     * Converts the XML property elements (with name and value) to a Properties
     * object.
     *
     * @param props List of Property elements from the JAXB-converted
     * client container element
     * @return corresponding Properties object
     */
    public static Properties toProperties(final List<Property> props) {
        Properties result = new Properties();
        for (Property p : props) {
            result.setProperty(p.getName(), p.getValue());
        }
        return result;
    }

    public static class Password {
        private char[] pw;

        private Password(String s) {
            pw = s.toCharArray();
        }

        public Password(char[] pw) {
            this.pw = pw;
        }

        public static Password parse(String s) {
            return new Password(s);
        }

        public static String print(Password p) {
            return new String(p.pw);
        }

        public char[] get() {
            return pw;
        }
    }
}
