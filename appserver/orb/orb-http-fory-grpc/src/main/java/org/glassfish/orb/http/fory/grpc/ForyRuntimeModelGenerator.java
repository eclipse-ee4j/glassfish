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

    /**
     * The field number the published IDL gives this field.
     *
     * <p>Both ends have to declare it. A client generated from the document
     * carries {@code fory:"id=1"}, and a writer that declares the id puts the
     * id on the wire where a writer that does not puts the field's name. The
     * two are not compatible and, worse, not an error: the reader matches
     * nothing and hands back an object with every field null, so the call is
     * answered and empty. {@code ForyIdlGenerator} numbers this single field
     * 1, and this is the other half of that statement.
     */
    private static final int VALUE_FIELD_ID = 1;

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
                // An empty class path on purpose: everything the generated
                // source needs comes from the file manager below, so the
                // compile does not depend on whatever the server's own class
                // path happens to hold.
                java.util.List.of("-g:none", "-classpath", ""), null,
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
                + " @org.apache.fory.annotation.ForyField(id = " + VALUE_FIELD_ID + ")"
                + " private final " + javaType + " value;"
                + " public " + simple + "(" + javaType + " value) { this.value=value; }"
                + " public " + javaType + " value() { return value; } }";
    }

    public record GeneratedModels(Class<?> request, Class<?> response) {
    }

    /** A class file the compiler reads from this bundle rather than from disk. */
    private static final class Bytecode extends SimpleJavaFileObject {
        private final String binaryName;
        private final byte[] bytecode;

        Bytecode(String binaryName, byte[] bytecode) {
            super(URI.create("bundle:///" + binaryName.replace('.', '/') + ".class"),
                    JavaFileObject.Kind.CLASS);
            this.binaryName = binaryName;
            this.bytecode = bytecode;
        }

        @Override
        public java.io.InputStream openInputStream() {
            return new java.io.ByteArrayInputStream(bytecode);
        }
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

        /**
         * The classes the generated source refers to besides {@code java.*}.
         *
         * <p>The compiler runs inside the server, where its own class path is
         * the launcher's and not this bundle's, so asking it to read
         * {@code @ForyField} from disk would fail at deployment - the one
         * place a unit test would never look. They are served from the
         * classloader that already has them: this bundle's.
         */
        private static final String FORY_ANNOTATIONS = "org.apache.fory.annotation";
        private static final java.util.List<String> FORY_ANNOTATION_CLASSES = java.util.List.of(
                "org.apache.fory.annotation.ForyField",
                "org.apache.fory.annotation.ForyField$Dynamic");

        private final Map<String, byte[]> classes = new HashMap<>();

        MemoryFileManager(JavaFileManager delegate) {
            super(delegate);
        }

        @Override
        public Iterable<JavaFileObject> list(Location location, String packageName,
                                             java.util.Set<JavaFileObject.Kind> kinds, boolean recurse)
                throws java.io.IOException {
            if (location == javax.tools.StandardLocation.CLASS_PATH
                    && FORY_ANNOTATIONS.equals(packageName)
                    && kinds.contains(JavaFileObject.Kind.CLASS)) {
                java.util.List<JavaFileObject> found = new java.util.ArrayList<>(2);
                for (String name : FORY_ANNOTATION_CLASSES) {
                    byte[] bytecode = read(name);
                    if (bytecode != null) {
                        found.add(new Bytecode(name, bytecode));
                    }
                }
                return found;
            }
            return super.list(location, packageName, kinds, recurse);
        }

        @Override
        public String inferBinaryName(Location location, JavaFileObject file) {
            if (file instanceof Bytecode bytecode) {
                return bytecode.binaryName;
            }
            return super.inferBinaryName(location, file);
        }

        private static byte[] read(String binaryName) {
            String resource = '/' + binaryName.replace('.', '/') + ".class";
            try (java.io.InputStream in = ForyRuntimeModelGenerator.class.getResourceAsStream(resource)) {
                return in == null ? null : in.readAllBytes();
            } catch (java.io.IOException e) {
                return null;
            }
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
