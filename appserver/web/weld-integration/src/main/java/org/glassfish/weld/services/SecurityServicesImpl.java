/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld.services;

import com.sun.enterprise.security.SecurityContext;

import java.security.Principal;

import org.jboss.weld.security.spi.SecurityServices;

public class SecurityServicesImpl implements SecurityServices {

    @Override
    public Principal getPrincipal() {
        return SecurityContext.getCurrent().getCallerPrincipal();
    }

    @Override
    public void cleanup() {
    }

    @Override
    public org.jboss.weld.security.spi.SecurityContext getSecurityContext() {
        return new SecurityContextImpl();
    }

    static class SecurityContextImpl implements org.jboss.weld.security.spi.SecurityContext {

        private final SecurityContext myContext;
        private SecurityContext oldContext;

        private SecurityContextImpl() {
            this.myContext = SecurityContext.getCurrent();
        }

        @Override
        public void associate() {
            if (oldContext != null) {
                throw new IllegalStateException("Security context is already associated");
            }
            oldContext = SecurityContext.getCurrent();
            SecurityContext.setCurrent(myContext);
        }

        @Override
        public void dissociate() {
            SecurityContext.setCurrent(oldContext);
            oldContext = null;
        }

        @Override
        public void close() {

        }
    }
}
