/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.faces.mirror;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * Represents a component class or a non-component base class declared in the
 * current compilation unit. This class offers several different "views" of its
 * properties, which can be useful during source code generation. In addition to
 * providing a map of all declared properties, it provides a map which contains
 * all inherited properties.
 */
public final class DeclaredInterfaceInfo extends DeclaredTypeInfo {

    /**
     * Create a new instance.
     * @param env annotation processing environment
     * @param decl type declaration representing the interface
     */
    DeclaredInterfaceInfo(final ProcessingEnvironment env,
            final TypeElement decl) {

        super(env, decl);
    }
}
