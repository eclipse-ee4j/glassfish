/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.nucleus.admin.progress;

import java.util.List;
import org.testng.annotations.Test;
import static org.glassfish.tests.utils.NucleusTestUtils.*;
import static org.testng.AssertJUnit.*;

/**
 *
 * @author martinmares
 */
@Test(testName="ProgressStatusFailTest")
public class ProgressStatusFailTest {

    public void failDuringExecution() {
        NadminReturn result = nadminWithOutput("progress-fail-in-half");
        assertFalse(result.returnValue);
        List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(result.out);
        assertFalse(prgs.isEmpty());
        assertEquals(50, prgs.get(prgs.size() - 1).getValue());
    }

    public void timeout() {
        NadminReturn result = nadminWithOutput(6 * 1000, "progress-custom", "3x1", "1x8", "2x1");
        assertFalse(result.returnValue);
        List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(result.out);
        assertFalse(prgs.isEmpty());
        assertEquals(50, prgs.get(prgs.size() - 1).getValue());
    }

}
