/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.common.util.logging;

public class LoggingPropertyNames {

    public static final String GFFileHandler = "com.sun.enterprise.server.logging.GFFileHandler.";

    public static final String SyslogHandler = "com.sun.enterprise.server.logging.SyslogHandler.";

    public static final String logRotationLimitInBytes = GFFileHandler + "rotationLimitInBytes";

    public static final String logRotationTimelimitInMinutes = GFFileHandler + "rotationTimelimitInMinutes";

    public static final String file = GFFileHandler + "file";

    public static final String logFormatter = GFFileHandler + "formatter";

    public static final String logHandler = "handlers";

    public static final String useSystemLogging = SyslogHandler + "useSystemLogging";

    public static final String retainErrorStatisticsForHours = GFFileHandler + "retainErrorsStasticsForHours";

    public static final String logFilter = GFFileHandler + "logFilterClass";

    public static final String logToConsole = GFFileHandler + "logtoConsole";

    public static final String alarms = GFFileHandler + "alarms";


    public static final String MAX_QUEUE_SIZE = GFFileHandler + "maxQueueSize";

    public static final String QUEUE_FLUSH_FREQUENCY = GFFileHandler + "queueFlushFrequency";

}

