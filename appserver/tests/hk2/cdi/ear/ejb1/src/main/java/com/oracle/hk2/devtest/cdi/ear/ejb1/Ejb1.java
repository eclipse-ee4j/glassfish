/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.hk2.devtest.cdi.ear.ejb1;

import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import com.oracle.hk2.devtest.cdi.ear.lib1.HK2Service;
import com.oracle.hk2.devtest.cdi.ear.lib1.Lib1HK2Service;

/**
 *
 * @author jwells
 *
 */
@Stateless
@Remote(Ejb1Remote.class)
public class Ejb1 implements Ejb1Remote {
    @Inject
    private Ejb1HK2Service ejb1Service;

    @Inject
    private Lib1HK2Service lib1Service;

    @Override
    public boolean isEjb1HK2ServiceAvailable() {
        return (ejb1Service != null) && HK2Service.EJB1.equals(ejb1Service.getComponentName()) ;
    }

    @Override
    public boolean isLib1HK2ServiceAvailable() {
        return (lib1Service != null) && HK2Service.LIB1.equals(lib1Service.getComponentName()) ;
    }
}
