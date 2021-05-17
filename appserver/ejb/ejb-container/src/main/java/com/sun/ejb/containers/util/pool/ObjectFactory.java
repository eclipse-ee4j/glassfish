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

/**
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/ejb/containers/util/pool/ObjectFactory.java,v $</I>
 * @author     $Author: tcfujii $
 * @version    $Revision: 1.3 $ $Date: 2005/12/25 04:13:35 $
 */

package com.sun.ejb.containers.util.pool;

import java.util.Properties;

/**
 * An object factory that 'knows' how to create / destroy objects
 */
public interface ObjectFactory {

    /**
     * Create an object. Called from Pool.getObject(param)
     */
    public Object create(Object param)
        throws PoolException;

    /**
     * Destroy an object. Called from Pool.destroyObject.
     */
    public void destroy(Object object);

}
