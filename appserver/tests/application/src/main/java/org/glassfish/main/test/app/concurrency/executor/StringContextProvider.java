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

public class StringContextProvider implements ThreadContextProvider {

    @Override
    public ThreadContextSnapshot currentContext(Map<String, String> props) {
        return new StringContextSnapshot(StringContext.get());
    }


    @Override
    public ThreadContextSnapshot clearedContext(Map<String, String> props) {
        return new StringContextSnapshot("");
    }


    @Override
    public String getThreadContextType() {
        return StringContext.class.getSimpleName();
    }


    private class StringContextSnapshot implements ThreadContextSnapshot {

        private final String contextSnapshot;

        StringContextSnapshot(final String snapshot) {
            contextSnapshot = snapshot;
        }


        @Override
        public ThreadContextRestorer begin() {
            ThreadContextRestorer restorer = new StringContextRestorer(StringContext.get());
            StringContext.set(contextSnapshot);
            return restorer;
        }
    }


    private static class StringContextRestorer implements ThreadContextRestorer {

        private final String contextToRestore;
        private boolean restored;

        StringContextRestorer(final String toRestore) {
            contextToRestore = toRestore;
        }


        @Override
        public void endContext() throws IllegalStateException {
            if (restored) {
                throw new IllegalStateException("already restored");
            }
            StringContext.set(contextToRestore);
            restored = true;
        }
    }


    public static class StringContext {

        private static final Logger LOG = System.getLogger(StringContext.class.getName());
        private static final ThreadLocal<String> local = new ThreadLocal<>();

        public static String get() {
            LOG.log(INFO, "get()");
            return local.get();
        }


        public static void set(final String value) {
            LOG.log(INFO, "set(value={0})", value);
            if (value == null) {
                local.remove();
            } else {
                local.set(value);
            }
        }
    }
}
