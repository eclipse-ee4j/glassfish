/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.internal.api.Globals;

import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.security.authorize.cache.PermissionCacheFactory;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This class implements a thread scoped data used for PolicyContext.
 *
 * @author Harry Singh
 * @author Jyri Virkki
 * @author Shing Wai Chan
 *
 */
public class HandlerData {

    private HttpServletRequest httpReq = null;
    private ComponentInvocation inv = null;
    private PolicyContextDelegate ejbDelegate = null;

    private HandlerData() {
        ejbDelegate = Globals.getDefaultHabitat().getService(PolicyContextDelegate.class, "EJB");
    }

    public static HandlerData getInstance() {
        return new HandlerData();
    }

    public void setHttpServletRequest(HttpServletRequest httpReq) {
        this.httpReq = httpReq;
    }

    public void setInvocation(ComponentInvocation inv) {
        this.inv = inv;
    }

    public Object get(String key) {
        if (PolicyContextHandlerImpl.HTTP_SERVLET_REQUEST.equalsIgnoreCase(key)) {
            return httpReq;
        }
        if (PolicyContextHandlerImpl.SUBJECT.equalsIgnoreCase(key)) {
            return SecurityContext.getCurrent().getSubject();
        }
        if (PolicyContextHandlerImpl.REUSE.equalsIgnoreCase(key)) {
            PermissionCacheFactory.resetCaches();
            return Integer.valueOf(0);
        }

        if (inv == null) {
            return null;
        }

        if (PolicyContextHandlerImpl.SOAP_MESSAGE.equalsIgnoreCase(key)) {
            return ejbDelegate != null ? ejbDelegate.getSOAPMessage(inv) : null;
        }
        if (PolicyContextHandlerImpl.ENTERPRISE_BEAN.equalsIgnoreCase(key)) {
            return ejbDelegate != null ? ejbDelegate.getEnterpriseBean(inv) : null;
        }
        if (PolicyContextHandlerImpl.EJB_ARGUMENTS.equalsIgnoreCase(key)) {
            return ejbDelegate != null ? ejbDelegate.getEJbArguments(inv) : null;
        }
        return null;
    }

    void reset() {
        httpReq = null;
        inv = null;
        ejbDelegate = null;
    }
}
