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

package org.jvnet.libpam;

import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jvnet.libpam.impl.CLibrary.Group;
import org.jvnet.libpam.impl.CLibrary.Passwd;

import static org.jvnet.libpam.impl.CLibrary.libc;

/**
 * Represents an Unix user. Immutable.
 *
 * @author Kohsuke Kawaguchi
 */
public class UnixUser {
    private final String userName, gecos, dir, shell;
    private final int uid, gid;
    private final Set<String> groups;

    UnixUser(String userName, Passwd pwd) throws PAMException {
        this.userName = userName;
        this.gecos = pwd.getPwGecos();
        this.dir = pwd.getPwDir();
        this.shell = pwd.getPwShell();
        this.uid = pwd.getPwUid();
        this.gid = pwd.getPwGid();

        long sz = 4; /* sizeof(gid_t) */

        int ngroups = 64;
        Memory m = new Memory(ngroups * sz);
        IntByReference pngroups = new IntByReference(ngroups);
        try {
            if (libc.getgrouplist(userName, pwd.getPwGid(), m, pngroups) < 0) {
                // allocate a bigger memory
                m = new Memory(pngroups.getValue() * sz);
                if (libc.getgrouplist(userName, pwd.getPwGid(), m, pngroups) < 0) {
                    // shouldn't happen, but just in case.
                    throw new PAMException("getgrouplist failed");
                }
            }
            ngroups = pngroups.getValue();
        } catch (LinkageError e) {
            // some platform, notably Solaris, doesn't have the getgrouplist function
            ngroups = libc._getgroupsbymember(userName, m, ngroups, 0);
            if (ngroups < 0) {
                throw new PAMException("_getgroupsbymember failed");
            }
        }

        groups = new HashSet<>();
        for (int i = 0; i < ngroups; i++) {
            int gid = m.getInt(i * sz);
            Group grp = libc.getgrgid(gid);
            if (grp == null) {
                continue;
            }
            groups.add(grp.gr_name);
        }
    }

    public UnixUser(String userName) throws PAMException {
        this(userName, Passwd.loadPasswd(userName));
    }

    /**
     * Copy constructor for mocking. Not intended for regular use. Only for testing. This signature may change in the
     * future.
     */
    protected UnixUser(String userName, String gecos, String dir, String shell, int uid, int gid, Set<String> groups) {
        this.userName = userName;
        this.gecos = gecos;
        this.dir = dir;
        this.shell = shell;
        this.uid = uid;
        this.gid = gid;
        this.groups = groups;
    }

    /**
     * Gets the unix account name. Never null.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Gets the UID of this user.
     */
    public int getUID() {
        return uid;
    }

    /**
     * Gets the GID of this user.
     */
    public int getGID() {
        return gid;
    }

    /**
     * Gets the gecos (the real name) of this user.
     */
    public String getGecos() {
        return gecos;
    }

    /**
     * Gets the home directory of this user.
     */
    public String getDir() {
        return dir;
    }

    /**
     * Gets the shell of this user.
     */
    public String getShell() {
        return shell;
    }

    /**
     * Gets the groups that this user belongs to.
     *
     * @return never null.
     */
    public Set<String> getGroups() {
        return Collections.unmodifiableSet(groups);
    }

    public static boolean exists(String name) {
        return libc.getpwnam(name) != null;
    }
}
