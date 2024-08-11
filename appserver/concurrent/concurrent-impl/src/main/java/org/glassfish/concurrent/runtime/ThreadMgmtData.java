/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.concurrent.runtime;

import jakarta.enterprise.concurrent.spi.ThreadContextRestorer;
import jakarta.enterprise.concurrent.spi.ThreadContextSnapshot;

import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * @author David Matejcek
 */
class ThreadMgmtData implements Serializable {
    private static final long serialVersionUID = -4031876173005308591L;
    private static final Logger LOG = System.getLogger(ThreadMgmtData.class.getName());

    private final List<ThreadContextSnapshot> snapshots;
    private final List<ThreadContextRestorer> restorers;

    public static ThreadMgmtData createNextGeneration(ThreadMgmtData oldGen) {
        List<ThreadContextRestorer> newRestorers = new ArrayList<>();
        for (ThreadContextSnapshot snapshot : oldGen.snapshots) {
            newRestorers.add(snapshot.begin());
        }
        return new ThreadMgmtData(emptyList(), newRestorers);
    }


    public ThreadMgmtData(List<ThreadContextSnapshot> snapshots) {
        this(snapshots, emptyList());
    }


    private ThreadMgmtData(List<ThreadContextSnapshot> snapshots, List<ThreadContextRestorer> restorers) {
        LOG.log(Level.TRACE, "ThreadMgmtData(snapshots={0}, restorers={1})", snapshots, restorers);
        this.snapshots = snapshots;
        this.restorers = restorers;
    }


    public void endContext() {
        for (ThreadContextRestorer restorer : restorers) {
            restorer.endContext();
        }
    }
}
