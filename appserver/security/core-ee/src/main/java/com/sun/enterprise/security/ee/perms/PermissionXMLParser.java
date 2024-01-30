/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.ee.perms;

import com.sun.enterprise.security.integration.PermissionCreator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * Paser to parse permissions.xml packaged in a ear or in a standalone module
 */
public class PermissionXMLParser {

    private static XMLInputFactory xmlInputFactory;
    static {
        xmlInputFactory = XMLInputFactory.newFactory();
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);

        // set an zero-byte XMLResolver as IBM JDK does not take SUPPORT_DTD=false
        // unless there is a jvm option com.ibm.xml.xlxp.support.dtd.compat.mode=false
        xmlInputFactory.setXMLResolver(new XMLResolver() {
            @Override
            public Object resolveEntity(String publicID, String systemID, String baseURI, String namespace) throws XMLStreamException {

                return new ByteArrayInputStream(new byte[0]);
            }
        });
    }

    private XMLStreamReader parser;
    private final PermissionCollection pc = new Permissions();
    private final PermissionCollection permissionCollectionToBeRestricted;


    public PermissionXMLParser(InputStream input, PermissionCollection permissionCollectionToBeRestricted)
        throws XMLStreamException {
        this.permissionCollectionToBeRestricted = permissionCollectionToBeRestricted;
        init(input);
    }


    protected static XMLInputFactory getXMLInputFactory() {
        return xmlInputFactory;
    }

    /**
     * This method will parse the input stream and set the XMLStreamReader object for latter use.
     *
     * @param input InputStream
     * @throws XMLStreamException;
     */
    protected void read(InputStream input) throws XMLStreamException {
        parser = getXMLInputFactory().createXMLStreamReader(input);

        int event = 0;
        String classname = null;
        String target = null;
        String actions = null;
        while (parser.hasNext() && (event = parser.next()) != END_DOCUMENT) {
            if (event == START_ELEMENT) {
                String name = parser.getLocalName();
                if ("permission".equals(name)) {
                    classname = null;
                    target = null;
                    actions = null;
                } else if ("class-name".equals(name)) {
                    classname = parser.getElementText();
                } else if ("name".equals(name)) {
                    target = parser.getElementText();
                } else if ("actions".equals(name)) {
                    actions = parser.getElementText();
                } else if ("permissions".equals(name)) {
                    // continue trough subtree
                } else {
                    skipSubTree(name);
                }
            } else if (event == END_ELEMENT) {
                String name = parser.getLocalName();
                if ("permission".equals(name)) {
                    if (classname != null && !classname.isEmpty()) {
                        addPermission(classname, target, actions);
                    }
                }
            }
        }
    }

    protected void init(InputStream input) throws XMLStreamException {

        try {
            read(input);
        } finally {
            if (parser != null) {
                try {
                    parser.close();
                } catch (Exception ex) {
                    // ignore
                }
            }
        }
    }

    protected void skipRoot(String name) throws XMLStreamException {
        while (true) {
            int event = parser.next();
            if (event == START_ELEMENT) {
                String localName = parser.getLocalName();
                if (!name.equals(localName)) {
                    // String msg = rb.getString(UNEXPECTED_ELEMENT_IN_XML);
                    // msg = MessageFormat.format(msg, new Object[] { name,
                    // localName });
                    // throw new XMLStreamException(msg);
                    throw new XMLStreamException("Unexpected element with name " + name);
                }
                return;
            }
        }
    }

    protected void skipSubTree(String name) throws XMLStreamException {
        while (true) {
            int event = parser.next();
            if (event == END_DOCUMENT) {
                throw new XMLStreamException("Unexpected element with name " + name);
            }
            if (event == END_ELEMENT && name.equals(parser.getLocalName())) {
                return;
            }
        }
    }

    private void addPermission(String classname, String target, String actions) {
        try {
            Permission pm = PermissionCreator.getInstance(classname, target, actions);
            if (pm != null) {
                if (permissionCollectionToBeRestricted != null && permissionCollectionToBeRestricted.implies(pm)) {
                    throw new SecurityException("Restricted Permission Declared - fail deployment!");
                }
                pc.add(pm);
            }
        } catch (ReflectiveOperationException e) {
            throw new SecurityException(e);
        }
    }

    protected PermissionCollection getPermissions() {
        return pc;
    }

}
