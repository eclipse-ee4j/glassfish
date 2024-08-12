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

package com.sun.appserv.ejb;

import com.sun.logging.LogDomains;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.glassfish.persistence.ejb.entitybean.container.ReadOnlyBeanNotifierImpl;
import org.glassfish.persistence.ejb.entitybean.container.spi.ReadOnlyEJBHome;
import org.glassfish.persistence.ejb.entitybean.container.spi.ReadOnlyEJBLocalHome;

/**
 * Class that is used to obtain ReadOnlyBeanNotifier
 *  and ReadOnlyBeanLocalNotifier.
 *
 * @author Mahesh Kannan
 */
public class ReadOnlyBeanHelper {
    protected static final Logger _logger =
        LogDomains.getLogger(ReadOnlyBeanHelper.class, LogDomains.EJB_LOGGER);

    public static ReadOnlyBeanNotifier getReadOnlyBeanNotifier(String ejbName) {
        try {
            Context ctx = new InitialContext();
            Object obj = ctx.lookup(ejbName);
            ReadOnlyEJBHome home = (ReadOnlyEJBHome)
                    PortableRemoteObject.narrow(obj, ReadOnlyEJBHome.class);
            ReadOnlyBeanNotifier roNotifier = new ReadOnlyBeanNotifierImpl();
            roNotifier.setHome(home);
            return roNotifier;
        } catch (Exception ex) {
            if(_logger.isLoggable(Level.SEVERE)) {
                _logger.log(Level.SEVERE, "entitybean.container.remote_exception", ex);
            }
        }
        return null;
    }

    public static ReadOnlyBeanLocalNotifier getReadOnlyBeanLocalNotifier(
            String ejbName)
    {
        try {
            Context ctx = new InitialContext();
            ReadOnlyEJBLocalHome home =
                (ReadOnlyEJBLocalHome) ctx.lookup(ejbName);
            return home.getReadOnlyBeanLocalNotifier();
        } catch (Exception ex) {
            if(_logger.isLoggable(Level.SEVERE)) {
                _logger.log(Level.SEVERE, "entitybean.container.remote_exception",ex);
            }
        }
        return null;
    }

}

