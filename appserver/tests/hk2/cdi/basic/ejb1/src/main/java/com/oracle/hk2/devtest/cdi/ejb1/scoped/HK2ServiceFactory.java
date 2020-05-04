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

package com.oracle.hk2.devtest.cdi.ejb1.scoped;

import jakarta.inject.Singleton;

import org.glassfish.hk2.api.Factory;
import org.jvnet.hk2.annotations.Service;

/**
 * This is an HK2 factory for an HK2Service.  A Factory is used
 * for the HK2Service so that we can FORCE HK2Service to NOT
 * be a recognizable bean to CDI
 * 
 * @author jwells
 *
 */
@Service @Singleton
public class HK2ServiceFactory implements Factory<HK2Service> {

    @Override
    @CustomScope
    public HK2Service provide() {
        return new HK2ServiceImpl(HK2Service.RETURN_VALUE);
    }

    @Override
    public void dispose(HK2Service instance) {
        
    }

}
