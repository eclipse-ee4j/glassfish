/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jvnet.libpam.impl.CLibrary.Passwd;

/**
 * Linux passwd
 *
 * ?struct passwd
 * {
 *   char *pw_name;
 *   char *pw_passwd;
 *   __uid_t pw_uid;
 *   __gid_t pw_gid;
 *   char *pw_gecos;
 *   char *pw_dir;
 *   char *pw_shell;
 * };
 *
 * @author Sebastian Sdorra
 */
public class LinuxPasswd extends Passwd {
    /* Honeywell login info */
    public String pw_gecos;

    /* home directory */
    public String pw_dir;

    /* default shell */
    public String pw_shell;


    public String getPwGecos() {
        return pw_gecos;
    }

    @Override
    public String getPwDir() {
        return pw_dir;
    }

    @Override
    public String getPwShell() {
        return pw_shell;
    }

    @Override
    protected List getFieldOrder() {
        List fieldOrder = new ArrayList(super.getFieldOrder());
        fieldOrder.addAll(Arrays.asList("pw_gecos", "pw_dir", "pw_shell"));
        return fieldOrder;
    }

    public void setPwGecos(String pw_gecos) {
        this.pw_gecos = pw_gecos;
    }

    public void setPwDir(String pw_dir) {
        this.pw_dir = pw_dir;
    }

    public void setPwShell(String pw_shell) {
        this.pw_shell = pw_shell;
    }

}
