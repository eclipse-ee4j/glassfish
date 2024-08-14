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

package org.glassfish.cdi.hk2;

import jakarta.inject.Singleton;

import java.lang.annotation.Annotation;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.ServiceHandle;

/**
 * This is an HK2 context for use with descriptors that are backed by CDI services (which are not Dependent or
 * Singleton). This scope is most like PerLookup, as it always asks for a new instance. Whether or not CDI truly gives a
 * new instance or not is up to CDI
 *
 * @author jwells
 *
 */
@Singleton
public class CDIContextBridge implements Context<CDIScope> {

    @Override
    public Class<? extends Annotation> getScope() {
        return CDIScope.class;
    }

    @Override
    public <U> U findOrCreate(ActiveDescriptor<U> activeDescriptor, ServiceHandle<?> root) {
        return activeDescriptor.create(root);
    }

    @Override
    public boolean containsKey(ActiveDescriptor<?> descriptor) {
        return false;
    }

    @Override
    public void destroyOne(ActiveDescriptor<?> descriptor) {
        // It is up to CDI to managed lifecycle

    }

    @Override
    public boolean supportsNullCreation() {
        return false;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void shutdown() {
        // It is up to CDI to manage lifecycle

    }

}
