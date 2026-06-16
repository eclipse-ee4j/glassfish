/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.layout.facelets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;

/**
 * <p>
 * This class provides namespace support for facelets taglib.xml files.
 * </p>
 *
 * @author Ken Paulsen
 */
public class NSContext implements javax.xml.namespace.NamespaceContext {

    /**
     * <p>
     * Creates a default NSContext.
     * </p>
     */
    public NSContext() {
        addNamespace("xml", XMLConstants.XML_NS_URI);
        addNamespace("xmlns", XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
    }

    /**
     * <p>
     * Returns the namespace for the given prefix.
     * </p>
     *
     * @throws IllegalArgumentException If <code>null</code> prefix is given.
     */
    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("null is not allowed.");
        }
        String result = _uris.get(prefix);
        if (result == null) {
            if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                result = _defaultNSURI;
            }
            if (result == null) {
                result = XMLConstants.NULL_NS_URI;
            }
        }
        return result;
    }

    /**
     * <p>
     * Returns the prefix for the given namespace. If not mapped, <code>null</code> is returned.
     * </p>
     *
     * @throws IllegalArgumentException If <code>null</code> prefix is given.
     */
    @Override
    public String getPrefix(String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("null is not allowed.");
        }
        String result = _prefixes.get(namespaceURI);
        if (result == null) {
            if (namespaceURI.equals(_defaultNSURI)) {
                result = XMLConstants.DEFAULT_NS_PREFIX;
            }
        }
        return result;
    }

    /**
     * <p>
     * This implementation doesn't support this functionality. Instead returns the same result as {@link #getPrefix(String)}
     * via an <code>Iterator</code>.
     * </p>
     */
    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        ArrayList list = new ArrayList<String>();
        list.add(getPrefix(namespaceURI));
        return list.iterator();
    }

    /**
     * <p>
     * This method sets the default NS URI to be used when the default prefix (<code>XMLConstants.DEFAULT_NS_PREFIX</code>)
     * is supplied.
     * </p>
     */
    public void setDefaultNSURI(String defaultNSURI) {
        _defaultNSURI = defaultNSURI;
    }

    /**
     * <p>
     * This method returns the default NS URI (null if not set).
     * </p>
     */
    public String getDefaultNSURI() {
        return _defaultNSURI;
    }

    /**
     * <p>
     * This method registers a Namespace mapping.
     * </p>
     */
    public void addNamespace(String prefix, String uri) {
        _uris.put(prefix, uri);
        _prefixes.put(uri, prefix);
    }

    private String _defaultNSURI = null;

    private Map<String, String> _uris = new HashMap<>();
    private Map<String, String> _prefixes = new HashMap<>();
}
