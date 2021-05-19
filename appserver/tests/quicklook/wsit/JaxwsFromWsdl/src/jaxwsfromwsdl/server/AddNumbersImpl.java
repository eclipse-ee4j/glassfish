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

package jaxwsfromwsdl.server;

@jakarta.jws.WebService (endpointInterface="jaxwsfromwsdl.server.AddNumbersPortType")
public class AddNumbersImpl{

    /**
     * @param number1
     * @param number2
     * @return The sum
     * @throws AddNumbersException
     *             if any of the numbers to be added is negative.
     */
    public int addNumbers (int number1, int number2)
    throws AddNumbersFault_Exception {
        if (number1 < 0 || number2 < 0) {
            String message = "Negative number cant be added!";
            String detail = "Numbers: " + number1 + ", " + number2;
            AddNumbersFault fault = new AddNumbersFault ();
            fault.setMessage (message);
            fault.setFaultInfo (detail);
            throw new AddNumbersFault_Exception (message, fault);
        }
        return number1 + number2;
    }

    /*
     * Simple one-way method that takes an integer.
     */
    public void oneWayInt(int number) {
        System.out.println("Service received: " + number);
    }

}
