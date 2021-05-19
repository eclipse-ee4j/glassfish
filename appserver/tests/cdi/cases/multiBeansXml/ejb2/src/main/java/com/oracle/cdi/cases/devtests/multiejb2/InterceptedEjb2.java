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

package com.oracle.cdi.cases.devtests.multiejb2;

import java.util.List;

import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

/**
 *
 * @author jwells
 */
@Stateless
@Remote(MultiBeansXmlEjb2.class)
public class InterceptedEjb2 implements MultiBeansXmlEjb2 {
    @SuppressWarnings("unused")
    @Inject
    private BeanManager manager;  // Forces this to be a CDI bean

    @RecordingInterceptor
    @Override
    public List<String> callMe(List<String> callerList) {
        callerList.add(CALL_ME2);
        return callerList;
    }
}
