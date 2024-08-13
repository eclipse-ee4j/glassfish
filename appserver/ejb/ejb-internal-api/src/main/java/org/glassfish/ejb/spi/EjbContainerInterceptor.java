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

package org.glassfish.ejb.spi;

import com.sun.enterprise.deployment.EjbDescriptor;

import org.jvnet.hk2.annotations.Contract;

/**
 * Contract interface for registering EjbContainerInterceptor to
 * the Ejb Container. It can be inherited by anyone who want to
 * extend the Ejb Container.
 *
 * @author Jeremy_Lv
 */
@Contract
public interface EjbContainerInterceptor {

    /**
     * User can set some useful informations before
     * invoking the EJB application
     * @param ejbDesc
     */
    void preInvoke(EjbDescriptor ejbDesc);

    /**
     * User can set some useful informations after
     * invoking the EJB application
     * @param ejbDesc
     */
    void postInvoke(EjbDescriptor ejbDesc);
}
