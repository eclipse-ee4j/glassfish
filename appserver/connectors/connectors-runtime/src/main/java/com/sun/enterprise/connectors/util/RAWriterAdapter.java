/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors.util;

import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writer that will be given to / used by MCF, MCs of resource adapter<br>
 * PrintWriter will be set during MCF initialization<br>
 */
public class RAWriterAdapter extends Writer {
    private Logger logger;
    //by default, autoFlush will be ON.
    private boolean autoFlush = true;
    //buffer used when autoFlush is OFF
    private StringBuffer log;

    public RAWriterAdapter(Logger logger) {
        this.logger = logger;
        initializeAutoFlush();
    }

    private void initializeAutoFlush() {
        String autoFlushValue = System.getProperty("com.sun.enterprise.connectors.LogWriterAutoFlush", "true");
        autoFlush = Boolean.valueOf(autoFlushValue);
    }

    @Override
    public void write(char cbuf[], int off, int len) {
        if (autoFlush) {
            logger.log(Level.INFO, new String(cbuf, off, len));
        } else {
            String s = new String(cbuf, off, len);
            if (log == null) {
                log = new StringBuffer(s);
            } else {
                log = log.append(s);
            }
        }
    }

    @Override
    public void flush() {
        if (!autoFlush) {
            logger.log(Level.INFO, log.toString());
            log = null;
        }
    }

    @Override
    public void close() {
        //no-op
    }
}
