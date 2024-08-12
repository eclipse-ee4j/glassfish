/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

//----------------------------------------------------------------------------
//
// Description: Recovery lock file handling
// Author:      Marina Vatkina
// Date:        Sep 2010
//
//----------------------------------------------------------------------------

package com.sun.enterprise.transaction.jts.recovery;

import com.sun.enterprise.transaction.jts.api.DelegatedTransactionRecoveryFence;
import com.sun.enterprise.transaction.jts.api.TransactionRecoveryFence;
import com.sun.jts.CosTransactions.Configuration;
import com.sun.jts.CosTransactions.LogControl;
import com.sun.jts.CosTransactions.RecoveryManager;
import com.sun.logging.LogDomains;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * This class manages lock file required for delegated recovery.
 *
 * <p>
 * Records in the recovery lock file have the following format:
 *
 * <p>
 * <code>PREFIX INSTANCE_NAME TIMESTAMP</code>
 *
 * <p>
 * Where <code>PREFIX</code> can be one of:
 * <ul>
 *  <li> "O" means OWNED by this instance, i.e. non-delegated recovery
 *  <li> "B" means recovered BY the specified instance
 *  <li> "F" means recovered FOR the specified instance
 * </ul>
 *
 * <p>
 * <code>TIMESTAMP</code> is the time of the recovery operation
 *
 * @author mvatkina
 *
 */
public class RecoveryLockFile implements TransactionRecoveryFence, DelegatedTransactionRecoveryFence {

    // Logger to log transaction messages = use class from com.sun.jts sub-package to find the bundle
    static Logger _logger = LogDomains.getLogger(Configuration.class, LogDomains.TRANSACTION_LOGGER);

    private final static String SEPARATOR = " ";
    private final static String OWN = "O";
    private final static String FOR = "F";
    private final static String BY = "B";
    private final static String END_LINE = "\n";

    // Single instance
    private static final RecoveryLockFile instance = new RecoveryLockFile();

    private volatile boolean started = false;
    private String instance_name;
    private String log_path;
    private GMSCallBack gmsCallBack;

    private RecoveryLockFile() {
    }

    public static DelegatedTransactionRecoveryFence getDelegatedTransactionRecoveryFence(GMSCallBack gmsCallBack) {
        instance.init(gmsCallBack);

        return instance;
    }

    @Override
    public void start() {
        if (!started) {
            gmsCallBack.finishDelegatedRecovery(log_path);
            started = true;
        }
    }

    private void init(GMSCallBack gmsCallBack) {
        this.gmsCallBack = gmsCallBack;
        instance_name = Configuration.getPropertyValue(Configuration.INSTANCE_NAME);
        log_path = LogControl.getLogPath();
        // Create (if it doesn't exist) recoveryLockFile to hold info about instance and delegated recovery
        File recoveryLockFile = LogControl.recoveryLockFile(null, log_path);
        try {
            recoveryLockFile.createNewFile();
        } catch (Exception ex) {
            _logger.log(WARNING, "jts.exception_creating_recovery_file", recoveryLockFile);
            _logger.log(WARNING, "", ex);
        }
        RecoveryManager.registerTransactionRecoveryFence(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void raiseFence() {
        while (isRecovering()) {
            // wait
            try {
                Thread.sleep(60000);
            } catch (Exception e) {
            }
        }
        registerRecovery();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void lowerFence() {
        _logger.log(INFO, "Lower Fence request for instance " + instance_name);
        doneRecovering();
        _logger.log(INFO, "Fence lowered for instance " + instance_name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFenceRaised(String logDir, String instance, long timestamp) {
        return isRecovering(logDir, instance, timestamp, BY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void raiseFence(String logPath, String instance) {
        raiseFence(logPath, instance, 0L);
    }

    /**
     * {@inheritDoc}
     */
    public void raiseFence(String logPath, String instance, long timestamp) {
        _logger.log(INFO, "Raise Fence request for instance " + instance);
        while (isRecovering(logPath, instance, timestamp, BY)) {
            // wait
            try {
                Thread.sleep(60000);
            } catch (Exception e) {
            }
        }
        registerRecovery(logPath, instance);
        _logger.log(INFO, "Fence raised for instance " + instance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void lowerFence(String logPath, String instance) {
        _logger.log(INFO, "Lower Fence request for instance " + instance);
        doneRecovering(logPath, instance);
        _logger.log(INFO, "Fence lowered for instance " + instance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInstanceRecoveredFor(String path, long timestamp) {
        if (!isRecovering(path, null, timestamp, FOR)) {
            return doneRecovering(path, null, FOR);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transferRecoveryTo(String logDir, String instance) {
        doneRecovering(logDir, null, BY);
        registerRecovery(logDir, instance);
    }

    /**
     * Returns true if running instance is doing its own recovery
     */
    private boolean isRecovering() {
        return isRecovering(log_path, instance_name, 0L, BY);
    }

    /**
     * Returns true if recovery file on the specified path contains information that the specified instance started recovery
     * after specified timestamp either for itself or by another instance.
     */
    private boolean isRecovering(String logDir, String instance, long timestamp, String prefix) {
        BufferedReader reader = null;
        File recoveryLockFile = LogControl.recoveryLockFile(".", logDir);
        if (!recoveryLockFile.exists()) {
            _logger.log(INFO, "Lock File not found " + recoveryLockFile);
            return false;
        }

        boolean result = false;
        _logger.log(INFO, "Checking Lock File " + recoveryLockFile);
        try (RandomAccessFile raf = new RandomAccessFile(recoveryLockFile, "rw")) {
            FileLock lock = raf.getChannel().lock();
            try {
                reader = new BufferedReader(new FileReader(recoveryLockFile));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    _logger.log(INFO, "Testing line: " + line);
                    String[] parts = line.split(SEPARATOR);
                    if (parts.length != 3) {
                        throw new IllegalStateException();
                    } else if ((parts[0].equals(OWN) && parts[1].equals(instance)) || (instance == null && parts[0].equals(prefix))) {
                        result = (Long.parseLong(parts[2]) > timestamp);
                        break;
                    } else {
                        // skip all other lines
                        continue;
                    }
                }
            } finally {
                lock.release();
            }
        } catch (Exception ex) {
            _logger.log(WARNING, "jts.exception_in_recovery_file_handling", ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {
                    _logger.log(WARNING, "jts.exception_in_recovery_file_handling", ex);
                }
            }
        }

        _logger.log(INFO, "Recovering? " + result);
        return result;
    }

    /**
     * Removes recovery data from the recovery lock file for running instance
     */
    private void doneRecovering() {
        doneRecovering(log_path, instance_name, OWN);
    }

    /**
     * Removes recovery data from the recovery lock files for both, the instance that the recovery is done for (i.e. for
     * specified instance), and the current instance which the recovery is done by (in the lock file on the specified path)
     */
    private void doneRecovering(String logPath, String instance) {
        doneRecovering(log_path, instance, FOR);
        doneRecovering(logPath, instance_name, BY);
    }

    /**
     * Removes recovery data from the recovery lock file.
     *
     * @return instance name if instance was unknown (null).
     */
    private String doneRecovering(String logPath, String instance, String prefix) {
        BufferedReader reader = null;
        FileWriter writer = null;
        String result = null;
        File recoveryLockFile = LogControl.recoveryLockFile(".", logPath);
        if (!recoveryLockFile.exists()) {
            _logger.log(INFO, "Lock Fine not found: " + recoveryLockFile);
            return null;
        }

        try (RandomAccessFile raf = new RandomAccessFile(recoveryLockFile, "rw")) {
            FileLock lock = raf.getChannel().lock();
            try {
                reader = new BufferedReader(new FileReader(recoveryLockFile));
                _logger.log(INFO, "Updating File " + recoveryLockFile);
                String line = null;
                List<String> list_out = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    _logger.log(INFO, "Processing line: " + line);
                    String[] parts = line.split(SEPARATOR);
                    if (parts.length != 3) {
                        // Remove such line
                        _logger.log(INFO, "...skipping bad line ...");
                        continue;
                    } else if (parts[0].equals(prefix) && (instance == null || parts[1].equals(instance))) {
                        // Remove such line
                        _logger.log(INFO, "...skipping found line ...");
                        result = parts[1];
                        continue;
                    }

                    list_out.add(line);
                }

                reader.close();
                reader = null;

                writer = new FileWriter(recoveryLockFile);
                for (String out : list_out) {
                    _logger.log(INFO, "Re-adding line: " + out);
                    writer.write(out);
                    writer.write(END_LINE);
                }
            } finally {
                lock.release();
            }
        } catch (Exception ex) {
            _logger.log(WARNING, "jts.exception_in_recovery_file_handling", ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {
                    _logger.log(WARNING, "jts.exception_in_recovery_file_handling", ex);
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ex) {
                    _logger.log(WARNING, "jts.exception_in_recovery_file_handling", ex);
                }
            }
        }

        return result;
    }

    /**
     * Writes into recovery lock file data about recovery for the running instance.
     */
    private void registerRecovery() {
        // Remove any stale data
        doneRecovering(log_path, null, BY);

        // And mark that it's self-recovery
        registerRecovery(log_path, instance_name, OWN);
    }

    /**
     * Writes into recovery lock file data about recovery for the specified instance by the current instance.
     */
    private void registerRecovery(String logPath, String instance) {
        // Remove stale data if there is any
        doneRecovering(log_path, null, FOR);

        registerRecovery(logPath, instance_name, BY);
        registerRecovery(log_path, instance, FOR);
    }

    /**
     * Writes data into recovery lock file on the specified path
     */
    private void registerRecovery(String logPath, String instance, String prefix) {
        FileWriter writer = null;
        File recoveryLockFile = LogControl.recoveryLockFile(".", logPath);
        if (!recoveryLockFile.exists()) {
            _logger.log(INFO, "Lock File not found " + recoveryLockFile);
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(recoveryLockFile, "rw")) {
            FileLock lock = raf.getChannel().lock();
            try {
                writer = new FileWriter(recoveryLockFile, true);
                _logger.log(INFO, "Writing into file " + recoveryLockFile);
                StringBuffer b = (new StringBuffer())
                        .append(prefix).append(SEPARATOR)
                        .append(instance).append(SEPARATOR)
                        .append(System.currentTimeMillis()).append(END_LINE);
                _logger.log(INFO, "Storing " + b);
                writer.write(b.toString());
            } finally {
                lock.release();
            }
        } catch (Exception ex) {
            _logger.log(WARNING, "jts.exception_in_recovery_file_handling", ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ex) {
                    _logger.log(WARNING, "jts.exception_in_recovery_file_handling", ex);
                }
            }
        }
    }

}
