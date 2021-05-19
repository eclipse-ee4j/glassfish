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

package com.acme;

import jakarta.ejb.*;
import jakarta.annotation.*;
import jakarta.interceptor.*;

import jakarta.inject.Inject;

public class DependentWithInjectedDataSource {

    @Resource(name="jdbc/__default")
    private javax.sql.DataSource ds;

    public DependentWithInjectedDataSource() {
        System.out.println("Constructed::DependentWithInjectedDataSource");
    }

    @PostConstruct
    public void init() {
        if (ds == null) {
            throw new IllegalStateException("ds is null in DependentWithInjectedDataSource");
        }
        System.out.println("Init::DependentWithInjectedDataSource");
    }

    public String toString() {
        return "DependentWithInjectedDataSource";
    }

    /**
     * This is here to ensure a true instance of this
     * object must be created
     */
    public void callMe() {
    }

}
