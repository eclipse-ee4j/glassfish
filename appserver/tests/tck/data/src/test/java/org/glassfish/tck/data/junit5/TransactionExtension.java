/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Ondro Mihalyi
 */
package org.glassfish.tck.data.junit5;

import jakarta.enterprise.inject.spi.CDI;

import java.lang.reflect.Method;
import java.util.Set;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;



public class TransactionExtension implements InvocationInterceptor {

    Set<String> targetTestMethodNames = Set.of(
            "testThirdAndFourthSlicesOf5",
            "testFirstSliceOf5",
            "testOrderByHasPrecedenceOverPageRequestSorts",
            "testStaticMetamodelAscendingSortsPreGenerated",
            "testFinalSliceOfUpTo5",
            "testThirdAndFourthPagesOf10",
            "testSliceOfNothing",
            "testFindPage",
            "testPageOfNothing",
            "testFirstPageOf10",
            "testBeyondFinalSlice",
            "testLiteralTrue",
            "testFinalPageOfUpTo10",
            "testStaticMetamodelAscendingSorts",
            "testBeyondFinalPage"
            );

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (targetTestMethodNames.contains(invocationContext.getExecutable().getName())) {
            CDI.current().select(TransactionalWrapper.class).get().proceed(invocation);
        } else {
            invocation.proceed();
        }
    }

}
