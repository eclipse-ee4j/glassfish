/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.jboss.weld.tck.glassfish;

import jakarta.enterprise.context.spi.Context;

import org.jboss.cdi.tck.spi.Contexts;
import org.jboss.weld.Container;
import org.jboss.weld.context.ApplicationContext;
import org.jboss.weld.context.DependentContext;
import org.jboss.weld.context.ManagedContext;
import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.http.HttpRequestContext;
import org.jboss.weld.util.ForwardingContext;

public class GlassFishContextImpl implements Contexts<Context> {
    @Override
    public RequestContext getRequestContext() {
        return Container.instance().deploymentManager().instance().select(HttpRequestContext.class).get();
    }

    @Override
    public void setActive(Context context) {
        context = ForwardingContext.unwrap(context);
        if (context instanceof ManagedContext) {
            ((ManagedContext) context).activate();
        } else if (context instanceof ApplicationContext) {
            // No-op, always active
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void setInactive(Context context) {
        context = ForwardingContext.unwrap(context);
        if (context instanceof ManagedContext) {
            ((ManagedContext) context).deactivate();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public DependentContext getDependentContext() {
        return Container.instance().deploymentManager().instance().select(DependentContext.class).get();
    }

    @Override
    public void destroyContext(Context context) {
        context = ForwardingContext.unwrap(context);
        if (context instanceof ManagedContext) {
            ManagedContext managedContext = (ManagedContext) context;
            managedContext.invalidate();
            managedContext.deactivate();
            managedContext.activate();
        } else if (context instanceof ApplicationContext) {
            ((ApplicationContext) context).invalidate();
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
