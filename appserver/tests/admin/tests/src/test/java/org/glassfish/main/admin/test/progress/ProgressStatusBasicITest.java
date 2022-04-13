/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.main.admin.test.progress;

import java.util.List;

import org.glassfish.main.admin.test.tool.asadmin.Asadmin;
import org.glassfish.main.admin.test.tool.asadmin.AsadminResult;
import org.glassfish.main.admin.test.tool.asadmin.GlassFishTestEnvironment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.glassfish.main.admin.test.progress.ProgressMessage.isIncreasing;
import static org.glassfish.main.admin.test.tool.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author martinmares
 */
@ExtendWith(JobTestExtension.class)
public class ProgressStatusBasicITest {

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();


    @Test
    public void simple() {
        AsadminResult result = ASADMIN.exec("progress-simple");
        assertThat(result, asadminOK());
        List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(result.getStdOut());
        assertThat(prgs, isIncreasing());
        assertEquals(12, prgs.size());
        for (int i = 0; i < 11; i++) {
            assertEquals(10 * i, prgs.get(i + 1).getValue());
            assertTrue(prgs.get(i + 1).isPercentage());
        }
    }

    @Test
    public void simpleNoTotal() {
        AsadminResult result = ASADMIN.exec("progress-simple", "--nototalsteps");
        assertThat(result, asadminOK());
        List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(result.getStdOut());
        assertThat(prgs, isIncreasing());
        boolean nonPercentageExists = false;
        for (ProgressMessage prg : prgs) {
            if (prg.getValue() != 0 && prg.getValue() != 100) {
                assertFalse(prg.isPercentage());
                nonPercentageExists = true;
            }
        }
        assertTrue(nonPercentageExists);
    }

    @Test
    public void simpleSpecInAnnotation() {
        AsadminResult result = ASADMIN.exec("progress-full-annotated");
        assertThat(result, asadminOK());
        List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(result.getStdOut());
        assertThat(prgs, hasSize(12));
        assertThat(prgs, isIncreasing());
        for (int i = 0; i < 11; i++) {
            assertEquals(10 * i, prgs.get(i + 1).getValue());
            assertTrue(prgs.get(i + 1).isPercentage());
        }
        assertEquals("annotated:", prgs.get(5).getScope());
    }

    @Test
    public void simpleTerse() {
        AsadminResult result = ASADMIN.exec("--terse", "progress-simple");
        assertThat(result, asadminOK());
        List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(result.getStdOut());
        assertThat(prgs, hasSize(0));
    }
}
