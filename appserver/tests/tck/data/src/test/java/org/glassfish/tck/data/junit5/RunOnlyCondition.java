/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Ondro Mihalyi
 */
package org.glassfish.tck.data.junit5;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 *
 * @author Ondro Mihalyi
 */
public class RunOnlyCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Optional<Method> method = context.getTestMethod();
        if (method.isPresent()) {
            boolean hasRunOnly = method.map(m -> m.isAnnotationPresent(RunOnly.class)).orElse(false);

            if (!hasRunOnly) {
                return ConditionEvaluationResult.disabled("Disabled: not marked with @RunOnly");
            }
        }

        return ConditionEvaluationResult.enabled("Enabled");
    }
}
