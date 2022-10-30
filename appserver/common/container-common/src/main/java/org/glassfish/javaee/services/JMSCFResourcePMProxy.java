/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.javaee.services;

import javax.naming.Context;
import javax.naming.NamingException;

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Service;

import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.deriveResourceName;
import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.getPMJndiName;

@Service
@PerLookup
public class JMSCFResourcePMProxy extends CommonResourceProxy {

    private static final long serialVersionUID = 1L;

    @Override
    public synchronized <T> T create(Context ic) throws NamingException {
        if (actualResourceName == null) {
            boolean wasDeployed = false;
            try {
                if (ic.lookup(desc.getJndiName().toString()) != null) {
                    wasDeployed = true;
                }
            } catch (NamingException ne) {}

            if (!wasDeployed) {
                try {
                    if (serviceLocator == null) {
                        serviceLocator = Globals.getDefaultHabitat();
                        if (serviceLocator == null) {
                            throw new NamingException("Unable to create resource " + "[" + desc.getJndiName()
                                + " ] as serviceLocator is null");
                        }
                    }
                    getResourceDeployer(desc).deployResource(desc);
                } catch (Exception e) {
                    NamingException ne = new NamingException("Unable to create resource [" + desc.getJndiName() + " ]");
                    ne.initCause(e);
                    throw ne;
                }
            }
            // append __PM suffix to jndi name
            actualResourceName = deriveResourceName(desc.getResourceId(), getPMJndiName(desc.getJndiName()),
                desc.getResourceType());
        }
        return (T) ic.lookup(actualResourceName.toString());
    }
}
