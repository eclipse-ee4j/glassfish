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

package com.sun.enterprise.tools.verifier;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import org.glassfish.internal.api.Globals;
import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Service
public class VerifierModuleStartup implements ModuleStartup {

    @Inject
    private ModulesRegistry mr;

    // force initialization of Globals, as many appserver modules
    // use Globals.
    @Inject
    Globals globals;

    private StartupContext startupContext;
    private ClassLoader oldCL;

    public void setStartupContext(StartupContext context) {
        this.startupContext = context;
    }

    public void start() {
        setTCL();
    }

    public void stop() {
        unsetTCL();
    }

    private void setTCL() {
        oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(null);
    }

    private void unsetTCL() {
        Thread.currentThread().setContextClassLoader(oldCL);
    }
}
