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

/*
 * PESSOFactory.java
 *
 * Created on August 24, 2004, 5:11 PM
 */

package com.sun.enterprise.web;

import com.sun.enterprise.security.web.GlassFishSingleSignOn;

/**
 * @author lwhite
 */
public class PESSOFactory implements SSOFactory {

    /** Creates a new instance of PESSOFactory */
    public PESSOFactory() {
    }

    /**
     * Creates a SingleSignOn valve
     * @param virtualServerName
     */
    public GlassFishSingleSignOn createSingleSignOnValve(
            String virtualServerName) {
        return new GlassFishSingleSignOn();
    }

}
