/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.contextpropagation.adaptors;

import java.util.concurrent.atomic.AtomicReference;

public abstract class TestableThread extends Thread {

    private final AtomicReference<Throwable> throwableHolder = new AtomicReference<>();

    protected abstract void runTest() throws Exception;

    public void startJoinAndCheckForFailures() {
        start();
        try {
            join();
        } catch (InterruptedException e) {
            throwableHolder.set(e);
        }
        Throwable throwable = throwableHolder.get();
        if (throwable == null) {
            return;
        }
        if (throwable instanceof AssertionError) {
            throw (AssertionError) throwable;
        }
        throw new RuntimeException(throwable.getMessage(), throwable);
    }


    @Override
    public void run() {
        try {
            runTest();
        } catch (Throwable t) {
            throwableHolder.set(t);
        }
    }
}
