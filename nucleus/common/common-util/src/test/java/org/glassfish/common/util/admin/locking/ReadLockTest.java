/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;

import org.glassfish.common.util.admin.ManagedFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for ManagedFile.accessRead() method
 * @author Jerome Dochez
 */
public class ReadLockTest {

    @Test
    public void readLock() throws Exception {

        File f = getFile();
        //System.out.println("trying the lock on " + f.getAbsolutePath());
        final ManagedFile managed = new ManagedFile(f, 1000, 1000);
        Lock fl = managed.accessRead();
        //System.out.println("Got the lock on " + f.getAbsolutePath());
        List<Future<Boolean>> results = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            results.add(Executors.newFixedThreadPool(2).submit(() -> {
                try {
                    Lock second = managed.accessRead();
                    second.unlock();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return Boolean.TRUE;
            }));
        }
        Thread.sleep(100);
        fl.unlock();
        for (Future<Boolean> result : results) {
            Boolean exitCode = result.get();
            assertTrue(exitCode.booleanValue());
        }
    }


    private File getFile() throws IOException {
        Enumeration<URL> urls = getClass().getClassLoader().getResources("adminport.xml");
        if (urls.hasMoreElements()) {
            try {
                File f = new File(urls.nextElement().toURI());
                if (f.exists()) {
                    return f;
                }
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        } else {
            //System.out.println("Not found !");
        }
        return null;
    }
}
