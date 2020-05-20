/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package test.com.sun.jaspic.config;

import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.AuthStatus;
import jakarta.security.auth.message.MessageInfo;
import jakarta.security.auth.message.MessagePolicy;
import jakarta.security.auth.message.module.ClientAuthModule;

/**
 *
 * @author Ron Monzillo
 */
public class SampleClientAuthModule implements ClientAuthModule {

    public void initialize(MessagePolicy mp, MessagePolicy mp1, CallbackHandler ch, Map map) throws AuthException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Class[] getSupportedMessageTypes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AuthStatus secureRequest(MessageInfo mi, Subject sbjct) throws AuthException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AuthStatus validateResponse(MessageInfo mi, Subject sbjct, Subject sbjct1) throws AuthException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void cleanSubject(MessageInfo mi, Subject sbjct) throws AuthException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
