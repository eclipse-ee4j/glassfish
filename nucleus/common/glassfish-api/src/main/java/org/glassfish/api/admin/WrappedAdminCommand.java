/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

/**
 * Represents command wrapped with {@link CommandWrapperImpl}.
 * It should be enough to wrap command without an overhead.
 * E.g.: <code>
 * new WrappedAdminCommand(command) {
 *     public void execute(...
 * }
 * </code>
 *
 * @author Andriy Zhdanov
 *
 */
public abstract class WrappedAdminCommand implements AdminCommand {
    private AdminCommand wrappedCommand;

    /**
     * Default constructor.
     *
     * @param command Original command is was wrapped.
     */
    public WrappedAdminCommand(AdminCommand command) {
        wrappedCommand = command;
    }

    /**
     * Original command that was wrapped.
     *
     * @return unwrapped command.
     */
    public AdminCommand getWrappedCommand() {
        return wrappedCommand;
    }

    /**
     * Get root of wrapped command.
     *
     * @return command.
     */
    protected AdminCommand getUnwrappedCommand() {
        AdminCommand unwrappedCommand = wrappedCommand;
        while (unwrappedCommand instanceof WrappedAdminCommand) {
            unwrappedCommand = ((WrappedAdminCommand) unwrappedCommand).getWrappedCommand();
        }
        return unwrappedCommand;
    }
}
