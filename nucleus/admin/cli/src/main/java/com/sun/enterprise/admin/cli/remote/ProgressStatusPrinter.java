/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.cli.remote;

import com.sun.enterprise.admin.progress.ProgressStatusClient;
import com.sun.enterprise.admin.remote.sse.GfSseInboundEvent;
import com.sun.enterprise.util.StringUtils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.AdminCommandEventBroker.AdminCommandListener;
import org.glassfish.api.admin.CommandProgress;
import org.glassfish.api.admin.progress.ProgressStatusDTO;
import org.glassfish.api.admin.progress.ProgressStatusEvent;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;

import static java.util.logging.Level.WARNING;
import static org.glassfish.api.admin.CommandProgress.EVENT_PROGRESSSTATUS_CHANGE;
import static org.glassfish.api.admin.CommandProgress.EVENT_PROGRESSSTATUS_STATE;

/**
 * Prints ProgressStatus changes to given logger
 *
 * @author mmares
 */
public class ProgressStatusPrinter implements AdminCommandListener<GfSseInboundEvent> {

    private class Ticker extends Thread {

        private final long pause;
        private volatile boolean stop;

        public Ticker(long pause) {
            this.pause = pause;
            this.setDaemon(true);
        }

        public void stopit() {
            this.stop = true;
        }

        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(pause);
                } catch (InterruptedException ex) {
                }
                if (stop) {
                    return;
                }
            } while (spin());
        }

    }

    /** Event name for printing user messages */
    public static final String USER_MESSAGE_NAME = "usermessage";

    private static final String CONTENT_TYPE = "application/json";

    private static final LocalStringsImpl strings = new LocalStringsImpl(ProgressStatusPrinter.class);

    private static final char[] spinner = new char[] { '|', '/', '-', '\\' };

    private String lastMessage;
    private int lastMsgLength = 0;
    private boolean firstPrint = true;
    private int spinnerIndex = -1;
    private Ticker ticker = null;

    private ProgressStatusClient client = new ProgressStatusClient(null);
    private CommandProgress commandProgress;
    private final boolean disableAnimation;
    private final boolean debugOutput;
    private final Logger logger;

    /**
     * Construct new printer
     *
     * @param disableAnimation will print each message on new line and spinner as dots
     * @param debugOutput expect printing of other data together with progress. New line must be printed ASAP.
     */
    public ProgressStatusPrinter(boolean disableAnimation, boolean debugOutput, Logger logger) {
        this.disableAnimation = disableAnimation;
        this.debugOutput = debugOutput;
        if (logger == null) {
            this.logger = Logger.getLogger(ProgressStatusPrinter.class.getName());
        } else {
            this.logger = logger;
        }
    }

    private synchronized boolean spin() {
        if (commandProgress == null || !commandProgress.isSpinnerActive() || debugOutput || lastMsgLength <= 1) {
            return false;
        }
        if (spinnerIndex >= 0 && !disableAnimation) {
            System.out.print("\b\b");
            lastMsgLength -= 2;
        }
        spinnerIndex++;
        if (spinnerIndex >= spinner.length) {
            spinnerIndex = 0;
        }
        if (disableAnimation) {
            System.out.print('.');
            lastMsgLength++;
        } else {
            System.out.print(spinner[spinnerIndex]);
            System.out.print(' ');
            lastMsgLength += 2;
        }
        return true;
    }

    @Override
    public synchronized void onAdminCommandEvent(String name, GfSseInboundEvent event) {
        try {
            if (EVENT_PROGRESSSTATUS_STATE.equals(name)) {
                ProgressStatusDTO dto = event.getData(ProgressStatusDTO.class, CONTENT_TYPE);
                client.mirror(dto);
                commandProgress = (CommandProgress) client.getProgressStatus();
                if (StringUtils.ok(commandProgress.getName()) && !StringUtils.ok(commandProgress.getLastMessage())) {
                    commandProgress.progress(strings.getString("progressstatus.message.starting", "Starting"));
                }
                printCommandProgress();
            } else if (EVENT_PROGRESSSTATUS_CHANGE.equals(name)) {
                if (commandProgress == null) {
                    logger.log(WARNING, strings.get("progressstatus.event.applyerror", "Inapplicable progress status event"));
                    return;
                }
                ProgressStatusEvent pse = event.getData(ProgressStatusEvent.class, CONTENT_TYPE);
                client.mirror(pse);
                printCommandProgress();
            } else if (USER_MESSAGE_NAME.equals(name)) {
                String msg = event.getData();
                printUserMessage(msg);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, strings.get("progressstatus.event.parseerror", "Can not parse progress status event"), ex);
        }
    }

    private synchronized void printUserMessage(String message) {
        if (message == null) {
            return;
        }
        if (lastMsgLength > 0) {
            if (disableAnimation || debugOutput) {
                if (disableAnimation) {
                    System.out.println();
                }
                System.out.println(message);
                System.out.print(lastMessage);
                if (debugOutput) {
                    System.out.println();
                }
            } else {
                System.out.print('\r');
                System.out.println(message);
                int spaceCount = lastMsgLength - message.length();
                for (int i = 0; i < spaceCount; i++) {
                    System.out.print(' ');
                }
                System.out.println();
                System.out.print(lastMessage);
            }
        } else {
            System.out.println(message);
        }
    }

    private synchronized void printCommandProgress() {
        //Now print
        if (commandProgress != null) {
            String message = ProgressStatusClient.composeMessageForPrint(commandProgress);
            //Print
            if (StringUtils.ok(message) && !message.equals(lastMessage)) {
                if (disableAnimation || debugOutput) {
                    if (!firstPrint && !debugOutput) {
                        System.out.println();
                    }
                    System.out.print(message);
                    if (debugOutput) {
                        System.out.println();
                    }
                } else {
                    if (!firstPrint) {
                        System.out.print('\r');
                    }
                    System.out.print(message);
                    System.out.print(' ');
                    int spaceCount = lastMsgLength - message.length();
                    for (int i = 0; i < spaceCount; i++) {
                        System.out.print(' ');
                    }
                    for (int i = 0; i < spaceCount; i++) {
                        System.out.print('\b');
                    }
                    spinnerIndex = -1;
                }
                this.firstPrint = false;
                this.lastMsgLength = message.length();
                this.lastMessage = message;
            }
            //Change ticker
            if (!debugOutput) {
                if (commandProgress.isSpinnerActive()) {
                    if (this.ticker == null) {
                        this.ticker = new Ticker(disableAnimation ? 1500L : 500L);
                        this.ticker.start();
                    }
                } else {
                    if (this.ticker != null) {
                        this.ticker.stopit();
                        this.ticker = null;
                    }
                }
            }
        }
    }

    public synchronized void deleteLastMessage() {
        if (lastMsgLength <= 0) {
            return;
        }
        if (disableAnimation || debugOutput) {
            System.out.println();
        } else {
            System.out.print('\r');
            for (int i = 0; i < lastMsgLength; i++) {
                System.out.print(' ');
            }
            System.out.print('\r');
        }
        if (this.ticker != null) {
            this.ticker.stopit();
            this.ticker = null;
        }
        lastMsgLength = 0;
    }

    public synchronized void reset() {
        client = new ProgressStatusClient(null);
        commandProgress = null;
        lastMessage = null;
        lastMsgLength = 0;
        firstPrint = true;
        if (this.ticker != null) {
            this.ticker.stopit();
            this.ticker = null;
        }
    }

}
