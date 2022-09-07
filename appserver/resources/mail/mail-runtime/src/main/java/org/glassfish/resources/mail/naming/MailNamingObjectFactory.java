/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resources.mail.naming;

import com.sun.enterprise.deployment.MailConfiguration;
import com.sun.logging.LogDomains;

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.glassfish.resources.mail.MailLogOutputStream;
import org.glassfish.resources.mail.MailSessionAuthenticator;


public class MailNamingObjectFactory implements ObjectFactory {

    private static final Logger LOG = LogDomains.getLogger(MailNamingObjectFactory.class, LogDomains.JNDI_LOGGER, false);

    public MailNamingObjectFactory() {
    }


    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
        throws Exception {
        Reference ref = (Reference) obj;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "MailNamingObjectFactory: " + ref + " Name:" + name);
        }
        MailConfiguration config = (MailConfiguration) ref.get(0).getContent();

        // Note: jakarta.mail.Session is not serializable,
        // but we need to get a new instance on every lookup.
        Properties props = config.getMailProperties();
        jakarta.mail.Session s = jakarta.mail.Session.getInstance(props, new MailSessionAuthenticator(props));
        if ("smtps".equals(props.getProperty("mail.transport.protocol"))) {
            s.setProtocolForAddress("rfc822", "smtps");
        }
        s.setDebugOut(new PrintStream(new MailLogOutputStream()));
        s.setDebug(true);

        return s;
    }
}
