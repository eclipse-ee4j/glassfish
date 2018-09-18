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

package com.sun.enterprise.admin.cli.remote;

import org.glassfish.api.admin.CommandException;

/** This is a trick for {@link RemoteRestCommand} where command lifecycle must be
 * restarted. One reason is potential change of cached {@code CommandModel}.
 *
 * @author mmares
 */
public class ReExecuted extends CommandException {
    
    private int executionResult;
    
    public ReExecuted(int executionResult) {
        super("Command was successfully reexecuted. Outer lifecycle will be skiped.");
        this.executionResult = executionResult;
    }

    public int getExecutionResult() {
        return executionResult;
    }
    
}
