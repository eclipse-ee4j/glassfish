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

/*
 * SimpleServerImpl.java
 *
 * Created on September 13, 2004, 11:24 AM
 */

package stubprops;

import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.EJBException;
import java.rmi.RemoteException;

/**
 *
 * @author dochez
 */
public class SimpleServerImpl implements SessionBean {

    SessionContext sc;

    /** Creates a new instance of SimpleServerImpl */
    public SimpleServerImpl() {
    }

    public void ejbCreate() throws RemoteException {
        System.out.println("In GoogleEJB::ejbCreate !!");
}

    public String sayHello(String who) throws RemoteException {
        return "hello" + who;
    }

    public void setSessionContext(SessionContext sc) {

        this.sc = sc;
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
