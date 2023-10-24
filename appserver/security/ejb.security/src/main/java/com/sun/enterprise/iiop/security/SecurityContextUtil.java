/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.iiop.security;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;
import com.sun.enterprise.common.iiop.security.SecurityContext;
import com.sun.enterprise.security.CORBAObjectPermission;
import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.net.Socket;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Policy;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.glassfish.enterprise.iiop.api.GlassFishORBHelper;
import org.glassfish.enterprise.iiop.api.ProtocolManager;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;

/**
 * This class provides has the helper methods to deal with the SecurityContext.
 * This represents the SecurityServiceImpl of V2
 *
 * @author Nithya Subramanian
 */

@Service
@Singleton
public class SecurityContextUtil implements PostConstruct {

    private static final Logger LOG = LogDomains.getLogger(SecurityContextUtil.class, LogDomains.SECURITY_LOGGER, false);

    public static final int STATUS_PASSED = 0;
    public static final int STATUS_FAILED = 1;
    public static final int STATUS_RETRY = 2;


    private static final String IS_A = "_is_a";
    private Policy policy;

    @Inject
    private GlassFishORBHelper orbHelper;

    @Inject
    private SecurityMechanismSelector sms;

    public SecurityContextUtil() {

    }

    @Override
    public void postConstruct() {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                policy = Policy.getPolicy();
                return null;
            }
        });
    }

    /**
     * This is called by the CSIv2 interceptor on the client before sending the IIOP message.
     *
     * @param effective_target the effective_target field of the PortableInterceptor ClientRequestInfo object.
     * @return a SecurityContext which is marshalled into the IIOP msg by the CSIv2 interceptor.
     * @throws InvalidMechanismException
     * @throws InvalidIdentityTokenException
     */
    public SecurityContext getSecurityContext(org.omg.CORBA.Object effective_target)
        throws InvalidMechanismException, InvalidIdentityTokenException {
        assert (orbHelper != null);
        IOR ior = ((com.sun.corba.ee.spi.orb.ORB) orbHelper.getORB()).getIOR(effective_target, false);
        if (StubAdapter.isStub(effective_target)) {
            if (StubAdapter.isLocal(effective_target)) {
                // XXX: Workaround for non-null connection object ri for local invocation.
                ConnectionExecutionContext.setClientThreadID(Thread.currentThread().getId());
                return null;
            }
        }

        try {
            return sms.selectSecurityContext(ior);
        } catch (InvalidMechanismException | InvalidIdentityTokenException e) {
            throw e;
        } catch (SecurityMechanismException e) {
            throw new RuntimeException("Could not select a security context.", e);
        }
    }


    /**
     * This is called by the CSIv2 interceptor on the client after a reply is received.
     *
     * @param reply_status the reply status from the call. The reply status field could indicate an
     *            an authentication retry. The following is the mapping of PI status
     *            to the reply_status field
     *            PortableInterceptor::SUCCESSFUL -> STATUS_PASSED
     *            PortableInterceptor::SYSTEM_EXCEPTION -> STATUS_FAILED
     *            PortableInterceptor::USER_EXCEPTION -> STATUS_PASSED
     *            PortableInterceptor::LOCATION_FORWARD -> STATUS_RETRY
     *            PortableInterceptor::TRANSPORT_RETRY -> STATUS_RETRY
     * @param effective_target the effective_target field of the PI ClientRequestInfo object.
     */
    public static void receivedReply(int reply_status, org.omg.CORBA.Object effective_target) {
        if (reply_status == STATUS_FAILED) {
            LOG.log(Level.FINE, "Failed status");
            // what kind of exception should we throw?
            throw new RuntimeException("Target did not accept security context");
        } else if (reply_status == STATUS_RETRY) {
            LOG.log(Level.FINE, "Retry status");
        } else {
            LOG.log(Level.FINE, "Passed status");
        }
    }

    /**
     * This is called by the CSIv2 interceptor on the server after receiving the IIOP message. If authentication fails a
     * FAILED status is returned. If a FAILED status is returned the CSIV2 interceptor will marshall the MessageError
     * service context and throw the NO_PERMISSION exception.
     *
     * @param context the SecurityContext which arrived in the IIOP message.
     * @return the status
     */
    public int setSecurityContext(SecurityContext context, byte[] object_id, String method, Socket socket) {
        LOG.log(Level.FINE, "ABOUT TO EVALUATE TRUST");

        try {
            // First check if the client sent the credentials
            // as required by the object's CSIv2 policy.
            // evaluateTrust will throw an exception if client did not
            // conform to security policy.
            SecurityContext ssc = sms.evaluateTrust(context, object_id, socket);

            Class cls = null;
            Subject s = null;
            if (ssc == null) {
                return STATUS_PASSED;
            }
            if (ssc.authcls == null) {
                cls = ssc.identcls;
            } else {
                cls = ssc.authcls;
            }
            s = ssc.subject;

            // Authenticate the client. An Exception is thrown if login fails.
            // SecurityContext is set on current thread if login succeeds.
            authenticate(s, cls);

            // Authorize the client for non-EJB objects.
            // Auth for EJB objects is done in BaseContainer.preInvoke().
            if (authorizeCORBA(object_id, method)) {
                return STATUS_PASSED;
            }
            return STATUS_FAILED;
        } catch (Exception e) {
            if (!method.equals(IS_A)) {
                LOG.log(Level.FINE, "Authentication Exception", e);
            }
            return STATUS_FAILED;
        }
    }

    // return true if authorization succeeds, false otherwise.
    private boolean authorizeCORBA(byte[] object_id, String method) throws Exception {

        // Check if target is an EJB
        ProtocolManager protocolMgr = orbHelper.getProtocolManager();
        // Check to make sure protocolMgr is not null.
        // This could happen during server initialization or if this call
        // is on a callback object in the client VM.
        if (protocolMgr == null) {
            return true;
        }
        if (protocolMgr.getEjbDescriptor(object_id) != null) {
            return true; // an EJB object

        }
        CORBAObjectPermission perm = new CORBAObjectPermission("*", method);

        // Create a ProtectionDomain for principal on current thread.
        com.sun.enterprise.security.SecurityContext sc = com.sun.enterprise.security.SecurityContext.getCurrent();
        Set principalSet = sc.getPrincipalSet();
        Principal[] principals = (principalSet == null ? null : (Principal[]) principalSet.toArray(new Principal[principalSet.size()]));
        CodeSource cs = new CodeSource(new java.net.URL("file://"), (java.security.cert.Certificate[]) null);
        ProtectionDomain prdm = new ProtectionDomain(cs, null, null, principals);

        // Check if policy gives principal the permissions
        boolean result = policy.implies(prdm, perm);

        LOG.log(Level.FINE, "CORBA Object permission evaluation result={0} for method={1}",
            new Object[] {result, method});
        return result;
    }

    /**
     * This is called by the CSIv2 interceptor on the server before sending the reply.
     *
     * @param context the SecurityContext which arrived in the IIOP message.
     */
    public void sendingReply(SecurityContext context) {
        // NO OP
    }

    /**
     * This is called on the server to unset the security context this is introduced to prevent the re-use of the thread
     * security context on re-use of the thread.
     */
    public static void unsetSecurityContext(boolean isLocal) {
        // logout method from LoginContext not called
        // as we dont want to unset the appcontainer context
        if (!isLocal) {
            com.sun.enterprise.security.SecurityContext.setCurrent(null);
        }
    }

    /**
     * Authenticate the user with the specified subject and credential class.
     */
    private void authenticate(Subject s, Class cls) throws SecurityMechanismException {
        try {
            PrivilegedAction<Void> action = () -> {
                LoginContextDriver.login(s, cls);
                return null;
            };
            AccessController.doPrivileged(action);
        } catch (Exception e) {
            throw new SecurityMechanismException("Cannot login user: " + e.getMessage(), e);
        }
    }

}
