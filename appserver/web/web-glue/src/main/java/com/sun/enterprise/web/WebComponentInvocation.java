/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.web;

import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;

import java.lang.reflect.Method;

import org.glassfish.api.invocation.ComponentInvocation;

public class WebComponentInvocation extends ComponentInvocation {


    /**
     * Used by container within JAXRPC handler processing code.
     */
    private Object webServiceTie;
    private Method webServiceMethod;

    public WebComponentInvocation(WebModule wm) {
        this(wm, null);
    }

    public WebComponentInvocation(WebModule wm, Object instance) {
        setComponentInvocationType(
                ComponentInvocation.ComponentInvocationType.SERVLET_INVOCATION);
        componentId = wm.getComponentId();
        jndiEnvironment = wm.getWebBundleDescriptor();
        container = wm;
        this.instance = instance;
        setResourceTableKey(_getResourceTableKey());

        moduleName = wm.getModuleName();
        appName = wm.getWebBundleDescriptor().getApplication().getAppName();
    }

    public WebComponentInvocation(WebModule wm, Object instance, String instanceName) {
      this(wm, instance);
      setInstanceName(instanceName);
    }

    private Object _getResourceTableKey() {
        Object resourceTableKey = null;
        if (instance instanceof Servlet || instance instanceof Filter) {
            // Servlet or Filter
            resourceTableKey = new PairKey(instance, Thread.currentThread());
        } else {
            resourceTableKey = instance;
        }

        return resourceTableKey;
    }

    public void setWebServiceTie(Object tie) {
        webServiceTie = tie;
    }

    public Object getWebServiceTie() {
        return webServiceTie;
    }

    public void setWebServiceMethod(Method method) {
        webServiceMethod = method;
    }

    public Method getWebServiceMethod() {
        return webServiceMethod;
    }

    private static class PairKey {
        private Object instance = null;
        private Thread thread = null;
        int hCode = 0;

        private PairKey(Object inst, Thread thr) {
            instance = inst;
            thread = thr;
            if (inst != null) {
                hCode = 7 * inst.hashCode();
            }
            if (thr != null) {
                hCode += thr.hashCode();
            }
        }

        @Override
        public int hashCode() {
            return hCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            boolean eq = false;
            if (obj != null && obj instanceof PairKey) {
                PairKey p = (PairKey)obj;
                if (instance != null) {
                    eq = (instance.equals(p.instance));
                } else {
                    eq = (p.instance == null);
                }

                if (eq) {
                    if (thread != null) {
                        eq = (thread.equals(p.thread));
                    } else {
                        eq = (p.thread == null);
                    }
                }
            }
            return eq;
        }
    }
}
