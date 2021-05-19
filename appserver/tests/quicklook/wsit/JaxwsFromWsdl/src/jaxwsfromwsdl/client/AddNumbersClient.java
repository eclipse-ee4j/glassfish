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

package jaxwsfromwsdl.client;

public class AddNumbersClient {

    public AddNumbersClient() {
    }

    public boolean testAddNumbers() {

        boolean status=false;
        AddNumbersPortType port = null;

        try {
            port = new AddNumbersService().getAddNumbersPort ();
            int number1 = 10;
            int number2 = 20;
            int number3 = 30;

            // System.out.printf ("Invoking addNumbers(%d, %d)\n", number1, number2);
            int result = port.addNumbers (number1, number2);
            // System.out.printf ("The result of adding %d and %d is %d.\n\n", number1, number2, result);
            if (result == number3)
          status = true;
        } catch(Exception ex){
            System.out.print("Got unexpected exception");
            // ex.printStackTrace();
    }
        return status;

    }

    public boolean testAddNumbersException() {

        boolean status=false;
        AddNumbersPortType port = null;

        try {
            port = new AddNumbersService().getAddNumbersPort ();
            int number1 = -10;
            int number2 = 20;

            // System.out.printf ("Invoking addNumbers(%d, %d)\n", number1, number2);
            int result = port.addNumbers (number1, number2);
            // System.out.printf ("The result of adding %d and %d is %d.\n\n", number1, number2, result);
        } catch (AddNumbersFault_Exception ex) {
        // System.out.print("Got expected exception");
          // System.out.printf ("Caught AddNumbersFault_Exception: %s\n", ex.getFaultInfo().getFaultInfo ());
            String info1 = ex.getFaultInfo().getFaultInfo();
        // System.out.print("info1="+info1+"---");
            String info2 = ex.getFaultInfo().getMessage();
        // System.out.print("info2="+info2+"---");
            if (info2.contains("Negative number cant be added!"))
        status = true;
    }

        return status;

    }

    public static void main (String[] args) {
        System.out.println("AddNumbersClient:main");
        AddNumbersClient client = new AddNumbersClient();
        boolean result = false;
        result = client.testAddNumbers();
        System.out.println("result1="+result);
        result = client.testAddNumbersException();
        System.out.println("result2="+result);
    }
}

