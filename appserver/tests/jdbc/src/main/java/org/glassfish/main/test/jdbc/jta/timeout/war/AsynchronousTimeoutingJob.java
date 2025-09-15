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

package org.glassfish.main.test.jdbc.jta.timeout.war;

import jakarta.ejb.Asynchronous;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.Path;

import java.util.logging.Logger;

import org.glassfish.main.test.jdbc.jta.timeout.war.stopwatch.Stopwatch;

@Path("timeout")
@Stateless
public class AsynchronousTimeoutingJob {

    @EJB
    private SlowJpaPartitioner partitioner;

    private static final Logger LOG = Logger.getLogger(AsynchronousTimeoutingJob.class.getName());

    @Stopwatch
    @Asynchronous
    public void tmeoutingAsync() {
        LOG.info("tmeoutingAsync()");
        partitioner.slowlyPreparePartition();
        LOG.info("Done.");
    }

    @Stopwatch
    @Asynchronous
    public void timeoutingAsyncWithFailingNextStep() {
        LOG.info("timeoutingAsyncWithFailingNextStep()");
        partitioner.slowlyPreparePartition();
        partitioner.executePreparedPartition();
        LOG.info("Done.");
    }

    @Stopwatch
    @Asynchronous
    public void timeoutingAsyncWithFailingNextStepCatchingExceptionAndRedo() {
        try {
            LOG.info("timeoutingAsyncWithFailingNextStepCatchingExceptionAndRedo()");
            partitioner.slowlyPreparePartition();
            partitioner.executePreparedPartition();
            LOG.info("Done in first attempt.");
        } catch (Exception e) {
            LOG.info(() -> String.format("Catched exception: %s; Reexecuting.", e));
            partitioner.executePreparedPartition();
            LOG.info("Done in second attempt.");
        }
    }
}
