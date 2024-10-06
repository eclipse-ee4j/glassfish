/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

import java.util.Iterator;
import java.util.List;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.glassfish.main.admin.test.progress.ProgressMessage.isIncreasing;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author martinmares
 */
@ExtendWith(JobTestExtension.class)
public class ProgressStatusSpecialITest {

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin(false);


    @Test
    public void stepBackCommand() {
        AsadminResult result = ASADMIN.exec("progress-step-back");
        assertThat(result, asadminOK());
        List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(result.getStdOut());
        assertThat(prgs, not(isIncreasing()));
        Iterator<ProgressMessage> itr = prgs.iterator();
        while (itr.hasNext()) {
            ProgressMessage prg = itr.next();
            if (prg.getValue() >= 80) {
                break;
            }
        }
        assertTrue(itr.hasNext(), "Exist more records");
        while (itr.hasNext()) {
            ProgressMessage prg = itr.next();
            assertThat(prg.getValue(), lessThanOrEqualTo(80));
            if (prg.getValue() < 80) {
                break;
            }
        }
        assertTrue(itr.hasNext(), "Exist more records");
        ProgressMessage prg = itr.next();
        assertThat(prg.getValue(), lessThan(80));
    }

    @Test
    public void doubleTotalCommand() {
        AsadminResult result = ASADMIN.exec("progress-double-totals");
        assertThat(result, asadminOK());
        List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(result.getStdOut());
        assertThat(prgs, not(isIncreasing()));
    }

}
