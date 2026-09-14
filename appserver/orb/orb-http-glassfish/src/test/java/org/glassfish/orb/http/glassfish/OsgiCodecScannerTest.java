/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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


package org.glassfish.orb.http.glassfish;

import java.util.List;

import org.glassfish.orb.http.protocol.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The scanner outside the framework it was written for.
 * <p>
 * It exists to bridge OSGi, where a ServiceLoader in one bundle cannot see a
 * provider in another. Everywhere else that problem does not exist, and the
 * scanner has to notice rather than fail: these same classes run in a plain
 * client JVM and in every test in this repository.
 */
class OsgiCodecScannerTest {

    @Test
    @DisplayName("with no framework present it answers with what is already on the class path")
    void withoutAFrameworkItDefersToTheClassPath() {
        // No bundle context here, so there is nothing to scan. Throwing would
        // take down the endpoint's startup for the absence of a problem.
        List<String> codecs = OsgiCodecScanner.scanAndRegister();

        assertNotNull(codecs);
        assertTrue(codecs.contains(ContentType.CODEC_JSER),
                "the built-in codec is the floor and must always be there: " + codecs);
    }

    @Test
    @DisplayName("scanning twice does not change the answer")
    void scanningIsIdempotent() {
        List<String> first = OsgiCodecScanner.scanAndRegister();
        List<String> second = OsgiCodecScanner.scanAndRegister();

        // The endpoint scans at startup; a second scan - a restart, another
        // instance - must not accumulate providers or reorder them.
        assertEquals(first, second);
        assertFalse(first.isEmpty());
    }
}
