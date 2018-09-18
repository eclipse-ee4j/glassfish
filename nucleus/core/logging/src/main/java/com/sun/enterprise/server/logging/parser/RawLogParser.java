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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;

import com.sun.enterprise.server.logging.LogFacade;

/**
 * @author sandeep.shrivastava
 *
 */
public class RawLogParser implements LogParser {

    private String streamName;
    
    public RawLogParser(String name) {
        streamName = name;
    }

    @Override
    public void parseLog(BufferedReader reader, LogParserListener listener)
            throws LogParserException {
        try {
            String line = null;
            long position = 0L;
            while ((line = reader.readLine()) != null) {
                ParsedLogRecord record = new ParsedLogRecord(line);
                record.setFieldValue(ParsedLogRecord.LOG_MESSAGE, line);
                listener.foundLogRecord(position, record);
                position++;
            }
        } catch(IOException e){
            throw new LogParserException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LogFacade.LOGGING_LOGGER.log(Level.FINE, "Got exception while clsoing reader "+ streamName, e); 
                }
            }
        }                

    }

}
