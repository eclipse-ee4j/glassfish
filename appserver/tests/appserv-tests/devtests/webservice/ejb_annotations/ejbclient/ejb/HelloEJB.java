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

package ejb;

import jakarta.ejb.Stateless;
import jakarta.xml.ws.WebServiceRef;

import endpoint.WebServiceEJBService;
import endpoint.WebServiceEJB;

@Stateless
public class HelloEJB implements Hello {


   @WebServiceRef
   WebServiceEJBService webService;

    public String invoke(String string) {
        System.out.println("invoked with " + string);
        System.out.println("getting the port now from " + webService);
        WebServiceEJB ejb = webService.getWebServiceEJBPort();
        System.out.println("got " + ejb);
        return ejb.sayHello(string);
   }
}
