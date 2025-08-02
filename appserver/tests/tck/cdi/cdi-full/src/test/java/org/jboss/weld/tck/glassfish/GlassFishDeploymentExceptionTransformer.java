/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.jboss.weld.tck.glassfish;

import jakarta.enterprise.inject.spi.DefinitionException;
import jakarta.enterprise.inject.spi.DeploymentException;

import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.arquillian.container.spi.client.container.DeploymentExceptionTransformer;

/**
 *
 * See AS7-1197 for more details.
 *
 * @see org.jboss.weld.tck.glassfish.GlassFishExtension
 * @author J J Snyder (j.j.snyder@oracle.com)
 */
public class GlassFishDeploymentExceptionTransformer implements DeploymentExceptionTransformer {

    private static final String[] DEPLOYMENT_EXCEPTION_FRAGMENTS = new String[] {
        "Only normal scopes can be passivating",
        "org.jboss.weld.exceptions.DeploymentException",
        "org.jboss.weld.exceptions.UnserializableDependencyException",
        "org.jboss.weld.exceptions.InconsistentSpecializationException",
        "CDI deployment failure:",
        "org.jboss.weld.exceptions.NullableDependencyException"
    };

    private static final String[] DEFINITION_EXCEPTION_FRAGMENTS = new String[] {
        "CDI definition failure:",
        "org.jboss.weld.exceptions.DefinitionException"
    };

    @Override
    public Throwable transform(Throwable throwable) {

        // Arquillian sometimes returns InvocationException with nested AS7
        // exception and sometimes AS7 exception itself
        @SuppressWarnings("unchecked")
        List<Throwable> throwableList = ExceptionUtils.getThrowableList(throwable);
        if (throwableList.isEmpty()) {
            return throwable;
        }

        final Throwable root;
        if (throwableList.size() == 1) {
            root = throwable;
        } else {
            root = ExceptionUtils.getRootCause(throwable);
        }

        if (root instanceof DeploymentException || root instanceof DefinitionException) {
            return root;
        }
        if (isFragmentFound(DEPLOYMENT_EXCEPTION_FRAGMENTS, root)) {
            return new DeploymentException(root.getMessage());
        }
        if (isFragmentFound(DEFINITION_EXCEPTION_FRAGMENTS, root)) {
            return new DefinitionException(root.getMessage());
        }
        return throwable;
    }

    private boolean isFragmentFound(String[] fragments, Throwable rootException) {
        for (String fragment : fragments) {
            if (rootException.getMessage().contains(fragment)) {
                return true;
            }
        }
        return false;
    }
}
