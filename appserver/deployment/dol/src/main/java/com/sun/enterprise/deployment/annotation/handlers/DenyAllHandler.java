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

package com.sun.enterprise.deployment.annotation.handlers;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.MethodPermission;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import java.lang.annotation.Annotation;

import org.glassfish.apf.AnnotationHandlerFor;
import org.jvnet.hk2.annotations.Service;

/**
 * This handler is responsible for handling the jakarta.annotation.security.DenyAll.
 *
 * @author Shing Wai Chan
 */
@Service
@AnnotationHandlerFor(DenyAll.class)
public class DenyAllHandler extends AbstractAuthAnnotationHandler {


    @Override
    protected void processEjbMethodSecurity(Annotation authAnnotation, MethodDescriptor md, EjbDescriptor ejbDesc) {
        ejbDesc.addPermissionedMethod(MethodPermission.getDenyAllMethodPermission(), md);
    }


    @Override
    public Class<? extends Annotation>[] getTypeDependencies() {
        return getEjbAnnotationTypes();
    }


    @Override
    protected Class<? extends Annotation>[] relatedAnnotationTypes() {
        return new Class[] {PermitAll.class, RolesAllowed.class};
    }
}
