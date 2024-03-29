/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment.node;

import org.xml.sax.helpers.NamespaceSupport;

/**
 * This class represents a XML element in an XML file
 *
 * @author Jerome Dochez
 */
public class XMLElement extends java.lang.Object {

    private String qName;
    private String prefix;
    private final NamespaceSupport namespaces;

    /** Creates new XMLElement */
    public XMLElement(String qName) {
        this(qName, null);
    }


    public XMLElement(String qName, NamespaceSupport namespaceSupport) {
        if (qName.indexOf(':') == -1) {
            this.qName = qName;
        } else {
            prefix = qName.substring(0, qName.indexOf(':'));
            this.qName = qName.substring(qName.indexOf(':') + 1);
        }
        // can be null
        namespaces = namespaceSupport;
    }


    public String getQName() {
        return qName;
    }


    public String getPrefix() {
        return prefix;
    }


    /**
     * Map a prefix to a namespaceURI based on the namespace context of this XML element.
     *
     * @return URI
     */
    public String getPrefixURIMapping(String prefixToResolve) {
        return namespaces == null || prefixToResolve == null ? null : namespaces.getURI(prefixToResolve);
    }


    public String getCompleteName() {
        if (prefix == null) {
            return qName;
        }
        return prefix + ":" + qName;
    }


    @Override
    public boolean equals(Object other) {
        if (other instanceof XMLElement) {
            return qName.equals(((XMLElement) other).getQName());
        }
        return false;
    }


    @Override
    public int hashCode() {
        return qName.hashCode();
    }


    @Override
    public String toString() {
        return qName;
    }
}
