/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.glassfish.main.test.app.concurrency.executor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@Startup
public class StartupBean {

    private boolean submittedTaskExecuted = false;

    @Resource
    ManagedExecutorService executor;

    public boolean isSubmittedTaskExecuted() {
        return submittedTaskExecuted;
    }

    @PostConstruct
    public void startup() {
        Thread startupThread = Thread.currentThread();
        executor.submit(() -> {
            submittedTaskExecuted = true;
            startupThread.interrupt();
        });
        try {
            Thread.sleep(10000);
            Logger.getLogger(StartupBean.class.getName()).log(Level.SEVERE, "Timeout reached waiting for submitted task to execute");
        } catch (InterruptedException ex) {
        }
    }
}
