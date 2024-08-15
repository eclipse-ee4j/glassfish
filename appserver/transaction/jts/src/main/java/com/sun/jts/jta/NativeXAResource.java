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

 import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

 /**
  * This interface extends JTA XAResource interface and defines
  * new methods for thread association support and resource
  * initialization.
  */

public interface NativeXAResource extends XAResource {

    // added functionality for thread management

    /**
     *  Opens the RM for the calling thread (xa_open).
     */
     public void open() throws XAException;

    /**
     *  Closes the RM for the calling thread (xa_close).
     */
    public void close() throws XAException;

    /**
     * checks if the thread has opened (xa_open) the RM atleast once.
     *
     * @param thread the thread to be checked for resource initialization.
     *
     * @return true if the thread has opened the resource (RM) atleast once.
     */
    public boolean isInitialized(Thread thread);

    /**
     * enlist the JDBC connection in XA (needed to support MSSQLServer)
     * this should be called once per connection per transaction
     */
    public void enlistConnectionInXA();
}
