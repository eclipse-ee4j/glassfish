/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.glassfish.main.jnosql.jakartapersistence;

import jakarta.inject.Inject;
import jakarta.interceptor.InvocationContext;

import org.eclipse.jnosql.jakartapersistence.mapping.EnsureTransactionInterceptor;
import org.eclipse.jnosql.jakartapersistence.mapping.spi.MethodInterceptor;

/**
 *
 * @author Ondro Mihalyi
 */
public class GlassFishRepositoryInterceptor implements MethodInterceptor {

    @Inject
    EnsureTransactionInterceptor ensureTransactionInterceptor = new EnsureTransactionInterceptor();

    EntityValidatorInterceptor entityValidator = new EntityValidatorInterceptor();

    @Override
    public Object intercept(InvocationContext context) throws Exception {
        return entityValidator.intercept(
                new ChainedInvocationContext(ensureTransactionInterceptor, context));
    }

}
