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

package org.glassfish.main.jul.record;

import java.util.logging.Level;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author David Matejcek
 */
public class MessageResolverTest {

    private final MessageResolver resolver = new MessageResolver();

    @Test
    public void testResolve() {
        final GlassFishLogRecord originalRecord = new GlassFishLogRecord(Level.INFO,
            "This number {0,number,#} is greater than this one {1,number,#}", false);
        originalRecord.setParameters(new Object[] {50L, 33L});
        final GlassFishLogRecord record = resolver.resolve(originalRecord);
        assertAll(() -> assertNull(record.getMessageKey()),
            () -> assertEquals("This number 50 is greater than this one 33", record.getMessage()));
    }
}
