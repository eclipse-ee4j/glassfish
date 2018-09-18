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

package org.glassfish.webservices.monitoring;

import com.sun.enterprise.deployment.BundleDescriptor;

import java.security.Principal;


/**
 * This interface allows to register interest in authentication events
 * in the web service container.
 *
 * @author Jerome Dochez
 */
public interface AuthenticationListener {

    /**
     * notification that a user properly authenticated while making
     * a web service invocation.
     */
    public void authSucess(BundleDescriptor desc, Endpoint endpoint, Principal principal);

    /**
     * notification that a user authentication attempt has failed.
     * @param endpoint the endpoint selector
     * @param principal Optional principal that failed
     */
    public void authFailure(BundleDescriptor desc, Endpoint endpoint, Principal principal);
}
