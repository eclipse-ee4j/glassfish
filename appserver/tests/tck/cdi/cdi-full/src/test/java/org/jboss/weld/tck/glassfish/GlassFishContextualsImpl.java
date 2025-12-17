/*
 * Copyright (c) 2024 Eclipse Foundation and/or its affiliates.
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
import jakarta.enterprise.context.spi.CreationalContext;

import org.jboss.cdi.tck.spi.Contextuals;

public class GlassFishContextualsImpl implements Contextuals {

    @Override
    public <T> Inspectable<T> create(T instance, Context context) {
        return new InspectableContextual<>(instance);
    }

    static class InspectableContextual<T> implements Inspectable<T> {

        private T instancePassedToConstructor;
        private T instancePassedToDestroy;

        private CreationalContext<T> creationalContextPassedToCreate;
        private CreationalContext<T> creationalContextPassedToDestroy;

        InspectableContextual(T instance) {
            this.instancePassedToConstructor = instance;
        }

        @Override
        public T create(CreationalContext<T> creationalContext) {
            this.creationalContextPassedToCreate = creationalContext;
            return instancePassedToConstructor;
        }

        @Override
        public void destroy(T instance, CreationalContext<T> creationalContext) {
            instancePassedToDestroy = instance;
            creationalContextPassedToDestroy = creationalContext;
        }

        @Override
        public CreationalContext<T> getCreationalContextPassedToCreate() {
            return creationalContextPassedToCreate;
        }

        @Override
        public T getInstancePassedToDestroy() {
            return instancePassedToDestroy;
        }

        @Override
        public CreationalContext<T> getCreationalContextPassedToDestroy() {
            return creationalContextPassedToDestroy;
        }
    }

}
