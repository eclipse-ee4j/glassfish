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

package com.oracle.hk2.devtest.cdi.ear.ejb2;

import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import com.oracle.hk2.devtest.cdi.ear.ejb1.Ejb1HK2Service;
import com.oracle.hk2.devtest.cdi.ear.lib1.Lib1HK2Service;

/**
 *
 * @author jwells
 *
 */
@Stateless
@Remote(Ejb2Remote.class)
public class Ejb2 implements Ejb2Remote {
    @Inject
    private Ejb1HK2Service ejb1Service;

    @Inject
    private Lib1HK2Service lib1Service;

    @Inject
    private Ejb2HK2Service ejb2Service;

    @Override
    public boolean isEjb2HK2ServiceAvailable() {
        return (ejb2Service != null);
    }

    @Override
    public boolean isEjb1HK2ServiceAvailable() {
        return (ejb1Service != null);
    }

    @Override
    public boolean isLib1HK2ServiceAvailable() {
        return (lib1Service != null);
    }
}
