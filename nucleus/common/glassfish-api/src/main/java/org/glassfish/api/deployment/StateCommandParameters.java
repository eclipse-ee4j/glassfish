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

package org.glassfish.api.deployment;

import org.glassfish.api.Param;

/**
 * parameters passed to commands changing the state of a deployed application
 */
public class StateCommandParameters extends OpsParams {

    @Param(primary = true)
    public String component = null;

    @Param(optional = true)
    public String target;

    @Override
    public String name() {
        return component;
    }

    @Override
    public String libraries() {
        throw new IllegalStateException("We need to be able to get access to libraries when enabling/disabling");
    }
}
