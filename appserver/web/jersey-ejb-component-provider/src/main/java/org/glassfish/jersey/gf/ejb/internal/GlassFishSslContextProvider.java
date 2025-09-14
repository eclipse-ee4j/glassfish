/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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


package org.glassfish.jersey.gf.ejb.internal;

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.client.spi.DefaultSslContextProvider;

/**
 * Default SSL context provider for REST clients used from GlassFish.
 */
public class GlassFishSslContextProvider implements DefaultSslContextProvider {

    @Override
    public SSLContext getDefaultSslContext() {
        try {
            // See SSLUtils.getSSLContext() - it sets the default SSLContext.
            return SSLContext.getDefault();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to obtain default SSLContext!", e);
        }
    }
}
