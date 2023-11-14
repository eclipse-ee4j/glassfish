/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejte.ccl.reporter;

public class ReporterClient{

    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]){
        if(args.length<1){
            usage();
        }
        echo(args[0]+" is the test name");

        String default_desc=args[0]+"_default_description";
        if(args.length>1 && !((args[1].trim()).equals(""))){
            echo(args[1]+" is the test description");
            default_desc = args[1];
        }
        int numTests = 1;
        if (args.length>=3) {
            numTests = Integer.parseInt(args[2]);
        }


        echo("adding description...");
        stat.addDescription(default_desc);
        echo("adding status...");
        if (numTests==1) {
                 stat.addStatus(args[0], SimpleReporterAdapter.DID_NOT_RUN);
        } else {
             for (int i=0;i<numTests; i++) {
                  stat.addStatus(args[0]+"-"+(i+1), SimpleReporterAdapter.DID_NOT_RUN);
             }
        }

        echo("printing summary...");
        stat.printSummary();
    }
    public static void usage(){
       String usg="Usage:"+
           "\tReporterClient <test name> [<test description>]"+
           "\tNote:Test description is not required but recommended";
       echo(usg);
    }
    public static void echo(String msg){
        System.out.println(msg);
    }
}
