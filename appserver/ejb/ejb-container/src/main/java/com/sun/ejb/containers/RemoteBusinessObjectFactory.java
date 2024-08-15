/*
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

package com.sun.ejb.containers;

import com.sun.ejb.EJBUtils;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 * An object factory to handle Remote EJB 30 business interface
 * reference access.
 */

public class RemoteBusinessObjectFactory implements ObjectFactory {

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable env) throws Exception {
        InitialContext ic = new InitialContext(env);
        Reference ref = (Reference) obj;
        RefAddr refAddr = ref.get("url");
        Object genericRemoteHomeObj = ic.lookup((String) refAddr.getContent());
        String busInterface = ref.getClassName();
        return EJBUtils.lookupRemote30BusinessObject(genericRemoteHomeObj, busInterface);
    }
}
