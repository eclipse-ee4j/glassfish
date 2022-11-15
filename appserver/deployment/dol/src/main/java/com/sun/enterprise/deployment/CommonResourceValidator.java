/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Vector;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.deployment.common.Descriptor;

/**
 * @author naman, date: 24/5/12
 */
public class CommonResourceValidator {

    private Descriptor descriptor;
    private final SimpleJndiName jndiName;
    private Vector<String> scope;

    public CommonResourceValidator(Descriptor descriptor, SimpleJndiName jndiName, Vector<String> scope) {
        this.setDescriptor(descriptor);
        this.jndiName = jndiName;
        this.setScope(scope);
    }

    public Descriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(Descriptor descriptor) {
        this.descriptor = descriptor;
    }

    public SimpleJndiName getJndiName() {
        return jndiName;
    }

    public Vector<String> getScope() {
        return scope;
    }

    public void setScope(Vector<String> scope) {
        this.scope = scope;
    }
}
