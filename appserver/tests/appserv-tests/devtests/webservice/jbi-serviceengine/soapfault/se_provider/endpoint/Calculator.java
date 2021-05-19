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

import jakarta.jws.WebService;
import jakarta.jws.WebMethod;

@WebService(
    name="Calculator",
    serviceName="CalculatorService",
    targetNamespace="http://example.com/Calculator"
)

public class Calculator {
    public Calculator() {}


    @WebMethod(operationName="throwRuntimeException", action="urn:ThrowRuntimeException")
    public String throwRuntimeException(String name) {
        String exceptionMsg = "Calculator :: Threw Runtime Exception";
        System.out.println(exceptionMsg);
        throw new RuntimeException(exceptionMsg);
    }

    @WebMethod(operationName="throwApplicationException", action="urn:ThrowApplicationException")
    public String throwApplicationException(String name) throws Exception {
        String exceptionMsg = "Calculator :: Threw Application Exception";
        System.out.println(exceptionMsg);
        throw new Exception(exceptionMsg);
    }
}
