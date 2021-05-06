/*
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

package endpoint;

import jakarta.jws.*;
import jakarta.jws.soap.SOAPBinding;

@WebService(
    name="Calculator",
    serviceName="CalculatorService",
    targetNamespace="http://example.com/Calculator"
)
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class Calculator {
        public Calculator() {}

        @WebMethod
        public int add(
                        @WebParam(name = "number1", targetNamespace = "http://example.com/Calculator", partName = "part1")
                        int j
                        ) throws Exception {
                                int i = 500;
                int k = i +j ;
                System.out.println("JBI Test :: bare-rpc-literal bc_consumer_se_provider : " + i + "+" + j +" = " + k);
                return k;
        }
}
