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

package org.jvnet.libpam.impl;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

import java.util.Arrays;
import java.util.List;

import org.jvnet.libpam.PAMException;

/**
 * @author Kohsuke Kawaguchi
 */
public interface CLibrary extends Library {
    /**
     * Comparing http://linux.die.net/man/3/getpwnam and my Mac OS X reveals that the structure of this field isn't very
     * portable. In particular, we cannot read the real name reliably.
     */
    public class Passwd extends Structure {
        /**
         * User name.
         */
        public String pw_name;
        /**
         * Encrypted password.
         */
        public String pw_passwd;
        public int pw_uid;
        public int pw_gid;

        // ... there are a lot more fields

        public static Passwd loadPasswd(String userName) throws PAMException {
            Passwd pwd = libc.getpwnam(userName);
            if (pwd == null) {
                throw new PAMException("No user information is available");
            }
            return pwd;
        }

        public String getPwName() {
            return pw_name;
        }

        public String getPwPasswd() {
            return pw_passwd;
        }

        public int getPwUid() {
            return pw_uid;
        }

        public int getPwGid() {
            return pw_gid;
        }

        public String getPwGecos() {
            return null;
        }

        public String getPwDir() {
            return null;
        }

        public String getPwShell() {
            return null;
        }

        @Override
        protected List getFieldOrder() {
            return Arrays.asList("pw_name", "pw_passwd", "pw_uid", "pw_gid");
        }

        public void setPwName(String pw_name) {
            this.pw_name = pw_name;
        }

        public void setPwPasswd(String pw_passwd) {
            this.pw_passwd = pw_passwd;
        }

        public void setPwUid(int pw_uid) {
            this.pw_uid = pw_uid;
        }

        public void setPwGid(int pw_gid) {
            this.pw_gid = pw_gid;
        }

    }

    public class Group extends Structure {
        public String gr_name;
        // ... the rest of the field is not interesting for us

        @Override
        protected List getFieldOrder() {
            return Arrays.asList("gr_name");
        }

        public void setGrName(String gr_name) {
            this.gr_name = gr_name;
        }
    }

    Pointer calloc(int count, int size);

    Pointer strdup(String s);

    Passwd getpwnam(String username);

    /**
     * Lists up group IDs of the given user. On Linux and most BSDs, but not on Solaris. See
     * http://www.gnu.org/software/hello/manual/gnulib/getgrouplist.html
     */
    int getgrouplist(String user, int/* gid_t */ group, Memory groups, IntByReference ngroups);

    /**
     * getgrouplist equivalent on Solaris. See
     * http://mail.opensolaris.org/pipermail/sparks-discuss/2008-September/000528.html
     */
    int _getgroupsbymember(String user, Memory groups, int maxgids, int numgids);

    Group getgrgid(int/* gid_t */ gid);

    Group getgrnam(String name);

    // other user/group related functions that are likely useful
    // see http://www.gnu.org/software/libc/manual/html_node/Users-and-Groups.html#Users-and-Groups

    public static final CLibrary libc = Instance.init();

    static class Instance {
        private static CLibrary init() {
            if (Platform.isMac() || Platform.isOpenBSD()) {
                return Native.loadLibrary("c", BSDCLibrary.class);
            } else if (Platform.isFreeBSD()) {
                return Native.loadLibrary("c", FreeBSDCLibrary.class);
            } else if (Platform.isSolaris()) {
                return Native.loadLibrary("c", SolarisCLibrary.class);
            } else if (Platform.isLinux()) {
                return Native.loadLibrary("c", LinuxCLibrary.class);
            } else {
                return Native.loadLibrary("c", CLibrary.class);
            }
        }
    }
}
