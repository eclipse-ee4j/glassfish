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

package com.sun.enterprise.deployment.annotation.handlers;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.MethodPermission;
import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.security.common.Role;
import org.jvnet.hk2.annotations.Service;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import java.lang.annotation.Annotation;

/**
 * This handler is responsible for handling the
 * jakarta.annotation.security.RolesAllowed.
 *
 * @author Shing Wai Chan
 */
@Service
@AnnotationHandlerFor(RolesAllowed.class)
public class RolesAllowedHandler extends AbstractAuthAnnotationHandler {

    public RolesAllowedHandler() {
    }

    /**
     * Add roles and permissions to given method in EjbDescriptor.
     * @param annotation
     * @param ejbDesc
     * @param md
     */
    @Override
    protected void processEjbMethodSecurity(Annotation authAnnotation,
            MethodDescriptor md, EjbDescriptor ejbDesc) {

        RolesAllowed rolesAllowedAn = (RolesAllowed)authAnnotation;
        for (String roleName : rolesAllowedAn.value()) {
            Role role = new Role(roleName);
            // add role if not exists
            ejbDesc.getEjbBundleDescriptor().addRole(role);
            ejbDesc.addPermissionedMethod(new MethodPermission(role), md);
        }
    }

    /**
     * @return an array of annotation types this annotation handler would
     * require to be processed (if present) before it processes it's own
     * annotation type.
     */
    @Override
    public Class<? extends Annotation>[] getTypeDependencies() {
        return getEjbAnnotationTypes();
    }

    @Override
    protected Class<? extends Annotation>[] relatedAnnotationTypes() {
        return new Class[] { DenyAll.class, PermitAll.class };
    }

}
