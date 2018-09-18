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

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

public class LogParserTest {

    public static final String UNIFORM_SERVER_LOG_FILE = "uniform-server.log";

    public static final String ODL_SERVER_LOG_FILE = "odl-server.log";

    public static final int ULF_EXPECTED_REC_COUNT = 138;

    public static final int ODL_EXPECTED_REC_COUNT = 45;

    @Test
    public void testUniformLogFormatParser() throws Exception {
        System.out.println("Starting test testUniformLogFormatParser");
        InputStream in = LogParserTest.class.getResourceAsStream(UNIFORM_SERVER_LOG_FILE);        
        LogParser parser = new UniformLogParser(UNIFORM_SERVER_LOG_FILE);
        LogParserListenerImpl listener = new LogParserListenerImpl();
        parser.parseLog(new BufferedReader(new InputStreamReader(in)), listener); 
        System.out.println("Found " + listener.count + " records in log file.");
        assertEquals(ULF_EXPECTED_REC_COUNT,listener.count);
        System.out.println("Test passed, found " + listener.count + " records as expected.");
    }

    @Test
    public void testODLLogFormatParser() throws Exception {
        System.out.println("Starting test testODLLogFormatParser");
        InputStream in = LogParserTest.class.getResourceAsStream(ODL_SERVER_LOG_FILE);        
        LogParser parser = new ODLLogParser(ODL_SERVER_LOG_FILE);
        LogParserListenerImpl listener = new LogParserListenerImpl();
        parser.parseLog(new BufferedReader(new InputStreamReader(in)), listener); 
        System.out.println("Found " + listener.count + " records in log file.");
        assertEquals(ODL_EXPECTED_REC_COUNT,listener.count);
        System.out.println("Test passed, found " + listener.count + " records as expected.");
    }

    private static final class LogParserListenerImpl implements LogParserListener {

        int count = 0;
        
        @Override
        public void foundLogRecord(long pos, ParsedLogRecord object) {
            count++;    
        }

        @Override
        public void outputSummary(BufferedWriter writer, Object... objects)
                throws IOException {            
        }
        
        @Override
        public void close() throws IOException {            
        }
        
    }
}
