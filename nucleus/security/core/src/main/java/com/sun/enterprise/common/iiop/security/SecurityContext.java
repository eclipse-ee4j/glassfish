/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

/*
 * @(#)SecurityContext.java        1.5 00/10/24
 */

package com.sun.enterprise.common.iiop.security;

import javax.security.auth.Subject;

/*
 * This interface is part of the contract between CSIV2 interceptors
 * and the rest of J2EE RI.
 *
 * @author  Nithya Subramanian
 */

/**
 * A subject is used a container for passing the security context information in the service context field. The security context
 * information in the subject must be stored either as a private or a public credential according to the following convention:
 *
 * PasswordCredential: Client authentication will be performed using the username and password in the PasswordCredential.
 * PasswordCredential must be passed as a PrivateCredential.
 *
 * X500Principal:: DN name specified in X500Principal: will be asserted. X500Principal must be passed as a PublicCredential.
 *
 * GSSUPName: Identity specified in GSSUPName will be asserted. GSSUPName must be passed as a PublicCredential.
 *
 * X509CertificateCredential: The certificate chain in the credential will be asserted. The credential must be passed as a
 * PublicCredential.
 *
 * AnonCredential: Anonymous identity will be asserted. Credential must be passed as a PublicCredential.
 *
 * Class fields in the SecurityContext are used for credential selection. There are two class fields: authcls and identcls.
 *
 * authcls is a Class object that identifies the credential for client authentication.
 *
 * identcls is a Class object that identifies the credential for identity assertion.
 *
 * The following semantics must be observed:
 *
 * 1. A client authentication token is always passed as a private credential. authcls set to the class of the authentication
 * token
 *
 * 2. An identity token is always passed as a public credential. identcls is set to the class of the identity token.
 *
 * 3. authcls is set to null if there is no client auth token
 *
 * 4. identcls is set to null if there is no ident token
 *
 * 5. There must not be more than one instance of class identified by authcls or identcls. However, there can be one instance of
 * identcls *and* authcls (this allows both a client auth token and an identity token to be passed across the interface).
 */

public class SecurityContext {
    public Subject subject;
    public Class<?> authcls;
    public Class<?> identcls;
}
