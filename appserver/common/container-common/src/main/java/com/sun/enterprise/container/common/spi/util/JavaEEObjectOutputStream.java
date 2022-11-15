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
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;

/**
 * @author Mahesh Kannan
 */
//FIXME: Seems dead, unused.
public class JavaEEObjectOutputStream extends ObjectOutputStream {

    Collection<JavaEEObjectStreamHandler> handlers;

    public JavaEEObjectOutputStream(OutputStream oos, boolean replaceObject,
        Collection<JavaEEObjectStreamHandler> handlers) throws IOException {
        super(oos);
        super.enableReplaceObject(replaceObject);
        this.handlers = handlers;
    }


    @Override
    protected Object replaceObject(Object obj) throws IOException {
        Object result = obj;
        for (JavaEEObjectStreamHandler handler : handlers) {
            result = handler.replaceObject(obj);
            if (result != obj) {
                break;
            }
        }
        return result;
    }
}
