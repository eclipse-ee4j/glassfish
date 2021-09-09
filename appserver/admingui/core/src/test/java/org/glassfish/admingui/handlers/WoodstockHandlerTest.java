/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.admingui.handlers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WoodstockHandlerTest {

    @Test
    public void testMessageForNullName() {
        assertNotNull(WoodstockHandler.prepareFileNotDeletedMessage(null));
    }


    @Test
    public void testMessageForNonNullName() {
        var actual = WoodstockHandler.prepareFileNotDeletedMessage("abcd");
        assertEquals("file Couldn't be found at abcd, proceeding execution without deletion.", actual);
    }
}
