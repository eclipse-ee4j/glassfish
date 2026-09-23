/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/** Generates the portable Fory IDL for a supported remote business view. */
public final class ForyIdlGenerator {

    private ForyIdlGenerator() {
    }

    /**
     * Generates the first, deliberately strict contract surface. Primitive
     * and String signatures are portable without application annotations; more
     * complex DTO mapping is added by the deploy-time model generator later.
     */
    public static String generate(String packageName, String serviceName, Class<?> view) {
        StringBuilder out = new StringBuilder(512)
                .append("syntax = \"proto3\";\n\n")
                .append("package ").append(packageName).append(";\n\n")
                .append("message Empty {}\n\n");
        Set<String> names = new HashSet<>();
        Method[] methods = Arrays.stream(view.getMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> method.getDeclaringClass() != Object.class)
                .sorted(Comparator.comparing(Method::getName)
                        .thenComparing(method -> Arrays.toString(method.getParameterTypes())))
                .toArray(Method[]::new);
        for (Method method : methods) {
            if (!names.add(method.getName())) {
                throw new IllegalArgumentException(
                        "overloaded EJB methods need an explicit Fory name: " + method);
            }
            String rpcName = upperFirst(method.getName());
            String request = rpcName + "Request";
            String response = rpcName + "Response";
            out.append("message ").append(request).append(" {\n");
            if (method.getParameterCount() > 1) {
                throw unsupported(method, "more than one parameter");
            }
            if (method.getParameterCount() == 1) {
                out.append("    ").append(foryType(method.getParameterTypes()[0]))
                        .append(" value = 1;\n");
            }
            out.append("}\n\nmessage ").append(response).append(" {\n");
            if (method.getReturnType() != void.class) {
                out.append("    ").append(foryType(method.getReturnType()))
                        .append(" value = 1;\n");
            }
            out.append("}\n\n");
        }
        out.append("service ").append(serviceName).append(" {\n");
        for (Method method : methods) {
            String rpcName = upperFirst(method.getName());
            out.append("    rpc ").append(rpcName).append("(")
                    .append(rpcName).append("Request) returns (")
                    .append(rpcName).append("Response);\n");
        }
        return out.append("}\n").toString();
    }

    private static String foryType(Class<?> type) {
        if (type == String.class || type == Character.class || type == char.class) return "string";
        if (type == boolean.class || type == Boolean.class) return "bool";
        if (type == byte.class || type == Byte.class || type == short.class || type == Short.class
                || type == int.class || type == Integer.class) return "int32";
        if (type == long.class || type == Long.class) return "int64";
        if (type == float.class || type == Float.class) return "float32";
        if (type == double.class || type == Double.class) return "float64";
        throw unsupported(type, "type is not portable without a generated DTO mapping");
    }

    private static IllegalArgumentException unsupported(Method method, String reason) {
        return new IllegalArgumentException(method + ": " + reason);
    }

    private static IllegalArgumentException unsupported(Class<?> type, String reason) {
        return new IllegalArgumentException(type.getName() + ": " + reason);
    }

    private static String upperFirst(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
