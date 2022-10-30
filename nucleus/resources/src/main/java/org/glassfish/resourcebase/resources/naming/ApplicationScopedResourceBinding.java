/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.resourcebase.resources.naming;

import org.glassfish.api.naming.JNDIBinding;
import org.glassfish.api.naming.SimpleJndiName;

import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_APP;

/**
 * Resource binding for application scoped resource
 *
 * @author Jagadish Ramu
 */
public class ApplicationScopedResourceBinding implements JNDIBinding {

    private final SimpleJndiName name;
    private final Object value;

    public ApplicationScopedResourceBinding(SimpleJndiName name, Object value) {
        if (name.isJavaApp()) {
            this.name = name;
        } else {
            this.name = new SimpleJndiName(JNDI_CTX_JAVA_APP + name);
        }
        this.value = value;
    }


    @Override
    public SimpleJndiName getName() {
        return name;
    }


    @Override
    public Object getValue() {
        return value;
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + "[name=" + name + ", value=" + value + "]";
    }
}
