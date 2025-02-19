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

import java.util.Locale;

/**
 * This enum serves to distinguish operating system capabilities over SSH.
 */
public enum OperatingSystem {
    /** Linux based operating systems usually use Bash */
    LINUX,
    /** windows based operating systems usually use PowerShell and cmd.exe and don't support POSIX permissions. */
    WINDOWS,
    /**
     * Generic operating systems are big unknown.
     * This enum can be used when we don't care about system capabilities.
     */
    GENERIC,
    ;

    static OperatingSystem parse(String osNameProperty) {
        String osName = osNameProperty.toLowerCase(Locale.ENGLISH);
        if (osName.contains("linux")) {
            return LINUX;
        }
        if (osName.contains("win")) {
            return WINDOWS;
        }
        return GENERIC;
    }
}
