/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.universal.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * A place to put all the ugly boiler plate for parsing an XML file.
 * @author Byron Nevins
 */
public final class XmlParserHelper {

    public XmlParserHelper(final File f) throws FileNotFoundException, XMLStreamException {
        reader = new InputStreamReader(new FileInputStream(f));
        parser = XMLInputFactory.newFactory().createXMLStreamReader(
                f.toURI().toString(), reader);
    }

    public XMLStreamReader get() {
        return parser;
    }

    /**
     * Don't forget to call this method when finished!
     * Closes the parser and the stream
     */
    public void stop() {
        // yes -- you **do** need to close BOTH of them!
        try {
            if (parser != null) {
                parser.close();
            }
        }
        catch (Exception e) {
            // ignore
        }
        try {
            if (reader != null) {
                reader.close();
            }
        }
        catch (Exception e) {
            // ignore
        }
    }

    private final XMLStreamReader parser;
    private final InputStreamReader reader;
}
