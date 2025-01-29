/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.cluster.ssh.launcher;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import java.io.IOException;

/**
 * Communication failure or unsuccessful command.
 * As usually we lack to have detailed information about what happened, this exception
 * adds the cause to its message, separated just by a space.
 * Exception instances then can be layered without losing the information.
 */
public class SSHException extends IOException {
    private static final long serialVersionUID = 1L;

    /**
     * @param message
     * @param cause the description of the cause will be appended to the message in parameter.
     */
    public SSHException(String message, Exception cause) {
        super(message + ' ' + toCauseMessage(cause), cause);
    }

    /**
     * @param message what happened.
     */
    public SSHException(String message) {
        super(message);
    }


    private static String toCauseMessage(Exception e) {
        if (e instanceof SSHException || e instanceof JSchException) {
            return e.getMessage();
        }
        if (e instanceof SftpException) {
            SftpException cause = (SftpException) e;
            // Permission denied is not much useful, so provide a bit better hint.
            if (cause.id == 3) {
                return "SFTP: " + e.getMessage() + " - the file or directory is probably open by some process.";
            }
            return "SFTP: " + e.getMessage();
        }
        // note: SftpException contains also an error id.
        return e.toString();
    }
}
