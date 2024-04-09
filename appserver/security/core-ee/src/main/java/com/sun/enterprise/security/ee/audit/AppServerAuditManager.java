/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.security.ee.audit;

import com.sun.appserv.security.AuditModule;
import com.sun.enterprise.security.BaseAuditModule;
import com.sun.enterprise.security.audit.BaseAuditManager;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.logging.LogDomains;

import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Service;

import static com.sun.logging.LogDomains.SECURITY_LOGGER;

/**
 * An EE-specific implementation of the audit manager.
 * <p>
 * This class delegates the nucleus-based work of handling server start-up and shutdown and user authentication to its
 * superclass, adding only the work specific to EE auditing here.
 *
 * @author Harpreet Singh
 * @author Shing Wai Chan
 * @author tjquinn
 */
@Service
@Singleton
@Rank(20) // so the app server prefers this impl to the non-EE one in nucleus
public final class AppServerAuditManager extends BaseAuditManager<AuditModule> {

    private static final String AUDIT_MGR_WS_INVOCATION_KEY = "auditmgr.webServiceInvocation";
    private static final String AUDIT_MGR_EJB_AS_WS_INVOCATION_KEY = "auditmgr.ejbAsWebServiceInvocation";

    private static final Logger LOG = LogDomains.getLogger(AppServerAuditManager.class, SECURITY_LOGGER, false);

    private static final LocalStringManagerImpl _localStrings = new LocalStringManagerImpl(AppServerAuditManager.class);

    private List<AuditModule> myAuditModules;

    private synchronized List<AuditModule> myAuditModules() {
        if (myAuditModules == null) {
            myAuditModules = instances(AuditModule.class);
        }
        return myAuditModules;
    }

    @Override
    public BaseAuditModule addAuditModule(String name, String classname, Properties props) throws Exception {
        final BaseAuditModule am = super.addAuditModule(name, classname, props);
        if (AuditModule.class.isAssignableFrom(am.getClass())) {
            myAuditModules().add((AuditModule) am);
        }
        return am;
    }

    @Override
    public BaseAuditModule removeAuditModule(String name) {
        final BaseAuditModule am = super.removeAuditModule(name);
        if (AuditModule.class.isAssignableFrom(am.getClass())) {
            myAuditModules().remove(am);
        }
        return am;
    }

    /**
     * logs the web authorization call for all loaded modules
     *
     * @see com.sun.appserv.security.AuditModule.webInvocation
     */
    public void webInvocation(final String user, final HttpServletRequest req, final String type, final boolean success) {
        if (auditOn) {
            for (AuditModule am : myAuditModules()) {
                try {
                    am.webInvocation(user, req, type, success);
                } catch (Exception ex) {
                    final String name = moduleName(am);
                    final String msg = _localStrings.getLocalString("auditmgr.webinvocation",
                            " Audit Module {0} threw the following exception during web invocation :", name);
                    LOG.log(Level.INFO, msg, ex);
                }
            }
        }
    }

    /**
     * logs the ejb authorization call for all ejb modules
     *
     * @see com.sun.appserv.security.AuditModule.ejbInvocation
     */
    public void ejbInvocation(final String user, final String ejb, final String method, final boolean success) {
        if (auditOn) {
            for (AuditModule am : myAuditModules()) {
                try {
                    am.ejbInvocation(user, ejb, method, success);
                } catch (Exception ex) {
                    final String name = moduleName(am);
                    final String msg = _localStrings.getLocalString("auditmgr.ejbinvocation",
                            " Audit Module {0} threw the following exception during ejb invocation :", name);
                    LOG.log(Level.INFO, msg, ex);
                }
            }
        }
    }

    /**
     * This method is called for the web service calls with MLS set and the endpoints deployed as servlets
     *
     * @see com.sun.appserv.security.AuditModule.webServiceInvocation
     */
    public void webServiceInvocation(final String uri, final String endpoint, final boolean validRequest) {
        if (auditOn) {
            for (AuditModule am : myAuditModules()) {
                try {
                    am.webServiceInvocation(uri, endpoint, validRequest);
                } catch (Exception ex) {
                    final String name = moduleName(am);
                    final String msg = _localStrings.getLocalString(AUDIT_MGR_WS_INVOCATION_KEY,
                            " Audit Module {0} threw the following exception during web service invocation :", name);
                    LOG.log(Level.INFO, msg, ex);
                }
            }
        }
    }

    /**
     * This method is called for the web service calls with MLS set and the endpoints deployed as servlets
     *
     * @see com.sun.appserv.security.AuditModule.webServiceInvocation
     */
    public void ejbAsWebServiceInvocation(final String endpoint, final boolean validRequest) {
        if (auditOn) {
            for (AuditModule auditModule : myAuditModules()) {
                try {
                    auditModule.ejbAsWebServiceInvocation(endpoint, validRequest);
                } catch (Exception ex) {
                    final String name = moduleName(auditModule);
                    final String msg = _localStrings.getLocalString(AUDIT_MGR_EJB_AS_WS_INVOCATION_KEY,
                            " Audit Module {0} threw the following exception during ejb as web service invocation :", name);
                    LOG.log(Level.INFO, msg, ex);
                }
            }
        }
    }
}
