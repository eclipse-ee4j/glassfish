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

import com.sun.corba.ee.org.omg.CSI.AuthorizationElement;
import com.sun.corba.ee.org.omg.CSI.EstablishContext;
import com.sun.corba.ee.org.omg.CSI.GSS_NT_ExportedNameHelper;
import com.sun.corba.ee.org.omg.CSI.IdentityToken;
import com.sun.corba.ee.org.omg.CSI.MTCompleteEstablishContext;
import com.sun.corba.ee.org.omg.CSI.MTContextError;
import com.sun.corba.ee.org.omg.CSI.SASContextBody;
import com.sun.corba.ee.org.omg.CSI.SASContextBodyHelper;
import com.sun.corba.ee.org.omg.CSI.X501DistinguishedNameHelper;
import com.sun.corba.ee.org.omg.CSI.X509CertificateChainHelper;
import com.sun.corba.ee.org.omg.CSIIOP.CompoundSecMech;
import com.sun.enterprise.common.iiop.security.AnonCredential;
import com.sun.enterprise.common.iiop.security.GSSUPName;
import com.sun.enterprise.common.iiop.security.SecurityContext;
import com.sun.enterprise.security.auth.login.common.PasswordCredential;
import com.sun.enterprise.security.auth.login.common.X509CertificateCredential;
import com.sun.logging.LogDomains;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.x500.X500Principal;

import org.glassfish.enterprise.iiop.api.GlassFishORBHelper;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.LOCATION_FORWARD;
import org.omg.PortableInterceptor.SUCCESSFUL;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.omg.PortableInterceptor.TRANSPORT_RETRY;
import org.omg.PortableInterceptor.USER_EXCEPTION;

import static com.sun.logging.LogDomains.SECURITY_LOGGER;
import static java.util.Arrays.asList;

/**
 * This class implements a client side security request interceptor for CSIV2. It is used to send and receive the
 * service context in a service context element in the service context list in an IIOP header.
 */
public class SecClientRequestInterceptor extends org.omg.CORBA.LocalObject implements ClientRequestInterceptor {

    private static final Logger LOG = LogDomains.getLogger(SecClientRequestInterceptor.class, SECURITY_LOGGER, false);

    /** name of interceptor */
    private final String name;

    /**
     * prname (name + "::") is name of interceptor used for logging purposes. It is only used in the call to
     * Logger.methodentry() in this file. Its purpose is to identify the interceptor name
     */
    private final String prname;
    /** used for marshalling */
    private final Codec codec;
    private final GlassFishORBHelper orbHelper;
    private final SecurityContextUtil secContextUtil;

    /**
     * Hard code the value of 15 for SecurityAttributeService until it is defined in IOP.idl. sc.context_id =
     * SecurityAttributeService.value;
     */
    protected static final int SECURITY_ATTRIBUTE_SERVICE_ID = 15;

    public SecClientRequestInterceptor(String name, Codec codec) {
        this.name = name;
        this.codec = codec;
        this.prname = name + "::";
        orbHelper = Lookups.getGlassFishORBHelper();
        secContextUtil = Lookups.getSecurityContextUtil();
    }

    @Override
    public String name() {
        return name;
    }

    /**
     * Retrieves a single credential from a credset for the specified class. It also performs some semantic checking and
     * logging.
     *
     * A null is returned if semantic checking fails.
     */
    private java.lang.Object getCred(Set credset, Class c) {

        java.lang.Object cred = null; // return value
        String clsname = c.getName();

        /* check that there is only instance of a credential in the subject */
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Checking for a single instance of class in subject");
            LOG.log(Level.FINE, "    Classname = " + clsname);
        }
        if (credset.size() != 1) {
            throw new SecurityException("Credential list size is not 1, but " + credset.size());
        }

        Iterator iter = credset.iterator();
        while (iter.hasNext()) {
            cred = iter.next();
        }
        LOG.log(Level.FINE, "Verified single instance of class {0}", clsname);
        return cred;
    }

    /**
     * Returns a client authentication token for the PasswordCredential in the subject. The client authentication token is
     * cdr encoded.
     */

    private byte[] createAuthToken(java.lang.Object cred, Class cls, ORB orb, CompoundSecMech mech) throws Exception {
        byte[] gsstoken = {}; // GSS token

        if (PasswordCredential.class.isAssignableFrom(cls)) {

            LOG.log(Level.FINE, "Constructing a PasswordCredential client auth token");

            /* Generate mechanism specific GSS token for the GSSUP mechanism */
            PasswordCredential pwdcred = (PasswordCredential) cred;
            GSSUPToken tok = GSSUPToken.getClientSideInstance(orb, codec, pwdcred, mech);
            gsstoken = tok.getGSSToken();
        }
        return gsstoken;
    }

    /**
     * create and return an identity token from the credential. The identity token is cdr encoded.
     */
    private IdentityToken createIdToken(java.lang.Object cred, Class cls, ORB orb) throws Exception {

        IdentityToken idtok = null;

        // byte[] cdrval ; // CDR encoding buffer
        Any any = orb.create_any();
        idtok = new IdentityToken();

        if (X500Principal.class.isAssignableFrom(cls)) {
            LOG.log(Level.FINE, "Constructing an X500 DN Identity Token");
            X500Principal x500Principal = (X500Principal) cred;
            X501DistinguishedNameHelper.insert(any, x500Principal.getEncoded());

            /* IdentityToken with CDR encoded X500 principal */
            idtok.dn(codec.encode_value(any));
        } else if (X509CertificateCredential.class.isAssignableFrom(cls)) {
            LOG.log(Level.FINE, "Constructing an X509 Certificate Chain Identity Token");

            /* create a DER encoding */
            X509CertificateCredential certcred = (X509CertificateCredential) cred;
            X509Certificate[] certchain = certcred.getX509CertificateChain();
            LOG.log(Level.FINE, "Certchain length = {0}", certchain.length);

            byte[] certBytes = CertificateFactory.getInstance("X.509")
                    .generateCertPath(asList(certchain))
                    .getEncoded();

            X509CertificateChainHelper.insert(any, certBytes);

            /* IdentityToken with CDR encoded certificate chain */
            idtok.certificate_chain(codec.encode_value(any));
        } else if (AnonCredential.class.isAssignableFrom(cls)) {
            LOG.log(Level.FINE, "Constructing an Anonymous Identity Token");
            idtok.anonymous(true);

        } else if (GSSUPName.class.isAssignableFrom(cls)) {
            /* GSSAPI Exported name */
            LOG.log(Level.FINE, "Constructing a GSS Exported name Identity Token");
            /* create a DER encoding */
            GSSUPName gssname = (GSSUPName) cred;

            byte[] expname = gssname.getExportedName();
            GSS_NT_ExportedNameHelper.insert(any, expname);

            /* IdentityToken with CDR encoded GSSUPName */
            idtok.principal_name(codec.encode_value(any));
        }

        return (idtok);
    }

    /**
     * send_request() interception point adds the security context to the service context field.
     */
    @Override
    public void send_request(ClientRequestInfo ri) throws ForwardRequest {
        /**
         * CSIV2 level 0 implementation only requires stateless clients. Client context id is therefore always set to 0.
         */
        long cContextId = 0; // CSIV2 requires type to be long

        // XXX: Workaround for non-null connection object ri for local invocation.
        ConnectionExecutionContext.removeClientThreadID();
        /**
         * CSIV2 level 0 implementation does not require any authorization tokens to be sent over the wire. So set cAuthzElem to
         * empty.
         */
        AuthorizationElement[] cAuthzElem = {};

        /* Client identity token to be added to the service context field */
        IdentityToken cIdentityToken = null;

        /* Client authentication token to be added to the service context field */
        byte[] cAuthenticationToken = {};

        /* CDR encoded Security Attribute Service element */
        byte[] cdr_encoded_saselm = null;

        java.lang.Object cred = null; // A single JAAS credential

        LOG.log(Level.FINE, "++++ Entered {0} send_request()", prname);
        SecurityContext secctxt = null; // SecurityContext to be sent
        ORB orb = orbHelper.getORB();
        org.omg.CORBA.Object effective_target = ri.effective_target();
        try {
            secctxt = secContextUtil.getSecurityContext(effective_target);
        } catch (InvalidMechanismException ime) {
            throw new RuntimeException(ime);
        } catch (InvalidIdentityTokenException iite) {
            throw new RuntimeException(iite);
        }

        /**
         * In an unprotected invocation, there is nothing to be sent to the service context field. Check for this case.
         */
        if (secctxt == null) {
            LOG.log(Level.FINE, "Security context is null (nothing to add to service context)");
            return;
        }

        final SecurityContext sCtx = secctxt;
        /* Construct an authentication token */
        if (secctxt.authcls != null) {
            PrivilegedAction<Object> action = () -> {
                Set<Object> credentials = sCtx.subject.getPrivateCredentials(sCtx.authcls);
                return getCred(credentials, sCtx.authcls);
            };
            cred = AccessController.doPrivileged(action);

            try {

                SecurityMechanismSelector sms = Lookups.getSecurityMechanismSelector();
                ConnectionContext cc = sms.getClientConnectionContext();
                CompoundSecMech mech = cc.getMechanism();

                cAuthenticationToken = createAuthToken(cred, secctxt.authcls, orb, mech);
            } catch (Exception e) {
                throw new SecurityException("Error while constructing an authentication token.");
            }
        }

        /* Construct an identity token */
        if (secctxt.identcls != null) {
            cred = getCred(secctxt.subject.getPublicCredentials(secctxt.identcls), secctxt.identcls);
            try {
                cIdentityToken = createIdToken(cred, secctxt.identcls, orb);
            } catch (Exception e) {
                throw new SecurityException("Error while constructing an identity token.");
            }
        } else {
            LOG.log(Level.FINE, "Constructing an Absent Identity Token");
            cIdentityToken = new IdentityToken();
            cIdentityToken.absent(true);
        }

        LOG.log(Level.FINE, "Creating an EstablishContext message");
        EstablishContext ec = new EstablishContext(cContextId, cAuthzElem, cIdentityToken, cAuthenticationToken);

        SASContextBody sasctxbody = new SASContextBody();
        sasctxbody.establish_msg(ec);

        /* CDR encode the SASContextBody */
        Any SasAny = orb.create_any();
        SASContextBodyHelper.insert(SasAny, sasctxbody);

        try {
            cdr_encoded_saselm = codec.encode_value(SasAny);
        } catch (Exception e) {
            throw new SecurityException("CDR Encoding error for a SAS context element.", e);
        }

        /* add SAS element to service context list */
        ServiceContext sc = new ServiceContext();
        sc.context_id = SECURITY_ATTRIBUTE_SERVICE_ID;
        sc.context_data = cdr_encoded_saselm;
        LOG.log(Level.FINE, "Adding EstablishContext message to service context list");
        boolean no_replace = false;
        ri.add_request_service_context(sc, no_replace);
        LOG.log(Level.FINE, "Added EstablishContext message to service context list");
    }

    @Override
    public void send_poll(ClientRequestInfo ri) {
    }

    /**
     * set the reply status
     */
    private void setreplyStatus(int status, org.omg.CORBA.Object target) {
        LOG.log(Level.FINE, "Status to be set: {0}", status);

        SecurityContextUtil.receivedReply(status, target);
        LOG.log(Level.FINE, "Invoked receivedReply()");
    }

    /**
     * Map the reply status code to a format suitable for J2EE RI.
     *
     * @param repst reply status from the service context field.
     * @return mapped status code
     *
     */
    private int mapreplyStatus(int repst) {
        int status;

        LOG.log(Level.FINE, "Reply status to be mapped = {0}", repst);

        switch (repst) {

        case SUCCESSFUL.value:
        case USER_EXCEPTION.value:
            status = SecurityContextUtil.STATUS_PASSED;
            break;

        case LOCATION_FORWARD.value:
        case TRANSPORT_RETRY.value:
            status = SecurityContextUtil.STATUS_RETRY;
            break;

        case SYSTEM_EXCEPTION.value:
            status = SecurityContextUtil.STATUS_FAILED;
            break;

        default:
            status = repst;
            /**
             * There is currently no mapping defined for any other status codes. So map this is to a STATUS_FAILED.
             */
            break;
        }
        LOG.log(Level.FINE, "Mapped reply status = {0}", status);
        return status;
    }

    private void handle_null_service_context(ClientRequestInfo ri) {
        LOG.log(Level.FINE, "No SAS context element found in service context list");
        setreplyStatus(SecurityContextUtil.STATUS_PASSED, ri.effective_target());
    }

    @Override
    public void receive_reply(ClientRequestInfo ri) {
        ServiceContext sc = null;
        int status = -1;

        LOG.log(Level.FINE, "Entered {0} receive_reply", prname);

        // get the service context element from the reply and decode the mesage.
        try {
            sc = ri.get_reply_service_context(SECURITY_ATTRIBUTE_SERVICE_ID);
            if (sc == null) {
                handle_null_service_context(ri);
                return;
            }
        } catch (org.omg.CORBA.BAD_PARAM e) {
            handle_null_service_context(ri);
            return;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Could not get the service context for id=" + SECURITY_ATTRIBUTE_SERVICE_ID, ex);
            return;
        }

        Any a;
        try {
            // decode the CDR encoding
            a = codec.decode_value(sc.context_data, SASContextBodyHelper.type());
        } catch (Exception e) {
            throw new SecurityException("CDR Decoding error for SAS context element.", e);
        }

        SASContextBody sasctxbody = SASContextBodyHelper.extract(a);
        short sasdiscr = sasctxbody.discriminator();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Received " + SvcContextUtils.getMsgname(sasdiscr) + " message");
        }

        // Verify that either a CompleteEstablishContext msg or an ContextError message was received.
        LOG.log(Level.FINE, "Verifying the SAS protocol reply message");

        // Check the discriminator value

        if (sasdiscr != MTCompleteEstablishContext.value && sasdiscr != MTContextError.value) {
            throw new SecurityException(
                "Reply message not one of CompleteEstablishContext or ContextError: " + sasdiscr);
        }

        /* Map the error code */
        int st = mapreplyStatus(ri.reply_status());

        setreplyStatus(st, ri.effective_target());
    }

    @Override
    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest {
        LOG.log(Level.FINE, "Entered {0} receive_exception", prname);
    }

    @Override
    public void receive_other(ClientRequestInfo ri) throws ForwardRequest {
    }

    @Override
    public void destroy() {
    }

    protected GlassFishORBHelper getORBHelper() {
        return this.orbHelper;
    }
}
