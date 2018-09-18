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

package com.sun.enterprise.tools.verifier.apiscan.classfile;

import org.objectweb.asm.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.ref.SoftReference;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
class ASMMethod extends MethodVisitor implements Method{

    private static String resourceBundleName = "com.sun.enterprise.tools.verifier.apiscan.LocalStrings";    
    private static Logger logger = Logger.getLogger("apiscan.classfile", resourceBundleName); // NOI18N

    private SoftReference<ClassFile> owningClass;

    private String descriptor;

    private int access;

    private String signature;

    private String[] exceptions;

    private String name;

    private Set<MethodRef> referencedMethods = new HashSet<MethodRef>();

    private Set<String> referencedClasses = new HashSet<String>();

    // A reference to represent itself
    private MethodRef methodRef;

    public ASMMethod(
            ClassFile owningClass, String name, String descriptor, int access,
            String signature, String[] exceptions) {
        super(Opcodes.ASM6);
        this.owningClass = new SoftReference<ClassFile>(owningClass);
        this.name = name;
        this.descriptor = descriptor;
        this.access = access;
        this.signature = signature;
        if(exceptions==null) {
            this.exceptions = new String[0];
        } else {
            this.exceptions = exceptions;
        }
    }

    public ClassFile getOwningClass() {
        return owningClass.get();
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public Collection<MethodRef> getReferencedMethods() {
        return Collections.unmodifiableCollection(referencedMethods);
    }

    public Collection<String> getReferencedClasses() {
        return Collections.unmodifiableCollection(referencedClasses);
    }

    // TODO: Not yet synchronized.
    public MethodRef getSelfReference() {
        if(methodRef==null){
            methodRef = new MethodRef(owningClass.get().getInternalName(), name, descriptor);
        }
        return methodRef;
    }

    public boolean isNative() {
        return (access & Opcodes.ACC_NATIVE) == Opcodes.ACC_NATIVE;
    }

    @Override public void visitFieldInsn(
            int opcode, String owner, String name, String desc) {
//        logger.entering(
//                "com.sun.enterprise.tools.verifier.apiscan.classfile.ASMMethod", "visitFieldInsn", // NOI18N
//                new Object[]{AbstractVisitor.OPCODES[opcode], owner, name, desc});
        addClass(owner);
    }

    @Override public void visitTryCatchBlock(
            Label start, Label end, Label handler, String type) {
        logger.entering(
                "com.sun.enterprise.tools.verifier.apiscan.classfile.ASMMethod", "visitTryCatchBlock", // NOI18N
                new Object[]{type});
        if(type!=null) { // try-finally comes as null
            addClass(type);
        }
    }

    public void visitMethodInsn(
            int opcode, String owner, String name, String desc) {
//        logger.entering(
//                "com.sun.enterprise.tools.verifier.apiscan.classfile.ASMMethod", "visitMethodInsn", new Object[]{ // NOI18N
//                    AbstractVisitor.OPCODES[opcode], owner, name, desc});
        addMethod(owner, name, desc);
    }

    // things like instanceof, checkcast, new, newarray and anewarray
    @Override public void visitTypeInsn(int opcode, String desc) {
//        logger.entering(
//                "com.sun.enterprise.tools.verifier.apiscan.classfile.ASMMethod", "visitTypeInsn", new Object[]{ // NOI18N
//                    AbstractVisitor.OPCODES[opcode], desc});
        switch (opcode) {
            case Opcodes.INSTANCEOF:
            case Opcodes.CHECKCAST:
            case Opcodes.ANEWARRAY:
                addClass(desc);
                break;
            case Opcodes.NEW:
                // skip as class gets added during constructor call.
                break;
            case Opcodes.NEWARRAY:
                // primitive type array, so skip
                break;
            default:
//                logger.logp(Level.WARNING, "com.sun.enterprise.tools.verifier.apiscan.classfile.ASMMethod", "visitTypeInsn", // NOI18N
//                        getClass().getName() + ".warning1", AbstractVisitor.OPCODES[opcode]); // NOI18N
                break;
        }
    }

    @Override public void visitMultiANewArrayInsn(String desc, int dims) {
        logger.entering(
                "com.sun.enterprise.tools.verifier.apiscan.classfile.ASMMethod", "visitMultiANewArrayInsn", // NOI18N
                new Object[]{desc});
        addClass(desc);
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder(
                decodeAccessFlag(access) +
                name +
                " " + // NOI18N
                descriptor+
                "{\n"); // NOI18N
        for(MethodRef mr : referencedMethods){
            sb.append(mr).append("\n"); // NOI18N
        }
        sb.append("}"); // NOI18N
        return sb.toString();
    }

    public int getAccess() {
        return access;
    }

    public String getSignature() {
        return signature;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getExceptions() {
        return exceptions;
    }

    private void addClass(String nameOrTypeDescriptor) {
        //sometimes we get names like Ljava.lang.Integer; or [I. So we need
        // to decode the names.
        if (nameOrTypeDescriptor.indexOf(';') != -1 ||
                nameOrTypeDescriptor.indexOf('[') != -1) {
            referencedClasses.addAll(
                    typeDescriptorToClassNames(nameOrTypeDescriptor));
        } else {
            referencedClasses.add(nameOrTypeDescriptor);
        }
    }

    private void addMethod(String owner, String name, String desc) {
        addClass(owner);
        // We don't need the following code as this is not required.
        // because if everything is null, then no class gets loaded.
//        for(String embeddedClassName : typeDescriptorToClassNames(desc)) {
//            referencedClasses.add(embeddedClassName);
//        }
        referencedMethods.add(new MethodRef(owner, name, desc));
    }

    public static String decodeAccessFlag(int access) {
        StringBuilder result = new StringBuilder("");
        if ((access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE) {
            result.append("private "); // NOI18N
        } else if ((access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED) {
            result.append("protected "); // NOI18N
        } else if ((access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC) {
            result.append("public "); // NOI18N
        }
        if ((access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT) {
            result.append("abstract "); // NOI18N
        } else if ((access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL) {
            result.append("final "); // NOI18N
        } else if ((access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) {
            result.append("static "); // NOI18N
        }
        if ((access & Opcodes.ACC_NATIVE) == Opcodes.ACC_NATIVE) {
            result.append("native "); // NOI18N
        }
        return result.toString();
    }

    private static List<String> typeDescriptorToClassNames(String signature) {
        logger.entering(
                "com.sun.enterprise.tools.verifier.apiscan.classfile.ASMMethod", "typeDescriptorToClassNames", // NOI18N
                new Object[]{signature});
        List<String> result = new ArrayList<String>();
        int i = 0;
        while ((i = signature.indexOf('L', i)) != -1) {
            int j = signature.indexOf(';', i);
            if (j > i) {
                // get name, minus leading 'L' and trailing ';'
                String className = signature.substring(i + 1, j);
                if (!Util.isPrimitive(className)) result.add(className);
                i = j + 1;
            } else
                break;
        }
        if (logger.isLoggable(Level.FINE)) {
            StringBuffer sb = new StringBuffer("Class Names are {"); // NOI18N
            int size = result.size();
            for (int k = 0; k < size; k++) {
                sb.append((String) result.get(k));
                if (k != size - 1) sb.append(", "); // NOI18N
            }
            sb.append("}"); // NOI18N
            logger.finer(sb.toString());
        }
        return result;
    }

}
