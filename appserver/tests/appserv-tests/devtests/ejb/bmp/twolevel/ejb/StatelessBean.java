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

package com.sun.s1asdev.ejb.bmp.twolevel.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import javax.rmi.*;
import java.util.*;
import java.sql.*;

public class StatelessBean
    implements SessionBean
{

    private SessionContext  sessionContext;
    private SimpleBMPHome   bmpHome;

    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;

            Context context = null;
            try {
                context    = new InitialContext();
                Object objRef = context.lookup("java:comp/env/ejb/SimpleBMPHome");
                bmpHome = (SimpleBMPHome) PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);
            } catch (NamingException e) {
                throw new EJBException("cant find SimpleBMPHome");
            }
    }

    public void createBMP(Integer key)
        throws RemoteException
    {
        try {
            SimpleBMP bmp = bmpHome.create(key.intValue());
        } catch (Exception ex) {
            throw new RemoteException("Error while creating SimpleBMP: " + key);
        }
    }

    public void createBMPAndTest(Integer key)
        throws RemoteException
    {
        try {
            SimpleBMP bmp = bmpHome.create(key.intValue());
            bmp.foo();

            SimpleBMP bmp1 = bmpHome.findByPrimaryKey(key);
            bmp1.foo();
        } catch (Exception ex) {
            throw new RemoteException("Error while creating SimpleBMP: " + key);
        }
    }

    public void ejbCreate() {}

    public void ejbRemove() {}

    public void ejbActivate() {}

    public void ejbPassivate() {}

    public void unsetEntityContext() {}

}
