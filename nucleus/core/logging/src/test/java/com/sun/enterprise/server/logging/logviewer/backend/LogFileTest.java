/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.server.logging.logviewer.backend;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.junit.Test;

import com.sun.enterprise.server.logging.logviewer.backend.LogFile.LogEntry;

/**
 * 
 * @author sanshriv
 *
 */
public class LogFileTest {

    @Test
    public void testLogEntryDeserialization() throws IOException, ClassNotFoundException {   
        ObjectInputStream objectInput = new ObjectInputStream(
                LogFileTest.class.getResource("logentry.bin").openStream());
        // Create and initialize a LogEntry from binary file
        LogFile.LogEntry entry = (LogEntry) objectInput.readObject(); 
        
        System.out.println("DateTime=" + entry.getLoggedDateTime());
        System.out.println("Level=" + entry.getLoggedLevel());
        System.out.println("Logger=" + entry.getLoggedLoggerName());
        System.out.println("Message=" + entry.getLoggedMessage());
        System.out.println("NameValuePairs=" + entry.getLoggedNameValuePairs());
        System.out.println("Product=" + entry.getLoggedProduct());
        System.out.println("MessageId=" + entry.getMessageId());
        System.out.println("RecordNumber=" + entry.getRecordNumber());
        
        objectInput.close();
    }

}
