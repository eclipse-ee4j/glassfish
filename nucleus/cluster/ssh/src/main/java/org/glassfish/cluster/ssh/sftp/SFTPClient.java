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

package org.glassfish.cluster.ssh.sftp;

import com.jcraft.jsch.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import org.glassfish.cluster.ssh.util.SSHUtil;

public class SFTPClient {

    private Session session = null;

    private ChannelSftp sftpChannel = null;

    public SFTPClient(Session session) throws JSchException {
        this.session = session;
        sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();
        SSHUtil.register(session);
    }

    public ChannelSftp getSftpChannel() {
        return sftpChannel;
    }

    /**
     * Close the SFTP connection and free any resources associated with it.
     * close() should be called when you are done using the SFTPClient
     */
    public void close() {
        if (session != null) {
            SSHUtil.unregister(session);
            session = null;
        }
    }

    /**
     * Checks if the given path exists.
     */
    public boolean exists(String path) throws SftpException {
        return _stat(normalizePath(path))!=null;
    }

    /**
     * Graceful stat that returns null if the path doesn't exist.
     */
    public SftpATTRS _stat(String path) throws SftpException {
        try {
            return sftpChannel.stat(normalizePath(path));
        } catch (SftpException e) {
            int c = e.id;
            if (c == ChannelSftp.SSH_FX_NO_SUCH_FILE)
                return null;
            else
                throw e;
        }
    }

    /**
     * Makes sure that the directory exists, by creating it if necessary.
     */
    public void mkdirs(String path, int posixPermission) throws SftpException {
        // remove trailing slash if present
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        path = normalizePath(path);
        SftpATTRS attrs = _stat(path);
        if (attrs != null && attrs.isDir())
            return;

        int idx = path.lastIndexOf("/");
        if (idx>0)
            mkdirs(path.substring(0,idx), posixPermission);
        sftpChannel.mkdir(path);
        sftpChannel.chmod(posixPermission, path);
    }

    public void chmod(String path, int permissions) throws SftpException {
        path = normalizePath(path);
        sftpChannel.chmod(permissions, path);
    }

    // Commands run in a shell on Windows need to have forward slashes.
    public static String normalizePath(String path){
        return path.replaceAll("\\\\","/");
    }

    public void cd(String path) throws SftpException {
        path = normalizePath(path);
        sftpChannel.cd(path);
    }
}
