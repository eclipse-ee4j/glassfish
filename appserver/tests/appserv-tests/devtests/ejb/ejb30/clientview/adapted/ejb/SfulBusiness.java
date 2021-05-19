/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb30.clientview.adapted;


public interface SfulBusiness extends Common, CommonFoo
{
    // The local business interface has no relationship to EJBLocalObject
    // so it's not a problem to define a method that happens to have the
    // same signature as one of EJBLocalObject's methods.  remove() is
    // a likely name for a method that has @Remove behavior so it needs
    // to work.
    public void remove();

    public void removeRetainIfException(boolean throwException)
        throws Exception;

    public SfulBusiness2 getSfulBusiness2();

}
