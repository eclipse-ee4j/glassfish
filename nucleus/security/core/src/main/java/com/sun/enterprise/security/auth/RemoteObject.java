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

package com.sun.enterprise.security.auth;

import com.sun.enterprise.util.*;
import javax.rmi.PortableRemoteObject;
import javax.rmi.CORBA.Tie;
import org.omg.CORBA.ORB;

/**
 * Just a Base class to make exporting remote objects a bit easier...
 * @author Harish Prabandham
 */

public class RemoteObject {
    private ORB orb;

    protected RemoteObject() {
        //TODO:V3 commented, uncomment later orb = ORBManager.getORB();
    }

    protected void exportObject(java.rmi.Remote remote)
    throws java.rmi.RemoteException {
        // create servant and tie
        PortableRemoteObject.exportObject(remote);
        Tie servantsTie = javax.rmi.CORBA.Util.getTie(remote);

        // Note: at this point the Tie doesnt have a delegate inside it,
        // so it is not really "exported".
        // The following call does orb.connect() which is the real exporting
        servantsTie.orb(orb);
    }

    java.rmi.Remote getStub(java.rmi.Remote remote)
    throws java.rmi.RemoteException {
        return PortableRemoteObject.toStub(remote);
    }
}





