/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * DO NOT USE THIS CODE FOR TOMCAT!!!
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;

class SlowClient extends Thread {

    Socket s = null;
    static WebTest test;
    public SlowClient(String host,int port,WebTest test) {
        this.test = test;
        setDaemon(true);
        try {
            s = new Socket(host, port);
            start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            while (true) {
                final OutputStream out = s.getOutputStream();
                out.write(0);
                out.flush();
                try {
                    System.out.println(getName() + " waiting");
                    Thread.sleep(10000);
                } catch (InterruptedException i) {
                    i.printStackTrace();
                }
            }
        } catch(IOException e) {
            test.count.incrementAndGet();
        }
    }
}
