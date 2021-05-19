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

import jakarta.inject.Named;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * There are two factories producing this type, both with different names
 *
 * @author jwells
 *
 */
@Service
public class HK2NamedServiceFactory implements Factory<HK2NamedService> {
    public final static String NAMED_SERVICE_NAME = "rumplestiltskin";

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Factory#provide()
     */
    @Override
    @Named(NAMED_SERVICE_NAME) @PerLookup
    public HK2NamedService provide() {
        return new HK2NamedService() {

            @Override
            public String getName() {
                return NAMED_SERVICE_NAME;
            }

        };
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Factory#dispose(java.lang.Object)
     */
    @Override
    public void dispose(HK2NamedService instance) {
        // Do nothing

    }

}
