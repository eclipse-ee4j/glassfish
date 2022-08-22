/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment;

import java.util.Collection;
import java.util.Vector;

/**
 * This class defines a method permission information in the assembly descriptor
 *
 * @author Jerome Dochez
 */
public class MethodPermissionDescriptor extends DescribableDescriptor {

    private static final long serialVersionUID = 1L;
    private final Vector<MethodDescriptor> methods = new Vector<>();
    private final Vector<MethodPermission> mps = new Vector<>();

    /** Creates new MethodPermissionDescriptor */
    public MethodPermissionDescriptor() {
    }


    public void addMethod(MethodDescriptor aMethod) {
        methods.add(aMethod);
    }


    public void addMethods(Collection<MethodDescriptor> methods) {
        this.methods.addAll(methods);
    }


    public void addMethodPermission(MethodPermission mp) {
        mps.add(mp);
    }


    public MethodDescriptor[] getMethods() {
        MethodDescriptor[] array = new MethodDescriptor[methods.size()];
        methods.copyInto(array);
        return array;
    }


    public MethodPermission[] getMethodPermissions() {
        MethodPermission[] array = new MethodPermission[mps.size()];
        mps.copyInto(array);
        return array;
    }


    @Override
    public void print(StringBuffer toStringBuffer) {
        StringBuffer buffer = toStringBuffer;
        if (getDescription() != null) {
            buffer.append("Method Permission ").append(getDescription());
        }
        buffer.append("\nFor the following Permissions ");
        for (MethodPermission mp : mps) {
            mp.print(buffer);
            buffer.append('\n');
        }
        buffer.append("\nFor the following ").append(methods.size()).append(" methods\n");
        for (MethodDescriptor md : methods) {
            md.print(buffer);
            buffer.append('\n');
        }
    }
}
