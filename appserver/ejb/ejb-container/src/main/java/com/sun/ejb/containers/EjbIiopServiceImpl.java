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

package com.sun.ejb.containers;

import com.sun.enterprise.deployment.EjbDescriptor;

import org.glassfish.enterprise.iiop.spi.EjbService;
import org.jvnet.hk2.annotations.Service;

/**
 *
 */
@Service
public class EjbIiopServiceImpl implements EjbService {


    public EjbDescriptor ejbIdToDescriptor(long ejbId) {

        // Ejb container util might not have been initialized yet if this is being
        // called from a security interceptor callback during the initial portions
        // of lazy-orb naming service initialization.  Just return null if
        // it's not available since in that case the id can not be for an EJB anyway.
        //
        return EjbContainerUtilImpl.isInitialized() ?
                EjbContainerUtilImpl.getInstance().getDescriptor(ejbId) : null;

    }


}
