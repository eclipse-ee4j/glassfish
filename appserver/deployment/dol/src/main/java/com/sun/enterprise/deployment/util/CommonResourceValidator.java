/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment.util;

import com.sun.enterprise.deployment.ResourceDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.glassfish.api.naming.SimpleJndiName;

/**
 * @author naman, date: 24/5/12
 * @author David Matejcek 2023
 */
class CommonResourceValidator {

    private final ResourceDescriptor descriptor;
    private final SimpleJndiName jndiName;
    private final List<DuplicitDescriptor> descriptors;

    CommonResourceValidator(ResourceDescriptor descriptor, SimpleJndiName jndiName, String scope) {
        this.descriptor = descriptor;
        this.jndiName = Objects.requireNonNull(jndiName, "jndiName");
        this.descriptors = new ArrayList<>();
        this.descriptors.add(new DuplicitDescriptor(descriptor, scope));
    }

    ResourceDescriptor getDescriptor() {
        return descriptor;
    }

    SimpleJndiName getJndiName() {
        return jndiName;
    }

    /**
     * @return descriptors with the same JNDI name and their scopes
     */
    List<DuplicitDescriptor> getDescriptors() {
        return descriptors;
    }

    void addDuplicity(ResourceDescriptor duplicit, String scope) {
        this.descriptors.add(new DuplicitDescriptor(duplicit, scope));
    }


    static class DuplicitDescriptor {
        public final ResourceDescriptor descriptor;
        public final String scope;

        private DuplicitDescriptor(ResourceDescriptor descriptor, String scope) {
            this.descriptor = descriptor;
            this.scope = scope;
        }

        @Override
        public String toString() {
            return scope;
        }
    }
}
