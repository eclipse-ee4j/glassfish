/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * Created by IntelliJ IDEA. User: cmott Date: Jul 29, 2010 Time: 6:56:38 PM
 */
public class SSHCommandExecutionException extends CommandException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new <code>SSHCommandExecutionException</code> without detail message.
     */
    public SSHCommandExecutionException() {
    }

    /**
     * Constructs a <code>SSHCommandExecutionException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public SSHCommandExecutionException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new <code>SSHCommandExecutionException</code> exception with the specified cause.
     */
    public SSHCommandExecutionException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new <code>SSHCommandExecutionException</code> exception with the specified detailed message and cause.
     */
    public SSHCommandExecutionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    private String SSHSettings = null;
    private String fullCommand = null;

    /*
     * Stores the settings for the SSH connection that apply to node that was used in the command execution
     */
    public void setSSHSettings(String sshSettings) {
        SSHSettings = sshSettings;
    }
    /*
     * Returns the settings for the SSH connection that apply to node that was used in the command execution
     */

    public String getSSHSettings() {
        return SSHSettings;
    }
    /*
     * Stores the fully qualified command that was run on the remote node over SSH
     */

    public void setCommandRun(String fullcommand) {
        fullCommand = fullcommand;
    }
    /*
     * Returns the fully qualified command that was run on the remote node over SSH
     */

    public String getCommandRun() {
        return fullCommand;
    }
}
