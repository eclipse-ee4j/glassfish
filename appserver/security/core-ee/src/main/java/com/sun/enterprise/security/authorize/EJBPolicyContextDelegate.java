/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.authorize;

import java.lang.reflect.Method;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.ejb.api.EJBInvocation;
import org.jvnet.hk2.annotations.Service;

/**
 * This class is primarily a delegate for PolicyContextHandler related queries But also handles Authorization of
 * WebServiceInvocations
 *
 * @author Kumar
 */
@Service(name = "EJB")
public class EJBPolicyContextDelegate implements PolicyContextDelegate {

    @Override
    public Object getEnterpriseBean(ComponentInvocation invocation) {
        if (invocation instanceof EJBInvocation) {
            return ((EJBInvocation) invocation).getJaccEjb();
        }
        return null;
    }

    @Override
    public Object getEJbArguments(ComponentInvocation invocation) {
        if (invocation instanceof EJBInvocation) {
            EJBInvocation ejbInvocation = (EJBInvocation) invocation;
            if (ejbInvocation.isAWebService()) {
                return null;
            }
            return ejbInvocation.getMethodParams() != null ? ejbInvocation.getMethodParams() : new Object[0];
        }

        return null;
    }

    @Override
    public Object getSOAPMessage(ComponentInvocation invocation) {
        if (invocation instanceof EJBInvocation) {
            EJBInvocation ejbInvocation = (EJBInvocation) invocation;
            if (ejbInvocation.isAWebService()) {
                // TODO:V3 does this violate JACC spec?, we may have to convert to SOAPMessage on demand
                // return eInv.getSOAPMessage();
                return ejbInvocation.getMessage();
            }
        }
        return null;
    }

    @Override
    public void setSOAPMessage(Object message, ComponentInvocation invocation) {
        if (invocation instanceof EJBInvocation) {
            EJBInvocation ejbInvocation = (EJBInvocation) invocation;
            if (ejbInvocation.isAWebService()) {
                ejbInvocation.setMessage(message);
            }
        }
    }

    @Override
    public boolean authorize(ComponentInvocation invocation, Method method) throws Exception {
        if (invocation instanceof EJBInvocation) {
            return ((EJBInvocation) invocation).authorizeWebService(method);

        }

        return true;
    }

}
