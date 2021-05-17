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

package org.glassfish.enterprise.iiop.api;

import org.glassfish.internal.grizzly.LazyServiceInitializer;

import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.glassfish.hk2.api.PostConstruct;

import org.omg.CORBA.ORB;

import javax.naming.NamingException;
import java.nio.channels.SelectableChannel;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import java.util.logging.Logger;



/**
 *
 * @author Ken Saks
 */
@Service
@Named("iiop-service")
public class ORBLazyServiceInitializer implements LazyServiceInitializer, PostConstruct {

static Logger logger = LogDomains.getLogger(ORBLazyServiceInitializer.class, LogDomains.SERVER_LOGGER);


    @Inject
    private GlassFishORBHelper orbHelper;

    boolean initializedSuccessfully = false;

    public void postConstruct() {
    }

    public String getServiceName() {

        return "iiop-service";
    }


    public boolean initializeService() {

        try {

            orbHelper.getORB();

            initializedSuccessfully = true;

            // TODO add check to ensure that lazy init is enabled for the orb
            // and throw exception if not

        } catch(Exception e) {
            logger.log(Level.WARNING, "ORB initialization failed in lazy init", e);
        }

        return initializedSuccessfully;
    }


    public void handleRequest(SelectableChannel channel) {
        if( initializedSuccessfully) {
            orbHelper.getSelectableChannelDelegate().handleRequest(channel);
        } else {
            logger.log(Level.WARNING, "Cannot handle SelectableChannel request in ORBLazyServiceInitializer." +
                    "ORB did not initialize successfully");
        }
    }




}
