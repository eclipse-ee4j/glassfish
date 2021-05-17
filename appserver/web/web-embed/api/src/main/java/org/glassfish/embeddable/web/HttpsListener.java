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

package org.glassfish.embeddable.web;

import org.glassfish.embeddable.web.config.SslConfig;

/**
 * HTTPS Listener which listens on a protocol that is secured.
 * SSL configuration element will be used to initialize security settings.
 *
 * @author Rajiv Mordani
 * @author Amy Roh
 *
 * @see SslConfig
 */
public class HttpsListener extends WebListenerBase  {

    private SslConfig sslConfig;

    /**
     * Initializes a newly created HttpsListener object with HTTPS protocol.
     */
    public HttpsListener() {
        super();
        this.setProtocol("https");
    }


    public HttpsListener(String id, int port) {
        super(id, port);
        this.setProtocol("https");
    }

    /**
     * Sets the SSL configuration for this web listener
     */
    public void setSslConfig(SslConfig sslConfig) {
        this.sslConfig = sslConfig;
    }

    /**
     * Gets the SslConfig for this web listener
     */
    public SslConfig getSslConfig() {
        return sslConfig;
    }


}
