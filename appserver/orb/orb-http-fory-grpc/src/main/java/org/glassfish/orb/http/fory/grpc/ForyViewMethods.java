/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */


package org.glassfish.orb.http.fory.grpc;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * The methods of a remote business view that are published as rpcs.
 *
 * <p>One definition, used by the skeleton, the IDL generator and the
 * publisher, so that they cannot disagree about which methods a view has: an
 * interface's static helper, a compiler-generated bridge or a synthetic
 * method is not a remote call, and counting it in one place and not in
 * another would unpublish the whole view.
 */
public final class ForyViewMethods {

    private ForyViewMethods() {
    }

    /**
     * @return the public instance methods of the view other than those of
     *         {@code Object}, ordered by name and then by parameter types
     */
    public static List<Method> of(Class<?> view) {
        return Arrays.stream(view.getMethods())
                .filter(method -> method.getDeclaringClass() != Object.class)
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .filter(method -> !method.isSynthetic() && !method.isBridge())
                .sorted(Comparator.comparing(Method::getName)
                        .thenComparing(method -> Arrays.toString(method.getParameterTypes())))
                .toList();
    }
}
