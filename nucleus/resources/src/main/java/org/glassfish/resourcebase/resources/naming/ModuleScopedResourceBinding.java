/*
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


/**
 * resource binding for module scoped resources
 * @author Jagadish Ramu
 *
 */
public class ModuleScopedResourceBinding implements JNDIBinding {

    private String name;
    private Object value;
    public ModuleScopedResourceBinding(String name, Object value){
        if(!(name.contains(ResourceNamingService.JAVA_MODULE_SCOPE_PREFIX)
                /*|| name.contains(ResourceNamingService.JAVA_GLOBAL_SCOPE_PREFIX)*/)){
            name = ResourceNamingService.JAVA_MODULE_SCOPE_PREFIX + name;
        }
        this.name = name;
        this.value = value;
    }
    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}
