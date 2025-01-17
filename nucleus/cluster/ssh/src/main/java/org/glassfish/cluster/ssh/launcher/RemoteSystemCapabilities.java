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

import java.lang.Runtime.Version;

/**
 * Detected capabilities of the remote operating system.
 */
public class RemoteSystemCapabilities {

    private final String javaHome;
    private final Version javaVersion;
    private final OperatingSystem operatingSystem;

    /**
     * @param javaHome - it is a string, because we want to use it as it is reported from the operating system.
     * @param javaVersion
     * @param operatingSystem
     */
    RemoteSystemCapabilities(String javaHome, Version javaVersion, OperatingSystem operatingSystem) {
        this.javaHome = javaHome;
        this.javaVersion = javaVersion;
        this.operatingSystem = operatingSystem;
    }


    /**
     * @return true if the java command is supported by the remote operating system.
     */
    public boolean isJavaSupported() {
        return javaHome != null && javaVersion != null;
    }


    /**
     * @return true if the remote system is NOT Windows.
     */
    public boolean isChmodSupported() {
        return operatingSystem != OperatingSystem.WINDOWS;
    }


    /**
     * @return detected operating system
     * @see OperatingSystem
     */
    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }



    @Override
    public String toString() {
        return getClass().getSimpleName() + "[os=" + operatingSystem + ", javaHome=" + javaHome
            + ", javaVersion=" + javaVersion + "]";
    }
}
