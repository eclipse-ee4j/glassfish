/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.naming;


import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;


/**
 * This class has the name of the legacy SerialInitContextFactory class in
 * V2 and earlier.  The naming implementations have been moved to the impl
 * subpackage. This class is provided for backward compatibility, since the
 * name of the class was exposed as part of the bootstrap properties.  Typically,
 * the java.naming.factory.initial property is resolved via an embedded
 * jndi.properties file, so in the common case the current SerialInitContextFactory
 * class will be instantiated directly.
 */

public class SerialInitContextFactory implements InitialContextFactory {

    private com.sun.enterprise.naming.impl.SerialInitContextFactory delegate =
        new com.sun.enterprise.naming.impl.SerialInitContextFactory();

    /**
     * Create the InitialContext object.
     */
    public Context getInitialContext(Hashtable env) throws NamingException {
        return delegate.getInitialContext(env);

    }

}
