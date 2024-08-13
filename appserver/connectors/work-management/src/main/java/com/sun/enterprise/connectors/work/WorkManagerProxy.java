/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors.work;


import jakarta.resource.spi.work.ExecutionContext;
import jakarta.resource.spi.work.Work;
import jakarta.resource.spi.work.WorkException;
import jakarta.resource.spi.work.WorkListener;
import jakarta.resource.spi.work.WorkManager;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Proxy for WorkManager.<br>
 * This implementation is Serializable(Externalizable) such that RAR implementation
 * can use it safely in Serialization mandated scenarios<br>
 *
 * @author Jagadish Ramu
 */
public class WorkManagerProxy implements WorkManager, Externalizable {

    private transient WorkManager wm;
    private String moduleName;

    public WorkManagerProxy(WorkManager wm, String moduleName){
        this.wm = wm;
        this.moduleName = moduleName;
    }

    public WorkManagerProxy(){
    }

    /**
     * @see jakarta.resource.spi.work.WorkManager
     */
    public void doWork(Work work) throws WorkException {
        wm.doWork(work);
    }

    /**
     * @see jakarta.resource.spi.work.WorkManager
     */
    public void doWork(Work work, long startTimeout, ExecutionContext executionContext,
                       WorkListener workListener) throws WorkException {
        wm.doWork(work, startTimeout, executionContext, workListener);
    }

    /**
     * @see jakarta.resource.spi.work.WorkManager
     */
    public long startWork(Work work) throws WorkException {
        return wm.startWork(work);
    }

    /**
     * @see jakarta.resource.spi.work.WorkManager
     */
    public long startWork(Work work, long startTimeout, ExecutionContext executionContext,
                          WorkListener workListener) throws WorkException {
        return wm.startWork(work, startTimeout, executionContext, workListener);
    }

    /**
     * @see jakarta.resource.spi.work.WorkManager
     */
    public void scheduleWork(Work work) throws WorkException {
        wm.scheduleWork(work);
    }
    /**
     * @see jakarta.resource.spi.work.WorkManager
     */
    public void scheduleWork(Work work, long startTimeout, ExecutionContext executionContext,
                             WorkListener workListener) throws WorkException {
        wm.scheduleWork(work, startTimeout, executionContext, workListener);
    }

    /**
     * @see java.io.Externalizable
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(moduleName);
    }

    /**
     * @see java.io.Externalizable
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        moduleName = in.readUTF();
        wm = WorkManagerFactoryImpl.retrieveWorkManager(moduleName);
    }

    public boolean equals(Object o){
        boolean equal = false;
        if(o instanceof WorkManagerProxy){
            WorkManagerProxy wmp = (WorkManagerProxy)o;
            equal = wmp.wm.equals(wm);
        }
        return equal;
    }

    public int hashCode(){
        return wm.hashCode();
    }
}
