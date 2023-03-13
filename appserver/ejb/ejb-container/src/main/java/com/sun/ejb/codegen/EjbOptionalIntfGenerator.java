/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
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

import com.sun.ejb.spi.container.OptionalLocalInterfaceProvider;
import com.sun.enterprise.container.common.spi.util.IndirectlySerializable;
import com.sun.enterprise.container.common.spi.util.SerializableObjectFactory;
import com.sun.enterprise.deployment.util.TypeUtil;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V11;

public class EjbOptionalIntfGenerator {

    private static final int INTF_FLAGS = ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES;

    private static final String DELEGATE_FIELD_NAME = "__ejb31_delegate";

    private final Map<String, byte[]> classMap = new HashMap<>();

    private final ClassLoader loader;

    private ProtectionDomain protectionDomain;

    public EjbOptionalIntfGenerator(ClassLoader loader) {
        this.loader = loader;
    }


    public Class loadClass(final String name) throws ClassNotFoundException {
        Class clz = null;
        try {
            clz = loader.loadClass(name);
        } catch (ClassNotFoundException cnfe) {
            final byte[] classData = classMap.get(name);
            if (classData != null) {
                PrivilegedAction<Class<?>> action = () -> ClassGenerator.defineClass(loader, name, classData,
                    protectionDomain);
                clz = AccessController.doPrivileged(action);
            }
        }
        if (clz == null) {
            throw new ClassNotFoundException(name);
        }
        return clz;
    }

    public void generateOptionalLocalInterface(Class ejbClass, String intfClassName)
        throws Exception {

        generateInterface(ejbClass, intfClassName, Serializable.class);
    }

    public void generateInterface(Class ejbClass, String intfClassName, final Class... interfaces) throws Exception {
        String[] interfaceNames = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            interfaceNames[i] = Type.getType(interfaces[i]).getInternalName();
        }

        if (protectionDomain == null) {
            protectionDomain = ejbClass.getProtectionDomain();
        }

        ClassWriter cw = new ClassWriter(INTF_FLAGS);

        ClassVisitor tv = cw;
        String intfInternalName = intfClassName.replace('.', '/');
        tv.visit(V11, ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE,
                intfInternalName, null,
                Type.getType(Object.class).getInternalName(),
                interfaceNames );

        for (java.lang.reflect.Method m : ejbClass.getMethods()) {
            if (qualifiedAsBeanMethod(m)) {
                generateInterfaceMethod(tv, m);
            }
        }

        tv.visitEnd();

        byte[] classData = cw.toByteArray();
        classMap.put(intfClassName, classData);
    }

    /**
     * Determines if a method from a bean class can be considered as a business
     * method for EJB of no-interface view.
     * @param m a public method
     * @return true if m can be included as a bean business method.
     */
    private boolean qualifiedAsBeanMethod(java.lang.reflect.Method m) {
        if (m.getDeclaringClass() == Object.class) {
            return false;
        }
        int mod = m.getModifiers();
        return !Modifier.isStatic(mod) && !Modifier.isFinal(mod);
    }

    private boolean hasSameSignatureAsExisting(java.lang.reflect.Method toMatch,
                                               Set<java.lang.reflect.Method> methods) {
        boolean sameSignature = false;
        for(java.lang.reflect.Method m : methods) {
            if( TypeUtil.sameMethodSignature(m, toMatch) ) {
                sameSignature = true;
                break;
            }
        }
        return sameSignature;
    }


    public void generateOptionalLocalInterfaceSubClass(Class superClass, String subClassName, Class delegateClass)
        throws Exception {
        generateSubclass(superClass, subClassName, delegateClass, IndirectlySerializable.class);
    }


    public void generateSubclass(Class superClass, String subClassName, Class delegateClass, Class... interfaces)
        throws Exception {
        if (protectionDomain == null) {
            protectionDomain = superClass.getProtectionDomain();
        }

        ClassWriter cw = new ClassWriter(INTF_FLAGS);

        ClassVisitor tv = cw;

        String[] interfaceNames = new String[interfaces.length + 1];
        interfaceNames[0] = OptionalLocalInterfaceProvider.class.getName().replace('.', '/');
        for (int i = 0; i < interfaces.length; i++) {
            interfaceNames[i+1] = interfaces[i].getName().replace('.', '/');
        }

        tv.visit(V11, ACC_PUBLIC, subClassName.replace('.', '/'), null,
                Type.getType(superClass).getInternalName(), interfaceNames);

        String fldDesc = Type.getDescriptor(delegateClass);
        FieldVisitor fv = tv.visitField(ACC_PRIVATE, DELEGATE_FIELD_NAME,
                fldDesc, null, null);
        fv.visitEnd();

        // Generate constructor. The EJB spec only allows no-arg constructors, but
        // CDI added requirements that allow a single constructor to define
        // parameters injected by CDI.
        {

            Constructor[] ctors = superClass.getConstructors();
            Constructor ctorWithParams = null;
            for(Constructor ctor : ctors) {
                if(ctor.getParameterTypes().length == 0) {
                    ctorWithParams = null;    //exists the no-arg ctor, use it
                    break;
                } else if(ctorWithParams == null) {
                    ctorWithParams = ctor;
                }
            }

            MethodVisitor cv = tv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            cv.visitVarInsn(ALOAD, 0);
            String paramTypeString = "()V";
            // if void, only one param (implicit 'this' param)
            int maxValue = 1;
            if (ctorWithParams != null) {
                Class[] paramTypes = ctorWithParams.getParameterTypes();
                for (Class paramType : paramTypes) {
                    cv.visitInsn(ACONST_NULL);
                }
                paramTypeString = Type.getConstructorDescriptor(ctorWithParams);
                // num params + one for 'this' pointer
                maxValue = paramTypes.length + 1;
            }
            cv.visitMethodInsn(INVOKESPECIAL,  Type.getType(superClass).getInternalName(), "<init>", paramTypeString, false);
            cv.visitInsn(RETURN);
            cv.visitMaxs(maxValue, 1);
        }

        generateSetDelegateMethod(tv, delegateClass, subClassName);

        for (Class anInterface : interfaces) {

            // dblevins: Don't think we need this special case.
            // Should be covered by letting generateBeanMethod
            // handle the methods on IndirectlySerializable.
            //
            // Not sure where the related tests are to verify.
            if (anInterface.equals(IndirectlySerializable.class)) {
                generateGetSerializableObjectFactoryMethod(tv, fldDesc, subClassName.replace('.', '/'));
                continue;
            }

            for (java.lang.reflect.Method method : anInterface.getMethods()) {
                generateBeanMethod(tv, subClassName, method, delegateClass);
            }
        }


        Set<java.lang.reflect.Method> allMethods = new HashSet<>();

        for (java.lang.reflect.Method m : superClass.getMethods()) {
            if (qualifiedAsBeanMethod(m)) {
                generateBeanMethod(tv, subClassName, m, delegateClass);
            }
        }

        for (Class clz = superClass; clz != Object.class; clz = clz.getSuperclass()) {
            java.lang.reflect.Method[] beanMethods = clz.getDeclaredMethods();
            for (java.lang.reflect.Method mth : beanMethods) {
                if( !hasSameSignatureAsExisting(mth, allMethods)) {
                    int modifiers = mth.getModifiers();
                    boolean isPublic = Modifier.isPublic(modifiers);
                    boolean isPrivate = Modifier.isPrivate(modifiers);
                    boolean isProtected = Modifier.isProtected(modifiers);
                    boolean isPackage = !isPublic && !isPrivate && !isProtected;

                    boolean isStatic = Modifier.isStatic(modifiers);

                    if( (isPackage || isProtected) && !isStatic ) {
                        generateNonAccessibleMethod(tv, mth);
                    }
                    allMethods.add(mth);
                }
            }
        }

        // add toString() method if it was not overridden
        java.lang.reflect.Method mth = Object.class.getDeclaredMethod("toString");
        if (!hasSameSignatureAsExisting(mth, allMethods)) {
            generateToStringBeanMethod(tv, superClass);
        }

        tv.visitEnd();

        byte[] classData = cw.toByteArray();
        classMap.put(subClassName, classData);
    }


    private static void generateInterfaceMethod(ClassVisitor cv, java.lang.reflect.Method m)
        throws Exception {

        String methodName = m.getName();
        Type returnType = Type.getReturnType(m);
        Type[] argTypes = Type.getArgumentTypes(m);

        Method asmMethod = new Method(methodName, returnType, argTypes);
        GeneratorAdapter cg = new GeneratorAdapter(ACC_PUBLIC + ACC_ABSTRACT,
                asmMethod, null, getExceptionTypes(m), cv);
        cg.endMethod();

    }

    private static void generateBeanMethod(ClassVisitor cv, String subClassName,
                                           java.lang.reflect.Method m, Class delegateClass)
        throws Exception {

        String methodName = m.getName();
        Type returnType = Type.getReturnType(m);
        Type[] argTypes = Type.getArgumentTypes(m);
        Method asmMethod = new Method(methodName, returnType, argTypes);

        GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, asmMethod, null,
                getExceptionTypes(m), cv);
        mg.loadThis();
        mg.visitFieldInsn(GETFIELD, subClassName.replace('.', '/'),
                DELEGATE_FIELD_NAME, Type.getType(delegateClass).getDescriptor());
        mg.loadArgs();
        mg.invokeInterface(Type.getType(delegateClass), asmMethod);
        mg.returnValue();
        mg.endMethod();

    }

    private static void generateToStringBeanMethod(ClassVisitor cv, Class superClass)
        throws Exception {

        String toStringMethodName = "toString";
        String toStringMethodDescriptor = "()Ljava/lang/String;";
        String stringBuilder = "java/lang/StringBuilder";

        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, toStringMethodName, toStringMethodDescriptor, null, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitTypeInsn(NEW, stringBuilder);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, stringBuilder, "<init>", "()V", false);
        mv.visitLdcInsn(superClass.getName() + "@");
        mv.visitMethodInsn(INVOKEVIRTUAL, stringBuilder, "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "toHexString", "(I)Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, stringBuilder, "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, stringBuilder, toStringMethodName, toStringMethodDescriptor, false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(2, 1);

    }

    private static void generateNonAccessibleMethod(ClassVisitor cv,
                                           java.lang.reflect.Method m)
        throws Exception {

        String methodName = m.getName();
        Type returnType = Type.getReturnType(m);
        Type[] argTypes = Type.getArgumentTypes(m);
        Method asmMethod = new Method(methodName, returnType, argTypes);

        // Only called for non-static Protected or Package access
        int access =  ACC_PUBLIC;

        GeneratorAdapter mg = new GeneratorAdapter(access, asmMethod, null,
                getExceptionTypes(m), cv);

        mg.throwException(Type.getType(jakarta.ejb.EJBException.class),
                "Illegal non-business method access on no-interface view");

        mg.returnValue();

        mg.endMethod();

    }

    private static void generateGetSerializableObjectFactoryMethod(ClassVisitor classVisitor,
                                                                   String fieldDesc,
                                                                   String classDesc) {

        MethodVisitor cv = classVisitor.visitMethod(ACC_PUBLIC, "getSerializableObjectFactory",
                "()L" + SerializableObjectFactory.class.getName().replace('.', '/') + ";", null, new String[] { "java/io/IOException" });
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(GETFIELD, classDesc, DELEGATE_FIELD_NAME, fieldDesc);
        cv.visitTypeInsn(CHECKCAST, IndirectlySerializable.class.getName().replace('.', '/'));
        cv.visitMethodInsn(INVOKEINTERFACE,
                IndirectlySerializable.class.getName().replace('.', '/'), "getSerializableObjectFactory",
                "()L" + SerializableObjectFactory.class.getName().replace('.', '/') + ";", true);
        cv.visitInsn(ARETURN);
        cv.visitMaxs(1, 1);
    }


    private static Type[] getExceptionTypes(java.lang.reflect.Method m) {
        Class[] exceptions = m.getExceptionTypes();
        Type[] eTypes = new Type[exceptions.length];
        for (int i=0; i<exceptions.length; i++) {
            eTypes[i] = Type.getType(exceptions[i]);
        }

        return eTypes;
    }

    private static void generateSetDelegateMethod(ClassVisitor cv, Class delegateClass,
                                                  String subClassName)
        throws Exception {

        Class optProxyClass = OptionalLocalInterfaceProvider.class;
        java.lang.reflect.Method proxyMethod = optProxyClass.getMethod(
                "setOptionalLocalIntfProxy", java.lang.reflect.Proxy.class);

        String methodName = proxyMethod.getName();
        Type returnType = Type.getReturnType(proxyMethod);
        Type[] argTypes = Type.getArgumentTypes(proxyMethod);
        Type[] eTypes = getExceptionTypes(proxyMethod);

        Method asmMethod = new Method(methodName, returnType, argTypes);
        GeneratorAdapter mg2 = new GeneratorAdapter(ACC_PUBLIC, asmMethod, null, eTypes, cv);
        mg2.visitVarInsn(ALOAD, 0);
        mg2.visitVarInsn(ALOAD, 1);
        mg2.visitTypeInsn(CHECKCAST, delegateClass.getName().replace('.', '/'));
        String delIntClassDesc = Type.getType(delegateClass).getDescriptor();
        mg2.visitFieldInsn(PUTFIELD, subClassName.replace('.', '/'), DELEGATE_FIELD_NAME, delIntClassDesc);
        mg2.returnValue();
        mg2.endMethod();
    }
}
