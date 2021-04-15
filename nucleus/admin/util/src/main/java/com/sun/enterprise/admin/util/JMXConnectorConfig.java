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

package com.sun.enterprise.admin.util;

public class JMXConnectorConfig {

    private String host = null;
    private String port = null;
    private String user = null;
    private char[] password = null;
    private String protocol = null;

    public JMXConnectorConfig() {
    }

    public JMXConnectorConfig(String host, String port, String user, char[] password, String protocol) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public char[] getPassword() {
        return password;
    }

    public String getProtocol() {
        return protocol;
    }
}
