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

package org.glassfish.common.util.admin.locking;

import org.glassfish.common.util.admin.ManagedFile;
import org.junit.Ignore;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

/**
 * Standalone test to hold a read or write lock.
 *
 * usage java cp ... TestClient [ read | write ]
 *
 * By default a read lock is obtained.
 *
 */
@Ignore
public class TestClient {

    public static void main(String[] args) {
        FileLockTest test = new FileLockTest();
        byte bytes[] = new byte[100];
        String mode = "read";
        if (args.length>0) {
            mode = args[0];
        }
        try {
            File f = test.getFile();
            ManagedFile managed = new ManagedFile(f, -1, -1);
            Lock lock = null;
            try {
                if (mode.equals("read")) {
                    lock = managed.accessRead();
                } else
                if (mode.equals("write")) {
                    lock = managed.accessWrite();
                } else {
                    //System.out.println("usage : TestClient [ read | write ]. Invalid option : " + mode);
                    return;
                }

            } catch (TimeoutException e) {
                e.printStackTrace();
                return;
            }
            //System.out.println("I have the lock in "+ mode +" mode, press enter to release ");
            System.in.read(bytes);
            lock.unlock();
            //System.out.println("released");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return;
        }

    }
}
