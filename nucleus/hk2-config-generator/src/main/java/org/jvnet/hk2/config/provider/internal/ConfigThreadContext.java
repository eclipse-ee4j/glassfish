/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config.provider.internal;

/**
 * @author jwells
 *
 */
public class ConfigThreadContext {

    private static final ThreadLocal<ConfigThreadContext> threadLocalContext = new ThreadLocal<>();

    /**
     * Performs a runnable action
     */
    public static void captureACCandRun(Runnable runnable) {
      ConfigThreadContext threadContext = threadLocalContext.get();
      if (threadContext != null) {
        // caller's original context already set
        runnable.run();
        return;
      }

      boolean created = (null == threadContext);
      if (created) {
        threadContext = new ConfigThreadContext();
        threadLocalContext.set(threadContext);
      }

      try {
        runnable.run();
      } finally {
        if (created) {
          threadLocalContext.set(null);
        }
      }
    }
}
