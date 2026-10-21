/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.tests.embedded.jms;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.Destination;
import jakarta.jms.JMSContext;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class HelloSender {

    private static final Logger LOGGER = Logger.getLogger(HelloSender.class.getName());

    @Inject
    JMSContext context;

    @Resource(lookup = "java:comp/jms/HelloQueue")
    private Destination helloQueue;

    public void sayHelloFromJms() {
        String msg = "Hello JMS at " + Instant.now();
        LOGGER.log(Level.INFO, "sending message from HelloSender: {0}", msg);
        context.createProducer().send(helloQueue, msg);
    }
}
