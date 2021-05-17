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

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.annotation.context.EjbContext;
import com.sun.enterprise.deployment.annotation.context.WebBundleContext;
import com.sun.enterprise.deployment.annotation.context.WebComponentContext;
import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.security.common.Role;
import org.jvnet.hk2.annotations.Service;

import jakarta.annotation.security.DeclareRoles;
import java.lang.annotation.Annotation;

/**
 * This handler is responsible for handling the
 * jakarta.annotation.security.DeclareRoles.
 *
 * @author Shing Wai Chan
 */
@Service
@AnnotationHandlerFor(DeclareRoles.class)
public class DeclareRolesHandler extends AbstractCommonAttributeHandler {

    public DeclareRolesHandler() {
    }

    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            EjbContext[] ejbContexts) throws AnnotationProcessorException {

        DeclareRoles rolesRefAn = (DeclareRoles)ainfo.getAnnotation();

        for (EjbContext ejbContext : ejbContexts) {
            EjbDescriptor ejbDescriptor = ejbContext.getDescriptor();
            for (String roleName : rolesRefAn.value()) {
                if (ejbDescriptor.getRoleReferenceByName(roleName) == null) {
                    RoleReference roleRef = new RoleReference(roleName, "");
                    roleRef.setRoleName(roleName);
                    roleRef.setSecurityRoleLink(
                           new SecurityRoleDescriptor(roleName, ""));
                    ejbDescriptor.addRoleReference(roleRef);
                }

                Role role = new Role(roleName);
                ejbDescriptor.getEjbBundleDescriptor().addRole(role);
            }
        }
        return getDefaultProcessedResult();
    }

    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            WebComponentContext[] webCompContexts)
            throws AnnotationProcessorException {
        WebBundleDescriptor webBundleDesc =
            webCompContexts[0].getDescriptor().getWebBundleDescriptor();
        return processAnnotation(ainfo, webBundleDesc);
    }

    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
             WebBundleContext webBundleContext)
             throws AnnotationProcessorException {
        WebBundleDescriptor webBundleDesc = webBundleContext.getDescriptor();
        return processAnnotation(ainfo, webBundleDesc);
    }

    private HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
             WebBundleDescriptor webBundleDesc) {
        DeclareRoles rolesRefAn = (DeclareRoles)ainfo.getAnnotation();
        for (String roleName : rolesRefAn.value()) {
            Role role = new Role(roleName);
            webBundleDesc.addRole(role);
        }
        return getDefaultProcessedResult();
    }

    /**
     * @return an array of annotation types this annotation handler would
     * require to be processed (if present) before it processes it's own
     * annotation type.
     */
    public Class<? extends Annotation>[] getTypeDependencies() {
        return getEjbAnnotationTypes();
    }

    protected boolean supportTypeInheritance() {
        return true;
    }
}
