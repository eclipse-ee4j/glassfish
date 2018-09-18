/*
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

package admin;

import java.io.*;
import java.net.*;

/**
 *
 * @author bnevins
 */
class FakeServer implements Runnable{

    public static void main(String[] args) {
        int port = -1;
        try {
            port = Integer.parseInt(args[0]);
        }
        catch (Exception e) {
            System.out.println("USAGE:  FakeServer port-number");
            System.exit(1);
        }
        FakeServer fakeServer = new FakeServer(port);
        fakeServer.run();
    }


    FakeServer(int port) {
        this.port = port;
    }

    public void run() {
        try {
            while (true) {
                ServerSocket listener = new ServerSocket(port);
                Socket server = listener.accept();
            }
        }
        catch (IOException ioe) {
            System.out.println("IOException: " + ioe);
            ioe.printStackTrace();
        }

    }
    private final int port;
}
