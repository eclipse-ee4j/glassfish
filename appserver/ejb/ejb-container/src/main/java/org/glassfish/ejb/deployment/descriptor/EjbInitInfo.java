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

package org.glassfish.ejb.deployment.descriptor;

import com.sun.enterprise.deployment.MethodDescriptor;

import org.glassfish.deployment.common.Descriptor;

/**
 * Contains information about a stateful session bean init method.
 */
public class EjbInitInfo extends Descriptor {

    private static final long serialVersionUID = 1L;
    private MethodDescriptor beanMethod;
    private MethodDescriptor createMethod;

    public MethodDescriptor getBeanMethod() {
        return beanMethod;
    }


    public void setBeanMethod(MethodDescriptor method) {
        beanMethod = method;
    }


    public MethodDescriptor getCreateMethod() {
        return createMethod;
    }


    public void setCreateMethod(MethodDescriptor method) {
        createMethod = method;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("bean method = ").append(beanMethod).append('\t');
        sb.append("create method = ").append(createMethod);
        return sb.toString();
    }
}
