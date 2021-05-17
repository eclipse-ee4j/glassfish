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

package com.sun.appserv.management.client.prefs;

import java.util.Arrays;

/**
 * An immutable class that represents an arbitrary LoginInfo for Appserver Administration Client. A LoginInfo
 * is specific to an admin host and admin port. Thus, with this scheme, there can be
 * at the most one LoginInfo for an operating system user of Appserver, for a given admin host
 * and admin port.
 * @since Appserver 9.0
 */
public final class LoginInfo implements Comparable<LoginInfo> {
    private String host;
    private int    port;
    private String user;
    private char[] password;

    /**
     * Creates an Immutable instance of a LoginInfo from given 4-tuple.
     * The host, user and password may not be null.
     * The port may not be a negative integer.
     * @param host String representing host
     * @param port integer representing port
     * @param user String representing user
     * @param password String representing password
     * @throws IllegalArgumentException if parameter contract is violated
     */
    public LoginInfo(final String host, final int port, final String user, final char[] password) {
        if (host == null || port < 0 || user == null || password == null)
            throw new IllegalArgumentException("null value"); // TODO
        init(host, port, user, password);
    }
    public String getHost() {
        return ( host );
    }
    public int getPort() {
        return ( port );
    }
    public String getUser() {
        return ( user );
    }
    public char[] getPassword() {
        return ( password );
    }
    public boolean equals(final Object other) {
        boolean same = false;
        if (other instanceof LoginInfo) {
            final LoginInfo that = (LoginInfo) other;
            same = this.host.equals(that.host) &&
                   this.port == that.port      &&
                   this.user.equals(that.user) &&
                    Arrays.equals(this.password,that.password);
        }
        return ( same );
    }
    public int hashCode() {
        return ( (int) 31 * host.hashCode() + 23 * port + 53 * user.hashCode() + 13 * Arrays.hashCode(password) );
    }
    private void init(final String host, final int port, final String user, final char[] password) {
        this.host     = host;
        this.port     = port;
        this.user     = user;
        this.password = password;
    }

    public String toString() {
        return ( host + port + user + (password != null ? new String(password) : null));
    }

    public int compareTo(final LoginInfo that) {
        final String thisKey = this.user + this.host + this.port;
        final String thatKey = that.user + that.host + that.port;
        return ( thisKey.compareTo(thatKey) );
    }
}
