/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.internal.api;

import javax.security.auth.Subject;

import org.jvnet.hk2.annotations.Service;

/**
 * Implements the internal system administrator contract for embedded
 * internal command submissions.
 *
 * @author tjquinn
 */
@Service(name="embedded")
public class EmbeddedSystemAdministrator implements InternalSystemAdministrator {

    @Override
    public Subject getSubject() {
        final Subject result = new Subject();
        result.getPrivateCredentials().add(new EmbeddedSystemAdministratorCreds());
        return result;
    }

    public boolean matches(final Subject other) {
        return ! other.getPrivateCredentials(EmbeddedSystemAdministratorCreds.class).isEmpty();
    }

    private static class EmbeddedSystemAdministratorCreds {}
}
