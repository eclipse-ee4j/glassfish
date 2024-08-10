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

package org.glassfish.security.services.api.context;


import org.glassfish.security.services.api.common.Attributes;
import org.jvnet.hk2.annotations.Contract;

/**
 * The Security Context Service maintains context needed by various security
 * services.  It is scoped per-thread (though this does not preclude it from
 * providing access to context that has different scope).
 */

@Contract
public interface SecurityContextService {

    /**
     * Return the Environment attributes collection associated with the current thread.
     *
     * @return The environment attributes.
     */
    Attributes getEnvironmentAttributes();

}
