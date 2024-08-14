/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.inject.Inject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Formatter;

import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

/**
 * A factory for FileLoggerHandler.
 *
 * @author Shing Wai Chan
 */
@Service
public class FileLoggerHandlerFactory implements PostConstruct {

    @Inject @Optional
    private Formatter logFormatter;

    private static ConcurrentMap<String, FileLoggerHandler> map =
        new ConcurrentHashMap<String, FileLoggerHandler>();

    public void postConstruct() {
    }

    public synchronized FileLoggerHandler getHandler(String logFile) {
        FileLoggerHandler handler = map.get(logFile);
        if (handler == null) {
            handler = new FileLoggerHandler(logFile);
            if (logFormatter != null) {
                handler.setFormatter(logFormatter);
            }
            map.put(logFile, handler);
        }

        return handler;
    }

    public synchronized void removeHandler(String logFile) {
        map.remove(logFile);
    }
}
