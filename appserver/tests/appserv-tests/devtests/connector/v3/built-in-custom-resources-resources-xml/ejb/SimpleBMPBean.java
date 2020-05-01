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

package com.sun.s1asdev.jdbc.CustomResourceFactories.ejb;

import com.sun.s1asdev.custom.resource.CustomResourceJavaBean;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Set;
import java.util.Iterator;
import java.net.URL;


public class SimpleBMPBean
        implements EntityBean {

    protected Object obj;

    public void setEntityContext(EntityContext entityContext) {
    }

    public boolean testJavaBean(String testValue) {
        try {
            InitialContext ic = new InitialContext();
            CustomResourceJavaBean o = (CustomResourceJavaBean) ic.lookup("java:comp/env/custom/my-java-bean");
            if (o != null) {
                //System.out.println("Custom Resource : " + o);
                System.out.println("Custom resource value : " + o.getProperty());
                if (o.getProperty().equalsIgnoreCase(testValue)) {
                    return true;
                }else{
                   System.out.println("testJavaBean failed");
                }
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean testPrimitives(String type, String value, String resourceName) throws RemoteException {
        try {
            InitialContext ic = new InitialContext();
            if(!resourceName.startsWith("java:")){
                resourceName = "java:comp/env/"+resourceName;
            }
            Object o = ic.lookup(resourceName);
            if (o != null) {
                System.out.println("Custom resource value : " + o);
                if (o.toString().equalsIgnoreCase(value)) {
                    return true;
                }
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean testProperties(Properties properties, String resourceName) throws RemoteException {

        try {
            InitialContext ic = new InitialContext();
            Properties p = (Properties) ic.lookup(resourceName);

            Set keys = p.keySet();
            Iterator iterator = keys.iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                String value = (String) p.get(key);

                String result = (String) properties.get(key);
                if (result != null) {
                    if (!result.equalsIgnoreCase(value)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }

            return true;
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean testURL(String url, String resourceName) throws RemoteException {
        try {
            InitialContext ic = new InitialContext();
            URL boundURL = (URL) ic.lookup(resourceName);
            if (boundURL != null) {
                if (boundURL.toString().equals(url))
                    return true;
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    public void ejbLoad() {
    }

    public void ejbStore() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void unsetEntityContext() {
    }

    public void ejbPostCreate() {
    }
}
