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

package com.sun.enterprise.server.logging;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
* Send a message via syslog.
*/
/**
 * this code is taken from spy.jar and enhanced
 * User: cmott
 */
public class Syslog {
    public static final int EMERG=0;
    public static final int ALERT=1;
    public static final int CRIT=2;
    public static final int ERR=3;
    public static final int WARNING=4;
    public static final int NOTICE=5;
    public static final int INFO=6;
    public static final int DEBUG=7;

    public static final int KERN = 0;
    public static final int USER = 8;
    public static final int MAIL = 16;
    public static final int DAEMON = 24;
    public static final int AUTH = 32;
    public static final int SYSLOG = 40;
    public static final int LPR = 48;
    public static final int NEWS = 56;
    public static final int UUCP = 64;
    public static final int CRON = 72;
    public static final int AUTHPRIV = 80;
    public static final int FTP = 88;
    public static final int LOCAL0 = 128;
    public static final int LOCAL1 = 136;
    public static final int LOCAL2 = 144;
    public static final int LOCAL3 = 152;
    public static final int LOCAL4 = 160;
    public static final int LOCAL5 = 168;
    public static final int LOCAL6 = 176;
    public static final int LOCAL7 = 184;

    private static final int SYSLOG_PORT=514;

    private final InetAddress addr;

    /**
     * Log to a particular log host.
     */
    public Syslog(String loghost) throws UnknownHostException {
      addr=InetAddress.getByName(loghost);
    }

    /**
     * Send a log message.
     */
    public void log(int facility, int level, String msg) {
      int fl=facility | level;

      String what="<" + fl + ">" + msg;
      // System.out.println("Writing to syslog:" + what);

      try {
        byte[] buf = what.getBytes();
        int len = buf.length;
        DatagramPacket dp = new DatagramPacket(buf,len,addr,SYSLOG_PORT);
        DatagramSocket s = new DatagramSocket();
        s.send(dp);
        if(!s.isClosed()) {
            s.close();
        }
      } catch(IOException e) {
        LogFacade.LOGGING_LOGGER.log(Level.SEVERE, LogFacade.ERROR_SENDING_SYSLOG_MSG, e);
      }
    }


}
