/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.main.jdke.cl;

import java.net.URL;
import java.net.URLClassLoader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Matejcek
 */
public class GlassfishUrlClassLoaderTest {

    @Test
    public void isParallel() {
        assertAll(
            () -> assertTrue(new URLClassLoader(new URL[0]).isRegisteredAsParallelCapable()),
            () -> assertTrue(new GlassfishUrlClassLoader("GlassfishUrlClassLoaderTest", new URL[0]).isRegisteredAsParallelCapable())
        );
    }
}
