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

package org.glassfish.api.admin;

import java.util.Map;

import javax.security.auth.Subject;

/**
 * Represents the ability (and responsibility) of an AdminCommand implementation to provide its own authorization logic,
 * instead of relying on the command framework and the AccessRequired annotation to do so.
 * <p>
 * Commands with complicated authorization requirements will implement this interface, in addition to the AdminCommand
 * interface.
 *
 * @author tjquinn
 */
public interface SelfAuthorizer {

    /**
     * Tells whether the Subject is authorized to execute the AdminCommand which implements this interface.
     * <p>
     * Note that the command framework will have injected parameter values into fields annotated with @Param before invoking
     * this method, so the logic in isAuthorized can use those parameters in making its decision.
     *
     * @param ctx the AdminCommandContext for the current command execution
     * @return true if the Subject (recorded in the command context) can run the command; false otherwise.
     */
    void authorize(Subject s, Map<String, Object> env);

}
