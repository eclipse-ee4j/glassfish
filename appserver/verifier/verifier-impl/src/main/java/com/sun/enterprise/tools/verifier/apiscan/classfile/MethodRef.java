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

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class MethodRef {
    private String owningClassNameInternal; // in internal form, e.g. com/acme/Address

    private String owningClassName; // in external form, i.e. java.lang.Object

    private String name; // main

    private String descriptor; // ([Ljava.lang.String;)I

    public static final String CLINIT_NAME = "<clinit>"; // NOI18N

    public static final String CLINIT_DESC = "()V"; // NOI18N

    public MethodRef(String owningClassNameInternal, String name, String descriptor) {
        this.owningClassNameInternal = owningClassNameInternal;
        this.owningClassName = Util.convertToExternalClassName(owningClassNameInternal);
        this.name = name;
        this.descriptor = descriptor;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getOwningClassNameInternal() {
        return owningClassNameInternal;
    }

    public String getOwningClassName(){
        return owningClassName;
    }

    public String getName() {
        return name;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodRef)) return false;
        final MethodRef methodRef = (MethodRef) o;
        if (descriptor != null ?
                !descriptor.equals(methodRef.descriptor) :
                methodRef.descriptor != null)
            return false;
        if (name != null ?
                !name.equals(methodRef.name) : methodRef.name != null)
            return false;
        if (owningClassNameInternal != null ?
                !owningClassNameInternal.equals(methodRef.owningClassNameInternal) :
                methodRef.owningClassNameInternal != null)
            return false;
        return true;
    }

    public int hashCode() {
        int result;
        result = (owningClassNameInternal != null ? owningClassNameInternal.hashCode() : 0);
        result = 29 * result + (name != null ? name.hashCode() : 0);
        result = 29 * result +
                (descriptor != null ? descriptor.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return owningClassNameInternal + "." + name + descriptor; // NOI18N
    }
}
