/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.common.util.admin;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;

import org.jvnet.hk2.annotations.Contract;

/**
 * Prescribes behavior of authentication callbacks which modules can implement
 * to provide callback behavior without the authentication logic needing to
 * know about specific implementations ahead of time.
 *
 * @author tjquinn
 */
@Contract
public interface AdminAuthCallback extends Callback {
    /**
     * Prescribes behavior of callbacks that use request-time data in making
     * their decisions.
     * <p>
     * Token-based authentication, for example, uses a token conveyed with the
     * request as a stand-in for username/password-based authentication.  To
     * keep inter-module dependencies simpler an implementation will probably
     * check the type of the data and then cast it if appropriate.
     */
    public interface RequestAccepter extends AdminAuthCallback {
        public void setRequest(Object data);
    }

    public interface Validator extends AdminAuthCallback {
        public boolean isValid();
    }

    public interface Authenticator extends AdminAuthCallback {
        public Subject getSubject();
    }
}
