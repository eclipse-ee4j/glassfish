/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import jakarta.enterprise.context.spi.Contextual;

import org.jboss.cdi.tck.spi.CreationalContexts;
import org.jboss.weld.contexts.CreationalContextImpl;

/**
 * This returns the Weld (and thus GlassFish) specific CreationalContextImpl with added methods
 * for inspection.
 */
public class GlassFishCreationalContextsImpl implements CreationalContexts {

    @Override
    public <T> Inspectable<T> create(Contextual<T> contextual) {
        return new InspectableCreationalContext<>(contextual);
    }

    static class InspectableCreationalContext<T> extends CreationalContextImpl<T> implements Inspectable<T> {

        private T lastBeanPushed;
        private boolean pushCalled;
        private boolean releaseCalled;

        public InspectableCreationalContext(Contextual<T> contextual) {
            super(contextual);
        }

        @Override
        public void push(T incompleteInstance) {
            lastBeanPushed = incompleteInstance;

            pushCalled = true;
            super.push(incompleteInstance);
        }

        @Override
        public Object getLastBeanPushed() {
            return lastBeanPushed;
        }

        @Override
        public boolean isPushCalled() {
            return pushCalled;
        }

        @Override
        public boolean isReleaseCalled() {
            return releaseCalled;
        }

        @Override
        public void release(Contextual<T> contextual, T instance) {
            releaseCalled = true;
            super.release(contextual, instance);
        }

        @Override
        public void release() {
            releaseCalled = true;
            super.release();
        }

    }

}
