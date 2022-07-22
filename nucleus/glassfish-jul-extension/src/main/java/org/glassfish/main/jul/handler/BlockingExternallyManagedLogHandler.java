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

package org.glassfish.main.jul.handler;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.glassfish.main.jul.GlassFishLogManager;


/**
 * A very special {@link Handler} useful to preinitialize the {@link GlassFishLogManager} but block
 * {@link LogRecord} processing until it is explicitly reconfigured.
 *
 * @author David Matejcek
 */
public class BlockingExternallyManagedLogHandler extends Handler implements ExternallyManagedLogHandler {

    @Override
    public boolean isReady() {
        return false;
    }


    @Override
    public void publish(LogRecord record) {
        // nothing
    }


    @Override
    public void flush() {
        // nothing
    }


    @Override
    public void close() throws SecurityException {
        // nothing
    }
}
