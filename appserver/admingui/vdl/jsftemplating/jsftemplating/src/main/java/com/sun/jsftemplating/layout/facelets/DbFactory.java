/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

/**
 *
 */
package com.sun.jsftemplating.layout.facelets;

import com.sun.jsftemplating.layout.xml.XMLErrorHandler;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class provides a convenient way to create a DocumentBuilder using the JSFTemplating entity resolver.
 *
 * @author Jason Lee
 *
 */
public class DbFactory {
    public static DocumentBuilder getInstance() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        String FEATURE = null;
        try {
            FEATURE = "http://xml.org/sax/features/external-parameter-entities";
            factory.setFeature(FEATURE, false);

            FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
            factory.setFeature(FEATURE, false);

            FEATURE = "http://xml.org/sax/features/external-general-entities";
            factory.setFeature(FEATURE, false);

            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("The feature '"
            + FEATURE + "' is not supported by your XML processor.", e);
        }
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(false);
        factory.setCoalescing(false);
        factory.setExpandEntityReferences(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        try {
            builder.setErrorHandler(new XMLErrorHandler(new PrintWriter(new OutputStreamWriter(System.err, "UTF-8"), true)));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }

        builder.setEntityResolver(new FaceletsClasspathEntityResolver());

        return builder;
    }
}
