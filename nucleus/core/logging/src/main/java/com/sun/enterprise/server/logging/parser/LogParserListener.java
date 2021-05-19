/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.server.logging.parser;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * @author sanshriv
 *
 */
public interface LogParserListener {

    /**
     * Invoked when the parser parses a log record from an input stream.
     * @param object
     */
    public void foundLogRecord(long position, ParsedLogRecord object);

    /**
     * Invoked to output the summary after all the records have been parsed.
     * @param writer
     * @param objects
     */
    public void outputSummary(BufferedWriter writer, Object ... objects) throws IOException;

    /**
     * Release any resources that were acquired during the initialization.
     */
    public void close() throws IOException;

}
