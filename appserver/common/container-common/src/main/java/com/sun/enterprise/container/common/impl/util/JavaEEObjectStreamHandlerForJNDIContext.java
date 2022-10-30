/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.container.common.impl.util;

import com.sun.enterprise.container.common.spi.util.JavaEEObjectStreamHandler;
import com.sun.enterprise.naming.impl.JavaURLContext;

import jakarta.inject.Inject;

import java.io.IOException;
import java.io.Serializable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.SimpleJndiName;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Mahesh Kannan 2008
 */
@Service
//FIXME: Seems dead, unused.
public class JavaEEObjectStreamHandlerForJNDIContext implements JavaEEObjectStreamHandler {

    @Inject
    private GlassfishNamingManager gfNM;

    @Override
    public Object replaceObject(Object obj) throws IOException {
        if (obj instanceof JavaURLContext) {
            JavaURLContext ctx = (JavaURLContext) obj;
            return new SerializableJNDIContext(ctx.getName());
        } else if (obj instanceof Context) {
            Context ctx = (Context) obj;
            try {
                // Serialize state for a jndi context. The spec only requires
                // support for serializing contexts pointing to java:comp/env
                // or one of its subcontexts.  We also support serializing the
                // references to the the default no-arg InitialContext, as well
                // as references to the the contexts java: and java:comp. All
                // other contexts will either not serialize correctly or will
                // throw an exception during deserialization.
                return new SerializableJNDIContext(SimpleJndiName.of(ctx.getNameInNamespace()));
            } catch (NamingException ex) {
                IOException ioe = new IOException();
                ioe.initCause(ex);
                throw ioe;
            }
        }
        return obj;
    }


    @Override
    public Object resolveObject(Object obj) throws IOException {
        Object result = obj;
        if (obj instanceof SerializableJNDIContext) {
            SerializableJNDIContext sctx = (SerializableJNDIContext) obj;
            try {
                SimpleJndiName name = sctx.getName();
                if (name.isEmpty()) {
                    return new InitialContext();
                }
                return gfNM.restoreJavaCompEnvContext(name);
            } catch (NamingException namEx) {
                IOException ioe = new IOException();
                ioe.initCause(namEx);
                throw ioe;
            }
        }
        return result;
    }

    private static final class SerializableJNDIContext implements Serializable {

        private static final long serialVersionUID = 1L;
        private final SimpleJndiName name;

        SerializableJNDIContext(SimpleJndiName name) {
            this.name = name;
        }

        public SimpleJndiName getName() {
            return name;
        }
    }

}
