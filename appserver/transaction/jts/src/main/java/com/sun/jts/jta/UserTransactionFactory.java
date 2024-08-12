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

package com.sun.jts.jta;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 * Factory for producing the UserTransactionImpl objects.
 *
 * @author Ram Jeyaraman
 * @version 1.0 Feb 09, 1999
 */
public class UserTransactionFactory implements ObjectFactory {

    /**
     * @param obj Reference information that can be used in creating an object.
     * @param name of this object relative to nameCtx (optional).
     * @param nameCtx context relative to which the name parameter specified.
     *     If null, name is relative to the default initial context.
     * @param environment possibly null environment used in creating the object.
     *
     * @return object created; null if an object cannot be created.
     *
     * @exception java.lang.Exception if this object factory encountered
     *     an exception while attempting to create an object.
     */
     public Object getObjectInstance(Object refObj, Name name,
        Context nameCtx, java.util.Hashtable env)
        throws Exception {

        if (refObj == null || !(refObj instanceof Reference))
            return null;

        Reference ref = (Reference) refObj;

        if (ref.getClassName().
            equals(UserTransactionImpl.class.getName())) {
            // create a new object
            return new UserTransactionImpl();
        }

        return null;
    }
}
