/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.main.test.app.security.jmac.http.soap.embedded;

import jakarta.jws.WebService;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.glassfish.main.test.app.hello.servlet.HelloServletPort;

@WebService(
    portName="HelloServletPort",
    targetNamespace = "urn:org:glassfish:main:test:app:hello:servlet",
    endpointInterface = "org.glassfish.main.test.app.hello.servlet.HelloServletPort",
    wsdlLocation = "WEB-INF/wsdl/hello-servlet.wsdl")
public class HelloServlet implements HelloServletPort {
    private static final Logger LOG = System.getLogger(HelloServlet.class.getName());

    @Override
    public String hello(String who) {
        String message = "HelloServlet " + who;
        LOG.log(Level.INFO, "Responding: {0}", message);
        return message;
    }
}
