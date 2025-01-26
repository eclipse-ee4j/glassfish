/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.glassfish.bootstrap.cfg;

/** Supported platform we know about */
public enum OsgiPlatform {
    Felix("felix"),
    Knopflerfish("knopflerfish"),
    Equinox("equinox"),
    Embedded(null),
    /**
     * Older synonym for {@link #Embedded}
     * @deprecated Used by glassfish-maven-embedded-plugin which has it hardcoded
     */
    @Deprecated(forRemoval = true, since = "7.0.18")
    Static(null),
    ;

    private final String frameworkStorageDirectoryName;

    OsgiPlatform(String frameworkStorageDirectoryName) {
        this.frameworkStorageDirectoryName = frameworkStorageDirectoryName;
    }

    public String getFrameworkStorageDirectoryName() {
        return frameworkStorageDirectoryName;
    }
}
