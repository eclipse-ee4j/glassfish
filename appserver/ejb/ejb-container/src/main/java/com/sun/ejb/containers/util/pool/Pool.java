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
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/ejb/containers/util/pool/Pool.java,v $</I>
 * @author     $Author: tcfujii $
 * @version    $Revision: 1.3 $ $Date: 2005/12/25 04:13:35 $
 */

package com.sun.ejb.containers.util.pool;

/**
 * Pool defines the methods that can be used by the application to access
 * pooled objects. The basic assumption is that all objects in the pool are
 * identical (homogeneous). This interface defines methods for a) getting an
 * object from the pool, b) returning an object back to the pool
 * and, c) destroying (instead of reusing) an object. In addition to these
 * methods, the Pool has methods for adding and removing PoolEventListeners.
 * There are six overloaded methods for getting objects from a pool.
 *
 */
public interface Pool {

    /**
       @deprecated
    */
    public Object getObject(boolean canWait, Object param)
        throws PoolException;

    /**
       @deprecated
    */
    public Object getObject(long maxWaitTime, Object param)
        throws PoolException;

    /**
     * Get an object from the pool within the specified time.
     * @param The amount of time the calling thread agrees to wait.
     * @param Some value that might be used while creating the object
     * @return an Object or null if an object could not be returned in
     *   'waitForMillis' millisecond.
     * @exception Throws PoolException if an object cannot be created
     */
    public Object getObject(Object param)
        throws PoolException;

    /**
     * Return an object back to the pool. An object that is obtained through
     *    getObject() must always be returned back to the pool using either
     *    returnObject(obj) or through destroyObject(obj).
     */
    public void returnObject(Object obj);

    /**
     * Destroys an Object. Note that applications should not ignore the
     * reference to the object that they got from getObject(). An object
     * that is obtained through getObject() must always be returned back to
     * the pool using either returnObject(obj) or through destroyObject(obj).
     * This method tells that the object should be destroyed and cannot be
     * reused.
     *
     */
    public void destroyObject(Object obj);

}
