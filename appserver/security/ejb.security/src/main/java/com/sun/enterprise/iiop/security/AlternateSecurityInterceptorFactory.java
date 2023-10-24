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

import org.omg.IOP.Codec;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

/**
 * Interface to override existing security RequestInterceptors.
 *
 * @author Sudarsan Sridhar
 */
public interface AlternateSecurityInterceptorFactory {
    String SEC_INTEROP_INTFACTORY_PROP = "com.sun.enterprise.iiop.security.interceptorFactory";

    ClientRequestInterceptor getClientRequestInterceptor(Codec codec);

    ServerRequestInterceptor getServerRequestInterceptor(Codec codec);
}
