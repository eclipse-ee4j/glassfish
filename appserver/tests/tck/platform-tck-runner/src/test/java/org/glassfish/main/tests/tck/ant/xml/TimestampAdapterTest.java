/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.tests.tck.ant.xml;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author David Matejcek
 */
public class TimestampAdapterTest {

    private final TimestampAdapter adapter = new TimestampAdapter();

    @Test
    public void unmarshall() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        ZonedDateTime zoned = now.atZone(ZoneId.systemDefault());
        OffsetDateTime offsetNow = now.atOffset(ZoneOffset.of("+05:00"));
        assertAll(
            () -> assertEquals(now, adapter.unmarshal(now.toString())),
            () -> assertEquals(now, adapter.unmarshal(offsetNow.toString())),
            () -> assertEquals(now, adapter.unmarshal(zoned.toString())),
            () -> assertEquals(today.atStartOfDay(), adapter.unmarshal(today.toString())),
            () -> assertEquals(LocalDateTime.of(2022, 9, 20, 10, 42, 0), adapter.unmarshal("2022-09-20T10:42")),
            () -> assertNull(adapter.unmarshal(null))
        );
    }


    @Test
    public void marshall() {
        LocalDateTime now = LocalDateTime.now();
        assertAll(
            () -> assertEquals(now.toString(), adapter.marshal(now)),
            () -> assertNull(adapter.marshal(null))
        );
    }
}
