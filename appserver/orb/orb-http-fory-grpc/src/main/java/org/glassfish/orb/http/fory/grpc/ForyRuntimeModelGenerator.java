/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

/** Generates the small request/response model classes needed by a deploy-time adapter. */
public final class ForyRuntimeModelGenerator {

    private ForyRuntimeModelGenerator() {
    }

    /**
     * Generates one pair of public value models for a unary method. The
     * compiler runs once during deployment; invocation code uses the resulting
     * classes and cached handles, never source generation.
     */
    public static GeneratedModels unary(String packageName, String methodName,
                                       Class<?> requestType, Class<?> responseType) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("a JDK compiler is required for deploy-time Fory models");
        }
        String base = Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);
        String requestName = packageName + '.' + base + "Request";
        String responseName = packageName + '.' + base + "Response";
        Map<String, String> sources = Map.of(
                requestName, source(requestName, requestType),
                responseName, source(responseName, responseType));
        MemoryFileManager files = new MemoryFileManager(compiler.getStandardFileManager(
                null, null, null));
        JavaCompiler.CompilationTask task = compiler.getTask(null, files, null,
                java.util.List.of("-g:none"), null,
                sources.entrySet().stream().map(e -> new Source(e.getKey(), e.getValue())).toList());
        if (!task.call()) {
            throw new IllegalArgumentException("cannot compile deploy-time Fory models for " + methodName);
        }
        return new GeneratedModels(files.load(requestName), files.load(responseName));
    }

    private static String source(String name, Class<?> type) {
        int dot = name.lastIndexOf('.');
        String packageName = name.substring(0, dot);
        String simple = name.substring(dot + 1);
        String javaType = type == void.class ? "java.lang.Void" : type.getCanonicalName();
        return "package " + packageName + "; public final class " + simple + " {"
                + " private final " + javaType + " value;"
                + " public " + simple + "(" + javaType + " value) { this.value=value; }"
                + " public " + javaType + " value() { return value; } }";
    }

    public record GeneratedModels(Class<?> request, Class<?> response) {
    }

    private static final class Source extends SimpleJavaFileObject {
        private final String source;

        Source(String name, String source) {
            super(URI.create("string:///" + name.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension),
                    JavaFileObject.Kind.SOURCE);
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
        }
    }

    private static final class MemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {
        private final Map<String, byte[]> classes = new HashMap<>();

        MemoryFileManager(JavaFileManager delegate) {
            super(delegate);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className,
                                                    JavaFileObject.Kind kind, FileObject sibling) {
            return new SimpleJavaFileObject(URI.create("mem:///" + className), kind) {
                private final java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream() {
                    @Override
                    public void close() {
                        classes.put(className, toByteArray());
                    }
                };

                @Override
                public java.io.OutputStream openOutputStream() {
                    return bytes;
                }

            };
        }

        Class<?> load(String name) {
            byte[] bytecode = classes.get(name);
            if (bytecode == null) {
                throw new IllegalStateException("compiler produced no bytecode for " + name);
            }
            return new ClassLoader(ForyRuntimeModelGenerator.class.getClassLoader()) {
                Class<?> define() {
                    return defineClass(name, bytecode, 0, bytecode.length);
                }
            }.define();
        }
    }
}
