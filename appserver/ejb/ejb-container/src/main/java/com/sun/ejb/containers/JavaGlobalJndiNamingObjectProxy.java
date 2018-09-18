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

import org.glassfish.api.naming.NamingObjectProxy;

import javax.naming.Context;

/**
 * Used to register portable global JNDI names for LOCAL EJB 2.x / 3.x references.
 * @author Mahesh Kannan
 */
public class JavaGlobalJndiNamingObjectProxy
    implements NamingObjectProxy {
    private BaseContainer container;

    private String intfName;

    public JavaGlobalJndiNamingObjectProxy(BaseContainer container, String intfName) {
        this.container = container;
        this.intfName = intfName;
    }

    public Object create(Context ic) {
        GenericEJBLocalHome genericLocalHome =
                container.getEJBLocalBusinessHome(intfName);
        return genericLocalHome.create(intfName);
    }
}
