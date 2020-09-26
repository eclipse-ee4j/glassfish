/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.jdbc.CustomResourceFactories.ejb;

import jakarta.ejb.*;
import java.rmi.*;
import java.util.Properties;

public interface SimpleBMP
    extends EJBObject {
    public boolean testJavaBean(String testValue) throws RemoteException;
    public boolean testPrimitives(String type, String value, String resourceName) throws RemoteException;
    public boolean testProperties(Properties properties, String resourceName) throws RemoteException;
    public boolean testURL(String url, String resourceName) throws RemoteException;
    public boolean testMappedNames() throws RemoteException;
    public boolean testLookupNames() throws RemoteException;
}
