/*
 * Copyright (c) 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

import jakarta.enterprise.concurrent.spi.ThreadContextProvider;
import jakarta.enterprise.concurrent.spi.ThreadContextRestorer;
import jakarta.enterprise.concurrent.spi.ThreadContextSnapshot;

import java.lang.System.Logger;
import java.util.Map;

import static java.lang.System.Logger.Level.INFO;

public class IntContextProvider implements ThreadContextProvider {

    @Override
    public ThreadContextSnapshot currentContext(Map<String, String> props) {
        return new IntContextSnapshot(IntContext.get());
    }


    @Override
    public ThreadContextSnapshot clearedContext(Map<String, String> props) {
        return new IntContextSnapshot(0);
    }


    @Override
    public String getThreadContextType() {
        return IntContext.class.getSimpleName();
    }

    private class IntContextSnapshot implements ThreadContextSnapshot {

        private final int contextSnapshot;

        IntContextSnapshot(final int snapshot) {
            contextSnapshot = snapshot;
        }


        @Override
        public ThreadContextRestorer begin() {
            ThreadContextRestorer restorer = new IntContextRestorer(IntContext.get());
            IntContext.set(contextSnapshot);
            return restorer;
        }
    }

    private static class IntContextRestorer implements ThreadContextRestorer {

        private final int contextToRestore;
        private boolean restored;

        IntContextRestorer(final int toRestore) {
            contextToRestore = toRestore;
        }


        @Override
        public void endContext() throws IllegalStateException {
            if (restored) {
                throw new IllegalStateException("already restored");
            }
            IntContext.set(contextToRestore);
            restored = true;
        }
    }

    public static class IntContext {

        private static final Logger LOG = System.getLogger(IntContext.class.getName());
        private static final ThreadLocal<Integer> local = ThreadLocal.withInitial(() -> 0);

        public static int get() {
            LOG.log(INFO, "get()");
            return local.get();
        }


        public static void set(final int value) {
            LOG.log(INFO, "set(value={0})", value);
            if (value == 0) {
                local.remove();
            } else {
                local.set(value);
            }
        }
    }
}
