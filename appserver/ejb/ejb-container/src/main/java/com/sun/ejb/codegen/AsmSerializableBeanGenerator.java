/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.codegen;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V11;

public class AsmSerializableBeanGenerator {

    private static final int INTF_FLAGS = ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES;

    private final ClassLoader loader;
    private final Class<?> baseClass;
    private final String subclassName;


    /**
     * Adds _Serializable to the original name.
     *
     * @param beanClass full class name
     */
    public static String getGeneratedSerializableClassName(String beanClass) {
        String packageName = Generator.getPackageName(beanClass);
        String simpleName = Generator.getBaseName(beanClass);
        String generatedSimpleName = "_" + simpleName + "_Serializable";
        return packageName == null ? generatedSimpleName : packageName + "." + generatedSimpleName;
    }


    public AsmSerializableBeanGenerator(ClassLoader loader, Class baseClass, String serializableSubclassName) {
        this.loader = loader;
        this.baseClass = baseClass;
        this.subclassName = serializableSubclassName;
    }

    public String getSerializableSubclassName() {
        return subclassName;
    }


    public Class generateSerializableSubclass() throws Exception {
        ClassWriter cw = new ClassWriter(INTF_FLAGS);

        //ClassVisitor tv = //(_debug)
        //new TraceClassVisitor(cw, new PrintWriter(System.out));
        ClassVisitor tv = cw;
        String subclassInternalName = subclassName.replace('.', '/');

        String[] interfaces = new String[] {
            Type.getType(Serializable.class).getInternalName()
        };

        tv.visit(V11, ACC_PUBLIC,
            subclassInternalName, null,
            Type.getType(baseClass).getInternalName(), interfaces);


        // Generate constructor. The EJB spec only allows no-arg constructors, but
        // CDI added requirements that allow a single constructor to define
        // parameters injected by CDI.

        Constructor<?>[] ctors = baseClass.getConstructors();
        Constructor<?> ctorWithParams = null;
        for(Constructor<?> ctor : ctors) {
            if(ctor.getParameterTypes().length == 0) {
                ctorWithParams = null;    //exists the no-arg ctor, use it
                break;
            } else if(ctorWithParams == null) {
                ctorWithParams = ctor;
            }
        }

        int numArgsToPass = 1; // default is 1 to just handle 'this'
        String paramTypeString = "()V";

        if (ctorWithParams != null) {
            Class<?>[] paramTypes = ctorWithParams.getParameterTypes();
            numArgsToPass = paramTypes.length + 1;
            paramTypeString = Type.getConstructorDescriptor(ctorWithParams);
        }

        MethodVisitor ctorv = tv.visitMethod(ACC_PUBLIC, "<init>", paramTypeString, null, null);

        for (int i = 0; i < numArgsToPass; i++) {
            ctorv.visitVarInsn(ALOAD, i);
        }
        ctorv.visitMethodInsn(INVOKESPECIAL,  Type.getType(baseClass).getInternalName(), "<init>", paramTypeString, false);
        ctorv.visitInsn(RETURN);
        ctorv.visitMaxs(numArgsToPass, numArgsToPass);

        MethodVisitor cv = cw.visitMethod(ACC_PRIVATE, "writeObject", "(Ljava/io/ObjectOutputStream;)V", null, new String[] { "java/io/IOException" });
        cv.visitVarInsn(ALOAD, 0);
        cv.visitVarInsn(ALOAD, 1);
        cv.visitMethodInsn(INVOKESTATIC, "com/sun/ejb/EJBUtils", "serializeObjectFields", "(Ljava/lang/Object;Ljava/io/ObjectOutputStream;)V", false);
        cv.visitInsn(RETURN);
        cv.visitMaxs(2, 2);


        cv = cw.visitMethod(ACC_PRIVATE, "readObject", "(Ljava/io/ObjectInputStream;)V", null, new String[] { "java/io/IOException", "java/lang/ClassNotFoundException" });
        cv.visitVarInsn(ALOAD, 0);
        cv.visitVarInsn(ALOAD, 1);
        cv.visitMethodInsn(INVOKESTATIC, "com/sun/ejb/EJBUtils", "deserializeObjectFields", "(Ljava/lang/Object;Ljava/io/ObjectInputStream;)V", false);
        cv.visitInsn(RETURN);
        cv.visitMaxs(2, 2);

        tv.visitEnd();

        byte[] classData = cw.toByteArray();

        PrivilegedAction<Class<?>> action = () -> ClassGenerator.defineClass(loader, subclassName, classData,
            baseClass.getProtectionDomain());
        return AccessController.doPrivileged(action);
    }
}
