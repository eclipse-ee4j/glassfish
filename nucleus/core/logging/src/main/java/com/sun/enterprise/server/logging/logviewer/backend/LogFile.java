/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.server.logging.logviewer.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.sun.enterprise.server.logging.LogFacade;
import com.sun.enterprise.server.logging.parser.LogParser;
import com.sun.enterprise.server.logging.parser.LogParserFactory;
import com.sun.enterprise.server.logging.parser.LogParserListener;
import com.sun.enterprise.server.logging.parser.ParsedLogRecord;


/**
 * <P>This class encapsulates the log file so that its details are not
 * exposed.  "getLongEntries" returns an unfiltered List of LogEntry objects
 * from the requested record number.  It will always search forward.
 * getIndexSize() returns the number of records between each index.
 * getLastIndexNumber returns the last index.</P>
 *
 * @AUTHOR: Hemanth Puttaswamy and Ken Paulsen
 * <p/>
 * <P>This class also contains an inner class for storing LogEntry
 * objects.</P>
 */
public class LogFile implements java.io.Serializable {

    private static final long serialVersionUID = -2960142541274652618L;

    private final long _indexSize = 10;
    private final String _logFileName;
    private final List<Long> _recordIdx = new ArrayList<>();

    /**
     * Constructor
     */
    public LogFile(String name) {
        _logFileName = name;
        _recordIdx.add(0L);
    }

    /**
     * This method returns up to _indexSize records starting with the given
     * record number.
     *
     * @param    startingRecord    The starting point to search for LogEntries
     */
    public List getLogEntries(long startingRecord) {
        return getLogEntries(startingRecord, getIndexSize());
    }

    /**
     * This method returns up to _indexSize records starting with the given
     * record number.  It will return up to "maxRecords" records.
     *
     * @param    startingRecord    The starting point to search for LogEntries
     * @param    maxRecords    The maximum number of records to return
     */
    public List<LogEntry> getLogEntries(final long startingRecord, final long maxRecords) {
        if (startingRecord < 0) {
            return null;
        }

        // Open the file at the desired starting Record
        final long recordsToIgnore = (startingRecord % getIndexSize());
        try (BufferedReader reader = getFilePosition(startingRecord - recordsToIgnore)) {
            final List<LogEntry> results = new ArrayList<>();
            if (reader == null) {
                return results;
            }

            File logFile = new File(getLogFileName());
            LogParser logParser = LogParserFactory.getInstance().createLogParser(logFile );
            logParser.parseLog(reader, new LogParserListener() {

                long counter = 0;


                @Override
                public void foundLogRecord(long position, ParsedLogRecord logRecord) {
                    counter++;
                    if (counter <= recordsToIgnore) {
                        return;
                    }
                    if (results.size() < maxRecords) {
                        LogEntry entry = new LogEntry(startingRecord + results.size());
                        entry.setLoggedDateTime(logRecord.getTimestamp());
                        entry.setLoggedLevel(logRecord.getLevel());
                        entry.setLoggedLoggerName(logRecord.getLogger());
                        entry.setLoggedMessage(logRecord.getMessage());
                        entry.setLoggedNameValuePairs(logRecord.getSupplementalAttributes().toString());
                        entry.setLoggedProduct(logRecord.getProductId());
                        entry.setMessageId(logRecord.getMessageKey());
                        results.add(entry);
                    }
                }
            });
            return results;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }


    /**
     * This method builds the file index in the beginning.  The index is for
     * the beginning of every record after the size specified by '_indexSize'
     * variable.
     */
    private synchronized void buildLogFileIndex() {
        // Open the file and skip to the where we left off
        final long startPos = _recordIdx.get(_recordIdx.size() - 1);
        final long localIndexSize = getIndexSize();
        try (BufferedReader reader = getLogFileReader(startPos)) {
            File logFile = new File(getLogFileName());
            LogParser logParser = LogParserFactory.getInstance().createLogParser(logFile);
            if (logParser != null) {
                logParser.parseLog(reader, new LogParserListener() {

                    long recordNumber = (_recordIdx.size() - 1) * localIndexSize;

                    @Override
                    public void foundLogRecord(long position, ParsedLogRecord object) {
                        long modIndex = recordNumber % localIndexSize;
                        if (modIndex == 0) {
                            _recordIdx.add(startPos+position);
                        }
                        recordNumber++;
                    }
                });
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * This method returns the file position given the record number.
     *
     * @return The file position.
     * @param    recordNumber    The Record Number
     */
    private BufferedReader getFilePosition(long recordNumber) {
        // The index is stored from the second slot. i.e., if there
        // are 100 records and the index will be on 20, 40, 60, 80, 100
        // if the _indexSize is 20. We don't store '0' hence we subtract
        // from 1 to get the right index
        int index = (int) (recordNumber / getIndexSize());
        if (index > _recordIdx.size()-1) {
            return null;
        }
        Long filePosition = _recordIdx.get(index);
        return getLogFileReader(filePosition);
    }

    /**
     * This method opens the server.log file and moves the stream to
     * the specified filePosition.
     */
    protected BufferedReader getLogFileReader(long fromFilePosition) {
        FileInputStream file = null;
        try {
            file = new FileInputStream(getLogFileName());
            long bytesToSkip = fromFilePosition-1;
            if (bytesToSkip > 0) {
                long bytesSkipped = file.skip(bytesToSkip);
                if (bytesSkipped != fromFilePosition) {
                    if (LogFacade.LOGGING_LOGGER.isLoggable(Level.FINE)) {
                        LogFacade.LOGGING_LOGGER.log(Level.FINE, "Did not skip exact bytes while positioning reader in " + getLogFileName());
                    }
                }
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(file));
            return reader;
        } catch (Exception ex) {
            if (LogFacade.LOGGING_LOGGER.isLoggable(Level.FINE)) {
                LogFacade.LOGGING_LOGGER.log(Level.FINE, "Error reading from file: " + getLogFileName(), ex);
            }
            if (file != null) {
                try {
                    file.close();
                } catch (Exception ex2) {
                    if (LogFacade.LOGGING_LOGGER.isLoggable(Level.FINE)) {
                        LogFacade.LOGGING_LOGGER.log(Level.FINE, "Error closing file: " + getLogFileName(), ex2);
                    }
                }
            }
        }
        return null;
    }


    public String getLogFileName() {
        return _logFileName;
    }

    /**
     * The log records are indexed, this method returns the last index.  It
     * will ensure that the indexes are up-to-date.
     */
    public long getLastIndexNumber() {
        buildLogFileIndex();
        return _recordIdx.size() - 1;
    }


    public long getIndexSize() {
        return _indexSize;
    }

    /**
     * Class to manage LogEntry information
     */
    public static class LogEntry implements Serializable {

        private static final long serialVersionUID = 1067284334612090072L;

        private long recordNumber;
        private OffsetDateTime loggedDateTime;
        private String loggedLevel;
        private String loggedProduct;
        private String loggedLoggerName;
        private String loggedNameValuePairs;
        private String loggedMessage;
        private String messageId;

        public LogEntry(long recordNumber) {
            this.recordNumber = recordNumber;
        }


        public OffsetDateTime getLoggedDateTime() {
            return this.loggedDateTime;
        }


        public void setLoggedDateTime(OffsetDateTime loggedDateTime) {
            this.loggedDateTime = loggedDateTime;
        }


        public String getLoggedLevel() {
            return loggedLevel;
        }


        public void setLoggedLevel(String loggedLevel) {
            this.loggedLevel = loggedLevel;
        }


        public String getLoggedProduct() {
            return loggedProduct;
        }


        public void setLoggedProduct(String loggedProduct) {
            this.loggedProduct = loggedProduct;
        }


        public String getLoggedLoggerName() {
            return loggedLoggerName;
        }


        public void setLoggedLoggerName(String loggedLoggerName) {
            this.loggedLoggerName = loggedLoggerName;
        }


        public String getLoggedNameValuePairs() {
            return loggedNameValuePairs;
        }


        public void setLoggedNameValuePairs(String loggedNameValuePairs) {
            this.loggedNameValuePairs = loggedNameValuePairs;
        }


        public void setLoggedMessage(String message) {
            this.loggedMessage = message;
        }


        public void appendLoggedMessage(String message) {
            loggedMessage += message;
        }


        public String getLoggedMessage() {
            return loggedMessage;
        }


        public String getMessageId() {
            return messageId;
        }


        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }


        public long getRecordNumber() {
            return recordNumber;
        }


        public void setRecordNumber(long recordNumber) {
            this.recordNumber = recordNumber;
        }


        @Override
        public String toString() {
            return getRecordNumber() + ":" + getLoggedMessage();
        }
    }
}
