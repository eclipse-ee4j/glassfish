/*
 * Copyright (c) 2001, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.bmp.readonly.ejb;

import jakarta.ejb.EJBHome;
import java.rmi.RemoteException;
import jakarta.ejb.FinderException;
import jakarta.ejb.CreateException;

public interface CourseHome extends EJBHome {

    /**
     * Gets a reference to the remote interface to the CourseBean bean.
     * @exception throws CreateException and RemoteException.
     *
     */
    public Course create(String courseId, String name)
        throws RemoteException, CreateException;

    /**
     * Gets a reference to the remote interface to the CourseBean object by Primary Key.
     * @exception throws FinderException and RemoteException.
     *
     */
    public Course findByPrimaryKey(String courseId)
        throws FinderException, RemoteException;
}
