/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.cluster.ssh.launcher;

import com.jcraft.jsch.UserInfo;

import java.lang.System.Logger;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * This object can be used to fake user input for Jsch.
 */
class GlassFishSshUserInfo implements UserInfo {

    private static final Logger LOG = System.getLogger(GlassFishSshUserInfo.class.getName());
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public String getPassphrase() {
        counter.incrementAndGet();
        LOG.log(DEBUG, "getPassphrase(); counter: " + counter);
        return null;
    }

    @Override
    public String getPassword() {
        LOG.log(DEBUG, "getPassword()");
        return null;
    }

    @Override
    public boolean promptPassword(String message) {
        LOG.log(DEBUG, "promptPassword(message={0})", message);
        return false;
    }

    @Override
    public boolean promptPassphrase(String message) {
        LOG.log(DEBUG, "promptPassphrase(message={0})", message);
        return true;
    }

    @Override
    public boolean promptYesNo(String message) {
        LOG.log(DEBUG, "promptYesNo(message={0})", message);
        return true;
    }

    @Override
    public void showMessage(String message) {
        LOG.log(DEBUG, "showMessage(message={0})", message);
    }

}
