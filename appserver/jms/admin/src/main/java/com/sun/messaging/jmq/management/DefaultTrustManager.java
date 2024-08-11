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

/*
 * @(#)DefaultTrustManager.java    1.4 06/29/07
 */

package com.sun.messaging.jmq.management;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/*
 *  IMPORTANT NOTE: Please do not modify this file directly. This source code is owned and shipped as a part of MQ but has only been included here
 *  since it is required for certain JMX operations especially when MQ is running in the HA mode. Please refer to GF issue 13602 for more details.
 */

/**
 * The default trust manager.
 *
 * TBD: Need to describe *when* this class is used i.e. what JMX configuration
 * properties trigger it's use.
 *
 * <p>If this class is used, the client does not require to install/configure
 * server certificates because all server certs are accepted.
 *
 * <p>This is useful for intra-net applications where servers are inside
 * firewall and are treated as trusted.
 */
public class DefaultTrustManager implements X509TrustManager {
    private boolean debug = false;

    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) {
        if ( debug ) {
            System.err.println("default trust manager is called to validate certs ...");
            System.err.println("returning 'true' for isServerTrusted call ...");
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
