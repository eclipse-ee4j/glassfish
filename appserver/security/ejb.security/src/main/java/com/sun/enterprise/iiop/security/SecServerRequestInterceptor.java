/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.iiop.security;

import static com.sun.logging.LogDomains.SECURITY_LOGGER;

import com.sun.corba.ee.org.omg.CSI.CompleteEstablishContext;
import com.sun.corba.ee.org.omg.CSI.ContextError;
import com.sun.corba.ee.org.omg.CSI.EstablishContext;
import com.sun.corba.ee.org.omg.CSI.GSS_NT_ExportedNameHelper;
import com.sun.corba.ee.org.omg.CSI.ITTAbsent;
import com.sun.corba.ee.org.omg.CSI.ITTAnonymous;
import com.sun.corba.ee.org.omg.CSI.ITTDistinguishedName;
import com.sun.corba.ee.org.omg.CSI.ITTPrincipalName;
import com.sun.corba.ee.org.omg.CSI.ITTX509CertChain;
import com.sun.corba.ee.org.omg.CSI.IdentityToken;
import com.sun.corba.ee.org.omg.CSI.MTEstablishContext;
import com.sun.corba.ee.org.omg.CSI.MTMessageInContext;
import com.sun.corba.ee.org.omg.CSI.SASContextBody;
import com.sun.corba.ee.org.omg.CSI.SASContextBodyHelper;
import com.sun.corba.ee.org.omg.CSI.X501DistinguishedNameHelper;
import com.sun.corba.ee.org.omg.CSI.X509CertificateChainHelper;
import com.sun.corba.ee.spi.legacy.connection.Connection;
import com.sun.corba.ee.spi.legacy.interceptor.RequestInfoExt;
import com.sun.enterprise.common.iiop.security.AnonCredential;
import com.sun.enterprise.common.iiop.security.GSSUPName;
import com.sun.enterprise.common.iiop.security.SecurityContext;
import com.sun.enterprise.security.auth.login.common.PasswordCredential;
import com.sun.enterprise.security.auth.login.common.X509CertificateCredential;
import com.sun.logging.LogDomains;
import java.io.ByteArrayInputStream;
import java.net.Socket;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;
import org.glassfish.enterprise.iiop.api.GlassFishORBHelper;
import org.omg.CORBA.Any;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.ORB;
import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

/**
 * Security server request interceptor
 * <p>
 * This class is a server side request interceptor for CSIV2.
 * It is used to send and receive the service context in a
 * a service context element in the service context list in
 * an IIOP header.
 *
 * @author: Nithya Subramanian
 */
public class SecServerRequestInterceptor extends org.omg.CORBA.LocalObject implements ServerRequestInterceptor {

    private static final Logger LOG = LogDomains.getLogger(SecServerRequestInterceptor.class, SECURITY_LOGGER);

    private final InheritableThreadLocal counterForCalls = new InheritableThreadLocal();

    /**
     * Hard code the value of 15 for SecurityAttributeService until it is defined in IOP.idl. sc.context_id =
     * SecurityAttributeService.value;
     */
    protected static final int SECURITY_ATTRIBUTE_SERVICE_ID = 15;
    // the major and minor codes for a invalid mechanism
    private static final int INVALID_MECHANISM_MAJOR = 2;
    private static final int INVALID_MECHANISM_MINOR = 1;

    public static final String SERVER_CONNECTION_CONTEXT = "ServerConnContext";

    /** used when inserting into service context field */
    private static final boolean NO_REPLACE = false;

    /** name of interceptor used for logging purposes (name + "::") */
    private final String prname;
    private final String name;
    private final Codec codec;
    private final SecurityContextUtil secContextUtil;
    private final GlassFishORBHelper orbHelper;
    private final SecurityMechanismSelector smSelector;

    // Not required
    // SecurityService secsvc = null; // Security Service
    public SecServerRequestInterceptor(String name, Codec codec) {
        this.name = name;
        this.codec = codec;
        this.prname = name + "::";
        secContextUtil = Lookups.getSecurityContextUtil();
        orbHelper = Lookups.getGlassFishORBHelper();
        smSelector = Lookups.getSecurityMechanismSelector();
    }

    @Override
    public String name() {
        return name;
    }

    /**
     * Create a ContextError message. This is currently designed to work only for the GSSUP mechanism.
     */
    private SASContextBody createContextError(int status) {
        /**
         * CSIV2 SPEC NOTE:
         *
         * Check that CSIV2 spec does not require an error token to be sent for the GSSUP mechanism.
         */

        return createContextError(1, status);
    }

    /**
     * create a context error with the specified major and minor status
     */
    private SASContextBody createContextError(int major, int minor) {

        LOG.log(Level.FINE, "Creating ContextError message: major code: {0}, minor code: {1} ",
            new Object[] {major, minor});
        byte error_token[] = {};
        ContextError ce = new ContextError(0, /* stateless client id */
                major, // major
                minor, // minor
                error_token);
        SASContextBody sasctxtbody = new SASContextBody();
        sasctxtbody.error_msg(ce);
        return sasctxtbody;
    }

    /**
     * Create a CompleteEstablishContext Message. This currently works only for the GSSUP mechanism.
     */
    private SASContextBody createCompleteEstablishContext(int status) {
        /**
         * CSIV2 SPEC NOTE:
         *
         * Check CSIV2 spec to make sure that there is no final_context_token for GSSUP mechanism
         */
        LOG.log(Level.FINE, "Creating CompleteEstablishContext message");
        byte[] final_context_token = {};
        CompleteEstablishContext cec = new CompleteEstablishContext(0, // stateless client id
                false, // for stateless
                final_context_token);
        SASContextBody sasctxtbody = new SASContextBody();
        sasctxtbody.complete_msg(cec);
        return sasctxtbody;
    }

    /**
     * CDR encode a SAS Context body and then construct a service context element.
     */
    private ServiceContext createSvcContext(SASContextBody sasctxtbody, ORB orb) {

        ServiceContext sc = null;

        Any a = orb.create_any();
        SASContextBodyHelper.insert(a, sasctxtbody);

        byte[] cdr_encoded_saselm = {};
        try {
            cdr_encoded_saselm = codec.encode_value(a);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Could not encode Any object.", e);
        }
        sc = new ServiceContext();
        sc.context_id = SECURITY_ATTRIBUTE_SERVICE_ID;
        sc.context_data = cdr_encoded_saselm;
        return sc;

    }

    /**
     * Create an identity from an Identity Token and stores it as a public credential in the JAAS subject in a security
     * context.
     *
     * Set the identcls field in the security context.
     *
     */
    private void createIdCred(SecurityContext sc, IdentityToken idtok) throws Exception {

        byte[] derenc; // used to hold DER encodings
        Any any; // Any object returned from codec.decode_value()

        switch (idtok.discriminator()) {

            case ITTAbsent.value:
                LOG.log(Level.FINE, "Identity token type is Absent");
                sc.identcls = null;
                break;

            case ITTAnonymous.value:
                LOG.log(Level.FINE, "Identity token type is Anonymous");
                LOG.log(Level.FINE, "Adding AnonyCredential to subject's PublicCredentials");
                sc.subject.getPublicCredentials().add(new AnonCredential());
                sc.identcls = AnonCredential.class;
                break;

            case ITTDistinguishedName.value:
                /* Construct a X500Principal */

                derenc = idtok.dn();
                /* Issue 5766: Decode CDR encoding if necessary */
                if (isCDR(derenc)) {
                    any = codec.decode_value(derenc, X501DistinguishedNameHelper.type());

                    /* Extract CDR encoding */
                    derenc = X501DistinguishedNameHelper.extract(any);
                }
                LOG.log(Level.FINE, "Create an X500Principal object from identity token");
                X500Principal xname = new X500Principal(derenc);
                LOG.log(Level.FINE, "Identity to be asserted is {0}", xname);
                LOG.log(Level.FINE, "Adding X500Principal to subject's PublicCredentials");
                sc.subject.getPublicCredentials().add(xname);
                sc.identcls = X500Principal.class;
                break;

            case ITTX509CertChain.value:
                /* Construct a X509CertificateChain */
                LOG.log(Level.FINE, "Identity token type is a X509 Certificate Chain");
                derenc = idtok.certificate_chain();
                /* Issue 5766: Decode CDR encoding if necessary */
                if (isCDR(derenc)) {
                    /* Decode CDR encoding */
                    any = codec.decode_value(derenc, X509CertificateChainHelper.type());

                    /* Extract DER encoding */
                    derenc = X509CertificateChainHelper.extract(any);
                }

                List<? extends Certificate> certificates = CertificateFactory.getInstance("X.509")
                    .generateCertPath(new ByteArrayInputStream(derenc))
                    .getCertificates();

                X509Certificate[] certchain = new X509Certificate[certificates.size()];

                LOG.log(Level.FINE, "Contents of X509 Certificate chain:");
                for (int i = 0; i < certchain.length; i++) {
                    X509Certificate certificate = (X509Certificate) certificates.get(i);
                    certchain[i] = certificate;
                    LOG.log(Level.FINE, "Added certificate to the chain: {0}",
                        certificate.getSubjectX500Principal().getName());
                }
                LOG.log(Level.FINE, "Creating a X509CertificateCredential object from certchain");
                // The alias field in the X509CertificateCredential is currently ignored by the RI. So it is set to "dummy".
                X509CertificateCredential cred = new X509CertificateCredential(certchain, certchain[0].getSubjectX500Principal().getName(), "default");
                LOG.log(Level.FINE, "Adding X509CertificateCredential to subject's PublicCredentials");
                sc.subject.getPublicCredentials().add(cred);
                sc.identcls = X509CertificateCredential.class;
                break;

            case ITTPrincipalName.value:
                LOG.log(Level.FINE, "Identity token type is GSS Exported Name");
                byte[] expname = idtok.principal_name();
                /* Issue 5766: Decode CDR encoding if necessary */
                if (isCDR(expname)) {
                    /* Decode CDR encoding */
                    any = codec.decode_value(expname, GSS_NT_ExportedNameHelper.type());

                    expname = GSS_NT_ExportedNameHelper.extract(any);
                }
                if (!GSSUtils.verifyMechOID(GSSUtils.GSSUP_MECH_OID, expname)) {
                    throw new SecurityException("Unknown identity assertion type: " + expname);
                }

                GSSUPName gssname = new GSSUPName(expname);

                sc.subject.getPublicCredentials().add(gssname);
                sc.identcls = GSSUPName.class;
                LOG.log(Level.FINE, "Adding GSSUPName credential to subject");
                break;

            default:
                throw new SecurityException("Unknown identity assertion type: " + idtok.discriminator());
        }
    }

    /**
     * Check if given byte is CDR encapsulated.
     *
     * @param bytes an input array of byte
     * @return boolean indicates whether input is CDR
     */
    private boolean isCDR(byte[] bytes) {
        return (bytes != null && bytes.length > 0 && (bytes[0] == 0x0 || bytes[0] == 0x1));
    }

    /**
     * Create an auth credential from authentication token and store it as a private credential in the JAAS subject in the
     * security context.
     *
     * Set the authcls field in the security context.
     *
     * This method currently only works for PasswordCredential tokens.
     */
    private void createAuthCred(SecurityContext securityContext, byte[] authtok, ORB orb) throws Exception {
        LOG.log(Level.FINE, "Constructing a PasswordCredential from client authentication token");

        /* create a GSSUPToken from the authentication token */
        GSSUPToken tok = GSSUPToken.getServerSideInstance(orb, codec, authtok);

        final PasswordCredential pwdcred = tok.getPwdcred();
        LOG.log(Level.FINE, "Password credential = {0}", pwdcred);
        LOG.log(Level.FINE, "Adding PasswordCredential to subject's PrivateCredentials");

        securityContext.subject.getPrivateCredentials().add(pwdcred);
        securityContext.authcls = PasswordCredential.class;
    }

    private void handle_null_service_context(ServerRequestInfo ri, ORB orb) {
        LOG.log(Level.FINE, "No SAS context element found in service context list for operation: {0}", ri.operation());
        ServiceContext sc = null;
        int secStatus = secContextUtil.setSecurityContext(null, ri.object_id(), ri.operation(), getServerSocket());

        if (secStatus == SecurityContextUtil.STATUS_FAILED) {
            SASContextBody sasctxbody = createContextError(INVALID_MECHANISM_MAJOR, INVALID_MECHANISM_MINOR);
            sc = createSvcContext(sasctxbody, orb);
            ri.add_reply_service_context(sc, NO_REPLACE);
            LOG.log(Level.FINE, "SecServerRequestInterceptor.receive_request: NO_PERMISSION");
            throw new NO_PERMISSION();
        }
    }

    @Override
    public void receive_request(ServerRequestInfo ri) throws ForwardRequest {
        SecurityContext seccontext = null; // SecurityContext to be sent
        ServiceContext sc = null; // service context
        int status = 0;
        boolean raise_no_perm = false;

        LOG.log(Level.FINE, "Entered {0} receive_request", prname);

        // secsvc = Csiv2Manager.getSecurityService();
        ORB orb = orbHelper.getORB();

        try {
            sc = ri.get_request_service_context(SECURITY_ATTRIBUTE_SERVICE_ID);
            if (sc == null) {
                handle_null_service_context(ri, orb);
                return;
            }
        } catch (org.omg.CORBA.BAD_PARAM e) {
            handle_null_service_context(ri, orb);
            return;
        }

        LOG.log(Level.FINE, "Received a non null SAS context element");
        /* Decode the service context field */
        Any SasAny;
        try {
            SasAny = codec.decode_value(sc.context_data, SASContextBodyHelper.type());
        } catch (Exception e) {
            throw new SecurityException("CDR Decoding error for SAS context element.", e);
        }

        LOG.log(Level.FINE, "Successfully decoded CDR encoded SAS context element.");
        SASContextBody sasctxbody = SASContextBodyHelper.extract(SasAny);

        short sasdiscr = sasctxbody.discriminator();
        LOG.log(Level.FINE, "SAS context element is a/an {0} message", SvcContextUtils.getMsgname(sasdiscr));

        /* Check message type received */

        /**
         * CSIV2 SPEC NOTE:
         *
         * Section 4.3 "TSS State Machine" , table 4-4 "TSS State Table" shows that a MessageInContext can be received. In this
         * case the table is somewhat unclear. But in this case a ContextError with the status code "No Context" ( specified in
         * section 4.5 "ContextError Values and Exceptions" must be sent back. A NO_PERMISSION exception must also be raised.
         *
         * ISSUE: should setSecurityContext(null) be called ?
         */

        if (sasdiscr == MTMessageInContext.value) {
            sasctxbody = createContextError(SvcContextUtils.MessageInContextMinor);
            sc = createSvcContext(sasctxbody, orb);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Adding ContextError message to service context list");
                LOG.log(Level.FINE, "SecurityContext set to null");
            }
            ri.add_reply_service_context(sc, NO_REPLACE);
            // no need to set the security context
//              secsvc.setSecurityContext(null, ri.object_id(), ri.operation());

            throw new NO_PERMISSION();
        }

        /**
         * CSIV2 SPEC NOTE:
         *
         * CSIV2 spec does not specify the actions for any message other than a MessageInContext and EstablishContext message.So
         * for such messages, this implementation simply drops the message on the floor. No other message is sent back. Neither
         * is an exception raised.
         *
         * ISSUE: Should there be some other action ?
         */

        if (sasdiscr != MTEstablishContext.value) {
            throw new SecurityException("Received message not an EstablishContext message, but: " + sasdiscr);
        }

        EstablishContext ec = sasctxbody.establish_msg();

        seccontext = new SecurityContext();
        seccontext.subject = new Subject();

        try {
            if (ec.client_authentication_token.length != 0) {
                LOG.log(Level.FINE, "Message contains Client Authentication Token");
                createAuthCred(seccontext, ec.client_authentication_token, orb);
            }
        } catch (Exception e) {
            throw new SecurityException("Error while creating a JAAS subject credential.", e);
        }

        try {
            if (ec.identity_token != null) {
                LOG.log(Level.FINE, "Message contains an Identity Token");
                createIdCred(seccontext, ec.identity_token);
            }
        } catch (SecurityException secex) {
            LOG.log(Level.SEVERE, "Could not create an identity for an identity token.", secex);
            sasctxbody = createContextError(INVALID_MECHANISM_MAJOR, INVALID_MECHANISM_MINOR);
            sc = createSvcContext(sasctxbody, orb);
            ri.add_reply_service_context(sc, NO_REPLACE);
            throw new NO_PERMISSION();
        } catch (Exception e) {
            throw new SecurityException("Error while creating a JAAS subject credential.", e);

        }

        LOG.log(Level.FINE, "Invoking setSecurityContext() to set security context");
        status = secContextUtil.setSecurityContext(seccontext, ri.object_id(), ri.operation(), getServerSocket());
        LOG.log(Level.FINE, "setSecurityContext() returned status code {0}", status);

        /**
         * CSIV2 SPEC NOTE:
         *
         * If ec.client_context_id is non zero, then this is a stateful request. As specified in section 4.2.1, a stateless
         * server must attempt to validate the security tokens in the security context field. If validation succeeds then
         * CompleteEstablishContext message is sent back. If validation fails, a ContextError must be sent back.
         */
        if (status == SecurityContextUtil.STATUS_FAILED) {
            LOG.log(Level.FINE, "setSecurityContext() returned STATUS_FAILED");
            sasctxbody = createContextError(status);
            sc = createSvcContext(sasctxbody, orb);
            LOG.log(Level.FINE, "Adding ContextError message to service context list");
            ri.add_reply_service_context(sc, NO_REPLACE);
            throw new NO_PERMISSION();
        }

        LOG.log(Level.FINE, "setSecurityContext() returned SUCCESS");
        sasctxbody = createCompleteEstablishContext(status);
        sc = createSvcContext(sasctxbody, orb);
        LOG.log(Level.FINE, "Adding CompleteEstablisContext message to service context list");
        ri.add_reply_service_context(sc, NO_REPLACE);
    }

    /*
     * This method is keeping a track of when to unset the security context Currently with the re-use of the threads made by
     * the orb the security context does not get unset. This method determines when to unset the security context
     */
    @Override
    public void receive_request_service_contexts(ServerRequestInfo ri) throws ForwardRequest {
        // cannot set this in receive_request due to the PI flow control
        // semantics. e.g. if receive_req for some other PI throws an
        // exception - the send_exception will be called that will muck
        // the stack up
        Counter cntr = (Counter) counterForCalls.get();
        if (cntr == null) {
            cntr = new Counter();
            counterForCalls.set(cntr);
        }
        if (cntr.count == 0) {
            // Not required
            // SecurityService secsvc = Csiv2Manager.getSecurityService();
            SecurityContextUtil.unsetSecurityContext(isLocal());
        }
        cntr.increment();

        Socket s = null;
        Connection c = null;
        if (ri instanceof RequestInfoExt) {
            c = ((RequestInfoExt) ri).connection();
        }
        ServerConnectionContext scc = null;
        if (c != null) {
            s = c.getSocket();
            LOG.log(Level.FINE, "RECEIVED request on connection: {0}", c);
            LOG.log(Level.FINE, "Socket = {0}", s);
            scc = new ServerConnectionContext(s);
        } else {
            scc = new ServerConnectionContext();
        }
        setServerConnectionContext(scc);
    }

    @Override
    public void send_reply(ServerRequestInfo ri) {
        unsetSecurityContext();
    }

    @Override
    public void send_exception(ServerRequestInfo ri) throws ForwardRequest {
        unsetSecurityContext();
    }

    @Override
    public void send_other(ServerRequestInfo ri) throws ForwardRequest {
        unsetSecurityContext();
    }

    @Override
    public void destroy() {
    }

    private void unsetSecurityContext() {
        try {
            Counter cntr = (Counter) counterForCalls.get();
            // sanity check
            if (cntr == null) {
                cntr = new Counter(1);
            }
            cntr.decrement();
            if (cntr.count == 0) {
                SecurityContextUtil.unsetSecurityContext(isLocal());
            }
        } finally {
            ConnectionExecutionContext.removeClientThreadID();
        }
    }

    private boolean isLocal() {
        boolean local = true;
        ServerConnectionContext scc = getServerConnectionContext();
        if (scc != null && scc.getSocket() != null) {
            local = false;
        }
        Long clientID = ConnectionExecutionContext.readClientThreadID();
        if (clientID != null && clientID == Thread.currentThread().getId()) {
            local = true;
        }
        return local;
    }

    private Socket getServerSocket() {
        ServerConnectionContext scc = getServerConnectionContext();
        if (scc != null) {
            return scc.getSocket();
        }
        return null;
    }

    private ServerConnectionContext getServerConnectionContext() {
        Hashtable h = ConnectionExecutionContext.getContext();
        ServerConnectionContext scc = (ServerConnectionContext) h.get(SERVER_CONNECTION_CONTEXT);
        return scc;
    }

    public static void setServerConnectionContext(ServerConnectionContext scc) {
        Hashtable h = ConnectionExecutionContext.getContext();
        h.put(SERVER_CONNECTION_CONTEXT, scc);
    }
}

class Counter {

    public int count;

    public Counter(int count) {
        this.count = count;
    }

    public Counter() {
        count = 0;
    }

    public void setCount(int counter) {
        count = counter;
    }

    public void increment() {
        count++;
    }

    public void decrement() {
        count--;
    }

    public String display() {
        return " Counter = " + count;
    }
}
