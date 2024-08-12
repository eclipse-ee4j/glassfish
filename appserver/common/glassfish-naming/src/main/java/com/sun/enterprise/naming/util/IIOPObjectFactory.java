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

package com.sun.enterprise.naming.util;

import java.util.Hashtable;
import java.util.logging.Level;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import static com.sun.enterprise.naming.util.LogFacade.logger;

/**
 * An object factory to handle URL references.
 * Handles references and looks up in the cosnaming contexts.
 */

public class IIOPObjectFactory implements ObjectFactory {
    Hashtable env = new Hashtable();

    public Object getObjectInstance(Object obj,
                                    Name name,
                                    Context nameCtx,
                                    Hashtable env) throws Exception {
        env.put("java.naming.factory.initial", "org.glassfish.jndi.cosnaming.CNCtxFactory");

        InitialContext ic = new InitialContext(env);

        Reference ref = (Reference) obj;
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "IIOPObjectFactory " + ref + " Name:" + name);
        }
        RefAddr refAddr = ref.get("url");
        Object realObject = ic.lookup((String) refAddr.getContent());
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Found Object:" + realObject);
        }
        return realObject;
    }

    public Hashtable getEnv() {
        return env;
    }
}
