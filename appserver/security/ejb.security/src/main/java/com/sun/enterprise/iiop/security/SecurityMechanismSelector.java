/*
 * Copyright (c) 2023, 2026 Contributors to the Eclipse Foundation
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

import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.encoding.EncapsInputStream;
import com.sun.corba.ee.org.omg.CSI.ITTAnonymous;
import com.sun.corba.ee.org.omg.CSI.ITTDistinguishedName;
import com.sun.corba.ee.org.omg.CSI.ITTPrincipalName;
import com.sun.corba.ee.org.omg.CSI.ITTX509CertChain;
import com.sun.corba.ee.org.omg.CSIIOP.AS_ContextSec;
import com.sun.corba.ee.org.omg.CSIIOP.CompoundSecMech;
import com.sun.corba.ee.org.omg.CSIIOP.Confidentiality;
import com.sun.corba.ee.org.omg.CSIIOP.DelegationByClient;
import com.sun.corba.ee.org.omg.CSIIOP.EstablishTrustInClient;
import com.sun.corba.ee.org.omg.CSIIOP.EstablishTrustInTarget;
import com.sun.corba.ee.org.omg.CSIIOP.IdentityAssertion;
import com.sun.corba.ee.org.omg.CSIIOP.Integrity;
import com.sun.corba.ee.org.omg.CSIIOP.SAS_ContextSec;
import com.sun.corba.ee.org.omg.CSIIOP.TAG_NULL_TAG;
import com.sun.corba.ee.org.omg.CSIIOP.TLS_SEC_TRANS;
import com.sun.corba.ee.org.omg.CSIIOP.TLS_SEC_TRANSHelper;
import com.sun.corba.ee.org.omg.CSIIOP.TransportAddress;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.ee.spi.transport.SocketInfo;
import com.sun.enterprise.common.iiop.security.AnonCredential;
import com.sun.enterprise.common.iiop.security.GSSUPName;
import com.sun.enterprise.common.iiop.security.SecurityContext;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbIORConfigurationDescriptor;
import com.sun.enterprise.security.SecurityServicesUtil;
import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.enterprise.security.auth.login.common.LoginException;
import com.sun.enterprise.security.auth.login.common.PasswordCredential;
import com.sun.enterprise.security.auth.login.common.X509CertificateCredential;
import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.security.common.ClientSecurityContext;
import com.sun.enterprise.security.common.SecurityConstants;
import com.sun.enterprise.security.ssl.SSLUtils;
import com.sun.enterprise.util.Utility;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.lang.System.Logger;
import java.net.Socket;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.enterprise.iiop.api.GlassFishORBFactory;
import org.glassfish.enterprise.iiop.api.GlassFishORBLocator;
import org.glassfish.enterprise.iiop.api.ProtocolManager;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.internal.api.ORBLocator;
import org.ietf.jgss.Oid;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.iiop.security.IORToSocketInfoImpl.createSocketInfo;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;

/**
 * This class is responsible for making various decisions for selecting security information to be sent in the IIOP
 * message based on target configuration and client policies. Note: This class can be called concurrently by multiple
 * client threads. However, none of its methods need to be synchronized because the methods either do not modify state
 * or are idempotent.
 *
 * @author Nithya Subramanian
 *
 */
@Service
@Singleton
public final class SecurityMechanismSelector implements PostConstruct {

    private static final Logger LOG = System.getLogger(SecurityMechanismSelector.class.getName());

    public static final String CLIENT_CONNECTION_CONTEXT = "ClientConnContext";
    // public static final String SERVER_CONNECTION_CONTEXT = "ServerConnContext";

    @Inject
    private SSLUtils sslUtils;
    @Inject
    private GlassFishORBLocator orbLocator;
    @Inject
    private GlassFishORBFactory orbFactory;
    @Inject
    private ProcessEnvironment processEnv;

    /**
     * A reference to POAProtocolMgr will be obtained dynamically
     * and set if not null. So set it to null here.
     */
    private ProtocolManager protocolMgr;
    private CSIV2TaggedComponentInfo ctc;
    private Set<EjbIORConfigurationDescriptor> corbaIORDescSet;
    private boolean sslRequired;

    @Override
    public void postConstruct() {
        try {
            // Initialize client security config
            if ("true".equals(orbFactory.getCSIv2Props().getProperty(ORBLocator.ORB_SSL_CLIENT_REQUIRED))) {
                sslRequired = true;
            }

            // initialize corbaIORDescSet with security config for CORBA objects
            corbaIORDescSet = new HashSet<>();
            EjbIORConfigurationDescriptor iorDesc = new EjbIORConfigurationDescriptor();
            EjbIORConfigurationDescriptor iorDesc2 = new EjbIORConfigurationDescriptor();
            String serverSslReqd = orbFactory.getCSIv2Props().getProperty(ORBLocator.ORB_SSL_SERVER_REQUIRED);
            if (serverSslReqd != null && serverSslReqd.equals("true")) {
                iorDesc.setIntegrity(EjbIORConfigurationDescriptor.REQUIRED);
                iorDesc.setConfidentiality(EjbIORConfigurationDescriptor.REQUIRED);
                iorDesc2.setIntegrity(EjbIORConfigurationDescriptor.REQUIRED);
                iorDesc2.setConfidentiality(EjbIORConfigurationDescriptor.REQUIRED);
            }
            String clientAuthReq = orbFactory.getCSIv2Props().getProperty(ORBLocator.ORB_CLIENT_AUTH_REQUIRED);
            if (clientAuthReq != null && clientAuthReq.equals("true")) {
                // Need auth either by SSL or username-password.
                // This sets SSL clientauth to required.
                iorDesc.setEstablishTrustInClient(EjbIORConfigurationDescriptor.REQUIRED);
                // This sets username-password auth to required.
                iorDesc2.setAuthMethodRequired(true);
                getCorbaIORDescSet().add(iorDesc2);
            }
            getCorbaIORDescSet().add(iorDesc);
        } catch (Exception e) {
            LOG.log(ERROR, "IIOP1005: An exception has occured in the ejb security initialization.", e);
        }
    }

    public ConnectionContext getClientConnectionContext() {
        Hashtable h = ConnectionExecutionContext.getContext();
        ConnectionContext scc = (ConnectionContext) h.get(CLIENT_CONNECTION_CONTEXT);
        return scc;
    }

    public void setClientConnectionContext(ConnectionContext scc) {
        Hashtable h = ConnectionExecutionContext.getContext();
        h.put(CLIENT_CONNECTION_CONTEXT, scc);
    }

    /**
     * This method determines if SSL should be used to connect to the target based on client and target policies. It will
     * return null if SSL should not be used or an SocketInfo containing the SSL port if SSL should be used.
     */
    public SocketInfo getSSLPort(IOR ior, ConnectionContext ctx) {
        // blocks until orb initialization completes.
        CompoundSecMech mechanism = null;
        try {
            mechanism = selectSecurityMechanism(ior);
        } catch (SecurityMechanismException sme) {
            throw new RuntimeException(sme.getMessage());
        }
        ctx.setIOR(ior);
        ctx.setMechanism(mechanism);

        TLS_SEC_TRANS ssl = null;
        if (mechanism != null) {
            ssl = getSSLInformation(mechanism);
        }

        if (ssl == null) {
            if (!isSslRequired()) {
                return null;
            }
            // Attempt to create SSL connection to host, ORBInitialPort
            IIOPAddress addr = ((IIOPProfileTemplate) ior.getProfile().getTaggedProfileTemplate()).getPrimaryAddress();
            int port = orbLocator.getORBData().getORBInitialPort();
            return createSocketInfo("SecurityMechanismSelector1", "SSL", addr.getHost(), port);
        }

        int targetRequires = ssl.target_requires;
        int targetSupports = ssl.target_supports;

        /*
         * If target requires any of Integrity, Confidentiality or EstablishTrustInClient, then SSL is used.
         */
        if (isSet(targetRequires, Integrity.value)
            || isSet(targetRequires, Confidentiality.value)
            || isSet(targetRequires, EstablishTrustInClient.value)) {
            LOG.log(DEBUG, "Target requires SSL");
            ctx.setSSLUsed(true);
            String type = "SSL";
            if (isSet(targetRequires, EstablishTrustInClient.value)) {
                type = "SSL_MUTUALAUTH";
                ctx.setSSLClientAuthenticationOccurred(true);
            }
            String host_name = ssl.addresses[0].host_name;
            int ssl_port = Utility.shortToInt(ssl.addresses[0].port);
            return createSocketInfo("SecurityMechanismSelector2", type, host_name, ssl_port);
        } else if (isSet(targetSupports, Integrity.value)
                || isSet(targetSupports, Confidentiality.value)
                || isSet(targetSupports, EstablishTrustInClient.value)) {
            LOG.log(DEBUG, "Target supports SSL");
            if (!isSslRequired()) {
                return null;
            }
            LOG.log(DEBUG, "Client is configured to require SSL for the target");
            ctx.setSSLUsed(true);
            String host_name = ssl.addresses[0].host_name;
            int ssl_port = Utility.shortToInt(ssl.addresses[0].port);
            return createSocketInfo("SecurityMechanismSelector3", "SSL", host_name, ssl_port);
        } else if (isSslRequired()) {
            throw new RuntimeException("SSL required by client but not supported by server.");
        } else {
            return null;
        }
    }

    private synchronized CSIV2TaggedComponentInfo getCtc() {
        if (this.ctc == null) {
            this.ctc = new CSIV2TaggedComponentInfo(orbLocator.getPartiallyInitializedOrb());
        }
        return ctc;
    }

    public java.util.List<SocketInfo> getSSLPorts(IOR ior, ConnectionContext ctx) {
        // blocks until orb initialization completes.
        orbLocator.getORB();
        CompoundSecMech mechanism = null;
        try {
            mechanism = selectSecurityMechanism(ior);
        } catch (SecurityMechanismException sme) {
            throw new RuntimeException(sme.getMessage());
        }
        ctx.setIOR(ior);
        ctx.setMechanism(mechanism);

        TLS_SEC_TRANS ssl = null;
        if (mechanism != null) {
            ssl = getSSLInformation(mechanism);
        }

        if (ssl == null) {
            if (!isSslRequired()) {
                return null;
            }
            // Attempt to create SSL connection to host, ORBInitialPort
            IIOPAddress addr = ((IIOPProfileTemplate) ior.getProfile().getTaggedProfileTemplate()).getPrimaryAddress();
            int port = orbLocator.getORBData().getORBInitialPort();
            SocketInfo info = createSocketInfo("SecurityMechanismSelector1", "SSL", addr.getHost(), port);
            List<SocketInfo> sInfos = new ArrayList<>();
            sInfos.add(info);
            return sInfos;
        }

        int targetRequires = ssl.target_requires;
        int targetSupports = ssl.target_supports;

        /*
         * If target requires any of Integrity, Confidentiality or EstablishTrustInClient, then SSL is used.
         */
        if (isSet(targetRequires, Integrity.value) || isSet(targetRequires, Confidentiality.value) || isSet(targetRequires, EstablishTrustInClient.value)) {
            LOG.log(DEBUG, "Target requires SSL");
            ctx.setSSLUsed(true);
            String type = "SSL";
            if (isSet(targetRequires, EstablishTrustInClient.value)) {
                type = "SSL_MUTUALAUTH";
                ctx.setSSLClientAuthenticationOccurred(true);
            }
            // SocketInfo[] socketInfos = new SocketInfo[ssl.addresses.size];
            List<SocketInfo> socketInfos = new ArrayList<>();
            for (TransportAddress element : ssl.addresses) {
                String host_name = element.host_name;
                int ssl_port = Utility.shortToInt(element.port);
                SocketInfo sInfo = createSocketInfo("SecurityMechanismSelector2", type, host_name, ssl_port);
                socketInfos.add(sInfo);
            }
            return socketInfos;
        } else if (isSet(targetSupports, Integrity.value) || isSet(targetSupports, Confidentiality.value)
                || isSet(targetSupports, EstablishTrustInClient.value)) {
            LOG.log(DEBUG, "Target supports SSL");
            if (!isSslRequired()) {
                return null;
            }
            LOG.log(DEBUG, "Client is configured to require SSL for the target");
            ctx.setSSLUsed(true);
            // SocketInfo[] socketInfos = new SocketInfo[ssl.addresses.size];
            List<SocketInfo> socketInfos = new ArrayList<>();
            for (TransportAddress element : ssl.addresses) {
                String host_name = element.host_name;
                int ssl_port = Utility.shortToInt(element.port);
                SocketInfo sInfo = createSocketInfo("SecurityMechanismSelector3", "SSL", host_name, ssl_port);
                socketInfos.add(sInfo);
            }
            return socketInfos;
        } else if (isSslRequired()) {
            throw new RuntimeException("SSL required by client but not supported by server.");
        } else {
            return null;
        }
    }

    /**
     * Select the security context to be used by the CSIV2 layer based on whether the current component is an application
     * client or a web/EJB component.
     */

    public SecurityContext selectSecurityContext(IOR ior) throws InvalidIdentityTokenException, InvalidMechanismException, SecurityMechanismException {
        SecurityContext context = null;
        ConnectionContext cc = new ConnectionContext();
        // print CSIv2 mechanism definition in IOR
        LOG.log(DEBUG, () -> "CSIv2 Mechanism List: " + getSecurityMechanismString(ctc, ior));

        getSSLPort(ior, cc);
        setClientConnectionContext(cc);

        CompoundSecMech mechanism = cc.getMechanism();
        if (mechanism == null) {
            return null;
        }
        boolean sslUsed = cc.getSSLUsed();
        boolean clientAuthOccurred = cc.getSSLClientAuthenticationOccurred();

        // Standalone client
        if (isNotServerOrACC()) {
            context = getSecurityContextForAppClient(null, sslUsed, clientAuthOccurred, mechanism);
            return context;
        }

        LOG.log(DEBUG, "SSL used: {0}, SSL Mutual auth: {1}", sslUsed, clientAuthOccurred);
        ComponentInvocation ci = null;
        /*
         * // BEGIN IASRI# 4646060 ci = invMgr.getCurrentInvocation(); if (ci == null) { // END IASRI# 4646060 return null; }
         * Object obj = ci.getContainerContext();
         */
        if (isACC()) {
            context = getSecurityContextForAppClient(ci, sslUsed, clientAuthOccurred, mechanism);
        } else {
            context = getSecurityContextForWebOrEJB(ci, sslUsed, clientAuthOccurred, mechanism);
        }
        return context;
    }

    /**
     * Create the security context to be used by the CSIV2 layer to marshal in the service context of the IIOP message from
     * an appclient or standalone client.
     *
     * @return the security context.
     */
    public SecurityContext getSecurityContextForAppClient(ComponentInvocation ci, boolean sslUsed,
        boolean clientAuthOccurred, CompoundSecMech mechanism)
        throws InvalidMechanismException, InvalidIdentityTokenException, SecurityMechanismException {
        return sendUsernameAndPassword(ci, sslUsed, clientAuthOccurred, mechanism);
    }

    /**
     * Create the security context to be used by the CSIV2 layer to marshal in the service context of the IIOP message from
     * an web component or EJB invoking another EJB.
     *
     * @return the security context.
     */
    public SecurityContext getSecurityContextForWebOrEJB(ComponentInvocation ci, boolean sslUsed,
        boolean clientAuthOccurred, CompoundSecMech mechanism)
        throws InvalidMechanismException, InvalidIdentityTokenException, SecurityMechanismException {
        SecurityContext ctx = null;
        if (!sslUsed) {
            ctx = propagateIdentity(false, ci, mechanism);
        } else {
            ctx = propagateIdentity(clientAuthOccurred, ci, mechanism);
        }
        return ctx;
    }

    List<SocketInfo> getSSLSocketInfo(com.sun.corba.ee.spi.ior.IOR ior) {
        ConnectionContext ctx = new ConnectionContext();
        List<SocketInfo> socketInfo = getSSLPorts(ior, ctx);
        setClientConnectionContext(ctx);
        return socketInfo;
    }

    private boolean isMechanismSupported(SAS_ContextSec sas) {
        byte[][] mechanisms = sas.supported_naming_mechanisms;
        byte[] mechSupported = GSSUtils.getMechanism();

        if (mechanisms == null) {
            return false;
        }

        for (byte[] mechanism : mechanisms) {
            if (Arrays.equals(mechSupported, mechanism)) {
                return true;
            }
        }
        return false;
    }

    public boolean isIdentityTypeSupported(SAS_ContextSec sas) {
        int ident_token = sas.supported_identity_types;
        // the identity token matches atleast one of the types we support
        int value = ident_token & CSIV2TaggedComponentInfo.SUPPORTED_IDENTITY_TOKEN_TYPES;
        if (value != 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get the security context to send username and password in the service context.
     *
     * @param whether username/password will be sent over plain IIOP or over IIOP/SSL.
     * @return the security context.
     * @exception SecurityMechanismException if there was an error.
     */
    private SecurityContext sendUsernameAndPassword(ComponentInvocation ci, boolean sslUsed, boolean clientAuthOccurred, CompoundSecMech mechanism)
            throws SecurityMechanismException {
        SecurityContext ctx = null;
        if (mechanism == null) {
            return null;
        }
        AS_ContextSec asContext = mechanism.as_context_mech;
        if (isSet(asContext.target_requires, EstablishTrustInClient.value)
                || (isSet(mechanism.target_requires, EstablishTrustInClient.value) && !clientAuthOccurred)) {

            ctx = getUsernameAndPassword(ci, mechanism);

            LOG.log(DEBUG, "Sending Username/Password");
        } else {
            return null;
        }
        return ctx;
    }

    /**
     * Get the security context to propagate principal/distinguished name in the service context.
     *
     * @param clientAuth whether SSL client authentication has happened.
     * @return the security context.
     * @exception SecurityMechanismException if there was an error.
     */
    private SecurityContext propagateIdentity(boolean clientAuth, ComponentInvocation ci, CompoundSecMech mechanism)
            throws InvalidIdentityTokenException, InvalidMechanismException, SecurityMechanismException {

        SecurityContext ctx = null;
        if (mechanism == null) {
            return null;
        }
        AS_ContextSec asContext = mechanism.as_context_mech;
        SAS_ContextSec sasContext = mechanism.sas_context_mech;
        LOG.log(DEBUG, "SAS CONTEXT's target_requires={0}", sasContext.target_requires);
        LOG.log(DEBUG, "SAS CONTEXT's target_supports={1}", sasContext.target_supports);

        if (isSet(asContext.target_requires, EstablishTrustInClient.value)) {
            ctx = getUsernameAndPassword(ci, mechanism);
            if (ctx.authcls == null) {
                // run as mode cannot send password
                throw new SecurityMechanismException(
                    "Cannot propagate username/password required by target when using run as identity");
            }
        } else if (isSet(sasContext.target_supports, IdentityAssertion.value) || isSet(sasContext.target_requires, IdentityAssertion.value)) {
            // called from the client side. thus before getting the identity. check the
            // mechanisms and the identity token supported
            if (!isIdentityTypeSupported(sasContext)) {
                throw new InvalidIdentityTokenException("The given identity token is unsupported.");
            }
            if (sasContext.target_supports == IdentityAssertion.value) {
                if (!isMechanismSupported(sasContext)) {
                    throw new InvalidMechanismException("The given mechanism type is unsupported.");
                }
            }

            // propagate principal/certificate/distinguished name
            ctx = getIdentity();
        } else if (isSet(asContext.target_supports, EstablishTrustInClient.value)) {
            if (clientAuth) { // client auth done we can send password
                ctx = getUsernameAndPassword(ci, mechanism);
                if (ctx.authcls == null) {
                    return null; // runas mode dont have username/password
                                 // dont really need to send it too
                }
            } else { // not sending anything for unauthenticated client
                return null;
            }
        } else {
            return null; // will never come here
        }
        return ctx;
    }

    /**
     * Get the username and password either from the JAAS subject or from thread local storage. For appclients if login
     * has'nt happened this method would trigger login and popup a user interface to gather authentication information.
     *
     * @return the security context.
     */
    private SecurityContext getUsernameAndPassword(ComponentInvocation ci, CompoundSecMech mechanism) throws SecurityMechanismException {
        try {
            Subject s = null;
            // if(ci == null) {
            if (isNotServerOrACC()) {
                // Standalone client ... Changed the security context
                // from which to fetch the subject
                ClientSecurityContext sc = ClientSecurityContext.getCurrent();
                if (sc == null) {
                    return null;
                }
                s = sc.getSubject();
                LOG.log(DEBUG, "SUBJECT: {0}", s);
            } else {
                // Object obj = ci.getContainerContext();
                // if(obj instanceof AppContainer) {
                if (isACC()) {
                    // get the subject
                    ClientSecurityContext sc = ClientSecurityContext.getCurrent();
                    if (sc == null) {
                        s = LoginContextDriver.doClientLogin(SecurityConstants.USERNAME_PASSWORD, SecurityServicesUtil.getInstance().getCallbackHandler());
                    } else {
                        s = sc.getSubject();
                    }
                } else {
                    // web/ejb
                    s = getSubjectFromSecurityCurrent();
                    // TODO check if username/password is available
                    // if not throw exception
                }
            }
            SecurityContext ctx = new SecurityContext();
            final Subject sub = s;
            ctx.subject = s;
            // determining if run-as has been used
            Set<PasswordCredential> privateCredSet = sub.getPrivateCredentials(PasswordCredential.class);
            // this is runas case dont set
            if (privateCredSet.isEmpty()) {
                LOG.log(DEBUG, "No private credential run as mode");
                // the auth class
                ctx.authcls = null;
                ctx.identcls = GSSUPName.class;

            } else {
                /**
                 * lookup the realm name that is required by the server and set it up in the PasswordCredential class.
                 */
                AS_ContextSec asContext = mechanism.as_context_mech;
                final byte[] target_name = asContext.target_name;
                byte[] _realm = null;
                if (target_name == null || target_name.length == 0) {
                    _realm = Realm.getDefaultRealm().getBytes();
                } else {
                    _realm = GSSUtils.importName(GSSUtils.GSSUP_MECH_OID, target_name);
                }
                final String realm_name = new String(_realm);
                final Iterator<PasswordCredential> it = privateCredSet.iterator();
                for (; it.hasNext();) {
                    PasswordCredential pc = it.next();
                    pc.setRealm(realm_name);
                }
                ctx.authcls = PasswordCredential.class;
            }
            return ctx;
        } catch (LoginException le) {
            throw le;
        } catch (Exception e) {
            LOG.log(ERROR, "IIOP1001: Exception getting username and password", e);
            return null;
        }
    }

    /**
     * Get the principal/distinguished name from thread local storage.
     *
     * @return the security context.
     */
    private SecurityContext getIdentity() throws SecurityMechanismException {
        LOG.log(DEBUG, "Getting PRINCIPAL/DN from TLS");

        SecurityContext ctx = new SecurityContext();
        final SecurityContext sCtx = ctx;
        // get stuff from the SecurityContext class
        com.sun.enterprise.security.SecurityContext scontext = com.sun.enterprise.security.SecurityContext.getCurrent();
        if (scontext == null || scontext.didServerGenerateCredentials()) {

            // a default guest/guest123 was created
            sCtx.identcls = AnonCredential.class;
            // remove all the public and private credentials
            Subject sub = new Subject();
            sCtx.subject = sub;
            sCtx.subject.getPublicCredentials().add(new AnonCredential());
            return sCtx;
        }

        Subject s = getSubjectFromSecurityCurrent();
        ctx.subject = s;

        // Figure out the credential class
        Set<PasswordCredential> credSet = s.getPrivateCredentials(PasswordCredential.class);
        if (credSet.size() == 1) {
            ctx.identcls = GSSUPName.class;
            Subject subj = new Subject();
            Iterator<PasswordCredential> iter = credSet.iterator();
            PasswordCredential pc = iter.next();
            GSSUPName gssname = new GSSUPName(pc.getUser(), pc.getRealm());
            subj.getPublicCredentials().add(gssname);
            ctx.subject = subj;
            return ctx;
        }

        Set<?> pubCredSet = s.getPublicCredentials();
        if (pubCredSet.size() != 1) {
            LOG.log(ERROR, "IIOP1002: Principal propagation: Cannot find principal information in subject");
            return null;
        }
        Iterator<?> credIter = pubCredSet.iterator();
        if (credIter.hasNext()) {
            Object o = credIter.next();
            if (o instanceof GSSUPName) {
                ctx.identcls = GSSUPName.class;
            } else if (o instanceof X500Principal) {
                ctx.identcls = X500Principal.class;
            } else {
                ctx.identcls = X509CertificateCredential.class;
            }
        } else {
            LOG.log(ERROR, "IIOP1003: Principal propagation: Cannot find credential information in subject.");
            return null;
        }
        return ctx;
    }

    private Subject getSubjectFromSecurityCurrent() throws SecurityMechanismException {
        com.sun.enterprise.security.SecurityContext sc = null;
        sc = com.sun.enterprise.security.SecurityContext.getCurrent();
        if (sc == null) {
            sc = com.sun.enterprise.security.SecurityContext.init();
        }
        if (sc == null) {
            throw new SecurityMechanismException("Could not find security information");
        }
        Subject s = sc.getSubject();
        LOG.log(DEBUG, "Subject in security current: {0}", s);
        if (s == null) {
            throw new SecurityMechanismException("Could not find subject information in the security context.");
        }
        return s;
    }

    private CompoundSecMech selectSecurityMechanism(IOR ior) throws SecurityMechanismException {
        return selectSecurityMechanism(getCtc().getSecurityMechanisms(ior));
    }

    /**
     * Select the security mechanism from the list of compound security mechanisms.
     */
    private CompoundSecMech selectSecurityMechanism(CompoundSecMech[] mechList) throws SecurityMechanismException {
        // We should choose from list of compound security mechanisms
        // which are in decreasing preference order. Right now we select
        // the first one.
        if (mechList == null || mechList.length == 0) {
            return null;
        }
        for (CompoundSecMech mech : mechList) {
            if (useMechanism(mech)) {
                return mech;
            }
        }
        throw new SecurityMechanismException("Cannot use any of the target's supported mechanisms");
    }

    private boolean useMechanism(CompoundSecMech mech) {
        boolean val = true;
        TLS_SEC_TRANS tls = getSSLInformation(mech);

        if (mech.sas_context_mech.supported_naming_mechanisms.length > 0 && !isMechanismSupported(mech.sas_context_mech)) {
            return false;
        } else if (mech.as_context_mech.client_authentication_mech.length > 0 && !isMechanismSupportedAS(mech.as_context_mech)) {
            return false;
        }

        if (tls == null) {
            return true;
        }
        int targetRequires = tls.target_requires;
        if (isSet(targetRequires, EstablishTrustInClient.value)) {
            if (!sslUtils.isKeyAvailable()) {
                val = false;
            }
        }
        return val;
    }

    private boolean isMechanismSupportedAS(AS_ContextSec as) {
        byte[] mechanism = as.client_authentication_mech;
        byte[] mechSupported = GSSUtils.getMechanism();

        if (mechanism == null) {
            return false;
        }

        if (Arrays.equals(mechanism, mechSupported)) {
            return true;
        }

        return false;
    }

    // Returns the target_name from PasswordCredential in Subject subj
    // subj must contain a single instance of PasswordCredential.

    private byte[] getTargetName(Subject subj) {

        byte[] tgt_name = {};

        final Subject sub = subj;
        final Set<PasswordCredential> credset =  sub.getPrivateCredentials(PasswordCredential.class);
        if (credset.size() == 1) {
            Iterator<PasswordCredential> iter = credset.iterator();
            PasswordCredential pc = iter.next();
            tgt_name = pc.getTargetName();
        }
        return tgt_name;
    }

    private boolean evaluateClientConformanceSsl(EjbIORConfigurationDescriptor iordesc, boolean sslUsed, X509Certificate[] certchain) {
        LOG.log(DEBUG, "evaluateClientConformanceSsl(iordesc={0}, ssl_used={1}, certchain={2})", iordesc, sslUsed, certchain);
        try {
            final int sslTargetRequires = getCtc().getTargetRequires(iordesc);
            final boolean sslCtcRequired = isSet(sslTargetRequires, Integrity.value)
                || isSet(sslTargetRequires, Confidentiality.value)
                || isSet(sslTargetRequires, EstablishTrustInClient.value);
            final boolean sslCtcSupported = getCtc().getTargetSupports(iordesc) != 0;
            /*
             * Check for conformance for using SSL usage.
             * a. if SSL was used, then either the target must require or support SSL. In the latter
             * case, SSL is used because of client policy.
             * b. if SSL was not used, then the target must not require it either. The target may or
             * may not support SSL (it is irrelevant).
             */
            if (LOG.isLoggable(DEBUG)) {
                LOG.log(DEBUG,
                    () -> "evaluateClientConformanceSsl "
                        + isSet(sslTargetRequires, Integrity.value) + " "
                        + isSet(sslTargetRequires, Confidentiality.value) + " "
                        + isSet(sslTargetRequires, EstablishTrustInClient.value) + " " + sslCtcRequired + " "
                        + sslCtcSupported + " " + sslUsed);
            }

            // Security mechanisms did not match.
            if (sslUsed) {
                if (!sslCtcRequired && !sslCtcSupported) {
                    LOG.log(DEBUG, "SSL was used, but it is not required and not supported, returning false.");
                    return false;
                }
            } else {
                if (sslCtcRequired) {
                    LOG.log(DEBUG, "SSL was not used, but it is required, returning false.");
                    return false;
                }
            }

            /*
             * Check for conformance for SSL client authentication.
             *
             * a. if client performed SSL client authentication, then the target must either require or support SSL client
             * authentication. If the target only supports, SSL client authentication is used because of client security policy.
             *
             * b. if SSL client authentication was not used, then the target must not require SSL client authentcation either. The
             * target may or may not support SSL client authentication (it is irrelevant).
             */

            LOG.log(DEBUG,
                () -> "evaluateClientConformanceSsl: "
                    + isSet(sslTargetRequires, EstablishTrustInClient.value) + " "
                    + isSet(getCtc().getTargetSupports(iordesc), EstablishTrustInClient.value));

            // security mechanism did not match
            if (certchain == null) {
                if (isSet(sslTargetRequires, EstablishTrustInClient.value)) {
                    return false;
                }
            } else {
                if (!isSet(sslTargetRequires, EstablishTrustInClient.value)
                    && !isSet(getCtc().getTargetSupports(iordesc), EstablishTrustInClient.value)) {
                    return false;
                }
            }

            // mechanism matched
            LOG.log(DEBUG, "evaluateClientConformanceSsl returns true");
            return true;
        } finally {
            LOG.log(DEBUG, "evaluateClientConformanceSsl finished.");
        }
    }

    /*
     * Evaluates a client's conformance to a security policies at the client authentication layer.
     *
     * returns true if conformant ; else returns false
     */
    private boolean evaluateClientConformanceAsContext(SecurityContext ctx, EjbIORConfigurationDescriptor iordesc, String realmName) {

        boolean client_authenticated = false;

        // get requirements and supports at the client authentication layer
        AS_ContextSec ascontext = null;
        try {
            ascontext = this.getCtc().createASContextSec(iordesc, realmName);
        } catch (Exception e) {
            LOG.log(ERROR, "IIOP1000: Exception creating ASContext", e);
            return false;
        }

        /*************************************************************************
         * Conformance Matrix:
         *
         * |------------|---------------------|---------------------|------------| | ClientAuth | targetrequires.ETIC |
         * targetSupports.ETIC | Conformant | |------------|---------------------|---------------------|------------| | Yes | 0
         * | 1 | Yes | | Yes | 0 | 0 | No | | Yes | 1 | X | Yes | | No | 0 | X | Yes | | No | 1 | X | No |
         * |------------|---------------------|---------------------|------------|
         *
         * Abbreviations: ETIC - EstablishTrusInClient
         *
         *************************************************************************/

        if ((ctx != null) && (ctx.authcls != null) && (ctx.subject != null)) {
            client_authenticated = true;
        } else {
            client_authenticated = false;
        }

        if (client_authenticated) {
            if (!(isSet(ascontext.target_requires, EstablishTrustInClient.value) || isSet(ascontext.target_supports, EstablishTrustInClient.value))) {
                return false; // non conforming client
            }
            // match the target_name from client with the target_name in policy

            byte[] client_tgtname = getTargetName(ctx.subject);

            if (ascontext.target_name.length != client_tgtname.length) {
                return false; // mechanism did not match.
            }
            for (int i = 0; i < ascontext.target_name.length; i++) {
                if (ascontext.target_name[i] != client_tgtname[i]) {
                    return false; // mechanism did not match
                }
            }
        } else {
            if (isSet(ascontext.target_requires, EstablishTrustInClient.value)) {
                return false; // no mechanism match.
            }
        }
        return true;
    }

    /*
     * Evaluates a client's conformance to a security policy at the sas context layer. The security policy is derived from
     * the EjbIORConfigurationDescriptor.
     *
     * returns true if conformant ; else returns false
     */
    private boolean evaluateClientConformanceSasContext(SecurityContext ctx, EjbIORConfigurationDescriptor iordesc) {

        boolean caller_propagated = false;

        // get requirements and supports at the sas context layer
        SAS_ContextSec sascontext = null;
        try {
            sascontext = this.getCtc().createSASContextSec(iordesc);
        } catch (Exception e) {
            LOG.log(ERROR, "Failed to create SAS_ContextSec", e);
            return false;
        }

        if ((ctx != null) && (ctx.identcls != null) && (ctx.subject != null)) {
            caller_propagated = true;
        } else {
            caller_propagated = false;
        }

        if (caller_propagated) {
            if (!isSet(sascontext.target_supports, IdentityAssertion.value)) {
                return false; // target does not support IdentityAssertion
            }

            /*
             * There is no need further checking here since SecServerRequestInterceptor code filters out the following: a.
             * IdentityAssertions of types other than those required by level 0 (for e.g. IdentityExtension) b. unsupported identity
             * types.
             *
             * The checks are done in SecServerRequestInterceptor rather than here to minimize code changes.
             */
            return true;
        }
        return true; // either caller was not propagated or mechanism matched.
    }

    /**
     * Evaluates a client's conformance to the security policies configured on the target. Returns true if conformant to the
     * security policies otherwise return false.
     *
     * Conformance checking is done as follows: First, the object_id is mapped to the set of EjbIORConfigurationDescriptor.
     * Each EjbIORConfigurationDescriptor corresponds to a single CompoundSecMechanism of the CSIv2 spec. A client is
     * considered to be conformant if a CompoundSecMechanism consistent with the client's actions is found i.e.
     * transport_mech, as_context_mech and sas_context_mech must all be consistent.
     *
     */
    private boolean evaluateClientConformance(SecurityContext ctx, byte[] object_id, boolean sslUsed, X509Certificate[] certchain) {
        // Obtain the IOR configuration descriptors for the Ejb using
        // the object_id within the SecurityContext field.

        // if object_id is null then nothing to evaluate. This is a sanity
        // check - for the object_id should never be null.

        if (object_id == null) {
            return true;
        }

        if (protocolMgr == null) {
            protocolMgr = orbLocator.getProtocolManager();
        }

        // Check to make sure protocolMgr is not null.
        // This could happen during server initialization or if this call
        // is on a callback object in the client VM.
        if (protocolMgr == null) {
            return true;
        }

        EjbDescriptor ejbDesc = protocolMgr.getEjbDescriptor(object_id);

        Set iorDescSet = null;
        if (ejbDesc != null) {
            iorDescSet = ejbDesc.getIORConfigurationDescriptors();
        } else {
            // Probably a non-EJB CORBA object.
            // Create a temporary EjbIORConfigurationDescriptor.
            iorDescSet = getCorbaIORDescSet();
        }

        LOG.log(DEBUG, "valuate_client_conformance: iorDescSet: {0}", iorDescSet);

        /*
         * if there are no IORConfigurationDescriptors configured, then no security policy is configured. So consider the client
         * to be conformant.
         */
        if (iorDescSet.isEmpty()) {
            return true;
        }

        // go through each EjbIORConfigurationDescriptor trying to find
        // a find a CompoundSecMechanism that matches client's actions.
        boolean checkSkipped = false;
        for (Object element : iorDescSet) {
            EjbIORConfigurationDescriptor iorDesc = (EjbIORConfigurationDescriptor) element;
            if (skipClientConformance(iorDesc)) {
                LOG.log(DEBUG, "Client conformance evaluation skipped for iorDesc={0}", iorDesc);
                checkSkipped = true;
                continue;
            }
            if (!evaluateClientConformanceSsl(iorDesc, sslUsed, certchain)) {
                LOG.log(DEBUG, "SSL: Client conformance evaluation NOT skipped for iorDesc={0} and sslUsed={1}",
                    iorDesc, sslUsed);
                checkSkipped = false;
                continue;
            }
            String realmName = "default";
            if (ejbDesc != null && ejbDesc.getApplication() != null) {
                realmName = ejbDesc.getApplication().getRealm();
            }
            if (realmName == null) {
                realmName = iorDesc.getRealmName();
            }
            if (realmName == null) {
                realmName = "default";
            }
            if (!evaluateClientConformanceAsContext(ctx, iorDesc, realmName)) {
                LOG.log(DEBUG, "AS Context: Client conformance evaluation NOT skipped for iorDesc={0} and realmName={1}",
                    iorDesc, realmName);
                checkSkipped = false;
                continue;
            }
            if (!evaluateClientConformanceSasContext(ctx, iorDesc)) {
                LOG.log(DEBUG, "SAS Context: Client conformance evaluation NOT skipped for iorDesc={0} and realmName={1}",
                    iorDesc, realmName);
                checkSkipped = false;
                continue;
            }
            return true; // security policy matched.
        }
        // was matching security policy found?
        return checkSkipped;
    }

    /**
     * if ejb requires no security - then skip checking the client-conformance
     */
    private boolean skipClientConformance(EjbIORConfigurationDescriptor ior) {
        String none = EjbIORConfigurationDescriptor.NONE;
        // sanity check
        if (ior == null) {
            return false;
        }
        // SSL is required and/or supported either
        if (!none.equalsIgnoreCase(ior.getIntegrity())) {
            return false;
        }
        if (!none.equalsIgnoreCase(ior.getConfidentiality())) {
            return false;
        }
        if (!none.equalsIgnoreCase(ior.getEstablishTrustInClient())) {
            return false;
        }
        // Username password is required
        if (ior.isAuthMethodRequired()) {
            return false;
        }
        // caller propagation is supported
        if (!none.equalsIgnoreCase(ior.getCallerPropagation())) {
            return false;
        }
        return true;
    }

    /**
     * Called by the target to interpret client credentials after validation.
     */
    public SecurityContext evaluateTrust(SecurityContext ctx, byte[] object_id, Socket socket) throws SecurityMechanismException {
        SecurityContext ssc = null;

        // ssl_used is true if SSL was used.
        boolean ssl_used = false;

        // X509 Certificicate chain is non null if client has authenticated at
        // the SSL level.

        X509Certificate[] certChain = null;

        // First gather all the information and then check the
        // conformance of the client to the security policies.
        // If the test for client conformance passes, then set the
        // security context.
        if ((socket != null) && (socket instanceof SSLSocket)) {
            ssl_used = true; // SSL was used
            // checkif there is a transport principal
            SSLSocket sslSock = (SSLSocket) socket;
            SSLSession sslSession = sslSock.getSession();
            try {
                certChain = (X509Certificate[]) sslSession.getPeerCertificates();
            } catch (Exception e) {
                LOG.log(DEBUG, "Cannot retrieve peer certificates", e);
            }
        }

        // For a local invocation - we don't need to check the security
        // policies. The following condition guarantees the call is local
        // and thus bypassing policy checks.

        // XXX: Workaround for non-null connection object ri for local invocation.
        // if (socket == null && ctx == null)
        Long ClientID = ConnectionExecutionContext.readClientThreadID();
        if (ClientID != null && ClientID == Thread.currentThread().getId() && ctx == null) {
            return null;
        }

        if (!evaluateClientConformance(ctx, object_id, ssl_used, certChain)) {
            throw new SecurityMechanismException(
                "Trust evaluation failed because client does not conform to configured security policies");
        }

        if (ctx == null) {
            if (socket == null || !ssl_used || certChain == null) {
                // Transport info is null and security context is null.
                // No need to set the anonymous credential here,
                // it will get set if any security operations
                // (e.g. getCallerPrincipal) are done.
                // Note: if the target object is not an EJB,
                // no security ctx is needed.
                return null;
            }
            // Set the transport principal in subject and
            // return the X500Principal class
            ssc = new SecurityContext();
            X500Principal x500principal = certChain[0].getSubjectX500Principal();
            ssc.subject = new Subject();
            ssc.subject.getPublicCredentials().add(x500principal);
            ssc.identcls = X500Principal.class;
            ssc.authcls = null;
            return ssc;
        } else {
            ssc = ctx;
        }

        Class authCls = ctx.authcls;
        Class identCls = ctx.identcls;

        ssc.authcls = null;
        ssc.identcls = null;

        if (identCls != null) {
            ssc.identcls = identCls;
        } else if (authCls != null) {
            ssc.authcls = authCls;
        } else {
            ssc.identcls = AnonCredential.class;
        }

        return ssc;
    }

    private static boolean isSet(int val1, int val2) {
        if ((val1 & val2) == val2) {
            return true;
        }
        return false;
    }

    private Set<EjbIORConfigurationDescriptor> getCorbaIORDescSet() {
        return corbaIORDescSet;
    }

    public boolean isSslRequired() {
        return sslRequired;
    }

    private boolean isNotServerOrACC() {
        return processEnv.getProcessType().equals(ProcessType.Other);
    }

    private boolean isACC() {
        return processEnv.getProcessType().equals(ProcessType.ACC);
    }

    private static final Hashtable<Integer, String> assocOptions;

    static {
        assocOptions = new Hashtable<>();
        assocOptions.put(Integer.valueOf(Integrity.value), "Integrity");
        assocOptions.put(Integer.valueOf(Confidentiality.value), "Confidentiality");
        assocOptions.put(Integer.valueOf(EstablishTrustInTarget.value), "EstablishTrustInTarget");
        assocOptions.put(Integer.valueOf(EstablishTrustInClient.value), "EstablishTrustInClient");
        assocOptions.put(Integer.valueOf(IdentityAssertion.value), "IdentityAssertion");
        assocOptions.put(Integer.valueOf(DelegationByClient.value), "DelegationByClient");
    }

    private static final Hashtable<Integer, String> identityTokenTypes;

    static {
        identityTokenTypes = new Hashtable<>();
        identityTokenTypes.put(Integer.valueOf(ITTAnonymous.value), "Anonymous");
        identityTokenTypes.put(Integer.valueOf(ITTPrincipalName.value), "PrincipalName");
        identityTokenTypes.put(Integer.valueOf(ITTX509CertChain.value), "X509CertChain");
        identityTokenTypes.put(Integer.valueOf(ITTDistinguishedName.value), "DistinguishedName");
    }

    public String getSecurityMechanismString(CSIV2TaggedComponentInfo tCI, IOR ior) {
        // need to print out top port value and hosr ior.getProfile().isLocal();
        String typeId = ior.getTypeId();
        CompoundSecMech[] mechList = tCI.getSecurityMechanisms(ior);
        return getSecurityMechanismString(mechList, typeId);
    }

    private String getSecurityMechanismString(CompoundSecMech[] list, String name) {
        StringBuilder b = new StringBuilder();
        b.append("\ntypeId: ").append(name);
        try {
            for (int i = 0; list != null && i < list.length; i++) {
                CompoundSecMech m = list[i];
                b.append("\nCSIv2 CompoundSecMech[").append(i).append("]\n\tTarget Requires:");
                Enumeration<Integer> keys = assocOptions.keys();
                while (keys.hasMoreElements()) {
                    Integer j = keys.nextElement();
                    if (isSet(m.target_requires, j.intValue())) {
                        b.append("\n\t\t").append(assocOptions.get(j));
                    }
                }

                TLS_SEC_TRANS ssl = getSSLInformation(m);
                if (ssl != null) {
                    b.append("\n\tTLS_SEC_TRANS\n\t\tTarget Requires:");
                    keys = assocOptions.keys();
                    while (keys.hasMoreElements()) {
                        Integer j = keys.nextElement();
                        if (isSet(ssl.target_requires, j.intValue())) {
                            b.append("\n\t\t\t").append(assocOptions.get(j));
                        }
                    }
                    b.append("\n\t\tTarget Supports:");
                    keys = assocOptions.keys();
                    while (keys.hasMoreElements()) {
                        Integer j = keys.nextElement();
                        if (isSet(ssl.target_supports, j.intValue())) {
                            b.append("\n\t\t\t").append(assocOptions.get(j));
                        }
                    }
                    TransportAddress[] aList = ssl.addresses;
                    for (int j = 0; j < aList.length; j++) {
                        TransportAddress a = aList[j];
                        b.append("\n\t\tAddress[").append(j).append("] Host Name: ").append(a.host_name).append(" port: ").append(a.port);
                    }
                }

                AS_ContextSec asContext = m.as_context_mech;
                if (asContext != null) {
                    b.append("\n\tAS_ContextSec\n\t\tTarget Requires:");
                    keys = assocOptions.keys();
                    while (keys.hasMoreElements()) {
                        Integer j = keys.nextElement();
                        if (isSet(asContext.target_requires, j.intValue())) {
                            b.append("\n\t\t\t").append(assocOptions.get(j));
                        }
                    }
                    b.append("\n\t\tTarget Supports:");
                    keys = assocOptions.keys();
                    while (keys.hasMoreElements()) {
                        Integer j = keys.nextElement();
                        if (isSet(asContext.target_supports, j.intValue())) {
                            b.append("\n\t\t\t").append(assocOptions.get(j));
                        }
                    }
                    try {
                        if (asContext.client_authentication_mech.length > 0) {
                            Oid oid = new Oid(asContext.client_authentication_mech);
                            b.append("\n\t\tclient_auth_mech_OID:").append(oid);
                        } else {
                            b.append("\n\t\tclient_auth_mech_OID: undefined");
                        }
                    } catch (Exception e) {
                        b.append("\n\t\tclient_auth_mech_OID: (invalid)").append(e.getMessage());
                    } finally {
                        b.append("\n\t\ttarget_name:").append(new String(asContext.target_name));
                    }
                }

                SAS_ContextSec sasContext = m.sas_context_mech;
                if (sasContext != null) {
                    b.append("\n\tSAS_ContextSec\n\t\tTarget Requires:");
                    keys = assocOptions.keys();
                    while (keys.hasMoreElements()) {
                        Integer j = keys.nextElement();
                        if (isSet(sasContext.target_requires, j.intValue())) {
                            b.append("\n\t\t\t").append(assocOptions.get(j));
                        }
                    }
                    b.append("\n\t\tTarget Supports:");
                    keys = assocOptions.keys();
                    while (keys.hasMoreElements()) {
                        Integer j = keys.nextElement();
                        if (isSet(sasContext.target_supports, j.intValue())) {
                            b.append("\n\t\t\t").append(assocOptions.get(j));
                        }
                    }
                    b.append("\n\t\tprivilege authorities:").append(Arrays.toString(sasContext.privilege_authorities));
                    byte[][] nameTypes = sasContext.supported_naming_mechanisms;
                    for (int j = 0; j < nameTypes.length; j++) {
                        try {
                            if (nameTypes[j].length > 0) {
                                Oid oid = new Oid(nameTypes[j]);
                                b.append("\n\t\tSupported Naming Mechanim[").append(j).append("]: ").append(oid);
                            } else {
                                b.append("\n\t\tSupported Naming Mechanim[").append(j).append("]:  undefined");
                            }
                        } catch (Exception e) {
                            b.append("\n\t\tSupported Naming Mechanism[").append(j).append("]: (invalid)").append(e.getMessage());
                        }
                    }
                    b.append("\n\t\tsupported Identity Types:");
                    long map = sasContext.supported_identity_types;
                    keys = identityTokenTypes.keys();
                    while (keys.hasMoreElements()) {
                        Integer j = keys.nextElement();
                        if (isSet(sasContext.supported_identity_types, j.intValue())) {
                            b.append("\n\t\t\t").append(identityTokenTypes.get(j));
                            map = map - j.intValue();
                        }
                    }
                    if (map > 0) {
                        b.append("\n\t\t\tcustom bits set: ").append(map);
                    }
                }
            }
            b.append("\n\n");
        } catch (Exception e) {
            LOG.log(ERROR, "Unexpected exception when trying to create a toString", e);
            // return whatever we already have.
            return b.toString();
        }
        return b.toString();
    }

    /**
     * Retrieve the SSL tagged component from the compound security mechanism.
     */
    private TLS_SEC_TRANS getSSLInformation(CompoundSecMech mech) {
        org.omg.IOP.TaggedComponent pcomp = mech.transport_mech;
        TLS_SEC_TRANS ssl = getSSLComponent(pcomp);
        return ssl;
    }

    private TLS_SEC_TRANS getSSLComponent(org.omg.IOP.TaggedComponent comp) {

        // a TAG_NULL_TAG implies that SSL is not required
        if (comp.tag == TAG_NULL_TAG.value) {
            return null;
        }

        byte[] b = comp.component_data;
        CDRInputObject in = new EncapsInputStream(orbLocator.getORB(), b, b.length);
        in.consumeEndian();
        return TLS_SEC_TRANSHelper.read(in);
    }
}
