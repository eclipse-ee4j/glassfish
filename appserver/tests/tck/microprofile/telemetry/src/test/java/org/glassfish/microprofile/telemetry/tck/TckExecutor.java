/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.glassfish.microprofile.telemetry.tck;

import java.util.concurrent.Executor;

/**
 * Executor used by the asynchronous tests of the Tracing TCK, see the TCK README.
 */
public class TckExecutor implements Executor {

    @Override
    public void execute(Runnable command) {
        Thread thread = new Thread(command, "telemetry-tck-executor");
        thread.setDaemon(true);
        thread.start();
    }
}
