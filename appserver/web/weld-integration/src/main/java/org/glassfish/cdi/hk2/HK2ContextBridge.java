/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;

import java.lang.annotation.Annotation;

import org.glassfish.hk2.api.ActiveDescriptor;

/**
 * This is an implementation of a CDI context that is put into CDI which will handle all of the hk2 scope/context pairs
 *
 * @author jwells
 *
 */
public class HK2ContextBridge implements Context {
    private final org.glassfish.hk2.api.Context<?> hk2Context;

    HK2ContextBridge(org.glassfish.hk2.api.Context<?> hk2Context) {
        this.hk2Context = hk2Context;
    }

    @Override
    public <T> T get(Contextual<T> contextual) {
        if (!(contextual instanceof HK2CDIBean)) {
            return null;
        }

        HK2CDIBean<T> hk2CdiBean = (HK2CDIBean<T>) contextual;

        ActiveDescriptor<T> descriptor = hk2CdiBean.getHK2Descriptor();

        if (!hk2Context.containsKey(descriptor)) {
            return null;
        }

        return hk2CdiBean.create(null);
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        return contextual.create(creationalContext);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return hk2Context.getScope();
    }

    @Override
    public boolean isActive() {
        return hk2Context.isActive();
    }

}
