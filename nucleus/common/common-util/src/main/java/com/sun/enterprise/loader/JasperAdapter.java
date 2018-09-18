/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.loader;

import java.net.URL;

/**
 * Represents an interface that must be implemented by classloaders
 * that don't extend URLClassLoader and are installed as the parent
 * classloader for web applications.
 *
 * This is used by the JSP engine to construct the classpath to pass to
 * javac during JSP compilation.
 */
public interface JasperAdapter {

    /**
     * Returns the search path of URLs for loading classes and resources.
     */
    public URL[] getURLs();
}
