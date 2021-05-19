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

package org.glassfish.ha.store.apt.generators;

import com.sun.mirror.type.TypeMirror;

import java.util.StringTokenizer;

import org.glassfish.ha.store.apt.processor.ClassVisitor;

/**
 * @author Mahesh Kannan
 */
public class StorableGenerator
        extends AbstractGenerator
        implements ClassVisitor {

    private static final String ATTR_NAMES = "_dirtyAttributeNames";

    private String versionGetterMethodName;

    public void visit(String packageName, String javaDoc, String className) {
        println("package " + packageName + ";");
        println();
        println("import java.util.Set;");
        println("import java.util.HashSet;");
        println();
        println("import org.glassfish.ha.store.spi.Storable;");
        println("import org.glassfish.ha.store.MutableStoreEntry;");
        println();
        println("/**");
        StringTokenizer st = new StringTokenizer(javaDoc, "\n");
        while (st.hasMoreTokens()) {
            println(" * " + st.nextToken() + "\n * ");
        }
        println(" */");
        println();
        println("public class Storable" + className + "__");
        increaseIndent();
        println("extends " + className);
        println("implements Storable, MutableStoreEntry {");
        println();
        println("private String _storeName;");
        println();
        println("private String _hashKey;");
        println();
        println("private Set<String> " + ATTR_NAMES + " = new HashSet<String>();");
        println();
    }

    private void handleDirtyAttribute(String setterMethodName, String attrName, TypeMirror paramType) {
        super.addAttribute(attrName, paramType);
        println("public void " + setterMethodName + "("
                + getWrapperType(paramType) + " value) { ");
        increaseIndent();
        println("_markAsDirty(\"" + attrName + "\");");
        println("super." + setterMethodName + "(value);");
        decreaseIndent();
    }

    public void visitSetter(String setterMethodName, String attrName, String javaDoc, TypeMirror paramType) {
        println("//@Attribute(name=\"" + attrName + "\")");
        handleDirtyAttribute(setterMethodName, attrName, paramType);
        println("}");
        println();
    }

    public void visitVersionMethod(String setterMethodName, String attrName, String javaDoc, TypeMirror paramType) {
        versionGetterMethodName = setterMethodName;
        println("//@Version(name=\"" + attrName + "\")");
        handleDirtyAttribute(setterMethodName, attrName, paramType);
        println("}");
        println();
    }

    public void visitEnd() {
        println("//Storable method");
        println("public String _getStoreName() {");
        increaseIndent();
        println("return _storeName;");
        decreaseIndent();
        println("}");
        println();

        String getVersionName = (versionGetterMethodName == null)
                ? null : versionGetterMethodName;
        if (getVersionName != null) {
            getVersionName = "g" + getVersionName.substring(1);
        }

        println("public String _getVersion() {");
        increaseIndent();
        println("return " + getVersionName + "();");
        decreaseIndent();
        println("}");
        println();

        println("public Set<String> _getDirtyAttributeNames() {");
        increaseIndent();
        println("return " + ATTR_NAMES + ";");
        decreaseIndent();
        println("}");
        println();

        println("//MutableStoreEntry methods");
        println("public void _markAsDirty(String attrName) {");
        increaseIndent();
        println(ATTR_NAMES + ".add(attrName);");
        decreaseIndent();
        println("}");
        println();

        println("public void _markAsClean(String attrName) {");
        increaseIndent();
        println(ATTR_NAMES + ".remove(attrName);");
        decreaseIndent();
        println("}");
        println();

        println("public void _markAsClean() {");
        increaseIndent();
        println(ATTR_NAMES + " = new HashSet<String>();");
        decreaseIndent();
        println("}");
        println();

        decreaseIndent();
        println("}");
    }
}
