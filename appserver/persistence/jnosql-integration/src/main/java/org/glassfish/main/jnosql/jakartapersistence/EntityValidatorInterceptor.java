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

import jakarta.interceptor.InvocationContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.executable.ExecutableValidator;

import java.util.Set;

import org.eclipse.jnosql.jakartapersistence.mapping.spi.MethodInterceptor;


/**
 *
 * @author Ondro Mihalyi
 */
public class EntityValidatorInterceptor implements MethodInterceptor {

    Validator validator = null;

    @Override
    public Object intercept(InvocationContext context) throws Exception {
        final ExecutableValidator executablesValidator = getOrCreateValidator().forExecutables();

        final Set<ConstraintViolation<Object>> parameterViolations = executablesValidator
                .validateParameters(context.getTarget(), context.getMethod(), context.getParameters());

        if (parameterViolations.isEmpty()) {
            final Object returnValue = context.proceed();
            final Set<ConstraintViolation<Object>> returnViolations = executablesValidator
                    .validateReturnValue(context.getTarget(), context.getMethod(), returnValue);
            if (returnViolations.isEmpty()) {
                return returnValue;
            }
            throw new ConstraintViolationException(returnViolations);
        }
        throw new ConstraintViolationException(parameterViolations);
    }

    private Validator getOrCreateValidator() {
        if (validator == null) {
            synchronized (this) {
                if (validator == null) {
                    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
                    validator = factory.usingContext().getValidator();
                }
            }
        }
        return validator;
    }

}
