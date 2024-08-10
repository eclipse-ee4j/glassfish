/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.glassfish.flashlight.impl.core;

/**
 * @author Mahesh Kannan
 *         Date: Nov 8, 2009
 * Fixed a bunch of FindBugs problem, 2/2012, Byron Nevins
 */
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.flashlight.FlashlightLoggerInfo;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.ASM9;

public class ProviderSubClassImplGenerator {
    private static final Logger logger = FlashlightLoggerInfo.getLogger();
    public final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ProviderSubClassImplGenerator.class);
    private String invokerId;
    private Class providerClazz;
    private static AtomicInteger counter = new AtomicInteger();

    public ProviderSubClassImplGenerator(Class providerClazz, String invokerId) {
        this.providerClazz = providerClazz;
        this.invokerId = invokerId;
    }

    public <T> Class<T> generateAndDefineClass(final Class<T> providerClazz, String invokerId) {

        int id = counter.incrementAndGet();
        String providerClassName = providerClazz.getName().replace('.', '/');
        String generatedClassName = providerClassName + invokerId + "_" + id;
        byte[] provClassData = null;
        try {
            InputStream is = providerClazz.getClassLoader().getResourceAsStream(providerClassName + ".class");
            int sz = is.available();
            provClassData = new byte[sz];
            int index = 0;
            while (index < sz) {
                int r = is.read(provClassData, index, sz - index);
                if (r > 0) {
                    index += r;
                }
            }
        }
        catch (Exception ex) {
            return null;
        }

        ClassReader cr = new ClassReader(provClassData);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
        byte[] classData = null;
        ProbeProviderSubClassGenerator sgen = new ProbeProviderSubClassGenerator(cw,
                invokerId, "_" + id);
        cr.accept(sgen, 0);
        classData = cw.toByteArray();

        ProtectionDomain pd = providerClazz.getProtectionDomain();

        SubClassLoader scl = createSubClassLoader(providerClazz);

        if(scl == null)
            return null;

        try {
            String gcName = scl.defineClass(generatedClassName, classData, pd);
            if (logger.isLoggable(Level.FINE))
                logger.fine("**** DEFINE CLASS SUCCEEDED for " + gcName + "," + generatedClassName);
            return (Class<T>) scl.loadClass(gcName);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Byron Nevins Feb 2012 FindBugs fix
     * Hide this ugly access control code in this method
     * @return the SubClassLoader or null if error(s)
     */
    private SubClassLoader createSubClassLoader(final Class theClass) {
        try {
            return AccessController.doPrivileged(
                    new PrivilegedExceptionAction<SubClassLoader>() {
                        @Override
                        public SubClassLoader run() throws Exception {
                            return new SubClassLoader(theClass.getClassLoader());
                        }
                    });
        }
        catch (Exception e) {
            return null;
        }
    }

    static class SubClassLoader
            extends ClassLoader {
        SubClassLoader(ClassLoader cl) {
            super(cl);
        }

        String defineClass(String className, byte[] data, ProtectionDomain pd)
                throws Exception {

            className = className.replace('/', '.');
            super.defineClass(className, data, 0, data.length, pd);
            return className;
        }
    }

    private static class ProbeProviderSubClassGenerator
            extends ClassVisitor {
        String superClassName;
        String token;
        String id;

        ProbeProviderSubClassGenerator(ClassVisitor cv, String token, String id) {
            super(ASM9, cv);
            this.id = id;
            this.token = token;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.superClassName = name;
            super.visit(version, access, name + token + id, signature, name, interfaces);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            AnnotationVisitor delegate = super.visitAnnotation(desc, visible);
            if ("Lorg/glassfish/external/probe/provider/annotations/ProbeProvider;".equals(desc)) {
                return new ProbeProviderAnnotationVisitor(delegate, token);
            }
            else {
                return delegate;
            }
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] strings) {

            if ("<init>".equals(name) && desc.equals("()V")) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, strings);
                mv.visitCode();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superClassName, "<init>", desc, false);
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();

                return null;
            }
            else {
                return super.visitMethod(access, name, desc, signature, strings);
            }
        }
    }

    private static class ProbeProviderAnnotationVisitor
            extends AnnotationVisitor {
        private AnnotationVisitor delegate;
        private String token;

        ProbeProviderAnnotationVisitor(AnnotationVisitor delegate, String token) {
            super(ASM9);
            this.delegate = delegate;
            this.token = token;
        }

        @Override
        public void visit(String attrName, Object value) {
            delegate.visit(attrName, ("probeProviderName".equals(attrName) ? value + token : value));
        }

        @Override
        public void visitEnum(String s, String s1, String s2) {
            delegate.visitEnum(s, s1, s2);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String s, String s1) {
            return delegate.visitAnnotation(s, s1);
        }

        @Override
        public AnnotationVisitor visitArray(String s) {
            return delegate.visitArray(s);
        }

        @Override
        public void visitEnd() {
            delegate.visitEnd();
        }
    }
}
