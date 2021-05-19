/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
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

package helloservice;

import jakarta.ejb.*;
import java.rmi.RemoteException;

public class SayHelloEJB implements SessionBean {

        private SessionContext sc;

        public SayHelloEJB() {}

        public void ejbCreate() throws CreateException {}

        public String sayHello(String s) throws RemoteException {
                return "Hello EJB returns your Hello : " + s;
        }

        public void setSessionContext(SessionContext sc) {
                                this.sc = sc;
        }

        public void ejbRemove() throws RemoteException {}
        public void ejbActivate() {}
        public void ejbPassivate() {}
}
