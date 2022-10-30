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

package com.sun.enterprise.container.common.spi.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.glassfish.common.util.ObjectInputStreamWithLoader;

/**
 * @author Mahesh Kannan
 */
//FIXME: Seems dead, unused.
public class JavaEEObjectInputStream extends ObjectInputStreamWithLoader {

    private final Collection<JavaEEObjectStreamHandler> handlers;

    public JavaEEObjectInputStream(InputStream ois, ClassLoader loader, boolean resolveObject,
                                    Collection<JavaEEObjectStreamHandler> handlers)
        throws IOException {
        super(ois, loader);
        super.enableResolveObject(resolveObject);
        this.handlers = handlers;
    }

    @Override
    protected Object resolveObject(Object obj)
        throws IOException {

        Object result = obj;
        for (JavaEEObjectStreamHandler handler : handlers) {
            result = handler.resolveObject(obj);
            if (result != obj) {
                break;
            }
        }

        return result;
    }
}
