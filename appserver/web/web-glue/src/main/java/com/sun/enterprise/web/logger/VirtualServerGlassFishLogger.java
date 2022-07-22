/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.web.logger;

import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.glassfish.main.jul.GlassFishLogger;


/**
 * Special {@link GlassFishLogger} used for some concrete virtual server.
 *
 * @author David Matejcek
 */
public final class VirtualServerGlassFishLogger extends GlassFishLogger {

    public VirtualServerGlassFishLogger(final String name, final ResourceBundle rb) {
        super(name);
        setResourceBundle(rb);
    }


    /**
     * Sets the thread id to current thread's id.
     */
    @Override
    public void log(final LogRecord record) {
        if (record.getResourceBundle() == null) {
            ResourceBundle bundle = getResourceBundle();
            if (bundle != null) {
                record.setResourceBundle(bundle);
            }
        }
        record.setThreadID((int) Thread.currentThread().getId());
        super.log(record);
    }


    @Override
    public synchronized void addHandler(Handler handler) {
        super.addHandler(handler);
        if (handler instanceof FileLoggerHandler) {
            ((FileLoggerHandler) handler).associate();
        }
    }


    @Override
    public synchronized void removeHandler(Handler handler) {
        if (!(handler instanceof FileLoggerHandler)) {
            super.removeHandler(handler);
        } else {
            boolean hasHandler = false;
            Handler[] hs = getHandlers();
            if (hs != null) {
                for (Handler h : hs) {
                    if (h == handler) {
                        hasHandler = true;
                        break;
                    }
                }
            }
            if (hasHandler) {
                super.removeHandler(handler);
                ((FileLoggerHandler) handler).disassociate();
            }
        }
    }
}
