/*
 * Copyright (c) 2004, 2018 Oracle and/or its affiliates. All rights reserved.
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

package libclasspath2.client;

import java.io.IOException;
import jakarta.ejb.EJB;
import libclasspath2.ResourceHelper;
import libclasspath2.ejb.LookupSBRemote;

/**
 *
 * @author tjquinn
 */
public class LibDirTestClient {

    private static @EJB() LookupSBRemote lookup;

    /** Creates a new instance of LibDirTestClient */
    public LibDirTestClient() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new LibDirTestClient().run(args);
        } catch (Throwable thr) {
            thr.printStackTrace(System.err);
            System.exit(-1);
        }
    }

    private void run(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("result=-1");
            System.out.println("note=You must specify the expected result string as the command-line argument");
            System.exit(1);
        }
        String expected = args[0];

        /*
         *Ask the EJB to find the required resources and property values.
         */
        ResourceHelper.Result serverResult = lookup.runTests(args, ResourceHelper.TestType.SERVER);

        /*
         *Now try to get the same results on the client side.
         */
        StringBuilder clientResults = new StringBuilder();
        ResourceHelper.Result clientResult = ResourceHelper.checkAll(args, ResourceHelper.TestType.CLIENT);

        if (serverResult.getResult() && clientResult.getResult()) {
            System.out.println("result=0");
            System.out.println("note=Received expected results");
        } else {
            System.out.println("result=-1");
            dumpResults(serverResult.getResult(), serverResult.getResults().toString(), "server");
            dumpResults(clientResult.getResult(), clientResult.getResults().toString(), "client");
            System.exit(1);
        }
    }

    private void dumpResults(boolean result, String results, String whereDetected) {
        if ( ! result) {
            System.out.println("note=Error(s) on the " + whereDetected + ":");
            for (String error : results.split("@")) {
                System.out.println("note=  " + error);
            }
            System.out.println("note=");
        }

    }
}
