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

package beans;

import javax.rmi.PortableRemoteObject;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.EJBException;
import jakarta.ejb.CreateException;
import java.util.Properties;
import java.util.Vector;
import java.sql.*;
import java.rmi.RemoteException;

import jakarta.transaction.UserTransaction;
import javax.naming.*;
import javax.sql.*;
//import com.sun.jdbcra.spi.JdbcSetupAdmin;

public class VersionCheckerEJB implements SessionBean {
        private InitialContext initContext = null;
        private SessionContext sessionContext = null;


    public VersionCheckerEJB() {
        debug("Constructor");
    }

    public void ejbCreate()
        throws CreateException {
        debug("ejbCreate()");
    }


    public int getVersion(){
            debug("Checking the version!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            try {
              initContext = new javax.naming.InitialContext();
            } catch (Exception e) {
              System.out.println("Exception occured when creating InitialContext: " + e.toString());
              e.printStackTrace();
              return -1;
            }

            try {
              com.sun.jdbcra.spi.JdbcSetupAdmin ja = (com.sun.jdbcra.spi.JdbcSetupAdmin) initContext.lookup("eis/testAdmin");
              int versionno =  ja.getVersion();
              debug("Version number is " + versionno);
              return versionno;
            } catch (Exception e) {
              e.printStackTrace();
              throw new RuntimeException(e.getMessage());
            }

           // return -1;
    }


    public void setSessionContext(SessionContext context) {
        debug(" bean removed");
        sessionContext = context;
    }

    public void ejbRemove() {
            debug(" bean removed");
    }

    public void ejbActivate() {
            debug(" bean activated");
    }

    public void ejbPassivate() {
            debug(" bean passivated");
    }


    private void debug(String msg) {
        System.out.println("[VersionCheckerEJB]:: -> " + msg);
    }
}
