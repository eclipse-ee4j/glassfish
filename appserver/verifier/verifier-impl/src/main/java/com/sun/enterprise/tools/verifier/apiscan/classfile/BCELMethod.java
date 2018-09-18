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

import java.util.Collection;
import java.util.logging.Logger;
import java.lang.ref.SoftReference;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class BCELMethod implements Method{

    private SoftReference<ClassFile> owningClass;

    private com.sun.org.apache.bcel.internal.classfile.Method method;

    private static Logger logger = Logger.getLogger("apiscan.classfile"); // NOI18N

    public BCELMethod(ClassFile owningClass,
                      com.sun.org.apache.bcel.internal.classfile.Method method) {
        logger.entering("BCELMethod", "BCELMethod", // NOI18N
                new Object[]{owningClass.getName(), method.getName()});
        this.owningClass = new SoftReference<ClassFile>(owningClass);
        this.method = method;
    }

    public ClassFile getOwningClass() {
        return owningClass.get();
    }

    public String getName() {
        return method.getName();
    }

    public String getDescriptor() {
        return method.getSignature();
    }

    public int getAccess() {
        return method.getAccessFlags();
    }

    public String getSignature() {
        throw new UnsupportedOperationException();
    }

    public String[] getExceptions() {
        throw new UnsupportedOperationException();
    }

    public Collection<MethodRef> getReferencedMethods() {
        throw new UnsupportedOperationException();
    }

    public Collection<String> getReferencedClasses() {
        throw new UnsupportedOperationException();
    }

    public MethodRef getSelfReference() {
        throw new UnsupportedOperationException();
    }

    public boolean isNative() {
        return method.isNative();
    }
}
