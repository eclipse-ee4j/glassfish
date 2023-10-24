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

package com.sun.enterprise.security.ee.acl;

import java.lang.reflect.Method;

/**
 * An EJB resource.
 *
 * @author Harish Prabandham
 */
public class EJBResource extends Resource {

    public EJBResource(String app, Method method) {
        super(app, method.getDeclaringClass().getName(), method.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }

        Resource r = (Resource) obj;

        return getApplication().equals(r.getApplication()) && getMethod().equals(r.getMethod()) && getName().equals(r.getName());
    }

    @Override
    public boolean implies(Resource resource) {
        return equals(resource);
    }
}
