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

import com.sun.jsftemplating.util.ClasspathEntityResolver;

import org.xml.sax.InputSource;

/**
 * @author Jason Lee
 *
 */
public class FaceletsClasspathEntityResolver extends ClasspathEntityResolver {
    @Override
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) {
        String grammarName = systemId.substring(systemId.lastIndexOf('/') + 1);
        return super.resolveEntity(name, publicId, baseURI, grammarName);
    }
}
