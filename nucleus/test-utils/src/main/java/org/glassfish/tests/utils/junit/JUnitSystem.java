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

package org.glassfish.tests.utils.junit;

import java.io.File;
import java.nio.file.Path;

/**
 * Helper class to make possible running tests the same way in Maven as in Eclipse IDE, probably
 * also other environments.
 */
public final class JUnitSystem {

    private JUnitSystem() {
        // utility class
    }

    /**
     * Useful for a heuristic inside Eclipse and other environments.
     *
     * @return Absolute path to the glassfish directory.
     */
    public static Path detectBasedir() {
        // Maven would set this property.
        final String basedir = System.getProperty("basedir");
        if (basedir != null) {
            return new File(basedir).toPath().toAbsolutePath();
        }
        // Maybe we are standing in the basedir.
        final File target = new File("target");
        if (target.exists()) {
            return target.toPath().toAbsolutePath().getParent();
        }
        // Eclipse IDE sometimes uses target as the current dir.
        return new File(".").toPath().toAbsolutePath().getParent();
    }
}
