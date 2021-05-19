/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package connector;

import java.lang.reflect.Method;
import java.util.Iterator;
import jakarta.resource.spi.endpoint.MessageEndpoint;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import jakarta.resource.spi.UnavailableException;
import jakarta.resource.spi.work.ExecutionContext;
import jakarta.resource.spi.work.Work;
import jakarta.resource.spi.work.WorkManager;
import jakarta.resource.spi.work.WorkException;

/**
 *
 * @author        Qingqing Ouyang
 */
public class TestWMWork implements Work {

    private boolean stop = false;
    private int id;
    private boolean isRogue;
    private boolean doNest;
    private WorkManager wm;
    private ExecutionContext ctx;

    public TestWMWork(int id, boolean isRogue) {
        this(id, isRogue, false, null);
    }

    public TestWMWork(int id, boolean isRogue,
            boolean doNest, ExecutionContext ctx) {
        this.id = id;
        this.isRogue = isRogue;
        this.doNest = doNest;
        this.ctx = ctx;
    }

    public void setWorkManager (WorkManager wm) {
        this.wm = wm;
    }

    public void run() {

        System.out.println("TestWMWork[" + id + "].start running");
        if (!isRogue) {
            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception ex) {
                System.out.println("TestWMWork[" + id + "].interrupted = ");
                ex.printStackTrace();
            }
        } else {
            System.out.println("TestWMWork: Simulating rogue RA's Work: Expected Arithmetic Exception - divide by Zero");
            int j = 100/0;
        }

        if (doNest && (wm != null)) {
            Work nestedWork = new TestWMWork(8888, false);
            try {
                wm.doWork(nestedWork, 1*1000, ctx, null);
            } catch (WorkException ex) {
                if (ex.getErrorCode().equals(
                            WorkException.TX_CONCURRENT_WORK_DISALLOWED)) {
                    System.out.println("TestWMWork[" + id + "] " +
                            "PASS: CAUGHT EXPECTED = " + ex.getErrorCode());
                } else {
                    System.out.println("TestWMWork[" + id + "] " +
                            "FAIL: CAUGHT UNEXPECTED = " + ex.getErrorCode());
                }
            }

            nestedWork = new TestWMWork(9999, false);
            try {
                ExecutionContext ec = new ExecutionContext();
                ec.setXid(new XID());
                ec.setTransactionTimeout(5*1000); //5 seconds
                wm.doWork(nestedWork, 1*1000, ec, null);
            } catch (Exception ex) {
                System.out.println("TestWMWork[" + id + "] " +
                        "FAIL: CAUGHT UNEXPECTED = " + ex.getMessage());
            }
        }

        System.out.println("TestWMWork[" + id + "].stop running");
    }

    public void release() {}

    public void stop() {
        this.stop = true;
    }

    public String toString() {
       return String.valueOf(id);
    }
}
