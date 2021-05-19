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
import org.glassfish.ha.store.apt.processor.ClassVisitor;

/**
 * @author Mahesh Kannan
 */
public class StoreEntryMetadataGenerator
        extends AbstractGenerator
        implements ClassVisitor {

    private String className;

    private int index = 0;

    public void visit(String packageName, String javaDoc, String className) {
        this.className = className;
        println("package " + packageName + ";");
        println();
        println("import java.util.ArrayList;");
        println("import java.util.Collection;");
        println("import java.util.HashMap;");
        println("import java.util.Map;");
        println();
        println("import org.glassfish.ha.store.spi.AttributeMetadata;");
        println();
        println("/**");
        println(" * Metadata for " + className);
        println(" *");
        println(" */");
        println();
        println("public class MetadataFor" + className + " {");
        increaseIndent();
        println();
    }

    public void visitSetter(String methodName, String attrName, String javaDoc, TypeMirror paramType) {
        printInfo(methodName, attrName, javaDoc, paramType, "Attribute");
    }

    public void visitVersionMethod(String methodName, String attrName, String javaDoc, TypeMirror paramType) {
        printInfo(methodName, attrName, javaDoc, paramType, "Version");
    }

    public void visitHashKeyMethod(String methodName, String attrName, String javaDoc, TypeMirror paramType) {
        printInfo(methodName, attrName, javaDoc, paramType, "HashKey");
    }

    private void printInfo(String methodName, String attrName, String javaDoc, TypeMirror paramType, String token) {
        attrNames.add(attrName);
        println("//@" + token + "(name=\"" + attrName + "\")");
        println("public static AttributeMetadata<" + className + ", " + getWrapperType(paramType) + "> "
                + attrName + " = ");
        println("\tnew AttributeMetadataImpl<" + className + ", " + getWrapperType(paramType) + ">("
                + index++ + ", \"" + attrName + "\", " + className + ".class" + ", " + getWrapperType(paramType) + ".class"
                + ", \"" + token + "\");");
        println();
    }

    public void visitEnd() {
        //generateStoreEntryMetadataMethods();
        println();
        println("public Collection<AttributeMetadata<" + className + ", ?>> getAllAttributeMetadata() {");
        println("\t return attributes__;");
        println("}");
        println();
        println("private static Collection<AttributeMetadata<" + className + ", ?>> attributes__");
        println("\t= new ArrayList<AttributeMetadata<" + className + ", ?>>();");
        println();
        print("static {");
        increaseIndent();

        println();
        for (String attr : attrNames) {
            println("attributes__.add(" + attr + ");");
        }
        decreaseIndent();
        println("}");

        printAttibuteMetaDataImplClass();
        decreaseIndent();
        println("}");
        println();
    }

    private void generateStoreEntryMetadataMethods() {
        /*
        println("public AttributeMetadata<" + className + ", ?> getAttributeMetadata("
            + "String attrName) {");
        println("\treturn attrMap__.get(attrName);");
        println("}");

        println();
        println("public Collection<AttributeMetadata<" + className + ", ?>> getAllAttributeMetadata() {");
        println("\t return attrMap__.values();");
        println("}");

        println();
        println("public Collection<String> getAllAttributeNames() {");
        println("\t return attributes__;");
        println("}");
        */
        println();
    }

    private void printAttibuteMetaDataImplClass() {
        println("private static class AttributeMetaDataImpl<V, T>");
        increaseIndent();
            println("implements AttributeMetaData<V, T> {");
            println();
            println("int index;");
            println("String attrName;");
            println("Class<V> vClazz;");
            println("T type;");
            println("String token;");
            println();
            println("public AttributeMetaDataImpl(int index, String attrName, Class<V> vClazz, Class<T> type, ");
            increaseIndent();
                println("String token) {");
                println("this.index = index;");
                println("this.attrName = attrName;");
                println("this.vClazz = vClazz;");
                println("this.type = type;");
                println("this.token = token;");
            decreaseIndent();
            println("}");
            println();
            println("public Class<T> getAttributeType() {");
            increaseIndent();
                println("return type;");
            decreaseIndent();
            println("}");
            println();
            println("public boolean isVersionAttribute() {");
            increaseIndent();
                println("return \"Version\".equals(token);");
            decreaseIndent();
            println("}");
            println();
            println("public Method isHashKeyAttribute() {");
            increaseIndent();
                println("return \"HashKey\".equals(token);");
            decreaseIndent();
            println("}");
            println();

        decreaseIndent();
        println("}");
    }
}
