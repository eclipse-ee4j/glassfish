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

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Jason Lee
 *
 */
public class ParsingErrorHandler implements ErrorHandler {
    // Log logger = LogFactory.getLog(this.getClass());

    public ParsingErrorHandler() {
        super();
    }

    @Override
    public void warning(SAXParseException arg0) throws SAXException {
//        logger.warn(arg0.getMessage());
    }

    @Override
    public void error(SAXParseException arg0) throws SAXException {
        // logger.error(arg0.getMessage());
        fatalError(arg0);
    }

    @Override
    public void fatalError(SAXParseException arg0) throws SAXException {
//        logger.error(arg0.getMessage());
        System.err.println(arg0.getMessage());
//        System.exit(-1);
    }

}
