/*
 * Copyright (c) 2022, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul.formatter;

import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Matejcek
 */
public class LogFormatDetectorTest {

    private static final String ODL_RECORD
        = "[2022-04-20T22:05:43.203321+0100] [glassfish 7.0] [INFO] [NCLS-LOGGING-00009] "
        + "[jakarta.enterprise.logging] [tid: _ThreadID=21 _ThreadName=RunLevelControllerThread-1587416743113] "
        + "[timeMillis: 1587416743203] [levelValue: 800] [[";
    private static final String ULF_RECORD
        = "[#|2022-04-20T22:02:35.314553+0100|INFO|glassfish 7.0|jakarta.enterprise.logging|_ThreadID=21;"
        + "_ThreadName=RunLevelControllerThread-1587416555246;_TimeMillis=1587416555314;_LevelValue=800;"
        + "_MessageID=NCLS-LOGGING-00009;|";
    private static final String ONELINE_RECORD = "22:22:15.796552    INFO                 main"
        + "                        org.glassfish.acme.GlassFishLogManagerTest.externalHandlers Tick tock!";
    private static final String RANDOM_RECORD = "liuasudhfuk fhuashfu hiufh fueqrhfuqrehf qufhr uihuih uih jj";

    private final LogFormatDetector detector = new LogFormatDetector();


    @Test
    public void odl() {
        assertAll(
            () -> assertTrue(detector.isODLFormatLogHeader(ODL_RECORD), "is ODL"),
            () -> assertFalse(detector.isOneLineLFormatLogHeader(ODL_RECORD), "is OneLine"),
            () -> assertFalse(detector.isUniformFormatLogHeader(ODL_RECORD), "is UNL"),
            () -> assertEquals(ODLLogFormatter.class.getName(), detector.detectFormatter(ODL_RECORD))
        );
    }

    @Test
    public void oneline() {
        assertAll(
            () -> assertFalse(detector.isODLFormatLogHeader(ONELINE_RECORD), "is ODL"),
            () -> assertTrue(detector.isOneLineLFormatLogHeader(ONELINE_RECORD), "is OneLine"),
            () -> assertFalse(detector.isUniformFormatLogHeader(ONELINE_RECORD), "is UNL"),
            () -> assertEquals(OneLineFormatter.class.getName(), detector.detectFormatter(ONELINE_RECORD))
        );
    }

    @Test
    public void uniform() {
        assertAll(
            () -> assertFalse(detector.isODLFormatLogHeader(ULF_RECORD), "is ODL"),
            () -> assertFalse(detector.isOneLineLFormatLogHeader(ULF_RECORD), "is OneLine"),
            () -> assertTrue(detector.isUniformFormatLogHeader(ULF_RECORD), "is UNL"),
            () -> assertEquals(UniformLogFormatter.class.getName(), detector.detectFormatter(ULF_RECORD))
        );
    }

    @Test
    public void unknown() {
        assertAll(
            () -> assertFalse(detector.isODLFormatLogHeader(RANDOM_RECORD), "is ODL"),
            () -> assertFalse(detector.isOneLineLFormatLogHeader(RANDOM_RECORD), "is OneLine"),
            () -> assertFalse(detector.isUniformFormatLogHeader(RANDOM_RECORD), "is UNL"),
            () -> assertNull(detector.detectFormatter(RANDOM_RECORD))
        );
    }


    @Test
    public void noFile() throws Exception {
        assertNull(detector.detectFormatter(null, UTF_8));
    }


    @Test
    public void isCompressedFile() {
        assertAll(
            () -> assertFalse(detector.isCompressedFile("xxx.log")),
            () -> assertTrue(detector.isCompressedFile("xxx.gz"))
        );
    }


    @Test
    public void isOneLineFormat() {
        assertAll(
            () -> assertFalse(detector.isOneLineLFormatLogHeader("22:22:15.796552    INFO thread")),
            () -> assertTrue(detector.isOneLineLFormatLogHeader("22:22:15.796552    INFO t c m")),
            () -> assertTrue(detector.isOneLineLFormatLogHeader("22:22:15.796552    INFO t c m xxx xxx x "))
        );
    }
}
