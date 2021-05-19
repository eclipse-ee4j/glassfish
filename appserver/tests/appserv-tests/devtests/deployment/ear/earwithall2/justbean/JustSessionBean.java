/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package justbean;

import java.rmi.RemoteException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;

public class JustSessionBean implements SessionBean {

    private SessionContext ctx;

    public void
    ejbCreate()
        throws RemoteException
    {
        log("JustSessionBean.ejbCreate()...");
    }

    public void
    ejbRemove()
        throws RemoteException
    {
        log("JustSessionBean.ejbRemove()...");
    }

    public void
    log(String message)
    {
        Log.log(message);
    }

    public String[]
    findAllMarbles()
    {
        System.out.println("JustSessionBean.findAllMarbles()...");
        String[] strArray = new String[2];
        strArray[0] = "This is a test.";
        strArray[1] = "You have lost all your marbles.";
        return strArray;
    }


    /**
     * ejbDestroy - called by the Container before this bean is destroyed.
     */
    public void
    ejbDestroy()
    {
        log("JustSessionBean.ejbDestroy()...");
    }

    /**
     * ejbActivate - called by the Container after this bean instance
     * is activated from its passive state.
     */
    public void
    ejbActivate()
    {
        log("JustSessionBean.ejbActivate()...");
    }

    /**
     * ejbPassivate - called by the Container before this bean instance
     * is put in passive state.
     */
    public void
    ejbPassivate()
    {
        log("JustSessionBean.ejbPassivate()...");
    }

    /**
     * setSessionContext - called by the Container after creation of this
     * bean instance.
     */
    public void
    setSessionContext(SessionContext context)
    {
        log("JustSessionBean.setSessionContext(ctx)... ctx = " + ctx);
        ctx = context;
    }
}

