/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment;

import jakarta.enterprise.inject.spi.Interceptor;

/**
 * Contains information about 1 ejb interceptor.
 */

public class EjbInterceptor extends InterceptorDescriptor
{

    private EjbBundleDescriptor ejbBundleDescriptor;
    private boolean cdiInterceptor = false;
    private Interceptor interceptor;

    public EjbBundleDescriptor getEjbBundleDescriptor() {
        return ejbBundleDescriptor;
    }

    public void setEjbBundleDescriptor(EjbBundleDescriptor bundleDescriptor) {
        ejbBundleDescriptor = bundleDescriptor;
        super.setBundleDescriptor(bundleDescriptor);
    }

    public String toString() {
        return "EjbInterceptor class = " + getInterceptorClassName();
    }

    public void setCDIInterceptor(boolean flag) {
        cdiInterceptor = flag;
    }

    public boolean isCDIInterceptor() {
        return cdiInterceptor;
    }

    /**
     * @return The interceptor.  May be null when CDI is not enabled.
     */
    public Interceptor getInterceptor() {
        return interceptor;
    }

    public void setInterceptor( Interceptor interceptor ) {
        this.interceptor = interceptor;
    }
}
