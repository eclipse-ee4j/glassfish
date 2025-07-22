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
package org.glassfish.main.jnosql.jakartapersistence.mapping.glassfishcontext;

import jakarta.interceptor.InvocationContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jnosql.jakartapersistence.mapping.spi.MethodInterceptor;

/**
 *
 * @author Ondro Mihalyi
 */
@MethodInterceptor.SaveEntity
public class EntityValidator implements MethodInterceptor {

    @Override
    public Object intercept(InvocationContext context) throws Exception {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        final Set<ConstraintViolation<Object>> violations = new HashSet<>();
        for (Object entity : context.getParameters()) {
             violations.addAll(validator.validate(entity));
        }
        if (violations.isEmpty()) {
            return context.proceed();
        }
        throw new ConstraintViolationException(violations);
    }

}
