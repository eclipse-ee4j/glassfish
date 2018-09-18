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

package com.sun.enterprise.tools.verifier;

import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.sun.enterprise.tools.verifier.util.LogDomains;
import com.sun.enterprise.tools.verifier.util.XMLValidationHandler;

public class NameToken {

    final static String XMLtop =
            "<!DOCTYPE NameToken [ <!ELEMENT NameToken EMPTY> <!ATTLIST NameToken value NMTOKEN #REQUIRED>]> <NameToken value=\""; // NOI18N


    final static String XMLbottom = "\"/>"; // NOI18N

    // Logger to log messages
    private static Logger logger = LogDomains.getLogger(
            LogDomains.AVK_VERIFIER_LOGGER);

    /**
     * Determine is value is legal NMToken type
     *
     * @param value xml element to be checked
     * @return <code>boolean</code> true if xml element is legal NMToken,
     *         false otherwise
     */
    public static boolean isNMTOKEN(String value) {
/*
        com.sun.enterprise.util.LocalStringManagerImpl smh =
            StringManagerHelper.getLocalStringsManager();
*/
        String XMLdoc = XMLtop + value + XMLbottom;
        logger.log(Level.FINE,
                "com.sun.enterprise.tools.verifier.NameToken.print", // NOI18N
                new Object[]{XMLdoc});

        try {
            InputSource source = new InputSource(
                    new ByteArrayInputStream(XMLdoc.getBytes()));
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setValidating(true);
            // ValidatingParser p = new ValidatingParser();
            XMLReader p = spf.newSAXParser().getXMLReader();
//            XMLErrorHandler eh = new XMLErrorHandler(null);
            p.setErrorHandler(new XMLValidationHandler());
            p.parse(source);
            return true;

        } catch (Exception e) {
            return false;
        } 
    }
}


