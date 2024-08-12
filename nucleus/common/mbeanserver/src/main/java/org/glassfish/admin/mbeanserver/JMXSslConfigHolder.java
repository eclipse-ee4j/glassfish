/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.mbeanserver;

import javax.net.ssl.SSLException;

import org.glassfish.grizzly.config.SSLConfigurator;
import org.glassfish.grizzly.config.dom.Ssl;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * This class extends the SSLConfigHolder for the sole purpose of
 * overriding the logEmptyWarning method, since this method in the super class
 * assumes that there is a network-config element assocaited with each ssl element
 * in the domain.xml. In case of JMX , the parent element is the jmx-connector
 * element.
 * @author prasad
 */

public class JMXSslConfigHolder extends SSLConfigurator {

    private static final String DEFAULT_SSL_PROTOCOL = "TLS";

    public JMXSslConfigHolder(final ServiceLocator habitat, final Ssl ssl) throws SSLException {
                super(habitat, ssl);
    }

    @Override
    protected void logEmptyWarning(Ssl ssl, final String msg) {
          // your log
    }


    void configureClientSSL() {

    }

}
