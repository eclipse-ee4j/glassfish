/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

import java.lang.System.Logger;

import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher;

import static com.sun.enterprise.tests.progress.ProgressCustomCommand.generateIntervals;
import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;
import static org.hamcrest.MatcherAssert.assertThat;

final class UsualLatency {
    private static final Logger LOG = System.getLogger(UsualLatency.class.getName());
    private static Long latency;

    static final synchronized long getMeasuredLatency() {
        if (latency == null) {
            final long start = System.currentTimeMillis();
            final AsadminResult result = getAsadmin().exec("progress-custom", generateIntervals(0L));
            latency = System.currentTimeMillis() - start;
            assertThat(result, AsadminResultMatcher.asadminOK());
            LOG.log(INFO, "Measured sample time for the progress-custom command is {0} ms", latency);
        }
        return latency;
    }
}
