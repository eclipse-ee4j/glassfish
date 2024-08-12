/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.commands;

import com.sun.enterprise.util.i18n.StringManager;

import java.util.Collections;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.LoggingMXBean;

/** Provides the logging information of all the loggers registered in the VM.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3
 */
public class LogReporter {

    private final StringManager sm    = StringManager.getManager(LogReporter.class);
    private final String ROOT_LOGGER  = "root";
    private final String ANON_LOGGER  = "anonymous";

    public String getLoggingReport() throws RuntimeException {
        try {
           StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
            LoggingMXBean lb = LogManager.getLoggingMXBean();
            List<String> loggers = lb.getLoggerNames();
            Collections.sort(loggers);
            String lf = System.getProperty("java.util.logging.config.file");
            sb.append(sm.getString("logging.config.file", lf));
            sb.append(sm.getString("reg.loggers", loggers.size()));
            sb.append(sm.getString("logger.details.1"));
            sb.append(sm.getString("logger.details.2"));
            sb.append(sm.getString("list.of.loggers"));
            sb.append("--------------------------------------------------");
            for (String logger : loggers) {
                String ln = (logger == null) ? ANON_LOGGER : logger;
                String parent = lb.getParentLoggerName(logger);
                if (parent == null || parent.length() == 0)
                    parent = ROOT_LOGGER;
                sb.append(ln + "|" + lb.getLoggerLevel(logger) + "|" + parent);
            }
            return (sb.toString());
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
