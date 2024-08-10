/*
 * Copyright (c) 2021 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.core.kernel.test.example;

import java.net.URI;
import java.security.Permission;
import java.util.Collections;
import java.util.List;

import javax.security.auth.Subject;

import org.glassfish.security.services.api.authorization.AuthorizationService;
import org.glassfish.security.services.api.authorization.AzAction;
import org.glassfish.security.services.api.authorization.AzAttributeResolver;
import org.glassfish.security.services.api.authorization.AzResource;
import org.glassfish.security.services.api.authorization.AzResult;
import org.glassfish.security.services.api.authorization.AzResult.Decision;
import org.glassfish.security.services.api.authorization.AzResult.Status;
import org.glassfish.security.services.api.authorization.AzSubject;
import org.glassfish.security.services.config.SecurityConfiguration;
import org.glassfish.security.services.impl.authorization.AuthorizationServiceImpl;
import org.glassfish.security.services.impl.authorization.AzActionImpl;
import org.glassfish.security.services.impl.authorization.AzObligationsImpl;
import org.glassfish.security.services.impl.authorization.AzResourceImpl;
import org.glassfish.security.services.impl.authorization.AzResultImpl;
import org.glassfish.security.services.impl.authorization.AzSubjectImpl;
import org.jvnet.hk2.annotations.Service;


/**
 * Alternative implementation to {@link AuthorizationServiceImpl} which depends on secure
 * environment - JUnit tests don't fulfill this requirement.
 *
 * @author David Matejcek
 */
@Service
public class AuthorizationServiceMock implements AuthorizationService {

    @Override
    public void initialize(SecurityConfiguration securityServiceConfiguration) {
    }


    @Override
    public boolean isPermissionGranted(Subject subject, Permission permission) {
        return true;
    }


    @Override
    public boolean isAuthorized(Subject subject, URI resource) {
        return true;
    }


    @Override
    public boolean isAuthorized(Subject subject, URI resource, String action) {
        return true;
    }


    @Override
    public AzResult getAuthorizationDecision(AzSubject subject, AzResource resource, AzAction action) {
        return new AzResultImpl(Decision.PERMIT, Status.OK, new AzObligationsImpl());
    }


    @Override
    public AzSubject makeAzSubject(Subject subject) {
        return new AzSubjectImpl(subject);
    }


    @Override
    public AzResource makeAzResource(URI resource) {
        return new AzResourceImpl(resource);
    }


    @Override
    public AzAction makeAzAction(String action) {
        return new AzActionImpl(action);
    }


    @Override
    public PolicyDeploymentContext findOrCreateDeploymentContext(String appContext) {
        return null;
    }


    @Override
    public boolean appendAttributeResolver(AzAttributeResolver resolver) {
        return false;
    }


    @Override
    public void setAttributeResolvers(List<AzAttributeResolver> resolverList) {
    }


    @Override
    public List<AzAttributeResolver> getAttributeResolvers() {
        return Collections.emptyList();
    }


    @Override
    public boolean removeAllAttributeResolvers() {
        return false;
    }

}
