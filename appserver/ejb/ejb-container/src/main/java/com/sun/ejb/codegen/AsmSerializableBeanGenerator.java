/*
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
import java.lang.reflect.ReflectPermission;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class AsmSerializableBeanGenerator
implements Opcodes {

    private static final int INTF_FLAGS = ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES;

    private byte[] classData = null;

    private Class loadedClass = null;
    private ClassLoader loader;

    private Class baseClass;

    private String subclassName;

    public AsmSerializableBeanGenerator(ClassLoader loader, Class baseClass, String serializableSubclassName) {
        this.loader = loader;
        this.baseClass = baseClass;
        subclassName = serializableSubclassName;
    }

    public String getSerializableSubclassName() {

        return subclassName;

    }

    public Class generateSerializableSubclass()
        throws Exception {

        try {
            loadedClass = loader.loadClass(subclassName);
            return loadedClass;
        } catch(ClassNotFoundException e) {
            // Not loaded yet.  Just continue
        }

        ClassWriter cw = new ClassWriter(INTF_FLAGS);

        //ClassVisitor tv = //(_debug)
        //new TraceClassVisitor(cw, new PrintWriter(System.out));
        ClassVisitor tv = cw;
        String subclassInternalName = subclassName.replace('.', '/');

        String[] interfaces = new String[] {
            Type.getType(Serializable.class).getInternalName()
        };

        tv.visit(V1_1, ACC_PUBLIC,
            subclassInternalName, null,
            Type.getType(baseClass).getInternalName(), interfaces);


        // Generate constructor. The EJB spec only allows no-arg constructors, but
        // JSR 299 added requirements that allow a single constructor to define
        // parameters injected by CDI.

        Constructor[] ctors = baseClass.getConstructors();
        Constructor ctorWithParams = null;
        for(Constructor ctor : ctors) {
            if(ctor.getParameterTypes().length == 0) {
                ctorWithParams = null;    //exists the no-arg ctor, use it
                break;
            } else if(ctorWithParams == null) {
                ctorWithParams = ctor;
            }
        }

        int numArgsToPass = 1; // default is 1 to just handle 'this'
        String paramTypeString = "()V";

        if( ctorWithParams != null ) {
            Class[] paramTypes = ctorWithParams.getParameterTypes();
            numArgsToPass = paramTypes.length + 1;
            paramTypeString = Type.getConstructorDescriptor(ctorWithParams);
        }

        MethodVisitor ctorv = tv.visitMethod(ACC_PUBLIC, "<init>", paramTypeString, null, null);

        for(int i = 0; i < numArgsToPass; i++) {
            ctorv.visitVarInsn(ALOAD, i);
        }

        ctorv.visitMethodInsn(INVOKESPECIAL,  Type.getType(baseClass).getInternalName(), "<init>",
            paramTypeString);
        ctorv.visitInsn(RETURN);
        ctorv.visitMaxs(numArgsToPass, numArgsToPass);

        MethodVisitor cv = cw.visitMethod(ACC_PRIVATE, "writeObject", "(Ljava/io/ObjectOutputStream;)V", null, new String[] { "java/io/IOException" });
        cv.visitVarInsn(ALOAD, 0);
        cv.visitVarInsn(ALOAD, 1);
        cv.visitMethodInsn(INVOKESTATIC, "com/sun/ejb/EJBUtils", "serializeObjectFields", "(Ljava/lang/Object;Ljava/io/ObjectOutputStream;)V");
        cv.visitInsn(RETURN);
        cv.visitMaxs(2, 2);


        cv = cw.visitMethod(ACC_PRIVATE, "readObject", "(Ljava/io/ObjectInputStream;)V", null, new String[] { "java/io/IOException", "java/lang/ClassNotFoundException" });
        cv.visitVarInsn(ALOAD, 0);
        cv.visitVarInsn(ALOAD, 1);
        cv.visitMethodInsn(INVOKESTATIC, "com/sun/ejb/EJBUtils", "deserializeObjectFields", "(Ljava/lang/Object;Ljava/io/ObjectInputStream;)V");
        cv.visitInsn(RETURN);
        cv.visitMaxs(2, 2);


        /**
        Type[] eTypes = new Type[] { Type.getType(java.io.IOException.class)};

        Method writeObjMethod = Method.getMethod("void writeObject (java.io.ObjectOutputStream)");
        GeneratorAdapter writeObjMethodAdapter =
                new GeneratorAdapter(ACC_PRIVATE, writeObjMethod, null, eTypes, tv);

        Type ejbUtilsType = Type.getType(com.sun.ejb.EJBUtils.class);
        Method ejbUtilsWrite = Method.getMethod
                ("void serializeObjectFields (java.lang.Class, java.lang.Object, java.lang.Object)");


        writeObjMethodAdapter.push(Type.getType(ejbClass));
        writeObjMethodAdapter.loadThis();
        writeObjMethodAdapter.loadArg(0);

        writeObjMethodAdapter.invokeStatic( ejbUtilsType, ejbUtilsWrite);

        writeObjMethodAdapter.endMethod();


        //
        eTypes = new Type[] { Type.getType(java.io.IOException.class),
                              Type.getType(java.lang.ClassNotFoundException.class)};

        Method readObjMethod = Method.getMethod("void readObject (java.io.ObjectInputStream)");
        GeneratorAdapter readObjMethodAdapter =
                new GeneratorAdapter(ACC_PRIVATE, readObjMethod, null, eTypes, tv);


        Method ejbUtilsRead = Method.getMethod
                ("void deserializeObjectFields (java.lang.Class, java.lang.Object, java.lang.Object)");


        readObjMethodAdapter.push(Type.getType(ejbClass));
        readObjMethodAdapter.loadThis();
        readObjMethodAdapter.loadArg(0);

        readObjMethodAdapter.invokeStatic( ejbUtilsType, ejbUtilsRead);

        readObjMethodAdapter.endMethod();

        **/

        tv.visitEnd();

        classData = cw.toByteArray();

        loadedClass = (Class) java.security.AccessController.doPrivileged(
                        new java.security.PrivilegedAction() {
                            public java.lang.Object run() {
                                return makeClass(subclassName, classData, baseClass.getProtectionDomain(), loader);
                            }
                        }
                );

        return loadedClass;
    }

     // A Method for the protected ClassLoader.defineClass method, which we access
    // using reflection.  This requires the supressAccessChecks permission.
    private static final java.lang.reflect.Method defineClassMethod = AccessController.doPrivileged(
        new PrivilegedAction<java.lang.reflect.Method>() {
            public java.lang.reflect.Method run() {
                try {
                    java.lang.reflect.Method meth = ClassLoader.class.getDeclaredMethod(
                        "defineClass", String.class,
                        byte[].class, int.class, int.class,
                        ProtectionDomain.class);
                    meth.setAccessible(true);
                    return meth;
                } catch (Exception exc) {
                    throw new RuntimeException(
                        "Could not find defineClass method!", exc ) ;
                }
            }
        }
    );

    private static final Permission accessControlPermission = new ReflectPermission("suppressAccessChecks");

    // This requires a permission check
    private Class<?> makeClass(String name, byte[] def, ProtectionDomain pd, ClassLoader loader) {
        SecurityManager sman = System.getSecurityManager() ;
        if (sman != null) {
            sman.checkPermission( accessControlPermission ) ;
        }

        try {
            return (Class) defineClassMethod.invoke(loader, name, def, 0, def.length, pd);
        } catch (Exception exc) {
            throw new RuntimeException("Could not invoke defineClass!", exc);
        }
    }
}
