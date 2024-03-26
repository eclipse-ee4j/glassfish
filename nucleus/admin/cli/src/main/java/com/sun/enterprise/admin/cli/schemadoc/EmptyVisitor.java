/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli.schemadoc;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class EmptyVisitor extends MethodVisitor {
    public EmptyVisitor(int api) {
        super(api);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String s, boolean b) {
        return null;
    }

    @Override
    public void visitAttribute(Attribute attribute) {
    }

    @Override
    public void visitEnd() {
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return null;
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int i, String s, boolean b) {
        return null;
    }

    @Override
    public void visitCode() {
    }

    @Override
    public void visitFrame(int i, int i1, Object[] objects, int i2, Object[] objects1) {
    }

    @Override
    public void visitInsn(int i) {
    }

    @Override
    public void visitIntInsn(int i, int i1) {
    }

    @Override
    public void visitVarInsn(int i, int i1) {
    }

    @Override
    public void visitTypeInsn(int i, String s) {
    }

    @Override
    public void visitFieldInsn(int i, String s, String s1, String s2) {
    }

    @Override
    public void visitMethodInsn(int i, String s, String s1, String s2, boolean isInterface) {
    }

    @Override
    public void visitJumpInsn(int i, Label label) {
    }

    @Override
    public void visitLabel(Label label) {
    }

    @Override
    public void visitLdcInsn(Object o) {
    }

    @Override
    public void visitIincInsn(int i, int i1) {
    }

    @Override
    public void visitTableSwitchInsn(int i, int i1, Label label, Label... labels) {
    }

    @Override
    public void visitLookupSwitchInsn(Label label, int[] ints, Label[] labels) {
    }

    @Override
    public void visitMultiANewArrayInsn(String s, int i) {
    }

    @Override
    public void visitTryCatchBlock(Label label, Label label1, Label label2, String s) {
    }

    @Override
    public void visitLocalVariable(String s, String s1, String s2, Label label, Label label1, int i) {
    }

    @Override
    public void visitLineNumber(int i, Label label) {
    }

    @Override
    public void visitMaxs(int i, int i1) {
    }
}
