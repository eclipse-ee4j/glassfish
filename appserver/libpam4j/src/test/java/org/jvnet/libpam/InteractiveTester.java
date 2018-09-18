/*
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

package org.jvnet.libpam;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class InteractiveTester extends TestCase {
    public InteractiveTester(String testName) {
        super(testName);
    }

    public void testPositiveCase() throws Exception {
        for (int i=0; i<1000; i++)
            testOne();
    }

    public void testOne() throws Exception {
        UnixUser u = new PAM("sshd").authenticate(System.getProperty("user.name"), System.getProperty("password"));
        if(!printOnce) {
            System.out.println(u.getUID());
            System.out.println(u.getGroups());
            printOnce = true;
        }
    }

    public void testGetGroups() throws Exception {
        System.out.println(new PAM("sshd").getGroupsOfUser(System.getProperty("user.name")));
    }

    public void testConcurrent() throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(10);
        Set<Future<?>> result = new HashSet<Future<?>>();
        for( int i=0; i<1000; i++ ) {
            result.add(es.submit(new Callable<Object>() {
                public Object call() throws Exception {
                    testOne();
                    return null;
                }
            }));
        }
        // wait for completion
        for (Future<?> f : result) {
            f.get();
        }
        es.shutdown();
    }

    public void testNegative() throws Exception {
        try {
            new PAM("sshd").authenticate("bogus","bogus");
            fail("expected a failure");
        } catch (PAMException e) {
            // yep
        }
    }

    public static void main(String[] args) throws Exception {
        UnixUser u = new PAM("sshd").authenticate(args[0], args[1]);
        System.out.println(u.getUID());
        System.out.println(u.getGroups());
        System.out.println(u.getGecos());
        System.out.println(u.getDir());
        System.out.println(u.getShell());
    }

    private boolean printOnce;
}
