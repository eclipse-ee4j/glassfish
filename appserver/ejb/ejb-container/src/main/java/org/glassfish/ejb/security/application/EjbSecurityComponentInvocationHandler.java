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

package org.glassfish.ejb.security.application;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.ComponentInvocation.ComponentInvocationType;
import org.glassfish.api.invocation.ComponentInvocationHandler;
import org.glassfish.api.invocation.InvocationException;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.invocation.RegisteredComponentInvocationHandler;
import org.jvnet.hk2.annotations.Service;

import com.sun.ejb.EjbInvocation;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Service(name = "ejbSecurityCIH")
@Singleton
public class EjbSecurityComponentInvocationHandler implements RegisteredComponentInvocationHandler {

    private static final Logger _logger = LogDomains.getLogger(EjbSecurityComponentInvocationHandler.class, LogDomains.EJB_LOGGER);

    @Inject
    private InvocationManager invManager;

    private ComponentInvocationHandler ejbSecurityCompInvHandler = new ComponentInvocationHandler() {

        @Override
        public void beforePreInvoke(ComponentInvocationType invType, ComponentInvocation prevInv, ComponentInvocation newInv)
            throws InvocationException {
            if (invType == ComponentInvocationType.EJB_INVOCATION) {
                assert (newInv instanceof EjbInvocation);
                try {
                    if (!newInv.isPreInvokeDone()) {
                        ((EjbInvocation) newInv).getEjbSecurityManager().preInvoke(newInv);
                    }
                } catch (Exception ex) {
                    _logger.log(Level.SEVERE, "ejb.security_preinvoke_exception", ex);
                    throw new InvocationException(ex);
                }
            }
        }

        @Override
        public void afterPreInvoke(ComponentInvocationType invType, ComponentInvocation prevInv, ComponentInvocation curInv)
            throws InvocationException {
        }

        @Override
        public void beforePostInvoke(ComponentInvocationType invType, ComponentInvocation prevInv, ComponentInvocation curInv)
            throws InvocationException {
        }

        @Override
        public void afterPostInvoke(ComponentInvocationType invType, ComponentInvocation prevInv, ComponentInvocation curInv)
            throws InvocationException {
            if (invType == ComponentInvocationType.EJB_INVOCATION) {
                assert (curInv instanceof EjbInvocation);
                try {
                    ((EjbInvocation) curInv).getEjbSecurityManager().postInvoke(curInv);
                } catch (Exception ex) {
                    _logger.log(Level.SEVERE, "ejb.security_postinvoke_exception", ex);
                    ((EjbInvocation) curInv).exception = ex;
                }
            }
        }
    };

    @Override
    public ComponentInvocationHandler getComponentInvocationHandler() {
        return ejbSecurityCompInvHandler;
    }

    @Override
    public void register() {
        invManager.registerComponentInvocationHandler(ComponentInvocationType.EJB_INVOCATION, this);
    }

}
