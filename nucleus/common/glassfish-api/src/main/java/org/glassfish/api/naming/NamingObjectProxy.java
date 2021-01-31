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

package org.glassfish.api.naming;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jvnet.hk2.annotations.Contract;

/**
 * A proxy object that can be bound to GlassfishNamingManager. Concrete implementation of this contract will take
 * appropriate action when the proxy is lookedup. Typically, this can be used to lazily instantiate an Object at lookup
 * time than at bind time.
 *
 * Again, it is upto the implementation to cache the result (inside the proxy implementation so that subsequent lookup
 * can obtain the same cacheed object. Or the implementation can choose to return different object every time.
 *
 * @author Mahesh Kannan
 *
 */

@Contract
public interface NamingObjectProxy {

    /**
     * Create and return an object.
     *
     * @return an object
     */
    Object create(Context ic) throws NamingException;

    /**
     * Special Naming Object proxy whose first create() call replaces itself in naming service.
     */
    public interface InitializationNamingObjectProxy extends NamingObjectProxy {
    }

}
