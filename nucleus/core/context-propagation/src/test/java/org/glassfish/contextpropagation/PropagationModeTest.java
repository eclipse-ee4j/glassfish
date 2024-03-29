/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.contextpropagation;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropagationModeTest {

    @Test
    public void testFromOrdinal() {
        PropagationMode[] modes = PropagationMode.values();
        for (PropagationMode mode : modes) {
            assertEquals(mode, PropagationMode.fromOrdinal(mode.ordinal()));
        }
    }


    @Test
    public void testDefaultSet() {
        assertEquals(EnumSet.of(PropagationMode.THREAD, PropagationMode.RMI, PropagationMode.SOAP,
            PropagationMode.JMS_QUEUE, PropagationMode.MIME_HEADER), PropagationMode.defaultSet());
    }

}
