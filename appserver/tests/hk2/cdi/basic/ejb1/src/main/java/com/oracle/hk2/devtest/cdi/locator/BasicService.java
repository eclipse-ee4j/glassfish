/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.hk2.devtest.cdi.locator;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * This service will be created with HK2 but will
 * have the CDI BeanManager injected into it
 *
 * @author jwells
 *
 */
@Singleton
public class BasicService {
    @Inject
    private BeanManager beanManager;

    /**
     * This method returns true if the BeanManager was injected.  This service
     * is created with HK2 but still has the CDI created object injected into
     * it
     *
     * @return true if this HK2 created service was injected with a CDI bean
     */
    public boolean gotInjectedWithBeanManager() {
        return (beanManager != null);
    }

}
