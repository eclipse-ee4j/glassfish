/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

/*
 * ContainerCallbackHandler
 *
 * Created on April 21, 2004, 11:56 AM
 */

package com.sun.enterprise.security.ee.authentication.jakarta.callback;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.security.SecurityServicesUtil;

/**
 * @author Shing Wai Chan
 */
@Service
@ContractsProvided({ ContainerCallbackHandler.class, CallbackHandler.class })
public final class ContainerCallbackHandler implements CallbackHandler {
    private CallbackHandler handler = null;

    public ContainerCallbackHandler() {
        if (Globals.getDefaultHabitat() == null || SecurityServicesUtil.getInstance().isACC()) {
            handler = new ClientContainerCallbackHandler();
        } else {
            handler = new ServerContainerCallbackHandler();
        }
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        handler.handle(callbacks);
    }

}
