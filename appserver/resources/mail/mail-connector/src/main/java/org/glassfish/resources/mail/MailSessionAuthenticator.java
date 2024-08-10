/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resources.mail;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

import java.util.Properties;

/**
 *
 * @author peterw99
 */
public class MailSessionAuthenticator extends Authenticator {

    private final Properties props;

    public MailSessionAuthenticator(Properties props) {
        this.props = props;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        PasswordAuthentication authenticator = null;
        String protocol = getRequestingProtocol();
        if(protocol != null) {
            String password = props.getProperty("mail." + protocol + ".password");
            String username = getDefaultUserName();
            if(password != null && username != null) {
                authenticator = new PasswordAuthentication(username, password);
            }
        }
        return authenticator;
    }

}
