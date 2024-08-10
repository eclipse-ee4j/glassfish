/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.javaee.full.deployment;

import com.sun.enterprise.loader.ASURLClassLoader;

import java.net.URL;

/**
 * Classloader that is responsible to load the ear libraries (lib/*.jar etc)
 */
public class EarLibClassLoader extends ASURLClassLoader {

    public EarLibClassLoader(URL[] urls, ClassLoader classLoader) {
        super(classLoader);

        for (URL url : urls) {
            addURL(url);
        }
    }

    @Override
    protected String getClassLoaderName() {
        return "EarLibClassLoader";
    }
}
