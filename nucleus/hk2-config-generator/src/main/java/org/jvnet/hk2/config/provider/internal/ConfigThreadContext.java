/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import java.security.AccessControlContext;
import java.security.AccessController;

/**
 * @author jwells
 *
 */
public class ConfigThreadContext {
    private static final ThreadLocal<ConfigThreadContext> tlc = new ThreadLocal<>();

    private AccessControlContext acc;

    /**
     * Performs a runnable action while managing the callers AccessControlContext in thread local storage
     */
    public static void captureACCandRun(Runnable runnable) {
      ConfigThreadContext ts = tlc.get();
      if (null != ts && null != ts.acc) {
        // caller's original context already set
        runnable.run();
        return;
      }

      boolean created = (null == ts);
      if (created) {
        ts = new ConfigThreadContext();
        tlc.set(ts);
      }

      ts.acc = AccessController.getContext();

      try {
        runnable.run();
      } finally {
        ts.acc = null;
        if (created) {
          tlc.set(null);
        }
      }
    }
}
