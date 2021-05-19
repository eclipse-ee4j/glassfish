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

import java.util.ArrayList;
import jakarta.ejb.EJBObject;
import java.rmi.RemoteException;


public interface Course extends EJBObject {

    /**
     * Returns an arraylist of StudentIds taking the course.
     * @exception RemoteException
     */
    public ArrayList getStudentIds() throws RemoteException;

    /**
     * Returns the name of the course.
     * @exception RemoteException
     *
     */
    public String getName() throws RemoteException;

    /**
     * Sets the name of the course.
     * @exception RemoteException
     *
     */
    public void setName(String name) throws RemoteException;

}
