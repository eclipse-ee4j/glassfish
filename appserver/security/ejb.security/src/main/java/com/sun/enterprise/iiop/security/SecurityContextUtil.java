/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation.
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
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;
import com.sun.enterprise.common.iiop.security.SecurityContext;
import com.sun.enterprise.security.CORBAObjectPermission;
import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.security.jacc.PolicyFactory;

import java.net.Socket;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.glassfish.enterprise.iiop.api.GlassFishORBHelper;
import org.glassfish.enterprise.iiop.api.ProtocolManager;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.FINE;

/**
 * This class provides has the helper methods to deal with the SecurityContext. This represents the SecurityServiceImpl
 * of V2
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

    @Inject
    private GlassFishORBHelper orbHelper;

    @Inject
    private SecurityMechanismSelector securityMechanismSelector;

    @Override
    public void postConstruct() {

    }

    /**
     * This is called by the CSIv2 interceptor on the client before sending the IIOP message.
     *
     * @param effective_target the effective_target field of the PortableInterceptor ClientRequestInfo object.
     * @return a SecurityContext which is marshalled into the IIOP msg by the CSIv2 interceptor.
     * @throws InvalidMechanismException
     * @throws InvalidIdentityTokenException
     */
    public SecurityContext getSecurityContext(org.omg.CORBA.Object effective_target) throws InvalidMechanismException, InvalidIdentityTokenException {
        IOR ior = ((ORB) orbHelper.getORB()).getIOR(effective_target, false);

        if (StubAdapter.isStub(effective_target)) {
            if (StubAdapter.isLocal(effective_target)) {
                // XXX: Workaround for non-null connection object ri for local invocation.
                ConnectionExecutionContext.setClientThreadID(Thread.currentThread().getId());
                return null;
            }
        }

        try {
            return securityMechanismSelector.selectSecurityContext(ior);
        } catch (InvalidMechanismException | InvalidIdentityTokenException e) {
            throw e;
        } catch (SecurityMechanismException e) {
            throw new RuntimeException("Could not select a security context.", e);
        }
    }

    /**
     * This is called by the CSIv2 interceptor on the client after a reply is received.
     *
     * @param reply_status the reply status from the call. The reply status field could indicate an an authentication retry.
     * The following is the mapping of PI status to the reply_status field PortableInterceptor::SUCCESSFUL -> STATUS_PASSED
     * PortableInterceptor::SYSTEM_EXCEPTION -> STATUS_FAILED PortableInterceptor::USER_EXCEPTION -> STATUS_PASSED
     * PortableInterceptor::LOCATION_FORWARD -> STATUS_RETRY PortableInterceptor::TRANSPORT_RETRY -> STATUS_RETRY
     * @param effective_target the effective_target field of the PI ClientRequestInfo object.
     */
    public static void receivedReply(int reply_status, org.omg.CORBA.Object effective_target) {
        if (reply_status == STATUS_FAILED) {
            LOG.log(FINE, "Failed status");
            // what kind of exception should we throw?
            throw new RuntimeException("Target did not accept security context");
        }

        if (reply_status == STATUS_RETRY) {
            LOG.log(FINE, "Retry status");
        } else {
            LOG.log(FINE, "Passed status");
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
        LOG.log(FINE, "ABOUT TO EVALUATE TRUST");

        try {
            // First check if the client sent the credentials
            // as required by the object's CSIv2 policy.
            // evaluateTrust will throw an exception if client did not
            // conform to security policy.
            SecurityContext securityContext = securityMechanismSelector.evaluateTrust(context, object_id, socket);
            if (securityContext == null) {
                return STATUS_PASSED;
            }

            Class<?> credentialClass = null;
            if (securityContext.authcls == null) {
                credentialClass = securityContext.identcls;
            } else {
                credentialClass = securityContext.authcls;
            }

            // Authenticate the client. An Exception is thrown if login fails.
            // SecurityContext is set on current thread if login succeeds.
            authenticate(securityContext.subject, credentialClass);

            // Authorize the client for non-EJB objects.
            // Auth for EJB objects is done in BaseContainer.preInvoke().
            if (authorizeCORBA(object_id, method)) {
                return STATUS_PASSED;
            }

            return STATUS_FAILED;
        } catch (Exception e) {
            if (!method.equals(IS_A)) {
                LOG.log(FINE, "Authentication Exception", e);
            }

            return STATUS_FAILED;
        }
    }

    // Return true if authorization succeeds, false otherwise.
    private boolean authorizeCORBA(byte[] object_id, String method) throws Exception {
        // Check if target is an EJB
        ProtocolManager protocolManager = orbHelper.getProtocolManager();

        // Check to make sure protocolMgr is not null.
        // This could happen during server initialization or if this call
        // is on a callback object in the client VM.
        if (protocolManager == null) {
            return true;
        }

        if (protocolManager.getEjbDescriptor(object_id) != null) {
            return true; // an EJB object
        }

        com.sun.enterprise.security.SecurityContext securityContext = com.sun.enterprise.security.SecurityContext.getCurrent();

        // Check if policy gives principal the permissions
        boolean result = PolicyFactory.getPolicyFactory()
                                      .getPolicy()
                                      .implies(
                                          new CORBAObjectPermission("*", method),
                                          securityContext.getPrincipalSet());

        LOG.log(FINE, "CORBA Object permission evaluation result={0} for method={1}", new Object[] { result, method });

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
    private void authenticate(Subject subject, Class<?> credentialClass) throws SecurityMechanismException {
        try {
            LoginContextDriver.login(subject, credentialClass);
        } catch (Exception e) {
            throw new SecurityMechanismException("Cannot login user: " + e.getMessage(), e);
        }
    }

}
