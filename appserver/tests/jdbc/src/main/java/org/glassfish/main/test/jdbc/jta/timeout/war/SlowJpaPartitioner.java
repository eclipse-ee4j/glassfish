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

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.lang.System.Logger;

import org.glassfish.main.test.jdbc.jta.timeout.war.stopwatch.Stopwatch;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

@Stateless
public class SlowJpaPartitioner {

    private static final Logger LOG = System.getLogger(SlowJpaPartitioner.class.getName());
    public static final int TIMEOUT_IN_SECONDS = 2;

    @PersistenceContext(unitName = "default")
    private EntityManager em;


    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void slowlyPreparePartition(){
        LOG.log(INFO, "slowlyPreparePartition()");
        try {
            persist("Entity 1");
            LOG.log(INFO, "Simulating time consuming work.");
            // to be sure we exceeded allowed time
            Thread.sleep(TIMEOUT_IN_SECONDS * 1000L + 300L);
            LOG.log(INFO, "Time consuming work finished.");
        } catch (InterruptedException ex) {
            // test broken, you should not get here.
            LOG.log(ERROR, "Thread Interruption", ex);
        } finally {
            em.flush();
        }
    }

    @Stopwatch
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void executePreparedPartition() {
        LOG.log(INFO, "executePreparedPartition()");
        persist("Entity 2");
        em.flush();
    }

    private void persist(String name){
        em.persist(new TestEntity(name));
    }
}
