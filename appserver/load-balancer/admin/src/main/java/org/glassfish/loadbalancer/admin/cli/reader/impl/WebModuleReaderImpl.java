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

package org.glassfish.loadbalancer.admin.cli.reader.impl;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.runtime.web.SunWebApp;

import org.glassfish.loadbalancer.admin.cli.reader.api.IdempotentUrlPatternReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.LbReaderException;
import org.glassfish.loadbalancer.admin.cli.reader.api.WebModuleReader;
import org.glassfish.loadbalancer.admin.cli.transform.Visitor;
import org.glassfish.loadbalancer.admin.cli.transform.WebModuleVisitor;

/**
 * Provides web module information relavant to Load balancer tier.
 *
 * @author Kshitiz Saxena
 */
public class WebModuleReaderImpl implements WebModuleReader {

    public WebModuleReaderImpl(String name, ApplicationRef ref, Application application, WebBundleDescriptor webBundleDescriptor) {
        _applicationRef = ref;
        _application = application;
        _name = name;
        _webBundleDescriptor = webBundleDescriptor;
        _sunWebApp = webBundleDescriptor.getSunDescriptor();
    }

    @Override
    public String getContextRoot() throws LbReaderException {
        if (_application.getContextRoot() != null) {
            return _application.getContextRoot();
        }
        return _webBundleDescriptor.getContextRoot();
    }

    @Override
    public String getErrorUrl() throws LbReaderException {
        return _sunWebApp.getAttributeValue(SunWebApp.ERROR_URL);
    }

    @Override
    public boolean getLbEnabled() throws LbReaderException {
        return Boolean.valueOf(_applicationRef.getLbEnabled()).booleanValue();
    }

    @Override
    public String getDisableTimeoutInMinutes() throws LbReaderException {
        return _applicationRef.getDisableTimeoutInMinutes();
    }

    @Override
    public IdempotentUrlPatternReader[] getIdempotentUrlPattern()
            throws LbReaderException {
        if (_sunWebApp == null) {
            return null;
        } else {
            int len = _sunWebApp.sizeIdempotentUrlPattern();
            if (len == 0) {
                return null;
            }
            IdempotentUrlPatternReader[] iRdrs =
                    new IdempotentUrlPatternReader[len];
            for (int i = 0; i < len; i++) {
                iRdrs[i] = new IdempotentUrlPatternReaderImpl(_sunWebApp.getIdempotentUrlPattern(i));
            }
            return iRdrs;
        }
    }

    @Override
    public void accept(Visitor v) throws Exception {
        if (v instanceof WebModuleVisitor) {
            WebModuleVisitor wv = (WebModuleVisitor) v;
            wv.visit(this);
        }
    }
    // ---- VARIABLE(S) - PRIVATE -----------------------------
    private String _name;
    private ApplicationRef _applicationRef = null;
    private Application _application = null;
    private WebBundleDescriptor _webBundleDescriptor;
    private SunWebApp _sunWebApp;
}
