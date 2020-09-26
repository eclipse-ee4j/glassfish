/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package connector;

import org.glassfish.security.common.PrincipalImpl;

import jakarta.resource.spi.work.SecurityContext;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.Subject;
import jakarta.security.auth.message.callback.CallerPrincipalCallback;
import java.util.List;
import java.util.ArrayList;

/**
 * To check Unauthenticated SIC with a null principal or null name
 */
public class UnauthenticatedSecurityContext_NullPrincipalOrName extends SecurityContext {

    private String principalName;
    private boolean translationRequired;
    private boolean nullPrincipal;

    public UnauthenticatedSecurityContext_NullPrincipalOrName(boolean translationRequired, String principalName, boolean nullPrincipal) {
        this.translationRequired = translationRequired;
        this.principalName = principalName;
        this.nullPrincipal = nullPrincipal;
    }

    public void setupSecurityContext(CallbackHandler callbackHandler, Subject subject, Subject subject1) {
        try {
            List<Callback> callbacks = new ArrayList<Callback>();

            CallerPrincipalCallback cpc;
            if (nullPrincipal) {
                PrincipalImpl p = null;
                cpc = new CallerPrincipalCallback(new Subject(), p);
            } else {
                String name = null;
                cpc = new CallerPrincipalCallback(new Subject(), name);
            }
            callbacks.add(cpc);
            Callback callbackArray[] = new Callback[callbacks.size()];
            callbackHandler.handle(callbacks.toArray(callbackArray));
        } catch (Exception e) {
            debug(e.toString());
        }
    }

    public void debug(String message) {
        System.out.println("JSR-322 [RA] [UnauthenticatedSecurityContext_NullPrincipalOrName]: " + message);
    }

}
