/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli.schemadoc;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.ASM9;

public class DocClassVisitor extends ClassVisitor {

    private boolean hasConfiguredAnnotation = false;
    private String className;
    private List<String> interfaces;
    private ClassDef classDef;
    private boolean showDeprecated;

    public DocClassVisitor(final boolean showDep) {
        super(ASM9);
        showDeprecated = showDep;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] intfs) {
        className = GenerateDomainSchema.toClassName(name);
        interfaces = new ArrayList<>();
        for (String intf : intfs) {
            interfaces.add(GenerateDomainSchema.toClassName(intf));
        }
        classDef = new ClassDef(className, interfaces);
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    @Override
    public void visitSource(String source, String debug) {
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        hasConfiguredAnnotation |= "Lorg/jvnet/hk2/config/Configured;".equals(desc);
        if ("Ljava/lang/Deprecated;".equals(desc) && classDef != null) {
            classDef.setDeprecated(true);
        }
        return null;
    }

    @Override
    public void visitAttribute(Attribute attr) {
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        String type = null;
        try {
            if (showDeprecated || ((access & Opcodes.ACC_DEPRECATED) != Opcodes.ACC_DEPRECATED)) {
                if (hasConfiguredAnnotation) {
                    if (signature != null) {
                        type = GenerateDomainSchema
                                .toClassName(signature.substring(signature.indexOf("<") + 1, signature.lastIndexOf(">") - 1));
                    } else {
                        type = GenerateDomainSchema.toClassName(desc);
                    }
                }
            }
        } catch (StringIndexOutOfBoundsException e) {
            throw new RuntimeException(e.getMessage());
        }
        return name.startsWith("get") && type != null ? new AttributeMethodVisitor(classDef, access, name, type) : null;
    }

    /**
     * Visits the end of the class. This method, which is the last one to be called, is used to inform the visitor that all
     * the fields and methods of the class have been visited.
     */
    @Override
    public void visitEnd() {
    }

    public ModuleVisitor visitModule() {
        return null;
    }

    public boolean isConfigured() {
        return hasConfiguredAnnotation;
    }

    public ClassDef getClassDef() {
        return hasConfiguredAnnotation ? classDef : null;
    }

    @Override
    public String toString() {
        return "DocClassVisitor{" + "className='" + className + '\'' + ", hasConfiguredAnnotation=" + hasConfiguredAnnotation + '}';
    }
}
